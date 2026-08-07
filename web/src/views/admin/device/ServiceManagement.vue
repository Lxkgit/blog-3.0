<template>
  <div>
    <el-card style="height: 85vh; overflow-y: auto;">
      <div style="height: 30px; ">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item style="cursor: pointer;"
            @click="showType = 0">云服务器</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType >= 0"
            @click="showType = 1">服务器设备</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType >= 2"
            @click="showType = 2">单片机</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType == 3">设备详情</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType >= 4"
            @click="showType = 4">传感器</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType == 5"
            @click="showType = 5">单片机详情</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType == 6">传感器数据</el-breadcrumb-item>
          <el-breadcrumb-item style="cursor: pointer;" v-if="showType == 7">传感器控制</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div style="display: flex; flex-flow: wrap;  align-items:flex-start;">
        <Service v-if="showType === 0"/>
        <Device v-if="showType === 1" @deviceId="receiveDeviceId" />
        <Chip v-else-if="showType === 2" :deviceId="deviceId" @chipId="receiveChipId" @deviceId="receiveDeviceInfoId" />
        <DeviceInfo v-else-if="showType === 3" :deviceId="deviceId" />
        <Sensor v-else-if="showType === 4" :chipId="chipId" @sensor="receiveSensor" @chipId="receiveChipInfoId" />
        <ChipInfo v-else-if="showType === 5" :chipId="chipId" />
        <SensorData v-else-if="showType === 6" :sensor="sensorData" />
        <SensorControl v-else-if="showType === 7" :sensor="sensorData" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Service from '@/components/admin/device/Service.vue';
import Device from '@/components/admin/device/Device.vue';
import DeviceInfo from '@/components/admin/device/DeviceInfo.vue';
import Chip from '@/components/admin/device/Chip.vue';
import ChipInfo from '@/components/admin/device/ChipInfo.vue';
import Sensor from '@/components/admin/device/Sensor.vue';
import SensorData from '@/components/admin/device/SensorData.vue';
import SensorControl from '@/components/admin/device/SensorControl.vue';


let showType = ref(1);
let deviceId = ref(0);
let chipId = ref(0);
let sensorData = ref({});

// 获取选中的设备id
const receiveDeviceId = (id: any) => {
  showType.value = 2;
  deviceId.value = id;
};

const receiveDeviceInfoId = (id: any) => {
  showType.value = 3;
  deviceId.value = id;
};

const receiveChipId = (id: any) => {
  showType.value = 4;
  chipId.value = id;
};

const receiveChipInfoId = (id: any) => {
  showType.value = 5;
  chipId.value = id;
};

const receiveSensor = (sensor: any) => {
  if (sensor.sensorTypeObj.sensorControlType === 0) {
    showType.value = 6;
  } else if (sensor.sensorTypeObj.sensorControlType === 1) {
    showType.value = 7;
  }
  sensorData.value = sensor;
};
</script>

<style scoped>

</style>
