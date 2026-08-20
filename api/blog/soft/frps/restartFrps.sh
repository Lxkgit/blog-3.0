#!/bin/bash

FRPS_DIR="/opt/soft/frps/frp_0.68.0_linux_amd64"
FRPS="${FRPS_DIR}/frps"
CONFIG="${FRPS_DIR}/frps.ini"
LOG="${FRPS_DIR}/frps.log"

echo "停止 frps..."

PID=$(pgrep -f "${FRPS} -c ${CONFIG}")

if [ -n "$PID" ]; then
    kill "$PID"
    sleep 2

    # 确认是否已经停止
    if pgrep -f "${FRPS} -c ${CONFIG}" > /dev/null; then
        echo "frps 未正常停止，强制结束..."
        pkill -9 -f "${FRPS} -c ${CONFIG}"
    fi
else
    echo "frps 当前未运行"
fi

echo "启动 frps..."

nohup "$FRPS" -c "$CONFIG" > "$LOG" 2>&1 &

echo "frps 重启完成"

