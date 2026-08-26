<template>
  <!-- 有子菜单 -->
  <el-sub-menu v-if="item.children && item.children.length > 0" :index="indexKey.toString()">
    <template #title>
      <el-icon>
        <MyIcon :type="item.menuIcon" />
      </el-icon>

      <span>{{ item.menuName }}</span>
    </template>

    <el-menu-item
      v-for="(item2, index2) in item.children"
      :key="index2"
      :index="`${indexKey.toString()}-${index2.toString()}`"
      @click="gotoSite(item2)"
    >
      {{ item2.menuName }}
    </el-menu-item>
  </el-sub-menu>

  <!-- 没有子菜单 -->
  <el-menu-item v-else :index="indexKey.toString()" @click="gotoSite(item)">
    <el-icon>
      <MyIcon :type="item.menuIcon" />
    </el-icon>

    <template #title>
      {{ item.menuName }}
    </template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

import { tagsStore } from '@/store/tag'
import icon from '@/utils/icon'

const { MyIcon } = icon()

const store = tagsStore()
const router = useRouter()

defineProps({
  indexKey: {
    type: [String, Number],
    default: 0,
  },

  item: {
    type: Object,
    default: () => {
      return {}
    },
  },
})

/**
 * 跳转页面
 */
const gotoSite = (item: any) => {
  router.push(item.menuPath)

  store.addTag(item.menuName, item.menuPath)

  store.selectedTag = store.tags.length
}
</script>

<style scoped>
/*
 * 这里不单独设置黑色背景。
 *
 * el-menu 的颜色统一交给 Element Plus
 * 的主题变量控制。
 */
</style>
