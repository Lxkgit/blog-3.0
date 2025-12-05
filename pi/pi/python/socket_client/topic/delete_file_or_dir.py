from logger.log_util import logger
import utils.file_util as file
import socket_client.domain.socket_msg as packet


# delete_file_or_dir topic 处理方法
async def topic_delete_file_or_dir(ws, receiveMsg):
    """处理删除文件或目录的请求"""
    logger.info(f"执行文件删除操作: {receiveMsg.get('data')}")
    data = receiveMsg.get('data', {})
    dirPath = data.get('dirPath', '')
    fileName = data.get('fileName')

    # 验证必要参数
    if not dirPath:
        deleteResult = "缺少必需的参数: dirPath"
    else:
        # 规范化路径（移除末尾斜杠）
        dirPath = dirPath.rstrip('/')

        if fileName:
            target_path = f"{dirPath}/{fileName}"
            logger.info(f"删除文件: {target_path}")
            deleteResult = file.delete_file_or_directory(target_path)
        else:
            logger.info(f"删除目录: {dirPath}")
            deleteResult = file.delete_file_or_directory(dirPath)

    # 构造响应消息
    msg = {
        'data': {
            'dirPath': dirPath,
            'fileName': fileName,
            'result': deleteResult
        }
    }
    logger.info(f"文件删除操作执行完成: {msg}")
    await ws.send(packet.build_socket_response(receiveMsg, msg))


