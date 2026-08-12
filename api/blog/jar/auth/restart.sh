#! /bin/bash

blogAuth="blog-auth"

echo '----restart container----'
cd /opt/docker/files/jar/auth || exit
docker stop ${blogAuth}
docker rm ${blogAuth}
docker rmi ${blogAuth}:3.0
docker build -t ${blogAuth}:3.0 .
docker run -d --name ${blogAuth} --privileged=true --restart=always --network blog_network --ip 172.18.0.22 -p 60002:60002 -v /opt/docker/files/logs:/opt/logs -v /opt/docker/files/:/opt/docker/files/ blog:3.0