#! /bin/bash

blogContent="blog-content"

JAVA_CONTENT_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59993"

echo "启动内容服务..."

exec java ${JAVA_CONTENT_OPTS} -Dspring.config.location=file:/opt/content/ -jar /opt/content/${blogContent}.jar