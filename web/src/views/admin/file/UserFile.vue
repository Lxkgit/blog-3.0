<template>
  <div>
    <div class="title_style">
      <span>个人云盘</span>
    </div>
    <el-card style="margin: 18px 2%; width: 95%; color: #606266">
      <div style="display: flex; font-size: 14px">
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
      </div>
      <el-divider content-position="left">文件目录</el-divider>
      <div
        style="
          display: flex;
          flex-wrap: wrap;
          align-content: flex-start;
          height: 630px;
          overflow: auto;
          margin: 10px;
          height: calc(100vh - 330px);
        "
        @contextmenu.prevent="openMenu($event)"
      >
        <div style="display: flex">
          <div v-if="true" style="width: 79vw">
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
            <!-- 目录数据行 -->
            <ul v-for="(row, rowIndex) in dirList.data" :key="rowIndex" class="table-row">
              <div style="display: flex; width: 100%; position: relative; cursor: pointer;" @contextmenu.prevent.stop="openMenu($event, 1, row)" @dblclick="openFileDirFun(row)">
                <li class="data-item" :style="{ width: headers[0].width }" >
                  <MyIcon type="icon-user" /> {{ row.dirName }}
                </li>
                <li class="data-item" :style="{ width: headers[1].width }">
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
                  <MyIcon type="icon-user" /> <MyIcon type="icon-user" />
                  <MyIcon type="icon-delete" @click="deleteFileDirFun(row)"/>
                </li>
              </div>
            </ul>
            <!-- 文件数据行 -->
            <ul v-for="(row, rowIndex) in fileList.data" :key="rowIndex" class="table-row">
              <div style="display: flex; width: 100%; position: relative; cursor: pointer;" @contextmenu.prevent.stop="openMenu($event, 2, row)">
                <li class="data-item" :style="{ width: headers[0].width }">
                  <MyIcon type="icon-edit" /> {{ row.fileName }}
                </li>
                <li class="data-item" :style="{ width: headers[1].width }">
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
                  <MyIcon type="icon-eye" @click="showImg(row)"></MyIcon>
                  <MyIcon type="icon-user" />
                  <MyIcon type="icon-delete" @click="deleteFileFun(row)"/>
                </li>
              </div>
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
                  <div style="height: 125px; cursor: pointer" @dblclick="openFileDirFun(item)">
                    本地目录
                    <div class="show_icon">
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
                  <div
                    v-if="fileTypeEnum(item.fileType).key === 1"
                    style="
                      height: 125px;
                      display: flex;
                      justify-content: space-between;
                      align-items: center;
                    "
                  >
                    <img
                      :src="item.fileUrl"
                      class="image"
                      style="width: 100%; height: 100%; object-fit: cover"
                      loading="lazy"
                    />
                    <div class="show_icon">
                      <MyIcon
                        title="预览"
                        class="icon_type"
                        type="icon-search"
                        @click="showImg(item)"
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
                  <div v-else-if="fileTypeEnum(item.fileType).key === 2" style="height: 125px">
                    <div class="show_icon">
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
                    压缩文件
                  </div>
                  <div v-else style="height: 125px">
                    <div class="show_icon">
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
                    其它类型文件
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
      <li v-if="menu.type === 0" @click="deleteFileDirFun()">删除目录</li>
      <li v-if="menu.type === 1">打开目录</li>
      <li v-if="menu.type === 2" @click="showFileDesc(null)">查看文件信息</li>
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
            <el-radio border :label="0">
              本地
              <el-tooltip content="文件存放本地服务器" placement="top" @click.stop.prevent>
                <MyIcon type="icon-wenhaofill" />
              </el-tooltip>
            </el-radio>
            <el-radio border :label="1">
              同步
              <el-tooltip content="文件存放远程树莓派" placement="top" @click.stop.prevent>
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
  </div>
</template>

<script setup lang="ts">
import {
  selectFileDirApi,
  selectFileApi,
  deleteFileDirApi,
  deleteFileApi,
  uploadApi,
  saveFileDirApi,
  syncFileApi,
} from '@/api/file'
import icon from '@/utils/icon'
import { onMounted, ref, reactive } from 'vue'
import mixin from '@/mixins/fileType'
import { ElImageViewer } from 'element-plus'
import { ElMessage } from 'element-plus'

let {
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
  selectFileDirOrFileFun,
  changePath,
  openFileDirFun,
  showImg,
  deleteFileDirFun,
  deleteFileFun,
  syncFileFun,
} = fileFn()

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


/**
 * 文件云盘接口合集
 */
function fileFn(): any {
  // 创建目录表单
  const createFileFormRef: any = ref(null)

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
    file: null,
  })
  /**
   * 打开菜单
   */
  const openMenu = (e: any, type?:any, row?: any) => {
    if(type === 1) {
      // 打开目录
      menu.file = row
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
            dirName: menu.dirFile.name
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
  }

  const createDir = () => {
    menu.dirDialog = true
    menu.visible = false
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
        filePathArr.value.splice(idx+1)
      }
      selectFileDirOrFileFun()
    } else {
      // 进入指定目录
      if (idx !== filePathArr.value.length - 1) {
        filePath.value = ''
        for (let i = 0; i <= idx; i++) {
          filePath.value += filePathArr.value[i]
        }
        filePathArr.value.splice(idx+1)
        selectFileDirOrFileFun()
      }
    }
  }

  /**
   * 打开文件目录
   */
  const openFileDirFun = (dir: any) => {
    filePathArr.value.push("/" + dir.dirName)

    if(filePath.value !== null) {
      filePath.value += '/' + dir.dirName
    } else {
      filePath.value = '/' + dir.dirName
    }
    selectFileDirOrFileFun()
  }

  /**
   * 展示图片
   */
  const showImg = (img: any) => {
    dialogImageUrl.show = true
    dialogImageUrl.url = []
    for (let i = 0; i < fileList.data.length; i++) {
      if (fileTypeEnum(fileList.data[i].fileType).key === 1) {
        if (fileList.data[i].fileUrl === img.fileUrl) {
          dialogImageUrl.index = i
        }
        dialogImageUrl.url.push(fileList.data[i].fileUrl)
      }
    }
  }

  /**
   * 删除目录
   */
  const deleteFileDirFun = (item: any) => {
    deleteFileDirApi({
      dirPath: filePath.value,
      dirName: item.dirName
    }).then((res: any) => {
      console.log(res)
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
      dirPath: filePath.value,
      fileName: item.fileName,
      id: item.id,
    }).then((res: any) => {

      if (res.code === 200) {
        ElMessage.success('文件删除成功')
        selectFileDirOrFileFun()
      }
    })
  }

  const syncFileFun = (item: any) => {
    syncFileApi({
      id: item.id,
      filePath: filePath.value,
      type: item.type,
      dirType: item.dirType,
      name: item.name,
      syncType: 1,
      fileCode: '1111',
    }).then((res: any) => {})
  }

  return {
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
    selectFileDirOrFileFun,
    changePath,
    openFileDirFun,
    showImg,
    deleteFileDirFun,
    deleteFileFun,
    syncFileFun,
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
  display: none;
  position: absolute;
  left: 40px;
  top: 50px;
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
  box-sizing: border-box; /* 关键对齐属性 */
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
  overflow: hidden; /* 处理超长内容 */
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 列宽控制逻辑 */
.header-item {
  flex-shrink: 0; /* 禁止宽度压缩 */
}
.header-item[style*='auto'] {
  flex: 1; /* 自动填充列 */
}

/* 斑马纹效果 */
.table-row:nth-child(even) {
  background-color: #f8f9fa;
}

/* 悬停效果 */
.table-row:hover {
  background-color: #e9ecef;
}
</style>
