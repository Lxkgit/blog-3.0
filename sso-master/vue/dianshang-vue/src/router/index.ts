//通过vue-router插件实现模版路由配置
import { createRouter,createWebHashHistory,createWebHistory } from 'vue-router';
 //引入路由数组
import {luYou} from '@/router/routers'
//创建路由器
const router=createRouter({
    //路由模式 没有#字符
    history: createWebHistory(),
    //注意单词 别写错了
     //注意单词 别写错了
    routes: luYou
})
 
export default router;