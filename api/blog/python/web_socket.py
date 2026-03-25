import argparse
import asyncio

from logger.log_util import logger
from socket_client.socket_base import connect_with_retry

# 执行入口
if __name__ == "__main__":
    # 添加参数解析
    parser = argparse.ArgumentParser(description='WebSocket Client')
    parser.add_argument('--ip', type=str, default='localhost', help='WebSocket server IP address (default: localhost)')
    parser.add_argument('--port', type=int, default=60001, help='WebSocket server port (default: 60001)')
    parser.add_argument('--path', type=str, default='/file/socket/python/localhost',
                        help='WebSocket endpoint path (default: /file/socket/python/localhost)')
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
