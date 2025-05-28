<template>

    <div style="width: 100%; height: 100%; display: flex; flex-flow: wrap;overflow-y: auto; align-items:flex-start;"
        @contextmenu.prevent="">
        <el-card v-for="(device, id) in serviceList.data" :key="id" @contextmenu.prevent.stop="" style="
      margin: 18px 2%;
      width: 45%;
      height: 200px;
      margin-bottom: 20px;
    ">
            <div style="display: flex">
                <div style="width: 80%; display: flex;">
                    <div style="flex: 1;">
                        <div style="margin-bottom: 10px;">
                            <el-button @click="" type='primary' text>
                                打开设备
                            </el-button>
                        </div>
                        <div>
                            <span>设备类型：</span> <el-tag type="primary">设备</el-tag>
                        </div>
                        <div>
                            <span>设备名称：</span> <span>{{ device.deviceName }}</span>
                        </div>
                        <div>
                            <span>设备编码：</span> <span>{{ showText(device.deviceCode, 5) }}</span>
                        </div>
                    </div>
                    <div style="flex: 1;">


                        <div>
                            <span>设备位置：</span> <span>{{ device.devicePosition }}</span>
                        </div>
                        <div>
                            <span>设备状态：</span>
                            <span>
                                <el-tag v-if="device.deviceStatus === 1" type="success">{{
                                    deviceStatus(device.deviceStatus) }}</el-tag>
                                <el-tag v-if="device.deviceStatus === 0" type="warning">{{
                                    deviceStatus(device.deviceStatus) }}</el-tag>
                            </span>
                        </div>
                        <div><span>时间模板：</span> <span>模板1</span></div>
                        <div>
                            <span>备注信息：</span> <span>{{ device.memo }}</span>
                        </div>
                        <div>
                            <span>创建时间：</span> <span>{{ device.createTime }}</span>
                        </div>
                        <div>
                            <span>修改时间：</span> <span>{{ device.updateTime }}</span>
                        </div>
                    </div>
                </div>
                <div style="width: 20%">
                    <MyIcon type="icon-device" />
                </div>
            </div>
        </el-card>
    </div>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { selectDeviceListApi, saveDeviceApi } from '@/api/file';
import { ElMessage } from 'element-plus';
import mixin from '@/mixins/device';
import icon from '@/utils/icon';

let { MyIcon } = icon();


let { deviceStatus } = mixin();

let {
    serviceList
} = serviceFun();

const emit = defineEmits(['serviceId']);

onMounted(() => {

});

const showText = (text: any, maxLength: Number) => {
    if (text.length > maxLength) {
        return text.substring(0, maxLength) + '...';
    }
    return text;
};

function serviceFun() {

    // 服务器列表
    let serviceList: any = reactive({ data: [] });

    return {
        serviceList
    };
}
</script>

<style scoped>
.contextmenu {
    position: fixed;
    min-width: min-content;
    z-index: 1900;
    border: 1px solid #d4d4d5;
    line-height: 1.4285em;
    max-width: 150px;
    background: #fff;
    font-weight: 400;
    font-style: normal;
    color: rgba(0, 0, 0, 0.87);
    border-radius: 0.28571429rem;
    box-shadow: 0 2px 4px 0 rgba(34, 36, 38, 0.12),
        0 2px 10px 0 rgba(34, 36, 38, 0.15);
}

.contextmenu div {
    position: relative;
    vertical-align: middle;
    line-height: 1;
    -webkit-tap-highlight-color: transparent;
    padding: 10px 15px;
    color: rgba(0, 0, 0, 0.87);
    font-size: 14px;
    cursor: pointer;
}

.contextmenu div:hover {
    background: #eee;
}

.contextmenu {
    margin: 0;
    background: #fff;
    z-index: 3000;
    position: absolute;
    list-style-type: none;
    padding: 5px 0;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 400;
    color: #333;
    box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, 0.3);
}

.contextmenu li {
    margin: 0;
    padding: 7px 16px;
    cursor: pointer;
}

.contextmenu li:hover {
    background: #eee;
}
</style>
