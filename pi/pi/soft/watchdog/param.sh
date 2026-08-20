#!/bin/bash


# Python
VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"

# 服务列表
SERVICES=("socket" "frpc" "camera")

# 声明数组参数
declare -A SERVICE_NAME
declare -A SERVICE_MATCH
declare -A SERVICE_CMD
declare -A SERVICE_LOG

# Python Socket
SERVICE_NAME[socket]="web_socket"
SERVICE_MATCH[socket]="/opt/soft/socket/code/web_socket.py"
SERVICE_CMD[socket]="$PYTHON_BIN /opt/soft/socket/code/web_socket.py --ip 172.18.0.5 --port 10201 --path /socket/python/localhost"
SERVICE_LOG[socket]="/opt/soft/socket/blog_socket.log"

# frpc
SERVICE_NAME[frpc]="frpc"
SERVICE_MATCH[frpc]="/opt/soft/frpc/frp_0.68.0_linux_amd64/frpc -c /opt/soft/frpc/frp_0.68.0_linux_amd64/frpc.ini"
SERVICE_CMD[frpc]="/opt/soft/frpc/frp_0.68.0_linux_amd64/frpc -c /opt/soft/frpc/frp_0.68.0_linux_amd64/frpc.ini"
SERVICE_LOG[frpc]="/opt/soft/frpc/frp_0.68.0_linux_amd64/frpc.log"

# Camera
SERVICE_NAME[camera]="startCSI"
SERVICE_MATCH[camera]="/opt/soft/camera/startCSI.sh"
SERVICE_CMD[camera]="/opt/soft/camera/startCSI.sh"
SERVICE_LOG[camera]="/opt/soft/camera/camera.log"