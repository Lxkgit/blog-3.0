import logging
from logging.handlers import RotatingFileHandler


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


# 全局单例
logger = setup_logger(log_file='blog-python.log', max_size=20, backup_count=7, log_level=logging.INFO)
