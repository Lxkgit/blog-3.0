#!/bin/bash

WATCHDOG_LOG="/opt/docker/watchdog/watchdog.log"

VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"

LOG_DIR="/opt/docker/watchdog/logs"
PID_DIR="/opt/docker/watchdog/pids"

mkdir -p "$LOG_DIR"
mkdir -p "$PID_DIR"

# -------------------- 任务列表（完整命令） --------------------
TASKS=(
  "$PYTHON_BIN /opt/docker/files/python/code/web_socket.py --ip 172.18.0.5"
  "/opt/docker/camera/startCSI.sh"
)

# -------------------- 提取脚本路径 --------------------
# 根据规则：脚本路径一定在第1或第2字段
get_script_path() {
    local task="$1"
    set -- $task

    if [[ "$1" == /* ]]; then
        echo "$1"
    elif [[ "$2" == /* ]]; then
        echo "$2"
    else
        echo ""
    fi
}

# -------------------- 启动任务并记录 PID --------------------
start_task() {
    local task="$1"
    local script_path
    script_path=$(get_script_path "$task")

    local script_name
    script_name=$(basename "$script_path")

    echo "$(date): [START] $task" >> "$WATCHDOG_LOG"

    nohup bash -c "$task" > "$LOG_DIR/${script_name}.log" 2>&1 &
    local pid=$!

    echo "$pid" > "$PID_DIR/${script_name}.pid"

    echo "$(date): [PID $pid] $script_name started" >> "$WATCHDOG_LOG"
}

# -------------------- 检查任务是否存活（根据 PID 文件） --------------------
check_task() {
    local task="$1"
    local script_path
    script_path=$(get_script_path "$task")
    local script_name
    script_name=$(basename "$script_path")

    local pid_file="$PID_DIR/${script_name}.pid"

    # PID 文件不存在 => 肯定没在运行
    [[ ! -f "$pid_file" ]] && return 1

    local pid
    pid=$(cat "$pid_file")

    # kill -0 检查进程是否存在（不杀死进程）
    if kill -0 "$pid" >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# -------------------- 主循环 --------------------
echo "$(date): Watchdog started." >> "$WATCHDOG_LOG"

while true; do
    for task in "${TASKS[@]}"; do
        if ! check_task "$task"; then
            echo "$(date): [MISSING] $task，正在重新启动..." >> "$WATCHDOG_LOG"
            start_task "$task"
        fi
    done
    sleep 10
done