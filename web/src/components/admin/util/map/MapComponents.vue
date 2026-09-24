<template>
  <div ref="mapRef" class="map-container"></div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

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

const props = defineProps<{
  locations?: MapLocation[]
}>()

const emit = defineEmits<{
  markerClick: [location: MapLocation]
}>()

const mapRef = ref<HTMLElement | null>(null)

let AMap: any = null
let map: any = null

const markers: any[] = []

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

    map = new AMap.Map(mapRef.value, {
      zoom: 5,
      center: [116.397428, 39.90923],
    })

    console.log('地图实例创建成功')

    renderLocations()
  } catch (error) {
    console.error('高德地图加载失败:', error)
  }
}

function renderLocations() {
  if (!map || !AMap) {
    return
  }

  markers.forEach((marker) => {
    marker.setMap(null)
  })

  markers.length = 0

  if (!props.locations?.length) {
    console.log('没有 IP 定位数据')
    return
  }

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

  locations.forEach((location) => {
    console.log('创建 Marker:', location.longitude, location.latitude)

    const marker = new AMap.Marker({
      map: map,
      position: [location.longitude, location.latitude],
      title: location.item.ip,
    })

    marker.on('click', () => {
      emit('markerClick', location.item)
    })

    markers.push(marker)
  })

  if (locations.length === 1) {
    map.setCenter([locations[0].longitude, locations[0].latitude])

    map.setZoom(12)

    return
  }

  let longitude = 0
  let latitude = 0

  locations.forEach((location) => {
    longitude += location.longitude
    latitude += location.latitude
  })

  longitude /= locations.length
  latitude /= locations.length

  map.setCenter([longitude, latitude])

  map.setZoom(8)
}

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

onMounted(async () => {
  await nextTick()
  await initMap()
})

onBeforeUnmount(() => {
  markers.forEach((marker) => {
    marker.setMap(null)
  })

  markers.length = 0

  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
}
</style>
