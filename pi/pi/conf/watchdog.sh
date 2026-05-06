#!/bin/bash

WATCHDOG_LOG="/opt/docker/watchdog/watchdog.log"

VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"

LOG_DIR="/opt/docker/watchdog/logs"
mkdir -p "$LOG_DIR"

# -------------------- 任务列表 --------------------
# 每个任务都是一个完整命令
TASKS=(
  "$PYTHON_BIN /opt/docker/files/python/code/web_socket.py --ip 172.18.0.5"
  "/opt/docker/camera/startCSI.sh"
)

# -------------------- 提取脚本路径 --------------------
# 根据规则：脚本路径一定在第1或第2字段
get_script_path() {
    local task="$1"
    set -- $task    # 把 task 拆成多个位置参数

    # $1：可能是 python 或脚本
    # $2：如果是 python，则脚本在 $2
    if [[ "$1" == /* ]]; then
        echo "$1"
    elif [[ "$2" == /* ]]; then
        echo "$2"
    else
        echo ""
    fi
}

# -------------------- 启动任务 --------------------
start_task() {
    local task="$1"
    local script_path
    script_path=$(get_script_path "$task")

    local script_name
    script_name=$(basename "$script_path")

    echo "$(date): [START] $task" >> "$WATCHDOG_LOG"

    nohup bash -c "$task" > "$LOG_DIR/${script_name}.log" 2>&1 &

    echo "$(date): [PID $!] $script_name started" >> "$WATCHDOG_LOG"
}

# -------------------- 检查任务是否存活 --------------------
check_task() {
    local task="$1"
    local script_path
    script_path=$(get_script_path "$task")

    local key
    key=$(basename "$script_path")

    pgrep -f "$key" >/dev/null
    return $?
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