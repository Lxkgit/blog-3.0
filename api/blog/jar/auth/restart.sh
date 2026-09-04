#!/bin/bash

blogAuth="blog-auth"

echo "---- restart container ----"
cd /opt/docker/files/jar/auth || exit 1
docker rm -f ${blogAuth} 2>/dev/null || true
docker rmi ${blogAuth}:3.0 2>/dev/null || true
docker build -t ${blogAuth}:3.0 .
if [ $? -ne 0 ]; then
    echo "Docker 镜像构建失败"
    exit 1
fi

docker run -d \
  --name ${blogAuth} \
  --restart=always \
  --network blog_network \
  --ip 172.18.0.22 \
  -p 60002:60002 \
  -v /opt/docker/files/logs:/opt/logs \
  -v /opt/docker/files/:/opt/docker/files/ \
  ${blogAuth}:3.0

if [ $? -ne 0 ]; then
    echo "Docker 容器启动失败"
    exit 1
fi

echo "鉴权服务启动成功"