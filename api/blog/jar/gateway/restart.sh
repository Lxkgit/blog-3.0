#! /bin/bash

blogGateway="blog-gateway"

echo "---- restart container ----"
cd /opt/docker/files/jar/gateway || exit 1
docker rm -f ${blogGateway} 2>/dev/null || true
docker rmi ${blogGateway}:3.0 2>/dev/null || true
docker build -t ${blogGateway}:3.0 .
if [ $? -ne 0 ]; then
    echo "Docker 镜像构建失败"
    exit 1
fi

docker run -d \
  --name ${blogGateway} \
  --restart=always \
  --network host \
  -v /opt/docker/files/logs:/opt/logs \
  -v /opt/docker/files/:/opt/docker/files/ \
  ${blogGateway}:3.0

if [ $? -ne 0 ]; then
    echo "Docker 容器启动失败"
    exit 1
fi

echo "网关服务启动成功"
