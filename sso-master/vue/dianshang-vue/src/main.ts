import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

//引入路由
import router from './router/index'
//引入大仓库
import pinia from './store/index'


const app = createApp(App)
 
//使用路由
app.use(router)
 
//使用大仓库
app.use(pinia)

 
//挂载到app上
app.mount('#app')
