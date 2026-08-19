#! /bin/bash

blogPiJar="blog-pi"

JAVA_PI_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 -Xms512m -Xmx4g -XX:+UseG1GC -Dfile.encoding=UTF-8"

echo "启动树莓派硬件接入服务..."
exec java ${JAVA_PI_OPTS} -Dspring.config.location=file:/opt/pi/ -jar /opt/pi/${blogPiJar}.jar
