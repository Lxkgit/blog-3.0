<template>
  <transition
    enter-active-class="animate__animated animate__fadeInDown"
    leave-active-class="animate__animated animate__fadeOutUp"
    mode="in-out"
  >
    <header id="tags_view_container" class="tags_view_container">
      <div class="tags_view_wrapper">
        <!-- 首页 -->
        <span class="home-button">
          <MyIcon type="icon-home3" @click="router.push('/')" />
        </span>

        <!-- Tab -->
        <router-link
          v-for="(item, index) in store.tags"
          :key="index"
          ref="tag"
          class="tags_view_item"
          :class="{ active: item.active }"
          :to="item.path"
          @contextmenu.prevent="openMenu(item.path, index, $event)"
        >
          <!-- 标签标题 -->
          <span class="tag-title">
            {{ item.title }}
          </span>

          <!-- 关闭按钮 -->
          <span v-if="item.close" class="tag-close" @click.prevent.stop="closeTag(index)">
            <MyIcon type="icon-close" />
          </span>
        </router-link>
      </div>

      <!-- 右键菜单 -->
      <ul
        v-if="visible"
        :style="{
          left: left + 'px',
          top: top + 'px',
        }"
        class="contextmenu"
      >
        <li @click="refresh">刷新</li>

        <li @click="closeTag()">关闭当前</li>

        <li @click="closeOther">关闭其他</li>

        <li @click="closeAll">关闭全部</li>
      </ul>
    </header>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'

import { tagsStore } from '@/store/tag'

import { useRoute, useRouter } from 'vue-router'

import icon from '@/utils/icon'

/* =========================================================
   基础
   ========================================================= */

const route = useRoute()

const router = useRouter()

const store = tagsStore()

const { MyIcon } = icon()

/* =========================================================
   右键菜单
   ========================================================= */

const visible = ref(false)

const top = ref(100)

const left = ref(100)

/* =========================================================
   路由变化
   激活对应 Tab
   ========================================================= */

watch(
  () => route.path,
  () => {
    console.log('PageTab path: ' + route.path)

    store.activeTag(route.path)
  },
)

/* =========================================================
   点击菜单外部关闭菜单
   ========================================================= */

const handleDocumentClick = () => {
  if (visible.value) {
    visible.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})

/* =========================================================
   关闭标签
   ========================================================= */

const closeTag = (index?: number) => {
  visible.value = false

  /*
   * 没有传 index
   * 表示从右键菜单关闭当前标签
   */
  if (index === undefined) {
    store.delTag(store.selectedTag, 0)

    router.push('/admin/index')

    return
  }

  /*
   * 当前关闭的是激活标签
   */
  if (store.tags[index].active) {
    let nextIndex = 0

    /*
     * 当前标签是最后一个
     */
    if (index === store.tags.length - 1) {
      nextIndex = store.tags.length - 2
    } else {
      /*
       * 当前标签是通过打开方式产生的标签
       */
      if (store.tags[index].open === 1) {
        nextIndex = index - 1
      } else {
        nextIndex = index
      }
    }

    /*
     * 删除标签
     */
    store.delTag(index, nextIndex)

    /*
     * 跳转到下一个标签
     */
    if (store.tags[nextIndex]) {
      router.push(store.tags[nextIndex].path)
    }
  } else {
    /*
     * 关闭非激活标签
     */
    store.delTag(index)
  }
}

/* =========================================================
   打开右键菜单
   ========================================================= */

const openMenu = (path: string, index: number, e: MouseEvent) => {
  /*
   * 右键点击时同步激活 Tab
   */
  store.activeTag(path)

  /*
   * 显示菜单
   */
  visible.value = true

  /*
   * 使用鼠标当前位置
   */
  left.value = e.clientX

  top.value = e.clientY
}

/* =========================================================
   关闭其他标签
   ========================================================= */

const closeOther = () => {
  visible.value = false

  store.delOtherTags()
}

/* =========================================================
   关闭全部标签
   ========================================================= */

const closeAll = () => {
  visible.value = false

  router.push('/admin/index')

  store.delAllTags()
}

/* =========================================================
   刷新当前页面
   ========================================================= */

const refresh = () => {
  window.location.reload()
}
</script>

<style scoped>
/* =========================================================
   Tab 容器
   ========================================================= */

.tags_view_container {
  position: relative;

  width: 100%;
  height: 35px;

  box-sizing: border-box;

  background-color: var(--el-bg-color-overlay);

  border-bottom: 1px solid var(--el-border-color);

  color: var(--el-text-color-primary);

  overflow: hidden;
}

/* =========================================================
   Tab 滚动区域
   ========================================================= */

.tags_view_wrapper {
  width: 100%;
  height: 100%;

  box-sizing: border-box;

  overflow-x: auto;
  overflow-y: hidden;

  white-space: nowrap;

  scrollbar-width: thin;

  scrollbar-color: var(--el-border-color) transparent;
}

/* =========================================================
   Chrome / Edge 滚动条
   ========================================================= */

.tags_view_wrapper::-webkit-scrollbar {
  height: 4px;
}

.tags_view_wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.tags_view_wrapper::-webkit-scrollbar-thumb {
  background: var(--el-border-color);

  border-radius: 4px;
}

.tags_view_wrapper::-webkit-scrollbar-thumb:hover {
  background: var(--el-text-color-placeholder);
}

/* =========================================================
   首页按钮
   ========================================================= */

.home-button {
  display: inline-flex;

  align-items: center;
  justify-content: center;

  vertical-align: top;

  width: 57px;
  height: 35px;

  box-sizing: border-box;

  color: var(--el-text-color-regular);

  cursor: pointer;

  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

/* 首页 hover */

.home-button:hover {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* 首页图标 */

.home-button :deep(svg) {
  width: 18px;
  height: 18px;
}

/* =========================================================
   Tab
   ========================================================= */

.tags_view_item {
  display: inline-flex;

  align-items: center;

  vertical-align: top;

  position: relative;

  box-sizing: border-box;

  height: 26px;
  min-height: 26px;

  margin-left: 5px;
  margin-top: 4px;

  padding: 0 5px 0 8px;

  cursor: pointer;

  border: 1px solid var(--el-border-color);

  border-radius: 4px;

  color: var(--el-text-color-regular);

  background-color: var(--el-fill-color-light);

  font-size: 12px;

  line-height: 24px;

  text-decoration: none;

  white-space: nowrap;

  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

/* =========================================================
   Tab hover
   ========================================================= */

.tags_view_item:hover {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color);

  border-color: var(--el-color-primary-light-5);
}

/* =========================================================
   Tab 标题
   ========================================================= */

.tag-title {
  display: inline-block;

  line-height: 24px;

  white-space: nowrap;
}

/* =========================================================
   激活 Tab
   ========================================================= */

.tags_view_item.active {
  color: #fff;

  background-color: var(--el-color-primary);

  border-color: var(--el-color-primary);

  box-shadow: 0 1px 4px var(--el-color-primary-light-7);
}

/* =========================================================
   激活 Tab 左侧圆点
   ========================================================= */

.tags_view_item.active::before {
  content: '';

  display: inline-block;

  width: 7px;
  height: 7px;

  margin-right: 5px;

  flex-shrink: 0;

  border-radius: 50%;

  background-color: rgba(255, 255, 255, 0.9);
}

/* =========================================================
   关闭按钮
   ========================================================= */

.tag-close {
  width: 22px;
  height: 22px;

  margin-left: 5px;

  padding: 0;

  display: inline-flex;

  align-items: center;
  justify-content: center;

  flex-shrink: 0;

  box-sizing: border-box;

  border-radius: 50%;

  color: var(--el-text-color-secondary);

  background-color: transparent;

  cursor: pointer;

  transition:
    color 0.15s ease,
    background-color 0.15s ease,
    transform 0.15s ease;
}

/* =========================================================
   X 图标
   ========================================================= */

.tag-close :deep(svg) {
  width: 17px !important;
  height: 17px !important;

  display: block;

  flex-shrink: 0;
}

/* =========================================================
   普通 Tab - 关闭按钮
   ========================================================= */

.tags_view_item:not(.active) .tag-close {
  color: var(--el-text-color-secondary);

  background-color: transparent;
}

/* 普通 Tab - hover */

.tags_view_item:not(.active) .tag-close:hover {
  color: var(--el-text-color-primary);

  background-color: var(--el-fill-color-darker);

  transform: scale(1.08);
}

/* =========================================================
   激活 Tab - 关闭按钮
   ========================================================= */

.tags_view_item.active .tag-close {
  color: rgba(255, 255, 255, 0.95);

  background-color: rgba(255, 255, 255, 0.16);
}

/* 激活 Tab - hover */

.tags_view_item.active .tag-close:hover {
  color: #fff;

  background-color: rgba(255, 255, 255, 0.34);

  transform: scale(1.08);
}

/* =========================================================
   暗色模式 - 普通 Tab
   ========================================================= */

:global(.dark) .tags_view_item:not(.active) {
  background-color: var(--el-bg-color);

  border-color: var(--el-border-color);

  color: var(--el-text-color-regular);
}

/* 暗色模式 - 普通 Tab hover */

:global(.dark) .tags_view_item:not(.active):hover {
  background-color: var(--el-fill-color);

  border-color: var(--el-color-primary-light-5);

  color: var(--el-color-primary-light-3);
}

/* =========================================================
   暗色模式 - 激活 Tab
   ========================================================= */

:global(.dark) .tags_view_item.active {
  background-color: var(--el-color-primary-dark-2);

  border-color: var(--el-color-primary-dark-2);

  color: #fff;

  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.35);
}

/* =========================================================
   暗色模式 - 普通 Tab 关闭按钮
   ========================================================= */

:global(.dark) .tags_view_item:not(.active) .tag-close {
  color: var(--el-text-color-secondary);
}

/* 暗色模式 - 普通 Tab 关闭 hover */

:global(.dark) .tags_view_item:not(.active) .tag-close:hover {
  color: var(--el-text-color-primary);

  background-color: var(--el-fill-color-darker);
}

/* =========================================================
   暗色模式 - 激活 Tab 关闭按钮
   ========================================================= */

:global(.dark) .tags_view_item.active .tag-close {
  color: rgba(255, 255, 255, 0.95);

  background-color: rgba(255, 255, 255, 0.18);
}

/* 暗色模式 - 激活 Tab 关闭 hover */

:global(.dark) .tags_view_item.active .tag-close:hover {
  color: #fff;

  background-color: rgba(255, 255, 255, 0.38);
}

/* =========================================================
   右键菜单
   ========================================================= */

.contextmenu {
  position: fixed;

  z-index: 3000;

  min-width: 115px;

  margin: 0;
  padding: 5px 0;

  list-style: none;

  box-sizing: border-box;

  color: var(--el-text-color-primary);

  background-color: var(--el-bg-color-overlay);

  border: 1px solid var(--el-border-color);

  border-radius: 6px;

  font-size: 12px;
  font-weight: 400;

  box-shadow: var(--el-box-shadow-light);

  overflow: hidden;
}

/* =========================================================
   右键菜单项目
   ========================================================= */

.contextmenu li {
  margin: 0;

  padding: 8px 16px;

  color: var(--el-text-color-regular);

  cursor: pointer;

  white-space: nowrap;

  transition:
    color 0.15s ease,
    background-color 0.15s ease;
}

/* 右键菜单 hover */

.contextmenu li:hover {
  color: var(--el-color-primary);

  background-color: var(--el-fill-color-light);
}

/* =========================================================
   小屏幕
   ========================================================= */

@media screen and (max-width: 768px) {
  .tags_view_container {
    height: 35px;
  }

  .home-button {
    width: 48px;
  }

  .tags_view_item {
    margin-left: 3px;

    padding-left: 7px;
    padding-right: 4px;

    font-size: 12px;
  }

  .tag-close {
    width: 22px;
    height: 22px;

    margin-left: 4px;
  }

  .tag-close :deep(svg) {
    width: 16px !important;
    height: 16px !important;
  }
}

/* =========================================================
   超小屏幕
   ========================================================= */

@media screen and (max-width: 420px) {
  .home-button {
    width: 44px;
  }

  .tags_view_item {
    margin-left: 2px;

    padding-left: 6px;
  }

  .tag-close {
    width: 22px;
    height: 22px;

    margin-left: 3px;
  }
}
</style>
