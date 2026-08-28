<template>
  <div class="video-container" :style="{ height: height }">
    <video ref="videoRef" class="video" autoplay muted playsinline controls></video>

    <div v-if="errorMessage" class="video-error">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount, defineProps, nextTick, defineExpose } from 'vue'

// MediaMTX 官方 reader.js
import '@/js/reader.js'

const props = defineProps({
  /**
   * MediaMTX 视频地址
   *
   * 例如：
   * http://124.221.195.130:8889/cam1/
   */
  videoSrc: {
    type: String,
    required: true,
  },

  /**
   * 播放器高度
   */
  height: {
    type: String,
    default: '100%',
  },

  /**
   * 重置播放器
   */
  resetOnLoad: {
    type: Boolean,
    default: false,
  },

  /**
   * 销毁播放器
   */
  destroyPlayer: {
    type: Boolean,
    default: false,
  },
})

const videoRef = ref(null)

let reader = null

let destroyed = false

let connecting = false

const errorMessage = ref('')

/**
 * 获取 WHEP 地址
 *
 * 例如：
 *
 * http://124.221.195.130:8889/cam1/
 *
 * 转换成：
 *
 * http://124.221.195.130:8889/cam1/whep
 */
const getWhepUrl = () => {
  const url = new URL(props.videoSrc)

  if (!url.pathname.endsWith('/')) {
    url.pathname += '/'
  }

  url.pathname += 'whep'

  return url.toString()
}

/**
 * 初始化播放器
 */
const initPlayer = async () => {
  if (destroyed || connecting || !videoRef.value || !props.videoSrc) {
    return
  }

  connecting = true

  errorMessage.value = ''

  try {
    /**
     * 如果当前已经存在 Reader
     * 先关闭当前 Reader
     *
     * 这里只影响当前 VideoPlayer。
     */
    if (reader) {
      console.log('关闭旧 MediaMTX Reader')

      try {
        reader.close()
      } catch (error) {
        console.error('关闭旧 Reader 失败:', error)
      }

      reader = null
    }

    /**
     * 清理旧的视频流
     */
    if (videoRef.value) {
      try {
        videoRef.value.pause()
        videoRef.value.srcObject = null
      } catch (error) {
        console.error('清理旧视频失败:', error)
      }
    }

    /**
     * 生成 WHEP 地址
     */
    const whepUrl = getWhepUrl()

    console.log('================================')
    console.log('开始连接 MediaMTX')
    console.log('视频地址:', props.videoSrc)
    console.log('WHEP 地址:', whepUrl)
    console.log('================================')

    /**
     * 使用 MediaMTX 官方 Reader
     *
     * 当前阶段不传 token。
     *
     * 因为你的：
     *
     * http://124.221.195.130:8889/cam1/
     *
     * 目前可以直接播放，没有启用鉴权。
     */
    reader = new window.MediaMTXWebRTCReader({
      url: whepUrl,

      /**
       * 播放错误
       */
      onError: (error) => {
        console.error('MediaMTX WebRTC 播放错误:', error)

        if (!destroyed) {
          errorMessage.value = error
        }
      },

      /**
       * 收到视频 / 音频 Track
       */
      onTrack: (event) => {
        console.log('收到 MediaMTX WebRTC Track')

        if (destroyed || !videoRef.value || !event.streams || event.streams.length === 0) {
          return
        }

        /**
         * MediaMTX 返回的 MediaStream
         *
         * 直接交给 video 播放。
         */
        videoRef.value.srcObject = event.streams[0]

        /**
         * 自动播放
         */
        videoRef.value
          .play()
          .then(() => {
            console.log('================================')
            console.log('视频播放成功')
            console.log('视频地址:', props.videoSrc)
            console.log('================================')

            errorMessage.value = ''
          })
          .catch((error) => {
            console.log('浏览器自动播放失败:', error)
            console.log('可以点击播放器播放按钮')
          })

        /**
         * 调整尺寸
         */
        resizePlayer()
      },

      /**
       * DataChannel
       */
      onDataChannel: (event) => {
        console.log('收到 MediaMTX DataChannel')

        if (!event.channel) {
          return
        }

        event.channel.binaryType = 'arraybuffer'

        event.channel.onmessage = (message) => {
          console.log('MediaMTX DataChannel:', message.data)
        }
      },
    })
  } catch (error) {
    console.error('初始化 MediaMTX 播放器失败:', error)

    if (!destroyed) {
      errorMessage.value = error instanceof Error ? error.message : String(error)
    }

    closePlayer()
  } finally {
    connecting = false
  }
}

/**
 * 关闭播放器
 *
 * 这里非常重要。
 *
 * Dialog 关闭 / 分屏移除 / 组件销毁时：
 *
 * Reader.close()
 *      ↓
 * RTCPeerConnection.close()
 *      ↓
 * WebRTC 连接关闭
 *
 * 同时：
 *
 * video.pause()
 * video.srcObject = null
 *
 * 确保不会继续播放声音。
 */
const closePlayer = () => {
  console.log('关闭当前 VideoPlayer')

  /**
   * 关闭 MediaMTX Reader
   */
  if (reader) {
    try {
      reader.close()
    } catch (error) {
      console.error('关闭 MediaMTX Reader 失败:', error)
    }

    reader = null
  }

  /**
   * 停止 video
   */
  if (videoRef.value) {
    try {
      videoRef.value.pause()

      /**
       * 清除 WebRTC MediaStream
       */
      videoRef.value.srcObject = null

      /**
       * 清除普通 src
       */
      videoRef.value.removeAttribute('src')

      /**
       * 强制刷新 video 状态
       */
      videoRef.value.load()
    } catch (error) {
      console.error('停止 video 失败:', error)
    }
  }

  connecting = false
}

/**
 * 重置播放器
 */
const resetPlayer = () => {
  console.log('重置 VideoPlayer')

  if (!videoRef.value) {
    return
  }

  try {
    videoRef.value.pause()

    /**
     * WebRTC MediaStream 一般没有 seek 能力。
     *
     * 所以这里只暂停，不强制 currentTime。
     */
    videoRef.value.currentTime = 0
  } catch (error) {
    console.error('重置播放器失败:', error)
  }
}

/**
 * 暂停
 */
const pausePlayer = () => {
  if (!videoRef.value) {
    return
  }

  videoRef.value.pause()
}

/**
 * 播放
 */
const playPlayer = () => {
  if (!videoRef.value) {
    return
  }

  videoRef.value
    .play()
    .then(() => {
      console.log('播放器开始播放')
    })
    .catch((error) => {
      console.log('播放失败:', error)
    })
}

/**
 * 调整播放器尺寸
 */
const resizePlayer = () => {
  if (!videoRef.value) {
    return
  }

  nextTick(() => {
    if (!videoRef.value) {
      return
    }

    const container = videoRef.value.parentElement

    if (!container) {
      return
    }

    const width = container.clientWidth
    const height = container.clientHeight

    console.log(`调整播放器尺寸: ${width} x ${height}`)
  })
}

/**
 * 组件挂载
 */
onMounted(() => {
  console.log('VideoPlayer mounted')

  destroyed = false

  /**
   * 初始化 MediaMTX Reader
   */
  initPlayer()

  /**
   * 浏览器窗口变化
   */
  window.addEventListener('resize', resizePlayer)
})

/**
 * resetOnLoad
 */
watch(
  () => props.resetOnLoad,
  (newVal) => {
    if (newVal) {
      resetPlayer()
    }
  },
)

/**
 * destroyPlayer
 *
 * 父组件可以通过修改 destroyPlayer
 * 主动关闭当前播放器。
 */
watch(
  () => props.destroyPlayer,
  (newVal) => {
    console.log('destroyPlayer:', newVal)

    if (newVal) {
      closePlayer()
    }
  },
)

/**
 * 视频地址发生变化
 *
 * 例如：
 *
 * cam1
 * ↓
 * cam2
 *
 * 只重新建立当前 VideoPlayer 的 Reader。
 */
watch(
  () => props.videoSrc,
  async (newSrc, oldSrc) => {
    console.log('视频地址发生变化')
    console.log('旧地址:', oldSrc)
    console.log('新地址:', newSrc)

    if (!newSrc || destroyed || newSrc === oldSrc) {
      return
    }

    /**
     * 关闭当前连接
     */
    closePlayer()

    await nextTick()

    /**
     * 建立新连接
     */
    if (!destroyed) {
      await initPlayer()
    }
  },
)

/**
 * 暴露方法给父组件
 */
defineExpose({
  resizePlayer,

  resetPlayer,

  pausePlayer,

  playPlayer,

  /**
   * 主动销毁播放器
   */
  destroyVideoPlayer: closePlayer,
})

/**
 * 组件销毁
 */
onBeforeUnmount(() => {
  console.log('VideoPlayer onBeforeUnmount')

  /**
   * 标记组件已经销毁
   *
   * 防止异步 Reader 在组件销毁后继续创建连接。
   */
  destroyed = true

  /**
   * 移除 resize
   */
  window.removeEventListener('resize', resizePlayer)

  /**
   * 彻底关闭 MediaMTX Reader
   */
  closePlayer()

  /**
   * 最后再保险清理一次原生 video
   */
  if (videoRef.value) {
    try {
      videoRef.value.pause()

      videoRef.value.srcObject = null

      videoRef.value.removeAttribute('src')

      videoRef.value.load()
    } catch (error) {
      console.error('清理原生 video 失败:', error)
    }
  }
})
</script>

<style scoped>
.video-container {
  position: relative;

  width: 100%;
  height: 100%;

  display: flex;

  justify-content: center;
  align-items: center;

  background: #000;

  overflow: hidden;
}

.video {
  width: 100%;
  height: 100%;

  display: block;

  background: #000;

  object-fit: contain;
}

.video-error {
  position: absolute;

  left: 50%;
  top: 50%;

  transform: translate(-50%, -50%);

  max-width: 80%;

  padding: 12px 20px;

  box-sizing: border-box;

  color: #fff;

  font-size: 14px;

  line-height: 1.5;

  text-align: center;

  background: rgba(0, 0, 0, 0.65);

  border-radius: 4px;

  word-break: break-word;
}
</style>
