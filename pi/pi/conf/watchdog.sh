#!/bin/bash

WATCHDOG_LOG="/opt/docker/watchdog/watchdog.log"

VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"

LOG_DIR="/opt/docker/watchdog/logs"
PID_DIR="/opt/docker/watchdog/pids"

mkdir -p "$LOG_DIR"
mkdir -p "$PID_DIR"

# ==========================================================
# 任务定义
# name    : 日志、PID 文件名称
# match   : 用于校验进程是否正确（cmdline 必须包含）
# command : 启动命令
# ==========================================================

declare -A WEB_SOCKET=(
    [name]="web_socket"
    [match]="/opt/docker/files/python/code/web_socket.py"
    [command]="$PYTHON_BIN /opt/docker/files/python/code/web_socket.py --ip 172.18.0.5 --port 10201 --path /socket/python/localhost"
)

declare -A CAMERA=(
    [name]="camera"
    [match]="/opt/docker/camera/startCSI.sh"
    [command]="bash /opt/docker/camera/startCSI.sh"
)

# ==========================================================
# 任务列表
# ==========================================================

TASKS=(
    WEB_SOCKET
    CAMERA
)

# ==========================================================
# 启动任务
# ==========================================================

start_task() {

    declare -n task=$1

    local name="${task[name]}"
    local command="${task[command]}"
    local pid_file="$PID_DIR/${name}.pid"

    # 避免重复启动
    if check_task "$1"; then
        return
    fi

    echo "$(date): [START] $command" >> "$WATCHDOG_LOG"
    nohup bash -c "exec $command" > "$LOG_DIR/${name}.log" 2>&1 &
    local pid=$!
    echo "$pid" > "$pid_file"
    echo "$(date): [PID $pid] $name started." >> "$WATCHDOG_LOG"
}

# ==========================================================
# 检查任务
# ==========================================================

check_task() {

    declare -n task=$1

    local name="${task[name]}"
    local match="${task[match]}"
    local pid_file="$PID_DIR/${name}.pid"

    # PID文件不存在
    [[ ! -f "$pid_file" ]] && return 1

    local pid
    pid=$(cat "$pid_file")

    # PID不存在
    if ! kill -0 "$pid" >/dev/null 2>&1; then
        rm -f "$pid_file"
        return 1
    fi

    # 校验命令行，防止PID被系统复用
    local cmdline
    cmdline=$(ps -p "$pid" -o args=)

    if [[ "$cmdline" == *"$match"* ]]; then
        return 0
    fi

    echo "$(date): [PID REUSED] pid=$pid cmd=$cmdline" >> "$WATCHDOG_LOG"

    rm -f "$pid_file"

    return 1
}

# ==========================================================
# 退出处理
# ==========================================================

cleanup() {
    echo "$(date): Watchdog stopped." >> "$WATCHDOG_LOG"
    exit 0
}

trap cleanup SIGINT SIGTERM

# ==========================================================
# 主循环
# ==========================================================

echo "$(date): Watchdog started." >> "$WATCHDOG_LOG"

while true; do
    for task in "${TASKS[@]}"; do
        if ! check_task "$task"; then
            declare -n t=$task
            echo "$(date): [RESTART] ${t[name]}" >> "$WATCHDOG_LOG"
            start_task "$task"
        fi
    done
    sleep 10
done