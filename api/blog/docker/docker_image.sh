#!/bin/sh

# 下载并导出docker镜像文件压缩包

# 异常立即退出脚本
set -e

# 正常情况下镜像都是存在的
docker pull openjdk:17
docker pull mysql:8.0.20
docker pull fauria/vsftpd
docker pull nginx:1.20.2
docker pull redis:6.2.5
docker pull nacos/nacos-server:v2.4.3
docker pull apache/rocketmq:5.1.4
docker pull elasticsearch:7.14.1
docker pull minio/minio:RELEASE.2025-05-24T17-08-30Z
docker pull bluenviron/mediamtx:1

# docker将下列镜像保存到 blog_docker_images_x86.tar 文件中
docker save \
    -o blog_docker_images_x86.tar \
    openjdk:17 \
    mysql:8.0.20 \
    fauria/vsftpd \
    nginx:1.20.2 \
    redis:6.2.5 \
    nacos/nacos-server:v2.4.3 \
    apache/rocketmq:5.1.4 \
    elasticsearch:7.14.1 \
    minio/minio:RELEASE.2025-05-24T17-08-30Z \
    bluenviron/mediamtx:1

# 将tar文件压缩为 .tar.gz 文件，并不保留原文件
gzip blog_docker_images_x86.tar

# 输出文件名称 blog_docker_images_x86.tar.gz