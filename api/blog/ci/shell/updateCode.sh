#!/bin/bash


source /opt/docker/ci/shell/config.sh

# 更新代码
update_code()
{
  echo "======================"
  echo "检查代码目录"
  echo "目录: ${SOURCE_DIR}"
  echo "======================"
  # 首次部署，拉取代码
  if [ ! -d "${SOURCE_DIR}" ]; then
      echo "代码不存在，开始clone..."
      mkdir -p "$(dirname ${SOURCE_DIR})"
      git clone ${GIT_URL} ${SOURCE_DIR}
      if [ $? -ne 0 ]; then
          echo "git clone失败"
          exit 1
      fi
      echo "代码clone完成"
      return
  fi
  # 已存在代码
  cd ${SOURCE_DIR}
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


# 主流程
main()
{
  update_code
}

main