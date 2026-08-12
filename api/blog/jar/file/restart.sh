#! /bin/bash

blogFile="blog-file"

echo '----restart container----'
cd /opt/docker/files/jar/file || exit
docker stop ${blogFile}
docker rm ${blogFile}
docker rmi ${blogFile}:3.0
docker build -t ${blogFile}:3.0 .
docker run -d --name ${blogFile} --privileged=true --restart=always --network blog_network --ip 172.18.0.24 -p 60004:60004 -p 59994:59994 -p 60032:60032 -p 21:21 -v /opt/docker/files/logs:/opt/logs -v /opt/docker/files/:/opt/docker/files/ blog:3.0