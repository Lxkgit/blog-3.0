<template>
  <div class="card-wrapper" @contextmenu.prevent.stop="handleEmptyContextMenu">
    <!-- ==================== 目录 ==================== -->
    <div
      v-for="item in dirList"
      :key="'dir-card-' + item.id"
      class="file-card-item"
      @contextmenu.prevent.stop="handleDirContextMenu($event, item)"
    >
      <div class="file-preview" @dblclick="handleOpenDir(item)">
        <MyIcon class="folder-icon" type="icon-folder" />

        <div class="show_icon">
          <MyIcon
            title="打开文件夹"
            class="icon_type"
            type="icon-file-open"
            @click.stop="handleOpenDir(item)"
          />

          <MyIcon
            title="删除"
            class="icon_type"
            type="icon-delete"
            @click.stop="handleDeleteDir(item)"
          />
        </div>
      </div>

      <div class="file-card-footer">
        <el-tag class="file-type-tag" size="small"> 目录 </el-tag>

        <span class="file-card-name" :title="item.dirName">
          {{ item.dirName }}
        </span>
      </div>
    </div>

    <!-- ==================== 文件 ==================== -->
    <div
      v-for="item in fileList"
      :key="'file-card-' + item.id"
      class="file-card-item"
      @contextmenu.prevent.stop="handleFileContextMenu($event, item)"
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
        <div v-else-if="fileTypeEnum(item.fileType).key === 2" class="file-icon-preview">
          <MyIcon :title="fileTypeEnum(item.fileType).value" class="large-icon" type="icon-zip" />
        </div>

        <!-- 视频 -->
        <div v-else-if="fileTypeEnum(item.fileType).key === 3" class="file-icon-preview">
          <MyIcon :title="fileTypeEnum(item.fileType).value" class="video-icon" type="icon-video" />
        </div>

        <!-- 普通文件 -->
        <div v-else class="file-icon-preview">
          <MyIcon :title="fileTypeEnum(item.fileType).value" class="large-icon" type="icon-file" />
        </div>

        <!-- 操作按钮 -->
        <div class="show_icon">
          <MyIcon
            title="预览"
            class="icon_type"
            type="icon-search"
            @click.stop="handleOpenFile(item)"
          />

          <MyIcon
            title="查看文件信息"
            class="icon_type"
            type="icon-file"
            @click.stop="handleShowFileDesc(item)"
          />

          <MyIcon
            title="删除"
            class="icon_type"
            type="icon-delete"
            @click.stop="handleDeleteFile(item)"
          />

          <!-- 移动 -->
          <el-dropdown>
            <el-button type="primary" size="small" @click.stop> 移动 </el-button>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="(dir, rowIndex) in dirList"
                  :key="rowIndex"
                  @click="handleMoveFile(dir, item)"
                >
                  {{ dir.dirName }}
                </el-dropdown-item>

                <el-dropdown-item @click="handleMoveFile(null, item)"> 删除 </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="file-card-footer">
        <el-tag class="file-type-tag" size="small">
          {{ fileTypeEnum(item.fileType).value }}
        </el-tag>

        <span class="file-card-name" :title="item.fileName">
          {{ item.fileName }}
        </span>

        <MyIcon v-if="item.type !== 0" type="icon-download" class="download-icon" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import icon from '@/utils/icon'
import mixin from '@/mixins/fileType'

const { MyIcon } = icon()

const { fileTypeEnum } = mixin()

interface Props {
  dirList: any[]
  fileList: any[]
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'context-menu', event: MouseEvent): void

  (e: 'dir-context-menu', event: MouseEvent, item: any): void
  (e: 'file-context-menu', event: MouseEvent, item: any): void

  (e: 'open-dir', item: any): void
  (e: 'delete-dir', item: any): void

  (e: 'open-file', item: any): void
  (e: 'show-file-desc', item: any): void
  (e: 'delete-file', item: any): void

  (e: 'move-file', dir: any, item: any): void
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
const handleDirContextMenu = (event: MouseEvent, item: any) => {
  emit('dir-context-menu', event, item)
}

/**
 * 文件右键
 */
const handleFileContextMenu = (event: MouseEvent, item: any) => {
  emit('file-context-menu', event, item)
}

/**
 * 打开目录
 */
const handleOpenDir = (item: any) => {
  emit('open-dir', item)
}

/**
 * 删除目录
 */
const handleDeleteDir = (item: any) => {
  emit('delete-dir', item)
}

/**
 * 打开文件
 */
const handleOpenFile = (item: any) => {
  emit('open-file', item)
}

/**
 * 查看文件信息
 */
const handleShowFileDesc = (item: any) => {
  emit('show-file-desc', item)
}

/**
 * 删除文件
 */
const handleDeleteFile = (item: any) => {
  emit('delete-file', item)
}

/**
 * 移动文件
 */
const handleMoveFile = (dir: any, item: any) => {
  emit('move-file', dir, item)
}
</script>

<style scoped>
.card-wrapper {
  width: 100%;
  height: 100%;

  min-width: 0;
  min-height: 0;

  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));

  align-content: start;

  gap: 16px;

  padding: 4px;

  overflow: auto;

  box-sizing: border-box;

  background: var(--el-bg-color);
}

.file-card-item {
  width: 100%;
  min-width: 0;
  max-width: 190px;

  height: 150px;

  box-sizing: border-box;

  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;

  background: var(--el-bg-color);

  overflow: hidden;

  box-shadow: var(--el-box-shadow-light);

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.file-card-item:hover {
  border-color: var(--el-border-color);

  transform: translateY(-2px);

  box-shadow: var(--el-box-shadow);
}

.file-preview {
  width: 100%;
  height: 120px;

  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;

  overflow: hidden;

  background: var(--el-fill-color-lighter);

  border-bottom: 1px solid var(--el-border-color-light);
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

  color: var(--el-text-color-regular);
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

  color: #fff;
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

  color: var(--el-text-color-regular);
}

.file-card-footer {
  width: 100%;
  height: 30px;

  display: flex;
  align-items: center;

  padding: 0 5px;

  box-sizing: border-box;

  overflow: hidden;

  color: var(--el-text-color-primary);

  background: var(--el-bg-color);
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

  color: var(--el-text-color-primary);

  line-height: 24px;
}

.download-icon {
  flex-shrink: 0;

  margin-left: 4px;

  color: var(--el-text-color-regular);
}
</style>
