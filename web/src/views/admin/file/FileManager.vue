<template>
  <div class="file-page">
    <el-card class="file-card">
      <!-- ==================== 顶部工具栏 ==================== -->
      <FileToolbar
        :file-path="filePath"
        :file-path-arr="filePathArr"
        :switch-flag="switchFlag"
        @upload="changeUpload"
        @change-path="changePath"
        @update:switch-flag="switchFlag = $event"
      />

      <el-divider content-position="left">文件目录</el-divider>

      <!-- ==================== 文件内容 ==================== -->
      <div class="file-content" @contextmenu.prevent="openMenu($event)">
        <!-- 列表模式 -->
        <FileList
          v-if="switchFlag"
          :headers="headers"
          :dir-list="dirList.data"
          :file-list="fileList.data"
          @context-menu="handleListContextMenu"
          @dir-context-menu="handleDirContextMenu"
          @file-context-menu="handleFileContextMenu"
          @open-dir="openFileDirFun"
          @delete-dir="deleteFileDirFun"
          @open-file="openFileFun"
          @sync-file="syncFileFun"
          @delete-file="deleteFileFun"
        />

        <!-- 卡片模式 -->
        <FileCard
          v-else
          :dir-list="dirList.data"
          :file-list="fileList.data"
          @context-menu="handleCardContextMenu"
          @dir-context-menu="handleCardDirContextMenu"
          @file-context-menu="handleCardFileContextMenu"
          @open-dir="openFileDirFun"
          @delete-dir="deleteFileDirFun"
          @open-file="openFileFun"
          @show-file-desc="showFileDesc"
          @delete-file="deleteFileFun"
          @move-file="moveFileFun"
        />
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
    <FileContextMenu
      :menu="menu"
      @refresh="refreshDir"
      @upload="changeUpload"
      @create-dir="createDir"
      @back="changePath(-2)"
      @open-dir="openDir"
      @delete-dir="deleteFileDirFun"
      @open-file="openFileFun"
      @file-info="showFileDesc"
      @delete-file="deleteFileFun"
      @close="menu.visible = false"
    />

    <!-- ==================== 文件信息 ==================== -->
    <FileInfoDialog v-model="menu.fileDialog" :file="menu.file" />

    <!-- ==================== 创建目录 ==================== -->
    <FileDirDialog
      v-model="menu.dirDialog"
      :dir-file="menu.dirFile"
      :rules="createFileRules"
      @save="saveFileDirFun"
    />

    <!-- ==================== 视频播放 ==================== -->
    <FileVideoDialog
      :video="video"
      :dir-list="dirList"
      :time-to-min-or-hour="timeToMinOrHour"
      @previous="nextVideo(-1)"
      @next="nextVideo(1)"
      @fullscreen="toggleFullscreen"
      @play-video="playVideo"
      @move-file="moveFileFun"
      @dialog-opened="handleDialogOpened"
      @dialog-open="handleDialogOpen"
      @dialog-close="handleDialogClose"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, reactive, ref } from 'vue'
import { ElImageViewer, ElMessage } from 'element-plus'

import FileToolbar from '@/components/admin/file/FileToolbar.vue'
import FileList from '@/components/admin/file/FileList.vue'
import FileCard from '@/components/admin/file/FileCard.vue'
import FileContextMenu from '@/components/admin/file/FileContextMenu.vue'
import FileInfoDialog from '@/components/admin/file/FileInfoDialog.vue'
import FileDirDialog from '@/components/admin/file/FileDirDialog.vue'
import FileVideoDialog from '@/components/admin/file/FileVideoDialog.vue'

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

import mixin from '@/mixins/fileType'
import timeFormat from '@/utils/timeFormat'
import { fileStore } from '@/store/file'

/* =========================================================
 * 基础工具
 * ========================================================= */

const { timeToMinOrHour } = timeFormat()
const { fileTypeEnum } = mixin()
const fStore = fileStore()

/* =========================================================
 * 页面数据
 * ========================================================= */

const switchFlag = ref(true)

const filePath = ref('/user')
const filePathArr = ref<string[]>(['/user'])

const dirList = reactive<any>({
  data: [],
})

const fileList = reactive<any>({
  data: [],
})

const dialogImageUrl = reactive<any>({
  url: [],
  index: 0,
  show: false,
})

/* =========================================================
 * 右键菜单
 * ========================================================= */

const menu = reactive<any>({
  visible: false,

  left: 0,
  top: 0,

  type: -1,

  fileDialog: false,
  dirDialog: false,

  dirFile: {
    name: '',
    dirType: 1,
  },

  dir: null,
  file: null,
})

/* =========================================================
 * 表格头
 * ========================================================= */

const headers = [
  { title: '文件/目录名称', width: '25%' },
  { title: '文件/目录大小', width: '17%' },
  { title: '文件状态', width: '16%' },
  { title: '创建用户', width: '13%' },
  { title: '创建时间', width: '16%' },
  { title: '操作', width: '13%' },
]

/* =========================================================
 * 创建目录验证规则
 * ========================================================= */

const createFileRules = {
  name: [
    {
      required: true,
      message: '请输入目录名称',
      trigger: 'blur',
    },
  ],
}

/* =========================================================
 * 生命周期
 * ========================================================= */

onMounted(() => {
  // 恢复上次目录
  if (fStore.filePath !== 'null') {
    filePath.value = fStore.filePath
    filePathArr.value = fStore.filePathArr
  }

  // 恢复列表 / 卡片模式
  switchFlag.value = fStore.switchFlag

  // 查询当前目录
  selectFileDirOrFileFun()

  // 返回上一级
  window.addEventListener('keydown', onKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
})

/* =========================================================
 * 键盘操作
 * ========================================================= */

function onKey(e: KeyboardEvent) {
  if (e.key === 'Backspace') {
    changePath(-2)
  }
}

/* =========================================================
 * 右键菜单
 * ========================================================= */

const openMenu = (e: MouseEvent, type: number = 0, row: any = null) => {
  menu.type = type

  if (type === 1) {
    // 目录
    menu.dir = row
  } else if (type === 2) {
    // 文件
    menu.file = row
  }

  menu.visible = true
  menu.left = e.clientX
  menu.top = e.clientY
}

/* =========================================================
 * 列表模式右键菜单
 * ========================================================= */

const handleListContextMenu = (event: MouseEvent) => {
  openMenu(event)
}

const handleDirContextMenu = (event: MouseEvent, row: any) => {
  openMenu(event, 1, row)
}

const handleFileContextMenu = (event: MouseEvent, row: any) => {
  openMenu(event, 2, row)
}

/* =========================================================
 * 卡片模式右键菜单
 * ========================================================= */

const handleCardContextMenu = (event: MouseEvent) => {
  openMenu(event)
}

const handleCardDirContextMenu = (event: MouseEvent, item: any) => {
  openMenu(event, 1, item)
}

const handleCardFileContextMenu = (event: MouseEvent, item: any) => {
  openMenu(event, 2, item)
}

/* =========================================================
 * 文件信息
 * ========================================================= */

const showFileDesc = (item: any) => {
  if (item === null) {
    menu.visible = false
  } else {
    menu.file = item
  }

  menu.fileDialog = true
}

/* =========================================================
 * 创建目录
 * ========================================================= */

const createDir = () => {
  menu.visible = false

  menu.dirFile.name = ''
  menu.dirFile.dirType = 1

  menu.dirDialog = true
}

/**
 * 创建目录
 *
 * FileDirDialog 已经负责表单验证，
 * manager 这里只负责真正提交 API。
 */
const saveFileDirFun = () => {
  saveFileDirApi({
    dirPath: filePath.value,
    dirName: menu.dirFile.name,
    dirType: menu.dirFile.dirType,
  }).then((res: any) => {
    if (res.code === 200) {
      menu.dirFile.name = ''
      menu.dirFile.dirType = 1

      menu.dirDialog = false

      ElMessage.success({
        message: '目录创建成功',
        type: 'success',
      })

      selectFileDirOrFileFun()
    }
  })
}

/* =========================================================
 * 上传文件
 * ========================================================= */

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
      ElMessage.success({
        message: '文件上传成功',
        type: 'success',
      })

      selectFileDirOrFileFun()
    } else {
      ElMessage.error({
        message: res.message,
        type: 'error',
      })
    }
  })
}

/* =========================================================
 * 刷新目录
 * ========================================================= */

const refreshDir = () => {
  menu.visible = false

  selectFileDirOrFileFun()

  ElMessage.success({
    message: '刷新成功',
    type: 'success',
  })
}

/* =========================================================
 * 打开目录
 * ========================================================= */

const openDir = () => {
  menu.visible = false

  openFileDirFun(menu.dir)
}

/* =========================================================
 * 查询当前目录下目录与文件列表
 * ========================================================= */

const selectFileDirOrFileFun = () => {
  const path = filePath.value

  fileList.data = []
  dirList.data = []

  // 根目录不传 dirPath
  const dirParams = path ? { dirPath: path } : {}
  const fileParams = path ? { dirPath: path } : {}

  selectFileDirApi(dirParams).then((res: any) => {
    if (res.code === 200) {
      dirList.data = res.result
    }
  })

  selectFileApi(fileParams).then((res: any) => {
    if (res.code === 200) {
      fileList.data = res.result
    }
  })
}

/* =========================================================
 * 切换目录路径
 *
 * idx:
 * -1 = 根目录
 * -2 = 上一级
 * >=0 = 指定路径
 * ========================================================= */

const changePath = (idx: number) => {
  /* -------------------------
   * 回到根目录
   * ------------------------- */

  if (idx === -1) {
    filePath.value = ''
    filePathArr.value = []

    fStore.filePath = filePath.value
    fStore.filePathArr = filePathArr.value

    selectFileDirOrFileFun()

    return
  }

  /* -------------------------
   * 回到上一级
   * ------------------------- */

  if (idx === -2) {
    if (filePathArr.value.length <= 1) {
      filePath.value = ''
      filePathArr.value = []
    } else {
      const targetIndex = filePathArr.value.length - 2

      filePath.value = ''

      for (let i = 0; i <= targetIndex; i++) {
        filePath.value += filePathArr.value[i]
      }

      filePathArr.value.splice(targetIndex + 1)
    }

    fStore.filePath = filePath.value
    fStore.filePathArr = filePathArr.value

    selectFileDirOrFileFun()

    return
  }

  /* -------------------------
   * 进入指定目录
   * ------------------------- */

  if (idx !== filePathArr.value.length - 1) {
    filePath.value = ''

    for (let i = 0; i <= idx; i++) {
      filePath.value += filePathArr.value[i]
    }

    filePathArr.value.splice(idx + 1)

    fStore.filePath = filePath.value
    fStore.filePathArr = filePathArr.value

    selectFileDirOrFileFun()
  }
}

/* =========================================================
 * 打开目录
 * ========================================================= */

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

/* =========================================================
 * 打开文件
 * ========================================================= */

const openFileFun = (file: any) => {
  const type = fileTypeEnum(file.fileType)

  if (type.key === 1) {
    showImg(file)
  } else if (type.key === 3) {
    playVideo(file)
  } else {
    ElMessage.warning(type.value + ' 类型文件不支持查看')
  }
}

/* =========================================================
 * 图片查看
 * ========================================================= */

const showImg = (img: any) => {
  dialogImageUrl.show = true
  dialogImageUrl.url = []
  dialogImageUrl.index = 0

  const imgList = fileList.data.filter((item: any) => fileTypeEnum(item.fileType).key === 1)

  imgList.forEach((item: any, index: number) => {
    dialogImageUrl.url.push(item.fileUrl)

    if (item.fileUrl === img.fileUrl) {
      dialogImageUrl.index = index
    }
  })
}

/* =========================================================
 * 视频
 * ========================================================= */

function videoFn(): any {
  const video = reactive({
    // Dialog 是否显示
    showVideoDialog: false,

    // 当前视频 URL
    videoUrl: '',

    // 播放器重置
    resetFlag: false,

    // 播放器销毁
    destroy: false,

    // 视频列表
    videoList: [] as any[],

    // 当前视频索引
    currentIndex: 0,

    // 是否全屏
    fullscreen: false,

    videoStyle: {
      videoWidth: '58vw',
      videoHeight: '59vh',
      listHeight: '400px',
    },
  })

  const videoStyle = {
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

  /* -------------------------
   * 打开视频 Dialog
   * ------------------------- */

  const openDialog = () => {
    video.showVideoDialog = true

    nextTick(() => {
      videoPlayerRef.value?.playPlayer?.()
    })
  }

  /* -------------------------
   * 切换全屏
   * ------------------------- */

  const toggleFullscreen = () => {
    video.fullscreen = !video.fullscreen

    video.videoStyle = video.fullscreen ? videoStyle.fullScreen : videoStyle.halfScreen
  }

  /* -------------------------
   * 播放视频
   * ------------------------- */

  const playVideo = (item: any) => {
    video.videoUrl = item.fileUrl

    for (let i = 0; i < video.videoList.length; i++) {
      if (video.videoUrl === video.videoList[i].fileUrl) {
        video.currentIndex = i
        break
      }
    }

    nextTick(() => {
      videoPlayerRef.value?.playPlayer?.()
    })
  }

  /* -------------------------
   * 上一个 / 下一个
   * ------------------------- */

  const nextVideo = (next: number) => {
    if (video.videoList.length === 0) {
      return
    }

    video.currentIndex += next

    if (video.currentIndex >= video.videoList.length) {
      video.currentIndex = video.currentIndex % video.videoList.length
    }

    if (video.currentIndex < 0) {
      video.currentIndex = video.videoList.length - 1
    }

    playVideo(video.videoList[video.currentIndex])
  }

  /* -------------------------
   * Dialog 完全打开
   * ------------------------- */

  const handleDialogOpened = () => {
    // 预留
  }

  /* -------------------------
   * Dialog 开始打开
   * ------------------------- */

  const handleDialogOpen = () => {
    // 预留
  }

  /* -------------------------
   * Dialog 关闭
   * ------------------------- */

  const handleDialogClose = () => {
    video.showVideoDialog = false

    selectFileDirOrFileFun()

    nextTick(() => {
      videoPlayerRef.value?.pausePlayer?.()
    })
  }

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

const {
  video,
  videoPlayerRef,
  openDialog,
  toggleFullscreen,
  playVideo: playVideoDialog,
  nextVideo,
  handleDialogOpened,
  handleDialogOpen,
  handleDialogClose,
} = videoFn()

/* =========================================================
 * 视频播放
 * ========================================================= */

const playVideo = (file: any) => {
  video.videoList = []
  video.videoUrl = file.fileUrl

  // 当前目录所有视频
  for (const item of fileList.data) {
    if (fileTypeEnum(item.fileType).key === 3) {
      video.videoList.push(item)
    }
  }

  // 解析视频信息
  for (let i = 0; i < video.videoList.length; i++) {
    const item = video.videoList[i]

    try {
      const fileJson = JSON.parse(item.fileJson)

      item.videoTime = fileJson.durationSeconds
    } catch {
      item.videoTime = 0
    }

    if (video.videoUrl === item.fileUrl) {
      video.currentIndex = i
    }
  }

  openDialog()
}

/* =========================================================
 * 删除当前视频
 * ========================================================= */

const removeVideo = (file: any) => {
  video.videoList = video.videoList.filter((item: any) => item.id !== file.id)

  if (video.videoList.length === 0) {
    handleDialogClose()
    return
  }

  nextVideo(0)
}

/* =========================================================
 * 删除目录
 * ========================================================= */

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

/* =========================================================
 * 删除文件
 * ========================================================= */

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

/* =========================================================
 * 同步文件
 * ========================================================= */

const syncFileFun = (item: any) => {
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

    return
  }

  if (item.fileStatus === 1 || item.fileStatus === 3) {
    ElMessage.warning('文件正在同步中')
    return
  }

  if (item.fileStatus === 2) {
    ElMessage.warning('文件正在等待数据同步')
    return
  }

  ElMessage.error('文件状态异常')
}

/* =========================================================
 * 移动文件
 * ========================================================= */

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
</script>

<style scoped>
/* =========================================================
 * 页面
 * ========================================================= */

.file-page {
  width: 100%;
  height: 100%;

  min-width: 0;
  min-height: 0;

  overflow: hidden;

  color: #606266;
}

/* =========================================================
 * 主卡片
 * ========================================================= */

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
 * 分割线
 * ========================================================= */

.file-card :deep(.el-divider) {
  flex-shrink: 0;

  margin: 16px 0;
}

/* =========================================================
 * 文件内容区域
 *
 * FileList / FileCard 自己负责内部布局，
 * manager 只负责给它提供可伸缩的容器。
 * ========================================================= */

.file-content {
  flex: 1;

  width: 100%;

  min-width: 0;
  min-height: 0;

  display: flex;
  flex-direction: column;

  overflow: hidden;
}

/* =========================================================
 * 文件信息 / 创建目录 Dialog
 *
 * Dialog 本身由对应子组件负责布局，
 * manager 这里只负责避免小屏溢出。
 * ========================================================= */

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
 * 视频 Dialog
 *
 * 视频内部布局已经交给 FileVideoDialog，
 * manager 不再维护播放器、视频列表等 CSS。
 * ========================================================= */

:deep(.video-dialog) {
  max-width: calc(100vw - 20px);
}

/* =========================================================
 * 手机
 * ========================================================= */

@media screen and (max-width: 768px) {
  .file-card {
    height: 100%;
    min-height: 0;
  }

  :deep(.file-info-dialog),
  :deep(.dir-dialog) {
    width: calc(100vw - 20px) !important;
    max-width: calc(100vw - 20px);
  }
}

/* =========================================================
 * 手机横屏
 *
 * 视频具体横屏布局由 FileVideoDialog 负责。
 * ========================================================= */

@media screen and (max-width: 900px) and (orientation: landscape) {
  :deep(.video-dialog) {
    width: 100vw !important;

    max-width: 100vw;
    max-height: 100dvh;

    margin: 0 !important;

    border-radius: 0;
  }
}
</style>
