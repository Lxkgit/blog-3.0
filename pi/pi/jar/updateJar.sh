#! /bin/bash


# 更新配置文件
updateJarConfig() {
  # jar包打包文件移动
  cp -r /opt/docker/ci/code/blog-3.0/pi/pi/jar/* /opt/docker/files/jar
  source /opt/docker/files/jar/jar.conf

  # 脚本文件去掉 Windows 换行符 \r
  find /opt/docker/files/jar -type f -name "*.sh" -exec sed -i 's/\r$//' {} \;
  # 授权可执行
  find /opt/docker/files/jar -type f -name "*.sh" -exec chmod +x {} \;

  # 服务目录
  SERVICES=("pi")
  for service in "${SERVICES[@]}"; do
    # 替换环境
    sed -i "s/@env@/${JAR_PROFILE}/g" "/opt/docker/files/jar/${service}/bootstrap.yml"
  done
}

# 编译打包jar包服务
buildJar() {
  # 更新并打包Java服务
  /opt/docker/ci/shell/buildController.sh pi pro install

  rm -rf /opt/docker/files/jar/pi/blog-pi.jar
  mv /opt/docker/ci/code/blog-3.0/pi/target/blog-pi.jar /opt/docker/files/jar/pi
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
