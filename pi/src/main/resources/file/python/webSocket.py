import asyncio
import websockets
import platform
import psutil
import json


async def send_heartbeat(websocket):
    """心跳包维持连接"""
    while True:
        try:
            msg = {
                'topic': 'heart',
                'message': get_computer_config()
            }
            await websocket.send(json.dumps(msg))
            await asyncio.sleep(60)
        except websockets.ConnectionClosed:
            break  # 连接断开时退出循环


def get_computer_config():
    # 获取操作系统信息
    os_info = platform.uname()
    system = os_info.system
    node_name = os_info.node
    release = os_info.release
    version = os_info.version
    machine = os_info.machine
    processor = os_info.processor

    # 获取CPU信息
    cpu_count = psutil.cpu_count()
    cpu_freq = psutil.cpu_percent()
    cpu_percent_total = psutil.cpu_percent()

    # 获取内存信息
    virtual_memory = psutil.virtual_memory()
    total_memory = virtual_memory.total
    available_memory = virtual_memory.available

    # 获取磁盘信息
    disk_info = psutil.disk_partitions()
    disk_usage = psutil.disk_usage('/')

    # 整理配置信息
    config = {
        'system': system,
        'node_name': node_name,
        'release': release,
        'version': version,
        'machine': machine,
        'processor': processor,
        'cpu_count': cpu_count,
        'cpu_freq': cpu_freq,
        'cpu_percent_total': cpu_percent_total,
        'total_memory': total_memory / 1024 / 1024 / 1024,
        'available_memory': available_memory / 1024 / 1024 / 1024,
        'disk_partitions': disk_info,
        'disk_usage': disk_usage,
    }

    return config


async def connect_with_retry(url):
    delay = 60
    while True:
        try:
            async with websockets.connect(url) as ws:
                print("连接成功！")
                # 启动心跳任务
                asyncio.create_task(send_heartbeat(ws))
                # 接收消息
                async for message in ws:
                    print(f"收到消息: {message}")
                return
        except (websockets.ConnectionClosedError, ConnectionRefusedError) as e:
            print(f"连接断开: {e} {delay}秒后重试...")
            await asyncio.sleep(delay)


# 执行入口
if __name__ == "__main__":
    webSocketUrl = "ws://localhost:10201/python"
    asyncio.run(connect_with_retry(webSocketUrl))
