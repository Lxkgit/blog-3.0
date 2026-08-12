#! /bin/bash

blogAuth="blog-auth"

JAVA_AUTH_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59991"

echo "启动鉴权服务..."

exec java ${JAVA_AUTH_OPTS} -Dspring.config.location=file:/opt/auth/ -jar /opt/auth/${blogAuth}.jar