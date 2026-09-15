<template>
  <div class="sensor-control-page">
    <el-tabs v-model="activeTab" class="main-tabs" @tab-click="activeTabHandleClick">
      <el-tab-pane label="单片机数据" name="chipData"> 单片机数据 </el-tab-pane>

      <el-tab-pane label="传感器数据统计" name="sensorData"> 传感器数据统计 </el-tab-pane>

      <el-tab-pane label="批量控制传感器" name="sensorControl">
        <div class="sensor-control-container">
          <el-tabs v-model="sensorControlActiveTab" class="control-tabs">
            <el-tab-pane label="传感器控制命令" name="sensorControl">
              <div class="control-content">
                <div class="table-header">
                  <div class="table-title">
                    <span>传感器控制命令</span>
                    <el-tag size="small" type="info">
                      {{ sensorControlList.data.length }}
                    </el-tag>
                  </div>

                  <div class="table-actions">
                    <el-button type="primary" @click="openCreateDialog"> 创建命令组 </el-button>

                    <el-popover
                      :visible="deleteCommandBtnPopoverByIds"
                      placement="top"
                      :width="180"
                    >
                      <p>删除所选命令？</p>

                      <div class="delete-popover-footer">
                        <el-button size="small" text @click="deleteCommandBtnPopoverByIds = false">
                          取消
                        </el-button>

                        <el-button size="small" type="primary" @click="deleteSensorControlFun(0)">
                          删除
                        </el-button>
                      </div>

                      <template #reference>
                        <el-button
                          type="danger"
                          plain
                          :disabled="commandIds.length === 0"
                          @click="deleteCommandBtnPopoverByIds = true"
                        >
                          批量删除
                        </el-button>
                      </template>
                    </el-popover>
                  </div>
                </div>

                <el-table
                  :data="sensorControlList.data"
                  stripe
                  border
                  class="sensor-control-table"
                  empty-text="暂无传感器控制命令"
                  @selection-change="checkCommandId"
                >
                  <el-table-column type="selection" width="55" align="center" />

                  <el-table-column
                    prop="controlName"
                    label="名称"
                    min-width="180"
                    show-overflow-tooltip
                  />

                  <el-table-column label="控制传感器" min-width="300">
                    <template #default="scope">
                      <el-tag
                        v-for="item in scope.row.sensorList"
                        :key="item.id"
                        :style="'color: ' + tagColor(item.id)"
                        class="sensor-tag"
                      >
                        {{ item.sensorName }}
                      </el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column
                    prop="controlMessage"
                    label="消息内容"
                    min-width="300"
                    show-overflow-tooltip
                  />

                  <el-table-column prop="createTime" label="创建时间" width="180" />

                  <el-table-column prop="updateTime" label="最近修改时间" width="180" />

                  <el-table-column fixed="right" label="操作" width="130" align="center">
                    <template #default="scope">
                      <div class="row-actions">
                        <el-button size="small" text @click="sendSensorControlFun(scope.row.id)">
                          <MyIcon type="icon-send" title="发送命令" />
                        </el-button>

                        <el-button size="small" text @click="updateSensorControlFun(scope.row.id)">
                          <MyIcon type="icon-edit" title="修改命令" />
                        </el-button>

                        <el-button size="small" text @click="deleteSensorControlFun(scope.row.id)">
                          <MyIcon type="icon-delete" title="删除命令" />
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>

                <div class="pagination-wrapper">
                  <el-pagination
                    background
                    layout="total, prev, pager, next, jumper"
                    :page-size="10"
                    :total="100"
                  />
                </div>
              </div>
            </el-tab-pane>

            <el-dialog
              v-model="dialogFormVisible"
              title="创建命令组"
              width="900px"
              class="sensor-command-dialog"
            >
              <el-form :model="sensorControlForm" class="command-form">
                <el-form-item label="指令名称" :label-width="formLabelWidth">
                  <el-input
                    v-model="sensorControlForm.name"
                    placeholder="请输入命令组名称"
                    clearable
                  />
                </el-form-item>

                <div class="sensor-command-list">
                  <div v-for="(id, idx) in length" :key="idx" class="sensor-command-item">
                    <el-divider />

                    <div class="sensor-command-row">
                      <div class="sensor-select-wrapper">
                        <el-form-item :label="'传感器 ' + id" :label-width="formLabelWidth">
                          <el-select
                            v-model="sensorControlForm.sensor[idx].sensorData"
                            placeholder="选择传感器"
                            value-key="id"
                            class="sensor-select"
                            @change="selectSensor(idx)"
                          >
                            <el-option
                              v-for="item in sensorList.data"
                              :key="item.id"
                              :label="item.sensorName"
                              :value="item"
                            />
                          </el-select>
                        </el-form-item>
                      </div>

                      <div class="sensor-parameter-wrapper">
                        <el-form-item
                          v-if="idx !== 0"
                          label="执行前延时(ms)"
                          :label-width="formLabelWidth"
                        >
                          <el-input-number
                            v-model="sensorControlForm.sensor[idx].delay"
                            :min="1"
                            :max="10000"
                          />
                        </el-form-item>

                        <div
                          v-for="item in sensorControlForm.sensor[idx].from"
                          :key="item.key || item.label"
                        >
                          <template v-if="item.type === 'input-number'">
                            <el-form-item :label="item.label" :label-width="formLabelWidth">
                              <el-input-number
                                v-model="item.value"
                                :min="item.min"
                                :max="item.max"
                              />
                            </el-form-item>
                          </template>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="sensor-command-footer">
                  <el-button type="primary" link @click="addSensor"> + 增加传感器 </el-button>

                  <el-button type="danger" link :disabled="length <= 1" @click="deleteSensor">
                    - 删除传感器
                  </el-button>
                </div>
              </el-form>

              <template #footer>
                <div class="dialog-footer">
                  <el-button @click="dialogFormVisible = false"> 取消 </el-button>

                  <el-button type="primary" @click="saveSensorControlFun"> 确认 </el-button>
                </div>
              </template>
            </el-dialog>

            <el-tab-pane label="传感器控制历史" name="sensorControlHistory">
              <div class="control-content">
                <div class="table-header">
                  <div class="table-title">
                    <span>传感器控制历史</span>
                    <el-tag size="small" type="info">
                      {{ tableData.length }}
                    </el-tag>
                  </div>
                </div>

                <el-table
                  :data="tableData"
                  stripe
                  border
                  class="sensor-control-table"
                  empty-text="暂无控制历史"
                >
                  <el-table-column prop="date" label="时间" width="180" />

                  <el-table-column prop="name" label="名称" width="180" />

                  <el-table-column
                    prop="address"
                    label="地址"
                    min-width="300"
                    show-overflow-tooltip
                  />

                  <el-table-column fixed="right" label="操作" width="110" align="center">
                    <template #default>
                      <div class="row-actions">
                        <el-button size="small" text>
                          <MyIcon type="icon-send" title="发送命令" />
                        </el-button>

                        <el-button size="small" text>
                          <MyIcon type="icon-delete" title="删除命令" />
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>

                <div class="pagination-wrapper">
                  <el-pagination
                    background
                    layout="total, prev, pager, next, jumper"
                    :page-size="10"
                    :total="100"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TabsPaneContext } from 'element-plus'
import { ElMessage } from 'element-plus'
import icon from '@/utils/icon'
import color from '@/utils/color'

import {
  selectSensorControlListApi,
  selectSensorListApi,
  saveSensorControlApi,
  sendSensorControlApi,
  updateSensorControlApi,
  deleteSensorControlApi,
  selectSensorTemplateByChipOrSensorIdApi,
  selectSensorControlByIdApi,
} from '@/api/file'

const props = defineProps({
  chipId: Number,
})

let {
  length,
  sensorControlForm,
  sensorList,
  sensorControlList,
  deleteCommandBtnPopoverByIds,
  commandIds,
  selectSensorListFun,
  selectSensor,
  addSensor,
  deleteSensor,
  selectSensorControlListFun,
  saveSensorControlFun,
  checkCommandId,
  updateSensorControlFun,
  deleteSensorControlFun,
  selectSensorTemplateByChipOrSensorIdFun,
  selectSensorControlByIdFun,
  sendSensorControlFun,
  resetSensorControlForm,
  openCreateDialog,
} = sensorControlFun()

const { MyIcon } = icon()
const { tagColor } = color()

const activeTab = ref('sensorControl')
const sensorControlActiveTab = ref('sensorControl')

const dialogTableVisible = ref(false)
const dialogFormVisible = ref(false)

const formLabelWidth = '140px'

const formItems: any = reactive({
  data: [],
})

const duoFrom: any = [
  {
    sensorType: '',
    label: '舵机旋转角度',
    type: 'input-number',
    min: 0,
    max: 180,
    key: 'data',
    value: 0,
  },
]

const setFromItemsFun = (sensorCode: any) => {
  if (sensorCode === 'DUO-180') {
    formItems.data = duoFrom
  }
}

const form = reactive({
  name: '',
  region: '',
  date1: '',
  date2: '',
  delivery: false,
  type: [],
  resource: '',
  desc: '',
})

const tableData = [
  {
    date: '2016-05-03',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
  {
    date: '2016-05-02',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
  {
    date: '2016-05-04',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
  {
    date: '2016-05-01',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
  {
    date: '2016-05-03',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
  {
    date: '2016-05-03',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
  {
    date: '2016-05-03',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
  },
]

onMounted(() => {
  selectSensorControlListFun()
  selectSensorListFun()
  selectSensorTemplateByChipOrSensorIdFun()
})

const activeTabHandleClick = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event)
}

function sensorControlFun() {
  const length = ref(1)

  const commandIds = reactive<any[]>([])
  const deleteCommandBtnPopoverByIds = ref(false)

  const sensorControlForm = reactive({
    id: 0,
    name: '',
    sensor: [
      {
        id: 0,
        idx: 0,
        sensorData: {} as any,
        sensorType: '',
        sensorCode: '',
        delay: 0,
        from: [] as any,
      },
    ],
  })

  const resetSensorControlForm = () => {
    sensorControlForm.id = 0
    sensorControlForm.name = ''

    sensorControlForm.sensor.splice(0, sensorControlForm.sensor.length)

    sensorControlForm.sensor.push({
      id: 0,
      idx: 0,
      sensorData: {} as any,
      sensorType: '',
      sensorCode: '',
      delay: 0,
      from: [],
    })

    length.value = 1
  }

  const openCreateDialog = () => {
    resetSensorControlForm()
    dialogFormVisible.value = true
  }

  const sensorControlList: any = reactive({
    data: [],
  })

  const sensorList: any = reactive({
    data: [],
  })

  const selectSensorListFun = () => {
    selectSensorListApi({
      pageNum: 1,
      pageSize: 20,
      chipId: props.chipId,
      sensorControlType: 1,
    }).then((res: any) => {
      if (res.code === 200) {
        sensorList.data = res.result.list || []
      }
    })
  }

  const sensorTemplateFrom: any = reactive({
    data: [],
  })

  const selectSensor = (idx: number) => {
    const sensor = sensorControlForm.sensor[idx]

    if (!sensor || !sensor.sensorData) {
      return
    }

    sensor.id = sensor.sensorData.id
    sensor.sensorType = sensor.sensorData.sensorType
    sensor.sensorCode = sensor.sensorData.sensorCode
    sensor.idx = idx
    sensor.delay = idx === 0 ? 0 : 100
    sensor.from = []

    for (let i = 0; i < sensorTemplateFrom.data.length; i++) {
      if (sensor.sensorType === sensorTemplateFrom.data[i].sensorType) {
        sensor.from = JSON.parse(sensorTemplateFrom.data[i].template)
        break
      }
    }
  }

  const addSensor = () => {
    if (length.value >= 20) {
      ElMessage.error('最多只能添加 20 个传感器')
      return
    }

    sensorControlForm.sensor.push({
      id: 0,
      idx: length.value,
      sensorData: {} as any,
      sensorType: '',
      sensorCode: '',
      delay: 100,
      from: [],
    })

    length.value++
  }

  const deleteSensor = () => {
    if (length.value <= 1) {
      ElMessage.error('至少保留 1 个传感器')
      return
    }

    sensorControlForm.sensor.pop()
    length.value--
  }

  const selectSensorControlListFun = () => {
    selectSensorControlListApi({
      pageNum: 1,
      pageSize: 10,
      chipId: props.chipId,
    }).then((res: any) => {
      if (res.code === 200) {
        sensorControlList.data = res.result.list || []
      }
    })
  }

  const saveSensorControlFun = () => {
    const isCreate = sensorControlForm.id === 0

    saveSensorControlApi({
      id: isCreate ? null : sensorControlForm.id,
      chipId: props.chipId,
      commandGroup: 1,
      controlName: sensorControlForm.name,
      controlMessage: JSON.stringify(sensorControlForm.sensor),
    }).then((res: any) => {
      if (res.code === 200) {
        if (isCreate) {
          ElMessage.success('命令创建成功')
        } else {
          ElMessage.success('命令修改成功')
        }

        dialogFormVisible.value = false

        resetSensorControlForm()

        selectSensorControlListFun()
      }
    })
  }

  const checkCommandId = (val: any[]) => {
    commandIds.splice(0, commandIds.length)

    for (let i = 0; i < val.length; i++) {
      commandIds.unshift(val[i].id)
    }
  }

  const selectSensorControlByIdFun = (id: any) => {
    selectSensorControlByIdApi(id).then((res: any) => {
      if (res.code === 200) {
        sensorControlForm.id = res.result.id
        sensorControlForm.name = res.result.name
        sensorControlForm.sensor = JSON.parse(res.result.sensor)
        length.value = sensorControlForm.sensor.length
      }
    })
  }

  const updateSensorControlFun = (id: any) => {
    resetSensorControlForm()

    dialogFormVisible.value = true

    selectSensorControlByIdFun(id)
  }

  const deleteSensorControlFun = (id: any) => {
    if (id === 0) {
      deleteCommandBtnPopoverByIds.value = false

      if (commandIds.length !== 0) {
        deleteSensorControlApi({
          ids: commandIds.join(),
        }).then((res: any) => {
          if (res.code === 200) {
            ElMessage.success('命令删除成功')
            selectSensorControlListFun()
          }
        })
      }
    } else {
      deleteSensorControlApi({
        ids: id,
      }).then((res: any) => {
        if (res.code === 200) {
          ElMessage.success('命令删除成功')
          selectSensorControlListFun()
        }
      })
    }
  }

  const selectSensorTemplateByChipOrSensorIdFun = () => {
    selectSensorTemplateByChipOrSensorIdApi({
      chipId: props.chipId,
    }).then((res: any) => {
      if (res.code === 200) {
        sensorTemplateFrom.data = res.result || []
      }
    })
  }

  const sendSensorControlFun = (id: any) => {
    sendSensorControlApi({
      id,
    }).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('命令发送成功')
      }
    })
  }

  return {
    length,
    sensorControlForm,
    sensorList,
    sensorControlList,
    deleteCommandBtnPopoverByIds,
    commandIds,
    selectSensorListFun,
    selectSensor,
    addSensor,
    deleteSensor,
    selectSensorControlListFun,
    saveSensorControlFun,
    checkCommandId,
    updateSensorControlFun,
    deleteSensorControlFun,
    selectSensorTemplateByChipOrSensorIdFun,
    selectSensorControlByIdFun,
    sendSensorControlFun,
    resetSensorControlForm,
    openCreateDialog,
  }
}
</script>

<style scoped>
.sensor-control-page {
  width: 100%;
  height: calc(100vh - 230px);
  min-height: 400px;
  overflow: hidden;
  box-sizing: border-box;
}

.main-tabs {
  height: 100%;
}

.sensor-control-container {
  width: 95%;
  height: calc(100vh - 290px);
  min-height: 340px;
  display: flex;
  flex-direction: column;
}

.control-tabs {
  height: 100%;
}

.control-content {
  height: calc(100vh - 335px);
  min-height: 280px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 12px;
  flex-shrink: 0;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sensor-control-table {
  width: 100%;
  flex: 1;
  min-height: 0;
}

.sensor-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  padding: 0 0 2px;
  min-height: 32px;
  flex-shrink: 0;
}

.delete-popover-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.sensor-command-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 5px;
}

.command-form {
  width: 100%;
}

.sensor-command-list {
  height: 420px;
  overflow-y: auto;
  padding: 0 10px 0 0;
  box-sizing: border-box;
}

.sensor-command-item {
  width: 100%;
}

.sensor-command-item :deep(.el-divider) {
  margin: 12px 0 18px;
}

.sensor-command-row {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.sensor-select-wrapper {
  flex: 1;
  min-width: 0;
}

.sensor-parameter-wrapper {
  flex: 1;
  min-width: 0;
}

.sensor-select {
  width: 100%;
}

.sensor-command-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 0 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media screen and (max-width: 1000px) {
  .sensor-command-row {
    flex-direction: column;
    gap: 0;
  }

  .sensor-select-wrapper,
  .sensor-parameter-wrapper {
    width: 100%;
  }
}
</style>
