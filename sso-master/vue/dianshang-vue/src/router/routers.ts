//单独暴露路由
export const luYou=[
    {
        path:'/login',
        component:()=> import('@/views/login/index.vue'),
        name:'login',
        meta:{
            //隐藏不显示到菜单上 true:隐藏 ,false:显示
            hidden:true,
            //菜单的名称
            title:'login',
            //饿了么ui图标的名字 固定写法
            icon:'Plus',
            auth:''
        }
    },
    {
        path:'/per',
        component:()=> import('@/views/per/index.vue'),
        name:'per',
        meta:{
            hidden:true,
            title:'per',
            icon:'Plus',
            auth:''
        }
    },
    {
        path:'/404',
        component:()=> import('@/views/404/index.vue'),
        name:'404',
        meta:{
            hidden:true,
            title:'404',
            icon:'Plus',
            auth:''
        }
    },
    {
        //根页面
        path:'/',
        component:()=> import('@/views/menu/index.vue'),
        name:'menu',
        meta:{
            hidden:true,
            title:'menu',
            icon:'Plus',
            auth:''
        }
    },
    {
        //匹配到不存在的路径就跳转404
        path:'/:pathMatch(.*)*',
        //重定向到404
        redirect: '/404',
        //任意路由
        name:'any',
        meta:{
            hidden:true,
            title:'any',
            icon:'Plus',
            auth:''
        }
    },
    {
        path:'/callback',
        component:()=> import('@/views/callback/index.vue'),
        name:'callback',
        meta:{
            hidden:true,
            title:'callback',
            icon:'Plus',
            auth:''
        }
    },
]