<template>
  <el-dialog v-model="video.showVideoDialog" class="video-dialog" :close-on-click-modal="false" title="视频播放"
    width="80vw" top="0" :fullscreen="video.fullscreen" @opened="handleDialogOpened" @open="handleDialogOpen"
    @close="handleDialogClose">
    <div class="video-container">
      <!-- =====================================================
           左侧播放器
           ===================================================== -->
      <div class="video-player">
        <VideoPlayer ref="videoPlayerRef" class="video-player-component" :video-src="video.videoUrl"
          :reset-on-load="video.resetFlag" :destroy-player="video.destroy" />
      </div>

      <!-- =====================================================
           右侧视频列表
           ===================================================== -->
      <div class="video-list-container">
        <!-- ==================== 控制按钮 ==================== -->
        <div class="list-controls">
          <el-button type="primary" :disabled="video.videoList.length === 0" @click="handlePrevious">
            上一个
          </el-button>

          <el-button type="primary" :disabled="video.videoList.length === 0" @click="handleNext">
            下一个
          </el-button>

          <el-button type="primary" @click="handleFullscreen">
            <MyIcon v-if="video.fullscreen" title="退出全屏" type="icon-half-screen" />

            <MyIcon v-else title="网页全屏" type="icon-full-screen" />
          </el-button>
        </div>

        <!-- ==================== 视频列表 ==================== -->
        <el-scrollbar class="scroll-container">
          <ul class="video-list">
            <li v-for="(item, index) in video.videoList" :key="index" :class="[
              'video-item',
              {
                active: video.currentIndex === index,
              },
            ]" @click="handlePlayVideo(item)">
              <!-- =================================================
                   当前正在播放的视频
                   ================================================= -->
              <div v-if="index === video.currentIndex" class="video-info">
                <div class="video-title">
                  <div class="file-name" :title="item.fileName">
                    <MyIcon title="视频正在播放" type="icon-playing" class="playing-icon" />

                    {{ item.fileName }}
                  </div>

                  <!-- 文件操作 -->
                  <div class="video-action">
                    <el-dropdown>
                      <el-button type="primary" size="small">
                        移动
                      </el-button>

                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-for="(dir, rowIndex) in dirList.data" :key="rowIndex" @click.stop="
                            handleMoveFile(dir, item)
                            ">
                            {{ dir.dirName }}
                          </el-dropdown-item>

                          <el-dropdown-item @click.stop="
                            handleMoveFile(null, item)
                            ">
                            删除
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>

                <div class="video-duration">
                  <MyIcon title="视频时长" type="icon-time" />

                  {{ timeToMinOrHour(item.videoTime) }}
                </div>
              </div>

              <!-- =================================================
                   非当前视频
                   ================================================= -->
              <div v-else class="video-info">
                <div class="video-title">
                  <div class="file-name" :title="item.fileName">
                    {{ item.fileName }}
                  </div>
                </div>

                <div class="video-duration">
                  <MyIcon title="视频时长" type="icon-time" />

                  {{ timeToMinOrHour(item.videoTime) }}
                </div>
              </div>
            </li>
          </ul>
        </el-scrollbar>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import VideoPlayer from '@/components/common/VideoPlayer.vue'
import icon from '@/utils/icon'

const { MyIcon } = icon()

interface Props {
  video: any
  dirList: any
  timeToMinOrHour: (time: any) => any
}

defineProps<Props>()

/**
 * ============================================================
 * VideoPlayer 实例
 * ============================================================
 *
 * 注意：
 *
 * 这里必须使用 ref。
 *
 * 不要把 videoPlayerRef 放进 Props。
 * ============================================================
 */
const videoPlayerRef = ref<{
  resizePlayer?: () => void
  resetPlayer?: () => void
  pausePlayer?: () => void
  playPlayer?: () => void

  /**
   * Dialog 关闭
   */
  closeVideoPlayer?: () => void

  /**
   * Dialog 打开
   */
  openVideoPlayer?: () => void

  /**
   * 完全销毁
   */
  destroyVideoPlayer?: () => void
} | null>(null)

const emit = defineEmits<{
  previous: []
  next: []
  fullscreen: []
  playVideo: [item: any]
  moveFile: [dir: any, item: any]
  dialogOpened: []
  dialogOpen: []
  dialogClose: []
}>()

/**
 * ============================================================
 * 上一个视频
 * ============================================================
 */
const handlePrevious = () => {
  emit('previous')
}

/**
 * ============================================================
 * 下一个视频
 * ============================================================
 */
const handleNext = () => {
  emit('next')
}

/**
 * ============================================================
 * 全屏切换
 * ============================================================
 */
const handleFullscreen = () => {
  emit('fullscreen')
}

/**
 * ============================================================
 * 播放视频
 * ============================================================
 */
const handlePlayVideo = (item: any) => {
  emit('playVideo', item)
}

/**
 * ============================================================
 * 移动 / 删除文件
 * ============================================================
 */
const handleMoveFile = (
  dir: any,
  item: any,
) => {
  emit('moveFile', dir, item)
}

/**
 * ============================================================
 * Dialog 打开完成
 * ============================================================
 *
 * Element Plus：
 *
 * opened
 *
 * 此时 Dialog DOM 已经完成显示。
 *
 * 重新启动播放器。
 * ============================================================
 */
const handleDialogOpened = () => {
  console.log(
    '视频 Dialog opened',
  )

  /**
   * 重新打开播放器
   */
  videoPlayerRef.value?.openVideoPlayer?.()

  emit('dialogOpened')
}

/**
 * ============================================================
 * Dialog 打开
 * ============================================================
 */
const handleDialogOpen = () => {
  emit('dialogOpen')
}

/**
 * ============================================================
 * Dialog 关闭
 * ============================================================
 *
 * 这里是本次最重要的修改。
 *
 * Dialog 关闭时：
 *
 * 普通视频：
 *     pause
 *
 * 摄像头：
 *     close Reader
 *     清理 srcObject
 *
 * 但是：
 *
 * 不 dispose Video.js
 *
 * 因为 Dialog 后面还可能再次打开。
 * ============================================================
 */
const handleDialogClose = () => {
  console.log(
    '视频 Dialog close，关闭播放器',
  )

  videoPlayerRef.value?.closeVideoPlayer?.()

  emit('dialogClose')
}
</script>

<style scoped>
/* =========================================================
 * Element Plus Dialog
 * ========================================================= */

:global(.el-dialog.video-dialog) {
  width: 80vw !important;

  max-width: 95vw !important;

  margin: 5vh auto 0 !important;

  height: 90vh !important;

  max-height: 90vh !important;

  display: flex !important;

  flex-direction: column !important;

  box-sizing: border-box !important;

  overflow: hidden !important;
}

/* =========================================================
 * Dialog Header
 * ========================================================= */

:global(.el-dialog.video-dialog .el-dialog__header) {
  flex: 0 0 auto;

  box-sizing: border-box;

  margin: 0;
}

/* =========================================================
 * Dialog Body
 * ========================================================= */

:global(.el-dialog.video-dialog .el-dialog__body) {
  flex: 1 1 auto;

  min-height: 0;

  height: auto;

  box-sizing: border-box;

  padding: 10px 20px 20px;

  overflow: hidden;
}

/* =========================================================
 * 视频整体区域
 * ========================================================= */

.video-container {
  width: 100%;
  height: 100%;

  min-width: 0;
  min-height: 0;

  display: flex;

  gap: 15px;

  box-sizing: border-box;

  overflow: hidden;
}

/* =========================================================
 * 左侧播放器
 * ========================================================= */

.video-player {
  flex: 1 1 auto;

  width: 0;

  min-width: 0;
  min-height: 0;

  height: 100%;

  display: flex;

  align-items: center;
  justify-content: center;

  overflow: hidden;

  box-sizing: border-box;
}

.video-player-component {
  width: 100%;
  height: 100%;

  min-width: 0;
  min-height: 0;

  display: block;
}

/* =========================================================
 * 右侧视频列表
 * ========================================================= */

.video-list-container {
  flex: 0 0 280px;

  width: 280px;

  min-width: 220px;
  max-width: 30%;

  height: 100%;

  min-height: 0;

  display: flex;
  flex-direction: column;

  box-sizing: border-box;

  overflow: hidden;
}

/* =========================================================
 * 控制按钮
 * ========================================================= */

.list-controls {
  flex: 0 0 auto;

  width: 100%;

  display: flex;

  align-items: center;

  gap: 8px;

  margin: 0 0 10px;

  box-sizing: border-box;
}

.list-controls .el-button {
  margin-left: 0;
}

/* =========================================================
 * 视频滚动区域
 * ========================================================= */

.scroll-container {
  flex: 1 1 auto;

  width: 100%;

  height: 0;

  min-height: 0;

  box-sizing: border-box;
}

/* =========================================================
 * 视频列表
 * ========================================================= */

.video-list {
  width: 100%;

  margin: 0;
  padding: 0;

  list-style: none;

  box-sizing: border-box;
}

.video-item {
  width: 100%;

  padding: 10px;

  box-sizing: border-box;

  cursor: pointer;

  border-radius: 4px;

  transition: background-color 0.2s;
}

.video-item:hover {
  background-color: var(--el-fill-color-light);
}

.video-item.active {
  background-color: var(--el-fill-color);
}

/* =========================================================
 * 视频信息
 * ========================================================= */

.video-info {
  width: 100%;

  min-width: 0;

  box-sizing: border-box;
}

.video-title {
  width: 100%;

  min-width: 0;

  display: flex;

  align-items: center;

  justify-content: space-between;

  gap: 8px;

  box-sizing: border-box;
}

.file-name {
  flex: 1 1 auto;

  min-width: 0;

  overflow: hidden;

  white-space: nowrap;

  text-overflow: ellipsis;
}

.playing-icon {
  margin-right: 5px;
}

.video-action {
  flex: 0 0 auto;
}

.video-duration {
  display: flex;

  align-items: center;

  margin-top: 6px;

  font-size: 13px;
}

/* =========================================================
 * 网页全屏
 * ========================================================= */

:global(.el-dialog.video-dialog.is-fullscreen) {
  width: 100vw !important;

  max-width: none !important;

  height: 100vh !important;

  max-height: 100vh !important;

  margin: 0 !important;

  border-radius: 0;

  box-sizing: border-box;
}

:global(.el-dialog.video-dialog.is-fullscreen .el-dialog__body) {
  padding: 10px 20px 20px;
}

/* =========================================================
 * 768 以下
 * ========================================================= */

@media screen and (max-width: 768px) {
  :global(.el-dialog.video-dialog) {
    width: 95vw !important;

    max-width: 95vw !important;

    height: 90vh !important;

    max-height: 90vh !important;

    margin: 5vh auto 0 !important;
  }

  .video-container {
    gap: 10px;
  }

  .video-list-container {
    flex-basis: 220px;

    width: 220px;

    min-width: 180px;

    max-width: 35%;
  }

  .video-item {
    padding: 8px;
  }
}

/* =========================================================
 * 手机竖屏
 * ========================================================= */

@media screen and (max-width: 600px) and (orientation: portrait) {
  :global(.el-dialog.video-dialog) {
    width: 96vw !important;

    max-width: 96vw !important;

    height: 90vh !important;

    max-height: 90vh !important;

    margin: 5vh auto 0 !important;
  }

  .video-container {
    flex-direction: column;
  }

  .video-player {
    width: 100%;

    height: auto;

    flex: 1 1 auto;

    min-height: 200px;
  }

  .video-list-container {
    flex: 0 0 220px;

    width: 100%;

    max-width: none;

    height: 220px;

    min-height: 180px;
  }
}
</style>
