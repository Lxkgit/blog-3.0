<template>
  <el-tabs v-model="activeTab" class="server-tabs">
    <el-tab-pane label="服务器监控" name="monitor">
      <div class="server-container">
        <!-- 基础信息 -->
        <el-card class="server-info-card" shadow="hover">
          <template #header>
            <div class="card-title">云服务器信息</div>
          </template>
          <div class="info-grid">
            <div>
              <span>服务器名称：</span>
              <span>{{ server.name }}</span>
            </div>
            <div>
              <span>服务器IP：</span>
              <span>{{ server.ip }}</span>
            </div>
            <div>
              <span>系统版本：</span>
              <span>{{ server.os }}</span>
            </div>
            <div>
              <span>运行时间：</span>
              <span>{{ server.uptime }}</span>
            </div>
            <div>
              <span>服务器状态：</span>
              <el-tag type="success"> 在线 </el-tag>
            </div>
          </div>
        </el-card>

        <!-- 资源监控 -->

        <div class="monitor-grid">
          <!-- CPU -->
          <el-card class="monitor-card" shadow="hover">
            <template #header> CPU使用率 </template>
            <div ref="cpuChart" class="chart"></div>
          </el-card>
          <!-- 内存 -->
          <el-card class="monitor-card" shadow="hover">
            <template #header> 内存使用率 </template>
            <div ref="memoryChart" class="chart"></div>
          </el-card>
          <!-- 磁盘 -->
          <el-card class="monitor-card" shadow="hover">
            <template #header> 磁盘信息 </template>
            <div class="disk-info">
              <el-progress type="dashboard" :percentage="disk.used" />
              <div>
                已使用
                {{ disk.used }}%
              </div>
              <div>
                总空间
                {{ disk.total }}
              </div>
            </div>
          </el-card>
          <!-- 网络 -->
          <el-card class="monitor-card" shadow="hover">
            <template #header> 网络流量 </template>
            <div class="network-info">
              <div>
                ↑ 上传：
                {{ network.upload }}
              </div>
              <div>
                ↓ 下载：
                {{ network.download }}
              </div>
            </div>
          </el-card>
        </div>

        <!-- 服务状态 -->
        <!-- <el-card class="service-card" shadow="hover">
          <template #header> 服务状态 </template>

          <div class="service-list">
            <div>
              Docker
              <el-tag type="success"> 正常 </el-tag>
            </div>
            <div>
              Nginx
              <el-tag type="success"> 正常 </el-tag>
            </div>
            <div>
              Java服务
              <el-tag type="success"> 正常 </el-tag>
            </div>
            <div>
              MediaMTX
              <el-tag type="success"> 正常 </el-tag>
            </div>
          </div>
        </el-card> -->
      </div>
    </el-tab-pane>

    <!-- 日志 -->

    <el-tab-pane label="运行日志" name="log"> 日志区域 </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'

const activeTab = ref('monitor')

onMounted(() => {
  initCpuChart()
})

const cpuChart = ref(null)

const server = reactive({
  name: '腾讯云服务器',

  ip: '49.xxx.xxx.xxx',

  os: 'Ubuntu 24.04',

  uptime: '30天12小时',
})

const disk = reactive({
  used: 65,

  total: '100G',
})

const network = reactive({
  upload: '2.3MB/s',

  download: '8.5MB/s',
})

// 模拟CPU心跳数据
const cpuData = ref([
  {
    time: '09:30:00',
    value: 45,
  },
  {
    time: '09:31:00',
    value: 52,
  },
  {
    time: '09:32:00',
    value: 60,
  },
  {
    time: '09:33:00',
    value: 78,
  },
  {
    time: '09:34:00',
    value: 100,
  },
])

const initCpuChart = () => {
  const chart = echarts.init(cpuChart.value)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
    },

    grid: {
      left: '8%',
      right: '5%',
      top: '10%',
      bottom: '25%',
      containLabel: true,
    },

    xAxis: {
      type: 'category',
      data: cpuData.value.map((item) => item.time),
    },

    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%',
      },
    },

    series: [
      {
        name: 'CPU',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: cpuData.value.map((item) => item.value),
      },
    ],
  })

  window.addEventListener('resize', () => {
    chart.resize()
  })
}
</script>

<style scoped>
.server-tabs {
  width: 100%;

  height: 100%;
}

:deep(.el-tabs__content) {
  width: 100%;
}

:deep(.el-tab-pane) {
  width: 100%;
}

.server-container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.server-info-card {
  border-radius: 14px;

  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;

  font-weight: 600;
}

.info-grid {
  display: grid;

  grid-template-columns: repeat(3, 1fr);

  gap: 18px;

  font-size: 14px;
}

.monitor-grid {
  width: 100%;

  display: grid;

  grid-template-columns: repeat(4, minmax(0, 1fr));

  gap: 20px;
}

.monitor-card {
  border-radius: 14px;

  height: 280px;

  transition: 0.25s;
}

.monitor-card:hover,
.service-card:hover,
.server-info-card:hover {
  transform: translateY(-3px);

  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.chart {
  height: 260px;
}

.disk-info {
  height: 260px;

  display: flex;

  flex-direction: column;

  align-items: center;

  justify-content: center;

  gap: 10px;
}

.network-info {
  height: 260px;

  display: flex;

  flex-direction: column;

  justify-content: center;

  gap: 20px;

  font-size: 16px;
}

.service-card {
  margin-top: 20px;

  border-radius: 14px;
}

.service-list {
  display: grid;

  grid-template-columns: repeat(4, 1fr);

  gap: 20px;
}

.service-list div {
  display: flex;

  justify-content: space-between;

  align-items: center;

  padding: 15px;

  background: var(--el-fill-color-light);

  border-radius: 10px;
}

@media (max-width: 900px) {
  .monitor-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .service-list {
    grid-template-columns: 1fr;
  }
}
</style>
