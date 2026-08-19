#!/bin/bash

source /opt/docker/ci/shell/config.sh
source /opt/docker/ci/shell/args.sh
source /opt/docker/ci/shell/utils.sh


# 构建后端
build_backend()
{
  echo "开始构建 API"
  echo "环境: ${PROFILE}"
  echo "模块: ${MODULES}"

  docker run --rm \
    -v ${SOURCE_DIR}:/workspace \
    -v ${MAVEN_DIR}:/root/.m2 \
    -w /workspace/api \
    ${MAVEN_IMAGE} \
    mvn clean package \
    -pl ${MODULES} \
    -am \
    -P${PROFILE} \
    -DskipTests

  if [ $? -ne 0 ]; then
    echo "后端构建失败"
    exit 1
  fi

  echo "后端构建成功"
}


# 展示打包结果
show_result()
{
  echo "========================================"
  echo "后端构建结果："
  for module in $(echo "${MODULES}" | tr "," " ")
  do
    echo "模块: ${module}"
    find "${SOURCE_DIR}/api/${module}/target" \
      -name "*.jar" \
      ! -name "*sources.jar"
  done
  echo "========================================"
}


# 主流程
main()
{
  # 参数解析
  parse_args "$@"

  # 参数校验
  check_args

  # 更新代码
  update_code

  # 构建
  build_backend

  # 展示结果
  show_result
}


main "$@"