#! /bin/bash
# 导出博客数据为blog.zip

set -x
echo "脚本开始执行: $(date)"

# 创建导出文件目录
echo "创建目录 /opt/docker/files/temp/blog/"
mkdir -p /opt/docker/files/temp/blog/
echo "mkdir return code: $?"

# 导出mysql
echo "开始导出 MySQL"
sudo docker exec mysql bash /opt/docker/files/python/code/shell/exportSql.sh
echo "docker exec exportSql.sh return code: $?"

# 导出博客文件数据
cd /opt/docker/minio

echo "设置 mc alias"
./mc alias set local http://172.18.0.11:9000 minio minio-960@*
echo "mc alias return code: $?"

echo "mirror minio blog bucket"
./mc mirror local/blog ./files
echo "mc mirror return code: $?"

cd /opt/docker/minio/files

echo "压缩 files.zip"
zip -r files.zip ./*
echo "zip files.zip return code: $?"

# 移动文件
echo "移动 files.zip"
mv /opt/docker/minio/files/files.zip /opt/docker/files/temp/blog
echo "mv return code: $?"

# 清空临时文件
echo "删除 /opt/docker/minio/files 目录"
rm -rf /opt/docker/minio/files
echo "rm -rf return code: $?"

# 压缩文件
cd /opt/docker/files/temp/blog

echo "压缩 blog.zip"
zip -r blog.zip ./*
echo "zip blog.zip return code: $?"

echo "脚本执行结束: $(date)"
