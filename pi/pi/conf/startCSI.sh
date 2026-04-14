#!/bin/bash

# 程序名称标识（便于停止）
PROC_NAME="rpicam-vid"

# 如果已在运行则先杀掉旧进程
pkill -f "$PROC_NAME"

# 启动推流
nohup sh -c "rpicam-vid -t 0 -n \
  --codec h264 --width 1280 --height 720 \
  --framerate 15 --bitrate 1500000 \
  --intra 20 \
  --profile high --level 4.2 \
  --denoise cdn_hq \
  -o - | \
ffmpeg -re -i - \
  -rtsp_transport tcp \
  -fflags nobuffer -flags low_delay -max_interleave_delta 0 -bufsize 500k \
  -vf \"drawtext=text='%{localtime}':fontcolor=white:fontsize=16:x=w-tw-10:y=10:box=1:boxcolor=black@0.5\" \
  -c:v libx264 -preset ultrafast -tune zerolatency \
  -f rtsp rtsp://127.0.0.1:8554/cam1" \
  > /dev/null 2>&1 &

echo "RTSP 推流已启动"