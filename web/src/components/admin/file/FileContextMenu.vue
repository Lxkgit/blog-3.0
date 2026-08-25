<template>
  <ul
    v-if="menu.visible"
    :style="{
      left: menu.left + 'px',
      top: menu.top + 'px',
    }"
    class="contextmenu"
  >
    <!-- ==================== 通用 ==================== -->

    <!-- 刷新 -->
    <li @click="handleRefresh">刷新</li>

    <!-- ==================== 当前目录 ==================== -->

    <!-- 上传文件 -->
    <li v-if="menu.type === 0">
      <el-upload
        :auto-upload="false"
        multiple
        :show-file-list="false"
        style="padding: 0"
        :on-change="handleUpload"
      >
        上传文件
      </el-upload>
    </li>

    <!-- 创建目录 -->
    <li v-if="menu.type === 0" @click="handleCreateDir">创建目录</li>

    <!-- 返回上一级 -->
    <li v-if="menu.type === 0" @click="handleBack">返回上一级</li>

    <!-- ==================== 目录 ==================== -->

    <!-- 打开目录 -->
    <li v-if="menu.type === 1" @click="handleOpenDir">打开目录</li>

    <!-- 查看目录信息 -->
    <li v-if="menu.type === 1" @click="handleDirInfo">查看目录信息</li>

    <!-- 删除目录 -->
    <li v-if="menu.type === 1" @click="handleDeleteDir">删除目录</li>

    <!-- ==================== 文件 ==================== -->

    <!-- 打开文件 -->
    <li v-if="menu.type === 2" @click="handleOpenFile">打开文件</li>

    <!-- 查看文件信息 -->
    <li v-if="menu.type === 2" @click="handleFileInfo">查看文件信息</li>

    <!-- 删除文件 -->
    <li v-if="menu.type === 2" @click="handleDeleteFile">删除文件</li>

    <!-- ==================== 关闭 ==================== -->

    <li @click="closeMenu">关闭菜单</li>
  </ul>
</template>

<script setup lang="ts">
import type { UploadFile, UploadFiles } from 'element-plus'

/**
 * 菜单数据
 *
 * dir / file 是 manager 中 openMenu
 * 保存的当前操作对象。
 */
interface Menu {
  visible: boolean
  left: number
  top: number
  type: number
  dir?: any
  file?: any
}

/**
 * Props
 */
interface Props {
  menu: Menu
}

const props = defineProps<Props>()

/**
 * 事件
 */
const emit = defineEmits<{
  refresh: []

  upload: [file: UploadFile, fileList: UploadFiles]

  createDir: []

  back: []

  openDir: []

  dirInfo: []

  deleteDir: [item: any]

  openFile: [item: any]

  fileInfo: [item: any]

  deleteFile: [item: any]

  close: []
}>()

/**
 * =========================================================
 * 菜单操作
 * =========================================================
 */

/**
 * 刷新
 */
const handleRefresh = () => {
  emit('refresh')
  closeMenu()
}

/**
 * 上传文件
 */
const handleUpload = (file: UploadFile, fileList: UploadFiles) => {
  emit('upload', file, fileList)
  closeMenu()
}

/**
 * 创建目录
 */
const handleCreateDir = () => {
  emit('createDir')
  closeMenu()
}

/**
 * 返回上一级
 */
const handleBack = () => {
  emit('back')
  closeMenu()
}

/**
 * 打开目录
 */
const handleOpenDir = () => {
  emit('openDir')
  closeMenu()
}

/**
 * 查看目录信息
 */
const handleDirInfo = () => {
  emit('dirInfo')
  closeMenu()
}

/**
 * 删除目录
 *
 * 当前目录对象由 manager 保存在 menu.dir
 */
const handleDeleteDir = () => {
  emit('deleteDir', props.menu.dir)
  closeMenu()
}

/**
 * 打开文件
 *
 * 当前文件对象由 manager 保存在 menu.file
 */
const handleOpenFile = () => {
  emit('openFile', props.menu.file)
  closeMenu()
}

/**
 * 查看文件信息
 */
const handleFileInfo = () => {
  emit('fileInfo', props.menu.file)
  closeMenu()
}

/**
 * 删除文件
 */
const handleDeleteFile = () => {
  emit('deleteFile', props.menu.file)
  closeMenu()
}

/**
 * 关闭菜单
 */
const closeMenu = () => {
  emit('close')
}
</script>

<style scoped>
.contextmenu {
  position: fixed;
  z-index: 9999;

  min-width: 140px;

  margin: 0;
  padding: 5px 0;

  list-style: none;

  background: #ffffff;

  border: 1px solid #ebeef5;
  border-radius: 4px;

  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.contextmenu li {
  height: 36px;

  padding: 0 15px;

  line-height: 36px;

  font-size: 14px;
  color: #606266;

  cursor: pointer;

  white-space: nowrap;
}

.contextmenu li:hover {
  background-color: #f5f7fa;
  color: #409eff;
}
</style>
