<template>
    <div class="dashboard">
        <div class="left-dashboard">
            <div class="cpu-container">
                <div class="resource-card">
                    <div class="card-header">
                        <div class="card-icon cpu-icon">
                            <MyIcon type="icon-setting" />
                        </div>
                        <div>
                            <h2 class="card-title">CPU 使用率</h2>
                            <p class="card-subtitle">实时处理器负载监控</p>
                        </div>
                    </div>
                    <ResourceChart type="gauge" :data="cpuData" color="#ffad50" height="150px" />
                </div>

                <div class="resource-card">
                    <div class="card-header">
                        <div class="card-icon memory-icon">
                            <MyIcon type="icon-setting" />
                        </div>
                        <div>
                            <h2 class="card-title">内存 使用率</h2>
                            <p class="card-subtitle">系统内存分配监控</p>
                        </div>
                    </div>
                    <ResourceChart type="gauge" :data="memoryData" color="#ffad50" height="150px" />
                </div>
            </div>
            <div class="memory-container">
                <div class="trend-card">
                    <h3>
                        <MyIcon type="icon-setting" />
                        CPU 使用趋势
                    </h3>
                    <ResourceChart type="line" :data="cpuTrendData" color="#4da6ff" height="220px" />
                </div>

                <div class="trend-card">
                    <h3>
                        <MyIcon type="icon-setting" /> 内存 使用趋势
                    </h3>
                    <ResourceChart type="line" :data="memoryTrendData" color="#ffad50" height="220px" />
                </div>
            </div>
        </div>
        <div class="right-dashboard">
            <!-- 表头 -->
            <ul class="table-header">
              <li
                v-for="(header, index) in headers"
                :key="index"
                class="header-item"
                :style="{ width: header.width }"
              >
                {{ header.title }}
              </li>
            </ul>
            <!-- 目录数据行 -->
            <ul v-for="(row, rowIndex) in dirList.data" :key="rowIndex" class="table-row">
              <div style="display: flex; width: 100%; position: relative; cursor: pointer;">
                <li class="data-item" :style="{ width: headers[0].width }" >
                  <MyIcon type="icon-user" /> {{ row.dirName }}
                </li>
                <li class="data-item" :style="{ width: headers[1].width }">

                </li>
                <li class="data-item" :style="{ width: headers[2].width }">目录</li>
                <li class="data-item" :style="{ width: headers[3].width }">
                  {{ row.createBy }}
                </li>
                <li class="data-item" :style="{ width: headers[4].width }">
                  {{ row.createTime }}
                </li>
                <li class="data-item" :style="{ width: headers[5].width }">
                  <MyIcon type="icon-user" /> <MyIcon type="icon-user" />
                </li>
              </div>
            </ul>
        </div>
    </div>

</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount,reactive } from 'vue';
import ResourceChart from '@/components/admin/device/ResourceChart.vue';
import icon from '@/utils/icon'


let headers = [
  { title: '挂载点', width: '25%' },
  { title: '分区大小', width: '17%' },
  { title: '使用容量', width: '16%' },
  { title: '剩余容量', width: '13%' },
  { title: '百分比', width: '16%' },
  { title: '操作', width: '13%' },
]

 let dirList: any = reactive({ data: [] })

let { MyIcon } = icon()
// 状态类计算
const cpuStatusClass = ref('normal');
const memoryStatusClass = ref('normal');

// 当前时间
const lastUpdateTime = ref(new Date().toLocaleTimeString());

// CPU数据
const cpuData:any = ref({
    value: 45.7,
    peak: 72.3,
    status: 'normal'
});

// 内存数据
const memoryData = ref({
    value: 68.2,
    peak: 85.1,
    status: 'warning'
});

// 生成趋势数据
const generateTrendData = (baseValue, max = 100) => {
    const now:any = new Date();
    const timeData = [];
    const values = [];

    for (let i = 59; i >= 0; i--) {
        const time = new Date(now - i * 60000);
        timeData.push(time.getHours() + ':' + (time.getMinutes() < 10 ? '0' : '') + time.getMinutes());
        values.push(Math.max(10, Math.min(max, baseValue + (Math.random() - 0.5) * 15)));
    }

    return { timeData, values };
};

// CPU趋势数据
const cpuTrendData = ref(generateTrendData(45, 100));

// 内存趋势数据
const memoryTrendData = ref(generateTrendData(65, 100));

// 更新状态类
const updateStatusClasses = () => {
    cpuStatusClass.value =
        cpuData.value.value > 80 ? 'critical' :
            cpuData.value.value > 60 ? 'warning' : 'normal';

    memoryStatusClass.value =
        memoryData.value.value > 85 ? 'critical' :
            memoryData.value.value > 70 ? 'warning' : 'normal';
};

// 模拟实时数据更新
let updateInterval:any;
onMounted(() => {
    updateInterval = setInterval(() => {
        // 更新仪表盘数据
        cpuData.value.value = 0;
        memoryData.value.value = 0;

        // 更新峰值
        if (cpuData.value.value > cpuData.value.peak) {
            cpuData.value.peak = cpuData.value.value;
        }

        if (memoryData.value.value > memoryData.value.peak) {
            memoryData.value.peak = memoryData.value.value;
        }

        // 更新时间
        lastUpdateTime.value = new Date().toLocaleTimeString();

        // 更新趋势数据（模拟）
        const newCpuTrend = [...cpuTrendData.value.values];
        const newMemoryTrend = [...memoryTrendData.value.values];

        newCpuTrend.shift();
        newCpuTrend.push(cpuData.value.value);

        newMemoryTrend.shift();
        newMemoryTrend.push(memoryData.value.value);

        cpuTrendData.value = {
            timeData: [...cpuTrendData.value.timeData],
            values: newCpuTrend
        };

        memoryTrendData.value = {
            timeData: [...memoryTrendData.value.timeData],
            values: newMemoryTrend
        };

        // 更新状态类
        updateStatusClasses();
    }, 5000);

    // 初始状态类更新
    updateStatusClasses();
});

// 清理定时器
onBeforeUnmount(() => {
    clearInterval(updateInterval);
});
</script>

<style scoped>
.dashboard {
    color: #e0e6ff;
    padding: 20px;
    display: flex;
}

.left-dashboard {}

.right-dashboard {
    margin-left: 50px;
    width: 700px;
}

/* 表格行通用样式 */
.table-header,
.table-row {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
  padding-left: 10px;
  box-sizing: border-box; /* 关键对齐属性 */
}

/* 表头样式 */
.table-header {
  /* background-color: #f8f9fa; */
  font-size: 20px;
  /* border-bottom: 2px solid #dee2e6; */
}

/* 单元格通用样式 */
.header-item,
.data-item {
  padding-top: 12px;
  padding-bottom: 12px;
  min-height: 15px;
  display: flex;
  align-items: center;
  overflow: hidden; /* 处理超长内容 */
  white-space: nowrap;
  text-overflow: ellipsis;
}

.cpu-container {
    display: flex;
    flex-wrap: wrap;
    gap: 25px;
    margin-bottom: 25px;
}

.resource-card {
    flex: 1;
    min-width: 300px;
    border-radius: 16px;
    padding: 25px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.05);
    transition: transform 0.3s ease;
}

.resource-card:hover {
    transform: translateY(-5px);
}

.card-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}

.card-icon {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    margin-right: 20px;
    font-size: 28px;
}

.cpu-icon {
    background: rgba(77, 166, 255, 0.15);
    color: #4da6ff;
}

.memory-icon {
    background: rgba(255, 173, 80, 0.15);
    color: #ffad50;
}

.card-title {
    font-size: 1.8rem;
    font-weight: 600;
    margin-bottom: 5px;
}

.card-subtitle {
    color: #8c9bdd;
    font-weight: 300;
}

.stats {
    display: flex;
    justify-content: space-around;
    margin-top: 20px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 12px;
    padding: 15px 0;
}

.disk-container {
    display: flex;
    flex-wrap: wrap;
    gap: 25px;
    margin-bottom: 20px;
}

.stat {
    text-align: center;
}

.stat-label {
    font-size: 24px;
}

.stat-value {
    font-size: 24px;
    font-weight: 600;
}

.memory-container {
    display: flex;
    flex-wrap: wrap;
    gap: 25px;
    margin-bottom: 20px;
}

.trend-card {
    flex: 1;
    min-width: 300px;
    background: rgba(30, 35, 60, 0.7);
    border-radius: 16px;
    padding: 25px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.05);
}

.trend-card h3 {
    font-size: 1.4rem;
    margin-bottom: 20px;
    color: #aab8ff;
    display: flex;
    align-items: center;
}

.trend-icon {
    margin-right: 10px;
    color: #4da6ff;
}

.trend-card:nth-child(2) .trend-icon {
    color: #ffad50;
}

.status-indicator {
    display: inline-block;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    margin-top: 8px;
}

.status-normal {
    background-color: #2ecc71;
}

.status-warning {
    background-color: #f39c12;
}

.status-critical {
    background-color: #e74c3c;
}
</style>
