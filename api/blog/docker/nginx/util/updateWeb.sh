#! /bin/bash

# Nginx Util 工具服务
mkdir -p /opt/docker/nginx/util/conf
mkdir -p /opt/docker/nginx/util/html
mkdir -p /opt/docker/nginx/util/logs

# 更新 Nginx 服务配置
cp /opt/docker/ci/code/blog-3.0/api/blog/docker/nginx/util/conf/nginx.conf /opt/docker/nginx/util/conf/

# 重启 Nginx Util 服务
/opt/docker/nginx/util/restartUtil.sh