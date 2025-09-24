<template>
  <div id="map" style="width: 100%; height: 100%"></div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

onMounted(async () => {
  const AMap = await AMapLoader.load({
    key: '490e7c7313a93b0eae7d9159945c407b', // 在高德开发者平台申请
    version: '2.0',
    plugins: ['AMap.Marker'],
  })



  // 加载定位插件
  AMap.plugin('AMap.Geolocation', () => {
    // @ts-ignore
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true, // 是否使用高精度定位
      timeout: 10000, // 定位的最大等待时间
      buttonPosition: 'RT', // 定位按钮在地图上的位置 'RB' → 右下角 'RT' → 右上角 'LT' → 左上角 'LB' → 左下角
      zoomToAccuracy: true, // 是否自动调整地图缩放到定位精度范围
    })

    const map = new AMap.Map('map', {})
    map.addControl(geolocation)

    geolocation.getCurrentPosition((status: string, result: any) => {
      if (status === 'complete') {
        const lnglat = [result.position.lng, result.position.lat]
        console.log('当前位置经纬度:', lnglat)
        map.setCenter(lnglat)

        // 添加标记
        // @ts-ignore
        new AMap.Marker({
          position: lnglat,
          map,
        })
      } else {
        console.error('定位失败:', result.message)
      }
    })
  })
})
</script>
