<!-- <template>
  <div>
    <el-card>
      <el-table
        @expand-change="selectTaskLogByTaskUUIDFun"
        row-key="taskUUID"
        :data="taskLogList.data"
        stripe
        style="height: 75vh"
      >
        <el-table-column type="expand">
          <template #default="props">
            <div m="4">
              <el-table :data="props.row.childLog" border stripe style="width: 93%; float: right">
                <el-table-column label="任务执行结果" prop="taskResultStatus" width="120">
                  <template #default="scope">
                    <el-tag v-if="scope.row.taskResultStatus === 1" type="success"> 成功 </el-tag>
                    <el-tag v-if="scope.row.taskResultStatus === 0" type="warning"> 失败 </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="任务执行返回数据">
                  <template #default="scope">
                    {{ scope.row.taskResult }}
                  </template>
                </el-table-column>

                <el-table-column label="上报时间" prop="startTime" width="160" />
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="childTaskCode" label="子任务编码" />
        <el-table-column prop="indexCount" label="当前执行次数" width="110" />
        <el-table-column prop="taskCount" label="任务执行总数" width="110">
          <template #default="scope">
            {{ scope.row.taskCount != -1 ? scope.row.taskCount : '∞' }}
          </template>
        </el-table-column>
        <el-table-column prop="logCount" label="日志数量" width="110" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="消耗时间" width="150">
          <template #default="scope">
            {{ diffFormat(scope.row.startTime, scope.row.endTime) }}
          </template>
        </el-table-column>
      </el-table>
      <div style="margin: 20px 0 50px 0">
        <el-pagination
          background
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          style="float: right"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="pageChange"
          @size-change="sizeChange"
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
import { selectTaskLogListApi, selectTaskLogByTaskUUIDApi } from '@/api/file'
import icon from '@/utils/icon'
import timeFormat from '@/utils/timeFormat'

let { diffFormat } = timeFormat()
let { page, size, total, taskLogList, pageChange, sizeChange, selectTaskLogByTaskUUIDFun } =
  taskLogFn()

let { MyIcon } = icon()

onMounted(() => {
  pageChange(1)
})

/**
 * 用户增删改查操作方法合集
 */
function taskLogFn(): any {
  // 分页参数
  let page = ref<number>(1)
  let size = ref<number>(20)
  let total = ref<number>(0)

  // 用户列表
  let taskLogList: any = reactive({ data: [] })

  /**
   * 页数修改查询数据
   */
  const pageChange = (page: any) => {
    getTaskLogList(page)
  }

  /**
   * 页大小修改查询数据
   */
  const sizeChange = (size: any) => {
    getTaskLogList(1)
  }

  /**
   * 分页获取任务日志
   */
  const getTaskLogList = (page: any) => {
    selectTaskLogListApi({ pageNum: page, pageSize: size.value }).then((res: any) => {
      if (res.code === 200) {
        taskLogList.data = res.result.list
        total.value = res.result.total
      }
    })
  }

  /**
   *
   * @param row
   * @param expandedRows
   */
  const selectTaskLogByTaskUUIDFun = (row: any, expandedRows: any[]) => {
    console.log(row)
    selectTaskLogByTaskUUIDApi({
      taskUUID: row.taskUUID,
    }).then((res: any) => {
      console.log(res)
      if (res.code === 200) {
        row.childLog = res.result
      }
    })
  }

  return {
    page,
    size,
    total,
    taskLogList,
    pageChange,
    sizeChange,
    getTaskLogList,
    selectTaskLogByTaskUUIDFun,
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
</style> -->
