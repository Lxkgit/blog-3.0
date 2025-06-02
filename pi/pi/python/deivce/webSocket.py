import asyncio
import websockets
import platform
import psutil
import json
import time
import os
import shutil
import random

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


def randomly_move_files(source_dir, target_dir, count):
    # 确保源目录存在
    if not os.path.exists(source_dir):
        raise FileNotFoundError(f"源目录 '{source_dir}' 不存在")

    # 创建目标目录（如果不存在）
    os.makedirs(target_dir, exist_ok=True)

    # 获取目录中的所有文件（排除子目录）
    all_files = [f for f in os.listdir(source_dir)
                 if os.path.isfile(os.path.join(source_dir, f))]

    # 按文件名排序并取前 count 个
    sorted_files = sorted(all_files)
    file_list = sorted_files[:count]

    if not file_list:
        print("源目录中没有文件可移动")
        return

    # 随机打乱文件顺序
    random.shuffle(file_list)

    # 移动文件
    moved_files = []
    for filename in file_list:
        src_path = os.path.join(source_dir, filename)
        dst_path = os.path.join(target_dir, filename)

        # 处理文件名冲突（添加随机后缀）
        while os.path.exists(dst_path):
            name, ext = os.path.splitext(filename)
            new_name = f"{name}_{random.randint(1, 1000)}{ext}"
            dst_path = os.path.join(target_dir, new_name)

        shutil.move(src_path, dst_path)
        moved_files.append(os.path.basename(dst_path))

    # 打印结果
    print(f"已移动 {len(moved_files)} 个文件到 {target_dir}:")
    for f in moved_files:
        print(f"  - {f}")
    return moved_files


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
                    receiveMsg = json.loads(message)
                    if receiveMsg.get("topic") == "move_file":
                        print(f"调用文件同步脚本: {receiveMsg.get('message')}")
                        source_directory = receiveMsg.get('sourceDirectory')
                        target_directory = receiveMsg.get('targetDirectory')
                        count = receiveMsg.get('count')
                        moved_files = randomly_move_files(source_directory, target_directory, count)
                        msg = {
                            'topic': receiveMsg.get("topic"),
                            'message': {
                                "requestId": receiveMsg.get("requestId"),
                                "sourceDirectory": source_directory,
                                "targetDirectory": target_directory,
                                "file_list": moved_files
                            }
                        }
                        await ws.send(json.dumps(msg))

                return
        except (websockets.ConnectionClosedError, ConnectionRefusedError) as e:
            print(f"连接断开: {e} {delay}秒后重试...")
            await asyncio.sleep(delay)


# 执行入口
if __name__ == "__main__":
    webSocketUrl = "ws://localhost:10201/python"
    asyncio.run(connect_with_retry(webSocketUrl))
