<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const memUsed: number[] = []
const xData: string[] = []

// 模拟内存数据（假设总内存 16GB）
function getMemoryData() {
  return +(Math.random() * 12 + 2).toFixed(2) // 2 - 14GB
}

function initChart() {
  chart = echarts.init(chartRef.value!)

  chart.setOption({
    title: { text: '内存使用情况' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xData },
    yAxis: {
      type: 'value',
      min: 0,
      max: 16,
      axisLabel: { formatter: '{value} GB' },
    },
    series: [
      { name: '已用内存', type: 'line', data: memUsed },
    ],
  })
}

function updateChart() {
  const now = new Date().toLocaleTimeString()

  const used = getMemoryData()

  xData.push(now)
  memUsed.push(used)

  if (xData.length > 20) {
    xData.shift()
    memUsed.shift()
  }

  chart?.setOption({
    xAxis: { data: xData },
    series: [{ data: memUsed }],
  })
}

let timer: number

onMounted(() => {
  initChart()
  timer = window.setInterval(updateChart, 3000)
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
</style>
