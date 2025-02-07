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
	curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

	# 启动docker
	sudo systemctl start docker
	# docker开始自启动
	systemctl enable docker.service
	# 创建自定义网络
	docker network create --subnet=172.18.0.0/24 blog_network
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
	echo "开始下载 openjdk:8 镜像文件..."
	command="docker pull openjdk:8"
	reLoad

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
	
	echo "开始下载 minio/minio 镜像文件..."
	command="docker pull minio/minio"
	reLoad
	
	echo "开始下载 xuxueli/xxl-job-admin:2.5.0 镜像文件..."
	command="docker pull xuxueli/xxl-job-admin:2.5.0"
	reLoad
}

# conda 下载
conda() {
	echo "开始下载 Anacoda ... "
	cd /opt/
	wget https://repo.anaconda.com/archive/Anaconda3-2024.10-1-Linux-x86_64.sh
	echo "开始安装 Anacoda ... "
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
	echo "Anacoda 安装完成 ... "
	
	py
}

# 构建 py 运行环境
py() {
	echo "安装python3.11 ... "
	conda create --name py3 python=3.11 -y
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

# 安装jdk
jdk() {
	echo "正在启动jdk..."
	docker run -d -it --name jdk --privileged=true --restart=always --network blog_network --ip 172.18.0.2 openjdk:8
}

# 修改 MySQL 配置文件
updateMysqlConf() {
	echo "开始修改MySQL配置文件..."
	# mysql 配置
	mv /opt/package/conf/my.cnf /opt/docker/mysql/conf
	sed -i "s/password=/password=${mysqlPassword}/" /opt/docker/mysql/conf/my.cnf
}

# MySQL 数据修改与导入
updateSqlFile() {
	echo "开始修改MySQL数据恢复脚本文件..."
	mv /opt/package/conf/mysql.sh /opt/docker/files
	mkdir -p /opt/docker/files/sql
	mv /opt/package/sql/* /opt/docker/files/sql
	chmod +x /opt/docker/files/mysql.sh
	sed -i 's/\r$//' /opt/docker/files/mysql.sh
	sed -i 's/\r$//' /opt/docker/files/sql/*.sql
	sed -i "s/mysqlPassword=/mysqlPassword=\"${mysqlPassword}\"/" /opt/docker/files/mysql.sh
	
	# 导入sql数据
	nohup sudo docker exec mysql bash /opt/docker/files/mysql.sh >/opt/docker/mysql/logs/importSql.log 2>&1
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
	
	updateSqlFile
}

# redis 配置文件修改
updateRedisConf() {
	echo "开始修改Redis配置文件..."
	# redis 配置
	mv /opt/package/conf/redis.conf /opt/docker/redis/conf
	sed -i "s/requirepass/requirepass ${redisPassword}/g" /opt/docker/redis/conf/redis.conf
}

# 创建 ftp 工作目录
createFtpDir() {
	mkdir -p /opt/docker/files/ftp
}

# 启动 ftp
ftp() {
  echo "正在启动ftp..."
  createFtpDir
  docker run -d --name vsftpd --privileged=true --restart=always --network blog_network --ip 172.18.0.4 -p 61120:20 -p 61121:21 -p 61110-61119:61110-61119 -e FTP_USER=${ftpUsername} -e FTP_PASS=${ftpPassword} -e PASV_MIN_PORT=61110 -e PASV_MAX_PORT=61119 -v /opt/docker/files/ftp:/home/vsftpd fauria/vsftpd
}

# 修改 nginx 配置文件
updateNginxConf(){
	# nginx 配置文件
	mv /opt/package/conf/nginx.conf /opt/docker/nginx/conf
}

# 安装 nginx
nginx() {

	# nginx 目录创建
	mkdir -p /opt/docker/nginx/conf.d
	mkdir -p /opt/docker/nginx/html
	mkdir -p /opt/docker/nginx/html/assets
	mkdir -p /opt/docker/nginx/logs
	mkdir -p /opt/docker/nginx/conf
	
	updateNginxConf
	echo "正在启动nginx..."
	docker run -d --name nginx --privileged=true --restart=always --network blog_network --ip 172.18.0.5 -p 80:80 -v /opt/docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf -v /opt/docker/nginx/html/:/opt/docker/nginx/html/ -v /opt/docker/nginx/logs/:/var/log/nginx/  -v /opt/docker/files/:/opt/docker/files/ nginx:1.20.2
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
	docker run --name elasticsearch -p 9200:9200 -p 9300:9300  -e ES_JAVA_OPTS="-Xms128m -Xmx256m" -e "discovery.type=single-node" -v /opt/docker/elasticsearch/data:/usr/share/elasticsearch/data -v /opt/docker/elasticsearch/plugins:/usr/share/elasticsearch/plugins -v /opt/docker/elasticsearch/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12 -v /opt/docker/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /opt/docker/files/:/opt/docker/files/ --network blog_network --ip 172.18.0.10 -d elasticsearch:7.14.1
	
	nohup sudo docker exec elasticsearch bash /opt/docker/files/elasticsearch.sh >/opt/docker/files/es.log 2>&1
}

# 创建 minio 目录
createMinioDir() {
	# minio 文件存放位置
	mkdir -p /opt/docker/files/minio
}

# minio 文件导入
importMinio() {
	sleep 1m
	mkdir -p /opt/docker/minio
	cd /opt/docker/minio
	mv /opt/package/file/mc /opt/docker/minio/
	mv /opt/package/file/files.zip /opt/docker/minio/
	unzip /opt/docker/minio/files.zip
	chmod +x mc
	./mc alias set local http://172.18.0.11:9000 minio ${minioPassword}
	./mc mb local/blog
	./mc mirror --overwrite /opt/docker/minio/files/ local/blog
	./mc anonymous set download local/blog
	
	# 导入文件后删除数据
	rm -rf /opt/docker/minio/files.zip
	rm -rf /opt/docker/minio/files
}

# 启动 minio
minio() {
	
	echo "正在启动minio..."
	createMinioDir
	docker run --name minio --network blog_network --ip 172.18.0.11 -p 9000:9000 -p 9001:9001 --restart always -e "MINIO_ROOT_USER=minio" -e "MINIO_ROOT_PASSWORD=${minioPassword}" -e "MINIO_BROWSER_REDIRECT_URL=http://172.18.0.11:9001/minio/ui/" -v /opt/docker/files/minio:/data -v /mnt/config:/root/.minio -d minio/minio server /data --console-address ":9001"
	importMinio
}

# 启动 xxlJob 
xxlJob() {
	
	mkdir -p /opt/docker/xxlJob/logs
	echo "正在启动xxlJob..."
	docker run --name xxljob --network blog_network --ip 172.18.0.12 -p 8080:8080 --restart=always --privileged=true -e PARAMS="--spring.datasource.username=root --spring.datasource.password=${mysqlPassword} --spring.datasource.url=jdbc:mysql://172.18.0.3:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai" -v /opt/docker/xxlJob/logs:/data/applogs -d xuxueli/xxl-job-admin:2.5.0

}

# 主函数
main() {
	timer_start=`date "+%Y-%m-%d %H:%M:%S"`
	
	dockerStart
	dockerLoad
	util
	unzipBlog
	addVirtualMemory
	conda
	jdk
	mysql
	ftp
	nginx
	redis
	nacos
	rocketMq
	elasticsearch
	minio
	xxlJob
	
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