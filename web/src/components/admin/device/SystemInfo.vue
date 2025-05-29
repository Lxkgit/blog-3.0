<template>
    <div style="display: flex; height: 550px; overflow: auto;">
        <div style="flex: 1; margin-right: 10px;">
            <div>
                <el-card shadow="hover">
                    <template #header>
                        <span>CPU信息</span>
                    </template>
                    <div id="trend" :style="{ 'min-height': '300px' }"></div>
                </el-card>
            </div>
            <div style="margin-top: 10px; height: 200px;">
                磁盘空间
            </div>
            <div style="margin-top: 10px; background-color: aqua;">
                单片机数据
            </div>
        </div>
        <div style="flex: 1; margin-left: 10px;">
            <el-card shadow="hover">
                <template #header>
                    <span>内存信息</span>
                </template>
                <div id="trend" :style="{ 'min-height': '300px' }"></div>
            </el-card>
        </div>
    </div>
</template>