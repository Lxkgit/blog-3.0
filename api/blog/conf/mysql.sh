#! /bin/bash
# mysql数据导入脚本

# 数据库名字
xxlSql="xxl_job"
nacosSql="nacos"
blogAuthSql="blog_auth"
blogContentSql="blog_content"
blogFileSql="blog_file"
blogGatewaySql="blog_gateway"

# MySQL登陆密码
mysqlPassword=

mysqlSQL() {
  # 等待MySQL启动完成
  sleep 1m
  mysql -uroot -p${mysqlPassword} <<EOF

  drop database if exists ${xxlSql};
  CREATE DATABASE  ${xxlSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${xxlSql};
  source /opt/docker/files/sql/${xxlSql}.sql;

  drop database if exists ${nacosSql};
  CREATE DATABASE  ${nacosSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${nacosSql};
  source /opt/docker/files/sql/${nacosSql}.sql;

  exit

EOF
  exit 0
}

mysqlSQL