<template>
  <el-dialog
    v-model="visible"
    title="创建目录"
    width="min(40vw, 600px)"
    :close-on-click-modal="false"
    class="dir-dialog"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="dirFile"
      :rules="rules"
      label-width="100px"
      label-position="left"
    >
      <el-form-item label="目录名称：" prop="name">
        <el-input v-model="dirFile.name" size="small" placeholder="请输入目录名称" clearable />
      </el-form-item>

      <el-form-item label="目录类型：" prop="dirType">
        <el-radio-group v-model="dirFile.dirType">
          <el-radio border :value="1">
            普通目录

            <el-tooltip content="普通目录" placement="top" @click.stop.prevent>
              <MyIcon type="icon-wenhaofill" />
            </el-tooltip>
          </el-radio>

          <el-radio border :value="2">
            评分目录

            <el-tooltip content="用于对文件质量分类存放" placement="top" @click.stop.prevent>
              <MyIcon type="icon-wenhaofill" />
            </el-tooltip>
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleSave"> 创建 </el-button>

        <el-button @click="handleClose"> 取消 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import icon from '@/utils/icon'

const { MyIcon } = icon()

interface DirFile {
  name: string
  dirType?: any
}

interface Props {
  modelValue: boolean
  dirFile: DirFile
  rules: any
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: []
  close: []
}>()

const formRef = ref()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

/**
 * 创建目录
 */
const handleSave = async () => {
  if (!formRef.value) {
    return
  }

  try {
    const valid = await formRef.value.validate()

    if (valid) {
      emit('save')
    }
  } catch {
    // 表单验证失败，不执行创建
  }
}

/**
 * 关闭 Dialog
 */
const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

/**
 * 暴露表单实例
 * 如果父页面后续需要主动 resetFields 可以使用
 */
defineExpose({
  formRef,
})
</script>

<style scoped>
.dir-dialog {
  max-width: calc(100vw - 30px);
}

:deep(.el-dialog__body) {
  max-height: 70vh;
  max-height: 70dvh;
  overflow: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-radio) {
  margin-right: 10px;
}

:deep(.el-tooltip) {
  margin-left: 4px;
  cursor: help;
}
</style>
