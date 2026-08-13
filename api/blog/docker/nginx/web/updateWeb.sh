#! /bin/bash

# 更新并打包Java服务
/opt/docker/ci/shell/buildController.sh web pro

rm -rf /opt/docker/nginx/web/html
mkdir -p /opt/docker/nginx/web/html
mv /opt/docker/ci/code/blog-3.0/web/dist/* /opt/docker/nginx/web/html

# 重启Java服务
/opt/docker/nginx/web/restartWeb.sh

