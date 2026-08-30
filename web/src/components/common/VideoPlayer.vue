<template>
  <div class="video-container" :style="{ height: height }">

    <!--
      这里仍然使用原来的 video 元素。

      普通视频：
        Video.js 接管这个 video

      摄像头：
        MediaMTX WebRTC Reader
        直接设置 video.srcObject
    -->
    <video ref="videoRef" class="video-js vjs-default-skin vjs-big-play-centered" autoplay muted playsinline></video>

    <!-- 播放错误 -->
    <div v-if="errorMessage" class="video-error">
      {{ errorMessage }}
    </div>
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
  defineExpose,
} from 'vue'

import videojs from 'video.js'
import 'video.js/dist/video-js.css'

// MediaMTX 官方 reader.js
import '@/js/reader.js'


const props = defineProps({
  /**
   * 视频地址
   *
   * 普通视频：
   * http://xxx/video/test.mp4
   *
   * 摄像头：
   * http://xxx/rtsp/cam1/?token=xxxx
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

/**
 * Video.js Player
 *
 * 只有普通视频使用。
 */
let player = null

/**
 * MediaMTX Reader
 *
 * 只有摄像头使用。
 */
let reader = null

/**
 * 组件是否已经销毁
 */
let destroyed = false

/**
 * MediaMTX 是否正在连接
 */
let connecting = false

/**
 * 当前播放模式
 *
 * normal = 普通视频
 * camera = 摄像头
 */
let currentMode = null

/**
 * 错误信息
 */
const errorMessage = ref('')


/**
 * ============================================================
 * 判断是否为摄像头视频
 * ============================================================
 *
 * 现在你的摄像头统一通过：
 *
 * /rtsp/cam1/
 *
 * 所以只要 pathname 以 /rtsp/ 开头，
 * 就使用 MediaMTX WebRTC。
 */
const isCameraStream = (src) => {
  if (!src) {
    return false
  }

  try {
    const url = new URL(
      src,
      window.location.origin,
    )

    return url.pathname.startsWith('/rtsp/')
  } catch (error) {
    console.error(
      '判断视频类型失败:',
      error,
    )

    return false
  }
}


/**
 * ============================================================
 * 获取 WHEP 地址
 * ============================================================
 *
 * 例如：
 *
 * /rtsp/cam1/?token=xxxx
 *
 * ↓
 *
 * /rtsp/cam1/whep?token=xxxx
 */
const getWhepUrl = (src) => {
  const url = new URL(src)

  if (!url.pathname.endsWith('/')) {
    url.pathname += '/'
  }

  url.pathname += 'whep'

  return url.toString()
}


/**
 * ============================================================
 * 初始化 Video.js
 * ============================================================
 *
 * 这里基本保持你最开始的配置。
 */
const initVideoJs = () => {
  if (!videoRef.value) {
    return
  }

  /**
   * 如果已经存在旧播放器
   * 先销毁
   */
  if (player) {
    try {
      player.dispose()
    } catch (error) {
      console.error(
        '销毁旧 Video.js 失败:',
        error,
      )
    }

    player = null
  }

  /**
   * 初始化 Video.js
   *
   * 保留你原来的配置：
   *
   * controls: true
   * preload: auto
   * autoplay: true
   * fluid: false
   * fill: false
   * responsive: false
   */
  player = videojs(
    videoRef.value,
    {
      controls: true,

      preload: 'auto',

      autoplay: true,

      fluid: false,

      fill: false,

      responsive: false,
    },
  )

  /**
   * 普通视频设置视频源
   */
  if (props.videoSrc) {
    player.src({
      type: 'video/mp4',
      src: props.videoSrc,
    })
  }
}


/**
 * ============================================================
 * 关闭 Video.js
 * ============================================================
 */
const closeVideoJs = () => {
  if (!player) {
    return
  }

  console.log(
    '关闭 Video.js 播放器',
  )

  try {
    player.pause()

    player.src({
      type: 'video/mp4',
      src: '',
    })

    player.dispose()
  } catch (error) {
    console.error(
      '关闭 Video.js 失败:',
      error,
    )
  }

  player = null
}


/**
 * ============================================================
 * 初始化 MediaMTX 摄像头
 * ============================================================
 */
const initCamera = async () => {
  if (
    destroyed ||
    connecting ||
    !videoRef.value ||
    !props.videoSrc
  ) {
    return
  }

  connecting = true

  errorMessage.value = ''

  try {

    /**
     * 确保 Video.js 已经关闭
     */
    closeVideoJs()

    /**
     * 如果存在旧 Reader
     */
    if (reader) {
      console.log(
        '关闭旧 MediaMTX Reader',
      )

      try {
        reader.close()
      } catch (error) {
        console.error(
          '关闭旧 Reader 失败:',
          error,
        )
      }

      reader = null
    }

    /**
     * 清理旧 video
     */
    if (videoRef.value) {
      try {
        videoRef.value.pause()

        videoRef.value.srcObject = null

        videoRef.value.removeAttribute(
          'src',
        )

        videoRef.value.load()
      } catch (error) {
        console.error(
          '清理旧视频失败:',
          error,
        )
      }
    }

    /**
     * 获取 WHEP 地址
     */
    const whepUrl = getWhepUrl(
      props.videoSrc,
    )

    console.log('================================')
    console.log(
      '开始连接 MediaMTX',
    )
    console.log(
      '视频地址:',
      props.videoSrc,
    )
    console.log(
      'WHEP 地址:',
      whepUrl,
    )
    console.log('================================')

    /**
     * MediaMTX 官方 Reader
     */
    reader =
      new window.MediaMTXWebRTCReader({
        url: whepUrl,

        /**
         * 播放错误
         */
        onError: (error) => {
          console.error(
            'MediaMTX WebRTC 播放错误:',
            error,
          )

          if (!destroyed) {
            errorMessage.value =
              error instanceof Error
                ? error.message
                : String(error)
          }
        },

        /**
         * 收到 Track
         */
        onTrack: (event) => {
          console.log(
            '收到 MediaMTX WebRTC Track',
          )

          if (
            destroyed ||
            !videoRef.value ||
            !event.streams ||
            event.streams.length === 0
          ) {
            return
          }

          /**
           * MediaStream
           * ↓
           * video
           */
          videoRef.value.srcObject =
            event.streams[0]

          /**
           * 自动播放
           */
          videoRef.value
            .play()
            .then(() => {
              console.log(
                '================================',
              )

              console.log(
                '摄像头视频播放成功',
              )

              console.log(
                '视频地址:',
                props.videoSrc,
              )

              console.log(
                '================================',
              )

              errorMessage.value = ''
            })
            .catch((error) => {
              console.log(
                '浏览器自动播放失败:',
                error,
              )

              console.log(
                '可以点击播放器播放按钮',
              )
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
          console.log(
            '收到 MediaMTX DataChannel',
          )

          if (!event.channel) {
            return
          }

          event.channel.binaryType =
            'arraybuffer'

          event.channel.onmessage = (
            message,
          ) => {
            console.log(
              'MediaMTX DataChannel:',
              message.data,
            )
          }
        },
      })
  } catch (error) {
    console.error(
      '初始化 MediaMTX 播放器失败:',
      error,
    )

    if (!destroyed) {
      errorMessage.value =
        error instanceof Error
          ? error.message
          : String(error)
    }

    closeCamera()
  } finally {
    connecting = false
  }
}


/**
 * ============================================================
 * 关闭摄像头
 * ============================================================
 */
const closeCamera = () => {
  console.log(
    '关闭 MediaMTX 摄像头',
  )

  /**
   * 关闭 Reader
   */
  if (reader) {
    try {
      reader.close()
    } catch (error) {
      console.error(
        '关闭 MediaMTX Reader 失败:',
        error,
      )
    }

    reader = null
  }

  /**
   * 清理 video
   */
  if (videoRef.value) {
    try {
      videoRef.value.pause()

      videoRef.value.srcObject = null

      videoRef.value.removeAttribute(
        'src',
      )

      videoRef.value.load()
    } catch (error) {
      console.error(
        '停止摄像头视频失败:',
        error,
      )
    }
  }

  connecting = false
}


/**
 * ============================================================
 * 初始化播放器
 * ============================================================
 *
 * 这里是整个组件最核心的地方。
 *
 * 普通视频：
 *     Video.js
 *
 * 摄像头：
 *     MediaMTX WebRTC
 */
const initPlayer = async () => {
  if (
    destroyed ||
    !videoRef.value ||
    !props.videoSrc
  ) {
    return
  }

  /**
   * 判断当前视频类型
   */
  const camera =
    isCameraStream(
      props.videoSrc,
    )

  /**
   * 防止重复初始化相同模式
   */
  currentMode = camera
    ? 'camera'
    : 'normal'

  console.log(
    '当前播放模式:',
    currentMode,
  )

  /**
   * ========================================================
   * 摄像头
   * ========================================================
   */
  if (camera) {
    await initCamera()

    return
  }

  /**
   * ========================================================
   * 普通视频
   * ========================================================
   */

  /**
   * 如果之前是摄像头
   * 关闭 Reader
   */
  closeCamera()

  /**
   * 初始化 Video.js
   */
  initVideoJs()
}


/**
 * ============================================================
 * 关闭当前播放器
 * ============================================================
 */
const closePlayer = () => {
  console.log(
    '关闭当前 VideoPlayer',
  )

  /**
   * 关闭 MediaMTX
   */
  closeCamera()

  /**
   * 关闭 Video.js
   */
  closeVideoJs()

  currentMode = null
}


/**
 * ============================================================
 * 重置播放器
 * ============================================================
 */
const resetPlayer = () => {
  console.log(
    '重置 VideoPlayer',
  )

  /**
   * ========================================================
   * 摄像头
   * ========================================================
   *
   * WebRTC 没有普通视频那样的时间轴。
   */
  if (
    isCameraStream(
      props.videoSrc,
    )
  ) {
    if (videoRef.value) {
      try {
        videoRef.value.pause()
      } catch (error) {
        console.error(
          '重置摄像头失败:',
          error,
        )
      }
    }

    return
  }

  /**
   * ========================================================
   * 普通视频
   * ========================================================
   *
   * 保留你原来的 Video.js 重置逻辑。
   */
  if (player) {
    console.log(
      '重置播放器-1',
    )

    player.currentTime(0)

    player.pause()

    player.hasStarted(false)

    player.trigger('reset')

    /**
     * 重置控制条
     */
    const controlBar =
      player.getChild(
        'ControlBar',
      )

    if (controlBar) {
      const playToggle =
        controlBar.getChild(
          'PlayToggle',
        )

      const progressControl =
        controlBar.getChild(
          'ProgressControl',
        )

      if (playToggle) {
        playToggle.trigger(
          'reset',
        )
      }

      if (progressControl) {
        progressControl.trigger(
          'reset',
        )
      }
    }
  }
}


/**
 * ============================================================
 * 暂停播放器
 * ============================================================
 */
const pausePlayer = () => {
  /**
   * 普通视频
   */
  if (player) {
    player.pause()

    return
  }

  /**
   * 摄像头
   */
  if (videoRef.value) {
    videoRef.value.pause()
  }
}


/**
 * ============================================================
 * 播放播放器
 * ============================================================
 */
const playPlayer = () => {
  /**
   * 普通视频
   */
  if (player) {
    player
      .play()
      .catch((error) => {
        console.log(
          'Video.js 播放失败:',
          error,
        )
      })

    return
  }

  /**
   * 摄像头
   */
  if (videoRef.value) {
    videoRef.value
      .play()
      .catch((error) => {
        console.log(
          'WebRTC 播放失败:',
          error,
        )
      })
  }
}


/**
 * ============================================================
 * 调整播放器尺寸
 * ============================================================
 */
const resizePlayer = () => {
  if (!videoRef.value) {
    return
  }

  nextTick(() => {
    if (!videoRef.value) {
      return
    }

    /**
     * ======================================================
     * 普通视频
     * ======================================================
     */
    if (player) {
      const container =
        videoRef.value.parentElement

      if (!container) {
        return
      }

      const width =
        container.clientWidth

      const height =
        container.clientHeight

      /**
       * 保留原来的 Video.js
       * 尺寸设置
       */
      player.width(width)

      player.height(height)

      console.log(
        `调整播放器尺寸: ${width} x ${height}`,
      )

      return
    }

    /**
     * ======================================================
     * 摄像头
     * ======================================================
     *
     * video 使用 CSS width/height。
     */
    const container =
      videoRef.value.parentElement

    if (!container) {
      return
    }

    const width =
      container.clientWidth

    const height =
      container.clientHeight

    console.log(
      `调整摄像头尺寸: ${width} x ${height}`,
    )
  })
}


/**
 * ============================================================
 * 组件挂载
 * ============================================================
 */
onMounted(() => {
  console.log(
    'VideoPlayer mounted',
  )

  destroyed = false

  /**
   * 默认打开播放器
   *
   * 普通视频 → Video.js
   *
   * 摄像头 → MediaMTX
   */
  initPlayer()

  /**
   * 窗口尺寸变化
   */
  window.addEventListener(
    'resize',
    resizePlayer,
  )
})


/**
 * ============================================================
 * resetOnLoad
 * ============================================================
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
 * ============================================================
 * destroyPlayer
 * ============================================================
 */
watch(
  () => props.destroyPlayer,
  (newVal) => {
    console.log(
      'destroyPlayer:',
      newVal,
    )

    if (newVal) {
      closePlayer()
    }
  },
)


/**
 * ============================================================
 * 视频地址变化
 * ============================================================
 *
 * 普通视频：
 *
 * video1.mp4
 *     ↓
 * video2.mp4
 *
 *
 * 摄像头：
 *
 * cam1
 *     ↓
 * cam2
 *
 *
 * 普通视频 ↔ 摄像头
 * 也可以正常切换。
 */
watch(
  () => props.videoSrc,
  async (newSrc, oldSrc) => {
    console.log(
      '视频地址发生变化',
    )

    console.log(
      '旧地址:',
      oldSrc,
    )

    console.log(
      '新地址:',
      newSrc,
    )

    if (
      !newSrc ||
      destroyed ||
      newSrc === oldSrc
    ) {
      return
    }

    /**
     * 先关闭当前播放器
     */
    closePlayer()

    /**
     * 等待 DOM 更新
     */
    await nextTick()

    /**
     * 再初始化
     */
    if (!destroyed) {
      await initPlayer()
    }
  },
)


/**
 * ============================================================
 * 暴露给父组件
 * ============================================================
 */
defineExpose({
  resizePlayer,

  resetPlayer,

  pausePlayer,

  playPlayer,

  /**
   * 主动销毁播放器
   */
  destroyVideoPlayer:
    closePlayer,
})


/**
 * ============================================================
 * 组件销毁
 * ============================================================
 */
onBeforeUnmount(() => {
  console.log(
    'VideoPlayer onBeforeUnmount',
  )

  /**
   * 标记销毁
   */
  destroyed = true

  /**
   * 移除 resize
   */
  window.removeEventListener(
    'resize',
    resizePlayer,
  )

  /**
   * 关闭播放器
   */
  closePlayer()

  /**
   * 最后保险清理 video
   */
  if (videoRef.value) {
    try {
      videoRef.value.pause()

      videoRef.value.srcObject = null

      videoRef.value.removeAttribute(
        'src',
      )

      videoRef.value.load()
    } catch (error) {
      console.error(
        '清理原生 video 失败:',
        error,
      )
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

/**
 * Video.js 和摄像头共用 video。
 */
.video-container> :deep(.video-js) {
  width: 100% !important;
  height: 100% !important;

  max-width: 100%;
  max-height: 100%;
}

/**
 * 摄像头 / 普通 video
 */
.video {
  width: 100%;
  height: 100%;

  display: block;

  background: #000;

  object-fit: contain;
}

/**
 * 错误提示
 */
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
