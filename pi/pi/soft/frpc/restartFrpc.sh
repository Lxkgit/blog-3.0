#!/bin/bash

FRPC_DIR="/opt/soft/frpc/frp_0.68.0_linux_arm64"
FRPC="${FRPC_DIR}/frpc"
CONFIG="${FRPC_DIR}/frpc.ini"
LOG="${FRPC_DIR}/frpc.log"

echo "停止 frpc..."

PID=$(pgrep -f "${FRPC} -c ${CONFIG}")

if [ -n "$PID" ]; then
    kill "$PID"
    sleep 2

    # 确认是否已经停止
    if pgrep -f "${FRPC} -c ${CONFIG}" > /dev/null; then
        echo "FRPC 未正常停止，强制结束..."
        pkill -9 -f "${FRPC} -c ${CONFIG}"
    fi
else
    echo "FRPC 当前未运行"
fi

echo "启动 FRPC..."

nohup "$FRPC" -c "$CONFIG" > "$LOG" 2>&1 &

echo "FRPC 重启完成"

