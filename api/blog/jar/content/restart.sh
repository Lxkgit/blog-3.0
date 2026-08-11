#! /bin/bash

blogContent="blog-content"

echo '----restart container----'
docker stop ${blogContent}
docker rm ${blogContent}
docker rmi ${blogContent}:3.0
docker build -t ${blogContent}:3.0 .
docker run -d --name ${blogContent} --privileged=true --restart=always --network blog_network --ip 172.18.0.23 -p 60003:60003 -v /opt/docker/files/logs:/opt/logs -v /opt/docker/files/:/opt/docker/files/ blog:3.0