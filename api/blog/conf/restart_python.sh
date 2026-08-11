#!/bin/bash

PYTHON_SCRIPT="/opt/docker/files/socket/code/web_socket.py"
SCRIPT_ARGS="--ip 172.18.0.21"
RESTART_LOG="/opt/docker/files/socket/restart.log"

# venv Python 绝对路径（必须指定）
PYTHON_BIN="/opt/python/bin/python"

# 格式化执行脚本异常字符并授权
chmod +x /opt/docker/files/socket/code/web_socket.py
sed -i 's/\r$//' /opt/docker/files/socket/code/web_socket.py
chmod +x /opt/docker/files/socket/code/shell/*.sh
sed -i 's/\r$//' /opt/docker/files/socket/code/shell/*.sh

echo "==============================" >> "$RESTART_LOG"
echo "$(date): 请求重启 Python 服务" >> "$RESTART_LOG"

# 精准匹配虚拟环境 Python 的进程
PIDS=$(pgrep -f "$PYTHON_BIN $PYTHON_SCRIPT $SCRIPT_ARGS")

if [ -n "$PIDS" ]; then
    echo "$(date): 杀死进程: $PIDS" >> "$RESTART_LOG"
    echo "$PIDS" | xargs kill -9
else
    echo "$(date): 未发现运行中的进程" >> "$RESTART_LOG"
fi

echo "$(date): 已请求重启，等待 watchdog 拉起" >> "$RESTART_LOG"
