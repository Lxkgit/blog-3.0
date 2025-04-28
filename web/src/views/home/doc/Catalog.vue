<template>
  <NavMenu></NavMenu>
  <div class="page">
    <div class="catalog animate__animated animate__zoomIn">
      <el-tree :data="catalogList" @node-click="handleNodeClick"></el-tree>
    </div>
  </div>
  <Footer></Footer>
  <BackTop></BackTop>
</template>

<script setup name="Catalog" lang="ts">
import {
  ElTree,
} from 'element-plus'
import NavMenu from "@/components/home/NavMenu.vue";
import Footer from "@/components/home/Footer.vue"
import BackTop from "@/components/home/BackTop.vue"
import { onBeforeRouteUpdate, useRouter } from "vue-router";
import { onActivated, onMounted, ref } from "vue";
import { systemStore } from "@/store/system.ts";
// import {getCatalogueList, getNoteDetail} from "@/api/blog";

const store = systemStore()
const router = useRouter()
// 笔记名称
const title = ref()
// 点击跳转笔记详情页
const handleNodeClick = (data: any) => {
  if (!data.children) {
    router.push({ path: `/detail/section/${data.id}` })
  }
}
// 笔记目录列表
const catalogList = ref([
  {
    id: 1,
    label: 1,
    children: [
      {
        id: 2,
        label: "123",
        children: null
      }
    ]
  },
  {
    id: 3,
    label: 3,
    children: [
      {
        id: 4,
        label: "123",
        children: null
      },
      {
        id: 5,
        label: "123",
        children: null
      },
    ]
  }
])

// 获取笔记目录数据
async function catalogueData(catalogueID: any) {

}

// 获取笔记名称
async function titleData(catalogueID: any) {

}

onMounted(async () => {
  let catalogueID = router.currentRoute.value.params.id
  await catalogueData(catalogueID)
  await titleData(catalogueID)
  store.menuIndex = '3-' + router.currentRoute.value.params.id
})
onBeforeRouteUpdate(async (to) => {
  await catalogueData(to.params.id)
});
onActivated(() => {
  store.menuIndex = '3-' + router.currentRoute.value.params.id
})
</script>

<style>
.catalog {
  padding: 20px 10px;
  background-color: var(--el-bg-color-overlay);
}

.catalog .el-tree .el-tree-node {
  padding: 10px 0;
}

.catalog .el-tree span {
  font-size: 16px !important;
}
</style>
