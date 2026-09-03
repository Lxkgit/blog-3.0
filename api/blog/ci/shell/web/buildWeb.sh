#!/bin/bash


source /opt/docker/ci/shell/config.sh

# 参数
PROFILE=""
SOURCE_DIR=""


# 帮助
show_help() {
  echo "使用方式:"
  echo "  ./buildWeb.sh -e pro -d /opt/docker/ci/code/blog-3.0/web"
  echo ""
  echo "参数:"
  echo "  -e    构建环境: pro | test"
  echo "  -d    源码目录"
  echo ""
  exit 1
}

# 参数解析
parse_args() {
  while getopts ":e:d:" opt
  do
    case "${opt}" in
      e)
        PROFILE="${OPTARG}"
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

# 检查参数
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
  # package.json
  if [ ! -f "${SOURCE_DIR}/package.json" ]; then
    echo "未找到 package.json"
    echo "源码目录: ${SOURCE_DIR}"
    exit 1
  fi
}

# 更新 NPM 依赖
update_npm() {

  if [ ! -d "${NPM_DIR}" ]; then
    echo "创建 npm 依赖缓存目录"
    mkdir -p "${NPM_DIR}"
  fi

  echo "更新 npm 依赖"
  echo "源码目录: ${SOURCE_DIR}"
  echo "npm 缓存: ${NPM_DIR}"

  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
    -v "${SOURCE_DIR}:/workspace" \
    -v "${NPM_DIR}:/root/.npm" \
    -w /workspace \
    "${NODE_IMAGE}" \
    npm install

  if [ $? -ne 0 ]; then
    echo "npm 依赖更新失败"
    return 1
  fi

  echo "npm 依赖更新成功"

  return 0
}

# 前端构建
build_web() {
  echo "========================================"
  echo "开始前端构建"
  echo "环境: ${PROFILE}"
  echo "源码目录: ${SOURCE_DIR}"
  echo "========================================"
  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
    -v "${SOURCE_DIR}:/workspace" \
    -v "${NPM_DIR}:/root/.npm" \
    -w /workspace \
    "${NODE_IMAGE}" \
    npm run build

  if [ $? -ne 0 ]; then
    echo "前端构建失败"
    return 1
  fi

  echo "前端构建成功"

  return 0
}



# 构建结果


show_result() {

  echo "前端构建结果"
  local dist_dir="${SOURCE_DIR}/dist"

  if [ ! -d "${dist_dir}" ]; then
    echo "未找到前端构建目录"
    echo "目录: ${dist_dir}"
    return 1
  fi

  echo "构建目录: ${dist_dir}"

  ls -lh "${dist_dir}"

  return 0
}

# 主流程
main() {

  parse_args "$@"

  check_args

  update_npm || exit 1

  build_web || exit 1

  show_result || exit 1
}


main "$@"