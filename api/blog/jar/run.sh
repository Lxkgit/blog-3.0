#! /bin/bash

blogGatewayJar="blog-gateway"
blogAuthJar="blog-auth"
blogContentJar="blog-content"
blogFileJar="blog-file"

JAVA_AUTH_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59991"
JAVA_GATEWAY_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59992"
JAVA_CONTENT_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59993"
JAVA_FILE_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59994"

echo "启动鉴权服务..."
nohup java ${JAVA_AUTH_OPTS} -jar ${blogAuthJar}.jar > /opt/docker/files/log/${blogAuthJar}.log 2>&1 &
sleep 3m
echo "启动网关服务..."
nohup java ${JAVA_GATEWAY_OPTS} -jar ${blogGatewayJar}.jar > /opt/docker/files/log/${blogGatewayJar}.log 2>&1 &
echo "启动内容服务..."
nohup java ${JAVA_CONTENT_OPTS} -jar ${blogContentJar}.jar > /opt/docker/files/log/${blogContentJar}.log 2>&1 &
echo "启动文件服务..."
nohup java ${JAVA_FILE_OPTS} -jar ${blogFileJar}.jar > /opt/docker/files/log/${blogFileJar}.log 2>&1 &

# 保持容器运行
tail -f /dev/null