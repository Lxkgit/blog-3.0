#!/bin/bash

# MySQL登陆密码
mysqlPassword="MySql@Admin123*."

# redis登陆密码
redisPassword="redis-960@*"

# 服务器相关依赖下载
util(){
	echo "下载服务器环境所需依赖..."
	waitAptLock
	sudo apt update
	# 压缩解压工具
	apt install -y unzip zip
}

# 等待解锁方法
waitAptLock() {
    echo "等待 apt/dpkg 锁释放..."

    while fuser /var/lib/dpkg/lock >/dev/null 2>&1 || \
          fuser /var/lib/apt/lists/lock >/dev/null 2>&1 || \
          fuser /var/cache/apt/archives/lock >/dev/null 2>&1 || \
          fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1
    do
        echo "锁被占用，等待 3 秒..."
        sleep 3
    done

    echo "锁已释放，继续执行..."
}

# 依赖文件解压
unzipPi() {
  mkdir -p /opt/package
  unzip pi.zip -d /opt/package
}

# 安装并配置docker
startDocker() {
  echo "启动docker ... "

  mv /opt/package/docker/docker-27.1.1.tgz /root
  tar -zxvf /root/docker-27.1.1.tgz -C /root
  sudo cp /root/docker/* /usr/bin/
  mv /opt/package/docker/docker.service /etc/systemd/system/

  chmod +x /etc/systemd/system/docker.service
  systemctl daemon-reload

  # 使docker开机自启
  systemctl enable docker.service

  # 启动docker服务
  systemctl start docker

	# 创建自定义网络
	docker network create --subnet=172.18.0.0/24 blog_network
}

# Java相关服务全部启动
startJava() {

  # 启动MySQL服务
  startMySQL

  # 启动MQTT服务
  startMQTT

  # 启动 redis
  startRedis

  # 启动树莓派服务
  startJar

  # 启动python脚本
  startPy
}

# 安装MySQL
startMySQL() {
  echo "启动mysql ... "

  # docker 导入MySQL镜像
  docker load -i /opt/package/images/mysql.tar

	# 宿主机创建数据存放目录 配置文件目录 日志目录
	mkdir -p /opt/docker/mysql/data /opt/docker/mysql/conf /opt/docker/mysql/logs

	updateMysqlConf

  docker run -d --name mysql --privileged=true --restart=always --network blog_network --ip 172.18.0.3 -p 3306:3306 -e MYSQL_ROOT_PASSWORD="${mysqlPassword}" -v /opt/docker/files:/opt/docker/files  mysql/mysql-server:8.0.32

  insertSqlData
}

# 修改 MySQL 配置文件
updateMysqlConf() {
	echo "开始修改MySQL配置文件..."
	# mysql 配置
	mv /opt/package/conf/my.cnf /opt/docker/mysql/conf
	sed -i "s/password=/password=${mysqlPassword}/" /opt/docker/mysql/conf/my.cnf
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

# 安装MQTT
startMQTT() {
  echo "启动mqtt ... "
  # docker 导入 MQTT 镜像
  docker load -i /opt/package/images/emqx.tar
  docker run -d --name emqx --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 1883:1883 -p 8083:8083 -p 8084:8084 -p 8883:8883 -p 18083:18083 emqx/emqx:5.4.1
}

# redis 配置文件修改
updateRedisConf() {
	echo "开始修改Redis配置文件..."
	# redis 配置
	mv /opt/package/conf/redis.conf /opt/docker/redis/conf
	sed -i "s/requirepass/requirepass ${redisPassword}/g" /opt/docker/redis/conf/redis.conf
}

# 安装 redis
startRedis() {
  echo "启动redis ... "
	# redis 目录创建
	mkdir -p /opt/docker/redis/conf /opt/docker/redis/data

	updateRedisConf
	# docker 导入 MQTT 镜像
  docker load -i /opt/package/images/redis.tar
	docker run -d --name redis --privileged=true --restart=always --network blog_network --ip 172.18.0.6 -p 6379:6379 -v /opt/docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /opt/docker/redis/data/:/data/  -v /opt/docker/files/:/opt/docker/files/ redis:6.2.5 redis-server /etc/redis/redis.conf
}

# 启动树莓派服务
startJar() {
  echo "3分钟后启动pi项目 ... "
  sleep 3m
  mkdir -p /opt/docker/files/jar /opt/docker/files/logs
  # 项目相关文件
  mv /opt/package/jar/* /opt/docker/files/jar
  # win和linux字符引起的错误
  sed -i 's/\r$//' /opt/docker/files/jar/run.sh
  chmod +x /opt/docker/files/jar/run.sh
  sed -i 's/\r$//' /opt/docker/files/jar/restart.sh
  chmod +x /opt/docker/files/jar/restart.sh
  # shellcheck disable=SC2164
  cd /opt/docker/files/jar
  docker load -i /opt/package/images/jdk17.tar
  docker build -t pi:1 .
  docker run -d --name pi --privileged=true --cap-add=SYS_ADMIN --restart=always --network blog_network --ip 172.18.0.5 -p 10201:10201 -p 9092:9092 -p 5005:5005 -v /opt/docker/files:/opt/docker/files pi:1
}

# python 脚本执行环境配置
buildPyEnv() {
	echo "安装python3.9 ... "

  waitAptLock
  sudo apt install -y python3 python3-pip python3.12-venv

  python3 -m venv /opt/python
  source /opt/python/bin/activate

	/opt/python/bin/pip install websockets
	/opt/python/bin/pip install psutil
}

# 启动python脚本
startPy() {
  buildPyEnv
  # Java服务启动较慢，等待Java服务完全启动后进行连接
  echo "4分钟后启动python脚本 ... "
  sleep 4m
  mkdir -p /opt/docker/files/python/code
  mv /opt/package/python/* /opt/docker/files/python/code
  unzip /opt/docker/files/python/code/python.zip -d /opt/docker/files/python/code
  chmod +x /opt/docker/files/python/code/web_socket.py
  sed -i 's/\r$//' /opt/docker/files/python/code/web_socket.py
  chmod +x /opt/docker/files/python/code/shell/*.sh
  sed -i 's/\r$//' /opt/docker/files/python/code/shell/*.sh

  startPyDaemon
}

# python 脚本守护线程
startPyDaemon() {

  # 开机唤醒守护线程配置
  mv /opt/package/conf/websocket-watchdog.service /etc/systemd/system/
  sed -i 's/\r$//' /etc/systemd/system/websocket-watchdog.service

  # 守护线程
  mv /opt/package/conf/websocket_watchdog.sh /opt/docker/files/python
  sed -i 's/\r$//' /opt/docker/files/python/websocket_watchdog.sh
  chmod +x /opt/docker/files/python/websocket_watchdog.sh

  # 重启脚本
  mv /opt/package/conf/restart_python.sh /opt/docker/files/python
  sed -i 's/\r$//' /opt/docker/files/python/restart_python.sh
  chmod +x /opt/docker/files/python/restart_python.sh

  # 重新加载systemd配置
  sudo systemctl daemon-reload
  # 开机自启
  sudo systemctl enable websocket-watchdog.service
  # 立即启动
  sudo systemctl start websocket-watchdog.service
}

# 安装树莓派SCI摄像头服务
startPISci() {
  waitAptLock
  sudo apt update
  sudo DEBIAN_FRONTEND=noninteractive apt upgrade -y
  sleep 10m
  sudo apt install -y git cmake meson ninja-build build-essential python3-pip python3-yaml python3-ply libgnutls28-dev openssl libexpat1-dev libcamera-dev v4l-utils
  sudo apt install -y libboost-dev libboost-system-dev libboost-filesystem-dev libboost-program-options-dev
  sudo apt install -y libavutil-dev libexif-dev libjpeg-dev libtiff5-dev libpng-dev libavcodec-dev libavdevice-dev libavformat-dev libswscale-dev libepoxy-dev libdrm-dev libwebp-dev libx11-dev
  sudo apt install -y python3-jinja2 libevent-dev libyaml-dev libudev-dev libtiff-dev libegl1-mesa-dev libgles2-mesa-dev ffmpeg

  # 安装 0.7.0 版本 libcamera
  unzip /opt/package/csi/libcamera.zip -d /root
  unzip /opt/package/csi/libpisp-1.3.0.zip -d /root/libcamera
  cd /root/libcamera || exit
  git checkout v0.7.0
  meson setup build
  ninja -C build
  ninja -C build install
  ldconfig

  unzip /opt/package/csi/libcamera-apps.zip -d /root
  cd /root/libcamera-apps || exit
  meson setup build --buildtype=release
  meson configure build -Denable_libav=disabled
  ninja -C build
  ninja -C build install
  echo "/usr/local/lib" > /etc/ld.so.conf.d/rpicam.conf
  ldconfig
}

# 安装 MediaMTX
startMediaMTX() {

  startPISci

  mkdir -p /opt/docker/mediamtx/config
  mv /opt/package/conf/mediamtx.yml /opt/docker/mediamtx/config
  docker load -i /opt/package/images/mediamtx_1_arm64.tar
  docker run --name mediamtx --network host -v /opt/docker/mediamtx/config/mediamtx.yml:/mediamtx.yml -v /opt/docker/mediamtx/recordings:/opt/docker/mediamtx/recordings -d bluenviron/mediamtx:1

  startFrpc
}

# 安装 Frp 客户端
# wget https://github.com/fatedier/frp/releases/download/v0.68.0/frp_0.68.0_linux_arm64.tar.gz
startFrpc() {
  mkdir -p /opt/frpc
  mv /opt/package/soft/frp_0.68.0_linux_arm64.tar.gz /opt/frpc
  tar -zxvf /opt/frpc/frp_0.68.0_linux_arm64.tar.gz -C /opt/frpc
  mv /opt/package/conf/frpc.ini /opt/frpc/frp_0.68.0_linux_arm64
  nohup /opt/frpc/frp_0.68.0_linux_arm64/frpc -c /opt/frpc/frp_0.68.0_linux_arm64/frpc.ini > /opt/frpc/frp_0.68.0_linux_arm64/frpc.log 2>&1 &
}

main() {
  timer_start=$(date "+%Y-%m-%d %H:%M:%S")

  # 安装依赖工具
  util

  # 解压依赖文件
  unzipPi

  # 安装docker
  startDocker

  # 启动Java服务
  startJava

  # 启动树莓派SCI摄像头服务
  startMediaMTX

  timer_end=$(date "+%Y-%m-%d %H:%M:%S")
  diff=$(( $(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}") ))
  duration=$(printf "%02d:%02d:%02d" $((diff/3600)) $((diff%3600/60)) $((diff%60)))
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