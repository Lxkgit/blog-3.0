#!/bin/bash

source /opt/docker/ci/shell/config.sh


PROJECT_NAME=""
GIT_URL=""
SOURCE_DIR=""

RETRY_COUNT=3
RETRY_WAIT=2


# 帮助
show_help() {

  echo "使用方式:"
  echo "  ./updateCode.sh -n blog"
  echo "  ./updateCode.sh -n excel"
  echo ""
  echo "参数:"
  echo "  -n    项目名称"
  echo ""

  exit 1
}

# 参数解析
parse_args() {
  while getopts ":n:" opt
  do
    case "${opt}" in
      n)
        PROJECT_NAME="${OPTARG}"
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

# 参数检查
check_args() {
  if [ -z "${PROJECT_NAME}" ]; then
    echo "缺少项目名称参数 -n"
    show_help
  fi
}

# 获取项目配置
get_project_config() {
  local project_upper
  local git_url_var
  local source_dir_var

  project_upper=$(echo "${PROJECT_NAME}" | tr '[:lower:]' '[:upper:]')

  git_url_var="${project_upper}_GIT_URL"
  source_dir_var="${project_upper}_SOURCE_DIR"

  GIT_URL="${!git_url_var}"
  SOURCE_DIR="${!source_dir_var}"

  if [ -z "${GIT_URL}" ]; then
    echo "不存在项目配置: ${PROJECT_NAME}"
    exit 1
  fi

  if [ -z "${SOURCE_DIR}" ]; then
    echo "项目源码目录未配置: ${PROJECT_NAME}"
    exit 1
  fi
}

# 克隆项目
clone_project() {

  echo "项目目录不存在，开始克隆..."
  echo "Git: ${GIT_URL}"
  echo "目录: ${SOURCE_DIR}"

  git clone "${GIT_URL}" "${SOURCE_DIR}"

  if [ $? -ne 0 ]; then
    echo "Git 克隆失败"
    return 1
  fi

  return 0
}

# 拉取项目
pull_project() {

  cd "${SOURCE_DIR}" || {
    echo "无法进入项目目录: ${SOURCE_DIR}"
    return 1
  }

  echo "当前分支: $(git branch --show-current)"
  echo "开始拉取代码..."

  git pull

  if [ $? -ne 0 ]; then
    echo "Git 更新失败"
    return 1
  fi

  return 0
}

# 更新代码
update_code() {

  if [ ! -d "${SOURCE_DIR}" ]; then
    clone_project
  else
    pull_project
  fi
}

# 重试更新
retry_update() {

  local attempt=1

  while [ "${attempt}" -le "${RETRY_COUNT}" ]
  do

    echo "第 ${attempt}/${RETRY_COUNT} 次更新"
    echo "项目: ${PROJECT_NAME}"

    if update_code; then

      echo "项目更新成功"
      echo "项目: ${PROJECT_NAME}"

      return 0
    fi

    if [ "${attempt}" -lt "${RETRY_COUNT}" ]; then

      echo ""
      echo "更新失败，${RETRY_WAIT} 秒后重试..."

      sleep "${RETRY_WAIT}"

    fi

    attempt=$((attempt + 1))

  done


  echo "项目更新失败"
  echo "项目: ${PROJECT_NAME}"
  echo "重试次数: ${RETRY_COUNT}"

  return 1
}

# 主函数
main() {

  parse_args "$@"

  check_args

  get_project_config

  echo "========================================"
  echo "开始更新项目"
  echo "项目: ${PROJECT_NAME}"
  echo "Git: ${GIT_URL}"
  echo "目录: ${SOURCE_DIR}"
  echo "最大重试次数: ${RETRY_COUNT}"
  echo "========================================"

  retry_update

  if [ $? -ne 0 ]; then
    exit 1
  fi
}


main "$@"