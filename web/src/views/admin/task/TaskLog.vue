<template>
  <div>
    <el-card class="task-log-card">
      <el-table
        :data="taskLogList.data"
        stripe
        border
        row-key="taskUuid"
        style="width: 100%; height: calc(100vh - 328px)"
        @expand-change="handleExpand"
      >
        <!-- 展开 -->
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-title">执行明细</div>
              <el-empty
                v-if="!row.detailLoading && row.detailList.length === 0"
                description="暂无执行明细"
              />
              <template v-else>
                <el-table
                  :data="row.detailList"
                  border
                  stripe
                  size="small"
                  v-loading="row.detailLoading"
                >
                  <el-table-column prop="taskName" label="任务名称" width="220" />
                  <el-table-column prop="taskResult" label="任务结果" fit />
                  <el-table-column prop="errorMsg" label="错误信息" fit />
                  <el-table-column prop="startTime" label="开始时间" width="180" />
                  <el-table-column prop="endTime" label="结束时间" width="180" />
                </el-table>
                <div class="expand-pagination" v-if="row.detailTotal > row.detailSize">
                  <el-pagination
                    background
                    layout="total, prev, pager, next, jumper"
                    v-model:current-page="row.detailPage"
                    :page-size="row.detailSize"
                    :total="row.detailTotal"
                    @current-change="() => selectTaskLogByTaskUuidFun(row)"
                  />
                </div>
              </template>
            </div>
          </template>
        </el-table-column>
        <!-- 外表 -->
        <el-table-column prop="taskName" label="任务名称" min-width="280" />
        <el-table-column prop="taskResultStatus" label="任务结果" width="120">
          <template #default="{ row }">
            <span>{{ taskResult(row.taskResultStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="logCount" label="日志数量" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
      </el-table>
      <div class="table-pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :page-size="size"
          :total="total"
          @current-change="selectTaskLogListFun"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { selectTaskLogListApi, selectTaskLogByTaskUuidApi } from '@/api/file'
import mixin from '@/mixins/task'

let { taskResult } = mixin()
const {
  size,
  total,
  taskLogList,

  selectTaskLogListFun,
  selectTaskLogByTaskUuidFun,
  handleExpand,
} = taskExecFn()

/**
 * 初始化
 */
onMounted(() => {
  selectTaskLogListFun(1)
})

/**
 * 任务日志方法集合
 */
function taskExecFn() {
  // 外表分页大小
  const size = ref<number>(10)

  // 外表总数量
  const total = ref<number>(0)

  // 外表数据
  const taskLogList = reactive<any>({
    data: [],
  })

  /**
   * 查询外表任务日志
   * @param pageNum 页数
   */
  const selectTaskLogListFun = async (pageNum: number) => {
    const res: any = await selectTaskLogListApi({
      pageNum,
      pageSize: size.value,
    })

    if (res.code === 200) {
      taskLogList.data = res.result.list.map((item: any) => ({
        ...item,
        // 内表数据
        detailList: [],
        // 内表总数
        detailTotal: 0,
        // 当前页
        detailPage: 1,
        // 每页数量
        detailSize: 10,
        // 加载状态
        detailLoading: false,
        // 是否加载过
        detailLoaded: false,
      }))
      total.value = res.result.total
    }
  }

  /**
   * 根据 taskUuid 查询内表数据
   * @param row 展开行
   */
  const selectTaskLogByTaskUuidFun = async (row: any) => {
    row.detailLoading = true
    try {
      const res: any = await selectTaskLogByTaskUuidApi({
        taskUuid: row.taskUuid,
        pageNum: row.detailPage,
        pageSize: row.detailSize,
      })
      if (res.code === 200) {
        row.detailList = res.result.list
        row.detailTotal = res.result.total
        row.detailLoaded = true
      }
    } finally {
      row.detailLoading = false
    }
  }

  /**
   * 展开事件
   * @param row 当前点击展开/收起的这一行
   * @param expandedRows 当前所有已经展开的行组成的数组
   */
  const handleExpand = (row: any, expandedRows: any[]) => {
    const expanded = expandedRows.some((item) => item.taskUuid === row.taskUuid)
    // 收起
    if (!expanded) {
      return
    }
    // 第一次展开查询第一页
    row.detailPage = 1
    selectTaskLogByTaskUuidFun(row)
  }

  return {
    size,
    total,
    taskLogList,

    selectTaskLogListFun,
    selectTaskLogByTaskUuidFun,
    handleExpand,
  }
}
</script>

<style scoped>
.task-log-card {
  margin: 18px 2%;
  width: 95%;
}

/* 展开区域 */
.expand-content {
  padding: 18px 42px 16px;
  background: #fafafa;
}

/* 标题 */
.expand-title {
  margin-bottom: 14px;
  padding-left: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid var(--el-color-primary);
}

/* 内表分页 */
.expand-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 外表分页 */
.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
