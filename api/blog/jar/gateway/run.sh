#! /bin/bash

blogGateway="blog-gateway"

JAVA_GATEWAY_OPTS="-Duser.timezone=GMT+8 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:59992"

echo "启动网关服务..."
exec java ${JAVA_GATEWAY_OPTS} -Dspring.config.location=file:/opt/gateway/ -jar /opt/gateway/${blogGateway}.jar