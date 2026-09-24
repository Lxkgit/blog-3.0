<template>
  <div class="page-container">
    <div class="page-title">地图</div>

    <el-card class="map-card">
      <div class="map-content">
        <!-- 左侧信息 -->
        <div class="map-sidebar">
          <div v-if="!selectedLocation" class="empty-info">点击地图上的 IP 定位点查看详细信息</div>

          <template v-else>
            <div class="info-title">IP 位置信息</div>

            <div class="info-item">
              <span class="label">IP：</span>
              <span>{{ selectedLocation.ip }}</span>
            </div>

            <div class="info-item">
              <span class="label">国家：</span>
              <span>{{ selectedLocation.country || '-' }}</span>
            </div>

            <div class="info-item">
              <span class="label">地区：</span>
              <span>{{ selectedLocation.region || '-' }}</span>
            </div>

            <div class="info-item">
              <span class="label">城市：</span>
              <span>{{ selectedLocation.city || '-' }}</span>
            </div>

            <div class="info-item">
              <span class="label">ISP：</span>
              <span>{{ selectedLocation.isp || '-' }}</span>
            </div>

            <div class="info-item">
              <span class="label">经度：</span>
              <span>{{ selectedLocation.lon ?? '-' }}</span>
            </div>

            <div class="info-item">
              <span class="label">纬度：</span>
              <span>{{ selectedLocation.lat ?? '-' }}</span>
            </div>
          </template>
        </div>

        <!-- 地图 -->
        <div class="map-wrapper">
          <MapComponents
            class="map-component"
            :locations="locations"
            @marker-click="handleMarkerClick"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MapComponents from '@/components/admin/util/map/MapComponents.vue'
import { selectIpLocationList } from '@/api/file'

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

const locations = ref<MapLocation[]>([])

const selectedLocation = ref<MapLocation | null>(null)

async function getLocations() {
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
}

function handleMarkerClick(location: MapLocation) {
  selectedLocation.value = location
}

onMounted(() => {
  getLocations()
})
</script>

<style scoped>
.page-container {
  width: 100%;
  height: 100%;
}

.page-title {
  margin-bottom: 12px;
  font-size: 20px;
  font-weight: 600;
}

.map-card :deep(.el-card__body) {
  padding: 0;
  height: calc(100vh - 200px);
  min-height: 450px;
  box-sizing: border-box;
  overflow: hidden;
}

.map-content {
  width: 100%;
  height: 100%;
  display: flex;
  box-sizing: border-box;
  overflow: hidden;
}

.map-sidebar {
  width: 280px;
  flex-shrink: 0;
  padding: 20px;
  box-sizing: border-box;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

.empty-info {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  text-align: center;
  line-height: 1.6;
}

.info-title {
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
}

.info-item {
  display: flex;
  margin-bottom: 14px;
  line-height: 22px;
  word-break: break-all;
}

.label {
  width: 60px;
  flex-shrink: 0;
  color: #999;
}

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
</style>
