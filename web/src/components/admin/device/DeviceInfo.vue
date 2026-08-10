<template>
  <div class="device-monitor">
    <el-tabs v-model="activeTab" class="device-tabs" @tab-click="activeTabHandleClick">
      <el-tab-pane label="设备信息" name="deviceInfo">
        <div class="tab-content-scroll">
          <div class="monitor-grid">
            <!-- CPU -->
            <el-card class="monitor-card cpu-card" shadow="hover">
              <template #header> CPU信息 </template>
              <CpuChart :data="cpuData" :cores="cpuCores" />
            </el-card>

            <!-- 内存 -->
            <el-card class="monitor-card" shadow="hover">
              <template #header> 内存使用率 </template>
              <MemoryChart :data="memoryData" :total="memoryTotal" />
            </el-card>

            <!-- 磁盘 -->
            <el-card class="monitor-card" shadow="hover">
              <template #header>
                <div class="card-title">磁盘空间</div>
              </template>
              <div class="empty-data">暂无数据</div>
            </el-card>

            <!-- 单片机 -->
            <el-card class="monitor-card chip-data" shadow="hover">
              <template #header>
                <div class="card-title">单片机数据</div>
              </template>
              <div class="empty-data">暂无数据</div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="单片机数据" name="chipData">
        <div class="empty-page">暂无数据</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import type { TabsPaneContext } from 'element-plus'
import { selectDeviceInfoByDeviceCodeApi } from '@/api/file'
import MemoryChart from '@/components/admin/device/echarts/MemoryChart.vue'
import CpuChart from '@/components/admin/device/echarts/CpuChart.vue'

/**
 * 设备数据
 */
let { memoryData, memoryTotal, cpuData, cpuCores, getDeviceInfoListFun } = deviceFun()

/**
 * tab
 */
const activeTab = ref<string | number>('deviceInfo')

const activeTabHandleClick = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event)
}

/**
 * 父组件参数
 */
const props = defineProps({
  deviceId: Number,
})

onMounted(() => {
  getDeviceInfoListFun(props.deviceId)
})

/**
 * 设备数据方法
 */
function deviceFun() {
  const deviceInfoList: any = reactive({ data: [] })

  /**
   * 内存数据
   */
  const memoryData = ref([])
  const memoryTotal = ref(0)

  /**
   * CPU数据
   */
  const cpuData = ref([])
  const cpuCores = ref(0)

  /**
   * 查询设备信息
   */
  const getDeviceInfoListFun = (deviceId: any) => {
    selectDeviceInfoByDeviceCodeApi({
      id: deviceId,
      dataCount: 50,
    }).then(async (res: any) => {
      if (res.code === 200) {
        memoryData.value = res.result.memoryUsed
        memoryTotal.value = res.result.memoryTotal

        cpuData.value = res.result.cpuUsage
        cpuCores.value = res.result.cpuCores
        await nextTick()
      }
    })
  }

  return {
    memoryData,
    memoryTotal,
    cpuData,
    cpuCores,
    deviceInfoList,

    getDeviceInfoListFun,
  }
}
</script>

<style scoped>
.device-monitor {
  width: 100%;

  height: 100%;

  padding: 20px;

  overflow: hidden;
}

.tab-content-scroll {
  height: calc(100vh - 310px);

  overflow-y: auto;

  padding-right: 10px;
}

.device-tabs {
  width: 100%;
}

.monitor-grid {
  display: grid;

  grid-template-columns: repeat(2, minmax(400px, 1fr));

  gap: 20px;

  padding-top: 10px;
}

.monitor-card {
  border-radius: 14px;

  min-height: 260px;

  transition: 0.25s;
}

.monitor-card:hover {
  transform: translateY(-3px);

  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.cpu-card {
  grid-column: 1/-1;
}

.chip-data {
  grid-column: 1/-1;

  min-height: 200px;
}

.card-title {
  font-size: 16px;

  font-weight: 600;
}

.chart-box {
  height: 300px;

  width: 100%;
}

.empty-data {
  height: 160px;

  display: flex;

  align-items: center;

  justify-content: center;

  color: #909399;

  font-size: 14px;
}

.empty-page {
  height: 300px;

  display: flex;

  align-items: center;

  justify-content: center;

  color: #909399;
}

@media (max-width: 900px) {
  .monitor-grid {
    grid-template-columns: 1fr;
  }

  .cpu-card,
  .chip-data {
    grid-column: auto;
  }
}
</style>
