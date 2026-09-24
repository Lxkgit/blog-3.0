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
            <span>IP 位置信息</span>
          </div>

          <div class="location-list">
            <div
              v-for="location in locations"
              :key="location.id"
              class="location-item"
            >
              <div class="info-item">
                <span class="info-label">IP地址</span>
                <span class="info-value">
                  {{ location.ip || '-' }}
                </span>
              </div>

              <div class="info-item">
                <span class="info-label">国家</span>
                <span class="info-value">
                  {{ location.country || '-' }}
                </span>
              </div>

              <div class="info-item">
                <span class="info-label">地区</span>
                <span class="info-value">
                  {{ location.region || '-' }}
                </span>
              </div>

              <div class="info-item">
                <span class="info-label">城市</span>
                <span class="info-value">
                  {{ location.city || '-' }}
                </span>
              </div>

              <div class="info-item">
                <span class="info-label">ISP</span>
                <span class="info-value">
                  {{ location.isp || '-' }}
                </span>
              </div>
            </div>

            <div
              v-if="locations.length === 0"
              class="empty-location"
            >
              暂无位置信息
            </div>
          </div>
        </aside>

        <!-- 右侧地图 -->
        <main class="map-wrapper">
          <MapComponents
            class="map-component"
            :locations="locations"
            :fences="fences"
          />
        </main>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MapComponents from '@/components/admin/util/map/MapComponents.vue'
import { selectIpLocationList } from '@/api/file'

/*
 * =========================================================
 * IP 定位数据
 * =========================================================
 */

interface MapLocation {
  id: number
  ip: string
  country: string | null
  countryCode: string | null
  region: string | null
  city: string | null
  lat: number | null
  lon: number | null
  isp: string | null
  createTime: string | null
}

/*
 * =========================================================
 * 地图围栏
 * =========================================================
 */

interface MapFence {
  id: number
  name: string
  type: 'POLYGON' | 'CIRCLE'
  path?: [number, number][]
  center?: [number, number]
  radius?: number
}

/*
 * =========================================================
 * 页面数据
 * =========================================================
 */

const locations = ref<MapLocation[]>([])

const fences = ref<MapFence[]>([])

/*
 * =========================================================
 * 获取 IP 定位
 * =========================================================
 */

async function loadLocations() {
  try {
    const res: any = await selectIpLocationList()
    if (res.code === 200) {
      locations.value = (res.result || []).filter(
        (item: MapLocation) =>
          item.lat !== null &&
          item.lon !== null &&
          Number.isFinite(Number(item.lat)) &&
          Number.isFinite(Number(item.lon)),
      )
    }
  } catch (error) {
    console.error('获取 IP 位置信息失败:', error)
  }
}

/*
 * =========================================================
 * 获取地图数据
 * =========================================================
 */

async function loadMapData() {
  await loadLocations()

  /*
   * 围栏接口后续接入
   */
  // const response = await selectFenceList()
  // fences.value = response.data.result || []
}

/*
 * =========================================================
 * 页面初始化
 * =========================================================
 */

onMounted(() => {
  loadMapData()
})
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
   Card 内容
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
   IP 列表
   ========================================================= */

.location-list {
  padding-top: 8px;
}

/* =========================================================
   单个 IP
   ========================================================= */

.location-item {
  margin-bottom: 12px;

  padding-bottom: 8px;

  border-bottom: 1px solid var(--el-border-color);
}

/* =========================================================
   信息项
   ========================================================= */

.info-item {
  min-height: 36px;

  display: flex;

  align-items: center;

  justify-content: space-between;

  gap: 15px;

  border-bottom: 1px solid var(--el-border-color-lighter);

  font-size: 13px;
}

/* =========================================================
   信息名称
   ========================================================= */

.info-label {
  flex-shrink: 0;

  color: var(--el-text-color-secondary);
}

/* =========================================================
   信息内容
   ========================================================= */

.info-value {
  min-width: 0;

  color: var(--el-text-color-primary);

  text-align: right;

  overflow: hidden;

  text-overflow: ellipsis;

  white-space: nowrap;
}

/* =========================================================
   空数据
   ========================================================= */

.empty-location {
  padding: 30px 0;

  color: var(--el-text-color-secondary);

  font-size: 13px;

  text-align: center;
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

  .location-list {
    display: grid;

    grid-template-columns: repeat(2, minmax(0, 1fr));

    column-gap: 20px;
  }

  .location-item {
    margin-bottom: 0;
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

  .location-list {
    grid-template-columns: 1fr;
  }

  .map-card :deep(.el-card__body) {
    min-height: 450px;
  }
}
</style>
