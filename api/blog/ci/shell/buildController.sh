#!/bin/bash

source /opt/docker/ci/shell/config.sh
source /opt/docker/ci/shell/args.sh
source /opt/docker/ci/shell/utils.sh


# =========================
# 构建微服务
# =========================

build_microservices() {

  echo "========================================"
  echo "开始构建微服务"
  echo "项目: ${PROJECT_NAME}"
  echo "构建目录: ${PACKAGE_NAME}"
  echo "构建类型: ${BUILD_TYPE}"
  echo "环境: ${PROFILE}"
  echo "源码: ${SOURCE_DIR}"
  echo "模块: ${MODULES}"
  echo "========================================"

  /opt/docker/ci/shell/java/buildMicroservices.sh \
    -e "${PROFILE}" \
    -m "${MODULES}" \
    -d "${SOURCE_DIR}"

  if [ $? -ne 0 ]; then
    echo "微服务构建失败"
    exit 1
  fi

  echo "微服务构建成功"
}


# =========================
# 构建单服务
# =========================

build_service() {

  echo "========================================"
  echo "开始构建单服务"
  echo "项目: ${PROJECT_NAME}"
  echo "构建目录: ${PACKAGE_NAME}"
  echo "构建类型: ${BUILD_TYPE}"
  echo "环境: ${PROFILE}"
  echo "源码: ${SOURCE_DIR}"
  echo "依赖目录: ${DEPENDENCY_DIR}"
  echo "========================================"


  if [ "${INSTALL}" = "install" ]; then

    /opt/docker/ci/shell/java/buildService.sh \
      -e "${PROFILE}" \
      -i "${INSTALL}" \
      -d "${SOURCE_DIR}" \
      -D "${DEPENDENCY_DIR}"

  else

    /opt/docker/ci/shell/java/buildService.sh \
      -e "${PROFILE}" \
      -d "${SOURCE_DIR}"

  fi


  if [ $? -ne 0 ]; then
    echo "单服务构建失败"
    exit 1
  fi


  echo "单服务构建成功"
}


# =========================
# 构建 Web
# =========================

build_web() {

  echo "========================================"
  echo "开始构建 Web"
  echo "项目: ${PROJECT_NAME}"
  echo "构建目录: ${PACKAGE_NAME}"
  echo "构建类型: ${BUILD_TYPE}"
  echo "环境: ${PROFILE}"
  echo "源码: ${SOURCE_DIR}"
  echo "========================================"

  /opt/docker/ci/shell/web/buildWeb.sh \
    -e "${PROFILE}" \
    -d "${SOURCE_DIR}"

  if [ $? -ne 0 ]; then
    echo "Web 构建失败"
    exit 1
  fi

  echo "Web 构建成功"
}


# =========================
# 执行构建
# =========================

run_build() {

  case "${BUILD_TYPE}" in

    microservices)
      build_microservices
      ;;

    service)
      build_service
      ;;

    web)
      build_web
      ;;

    *)
      echo "不支持的构建类型: ${BUILD_TYPE}"
      exit 1
      ;;

  esac
}


# =========================
# 主函数
# =========================

main() {

  parse_args "$@"

  # 解析项目、构建配置
  check_controller_args

  echo ""
  echo "========================================"
  echo "构建信息"
  echo "项目: ${PROJECT_NAME}"
  echo "构建目录: ${PACKAGE_NAME}"
  echo "构建类型: ${BUILD_TYPE}"
  echo "源码目录: ${SOURCE_DIR}"
  echo "环境: ${PROFILE}"
  echo "========================================"
  echo ""

  # 更新代码
  update_code

  # 更新代码后重新确认源码目录
  check_source_dir

  # 执行构建
  run_build
}


main "$@"