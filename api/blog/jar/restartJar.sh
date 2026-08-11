#! /bin/bash

# 启动 auth 服务
/opt/docker/jar/auth/restart.sh
sleep 1m

# 启动 gateway 服务
/opt/docker/jar/gateway/restart.sh
sleep 1m

# 启动 content 服务
/opt/docker/jar/content/restart.sh
sleep 1m

# 启动 file 服务
/opt/docker/jar/file/restart.sh