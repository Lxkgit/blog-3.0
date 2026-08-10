#!/bin/bash


# 项目名称
PROJECT_NAME="blog-3.0"
# Git仓库地址
GIT_URL="git@gitee.com:lxkgs/blog-3.0.git"
# 项目源码目录
SOURCE_DIR="/opt/docker/source/${PROJECT_NAME}"

# Maven缓存目录
MAVEN_DIR="/opt/docker/maven"
# Maven镜像
MAVEN_IMAGE="maven:3.9.9-eclipse-temurin-17"

# Node 镜像
NODE_IMAGE="node:22"