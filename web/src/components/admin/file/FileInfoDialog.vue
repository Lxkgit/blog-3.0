<template>
  <el-dialog
    v-model="visible"
    title="文件信息"
    width="min(40vw, 600px)"
    :close-on-click-modal="false"
    class="file-info-dialog"
  >
    <el-form v-if="file" :model="file" label-width="100px" label-position="left">
      <el-form-item label="文件名称：">
        <el-input v-model="file.fileName" size="small" readonly />
      </el-form-item>

      <el-form-item :label="file.type === 0 ? '目录大小：' : '文件大小：'">
        <el-input v-model="file.fileSize" size="small" readonly />
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue: boolean
  file: any
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})
</script>

<style scoped>
.file-info-dialog {
  max-width: calc(100vw - 30px);
}

:deep(.el-dialog__body) {
  max-height: 70vh;
  max-height: 70dvh;
  overflow: auto;
}
</style>
