<template>
  <div class="device-page" @contextmenu.prevent="openMenu($event, null)">
    <!-- 设备列表 -->

    <el-card
      v-for="device in deviceList.data"
      :key="device.id"
      class="device-card"
      @contextmenu.prevent.stop="openMenu($event, device)"
    >
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

    <!-- 新增设备 -->

    <el-dialog
      v-model="dialogFormVisible"
      title="新增服务器设备"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="deviceRef" :model="device" :rules="deviceRules">
        <el-form-item label="设备名称" :label-width="formLabelWidth" prop="deviceName">
          <el-input v-model="device.deviceName" />
        </el-form-item>

        <el-form-item label="设备编码" :label-width="formLabelWidth" prop="deviceCode">
          <el-input v-model="device.deviceCode" />
        </el-form-item>

        <el-form-item label="设备位置" :label-width="formLabelWidth" prop="devicePosition">
          <el-input v-model="device.devicePosition" />
        </el-form-item>

        <el-form-item label="备注信息" :label-width="formLabelWidth" prop="memo">
          <el-input v-model="device.memo" />
        </el-form-item>

        <el-form-item label="时间模板" :label-width="formLabelWidth" prop="timeTemplate">
          <el-select v-model="device.timeTemplate" placeholder="选择时间模板">
            <el-option label="时间模板1" :value="1" />

            <el-option label="时间模板2" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogFormVisible = false"> 取消 </el-button>

        <el-button type="primary" @click="addDeviceFun"> 保存 </el-button>
      </template>
    </el-dialog>

    <!-- 右键菜单 -->

    <ul
      v-if="menu.visible"
      class="contextmenu"
      :style="{
        left: menu.left + 'px',
        top: menu.top + 'px',
      }"
    >
      <li @click="refreshDevice">刷新</li>

      <li v-if="menu.type === 1">修改设备信息</li>

      <li @click="menu.visible = false">关闭菜单</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

import { selectDeviceListApi, saveDeviceApi } from '@/api/file'

import { ElMessage } from 'element-plus'

import mixin from '@/mixins/device'

const { deviceStatus } = mixin()

const emit = defineEmits(['deviceId'])

const {
  dialogFormVisible,
  formLabelWidth,
  device,
  menu,
  deviceRules,
  deviceList,
  openMenu,
  refreshDevice,
  getDeviceListFun,
  openDevice,
  addDeviceFun,
} = deviceFun()

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
  const deviceRef: any = ref(null)

  const dialogFormVisible = ref(false)

  const formLabelWidth = '90px'

  const device = reactive({
    deviceName: '',
    deviceCode: '',
    devicePosition: '',
    memo: '',
    timeTemplate: '',
  })

  const menu: any = reactive({
    visible: false,

    left: 0,

    top: 0,

    type: 0,

    device: null,
  })

  const deviceList: any = reactive({
    data: [],
  })

  const deviceRules = {
    deviceName: [
      {
        required: true,
        message: '请输入设备名称',
        trigger: 'blur',
      },
    ],

    deviceCode: [
      {
        required: true,
        message: '请输入设备编码',
        trigger: 'blur',
      },
    ],

    devicePosition: [
      {
        required: true,
        message: '请输入设备位置',
        trigger: 'blur',
      },
    ],

    memo: [
      {
        required: true,
        message: '请输入备注',
        trigger: 'blur',
      },
    ],

    timeTemplate: [
      {
        required: true,
        message: '请选择模板',
        trigger: 'blur',
      },
    ],
  }

  const openMenu = (e: any, item: any) => {
    menu.visible = true

    menu.left = e.clientX

    menu.top = e.clientY

    if (item) {
      menu.device = item

      menu.type = 1
    } else {
      menu.device = null

      menu.type = 0
    }
  }

  const refreshDevice = () => {
    menu.visible = false

    getDeviceListFun()
  }

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

  const addDeviceFun = () => {
    deviceRef.value.validate((valid: any) => {
      if (valid) {
        saveDeviceApi({
          deviceName: device.deviceName,

          deviceCode: device.deviceCode,

          devicePosition: device.devicePosition,

          memo: device.memo,

          timeTemplate: device.timeTemplate,
        }).then((res: any) => {
          if (res.code === 200) {
            ElMessage.success('设备创建成功')

            getDeviceListFun()
          }
        })

        dialogFormVisible.value = false
      }
    })
  }

  return {
    deviceRef,
    dialogFormVisible,
    formLabelWidth,
    device,
    menu,
    deviceRules,
    deviceList,
    openMenu,
    refreshDevice,
    getDeviceListFun,
    openDevice,
    addDeviceFun,
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
