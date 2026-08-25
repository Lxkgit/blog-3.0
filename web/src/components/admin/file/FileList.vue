<template>
  <div class="list-wrapper" @contextmenu.prevent.stop="handleEmptyContextMenu">
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
      <!-- ==================== 目录 ==================== -->
      <ul v-for="(row, rowIndex) in dirList" :key="'dir-' + rowIndex" class="table-row">
        <div
          class="table-row-inner"
          @contextmenu.prevent.stop="handleDirContextMenu($event, row)"
          @dblclick="handleOpenDir(row)"
        >
          <!-- 名称 -->
          <li class="data-item name-item" :style="{ width: headers[0].width }" :title="row.dirName">
            <MyIcon type="icon-folder" />

            <span class="file-name-text">
              {{ row.dirName }}
            </span>
          </li>

          <!-- 大小 -->
          <li class="data-item" :style="{ width: headers[1].width }">
            {{ fileSizeConvert(row.occupySpace).value }}
          </li>

          <!-- 状态 -->
          <li class="data-item" :style="{ width: headers[2].width }">目录</li>

          <!-- 创建用户 -->
          <li class="data-item" :style="{ width: headers[3].width }">
            {{ row.createBy }}
          </li>

          <!-- 创建时间 -->
          <li class="data-item" :style="{ width: headers[4].width }">
            {{ row.createTime }}
          </li>

          <!-- 操作 -->
          <li class="data-item action-item" :style="{ width: headers[5].width }">
            <MyIcon
              class="icon-btn"
              type="icon-file-open"
              title="打开文件夹"
              @click.stop="handleOpenDir(row)"
            />

            <MyIcon
              class="icon-btn"
              type="icon-delete"
              title="删除文件夹"
              @click.stop="handleDeleteDir(row)"
            />
          </li>
        </div>
      </ul>

      <!-- ==================== 文件 ==================== -->
      <ul v-for="(row, rowIndex) in fileList" :key="'file-' + rowIndex" class="table-row">
        <div
          class="table-row-inner"
          @contextmenu.prevent.stop="handleFileContextMenu($event, row)"
          @dblclick="handleOpenFile(row)"
        >
          <!-- 名称 -->
          <li
            class="data-item name-item"
            :style="{ width: headers[0].width }"
            :title="row.fileName"
          >
            <MyIcon v-if="fileTypeEnum(row.fileType).key === 1" type="icon-img" />

            <MyIcon v-else-if="fileTypeEnum(row.fileType).key === 2" type="icon-zip" />

            <MyIcon v-else-if="fileTypeEnum(row.fileType).key === 3" type="icon-video" />

            <MyIcon v-else type="icon-file" />

            <span class="file-name-text">
              {{ row.fileName }}
            </span>
          </li>

          <!-- 大小 -->
          <li class="data-item" :style="{ width: headers[1].width }">
            {{ fileSizeConvert(row.fileSize).value }}
          </li>

          <!-- 状态 -->
          <li class="data-item" :style="{ width: headers[2].width }">
            {{ fileStatusEnum(row.fileStatus).value }}
          </li>

          <!-- 创建用户 -->
          <li class="data-item" :style="{ width: headers[3].width }">
            {{ row.createBy }}
          </li>

          <!-- 创建时间 -->
          <li class="data-item" :style="{ width: headers[4].width }">
            {{ row.createTime }}
          </li>

          <!-- 操作 -->
          <li class="data-item action-item" :style="{ width: headers[5].width }">
            <MyIcon
              class="icon-btn"
              title="预览"
              type="icon-eye"
              @click.stop="handleOpenFile(row)"
            />

            <MyIcon
              class="icon-btn"
              title="同步"
              type="icon-refresh"
              @click.stop="handleSyncFile(row)"
            />

            <MyIcon
              class="icon-btn"
              title="删除"
              type="icon-delete"
              @click.stop="handleDeleteFile(row)"
            />
          </li>
        </div>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import icon from '@/utils/icon'
import mixin from '@/mixins/fileType'

const { MyIcon } = icon()
const { fileTypeEnum, fileStatusEnum, fileSizeConvert } = mixin()

interface Props {
  headers: Array<{
    title: string
    width: string
  }>
  dirList: any[]
  fileList: any[]
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'context-menu', event: MouseEvent): void
  (e: 'dir-context-menu', event: MouseEvent, row: any): void
  (e: 'file-context-menu', event: MouseEvent, row: any): void

  (e: 'open-dir', row: any): void
  (e: 'delete-dir', row: any): void

  (e: 'open-file', row: any): void
  (e: 'sync-file', row: any): void
  (e: 'delete-file', row: any): void
}>()

/**
 * 空白区域右键
 */
const handleEmptyContextMenu = (event: MouseEvent) => {
  emit('context-menu', event)
}

/**
 * 目录右键
 */
const handleDirContextMenu = (event: MouseEvent, row: any) => {
  emit('dir-context-menu', event, row)
}

/**
 * 文件右键
 */
const handleFileContextMenu = (event: MouseEvent, row: any) => {
  emit('file-context-menu', event, row)
}

/**
 * 打开目录
 */
const handleOpenDir = (row: any) => {
  emit('open-dir', row)
}

/**
 * 删除目录
 */
const handleDeleteDir = (row: any) => {
  emit('delete-dir', row)
}

/**
 * 打开文件
 */
const handleOpenFile = (row: any) => {
  emit('open-file', row)
}

/**
 * 同步文件
 */
const handleSyncFile = (row: any) => {
  emit('sync-file', row)
}

/**
 * 删除文件
 */
const handleDeleteFile = (row: any) => {
  emit('delete-file', row)
}
</script>

<style scoped>
.list-wrapper {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;

  display: flex;
  flex-direction: column;

  overflow: hidden;

  color: var(--el-text-color-primary);
  background: var(--el-bg-color);
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

  color: var(--el-text-color-primary);
  background: var(--el-fill-color-light);

  font-weight: 600;

  border-bottom: 1px solid var(--el-border-color);
}

.table-body {
  flex: 1;
  min-height: 0;

  overflow: auto;
}

.table-row {
  min-height: 48px;
  padding-left: 10px;

  color: var(--el-text-color-primary);

  transition: background-color 0.2s;
}

.table-row:nth-child(even) {
  background-color: var(--el-fill-color-lighter);
}

.table-row:hover {
  background-color: var(--el-fill-color-light);
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

  /*
   * 这里从原来的 10px 改成 8px 10px，
   * 同时提高行高，解决字母下半部分被遮挡的问题
   */
  padding: 8px 6px;

  min-height: 48px;

  display: flex;
  align-items: center;

  overflow: hidden;

  white-space: nowrap;
  text-overflow: ellipsis;

  line-height: 28px;
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

  line-height: 28px;
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

  background: var(--el-fill-color);

  color: var(--el-text-color-regular);

  cursor: pointer;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease,
    color 0.2s ease;
}

.icon-btn:hover {
  color: var(--el-color-primary);

  background: var(--el-fill-color-light);

  transform: translateY(-2px);

  box-shadow: var(--el-box-shadow-light);
}
</style>
