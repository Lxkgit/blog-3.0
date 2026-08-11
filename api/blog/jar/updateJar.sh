#! /bin/bash

# 更新并打包Java服务
/opt/docker/ci/shell/buildController.sh java pro blog-auth,blog-gateway,blog-content,blog-file

rm -rf /opt/docker/files/jar/auth/blog-auth.jar
mv /opt/docker/ci/source/blog-3.0/api/blog-auth/target/blog-auth.jar /opt/docker/files/jar/auth

rm -rf /opt/docker/files/jar/gateway/blog-gateway.jar
mv /opt/docker/ci/source/blog-3.0/api/blog-gateway/target/blog-gateway.jar /opt/docker/files/jar/gateway

rm -rf /opt/docker/files/jar/content/blog-content.jar
mv /opt/docker/ci/source/blog-3.0/api/blog-content/target/blog-content.jar /opt/docker/files/jar/content

rm -rf /opt/docker/files/jar/file/blog-file.jar
mv /opt/docker/ci/source/blog-3.0/api/blog-file/target/blog-file.jar /opt/docker/files/jar/file

# 重启Java服务
/opt/docker/files/jar/restartJar.sh

