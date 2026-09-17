#!/bin/bash

# Nginx 配置目录
SOURCE="/opt/docker/ci/code/blog-3.0/api/blog/docker/nginx"
TARGET="/opt/docker/nginx"

# 更新 Nginx 文件
rm -rf "$TARGET"
cp -r "$SOURCE" "$TARGET"

# 授权重启脚本
find "$TARGET" -type f -name "*.sh" -exec chmod +x {} \;

# 重启全部 Nginx
"$TARGET/router/restartRouter.sh"
"$TARGET/other/restartOther.sh"

"$TARGET/web/updateWeb.sh"
"$TARGET/util/updateUtil.sh"

