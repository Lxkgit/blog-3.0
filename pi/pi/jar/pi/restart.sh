#! /bin/bash

blogPi="blog-pi"

echo "---- restart container ----"
cd /opt/docker/files/jar/pi || exit 1
docker rm -f ${blogPi} 2>/dev/null || true
docker build -t ${blogPi}:3.0 .
if [ $? -ne 0 ]; then
    echo "Docker 镜像构建失败"
    exit 1
fi

#--cap-add=SYS_ADMIN \
docker run -d \
  --name pi \
  --privileged=true \
  --restart=always \
  --network blog_network \
  --ip 172.18.0.5 \
  -p 10201:10201 -p 9092:9092 -p 5005:5005 \
  -v /opt/docker/files/logs:/opt/logs \
  -v /opt/docker/files/:/opt/docker/files/ \
  ${blogPi}:3.0

if [ $? -ne 0 ]; then
    echo "Docker 容器启动失败"
    exit 1
fi

echo "树莓派服务启动成功"
