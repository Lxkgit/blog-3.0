#!/bin/bash


# 项目列表
PROJECT_NAMES=(
  "blog"
  "excel"
  "game"
)

# blog-3.0
BLOG_GIT_URL="git@gitee.com:lxkgs/blog-3.0.git"
BLOG_SOURCE_DIR="/opt/docker/ci/code/blog-3.0"
BLOG_BUILD_TYPES=(
  "microservices:api"
  "service:pi:api"
  "web:web"
)

# web-excel
EXCEL_GIT_URL="https://github.com/Lxkgit/web_excel.git"
EXCEL_SOURCE_DIR="/opt/docker/ci/code/web-excel"
EXCEL_BUILD_TYPES=(
  "web:"
)

# web-game
GAME_GIT_URL="https://github.com/Lxkgit/game_library.git"
GAME_SOURCE_DIR="/opt/docker/ci/code/game-library"
GAME_BUILD_TYPES=(
  "web:"
)


# Maven
MAVEN_DIR="/opt/docker/ci/maven"
MAVEN_IMAGE="maven:3.9.9-eclipse-temurin-17"

# NPM
NPM_DIR="/opt/docker/ci/npm"
NODE_IMAGE="node:22"