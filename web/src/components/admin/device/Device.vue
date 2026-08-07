<template>
  <div class="device-page">
    <!-- 设备列表 -->
    <el-card v-for="device in deviceList.data" :key="device.id" class="device-card">
      <div class="card-header">
        <div class="device-title">
          <el-tag type="primary"> 设备 </el-tag>
          <span>
            {{ device.deviceName }}
          </span>
        </div>
        <el-button type="primary" text @click="openDevice(device)"> 打开设备 </el-button>
      </div>
      <div class="device-content">
        <div class="info-item">
          <label> 设备编码 </label>
          <span>
            {{ showText(device.deviceCode, 15) }}
          </span>
        </div>
        <div class="info-item">
          <label> 设备位置 </label>
          <span>
            {{ device.devicePosition }}
          </span>
        </div>
        <div class="info-item">
          <label> 设备状态 </label>
          <el-tag v-if="device.deviceStatus === 1" type="success">
            {{ deviceStatus(device.deviceStatus) }}
          </el-tag>
          <el-tag v-else type="warning">
            {{ deviceStatus(device.deviceStatus) }}
          </el-tag>
        </div>
        <!-- 备注信息 独占一行 -->
        <div class="info-item full-row">
          <label> 备注信息 </label>
          <span>
            {{ device.memo }}
          </span>
        </div>
        <!-- 时间 同一行 -->
        <div class="time-row">
          <div class="info-time">
            创建时间：
            {{ device.createTime }}
          </div>
          <div class="info-time">
            修改时间：
            {{ device.updateTime }}
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { selectDeviceListApi } from '@/api/file'
import mixin from '@/mixins/device'

const { deviceStatus } = mixin()
const emit = defineEmits(['deviceId'])

const { deviceList, getDeviceListFun, openDevice } = deviceFun()

onMounted(() => {
  getDeviceListFun()
})

const showText = (text: string, max: number) => {
  if (!text) {
    return ''
  }

  return text.length > max ? text.substring(0, max) + '...' : text
}

function deviceFun() {

  const deviceList: any = reactive({
    data: [],
  })

  const getDeviceListFun = () => {
    selectDeviceListApi().then((res: any) => {
      if (res.code === 200) {
        deviceList.data = res.result
      }
    })
  }

  const openDevice = (item: any) => {
    emit('deviceId', item.id)
  }

  return {
    deviceList,
    getDeviceListFun,
    openDevice,
  }
}
</script>

<style scoped>
.device-page {
  width: 100%;

  height: 100%;

  padding: 20px;

  overflow: auto;

  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));

  gap: 20px;

  align-content: start;
}

.device-card {
  height: 220px;

  border-radius: 14px;

  transition: 0.25s;
}

.device-card:hover {
  transform: translateY(-4px);

  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 18px;
}

.device-title {
  display: flex;

  align-items: center;

  gap: 12px;

  font-size: 18px;

  font-weight: 600;
}

.device-content {
  display: grid;

  grid-template-columns: 1fr 1fr;

  gap: 14px;
}

/* 独占一行 */

.full-row {
  grid-column: 1 / -1;
}

/* 创建更新时间 */

.time-row {
  grid-column: 1 / -1;

  display: flex;

  gap: 50px;

  align-items: center;
}

.info-time {
  font-size: 12px;

  color: #999;
}

.info-item {
  display: flex;

  align-items: center;

  font-size: 14px;
}

.info-item label {
  width: 80px;

  color: #909399;
}

.info-time {
  font-size: 12px;

  color: #999;
}

.contextmenu {
  position: fixed;

  z-index: 3000;

  background: white;

  border-radius: 8px;

  padding: 6px 0;

  margin: 0;

  list-style: none;

  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.contextmenu li {
  padding: 8px 20px;

  cursor: pointer;

  font-size: 14px;
}

.contextmenu li:hover {
  background: #f5f7fa;
}
</style>
