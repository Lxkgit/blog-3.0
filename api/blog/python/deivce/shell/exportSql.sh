#! /bin/bash
# 导出mysql数据

nacosSql="nacos"
xxlJobSql="xxl_job"
blogAuthSql="blog_auth"
blogContentSql="blog_content"
blogFileSql="blog_file"
blogGatewaySql="blog_gateway"

mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${nacosSql} > /opt/docker/files/sql/${nacosSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${xxlJobSql} > /opt/docker/files/sql/${xxlJobSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogAuthSql} > /opt/docker/files/sql/${blogAuthSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogContentSql} > /opt/docker/files/sql/${blogContentSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogFileSql} > /opt/docker/files/sql/${blogFileSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogGatewaySql} > /opt/docker/files/sql/${blogGatewaySql}.sql