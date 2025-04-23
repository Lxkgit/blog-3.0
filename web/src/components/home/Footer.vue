<template>
  <div class="fixed">
    <footer>
      <el-breadcrumb separator="|">
        <el-breadcrumb-item :to="{ path: '/link' }">友情链接</el-breadcrumb-item>
        <el-breadcrumb-item><a :href="footerData.sitemap">网站地图</a></el-breadcrumb-item>
      </el-breadcrumb>
    </footer>
  </div>
</template>

<script setup lang="ts">
import {
  ElBreadcrumb,
  ElBreadcrumbItem,
} from 'element-plus'
import { onMounted, reactive } from "vue";

const footerData = reactive({
  copyright: '',
  icp_num: '',
  icp_url: '',
  police_num: '',
  police_url: '',
  sitemap: ''
})

async function siteConfigData() {

}

onMounted(() => {
  siteConfigData()
})
</script>

<style scoped>
footer {
  border-top: 1px solid var(--el-border-color);
  width: 100%;
  background-color: var(--el-bg-color-overlay);
  height: 70px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
