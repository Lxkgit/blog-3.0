#!/bin/bash

# 参数
PROFILE=""
PROJECT_NAME=""
PACKAGE_NAME=""

INSTALL=""
MODULES=""

BUILD_TYPE=""
SOURCE_DIR=""

PROJECT_GIT_URL=""
PROJECT_SOURCE_DIR=""
PROJECT_BUILD_TYPES=()


# 帮助
show_help() {
  echo "使用方式:"
  echo "  微服务: ./buildController.sh -e pro -p blog -n api -m blog-auth,blog-gateway"
  echo "  单服务: ./buildController.sh -e pro -p blog -n pi"
  echo "  单服务 + 安装依赖: ./buildController.sh -e pro -p blog -n pi -i install"
  echo "  前端打包: ./buildController.sh -e pro -p blog -n web"
  echo ""
  echo "参数:"
  echo "  -e    构建环境: pro | test"
  echo "  -p    项目名称"
  echo "  -n    构建目录(仓库二级目录，没有可忽略)，例如 api | pi | web"
  echo "  -i    安装依赖: install 仅单服务构建使用"
  echo "  -m    Maven 模块，仅微服务构建使用"
  echo ""
  exit 1
}

# 参数解析
parse_args() {
  while getopts ":e:p:n:i:m:" opt
  do
    case "${opt}" in
      e)
        PROFILE="${OPTARG}"
        ;;
      p)
        PROJECT_NAME="${OPTARG}"
        ;;
      n)
        PACKAGE_NAME="${OPTARG}"
        ;;
      i)
        INSTALL="${OPTARG}"
        ;;
      m)
        MODULES="${OPTARG}"
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

# 检查环境
check_profile() {
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
}

# 检查项目
check_project_name() {
  if [ -z "${PROJECT_NAME}" ]; then
    echo "缺少项目参数 -p"
    show_help
  fi
}

# 获取项目配置
get_project_config() {
  local project_upper
  local git_url_var
  local source_dir_var
  local build_types_var

  project_upper=$(echo "${PROJECT_NAME}" | tr '[:lower:]' '[:upper:]')

  git_url_var="${project_upper}_GIT_URL"
  source_dir_var="${project_upper}_SOURCE_DIR"
  build_types_var="${project_upper}_BUILD_TYPES"

  PROJECT_GIT_URL="${!git_url_var}"
  PROJECT_SOURCE_DIR="${!source_dir_var}"

  # 通过 nameref 获取配置中的数组内容
  local -n build_types_ref="${build_types_var}"
  PROJECT_BUILD_TYPES=("${build_types_ref[@]}")

  if [ -z "${PROJECT_GIT_URL}" ]; then
    echo "不存在项目配置: ${PROJECT_NAME}"
    exit 1
  fi

  if [ -z "${PROJECT_SOURCE_DIR}" ]; then
    echo "项目源码目录未配置: ${PROJECT_NAME}"
    exit 1
  fi

  if [ "${#PROJECT_BUILD_TYPES[@]}" -eq 0 ]; then
    echo "项目未配置打包方式: ${PROJECT_NAME}"
    exit 1
  fi
}

# 获取构建配置 配置格式:
# 打包类型:打包服务名称(可选):打包依赖(可选,目前只用于Java服务打包前构建依赖包) microservices:api service:pi: api
get_build_config() {

  # 接收第一个参数
  local build_item="$1"

  # 构建类型
  local build_type
  # 构建目录
  local relative_dir
  # 依赖目录
  local dependency_dir

  # 获取构建类型  %%:* 表示: 从字符串末尾方向匹配 :* 然后删除最长匹配部分
  build_type="${build_item%%:*}"

  # 去掉构建类型  #*: 表示: 从字符串开头删除第一个 : 前面的内容 包括 :
  local config="${build_item#*:}"

  # 获取构建目录
  relative_dir="${config%%:*}"

  # 获取依赖目录
  if [[ "${config}" == *:* ]]; then
    dependency_dir="${config#*:}"
  else
    dependency_dir=""
  fi

  # 构建目录匹配
  if [ -n "${PACKAGE_NAME}" ]; then
    if [ "${relative_dir}" != "${PACKAGE_NAME}" ]; then
      return 1
    fi
  else
    # 未指定 -n 时，只允许配置的构建目录为空
    if [ -n "${relative_dir}" ]; then
      return 1
    fi
  fi

  # 设置构建类型
  BUILD_TYPE="${build_type}"

  # 设置源码目录
  if [ -n "${relative_dir}" ]; then
    SOURCE_DIR="${PROJECT_SOURCE_DIR}/${relative_dir}"
  else
    SOURCE_DIR="${PROJECT_SOURCE_DIR}"
  fi

  # 设置依赖目录
  DEPENDENCY_DIR=""
  if [ -n "${dependency_dir}" ]; then
    if [[ "${dependency_dir}" = /* ]]; then
      DEPENDENCY_DIR="${dependency_dir}"
    else
      DEPENDENCY_DIR="${PROJECT_SOURCE_DIR}/${dependency_dir}"
    fi
  fi
  return 0
}

# 解析构建配置
resolve_build_config() {

  local build_item

  
  # 未指定 -n
  

  if [ -z "${PACKAGE_NAME}" ]; then

    if [ "${#PROJECT_BUILD_TYPES[@]}" -ne 1 ]; then

      echo "项目包含多个构建目录，请使用 -n 指定"
      echo "项目: ${PROJECT_NAME}"
      echo ""
      echo "项目支持的构建目录:"

      for build_item in "${PROJECT_BUILD_TYPES[@]}"
      do
        local build_config="${build_item#*:}"
        local build_name="${build_config%%:*}"

        echo "  ${build_name}"
      done

      exit 1
    fi

    build_item="${PROJECT_BUILD_TYPES[0]}"

  else
    # 指定 -n
    for build_item in "${PROJECT_BUILD_TYPES[@]}"
    do
      if get_build_config "${build_item}"; then
        return 0
      fi
    done
    echo "无法找到构建配置"
    echo "项目: ${PROJECT_NAME}"
    echo "构建目录: ${PACKAGE_NAME}"
    echo ""
    echo "项目支持的构建目录:"
    for build_item in "${PROJECT_BUILD_TYPES[@]}"
    do
      local build_config="${build_item#*:}"
      local build_name="${build_config%%:*}"

      echo "  ${build_name}"
    done

    exit 1
  fi
  # 处理唯一构建配置
  get_build_config "${build_item}"
}

check_source_dir() {

  if [ ! -d "${SOURCE_DIR}" ]; then
    echo "源码目录不存在: ${SOURCE_DIR}"
    exit 1
  fi
}

# 微服务参数检查
check_microservices_args() {

  if [ -z "${MODULES}" ]; then
    echo "微服务构建缺少参数 -m"
    show_help
  fi

  if [ -n "${INSTALL}" ]; then
    echo "微服务构建不支持 -i"
    exit 1
  fi
}

# 单服务参数检查
check_service_args() {

  if [ -n "${MODULES}" ]; then
    echo "单服务构建不支持 -m"
    exit 1
  fi


  if [ -n "${INSTALL}" ] && [ "${INSTALL}" != "install" ]; then
    echo "-i 参数错误，只支持 install"
    exit 1
  fi


  if [ "${INSTALL}" = "install" ] && [ -z "${DEPENDENCY_DIR}" ]; then
    echo "当前单服务未配置公共依赖目录"
    echo "项目: ${PROJECT_NAME}"
    echo "构建目录: ${PACKAGE_NAME}"
    exit 1
  fi
}

# Web 参数检查
check_web_args() {

  if [ -n "${INSTALL}" ]; then
    echo "Web 构建不支持 -i"
    exit 1
  fi

  if [ -n "${MODULES}" ]; then
    echo "Web 构建不支持 -m"
    exit 1
  fi
}

# 构建参数检查
check_build_args() {

  case "${BUILD_TYPE}" in

    microservices)
      check_microservices_args
      ;;

    service)
      check_service_args
      ;;

    web)
      check_web_args
      ;;

    *)
      echo "不支持的构建类型: ${BUILD_TYPE}"
      exit 1
      ;;

  esac
}

# Controller 参数检查
check_controller_args() {

  check_profile
  check_project_name

  get_project_config
  resolve_build_config

  check_build_args
}