<template>
  <div
    class="side-bar"
    :class="{
      'side-bar-open': !store.sideBar,
      'side-bar-close': store.sideBar,
    }"
  >
    <!-- ==================== 展开 / 收起 ==================== -->

    <div class="side-bar-header" @click="store.sideBar = !store.sideBar">
      <el-icon>
        <MyIcon :type="store.sideBar ? 'icon-indent' : 'icon-outdent'" />
      </el-icon>
    </div>

    <!-- ==================== 菜单 ==================== -->

    <el-menu class="el-menu-vertical-demo" :collapse="store.sideBar" :collapse-transition="true">
      <!-- 个人中心 -->

      <el-menu-item index="/admin/index" @click="router.push('/admin/index')">
        <el-icon>
          <MyIcon type="icon-user" />
        </el-icon>

        <template #title>
          <span class="menu-icon-text"> 个人中心 </span>
        </template>
      </el-menu-item>

      <!-- 后台菜单 -->

      <MyMenu
        v-for="(item, index) in adminMenus.data"
        :key="index"
        :index-key="index"
        :item="item"
      />
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'

import MyMenu from '@/components/admin/MyMenu.vue'
import icon from '@/utils/icon'
import { systemStore } from '@/store/system'
import { userMenuApi } from '@/api/auth'

/* ==================== Store ==================== */

const store = systemStore()

/* ==================== Router ==================== */

const router = useRouter()

/* ==================== Icon ==================== */

const { MyIcon } = icon()

/* ==================== 菜单数据 ==================== */

const adminMenus: any = reactive({
  data: [],
})

/* ==================== 获取菜单 ==================== */

const getMenuFun = () => {
  userMenuApi(2).then((res: any) => {
    if (res.code === 200) {
      adminMenus.data = res.result
    }
  })
}

/* ==================== 初始化 ==================== */

onMounted(() => {
  getMenuFun()
})
</script>

<style scoped>
/* =========================================================
   侧边栏
   ========================================================= */

.side-bar {
  /*
   * 默认折叠
   */
  width: 64px;

  height: 100%;

  box-sizing: border-box;

  overflow: hidden;

  /*
   * 和顶部导航栏完全一致
   */
  background-color: var(--el-bg-color-overlay);

  color: var(--el-text-color-primary);

  /*
   * 右侧边界
   */
  border-right: 1px solid var(--el-border-color);

  /*
   * 整个侧边栏宽度动画
   */
  transition:
    width 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    background-color 0.2s ease,
    color 0.2s ease;
}

/* =========================================================
   展开状态
   ========================================================= */

.side-bar-open {
  width: 200px;
}

/* =========================================================
   收起状态
   ========================================================= */

.side-bar-close {
  width: 64px;
}

/* =========================================================
   顶部按钮
   ========================================================= */

.side-bar-header {
  width: 100%;
  height: 40px;

  display: flex;
  align-items: center;

  padding-left: 22px;

  box-sizing: border-box;

  flex-shrink: 0;

  cursor: pointer;
  user-select: none;

  color: var(--el-text-color-regular);

  background-color: var(--el-bg-color-overlay);

  border-bottom: 1px solid var(--el-border-color);

  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

/* 按钮 hover */

.side-bar-header:hover {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   顶部按钮图标
   ========================================================= */

.side-bar-header .el-icon {
  flex-shrink: 0;

  font-size: 18px;

  transition:
    transform 0.35s ease,
    color 0.2s ease;
}

/* =========================================================
   菜单
   ========================================================= */

.el-menu-vertical-demo {
  width: 100% !important;

  height: calc(100vh - 138px);

  box-sizing: border-box;

  /*
   * 背景和导航栏一致
   */
  background-color: var(--el-bg-color-overlay);

  color: var(--el-text-color-primary);

  /*
   * 右边界由 side-bar 负责
   */
  border-right: none;

  /*
   * 不自己控制 width，
   * 跟随父级 side-bar。
   */
}

/* =========================================================
   一级菜单
   ========================================================= */

:deep(.el-menu-item) {
  color: var(--el-text-color-regular);

  background-color: var(--el-bg-color-overlay);

  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

/* hover */

:deep(.el-menu-item:hover) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* 当前选中 */

:deep(.el-menu-item.is-active) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   子菜单标题
   ========================================================= */

:deep(.el-sub-menu__title) {
  color: var(--el-text-color-regular);

  background-color: var(--el-bg-color-overlay);

  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

/* hover */

:deep(.el-sub-menu__title:hover) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   子菜单
   ========================================================= */

:deep(.el-sub-menu .el-menu) {
  background-color: var(--el-bg-color-overlay);

  border-right: none;
}

/* 子菜单菜单项 */

:deep(.el-sub-menu .el-menu-item) {
  color: var(--el-text-color-regular);

  background-color: var(--el-bg-color-overlay);
}

/* 子菜单 hover */

:deep(.el-sub-menu .el-menu-item:hover) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* 子菜单选中 */

:deep(.el-sub-menu .el-menu-item.is-active) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   菜单图标
   ========================================================= */

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  flex-shrink: 0;

  color: inherit;

  transition:
    color 0.2s ease,
    transform 0.3s ease;
}

/* =========================================================
   菜单文字
   ========================================================= */

:deep(.el-menu-item span),
:deep(.el-sub-menu__title span) {
  transition: opacity 0.3s ease;
}

/* =========================================================
   折叠状态
   ========================================================= */

:deep(.el-menu--collapse) {
  width: 64px;

  background-color: var(--el-bg-color-overlay);

  border-right: none;
}

/* =========================================================
   折叠状态菜单项
   ========================================================= */

:deep(.el-menu--collapse .el-menu-item) {
  width: 64px;

  background-color: var(--el-bg-color-overlay);
}

/* 折叠状态 hover */

:deep(.el-menu--collapse .el-menu-item:hover) {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   折叠状态图标
   ========================================================= */

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  margin-right: 0;

  transition:
    transform 0.3s ease,
    color 0.2s ease;
}

/* =========================================================
   菜单内部
   ========================================================= */

:deep(.el-menu) {
  background-color: var(--el-bg-color-overlay);

  border-right: none;
}

/* =========================================================
   菜单文字
   ========================================================= */

.menu-icon-text {
  color: inherit;

  white-space: nowrap;

  transition: opacity 0.3s ease;
}

/* =========================================================
   子菜单箭头
   ========================================================= */

:deep(.el-sub-menu__icon-arrow) {
  transition:
    transform 0.3s ease,
    color 0.2s ease;
}

/* =========================================================
   菜单整体动画
   ========================================================= */

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

/* =========================================================
   移动端
   ========================================================= */

@media screen and (max-width: 768px) {
  .side-bar-open {
    width: 200px;
  }

  .side-bar-close {
    width: 64px;
  }
}
</style>
