#!/bin/bash


source /opt/docker/ci/shell/config.sh
PROFILE=$1
MODULES=$2

# 参数校验
check_param()
{
  if [ -z "${PROFILE}" ]; then
    echo "缺少环境"
    exit 1
  fi

  if [ -z "${MODULES}" ]; then
    echo "缺少模块"
    exit 1
  fi
}

# 更新代码
update_code()
{
  /opt/docker/ci/shell/updateCode.sh
}

# 构建镜像打包
build_backend()
{
  docker run --rm \
    -v ${SOURCE_DIR}:/workspace \
    -v ${MAVEN_DIR}:/root/.m2 \
    -w /workspace/api \
    ${MAVEN_IMAGE} \
    mvn clean install \
    -DskipTests

  docker run --rm \
    -v ${SOURCE_DIR}:/workspace \
    -v ${MAVEN_DIR}:/root/.m2 \
    -w /workspace/pi \
    ${MAVEN_IMAGE} \
    mvn clean package \
    -P${PROFILE} \
    -DskipTests

  if [ $? -ne 0 ]; then
      echo "后端构建失败"
      exit 1
  fi
}

show_result()
{
  echo "后端构建结果："
  for module in $(echo ${MODULES} | tr "," " ")
  do
      find ${SOURCE_DIR}/pi/${module}/target \
      -name "*.jar" \
      ! -name "*sources.jar"
  done
}

main()
{
  # 参数校验
  check_param
  # 代码更新
  update_code
  # 代码打包
  build_backend
  # 展示打包结果
  show_result
}

main