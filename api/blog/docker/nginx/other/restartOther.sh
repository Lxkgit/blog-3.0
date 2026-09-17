#!/bin/bash

if docker ps -a --format '{{.Names}}' | grep -q '^nginx-other$'; then
    echo "nginx-other 容器已存在，重启容器..."
    docker restart nginx-other
else
    echo "nginx-other 容器不存在，创建并启动..."
    docker run -d \
        --name nginx-other \
        --restart=always \
        --network host \
        -v /opt/docker/nginx/other/conf/nginx.conf:/etc/nginx/nginx.conf:ro \
        -v /opt/docker/nginx/other/logs:/var/log/nginx \
        nginx:1.20.2
fi