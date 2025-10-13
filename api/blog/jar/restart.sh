echo '----restart container----'
cd /opt/docker/files/jar
docker stop blog
docker rm blog
docker rmi blog:3.0
docker build -t blog:3.0 .
docker run -d --name blog --privileged=true --restart=always --network blog_network --ip 172.18.0.13 -p 60001:60001 -p 60002:60002 -p 59994:59994 -p 60032:60032 -p 21:21 -v /opt/docker/files/logs:/opt/logs -v /opt/docker/files/:/opt/docker/files/ blog:3.0