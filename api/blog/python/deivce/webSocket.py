import asyncio
import websockets
import platform
import psutil
import json
import time

service_info_delay = 60


async def send_heartbeat(websocket):
    print("启动心跳任务")
    await websocket.send("{'topic': 'register', 'message': 'smp_service'}")
    while True:
        try:
            msg = {
                'topic': 'heart',
                'message': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
            }
            await websocket.send(json.dumps(msg))
            await asyncio.sleep(60)
        except websockets.ConnectionClosed:
            break  # 连接断开时退出循环


async def send_service_info(websocket):
    print("启动服务器数据监测任务")
    while True:
        try:
            msg = {
                'topic': 'system',
                'message': get_computer_config()
            }
            await websocket.send(json.dumps(msg))
            await asyncio.sleep(60)
        except websockets.ConnectionClosed:
            break  # 连接断开时退出循环


def get_computer_config():
    # 获取操作系统信息
    # system: 操作系统名称（如 Linux, Windows, Darwin）
    # node_name: 网络主机名（等同于命令行 hostname）
    # machine: 硬件架构（如 x86_64, ARM64）
    os_info = platform.uname()
    system = os_info.system
    node_name = os_info.node
    machine = os_info.machine

    # 获取CPU信息
    # 获取逻辑 CPU 核心数
    cpu_count = psutil.cpu_count(logical=False)
    # 所有逻辑核心的占用率列表（每个元素为对应核心的百分比）
    cpu_freq = psutil.cpu_percent(interval=0.1, percpu=True)
    # 所有逻辑核心的平均占用率（总占用率）
    cpu_percent_total = psutil.cpu_percent(interval=0.1)

    # 获取内存信息
    # total_memory: 物理内存总量（字节）
    # available_memory: 可用内存（不同系统计算方式不同，Linux 包括缓存/缓冲区）
    virtual_memory = psutil.virtual_memory()
    total_memory = virtual_memory.total
    available_memory = virtual_memory.available

    # 获取磁盘信息
    # 获取所有磁盘分区的列表（如设备名、挂载点、文件系统类型）
    disk_info = psutil.disk_partitions()
    # 获取根目录所在分区的磁盘使用情况（总容量、已用、可用空间）
    disk_usage = psutil.disk_usage('/')

    # 整理配置信息
    config = {
        'system': system,
        'node_name': node_name,
        'machine': machine,
        'cpu_count': cpu_count,
        'cpu_freq': cpu_freq,
        'cpu_percent_total': cpu_percent_total,
        'total_memory': total_memory,
        'available_memory': available_memory,
        'disk_partitions': disk_info,
        'disk_usage': disk_usage,
    }

    return config


async def connect_with_retry(url):
    delay = 60
    while True:
        try:
            async with websockets.connect(url) as ws:
                print("socket 连接成功")
                # 启动心跳任务
                asyncio.create_task(send_heartbeat(ws))
                # 启动服务器数据监测任务
                asyncio.create_task(send_service_info(ws))
                # 接收消息
                async for message in ws:
                    print(f"收到消息: {message}")
                return
        except (websockets.ConnectionClosedError, ConnectionRefusedError) as e:
            print(f"连接断开: {e} {delay}秒后重试...")
            await asyncio.sleep(delay)


# 执行入口
if __name__ == "__main__":
    webSocketUrl = "ws://localhost:60001/file/python"
    asyncio.run(connect_with_retry(webSocketUrl))
