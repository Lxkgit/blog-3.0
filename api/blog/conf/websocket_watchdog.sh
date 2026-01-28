#!/bin/bash

# 守护进程日志文件
WATCHDOG_LOG="/opt/docker/files/python/watchdog.log"
# Python脚本路径
PYTHON_SCRIPT="/opt/docker/files/python/code/web_socket.py"

# Conda 基础路径和环境名称
CONDA_BASE="/root/anaconda3"
CONDA_ENV="py3"
# Conda 环境 Python 路径
PYTHON_BIN="$CONDA_BASE/envs/$CONDA_ENV/bin/python"

# 启动参数
SCRIPT_ARGS="--ip 172.18.0.13"

# 启动Python脚本的函数
start_script() {
    echo "$(date): 启动Python脚本..." >> "$WATCHDOG_LOG"
    $PYTHON_BIN "$PYTHON_SCRIPT" $SCRIPT_ARGS > python.log 2>&1 &
    echo "$(date): Python脚本已启动 (PID: $!)" >> "$WATCHDOG_LOG"
}

# 检查Python脚本是否在运行
check_script() {
    pgrep -f "python $PYTHON_SCRIPT $SCRIPT_ARGS" >/dev/null
    return $?
}

# 主循环
while true; do
    if ! check_script; then
        echo "$(date): Python脚本未运行，重新启动中..." >> "$WATCHDOG_LOG"
        start_script
    fi
    sleep 10
done
