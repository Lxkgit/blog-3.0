#! /bin/bash

blogFile="blog-file"

echo "---- restart container ----"
cd /opt/docker/files/jar/file || exit 1
docker rm -f ${blogFile} 2>/dev/null || true
docker rmi ${blogFile}:3.0 2>/dev/null || true
docker build -t ${blogFile}:3.0 .
if [ $? -ne 0 ]; then
    echo "Docker 镜像构建失败"
    exit 1
fi

docker run -d \
  --name ${blogFile} \
  --restart=always \
  --network blog_network \
  --ip 172.18.0.24 \
  -p 60004:60004 -p 59994:59994 -p 60032:60032 -p 21:21 \
  -v /opt/docker/files/logs:/opt/logs \
  -v /opt/docker/files/:/opt/docker/files/ \
  ${blogFile}:3.0

if [ $? -ne 0 ]; then
    echo "Docker 容器启动失败"
    exit 1
fi

echo "文件服务启动成功"
