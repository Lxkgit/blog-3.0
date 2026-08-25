# 配置常量
CONFIG = {
    "HEARTBEAT_INTERVAL": 30,  # 心跳间隔(秒)
    "SERVICE_INFO_INTERVAL": 300,  # 系统信息上报间隔(秒)
    "INITIAL_RETRY_DELAY": 5,  # 初始重试延迟(秒)
    "MAX_RETRY_DELAY": 60,  # 最大重试延迟(秒)
    "CONNECT_TIMEOUT": 15,  # 连接超时(秒)
}

# socket服务器环境目录
DIR_PATH = "/opt/soft/socket/code"

# shell 脚本绝对路径
SHELL_PATH = {
    "EXPORT_BLOG_FILE": DIR_PATH + "/shell/exportBlogFile.sh",
    "EXPORT_PI_FILE": DIR_PATH + "/shell/exportPiFile.sh",
}
