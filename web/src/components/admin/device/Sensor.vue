<template>
  <!-- 单片机信息 -->

  <div class="chip-wrapper">
    <el-card class="chip-detail-card">
      <div class="card-header">
        <div class="chip-title">
          <el-tag type="success"> 单片机 </el-tag>
          <span>
            {{ chip.chipName }}
          </span>
        </div>
        <el-button type="success" text @click="openChipInfo(props.chipId)">
          查看单片机详情
        </el-button>
      </div>

      <div class="device-content">
        <div class="info-item">
          <label> 单片机编码 </label>
          <span>
            {{ chip.chipCode }}
          </span>
        </div>

        <div class="info-item">
          <label> 单片机状态 </label>

          <el-tag v-if="chip.chipStatus === 1" type="success">
            {{ deviceStatus(chip.chipStatus) }}
          </el-tag>

          <el-tag v-else type="warning">
            {{ deviceStatus(chip.chipStatus) }}
          </el-tag>
        </div>

        <!-- 备注 -->
        <div class="info-item full-row">
          <label> 备注信息 </label>
          <span>
            {{ chip.memo }}
          </span>
        </div>

        <!-- 时间 -->
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

  <!-- 传感器列表 -->
  <div class="sensor-container">
    <el-card
      v-for="sensor in sensorList.data"
      :key="sensor.id"
      class="sensor-card"
      @click="openSensor(sensor)"
    >
      <div class="sensor-header">
        <div class="sensor-title">
          <el-tag type="danger"> 传感器 </el-tag>
          <span>
            {{ sensor.sensorName }}
          </span>
        </div>
        <el-button type="danger" text @click.stop="openSensor(sensor)"> 打开 </el-button>
      </div>

      <div class="device-content">
        <div class="info-item">
          <label> 传感器编码 </label>
          <span>
            {{ sensor.sensorCode }}
          </span>
        </div>

        <div class="info-item">
          <label> 类型 </label>
          <span>
            {{ sensor.sensorType }}
          </span>
        </div>

        <div class="info-item full-row">
          <label> 备注信息 </label>
          <span>
            {{ sensor.memo }}
          </span>
        </div>

        <div class="time-row">
          <div class="info-time">
            创建时间：
            {{ sensor.createTime }}
          </div>
          <div class="info-time">
            修改时间：

            {{ sensor.updateTime }}
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  selectChipByIdApi,
  selectSensorListApi,
  selectSensorTypeListApi,
  saveSensorApi,
} from '@/api/file'
let { chip, sensorList, openChipInfo, selectChipByIdFun, selectSensorListFun, openSensor } =
  sensorFun()
import mixin from '@/mixins/device'

const { deviceStatus } = mixin()
const emit = defineEmits(['sensor', 'chipId'])

const props = defineProps({
  chipId: Number,
})

onMounted(() => {
  selectChipByIdFun(props.chipId)
  selectSensorListFun()
})

function sensorFun() {
  // 新增设备表单对象
  const sensorRef: any = ref(null)
  // 新增设备dialog
  const dialogFormVisible = ref(false)
  const formLabelWidth = '100px'
  // 新增设备信息
  const sensor = reactive({
    sensorName: '',
    sensorCode: '',
    sensorTypeId: '',
    memo: '',
  })

  // 新增设备表单验证规则
  const sensorRules = {
    sensorName: [
      {
        required: true,
        message: '请输入设备名称',
        trigger: 'blur',
      },
    ],
    sensorCode: [
      {
        required: true,
        message: '请输入设备编码',
        trigger: 'blur',
      },
    ],
    sensorTypeId: [
      {
        required: true,
        message: '请输入设备位置',
        trigger: 'blur',
      },
    ],
    memo: [
      {
        required: true,
        message: '请输入设备备注信息',
        trigger: 'blur',
      },
    ],
  }
  // 单片机信息
  let chip: any = ref({})

  // 传感器列表
  let sensorList: any = reactive({ data: [] })

  // 传感器列表
  let sensorTypeList: any = reactive({ data: [] })

  // 获取单片机信息
  const selectChipByIdFun = (id: any) => {
    selectChipByIdApi({ id: id }).then((res: any) => {
      if (res.code === 200) {
        chip.value = res.result
      }
    })
  }

  const openChipInfo = (chipId: any) => {
    emit('chipId', chipId)
  }

  // 查询单片机下全部传感器
  const selectSensorListFun = () => {
    selectSensorListApi({
      pageNum: 1,
      pageSize: 10,
      chipId: props.chipId,
    }).then((res: any) => {
      if (res.code === 200) {
        console.log(res.result.list)
        sensorList.data = res.result.list
      }
    })
  }

  // 查询支持的全部传感器类型
  const selectSensorTypeListFun = () => {
    selectSensorTypeListApi().then((res: any) => {
      sensorTypeList.data = res.result
    })
  }

  // 打开传感器
  const openSensor = (sensor: any) => {
    emit('sensor', sensor)
  }

  return {
    sensorRef,
    dialogFormVisible,
    formLabelWidth,
    sensor,
    sensorRules,
    chip,
    sensorList,
    sensorTypeList,
    openChipInfo,
    selectChipByIdFun,
    selectSensorListFun,
    selectSensorTypeListFun,
    openSensor,
  }
}
</script>

<style scoped>
/* 单片机详情 */

.chip-wrapper {
  width: 100%;

  margin-top: 20px;
}

.chip-detail-card {
  width: 45%;

  min-width: 420px;

  margin-left: 2%;

  border-radius: 14px;

  transition: 0.25s;
}

.chip-detail-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

/* 通用头部 */

.card-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 18px;
}

.chip-title,
.sensor-title {
  display: flex;

  align-items: center;

  gap: 12px;

  font-size: 18px;

  font-weight: 600;
}

/* 信息 */

.device-content {
  display: grid;

  grid-template-columns: repeat(2, 1fr);

  gap: 15px;
}

.info-item {
  display: flex;

  align-items: center;

  font-size: 14px;

  color: #606266;
}

.info-item label {
  width: 100px;

  flex-shrink: 0;

  color: #909399;
}

.full-row {
  grid-column: 1/-1;
}

.time-row {
  grid-column: 1/-1;

  display: flex;

  gap: 60px;
}

.info-time {
  font-size: 12px;

  color: #999;
}

/* 传感器 */

.sensor-container {
  margin: 20px 2%;

  display: grid;

  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));

  gap: 20px;
}

.sensor-card {
  height: 180px;

  border-radius: 14px;

  cursor: pointer;

  transition: 0.25s;
}

.sensor-card:hover {
  transform: translateY(-5px);

  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.sensor-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 18px;
}

@media screen and (max-width: 900px) {
  .chip-detail-card {
    width: 95%;

    min-width: 0;
  }

  .device-content {
    grid-template-columns: 1fr;
  }

  .time-row {
    flex-direction: column;

    gap: 8px;
  }

  .sensor-container {
    grid-template-columns: 1fr;
  }
}
</style>
