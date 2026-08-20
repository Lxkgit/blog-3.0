#!/bin/bash


# 加载参数配置
CONFIG_FILE="/opt/soft/watchdog/param.sh"

# Watchdog 参数配置
WATCHDOG_LOG="/opt/soft/watchdog/watchdog.log"
CHECK_INTERVAL=30
LOCK_FILE="/var/run/blog-watchdog.lock"

# 加载配置
load_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
      echo "$(date '+%Y-%m-%d %H:%M:%S'): 配置文件不存在: $CONFIG_FILE"
      exit 1
  fi
  source "$CONFIG_FILE"
}

# Watchdog 锁
init_lock() {
  exec 200>"$LOCK_FILE"
  if ! flock -n 200; then
      echo "$(date '+%Y-%m-%d %H:%M:%S'): Watchdog 已经在运行，退出"
      exit 1
  fi
}

# 日志
log() {
  local log_dir
  log_dir="$(dirname "$WATCHDOG_LOG")"
  if [ ! -d "$log_dir" ]; then
      mkdir -p "$log_dir"
  fi
  echo "$(date '+%Y-%m-%d %H:%M:%S'): $1" >> "$WATCHDOG_LOG"
}

# 检查服务是否运行
is_running() {
  local match="$1"
  pgrep -f "$match" > /dev/null 2>&1
}

# 启动服务
start_service() {
  local key="$1"
  local name="${SERVICE_NAME[$key]}"
  local match="${SERVICE_MATCH[$key]}"
  local cmd="${SERVICE_CMD[$key]}"
  local log_file="${SERVICE_LOG[$key]}"
  log "$name 未运行，正在启动..."
  # 创建日志目录
  local log_dir
  log_dir="$(dirname "$log_file")"
  if [ ! -d "$log_dir" ]; then
      mkdir -p "$log_dir"
  fi
  # 启动服务
  nohup bash -c "$cmd" > "$log_file" 2>&1 &
  # 等待启动
  sleep 1
  # 检查启动结果
  if is_running "$match"; then
      local pid
      pid="$(pgrep -f "$match" | head -n 1)"
      log "$name 启动成功 (PID: $pid)"
  else
      log "$name 启动失败，请检查日志: $log_file"
  fi
}

# 检查单个服务
check_service() {
  local key="$1"
  local name="${SERVICE_NAME[$key]}"
  local match="${SERVICE_MATCH[$key]}"
  local cmd="${SERVICE_CMD[$key]}"
  local log_file="${SERVICE_LOG[$key]}"
  if [ -z "$name" ] || [ -z "$match" ] || [ -z "$cmd" ] || [ -z "$log_file" ]; then
      log "服务配置不完整: $key"
      return
  fi

  if ! is_running "$match"; then
      start_service "$key"
  fi
}

# 检查所有服务
check_services() {
  local key
  for key in "${SERVICES[@]}"; do
      check_service "$key"
  done
}

# 主循环

main() {
  load_config
  init_lock
  log "========================================"
  log "Watchdog 启动"
  log "监控服务数量: ${#SERVICES[@]}"
  log "检查间隔: ${CHECK_INTERVAL}s"
  log "========================================"

  while true; do
      check_services
      sleep "$CHECK_INTERVAL"
  done
}

main