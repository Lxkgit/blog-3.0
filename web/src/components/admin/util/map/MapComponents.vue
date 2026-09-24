<template>
  <div ref="mapRef" class="map-container"></div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

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
 * Props
 * =========================================================
 */

const props = defineProps<{
  locations?: MapLocation[]
}>()

/*
 * =========================================================
 * 地图 DOM
 * =========================================================
 */

const mapRef = ref<HTMLElement | null>(null)

/*
 * =========================================================
 * 高德地图
 * =========================================================
 */

let AMap: any = null

let map: any = null

/*
 * =========================================================
 * Marker
 * =========================================================
 */

const markers: any[] = []

/*
 * =========================================================
 * 初始化地图
 * =========================================================
 */

async function initMap() {
  try {
    AMap = await AMapLoader.load({
      key: '490e7c7313a93b0eae7d9159945c407b',
      version: '2.0',
    })

    console.log('高德地图加载成功')

    if (!mapRef.value) {
      console.error('地图 DOM 不存在')
      return
    }

    /*
     * 创建地图
     */

    map = new AMap.Map(mapRef.value, {
      zoom: 5,

      center: [116.397428, 39.90923],
    })

    console.log('地图实例创建成功')

    /*
     * 绘制点位
     */

    renderLocations()
  } catch (error) {
    console.error('高德地图加载失败:', error)
  }
}

/*
 * =========================================================
 * 绘制 IP 点位
 * =========================================================
 */

function renderLocations() {
  if (!map || !AMap) {
    return
  }

  /*
   * 清除旧 Marker
   */

  markers.forEach((marker) => {
    marker.setMap(null)
  })

  markers.length = 0

  /*
   * 没有数据
   */

  if (!props.locations?.length) {
    console.log('没有 IP 定位数据')
    return
  }

  /*
   * 过滤有效坐标
   */

  const locations = props.locations
    .map((item) => {
      return {
        item,

        longitude: Number(item.lon),

        latitude: Number(item.lat),
      }
    })
    .filter((item) => {
      return (
        Number.isFinite(item.longitude) &&
        Number.isFinite(item.latitude) &&
        item.longitude >= -180 &&
        item.longitude <= 180 &&
        item.latitude >= -90 &&
        item.latitude <= 90
      )
    })

  console.log('有效定位点:', locations)

  if (!locations.length) {
    return
  }

  /*
   * =======================================================
   * 创建 Marker
   * =======================================================
   */

  locations.forEach((location) => {
    console.log('创建 Marker:', location.longitude, location.latitude)

    const marker = new AMap.Marker({
      map: map,

      position: [location.longitude, location.latitude],

      title: location.item.ip,
    })

    markers.push(marker)
  })

  /*
   * =======================================================
   * 设置地图中心
   * =======================================================
   */

  if (locations.length === 1) {
    map.setCenter([locations[0].longitude, locations[0].latitude])

    map.setZoom(12)

    return
  }

  /*
   * =======================================================
   * 多个点
   *
   * 不使用 Bounds
   * 直接计算中心点
   * =======================================================
   */

  let longitude = 0

  let latitude = 0

  locations.forEach((location) => {
    longitude += location.longitude

    latitude += location.latitude
  })

  longitude /= locations.length

  latitude /= locations.length

  console.log('地图中心:', longitude, latitude)

  /*
   * 直接设置中心
   */

  map.setCenter([longitude, latitude])

  /*
   * 上海 + 杭州
   * 直接使用合适的缩放级别
   */

  map.setZoom(8)
}

/*
 * =========================================================
 * 监听数据
 * =========================================================
 */

watch(
  () => props.locations,
  () => {
    if (map) {
      renderLocations()
    }
  },
  {
    deep: true,
  },
)

/*
 * =========================================================
 * 初始化
 * =========================================================
 */

onMounted(async () => {
  await nextTick()

  await initMap()
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
}
</style>
