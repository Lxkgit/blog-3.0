#! /bin/bash

# 更新并打包Java服务
/opt/docker/ci/shell/buildController.sh web pro

rm -rf /opt/docker/nginx/html/*
mv /opt/docker/ci/code/blog-3.0/web/dist/* /opt/docker/nginx/html/

# 重启Java服务
/opt/docker/web/restartWeb.sh

