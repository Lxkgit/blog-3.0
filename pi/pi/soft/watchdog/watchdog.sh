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

WATCHDOG_LOG="/opt/soft/watchdog/watchdog.log"

VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"


# =========================
# 服务配置
# =========================

# ---------- Python Socket ----------
SOCKET_NAME="web_socket"
SOCKET_MATCH="/opt/soft/socket/code/web_socket.py"
SOCKET_CMD="$PYTHON_BIN /opt/soft/socket/code/web_socket.py --ip 172.18.0.5 --port 10201 --path /socket/python/localhost"
SOCKET_LOG="/opt/soft/socket/blog_socket.log"

# ---------- frpc ----------
FRPS_NAME="frpc"
FRPS_MATCH="/opt/soft/frpc/frp_0.68.0_linux_amd64/frpc -c /opt/soft/frpc/frp_0.68.0_linux_amd64/frpc.ini"
FRPS_CMD="/opt/soft/frpc/frp_0.68.0_linux_amd64/frpc -c /opt/soft/frpc/frp_0.68.0_linux_amd64/frpc.ini"
FRPS_LOG="/opt/soft/frpc/frp_0.68.0_linux_amd64/frpc.log"

# ---------- camera ----------
CAMERA_NAME="startCSI"
CAMERA_MATCH="/opt/soft/camera/startCSI.sh"
CAMERA_CMD="/opt/soft/camera/startCSI.sh"
CAMERA_LOG="/opt/soft/camera/camera.log"

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

check_camera() {
    check_service "$CAMERA_NAME" "$CAMERA_MATCH" "$CAMERA_CMD" "$CAMERA_LOG"
}

# =========================
# 主循环
# =========================
main() {
  log "Watchdog 启动"

  while true; do
      check_socket
      check_frps
      check_camera
      sleep 10
  done
}

main