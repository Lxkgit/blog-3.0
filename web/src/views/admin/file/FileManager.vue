<template>
  <div class="file-page">
    <el-card class="file-card">
      <!-- 顶部工具栏 -->
      <div class="file-toolbar">
        <!-- 上传文件 -->
        <el-upload
          :auto-upload="false"
          multiple
          :show-file-list="false"
          :on-change="changeUpload"
        >
          <el-button type="success" size="small" text>
            上传文件
          </el-button>
        </el-upload>

        <!-- 路径 -->
        <div class="path-toolbar">
          <MyIcon
            :style="
              filePath === ''
                ? { 'pointer-events': 'none' }
                : { cursor: 'pointer' }
            "
            class="back-icon"
            type="icon-shangyibu"
            title="返回上一级"
            @click="changePath(-2)"
          />

          <span class="path-label">
            当前路径：
          </span>

          <div class="path-list">
            <span
              class="file_path"
              @click="changePath(-1)"
            >
              根目录
            </span>

            <div
              v-for="(item, idx) in filePathArr"
              :key="idx"
              class="path-item"
            >
              <span
                class="file_path"
                @click="changePath(idx)"
              >
                {{ item }}
              </span>
            </div>
          </div>
        </div>

        <!-- 列表 / 卡片 -->
        <div class="view-switch">
          <el-switch
            v-model="switchFlag"
            class="mb-2"
            style="
              --el-switch-on-color: #13ce66;
              --el-switch-off-color: #ff4949;
            "
            active-text="列表"
            inactive-text="卡片"
            @change="updateSwitchFlag"
          />
        </div>
      </div>

      <el-divider content-position="left">
        文件目录
      </el-divider>

      <!-- 文件内容区域 -->
      <div
        class="file-content"
        @contextmenu.prevent="openMenu($event)"
      >
        <!-- ==================== 列表模式 ==================== -->
        <div
          v-if="switchFlag"
          class="list-wrapper"
          @contextmenu.prevent.stop="openMenu($event)"
        >
          <!-- 表头 -->
          <ul class="table-header">
            <li
              v-for="(header, index) in headers"
              :key="index"
              class="header-item"
              :style="{ width: header.width }"
            >
              {{ header.title }}
            </li>
          </ul>

          <!-- 数据 -->
          <div class="table-body">
            <!-- 目录 -->
            <ul
              v-for="(row, rowIndex) in dirList.data"
              :key="'dir-' + rowIndex"
              class="table-row"
            >
              <div
                class="table-row-inner"
                @contextmenu.prevent.stop="openMenu($event, 1, row)"
                @dblclick="openFileDirFun(row)"
              >
                <li
                  class="data-item name-item"
                  :style="{ width: headers[0].width }"
                >
                  <MyIcon type="icon-folder" />
                  <span class="file-name-text">
                    {{ row.dirName }}
                  </span>
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[1].width }"
                >
                  {{ fileSizeConvert(row.occupySpace).value }}
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[2].width }"
                >
                  目录
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[3].width }"
                >
                  {{ row.createBy }}
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[4].width }"
                >
                  {{ row.createTime }}
                </li>

                <li
                  class="data-item action-item"
                  :style="{ width: headers[5].width }"
                >
                  <MyIcon
                    class="icon-btn"
                    type="icon-file-open"
                    title="打开文件夹"
                    @click="openFileDirFun(row)"
                  />

                  <MyIcon
                    class="icon-btn"
                    type="icon-delete"
                    title="删除文件夹"
                    @click="deleteFileDirFun(row)"
                  />
                </li>
              </div>
            </ul>

            <!-- 文件 -->
            <ul
              v-for="(row, rowIndex) in fileList.data"
              :key="'file-' + rowIndex"
              class="table-row"
            >
              <div
                class="table-row-inner"
                @contextmenu.prevent.stop="openMenu($event, 2, row)"
                @dblclick="openFileFun(row)"
              >
                <li
                  class="data-item name-item"
                  :style="{ width: headers[0].width }"
                  :title="row.fileName"
                >
                  <MyIcon
                    v-if="fileTypeEnum(row.fileType).key === 1"
                    type="icon-img"
                  />

                  <MyIcon
                    v-else-if="fileTypeEnum(row.fileType).key === 2"
                    type="icon-zip"
                  />

                  <MyIcon
                    v-else-if="fileTypeEnum(row.fileType).key === 3"
                    type="icon-video"
                  />

                  <MyIcon
                    v-else
                    type="icon-file"
                  />

                  <span class="file-name-text">
                    {{ row.fileName }}
                  </span>
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[1].width }"
                >
                  {{ fileSizeConvert(row.fileSize).value }}
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[2].width }"
                >
                  {{ fileStatusEnum(row.fileStatus).value }}
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[3].width }"
                >
                  {{ row.createBy }}
                </li>

                <li
                  class="data-item"
                  :style="{ width: headers[4].width }"
                >
                  {{ row.createTime }}
                </li>

                <li
                  class="data-item action-item"
                  :style="{ width: headers[5].width }"
                >
                  <MyIcon
                    class="icon-btn"
                    title="预览"
                    type="icon-eye"
                    @click="openFileFun(row)"
                  />

                  <MyIcon
                    class="icon-btn"
                    title="同步"
                    type="icon-refresh"
                    @click="syncFileFun(row)"
                  />

                  <MyIcon
                    class="icon-btn"
                    title="删除"
                    type="icon-delete"
                    @click="deleteFileFun(row)"
                  />
                </li>
              </div>
            </ul>
          </div>
        </div>

        <!-- ==================== 卡片模式 ==================== -->
        <div
          v-else
          class="card-wrapper"
        >
          <!-- 目录 -->
          <div
            v-for="item in dirList.data"
            :key="'dir-card-' + item.id"
            class="file-card-item"
            @contextmenu.prevent.stop="openMenu($event, 1, item)"
          >
            <div
              class="file-preview"
              @dblclick="openFileDirFun(item)"
            >
              <MyIcon
                class="folder-icon"
                type="icon-folder"
              />

              <div class="show_icon">
                <MyIcon
                  title="打开文件夹"
                  class="icon_type"
                  type="icon-file-open"
                  @click="openFileDirFun(item)"
                />

                <MyIcon
                  title="删除"
                  class="icon_type"
                  type="icon-delete"
                  @click="deleteFileDirFun(item)"
                />
              </div>
            </div>

            <div class="file-card-footer">
              <el-tag
                class="file-type-tag"
                size="small"
              >
                目录
              </el-tag>

              <span
                class="file-card-name"
                :title="item.dirName"
              >
                {{ item.dirName }}
              </span>
            </div>
          </div>

          <!-- 文件 -->
          <div
            v-for="item in fileList.data"
            :key="'file-card-' + item.id"
            class="file-card-item"
            @contextmenu.prevent.stop="openMenu($event, 2, item)"
          >
            <div class="file-preview">
              <!-- 图片 -->
              <img
                v-if="fileTypeEnum(item.fileType).key === 1"
                :src="item.fileUrl"
                class="image-preview"
                loading="lazy"
              />

              <!-- 压缩包 -->
              <div
                v-else-if="fileTypeEnum(item.fileType).key === 2"
                class="file-icon-preview"
              >
                <MyIcon
                  :title="fileTypeEnum(item.fileType).value"
                  class="large-icon"
                  type="icon-zip"
                />
              </div>

              <!-- 视频 -->
              <div
                v-else-if="fileTypeEnum(item.fileType).key === 3"
                class="file-icon-preview"
              >
                <MyIcon
                  :title="fileTypeEnum(item.fileType).value"
                  class="video-icon"
                  type="icon-video"
                />
              </div>

              <!-- 普通文件 -->
              <div
                v-else
                class="file-icon-preview"
              >
                <MyIcon
                  :title="fileTypeEnum(item.fileType).value"
                  class="large-icon"
                  type="icon-file"
                />
              </div>

              <div class="show_icon">
                <MyIcon
                  title="预览"
                  class="icon_type"
                  type="icon-search"
                  @click="openFileFun(item)"
                />

                <MyIcon
                  title="查看文件信息"
                  class="icon_type"
                  type="icon-file"
                  @click="showFileDesc(item)"
                />

                <MyIcon
                  title="删除"
                  class="icon_type"
                  type="icon-delete"
                  @click="deleteFileFun(item)"
                />

                <el-dropdown>
                  <el-button
                    type="primary"
                    size="small"
                  >
                    移动
                  </el-button>

                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="(dir, rowIndex) in dirList.data"
                        :key="rowIndex"
                        @click="moveFileFun(dir, item)"
                      >
                        {{ dir.dirName }}
                      </el-dropdown-item>

                      <el-dropdown-item
                        @click="moveFileFun(null, item)"
                      >
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="file-card-footer">
              <el-tag
                class="file-type-tag"
                size="small"
              >
                {{ fileTypeEnum(item.fileType).value }}
              </el-tag>

              <span
                class="file-card-name"
                :title="item.fileName"
              >
                {{ item.fileName }}
              </span>

              <MyIcon
                v-if="item.type !== 0"
                type="icon-download"
                class="download-icon"
              />
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- ==================== 图片查看 ==================== -->
    <el-image-viewer
      v-if="dialogImageUrl.show"
      :initial-index="dialogImageUrl.index"
      :index="dialogImageUrl.index"
      :url-list="dialogImageUrl.url"
      :hide-on-click-modal="true"
      :show-progress="true"
      @close="dialogImageUrl.show = false"
    />

    <!-- ==================== 右键菜单 ==================== -->
    <ul
      v-if="menu.visible"
      :style="{
        left: menu.left + 'px',
        top: menu.top + 'px'
      }"
      class="contextmenu"
    >
      <li @click="refreshDir()">
        刷新
      </li>

      <li v-if="menu.type === 0">
        <el-upload
          :auto-upload="false"
          multiple
          :show-file-list="false"
          style="padding: 0"
          @click="menu.visible = false"
          :on-change="changeUpload"
        >
          上传文件
        </el-upload>
      </li>

      <li
        v-if="menu.type === 0"
        @click="createDir()"
      >
        创建目录
      </li>

      <li
        v-if="menu.type === 0"
        @click="changePath(-2)"
      >
        返回上一级
      </li>

      <li
        v-if="menu.type === 1"
        @click="openDir()"
      >
        打开目录
      </li>

      <li
        v-if="menu.type === 1"
        @click=""
      >
        查看目录信息
      </li>

      <li
        v-if="menu.type === 1"
        @click="deleteFileDirFun()"
      >
        删除目录
      </li>

      <li
        v-if="menu.type === 2"
        @click=""
      >
        打开文件
      </li>

      <li
        v-if="menu.type === 2"
        @click="showFileDesc(null)"
      >
        查看文件信息
      </li>

      <li
        v-if="menu.type === 2"
        @click=""
      >
        删除文件
      </li>

      <li @click="menu.visible = false">
        关闭菜单
      </li>
    </ul>

    <!-- ==================== 文件信息 ==================== -->
    <el-dialog
      v-model="menu.fileDialog"
      width="min(40vw, 600px)"
      class="file-info-dialog"
    >
      <el-form
        :model="menu.file"
        label-width="100px"
      >
        <el-form-item label="文件名称: ">
          <el-input
            v-model="menu.file.fileName"
            size="small"
          />
        </el-form-item>

        <el-form-item
          v-if="menu.file.type === 0"
          label="目录大小: "
        >
          <el-input
            v-model="menu.file.fileSize"
            size="small"
          />
        </el-form-item>

        <el-form-item
          v-else
          label="文件大小: "
        >
          <el-input
            v-model="menu.file.fileSize"
            size="small"
          />
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- ==================== 创建目录 ==================== -->
    <el-dialog
      v-model="menu.dirDialog"
      width="min(40vw, 600px)"
      class="dir-dialog"
    >
      <el-form
        :model="menu.dirFile"
        label-width="100px"
        ref="createFileFormRef"
        :rules="createFileRules"
        label-position="left"
      >
        <el-form-item
          label="目录名称: "
          prop="name"
        >
          <el-input
            v-model="menu.dirFile.name"
            size="small"
          />
        </el-form-item>

        <el-form-item
          label="目录类型: "
          prop="dirType"
        >
          <el-radio-group v-model="menu.dirFile.dirType">
            <el-radio border :label="1">
              普通目录

              <el-tooltip
                content="普通目录"
                placement="top"
                @click.stop.prevent
              >
                <MyIcon type="icon-wenhaofill" />
              </el-tooltip>
            </el-radio>

            <el-radio border :label="2">
              评分目录

              <el-tooltip
                content="用于对文件质量分类存放"
                placement="top"
                @click.stop.prevent
              >
                <MyIcon type="icon-wenhaofill" />
              </el-tooltip>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button
            type="primary"
            @click="saveFileDirFun"
          >
            创建
          </el-button>

          <el-button
            @click="menu.dirDialog = false"
          >
            取消
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- ==================== 视频播放 ==================== -->
    <el-dialog
      v-model="video.showVideoDialog"
      class="video-dialog"
      :close-on-click-modal="false"
      title="视频播放"
      width="80vw"
      top="5vh"
      @opened="handleDialogOpened"
      @open="handleDialogOpen"
      @close="handleDialogClose"
      :fullscreen="video.fullscreen"
    >
      <div class="video-container">
        <!-- 左侧播放器 -->
        <div class="video-player">
          <VideoPlayer
            ref="videoPlayerRef"
            class="video-player-component"
            :video-src="video.videoUrl"
            :reset-on-load="video.resetFlag"
            :destroy-player="video.destroy"
          />
        </div>

        <!-- 右侧视频列表 -->
        <div class="video-list-container">
          <div class="list-controls">
            <el-button
              type="primary"
              @click="nextVideo(-1)"
              :disabled="video.videoList.length === 0"
            >
              上一个
            </el-button>

            <el-button
              type="primary"
              @click="nextVideo(1)"
              :disabled="video.videoList.length === 0"
            >
              下一个
            </el-button>

            <el-button
              type="primary"
              @click="toggleFullscreen()"
            >
              <MyIcon
                v-if="video.fullscreen"
                title="退出全屏"
                type="icon-half-screen"
              />

              <MyIcon
                v-else
                title="网页全屏"
                type="icon-full-screen"
              />
            </el-button>
          </div>

          <el-scrollbar class="scroll-container">
            <ul class="video-list">
              <li
                v-for="(item, index) in video.videoList"
                :key="index"
                :class="[
                  'video-item',
                  {
                    active: video.currentIndex === index
                  }
                ]"
                @click="playVideo(item)"
              >
                <div
                  v-if="index === video.currentIndex"
                  class="video-info"
                >
                  <div class="video-title">
                    <div
                      class="file-name"
                      :title="item.fileName"
                    >
                      <MyIcon
                        title="视频正在播放"
                        type="icon-playing"
                        class="playing-icon"
                      />

                      {{ item.fileName }}
                    </div>

                    <div class="video-action">
                      <el-dropdown>
                        <el-button
                          type="primary"
                          size="small"
                        >
                          移动
                        </el-button>

                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item
                              v-for="(dir, rowIndex) in dirList.data"
                              :key="rowIndex"
                              @click="moveFileFun(dir, item)"
                            >
                              {{ dir.dirName }}
                            </el-dropdown-item>

                            <el-dropdown-item
                              @click="moveFileFun(null, item)"
                            >
                              删除
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>

                  <div class="video-duration">
                    <MyIcon
                      title="视频时长"
                      type="icon-time"
                    />

                    {{ timeToMinOrHour(item.videoTime) }}
                  </div>
                </div>

                <div
                  v-else
                  class="video-info"
                >
                  <div class="video-title">
                    <div class="file-name">
                      {{ item.fileName }}
                    </div>
                  </div>

                  <div class="video-duration">
                    <MyIcon
                      title="视频时长"
                      type="icon-time"
                    />

                    {{ timeToMinOrHour(item.videoTime) }}
                  </div>
                </div>
              </li>
            </ul>
          </el-scrollbar>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import VideoPlayer from '@/components/common/VideoPlayer.vue'
import {
  selectFileDirApi,
  selectFileApi,
  deleteFileDirApi,
  deleteFileApi,
  uploadApi,
  saveFileDirApi,
  syncFileApi,
  moveFileApi,
} from '@/api/file'
import icon from '@/utils/icon'
import { onMounted, onUnmounted, onBeforeUnmount, ref, reactive, nextTick } from 'vue'
import mixin from '@/mixins/fileType'
import { ElImageViewer } from 'element-plus'
import { ElMessage } from 'element-plus'
import timeFormat from '@/utils/timeFormat'
import { fileStore } from '@/store/file'

let { timeToMinOrHour } = timeFormat()

let {
  switchFlag,
  createFileRules,
  filePath,
  filePathArr,
  dirList,
  fileList,
  dialogImageUrl,
  menu,
  openMenu,
  updateSwitchFlag,
  showFileDesc,
  saveFileDirFun,
  changeUpload,
  refreshDir,
  createDir,
  openDir,
  selectFileDirOrFileFun,
  changePath,
  openFileDirFun,
  openFileFun,
  deleteFileDirFun,
  deleteFileFun,
  syncFileFun,
  moveFileFun,
} = fileFn()

let {
  video,
  videoPlayerRef,
  openDialog,
  toggleFullscreen,
  playVideo,
  nextVideo,
  handleDialogOpened,
  handleDialogOpen,
  handleDialogClose,
} = videoFn()

const fStore = fileStore()

let { fileTypeEnum, fileStatusEnum, fileSizeConvert } = mixin()
let { MyIcon } = icon()

let headers = [
  { title: '文件/目录名称', width: '25%' },
  { title: '文件/目录大小', width: '17%' },
  { title: '文件状态', width: '16%' },
  { title: '创建用户', width: '13%' },
  { title: '创建时间', width: '16%' },
  { title: '操作', width: '13%' },
]

onMounted(() => {
  // 初始化目录
  if (fStore.filePath !== 'null') {
    filePath.value = fStore.filePath
    filePathArr.value = fStore.filePathArr
  }
  switchFlag.value = fStore.switchFlag
  selectFileDirOrFileFun()
  window.addEventListener('keydown', onKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
})

function onKey(e: KeyboardEvent) {
  if (e.key === 'Backspace') {
    changePath(-2)
  }
}

function videoFn(): any {
  let video: any = reactive({
    // dialog 是否显示
    showVideoDialog: false,
    // 当前播放视频url
    videoUrl: '',
    // 重置播放器
    resetFlag: false,
    // 销毁播放器
    destroy: false,
    // 全部视频文件列表
    videoList: [],
    // 当前播放视频 计数从0开始
    currentIndex: 0,
    // dialog是否全屏
    fullscreen: false,
    videoStyle: {
      videoWidth: '58vw',
      videoHeight: '59vh',
      listHeight: '400px',
    },
  })

  let videoStyle = {
    fullScreen: {
      videoWidth: '78vw',
      videoHeight: '90vh',
      listHeight: '800px',
    },

    halfScreen: {
      videoWidth: '58vw',
      videoHeight: '59vh',
      listHeight: '400px',
    },
  }

  interface PlayerRef {
    playPlayer: () => void
    pausePlayer?: () => void
  }

  const videoPlayerRef = ref<PlayerRef | null>(null)

  // 打开对话框时设置视频URL
  const openDialog = () => {
    video.showVideoDialog = true

    nextTick(() => {
      // 确保播放器已初始化
      if (videoPlayerRef.value) {
        videoPlayerRef.value.playPlayer?.()
      }
    })
  }

  // 独立控制全屏状态的方法
  const toggleFullscreen = () => {
    video.fullscreen = !video.fullscreen
    updateVideoStyle() // 更新样式
  }

  const playVideo = (item: any) => {
    video.videoUrl = item.fileUrl

    for (let i = 0; i < video.videoList.length; i++) {
      if (video.videoUrl === video.videoList[i].fileUrl) {
        video.currentIndex = i
      }
    }

    nextTick(() => {
      // 确保播放器已初始化
      if (videoPlayerRef.value) {
        videoPlayerRef.value.playPlayer?.()
      }
    })
  }

  const nextVideo = (next: any) => {
    video.currentIndex += next
    if (video.currentIndex >= video.videoList.length) {
      video.currentIndex = video.currentIndex % video.videoList.length
    } else if (video.currentIndex < 0) {
      video.currentIndex = video.videoList.length - 1
    }
    playVideo(video.videoList[video.currentIndex])
  }

  // 提取样式更新逻辑
  const updateVideoStyle = () => {
    video.videoStyle = video.fullscreen ? videoStyle.fullScreen : videoStyle.halfScreen
  }

  // 对话框完全打开后调整播放器
  const handleDialogOpened = () => {
    // 切换重置标志以触发重置
    // video.resetFlag = !video.resetFlag
  }

  const handleDialogClose = () => {
    video.showVideoDialog = false
    selectFileDirOrFileFun()
    nextTick(() => {
      // 确保播放器已初始化
      if (videoPlayerRef.value) {
        videoPlayerRef.value.pausePlayer?.()
      }
    })
  }

  // 对话框开始打开时（可选）
  const handleDialogOpen = () => {
    // 如果需要，可以在这里重置播放器状态
  }

  onBeforeUnmount(() => {})

  return {
    video,
    videoPlayerRef,
    openDialog,
    toggleFullscreen,
    playVideo,
    nextVideo,
    handleDialogOpened,
    handleDialogOpen,
    handleDialogClose,
  }
}

/**
 * 文件云盘接口合集
 */
function fileFn(): any {
  // 创建目录表单
  const createFileFormRef: any = ref(null)

  let switchFlag = ref(true)
  let filePath: any = ref('/user')
  let filePathArr = ref(['/user'])
  let dirList: any = reactive({ data: [] })
  let fileList: any = reactive({ data: [] })
  let dialogImageUrl: any = reactive({ url: [], index: 0, show: false })

  let menu: any = reactive({
    visible: false,
    left: 0,
    top: 0,
    type: -1,
    fileDialog: false,
    dirDialog: false,
    dirFile: {
      name: '',
    },
    dir: null,
    file: null,
  })

  /**
   * 打开菜单
   */
  const openMenu = (e: any, type?: any, row?: any) => {
    if (type === 1) {
      // 打开目录
      menu.dir = row
      menu.type = type
    } else if (type === 2) {
      // 打开文件
      menu.file = row
      menu.type = type
    } else {
      // 空白页面打开菜单
      menu.type = 0
    }

    menu.visible = true
    menu.left = e.clientX
    menu.top = e.clientY
  }

  /**
   * 查看文件详细信息
   */
  const showFileDesc = (item: any) => {
    if (item === null) {
      menu.visible = false
    } else {
      menu.file = item
    }
    menu.fileDialog = true
  }

  // 注册表单验证规则
  const createFileRules = {
    name: [{ required: true, message: '请输入目录名称', trigger: 'blur' }],
  }

  const updateSwitchFlag = (val: boolean) => {
    console.log('切换开关')
    console.log('切换开关' + val)
    fStore.switchFlag = val
  }

  /**
   * 创建文件目录
   */
  const saveFileDirFun = () => {
    if (createFileFormRef.value !== null) {
      createFileFormRef.value.validate((valid: any) => {
        if (valid) {
          saveFileDirApi({
            dirPath: filePath.value,
            dirName: menu.dirFile.name,
            dirType: menu.dirFile.dirType,
          }).then((res: any) => {
            if (res.code === 200) {
              menu.dirFile.name = ''
              ElMessage.success({ message: '目录创建成功', type: 'success' })
              selectFileDirOrFileFun()
            }
          })
          menu.dirDialog = false
        }
      })
    }
  }

  /**
   * 上传文件
   */
  const changeUpload = (file: any, fileLists: any) => {
    if (file.size / 1024 / 1024 > 1024) {
      ElMessage.error('单文件最大上传大小为1G')
      return
    }
    const data = new FormData()
    data.append('file', file.raw)
    data.append('appointPath', filePath.value)
    uploadApi(data).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success({ message: '文件上传成功', type: 'success' })
        selectFileDirOrFileFun()
      } else {
        ElMessage.error({ message: res.message, type: 'error' })
      }
    })
  }

  /**
   * 刷新当前目录
   */
  const refreshDir = () => {
    menu.visible = false
    selectFileDirOrFileFun()
    ElMessage.success({ message: '刷新成功', type: 'success' })
  }

  /**
   * 菜单-创建目录
   */
  const createDir = () => {
    menu.dirDialog = true
    menu.visible = false
  }

  /**
   * 菜单-打开目录
   */
  const openDir = () => {
    menu.visible = false
    openFileDirFun(menu.dir)
  }

  /**
   * 查询当前目录下目录与文件列表
   */
  const selectFileDirOrFileFun = () => {
    let path = filePath.value
    fileList.data = []
    dirList.data = []
    selectFileDirApi({
      dirPath: path,
    }).then((res: any) => {
      if (res.code === 200) {
        dirList.data = res.result
      }
    })
    selectFileApi({
      dirPath: path,
    }).then((res: any) => {
      if (res.code === 200) {
        fileList.data = res.result
      }
    })
  }

  /**
   * 切换目录
   */
  const changePath = (idx: any) => {
    // 回到根目录
    if (idx === -1) {
      filePath.value = null
      filePathArr.value = []
      selectFileDirOrFileFun()
      fStore.filePath = filePath.value
      fStore.filePathArr = filePathArr.value
    } else if (idx === -2) {
      // 回到上一级
      if (filePathArr.value.length <= 1) {
        filePath.value = null
        filePathArr.value = []
        fStore.filePath = filePath.value
        fStore.filePathArr = filePathArr.value
      } else {
        idx = filePathArr.value.length - 2
        filePath.value = ''
        for (let i = 0; i <= idx; i++) {
          filePath.value += filePathArr.value[i]
        }
        filePathArr.value.splice(idx + 1)
        fStore.filePath = filePath.value
        fStore.filePathArr = filePathArr.value
      }
      selectFileDirOrFileFun()
    } else {
      // 进入指定目录
      if (idx !== filePathArr.value.length - 1) {
        filePath.value = ''
        for (let i = 0; i <= idx; i++) {
          filePath.value += filePathArr.value[i]
        }
        filePathArr.value.splice(idx + 1)
        selectFileDirOrFileFun()
        fStore.filePath = filePath.value
        fStore.filePathArr = filePathArr.value
      }
    }
    console.log('当前路径 ： ' + filePath + '  store' + fStore.filePath)
  }

  /**
   * 打开文件目录
   */
  const openFileDirFun = (dir: any) => {
    filePathArr.value.push('/' + dir.dirName)

    if (filePath.value !== null) {
      filePath.value += '/' + dir.dirName
    } else {
      filePath.value = '/' + dir.dirName
    }
    fStore.filePath = filePath.value
    fStore.filePathArr = filePathArr.value
    selectFileDirOrFileFun()
  }

  /**
   * 打开文件
   * @param file 文件
   */
  const openFileFun = (file: any) => {
    if (fileTypeEnum(file.fileType).key === 1) {
      showImg(file)
    } else if (fileTypeEnum(file.fileType).key === 3) {
      playVideo(file)
    } else {
      ElMessage.warning(fileTypeEnum(file.fileType).value + ' 类型文件不支持查看')
    }
  }

  /**
   * 展示图片
   */
  const showImg = (img: any) => {
    dialogImageUrl.show = true
    dialogImageUrl.url = []
    let imgList = []
    for (let i = 0; i < fileList.data.length; i++) {
      if (fileTypeEnum(fileList.data[i].fileType).key === 1) {
        imgList.push(fileList.data[i])
      }
    }
    for (let i = 0; i < imgList.length; i++) {
      if (fileTypeEnum(imgList[i].fileType).key === 1) {
        if (imgList[i].fileUrl === img.fileUrl) {
          dialogImageUrl.index = i
        }
        dialogImageUrl.url.push(imgList[i].fileUrl)
      }
    }
  }

  /**
   * 播放视频
   * @param video 当前需要播放的视频
   */
  const playVideo = (file: any) => {
    video.videoList = []
    video.videoUrl = file.fileUrl
    // 添加全部
    for (let i = 0; i < fileList.data.length; i++) {
      if (fileTypeEnum(fileList.data[i].fileType).key === 3) {
        video.videoList.push(fileList.data[i])
      }
    }

    for (let i = 0; i < video.videoList.length; i++) {
      let fileJson = JSON.parse(video.videoList[i].fileJson)
      video.videoList[i].videoTime = fileJson.durationSeconds

      if (video.videoUrl === video.videoList[i].fileUrl) {
        video.currentIndex = i
      }
    }

    openDialog()
  }

  const removeVideo = (file: any) => {
    video.videoList = video.videoList.filter((v: any) => v.id !== file.id)
    if (video.videoList.length === 0) {
      handleDialogClose()
      return
    }
    nextVideo(0)
  }

  /**
   * 删除目录
   */
  const deleteFileDirFun = (item: any) => {
    deleteFileDirApi({
      dirPath: filePath.value,
      dirName: item.dirName,
    }).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('目录删除成功')
        selectFileDirOrFileFun()
      }
    })
  }

  /**
   * 删除文件
   */
  const deleteFileFun = (item: any) => {
    deleteFileApi({
      idList: item.id,
    }).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('文件删除成功')
        selectFileDirOrFileFun()
      }
    })
  }

  /**
   * 同步文件
   * @param item
   */
  const syncFileFun = (item: any) => {
    let syncStatus: any
    if (item.fileStatus === 0 || item.fileStatus === 4) {
      syncFileApi({
        id: item.id,
        syncFileStatus: item.fileStatus === 0 ? 4 : 0,
      }).then((res: any) => {
        if (res.code === 200) {
          selectFileDirOrFileFun()
          ElMessage.success('同步命令发送成功')
        }
      })
    } else if (item.fileStatus === 1 || item.fileStatus === 3) {
      ElMessage.warning('文件正在同步中')
    } else if (item.fileStatus === 2) {
      ElMessage.warning('文件正在等待数据同步')
    } else {
      ElMessage.error('文件状态异常')
    }
  }

  /**
   * 移动文件
   * @param dir
   * @param file
   */
  const moveFileFun = (dir: any, file: any) => {
    if (dir === null) {
      deleteFileFun(file)
    } else {
      moveFileApi({
        id: file.id,
        newDirId: dir.id,
      }).then((res: any) => {
        if (res.code === 200) {
          ElMessage.success('文件移动成功')
          selectFileDirOrFileFun()
        }
      })
    }
    removeVideo(file)
  }

  return {
    switchFlag,
    createFileFormRef,
    createFileRules,
    filePath,
    filePathArr,
    dirList,
    fileList,
    dialogImageUrl,
    menu,
    openMenu,
    updateSwitchFlag,
    showFileDesc,
    saveFileDirFun,
    changeUpload,
    refreshDir,
    createDir,
    openDir,
    selectFileDirOrFileFun,
    changePath,
    openFileDirFun,
    openFileFun,
    showImg,
    deleteFileDirFun,
    deleteFileFun,
    syncFileFun,
    moveFileFun,
  }
}
</script>

<style scoped>
/* 文件/目录列表文字显示修复 */
.file-name,
.file_path {
  line-height: 24px;
  min-height: 24px;
  box-sizing: border-box;
}

/* 文件列表中的名称 */
.file-name {
  display: flex;
  align-items: center;
}

/* 目录/文件列表项 */
.file-item {
  min-height: 40px;
  height: auto;
  line-height: normal;
  box-sizing: border-box;
}

/* 如果名称外面还有文字容器 */
.file-info {
  min-height: 24px;
  line-height: 24px;
  box-sizing: border-box;
}

/* Element Plus 表格里的文件名 */
:deep(.el-table .cell) {
  line-height: 24px;
  min-height: 24px;
  padding-top: 2px;
  padding-bottom: 2px;
  box-sizing: border-box;
}

/* 卡片模式下的文件名称 */
.file-card,
.file-card-name {
  line-height: 24px;
  min-height: 24px;
  height: auto;
  box-sizing: border-box;
}

/* 防止父元素把文字裁掉 */
.file-name,
.file-info,
.file-card-name {
  overflow: visible;
}
/* =========================================================
   页面整体
   ========================================================= */

.file-page {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  color: #606266;
}

.file-card {
  width: 100%;
  height: 85vh;
  height: 85dvh;
  min-height: 400px;
  box-sizing: border-box;
  overflow: hidden;
}

.file-card :deep(.el-card__body) {
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}


/* =========================================================
   顶部工具栏
   ========================================================= */

.file-toolbar {
  width: 100%;
  min-width: 0;
  flex-shrink: 0;

  display: flex;
  align-items: flex-start;
  gap: 0;

  font-size: 14px;
}

.path-toolbar {
  min-width: 0;
  max-width: calc(100% - 300px);

  display: flex;
  align-items: center;

  line-height: 23px;
  margin-left: 20px;
}

.back-icon {
  flex-shrink: 0;
  margin-right: 20px;
  outline: 0;
}

.path-label {
  flex-shrink: 0;
  white-space: nowrap;
}

.path-list {
  min-width: 0;

  display: flex;
  flex-wrap: wrap;
  align-items: center;

  margin-left: 10px;

  overflow: hidden;
}

.path-item {
  display: inline-flex;
  margin-left: 5px;
  max-width: 300px;
}

.file_path {
  cursor: pointer;
  white-space: nowrap;
}

.file_path:hover {
  color: var(--el-color-primary);
}

.view-switch {
  flex-shrink: 0;
  margin-left: auto;
  margin-right: 20px;
}


/* =========================================================
   Divider
   ========================================================= */

.file-card :deep(.el-divider) {
  flex-shrink: 0;
  margin: 16px 0;
}


/* =========================================================
   文件内容
   ========================================================= */

.file-content {
  flex: 1;
  min-width: 0;
  min-height: 0;

  overflow: hidden;

  display: flex;
  flex-direction: column;

  margin: 0;
}


/* =========================================================
   列表模式
   ========================================================= */

.list-wrapper {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;

  display: flex;
  flex-direction: column;

  overflow: hidden;
}

.table-header,
.table-row {
  display: flex;

  width: 100%;

  list-style: none;
  padding: 0;
  margin: 0;

  box-sizing: border-box;
}

.table-header {
  flex-shrink: 0;

  padding-left: 10px;

  background-color: #f8f9fa;

  font-weight: 600;

  border-bottom: 2px solid #dee2e6;
}

.table-body {
  flex: 1;
  min-height: 0;

  overflow: auto;
}

.table-row {
  min-height: 45px;

  padding-left: 10px;

  transition: background-color 0.2s;
}

.table-row:nth-child(even) {
  background-color: #f8f9fa;
}

.table-row:hover {
  background-color: #e9ecef;
}

.table-row-inner {
  display: flex;

  width: 100%;
  min-width: 850px;

  position: relative;

  cursor: pointer;
}

.header-item,
.data-item {
  flex-shrink: 0;
  min-width: 0;

  box-sizing: border-box;

  padding: 10px 6px;

  min-height: 42px;

  display: flex;
  align-items: center;

  overflow: hidden;

  white-space: nowrap;
  text-overflow: ellipsis;
}

.name-item {
  overflow: hidden;
}

.file-name-text {
  min-width: 0;

  margin-left: 6px;

  overflow: hidden;

  text-overflow: ellipsis;

  white-space: nowrap;
}

.action-item {
  overflow: visible;
}

.icon-btn {
  flex-shrink: 0;

  margin-left: 8px;

  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;

  width: 30px;
  height: 30px;

  border-radius: 8px;

  background: #f1f5f9;

  cursor: pointer;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.icon-btn:hover {
  transform: translateY(-2px);

  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}


/* =========================================================
   卡片模式
   ========================================================= */

.card-wrapper {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;

  display: grid;

  grid-template-columns:
    repeat(
      auto-fill,
      minmax(150px, 1fr)
    );

  align-content: start;

  gap: 16px;

  padding: 4px;

  overflow: auto;

  box-sizing: border-box;
}

.file-card-item {
  width: 100%;
  min-width: 0;
  max-width: 190px;

  height: 150px;

  box-sizing: border-box;

  border: 1px solid #ebeef5;
  border-radius: 4px;

  background: #fff;

  overflow: hidden;

  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.file-card-item:hover {
  transform: translateY(-2px);

  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.file-preview {
  width: 100%;
  height: 120px;

  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;

  overflow: hidden;

  border-bottom: 1px solid #dcdfe6;
}

.folder-icon {
  transform: scale(5);
}

.image-preview {
  width: 100%;
  height: 100%;

  object-fit: cover;

  display: block;
}

.file-icon-preview {
  width: 100%;
  height: 100%;

  display: flex;
  align-items: center;
  justify-content: center;
}

.large-icon {
  transform: scale(5);
}

.video-icon {
  transform: scale(5.5);
}

.show_icon {
  display: none;

  position: absolute;

  left: 50%;
  top: 50%;

  transform: translate(-50%, -50%);

  padding: 6px;

  background: var(--el-color-primary);

  border-radius: 6px;

  white-space: nowrap;

  z-index: 2;
}

.file-card-item:hover .show_icon {
  display: flex;

  align-items: center;

  gap: 4px;
}

.icon_type {
  cursor: pointer;

  margin-right: 4px;

  outline: 0;

  font-size: 18px;
}

.file-card-footer {
  width: 100%;
  height: 30px;

  display: flex;
  align-items: center;

  padding: 0 5px;

  box-sizing: border-box;

  overflow: hidden;
}

.file-type-tag {
  flex-shrink: 0;
}

.file-card-name {
  min-width: 0;

  flex: 1;

  margin-left: 5px;

  overflow: hidden;

  text-overflow: ellipsis;

  white-space: nowrap;

  display: inline-block;
}

.download-icon {
  flex-shrink: 0;

  margin-left: 4px;
}


/* =========================================================
   右键菜单
   ========================================================= */

.contextmenu {
  position: fixed;

  min-width: 100px;
  max-width: 180px;

  z-index: 3000;

  margin: 0;

  padding: 5px 0;

  list-style-type: none;

  background: #fff;

  border: 1px solid #d4d4d5;

  border-radius: 4px;

  font-size: 12px;

  font-weight: 400;

  color: #333;

  box-shadow:
    0 2px 4px rgba(34, 36, 38, 0.12),
    0 2px 10px rgba(34, 36, 38, 0.15);
}

.contextmenu li {
  margin: 0;

  padding: 8px 16px;

  cursor: pointer;

  white-space: nowrap;
}

.contextmenu li:hover {
  background: #eee;
}


/* =========================================================
   文件信息 / 创建目录 Dialog
   ========================================================= */

:deep(.file-info-dialog),
:deep(.dir-dialog) {
  max-width: calc(100vw - 30px);
}

:deep(.file-info-dialog .el-dialog__body),
:deep(.dir-dialog .el-dialog__body) {
  max-height: 70vh;
  max-height: 70dvh;

  overflow: auto;
}


/* =========================================================
   视频 Dialog
   ========================================================= */

/*
 * 普通状态
 * 不设置 width / height，让 el-dialog 自己控制
 */
:deep(.video-dialog) {
  max-width: calc(100vw - 30px);
  box-sizing: border-box;
}

:deep(.video-dialog .el-dialog__header) {
  flex-shrink: 0;
}

:deep(.video-dialog .el-dialog__body) {
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
}


/* =========================================================
   视频主体
   ========================================================= */

.video-container {
  width: 100%;

  height: min(72vh, 800px);
  height: min(72dvh, 800px);

  min-height: 300px;

  display: flex;

  gap: 20px;

  overflow: hidden;
}


/* =========================================================
   播放器
   ========================================================= */

.video-player {
  flex: 1;

  min-width: 0;
  min-height: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  overflow: hidden;

  background: #000;

  border-radius: 8px;
}

.video-player-component {
  width: 100% !important;
  height: 100% !important;

  max-width: 100%;
  max-height: 100%;
}


/* =========================================================
   视频列表
   ========================================================= */

.video-list-container {
  width: min(300px, 22vw);

  min-width: 220px;
  min-height: 0;

  display: flex;
  flex-direction: column;

  overflow: hidden;

  border: 1px solid #ebeef5;
  border-radius: 8px;

  background: #fff;

  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.list-controls {
  flex-shrink: 0;

  display: flex;
  flex-wrap: wrap;

  justify-content: center;
  align-items: center;

  gap: 8px;

  padding: 10px;

  border-bottom: 1px solid #ebeef5;

  background: #f5f7fa;
}

.list-controls .el-button {
  flex: 1 1 70px;

  min-width: 70px;
  max-width: 120px;

  margin-left: 0;

  font-size: 13px;
}

.scroll-container {
  flex: 1;

  min-height: 0;

  height: 0;
}

.video-list {
  list-style: none;

  padding: 0;
  margin: 0;
}

.video-item {
  display: flex;

  min-width: 0;

  padding: 12px;

  cursor: pointer;

  transition: background-color 0.2s;

  border-bottom: 1px solid #f0f0f0;

  box-sizing: border-box;
}

.video-item:last-child {
  border-bottom: none;
}

.video-item:hover {
  background: #f5f7fa;
}

.video-item.active {
  background: #ecf5ff;

  border-left: 3px solid #409eff;
}

.video-info {
  flex: 1;

  min-width: 0;

  padding-left: 4px;
}

.video-title {
  display: flex;

  justify-content: space-between;
  align-items: center;

  gap: 8px;

  min-width: 0;

  font-size: 14px;

  font-weight: 500;

  line-height: 20px;
}

.file-name {
  flex: 1;

  min-width: 0;

  display: flex;

  align-items: center;

  overflow: hidden;

  text-overflow: ellipsis;

  white-space: nowrap;
}

.playing-icon {
  flex-shrink: 0;

  margin-right: 8px;
}

.video-action {
  flex-shrink: 0;
}

.video-duration {
  color: #909399;

  margin-top: 5px;

  display: flex;
  align-items: center;

  gap: 5px;

  font-size: 12px;
}


/* =========================================================
   ★ 电脑端 fullscreen
   ========================================================= */

/*
 * 这里故意不写 width / height。
 *
 * Element Plus 的：
 *
 * :fullscreen="video.fullscreen"
 *
 * 会给 el-dialog 加 fullscreen class，
 * 然后由 Element Plus 自己处理：
 *
 * width: 100%;
 * height: 100%;
 * margin: 0;
 *
 * 这样你原来的 toggleFullscreen() 就不会失效。
 */

:deep(.video-dialog.is-fullscreen) {
  max-width: none;

  margin: 0;

  box-sizing: border-box;
}

:deep(.video-dialog.is-fullscreen .el-dialog__body) {
  min-height: 0;

  overflow: hidden;
}

:deep(.video-dialog.is-fullscreen .video-container) {
  /*
   * fullscreen 后，视频区域占满 Dialog 剩余空间
   */
  height: calc(100vh - 80px);
  height: calc(100dvh - 80px);

  min-height: 0;
}


/* =========================================================
   平板
   ========================================================= */

@media screen and (max-width: 900px) {
  .video-container {
    gap: 10px;
  }

  .video-list-container {
    width: 240px;

    min-width: 200px;
  }

  .video-title {
    font-size: 13px;
  }
}


/* =========================================================
   手机竖屏
   ========================================================= */

@media screen and (max-width: 768px) {
  :deep(.video-dialog) {
    width: calc(100vw - 20px) !important;

    max-width: calc(100vw - 20px);
  }

  .video-container {
    height: min(70vh, 700px);
    height: min(70dvh, 700px);

    min-height: 300px;

    flex-direction: column;
  }

  .video-player {
    flex: 1;

    width: 100%;

    min-height: 200px;
  }

  .video-list-container {
    width: 100%;

    min-width: 0;

    height: 220px;

    flex-shrink: 0;
  }

  /*
   * 手机竖屏 fullscreen
   */
  :deep(.video-dialog.is-fullscreen) {
    width: 100vw !important;

    max-width: 100vw;

    margin: 0;
  }

  :deep(.video-dialog.is-fullscreen .video-container) {
    height: calc(100dvh - 70px);
  }
}


/* =========================================================
   手机横屏
   ========================================================= */

@media screen and (max-width: 900px) and (orientation: landscape) {
  /*
   * 手机横屏普通 Dialog 就直接铺满
   *
   * 注意：
   * 这里只在 max-width: 900px 时生效，
   * 所以不会影响电脑。
   */
  :deep(.video-dialog) {
    width: 100vw !important;

    max-width: 100vw;

    max-height: 100dvh;

    margin: 0 !important;

    border-radius: 0;
  }

  :deep(.video-dialog .el-dialog__header) {
    height: 44px;

    padding: 8px 12px;

    box-sizing: border-box;
  }

  :deep(.video-dialog .el-dialog__body) {
    height: calc(100dvh - 44px);

    padding: 6px 8px 8px;

    box-sizing: border-box;

    overflow: hidden;
  }

  .video-container {
    width: 100%;

    height: 100%;

    min-height: 0;

    display: flex;

    flex-direction: row;

    gap: 8px;
  }

  .video-player {
    flex: 1;

    width: auto;

    min-width: 0;
    min-height: 0;

    height: 100%;
  }

  .video-player-component {
    width: 100% !important;
    height: 100% !important;
  }

  .video-list-container {
    flex: 0 0 30%;

    width: 30%;

    min-width: 180px;
    max-width: 280px;

    height: 100%;

    min-height: 0;
  }

  .list-controls {
    padding: 6px;

    gap: 5px;
  }

  .list-controls .el-button {
    min-width: 55px;

    height: 30px;

    padding: 4px 8px;

    font-size: 12px;
  }

  .video-item {
    padding: 8px;
  }

  .video-title {
    font-size: 12px;

    line-height: 17px;
  }

  .video-duration {
    font-size: 11px;
  }

  .video-action .el-button {
    height: 28px;

    padding: 4px 8px;

    font-size: 12px;
  }
}


/* =========================================================
   手机横屏 fullscreen
   ========================================================= */

@media screen and (max-width: 900px) and (orientation: landscape) {
  :deep(.video-dialog.is-fullscreen) {
    width: 100vw !important;

    max-width: 100vw;

    height: 100dvh;

    max-height: 100dvh;

    margin: 0 !important;

    border-radius: 0;
  }

  :deep(.video-dialog.is-fullscreen .el-dialog__header) {
    height: 44px;
  }

  :deep(.video-dialog.is-fullscreen .el-dialog__body) {
    height: calc(100dvh - 44px);
  }

  :deep(.video-dialog.is-fullscreen .video-container) {
    height: 100%;
  }
}


/* =========================================================
   手机横屏 - 窄设备
   ========================================================= */

@media screen and (max-width: 700px) and (orientation: landscape) {
  :deep(.video-dialog .el-dialog__header) {
    height: 40px;

    padding: 6px 10px;
  }

  :deep(.video-dialog .el-dialog__body) {
    height: calc(100dvh - 40px);

    padding: 4px 6px 6px;
  }

  .video-container {
    gap: 5px;
  }

  .video-list-container {
    flex-basis: 32%;

    width: 32%;

    min-width: 155px;
  }

  .list-controls {
    padding: 4px;

    gap: 3px;
  }

  .list-controls .el-button {
    min-width: 45px;

    padding: 3px 5px;

    font-size: 11px;
  }

  .video-item {
    padding: 6px;
  }

  .video-title {
    font-size: 11px;
  }

  .video-duration {
    font-size: 10px;
  }
}


/* =========================================================
   超小手机横屏
   ========================================================= */

@media screen and (max-width: 550px) and (orientation: landscape) {
  .video-list-container {
    flex-basis: 35%;

    width: 35%;

    min-width: 135px;
  }

  .video-action {
    display: none;
  }

  .video-item {
    padding: 5px;
  }

  .video-title {
    font-size: 10px;

    line-height: 15px;
  }

  .video-duration {
    font-size: 9px;
  }

  .playing-icon {
    margin-right: 4px;
  }
}
</style>
