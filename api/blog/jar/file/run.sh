#! /bin/bash

blogFile="blog-file"

JAVA_FILE_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59994"

echo "启动文件服务..."

exec java ${JAVA_FILE_OPTS} -Dspring.config.location=file:/opt/file/ -jar /opt/file/${blogFile}.jar