<template>
  <div class="map-page">
    <!-- 页面标题 -->
    <div class="title_style">
      <span>地图</span>
    </div>

    <!-- 地图内容 -->
    <el-card class="map-card">
      <div class="map-content">
        <!-- 左侧信息 -->
        <aside class="map-sidebar">
          <div class="sidebar-title">
            <span>位置信息</span>
          </div>

          <div class="location-info">
            <div class="info-item">
              <span class="info-label"> 定位方式 </span>

              <span class="info-value"> IP定位 </span>
            </div>

            <div class="info-item">
              <span class="info-label"> IP地址 </span>

              <span class="info-value"> - </span>
            </div>

            <div class="info-item">
              <span class="info-label"> 国家 </span>

              <span class="info-value"> - </span>
            </div>

            <div class="info-item">
              <span class="info-label"> 省份 </span>

              <span class="info-value"> - </span>
            </div>

            <div class="info-item">
              <span class="info-label"> 城市 </span>

              <span class="info-value"> - </span>
            </div>

            <div class="info-item">
              <span class="info-label"> 经度 </span>

              <span class="info-value"> - </span>
            </div>

            <div class="info-item">
              <span class="info-label"> 纬度 </span>

              <span class="info-value"> - </span>
            </div>
          </div>
        </aside>

        <!-- 右侧地图 -->
        <main class="map-wrapper">
          <MapComponents class="map-component" />
        </main>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import MapComponents from '@/components/admin/util/map/MapComponents.vue'
</script>

<style scoped>
/* =========================================================
   页面
   ========================================================= */

.map-page {
  width: 100%;

  box-sizing: border-box;
}

/* =========================================================
   页面标题
   ========================================================= */

.title_style {
  height: 31px;

  display: flex;

  align-items: center;

  justify-content: flex-start;

  box-sizing: border-box;

  color: var(--el-text-color-primary);

  font-size: 24px;

  font-weight: 600;

  line-height: 31px;

  text-align: left;
}

/* =========================================================
   Card
   ========================================================= */

.map-card {
  margin: 18px 2% 0;

  box-sizing: border-box;

  overflow: hidden;
}

/* =========================================================
   去除 Card 默认内容区域影响
   ========================================================= */

.map-card :deep(.el-card__body) {
  padding: 0;

  height: calc(100vh - 200px);

  min-height: 450px;

  box-sizing: border-box;

  overflow: hidden;
}

/* =========================================================
   整体内容
   ========================================================= */

.map-content {
  width: 100%;

  height: 100%;

  display: flex;

  box-sizing: border-box;

  overflow: hidden;
}

/* =========================================================
   左侧信息栏
   ========================================================= */

.map-sidebar {
  width: 20%;

  min-width: 220px;

  max-width: 320px;

  height: 100%;

  box-sizing: border-box;

  padding: 20px;

  background: var(--el-bg-color);

  border-right: 1px solid var(--el-border-color);

  overflow-y: auto;
}

/* =========================================================
   左侧标题
   ========================================================= */

.sidebar-title {
  padding-bottom: 15px;

  color: var(--el-text-color-primary);

  font-size: 16px;

  font-weight: 600;

  border-bottom: 1px solid var(--el-border-color);
}

/* =========================================================
   位置信息
   ========================================================= */

.location-info {
  padding-top: 8px;
}

/* 信息项 */

.info-item {
  min-height: 42px;

  display: flex;

  align-items: center;

  justify-content: space-between;

  gap: 15px;

  border-bottom: 1px solid var(--el-border-color-lighter);

  font-size: 13px;
}

/* 信息名称 */

.info-label {
  flex-shrink: 0;

  color: var(--el-text-color-secondary);
}

/* 信息内容 */

.info-value {
  min-width: 0;

  color: var(--el-text-color-primary);

  text-align: right;

  overflow: hidden;

  text-overflow: ellipsis;

  white-space: nowrap;
}

/* =========================================================
   地图
   ========================================================= */

.map-wrapper {
  flex: 1;

  min-width: 0;

  height: 100%;

  box-sizing: border-box;

  padding: 12px;
}

.map-component {
  display: block;

  width: 100%;

  height: 100%;

  min-height: 400px;

  overflow: hidden;

  border-radius: 6px;
}

/* =========================================================
   滚动条
   ========================================================= */

.map-sidebar {
  scrollbar-width: thin;

  scrollbar-color: var(--el-border-color) transparent;
}

.map-sidebar::-webkit-scrollbar {
  width: 5px;
}

.map-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.map-sidebar::-webkit-scrollbar-thumb {
  background: var(--el-border-color);

  border-radius: 5px;
}

/* =========================================================
   平板
   ========================================================= */

@media screen and (max-width: 1024px) {
  .map-sidebar {
    width: 25%;

    min-width: 200px;

    padding: 16px;
  }
}

/* =========================================================
   手机 / 窄窗口
   ========================================================= */

@media screen and (max-width: 768px) {
  .map-card {
    margin-left: 10px;

    margin-right: 10px;
  }

  .map-card :deep(.el-card__body) {
    height: calc(100vh - 180px);

    min-height: 500px;
  }

  .map-content {
    flex-direction: column;
  }

  .map-sidebar {
    width: 100%;

    max-width: none;

    min-width: 0;

    height: auto;

    max-height: 180px;

    padding: 12px 16px;

    border-right: 0;

    border-bottom: 1px solid var(--el-border-color);
  }

  .sidebar-title {
    padding-bottom: 10px;
  }

  .location-info {
    display: grid;

    grid-template-columns: repeat(2, minmax(0, 1fr));

    column-gap: 20px;
  }

  .info-item {
    min-height: 36px;
  }

  .map-wrapper {
    flex: 1;

    min-height: 0;

    padding: 8px;
  }

  .map-component {
    min-height: 300px;
  }
}

/* =========================================================
   超小屏幕
   ========================================================= */

@media screen and (max-width: 480px) {
  .title_style {
    font-size: 20px;
  }

  .map-card {
    margin-top: 12px;

    margin-left: 6px;

    margin-right: 6px;
  }

  .location-info {
    grid-template-columns: 1fr;
  }

  .map-card :deep(.el-card__body) {
    min-height: 450px;
  }
}
</style>
