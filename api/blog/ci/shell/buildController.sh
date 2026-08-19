#!/bin/bash

source /opt/docker/ci/shell/config.sh


# ==============================
# 参数校验
# ==============================
check_args() {
  if [ $# -lt 1 ]; then
    echo "错误：缺少构建类型"
    echo ""
    echo "使用方式:"
    echo "  整体构建: ./buildController.sh blog pro(test)"
    echo "  后端构建: ./buildController.sh java pro(test) blog-auth,blog-gateway,blog-content,blog-file"
    echo "  前端构建: ./buildController.sh web pro(test)"
    exit 1
  fi

  local type=$1
  shift

  case ${type} in
  java)
    if [ $# -lt 2 ]; then
      echo "错误：后端构建参数不足"
      echo "使用方式:"
      echo "  ./buildController.sh java pro(test) blog-auth,blog-gateway,blog-content,blog-file"
      exit 1
    fi
    ;;

  web)
    if [ $# -ne 1 ]; then
      echo "错误：前端构建参数数量错误"
      echo "使用方式:"
      echo "  ./buildController.sh web pro"
      echo "  ./buildController.sh web test"
      exit 1
    fi

    case "$1" in
    pro|test)
      ;;
    *)
      echo "错误：环境参数只能是 pro 或 test"
      exit 1
      ;;
    esac
    ;;

  *)
    echo "未知构建类型: ${type}"
    echo ""
    echo "使用方式:"
    echo "  整体构建: ./buildController.sh blog pro(test)"
    echo "  后端构建: ./buildController.sh java pro(test) blog-auth,blog-gateway,blog-content,blog-file"
    echo "  前端构建: ./buildController.sh web pro(test)"
    exit 1
    ;;
  esac
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

  /opt/docker/ci/shell/java/buildJava.sh "$@" blog-auth,blog-gateway,blog-content,blog-file
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
build_java() {
  echo "进入后端构建"

  /opt/docker/ci/shell/java/buildJava.sh "$@"
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
  java)
    build_java "$@"
    ;;
  web)
    build_web "$@"
    ;;
  esac
}


main "$@"