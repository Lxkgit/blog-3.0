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
                :class="{ online: data.online }"
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
            <VideoPlayer
              :video-src="playingCameras[index - 1].url"
              :video-token="playingCameras[index - 1].token"
              height="100%"
            />

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
import { ref, computed } from 'vue'
import VideoPlayer from '@/components/common/VideoPlayer.vue'

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
 * 这里先使用测试数据。
 *
 * 后面接你的摄像头接口时，
 * 只需要把 cameraTree 替换成接口返回的数据。
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
        type: 'camera',
        online: true,
        url: 'http://124.221.195.130:8889/cam1/',
        token: '',
      }

    ],
  }
])

/**
 * 当前播放中的摄像头
 *
 * 最大数量由 screenCount 决定。
 */
const playingCameras = ref([])

/**
 * 点击摄像头
 */
const handleCameraClick = (data) => {
  /**
   * 点击组织节点不处理
   */
  if (data.type !== 'camera') {
    return
  }

  /**
   * 摄像头离线
   */
  if (!data.online) {
    return
  }

  /**
   * 已经打开的不重复添加
   */
  const exists = playingCameras.value.some((item) => item.id === data.id)

  if (exists) {
    return
  }

  /**
   * 达到当前分屏数量
   */
  if (playingCameras.value.length >= screenCount.value) {
    /**
     * 这里采用替换第一个画面的方式。
     *
     * 后面如果你想改成提示“分屏已满”，
     * 可以再调整。
     */
    playingCameras.value.shift()
  }

  /**
   * 添加摄像头
   *
   * token 暂时为空。
   *
   * 后续由这个页面获取 token 后
   * 填入 data.token。
   */
  playingCameras.value.push({
    ...data,
    token: data.token || '',
  })
}

/**
 * 切换分屏
 */
const changeScreen = (count) => {
  screenCount.value = count

  /**
   * 如果当前播放数量超过新的分屏数量，
   * 删除多余的摄像头。
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

/* 摄像头状态 */

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

/* 视频窗口 */

.video-item {
  position: relative;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: #000;
}

/* 空窗口 */

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

/* 摄像头名称 */

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

/* 关闭按钮 */

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
