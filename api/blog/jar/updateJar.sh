#! /bin/bash

source /opt/docker/files/jar/jar.conf

profile="${JAR_PROFILE}"
hostIp="${JAR_HOST_IP}"

# 更新配置文件
updateJarConfig() {
  # jar包打包文件移动
  cp -r /opt/docker/ci/code/blog-3.0/api/blog/jar/* /opt/docker/files/jar

  # 指定配置文件
  sed -i "s/@env@/${profile}/g" /opt/docker/files/jar/auth/bootstrap.yml
  sed -i "s/@env@/${profile}/g" /opt/docker/files/jar/content/bootstrap.yml
  sed -i "s/@env@/${profile}/g" /opt/docker/files/jar/gateway/bootstrap.yml
  sed -i "s/@env@/${profile}/g" /opt/docker/files/jar/file/bootstrap.yml

  # 配置文件中ip替换 （bootstrap 文件中 ${devServiceIp} 字段只设置nacos连接地址，由于云服务器禁用了nacos公网访问端口，导致无法通过公网IP连接，所以此处设置为nacos在docker容器中的ip）
  sed -i "s/\${devServiceIp}/172.18.0.7/g" /opt/docker/files/jar/auth/bootstrap.yml
  sed -i "s/\${devServiceIp}/172.18.0.7/g" /opt/docker/files/jar/content/bootstrap.yml
  sed -i "s/\${devServiceIp}/172.18.0.7/g" /opt/docker/files/jar/gateway/bootstrap.yml
  sed -i "s/\${devServiceIp}/172.18.0.7/g" /opt/docker/files/jar/file/bootstrap.yml

  # 配置文件中ip替换
  sed -i "s/\${devServiceIp}/${hostIp}/g" /opt/docker/files/jar/auth/application-${profile}.yml
  sed -i "s/\${devServiceIp}/${hostIp}/g" /opt/docker/files/jar/content/application-${profile}.yml
  sed -i "s/\${devServiceIp}/${hostIp}/g" /opt/docker/files/jar/gateway/application-${profile}.yml
  sed -i "s/\${devServiceIp}/${hostIp}/g" /opt/docker/files/jar/file/application-${profile}.yml
}

# 编译打包jar包服务
buildJar() {
  # 更新并打包Java服务
  /opt/docker/ci/shell/buildController.sh java pro blog-auth,blog-gateway,blog-content,blog-file

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
