<template>
  <div class="video-container" :style="{ height: height }">
    <video ref="videoRef" class="video-js vjs-default-skin vjs-big-play-centered"></video>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount, defineProps, nextTick, defineExpose } from 'vue';
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
  resetOnLoad: { // 新增重置标志
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

// 初始化播放器
const initPlayer = () => {
  if (player) {
    player.dispose(); // 销毁旧播放器
  }

  player = videojs(videoRef.value, {
    controls: true,
    preload: 'auto',
    autoplay: true,
    fluid: false, // 禁用流体布局，使用固定尺寸
    fill: false,  // 禁用填充模式
    responsive: false // 禁用响应式
  });

  player.src({
    type: 'video/mp4',
    src: props.videoSrc
  });

};

// 重置播放器状态
const resetPlayer = () => {
  if (player) {
    console.log("重置播放器-1")
    player.currentTime(0); // 重置进度到开始
    player.pause();        // 暂停播放
    player.hasStarted(false); // 重置播放状态
    player.trigger('reset'); // 触发自定义重置事件

    // 重置控制条状态
    const controlBar = player.getChild('ControlBar');
    if (controlBar) {
      controlBar.getChild('PlayToggle').trigger('reset');
      controlBar.getChild('ProgressControl').trigger('reset');
    }
  }
};

const pausePlayer = () => {
  if (player) {
    player.pause();
  }
}

const playPlayer = () => {
  if (player) {
    player.play();
  }
}


// 调整播放器尺寸
const resizePlayer = () => {
  if (!player) return;

  nextTick(() => {
    // 获取容器实际尺寸
    const container = videoRef.value.parentElement;
    const width = container.clientWidth;
    const height = container.clientHeight;

    // 设置播放器尺寸
    player.width(width);
    player.height(height);

    console.log(`调整播放器尺寸: ${width} x ${height}`);
  });
};

onMounted(() => {
  initPlayer();

  // 添加窗口和容器尺寸变化的监听
  window.addEventListener('resize', resizePlayer);
});

// 监听重置标志变化
watch(() => props.resetOnLoad, (newVal) => {
  if (newVal) {
    resetPlayer();
  }
});

// 监听重置标志变化
watch(() => props.destroyPlayer, (newVal) => {
  console.log("销毁播放器-watch")
  if (newVal) {
    destroyPlayer();
  }
});

// 监听视频源变化
watch(() => props.videoSrc, (newSrc) => {
  console.log("监听视频源变化-watch")
  console.log("视频源:" + newSrc)
  if (player && newSrc) {
    player.src({
      type: 'video/mp4',
      src: newSrc
    });

    // 尺寸可能需要重新调整
    resizePlayer();
  }
});

// 暴露方法给父组件
defineExpose({
  resizePlayer,
  resetPlayer, // 暴露重置方法
  pausePlayer,
  playPlayer
});

onBeforeUnmount(() => {
  if (player) {
    player.dispose();
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
  /* 添加背景色避免空白 */
}

/* 确保 video 元素占满容器 */
.video-container> :deep(.video-js) {
  width: 100% !important;
  height: 100% !important;
  max-width: 100%;
  max-height: 100%;
}
</style>
