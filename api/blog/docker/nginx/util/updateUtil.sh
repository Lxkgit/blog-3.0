#! /bin/bash

# Nginx Util 工具服务
mkdir -p /opt/docker/nginx/util/conf
mkdir -p /opt/docker/nginx/util/html
mkdir -p /opt/docker/nginx/util/logs

# 更新 Nginx 服务配置
/opt/docker/ci/shell/updateCode.sh -n blog
cp /opt/docker/ci/code/blog-3.0/api/blog/docker/nginx/util/conf/nginx.conf /opt/docker/nginx/util/conf/

# 更新并打包 excel 服务
/opt/docker/ci/shell/buildController.sh -e pro -p excel

rm -rf /opt/docker/nginx/util/html
mkdir -p /opt/docker/nginx/util/html/excel
mv /opt/docker/ci/code/web-excel/dist/* /opt/docker/nginx/util/html/excel

# 更新并打包 game 服务
/opt/docker/ci/shell/buildController.sh -e pro -p game

rm -rf /opt/docker/nginx/util/game
mkdir -p /opt/docker/nginx/util/html/game
mv /opt/docker/ci/code/game-library/dist/* /opt/docker/nginx/util/html/game

# 重启 Nginx Util 服务
/opt/docker/nginx/util/restartUtil.sh