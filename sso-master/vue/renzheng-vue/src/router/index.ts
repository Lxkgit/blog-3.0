//通过vue-router插件实现模版路由配置
import { createRouter,createWebHistory } from 'vue-router';
 
//创建路由器
const router=createRouter({
    //路由模式
    history: createWebHistory(),
    //注意单词 别写错了
    routes:[
        {
            path:'/404',
            component:()=> import('../views/404/index.vue'),
            name:'404'
        },
        {
            //首页
            path:'/',
            component:()=> import('../App.vue'),
            name:'app'
        },
        {
            //匹配到不存在的路径就跳转404
            path:'/:pathMatch(.*)*',
            //重定向到404
            redirect: '/404',
            //任意路由
            name:'any'
        }
        
    ]
})
 
export default router;