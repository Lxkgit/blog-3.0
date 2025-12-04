from logger.log_util import logger
import socket_client.domain.socket_msg as packet


# export_blog_file topic 处理方法
async def topic_move_file(ws, receiveMsg):
    logger.info(f"调用文件同步脚本: {receiveMsg.get('data')}")
    # await execute_shell_script(ws, SHELL_PATH["EXPORT_SCRIPT"], receiveMsg)
    msg = {
        "data": {
            "fileResult": receiveMsg.get('data'),
            "sqlResult": "sqlResult"
        }
    }
    logger.info(f"文件移动完成: {msg}")
    await ws.send(packet.build_socket_response(receiveMsg, msg))
