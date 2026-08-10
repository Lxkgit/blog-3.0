#!/bin/bash


source /opt/docker/deploy/config.sh

TYPE=$1
shift
case ${TYPE} in
java)
  echo "进入后端构建"
  /opt/docker/deploy/java/build.sh "$@"
  ;;
web)
  echo "进入前端构建"
  /opt/docker/deploy/web/build.sh "$@"
  ;;
*)
  echo "未知构建类型"
  echo ""
  echo "使用方式:"
  echo ""
  echo "后端:"
  echo "./build.sh java pro blog-gateway,blog-file"
  echo ""
  echo "前端:"
  echo "./build.sh web pro"
  exit 1
  ;;
esac