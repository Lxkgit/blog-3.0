#! /bin/bash
# 导出mysql数据

nacosSql="nacos"
blogAuthSql="blog_auth"
blogContentSql="blog_content"
blogFileSql="blog_file"
blogGatewaySql="blog_gateway"

mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${nacosSql} > /opt/docker/files/temp/blog/${nacosSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogAuthSql} > /opt/docker/files/temp/blog/${blogAuthSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogContentSql} > /opt/docker/files/temp/blog/${blogContentSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogFileSql} > /opt/docker/files/temp/blog/${blogFileSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogGatewaySql} > /opt/docker/files/temp/blog/${blogGatewaySql}.sql
