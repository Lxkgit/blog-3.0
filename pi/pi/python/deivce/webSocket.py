import asyncio
import websockets
import platform
import psutil
import json
import time
import argparse
import logging
import shutil
import os
import subprocess
from pathlib import Path
from logging.handlers import RotatingFileHandler

# 配置常量
CONFIG = {
    "HEARTBEAT_INTERVAL": 30,  # 心跳间隔(秒)
    "SERVICE_INFO_INTERVAL": 60,  # 系统信息上报间隔(秒)
    "INITIAL_RETRY_DELAY": 5,  # 初始重试延迟(秒)
    "MAX_RETRY_DELAY": 60,  # 最大重试延迟(秒)
    "CONNECT_TIMEOUT": 15,  # 连接超时(秒)

}

SHELL_PATH = {

}

SHELL_CMD = {

}


def setup_logger(
        log_file='app.log',
        max_size=10,  # 单位：MB
        backup_count=5,
        log_level=logging.INFO,
        console_output=True):
    """
    配置带轮转功能的日志系统

    参数:
    log_file (str): 日志文件路径，默认 'app.log'
    max_size (int): 单个日志文件最大大小(MB)，默认10MB
    backup_count (int): 保留的旧日志文件数量，默认5个
    log_level (int): 日志级别，默认 logging.INFO
    console_output (bool): 是否同时在控制台输出日志，默认True
    """
    # 创建日志记录器
    global console_handler
    logger = logging.getLogger()
    logger.setLevel(log_level)

    # 清除现有处理器（避免重复添加）
    for handler in logger.handlers[:]:
        logger.removeHandler(handler)

    # 1. 创建文件处理器（带轮转功能）
    file_handler = RotatingFileHandler(
        filename=log_file,
        maxBytes=max_size * 1024 * 1024,  # 转换为字节
        backupCount=backup_count,
        encoding='utf-8'
    )

    # 2. 可选：创建控制台处理器
    if console_output:
        console_handler = logging.StreamHandler()
        console_handler.setLevel(log_level)

    # 创建日志格式
    formatter = logging.Formatter(
        '%(asctime)s | %(levelname)-8s | %(filename)s:%(lineno)d | %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )

    # 应用格式到处理器
    file_handler.setFormatter(formatter)
    if console_output:
        console_handler.setFormatter(formatter)

    # 添加处理器到日志记录器
    logger.addHandler(file_handler)
    if console_output:
        logger.addHandler(console_handler)

    # 返回配置好的logger（可选）
    return logger


logger = setup_logger(log_file='blog-python.log', max_size=20, backup_count=7, log_level=logging.INFO)


# socket 连接主方法
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
        await ws.send(json.dumps({'topic': 'register', 'data': 'service'}))

        # 创建任务
        heartbeat_task = asyncio.create_task(send_heartbeat(ws))
        service_task = asyncio.create_task(send_service_info(ws))
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


# 处理接收的消息
async def handle_messages(ws):
    async for message in ws:
        logger.info(f"收到消息: {message}")
        try:
            receiveMsg = json.loads(message)
            if receiveMsg.get("socketPacketType") == "request":
                if receiveMsg.get("topic") == "move_file":
                    logger.info(f"调用文件同步脚本: {receiveMsg.get('data')}")
                    fileNameList = receiveMsg.get('data').get('fileNameList')
                    sourceDirectory = receiveMsg.get('data').get("sourceDirectory")
                    targetDirectory = receiveMsg.get('data').get("targetDirectory")
                    count = receiveMsg.get('data').get("count")
                    if not fileNameList:
                        fileNameList = get_path_first_x_filename(sourceDirectory, count)
                    for filename in fileNameList:
                        move_file_or_directory(sourceDirectory + "/" + filename, targetDirectory)
                    # 执行完成响应socket
                    msg = {
                        "data": {
                            "type": receiveMsg.get('data').get("type"),
                            "data": receiveMsg.get('data').get("data"),
                            "servicePath": receiveMsg.get('data').get("servicePath"),
                            "fileNameList": fileNameList,
                            "targetDirectory": targetDirectory
                        }
                    }
                    logger.info(f"博客数据导出任务执行完成: {msg}")
                    await ws.send(json.dumps(build_msg(receiveMsg, msg)))
                elif receiveMsg.get("topic") == "delete_file_or_dir":
                    logger.info(f"执行文件删除操作: {receiveMsg.get('data')}")
                    dirPath = receiveMsg.get('data').get('dirPath')
                    fileName = receiveMsg.get('data').get('fileName')

                    # python 特色的三目运算符 [当条件为真时的值] if [条件] else [当条件为假时的值]
                    dirPath = dirPath[:-1] if dirPath.endswith('/') else dirPath

                    if fileName:
                        deleteResult = delete_file_or_directory(os.path.join(dirPath, fileName))
                    else:
                        deleteResult = delete_file_or_directory(dirPath)
                        # 执行完成响应socket
                    msg = {
                        'data': {
                            'dirPath': dirPath,
                            'fileName': fileName,
                            'result': deleteResult
                        }
                    }
                    logger.info(f"博客数据导出任务执行完成: {msg}")
                    await ws.send(json.dumps(build_msg(receiveMsg, msg)))

        except json.JSONDecodeError:
            logger.warning(f"无法解析的消息: {message}")

# socket 响应消息公共部分
def build_msg(receiveMsg, private_data: dict):
    # 公共部分
    common = {
        "requestId": receiveMsg.get("requestId"),
        "socketPacketType": "response",
        "topic": receiveMsg.get("topic"),
        "msgHead": receiveMsg.get("msgHead")
    }
    # 合并公共和私有
    msg = {**common, **private_data}
    return msg

# 执行Shell脚本
def execute_shell_script(shell_script_path):
    """同步执行Shell脚本并返回结果"""
    try:
        # 执行脚本并捕获输出
        result = subprocess.run(
            shell_script_path,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,  # 合并标准输出和错误输出
            text=True,
            check=False
        )
        logger.info(f"脚本执行完成，执行输出: {result.stdout}")
        logger.info(f"脚本执行完成，返回码: {result.returncode}")
        return {
            "success": result.returncode == 0,
            "output": result.stdout,
            "returncode": result.returncode
        }

    except Exception as e:
        logger.error(f"执行脚本错误: {str(e)}")
        return {
            "success": False,
            "output": str(e),
            "returncode": -1
        }


# 获取指定目录下前x文件的名称
def get_path_first_x_filename(directory, count):
    # 获取目录下所有条目
    all_entries = os.listdir(directory)

    # 过滤出文件并排序
    files = sorted(
        [entry for entry in all_entries if os.path.isfile(os.path.join(directory, entry))],
        key=lambda f: f.lower()  # 不区分大小写排序
    )

    # 返回前x个文件
    return files[:count]


# 文件或目录移动方法
def move_file_or_directory(source_path, destination_path):
    """
    移动文件或目录到指定目录（总是将destination_path视为目录）
    参数:
        source_path (str): 源文件/目录路径
        destination_path (str): 目标目录路径（总是作为目录处理）
    返回:
        tuple: (success: bool, message: str)
    """
    # 检查源路径是否存在
    if not os.path.exists(source_path):
        logger.info(f"错误：源路径 '{source_path}' 不存在")
        return False

    try:
        # 确保目标路径是目录格式（去除可能的尾部分隔符）
        destination_path = destination_path.rstrip(os.sep)

        # 创建目标目录（包括所有父目录）
        os.makedirs(destination_path, exist_ok=True)

        # 构建完整目标路径（目标目录 + 源文件名）
        final_path = os.path.join(destination_path, os.path.basename(source_path))

        # 执行移动操作
        shutil.move(source_path, final_path)

        logger.info(f"成功移动 '{source_path}' 到目录 '{destination_path}'")
        return True

    except PermissionError:
        return False, f"权限错误：无法移动 '{source_path}'，请检查文件权限"

    except FileNotFoundError as exception:
        return False, f"路径错误：{str(exception)}"

    except Exception as exception:
        return False, f"移动失败：{str(exception)}"


# 修改文件名称
def rename_file(file_path, new_name):
    # 检查文件是否存在
    if not os.path.isfile(file_path):
        print(f"错误：文件 '{file_path}' 不存在")
        return

    # 提取目录路径和原后缀名
    directory = os.path.dirname(file_path)
    _, old_extension = os.path.splitext(file_path)

    # 构建新文件路径（保持原后缀）
    new_file_path = os.path.join(directory, f"{new_name}{old_extension}")

    # 执行重命名操作
    try:
        os.rename(file_path, new_file_path)
        print(f"文件已重命名为: {os.path.basename(new_file_path)}")
    except Exception as e:
        print(f"重命名失败: {str(e)}")


# 删除目录或文件
def delete_file_or_directory(path):
    """
    安全删除文件或目录（包含所有内容）
    参数:
        path (str): 要删除的文件或目录路径
    返回:
        bool: 删除成功返回True，否则返回False
    """
    try:
        # 转换为绝对路径
        target = Path(path).resolve()

        # 基本安全校验
        if not target.exists():
            return False

        # 关键目录保护（防止误删系统文件）
        protected_paths = [
            Path("/"),
            Path.home(),
            Path("/etc"),
            Path("/bin"),
            Path("/usr"),
            Path("/var"),
            Path("/lib")
        ]

        # 检查是否尝试删除受保护路径
        if any(target == p or target.is_relative_to(p) for p in protected_paths):
            return False

        logger.info(f"删除目录或文件：{path}")
        # 执行删除操作
        if target.is_file():
            os.remove(target)
        elif target.is_dir():
            shutil.rmtree(target)
        else:
            return False  # 跳过特殊文件类型

        return True

    except Exception:
        return False


# socket 连接心跳
async def send_heartbeat(websocket):
    logger.info("启动心跳任务")
    try:
        while True:
            try:
                msg = {
                    'topic': 'heart',
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


# 服务器设备状态数据上报方法
async def send_service_info(websocket):
    logger.info("启动服务器数据监测任务")

    # 缓存上次采集的磁盘信息
    last_disk_info = None
    last_cpu_info = None

    try:
        while True:
            try:
                # 获取当前时间
                start_time = time.monotonic()

                # 获取系统配置
                config = get_computer_config(last_disk_info, last_cpu_info)

                # 更新缓存
                last_disk_info = config["disks"]
                last_cpu_info = config["cpu"]

                # 发送消息
                msg = {
                    'topic': 'system',
                    'data': config
                }
                await websocket.send(json.dumps(msg))

                # 计算实际耗时并调整等待时间
                elapsed = time.monotonic() - start_time
                wait_time = max(1, CONFIG["SERVICE_INFO_INTERVAL"] - elapsed)
                await asyncio.sleep(wait_time)

            except websockets.ConnectionClosed:
                logger.info("数据监测任务: 连接已关闭")
                break
    except asyncio.CancelledError:
        logger.info("数据监测任务被取消")
    except Exception as exception:
        logger.error(f"数据监测任务错误: {str(exception)}")


# 获取服务器状态数据方法（优化版本）
def get_computer_config(prev_disks=None, prev_cpu=None):
    # 获取操作系统信息
    os_info = platform.uname()

    # 获取CPU信息 - 使用缓存减少计算
    if not prev_cpu:
        cpu_count = psutil.cpu_count(logical=False)
        cpu_percent_per_core = psutil.cpu_percent(interval=0.1, percpu=True)
        cpu_percent_total = psutil.cpu_percent(interval=0.1)
    else:
        cpu_count = prev_cpu["physical_cores"]
        cpu_percent_per_core = psutil.cpu_percent(interval=0.1, percpu=True)
        cpu_percent_total = psutil.cpu_percent(interval=0.1)

    # 获取内存信息
    virtual_memory = psutil.virtual_memory()

    # 获取磁盘信息 - 使用缓存减少IO
    if not prev_disks:
        disk_partitions = get_disk_info()
    else:
        # 每分钟刷新一次磁盘信息
        disk_partitions = prev_disks if time.time() % 60 > 10 else get_disk_info()

    # 整理配置信息
    return {
        'system': os_info.system,
        'node_name': os_info.node,
        'machine': os_info.machine,
        'cpu': {
            'physical_cores': cpu_count,
            'usage_per_core': cpu_percent_per_core,
            'total_usage': cpu_percent_total
        },
        'memory': {
            'total': virtual_memory.total,
            'available': virtual_memory.available,
            'used': virtual_memory.used,
            'percent': virtual_memory.percent
        },
        'disks': disk_partitions,
        'timestamp': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
    }


# 获取磁盘信息（独立函数）
def get_disk_info():
    disk_partitions = []
    for partition in psutil.disk_partitions():
        try:
            # 跳过特殊文件系统
            if partition.fstype in ['tmpfs', 'squashfs', 'overlay']:
                continue

            usage = psutil.disk_usage(partition.mountpoint)
            disk_partitions.append({
                'device': partition.device,
                'mountpoint': partition.mountpoint,
                'fstype': partition.fstype,
                'opts': partition.opts,
                'usage': {
                    'total': usage.total,
                    'used': usage.used,
                    'free': usage.free,
                    'percent': usage.percent
                }
            })
        except PermissionError:
            logger.warning(f"权限不足无法访问: {partition.mountpoint}")
        except FileNotFoundError:
            logger.warning(f"挂载点不存在: {partition.mountpoint}")
        except Exception as exception:
            logger.error(f"获取磁盘信息错误: {partition.mountpoint} - {str(exception)}")

    return disk_partitions


# 执行入口
if __name__ == "__main__":
    # 添加参数解析
    parser = argparse.ArgumentParser(description='WebSocket Client')
    parser.add_argument('--ip', type=str, default='localhost',
                        help='WebSocket server IP address (default: localhost)')
    parser.add_argument('--port', type=int, default=10201,
                        help='WebSocket server port (default: 10201)')
    parser.add_argument('--path', type=str, default='/socket/python/localhost',
                        help='WebSocket endpoint path (default: /socket/python/localhost)')
    args = parser.parse_args()

    # 构建WebSocket URL
    webSocketUrl = f"ws://{args.ip}:{args.port}{args.path}"
    logger.info(f"连接URL: {webSocketUrl}")

    try:
        asyncio.run(connect_with_retry(webSocketUrl))
    except KeyboardInterrupt:
        logger.info("程序被用户中断")
    except Exception as exception:
        logger.exception(f"程序发生未处理异常: {str(exception)}")
