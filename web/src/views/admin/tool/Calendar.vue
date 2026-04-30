<template>
  <div>
    <div>
      <div class="calendar">
        <div class="calendar-header">
          <button class="nav-button" @click="changeMonth(-1)">← 上月</button>
          <div class="month-year">{{ monthYear }}</div>
          <button class="nav-button" @click="changeMonth(1)">下月 →</button>
        </div>

        <div class="calendar-grid">
          <div v-for="day in dayHeaders" :key="day" class="day-header">
            {{ day }}
          </div>
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
            <div class="date-number">{{ day.date.getDate() }}</div>
            <div class="events-container">
              <div
                v-for="event in getDayEvents(day.date)"
                :key="event.title"
                class="event-marker"
                :style="{ backgroundColor: event.color }"
              >
                {{ event.title }}
              </div>
            </div>
            <div v-if="getDayEvents(day.date).length > 0" class="event-count">
              {{ getDayEvents(day.date).length }}
            </div>
          </div>
        </div>
      </div>

      <!-- 事件详情弹窗 -->
      <!-- <div v-if="showEventModal" class="modal" @click.self="closeModal">
                    <div class="modal-close" @click="closeModal">×</div>
                    <h3>{{ modalDateString }}</h3>
                    <div class="modal-events">
                        <div v-for="event in modalEvents" :key="event.title" class="event-marker"
                            :style="{ backgroundColor: event.color }">
                            {{ event.title }}
                        </div>
                    </div>
                </div> -->

      <!-- 右键菜单 -->
      <div v-show="showContextMenu" class="context-menu" :style="contextMenuStyle">
        <div class="menu-item" @click="addEvent('meeting')">添加会议</div>
        <div class="menu-item" @click="addEvent('reminder')">添加提醒</div>
        <div class="menu-item" @click="addEvent('custom')">自定义事项</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface CalendarDay {
  date: Date
  isCurrentMonth: boolean
  isToday: boolean
}

const formatDate = (date: Date) => {
  return [date.getFullYear(), (date.getMonth() + 1).toString(), date.getDate().toString()].join('-')
}

// 响应式状态
const currentDate = ref(new Date())
const events = ref([
  {
    date: formatDate(new Date()),
    title: '今日事项-1',
    color: '#2c7be5',
    type: 'custom',
  },
])
const showEventModal = ref(false)
let showContextMenu = ref(false)
const modalDate = ref<Date>(new Date())
const contextMenuDate = ref<Date | null>(null)
const contextMenuStyle = ref({
  left: '0px',
  top: '0px',
})

// 计算属性
const monthYear = computed(() => {
  return `${currentDate.value.getFullYear()}年${currentDate.value.getMonth() + 1}月`
})

/**
 * 日历表头
 */
const dayHeaders = computed(() => ['日', '一', '二', '三', '四', '五', '六'])

/**
 * 日历展示数据
 */
const calendarDays = computed(() => {
  const days: CalendarDay[] = []
  const startDate = getCalendarStartDate()

  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)

    days.push({
      date,
      isCurrentMonth: date.getMonth() === currentDate.value.getMonth(),
      isToday: isToday(date),
    })
  }

  return days
})

const modalDateString = computed(() => {
  return modalDate.value
    ? `${modalDate.value.getFullYear()}年${modalDate.value.getMonth() + 1}月${modalDate.value.getDate()}日`
    : ''
})

const modalEvents = computed(() => {
  return modalDate.value ? getDayEvents(modalDate.value) : []
})

/**
 * 获取日历展示第一天的日期
 */
const getCalendarStartDate = () => {
  const firstDay = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), 1)
  const startDate = new Date(firstDay)
  startDate.setDate(1 - firstDay.getDay())
  return startDate
}

const isToday = (date: Date) => {
  return formatDate(date) === formatDate(new Date())
}

const getDayEvents = (date: Date) => {
  return events.value.filter((e) => e.date === formatDate(date))
}

/**
 * 日历点击日事件
 * @param date
 */
const handleDayClick = (date: Date) => {
  if (date.getMonth() !== currentDate.value.getMonth()) {
    currentDate.value = new Date(date)
    return
  }

  modalDate.value = date
  showEventModal.value = true
}

/**
 * 打开日历菜单
 * @param event
 * @param date
 */
const handleContextMenu = (event: MouseEvent, date: Date) => {
  contextMenuDate.value = date
  showContextMenu.value = true
  contextMenuStyle.value = {
    left: `${event.clientX}px`,
    top: `${event.clientY}px`,
  }
}

/**
 * 创建日历事件
 * @param type
 */
const addEvent = (type: 'meeting' | 'reminder' | 'custom') => {
  const title = prompt(`请输入${getEventTypeName(type)}：`)
  if (title && contextMenuDate.value) {
    events.value.push({
      date: formatDate(contextMenuDate.value),
      title,
      color: getEventColor(type),
      type,
    })
    showContextMenu.value = false
  }
}

const getEventTypeName = (type: string) => {
  return (
    {
      meeting: '会议',
      reminder: '提醒',
      custom: '事项',
    }[type] || '事项'
  )
}

const getEventColor = (type: string) => {
  return (
    {
      meeting: '#2c7be5',
      reminder: '#e74c3c',
      custom: '#2ecc71',
    }[type] || '#2c7be5'
  )
}

/**
 * 切换日历月份
 * @param offset
 */
const changeMonth = (offset: number) => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + offset)
  currentDate.value = newDate
}

const closeModal = () => {
  showEventModal.value = false
}

// 初始化
onMounted(() => {
  document.addEventListener('click', () => {
    showContextMenu.value = false
  })
})
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

.calendar {
  max-width: auto;
  margin: 0 auto;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(19, 193, 206, 0.05);
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
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.calendar-grid {
  display: grid;
  /* repeat(7, 1fr) 表示重复 7 次，1fr: 每列占用 1 份剩余空间 */
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

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  max-width: 400px;
  width: 90%;
}

.modal-close {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  font-size: 20px;
}

/* 右键菜单 */
.context-menu {
  height: 100px;
  position: fixed;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  z-index: 1001;
  min-width: 120px;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:hover {
  background: var(--hover-bg);
}

@media (max-width: 768px) {
  .calendar-day {
    min-height: 80px;
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

/* 保持之前的CSS样式不变 */
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

.nav-button {
  background: none;
  padding: 6px 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.nav-button:hover {
  color: var(--el-color-primary);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
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
</style>
