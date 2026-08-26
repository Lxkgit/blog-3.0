import { computed, ref } from 'vue'
import { systemStore } from '@/store/system'

const store = systemStore()

// ==================== 导航模式 ====================

// 当前配置的导航模式
const navigationMode = computed(() => store.navigation)

// 当前实际显示状态
const navigationType = ref<'show' | 'hide'>('show')

// 上一次滚动位置
const lastTop = ref(0)

// 是否已经初始化
let initialized = false

// ==================== 导航模式选项 ====================

const navigationList = [
  {
    value: 'auto',
    label: '自动',
  },
  {
    value: 'show',
    label: '固定显示',
  },
  {
    value: 'hide',
    label: '滚动隐藏',
  },
]

// ==================== 设置导航模式 ====================

const setNavigation = (value: string) => {
  store.setNavigation(value)

  switch (value) {
    case 'show':
      navigationType.value = 'show'
      break

    case 'hide':
      navigationType.value = 'hide'
      break

    case 'auto':
      // 自动模式由滚动事件控制
      navigationType.value = 'show'
      break

    default:
      navigationType.value = 'show'
      break
  }
}

// ==================== 滚动处理 ====================

const scrollHandle = () => {
  const newTop = document.body.scrollTop || document.documentElement.scrollTop

  const mode = navigationMode.value

  // 固定显示
  if (mode === 'show') {
    navigationType.value = 'show'
    lastTop.value = newTop
    return
  }

  // 滚动隐藏
  if (mode === 'hide') {
    navigationType.value = newTop < 100 ? 'show' : 'hide'

    lastTop.value = newTop
    return
  }

  // 自动
  if (mode === 'auto') {
    if (newTop <= 0) {
      navigationType.value = 'show'
    } else if (newTop > lastTop.value) {
      // 向下滚动
      navigationType.value = 'hide'
    } else if (newTop < lastTop.value) {
      // 向上滚动
      navigationType.value = 'show'
    }

    lastTop.value = newTop
  }
}

// ==================== 防抖 ====================

let scrollTimer: ReturnType<typeof setTimeout> | null = null

const handleScroll = () => {
  if (scrollTimer !== null) {
    clearTimeout(scrollTimer)
  }

  scrollTimer = setTimeout(() => {
    scrollHandle()
    scrollTimer = null
  }, 100)
}

// ==================== 初始化 ====================

const initNavigation = () => {
  if (initialized) {
    return
  }

  initialized = true

  navigationType.value = 'show'
  lastTop.value = document.body.scrollTop || document.documentElement.scrollTop

  window.addEventListener('scroll', handleScroll, false)
}

// ==================== 导航 ====================

function navigation() {
  initNavigation()

  return {
    navigationMode,
    navigationList,
    navigationType,
    setNavigation,
  }
}

export default navigation
