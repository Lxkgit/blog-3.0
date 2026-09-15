<template>
  <el-card class="sensor-data-card">
    <div class="table-header">
      <div class="table-title">
        监测数据
        <el-tag size="small" type="info">
          {{ total }}
        </el-tag>
      </div>
    </div>

    <el-table
      :data="sensorDataList.data"
      stripe
      border
      class="sensor-data-table"
      v-loading="loading"
      empty-text="暂无监测数据"
    >
      <el-table-column prop="sensorData" label="监测数据" min-width="400" show-overflow-tooltip>
        <template #default="scope">
          <span class="sensor-data-text">
            {{ formatSensorData(scope.row.sensorData) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="监测时间" width="200" />
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        background
        layout="total, prev, pager, next, jumper"
        :page-size="size"
        :total="total"
        @current-change="selectSensorDataPageFun"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { selectSensorDataListApi } from '@/api/file'

const props = defineProps({
  sensor: Object,
})

let {
  page,
  size,
  total,
  sensorDataList,
  loading,
  selectSensorDataPageFun,
  selectSensorDataByIdFun,
} = sensorFun()

onMounted(() => {
  if (props.sensor?.id) {
    selectSensorDataByIdFun(props.sensor.id, 1)
  }
})

function sensorFun() {
  const page = ref<number>(1)

  const size = ref<number>(12)

  const total = ref<number>(0)

  const loading = ref<boolean>(false)

  const sensorDataList = reactive({
    data: [] as any[],
  })

  const selectSensorDataPageFun = (currentPage: number) => {
    page.value = currentPage

    if (!props.sensor?.id) {
      return
    }

    selectSensorDataByIdFun(props.sensor.id, currentPage)
  }

  const selectSensorDataByIdFun = (id: any, currentPage: number) => {
    if (!id) {
      return
    }

    loading.value = true

    selectSensorDataListApi({
      pageNum: currentPage,
      pageSize: size.value,
      sensorId: id,
    })
      .then((res: any) => {
        if (res.code === 200) {
          total.value = res.result.total || 0
          sensorDataList.data = res.result.list || []
        }
      })
      .catch(() => {
        sensorDataList.data = []
      })
      .then(() => {
        loading.value = false
      })
  }

  return {
    page,
    size,
    total,
    loading,
    sensorDataList,
    selectSensorDataPageFun,
    selectSensorDataByIdFun,
  }
}

const formatSensorData = (data: any) => {
  if (data === null || data === undefined || data === '') {
    return '-'
  }

  if (typeof data !== 'string') {
    return JSON.stringify(data)
  }

  try {
    const json = JSON.parse(data)

    if (typeof json === 'object') {
      return JSON.stringify(json, null, 2)
    }

    return String(json)
  } catch {
    return data
  }
}
</script>

<style scoped>
.sensor-data-card {
  margin: 10px 2%;
  width: 94%;
  height: calc(100vh - 230px);
  overflow: hidden;
  box-sizing: border-box;
}

.sensor-data-card :deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 12px;
  flex-shrink: 0;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.sensor-data-table {
  width: 100%;
  flex: 1;
  min-height: 0;
}

.sensor-data-text {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  padding: 0;
  flex-shrink: 0;
}
</style>
