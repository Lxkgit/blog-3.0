<template>
  <el-drawer
    v-model="drawerVisible"
    title="系统设置"
    direction="rtl"
    size="min(360px, 85vw)"
    :before-close="handleClose"
    destroy-on-close
  >
    <div class="setting-panel">
      <!-- 显示模式 -->
      <section class="setting-section">
        <h4>显示模式</h4>

        <div class="display-preview">
          <div class="preview-item" :class="{ active: isDark === false }">
            <img src="@/assets/images/light.png" alt="浅色模式" />
            <span>浅色</span>
          </div>

          <div class="preview-item" :class="{ active: isDark === true }">
            <img src="@/assets/images/dark.png" alt="深色模式" />
            <span>深色</span>
          </div>
        </div>

        <div class="setting-control">
          <span>深色模式</span>

          <el-switch
            v-model="isDarkSwitch"
            active-text="开启"
            inactive-text="关闭"
            @change="setDarkMode"
          />
        </div>
      </section>

      <el-divider />

      <!-- 主题色 -->
      <section class="setting-section">
        <h4>主题色</h4>

        <div class="theme-list">
          <el-tooltip
            v-for="(item, index) in themeList"
            :key="index"
            effect="dark"
            :content="item.name"
            placement="top"
          >
            <button
              type="button"
              class="theme-color"
              :class="{
                active: colorValue === item.value,
              }"
              :style="{
                backgroundColor: item.value,
              }"
              @click="colorChoose(item.value)"
            >
              <span v-if="colorValue === item.value" class="theme-check"> ✓ </span>
            </button>
          </el-tooltip>
        </div>
      </section>

      <el-divider />

      <!-- 前台导航 -->
      <section v-if="props.kind === 'front'" class="setting-section">
        <h4>导航菜单</h4>

        <div class="setting-control">
          <span>菜单显示模式</span>

          <el-select v-model="navValue" class="setting-select" @change="navChange">
            <el-option
              v-for="item in navigationList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
      </section>

      <!-- 后台侧边菜单 -->
      <section v-else class="setting-section">
        <h4>侧边菜单</h4>

        <div class="setting-control">
          <span>折叠菜单</span>

          <el-switch v-model="asideMenuFold" @change="asideMenuFoldChange" />
        </div>
      </section>

      <el-divider />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { systemStore } from '@/store/system'
import dark from '@/utils/dark'
import color from '@/utils/color'
import theme from '@/utils/theme'
import navigation from '@/utils/navigation'

const props = defineProps({
  // 导航栏类型（前台 / 后台）
  kind: {
    type: String,
    required: false,
    default: 'front',
  },

  // 控制 Drawer 显示
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
}>()

const store = systemStore()

// ==================== Drawer ====================

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => {
    emit('update:modelValue', value)
  },
})

const handleClose = () => {
  drawerVisible.value = false
}

// ==================== 深色模式 ====================

const { isDark, setDark } = dark()

const isDarkSwitch: any = ref(false)

const setDarkMode = (value: string | number | boolean) => {
  isDarkSwitch.value = value === true

  setDark(isDarkSwitch.value)
}

// ==================== 主题色 ====================

const { setTheme } = theme()
const { themeList } = color()

const colorValue = ref('')

const colorChoose = (value: string) => {
  colorValue.value = value

  setTheme(value)
}

// ==================== 导航菜单 ====================

const { navigationList, setNavigation } = navigation()

const navValue = ref('')

const navChange = (value: string) => {
  navValue.value = value

  setNavigation(value)
}

// ==================== 侧边菜单 ====================

const asideMenuFold: any = ref(false)

const asideMenuFoldChange = (value: string | number | boolean) => {
  asideMenuFold.value = value === true

  store.asideMenuFold = asideMenuFold.value
}

// ==================== 初始化 ====================

onMounted(() => {
  asideMenuFold.value = store.asideMenuFold

  colorValue.value = store.theme

  navValue.value = store.navigation

  isDarkSwitch.value = store.isDark
})
</script>

<style scoped>
/* =========================================================
   Drawer
   ========================================================= */

.setting-panel {
  padding-bottom: 20px;
}

.setting-section {
  color: var(--el-text-color-primary);
}

.setting-section h4 {
  margin: 8px 0 20px;

  color: var(--el-text-color-primary);

  font-size: 15px;
  font-weight: 600;
}

/* =========================================================
   显示模式
   ========================================================= */

.display-preview {
  display: flex;
  justify-content: center;

  gap: 24px;

  margin-bottom: 22px;
}

.preview-item {
  display: flex;
  flex-direction: column;
  align-items: center;

  gap: 8px;

  color: var(--el-text-color-secondary);

  font-size: 13px;
}

.preview-item img {
  width: 92px;
  height: 65px;

  object-fit: cover;

  border-radius: 8px;

  border: 2px solid transparent;

  box-shadow: var(--el-box-shadow-light);

  box-sizing: border-box;

  transition: all 0.2s ease;
}

.preview-item.active img {
  border-color: var(--el-color-primary);

  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.preview-item.active span {
  color: var(--el-color-primary);
}

/* =========================================================
   设置项
   ========================================================= */

.setting-control {
  min-height: 38px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  gap: 15px;

  color: var(--el-text-color-regular);

  font-size: 14px;
}

.setting-select {
  width: 140px;
}

/* =========================================================
   主题颜色
   ========================================================= */

.theme-list {
  display: flex;
  flex-wrap: wrap;

  gap: 16px;

  padding: 4px 2px;
}

.theme-color {
  position: relative;

  width: 32px;
  height: 32px;

  padding: 0;

  border: 2px solid transparent;
  border-radius: 7px;

  cursor: pointer;

  box-sizing: border-box;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.theme-color:hover {
  transform: scale(1.12);
}

.theme-color.active {
  box-shadow:
    0 0 0 2px var(--el-bg-color),
    0 0 0 4px var(--el-color-primary);
}

.theme-check {
  position: absolute;

  inset: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  color: #fff;

  font-size: 17px;
  font-weight: bold;

  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
}

/* =========================================================
   Divider
   ========================================================= */

.setting-panel :deep(.el-divider) {
  margin: 24px 0;
}

/* =========================================================
   小屏幕
   ========================================================= */

@media screen and (max-width: 420px) {
  .display-preview {
    gap: 18px;
  }

  .preview-item img {
    width: 86px;
    height: 61px;
  }

  .setting-select {
    width: 130px;
  }
}
</style>
