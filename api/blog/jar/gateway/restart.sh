#! /bin/bash

blogGateway="blog-gateway"

echo '----restart container----'
cd /opt/docker/files/jar/gateway || exit
docker stop ${blogGateway}
docker rm ${blogGateway}
docker rmi ${blogGateway}:3.0
docker build -t ${blogGateway}:3.0 .
docker run -d --name ${blogGateway} --privileged=true --restart=always --network blog_network --ip 172.18.0.21 -p 60001:60001 -v /opt/docker/files/logs:/opt/logs -v /opt/docker/files/:/opt/docker/files/ blog:3.0