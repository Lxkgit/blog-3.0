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
   * 销毁播放器
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
 * 是否销毁
 */
let destroyed = false

/**
 * 播放版本
 *
 * 每次切换视频 +1
 *
 * 旧的异步 Reader 回调如果发现版本不一致，
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
 * 判断当前播放版本是否有效
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
 *
 * 不销毁 DOM。
 *
 * 两种播放器始终各自拥有自己的 video。
 * ============================================================
 */
const updateVideoVisibility = () => {
  const normalVideo = normalVideoRef.value
  const cameraVideo = cameraVideoRef.value

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
  const video = cameraVideoRef.value

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
  /**
   * 保存旧 Reader
   */
  const oldReader = reader

  /**
   * 立即清空引用
   *
   * 非常重要。
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
   * 清理摄像头 video
   */
  clearCameraVideo()
}


/**
 * ============================================================
 * 初始化 Video.js
 * ============================================================
 *
 * 注意：
 *
 * Video.js 只初始化一次。
 *
 * 普通视频切换：
 *
 * video1.mp4
 * ↓
 * video2.mp4
 *
 * 只调用：
 *
 * player.src(...)
 *
 * 不再 dispose → init。
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
   * 如果已经存在 Player
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
     * 播放错误监听
     */
    player.on(
      'error',
      () => {
        /**
         * 如果当前不是普通视频，
         * 不显示 Video.js 错误。
         */
        if (
          destroyed ||
          currentMode !== 'normal'
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
 * 设置普通视频
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

  /**
   * 更新显示状态
   */
  updateVideoVisibility()

  /**
   * 关闭摄像头
   */
  closeCamera()

  /**
   * 等待 DOM
   */
  await nextTick()

  /**
   * 切换期间已经产生新播放版本
   */
  if (
    !isGenerationValid(generation)
  ) {
    return
  }

  /**
   * 确保 Video.js 已经初始化
   */
  await initVideoJs()

  /**
   * 再检查一次
   */
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

  /**
   * 清除错误
   */
  errorMessage.value = ''

  /**
   * ========================================================
   * 核心
   *
   * 普通视频切换只修改 Video.js source。
   *
   * 不 dispose。
   * ========================================================
   */
  try {
    player.pause()

    player.src({
      type: 'video/mp4',
      src,
    })

    /**
     * 等待 source 设置完成
     */
    await nextTick()

    if (
      !isGenerationValid(generation) ||
      !player
    ) {
      return
    }

    /**
     * 自动播放
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
    /**
     * 如果已经切换到了其它视频，
     * 当前错误属于旧视频。
     */
    if (
      !isGenerationValid(generation)
    ) {
      return
    }

    /**
     * 自动播放被浏览器阻止
     *
     * 这种情况不一定是真正的视频错误。
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
 * 初始化 MediaMTX 摄像头
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

  /**
   * 当前模式
   */
  currentMode = 'camera'

  /**
   * 显示摄像头 video
   */
  updateVideoVisibility()

  /**
   * 先关闭旧 Reader
   */
  closeCamera()

  /**
   * 获取 WHEP
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

  /**
   * ========================================================
   * 创建 Reader
   * ========================================================
   */

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
          /**
           * 旧连接直接忽略。
           */
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
          /**
           * 旧连接直接忽略。
           */
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

          /**
           * MediaStream
           */
          const stream =
            event.streams[0]

          /**
           * 再检查一次
           */
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
           * 设置 srcObject
           */
          video.srcObject = stream

          /**
           * 自动播放
           */
          video
            .play()
            .then(() => {
              /**
               * play() 是异步的。
               *
               * 所以这里必须再次检查版本。
               */
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
              /**
               * 已经切换视频，
               * 忽略旧播放错误。
               */
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
     * ========================================================
     * Reader 创建完成
     * ========================================================
     *
     * 如果创建期间用户已经切换视频，
     * 当前 Reader 立即关闭。
     * ========================================================
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
     * 当前 Reader
     */
    reader = currentReader

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

    /**
     * 清理当前 Reader
     */
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

    /**
     * 只有当前 Reader 才清理
     */
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
 * 初始化播放器
 * ============================================================
 */
const initPlayer = async (
  src = props.videoSrc,
) => {
  if (
    destroyed ||
    !src
  ) {
    return
  }

  /**
   * 每一次真正初始化都产生新的版本。
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
     * 普通视频暂停
     *
     * 注意：
     * 不 dispose Video.js。
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
     * 初始化摄像头
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
 * 关闭 Video.js
 * ============================================================
 *
 * 只在：
 *
 * 1. 组件销毁
 * 2. destroyPlayer
 *
 * 时调用。
 *
 * 普通视频切换绝对不调用。
 * ============================================================
 */
const closeVideoJs = () => {
  const oldPlayer = player

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
 * 关闭整个播放器
 * ============================================================
 */
const closePlayer = () => {
  console.log(
    '关闭当前 VideoPlayer',
  )

  /**
   * 让旧异步任务失效
   */
  playGeneration++

  /**
   * 清除错误
   */
  errorMessage.value = ''

  /**
   * 关闭 Reader
   */
  closeCamera()

  /**
   * 暂停 Video.js
   *
   * 这里不 dispose。
   *
   * 除非 destroyPlayer / unmount。
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
   * 模式清空
   */
  currentMode = null

  /**
   * 更新显示
   */
  updateVideoVisibility()
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
   * 模式
   */
  currentMode = null

  /**
   * 更新显示
   */
  updateVideoVisibility()
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
   * ========================================================
   * 普通视频
   * ========================================================
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
 * 暂停
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
 * 播放
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
 * 调整尺寸
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
     * ======================================================
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
 * mounted
 * ============================================================
 */
onMounted(async () => {
  console.log(
    'VideoPlayer mounted',
  )

  destroyed = false

  /**
   * 初始化当前视频
   */
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
 * 视频地址变化
 * ============================================================
 *
 * 这里是切换视频的核心。
 *
 * 普通：
 *
 * video1
 * ↓
 * video2
 *
 * 不销毁 Video.js。
 *
 *
 * 摄像头：
 *
 * cam1
 * ↓
 * cam2
 *
 * 关闭旧 Reader。
 *
 *
 * 普通 ↔ 摄像头：
 *
 * 使用两个完全独立的 video。
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
      '================================',
    )

    if (
      destroyed ||
      !newSrc ||
      newSrc === oldSrc
    ) {
      return
    }

    /**
     * ========================================================
     * 让旧播放立即失效
     * ========================================================
     *
     * 注意：
     *
     * 这里不要调用 closePlayer()。
     *
     * 因为 closePlayer() 会增加 generation，
     * 然后 initPlayer() 又会增加一次。
     *
     * 直接在这里生成新的播放版本即可。
     * ========================================================
     */
    const generation =
      ++playGeneration

    /**
     * 清除旧错误
     */
    errorMessage.value = ''

    /**
     * 判断新视频类型
     */
    const camera =
      isCameraStream(newSrc)

    /**
     * ========================================================
     * 新视频是摄像头
     * ========================================================
     */
    if (camera) {
      currentMode = 'camera'

      /**
       * 先暂停普通视频
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
     * 新视频是普通视频
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
 * 暴露给父组件
 * ============================================================
 */
defineExpose({
  resizePlayer,

  resetPlayer,

  pausePlayer,

  playPlayer,

  /**
   * 主动销毁
   */
  destroyVideoPlayer:
    destroyPlayerInstance,
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
 * 普通视频
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
 *
 * 默认隐藏。
 *
 * 由 JS 根据 currentMode 控制显示。
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
 *
 * 默认隐藏。
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
