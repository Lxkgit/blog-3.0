<template>
  <div>
    <div class="title_style">
      <span>个人云盘</span>
    </div>
    <el-card style="margin: 18px 2%; width: 95%; color: #606266">
      <div
        style="display: flex; font-size: 14px; justify-content: flex-start; align-items: flex-start"
      >
        <el-upload :auto-upload="false" multiple :show-file-list="false" :on-change="changeUpload">
          <el-button type="success" size="small" text>上传文件</el-button>
        </el-upload>
        <div style="line-height: 23px; margin-left: 20px">
          <MyIcon
            :style="[filePath === '' ? { 'pointer-events': 'none' } : { cursor: 'pointer' }]"
            style="margin-right: 20px; outline: 0"
            type="icon-shangyibu"
            @click="changePath(-2)"
            title="返回上一级"
          />
          <span style="">当前路径：&nbsp;</span>
          <div style="display: inline; margin-left: 10px">
            <span class="file_path" style="cursor: pointer" @click="changePath(-1)">根目录</span>
            <div
              v-for="(item, idx) in filePathArr"
              style="display: inline; margin-left: 5px"
              :key="idx"
            >
              <span class="file_path" style="cursor: pointer" @click="changePath(idx)">
                {{ item }}
              </span>
            </div>
          </div>
        </div>
        <div style="margin-left: auto; margin-right: 20px">
          <el-switch
            v-model="switchFlag"
            class="mb-2"
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            active-text="列表"
            inactive-text="卡片"
          />
        </div>
      </div>
      <el-divider content-position="left">文件目录</el-divider>
      <div
        style="
          display: flex;
          flex-wrap: wrap;
          align-content: flex-start;
          overflow: auto;
          margin: 10px;
          height: calc(100vh - 335px);
        "
        @contextmenu.prevent="openMenu($event)"
      >
        <div style="display: flex">
          <div v-if="switchFlag" style="width: 77vw" @contextmenu.prevent.stop="openMenu($event)">
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
            <ul style="height: calc(100vh - 380px); overflow: auto">
              <!-- 目录数据行 -->
              <ul v-for="(row, rowIndex) in dirList.data" :key="rowIndex" class="table-row">
                <div
                  style="display: flex; width: 100%; position: relative; cursor: pointer"
                  @contextmenu.prevent.stop="openMenu($event, 1, row)"
                  @dblclick="openFileDirFun(row)"
                >
                  <li class="data-item" :style="{ width: headers[0].width }">
                    <MyIcon type="icon-folder" /> {{ row.dirName }}
                  </li>
                  <li
                    class="data-item"
                    style="text-indent: 10px"
                    :style="{ width: headers[1].width }"
                  >
                    {{ fileSizeConvert(row.occupySpace).value }}
                  </li>
                  <li class="data-item" :style="{ width: headers[2].width }">目录</li>
                  <li class="data-item" :style="{ width: headers[3].width }">
                    {{ row.createBy }}
                  </li>
                  <li class="data-item" :style="{ width: headers[4].width }">
                    {{ row.createTime }}
                  </li>
                  <li class="data-item" :style="{ width: headers[5].width }">
                    <MyIcon
                      class="icon-btn"
                      type="icon-file-open"
                      @click="openFileDirFun(row)"
                      title="打开文件夹"
                    />
                    <MyIcon
                      class="icon-btn"
                      type="icon-delete"
                      @click="deleteFileDirFun(row)"
                      title="删除文件夹"
                    />
                  </li>
                </div>
              </ul>
              <!-- 文件数据行 -->
              <ul v-for="(row, rowIndex) in fileList.data" :key="rowIndex" class="table-row">
                <div
                  style="display: flex; width: 100%; position: relative; cursor: pointer"
                  @contextmenu.prevent.stop="openMenu($event, 2, row)"
                  @dblclick="openFileFun(row)"
                >
                  <li class="data-item" :style="{ width: headers[0].width }" :title="row.fileName">
                    <MyIcon v-if="fileTypeEnum(row.fileType).key === 1" type="icon-img" />
                    <MyIcon v-else-if="fileTypeEnum(row.fileType).key === 2" type="icon-zip" />
                    <MyIcon v-else-if="fileTypeEnum(row.fileType).key === 3" type="icon-video" />
                    <MyIcon v-else type="icon-file" />
                    {{ row.fileName }}
                  </li>
                  <li
                    class="data-item"
                    style="text-indent: 10px"
                    :style="{ width: headers[1].width }"
                  >
                    {{ fileSizeConvert(row.fileSize).value }}
                  </li>
                  <li class="data-item" :style="{ width: headers[2].width }">
                    {{ fileStatusEnum(row.fileStatus).value }}
                  </li>
                  <li class="data-item" :style="{ width: headers[3].width }">
                    {{ row.createBy }}
                  </li>
                  <li class="data-item" :style="{ width: headers[4].width }">
                    {{ row.createTime }}
                  </li>
                  <li class="data-item" :style="{ width: headers[5].width }">
                    <MyIcon
                      class="icon-btn"
                      title="预览"
                      type="icon-eye"
                      @click="openFileFun(row)"
                    ></MyIcon>
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
            </ul>
          </div>
          <div v-else style="display: flex; width: 79vw; flex-wrap: wrap">
            <ul v-for="item in dirList.data">
              <el-card
                :body-style="{ padding: '0px' }"
                shadow="hover"
                style="width: 155px; height: 150px; margin-bottom: 25px; margin-right: 10px"
                @contextmenu.prevent.stop="openMenu($event, item)"
                :key="item.id"
              >
                <div
                  style="
                    width: 159px;
                    height: 125px;
                    border-bottom: 1px solid #dcdfe6;
                    position: relative;
                  "
                  class="file_item"
                >
                  <div
                    style="
                      height: 125px;
                      cursor: pointer;
                      display: flex;
                      flex-direction: column;
                      justify-content: center;
                    "
                    @dblclick="openFileDirFun(item)"
                  >
                    <MyIcon style="transform: scale(6)" type="icon-folder" />

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
                </div>
                <div style="display: flex; padding-left: 5px; padding-top: 2px">
                  <el-tag class="mx-1" size="small" style=""> 目录 </el-tag>
                  <span
                    :title="item.dirName"
                    style="
                      margin-left: 5px;
                      width: 90px;
                      height: 20px;
                      overflow: hidden;
                      text-overflow: ellipsis;
                      -o-text-overflow: ellipsis;
                      white-space: nowrap;
                      display: inline-block;
                    "
                    >{{ item.dirName }}</span
                  >
                </div>
              </el-card>
            </ul>
            <ul v-for="item in fileList.data">
              <el-card
                :body-style="{ padding: '0px' }"
                shadow="hover"
                style="width: 155px; height: 150px; margin-bottom: 25px; margin-right: 10px"
                @contextmenu.prevent.stop="openMenu($event, item)"
                :key="item.id"
              >
                <div
                  style="
                    width: 159px;
                    height: 125px;
                    border-bottom: 1px solid #dcdfe6;
                    position: relative;
                  "
                  class="file_item"
                >
                  <div style="height: 125px; display: flex; align-items: center">
                    <img
                      v-if="fileTypeEnum(item.fileType).key === 1"
                      :src="item.fileUrl"
                      class="image"
                      style="width: 100%; height: 100%; object-fit: cover"
                      loading="lazy"
                    />
                    <div
                      v-else-if="fileTypeEnum(item.fileType).key === 2"
                      style="
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        width: 100%;
                        height: 100%;
                      "
                    >
                      <MyIcon
                        :title="fileTypeEnum(item.fileType).value"
                        style="transform: scale(5)"
                        type="icon-zip"
                      />
                    </div>
                    <div
                      v-else-if="fileTypeEnum(item.fileType).key === 3"
                      style="
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        width: 100%;
                        height: 100%;
                      "
                    >
                      <MyIcon
                        :title="fileTypeEnum(item.fileType).value"
                        style="transform: scale(6)"
                        type="icon-video"
                      />
                    </div>
                    <div
                      v-else
                      style="
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        width: 100%;
                        height: 100%;
                      "
                    >
                      <MyIcon
                        :title="fileTypeEnum(item.fileType).value"
                        style="transform: scale(5)"
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
                    </div>
                  </div>
                </div>
                <div style="display: flex; padding-left: 5px; padding-top: 2px">
                  <el-tag class="mx-1" size="small" style="">
                    {{ fileTypeEnum(item.fileType).value }}
                  </el-tag>
                  <span
                    :title="item.fileName"
                    style="
                      margin-left: 5px;
                      width: 90px;
                      height: 20px;
                      overflow: hidden;
                      text-overflow: ellipsis;
                      -o-text-overflow: ellipsis;
                      white-space: nowrap;
                      display: inline-block;
                    "
                    >{{ item.fileName }}</span
                  >
                  <MyIcon v-if="item.type !== 0" type="icon-download" style="line-height: 21px" />
                </div>
              </el-card>
            </ul>
          </div>
        </div>
      </div>
    </el-card>
    <el-image-viewer
      v-if="dialogImageUrl.show"
      :initial-index="dialogImageUrl.index"
      :index="dialogImageUrl.index"
      :url-list="dialogImageUrl.url"
      :hide-on-click-modal="true"
      :show-progress="true"
      @close="dialogImageUrl.show = false"
    >
    </el-image-viewer>
    <ul
      v-if="menu.visible"
      :style="{ left: menu.left + 'px', top: menu.top + 'px' }"
      class="contextmenu"
    >
      <li @click="refreshDir()">刷新</li>
      <li v-if="menu.type === 0">
        <el-upload
          :auto-upload="false"
          multiple
          :show-file-list="false"
          @click="menu.visible = false"
          :on-change="changeUpload"
          style="padding: 0"
        >
          上传文件
        </el-upload>
      </li>
      <li v-if="menu.type === 0" @click="createDir()">创建目录</li>
      <li v-if="menu.type === 0" @click="changePath(-2)">返回上一级</li>
      <li v-if="menu.type === 1" @click="openDir()">打开目录</li>
      <li v-if="menu.type === 1" @click="">查看目录信息</li>
      <li v-if="menu.type === 1" @click="deleteFileDirFun()">删除目录</li>
      <li v-if="menu.type === 2" @click="">打开文件</li>
      <li v-if="menu.type === 2" @click="showFileDesc(null)">查看文件信息</li>
      <li v-if="menu.type === 2" @click="">删除文件</li>
      <li @click="menu.visible = false">关闭菜单</li>
    </ul>
    <el-dialog v-model="menu.fileDialog" width="40%">
      <el-form :model="menu.file" label-width="100px">
        <el-form-item label="文件名称: ">
          <el-input v-model="menu.file.fileName" size="small" />
        </el-form-item>
        <el-form-item v-if="menu.file.type === 0" label="目录大小: ">
          <el-input v-model="menu.file.fileSize" size="small" />
        </el-form-item>
        <el-form-item v-else label="文件大小: ">
          <el-input v-model="menu.file.fileSize" size="small" />
        </el-form-item>
      </el-form>
    </el-dialog>
    <el-dialog v-model="menu.dirDialog" width="40%">
      <el-form
        :model="menu.dirFile"
        label-width="100px"
        ref="createFileFormRef"
        :rules="createFileRules"
        label-position="left"
      >
        <el-form-item label="目录名称: " prop="name">
          <el-input v-model="menu.dirFile.name" size="small" />
        </el-form-item>
        <el-form-item label="目录类型: " prop="dirType">
          <el-radio-group v-model="menu.dirFile.dirType">
            <el-radio border :label="1">
              普通目录
              <el-tooltip content="普通目录" placement="top" @click.stop.prevent>
                <MyIcon type="icon-wenhaofill" />
              </el-tooltip>
            </el-radio>
            <el-radio border :label="2">
              评分目录
              <el-tooltip content="用于对文件质量分类存放" placement="top" @click.stop.prevent>
                <MyIcon type="icon-wenhaofill" /> </el-tooltip
            ></el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="saveFileDirFun">创建</el-button>
          <el-button @click="menu.dirDialog = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog
      v-model="video.showVideoDialog"
      :close-on-click-modal="false"
      title="视频播放"
      width="80vw"
      @opened="handleDialogOpened"
      @open="handleDialogOpen"
      @close="handleDialogClose"
      :fullscreen="video.fullscreen"
    >
      <div class="video-container">
        <!-- 左侧视频播放器 -->
        <div class="video-player">
          <VideoPlayer
            ref="videoPlayerRef"
            style="margin-top: 10px"
            :video-src="video.videoUrl"
            :reset-on-load="video.resetFlag"
            :destroy-player="video.destroy"
            :style="{ width: video.videoStyle.videoWidth, height: video.videoStyle.videoHeight }"
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
            <el-button type="primary" @click="toggleFullscreen()">
              <MyIcon v-if="video.fullscreen" title="网页全屏" type="icon-half-screen" />
              <MyIcon v-else title="网页全屏" type="icon-full-screen" />
            </el-button>
          </div>

          <el-scrollbar class="scroll-container">
            <ul class="video-list" :style="{ height: video.videoStyle.listHeight }">
              <li
                v-for="(item, index) in video.videoList"
                :key="index"
                :class="['video-item', { active: video.currentIndex === index }]"
                @click="playVideo(item)"
              >
                <div class="video-info" v-if="index === video.currentIndex">
                  <div class="video-title">
                    <div class="file-name" :title="item.fileName">
                      <MyIcon title="视频正在播放" type="icon-playing" style="margin-right: 10px" />
                      {{ item.fileName }}
                    </div>
                    <div class="video-action">
                      <el-dropdown>
                        <el-button type="primary">移动</el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item
                              v-for="(dir, rowIndex) in dirList.data"
                              :key="rowIndex"
                              @click="moveFileFun(dir, item)"
                            >
                              {{ dir.dirName }}
                            </el-dropdown-item>
                            <el-dropdown-item @click="moveFileFun(null, item)">
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
                <div class="video-info" v-else>
                  <div class="video-title">
                    <div>{{ item.fileName }}</div>
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
import { onMounted, onBeforeUnmount, ref, reactive, nextTick } from 'vue'
import mixin from '@/mixins/fileType'
import { ElImageViewer } from 'element-plus'
import { ElMessage } from 'element-plus'
import timeFormat from '@/utils/timeFormat'

let { timeToMinOrHour } = timeFormat()

let {
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
  selectFileDirOrFileFun()
})

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

  const videoPlayerRef = ref(null)

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
    // dirType: [{ required: true, message: '请选择目录类型', trigger: 'blur' }],
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
    } else if (idx === -2) {
      // 回到上一级
      if (filePathArr.value.length <= 1) {
        filePath.value = null
        filePathArr.value = []
      } else {
        idx = filePathArr.value.length - 2
        filePath.value = ''
        for (let i = 0; i <= idx; i++) {
          filePath.value += filePathArr.value[i]
        }
        filePathArr.value.splice(idx + 1)
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
      }
    }
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
    video.videoList = video.videoList.filter((v) => v.id !== file.id)
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
        ElMessage.success('文件删除成功')
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
.title_style {
  display: flex;
  justify-content: flex-start;
  align-items: baseline;
  max-height: 31px;
  color: #445160;
  font-size: 24px;
  font-weight: 600;
  text-align: left;
}

.contextmenu {
  position: fixed;
  min-width: min-content;
  z-index: 1900;
  border: 1px solid #d4d4d5;
  line-height: 1.4285em;
  max-width: 150px;
  background: #fff;
  font-weight: 400;
  font-style: normal;
  color: rgba(0, 0, 0, 0.87);
  border-radius: 0.28571429rem;
  box-shadow:
    0 2px 4px 0 rgba(34, 36, 38, 0.12),
    0 2px 10px 0 rgba(34, 36, 38, 0.15);
}

.contextmenu div {
  position: relative;
  vertical-align: middle;
  line-height: 1;
  -webkit-tap-highlight-color: transparent;
  padding: 10px 15px;
  color: rgba(0, 0, 0, 0.87);
  font-size: 14px;
  cursor: pointer;
}

.contextmenu div:hover {
  background: #eee;
}

.file_path:hover {
  color: var(--el-color-primary);
}

.show_icon {
  background-color: var(--el-color-primary);
  display: none;
  margin-left: 35px;
  position: absolute;
  font-size: 20px;
}

.file_item:hover .show_icon {
  display: inline;
}

.icon_type {
  cursor: pointer;
  margin-right: 5px;
  outline: 0;
}

.contextmenu {
  margin: 0;
  background: #fff;
  z-index: 3000;
  position: absolute;
  list-style-type: none;
  padding: 5px 0;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #333;
  box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, 0.3);
}

.contextmenu li {
  margin: 0;
  padding: 7px 16px;
  cursor: pointer;
}

.contextmenu li:hover {
  background: #eee;
}

/* 核心样式 */
.table-container {
  width: 100%;
  min-width: 1200px;
  margin: 0 auto;
}

/* 表格行通用样式 */
.table-header,
.table-row {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
  padding-left: 10px;
  box-sizing: border-box;
  /* 关键对齐属性 */
}

/* 表头样式 */
.table-header {
  background-color: #f8f9fa;
  font-weight: 600;
  border-bottom: 2px solid #dee2e6;
}

/* 单元格通用样式 */
.header-item,
.data-item {
  padding-top: 12px;
  padding-bottom: 12px;
  min-height: 15px;
  display: flex;
  align-items: center;
  overflow: hidden;
  /* 处理超长内容 */
  white-space: nowrap;
  text-overflow: ellipsis;
}

.icon-btn {
  margin-left: 10px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  border-radius: 8px;
  background: #f1f5f9;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  color: #64748b;
  font-size: 18px;
}

/* 悬停效果 */
.icon-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.icon-btn.icon-eye:hover {
  background: #dbeafe;
  color: #3b82f6;
}

.icon-btn.icon-refresh:hover {
  background: #f0fdf4;
  color: #22c55e;
  animation: spin 0.5s linear;
}

.icon-btn.icon-delete:hover {
  background: #fee2e2;
  color: #ef4444;
}

/* 悬停提示 */
.icon-btn::after {
  content: attr(data-tooltip);
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 0.8rem;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
}

.icon-btn:hover::after {
  opacity: 1;
}

/* 列宽控制逻辑 */
.header-item {
  flex-shrink: 0;
  /* 禁止宽度压缩 */
}

.header-item[style*='auto'] {
  flex: 1;
  /* 自动填充列 */
}

/* 斑马纹效果 */
.table-row:nth-child(even) {
  background-color: #f8f9fa;
}

/* 悬停效果 */
.table-row:hover {
  background-color: #e9ecef;
}

.video-container {
  display: flex;
  height: 100%;
  gap: 20px;
}

.video-player {
  flex: 1;
  min-width: 0;
}

.player {
  width: 100%;
  height: 100%;
  background: #000;
  border-radius: 8px;
}

.video-list-container {
  width: 20vw;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.list-controls {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
}

.list-controls .el-button {
  flex: 1 1 auto;
  min-width: 70px;
  max-width: 120px;
  font-size: clamp(12px, 2vw, 14px);
  padding: 6px 0;
}

.scroll-container {
  flex: 1;
  height: 0;
}

.video-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.video-item {
  display: flex;
  padding: 12px;
  cursor: pointer;
  transition: background 0.3s;
  border-bottom: 1px solid #f0f0f0;
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

.video-thumb {
  width: 120px;
  height: 68px;
  border-radius: 4px;
  overflow: hidden;
  background: #000;
  flex-shrink: 0;
}

.video-thumb video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-info {
  flex: 1;
  padding-left: 12px;
  min-width: 0;
}

.video-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
}

.video-title .file-name {
  display: flex;
  align-items: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: calc(100% - 120px); /* 预留右侧按钮宽度 */
  cursor: default; /* 鼠标悬停显示 tooltip */
}

.video-title .video-action {
  flex-shrink: 0; /* 保证按钮不被压缩 */
}

.dir-icon {
  margin-left: 10px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  font-size: 18px;
}

.dir-icon .my-icon {
  opacity: 0;
}

/* 核心效果 - 悬停时显示目录图标 */
.video-item:hover .dir-icon .my-icon {
  opacity: 1; /* 显示图标 */
  transform: scale(1);
}

.video-item:hover .dir-icon {
  background-color: #759ad4;
}

.video-duration {
  color: #b0b0ff;
  padding-left: 10px;
}
</style>
