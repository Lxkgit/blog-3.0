# 指定基础镜像
FROM openjdk:17

MAINTAINER gszero

# 指定工作目录
WORKDIR /opt

# 文件与目录复制到docker容器中
COPY run.sh /opt/run.sh
COPY blog-pi.jar /opt/pi/blog-pi.jar
COPY application-pro.yml /opt/pi/application-pro.yml
COPY bootstrap.yml /opt/pi/bootstrap.yml

# 容器暴露的端口
EXPOSE 10201

# 赋予启动脚本可执行权限
RUN chmod +x /opt/run.sh

CMD ["/opt/run.sh"]
