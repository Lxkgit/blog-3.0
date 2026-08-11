# 指定基础镜像
FROM openjdk:17

MAINTAINER gszero

# 指定工作目录
WORKDIR /opt

# 文件与目录复制到docker容器中
ADD run.sh /opt/run.sh

ADD auth /opt/auth

# 容器暴露的端口
EXPOSE 60002

# 赋予启动脚本可执行权限
RUN chmod +x run.sh

CMD ["/opt/run.sh"]
