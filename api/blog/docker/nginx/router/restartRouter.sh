#!/bin/bash

if docker ps -a --format '{{.Names}}' | grep -q '^nginx-router$'; then
    echo "nginx-router 容器已存在，重启容器..."
    docker restart nginx-router
else
    echo "nginx-router 容器不存在，创建并启动..."
    docker run -d \
        --name nginx-router \
        --restart=always \
        --network host \
        -v /opt/docker/nginx/router/conf/nginx.conf:/etc/nginx/nginx.conf:ro \
        -v /opt/docker/nginx/router/logs:/var/log/nginx \
        nginx:1.20.2
fi