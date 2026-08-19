#!/bin/bash


# ==============================
# 公共参数
# ==============================
TYPE=""
PROFILE=""
INSTALL=""
MODULES=""


# ==============================
# 帮助
# ==============================
echo_help()
{
  echo "参数错误"
  echo ""
  echo "使用方式:"
  echo ""
  echo "  博客整体构建:"
  echo "    ./buildController.sh -s blog -e pro"
  echo ""
  echo "  接口服务构建:"
  echo "    ./buildController.sh -s api -e pro -m blog-auth,blog-gateway,blog-content,blog-file"
  echo ""
  echo "  树莓派构建:"
  echo "    ./buildController.sh -s pi -e pro"
  echo ""
  echo "  树莓派构建并安装公共依赖:"
  echo "    ./buildController.sh -s pi -e pro -i install"
  echo ""
  echo "  前端页面构建:"
  echo "    ./buildController.sh -s web -e pro"
  echo ""
  echo "参数说明:"
  echo "  -s    构建类型: blog | api | pi | web"
  echo "  -e    构建环境: pro | test"
  echo "  -i    安装公共依赖: install，仅 pi 使用"
  echo "  -m    Maven 模块列表，仅 api 使用"
  echo ""

  exit 1
}


# ==============================
# 参数解析
# ==============================
parse_args()
{
  TYPE=""
  PROFILE=""
  INSTALL=""
  MODULES=""

  while getopts ":s:e:i:m:" opt
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


# ==============================
# Controller 参数校验
# ==============================
check_controller_args()
{
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

    blog|web)
      ;;

  esac
}


# ==============================
# 公共参数校验
# ==============================
check_common_args()
{
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


# ==============================
# API 参数校验
# ==============================
check_api_args()
{
  if [ -z "${MODULES}" ]; then
    echo "API 构建缺少模块参数 -m"
    echo_help
  fi
}


# ==============================
# PI 参数校验
# ==============================
check_pi_args()
{
  if [ -n "${INSTALL}" ] && [ "${INSTALL}" != "install" ]; then
    echo "-i 参数错误，只支持: install"
    exit 1
  fi
}


# ==============================
# WEB 参数校验
# ==============================
check_web_args()
{
  :
}