#! /bin/bash

# 启动 auth 服务
/opt/docker/files/jar/auth/restart.sh
sleep 1m

# 启动 gateway 服务
/opt/docker/files/jar/auth/restart.sh
sleep 1m

# 启动 content 服务
/opt/docker/files/jar/auth/restart.sh
sleep 1m

# 启动 file 服务
/opt/docker/files/jar/auth/restart.sh