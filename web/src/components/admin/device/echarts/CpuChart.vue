<template>
  <div ref="chartRef" class="cpu-chart"></div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'

interface CpuItem {
  total: string | number
  time: string
  per: number[]
}

const props = defineProps<{
  data: CpuItem[]
  cores: number
}>()

const chartRef = ref()
let chart: any = null
const initChart = async () => {
  await nextTick()
  if (!chartRef.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  /**
   * x轴
   */
  const times = props.data.map((item) => item.time).reverse()

  /**
   * 总CPU
   */
  const totalData = props.data.map((item) => Number(item.total)).reverse()

  /**
   * series
   */
  const series: any[] = [
    {
      name: 'CPU总占用',
      type: 'line',
      // 平滑曲线
      smooth: true,
      // 线宽
      lineStyle: {
        width: 3,
      },
      symbol: 'circle',
      symbolSize: 6,
      data: totalData,
    },
  ]

  /**
   * 每个核心
   */
  for (let i = 0; i < props.cores; i++) {
    series.push({
      name: `CPU${i}`,
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: {
        width: 1,
      },
      data: props.data.map((item) => item.per[i] ?? 0).reverse(),
    })
  }

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let html = `<div>${params[0].axisValue}</div>`
        params.forEach((item: any) => {
          html += `
      <div>
        ${item.marker}
        ${item.seriesName} :
        <b>
          ${Number(item.value).toFixed(1)}%
        </b>
      </div>
    `
        })
        return html
      },
    },

    grid: {
      left: '8%',
      right: '5%',
      top: '10%',
      bottom: '20%',
      containLabel: true,
    },

    xAxis: {
      type: 'category',
      data: times,
    },

    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%',
      },
    },
    series,
  })
}

watch(
  () => [props.data, props.cores],
  () => {
    initChart()
  },
  {
    deep: true,
  },
)

onMounted(() => {
  initChart()
  window.addEventListener('resize', resize)
})

const resize = () => {
  chart?.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
})
</script>

<style scoped>
.cpu-chart {
  width: 100%;
  height: 300px;
}
</style>
