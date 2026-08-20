#!/bin/bash


# Python
VENV_PATH="/opt/python"
PYTHON_BIN="$VENV_PATH/bin/python"

# 服务列表
SERVICES=("socket" "frps")

# 声明数组参数
declare -gA SERVICE_NAME
declare -gA SERVICE_MATCH
declare -gA SERVICE_CMD
declare -gA SERVICE_LOG

# Python Socket
SERVICE_NAME[socket]="web_socket"
SERVICE_MATCH[socket]="/opt/soft/socket/code/web_socket.py"
SERVICE_CMD[socket]="$PYTHON_BIN /opt/soft/socket/code/web_socket.py --ip 172.18.0.21 --port 60001 --path /file/socket/python/localhost"
SERVICE_LOG[socket]="/opt/soft/socket/blog_socket.log"

# frps
SERVICE_NAME[frps]="frps"
SERVICE_MATCH[frps]="/opt/soft/frps/frp_0.68.0_linux_amd64/frps -c /opt/soft/frps/frp_0.68.0_linux_amd64/frps.ini"
SERVICE_CMD[frps]="/opt/soft/frps/frp_0.68.0_linux_amd64/frps -c /opt/soft/frps/frp_0.68.0_linux_amd64/frps.ini"
SERVICE_LOG[frps]="/opt/soft/frps/frp_0.68.0_linux_amd64/frps.log"
