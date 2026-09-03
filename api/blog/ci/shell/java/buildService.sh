#!/bin/bash


source /opt/docker/ci/shell/config.sh


# =========================
# 参数
# =========================

PROFILE=""
INSTALL=""
SOURCE_DIR=""
DEPENDENCY_DIR=""


# =========================
# 帮助
# =========================

show_help() {

  echo "使用方式:"
  echo ""
  echo "  单服务:"
  echo "    ./buildService.sh -e pro -d /opt/docker/ci/code/blog-3.0/pi"
  echo ""
  echo "  单服务 + 安装公共依赖:"
  echo "    ./buildService.sh -e pro -i install -d /opt/docker/ci/code/blog-3.0/pi -D /opt/docker/ci/code/blog-3.0/api"
  echo ""
  echo "参数:"
  echo "  -e    构建环境: pro | test"
  echo "  -i    安装依赖: install"
  echo "  -d    源码目录"
  echo "  -D    公共依赖目录"
  echo ""

  exit 1
}


# =========================
# 参数解析
# =========================

parse_args() {

  while getopts ":e:i:d:D:" opt
  do
    case "${opt}" in

      e)
        PROFILE="${OPTARG}"
        ;;

      i)
        INSTALL="${OPTARG}"
        ;;

      d)
        SOURCE_DIR="${OPTARG}"
        ;;

      D)
        DEPENDENCY_DIR="${OPTARG}"
        ;;

      :)
        echo "参数 -${OPTARG} 缺少参数"
        show_help
        ;;

      \?)
        echo "未知参数: -${OPTARG}"
        show_help
        ;;

    esac
  done
}


# =========================
# 检查参数
# =========================

check_args() {

  # 环境

  if [ -z "${PROFILE}" ]; then
    echo "缺少环境参数 -e"
    show_help
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


  # 源码目录

  if [ -z "${SOURCE_DIR}" ]; then
    echo "缺少源码目录参数 -d"
    show_help
  fi

  if [ ! -d "${SOURCE_DIR}" ]; then
    echo "源码目录不存在: ${SOURCE_DIR}"
    exit 1
  fi


  # 安装参数

  if [ -n "${INSTALL}" ] && [ "${INSTALL}" != "install" ]; then
    echo "-i 参数错误，只支持 install"
    exit 1
  fi


  # 安装依赖时必须指定依赖目录

  if [ "${INSTALL}" = "install" ]; then

    if [ -z "${DEPENDENCY_DIR}" ]; then
      echo "使用 -i install 时必须指定依赖目录 -D"
      show_help
    fi

    if [ ! -d "${DEPENDENCY_DIR}" ]; then
      echo "依赖目录不存在: ${DEPENDENCY_DIR}"
      exit 1
    fi

  else

    if [ -n "${DEPENDENCY_DIR}" ]; then
      echo "未使用 -i install 时不允许使用 -D"
      exit 1
    fi

  fi
}


# =========================
# 安装公共依赖
# =========================

install_dependency() {

  echo "========================================"
  echo "开始安装公共依赖"
  echo "依赖目录: ${DEPENDENCY_DIR}"
  echo "========================================"


  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
    -v "${DEPENDENCY_DIR}:/workspace" \
    -v "${MAVEN_DIR}:/root/.m2" \
    -w /workspace \
    "${MAVEN_IMAGE}" \
    mvn clean install \
    -DskipTests


  if [ $? -ne 0 ]; then
    echo "公共依赖安装失败"
    return 1
  fi


  echo "公共依赖安装成功"

  return 0
}


# =========================
# 单服务构建
# =========================

build_service() {

  echo "========================================"
  echo "开始构建单服务包"
  echo "环境: ${PROFILE}"
  echo "源码目录: ${SOURCE_DIR}"
  echo "========================================"


  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
    -v "${SOURCE_DIR}:/workspace" \
    -v "${MAVEN_DIR}:/root/.m2" \
    -w /workspace \
    "${MAVEN_IMAGE}" \
    mvn clean package \
    -P"${PROFILE}" \
    -DskipTests


  if [ $? -ne 0 ]; then
    echo "单服务包构建失败"
    return 1
  fi


  echo "单服务包构建成功"

  return 0
}


# =========================
# 构建结果
# =========================

show_result() {

  echo "========================================"
  echo "单服务构建结果"
  echo "========================================"


  local jar_dir="${SOURCE_DIR}/target"


  if [ ! -d "${jar_dir}" ]; then
    echo "未找到构建目录"
    echo "目录: ${jar_dir}"
    return 1
  fi


  echo "构建目录: ${jar_dir}"


  ls -lh "${jar_dir}"/*.jar 2>/dev/null

  if [ $? -ne 0 ]; then
    echo "未找到 Jar 包"
    return 1
  fi


  echo "========================================"

  return 0
}


# =========================
# 主流程
# =========================

main() {

  parse_args "$@"

  check_args


  if [ "${INSTALL}" = "install" ]; then

    install_dependency || exit 1

  fi


  build_service || exit 1


  show_result || exit 1
}


main "$@"