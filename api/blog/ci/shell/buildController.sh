#!/bin/bash

source /opt/docker/ci/shell/config.sh
source /opt/docker/ci/shell/args.sh


# ==============================
# 博客整体构建
# ==============================
build_blog() {

  echo "========================================"
  echo "开始博客整体构建"
  echo "环境: ${PROFILE}"
  echo "========================================"

  echo ""
  echo "========== 开始后端构建 =========="

  /opt/docker/ci/shell/java/buildApi.sh -e "${PROFILE}" -m "blog-auth,blog-gateway,blog-content,blog-file"

  if [ $? -ne 0 ]; then
    echo "后端构建失败，停止整体构建"
    exit 1
  fi

  echo ""
  echo "========== 后端构建成功 =========="
  echo "========== 开始前端构建 =========="

  /opt/docker/ci/shell/web/buildWeb.sh -e "${PROFILE}"

  if [ $? -ne 0 ]; then
    echo "前端构建失败"
    exit 1
  fi

  echo ""
  echo "========================================"
  echo "博客整体构建完成"
  echo "========================================"
}


# ==============================
# 后端构建
# ==============================
build_api() {

  echo "========================================"
  echo "开始接口服务构建"
  echo "环境: ${PROFILE}"
  echo "模块: ${MODULES}"
  echo "========================================"

  /opt/docker/ci/shell/java/buildApi.sh -e "${PROFILE}" -m "${MODULES}"

  if [ $? -ne 0 ]; then
    echo "后端构建失败"
    exit 1
  fi
}


# ==============================
# 树莓派构建
# ==============================
build_pi() {

  echo "========================================"
  echo "开始树莓派服务构建"
  echo "环境: ${PROFILE}"

  if [ -n "${INSTALL}" ]; then
    echo "依赖安装: ${INSTALL}"
  else
    echo "依赖安装: 不安装"
  fi

  echo "========================================"

  if [ -n "${INSTALL}" ]; then
    /opt/docker/ci/shell/java/buildPi.sh -e "${PROFILE}" -i "${INSTALL}"
  else
    /opt/docker/ci/shell/java/buildPi.sh -e "${PROFILE}"
  fi

  if [ $? -ne 0 ]; then
    echo "树莓派服务构建失败"
    exit 1
  fi
}


# ==============================
# 前端构建
# ==============================
build_web() {

  echo "========================================"
  echo "开始前端构建"
  echo "环境: ${PROFILE}"
  echo "========================================"

  /opt/docker/ci/shell/web/buildWeb.sh -e "${PROFILE}"

  if [ $? -ne 0 ]; then
    echo "前端构建失败"
    exit 1
  fi
}


# ==============================
# 脚本入口
# ==============================
main() {

  # 参数解析
  parse_args "$@"

  # 参数校验
  check_controller_args

  # 根据构建类型执行
  case "${TYPE}" in

  blog)
    build_blog
    ;;

  api)
    build_api
    ;;

  pi)
    build_pi
    ;;

  web)
    build_web
    ;;

  esac
}


main "$@"