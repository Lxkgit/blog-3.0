#! /bin/bash



# 更新配置文件
updateJarConfig() {
  # jar包打包文件移动
  cp -r /opt/docker/ci/code/blog-3.0/api/blog/jar/* /opt/docker/files/jar
  source /opt/docker/files/jar/jar.conf

  # 脚本文件去掉 Windows 换行符 \r
  find /opt/docker/files/jar -type f -name "*.sh" -exec sed -i 's/\r$//' {} \;
  # 授权可执行
  find /opt/docker/files/jar -type f -name "*.sh" -exec chmod +x {} \;

  # 服务目录
  SERVICES=("auth" "content" "gateway" "file")
  for service in "${SERVICES[@]}"; do
    # 替换环境
    sed -i "s/@env@/${JAR_PROFILE}/g" "/opt/docker/files/jar/${service}/bootstrap.yml"
    # bootstrap.yml：Nacos 使用 Docker 内网 IP
    sed -i "s/\${devServiceIp}/${NACOS_IP}/g" "/opt/docker/files/jar/${service}/bootstrap.yml"
    # application-${JAR_PROFILE}.yml：替换成云服务器 IP
    sed -i "s/\${devServiceIp}/${JAR_HOST_IP}/g" "/opt/docker/files/jar/${service}/application-${JAR_PROFILE}.yml"
  done
}

# 编译打包jar包服务
buildJar() {
  # 更新并打包Java服务
  /opt/docker/ci/shell/buildController.sh api pro blog-auth,blog-gateway,blog-content,blog-file

  rm -rf /opt/docker/files/jar/auth/blog-auth.jar
  mv /opt/docker/ci/code/blog-3.0/api/blog-auth/target/blog-auth.jar /opt/docker/files/jar/auth

  rm -rf /opt/docker/files/jar/gateway/blog-gateway.jar
  mv /opt/docker/ci/code/blog-3.0/api/blog-gateway/target/blog-gateway.jar /opt/docker/files/jar/gateway

  rm -rf /opt/docker/files/jar/content/blog-content.jar
  mv /opt/docker/ci/code/blog-3.0/api/blog-content/target/blog-content.jar /opt/docker/files/jar/content

  rm -rf /opt/docker/files/jar/file/blog-file.jar
  mv /opt/docker/ci/code/blog-3.0/api/blog-file/target/blog-file.jar /opt/docker/files/jar/file
}

# 重启Java服务
restartJar() {
  /opt/docker/files/jar/restartJar.sh
}

main(){


  updateJarConfig
  buildJar
  restartJar

}

main
