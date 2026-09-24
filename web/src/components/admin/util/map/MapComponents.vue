<template>
  <div ref="mapRef" class="map-container"></div>
</template>

<script setup lang="ts">
import {
  nextTick,
  onMounted,
  ref,
  watch,
} from 'vue'
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
 * Props
 * =========================================================
 */

const props = defineProps<{
  locations?: MapLocation[]
  fences?: MapFence[]
}>()

/*
 * =========================================================
 * 地图 DOM
 * =========================================================
 */

const mapRef = ref<HTMLElement | null>(null)

/*
 * =========================================================
 * 高德地图实例
 * =========================================================
 */

let map: any = null

let AMap: any = null

/*
 * =========================================================
 * IP 定位 Marker
 * =========================================================
 */

const locationMarkers: any[] = []

/*
 * =========================================================
 * 围栏图层
 * =========================================================
 */

const fenceOverlays: any[] = []

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

    if (!mapRef.value) {
      return
    }

    map = new AMap.Map(mapRef.value, {
      zoom: 5,
      center: [116.397428, 39.90923],
    })

    renderMap()
  } catch (error) {
    console.error('高德地图加载失败:', error)
  }
}

/*
 * =========================================================
 * 渲染地图
 * =========================================================
 */

function renderMap() {
  if (!map || !AMap) {
    return
  }

  clearLocationMarkers()

  clearFenceOverlays()

  renderFences()

  renderLocations()
}

/*
 * =========================================================
 * 绘制 IP 定位
 * =========================================================
 */

function renderLocations() {
  if (!props.locations?.length) {
    return
  }

  const validLocations = props.locations.filter(
    (location) =>
      typeof location.lon === 'number' &&
      typeof location.lat === 'number',
  )

  if (!validLocations.length) {
    return
  }

  /*
   * 创建所有 Marker
   */

  validLocations.forEach((location) => {
    const position = [
      location.lon,
      location.lat,
    ]

    const marker = new AMap.Marker({
      position,
      title: location.ip,
      map,
    })

    marker.on('click', () => {
      console.log('点击 IP 定位:', location)
    })

    locationMarkers.push(marker)
  })

  /*
   * 只有一个定位点时直接定位
   */

  if (validLocations.length === 1) {
    const location = validLocations[0]

    map.setCenter([
      location.lon,
      location.lat,
    ])

    map.setZoom(12)

    return
  }

  /*
   * 多个定位点自动调整地图范围
   */

  const bounds = new AMap.Bounds()

  validLocations.forEach((location) => {
    bounds.extend([
      location.lon,
      location.lat,
    ])
  })

  map.setBounds(bounds)
}

/*
 * =========================================================
 * 绘制围栏
 * =========================================================
 */

function renderFences() {
  if (!props.fences?.length) {
    return
  }

  props.fences.forEach((fence) => {
    /*
     * =====================================================
     * 多边形围栏
     * =====================================================
     */

    if (
      fence.type === 'POLYGON' &&
      fence.path &&
      fence.path.length >= 3
    ) {
      const polygon = new AMap.Polygon({
        path: fence.path,
        strokeWeight: 2,
        strokeColor: '#409EFF',
        strokeOpacity: 0.9,
        fillColor: '#409EFF',
        fillOpacity: 0.15,
        map,
      })

      polygon.on('click', () => {
        console.log('点击围栏:', fence)
      })

      fenceOverlays.push(polygon)

      return
    }

    /*
     * =====================================================
     * 圆形围栏
     * =====================================================
     */

    if (
      fence.type === 'CIRCLE' &&
      fence.center &&
      typeof fence.radius === 'number'
    ) {
      const circle = new AMap.Circle({
        center: fence.center,
        radius: fence.radius,
        strokeWeight: 2,
        strokeColor: '#409EFF',
        strokeOpacity: 0.9,
        fillColor: '#409EFF',
        fillOpacity: 0.15,
        map,
      })

      circle.on('click', () => {
        console.log('点击围栏:', fence)
      })

      fenceOverlays.push(circle)
    }
  })
}

/*
 * =========================================================
 * 清除 IP Marker
 * =========================================================
 */

function clearLocationMarkers() {
  locationMarkers.forEach((marker) => {
    marker.setMap(null)
  })

  locationMarkers.length = 0
}

/*
 * =========================================================
 * 清除围栏
 * =========================================================
 */

function clearFenceOverlays() {
  fenceOverlays.forEach((overlay) => {
    overlay.setMap(null)
  })

  fenceOverlays.length = 0
}

/*
 * =========================================================
 * 监听 IP 定位变化
 * =========================================================
 */

watch(
  () => props.locations,
  () => {
    if (map) {
      renderMap()
    }
  },
  {
    deep: true,
  },
)

/*
 * =========================================================
 * 监听围栏变化
 * =========================================================
 */

watch(
  () => props.fences,
  () => {
    if (map) {
      renderMap()
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
