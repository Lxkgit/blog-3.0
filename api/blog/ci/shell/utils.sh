#!/bin/bash


# 更新代码
update_code() {

  if [ -z "${PROJECT_NAME}" ]; then
    echo "项目名称为空，无法更新代码"
    exit 1
  fi

  echo "========================================"
  echo "开始更新代码"
  echo "项目: ${PROJECT_NAME}"

  /opt/docker/ci/shell/updateCode.sh \
    -n "${PROJECT_NAME}"

  if [ $? -ne 0 ]; then
    echo "代码更新失败: ${PROJECT_NAME}"
    exit 1
  fi

  echo "代码更新成功: ${PROJECT_NAME}"
}