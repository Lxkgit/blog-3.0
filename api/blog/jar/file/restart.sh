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
  --network host \
  -v /opt/docker/files/logs:/opt/logs \
  -v /opt/docker/files/:/opt/docker/files/ \
  ${blogFile}:3.0

if [ $? -ne 0 ]; then
    echo "Docker 容器启动失败"
    exit 1
fi

echo "文件服务启动成功"
