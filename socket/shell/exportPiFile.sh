#! /bin/bash
# 导出树莓派数据为pi.zip

# 在执行每一条命令之前，先把展开后的命令打印出来
set -x
echo "脚本开始执行: $(date)"

# 创建导出文件目录
echo "创建目录 /opt/docker/files/temp/blog/"
mkdir -p /opt/docker/files/temp/blog/
echo "mkdir return code: $?"

# 导出mysql
echo "开始导出 MySQL"
sudo docker exec mysql bash /opt/soft/socket/code/shell/exportPiSql.sh
echo "docker exec exportSql.sh return code: $?"

# 压缩文件
cd /opt/docker/files/temp/blog || exit

echo "压缩 pi.zip"
zip -r pi.zip ./*
echo "zip pi.zip return code: $?"

echo "脚本执行结束: $(date)"
