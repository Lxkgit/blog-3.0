#!/bin/bash

source /opt/docker/ci/shell/config.sh


# ==============================
# 参数校验
# ==============================
check_args() {
  if [ $# -lt 1 ]; then
    echo_help
  fi

  local type=$1
  shift

  case ${type} in
  api)
    if [ $# -lt 2 ]; then
      echo_help
    fi
    ;;

  pi)
    if [ $# -lt 2 ]; then
      echo_help
    fi
    ;;

  web)
    if [ $# -ne 1 ]; then
      echo_help
    fi
    ;;

  *)
    echo_help
    ;;
  esac
}

echo_help() {
  echo "参数校验异常"
  echo ""
  echo "使用方式:"
  echo "  博客整体构建: ./buildController.sh blog pro(test)"
  echo "  接口服务构建: ./buildController.sh api pro(test) blog-auth,blog-gateway,blog-content,blog-file"
  echo "  前端页面构建: ./buildController.sh web pro(test)"
  echo "  树莓派构建: ./buildController.sh pi pro(test) blog-pi"
  exit 1
}

# ==============================
# 博客整体构建
# ==============================
build_blog() {
  echo "========================================"
  echo "开始博客整体构建"
  echo "环境: $1"
  echo "========================================"

  echo ""
  echo "========== 开始后端构建 =========="

  /opt/docker/ci/shell/java/buildApi.sh "$@" blog-auth,blog-gateway,blog-content,blog-file
  if [ $? -ne 0 ]; then
    echo "后端构建失败，停止整体构建"
    exit 1
  fi

  echo ""
  echo "========== 后端构建成功 =========="
  echo "========== 开始前端构建 =========="

  /opt/docker/ci/shell/web/buildWeb.sh "$@"
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
  echo "进入后端构建"

  /opt/docker/ci/shell/java/buildApi.sh "$@"
  if [ $? -ne 0 ]; then
    echo "后端构建失败"
    exit 1
  fi
}

build_pi() {
  echo "进入后端构建"

  /opt/docker/ci/shell/java/buildPi.sh "$@"
  if [ $? -ne 0 ]; then
    echo "后端构建失败"
    exit 1
  fi
}


# ==============================
# 前端构建
# ==============================
build_web() {
  echo "进入前端构建"

  /opt/docker/ci/shell/web/buildWeb.sh "$@"
  if [ $? -ne 0 ]; then
    echo "前端构建失败"
    exit 1
  fi
}


# ==============================
# 脚本入口
# ==============================
main() {
  local type=$1
  shift

  check_args "$type" "$@"

  case ${type} in
  blog)
    build_blog "$@"
    ;;
  api)
    build_api "$@"
    ;;
  pi)
    build_pi "$@"
    ;;
  web)
    build_web "$@"
    ;;
  esac
}


main "$@"