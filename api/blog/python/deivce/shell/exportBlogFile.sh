#! /bin/bash
# 导出博客数据为blog.zip

# 导出mysql
sudo docker exec mysql bash /opt/docker/files/python/shell/exportSql.sh

# 导出博客文件数据
cd /opt/docker/minio
./mc alias set local http://172.18.0.11:9000 minio minio-960@*
./mc mirror local/blog ./files
cd files
zip -r files.zip ./*

# 移动文件
mkdir -p /opt/docker/files/temp/blog
mv /opt/docker/minio/files/files.zip /opt/docker/files/temp/blog

# 压缩文件
cd /opt/docker/files/temp/blog
zip -r blog.zip ./*

# 脚本最终生成文件位置：/opt/docker/files/temp/blog/blog.zip



