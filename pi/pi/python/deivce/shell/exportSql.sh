#! /bin/bash
# 导出mysql数据

blogPiSql="blog_pi"

mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogPiSql} > /opt/docker/files/temp/blog/${blogPiSql}.sql
