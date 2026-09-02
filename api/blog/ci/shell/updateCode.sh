#!/bin/bash

source /opt/docker/ci/shell/config.sh


# 更新代码
update_code() {

  echo "======================"
  echo "检查代码目录"
  echo "项目: ${PROJECT_NAME}"
  echo "仓库: ${GIT_URL}"
  echo "目录: ${SOURCE_DIR}"
  echo "======================"

  # 首次部署，拉取代码
  if [ ! -d "${SOURCE_DIR}" ]; then
      echo "代码不存在，开始clone..."

      mkdir -p "$(dirname "${SOURCE_DIR}")"

      git clone "${GIT_URL}" "${SOURCE_DIR}"

      if [ $? -ne 0 ]; then
          echo "git clone失败"
          exit 1
      fi

      echo "代码clone完成"
      return
  fi

  # 已存在代码
  cd "${SOURCE_DIR}" || exit 1

  # 判断是否git仓库
  if [ ! -d ".git" ]; then
      echo "目录存在，但是不是git仓库"
      exit 1
  fi

  echo "拉取最新代码..."

  git pull

  if [ $? -ne 0 ]; then
      echo "git pull失败"
      exit 1
  fi

  echo "代码更新完成"
}


# =========================
# 主流程
# =========================

main() {

  # 参数解析
  while getopts "n:" opt; do
    case "${opt}" in
      n)
        PROJECT_NAME="${OPTARG}"
        ;;
      *)
        echo "用法: $0 -n 项目名称"
        exit 1
        ;;
    esac
  done

  # 必须指定项目
  if [ -z "${PROJECT_NAME}" ]; then
      echo "错误: 必须指定项目名称"
      echo "用法: $0 -n 项目名称"
      exit 1
  fi


  # 根据项目选择配置
  case "${PROJECT_NAME}" in

    blog-3.0)
      GIT_URL="${BLOG_GIT_URL}"
      SOURCE_DIR="${BLOG_SOURCE_DIR}"
      ;;

    web-excel)
      GIT_URL="${WEB_EXCEL_GIT_URL}"
      SOURCE_DIR="${WEB_EXCEL_SOURCE_DIR}"
      ;;

    *)
      echo "错误: 不支持的项目: ${PROJECT_NAME}"
      echo ""
      echo "支持的项目:"
      echo "  blog-3.0"
      exit 1
      ;;

  esac

  update_code
}


main "$@"