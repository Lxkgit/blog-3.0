<template>
  <div class="video-container" :style="{ height: height }">
    <video ref="videoRef" class="video-js vjs-default-skin vjs-big-play-centered"></video>
  </div>
</template>

<script setup>
import {
  ref,
  onMounted,
  watch,
  onBeforeUnmount,
  defineProps,
  nextTick,
  defineExpose
} from 'vue';
import videojs from 'video.js';
import 'video.js/dist/video-js.css';

const props = defineProps({
  videoSrc: {
    type: String,
    required: true
  },
  height: {
    type: String,
    default: '100%'
  },
  resetOnLoad: {
    type: Boolean,
    default: false
  },
  destroyPlayer: {
    type: Boolean,
    default: false
  }
});

const videoRef = ref(null);
let player = null;


/**
 * 根据视频地址判断 Video.js 播放类型
 *
 * 摄像头：
 *   /rtsp/cam1/?token=xxx
 *   没有 mp4 后缀，因此使用 video/mp4
 *
 * 普通视频：
 *   xxx.mp4
 *   xxx.m3u8
 *   xxx.webm
 *   xxx.ogg
 *   根据后缀使用对应类型
 */
const getVideoType = (src) => {
  if (!src) {
    return 'video/mp4';
  }

  // 去掉 query 参数和 hash
  const cleanSrc = src.split('?')[0].split('#')[0].toLowerCase();

  // m3u8 / HLS
  if (cleanSrc.endsWith('.m3u8')) {
    return 'application/x-mpegURL';
  }

  // WebM
  if (cleanSrc.endsWith('.webm')) {
    return 'video/webm';
  }

  // Ogg
  if (cleanSrc.endsWith('.ogv') || cleanSrc.endsWith('.ogg')) {
    return 'video/ogg';
  }

  // MP4
  if (cleanSrc.endsWith('.mp4')) {
    return 'video/mp4';
  }

  /**
   * 摄像头视频流：
   *
   * 例如：
   * /rtsp/cam1/?token=xxxx
   *
   * 没有明确的文件后缀。
   *
   * 保持你现在摄像头能够正常播放的行为。
   */
  return 'video/mp4';
};


/**
 * 设置视频源
 */
const setVideoSource = (src) => {
  if (!player || !src) {
    return;
  }

  const type = getVideoType(src);

  console.log('设置视频源:', src);
  console.log('视频类型:', type);

  player.src({
    type,
    src
  });
};


// 初始化播放器
const initPlayer = () => {
  if (player) {
    player.dispose();
  }

  player = videojs(videoRef.value, {
    controls: true,
    preload: 'auto',
    autoplay: true,
    fluid: false,
    fill: false,
    responsive: false
  });

  setVideoSource(props.videoSrc);
};


// 重置播放器状态
const resetPlayer = () => {
  if (player) {
    console.log('重置播放器-1');

    player.currentTime(0);
    player.pause();
    player.hasStarted(false);
    player.trigger('reset');

    const controlBar = player.getChild('ControlBar');

    if (controlBar) {
      const playToggle = controlBar.getChild('PlayToggle');
      const progressControl = controlBar.getChild('ProgressControl');

      if (playToggle) {
        playToggle.trigger('reset');
      }

      if (progressControl) {
        progressControl.trigger('reset');
      }
    }
  }
};


const pausePlayer = () => {
  if (player) {
    player.pause();
  }
};


const playPlayer = () => {
  if (player) {
    player.play();
  }
};


// 调整播放器尺寸
const resizePlayer = () => {
  if (!player) return;

  nextTick(() => {
    if (!videoRef.value || !videoRef.value.parentElement) {
      return;
    }

    const container = videoRef.value.parentElement;

    const width = container.clientWidth;
    const height = container.clientHeight;

    player.width(width);
    player.height(height);

    console.log(`调整播放器尺寸: ${width} x ${height}`);
  });
};


onMounted(() => {
  initPlayer();

  window.addEventListener('resize', resizePlayer);

  // 初始化完成后调整一次尺寸
  resizePlayer();
});


// 监听重置标志变化
watch(() => props.resetOnLoad, (newVal) => {
  if (newVal) {
    resetPlayer();
  }
});


// 监听销毁标志
watch(() => props.destroyPlayer, (newVal) => {
  console.log('销毁播放器-watch');

  if (newVal) {
    destroyPlayer();
  }
});


// 监听视频源变化
watch(() => props.videoSrc, (newSrc) => {
  console.log('监听视频源变化-watch');
  console.log('视频源:', newSrc);

  if (player && newSrc) {
    setVideoSource(newSrc);

    // 尺寸可能需要重新调整
    resizePlayer();
  }
});


// 销毁播放器
const destroyPlayer = () => {
  if (player) {
    console.log('销毁播放器');

    player.dispose();
    player = null;
  }
};


// 暴露方法给父组件
defineExpose({
  resizePlayer,
  resetPlayer,
  pausePlayer,
  playPlayer,
  destroyPlayer
});


onBeforeUnmount(() => {
  if (player) {
    player.dispose();
    player = null;
  }

  window.removeEventListener('resize', resizePlayer);
});
</script>

<style scoped>
.video-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #000;
}

/* 确保 video 元素占满容器 */
.video-container> :deep(.video-js) {
  width: 100% !important;
  height: 100% !important;
  max-width: 100%;
  max-height: 100%;
}
</style>
