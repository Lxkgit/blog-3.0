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

        <!-- 侧边栏 -->
        <MenuSideBar v-model="drawer" :kind="props.kind" />

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
    </header>
  </transition>

  <!-- 占位，防止 fixed header 遮挡页面 -->
  <div class="placeholder"></div>
</template>

<script setup lang="ts">
import MenuSideBar from '@/components/common/MenuSideBar.vue'
import { onMounted, ref } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import icon from '@/utils/icon'
import { systemStore } from '@/store/system'
import user from '@/utils/user'
import navigation from '@/utils/navigation'

const router = useRouter()
const store = systemStore()

const { MyIcon } = icon()

const { navigationType } = navigation()

const { isLogin, userName, userLogoutFun } = user()

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

// ==================== 初始化 ====================

onMounted(() => {
  isLogin.value = store.isLogin

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

  box-sizing: border-box;

  background-color: var(--el-bg-color-overlay);
  border-bottom: 1px solid var(--el-border-color);

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

  display: flex;
  align-items: center;

  padding: 0 20px;

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

  line-height: 1;

  white-space: nowrap;
}

/* =========================================================
   右侧
   ========================================================= */

.header-right {
  height: 100%;

  display: flex;
  align-items: center;

  gap: 8px;

  padding-right: 16px;

  flex-shrink: 0;
}

/* =========================================================
   顶部操作按钮
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

  overflow: hidden;

  color: var(--el-text-color-primary);

  font-size: 14px;

  line-height: 1;

  white-space: nowrap;
  text-overflow: ellipsis;
}

.dropdown-icon {
  margin-left: 5px;

  color: var(--el-text-color-secondary);

  font-size: 14px;
}

/* =========================================================
   Placeholder
   ========================================================= */

.placeholder {
  height: 60px;
}

/* =========================================================
   平板 / 手机
   ========================================================= */

@media screen and (max-width: 768px) {
  .logo {
    padding: 0 12px;
  }

  .logo-title {
    font-size: 18px;
  }

  .header-right {
    gap: 4px;

    padding-right: 8px;
  }

  .header-action {
    width: 38px;
    height: 38px;
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
}

/* =========================================================
   超小屏幕
   ========================================================= */

@media screen and (max-width: 420px) {
  .logo {
    max-width: 100%;

    overflow: hidden;
  }

  .logo-title {
    max-width: 100%;

    overflow: hidden;

    font-size: 16px;

    text-overflow: ellipsis;
  }

  .header-right {
    gap: 2px;
  }
}
</style>
