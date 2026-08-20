#!/bin/bash

# 停止 rpicam-vid 和 ffmpeg
pkill -f rpicam-vid
pkill -f ffmpeg

echo "$(date '+%Y-%m-%d %H:%M:%S'): RTSP 推流已停止，等待 watchdog 自动拉起"