from logger.log_util import logger
import utils.file_util as file
import socket_client.domain.socket_msg as packet


# export_blog_file topic 处理方法
async def topic_move_file(ws, receiveMsg):
    logger.info(f"调用文件同步脚本: {receiveMsg.get('data')}")
    file.move_file_or_directory("/opt/docker/files/temp/blog/blog.zip", blogFilePath)
    msg = {
        "data": {
            "fileResult": receiveMsg.get('data'),
            "sqlResult": "sqlResult"
        }
    }
    logger.info(f"文件移动完成: {msg}")
    await ws.send(packet.build_socket_response(receiveMsg, msg))
