echo '----restart container----'
cd /opt/docker/files/jar
docker stop pi
docker rm pi
docker rmi pi:1
docker build -t pi:1 .
docker run -d --name pi --privileged=true --cap-add=SYS_ADMIN --restart=always --network blog_network --ip 172.18.0.5 -p 10201:10201 -p 9092:9092 -p 5005:5005 -v /opt/docker/files:/opt/docker/files pi:1