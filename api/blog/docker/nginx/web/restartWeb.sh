#!/bin/bash

if docker ps -a --format '{{.Names}}' | grep -q '^nginx-web$'; then
    echo "nginx-web 容器已存在，重启容器..."
    docker restart nginx-web
else
    echo "nginx-web 容器不存在，创建并启动..."
    docker run -d \
        --name nginx-web \
        --restart=always \
        --network blog_network \
        --ip 172.18.0.32 \
        -v /opt/docker/nginx/web/conf/nginx.conf:/etc/nginx/nginx.conf:ro \
        -v /opt/docker/nginx/web/html:/usr/share/nginx/html:ro \
        -v /opt/docker/nginx/web/logs:/var/log/nginx \
        nginx:1.20.2
fi