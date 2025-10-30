#! /bin/bash

# 下载失败重新尝试次数
reload=10

# 将命令赋值给变量
command=""

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

oldIpAddr="49.232.129.253"

# 安装docker
dockerStart() {

	# docker镜像存放目录
	mkdir -p /etc/docker
	mkdir -p /opt/docker/images
	# docker 全部容器共享目录
	mkdir -p /opt/docker/files
	echo "开始安装docker..."

	# 配置docker下载镜像源
	mkdir -p /etc/docker
	rm -rf /etc/docker/daemon.json
	touch /etc/docker/daemon.json
	echo "{"  >> /etc/docker/daemon.json
	echo '  "registry-mirrors": ['  >> /etc/docker/daemon.json
	echo '      "https://docker.m.daocloud.io",'  >> /etc/docker/daemon.json
	echo '      "https://docker.1panel.live"'  >> /etc/docker/daemon.json
	echo "  ]"  >> /etc/docker/daemon.json
	echo "}"  >> /etc/docker/daemon.json

	# 一键安装docker
#	curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
  installDocker
  #	判断docker是否正确安装
	if [ $? -ne 0 ]; then
      echo "docker 安装失败, 脚本执行退出"
      exit 1
  fi
  if ! command -v docker &>/dev/null; then
      echo "docker 未正常启动 "
      exit 1
  fi

	# 启动docker
	sudo systemctl start docker
	# docker开始自启动
	systemctl enable docker.service
	# 创建自定义网络
	docker network create --subnet=172.18.0.0/24 blog_network
}

installDocker() {
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

}

# 镜像文件重新下载
reLoad() {
  count=1
  while [ $count -le $reload ]; do
    eval "$command"
    if [ $? -eq 0 ]; then
      echo "镜像文件下载成功..."
      break
    else
      echo "第 ${count} 次尝试重新下载..."
      ((count++))
    fi
    if [ $count -gt $reload ]; then
      echo "docker镜像下载失败，脚本停止执行..."
      exit 1
    fi
  done
}

# docker 镜像文件下载
dockerLoad() {

	echo "开始下载 mysql:8.0.20 镜像文件..."
	command="docker pull mysql:8.0.20"
	reLoad
	
	echo "开始下载 fauria/vsftpd 镜像文件..."
	command="docker pull fauria/vsftpd"
	reLoad

	echo "开始下载 nginx:1.20.2 镜像文件..."
	command="docker pull nginx:1.20.2"
	reLoad
	
	echo "开始下载 redis:6.2.5 镜像文件..."
	command="docker pull redis:6.2.5"
	reLoad
	
	echo "开始下载 nacos/nacos-server:v2.4.3 镜像文件..."
	command="docker pull nacos/nacos-server:v2.4.3"
	reLoad

	echo "开始下载 apache/rocketmq:5.1.4 镜像文件..."
	command="docker pull apache/rocketmq:5.1.4"
	reLoad

	echo "开始下载 elasticsearch:7.14.1 镜像文件..."
	command="docker pull elasticsearch:7.14.1"
	reLoad
	
	echo "开始下载 minio/minio:RELEASE.2025-05-24T17-08-30Z 镜像文件..."
	command="docker pull minio/minio:RELEASE.2025-05-24T17-08-30Z"
	reLoad
	
#	echo "开始下载 xuxueli/xxl-job-admin:2.5.0 镜像文件..."
#	command="docker pull xuxueli/xxl-job-admin:2.5.0"
#	reLoad
}

# conda 下载
conda() {
	echo "开始下载 Anaconda ... "
	cd /opt/
	mv /opt/package/python/Anaconda3-2024.10-1-Linux-x86_64.sh /opt/
#	wget https://repo.anaconda.com/archive/Anaconda3-2024.10-1-Linux-x86_64.sh
	echo "开始安装 Anaconda ... "
	sh Anaconda3-2024.10-1-Linux-x86_64.sh<<EOF

q
yes

yes
EOF
	echo "export PATH=/opt/anaconda3/bin:\$PATH"  >> /etc/profile
	echo "export PATH=/opt/anaconda3/bin:\$PATH"  >> ~/.bashrc

	# 更新环境变量
	source /etc/profile
	source ~/.bashrc
	
	# 安装conda后命令行前面base隐藏
	conda config --set auto_activate_base False
	echo "Anaconda 安装完成 ... "
	
	py
}

# 构建 py 运行环境
py() {
	echo "安装python3.9 ... "
	conda create --name py3 python=3.9 -y
	conda activate py3

	pip install websockets
	pip install psutil
}

# 服务器相关依赖下载
util(){
	echo "下载服务器环境所需依赖..."
	# 压缩解压工具
	yum install -y unzip zip
	yum install -y lrzsz
}

# 解压上传的文件
unzipBlog() {
	echo "开始解压博客文件..."
	# 上传部署压缩包解压目录
	mkdir -p /opt/package
	mv ./blog.zip /opt/package
	cd /opt/package
	unzip blog.zip
}

# 添加4g的虚拟内存
addVirtualMemory() {
	echo "开始创建虚拟内存..."
	cd /usr
	mkdir swap
	cd swap/
	dd if=/dev/zero of=/usr/swap/swapfile bs=1M count=4096
	du -sh /usr/swap/swapfile
	mkswap /usr/swap/swapfile
	swapon /usr/swap/swapfile
	free -m
	echo "/usr/swap/swapfile swap swap defaults 0 0"  >> /etc/fstab
}
mi
# 修改 MySQL 配置文件
updateMysqlConf() {
	echo "开始修改MySQL配置文件..."
	# mysql 配置
	mv /opt/package/conf/my.cnf /opt/docker/mysql/conf
	sed -i "s/password=/password=${mysqlPassword}/" /opt/docker/mysql/conf/my.cnf
}

# 更新MySQL数据IP地址，用于迁移服务器，替换旧ip
updateSqlData() {
  newIpAddr=$(curl -4s --fail --connect-timeout 2 ifconfig.me 2>/dev/null || curl -4s --fail --connect-timeout 2 icanhazip.com 2>/dev/null | tr -d '\n')
  echo "${newIpAddr:-No IP Found}"
}

# MySQL 数据修改与导入
insertSqlData() {
	echo "开始导入MySQL数据..."
	mkdir -p /opt/docker/files/sql
	mv /opt/package/conf/mysql.sh /opt/docker/files/sql
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
mysql() {
	# mysql文件目录
	# 宿主机创建数据存放目录映射到容器
	mkdir -p /opt/docker/mysql/data
	# 宿主机创建配置文件目录映射到容器
	mkdir -p /opt/docker/mysql/conf
	# 宿主机创建日志目录映射到容器
	mkdir -p /opt/docker/mysql/logs
	
	updateMysqlConf
	echo "正在启动mysql..."
	docker run -d --name mysql --privileged=true --restart=always --network blog_network --ip 172.18.0.3 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=${mysqlPassword} -v /opt/docker/mysql/data/:/var/lib/mysql -v /opt/docker/mysql/conf/my.cnf:/etc/mysql/my.cnf -v /opt/docker/mysql/logs/:/var/log/mysql/ -v /opt/docker/files/:/opt/docker/files/ mysql:8.0.20
	
	insertSqlData
}

# 创建 ftp 工作目录
createFtpDir() {
	mkdir -p /opt/docker/files/ftp/system/temp/
	chmod 777 /opt/docker/files/ftp/system/temp/
}

# 启动 ftp
ftp() {
  echo "正在启动ftp..."
  createFtpDir
#  docker run -d --name vsftpd --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 61120:20 -p 61121:21 -p 61110-61119:61110-61119 -e FTP_USER=${ftpUsername} -e FTP_PASS=${ftpPassword} -e PASV_MIN_PORT=61110 -e PASV_MAX_PORT=61119 -v /opt/docker/files/ftp:/home/vsftpd fauria/vsftpd
  docker run -d --name vsftpd --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 61120:20 -p 61121:21 -p 61122-61199:61122-61199 -e FTP_USER=${ftpUsername} -e FTP_PASS=${ftpPassword} -e PASV_MIN_PORT=61122 -e PASV_MAX_PORT=61199 -e PASV_ADDRESS=49.232.129.253 -v /opt/docker/files/ftp:/home/vsftpd fauria/vsftpd
#  docker run -d --name vsftpd --privileged=true --restart=always -p 61120:20 -p 61121:21 -p 61122-61199:61122-61199 -e FTP_USER=${ftpUsername} -e FTP_PASS=${ftpPassword} -e PASV_ADDRESS=49.232.129.253 -e PASV_MIN_PORT=61122 -e PASV_MAX_PORT=61199 -v /opt/docker/files/ftp:/home/vsftpd fauria/vsftpd
}

# 安装 nginx
nginx() {
	# nginx 目录创建
	mkdir -p /opt/docker/nginx/conf.d
	mkdir -p /opt/docker/nginx/html
	mkdir -p /opt/docker/nginx/html/assets
	mkdir -p /opt/docker/nginx/logs
	mkdir -p /opt/docker/nginx/conf

	# nginx 配置文件
  mv /opt/package/conf/nginx.conf /opt/docker/nginx/conf

  # web页面相关
  mv /opt/package/web/dist/* /opt/docker/nginx/html

	echo "正在启动nginx..."
	docker run -d --name nginx --privileged=true --restart=always --network blog_network --ip 172.18.0.5 -p 80:80 -v /opt/docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf -v /opt/docker/nginx/html/:/opt/docker/nginx/html/ -v /opt/docker/nginx/logs/:/var/log/nginx/  -v /opt/docker/files/:/opt/docker/files/ nginx:1.20.2
}

# redis 配置文件修改
updateRedisConf() {
	echo "开始修改Redis配置文件..."
	# redis 配置
	mv /opt/package/conf/redis.conf /opt/docker/redis/conf
	sed -i "s/requirepass/requirepass ${redisPassword}/g" /opt/docker/redis/conf/redis.conf
}

# 启动 redis
redis() {
	# redis 目录创建
	mkdir -p /opt/docker/redis/conf/
	mkdir -p /opt/docker/redis/data/
	
	updateRedisConf
	echo "正在启动redis..."
	docker run -d --name redis --privileged=true --restart=always --network blog_network --ip 172.18.0.6 -p 6379:6379 -v /opt/docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /opt/docker/redis/data/:/data/  -v /opt/docker/files/:/opt/docker/files/ redis:6.2.5 redis-server /etc/redis/redis.conf
}

# 安装nacos
nacos() {
  echo "正在启动nacos..."
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
	mv /opt/package/conf/broker.conf /opt/docker/rocketmq/broker/conf/
}

# 启动 rocketmq
rocketMq() {
  echo "正在启动rocketmq..."
  updateRocketMq
  # rmqnamesrv
  docker run -d --name rmqnamesrv --privileged=true --restart=always  --network blog_network --ip 172.18.0.8 -p 9876:9876 -e "MAX_POSSIBLE_HEAP=100000000" -e "MAX_HEAP_SIZE=256M" -e "HEAP_NEWSIZE=128M" -v /opt/docker/rocketmq/namesrv/logs:/home/rocketmq/logs -v /opt/docker/rocketmq/namesrv/store:/root/store apache/rocketmq:5.1.4 sh mqnamesrv
  # rmqbroker
  docker run -d --name rmqbroker --privileged=true --restart=always --network blog_network --ip 172.18.0.9 -p 10911:10911 -p 10909:10909 -e "NAMESRV_ADDR=172.18.0.8:9876"  -e "MAX_POSSIBLE_HEAP=200000000" -e "MAX_HEAP_SIZE=256M" -e "HEAP_NEWSIZE=256M" -v /opt/docker/rocketmq/broker/logs:/root/logs -v /opt/docker/rocketmq/broker/store:/root/store -v /opt/docker/rocketmq/broker/conf/broker.conf:/home/rocketmq/broker.conf apache/rocketmq:5.1.4 sh mqbroker -c /home/rocketmq/broker.conf
}

# 修改 elasticsearch 配置文件
updateElasticsearchConf() {
	mv /opt/package/conf/elasticsearch.yml /opt/docker/elasticsearch/config/
	mv /opt/package/conf/elasticsearch.sh /opt/docker/files/
	chmod +x /opt/docker/files/elasticsearch.sh
	sed -i 's/\r$//' /opt/docker/files/elasticsearch.sh
	sed -i "s/elasticPassword=/elasticPassword=${elasticsearchPassword}/g" /opt/docker/files/elasticsearch.sh
}

# 安装 elasticsearch
elasticsearch() {
	mkdir -p /opt/docker/elasticsearch/data
	mkdir -p /opt/docker/elasticsearch/plugins
	mkdir -p /opt/docker/elasticsearch/config
	chmod -R 777 /opt/docker/elasticsearch/

	updateElasticsearchConf
	echo "正在启动elasticsearch..."
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
	mv /opt/package/files/mc /opt/docker/minio/
	cd /opt/docker/minio
	chmod +x mc
	./mc alias set local http://172.18.0.11:9000 minio ${minioPassword}
	./mc mb local/blog
	./mc mirror --overwrite /opt/docker/minio/blog/ local/blog
	./mc anonymous set download local/blog

	# minio 权限配置
	mv /opt/package/conf/public-policy.json /opt/docker/minio/
	./mc anonymous set-json public-policy.json local/blog

	# 导入文件后删除数据
	rm -rf /opt/docker/minio/blog
	rm -rf /opt/docker/minio/files.zip
}

# 启动 minio
minio() {
	echo "正在启动minio..."
	docker run --name minio --network blog_network --ip 172.18.0.11 -p 9000:9000 -p 9001:9001 --restart=always -e "MINIO_ROOT_USER=minio" -e "MINIO_ROOT_PASSWORD=${minioPassword}" -e "MINIO_BROWSER_REDIRECT_URL=http://172.18.0.11:9001/minio/ui/" -v /opt/docker/files/minio:/data -v /mnt/config:/root/.minio -d minio/minio:RELEASE.2025-05-24T17-08-30Z server /data --console-address ":9001"
	importMinio
}

# 启动 xxlJob 
xxlJob() {
	mkdir -p /opt/docker/xxlJob/logs
	echo "正在启动xxlJob..."
	docker run --name xxljob --network blog_network --ip 172.18.0.12 -p 8080:8080 --restart=always --privileged=true -e PARAMS="--spring.datasource.username=root --spring.datasource.password=${mysqlPassword} --spring.datasource.url=jdbc:mysql://172.18.0.3:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai --xxl.job.accessToken=aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789+=" -v /opt/docker/xxlJob/logs:/data/applogs -d xuxueli/xxl-job-admin:2.5.0
}

startJar() {
  mkdir -p /opt/docker/files/jar
  mv /opt/package/jar/* /opt/docker/files/jar
  sed -i 's/\r$//' /opt/docker/files/jar/run.sh
  chmod +x /opt/docker/files/jar/run.sh
  sed -i 's/\r$//' /opt/docker/files/jar/restart.sh
  chmod +x /opt/docker/files/jar/restart.sh
  mkdir -p /opt/docker/files/logs
  # 等待nacos启动
  echo "3分钟后启动博客服务..."
  sleep 3m
  cd /opt/docker/files/jar
  docker build -t blog:3.0 .
  docker run -d --name blog --privileged=true --restart=always --network blog_network --ip 172.18.0.13 -p 60001:60001 -p 60002:60002 -p 59994:59994 -p 60032:60032 -v /opt/docker/files/logs:/opt/logs -v /opt/docker/files/:/opt/docker/files/ blog:3.0
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

  # 重新加载systemd配置
  sudo systemctl daemon-reload
  # 开机自启
  sudo systemctl enable websocket-watchdog.service
  # 立即启动
  sudo systemctl start websocket-watchdog.service
}

# 启动python脚本
startPy() {
  # Java服务启动较慢，等待Java服务完全启动后进行连接
  echo "8分钟后启动socket脚本..."
  sleep 8m
  mkdir -p /opt/docker/files/python
  mv /opt/package/python/* /opt/docker/files/python
  chmod +x /opt/docker/files/python/webSocket.py
  sed -i 's/\r$//' /opt/docker/files/python/webSocket.py
  chmod +x /opt/docker/files/python/shell/*.sh
  sed -i 's/\r$//' /opt/docker/files/python/shell/*.sh
#  cd /opt/docker/files/python
#  nohup bash -c 'source "$(conda info --base)/etc/profile.d/conda.sh" && conda run -n py3 python webSocket.py --ip 172.18.0.13' >python.log 2>&1 &

  startPyDaemon
}

# 主函数
main() {
	timer_start=`date "+%Y-%m-%d %H:%M:%S"`

	unzipBlog
	dockerStart
	dockerLoad
	util
	addVirtualMemory
	conda
	mysql
	ftp
	nginx
	redis
	nacos
	rocketMq
	elasticsearch
	minio
#	xxlJob

	startJar
  startPy

	timer_end=`date "+%Y-%m-%d %H:%M:%S"`
	duration=`echo $(($(date +%s -d "${timer_end}") - $(date +%s -d "${timer_start}"))) | awk '{t=split("60 s 60 m 24 h 999 d",a);for(n=1;n<t;n+=2){if($1==0)break;s=$1%a[n]a[n+1]s;$1=int($1/a[n])}print s}'`
	echo "脚本执行完成 耗时： $duration "
	exit 0
}

# 获取参数
while getopts "n:" arg
  do
		case "$arg" in
		  n)
			  # 指定镜像文件重新下载次数
				reload=$OPTARG
				echo $reload
				;;
			?)
				echo "没有找到这条命令 ... "
				exit 1
				;;
		esac
	done

main
