#!/bin/bash

PYTHON_BIN="/opt/python/bin/python"
PYTHON_SCRIPT="/opt/soft/socket/code/web_socket.py"

echo "=============================="
echo "$(date '+%Y-%m-%d %H:%M:%S'): 请求停止 Python 服务"

# 精准匹配当前 Socket 服务
PIDS=$(pgrep -f "$PYTHON_BIN $PYTHON_SCRIPT")

if [ -n "$PIDS" ]; then
    echo "$(date '+%Y-%m-%d %H:%M:%S'): 杀死进程: $PIDS"
    echo "$PIDS" | xargs -r kill -9
else
    echo "$(date '+%Y-%m-%d %H:%M:%S'): 未发现运行中的进程"
fi

echo "$(date '+%Y-%m-%d %H:%M:%S'): Socket 服务已停止，等待 watchdog 自动拉起"