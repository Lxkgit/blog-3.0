<template>
  <div>
    <div class="title_style">
      <span>地图</span>
    </div>
    <el-card style="margin: 18px 2%">
      <!-- 左边 div -->
      <div style="width: 20%; float: left">11</div>

      <!-- 右边 div -->
      <div style="width: 80%; float: right; height: 100%">
        <MapComponents style="height: calc(100vh - 246px); margin-bottom: 20px" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import MapComponents from '@/components/admin/util/map/MapComponents.vue'

onMounted(() => {})

/**
 * 用户增删改查操作方法合集
 */
function userFn(): any {
  return {}
}
</script>

<style scoped>
.title_style {
  display: flex;
  justify-content: flex-start;
  align-items: baseline;
  max-height: 31px;
  color: #445160;
  font-size: 24px;
  font-weight: 600;
  text-align: left;
}
</style>
