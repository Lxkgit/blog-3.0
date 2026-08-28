<template>
  <div class="camera-page">
    <!-- 左侧摄像头组织树 -->
    <div class="camera-sidebar">
      <div class="sidebar-header">
        <span>摄像头</span>
      </div>

      <div class="camera-tree">
        <el-tree
          :data="cameraTree"
          node-key="id"
          :props="treeProps"
          default-expand-all
          highlight-current
          @node-click="handleCameraClick"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <MyIcon :type="data.type === 'group' ? 'folder' : 'video'" class="tree-icon" />

              <span class="tree-name">
                {{ data.name }}
              </span>

              <span
                v-if="data.type === 'camera'"
                class="camera-status"
                :class="{
                  online: data.online,
                  loading: loadingCameras.has(data.id),
                }"
              ></span>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 右侧视频区域 -->
    <div class="camera-content">
      <!-- 顶部工具栏 -->
      <div class="content-header">
        <div class="header-left">
          <span class="title">监控视频</span>

          <span class="playing-count"> {{ playingCameras.length }} / {{ screenCount }} </span>
        </div>

        <div class="header-right">
          <!-- 分屏 -->
          <el-button-group>
            <el-button :type="screenCount === 1 ? 'primary' : ''" @click="changeScreen(1)">
              1
            </el-button>

            <el-button :type="screenCount === 4 ? 'primary' : ''" @click="changeScreen(4)">
              4
            </el-button>

            <el-button :type="screenCount === 9 ? 'primary' : ''" @click="changeScreen(9)">
              9
            </el-button>
          </el-button-group>

          <!-- 清空 -->
          <el-button class="clear-btn" @click="clearAll"> 清空 </el-button>
        </div>
      </div>

      <!-- 视频分屏 -->
      <div
        class="video-grid"
        :class="{
          'grid-1': screenCount === 1,
          'grid-4': screenCount === 4,
          'grid-9': screenCount === 9,
        }"
      >
        <div v-for="index in screenCount" :key="index" class="video-item">
          <!-- 有摄像头 -->
          <template v-if="playingCameras[index - 1]">
            <VideoPlayer :video-src="playingCameras[index - 1].url" height="100%" />

            <!-- 摄像头名称 -->
            <div class="camera-name">
              {{ playingCameras[index - 1].name }}
            </div>

            <!-- 关闭当前窗口 -->
            <div class="close-video" @click.stop="removeCamera(index - 1)">
              <MyIcon type="close" />
            </div>
          </template>

          <!-- 空窗口 -->
          <div v-else class="empty-video">
            <MyIcon type="video" class="empty-icon" />

            <span>请选择摄像头</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VideoPlayer from '@/components/common/VideoPlayer.vue'

import { getCameraTokenApi } from '@/api/file'

/**
 * 视频流服务器地址
 *
 * 最终视频地址：
 *
 * http://124.221.195.130/rtsp/cam1/?token=xxxx
 */
const cameraStreamHost = 'http://124.221.195.130'

/**
 * 分屏数量
 *
 * 默认四分屏
 */
const screenCount = ref(4)

/**
 * 树配置
 */
const treeProps = {
  children: 'children',
  label: 'name',
}

/**
 * 摄像头组织树
 *
 * stream 为 MediaMTX / RTSP 转发名称
 *
 * 不再保存完整 url
 */
const cameraTree = ref([
  {
    id: 'smp-1',
    name: '树莓派',
    type: 'group',
    children: [
      {
        id: 'camera-1',
        name: '大厅摄像头',
        stream: 'cam1',
        type: 'camera',
        online: true,
      },
    ],
  },
])

/**
 * 当前播放中的摄像头
 */
const playingCameras = ref([])

/**
 * 正在获取 token 的摄像头
 *
 * 防止用户连续点击同一个摄像头
 * 导致重复请求 token
 */
const loadingCameras = ref(new Set())

/**
 * 根据 stream 生成基础视频地址
 *
 * 例如：
 *
 * cam1
 *
 * =>
 *
 * http://124.221.195.130/rtsp/cam1/
 */
const getCameraUrl = (stream) => {
  return `${cameraStreamHost}/rtsp/${stream}/`
}

/**
 * 根据视频地址和 token
 * 生成最终访问地址
 *
 * 例如：
 *
 * http://124.221.195.130/rtsp/cam1/
 *
 * =>
 *
 * http://124.221.195.130/rtsp/cam1/?token=xxxx
 */
const getCameraUrlWithToken = (stream, token) => {
  const url = getCameraUrl(stream)

  return `${url}?token=${encodeURIComponent(token)}`
}

/**
 * 点击摄像头
 */
const handleCameraClick = async (data) => {
  // 组织节点不处理
  if (data.type !== 'camera') {
    return
  }

  // 摄像头离线
  if (!data.online) {
    return
  }

  // 已经播放的不重复添加
  const exists = playingCameras.value.some((item) => item.id === data.id)

  if (exists) {
    return
  }

  // 正在获取 token，不重复请求
  if (loadingCameras.value.has(data.id)) {
    return
  }

  loadingCameras.value.add(data.id)

  try {
    // 根据当前摄像头 stream 获取 token
    const res = await getCameraTokenApi(data.stream)

    const token = res.result.token

    if (!token) {
      throw new Error('获取摄像头授权 token 失败')
    }

    // 分屏已满，移除第一个
    if (playingCameras.value.length >= screenCount.value) {
      playingCameras.value.shift()
    }

    // 生成最终视频地址
    const url = getCameraUrlWithToken(data.stream, token)

    // 加入播放列表
    playingCameras.value.push({
      ...data,
      url,
      token,
    })
  } catch (error) {
    console.error('获取摄像头 token 失败：', error)
  } finally {
    loadingCameras.value.delete(data.id)
  }
}

/**
 * 切换分屏
 */
const changeScreen = (count) => {
  screenCount.value = count

  /**
   * 如果当前播放数量超过新的分屏数量，
   * 删除多余的摄像头
   */
  if (playingCameras.value.length > count) {
    playingCameras.value = playingCameras.value.slice(0, count)
  }
}

/**
 * 删除某一个视频
 */
const removeCamera = (index) => {
  if (index < 0 || index >= playingCameras.value.length) {
    return
  }

  playingCameras.value.splice(index, 1)
}

/**
 * 清空所有视频
 */
const clearAll = () => {
  playingCameras.value = []
}
</script>

<style scoped>
.camera-page {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: hidden;
  background: var(--el-bg-color);
}

/* =========================
   左侧摄像头树
   ========================= */

.camera-sidebar {
  width: 260px;
  min-width: 260px;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}

.sidebar-header {
  height: 50px;
  min-height: 50px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid var(--el-border-color-light);
}

.camera-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tree-node {
  width: 100%;
  height: 32px;
  display: flex;
  align-items: center;
  min-width: 0;
}

.tree-icon {
  margin-right: 7px;
  font-size: 16px;
  flex-shrink: 0;
}

.tree-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* =========================
   摄像头状态
   ========================= */

.camera-status {
  width: 7px;
  height: 7px;
  margin-left: 8px;
  margin-right: 4px;
  border-radius: 50%;
  background: #999;
  flex-shrink: 0;
}

.camera-status.online {
  background: #67c23a;
}

/* 获取 token 时旋转 */

.camera-status.loading {
  border: 2px solid var(--el-color-primary);
  border-top-color: transparent;
  background: transparent;
  animation: camera-loading 0.8s linear infinite;
}

@keyframes camera-loading {
  to {
    transform: rotate(360deg);
  }
}

/* =========================
   右侧内容
   ========================= */

.camera-content {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部 */

.content-header {
  height: 50px;
  min-height: 50px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 15px;
  font-weight: 600;
}

.playing-count {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clear-btn {
  margin-left: 4px;
}

/* =========================
   视频网格
   ========================= */

.video-grid {
  flex: 1;
  min-height: 0;
  padding: 4px;
  display: grid;
  gap: 4px;
  background: #111;
}

/* 1 分屏 */

.video-grid.grid-1 {
  grid-template-columns: 1fr;
  grid-template-rows: 1fr;
}

/* 4 分屏 */

.video-grid.grid-4 {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

/* 9 分屏 */

.video-grid.grid-9 {
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
}

/* =========================
   视频窗口
   ========================= */

.video-item {
  position: relative;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: #000;
}

/* =========================
   空窗口
   ========================= */

.empty-video {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #666;
  font-size: 13px;
}

.empty-icon {
  font-size: 28px;
  opacity: 0.5;
}

/* =========================
   摄像头名称
   ========================= */

.camera-name {
  position: absolute;
  left: 8px;
  bottom: 8px;
  padding: 4px 8px;
  border-radius: 3px;
  background: rgb(0 0 0 / 55%);
  color: #fff;
  font-size: 12px;
  z-index: 10;
}

/* =========================
   关闭按钮
   ========================= */

.close-video {
  position: absolute;
  right: 8px;
  top: 8px;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: rgb(0 0 0 / 55%);
  color: #fff;
  cursor: pointer;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s;
}

.video-item:hover .close-video {
  opacity: 1;
}

.close-video:hover {
  background: rgb(0 0 0 / 75%);
}
</style>
