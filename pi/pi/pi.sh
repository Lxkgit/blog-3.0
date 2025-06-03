#!/bin/bash

# MySQL登陆密码
mysqlPassword="MySql@Admin123*."

# redis登陆密码
redisPassword="redis-960@*"

# 安装并配置docker
dockerStart() {
  echo "启动docker ... "
  # 一键安装docker
	curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
  #	判断docker是否正确安装
	if [ $? -ne 0 ]; then
      echo "docker 安装失败, 脚本执行退出" >&2
      exit 1
  fi
  if ! command -v docker &>/dev/null; then
      echo "docker 未正常启动 " >&2
      exit 1
  fi
	# 启动docker
	sudo systemctl start docker
	# docker开始自启动
	systemctl enable docker.service
	# 创建自定义网络
	docker network create --subnet=172.18.0.0/24 blog_network
}

unzipPi() {
  # 上传部署压缩包解压目录
  mkdir -p /opt/package
  mv ./pi.zip /opt/package
  cd /opt/package
  unzip pi.zip
}

# conda 下载
installConda() {
  cd /opt/package/soft
	echo "开始安装 Anaconda ... "
	sh Anaconda3-2024.10-1-Linux-aarch64.sh<<EOF

q
yes

yes
EOF
	echo "export PATH=/root/anaconda3/bin:\$PATH"  >> /etc/profile
	echo "export PATH=/root/anaconda3/bin:\$PATH"  >> ~/.bashrc

	# 更新环境变量
	source /etc/profile
	source ~/.bashrc

	# 安装conda后命令行前面base隐藏
	conda config --set auto_activate_base False
	echo "Anaconda 安装完成 ... "

	createPythonEnv
}

# 构建 py 运行环境
createPythonEnv() {
	echo "安装python3.9 ... "
	conda create --name py3 python=3.9 -y
	conda activate py3
}

# docker 镜像加载
dockerLoad() {
  echo "docker 镜像加载 ... "
  cd /opt/package/images
  docker load < emqx.tar
  docker load < mysql.tar
  docker load < jdk17.tar
  docker load < redis.tar
}

# 安装jdk
installJdk() {
  echo "启动jdk ... "
  docker run -d -it --name jdk8 --privileged=true --restart=always --network blog_network --ip 172.18.0.2 openjdk:17
}

# 修改 MySQL 配置文件
updateMysqlConf() {
	echo "开始修改MySQL配置文件..."
	# mysql 配置
	mv /opt/package/conf/my.cnf /opt/docker/mysql/conf
	sed -i "s/password=/password=${mysqlPassword}/" /opt/docker/mysql/conf/my.cnf
}

# 安装MySQL
installMysql() {
	# mysql文件目录
	# 宿主机创建数据存放目录映射到容器
	mkdir -p /opt/docker/mysql/data
	# 宿主机创建配置文件目录映射到容器
	mkdir -p /opt/docker/mysql/conf
	# 宿主机创建日志目录映射到容器
	mkdir -p /opt/docker/mysql/logs

	updateMysqlConf

  echo "启动mysql ... "
  docker run -d --name mysql --privileged=true --restart=always --network blog_network --ip 172.18.0.3 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=${mysqlPassword} -v /opt/docker/files:/opt/docker/files  mysql/mysql-server:8.0.32


}

# MySQL 数据修改与导入
insertSqlData() {
    echo "开始修改MySQL数据恢复脚本文件..."
	  # MySQL数据文件
	  mkdir -p /opt/docker/files/sql
    mv /opt/package/sql/* /opt/docker/files/sql
    # MySQL容器执行脚本
    mv /opt/package/conf/mysql.sh /opt/docker/files

    chmod +x /opt/docker/files/mysql.sh
    sed -i 's/\r$//' /opt/docker/files/mysql.sh
    sed -i 's/\r$//' /opt/docker/files/sql/*.sql
    sed -i "s/mysqlPassword=/mysqlPassword=\"${mysqlPassword}\"/" /opt/docker/files/mysql.sh

    # 导入sql数据
    nohup sudo docker exec mysql bash /opt/docker/files/mysql.sh >/opt/docker/mysql/logs/sql.log 2>&1
}

installMqtt() {
  echo "启动mqtt ... "
  docker run -d --name emqx --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 1883:1883 -p 8083:8083 -p 8084:8084 -p 8883:8883 -p 18083:18083 emqx/emqx:5.4.1
}

# redis 配置文件修改
updateRedisConf() {
	echo "开始修改Redis配置文件..."
	# redis 配置
	mv /opt/package/conf/redis.conf /opt/docker/redis/conf
	sed -i "s/requirepass/requirepass ${redisPassword}/g" /opt/docker/redis/conf/redis.conf
}

# 启动 redis
installRedis() {
	# redis 目录创建
	mkdir -p /opt/docker/redis/conf/
	mkdir -p /opt/docker/redis/data/

	updateRedisConf
	echo "正在启动redis..."
	docker run -d --name redis --privileged=true --restart=always --network blog_network --ip 172.18.0.6 -p 6379:6379 -v /opt/docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /opt/docker/redis/data/:/data/  -v /opt/docker/files/:/opt/docker/files/ redis:6.2.5 redis-server /etc/redis/redis.conf
}

startJar() {
  sleep 5m
  echo "启动pi项目 ... "
  mkdir -p /opt/docker/files/jar
  mkdir -p /opt/docker/files/logs
  # 项目相关文件
  mv /opt/package/jar/* /opt/docker/files/jar
  # win和linux字符引起的错误
  sed -i 's/\r$//' /opt/docker/files/jar/run.sh
  cd /opt/docker/files/jar
  docker build -t pi:1 .
  docker run -d --name pi --privileged=true --cap-add=SYS_ADMIN --restart=always --network blog_network --ip 172.18.0.5 -p 10201:10201 -p 9092:9092 -p 5005:5005 -v /opt/docker/files:/opt/docker/files pi:1
}

startPy() {
    mkdir -p /opt/docker/files/python
    mv /opt/package/python/* /opt/docker/files/python
    cd /opt/docker/files/python
    nohup python -u webSocket.py > output.log 2>&1 &
}

main() {
  timer_start=`date "+%Y-%m-%d %H:%M:%S"`

  dockerStart
  unzipPi
  installConda
  dockerLoad
  installJdk
  installMysql
  installMqtt
  installRedis

  startJar
  startPy

  timer_end=`date "+%Y-%m-%d %H:%M:%S"`
  duration=`echo $(($(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}"))) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
  echo "脚本执行完成 耗时： $duration "
  exit 0
}

main

# 压缩包pi.zip目录
# pi.zip
# - images        # 存放docker镜像文件
# - jar           # 存放博客jar包
# - sql           # 存放博客sql文件
# - conf          # 存放需要替换的配置文件

