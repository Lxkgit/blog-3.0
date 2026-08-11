#!/bin/bash

source /opt/docker/ci/shell/config.sh
PROFILE=$1


# 参数校验
check_param()
{
  if [ -z "${PROFILE}" ]; then
    echo "缺少环境"
    exit 1
  fi
  if [ ! -d "${NPM_DIR}" ]; then
    echo "创建 npm 依赖缓存目录"
    mkdir -p "${NPM_DIR}"
  fi
}


# 更新代码
update_code()
{
  /opt/docker/ci/shell/updateCode.sh
  if [ $? -ne 0 ]; then
    echo "代码更新失败"
    exit 1
  fi
}


# 更新 npm 依赖
update_npm()
{
  echo "更新 npm 依赖"
  docker run --rm \
    -v ${SOURCE_DIR}/web:/workspace \
    -v ${NPM_DIR}:/root/.npm \
    -w /workspace \
    ${NODE_IMAGE} \
    npm install

  if [ $? -ne 0 ]; then
    echo "npm 依赖更新失败"
    exit 1
  fi
}


# 前端打包
build_web()
{
  echo "开始前端构建"
  docker run --rm \
    -v ${SOURCE_DIR}/web:/workspace \
    -v ${NPM_DIR}:/root/.npm \
    -w /workspace \
    ${NODE_IMAGE} \
    npm run build

  if [ $? -ne 0 ]; then
    echo "前端构建失败"
    exit 1
  fi
}


# 展示打包结果
show_result()
{
  echo "前端构建结果："
  if [ -d "${SOURCE_DIR}/web/dist" ]; then
    echo "${SOURCE_DIR}/web/dist"
    ls -lh "${SOURCE_DIR}/web/dist"
  else
    echo "未找到前端构建目录"
  fi
}


main()
{
  # 参数校验
  check_param
  # 代码更新
  update_code
  # 更新 npm 依赖
  update_npm
  # 前端打包
  build_web
  # 展示打包结果
  show_result
}

main