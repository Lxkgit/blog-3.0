#!/bin/bash


# 项目名称
PROJECT_NAME="blog-3.0"
# Git仓库地址
GIT_URL="git@gitee.com:lxkgs/blog-3.0.git"
# 项目源码目录
SOURCE_DIR="/opt/docker/ci/code/${PROJECT_NAME}"

# Maven缓存目录
MAVEN_DIR="/opt/docker/ci/maven"
# Maven镜像
MAVEN_IMAGE="maven:3.9.9-eclipse-temurin-17"

# NPM缓存目录
NPM_DIR="/opt/docker/ci/npm"
# Node 镜像
NODE_IMAGE="node:22"
