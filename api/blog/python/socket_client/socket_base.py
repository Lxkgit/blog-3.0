import websockets
import asyncio
import json
import time
import socket_client.domain.socket_msg as packet

from socket_client.device.device_info import send_device_info
from logger.log_util import logger
from socket_client.topic.move_file import topic_move_file
from socket_client.topic.export_blog_file import topic_export_blog_file
from socket_client.topic.delete_file_or_dir import topic_delete_file_or_dir
from config.constant import CONFIG


# socket_client 连接主方法
async def connect_with_retry(url):
    retry_delay = CONFIG["INITIAL_RETRY_DELAY"]

    while True:
        try:
            logger.info(f"尝试连接到: {url}")
            async with websockets.connect(
                    url,
                    ping_interval=CONFIG["HEARTBEAT_INTERVAL"],
                    ping_timeout=CONFIG["HEARTBEAT_INTERVAL"] // 2,
                    open_timeout=CONFIG["CONNECT_TIMEOUT"]
            ) as ws:
                logger.info("WebSocket 连接成功")
                await handle_connection(ws)

                # 重置重试延迟
                retry_delay = CONFIG["INITIAL_RETRY_DELAY"]

        except (websockets.ConnectionClosedError, ConnectionRefusedError) as exception:
            logger.warning(f"连接断开: {exception} {retry_delay}秒后重试...")
            await asyncio.sleep(retry_delay)
            retry_delay = min(retry_delay * 2, CONFIG["MAX_RETRY_DELAY"])

        except websockets.InvalidURI as exception:
            logger.error(f"无效的URL: {url} - {exception}")
            break

        except asyncio.TimeoutError:
            logger.warning(f"连接超时，{retry_delay}秒后重试...")
            await asyncio.sleep(retry_delay)
            retry_delay = min(retry_delay * 2, CONFIG["MAX_RETRY_DELAY"])

        except Exception as exception:
            logger.error(f"连接错误: {type(exception).__name__}: {str(exception)} {retry_delay}秒后重试...")
            await asyncio.sleep(retry_delay)
            retry_delay = min(retry_delay * 2, CONFIG["MAX_RETRY_DELAY"])


# 处理WebSocket连接
async def handle_connection(ws):
    tasks = []
    try:
        # 发送注册消息
        await ws.send(json.dumps({'topic': 'register', 'data': 'device'}))

        # 创建任务
        heartbeat_task = asyncio.create_task(send_heartbeat(ws))
        service_task = asyncio.create_task(send_device_info(ws))
        message_task = asyncio.create_task(handle_messages(ws))
        tasks = [heartbeat_task, service_task, message_task]

        # 等待所有任务完成
        await asyncio.gather(*tasks)

    except asyncio.CancelledError:
        logger.info("连接处理被取消")
    except Exception as exception:
        logger.error(f"连接处理错误: {type(exception).__name__}: {str(exception)}")
    finally:
        # 取消所有任务
        for task in tasks:
            if not task.done():
                task.cancel()
        # 等待所有任务取消完成
        await asyncio.gather(*tasks, return_exceptions=True)


# socket_client 连接心跳
async def send_heartbeat(websocket):
    logger.info("启动心跳任务")
    try:
        while True:
            try:
                msg = {
                    'socketPacketType': 'heartbeat',
                    'topic': 'heartbeat',
                    'data': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
                }
                await websocket.send(json.dumps(msg))
                await asyncio.sleep(CONFIG["HEARTBEAT_INTERVAL"])

            except websockets.ConnectionClosed:
                logger.info("心跳任务: 连接已关闭")
                break
    except asyncio.CancelledError:
        logger.info("心跳任务被取消")
    except Exception as exception:
        logger.error(f"心跳任务错误: {str(exception)}")


# 处理接收的消息
async def handle_messages(ws):
    async for message in ws:
        logger.info(f"收到消息: {message}")
        try:
            receiveMsg = packet.get_socket_packet(message)
            if receiveMsg.get("socketPacketType") == "request":
                topic = receiveMsg.get("topic")
                if topic == "move_file":
                    await topic_move_file(ws, receiveMsg)
                elif topic == "export_blog_file":
                    await topic_export_blog_file(ws, receiveMsg)
                elif topic == "delete_file_or_dir":
                    await topic_delete_file_or_dir(ws, receiveMsg)
        except json.JSONDecodeError:
            logger.warning(f"无法解析的消息: {message}")
