#!/bin/bash

# 停止 rpicam-vid 和 ffmpeg
pkill -f rpicam-vid
pkill -f ffmpeg

echo "RTSP 推流已停止"