<template>
  <transition
    enter-active-class="animate__animated animate__fadeInDown"
    leave-active-class="animate__animated animate__fadeOutUp"
    mode="in-out"
  >
    <header
      class="navigation-show"
      :class="{
        'navigation-hide': navigationType === 'hide',
      }"
    >
      <!-- ==================== 左侧 Logo ==================== -->
      <div v-if="props.kind === 'front'" class="header-left">
        <div class="site-logo" @click="router.push('/')">
          <el-image class="site-logo-image" :src="siteConfig.logo" fit="fill" />

          <span class="site-name">
            {{ siteConfig.name }}
          </span>
        </div>
      </div>

      <!-- ==================== 中间导航 ==================== -->
      <nav class="header-menu">
        <el-menu :default-active="menuIndex" mode="horizontal" :ellipsis="false">
          <el-menu-item index="1" @click="router.push('/')">
            <MyIcon type="icon-home3" />
            <span class="menu-title">首页</span>
          </el-menu-item>

          <el-menu-item index="2" @click="router.push('/category')">
            <MyIcon type="icon-article" />
            <span class="menu-title">文章</span>
          </el-menu-item>

          <el-menu-item index="3" @click="router.push('/document')">
            <MyIcon type="icon-book" />
            <span class="menu-title">文档</span>
          </el-menu-item>

          <el-menu-item index="4" @click="router.push('/classify')">
            <MyIcon type="icon-shijianzhou" />
            <span class="menu-title">归档</span>
          </el-menu-item>
        </el-menu>
      </nav>

      <!-- ==================== 右侧操作 ==================== -->
      <div class="header-right">
        <!-- 搜索 -->
        <el-tooltip effect="dark" content="搜索" placement="bottom">
          <button
            class="header-action"
            type="button"
            :class="{
              active: menuIndex === '7',
            }"
            @click="router.push('/search')"
          >
            <MyIcon type="icon-search" />
          </button>
        </el-tooltip>

        <!-- 设置 -->
        <el-tooltip effect="dark" content="设置" placement="bottom">
          <button class="header-action" type="button" @click="drawer = true">
            <MyIcon type="icon-setting" />
          </button>
        </el-tooltip>

        <!-- 侧边栏 -->
        <MenuSideBar v-model="drawer" :kind="props.kind" />

        <!-- 用户 -->
        <div class="user-wrapper">
          <el-dropdown v-if="isLogin" trigger="click" @visible-change="dropdownChange">
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
                <el-dropdown-item @click="selfPage"> 个人中心 </el-dropdown-item>

                <el-dropdown-item @click="userLogoutFun"> 退出登录 </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 未登录 -->
          <div v-else class="login-button" @click="toLogin">登录</div>
        </div>
      </div>
    </header>
  </transition>

  <!-- 防止 fixed Header 遮挡页面 -->
  <div class="placeholder"></div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import { callbackUrlApi } from '@/api/auth'
import { systemStore } from '@/store/system'
import { tagsStore } from '@/store/tag'

import icon from '@/utils/icon'
import user from '@/utils/user'
import navigation from '@/utils/navigation'

import MenuSideBar from '@/components/common/MenuSideBar.vue'

const router = useRouter()

const store = systemStore()
const tagStore = tagsStore()

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

// ==================== 网站信息 ====================

const siteConfig = reactive({
  logo: 'https://img2.baidu.com/it/u=2241198009,1203637343&fm=253&fmt=auto',
  name: 'GSZero',
})

// ==================== 菜单 ====================

const menuIndex = computed(() => store.menuIndex)

// ==================== 设置 ====================

const drawer = ref(false)

// ==================== 用户 ====================

const isDropdown = ref(false)

const dropdownChange = (value: boolean) => {
  isDropdown.value = value
}

const photo = ref('')

async function getPhotoData() {
  // 后续这里接真实用户头像
  // const data = await getUserinfoId(userId.value)
  // photo.value = data.photo

  photo.value = 'https://img2.baidu.com/it/u=2241198009,1203637343&fm=253&fmt=auto'
}

// ==================== 个人中心 ====================

const selfPage = () => {
  tagStore.activeTag('/admin/index')
  router.push('/admin/index')
}

// ==================== 登录 ====================

const toLogin = () => {
  // const rzId = Cookies.get('rzId')

  callbackUrlApi().then((res: any) => {
    if (res.code !== 200) {
      ElMessage.error('获取登录信息失败')
      return
    }

    let target =
      res.result.serviceIp +
      '/auth/oauth2/authorize' +
      '?response_type=code' +
      '&client_id=dianshang' +
      '&scope=openid' +
      '&redirect_uri=' +
      res.result.callbackUrl

    store.callback.serviceIp = res.result.serviceIp
    store.callback.callbackUrl = res.result.callbackUrl

    // if (rzId) {
    //   target += '&rzId=' + rzId
    // }

    window.location.href = target
  })
}

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

  /*
   * 向上收回动画
   *
   * cubic-bezier：
   * 开始慢 → 中间平滑 → 最后慢慢收回
   */
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
}

/* =========================================================
   导航栏隐藏
   ========================================================= */

.navigation-hide {
  transform: translateY(-100%);
}

/* =========================================================
   左侧 Logo
   ========================================================= */

.header-left {
  min-width: 0;

  display: flex;
  align-items: center;

  flex-shrink: 0;
}

.site-logo {
  height: 60px;

  display: flex;
  align-items: center;

  padding: 0 20px;

  cursor: pointer;
  user-select: none;

  transition: background-color 0.2s ease;
}

.site-logo:hover {
  background-color: var(--el-fill-color-light);
}

.site-logo-image {
  width: 40px;
  height: 40px;

  flex-shrink: 0;

  border-radius: 8px;
}

.site-name {
  margin-left: 12px;

  color: var(--el-text-color-primary);

  font-size: 18px;
  font-weight: 600;

  white-space: nowrap;
}

/* =========================================================
   中间导航
   ========================================================= */

.header-menu {
  min-width: 0;

  /*
   * 占据 Logo 和右侧操作之间的剩余空间
   */
  flex: 1;

  height: 60px;

  display: flex;
  align-items: center;

  /*
   * 导航不居中，从左侧开始
   */
  justify-content: flex-start;

  /*
   * 和 Logo 留一点距离
   */
  margin-left: 220px;
}

/* Element Plus 横向菜单 */

.header-menu :deep(.el-menu) {
  height: 60px;

  width: auto;

  border-bottom: none;

  background-color: transparent;
}

/* 菜单项 */

.header-menu :deep(.el-menu-item) {
  height: 60px;

  padding: 0 20px;

  color: var(--el-text-color-regular);

  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

/* Hover */

.header-menu :deep(.el-menu-item:hover) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* 当前选中 */

.header-menu :deep(.el-menu-item.is-active) {
  color: var(--el-color-primary);
}

/* 菜单文字 */

.menu-title {
  margin-left: 5px;
}

/* =========================================================
   右侧
   ========================================================= */

.header-right {
  height: 60px;

  display: flex;
  align-items: center;

  gap: 6px;

  padding: 0 16px;

  flex-shrink: 0;
}

/* =========================================================
   操作按钮
   ========================================================= */

.header-action {
  width: 40px;
  height: 40px;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 0;

  border: none;
  border-radius: 8px;

  outline: none;

  background: transparent;

  color: var(--el-text-color-regular);

  font-size: 21px;

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

.header-action.active {
  color: var(--el-color-primary);
}

/* =========================================================
   用户
   ========================================================= */

.user-wrapper {
  height: 60px;

  display: flex;
  align-items: center;
}

.user-info {
  height: 42px;

  display: flex;
  align-items: center;

  padding: 0 8px;

  border-radius: 8px;

  cursor: pointer;
  outline: none;

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
  max-width: 120px;

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
   登录
   ========================================================= */

.login-button {
  padding: 8px 14px;

  border-radius: 7px;

  color: var(--el-text-color-regular);

  font-size: 14px;

  cursor: pointer;

  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.login-button:hover {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   页面占位
   ========================================================= */

.placeholder {
  height: 60px;
}

/* =========================================================
   平板
   ========================================================= */

@media screen and (max-width: 900px) {
  .site-name {
    display: none;
  }

  .site-logo {
    padding: 0 12px;
  }

  .header-menu :deep(.el-menu-item) {
    padding: 0 14px;
  }

  .menu-title {
    margin-left: 4px;
  }

  .header-right {
    padding: 0 10px;
  }
}

/* =========================================================
   手机
   ========================================================= */

@media screen and (max-width: 650px) {
  .site-logo-image {
    width: 36px;
    height: 36px;
  }

  .header-menu :deep(.el-menu-item) {
    padding: 0 10px;
  }

  .header-menu :deep(.el-menu-item .menu-title) {
    display: none;
  }

  .header-menu :deep(.el-menu-item) {
    font-size: 20px;
  }

  .header-right {
    gap: 2px;

    padding: 0 6px;
  }

  .header-action {
    width: 38px;
    height: 38px;
  }

  .user-name,
  .dropdown-icon {
    display: none;
  }

  .user-info {
    padding: 0 4px;
  }
}

/* =========================================================
   超小屏幕
   ========================================================= */

@media screen and (max-width: 420px) {
  .site-logo {
    padding: 0 8px;
  }

  .site-logo-image {
    width: 34px;
    height: 34px;
  }

  .header-menu :deep(.el-menu-item) {
    padding: 0 8px;
  }

  .header-right {
    padding: 0 4px;
  }

  .header-action {
    width: 36px;
    height: 36px;
  }
}
</style>
