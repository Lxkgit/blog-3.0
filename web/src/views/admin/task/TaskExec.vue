<template>
  <div>
    <el-card style="margin: 18px 2%; width: 95%">
      <el-table :data="taskExecList.data" stripe style="width: 100%; height: calc(100vh - 328px)">
        <el-table-column prop="task.taskName" label="任务名称" width="300" />
        <el-table-column prop="timerTask.uuid" label="任务UUID" width="300" />
        <el-table-column prop="timerTask.executeCount" label="当前执行次数" width="110" />
        <el-table-column prop="timerTask.createTime" label="创建时间" width="180" />
        <el-table-column prop="timerTask.triggerTime" label="执行时间" width="160" />

        <el-table-column fixed="right" label="操作" width="110">
          <template #default="scope">
            <el-button style="margin: 0; padding: 8px" @click="" size="small" text title="立即执行">
              <MyIcon type="icon-send" />
            </el-button>
            <!-- <el-button
              style="margin: 0; padding: 8px"
              @click="deleteTaskFun(scope.row.id)"
              size="small"
              text
            >
              <MyIcon type="icon-delete" />
            </el-button> -->
          </template>
        </el-table-column>
      </el-table>
      <div style="margin: 20px 0 50px 0">
        <el-pagination
          background
          style="float: right"
          layout="total, prev, pager, next, jumper"
          @current-change="runningTaskListFun"
          :page-size="size"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import icon from '@/utils/icon'
import { runningTaskListApi } from '@/api/file'

import mixin from '@/mixins/task'

let { taskStatus, taskTrigger } = mixin()
let { MyIcon } = icon()

let { size, total, taskExecList, runningTaskListFun } = taskExecFn()

/**
 * 页面初始化
 */
onMounted(() => {
  runningTaskListFun(1)
})

/**
 * 角色删改查接口方法合集
 */
function taskExecFn(): any {
  // 页面展示任务条数
  let size = ref<number>(10)
  // 总任务数
  let total = ref<number>(0)
  // 执行任务列表
  let taskExecList: any = reactive({ data: [] })
  // 单任务对象 （展示、创建任务）
  let taskExec: any = reactive({ paramJson: {} })

  const runningTaskListFun = (pageNum: number) => {
    runningTaskListApi({ pageNum, pageSize: size.value }).then((res: any) => {
      if (res.code === 200) {
        taskExecList.data = res.result.list
        total.value = res.result.total
      }
    })
  }

  return {
    size,
    total,
    taskExecList,

    runningTaskListFun,
  }
}
</script>

<style scoped></style>
