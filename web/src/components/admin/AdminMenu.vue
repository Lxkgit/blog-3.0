<template>
  <transition
    enter-active-class="animate__animated animate__fadeInDown"
    leave-active-class="animate__animated animate__fadeOutUp"
    mode="in-out"
  >
    <header v-if="navigationType === 'show'" class="navigation-show">
      <!-- 左侧 -->
      <div class="header-left">
        <div class="logo" @click="router.push('/')">
          <span class="logo-title"> GSZero博客管理中心 </span>
        </div>
      </div>

      <!-- 右侧 -->
      <div class="header-right">
        <!-- 设置 -->
        <el-tooltip effect="dark" content="设置" placement="bottom">
          <button class="header-action" type="button" @click="drawer = true">
            <MyIcon type="icon-setting" />
          </button>
        </el-tooltip>

        <!-- 用户 -->
        <div v-if="isLogin" class="user-wrapper">
          <el-dropdown trigger="click" @visible-change="dropdownChange">
            <div class="user-info">
              <el-avatar class="user-avatar" :src="photo" />

              <span class="user-name">
                {{ userName }}
              </span>

              <el-icon class="dropdown-icon">
                <ArrowUp v-if="isDropdown" />
                <ArrowDown v-else />
              </el-icon>
            </div>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="userLogoutFun"> 退出登录 </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 设置 -->
      <el-drawer
        v-model="drawer"
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
                <img src="~@/assets/images/light.png" alt="浅色模式" />
                <span>浅色</span>
              </div>

              <div class="preview-item" :class="{ active: isDark === true }">
                <img src="~@/assets/images/dark.png" alt="深色模式" />
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
    </header>
  </transition>

  <!-- 占位，防止 fixed header 遮挡页面 -->
  <div class="placeholder"></div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import icon from '@/utils/icon'
import { systemStore } from '@/store/system'
import user from '@/utils/user'
import dark from '@/utils/dark'
import color from '@/utils/color'
import theme from '@/utils/theme'
import navigation from '@/utils/navigation'

const router = useRouter()
const store = systemStore()

const { MyIcon } = icon()

const { isDark, setDark } = dark()
const { setTheme } = theme()
const { navigationList, setNavigation, navigationType } = navigation()

const { isLogin, userName, userLogoutFun } = user()

const { themeList } = color()

const props = defineProps({
  // 导航栏类型（前台 / 后台）
  kind: {
    type: String,
    required: false,
    default: 'front',
  },
})

// ==================== 用户 ====================

const isDropdown = ref(false)

const dropdownChange = (value: boolean) => {
  isDropdown.value = value
}

const photo = ref<string>('')

async function getPhotoData() {
  photo.value = 'https://img2.baidu.com/it/u=2241198009,1203637343&fm=253&fmt=auto'
}

// ==================== 设置 ====================

const drawer = ref(false)

const handleClose = () => {
  drawer.value = false
}

// ==================== 深色模式 ====================

const isDarkSwitch = ref(false)

const setDarkMode = () => {
  setDark(isDarkSwitch.value)
}

// ==================== 侧边菜单 ====================

const asideMenuFold = ref(false)

const asideMenuFoldChange = () => {
  store.setAsideMenuFold(asideMenuFold.value)
}

// ==================== 主题色 ====================

const colorValue = ref('')

const colorChoose = (value: string) => {
  colorValue.value = value
  setTheme(value)
}

// ==================== 导航菜单 ====================

const navValue = ref('')

const navChange = (value: string) => {
  setNavigation(value)
}

// ==================== 初始化 ====================

onMounted(() => {
  isLogin.value = store.isLogin

  asideMenuFold.value = store.asideMenuFold

  colorValue.value = store.theme

  navValue.value = store.navigation

  isDarkSwitch.value = store.isDark

  if (isLogin.value) {
    getPhotoData()
  }
})
</script>

<style scoped>
/* =========================================================
   Header
   ========================================================= */

.navigation-show {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;

  width: 100%;
  height: 60px;

  display: flex;
  align-items: center;

  background-color: var(--el-bg-color-overlay);
  border-bottom: 1px solid var(--el-border-color);

  box-sizing: border-box;

  box-shadow: var(--el-box-shadow-light);
}

/* =========================================================
   左侧
   ========================================================= */

.header-left {
  min-width: 0;
  flex: 1;

  height: 100%;

  display: flex;
  align-items: center;
}

.logo {
  height: 100%;
  padding: 0 20px;

  display: flex;
  align-items: center;

  cursor: pointer;

  user-select: none;

  transition: background-color 0.2s ease;
}

.logo:hover {
  background-color: var(--el-fill-color-light);
}

.logo-title {
  color: var(--el-text-color-primary);

  font-size: 22px;
  font-weight: 700;

  white-space: nowrap;
}

/* =========================================================
   右侧
   ========================================================= */

.header-right {
  height: 100%;

  display: flex;
  align-items: center;

  padding-right: 16px;

  gap: 8px;

  flex-shrink: 0;
}

/* =========================================================
   顶部按钮
   ========================================================= */

.header-action {
  width: 40px;
  height: 40px;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 0;
  border: 0;

  border-radius: 8px;

  background: transparent;

  color: var(--el-text-color-regular);

  font-size: 22px;

  cursor: pointer;

  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease;
}

.header-action:hover {
  color: var(--el-color-primary);
  background-color: var(--el-fill-color-light);
}

.header-action:active {
  transform: scale(0.92);
}

/* =========================================================
   用户
   ========================================================= */

.user-wrapper {
  height: 100%;

  display: flex;
  align-items: center;
}

.user-info {
  height: 42px;

  display: flex;
  align-items: center;

  padding: 0 8px;

  border-radius: 8px;

  outline: none;

  cursor: pointer;

  transition: background-color 0.2s ease;
}

.user-info:hover {
  background-color: var(--el-fill-color-light);
}

.user-avatar {
  width: 34px;
  height: 34px;

  flex-shrink: 0;
}

.user-name {
  max-width: 130px;

  margin-left: 8px;

  color: var(--el-text-color-primary);

  font-size: 14px;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-icon {
  margin-left: 5px;

  color: var(--el-text-color-secondary);

  font-size: 14px;
}

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
   Placeholder
   ========================================================= */

.placeholder {
  height: 60px;
}

/* =========================================================
   小屏幕
   ========================================================= */

@media screen and (max-width: 768px) {
  .logo {
    padding: 0 12px;
  }

  .logo-title {
    font-size: 18px;
  }

  .header-right {
    padding-right: 8px;
  }

  .user-name {
    display: none;
  }

  .dropdown-icon {
    display: none;
  }

  .user-info {
    padding: 0 5px;
  }

  .header-action {
    width: 38px;
    height: 38px;
  }
}

/* =========================================================
   超小屏幕
   ========================================================= */

@media screen and (max-width: 420px) {
  .logo-title {
    font-size: 16px;
  }

  .header-left {
    overflow: hidden;
  }

  .logo {
    max-width: 100%;
    overflow: hidden;
  }

  .logo-title {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .header-right {
    gap: 2px;
  }
}
</style>
