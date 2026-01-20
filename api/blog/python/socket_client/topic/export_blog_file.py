from logger.log_util import logger
import socket_client.domain.socket_msg as packet
import utils.shell_util as shell
import utils.file_util as file
from config.constant import SHELL_PATH
import time


# export_blog_file topic 处理方法
async def topic_export_blog_file(ws, receiveMsg):
    logger.info("开始执行 博客数据导出 任务")
    start_time = time.perf_counter()
    try:
        blogFilePath = receiveMsg.get('data').get('blogFilePath')
        logger.info(f"博客数据导出位置: {blogFilePath}")
        # 导出博客文件数据
        shell.execute_shell_script(SHELL_PATH["EXPORT_BLOG_FILE"])
        # 文件移动到ftp目录中 ftp目录由服务器指定
        file.move_file_or_directory("/opt/docker/files/temp/blog/blog.zip", blogFilePath)
        # 删除临时文件
#         file.delete_file_or_directory("/opt/docker/files/temp/blog")
        # 执行完成响应socket
        msg = {
            'data': {
                'blogFilePath': blogFilePath,
                'blogFileName': 'blog.zip'
            }
        }
        logger.info(f"博客数据导出任务执行完成: {msg}")
        await ws.send(packet.build_socket_response(receiveMsg, msg))
    finally:
        cost = (time.perf_counter() - start_time)
        logger.info("博客数据导出 完成，耗时 %.2f s", cost)
