<template>
  <div id="map" class="map-container"></div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

onMounted(async () => {
  try {
    /*
     * =====================================================
     * 加载高德地图
     * =====================================================
     */

    const AMap = await AMapLoader.load({
      key: '490e7c7313a93b0eae7d9159945c407b',
      version: '2.0',
      plugins: ['AMap.Geolocation', 'AMap.Marker'],
    })

    /*
     * =====================================================
     * 创建地图
     * =====================================================
     */

    const map = new AMap.Map('map', {
      zoom: 11,
    })

    /*
     * =====================================================
     * 创建定位插件
     * =====================================================
     */

    const geolocation = new AMap.Geolocation({
      /*
       * 是否使用高精度定位
       */
      enableHighAccuracy: true,

      /*
       * 定位超时时间
       */
      timeout: 10000,

      /*
       * 定位按钮
       */
      buttonPosition: 'RT',

      /*
       * 自动调整地图缩放
       */
      zoomToAccuracy: true,

      /*
       * 是否显示定位信息
       */
      showMarker: false,

      /*
       * 是否显示圆圈
       */
      showCircle: false,
    })

    /*
     * =====================================================
     * 添加定位控件
     * =====================================================
     */

    map.addControl(geolocation)

    /*
     * =====================================================
     * 获取当前位置
     * =====================================================
     */

    geolocation.getCurrentPosition((status: string, result: any) => {
      console.log('高德定位结果:', status, result)

      /*
       * 定位成功
       */
      if (status === 'complete') {
        const lnglat = [result.position.lng, result.position.lat]

        console.log('当前位置经纬度:', lnglat)

        /*
         * 地图移动到当前位置
         */
        map.setCenter(lnglat)

        /*
         * 设置缩放级别
         */
        map.setZoom(15)

        /*
         * 添加当前位置 Marker
         */
        new AMap.Marker({
          position: lnglat,

          map: map,
        })

        return
      }

      /*
       * =================================================
       * 定位失败
       * =================================================
       */

      console.error('高德定位失败:', result)

      console.error('当前访问地址:', window.location.href)

      console.error('当前协议:', window.location.protocol)

      /*
       * HTTP 环境提示
       */
      if (window.location.protocol === 'http:' && window.location.hostname !== 'localhost') {
        console.warn('当前网站使用 HTTP，浏览器可能禁止定位。生产环境建议使用 HTTPS。')
      }
    })
  } catch (error) {
    console.error('高德地图加载失败:', error)
  }
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
}
</style>
