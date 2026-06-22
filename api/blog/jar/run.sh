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
nohup java ${JAVA_AUTH_OPTS} -Dspring.config.location=file:/opt/auth/ -jar /opt/auth/${blogAuthJar}.jar &

echo "3分钟后启动剩余服务..."
sleep 3m

echo "启动网关服务..."
nohup java ${JAVA_GATEWAY_OPTS} -Dspring.config.location=file:/opt/gateway/ -jar /opt/gateway/${blogGatewayJar}.jar &

echo "启动内容服务..."
nohup java ${JAVA_CONTENT_OPTS} -Dspring.config.location=file:/opt/content/ -jar /opt/content/${blogContentJar}.jar &

echo "启动文件服务..."
nohup java ${JAVA_FILE_OPTS} -Dspring.config.location=file:/opt/file/ -jar /opt/file/${blogFileJar}.jar &


# 保持容器运行
tail -f /dev/null