import websockets
import platform
import psutil
import asyncio
import json
import time
from logger.log_util import logger
from config.constant import CONFIG


# 服务器设备状态数据上报方法
async def send_device_info(websocket):
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
                    'socketPacketType': 'request',
                    'topic': 'system_info',
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
