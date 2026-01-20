#! /bin/bash
# 导出博客数据为blog.zip

set -x
echo "[INFO] 脚本开始执行: $(date)"

# 创建导出文件目录
echo "[INFO] 创建目录 /opt/docker/files/temp/blog/"
mkdir -p /opt/docker/files/temp/blog/
echo "[INFO] mkdir return code: $?"

# 导出mysql
echo "[INFO] 开始导出 MySQL"
sudo docker exec mysql bash /opt/docker/files/python/code/shell/exportSql.sh
echo "[INFO] docker exec exportSql.sh return code: $?"

# 导出博客文件数据
echo "[INFO] 进入 /opt/docker/minio"
cd /opt/docker/minio
echo "[INFO] cd return code: $?"

echo "[INFO] 设置 mc alias"
./mc alias set local http://172.18.0.11:9000 minio minio-960@*
echo "[INFO] mc alias return code: $?"

echo "[INFO] mirror minio blog bucket"
./mc mirror local/blog ./files
echo "[INFO] mc mirror return code: $?"

echo "[INFO] 进入 files 目录"
cd files
echo "[INFO] cd files return code: $?"

echo "[INFO] 压缩 files.zip"
zip -r files.zip ./*
echo "[INFO] zip files.zip return code: $?"

# 移动文件
echo "[INFO] 移动 files.zip"
mv /opt/docker/minio/files/files.zip /opt/docker/files/temp/blog
echo "[INFO] mv return code: $?"

# 压缩文件
echo "[INFO] 进入最终目录"
cd /opt/docker/files/temp/blog
echo "[INFO] cd final dir return code: $?"

echo "[INFO] 压缩 blog.zip"
zip -r blog.zip ./*
echo "[INFO] zip blog.zip return code: $?"

echo "[INFO] 脚本执行结束: $(date)"
