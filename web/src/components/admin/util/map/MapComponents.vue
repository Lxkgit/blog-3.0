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
  type: string
  ip: string
  country: string
  province: string
  city: string
  longitude: number
  latitude: number
}

/*
 * =========================================================
 * 地图围栏
 * =========================================================
 */

interface MapFence {
  id: number
  name: string

  /*
   * POLYGON：多边形
   * CIRCLE：圆形
   */
  type: 'POLYGON' | 'CIRCLE'

  /*
   * 多边形坐标
   */
  path?: [number, number][]

  /*
   * 圆形中心
   */
  center?: [number, number]

  /*
   * 圆形半径，单位：米
   */
  radius?: number
}

/*
 * =========================================================
 * Props
 * =========================================================
 */

const props = defineProps<{
  location?: MapLocation | null
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

let locationMarker: any = null

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

    /*
     * 第一次绘制
     */

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

  /*
   * 清除旧 Marker
   */

  clearLocationMarker()

  /*
   * 清除旧围栏
   */

  clearFenceOverlays()

  /*
   * 绘制围栏
   */

  renderFences()

  /*
   * 绘制 IP 定位
   */

  renderLocation()
}

/*
 * =========================================================
 * 绘制 IP 定位
 * =========================================================
 */

function renderLocation() {
  if (!props.location) {
    return
  }

  const { longitude, latitude } = props.location

  /*
   * 检查坐标
   */

  if (typeof longitude !== 'number' || typeof latitude !== 'number') {
    return
  }

  const position = [longitude, latitude]

  /*
   * 创建 Marker
   */

  locationMarker = new AMap.Marker({
    position,

    title: `${props.location.city || ''} ${props.location.ip || ''}`,

    map,
  })

  /*
   * 地图移动到 IP 定位位置
   */

  map.setCenter(position)

  map.setZoom(12)
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

    if (fence.type === 'CIRCLE' && fence.center && typeof fence.radius === 'number') {
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

function clearLocationMarker() {
  if (!locationMarker) {
    return
  }

  locationMarker.setMap(null)

  locationMarker = null
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
  () => props.location,
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
