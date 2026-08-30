<template>
  <div class="video-container" :style="{ height: height }">
    <!--
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
  onBeforeUnmount,
  watch,
  nextTick,
  defineExpose,
} from 'vue'

import videojs from 'video.js'
import 'video.js/dist/video-js.css'

// MediaMTX 官方 Reader
import '@/js/reader.js'


/**
 * ============================================================
 * Props
 * ============================================================
 */
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


/**
 * ============================================================
 * 状态
 * ============================================================
 */

/**
 * 原生 video
 */
const videoRef = ref(null)

/**
 * 播放错误
 */
const errorMessage = ref('')


/**
 * ============================================================
 * Video.js
 * ============================================================
 *
 * 普通视频使用
 */
let player = null


/**
 * ============================================================
 * MediaMTX Reader
 * ============================================================
 *
 * 摄像头使用
 */
let reader = null


/**
 * ============================================================
 * 当前播放模式
 * ============================================================
 *
 * normal
 * camera
 */
let currentMode = null


/**
 * ============================================================
 * 组件销毁状态
 * ============================================================
 */
let destroyed = false


/**
 * ============================================================
 * 播放代次
 * ============================================================
 *
 * 这是整个组件切换视频最关键的变量。
 *
 * 每一次视频切换：
 *
 *   playGeneration++
 *
 * 旧 Reader 保存自己的 generation。
 *
 * 当旧 Reader 后面异步触发 onTrack / onError 等回调时，
 * 如果发现：
 *
 *   generation !== playGeneration
 *
 * 就说明自己已经过期。
 *
 * 直接忽略。
 *
 * 防止：
 *
 *   cam1 Reader
 *       ↓
 *   切换 cam2
 *       ↓
 *   cam1 延迟 onTrack
 *       ↓
 *   抢走 video.srcObject
 */
let playGeneration = 0


/**
 * ============================================================
 * 判断视频是否仍然有效
 * ============================================================
 */
const isGenerationValid = (generation) => {
  return (
    !destroyed &&
    generation === playGeneration
  )
}


/**
 * ============================================================
 * 判断是否为摄像头
 * ============================================================
 *
 * 摄像头统一：
 *
 * /rtsp/cam1/
 *
 * 所以只判断 pathname。
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
 * 输入：
 *
 * /rtsp/cam1/?token=xxxx
 *
 * 输出：
 *
 * /rtsp/cam1/whep?token=xxxx
 */
const getWhepUrl = (src) => {
  const url = new URL(
    src,
    window.location.origin,
  )

  /**
   * 确保最后存在 /
   */
  if (!url.pathname.endsWith('/')) {
    url.pathname += '/'
  }

  /**
   * 添加 whep
   */
  url.pathname += 'whep'

  return url.toString()
}


/**
 * ============================================================
 * 清理原生 video
 * ============================================================
 *
 * 注意：
 *
 * 这里不调用 video.load()
 *
 * WebRTC 切换过程中频繁 load() 没有必要，
 * 还容易让浏览器媒体状态产生额外变化。
 */
const clearVideoElement = () => {
  const video = videoRef.value

  if (!video) {
    return
  }

  try {
    /**
     * 停止播放
     */
    video.pause()
  } catch (error) {
    console.error(
      '暂停 video 失败:',
      error,
    )
  }

  try {
    /**
     * 清除 MediaStream
     */
    video.srcObject = null
  } catch (error) {
    console.error(
      '清理 video.srcObject 失败:',
      error,
    )
  }

  try {
    /**
     * 清理普通 video src
     */
    video.removeAttribute('src')
  } catch (error) {
    console.error(
      '清理 video.src 失败:',
      error,
    )
  }
}


/**
 * ============================================================
 * 关闭 MediaMTX Reader
 * ============================================================
 */
const closeCamera = () => {
  /**
   * 保存旧 Reader
   *
   * 不直接操作全局变量，
   * 避免旧回调和新 Reader 产生引用混乱。
   */
  const oldReader = reader

  /**
   * 立即清空当前 Reader
   */
  reader = null

  if (oldReader) {
    console.log(
      '关闭 MediaMTX Reader',
    )

    try {
      oldReader.close()
    } catch (error) {
      console.error(
        '关闭 MediaMTX Reader 失败:',
        error,
      )
    }
  }

  /**
   * 清理 video
   */
  clearVideoElement()
}


/**
 * ============================================================
 * 关闭 Video.js
 * ============================================================
 */
const closeVideoJs = () => {
  const oldPlayer = player

  /**
   * 先清空引用
   */
  player = null

  if (!oldPlayer) {
    return
  }

  console.log(
    '关闭 Video.js',
  )

  try {
    /**
     * 暂停
     */
    oldPlayer.pause()
  } catch (error) {
    console.error(
      '暂停 Video.js 失败:',
      error,
    )
  }

  try {
    /**
     * 清空 source
     */
    oldPlayer.src({
      type: 'video/mp4',
      src: '',
    })
  } catch (error) {
    console.error(
      '清空 Video.js source 失败:',
      error,
    )
  }

  try {
    /**
     * 销毁
     */
    oldPlayer.dispose()
  } catch (error) {
    console.error(
      '销毁 Video.js 失败:',
      error,
    )
  }
}


/**
 * ============================================================
 * 关闭当前播放器
 * ============================================================
 *
 * 这个函数用于：
 *
 * 1. 切换视频
 * 2. 主动销毁
 * 3. 组件销毁
 *
 * 关键：
 *
 * 每次关闭当前播放器，
 * 都让旧异步连接全部失效。
 */
const closePlayer = () => {
  console.log(
    '关闭当前 VideoPlayer',
  )

  /**
   * 让当前所有旧 Reader 失效
   */
  playGeneration++

  /**
   * 清除错误
   */
  errorMessage.value = ''

  /**
   * 关闭 WebRTC
   */
  closeCamera()

  /**
   * 关闭 Video.js
   */
  closeVideoJs()

  /**
   * 清除模式
   */
  currentMode = null
}


/**
 * ============================================================
 * 初始化 Video.js
 * ============================================================
 */
const initVideoJs = (generation) => {
  /**
   * 初始化前检查
   */
  if (
    destroyed ||
    !videoRef.value ||
    !props.videoSrc ||
    !isGenerationValid(generation)
  ) {
    return
  }

  console.log(
    '初始化 Video.js',
  )

  /**
   * 如果存在旧播放器，
   * 保险关闭。
   */
  if (player) {
    closeVideoJs()
  }

  /**
   * 创建 Video.js
   */
  const newPlayer = videojs(
    videoRef.value,
    {
      controls: true,

      preload: 'auto',

      autoplay: true,

      muted: true,

      fluid: false,

      fill: false,

      responsive: false,
    },
  )

  /**
   * 创建过程中如果发生了切换，
   * 当前播放器立即作废。
   */
  if (
    !isGenerationValid(generation)
  ) {
    try {
      newPlayer.dispose()
    } catch (error) {
      console.error(
        '销毁过期 Video.js 失败:',
        error,
      )
    }

    return
  }

  /**
   * 保存播放器
   */
  player = newPlayer

  /**
   * 设置 source
   */
  player.src({
    type: 'video/mp4',
    src: props.videoSrc,
  })

  /**
   * 调整尺寸
   */
  resizePlayer()
}


/**
 * ============================================================
 * 初始化 MediaMTX
 * ============================================================
 */
const initCamera = (generation) => {
  /**
   * 初始化前检查
   */
  if (
    destroyed ||
    !videoRef.value ||
    !props.videoSrc ||
    !isGenerationValid(generation)
  ) {
    return
  }

  /**
   * 保存当前地址
   *
   * 后续回调全部使用这个地址。
   *
   * 防止 props.videoSrc 后续变化后，
   * 日志显示错误。
   */
  const source = props.videoSrc

  /**
   * 获取 WHEP
   */
  const whepUrl =
    getWhepUrl(source)

  console.log('================================')
  console.log(
    '开始连接 MediaMTX',
  )
  console.log(
    '连接版本:',
    generation,
  )
  console.log(
    '视频地址:',
    source,
  )
  console.log(
    'WHEP 地址:',
    whepUrl,
  )
  console.log('================================')

  /**
   * 当前 Reader
   *
   * 注意：
   *
   * 这里必须单独保存。
   *
   * 因为 Reader 的异步回调里面，
   * 需要判断：
   *
   * reader === currentReader
   *
   * 防止旧 Reader 操作新视频。
   */
  let currentReader = null

  try {
    currentReader =
      new window.MediaMTXWebRTCReader({
        url: whepUrl,

        /**
         * ======================================================
         * Reader 错误
         * ======================================================
         */
        onError: (error) => {
          /**
           * 旧 Reader：
           *
           * 直接忽略。
           */
          if (
            !isGenerationValid(
              generation,
            ) ||
            reader !== currentReader
          ) {
            console.log(
              '忽略旧 MediaMTX Reader 错误:',
              generation,
            )

            return
          }

          console.error(
            'MediaMTX WebRTC 播放错误:',
            error,
          )

          errorMessage.value =
            error instanceof Error
              ? error.message
              : String(error)
        },

        /**
         * ======================================================
         * 收到 WebRTC Track
         * ======================================================
         */
        onTrack: (event) => {
          /**
           * 重点：
           *
           * 必须同时满足：
           *
           * 1. 组件没有销毁
           * 2. generation 是当前版本
           * 3. reader 还是当前 Reader
           */
          if (
            !isGenerationValid(
              generation,
            ) ||
            reader !== currentReader
          ) {
            console.log(
              '忽略旧 MediaMTX Track:',
              generation,
            )

            return
          }

          /**
           * 检查 video
           */
          const video =
            videoRef.value

          if (!video) {
            return
          }

          /**
           * 检查 MediaStream
           */
          if (
            !event.streams ||
            event.streams.length === 0
          ) {
            console.warn(
              'MediaMTX Track 没有 MediaStream',
            )

            return
          }

          /**
           * 当前 MediaStream
           */
          const stream =
            event.streams[0]

          /**
           * 设置之前再检查一次。
           */
          if (
            !isGenerationValid(
              generation,
            ) ||
            reader !== currentReader
          ) {
            return
          }

          console.log(
            '收到当前 MediaMTX WebRTC Track',
          )

          /**
           * 设置 MediaStream
           */
          video.srcObject =
            stream

          /**
           * 自动播放
           */
          video
            .play()
            .then(() => {
              /**
               * play() 是异步的。
               *
               * 所以这里还必须检查一次。
               */
              if (
                !isGenerationValid(
                  generation,
                ) ||
                reader !== currentReader
              ) {
                return
              }

              console.log('================================')
              console.log(
                '摄像头视频播放成功',
              )
              console.log(
                '连接版本:',
                generation,
              )
              console.log(
                '视频地址:',
                source,
              )
              console.log('================================')

              /**
               * 清除旧错误
               */
              errorMessage.value = ''

              /**
               * 调整尺寸
               */
              resizePlayer()
            })
            .catch((error) => {
              /**
               * 已经切换视频，
               * 不处理旧播放错误。
               */
              if (
                !isGenerationValid(
                  generation,
                )
              ) {
                return
              }

              console.warn(
                '浏览器自动播放失败:',
                error,
              )
            })
        },

        /**
         * ======================================================
         * DataChannel
         * ======================================================
         */
        onDataChannel: (event) => {
          /**
           * 旧 Reader 直接忽略
           */
          if (
            !isGenerationValid(
              generation,
            ) ||
            reader !== currentReader
          ) {
            return
          }

          console.log(
            '收到 MediaMTX DataChannel',
          )

          if (!event.channel) {
            return
          }

          const channel =
            event.channel

          channel.binaryType =
            'arraybuffer'

          /**
           * 防止旧 DataChannel
           * 后续继续工作。
           */
          channel.onmessage = (
            message,
          ) => {
            if (
              !isGenerationValid(
                generation,
              ) ||
              reader !== currentReader
            ) {
              return
            }

            console.log(
              'MediaMTX DataChannel:',
              message.data,
            )
          }
        },
      })

    /**
     * ========================================================
     * Reader 创建之后再次检查
     * ========================================================
     *
     * 防止：
     *
     * 创建 cam1 Reader
     * ↓
     * 用户马上切换 cam2
     * ↓
     * cam1 已经失效
     * ↓
     * Reader 创建完成
     */
    if (
      !isGenerationValid(
        generation,
      )
    ) {
      console.log(
        'Reader 创建完成但已经过期:',
        generation,
      )

      try {
        currentReader.close()
      } catch (error) {
        console.error(
          '关闭过期 Reader 失败:',
          error,
        )
      }

      return
    }

    /**
     * 当前 Reader
     */
    reader = currentReader

  } catch (error) {
    console.error(
      '初始化 MediaMTX 播放器失败:',
      error,
    )

    /**
     * 只有当前连接才能显示错误。
     */
    if (
      isGenerationValid(
        generation,
      )
    ) {
      errorMessage.value =
        error instanceof Error
          ? error.message
          : String(error)
    }

    /**
     * 清理当前 Reader
     */
    if (currentReader) {
      try {
        currentReader.close()
      } catch (closeError) {
        console.error(
          '关闭 MediaMTX Reader 失败:',
          closeError,
        )
      }
    }

    /**
     * 只有当前 Reader
     * 才允许清空全局 reader。
     */
    if (
      reader === currentReader
    ) {
      reader = null
    }

    /**
     * 清理 video
     */
    clearVideoElement()
  }
}


/**
 * ============================================================
 * 初始化播放器
 * ============================================================
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
   * ========================================================
   * 创建新的播放版本
   * ========================================================
   *
   * 注意：
   *
   * 这里不能复用旧 generation。
   */
  const generation =
    ++playGeneration

  /**
   * 当前是否摄像头
   */
  const camera =
    isCameraStream(
      props.videoSrc,
    )

  /**
   * 保存当前模式
   */
  currentMode = camera
    ? 'camera'
    : 'normal'

  console.log('================================')
  console.log(
    '初始化播放器',
  )
  console.log(
    '播放版本:',
    generation,
  )
  console.log(
    '播放模式:',
    currentMode,
  )
  console.log(
    '视频地址:',
    props.videoSrc,
  )
  console.log('================================')

  /**
   * ========================================================
   * 摄像头
   * ========================================================
   */
  if (camera) {
    /**
     * 普通视频关闭
     */
    closeVideoJs()

    /**
     * 关闭旧 Reader
     */
    closeCamera()

    /**
     * 再检查一次。
     */
    if (
      !isGenerationValid(
        generation,
      )
    ) {
      return
    }

    /**
     * 创建 WebRTC
     */
    initCamera(
      generation,
    )

    return
  }

  /**
   * ========================================================
   * 普通视频
   * ========================================================
   */

  /**
   * 关闭 WebRTC
   */
  closeCamera()

  /**
   * 等待 DOM
   */
  await nextTick()

  /**
   * 如果等待期间切换了视频，
   * 当前初始化取消。
   */
  if (
    !isGenerationValid(
      generation,
    )
  ) {
    return
  }

  /**
   * 初始化 Video.js
   */
  initVideoJs(
    generation,
  )
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
   * WebRTC 没有普通视频时间轴。
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
   */
  if (player) {
    console.log(
      '重置普通视频播放器',
    )

    /**
     * 回到开头
     */
    player.currentTime(0)

    /**
     * 暂停
     */
    player.pause()

    /**
     * 重置开始状态
     */
    player.hasStarted(false)

    /**
     * 触发 reset
     */
    player.trigger('reset')

    /**
     * 控制栏
     */
    const controlBar =
      player.getChild(
        'ControlBar',
      )

    if (controlBar) {
      /**
       * 播放按钮
       */
      const playToggle =
        controlBar.getChild(
          'PlayToggle',
        )

      /**
       * 进度条
       */
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
        console.warn(
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
        console.warn(
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
     * Video.js
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

      player.width(width)

      player.height(height)

      console.log(
        `调整 Video.js 尺寸: ${width} x ${height}`,
      )

      return
    }

    /**
     * ======================================================
     * MediaMTX
     * ======================================================
     *
     * 原生 video 通过 CSS 控制。
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
      `调整 WebRTC 尺寸: ${width} x ${height}`,
    )
  })
}


/**
 * ============================================================
 * mounted
 * ============================================================
 */
onMounted(() => {
  console.log(
    'VideoPlayer mounted',
  )

  destroyed = false

  /**
   * 初始化
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
  (newValue) => {
    if (newValue) {
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
  (newValue) => {
    console.log(
      'destroyPlayer:',
      newValue,
    )

    if (newValue) {
      closePlayer()
    }
  },
)


/**
 * ============================================================
 * 视频地址变化
 * ============================================================
 */
watch(
  () => props.videoSrc,
  async (
    newSrc,
    oldSrc,
  ) => {
    console.log('================================')
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
    console.log('================================')

    /**
     * 没有地址
     */
    if (
      destroyed ||
      !newSrc
    ) {
      return
    }

    /**
     * 地址没有变化
     */
    if (
      newSrc === oldSrc
    ) {
      return
    }

    /**
     * ========================================================
     * 关闭旧播放器
     * ========================================================
     *
     * closePlayer() 会：
     *
     * 1. playGeneration++
     * 2. Reader.close()
     * 3. video.srcObject = null
     * 4. Video.js.dispose()
     *
     * 最重要的是：
     *
     * 旧 Reader 的 generation
     * 从这一刻开始失效。
     */
    closePlayer()

    /**
     * 等待 Vue 更新
     */
    await nextTick()

    /**
     * 如果等待过程中组件销毁，
     * 不再初始化。
     */
    if (destroyed) {
      return
    }

    /**
     * 初始化新视频
     */
    await initPlayer()
  },
)


/**
 * ============================================================
 * 暴露给父组件
 * ============================================================
 */
defineExpose({
  /**
   * 调整尺寸
   */
  resizePlayer,

  /**
   * 重置
   */
  resetPlayer,

  /**
   * 暂停
   */
  pausePlayer,

  /**
   * 播放
   */
  playPlayer,

  /**
   * 主动销毁
   */
  destroyVideoPlayer:
    closePlayer,
})


/**
 * ============================================================
 * beforeUnmount
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
   * 让所有异步 Reader 回调失效
   */
  playGeneration++

  /**
   * 移除 resize
   */
  window.removeEventListener(
    'resize',
    resizePlayer,
  )

  /**
   * 关闭 MediaMTX
   */
  closeCamera()

  /**
   * 关闭 Video.js
   */
  closeVideoJs()

  /**
   * 最后清理 video
   */
  clearVideoElement()
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
 * ============================================================
 * Video.js
 * ============================================================
 */
.video-container> :deep(.video-js) {
  width: 100% !important;
  height: 100% !important;

  max-width: 100%;
  max-height: 100%;
}


/**
 * ============================================================
 * 原生 video
 * ============================================================
 *
 * MediaMTX WebRTC 使用。
 *
 * 注意：
 *
 * 原来的：
 *
 * .video
 *
 * 是匹配不到的，
 * 因为模板中的 video 没有 class="video"。
 */
.video-container>video {
  width: 100%;
  height: 100%;

  display: block;

  background: #000;

  object-fit: contain;
}


/**
 * ============================================================
 * 错误提示
 * ============================================================
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

  z-index: 10;
}
</style>
