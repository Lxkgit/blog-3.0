<template>
    <SystemInfo />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { selectDeviceListApi, saveDeviceApi } from '@/api/file';
import { ElMessage } from 'element-plus';
import mixin from '@/mixins/device';
import icon from '@/utils/icon';
import SystemInfo from '@/components/admin/device/SystemInfo.vue';

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
    let serviceList: any = reactive({
        data: [
            {
                "serviceName": "云服务器",
                "memo": "11",
                "createTime": "11",
                "updateTime": "11",
            }
        ]
    });

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
