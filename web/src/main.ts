import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import store from '@/store'
import router from '@/router'
// import "animate.css";
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
// import 'nprogress/nprogress.css'
import '@/assets/style/index.css'
import '@/assets/style/css-vars.css'
import "@/assets/style/normalize.css"
import "@/assets/style/hover-min.css"

import VueVideoPlayer from '@videojs-player/vue' // 官方 Vue3 适配库 :cite[3]:cite[6]
import 'video.js/dist/video-js.css'



const app = createApp(App)
app.use(store)
app.use(router)
app.use(ElementPlus, { size: 'default', zIndex: 3000 })
app.use(VueVideoPlayer)

app.mount('#app')
// 自定义指令-动态title
app.directive('title', {
	updated(el, binding) {
		document.title = binding.value
	}
})
