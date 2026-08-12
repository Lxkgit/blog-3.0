# 指定基础镜像
FROM openjdk:17

MAINTAINER gszero

# 指定工作目录
WORKDIR /opt

# 文件与目录复制到docker容器中
COPY run.sh /opt/run.sh
COPY blog-gateway.jar /opt/auth/blog-gateway.jar
COPY application-dev.yml /opt/auth/application-dev.yml
COPY application-pro.yml /opt/auth/application-pro.yml
COPY application-test.yml /opt/auth/application-test.yml
COPY bootstrap.yml /opt/auth/bootstrap.yml

# 容器暴露的端口
EXPOSE 60001

# 赋予启动脚本可执行权限
RUN chmod +x /opt/run.sh

CMD ["/opt/run.sh"]