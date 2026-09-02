#!/bin/bash


# 公共参数
TYPE=""
PROFILE=""
INSTALL=""
MODULES=""
SOURCE_DIR=""
DEPENDENCY_DIR=""


# 帮助
echo_help() {
  echo "参数错误"
  echo ""
  echo "使用方式:"
  echo ""
  echo "  博客整体构建:"
  echo "    ./buildController.sh -s blog -e pro"
  echo ""
  echo "  接口服务构建:"
  echo "    ./buildController.sh -s api -e pro -m blog-auth,blog-gateway,blog-content,blog-file -d /opt/docker/ci/code/blog-3.0/api"
  echo ""
  echo "  树莓派构建:"
  echo "    ./buildController.sh -s pi -e pro -d /opt/docker/ci/code/blog-3.0/pi"
  echo ""
  echo "  树莓派构建并安装公共依赖:"
  echo "    ./buildController.sh -s pi -e pro -i install -d /opt/docker/ci/code/blog-3.0/pi -D /opt/docker/ci/code/blog-3.0/api"
  echo ""
  echo "  博客前端页面构建:"
  echo "    ./buildController.sh -s web -e pro -d /opt/docker/ci/code/blog-3.0/web"
  echo ""
  echo "  前端页面构建:"
  echo "    ./buildController.sh -s web -e pro -d /opt/docker/ci/code/web-excel"
  echo ""
  echo "参数说明:"
  echo "  -s    构建类型: blog | api | pi | web"
  echo "  -e    构建环境: pro | test"
  echo "  -i    安装公共依赖: install，仅 pi 使用"
  echo "  -m    Maven 模块列表，仅 api 使用"
  echo "  -d    项目源码目录，api | pi | web 必须指定"
  echo "  -D    依赖项目目录，仅 pi 使用 -i install 添加"
  echo ""
  exit 1
}


# 参数解析
parse_args() {
  TYPE=""
  PROFILE=""
  INSTALL=""
  MODULES=""
  SOURCE_DIR=""
  DEPENDENCY_DIR=""

  while getopts ":s:e:i:m:d:D:" opt
  do
    case "${opt}" in
      s)
        TYPE="${OPTARG}"
        ;;
      e)
        PROFILE="${OPTARG}"
        ;;
      i)
        INSTALL="${OPTARG}"
        ;;
      m)
        MODULES="${OPTARG}"
        ;;
      d)
        SOURCE_DIR="${OPTARG}"
        ;;
      D)
        DEPENDENCY_DIR="${OPTARG}"
        ;;
      :)
        echo "参数 -${OPTARG} 缺少参数"
        echo_help
        ;;
      \?)
        echo "未知参数: -${OPTARG}"
        echo_help
        ;;
    esac
  done
}


# Controller 参数校验
check_controller_args() {
  if [ -z "${TYPE}" ]; then
    echo "缺少构建类型 -s"
    echo_help
  fi

  case "${TYPE}" in
    blog|api|pi|web)
      ;;
    *)
      echo "构建类型错误: ${TYPE}"
      echo "只支持: blog | api | pi | web"
      exit 1
      ;;
  esac

  check_common_args

  case "${TYPE}" in
    api)
      check_api_args
      ;;
    pi)
      check_pi_args
      ;;
    web)
      check_web_args
      ;;
    blog)
      ;;
  esac
}


# 公共参数校验
check_common_args() {
  if [ -z "${PROFILE}" ]; then
    echo "缺少环境参数 -e"
    echo_help
  fi

  case "${PROFILE}" in
    pro|test)
      ;;
    *)
      echo "环境参数错误: ${PROFILE}"
      echo "只支持: pro | test"
      exit 1
      ;;
  esac
}


# API 参数校验
check_api_args() {
  # 必须指定源码目录
  if [ -z "${SOURCE_DIR}" ]; then
    echo "API 构建缺少源码目录参数 -d"
    echo_help
  fi

  if [ ! -d "${SOURCE_DIR}" ]; then
    echo "源码目录不存在: ${SOURCE_DIR}"
    exit 1
  fi

  # 必须指定模块
  if [ -z "${MODULES}" ]; then
    echo "API 构建缺少模块参数 -m"
    echo_help
  fi
}


# PI 参数校验
check_pi_args() {
  # 必须指定源码目录
  if [ -z "${SOURCE_DIR}" ]; then
    echo "PI 构建缺少源码目录参数 -d"
    echo_help
  fi

  if [ ! -d "${SOURCE_DIR}" ]; then
    echo "源码目录不存在: ${SOURCE_DIR}"
    exit 1
  fi

  # -i 参数校验
  if [ -n "${INSTALL}" ] && [ "${INSTALL}" != "install" ]; then
    echo "-i 参数错误，只支持: install"
    echo_help
  fi

  # 安装公共依赖时，必须指定依赖项目目录
  if [ "${INSTALL}" = "install" ]; then
    if [ -z "${DEPENDENCY_DIR}" ]; then
      echo "使用 -i install 时必须指定依赖项目目录 -D"
      echo_help
    fi

    if [ ! -d "${DEPENDENCY_DIR}" ]; then
      echo "依赖项目目录不存在: ${DEPENDENCY_DIR}"
      exit 1
    fi
  fi
}


# WEB 参数校验
check_web_args() {
  # 必须指定源码目录
  if [ -z "${SOURCE_DIR}" ]; then
    echo "WEB 构建缺少源码目录参数 -d"
    echo_help
  fi

  if [ ! -d "${SOURCE_DIR}" ]; then
    echo "源码目录不存在: ${SOURCE_DIR}"
    exit 1
  fi
}