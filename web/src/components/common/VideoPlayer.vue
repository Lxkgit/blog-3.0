<template>
  <div class="video-container" :style="{ height: height }">
    <!-- =====================================================
         普通视频
         Video.js 独占这个 video
         ===================================================== -->
    <video ref="normalVideoRef" class="video-js vjs-default-skin vjs-big-play-centered normal-video" playsinline
      muted></video>

    <!-- =====================================================
         摄像头
         MediaMTX WebRTC 独占这个 video
         ===================================================== -->
    <video ref="cameraVideoRef" class="camera-video" autoplay muted playsinline></video>

    <!-- =====================================================
         播放错误
         ===================================================== -->
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
   * 是否销毁播放器
   */
  destroyPlayer: {
    type: Boolean,
    default: false,
  },
})


/**
 * ============================================================
 * DOM
 * ============================================================
 */

/**
 * 普通视频 DOM
 *
 * 只给 Video.js 使用
 */
const normalVideoRef = ref(null)

/**
 * 摄像头 DOM
 *
 * 只给 MediaMTX WebRTC 使用
 */
const cameraVideoRef = ref(null)


/**
 * ============================================================
 * 状态
 * ============================================================
 */

/**
 * Video.js Player
 *
 * 整个组件生命周期内尽量只创建一次。
 */
let player = null

/**
 * MediaMTX Reader
 */
let reader = null

/**
 * 当前模式
 *
 * normal
 * camera
 */
let currentMode = null

/**
 * 是否已经销毁
 */
let destroyed = false

/**
 * 当前 Dialog 是否打开
 *
 * 这个状态非常重要。
 *
 * Dialog 关闭后：
 *
 * videoSrc 可能完全不变。
 *
 * 因此不能只依赖 videoSrc watch。
 */
let videoOpened = true

/**
 * 播放版本
 *
 * 每次开始新的播放：
 *
 * +1
 *
 * 旧异步 Reader 回调发现版本不同：
 *
 * 直接忽略。
 */
let playGeneration = 0

/**
 * 播放错误
 */
const errorMessage = ref('')


/**
 * ============================================================
 * 判断是否是摄像头
 * ============================================================
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
 * 判断播放版本是否有效
 * ============================================================
 */
const isGenerationValid = (
  generation,
) => {
  return (
    !destroyed &&
    videoOpened &&
    generation === playGeneration
  )
}


/**
 * ============================================================
 * 获取 WHEP 地址
 *
 * /rtsp/cam1/?token=xxxx
 *
 * ↓
 *
 * /rtsp/cam1/whep?token=xxxx
 * ============================================================
 */
const getWhepUrl = (src) => {
  const url = new URL(
    src,
    window.location.origin,
  )

  if (!url.pathname.endsWith('/')) {
    url.pathname += '/'
  }

  url.pathname += 'whep'

  return url.toString()
}


/**
 * ============================================================
 * 显示 / 隐藏播放器
 * ============================================================
 */
const updateVideoVisibility = () => {
  const normalVideo =
    normalVideoRef.value

  const cameraVideo =
    cameraVideoRef.value

  if (normalVideo) {
    normalVideo.style.display =
      currentMode === 'normal'
        ? 'block'
        : 'none'
  }

  if (cameraVideo) {
    cameraVideo.style.display =
      currentMode === 'camera'
        ? 'block'
        : 'none'
  }
}


/**
 * ============================================================
 * 清理摄像头 video
 * ============================================================
 */
const clearCameraVideo = () => {
  const video =
    cameraVideoRef.value

  if (!video) {
    return
  }

  try {
    video.pause()
  } catch (error) {
    console.warn(
      '暂停摄像头 video 失败:',
      error,
    )
  }

  try {
    video.srcObject = null
  } catch (error) {
    console.warn(
      '清理摄像头 srcObject 失败:',
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
  const oldReader =
    reader

  /**
   * 立即清空引用。
   *
   * 防止旧 Reader 被误认为当前 Reader。
   */
  reader = null

  if (oldReader) {
    console.log(
      '关闭 MediaMTX Reader',
    )

    try {
      oldReader.close()
    } catch (error) {
      console.warn(
        '关闭 MediaMTX Reader 失败:',
        error,
      )
    }
  }

  /**
   * 清理 video
   */
  clearCameraVideo()
}


/**
 * ============================================================
 * 初始化 Video.js
 * ============================================================
 */
const initVideoJs = async () => {
  if (
    destroyed ||
    !normalVideoRef.value
  ) {
    return
  }

  /**
   * 已经初始化：
   *
   * 不重复创建。
   */
  if (player) {
    return
  }

  console.log(
    '初始化 Video.js',
  )

  try {
    player = videojs(
      normalVideoRef.value,
      {
        controls: true,

        preload: 'auto',

        autoplay: true,

        muted: true,

        playsinline: true,

        fluid: false,

        fill: false,

        responsive: false,
      },
    )

    /**
     * Video.js 错误监听
     */
    player.on(
      'error',
      () => {
        if (
          destroyed ||
          currentMode !== 'normal' ||
          !videoOpened
        ) {
          return
        }

        const error =
          player?.error()

        if (error) {
          errorMessage.value =
            error.message ||
            '视频播放失败'
        }
      },
    )

    console.log(
      'Video.js 初始化成功',
    )
  } catch (error) {
    console.error(
      '初始化 Video.js 失败:',
      error,
    )

    player = null

    errorMessage.value =
      error instanceof Error
        ? error.message
        : String(error)
  }
}


/**
 * ============================================================
 * 播放普通视频
 * ============================================================
 */
const playNormalVideo = async (
  src,
  generation,
) => {
  if (
    destroyed ||
    !src ||
    !isGenerationValid(generation)
  ) {
    return
  }

  currentMode = 'normal'

  updateVideoVisibility()

  /**
   * 关闭摄像头
   */
  closeCamera()

  /**
   * 等待 DOM
   */
  await nextTick()

  if (
    !isGenerationValid(generation)
  ) {
    return
  }

  /**
   * 初始化 Video.js
   */
  await initVideoJs()

  if (
    destroyed ||
    !player ||
    !isGenerationValid(generation)
  ) {
    return
  }

  console.log(
    '播放普通视频:',
    src,
  )

  errorMessage.value = ''

  try {
    /**
     * 先暂停旧视频
     */
    player.pause()

    /**
     * 设置新 source
     */
    player.src({
      type: 'video/mp4',
      src,
    })

    await nextTick()

    if (
      !isGenerationValid(generation) ||
      !player
    ) {
      return
    }

    /**
     * 播放
     */
    const playPromise =
      player.play()

    if (playPromise) {
      await playPromise
    }

    if (
      !isGenerationValid(generation)
    ) {
      return
    }

    console.log(
      '普通视频播放成功:',
      src,
    )

    errorMessage.value = ''

    resizePlayer()
  } catch (error) {
    if (
      !isGenerationValid(generation)
    ) {
      return
    }

    /**
     * 自动播放被浏览器阻止
     */
    if (
      error?.name ===
      'NotAllowedError'
    ) {
      console.warn(
        '浏览器阻止自动播放:',
        error,
      )

      return
    }

    console.error(
      '普通视频播放失败:',
      error,
    )

    errorMessage.value =
      error instanceof Error
        ? error.message
        : String(error)
  }
}


/**
 * ============================================================
 * 播放 MediaMTX 摄像头
 * ============================================================
 */
const playCamera = (
  src,
  generation,
) => {
  if (
    destroyed ||
    !src ||
    !isGenerationValid(generation)
  ) {
    return
  }

  currentMode = 'camera'

  updateVideoVisibility()

  /**
   * 关闭旧 Reader
   */
  closeCamera()

  /**
   * 获取 WHEP 地址
   */
  const whepUrl =
    getWhepUrl(src)

  console.log(
    '================================',
  )

  console.log(
    '开始连接 MediaMTX',
  )

  console.log(
    '播放版本:',
    generation,
  )

  console.log(
    '视频地址:',
    src,
  )

  console.log(
    'WHEP 地址:',
    whepUrl,
  )

  console.log(
    '================================',
  )

  let currentReader = null

  try {
    currentReader =
      new window.MediaMTXWebRTCReader({
        url: whepUrl,

        /**
         * ==================================================
         * Error
         * ==================================================
         */
        onError: (error) => {
          if (
            !isGenerationValid(
              generation,
            )
          ) {
            console.log(
              '忽略旧 Reader 错误:',
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
         * ==================================================
         * Track
         * ==================================================
         */
        onTrack: (event) => {
          if (
            !isGenerationValid(
              generation,
            )
          ) {
            console.log(
              '忽略旧 Reader Track:',
              generation,
            )

            return
          }

          const video =
            cameraVideoRef.value

          if (!video) {
            return
          }

          if (
            !event.streams ||
            event.streams.length === 0
          ) {
            console.warn(
              'MediaMTX Track 没有 MediaStream',
            )

            return
          }

          const stream =
            event.streams[0]

          if (
            !isGenerationValid(
              generation,
            )
          ) {
            return
          }

          console.log(
            '收到当前 MediaMTX Track',
          )

          /**
           * 设置 MediaStream
           */
          video.srcObject = stream

          /**
           * 自动播放
           */
          video
            .play()
            .then(() => {
              if (
                !isGenerationValid(
                  generation,
                )
              ) {
                return
              }

              console.log(
                '================================',
              )

              console.log(
                '摄像头视频播放成功',
              )

              console.log(
                '播放版本:',
                generation,
              )

              console.log(
                '视频地址:',
                src,
              )

              console.log(
                '================================',
              )

              errorMessage.value = ''

              resizePlayer()
            })
            .catch((error) => {
              if (
                !isGenerationValid(
                  generation,
                )
              ) {
                return
              }

              console.warn(
                'WebRTC 自动播放失败:',
                error,
              )
            })
        },

        /**
         * ==================================================
         * DataChannel
         * ==================================================
         */
        onDataChannel: (event) => {
          if (
            !isGenerationValid(
              generation,
            )
          ) {
            return
          }

          if (!event.channel) {
            return
          }

          console.log(
            '收到 MediaMTX DataChannel',
          )

          const channel =
            event.channel

          channel.binaryType =
            'arraybuffer'

          channel.onmessage = (
            message,
          ) => {
            if (
              !isGenerationValid(
                generation,
              )
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
     * Reader 创建之后再次检查。
     *
     * 防止：
     *
     * 创建 Reader 的过程中
     * 用户已经关闭 Dialog / 切换视频。
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
        console.warn(
          '关闭过期 Reader 失败:',
          error,
        )
      }

      return
    }

    /**
     * 设置当前 Reader
     */
    reader =
      currentReader
  } catch (error) {
    console.error(
      '初始化 MediaMTX Reader 失败:',
      error,
    )

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

    if (currentReader) {
      try {
        currentReader.close()
      } catch (closeError) {
        console.warn(
          '关闭 MediaMTX Reader 失败:',
          closeError,
        )
      }
    }

    if (
      reader === currentReader
    ) {
      reader = null
    }

    clearCameraVideo()
  }
}


/**
 * ============================================================
 * 初始化 / 播放指定视频
 * ============================================================
 */
const initPlayer = async (
  src = props.videoSrc,
) => {
  if (
    destroyed ||
    !src ||
    !videoOpened
  ) {
    return
  }

  /**
   * 创建新的播放版本
   */
  const generation =
    ++playGeneration

  /**
   * 判断类型
   */
  const camera =
    isCameraStream(src)

  currentMode =
    camera
      ? 'camera'
      : 'normal'

  console.log(
    '================================',
  )

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
    src,
  )

  console.log(
    '================================',
  )

  /**
   * ========================================================
   * 摄像头
   * ========================================================
   */
  if (camera) {
    /**
     * 暂停普通视频
     */
    if (player) {
      try {
        player.pause()
      } catch (error) {
        console.warn(
          '暂停普通视频失败:',
          error,
        )
      }
    }

    /**
     * 关闭旧 Reader
     */
    closeCamera()

    /**
     * 更新显示
     */
    updateVideoVisibility()

    /**
     * 创建摄像头 Reader
     */
    playCamera(
      src,
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
   * 关闭摄像头
   */
  closeCamera()

  /**
   * 更新显示
   */
  updateVideoVisibility()

  /**
   * 播放普通视频
   */
  await playNormalVideo(
    src,
    generation,
  )
}


/**
 * ============================================================
 * Dialog 打开
 * ============================================================
 *
 * 重点：
 *
 * Dialog 再次打开时：
 *
 * videoSrc 可能没有变化。
 *
 * 所以必须主动重新初始化播放。
 */
const openVideoPlayer = async () => {
  if (destroyed) {
    return
  }

  console.log(
    '打开 VideoPlayer',
  )

  videoOpened = true

  /**
   * 清除错误
   */
  errorMessage.value = ''

  /**
   * 等待 Dialog DOM 尺寸稳定
   */
  await nextTick()

  if (
    destroyed ||
    !videoOpened ||
    !props.videoSrc
  ) {
    return
  }

  /**
   * 重新播放当前视频
   */
  await initPlayer(
    props.videoSrc,
  )

  /**
   * 调整尺寸
   */
  resizePlayer()
}


/**
 * ============================================================
 * Dialog 关闭
 * ============================================================
 *
 * 这里是这次修改最重要的地方。
 *
 * 不销毁 Video.js。
 *
 * 只是：
 *
 * 普通视频：
 *     pause
 *
 * 摄像头：
 *     close Reader
 *
 * 同时让旧异步操作全部失效。
 * ============================================================
 */
const closePlayer = () => {
  console.log(
    '关闭当前 VideoPlayer',
  )

  /**
   * 标记 Dialog 已关闭
   */
  videoOpened = false

  /**
   * 让所有异步播放失效
   */
  playGeneration++

  /**
   * 清除错误
   */
  errorMessage.value = ''

  /**
   * 关闭摄像头
   */
  closeCamera()

  /**
   * 暂停普通视频
   *
   * 注意：
   *
   * 这里不 dispose。
   */
  if (player) {
    try {
      player.pause()
    } catch (error) {
      console.warn(
        '暂停 Video.js 失败:',
        error,
      )
    }
  }

  /**
   * 清理普通 video 的播放状态。
   *
   * 不调用 dispose。
   */
  if (normalVideoRef.value) {
    try {
      normalVideoRef.value.pause()
    } catch (error) {
      console.warn(
        '暂停普通 video 失败:',
        error,
      )
    }
  }

  /**
   * 模式清空
   */
  currentMode = null

  /**
   * 隐藏两个 video
   */
  updateVideoVisibility()
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
  if (
    currentMode === 'normal' &&
    player
  ) {
    try {
      player.pause()
    } catch (error) {
      console.warn(
        '暂停普通视频失败:',
        error,
      )
    }

    return
  }

  /**
   * 摄像头
   */
  if (
    currentMode === 'camera' &&
    cameraVideoRef.value
  ) {
    try {
      cameraVideoRef.value.pause()
    } catch (error) {
      console.warn(
        '暂停摄像头失败:',
        error,
      )
    }
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
  if (
    currentMode === 'normal' &&
    player
  ) {
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
  if (
    currentMode === 'camera' &&
    cameraVideoRef.value
  ) {
    cameraVideoRef.value
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
 * 重置播放器
 * ============================================================
 */
const resetPlayer = () => {
  console.log(
    '重置 VideoPlayer',
  )

  /**
   * Dialog 已关闭：
   *
   * 不进行任何播放操作。
   */
  if (!videoOpened) {
    return
  }

  /**
   * 摄像头
   */
  if (
    isCameraStream(
      props.videoSrc,
    )
  ) {
    const video =
      cameraVideoRef.value

    if (video) {
      try {
        video.pause()
      } catch (error) {
        console.warn(
          '重置摄像头失败:',
          error,
        )
      }
    }

    return
  }

  /**
   * 普通视频
   */
  if (!player) {
    return
  }

  console.log(
    '重置普通视频',
  )

  try {
    player.currentTime(0)
  } catch (error) {
    console.warn(
      '设置视频时间失败:',
      error,
    )
  }

  try {
    player.pause()
  } catch (error) {
    console.warn(
      '暂停视频失败:',
      error,
    )
  }

  try {
    player.hasStarted(false)
  } catch (error) {
    console.warn(
      '重置 hasStarted 失败:',
      error,
    )
  }

  try {
    player.trigger('reset')
  } catch (error) {
    console.warn(
      '触发 reset 失败:',
      error,
    )
  }
}


/**
 * ============================================================
 * 调整播放器尺寸
 * ============================================================
 */
const resizePlayer = () => {
  nextTick(() => {
    if (destroyed) {
      return
    }

    /**
     * ======================================================
     * Video.js
     * ======================================================
     */
    if (
      currentMode === 'normal' &&
      player &&
      normalVideoRef.value
    ) {
      const container =
        normalVideoRef.value
          .parentElement

      if (!container) {
        return
      }

      const width =
        container.clientWidth

      const height =
        container.clientHeight

      if (
        width <= 0 ||
        height <= 0
      ) {
        return
      }

      try {
        player.width(width)
        player.height(height)
      } catch (error) {
        console.warn(
          '调整 Video.js 尺寸失败:',
          error,
        )
      }

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
     * 原生 video 使用 CSS。
     */
    if (
      currentMode === 'camera' &&
      cameraVideoRef.value
    ) {
      const container =
        cameraVideoRef.value
          .parentElement

      if (!container) {
        return
      }

      console.log(
        `调整 WebRTC 尺寸: ${container.clientWidth} x ${container.clientHeight}`,
      )
    }
  })
}


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
    console.log(
      '================================',
    )

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

    console.log(
      'Dialog 是否打开:',
      videoOpened,
    )

    console.log(
      '================================',
    )

    /**
     * Dialog 已关闭：
     *
     * 只记录新地址。
     *
     * 不启动播放。
     */
    if (
      destroyed ||
      !newSrc ||
      newSrc === oldSrc ||
      !videoOpened
    ) {
      return
    }

    /**
     * 让旧播放失效
     */
    const generation =
      ++playGeneration

    /**
     * 清除旧错误
     */
    errorMessage.value = ''

    /**
     * 判断类型
     */
    const camera =
      isCameraStream(newSrc)

    /**
     * ========================================================
     * 摄像头
     * ========================================================
     */
    if (camera) {
      currentMode = 'camera'

      /**
       * 暂停普通视频
       */
      if (player) {
        try {
          player.pause()
        } catch (error) {
          console.warn(
            '暂停普通视频失败:',
            error,
          )
        }
      }

      /**
       * 关闭旧摄像头
       */
      closeCamera()

      /**
       * 更新显示
       */
      updateVideoVisibility()

      /**
       * 创建新 Reader
       */
      playCamera(
        newSrc,
        generation,
      )

      return
    }

    /**
     * ========================================================
     * 普通视频
     * ========================================================
     */
    currentMode = 'normal'

    /**
     * 关闭旧摄像头
     */
    closeCamera()

    /**
     * 更新显示
     */
    updateVideoVisibility()

    /**
     * 播放普通视频
     */
    await playNormalVideo(
      newSrc,
      generation,
    )
  },
)


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
      destroyPlayerInstance()
    }
  },
)


/**
 * ============================================================
 * 完全销毁 Video.js
 * ============================================================
 *
 * 只有组件真正销毁，
 * 或者父组件明确要求 destroyPlayer，
 * 才调用。
 * ============================================================
 */
const closeVideoJs = () => {
  const oldPlayer =
    player

  player = null

  if (!oldPlayer) {
    return
  }

  console.log(
    '销毁 Video.js',
  )

  try {
    oldPlayer.pause()
  } catch (error) {
    console.warn(
      '暂停 Video.js 失败:',
      error,
    )
  }

  try {
    oldPlayer.dispose()
  } catch (error) {
    console.warn(
      '销毁 Video.js 失败:',
      error,
    )
  }
}


/**
 * ============================================================
 * 完全销毁播放器
 * ============================================================
 */
const destroyPlayerInstance = () => {
  console.log(
    '完全销毁 VideoPlayer',
  )

  /**
   * 标记 Dialog 不再播放
   */
  videoOpened = false

  /**
   * 让所有异步操作失效
   */
  playGeneration++

  /**
   * 关闭 Reader
   */
  closeCamera()

  /**
   * 销毁 Video.js
   */
  closeVideoJs()

  /**
   * 清理摄像头 video
   */
  clearCameraVideo()

  /**
   * 清理错误
   */
  errorMessage.value = ''

  /**
   * 当前模式
   */
  currentMode = null

  /**
   * 更新显示
   */
  updateVideoVisibility()
}


/**
 * ============================================================
 * 暴露给父组件
 * ============================================================
 */
defineExpose({
  /**
   * Dialog 打开时调用
   *
   * 重新建立播放。
   */
  openVideoPlayer,

  /**
   * Dialog 关闭时调用
   *
   * 暂停普通视频，
   * 关闭摄像头 Reader。
   *
   * 不销毁 Video.js。
   */
  closeVideoPlayer: closePlayer,

  /**
   * 普通的暂停
   */
  pausePlayer,

  /**
   * 普通的播放
   */
  playPlayer,

  /**
   * 重置
   */
  resetPlayer,

  /**
   * 调整尺寸
   */
  resizePlayer,

  /**
   * 真正销毁播放器
   */
  destroyVideoPlayer:
    destroyPlayerInstance,
})


/**
 * ============================================================
 * mounted
 * ============================================================
 */
onMounted(async () => {
  console.log(
    'VideoPlayer mounted',
  )

  destroyed = false

  /**
   * 初始状态认为 Dialog 是打开状态。
   *
   * 如果这个组件只在 Dialog 打开时才创建，
   * 这里可以直接播放。
   */
  videoOpened = true

  await nextTick()

  if (
    props.videoSrc
  ) {
    await initPlayer(
      props.videoSrc,
    )
  }

  /**
   * 监听窗口尺寸
   */
  window.addEventListener(
    'resize',
    resizePlayer,
  )
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
   * 让所有异步操作失效
   */
  playGeneration++

  /**
   * Dialog 状态
   */
  videoOpened = false

  /**
   * 移除 resize
   */
  window.removeEventListener(
    'resize',
    resizePlayer,
  )

  /**
   * 关闭 Reader
   */
  closeCamera()

  /**
   * 销毁 Video.js
   */
  closeVideoJs()

  /**
   * 清理摄像头 video
   */
  clearCameraVideo()
})
</script>

<style scoped>
/**
 * ============================================================
 * 外层
 * ============================================================
 */
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
 * 普通视频
 * ============================================================
 */
.normal-video {
  width: 100%;

  height: 100%;

  display: none;

  background: #000;

  object-fit: contain;
}


/**
 * ============================================================
 * 摄像头视频
 * ============================================================
 */
.camera-video {
  width: 100%;

  height: 100%;

  display: none;

  background: #000;

  object-fit: contain;
}


/**
 * ============================================================
 * 错误
 * ============================================================
 */
.video-error {
  position: absolute;

  left: 50%;

  top: 50%;

  transform: translate(-50%,
      -50%);

  max-width: 80%;

  padding: 12px 20px;

  box-sizing: border-box;

  color: #fff;

  font-size: 14px;

  line-height: 1.5;

  text-align: center;

  background: rgba(0,
      0,
      0,
      0.65);

  border-radius: 4px;

  word-break: break-word;

  z-index: 10;
}
</style>
