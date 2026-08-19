#!/bin/bash


source /opt/docker/ci/shell/config.sh
PROFILE=$1
INSTALL=""

# 参数校验
check_param()
{

  if [ -z "${PROFILE}" ]; then
    echo "缺少环境"
    exit 1
  fi

  while getopts "i:" opt; do
    case ${opt} in
      i)
        INSTALL="${OPTARG}"
        ;;
      *)
        echo "用法: $0 <module> <profile> [-i install]"
        exit 1
        ;;
    esac
  done
}

# 更新代码
update_code()
{
  /opt/docker/ci/shell/updateCode.sh
}

# 构建镜像打包
build_backend()
{

  if [ "${INSTALL}" = "install" ]; then
    echo "INSTALL=install，开始安装 api 公共依赖..."

    docker run --rm \
      -v ${SOURCE_DIR}:/workspace \
      -v ${MAVEN_DIR}:/root/.m2 \
      -w /workspace/api \
      ${MAVEN_IMAGE} \
      mvn clean install \
      -DskipTests

    if [ $? -ne 0 ]; then
      echo "api 公共依赖安装失败"
      exit 1
    fi
  fi



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
  find ${SOURCE_DIR}/pi/target -name "*.jar" ! -name "*sources.jar"
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