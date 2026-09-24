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
 * 地图实例
 * =========================================================
 */

let map: any = null

let AMap: any = null

/*
 * =========================================================
 * IP Marker
 * =========================================================
 */

const locationMarkers: any[] = []

/*
 * =========================================================
 * 围栏
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
    console.log('没有 IP 定位数据')

    return
  }

  const validLocations = props.locations.filter((location) => {
    const longitude = Number(location.lon)
    const latitude = Number(location.lat)

    return (
      Number.isFinite(longitude) &&
      Number.isFinite(latitude) &&
      longitude >= -180 &&
      longitude <= 180 &&
      latitude >= -90 &&
      latitude <= 90
    )
  })

  console.log('有效定位点:', validLocations)

  if (!validLocations.length) {
    return
  }

  /*
   * =======================================================
   * 创建 Marker
   * =======================================================
   */

  validLocations.forEach((location) => {
    const longitude = Number(location.lon)
    const latitude = Number(location.lat)

    /*
     * 直接使用高德默认 Marker
     *
     * 不使用 Icon
     * 不使用 Pixel
     * 不使用 offset
     */

    const marker = new AMap.Marker({
      position: [longitude, latitude],

      title: location.ip,

      map: map,

      zIndex: 100,
    })

    /*
     * 点击 Marker
     */

    marker.on('click', () => {
      const address = [location.country, location.region, location.city].filter(Boolean).join(' ')

      const infoWindow = new AMap.InfoWindow({
        content: `
          <div style="
            padding: 10px;
            min-width: 180px;
            color: #333;
            font-size: 13px;
          ">
            <div style="
              font-size: 15px;
              font-weight: 600;
              margin-bottom: 8px;
            ">
              ${location.city || '未知位置'}
            </div>

            <div style="line-height: 24px;">
              <div>IP：${location.ip || '-'}</div>
              <div>位置：${address || '-'}</div>
              <div>ISP：${location.isp || '-'}</div>
              <div>
                坐标：
                ${longitude.toFixed(6)},
                ${latitude.toFixed(6)}
              </div>
            </div>
          </div>
        `,
      })

      infoWindow.open(map, [longitude, latitude])
    })

    locationMarkers.push(marker)
  })

  /*
   * =======================================================
   * 自动调整地图视野
   * =======================================================
   */

  if (validLocations.length === 1) {
    const location = validLocations[0]

    map.setCenter([Number(location.lon), Number(location.lat)])

    map.setZoom(12)

    return
  }

  /*
   * 多个点自动适应
   */

  const bounds = new AMap.Bounds()

  validLocations.forEach((location) => {
    bounds.extend([Number(location.lon), Number(location.lat)])
  })

  map.setBounds(bounds)

  /*
   * 防止因为两个点距离太近导致地图放得过大
   */

  if (map.getZoom() > 15) {
    map.setZoom(12)
  }
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

    if (fence.type === 'POLYGON' && fence.path && fence.path.length >= 3) {
      const polygon = new AMap.Polygon({
        path: fence.path,

        strokeWeight: 2,

        strokeColor: '#409EFF',

        strokeOpacity: 0.9,

        fillColor: '#409EFF',

        fillOpacity: 0.15,

        map: map,
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

    if (fence.type === 'CIRCLE' && fence.center && typeof fence.radius === 'number') {
      const circle = new AMap.Circle({
        center: fence.center,

        radius: fence.radius,

        strokeWeight: 2,

        strokeColor: '#409EFF',

        strokeOpacity: 0.9,

        fillColor: '#409EFF',

        fillOpacity: 0.15,

        map: map,
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
 * 清除 Marker
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
 * 监听 IP 数据
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
 * 监听围栏
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
