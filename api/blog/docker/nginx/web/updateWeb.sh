#! /bin/bash

# Nginx Web 服务
mkdir -p /opt/docker/nginx/web/conf
mkdir -p /opt/docker/nginx/web/html
mkdir -p /opt/docker/nginx/web/logs

# 更新并打包Java服务
/opt/docker/ci/shell/buildController.sh -s web -e pro

rm -rf /opt/docker/nginx/web/html
mkdir -p /opt/docker/nginx/web/html
mv /opt/docker/ci/code/blog-3.0/web/dist/* /opt/docker/nginx/web/html

# 更新nginx服务配置
cp /opt/docker/ci/code/blog-3.0/api/blog/docker/nginx/web/conf/nginx.conf /opt/docker/nginx/web/conf/

# 重启Java服务
/opt/docker/nginx/web/restartWeb.sh

