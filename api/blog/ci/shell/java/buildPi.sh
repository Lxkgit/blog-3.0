#!/bin/bash

source /opt/docker/ci/shell/config.sh
source /opt/docker/ci/shell/args.sh


# 构建后端
build_backend() {
  # 安装 api 公共依赖
  if [ "${INSTALL}" = "install" ]; then
    echo "INSTALL=install，开始安装 api 公共依赖..."

    docker run --rm \
      --cpus=2 \
      --memory=2g \
      --memory-swap=2g \
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

    echo "api 公共依赖安装成功"
  fi

  # 打包 pi
  echo "开始构建 pi..."

  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
    -v ${SOURCE_DIR}:/workspace \
    -v ${MAVEN_DIR}:/root/.m2 \
    -w /workspace/pi \
    ${MAVEN_IMAGE} \
    mvn clean package \
    -P${PROFILE} \
    -DskipTests

  if [ $? -ne 0 ]; then
    echo "pi 后端构建失败"
    exit 1
  fi

  echo "pi 后端构建成功"
}


# 展示打包结果
show_result() {
  echo "========================================"
  echo "pi 后端构建结果："
  find ${SOURCE_DIR}/pi/target -name "*.jar" ! -name "*sources.jar"
  echo "========================================"
}


# 主流程
main() {
  # 参数解析
  parse_args "$@"

  # 参数校验统一由 args.sh 完成
  check_common_args
  check_pi_args

  # 更新代码
  update_code

  # 构建
  build_backend

  # 展示结果
  show_result
}


main "$@"