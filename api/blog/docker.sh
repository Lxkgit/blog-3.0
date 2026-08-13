#!/bin/sh



# 参数部分
# 下载失败重新尝试次数
reload=10
# 默认服务配置
profile="pro"


# 将命令赋值给变量
command=""

# 服务器部署文件信息
SERVICE_INFO_FILE="/opt/package/info.conf"

# MySQL登陆密码
mysqlPassword="MySql@Admin123*."
# redis登陆密码
redisPassword="redis-960@*"
# es登录密码
elasticsearchPassword="elasticsearch-960@*"
# minio登录密码
minioPassword="minio-960@*"
# ftp登录默认用户
ftpUsername="system"
ftpPassword="Ftp@Admin123*."

# 本机ip
hostIp=""
# 上次部署ip
lastIp=""

# 系统颜色变量
RED='\033[31m'
GREEN='\033[32m'
YELLOW='\033[33m'
BLUE='\033[34m'
NC='\033[0m'

# 服务器相关依赖下载
util(){
	echo "${YELLOW}下载服务器环境所需依赖...${NC}"
	apt-get update
	# 压缩解压工具
	apt install -y unzip zip lrzsz
}

# 解压上传的文件
unzipBlog() {
	echo "${YELLOW}开始解压博客文件...${NC}"
	# 上传部署压缩包解压目录
	mkdir -p /opt/package
  mv ./blog.zip /opt/package/
  unzip /opt/package/blog.zip -d /opt/package/
}

getServiceIp() {

  if [ "$profile" = "pro" ]; then
    hostIp=$(curl -4 -s ifconfig.me)
  else
    hostIp=$(ip route get 8.8.8.8 | awk '{print $7; exit}')
  fi

  if [ -f "$SERVICE_INFO_FILE" ]; then
    sed -i 's/\r$//' ${SERVICE_INFO_FILE}
    # shellcheck disable=SC1090
    . "${SERVICE_INFO_FILE}"
  else
    echo "${YELLOW}首次部署，未找到历史配置文件${NC}"
  fi

  echo "${BLUE}====================================${NC}"
  echo "上次部署IP: ${RED}${lastIp}${NC}"
  echo "本次部署IP: ${GREEN}${hostIp}${NC}"
  echo "${BLUE}====================================${NC}"

}

# 添加4g的虚拟内存
addVirtualMemory() {
	echo "${YELLOW}开始创建虚拟内存...${NC}"
	cd /usr || exit
	mkdir swap
	cd swap/ || exit
	dd if=/dev/zero of=/usr/swap/swapfile bs=1M count=4096
	du -sh /usr/swap/swapfile
	mkswap /usr/swap/swapfile
	swapon /usr/swap/swapfile
	free -m
	echo "/usr/swap/swapfile swap swap defaults 0 0"  >> /etc/fstab
}

# 安装docker
startDocker() {

  echo "${YELLOW}开始安装docker...${NC}"

	# docker镜像存放目录 全部容器共享目录
	mkdir -p /etc/docker /opt/docker/images /opt/docker/files

	# 配置docker下载镜像源
	rm -rf /etc/docker/daemon.json
	touch /etc/docker/daemon.json
	echo "{"  >> /etc/docker/daemon.json
	echo '  "registry-mirrors": ['  >> /etc/docker/daemon.json
	echo '      "https://docker.m.daocloud.io",'  >> /etc/docker/daemon.json
	echo '      "https://docker.1panel.live"'  >> /etc/docker/daemon.json
	echo "  ]"  >> /etc/docker/daemon.json
	echo "}"  >> /etc/docker/daemon.json

  # 离线安装docker
  installDocker

	# 创建自定义网络
	docker network create --subnet=172.18.0.0/24 blog_network

  # 离线导入docker镜像
  checkAndImportImages

  # 在线下载docker镜像
	dockerLoad
}

# 安装docker
installDocker() {
  cp /opt/docker/ci/code/blog-3.0/api/blog/docker/docker-27.1.1.tgz /root
  tar -zxvf /root/docker-27.1.1.tgz -C /root
  sudo cp /root/docker/* /usr/bin/
  cp /opt/docker/ci/code/blog-3.0/api/blog/docker/docker.service /etc/systemd/system/

  chmod +x /etc/systemd/system/docker.service
  systemctl daemon-reload

  # 使docker开机自启
  systemctl enable docker.service

  # 启动docker服务
  systemctl start docker

}

# 如果有镜像文件可以直接导入
checkAndImportImages() {
  IMAGE_FILE="/opt/docker/ci/code/blog-3.0/api/blog/docker/blog_docker_images_x86.tar.gz"
  [ ! -f "$IMAGE_FILE" ] && {
      echo "${YELLOW}镜像文件不存在: $IMAGE_FILE${NC}"
      return  1
  }
  echo "${YELLOW}开始导入镜像包...${NC}"
  docker load -i "$IMAGE_FILE"
  echo "${GREEN}导入完成${NC}"
}

# 镜像文件重新下载
reLoad() {
  count=1
  while [ $count -le "$reload" ]; do
    eval "$command"
    if [ $? -eq 0 ]; then
      echo "${GREEN}镜像文件下载成功...${NC}"
      break
    else
      echo "${YELLOW}第 ${count} 次尝试重新下载...${NC}"
      count=$((count + 1))
    fi
    if [ $count -gt "$reload" ]; then
      echo "${RED}docker镜像下载失败，脚本停止执行...${NC}"
      exit 1
    fi
  done
}

# docker 镜像下载
dockerLoad() {

  for image in \
      openjdk:17 \
      mysql:8.0.20 \
      fauria/vsftpd \
      nginx:1.20.2 \
      redis:6.2.5 \
      nacos/nacos-server:v2.4.3 \
      apache/rocketmq:5.1.4 \
      elasticsearch:7.14.1 \
      minio/minio:RELEASE.2025-05-24T17-08-30Z \
      bluenviron/mediamtx:1 \
      maven:3.9.9-eclipse-temurin-17 \
      node:22
  do
      echo "${YELLOW}开始下载 ${image} 镜像文件...${NC}"
      command="docker pull ${image}"
      reLoad
  done
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

# Java相关服务全部启动
startJava() {

  # 启动MySQL服务
  startMySQL

  # 启动 ftp
  startFtp

  # 安装 nginx
  startNginx

  # 启动 redis
  startRedis

  # 安装nacos
  startNacos

  # 启动 rocketmq
  startRocketMq

  # 安装 elasticsearch
  startElasticsearch

  # 启动 minio
  startMinio

  # 启动Java服务
  startJar

  # 启动python脚本
  startPy

  # 安装 MediaMTX
  startMediaMTX
}

# 修改 MySQL 配置文件
updateMysqlConf() {
	echo "${YELLOW}开始修改MySQL配置文件...${NC}"
	# mysql 配置
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/mysql/my.cnf /opt/docker/mysql/conf
	sed -i "s/password=/password=${mysqlPassword}/" /opt/docker/mysql/conf/my.cnf
}

# 更新MySQL数据IP地址，用于迁移服务器，替换旧ip
updateSqlData() {
  echo "${YELLOW}开始替换sql文件中IP地址...${NC}"
  echo "${BLUE}====================================${NC}"
  echo "替换前IP: ${RED}${lastIp}${NC}"
  echo "替换后IP: ${GREEN}${hostIp}${NC}"
  echo "${BLUE}====================================${NC}"
  # 配置文件中ip替换
  sed -i "s/${lastIp}/${hostIp}/g" /opt/docker/files/sql/nacos.sql
  sed -i "s/${lastIp}/${hostIp}/g" /opt/docker/files/sql/blog_auth.sql
  sed -i "s/${lastIp}/${hostIp}/g" /opt/docker/files/sql/blog_content.sql
  sed -i "s/${lastIp}/${hostIp}/g" /opt/docker/files/sql/blog_file.sql
  sed -i "s/${lastIp}/${hostIp}/g" /opt/docker/files/sql/blog_gateway.sql
}

# MySQL 数据修改与导入
insertSqlData() {
	echo "${YELLOW}开始导入MySQL数据...${NC}"
	mkdir -p /opt/docker/files/sql
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/mysql/mysql.sh /opt/docker/files/sql
	mv /opt/package/sql/* /opt/docker/files/sql
	chmod +x /opt/docker/files/sql/mysql.sh
	sed -i 's/\r$//' /opt/docker/files/sql/mysql.sh
	sed -i 's/\r$//' /opt/docker/files/sql/*.sql
	sed -i "s/mysqlPassword=/mysqlPassword=\"${mysqlPassword}\"/" /opt/docker/files/sql/mysql.sh

	updateSqlData

	# 导入sql数据
	nohup sudo docker exec mysql bash /opt/docker/files/sql/mysql.sh >/opt/docker/mysql/logs/importSql.log 2>&1
}

# 安装MySQL
startMySQL() {
	# mysql文件目录
	# 宿主机创建数据存放目录映射到容器
	mkdir -p /opt/docker/mysql/data
	# 宿主机创建配置文件目录映射到容器
	mkdir -p /opt/docker/mysql/conf
	# 宿主机创建日志目录映射到容器
	mkdir -p /opt/docker/mysql/logs
	
	updateMysqlConf
	echo "${YELLOW}正在启动mysql...${NC}"
	docker run -d --name mysql --privileged=true --restart=always --network blog_network --ip 172.18.0.3 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=${mysqlPassword} -v /opt/docker/mysql/data/:/var/lib/mysql -v /opt/docker/mysql/conf/my.cnf:/etc/mysql/my.cnf -v /opt/docker/mysql/logs/:/var/log/mysql/ -v /opt/docker/files/:/opt/docker/files/ mysql:8.0.20
	
	insertSqlData
}

# 创建 ftp 工作目录
createFtpDir() {
	mkdir -p /opt/docker/files/ftp/system/temp/
	chmod 777 /opt/docker/files/ftp/system/temp/
}

# 启动 ftp
startFtp() {
  echo "${YELLOW}正在启动ftp...${NC}"
  createFtpDir
  docker run -d --name vsftpd --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 61120:20 -p 61121:21 -p 61122-61199:61122-61199 -e FTP_USER=${ftpUsername} -e FTP_PASS=${ftpPassword} -e PASV_MIN_PORT=61122 -e PASV_MAX_PORT=61199 -e PASV_ADDRESS=49.232.129.253 -v /opt/docker/files/ftp:/home/vsftpd fauria/vsftpd
}

# 安装 nginx
startNginx() {
  # nginx 配置文件
  cp -r /opt/docker/ci/code/blog-3.0/api/blog/docker/nginx /opt/docker

  # 脚本文件去掉 Windows 换行符 \r
  sed -i 's/\r$//' /opt/docker/nginx/web/*.sh
  # 授权可执行
  chmod +x /opt/docker/nginx/web/*.sh

  echo "${YELLOW}正在启动nginx...${NC}"
  /opt/docker/nginx/web/updateWeb.sh
  docker run -d --name nginx-other --restart=always --network blog_network -v /opt/docker/nginx/other/conf/nginx.conf:/etc/nginx/nginx.conf:ro nginx:1.20.2
  docker run -d --name nginx-router --restart=always --network blog_network -p 80:80 -v /opt/docker/nginx/router/conf/nginx.conf:/etc/nginx/nginx.conf:ro nginx:1.20.2
}

# redis 配置文件修改
updateRedisConf() {
	echo "${YELLOW}开始修改Redis配置文件...${NC}"
	# redis 配置
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/redis/redis.conf /opt/docker/redis/conf
	sed -i "s/requirepass/requirepass ${redisPassword}/g" /opt/docker/redis/conf/redis.conf
}

# 启动 redis
startRedis() {
	# redis 目录创建
	mkdir -p /opt/docker/redis/conf/
	mkdir -p /opt/docker/redis/data/
	
	updateRedisConf
	echo "${YELLOW}正在启动redis...${NC}"
	docker run -d --name redis --privileged=true --restart=always --network blog_network --ip 172.18.0.6 -p 6379:6379 -v /opt/docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /opt/docker/redis/data/:/data/  -v /opt/docker/files/:/opt/docker/files/ redis:6.2.5 redis-server /etc/redis/redis.conf
}

# 安装nacos
startNacos() {
  echo "${YELLOW}正在启动nacos...${NC}"
  docker run -d --name nacos --privileged=true --restart=always --network blog_network --ip 172.18.0.7 -p 8848:8848 -p 9848:9848 -p 9849:9849 -e JVM_XMS=256m -e JVM_XMX=256m -e MODE=standalone -e PREFER_HOST_MODE=hostname -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=172.18.0.3 -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_PASSWORD=${mysqlPassword} -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_DB_PARAM='characterEncoding=utf8&connectTimeout=10000&socketTimeout=30000&autoReconnect=true&serverTimezone=UTC&allowPublicKeyRetrieval=true' nacos/nacos-server:v2.4.3
}

# 添加 rocketmq 配置文件
updateRocketMq() {
	# rocketMq 目录创建
	# 创建namesrv数据存储路径
	mkdir -p /opt/docker/rocketmq/namesrv/logs
	mkdir -p /opt/docker/rocketmq/namesrv/store
	# 创建broker
	mkdir -p /opt/docker/rocketmq/broker/logs
	mkdir -p /opt/docker/rocketmq/broker/store
	mkdir -p /opt/docker/rocketmq/broker/conf
	
	# rocketmq broker配置文件
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/mq/broker.conf /opt/docker/rocketmq/broker/conf/
}

# 启动 rocketmq
startRocketMq() {
  echo "${YELLOW}正在启动rocketmq...${NC}"
  updateRocketMq
  # rmqnamesrv
  docker run -d --name rmqnamesrv --privileged=true --restart=always  --network blog_network --ip 172.18.0.8 -p 9876:9876 -e "MAX_POSSIBLE_HEAP=100000000" -e "MAX_HEAP_SIZE=256M" -e "HEAP_NEWSIZE=128M" -v /opt/docker/rocketmq/namesrv/logs:/home/rocketmq/logs -v /opt/docker/rocketmq/namesrv/store:/root/store apache/rocketmq:5.1.4 sh mqnamesrv
  # rmqbroker
  docker run -d --name rmqbroker --privileged=true --restart=always --network blog_network --ip 172.18.0.9 -p 10911:10911 -p 10909:10909 -e "NAMESRV_ADDR=172.18.0.8:9876"  -e "MAX_POSSIBLE_HEAP=200000000" -e "MAX_HEAP_SIZE=256M" -e "HEAP_NEWSIZE=256M" -v /opt/docker/rocketmq/broker/logs:/root/logs -v /opt/docker/rocketmq/broker/store:/root/store -v /opt/docker/rocketmq/broker/conf/broker.conf:/home/rocketmq/broker.conf apache/rocketmq:5.1.4 sh mqbroker -c /home/rocketmq/broker.conf
}

# 修改 elasticsearch 配置文件
updateElasticsearchConf() {
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/es/elasticsearch.yml /opt/docker/elasticsearch/config/
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/es/elasticsearch.sh /opt/docker/files/
	chmod +x /opt/docker/files/elasticsearch.sh
	sed -i 's/\r$//' /opt/docker/files/elasticsearch.sh
	sed -i "s/elasticPassword=/elasticPassword=${elasticsearchPassword}/g" /opt/docker/files/elasticsearch.sh
}

# 安装 elasticsearch
startElasticsearch() {
	mkdir -p /opt/docker/elasticsearch/data
	mkdir -p /opt/docker/elasticsearch/plugins
	mkdir -p /opt/docker/elasticsearch/config
	chmod -R 777 /opt/docker/elasticsearch/

	updateElasticsearchConf
	echo "${YELLOW}正在启动elasticsearch...${NC}"
	docker run --name elasticsearch -p 9200:9200 -p 9300:9300 --restart=always -e ES_JAVA_OPTS="-Xms128m -Xmx256m" -e "discovery.type=single-node" -v /opt/docker/elasticsearch/data:/usr/share/elasticsearch/data -v /opt/docker/elasticsearch/plugins:/usr/share/elasticsearch/plugins -v /opt/docker/elasticsearch/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12 -v /opt/docker/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /opt/docker/files/:/opt/docker/files/ --network blog_network --ip 172.18.0.10 -d elasticsearch:7.14.1
	
	nohup sudo docker exec elasticsearch bash /opt/docker/files/elasticsearch.sh >/opt/docker/files/es.log 2>&1
}

# minio 文件导入
importMinio() {
	sleep 1m
	# minio 导入文件
	mkdir -p /opt/docker/minio/blog
	mv /opt/package/files/files.zip /opt/docker/minio/
	unzip /opt/docker/minio/files.zip -d /opt/docker/minio/blog

	# minio 数据导入
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/minio/mc /opt/docker/minio/
	cd /opt/docker/minio || exit
	chmod +x mc
	./mc alias set local http://172.18.0.11:9000 minio "${minioPassword}"
	./mc mb local/blog
	./mc mirror --overwrite /opt/docker/minio/blog/ local/blog
	./mc anonymous set download local/blog

	# minio 权限配置
	cp /opt/docker/ci/code/blog-3.0/api/blog/docker/minio/public-policy.json /opt/docker/minio/
	./mc anonymous set-json public-policy.json local/blog

	# 导入文件后删除数据
	rm -rf /opt/docker/minio/blog
	rm -rf /opt/docker/minio/files.zip
}

# 启动 minio
startMinio() {
	echo "${YELLOW}正在启动minio...${NC}"
	docker run --name minio --network blog_network --ip 172.18.0.11 -p 9000:9000 -p 9001:9001 --restart=always -e "MINIO_ROOT_USER=minio" -e "MINIO_ROOT_PASSWORD=${minioPassword}" -e "MINIO_BROWSER_REDIRECT_URL=http://172.18.0.11:9001/minio/ui/" -v /opt/docker/files/minio:/data -v /mnt/config:/root/.minio -d minio/minio:RELEASE.2025-05-24T17-08-30Z server /data --console-address ":9001"
	importMinio
}



# 启动Java服务
startJar() {
  # 创建目录日志与服务
  mkdir -p /opt/docker/files/logs
  mkdir -p /opt/docker/files/jar

  # 复制全部配置文件
  cp /opt/docker/ci/code/blog-3.0/api/blog/jar/updateJar.sh /opt/docker/files/jar

  # 等待nacos启动
  echo "${YELLOW}3分钟后启动博客服务...${NC}"
  sleep 3m
  chmod +x /opt/docker/files/jar/updateJar.sh
  sed -i 's/\r$//' /opt/docker/files/jar/updateJar.sh
  /opt/docker/files/jar/updateJar.sh
}

# python 脚本执行环境配置
buildPyEnv() {
	echo "${YELLOW}安装python环境 ...${NC}"
  mkdir /opt/python

  echo "exit 0" > /usr/sbin/needrestart
  chmod +x /usr/sbin/needrestart

  apt-get install -y python3 python3-pip python3-venv

  python3 -m venv /opt/python || exit 1

  /opt/python/bin/python -m pip install --upgrade pip
  /opt/python/bin/python -m pip install websockets psutil

}

# 启动python脚本
startPy() {
  buildPyEnv
  # Java服务启动较慢，等待Java服务完全启动后进行连接
  echo "${YELLOW}8分钟后启动socket脚本...${NC}"
  sleep 8m
  mkdir -p /opt/docker/files/socket/code
  cp -r /opt/docker/ci/code/blog-3.0/socket/* /opt/docker/files/socket/code
  chmod +x /opt/docker/files/socket/code/web_socket.py
  sed -i 's/\r$//' /opt/docker/files/socket/code/web_socket.py
  chmod +x /opt/docker/files/socket/code/shell/*.sh
  sed -i 's/\r$//' /opt/docker/files/socket/code/shell/*.sh

  # 重启脚本
  cp /opt/docker/ci/code/blog-3.0/api/blog/conf/restart_python.sh /opt/docker/files/socket
  sed -i 's/\r$//' /opt/docker/files/socket/restart_python.sh
  chmod +x /opt/docker/files/socket/restart_python.sh
}

# 安装 MediaMTX
startMediaMTX() {
  mkdir -p /opt/docker/mediamtx/config
  cp /opt/docker/ci/code/blog-3.0/api/blog/docker/mediamtx/mediamtx.yml /opt/docker/mediamtx/config
  docker run --name mediamtx --restart=always --privileged=true --network blog_network --ip 172.18.0.14 -p 8554:8554 -p 8889:8889 -p 8189:8189/udp -e TZ=Asia/Shanghai -v /etc/localtime:/etc/localtime:ro -v /etc/timezone:/etc/timezone:ro -v /opt/docker/mediamtx/config/mediamtx.yml:/mediamtx.yml -v /opt/docker/mediamtx/recordings:/opt/docker/mediamtx/recordings -d bluenviron/mediamtx:1
  startFrps
}

# 安装 Frp 服务端
# wget https://github.com/fatedier/frp/releases/download/v0.68.0/frp_0.68.0_linux_amd64.tar.gz
# frps服务由守护线程启动
startFrps() {
  mkdir -p /opt/frps
  cp /opt/docker/ci/code/blog-3.0/api/blog/soft/frps/frp_0.68.0_linux_amd64.tar.gz /opt/frps
  tar -zxvf /opt/frps/frp_0.68.0_linux_amd64.tar.gz -C /opt/frps
  cp /opt/docker/ci/code/blog-3.0/api/blog/soft/frps/frps.ini /opt/frps/frp_0.68.0_linux_amd64
  cp /opt/docker/ci/code/blog-3.0/api/blog/soft/frps/restartFrps.sh /opt/frps/frp_0.68.0_linux_amd64

  sed -i 's/\r$//' /opt/frps/frp_0.68.0_linux_amd64/restartFrps.sh
  chmod +x /opt/frps/frp_0.68.0_linux_amd64/restartFrps.sh
}

# 守护除 docker 之外的基本启动
startWatchDog() {

  # 守护线程目录
  mkdir -p /opt/watchdog

  # 开机唤醒守护线程配置
  cp /opt/docker/ci/code/blog-3.0/api/blog/soft/watchdog/watchdog.service /etc/systemd/system/
  sed -i 's/\r$//' /etc/systemd/system/watchdog.service

  # 守护线程
  cp /opt/docker/ci/code/blog-3.0/api/blog/soft/watchdog/watchdog.sh /opt/watchdog
  sed -i 's/\r$//' /opt/watchdog/watchdog.sh
  chmod +x /opt/watchdog/watchdog.sh

  # 重新加载systemd配置
  sudo systemctl daemon-reload
  # 开机自启
  sudo systemctl enable watchdog.service
  # 立即启动
  sudo systemctl start watchdog.service
}

# 主函数
main() {
  timer_start=$(date "+%Y-%m-%d %H:%M:%S")

  # 前置工具配置
  util

  # 解压压缩包
  unzipBlog

  # 获取服务器ip
  getServiceIp

  # 添加虚拟内存
  addVirtualMemory

  # 构建启动项目
  ciBuild

	# 安装docker
  startDocker

  # Java相关服务全部启动
  startJava

  # 守护线程
  startWatchDog

  timer_end=$(date "+%Y-%m-%d %H:%M:%S")
  diff=$(( $(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}") ))
  duration=$(printf "%02d:%02d:%02d" $((diff/3600)) $((diff%3600/60)) $((diff%60)))
  echo "${YELLOW}脚本执行完成 耗时： $duration ${NC}"
	exit 0
}

# 获取参数
while getopts "n:e:" arg
  do
		case "$arg" in
		  n)
			  # 指定镜像文件重新下载次数
				reload=$OPTARG
				echo "$reload"
				;;
      e)
        profile=$OPTARG
        ;;
			?)
				echo "${RED}没有找到这条命令 ...${NC}"
				exit 1
				;;
		esac
	done



main
