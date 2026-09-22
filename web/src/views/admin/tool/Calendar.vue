<template>
  <div class="calendar-container">
    <div class="calendar">
      <!-- 日历头部 -->
      <div class="calendar-header">
        <button class="nav-button" @click="changeMonth(-1)">← 上月</button>

        <div class="month-year">
          {{ monthYear }}
        </div>

        <button class="nav-button" @click="changeMonth(1)">下月 →</button>
      </div>

      <!-- 日历 -->
      <div class="calendar-grid">
        <!-- 星期 -->
        <div v-for="day in dayHeaders" :key="day" class="day-header">
          {{ day }}
        </div>

        <!-- 日期 -->
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="calendar-day"
          :class="{
            'other-month': !day.isCurrentMonth,
            today: day.isToday,
          }"
          @click="handleDayClick(day.date)"
          @contextmenu.prevent="handleContextMenu($event, day.date)"
        >
          <div class="date-number">
            {{ day.date.getDate() }}
          </div>

          <!-- 当天事件 -->
          <div class="events-container">
            <div
              v-for="event in getDayEvents(day.date)"
              :key="event.id"
              class="event-marker"
              :class="{
                'system-event': event.sourceType === 1,
                'user-event': event.sourceType === 2,
                'completed-event': event.completed === 1,
              }"
              :style="{ backgroundColor: getEventColor(event) }"
              @click.stop="handleEventClick(event)"
            >
              {{ event.title }}
            </div>
          </div>

          <!-- 事件数量 -->
          <div v-if="getDayEvents(day.date).length > 0" class="event-count">
            {{ getDayEvents(day.date).length }}
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 右键菜单 ==================== -->
    <div v-show="showContextMenu" class="context-menu" :style="contextMenuStyle">
      <div class="menu-item" @click="addEvent(1)">添加日程</div>

      <div class="menu-item" @click="addEvent(2)">添加闹钟</div>

      <div class="menu-item" @click="addEvent(3)">添加待办</div>
    </div>

    <!-- ==================== 新增/修改 Dialog ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="calendarForm" label-width="80px">
        <!-- 标题 -->
        <el-form-item
          label="标题"
          prop="title"
          :rules="[
            {
              required: true,
              message: '请输入标题',
              trigger: 'blur',
            },
          ]"
        >
          <el-input
            v-model="calendarForm.title"
            placeholder="请输入日程标题"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <!-- 类型 -->
        <el-form-item label="类型">
          <el-radio-group
            :model-value="calendarForm.recordType"
            :disabled="isEdit"
            @update:model-value="(value) => (calendarForm.recordType = Number(value))"
          >
            <el-radio :value="1"> 日程 </el-radio>

            <el-radio :value="2"> 闹钟 </el-radio>

            <el-radio :value="3"> 待办 </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 日期 -->
        <el-form-item label="日期">
          <el-date-picker
            v-model="calendarForm.eventDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 开始时间 -->
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="calendarForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 结束时间 -->
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="calendarForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 提醒时间 -->
        <el-form-item label="提醒时间">
          <el-date-picker
            v-model="calendarForm.remindTime"
            type="datetime"
            placeholder="选择提醒时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 重复 -->
        <el-form-item label="重复">
          <el-select v-model="calendarForm.repeatType" style="width: 100%">
            <el-option label="不重复" :value="0" />

            <el-option label="每天" :value="1" />

            <el-option label="每周" :value="2" />

            <el-option label="每月" :value="3" />

            <el-option label="每年" :value="4" />
          </el-select>
        </el-form-item>

        <!-- 完成状态 -->
        <el-form-item v-if="isEdit" label="完成">
          <el-switch
            :model-value="calendarForm.completed === 1"
            @update:model-value="(value) => (calendarForm.completed = value ? 1 : 0)"
            :active-value="1"
            :inactive-value="0"
            active-text="已完成"
            inactive-text="未完成"
          />
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
            v-model="calendarForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- Dialog 底部 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="isEdit" type="danger" plain @click="deleteCalendar"> 删除 </el-button>

          <div class="footer-right">
            <el-button @click="dialogVisible = false"> 取消 </el-button>

            <el-button type="primary" :loading="saveLoading" @click="saveCalendar">
              {{ isEdit ? '保存修改' : '创建日程' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- ==================== 当天事件 Dialog ==================== -->
    <el-dialog v-model="dayDialogVisible" :title="dayDialogTitle" width="500px">
      <div class="day-event-list">
        <div
          v-for="event in dayDialogEvents"
          :key="event.id"
          class="day-event-item"
          @click="handleEventClick(event)"
        >
          <div class="day-event-color" :style="{ backgroundColor: getEventColor(event) }" />

          <div class="day-event-content">
            <div class="day-event-title">
              {{ event.title }}
            </div>

            <div class="day-event-info">
              {{ getRecordTypeName(event) }}

              <span v-if="event.startTime"> · {{ formatTime(event.startTime) }} </span>
            </div>
          </div>

          <el-tag size="small" :type="event.sourceType === 1 ? 'info' : 'success'">
            {{ event.sourceType === 1 ? '系统' : '我的' }}
          </el-tag>
        </div>

        <el-empty v-if="dayDialogEvents.length === 0" description="当天暂无日程" />
      </div>

      <template #footer>
        <el-button type="primary" @click="openCreateDialog(dayDialogDate)"> 新增日程 </el-button>

        <el-button @click="dayDialogVisible = false"> 关闭 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

import {
  saveCalendarApi,
  selectCalendarByIdApi,
  selectCalendarMonthApi,
  updateCalendarApi,
  deleteCalendarApi,
} from '@/api/file'

interface CalendarDay {
  date: Date
  isCurrentMonth: boolean
  isToday: boolean
}

interface CalendarEvent {
  id: number
  userId: number
  sourceType: number
  recordType: number
  sourceId: number | null
  title: string
  eventDate: string
  startTime: string
  endTime: string
  remindTime: string
  completed: number
  repeatType: number
  remark: string
  sort: number
}

/**
 * 日历表单类型
 *
 * 这里显式指定类型，避免 Element Plus
 * el-radio-group / el-switch 出现
 * string | number | boolean 类型冲突。
 */
interface CalendarForm {
  id: number
  sourceType: number
  recordType: number
  sourceId: number | null
  title: string
  eventDate: string
  startTime: string
  endTime: string
  remindTime: string
  completed: number
  repeatType: number
  remark: string
  sort: number
}

/**
 * 日期格式化
 *
 * 不使用 padStart，避免 TS lib 版本问题。
 */
const formatDate = (date: Date) => {
  const year = date.getFullYear()

  const month = date.getMonth() + 1

  const day = date.getDate()

  return [year, month < 10 ? `0${month}` : String(month), day < 10 ? `0${day}` : String(day)].join(
    '-',
  )
}

/* ==================== 日历状态 ==================== */

const currentDate = ref<Date>(new Date())

const calendarEvents = ref<CalendarEvent[]>([])

const loading = ref(false)

/* ==================== Dialog ==================== */

const dialogVisible = ref(false)

const dayDialogVisible = ref(false)

const formRef = ref<FormInstance>()

const saveLoading = ref(false)

const isEdit = ref(false)

/* ==================== 当天事件 Dialog ==================== */

const dayDialogDate = ref('')

const dayDialogEvents = ref<CalendarEvent[]>([])

const dayDialogTitle = computed(() => {
  if (!dayDialogDate.value) {
    return '日程'
  }

  const date = new Date(dayDialogDate.value + 'T00:00:00')

  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

/* ==================== 表单 ==================== */

const calendarForm = ref<CalendarForm>({
  id: 0,

  sourceType: 2,

  recordType: 1,

  sourceId: null,

  title: '',

  eventDate: '',

  startTime: '',

  endTime: '',

  remindTime: '',

  completed: 0,

  repeatType: 0,

  remark: '',

  sort: 0,
})

const dialogTitle = computed(() => {
  return isEdit.value ? '修改日程' : '新增日程'
})

/* ==================== 右键菜单 ==================== */

const showContextMenu = ref(false)

const contextMenuDate = ref<Date | null>(null)

const contextMenuStyle = ref({
  left: '0px',
  top: '0px',
})

/* ==================== 星期 ==================== */

const dayHeaders = computed(() => ['日', '一', '二', '三', '四', '五', '六'])

/* ==================== 当前月份 ==================== */

const monthYear = computed(() => {
  return `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`
})

/* ==================== 生成42个日期 ==================== */

const calendarDays = computed(() => {
  const days: CalendarDay[] = []

  const startDate = getCalendarStartDate()

  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)

    date.setDate(startDate.getDate() + i)

    days.push({
      date,

      isCurrentMonth:
        date.getMonth() === currentDate.value.getMonth() &&
        date.getFullYear() === currentDate.value.getFullYear(),

      isToday: isToday(date),
    })
  }

  return days
})

/* ==================== 获取日历开始日期 ==================== */

const getCalendarStartDate = () => {
  const firstDay = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), 1)

  const startDate = new Date(firstDay)

  startDate.setDate(1 - firstDay.getDay())

  return startDate
}

/* ==================== 判断今天 ==================== */

const isToday = (date: Date) => {
  return formatDate(date) === formatDate(new Date())
}

/* ==================== 获取当天事件 ==================== */

const getDayEvents = (date: Date) => {
  const dateString = formatDate(date)

  return calendarEvents.value.filter((event) => event.eventDate === dateString)
}

/* ==================== 查询月份数据 ==================== */

const loadCalendar = async () => {
  loading.value = true

  try {
    const year = currentDate.value.getFullYear()

    const month = currentDate.value.getMonth() + 1

    const res: any = await selectCalendarMonthApi({
      year,
      month,
    })

    if (res.code === 200) {
      calendarEvents.value = res.result || []
    } else {
      calendarEvents.value = []

      ElMessage.error('获取日历失败')
    }
  } catch (error) {
    calendarEvents.value = []

    ElMessage.error('获取日历失败')
  } finally {
    loading.value = false
  }
}

/* ==================== 切换月份 ==================== */

const changeMonth = (offset: number) => {
  const newDate = new Date(currentDate.value)

  newDate.setMonth(newDate.getMonth() + offset)

  currentDate.value = newDate

  loadCalendar()
}

/* ==================== 点击日期 ==================== */

const handleDayClick = (date: Date) => {
  showContextMenu.value = false

  /*
   * 点击其他月份日期
   */
  if (
    date.getMonth() !== currentDate.value.getMonth() ||
    date.getFullYear() !== currentDate.value.getFullYear()
  ) {
    currentDate.value = new Date(date)

    loadCalendar()

    return
  }

  const dateString = formatDate(date)

  dayDialogDate.value = dateString

  dayDialogEvents.value = getDayEvents(date)

  dayDialogVisible.value = true
}

/* ==================== 点击具体事件 ==================== */

const handleEventClick = async (event: CalendarEvent) => {
  dayDialogVisible.value = false

  /*
   * 系统记录不能修改
   */
  if (event.sourceType === 1) {
    ElMessage.info(`${getRecordTypeName(event)}：${event.title}`)

    return
  }

  /*
   * 用户记录可以修改
   */
  openEditDialog(event.id)
}

/* ==================== 打开新增 Dialog ==================== */

const openCreateDialog = (date?: string) => {
  resetForm()

  isEdit.value = false

  if (date) {
    calendarForm.value.eventDate = date
  } else {
    calendarForm.value.eventDate = formatDate(currentDate.value)
  }

  dayDialogVisible.value = false

  dialogVisible.value = true
}

/* ==================== 打开右键菜单 ==================== */

const handleContextMenu = (event: MouseEvent, date: Date) => {
  contextMenuDate.value = date

  showContextMenu.value = true

  contextMenuStyle.value = {
    left: `${event.clientX}px`,
    top: `${event.clientY}px`,
  }
}

/* ==================== 右键新增 ==================== */

const addEvent = (recordType: number) => {
  showContextMenu.value = false

  const date = contextMenuDate.value

  if (!date) {
    return
  }

  resetForm()

  isEdit.value = false

  calendarForm.value.recordType = recordType

  calendarForm.value.eventDate = formatDate(date)

  dialogVisible.value = true
}

/* ==================== 打开修改 Dialog ==================== */

const openEditDialog = async (id: number) => {
  try {
    const res: any = await selectCalendarByIdApi(id)

    if (res.code !== 200) {
      ElMessage.error('获取日程失败')

      return
    }

    const data = res.result

    /*
     * 系统记录不能编辑
     */
    if (data.sourceType === 1) {
      ElMessage.info('系统记录不能修改')

      return
    }

    calendarForm.value = {
      id: data.id,

      sourceType: data.sourceType,

      recordType: Number(data.recordType),

      sourceId: data.sourceId,

      title: data.title || '',

      eventDate: data.eventDate || '',

      startTime: data.startTime || '',

      endTime: data.endTime || '',

      remindTime: data.remindTime || '',

      completed: Number(data.completed || 0),

      repeatType: Number(data.repeatType || 0),

      remark: data.remark || '',

      sort: Number(data.sort || 0),
    }

    isEdit.value = true

    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取日程失败')
  }
}

/* ==================== 保存 ==================== */

const saveCalendar = async () => {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  saveLoading.value = true

  try {
    const params = {
      ...calendarForm.value,

      sourceType: 2,

      sourceId: null,

      startTime: calendarForm.value.startTime || null,

      endTime: calendarForm.value.endTime || null,

      remindTime: calendarForm.value.remindTime || null,
    }

    let res: any

    if (isEdit.value) {
      res = await updateCalendarApi(params)
    } else {
      res = await saveCalendarApi(params)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '创建成功')

      dialogVisible.value = false

      await loadCalendar()
    } else {
      ElMessage.error(res.result || (isEdit.value ? '修改失败' : '创建失败'))
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '修改失败' : '创建失败')
  } finally {
    saveLoading.value = false
  }
}

/* ==================== 删除 ==================== */

const deleteCalendar = async () => {
  if (!calendarForm.value.id) {
    return
  }

  try {
    await ElMessageBox.confirm('删除后无法恢复，确定要删除这条日程吗？', '删除日程', {
      confirmButtonText: '确定删除',

      cancelButtonText: '取消',

      type: 'warning',
    })

    const res: any = await deleteCalendarApi(calendarForm.value.id)

    if (res.code === 200) {
      ElMessage.success('删除成功')

      dialogVisible.value = false

      await loadCalendar()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

/* ==================== 重置表单 ==================== */

const resetForm = () => {
  calendarForm.value = {
    id: 0,

    sourceType: 2,

    recordType: 1,

    sourceId: null,

    title: '',

    eventDate: formatDate(currentDate.value),

    startTime: '',

    endTime: '',

    remindTime: '',

    completed: 0,

    repeatType: 0,

    remark: '',

    sort: 0,
  }
}

/* ==================== 类型名称 ==================== */

const getRecordTypeName = (event: CalendarEvent) => {
  if (event.sourceType === 1) {
    if (event.recordType === 1) {
      return '文章'
    }

    if (event.recordType === 2) {
      return '文档'
    }

    if (event.recordType === 3) {
      return '日记'
    }
  }

  if (event.sourceType === 2) {
    if (event.recordType === 1) {
      return '日程'
    }

    if (event.recordType === 2) {
      return '闹钟'
    }

    if (event.recordType === 3) {
      return '待办'
    }
  }

  return '事项'
}

/* ==================== 事件颜色 ==================== */

const getEventColor = (event: CalendarEvent) => {
  /*
   * 系统记录
   */
  if (event.sourceType === 1) {
    return '#909399'
  }

  /*
   * 日程
   */
  if (event.recordType === 1) {
    return '#2c7be5'
  }

  /*
   * 闹钟
   */
  if (event.recordType === 2) {
    return '#e74c3c'
  }

  /*
   * 待办
   */
  return '#2ecc71'
}

/* ==================== 时间显示 ==================== */

const formatTime = (value: string) => {
  if (!value) {
    return ''
  }

  if (value.length >= 16) {
    return value.substring(11, 16)
  }

  return value
}

/* ==================== 初始化 ==================== */

onMounted(() => {
  document.addEventListener('click', () => {
    showContextMenu.value = false
  })

  loadCalendar()
})
</script>

<style scoped>
.calendar-container {
  margin-top: 18px;
  margin-left: 33px;
  margin-right: 49px;
}

.calendar {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.month-year {
  font-size: 20px;
  color: #1a1a1a;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.nav-button {
  border: 2px solid #e0e0e0;
  background: none;
  border-radius: 4px;
  padding: 6px 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.nav-button:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: white;
}

.day-header {
  padding: 14px 16px;
  text-align: center;
  background: var(--header-bg);
  color: var(--header-text);
  font-weight: 500;
  font-size: 14px;
  border-bottom: 1px solid var(--border-color);
}

.calendar-day {
  height: 80px;
  padding: 12px;
  border-right: 1px solid rgba(var(--el-color-primary-rgb), 0.2);
  border-bottom: 1px solid rgba(var(--el-color-primary-rgb), 0.2);
  position: relative;
  background: white;
  cursor: pointer;
}

.calendar-day:nth-child(7n + 1) {
  border-left: 1px solid var(--border-color);
}

.calendar-day.other-month {
  background: #fafafa;
  color: #999;
}

.calendar-day:hover {
  background: var(--hover-bg);
  box-shadow: inset 0 0 0 2px var(--primary-color);
  z-index: 1;
}

.today {
  background: #f0f7ff !important;
}

.date-number {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.events-container {
  max-height: 80px;
  overflow: hidden;
  position: relative;
}

.events-container::after {
  content: '⋯';
  position: absolute;
  bottom: 0;
  right: 0;
  background: linear-gradient(to right, transparent, white 50%);
  padding-left: 20px;
}

.event-marker {
  font-size: 12px;
  padding: 4px 8px;
  margin: 4px 0;
  border-radius: 4px;
  color: white;
  display: flex;
  align-items: center;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  cursor: pointer;
}

.event-marker:hover {
  opacity: 0.85;
}

.system-event {
  opacity: 0.8;
}

.user-event {
  font-weight: 500;
}

.completed-event {
  text-decoration: line-through;
  opacity: 0.6;
}

.event-count {
  position: absolute;
  top: 6px;
  right: 6px;
  background: var(--primary-color);
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ==================== 右键菜单 ==================== */

.context-menu {
  position: fixed;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  z-index: 1001;
  min-width: 120px;
  overflow: hidden;
}

.menu-item {
  padding: 9px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:hover {
  background: var(--hover-bg);
}

/* ==================== 当天事件 Dialog ==================== */

.day-event-list {
  max-height: 400px;
  overflow-y: auto;
}

.day-event-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  background: #f8f9fa;
  cursor: pointer;
  transition: background 0.2s;
}

.day-event-item:hover {
  background: #f0f2f5;
}

.day-event-color {
  width: 4px;
  height: 36px;
  border-radius: 2px;
  margin-right: 12px;
}

.day-event-content {
  flex: 1;
  min-width: 0;
}

.day-event-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.day-event-info {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-right {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .calendar-container {
    margin-left: 10px;
    margin-right: 10px;
  }

  .calendar-day {
    height: 80px;
    padding: 8px;
  }

  .date-number {
    font-size: 12px;
  }

  .event-marker {
    font-size: 10px;
    padding: 2px 4px;
  }

  .day-header {
    padding: 12px;
    font-size: 13px;
  }
}
</style>
