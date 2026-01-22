from logger.log_util import logger
import socket_client.domain.socket_msg as packet
import utils.file_util as file


# export_blog_file topic 处理方法
async def topic_move_file(ws, receiveMsg):
    logger.info(f"调用文件同步脚本: {receiveMsg.get('data')}")
    fileNameList = receiveMsg.get('data').get('fileNameList')
    fileSource = receiveMsg.get('data').get('fileSource')
    sourceDirectory = receiveMsg.get('data').get("sourceDirectory")
    targetDirectory = receiveMsg.get('data').get("targetDirectory")
    count = receiveMsg.get('data').get("count")
    if not fileNameList:
        fileNameList = file.get_path_first_x_filename(sourceDirectory, count)
    for filename in fileNameList:
        if fileSource == 1:
            file.copy_file_or_directory(sourceDirectory + "/" + filename, targetDirectory)
        if fileSource == 2:
            file.move_file_or_directory(sourceDirectory + "/" + filename, targetDirectory)
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
    await ws.send(packet.build_socket_response(receiveMsg, msg))
