#!/bin/bash


source /opt/docker/deploy/config.sh

cd ${SOURCE_DIR}/web

docker run --rm \
  -v ${SOURCE_DIR}/web:/workspace \
  -w /workspace \
  node:22 \
  bash -c "npm install &&npm run build"