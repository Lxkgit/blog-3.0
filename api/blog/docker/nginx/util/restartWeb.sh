#!/bin/bash

if docker ps -a --format '{{.Names}}' | grep -q '^nginx-util$'; then
    echo "nginx-util 容器已存在，重启容器..."
    docker restart nginx-util
else
    echo "nginx-util 容器不存在，创建并启动..."
    docker run -d \
        --name nginx-util \
        --restart=always \
        --network blog_network \
        --ip 172.18.0.34 \
        -v /opt/docker/nginx/util/conf/nginx.conf:/etc/nginx/nginx.conf:ro \
        -v /opt/docker/nginx/util/html:/usr/share/nginx/html:ro \
        -v /opt/docker/nginx/util/logs:/var/log/nginx \
        nginx:1.20.2
fi