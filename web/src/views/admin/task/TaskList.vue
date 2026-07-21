<template>
  <div>
    <el-card style="margin: 18px 2%; width: 95%">
      <el-button type="primary" plain @click="clickCreateTaskFun">创建任务</el-button>
      <!-- <el-popover :visible="deleteBtnVisible" placement="top" :width="160">
        <p>删除所选日记？</p>
        <div style="text-align: right; margin: 0">
          <el-button size="small" text @click="deleteBtnVisible = false">取消</el-button>
          <el-button size="small" type="primary" @click="deleteDiaryFun(0)">删除</el-button>
        </div>
        <template #reference>
          <el-button :disabled="ids.length > 0 ? false : true" type="danger" @click="deleteBtnVisible = true" plain>删除</el-button>
        </template>
      </el-popover> -->

      <el-table :data="taskList.data" stripe style="width: 100%; height: calc(100vh - 328px)">
        <!-- <el-table-column type="selection" width="55"/> -->
        <el-table-column prop="taskName" label="任务名称" fit />
        <el-table-column prop="taskCode" label="任务编码" width="245" />
        <el-table-column prop="taskCount" label="执行次数" width="110" />
        <el-table-column prop="taskStatus" label="任务状态" width="110">
          <template #default="scope">
            {{ taskStatus(scope.row.taskStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="taskTrigger" label="执行类型" width="110">
          <template #default="scope">
            {{ taskTrigger(scope.row.taskTrigger) }}
          </template>
        </el-table-column>
        <el-table-column prop="taskTime" label="执行时间" width="160" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="最近更新" fit />
        <el-table-column fixed="right" label="操作" width="110">
          <template #default="scope">
            <el-button
              style="margin: 0; padding: 8px"
              @click="clickViewTaskFun(scope.row)"
              size="small"
              text
            >
              <MyIcon type="icon-edit" />
            </el-button>
            <el-button
              style="margin: 0; padding: 8px"
              @click="deleteTaskFun(scope.row.id)"
              size="small"
              text
            >
              <MyIcon type="icon-delete" />
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin: 20px 0 50px 0">
        <el-pagination
          background
          style="float: right"
          layout="total, prev, pager, next, jumper"
          @current-change="selectTaskListFun"
          :page-size="size"
          :total="total"
        >
        </el-pagination>
      </div>

      <el-dialog v-model="createTaskDialog" title="创建任务" width="760px">
        <div>
          <el-form :model="taskItem" label-position="left" label-width="100px">
            <el-form-item label="任务名称">
              <el-input
                v-model="taskItem.taskName"
                placeholder="请输入任务名称"
                style="width: 300px"
              ></el-input>
            </el-form-item>
            <el-form-item label="任务类型">
              <el-select
                v-model="selectTaskType"
                value-key="code"
                placeholder="请选择任务类型"
                @change="taskTypeChange"
                style="width: 250px"
              >
                <el-option
                  v-for="item in baseTaskList.data"
                  :key="item.code"
                  :label="item.name"
                  :value="item"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="任务状态">
              <el-switch
                v-model="taskItem.taskStatus"
                active-text="启用"
                active-value="1"
                active-color="#13ce66"
                inactive-text="禁用"
                inactive-value="2"
                inactive-color="#ff4949"
                style="padding-left: 20px"
              />
            </el-form-item>

            <el-form-item label="执行次数">
              <el-input-number v-model="taskItem.taskCount" />
            </el-form-item>

            <el-form-item label="触发方式">
              <el-select
                v-model="taskItem.taskTrigger"
                placeholder="选择触发方式"
                style="width: 250px"
              >
                <el-option label="指定时间" :value="1" />
                <el-option label="延时" :value="2" />
                <el-option label="cron表达式" :value="3" />
              </el-select>
            </el-form-item>
            <!-- 指定时间 -->
            <el-form-item v-if="taskItem.taskTrigger === 1" label="执行时间">
              <el-date-picker
                v-model="taskItem.taskTime"
                type="datetime"
                placeholder="请选择执行时间"
                style="width: 300px"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>

            <!-- 延时 -->
            <el-form-item v-else-if="taskItem.taskTrigger === 2" label="延时时间">
              <el-input-number v-model="taskItem.taskTime" :min="1" style="width: 150px" />
              <span style="margin-left: 10px">秒</span>
            </el-form-item>

            <!-- cron -->
            <el-form-item v-else-if="taskItem.taskTrigger === 3" label="Cron表达式">
              <el-input
                v-model="taskItem.taskTime"
                placeholder="例如： 0 0/5 * * * ?"
                style="width: 300px"
              />
            </el-form-item>

            <el-form-item label="任务参数">
              <el-form :model="taskItem.paramJson" label-position="left" label-width="150px">
                <el-form-item
                  v-for="item in selectTaskType.paramTemplate"
                  :key="item.paramName"
                  :label="item.name"
                  style="margin-bottom: 10px"
                >
                  <!-- 文本 -->
                  <el-input
                    v-if="item.type === 'input'"
                    v-model="taskItem.paramJson[item.paramName]"
                    :maxlength="item.length"
                    style="width: 350px"
                  />
                  <!-- 数字 -->
                  <el-input-number
                    v-else-if="item.type === 'input-number'"
                    v-model="taskItem.paramJson[item.paramName]"
                    :min="item.min"
                    :max="item.max"
                    style="width: 150px"
                  />
                </el-form-item>
              </el-form>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveTaskFun">创建</el-button>
              <el-button @click="createTaskDialog = false">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import icon from '@/utils/icon'
import {
  insertTaskApi,
  updateTaskApi,
  deleteTaskApi,
  selectTaskBaseApi,
  startTaskApi,
  selectTaskListApi,
} from '@/api/file'

import mixin from '@/mixins/task'

let { taskStatus, taskTrigger } = mixin()
let { MyIcon } = icon()

let {
  size,
  total,
  taskList,
  taskItem,
  createTaskDialog,
  baseTaskList,
  selectTaskType,

  selectTaskListFun,
  clickCreateTaskFun,
  clickViewTaskFun,
  taskTypeChange,
  saveTaskFun,
  deleteTaskFun,
} = taskFn()

/**
 * 页面初始化
 */
onMounted(() => {
  selectTaskListFun(1)
})

/**
 * 角色删改查接口方法合集
 */
function taskFn(): any {
  // 页面展示任务条数
  let size = ref<number>(10)
  // 总任务数
  let total = ref<number>(0)
  // 基础任务数据
  let taskList: any = reactive({ data: [] })
  // 单任务对象 （展示、创建任务）
  let taskItem: any = reactive({ paramJson: {} })
  // 创建任务弹窗
  let createTaskDialog = ref(false)
  // 基础任务列表
  let baseTaskList: any = reactive({ data: [] })
  // 当前选中基础任务
  let selectTaskType: any = ref({
    code: '',
    paramTemplate: [],
  })

  /**
   *查询任务列表
   * @param page 查询页
   */
  const selectTaskListFun = (page: any) => {
    selectTaskListApi({
      pageNum: page,
      pageSize: size.value,
    }).then((res: any) => {
      if (res.code === 200) {
        taskList.data = res.result.list
        total.value = res.result.total
      }
    })
  }

  /**
   * 查询基础任务列表
   */
  const selectTaskBaseFun = async () => {
    const res: any = await selectTaskBaseApi()
    baseTaskList.data = res.result
  }

  /**
   * 基础任务参数模板转换为json数据
   * @param value 当前选中基础任务
   */
  const taskTypeChange = (value: any) => {
    selectTaskType.value = {
      ...value,
      paramTemplate: value.paramTemplate ? (typeof value.paramTemplate === 'string' ? JSON.parse(value.paramTemplate) : value.paramTemplate) : [],
    }
  }

  /**
   * 点击创建任务按钮 打开弹窗
   */
  const clickCreateTaskFun = () => {
    createTaskDialog.value = true
    selectTaskBaseFun()
  }

  /**
   * 点击保存任务按钮 关闭弹窗
   */
  const clickSaveTaskFun = () => {
    createTaskDialog.value = false
    resetTaskItem()
  }

  /**
   * 重置任务对象
   */
  const resetTaskItem = () => {
    Object.keys(taskItem).forEach((key) => {
      delete taskItem[key]
    })

    Object.assign(taskItem, {
      paramJson: {},
    })
  }

  /**
   *点击查看任务按钮 打开弹窗
   * @param row 当前行
   */
  const clickViewTaskFun = async (row: any) => {
    await selectTaskBaseFun()
    Object.assign(taskItem, {
      ...row,
      taskTrigger: Number(row.taskTrigger),
      taskTime: row.taskTrigger === 2 ? Number(row.taskTime) : row.taskTime,
      paramJson: row.paramJson ? JSON.parse(row.paramJson) : {},
    })

    for (const baseTask of baseTaskList.data) {
      if (baseTask.code === row.taskCode) {
        taskTypeChange(baseTask)
        break
      }
    }

    createTaskDialog.value = true
  }

  /**
   *保存任务
   */
  const saveTaskFun = async () => {
    if (taskItem.id != null && taskItem.id !== '') {
      await updateTaskFun()
    } else {
      await insertTaskFun()
    }
    clickSaveTaskFun()
    selectTaskListFun(1)
  }

  /**
   *创建任务
   */
  const insertTaskFun = async () => {
    await insertTaskApi({
      taskName: taskItem.taskName,
      taskCode: taskItem.taskCode,
      taskStatus: taskItem.taskStatus,
      taskCount: taskItem.taskCount,
      taskTrigger: taskItem.taskTrigger,
      taskTime: taskItem.taskTime,
      paramJson: JSON.stringify(taskItem.paramJson),
    }).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('保存成功')
      }
    })
  }

  /**
   *修改任务
   */
  const updateTaskFun = async () => {
    await updateTaskApi({
      id: taskItem.id,
      taskName: taskItem.taskName,
      taskCode: taskItem.taskCode,
      taskStatus: taskItem.taskStatus,
      taskCount: taskItem.taskCount,
      taskTrigger: taskItem.taskTrigger,
      taskTime: taskItem.taskTime,
      paramJson: JSON.stringify(taskItem.paramJson),
    }).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('修改成功')
      }
    })
  }

  /**
   * 删除任务
   * @param id 任务id
   */
  const deleteTaskFun = (id: number) => {
    deleteTaskApi(id).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        selectTaskListFun(1)
      }
    })
  }

  return {
    size,
    total,
    taskList,
    taskItem,
    createTaskDialog,
    baseTaskList,
    selectTaskType,

    selectTaskListFun,
    clickCreateTaskFun,
    taskTypeChange,
    clickViewTaskFun,
    saveTaskFun,
    updateTaskFun,
    deleteTaskFun,
  }
}
</script>

<style scoped></style>
