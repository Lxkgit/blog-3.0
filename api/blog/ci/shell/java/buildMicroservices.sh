#!/bin/bash


source /opt/docker/ci/shell/config.sh


# =========================
# 参数
# =========================

PROFILE=""
MODULES=""
SOURCE_DIR=""


# =========================
# 帮助
# =========================

show_help() {

  echo "使用方式:"
  echo ""
  echo "  ./buildMicroservices.sh -e pro -m blog-auth,blog-gateway -d /opt/docker/ci/code/blog-3.0/api"
  echo ""
  echo "参数:"
  echo "  -e    构建环境: pro | test"
  echo "  -m    Maven 模块"
  echo "  -d    源码目录"
  echo ""

  exit 1
}


# =========================
# 参数解析
# =========================

parse_args() {

  while getopts ":e:m:d:" opt
  do
    case "${opt}" in

      e)
        PROFILE="${OPTARG}"
        ;;

      m)
        MODULES="${OPTARG}"
        ;;

      d)
        SOURCE_DIR="${OPTARG}"
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


  if [ -z "${MODULES}" ]; then
    echo "缺少 Maven 模块参数 -m"
    show_help
  fi


  if [ -z "${SOURCE_DIR}" ]; then
    echo "缺少源码目录参数 -d"
    show_help
  fi


  if [ ! -d "${SOURCE_DIR}" ]; then
    echo "源码目录不存在: ${SOURCE_DIR}"
    exit 1
  fi
}


# =========================
# Maven 构建
# =========================

build_microservices() {

  echo "========================================"
  echo "开始构建微服务包"
  echo "环境: ${PROFILE}"
  echo "模块: ${MODULES}"
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
    -pl "${MODULES}" \
    -am \
    -P"${PROFILE}" \
    -DskipTests


  if [ $? -ne 0 ]; then
    echo "微服务包构建失败"
    return 1
  fi


  echo "微服务包构建成功"

  return 0
}


# =========================
# 构建结果
# =========================

show_result() {

  echo "========================================"
  echo "微服务构建结果"
  echo "========================================"


  IFS=',' read -ra MODULE_LIST <<< "${MODULES}"


  for module in "${MODULE_LIST[@]}"
  do

    local jar_dir="${SOURCE_DIR}/${module}/target"


    echo ""
    echo "模块: ${module}"


    if [ -d "${jar_dir}" ]; then

      ls -lh "${jar_dir}"/*.jar 2>/dev/null

      if [ $? -ne 0 ]; then
        echo "未找到 Jar 包"
      fi

    else

      echo "未找到构建目录: ${jar_dir}"

    fi

  done


  echo "========================================"
}


# =========================
# 主流程
# =========================

main() {

  parse_args "$@"

  check_args

  build_microservices || exit 1

  show_result
}


main "$@"