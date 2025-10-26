#! /bin/bash

blogPiJar="blog-pi"

JAVA_OPTS="-Duser.timezone=GMT+8 \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
-Xms512m -Xmx4g -XX:+UseG1GC -Dfile.encoding=UTF-8"

echo "启动树莓派硬件接入服务..."
nohup java ${JAVA_OPTS} -jar ${blogPiJar}.jar > /opt/docker/files/logs/${blogPiJar}.log 2>&1 &


# 保持容器运行
tail -f /dev/null