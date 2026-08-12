#! /bin/bash

blogContent="blog-content"

echo "---- restart container ----"
cd /opt/docker/files/jar/content || exit 1
docker rm -f ${blogContent} 2>/dev/null || true
docker build -t ${blogContent}:3.0 .
if [ $? -ne 0 ]; then
    echo "Docker 镜像构建失败"
    exit 1
fi

docker run -d \
  --name ${blogContent} \
  --restart=always \
  --network blog_network \
  --ip 172.18.0.23 \
  -p 60003:60003 \
  -v /opt/docker/files/logs:/opt/logs \
  -v /opt/docker/files/:/opt/docker/files/ \
  ${blogContent}:3.0

if [ $? -ne 0 ]; then
    echo "Docker 容器启动失败"
    exit 1
fi

echo "内容服务启动成功"
