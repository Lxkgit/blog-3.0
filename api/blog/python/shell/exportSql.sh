#! /bin/bash
# 导出mysql数据

nacosSql="nacos"
blogAuthSql="blog_auth"
blogContentSql="blog_content"
blogFileSql="blog_file"
blogGatewaySql="blog_gateway"

blogAuthTestSql="blog_auth_test"
blogContentTestSql="blog_content_test"
blogFileTestSql="blog_file_test"
blogGatewayTestSql="blog_gateway_test"

mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${nacosSql} > /opt/docker/files/temp/blog/${nacosSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogAuthSql} > /opt/docker/files/temp/blog/${blogAuthSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogContentSql} > /opt/docker/files/temp/blog/${blogContentSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogFileSql} > /opt/docker/files/temp/blog/${blogFileSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogGatewaySql} > /opt/docker/files/temp/blog/${blogGatewaySql}.sql

mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogAuthTestSql} > /opt/docker/files/temp/blog/${blogAuthTestSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogContentTestSql} > /opt/docker/files/temp/blog/${blogContentTestSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogFileTestSql} > /opt/docker/files/temp/blog/${blogFileTestSql}.sql
mysqldump --defaults-extra-file=/etc/mysql/my.cnf ${blogGatewayTestSql} > /opt/docker/files/temp/blog/${blogGatewayTestSql}.sql