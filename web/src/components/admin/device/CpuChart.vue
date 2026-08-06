<!-- <template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

// CPU: 两个核心的初始数据
const cpuCore1: number[] = []
const cpuCore2: number[] = []
const xData: string[] = []

// 模拟 CPU 数据（你可以换成后端接口）
function getCpuData() {
  // 随机 0-100%
  return {
    core1: +(Math.random() * 80 + 20).toFixed(2),
    core2: +(Math.random() * 70 + 10).toFixed(2),
  }
}

function initChart() {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)

  chart.setOption({
    title: { text: 'CPU 利用率 (2 核)' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['Core1', 'Core2'] },
    xAxis: { type: 'category', data: xData },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { formatter: '{value} %' },
    },
    series: [
      { name: 'Core1', type: 'line', data: cpuCore1 },
      { name: 'Core2', type: 'line', data: cpuCore2 },
    ],
  })
}

function updateChart() {
  const now = new Date()
  const timeStr = now.toLocaleTimeString()

  const data = getCpuData()

  xData.push(timeStr)
  cpuCore1.push(data.core1)
  cpuCore2.push(data.core2)

  // 只保持 20 个点
  if (xData.length > 20) {
    xData.shift()
    cpuCore1.shift()
    cpuCore2.shift()
  }

  chart?.setOption({
    xAxis: { data: xData },
    series: [
      { data: cpuCore1 },
      { data: cpuCore2 },
    ],
  })
}

let timer: number

onMounted(() => {
  initChart()
  timer = window.setInterval(updateChart, 2000)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  chart?.dispose()
})
</script>

<style scoped>
.chart {
  width: 100%;
  height: 350px;
}
</style> -->
