#!/bin/bash

# =========================
# Watchdog 锁
# =========================

LOCK_FILE="/var/run/blog-watchdog.lock"

exec 200>"$LOCK_FILE"

if ! flock -n 200; then
    echo "$(date '+%Y-%m-%d %H:%M:%S'): Watchdog 已经在运行，退出"
    exit 1
fi


# =========================
# 全局配置
# =========================

WATCHDOG_LOG="/opt/watchdog/watchdog.log"

VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"


# =========================
# 服务配置
# =========================

# ---------- Python Socket ----------
SOCKET_NAME="web_socket"
SOCKET_MATCH="/opt/socket/code/web_socket.py"
SOCKET_CMD="$PYTHON_BIN /opt/socket/code/web_socket.py --ip 172.18.0.21 --port 60001 --path /file/socket/python/localhost"
SOCKET_LOG="/opt/socket/blog_socket.log"

# ---------- frps ----------
FRPS_NAME="frps"
FRPS_MATCH="/opt/frps/frp_0.68.0_linux_amd64/frps -c /opt/frps/frp_0.68.0_linux_amd64/frps.ini"
FRPS_CMD="/opt/frps/frp_0.68.0_linux_amd64/frps -c /opt/frps/frp_0.68.0_linux_amd64/frps.ini"
FRPS_LOG="/opt/frps/frp_0.68.0_linux_amd64/frps.log"

# =========================
# 通用函数
# =========================

# 写 Watchdog 日志
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S'): $1" >> "$WATCHDOG_LOG"
}


# 检查服务是否运行
is_running() {
    local match="$1"

    pgrep -f "$match" > /dev/null 2>&1
}


# 启动服务
start_service() {
    local name="$1"
    local match="$2"
    local cmd="$3"
    local log_file="$4"

    log "$name 未运行，正在启动..."

    nohup bash -c "$cmd" > "$log_file" 2>&1 &

    local pid=$!

    sleep 1

    if is_running "$match"; then
        log "$name 启动成功 (PID: $pid)"
    else
        log "$name 启动失败，请检查日志: $log_file"
    fi
}


# 检查服务并自动启动
check_service() {
    local name="$1"
    local match="$2"
    local cmd="$3"
    local log_file="$4"

    if ! is_running "$match"; then
        start_service "$name" "$match" "$cmd" "$log_file"
    fi
}


# =========================
# 服务检查函数
# =========================

check_socket() {
    check_service "$SOCKET_NAME" "$SOCKET_MATCH" "$SOCKET_CMD" "$SOCKET_LOG"
}

check_frps() {
    check_service "$FRPS_NAME" "$FRPS_MATCH" "$FRPS_CMD" "$FRPS_LOG"
}


# =========================
# 主循环
# =========================
main() {
  log "Watchdog 启动"

  while true; do
      check_socket
      check_frps
      sleep 10
  done
}

main