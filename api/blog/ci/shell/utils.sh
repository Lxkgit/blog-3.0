#!/bin/bash

# 更新代码
update_code() {
  /opt/docker/ci/shell/updateCode.sh

  if [ $? -ne 0 ]; then
    echo "代码更新失败"
    exit 1
  fi
}