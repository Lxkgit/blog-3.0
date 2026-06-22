# 指定基础镜像
FROM openjdk:17

MAINTAINER gszero

# 指定工作目录
WORKDIR /opt

# 文件与目录复制到docker容器中
ADD run.sh /opt/run.sh

ADD gateway /opt/gateway
ADD auth /opt/auth
ADD content /opt/content
ADD file /opt/file

#  expose  该容器暴露的端口是多少，就是jar在容器中以多少端口运行
EXPOSE 60001

# 赋予启动脚本可执行权限
RUN chmod +x run.sh

CMD ["/opt/run.sh"]

#CMD ["java", "-jar", "blog-gateway.jar"]
#CMD ["java", "-jar", "blog-user.jar"]
#CMD ["java", "-jar", "blog-auth.jar"]
#CMD ["java", "-jar", "blog-content.jar"]
#CMD ["java", "-jar", "blog-file.jar"]