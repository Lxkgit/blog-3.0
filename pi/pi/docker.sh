#!/bin/bash

export DEBIAN_FRONTEND=noninteractive
export NEEDRESTART_MODE=a

# MySQL登陆密码
mysqlPassword="MySql@Admin123*."

# redis登陆密码
redisPassword="redis-960@*"

# 系统颜色变量
RED='\033[31m'
GREEN='\033[32m'
YELLOW='\033[33m'
BLUE='\033[34m'
NC='\033[0m'

# 服务器相关依赖下载
util(){
	echo "${YELLOW}下载服务器环境所需依赖...${NC}"
	waitAptLock
	# 压缩解压工具
	apt-get install -y unzip zip
}

# 等待解锁方法
waitAptLock() {
  echo "${YELLOW}等待 apt/dpkg 锁释放...${NC}"

  while \
      fuser /var/lib/dpkg/lock >/dev/null 2>&1 || \
      fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1 || \
      fuser /var/lib/apt/lists/lock >/dev/null 2>&1 || \
      fuser /var/cache/apt/archives/lock >/dev/null 2>&1 || \
      pgrep -x apt >/dev/null || \
      pgrep -x apt-get >/dev/null || \
      pgrep -x dpkg >/dev/null || \
      pgrep -x unattended-upgrade >/dev/null
  do
      echo "${YELLOW}apt-get 正在运行，等待 3 秒...${NC}"
      sleep 3
  done

  echo "${GREEN}apt-get 已空闲${NC}"
}

# 依赖文件解压
unzipPi() {
  echo "${YELLOW}开始解压树莓派文件...${NC}"
  mkdir -p /opt/package
  mv ./pi.zip /opt/package/
  unzip /opt/package/pi.zip -d /opt/package
}

# 自动化构建启动项目
ciBuild() {
  # 脚本文件移动
  mkdir -p /opt/docker
  cp -r /opt/package/ci /opt/docker/

  # ssh 密钥授权文件
  sshConfig

  # 脚本文件去掉 Windows 换行符 \r
  find /opt/docker/ci/shell -type f -name "*.sh" -exec sed -i 's/\r$//' {} \;
  # 授权可执行
  find /opt/docker/ci/shell -type f -name "*.sh" -exec chmod +x {} \;

  # 拉取代码
  /opt/docker/ci/shell/updateCode.sh
}

sshConfig(){

  mkdir -p /root/.ssh
  tar xzvf /opt/docker/ci/ssh/ssh-gitee-backup.tar.gz -C /
  chmod 700 /root/.ssh
  chmod 600 /root/.ssh/id_ed25519
  chmod 644 /root/.ssh/id_ed25519.pub
  chmod 644 /root/.ssh/known_hosts
}

# 安装并配置docker
startDocker() {

  echo "${YELLOW}开始安装docker...${NC}"

  cp /opt/docker/ci/code/blog-3.0/pi/pi/docker/docker-27.1.1.tgz /root
  tar -zxvf /root/docker-27.1.1.tgz -C /root
  sudo cp /root/docker/* /usr/bin/
  cp /opt/docker/ci/code/blog-3.0/pi/pi/docker/docker.service /etc/systemd/system/

  chmod +x /etc/systemd/system/docker.service
  systemctl daemon-reload

  # 使docker开机自启
  systemctl enable docker.service

  # 启动docker服务
  systemctl start docker

  until docker info >/dev/null 2>&1; do
    echo "等待docker服务启动 ... "
    sleep 1
  done

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

  # 挂载硬盘
  mountDisk

  # 启动NAS服务
  createSamba
}

# 修改 MySQL 配置文件
updateMysqlConf() {
	echo "开始修改MySQL配置文件..."
	# mysql 配置
	cp /opt/docker/ci/code/blog-3.0/pi/pi/docker/mysql/my.cnf /opt/docker/mysql/conf
	sed -i "s/password=/password=${mysqlPassword}/" /opt/docker/mysql/conf/my.cnf
}

# MySQL 数据修改与导入
insertSqlData() {
  echo "开始修改MySQL数据恢复脚本文件..."
  # MySQL数据文件
  mkdir -p /opt/docker/files/sql
  mv /opt/package/sql/* /opt/docker/files/sql
  # MySQL容器执行脚本
  cp /opt/docker/ci/code/blog-3.0/pi/pi/docker/mysql/mysql.sh /opt/docker/files

  chmod +x /opt/docker/files/mysql.sh
  sed -i 's/\r$//' /opt/docker/files/mysql.sh
  sed -i 's/\r$//' /opt/docker/files/sql/*.sql
  sed -i "s/mysqlPassword=/mysqlPassword=\"${mysqlPassword}\"/" /opt/docker/files/mysql.sh

  # 导入sql数据
  nohup sudo docker exec mysql bash /opt/docker/files/mysql.sh >/opt/docker/mysql/logs/sql.log 2>&1
}

# 安装MySQL
startMySQL() {
  echo "${YELLOW}正在启动mysql...${NC}"

  # docker 导入MySQL镜像
  docker load -i /opt/package/images/mysql.tar

	# 宿主机创建数据存放目录 配置文件目录 日志目录
	mkdir -p /opt/docker/mysql/data /opt/docker/mysql/conf /opt/docker/mysql/logs

	updateMysqlConf

  docker run -d --name mysql --privileged=true --restart=always --network blog_network --ip 172.18.0.3 -p 3306:3306 -e MYSQL_ROOT_PASSWORD="${mysqlPassword}" -v /opt/docker/files:/opt/docker/files  mysql/mysql-server:8.0.32

  insertSqlData
}

# 安装MQTT
startMQTT() {
  echo "${YELLOW}正在启动mqtt ... ${NC}"
  # docker 导入 MQTT 镜像
  docker load -i /opt/package/images/emqx.tar
  docker run -d --name emqx --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 1883:1883 -p 8083:8083 -p 8084:8084 -p 8883:8883 -p 18083:18083 emqx/emqx:5.4.1
}

# redis 配置文件修改
updateRedisConf() {
	echo "${YELLOW}开始修改Redis配置文件...${NC}"
	# redis 配置
	cp /opt/docker/ci/code/blog-3.0/pi/pi/soft/redis/redis.conf /opt/docker/redis/conf
	sed -i "s/requirepass/requirepass ${redisPassword}/g" /opt/docker/redis/conf/redis.conf
}

# 安装 redis
startRedis() {
  echo "${YELLOW}正在启动redis...${NC}"
	# redis 目录创建
	mkdir -p /opt/docker/redis/conf /opt/docker/redis/data

	updateRedisConf
	# docker 导入 MQTT 镜像
  docker load -i /opt/package/images/redis.tar
	docker run -d --name redis --privileged=true --restart=always --network blog_network --ip 172.18.0.6 -p 6379:6379 -v /opt/docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /opt/docker/redis/data/:/data/  -v /opt/docker/files/:/opt/docker/files/ redis:6.2.5 redis-server /etc/redis/redis.conf
}

# 启动Java服务
startJar() {

  docker load -i /opt/package/images/maven.tar

  # 创建目录日志与服务
  mkdir -p /opt/docker/files/logs
  mkdir -p /opt/docker/files/jar

  # 复制全部配置文件
  cp /opt/docker/ci/code/blog-3.0/pi/pi/jar/updateJar.sh /opt/docker/files/jar

  # 等待nacos启动
  echo "${YELLOW}3分钟后启动pi项目 ... ${NC}"
  sleep 3m
  chmod +x /opt/docker/files/jar/updateJar.sh
  sed -i 's/\r$//' /opt/docker/files/jar/updateJar.sh
  /opt/docker/files/jar/updateJar.sh
}

# python 脚本执行环境配置
buildPyEnv() {
	echo "${YELLOW}安装python环境 ...${NC}"

  waitAptLock
  sudo apt-get install -y python3 python3-pip python3-venv

  python3 -m venv /opt/python
  source /opt/python/bin/activate

  /opt/python/bin/python -m pip install --upgrade pip
  /opt/python/bin/python -m pip install websockets psutil

	# 退出python虚拟环境
	deactivate
}

# 启动python脚本
startPy() {
  buildPyEnv
  # Java服务启动较慢，等待Java服务完全启动后进行连接
  echo "${YELLOW}3分钟后启动socket脚本...${NC}"
  sleep 3m

  # 重启脚本
  cp -r /opt/docker/ci/code/blog-3.0/pi/pi/soft/socket /opt/soft
  sed -i 's/\r$//' /opt/soft/socket/*.sh
  chmod +x /opt/soft/socket/*.sh

  /opt/soft/socket/updateSocket.sh
}

# 挂载硬盘
mountDisk() {

  echo "安装 NTFS 热插拔..."

  sudo apt install -y ntfs-3g

  # 处理硬盘挂载配置文件
  cp /opt/docker/ci/code/blog-3.0/pi/pi/conf/automount@.service /etc/systemd/system/
  cp /opt/docker/ci/code/blog-3.0/pi/pi/conf/99-automount.rules /etc/udev/rules.d/
  sudo sed -i 's/\r$//' /etc/systemd/system/automount@.service
  sudo sed -i 's/\r$//' /etc/udev/rules.d/99-automount.rules

  sudo systemctl daemon-reload

  sudo udevadm control --reload-rules
  sudo udevadm trigger

  echo "NTFS 热插拔安装完成"
}

# 配置 NAS 服务
createSamba() {

  # 安装 Samba
  apt-get install -y samba smbclient

  # 创建一个专用 NAS 用户
  if ! id nas >/dev/null 2>&1; then
    useradd -M -s /usr/sbin/nologin nas
  fi

  # 创建 Samba 用户
  echo -e "nas\nnas" | smbpasswd -a -s nas
  smbpasswd -e nas

  # 添加 Samba 配置
  cp /opt/docker/ci/code/blog-3.0/pi/pi/soft/samba/samba.conf /etc/samba/smb.conf

  # 添加 nas 用户目录权限
  chown -R nas:nas /mnt
#  chmod -R 777 /mnt

  # 检查 Samba 配置
  testparm -s

  # 重启 Samba
  systemctl restart smbd
  systemctl enable smbd
}

# 安装树莓派 CSI 摄像头服务
startPISci() {

  echo "开始安装 CSI 摄像头环境"

  waitAptLock
  echo "安装编译依赖"
  sudo apt-get install -y  git unzip cmake meson ninja-build build-essential pkg-config python3-pip python3-yaml \
  python3-ply python3-jinja2 libgnutls28-dev openssl libexpat1-dev libboost-dev libboost-system-dev libboost-filesystem-dev \
  libboost-program-options-dev libavutil-dev libavcodec-dev libavdevice-dev libavformat-dev libswscale-dev libexif-dev \
  libjpeg-dev libpng-dev libtiff-dev libepoxy-dev libdrm-dev libwebp-dev libx11-dev libevent-dev libyaml-dev libudev-dev \
  libegl1-mesa-dev libgles2-mesa-dev ffmpeg v4l-utils

  #################################################
  # 编译 libcamera
  #################################################

  echo "开始安装 libcamera 0.7.0"

  # 删除 Ubuntu 自带版本，避免冲突
  sudo apt-get remove -y libcamera-dev libcamera0 2>/dev/null || true
  unzip /opt/package/csi/libcamera.zip -d /root
  # 安装 libpisp
  unzip /opt/package/csi/libpisp-1.3.0.zip -d /root/libcamera
  cp /opt/package/csi/googletest-release-1.11.0.zip /root/libcamera/subprojects/packagefiles/gtest-1.11.0.zip
  cp /opt/package/csi/gtest_1.11.0-1_patch.zip /root/libcamera/subprojects/packagefiles/gtest_1.11.0-1_patch.zip
  cd /root/libcamera || exit 1
  git checkout v0.7.0
  pip3 install --upgrade meson
  rm -rf build
  meson setup build --buildtype=release -Dpipelines=rpi/vc4
  ninja -C build
  ninja -C build install
  ldconfig

  #################################################
  # 设置 pkg-config
  #################################################
  echo "配置 libcamera pkg-config"
  export PKG_CONFIG_PATH=/usr/local/lib/aarch64-linux-gnu/pkgconfig:/usr/local/lib/pkgconfig:$PKG_CONFIG_PATH
  echo "export PKG_CONFIG_PATH=/usr/local/lib/aarch64-linux-gnu/pkgconfig:/usr/local/lib/pkgconfig:\$PKG_CONFIG_PATH" >> /etc/profile
  echo "检查 libcamera"
  pkg-config --modversion libcamera

  #################################################
  # 编译 rpicam-apps
  #################################################
  echo "开始编译 rpicam-apps"
  unzip /opt/package/csi/libcamera-apps.zip -d /root
  cd /root/libcamera-apps || exit 1
  rm -rf build
  PKG_CONFIG_PATH=/usr/local/lib/aarch64-linux-gnu/pkgconfig:/usr/local/lib/pkgconfig  meson setup build --buildtype=release

  # 关闭 libav，避免额外依赖
  meson configure build -Denable_libav=disabled
  ninja -C build
  ninja -C build install

  #################################################
  # 动态库配置
  #################################################
  echo "/usr/local/lib" > /etc/ld.so.conf.d/rpicam.conf
  echo "/usr/local/lib/aarch64-linux-gnu" >> /etc/ld.so.conf.d/rpicam.conf
  ldconfig

  #################################################
  # 验证
  #################################################
  echo "安装完成"
  echo "libcamera版本:"
  pkg-config --modversion libcamera

  echo "rpicam版本:"
  rpicam-hello --version

  echo "摄像头列表:"
  rpicam-hello --list-cameras

}


# 安装 MediaMTX
startMediaMTX() {

  startPISci

  mkdir -p /opt/docker/mediamtx/config
  cp /opt/docker/ci/code/blog-3.0/pi/pi/docker/mediamtx/mediamtx.yml /opt/docker/mediamtx/config
  docker load -i /opt/package/images/mediamtx_1_arm64.tar
  docker run --name mediamtx --network host -v /opt/docker/mediamtx/config/mediamtx.yml:/mediamtx.yml -v /opt/docker/mediamtx/recordings:/opt/docker/mediamtx/recordings -d bluenviron/mediamtx:1

  startFrpc
  startCamera
}

# 安装 Frp 客户端
# wget https://github.com/fatedier/frp/releases/download/v0.68.0/frp_0.68.0_linux_arm64.tar.gz
startFrpc() {
  mkdir -p /opt/soft/frpc
  cp /opt/docker/ci/code/blog-3.0/pi/pi/soft/frpc/frp_0.68.0_linux_arm64.tar.gz /opt/soft/frpc
  tar -zxvf /opt/soft/frpc/frp_0.68.0_linux_arm64.tar.gz -C /opt/soft/frpc
  cp /opt/docker/ci/code/blog-3.0/pi/pi/soft/frpc/frpc.ini /opt/soft/frpc/frp_0.68.0_linux_arm64
  cp /opt/docker/ci/code/blog-3.0/pi/pi/soft/frpc/restartFrpc.sh /opt/soft/frpc/frp_0.68.0_linux_arm64

  sed -i 's/\r$//' /opt/soft/frpc/frp_0.68.0_linux_arm64/restartFrpc.sh
  chmod +x /opt/soft/frpc/frp_0.68.0_linux_arm64/restartFrpc.sh
}

# 启动摄像头脚本文件位置，脚本由守护线程管理
startCamera() {
  cp /opt/docker/ci/code/blog-3.0/pi/pi/soft/camera /opt/soft

  sed -i 's/\r$//' /opt/soft/camera/*.sh
  chmod +x /opt/soft/camera/*.sh
}

# 守护除 docker 之外的基本启动
startWatchdog() {

  # 守护线程目录
  mkdir -p /opt/soft/watchdog

  # 开机唤醒守护线程配置
  cp /opt/docker/ci/code/blog-3.0/api/blog/soft/watchdog/watchdog.service /etc/systemd/system/
  sed -i 's/\r$//' /etc/systemd/system/watchdog.service

  # 守护线程 与 参数配置文件
  cp -r /opt/docker/ci/code/blog-3.0/api/blog/soft/watchdog/watchdog.sh /opt/soft/watchdog
  cp -r /opt/docker/ci/code/blog-3.0/pi/pi/soft/watchdog/param.sh /opt/soft/watchdog
  sed -i 's/\r$//' /opt/soft/watchdog/*.sh
  chmod +x /opt/soft/watchdog/*.sh

  # 重新加载systemd配置
  sudo systemctl daemon-reload
  # 开机自启
  sudo systemctl enable watchdog.service
  # 立即启动
  sudo systemctl start watchdog.service
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

  # 守护线程
  startWatchdog

  timer_end=$(date "+%Y-%m-%d %H:%M:%S")
  diff=$(( $(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}") ))
  duration=$(printf "%02d:%02d:%02d" $((diff/3600)) $((diff%3600/60)) $((diff%60)))
  echo "脚本执行完成 耗时： $duration "
  exit 0
}

main
