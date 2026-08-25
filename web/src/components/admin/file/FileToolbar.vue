<template>
  <div class="file-toolbar">
    <!-- 上传文件 -->
    <el-upload :auto-upload="false" multiple :show-file-list="false" :on-change="handleUpload">
      <el-button type="success" size="small" text> 上传文件 </el-button>
    </el-upload>

    <!-- 路径 -->
    <div class="path-toolbar">
      <MyIcon
        :style="filePath === '' ? { pointerEvents: 'none' } : { cursor: 'pointer' }"
        class="back-icon"
        type="icon-shangyibu"
        title="返回上一级"
        @click="handleChangePath(-2)"
      />

      <span class="path-label"> 当前路径： </span>

      <div class="path-list">
        <span class="file_path" @click="handleChangePath(-1)"> 根目录 </span>

        <div v-for="(item, idx) in filePathArr" :key="idx" class="path-item">
          <span class="file_path" @click="handleChangePath(idx)">
            {{ item }}
          </span>
        </div>
      </div>
    </div>

    <!-- 列表 / 卡片切换 -->
    <div class="toolbar-right">
      <el-switch
        v-model="localSwitchFlag"
        active-text="列表"
        inactive-text="卡片"
        @change="handleSwitch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import icon from '@/utils/icon'

const { MyIcon } = icon()

interface Props {
  filePath: string | null
  filePathArr: string[]
  switchFlag: boolean
}

const props = defineProps<Props>()

const localSwitchFlag = ref(props.switchFlag)

watch(
  () => props.switchFlag,
  (value) => {
    localSwitchFlag.value = value
  },
)

const emit = defineEmits<{
  (e: 'upload', file: any, fileList: any[]): void
  (e: 'change-path', index: number): void
  (e: 'update:switchFlag', value: boolean): void
}>()

const handleUpload = (file: any, fileList: any[]) => {
  emit('upload', file, fileList)
}

const handleChangePath = (index: number) => {
  emit('change-path', index)
}

const handleSwitch = (value: string | number | boolean) => {
  const switchValue = Boolean(value)

  localSwitchFlag.value = switchValue

  emit('update:switchFlag', switchValue)
}
</script>

<style scoped>
.file-toolbar {
  width: 100%;
  min-width: 0;
  flex-shrink: 0;

  display: flex;
  align-items: center;
  gap: 0;

  font-size: 14px;
}

/* ==================== 路径 ==================== */

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

  line-height: 24px;
  min-height: 24px;

  white-space: nowrap;

  box-sizing: border-box;
}

.file_path:hover {
  color: var(--el-color-primary);
}

/* ==================== 右侧切换 ==================== */

.toolbar-right {
  flex-shrink: 0;
  margin-left: auto;

  display: flex;
  align-items: center;
}
</style>
