<template>
  <div>
    <div class="title_style">
      <span>定时任务</span>
    </div>
    <el-card style="margin: 18px 2%;width: 95%">
      <el-table @expand-change="selectTaskEntityByIdFun"  row-key="taskCode" :expand-row-keys="expandedRowKeys" :data="baseTaskList.data"
        style="width: 100%; height: calc(100vh - 246px);">
        <el-table-column type="expand">
          <template #default="props">
            <div m="4">
              <el-table :data="props.row.childTaskList" border stripe style="width: 93%; float: right;">
                <el-table-column label="任务编码" prop="childTaskCode" />

                <el-table-column label="当前执行次数" prop="taskCount" />
                <el-table-column label="总执行次数" prop="taskCount" />

                <el-table-column label="创建时间" prop="createTime" />
                <el-table-column label="修改时间" prop="updateTime" />
                <el-table-column fixed="right" label="操作" width="100">
                  <template #default="scope">
                    <el-button @click="startTaskFun(scope.row)" size="small" text >
                      <MyIcon type="icon-send" />
                    </el-button>
                    <el-button style="margin-left: 0;" @click="" size="small" text>
                      <MyIcon type="icon-delete" />
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="taskName" label="任务名称" fit>
        </el-table-column>

        <el-table-column fixed="right" label="操作" width="100">
          <template #default="scope">
            <el-button @click="show(scope.row)" size="small" text>
              <MyIcon type="icon-edit" />
            </el-button>
            <el-button style="margin-left: 0;" @click.native.prevent="" size="small" text>
              <MyIcon type="icon-delete" />
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import icon from '@/utils/icon'
import {
  selectTaskBaseListApi,
  selectTaskEntityByIdApi,
  startTaskApi
} from '@/api/file'
let { MyIcon } = icon()

let {
  baseTaskList,
  expandedRowKeys,
  show,
  startTaskFun,
  selectTaskBaseListFun,
  selectTaskEntityByIdFun
} = taskFn()


/**
 * 页面初始化
 */
onMounted(() => {
  selectTaskBaseListFun()
})

/**
 * 角色删改查接口方法合集
 */
function taskFn(): any {

  // 基础任务数据
  let baseTaskList: any = reactive({ data: [] })

  let expandedRowKeys: any [];


  // 查询基础任务
  const selectTaskBaseListFun = () => {
    selectTaskBaseListApi().then((res: any) => {
      if (res.code === 200) {
        baseTaskList.data = res.result
      }
    })
  }

  // 查询子任务
  const selectTaskEntityByIdFun = (row: any, expandedRows: any[]) => {
    console.log(row)
      // 更新expandedRowKeys
    expandedRowKeys = expandedRows.map(item => item.taskCode);
    selectTaskEntityByIdApi(
      {
        "taskCode": row.taskCode
      }
    ).then((res: any) => {
      console.log(res)
      if (res.code === 200) {
        row.childTaskList = res.result
      }
    })
  }

    // 查询子任务
  const show = (row: any) => {
    console.log(row)
    ElMessage.info(row.taskName)

  }

   // 查询子任务
  const startTaskFun = (row: any) => {
    startTaskApi(
      {
        "childTaskCode": row.childTaskCode
      }
    ).then((res: any) => {
      console.log(res)
      if (res.code === 200) {
        ElMessage.success("任务:{" + row.childTaskCode + "}启动成功")
      }
    })
  }






  return {
    baseTaskList,
    expandedRowKeys,
    show,
    startTaskFun,
    selectTaskBaseListFun,
    selectTaskEntityByIdFun

  }
}
</script>

<style scoped>
.title_style {
  display: flex;
  justify-content: flex-start;
  align-items: baseline;
  max-height: 31px;
  color: #445160;
  font-size: 24px;
  font-weight: 600;
  text-align: left;
}
</style>
