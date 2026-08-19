#!/bin/bash

source /opt/docker/ci/shell/config.sh
source /opt/docker/ci/shell/args.sh
source /opt/docker/ci/shell/utils.sh

# 更新 npm 依赖
update_npm()
{
  if [ ! -d "${NPM_DIR}" ]; then
    echo "创建 npm 依赖缓存目录"
    mkdir -p "${NPM_DIR}"
  fi

  echo "更新 npm 依赖"

  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
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
  echo "环境: ${PROFILE}"

  docker run --rm \
    --cpus=2 \
    --memory=2g \
    --memory-swap=2g \
    -v ${SOURCE_DIR}/web:/workspace \
    -v ${NPM_DIR}:/root/.npm \
    -w /workspace \
    ${NODE_IMAGE} \
    npm run build

  if [ $? -ne 0 ]; then
    echo "前端构建失败"
    exit 1
  fi

  echo "前端构建成功"
}


# 展示打包结果
show_result()
{
  echo "========================================"
  echo "前端构建结果："
  if [ -d "${SOURCE_DIR}/web/dist" ]; then
    echo "构建目录: ${SOURCE_DIR}/web/dist"
    ls -lh "${SOURCE_DIR}/web/dist"
  else
    echo "未找到前端构建目录"
    exit 1
  fi
  echo "========================================"
}


# 主流程
main()
{
  # 参数解析
  parse_args "$@"

  # 参数校验
  check_common_args

  # 更新代码
  update_code

  # 更新 npm 依赖
  update_npm

  # 前端打包
  build_web

  # 展示结果
  show_result
}


main "$@"