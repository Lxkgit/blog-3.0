<template>
  <el-card class="sensor-control-card">
    <div class="page-header">
      <div class="sensor-info">
        <div class="sensor-title">
          <span class="title">传感器控制</span>
          <el-tag v-if="sensor.sensorStatus === 1" type="success">
            {{ deviceStatus(sensor.sensorStatus) }}
          </el-tag>
          <el-tag v-else type="warning">
            {{ deviceStatus(sensor.sensorStatus) }}
          </el-tag>
        </div>

        <div class="sensor-info-list">
          <div class="info-item">
            <span class="label">传感器名称</span>
            <span class="value">{{ sensor.sensorName || '-' }}</span>
          </div>

          <div class="info-item">
            <span class="label">传感器编码</span>
            <span class="value">{{ sensor.sensorCode || '-' }}</span>
          </div>

          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ sensor.createTime || '-' }}</span>
          </div>

          <div class="info-item">
            <span class="label">修改时间</span>
            <span class="value">{{ sensor.updateTime || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="header-action">
        <el-button type="primary" @click="openCreateDialog">
          <MyIcon type="icon-add" />
          创建命令
        </el-button>
      </div>
    </div>

    <el-divider />

    <div class="table-header">
      <div class="table-title">
        控制命令
        <el-tag size="small" type="info">
          {{ total }}
        </el-tag>
      </div>
    </div>

    <el-table
      :data="sensorControlList.data"
      stripe
      border
      class="control-table"
      v-loading="loading"
      empty-text="暂无控制命令"
    >
      <el-table-column prop="controlName" label="控制命令名称" min-width="180" />

      <el-table-column
        prop="controlMessage"
        label="控制命令"
        min-width="300"
        show-overflow-tooltip
      />

      <el-table-column prop="createTime" label="创建时间" width="180" />

      <el-table-column prop="updateTime" label="最近修改时间" width="180" />

      <el-table-column fixed="right" label="操作" width="175" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="sendSensorControlFun(scope.row.id)">
            <MyIcon type="icon-send" title="发送命令" />
            发送
          </el-button>

          <el-button link type="primary" size="small" @click="updateSensorControlFun(scope.row.id)">
            <MyIcon type="icon-edit" title="修改命令" />
            修改
          </el-button>

          <el-button link type="danger" size="small" @click="deleteSensorControlFun(scope.row.id)">
            <MyIcon type="icon-delete" title="删除命令" />
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        background
        layout="total, prev, pager, next, jumper"
        :page-size="size"
        :total="total"
        @current-change="selectSensorControlPageFun"
      />
    </div>
  </el-card>

  <el-dialog
    v-model="dialogFormVisible"
    :title="dialogTitle"
    width="520px"
    :close-on-click-modal="false"
    @closed="resetSensorControlForm"
  >
    <el-form ref="formRef" :model="sensorControlForm" label-width="100px">
      <el-form-item
        label="指令名称"
        prop="name"
        :rules="[
          {
            required: true,
            message: '请输入指令名称',
            trigger: 'blur',
          },
        ]"
      >
        <el-input
          v-model="sensorControlForm.name"
          placeholder="请输入控制命令名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-divider content-position="left"> 命令参数 </el-divider>

      <template v-for="(item, index) in sensorControlForm.sensor[0].from" :key="index">
        <el-form-item v-if="item.type === 'input-number'" :label="item.label">
          <el-input-number
            v-model="item.value"
            :min="item.min"
            :max="item.max"
            :step="item.step || 1"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item v-else-if="item.type === 'input'" :label="item.label">
          <el-input v-model="item.value" :placeholder="`请输入${item.label}`" clearable />
        </el-form-item>

        <el-form-item v-else-if="item.type === 'switch'" :label="item.label">
          <el-switch v-model="item.value" />
        </el-form-item>

        <el-form-item v-else-if="item.type === 'select'" :label="item.label">
          <el-select v-model="item.value" :placeholder="`请选择${item.label}`" style="width: 220px">
            <el-option
              v-for="option in item.options || []"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </template>

      <el-empty
        v-if="sensorControlForm.sensor[0].from.length === 0"
        description="该传感器暂无可配置参数"
        :image-size="80"
      />
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false"> 取消 </el-button>

        <el-button type="primary" :loading="saveLoading" @click="saveSensorControlFun">
          {{ isEdit ? '保存修改' : '创建命令' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

import {
  selectSensorByIdApi,
  selectSensorControlListApi,
  sendSensorControlApi,
  saveSensorControlApi,
  selectSensorControlByIdApi,
  deleteSensorControlApi,
  selectSensorTemplateByChipOrSensorIdApi,
} from '@/api/file'

import icon from '@/utils/icon'
import mixin from '@/mixins/device'

const { deviceStatus } = mixin()
const { MyIcon } = icon()

const props = defineProps({
  sensor: Object,
})

const formRef = ref<FormInstance>()

const loading = ref(false)
const saveLoading = ref(false)

const page = ref(1)
const size = ref(8)
const total = ref(0)

const sensor = ref<any>({})

const sensorControlList = reactive({
  data: [] as any[],
})

const dialogFormVisible = ref(false)

const sensorControlForm = reactive({
  id: 0,
  name: '',
  sensor: [
    {
      id: 0,
      idx: 0,
      sensorData: {} as any,
      sensorType: '',
      sensorCode: '',
      delay: 0,
      from: [] as any[],
    },
  ],
})

const sensorTemplateFrom = reactive({
  data: [] as any[],
})

const isEdit = computed(() => sensorControlForm.id !== 0)

const dialogTitle = computed(() => (isEdit.value ? '修改传感器控制命令' : '创建传感器控制命令'))

const sensorId = computed(() => props.sensor?.id)

onMounted(() => {
  if (!sensorId.value) {
    return
  }

  selectSensorTemplateByChipOrSensorIdFun(sensorId.value)
  selectSensorByIdFun(sensorId.value)
  selectSensorControlListFun(sensorId.value, 1)
})

const selectSensorByIdFun = (id: any) => {
  selectSensorByIdApi({ id }).then((res: any) => {
    if (res.code === 200) {
      sensor.value = res.result
    }
  })
}

const selectSensorControlPageFun = (currentPage: number) => {
  page.value = currentPage

  if (sensorId.value) {
    selectSensorControlListFun(sensorId.value, currentPage)
  }
}

const selectSensorControlListFun = (sensorId: any, currentPage: number) => {
  loading.value = true

  selectSensorControlListApi({
    pageNum: currentPage,
    pageSize: size.value,
    sensorId,
  })
    .then((res: any) => {
      if (res.code === 200) {
        total.value = res.result.total
        sensorControlList.data = res.result.list || []
      }

      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}

const sendSensorControlFun = (id: any) => {
  ElMessageBox.confirm('确定要发送该控制命令吗？', '发送命令', {
    confirmButtonText: '确定发送',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      return sendSensorControlApi({ id })
    })
    .then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('命令发送成功')
      }
    })
    .catch(() => {})
}

const openCreateDialog = () => {
  resetSensorControlForm()
  setSensorTemplateFrom()
  dialogFormVisible.value = true
}

const resetSensorControlForm = () => {
  sensorControlForm.id = 0
  sensorControlForm.name = ''

  sensorControlForm.sensor[0].id = sensorId.value || 0
  sensorControlForm.sensor[0].idx = 0
  sensorControlForm.sensor[0].sensorData = {}
  sensorControlForm.sensor[0].sensorType = props.sensor?.sensorType || ''
  sensorControlForm.sensor[0].sensorCode = props.sensor?.sensorCode || ''
  sensorControlForm.sensor[0].delay = 0
  sensorControlForm.sensor[0].from = []
}

const setSensorTemplateFrom = () => {
  const currentSensorType = props.sensor?.sensorType

  const template = sensorTemplateFrom.data.find(
    (item: any) => item.sensorType === currentSensorType,
  )

  if (!template) {
    return
  }

  try {
    sensorControlForm.sensor[0].from = JSON.parse(template.template)
    sensorControlForm.sensor[0].sensorType = currentSensorType
    sensorControlForm.sensor[0].sensorCode = props.sensor?.sensorCode || ''
  } catch (error) {
    console.error('传感器模板解析失败:', error)
    sensorControlForm.sensor[0].from = []
  }
}

const saveSensorControlFun = async () => {
  if (!sensorId.value) {
    ElMessage.error('传感器信息不存在')
    return
  }

  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  sensorControlForm.sensor[0].id = sensorId.value

  saveLoading.value = true

  saveSensorControlApi({
    id: sensorControlForm.id === 0 ? null : sensorControlForm.id,
    sensorId: sensorId.value,
    commandGroup: 0,
    controlName: sensorControlForm.name,
    controlMessage: JSON.stringify(sensorControlForm.sensor),
  })
    .then((res: any) => {
      if (res.code === 200) {
        ElMessage.success(sensorControlForm.id === 0 ? '命令创建成功' : '命令修改成功')

        dialogFormVisible.value = false
        page.value = 1

        selectSensorControlListFun(sensorId.value, 1)
      }

      saveLoading.value = false
    })
    .catch(() => {
      saveLoading.value = false
    })
}

const selectSensorControlByIdFun = (id: any) => {
  selectSensorControlByIdApi(id).then((res: any) => {
    if (res.code !== 200) {
      ElMessage.error(res.message || '获取命令失败')
      return
    }

    sensorControlForm.id = res.result.id
    sensorControlForm.name = res.result.name
    sensorControlForm.sensor = JSON.parse(res.result.sensor)
  })
}

const updateSensorControlFun = (id: any) => {
  dialogFormVisible.value = true
  selectSensorControlByIdFun(id)
}

const deleteSensorControlFun = (id: any) => {
  ElMessageBox.confirm('删除后无法恢复，确定要删除该控制命令吗？', '删除命令', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      return deleteSensorControlApi({ ids: id })
    })
    .then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('命令删除成功')

        page.value = 1

        if (sensorId.value) {
          selectSensorControlListFun(sensorId.value, 1)
        }
      }
    })
    .catch(() => {})
}

const selectSensorTemplateByChipOrSensorIdFun = (id: any) => {
  selectSensorTemplateByChipOrSensorIdApi({
    sensorId: id,
  }).then((res: any) => {
    if (res.code === 200) {
      sensorTemplateFrom.data = res.result || []
    }
  })
}
</script>

<style scoped>
.sensor-control-card {
  margin: 10px 2%;
  width: 94%;
  height: calc(100vh - 230px);
  overflow-y: auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  padding: 8px 2%;
}

.sensor-info {
  flex: 1;
  min-width: 0;
}

.sensor-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.sensor-title .title {
  font-size: 18px;
  font-weight: 600;
}

.sensor-info-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 40px;
}

.info-item {
  display: flex;
  align-items: center;
  min-width: 220px;
  line-height: 28px;
}

.info-item .label {
  color: var(--el-text-color-secondary);
  margin-right: 10px;
  white-space: nowrap;
}

.info-item .value {
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-action {
  flex-shrink: 0;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 0 15px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.control-table {
  width: 100%;
  height: calc(100vh - 510px);
  min-height: 300px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  padding-bottom: 5px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-divider) {
  margin: 10px 0 20px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 10px;
}

@media screen and (max-width: 900px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-action {
    width: 100%;
  }

  .header-action .el-button {
    width: 100%;
  }

  .sensor-info-list {
    gap: 5px 15px;
  }

  .info-item {
    width: 100%;
  }
}
</style>
