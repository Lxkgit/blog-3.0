<template>
  <div class="device-monitor">
    <el-tabs v-model="activeTab" class="device-tabs" @tab-click="activeTabHandleClick">
      <el-tab-pane label="设备信息" name="deviceInfo">
        <div class="monitor-grid">
          <!-- CPU -->

          <el-card class="monitor-card cpu-card" shadow="hover">
            <template #header>
              <div class="card-title">CPU信息</div>
            </template>

            <div ref="cpuChart" class="chart-box"></div>
          </el-card>

          <!-- 内存 -->

          <el-card class="monitor-card" shadow="hover">
            <template #header>
              <div class="card-title">内存信息</div>
            </template>

            <div ref="memoryChart" class="chart-box"></div>
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
      </el-tab-pane>

      <el-tab-pane label="单片机数据" name="chipData">
        <div class="empty-page">暂无数据</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
//@ts-nocheck
import { onMounted, reactive, ref, watch } from 'vue'
import type { TabsPaneContext } from 'element-plus'
import { selectDeviceInfoByIdApi } from '@/api/file'

import dark from '@/utils/dark'
import SystemInfo from '@/components/admin/device/SystemInfo.vue'

let { deviceInfoList, getDeviceInfoListFun } = deviceFun()

let { isDark } = dark()
// 页面第一层tab标签
const activeTab = ref('deviceInfo')
// 传感器控制页面tab标签
const sensorControlActiveTab = ref('deviceInfo')

// 页面第一层tab标签点击事件
const activeTabHandleClick = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event)
}

const props = defineProps({
  deviceId: Number,
})

// 监听切换主题色事件
watch(
  () => isDark.value,
  (newVal) => {
    console.log('切换颜色了')
    console.log(newVal)
    setColor()
    trend()
    article()
    note()
    time()
  },
  { deep: true },
)

onMounted(() => {
  getDeviceInfoListFun(props.deviceId)
  console.log('dark', isDark.value)
  setColor()

  // statisticsData()
  trend()

  // note()
  // time()
})

// echarts明亮模式曲线颜色
const echartsLight = ref([
  '#008dd4',
  '#f1c40f',
  '#2ecc71',
  '#f2b3c9',
  '#16a085',
  '#e67e22',
  '#008dd0',
  '#c22931',
  '#8e44ad',
  '#157623',
])
// echarts暗黑模式曲线颜色
const echartsDark = ref([
  '#e1605e',
  '#3498db',
  '#658f95',
  '#e9937e',
  '#7cb9a0',
  '#f0734f',
  '#eed875',
  '#62996a',
  '#5db0b3',
  '#1abc9c',
])
// echarts背景色
const bgc = ref()
// echarts颜色
const color = ref()
// echarts文本颜色
const text = ref()
// 设置echarts主题色
const setColor = () => {
  console.log('判断是否是深色模式了')
  console.log(isDark.value)
  if (isDark.value === true) {
    // bgc.value = '#1d1e1f'
    color.value = echartsDark.value
    text.value = '#b2b2b2'
  } else {
    // bgc.value = '#ffffff'
    color.value = echartsLight.value
    text.value = '#2a2b2d'
  }
  console.log(bgc.value)
}

// 浏览趋势折线图
async function trend() {
  const query = { chart: 'trend', user: 1 }
  // const chartData = await getUserEcharts(query)
  const chartData: any[] = [
    {
      article_view: 1,
      article_collect: 2,
      article_comment: 3,
      section_view: 4,
      section_collect: 5,
      section_comment: 6,
    },
    {
      article_view: 11,
      article_collect: 12,
      article_comment: 13,
      section_view: 14,
      section_collect: 15,
      section_comment: 16,
    },
    {
      article_view: 11,
      article_collect: 12,
      article_comment: 13,
      section_view: 14,
      section_collect: 15,
      section_comment: 16,
    },
  ]
  console.log('trend', chartData)
  const date = []
  const article_view = []
  const article_collect = []
  const article_comment = []
  const section_view = []
  const section_collect = []
  const section_comment = []
  for (let i in chartData) {
    // date.push(chartData[i].date.slice(5))
    article_view.push(chartData[i].article_view)
    article_collect.push(chartData[i].article_collect)
    article_comment.push(chartData[i].article_comment)
    section_view.push(chartData[i].section_view)
    section_collect.push(chartData[i].section_collect)
    section_comment.push(chartData[i].section_comment)
  }
  let myChart: any
  if (isDark.value) {
    myChart = echarts.init(document.getElementById('trend') as HTMLElement, 'dark')
  } else {
    myChart = echarts.init(document.getElementById('trend') as HTMLElement)
  }
  // 绘制图表
  myChart.setOption({
    color: color.value,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    legend: {
      data: ['浏览文章数', '收藏文章数', '评论文章数', '浏览笔记数', '收藏笔记数', '评论笔记数'],
      textStyle: {
        color: text.value,
      },
    },
    grid: {
      left: '3%',
      right: '5%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: date,
      },
    ],
    yAxis: [
      {
        type: 'value',
      },
    ],
    series: [
      {
        name: '浏览文章数',
        type: 'line',
        emphasis: {
          focus: 'series',
        },
        label: {
          show: true,
          position: 'bottom',
          textStyle: {
            fontSize: 20,
          },
        },
        data: article_view,
      },
      {
        name: '收藏文章数',
        type: 'line',
        emphasis: {
          focus: 'series',
        },
        data: article_collect,
      },
      {
        name: '评论文章数',
        type: 'line',
        emphasis: {
          focus: 'series',
        },
        data: article_comment,
      },
      {
        name: '浏览笔记数',
        type: 'line',
        emphasis: {
          focus: 'series',
        },
        data: section_view,
      },
      {
        name: '收藏笔记数',
        type: 'line',
        emphasis: {
          focus: 'series',
        },
        data: section_comment,
      },
      {
        name: '评论笔记数',
        type: 'line',
        emphasis: {
          focus: 'series',
        },
        data: section_collect,
      },
    ],
    backgroundColor: bgc.value,
  })
  //自适应大小
  window.onresize = function () {
    myChart.resize()
  }
}

function deviceFun() {
  // 设备详细信息列表
  let deviceInfoList: any = reactive({ data: [] })

  // 获取设备详细信息列表
  const getDeviceInfoListFun = (id: any) => {
    selectDeviceInfoByIdApi({ id: id }).then((res: any) => {
      if (res.code === 200) {
        deviceInfoList.data = res.result
      }
    })
  }

  return {
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

  overflow: auto;
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
