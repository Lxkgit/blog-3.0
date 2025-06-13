#! /bin/bash
# 导出博客数据为blog.zip

# 创建导出文件目录
mkdir -p /opt/docker/files/temp/blog/

# 导出mysql
sudo docker exec mysql bash /opt/docker/files/python/shell/exportSql.sh

# 压缩文件
cd /opt/docker/files/temp/blog
zip -r pi.zip ./*

# 脚本最终生成文件位置：/opt/docker/files/temp/blog/blog.zip



