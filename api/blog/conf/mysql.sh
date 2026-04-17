#! /bin/bash
# mysql数据导入脚本

# 数据库名字
nacosSql="nacos"
blogAuthSql="blog_auth"
blogContentSql="blog_content"
blogFileSql="blog_file"
blogGatewaySql="blog_gateway"

blogAuthTestSql="blog_auth_test"
blogContentTestSql="blog_content_test"
blogFileTestSql="blog_file_test"
blogGatewayTestSql="blog_gateway_test"
# MySQL登陆密码
mysqlPassword=

mysqlSQL() {
  # 等待MySQL启动完成
  sleep 1m
  mysql -uroot -p${mysqlPassword} <<EOF

  drop database if exists ${nacosSql};
  CREATE DATABASE  ${nacosSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${nacosSql};
  source /opt/docker/files/sql/${nacosSql}.sql;

  drop database if exists ${blogAuthSql};
  CREATE DATABASE  ${blogAuthSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogAuthSql};
  source /opt/docker/files/sql/${blogAuthSql}.sql;

  drop database if exists ${blogContentSql};
  CREATE DATABASE  ${blogContentSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogContentSql};
  source /opt/docker/files/sql/${blogContentSql}.sql;

  drop database if exists ${blogFileSql};
  CREATE DATABASE  ${blogFileSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogFileSql};
  source /opt/docker/files/sql/${blogFileSql}.sql;

  drop database if exists ${blogGatewaySql};
  CREATE DATABASE  ${blogGatewaySql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogGatewaySql};
  source /opt/docker/files/sql/${blogGatewaySql}.sql;

  drop database if exists ${blogAuthTestSql};
  CREATE DATABASE  ${blogAuthTestSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogAuthTestSql};
  source /opt/docker/files/sql/${blogAuthTestSql}.sql;

  drop database if exists ${blogContentTestSql};
  CREATE DATABASE  ${blogContentTestSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogContentTestSql};
  source /opt/docker/files/sql/${blogContentTestSql}.sql;

  drop database if exists ${blogFileTestSql};
  CREATE DATABASE  ${blogFileTestSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogFileTestSql};
  source /opt/docker/files/sql/${blogFileTestSql}.sql;

  drop database if exists ${blogGatewayTestSql};
  CREATE DATABASE  ${blogGatewayTestSql} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  use ${blogGatewayTestSql};
  source /opt/docker/files/sql/${blogGatewayTestSql}.sql;

  exit

EOF
  exit 0
}

mysqlSQL