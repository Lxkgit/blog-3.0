#!/bin/bash

# Nginx 服务

if docker ps -a --format '{{.Names}}' | grep -q '^nginx$'; then
    echo "nginx 容器已存在，重启容器..."
    docker restart nginx
else
    echo "nginx 容器不存在，创建并启动..."
    docker run -d \
      --name nginx \
      --privileged=true \
      --restart=always \
      --network blog_network \
      --ip 172.18.0.5 \
      -p 80:80 \
      -v /opt/docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf \
      -v /opt/docker/nginx/html/:/opt/docker/nginx/html/ \
      -v /opt/docker/nginx/logs/:/var/log/nginx/ \
      -v /opt/docker/files/:/opt/docker/files/ \
      nginx:1.20.2
fi