<template>
  <div ref="chartRef" class="memory-chart"></div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

interface MemoryItem {
  time: string
  value: number
}

const props = defineProps<{
  /**
   * 内存历史数据
   */
  data: MemoryItem[]

  /**
   * 总内存(byte)
   */
  total: number
}>()

const chartRef = ref<HTMLElement>()
let chart: any = null

/**
 * 字节格式化
 */
const formatMemory = (value: number) => {
  if (value >= 1024 * 1024 * 1024) {
    return (value / 1024 / 1024 / 1024).toFixed(2) + ' GB'
  }
  if (value >= 1024 * 1024) {
    return (value / 1024 / 1024).toFixed(2) + ' MB'
  }
  if (value >= 1024) {
    return (value / 1024).toFixed(2) + ' KB'
  }
  return value + ' B'
}

const renderChart = async () => {
  await nextTick()
  if (!chartRef.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const totalGB = props.total / 1024 / 1024 / 1024
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const item = params[0]
        const usedGB = Number(item.value)
        const usedByte = usedGB * 1024 * 1024 * 1024
        const percent = ((usedByte / props.total) * 100).toFixed(2)
        return `
        <div>
          <div>
            ${item.axisValue}
          </div>
          <div>
            内存:
            <b>
              ${formatMemory(usedByte)}
            </b>
          </div>
          <div>
            占用率:
            <b>
              ${percent}%
            </b>
          </div>
          <div>
            总内存:
            ${formatMemory(props.total)}
          </div>
        </div>
        `
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
      data: props.data.map((item) => item.time),
    },

    yAxis: {
      type: 'value',
      max: totalGB,
      axisLabel: {
        formatter: (value: number) => {
          return value.toFixed(1) + ' GB'
        },
      },
    },

    series: [
      {
        name: '内存使用',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: props.data.map((item) => Number((item.value / 1024 / 1024 / 1024).toFixed(2))),
      },
    ],
  })
}

/**
 * 监听外部数据变化
 */
watch(
  () => [props.data, props.total],
  () => {
    renderChart()
  },
  {
    deep: true,
  },
)

onMounted(() => {
  renderChart()
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
.memory-chart {
  width: 100%;

  height: 300px;
}
</style>
