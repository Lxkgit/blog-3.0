<template>
  <!-- 设备信息 -->

  <div class="device-wrapper">
    <el-card class="device-card">
      <div class="card-header">
        <div class="device-title">
          <el-tag type="primary"> 设备 </el-tag>
          <span>
            {{ device.deviceName }}
          </span>
        </div>
        <el-button type="primary" text @click="openDeviceInfo(device.id)"> 查看设备详情 </el-button>
      </div>

      <div class="device-content">
        <div class="info-item">
          <label> 设备编码 </label>
          <span>
            {{ device.deviceCode }}
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

        <div class="info-item">
          <label> 时间模板 </label>
          <span>
            {{ device.timeTemplate }}
          </span>
        </div>

        <!-- 备注独占 -->
        <div class="info-item full-row">
          <label> 备注信息 </label>

          <span>
            {{ device.memo }}
          </span>
        </div>

        <!-- 时间同行 -->
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

  <!-- 单片机列表 -->

  <div class="chip-container">
    <el-card v-for="chip in chipList.data" :key="chip.id" class="chip-card" @click="openChip(chip)">
      <div class="chip-header">
        <div class="chip-title">
          <el-tag type="success"> 单片机 </el-tag>

          <span>
            {{ chip.chipName }}
          </span>
        </div>

        <el-button type="success" text @click.stop="openChip(chip)"> 打开 </el-button>
      </div>

      <div class="chip-content">
        <div class="info-item">
          <label> 类型 </label>

          <span>
            {{ chip.chipType }}
          </span>
        </div>

        <div class="info-item">
          <label> 编码 </label>

          <span>
            {{ chip.chipCode }}
          </span>
        </div>

        <div class="info-item">
          <label> 状态 </label>

          <el-tag v-if="chip.chipStatus === 1" type="success">
            {{ deviceStatus(chip.chipStatus) }}
          </el-tag>

          <el-tag v-else type="warning">
            {{ deviceStatus(chip.chipStatus) }}
          </el-tag>
        </div>

        <!-- 备注独占 -->

        <div class="info-item full-row">
          <label> 备注信息 </label>

          <span>
            {{ chip.memo }}
          </span>
        </div>

        <!-- 时间同行 -->

        <div class="time-row">
          <div class="info-time">
            创建时间：
            {{ chip.createTime }}
          </div>

          <div class="info-time">
            修改时间：
            {{ chip.updateTime }}
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

import { selectDeviceByDeviceIdApi, selectChipListApi, saveChipApi } from '@/api/file'

import { ElMessage } from 'element-plus'
import mixin from '@/mixins/device'

const { deviceStatus } = mixin()
const props = defineProps({
  deviceId: Number,
})

const emit = defineEmits(['chipId', 'deviceId'])

let {
  chipRef,

  dialogFormVisible,

  formLabelWidth,

  chipRules,

  device,

  chip,

  chipList,

  selectDeviceByIdFun,

  selectChipListFun,

  openDeviceInfo,

  openChip,

  addChipFun,
} = chipFun()

onMounted(() => {
  selectDeviceByIdFun(props.deviceId)

  selectChipListFun()
})

function chipFun() {
  const chipRef: any = ref(null)

  const dialogFormVisible = ref(false)

  const formLabelWidth = '100px'

  const chipRules = {
    chipName: [
      {
        required: true,

        message: '请输入单片机名称',

        trigger: 'blur',
      },
    ],

    chipCode: [
      {
        required: true,

        message: '请输入单片机编码',

        trigger: 'blur',
      },
    ],

    chipType: [
      {
        required: true,

        message: '请输入单片机类型',

        trigger: 'blur',
      },
    ],

    memo: [
      {
        required: true,

        message: '请输入单片机备注信息',

        trigger: 'blur',
      },
    ],
  }

  // 设备信息

  let device: any = ref({})

  // 新增单片机

  const chip = reactive({
    chipName: '',

    chipCode: '',

    chipType: '',

    memo: '',
  })

  // 单片机列表

  let chipList: any = reactive({
    data: [],
  })

  const selectDeviceByIdFun = (id: any) => {
    selectDeviceByDeviceIdApi({
      id: id,
    }).then((res: any) => {
      if (res.code === 200) {
        device.value = res.result
      }
    })
  }

  const selectChipListFun = () => {
    selectChipListApi({
      pageNum: 1,

      pageSize: 10,

      deviceId: props.deviceId,
    }).then((res: any) => {
      if (res.code === 200) {
        chipList.data = res.result.list
      }
    })
  }

  const openDeviceInfo = (id: any) => {
    emit(
      'deviceId',

      id,
    )
  }

  const openChip = (item: any) => {
    emit(
      'chipId',

      item.id,
    )
  }

  const addChipFun = () => {
    if (chipRef.value !== null) {
      chipRef.value.validate((valid: any) => {
        if (valid) {
          saveChipApi({
            chipName: chip.chipName,

            chipCode: chip.chipCode,

            deviceId: props.deviceId,

            chipType: chip.chipType,

            memo: chip.memo,
          }).then((res: any) => {
            if (res.code === 200) {
              ElMessage.success('单片机创建成功')

              selectChipListFun()
            }
          })

          dialogFormVisible.value = false

          chip.chipName = ''

          chip.chipCode = ''

          chip.chipType = ''

          chip.memo = ''
        }
      })
    }
  }

  return {
    chipRef,

    dialogFormVisible,

    formLabelWidth,

    chipRules,

    device,

    chip,

    chipList,

    selectDeviceByIdFun,

    selectChipListFun,

    openDeviceInfo,

    openChip,

    addChipFun,
  }
}
</script>

<style scoped>
/* =========================
   页面基础
========================= */

.device-wrapper {
  width: 100%;

  margin-top: 20px;
}

.device-card {
  width: 70%;

  min-width: 600px;

  max-width: 1000px;

  margin-left: 2%;

  border-radius: 14px;
}

.device-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

/* =========================
   标题区域
========================= */

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

/* =========================
   信息区域
========================= */

.device-content,
.chip-content {
  display: grid;

  grid-template-columns: repeat(2, 1fr);

  gap: 15px;
}

.info-item {
  display: flex;

  align-items: center;

  min-height: 24px;

  font-size: 14px;

  color: #606266;
}

.info-item label {
  width: 90px;

  flex-shrink: 0;

  color: #909399;
}

.info-item span {
  word-break: break-all;
}

/*
  备注信息独占一行
*/

.full-row {
  grid-column: 1 / -1;
}

/* =========================
   创建更新时间
========================= */

.time-row {
  grid-column: 1 / -1;

  display: flex;

  align-items: center;

  gap: 60px;
}

.info-time {
  font-size: 12px;

  color: #999;
}

/* =========================
   单片机列表
========================= */

.chip-container {
  width: 100%;

  margin: 20px 2%;

  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));

  gap: 20px;
}

.chip-card {
  height: 230px;

  border-radius: 14px;

  cursor: pointer;

  transition: 0.25s;
}

.chip-card:hover {
  transform: translateY(-5px);

  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

/* =========================
   单片机标题
========================= */

.chip-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 18px;
}

.chip-title {
  display: flex;

  align-items: center;

  gap: 12px;

  font-size: 16px;

  font-weight: 600;
}

/* =========================
   响应式
========================= */

@media screen and (max-width: 900px) {
  .device-content,
  .chip-content {
    grid-template-columns: 1fr;
  }

  .time-row {
    flex-direction: column;

    align-items: flex-start;

    gap: 8px;
  }

  .chip-container {
    grid-template-columns: 1fr;
  }
}
</style>
