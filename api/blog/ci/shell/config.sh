#!/bin/bash

# =========================
# blog-3.0
# =========================
BLOG_PROJECT_NAME="blog-3.0"
BLOG_GIT_URL="git@gitee.com:lxkgs/blog-3.0.git"
BLOG_SOURCE_DIR="/opt/docker/ci/code/${BLOG_PROJECT_NAME}"

# =========================
# web-excel
# =========================
WEB_EXCEL_PROJECT_NAME="web-excel"
WEB_EXCEL_GIT_URL="https://github.com/Lxkgit/web_excel.git"
WEB_EXCEL_SOURCE_DIR="/opt/docker/ci/code/${WEB_EXCEL_PROJECT_NAME}"

# Maven缓存目录
MAVEN_DIR="/opt/docker/ci/maven"
# Maven镜像
MAVEN_IMAGE="maven:3.9.9-eclipse-temurin-17"

# NPM缓存目录
NPM_DIR="/opt/docker/ci/npm"
# Node 镜像
NODE_IMAGE="node:22"
