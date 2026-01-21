#!/bin/bash

PYTHON_SCRIPT="/opt/docker/files/python/code/web_socket.py"
SCRIPT_ARGS="--ip 172.18.0.13"
RESTART_LOG="/opt/docker/files/python/restart.log"

# 删除 code 目录下除 python.zip 之外的全部文件
find /opt/docker/files/python/code -mindepth 1 ! -name 'python.zip' -exec rm -rf {} +

# 解压新版本代码压缩包
unzip /opt/docker/files/python/code/python.zip -d /opt/docker/files/python/code

# 格式化执行脚本异常字符并授权
chmod +x /opt/docker/files/python/code/web_socket.py
sed -i 's/\r$//' /opt/docker/files/python/code/web_socket.py
chmod +x /opt/docker/files/python/code/shell/*.sh
sed -i 's/\r$//' /opt/docker/files/python/code/shell/*.sh

rm -rf /opt/docker/files/python/code/python.zip

echo "==============================" >> "$RESTART_LOG"
echo "$(date): 请求重启 Python 服务" >> "$RESTART_LOG"

PIDS=$(pgrep -f "python $PYTHON_SCRIPT $SCRIPT_ARGS")

if [ -n "$PIDS" ]; then
    echo "$(date): 杀死进程: $PIDS" >> "$RESTART_LOG"
    echo "$PIDS" | xargs kill -9
else
    echo "$(date): 未发现运行中的进程" >> "$RESTART_LOG"
fi

echo "$(date): 已请求重启，等待 watchdog 拉起" >> "$RESTART_LOG"
