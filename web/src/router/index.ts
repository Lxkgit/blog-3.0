import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/HomePage.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginRegister.vue'),
    },
    {
      path: '/callback',
      name: 'callback',
      component: () => import('@/components/login/Callback.vue'),
    },
    {
      path: '/category',
      name: 'Category',
      component: () => import('@/views/home/article/Category.vue'),
      meta: {
        title: '文章分类',
        keepAlive: true,
        isAuth: false,
      },
    },
    {
      path: '/detail/article/:id',
      name: 'ArticleDetail',
      component: () => import('@/views/home/article/ArticleDetail.vue'),
      meta: {
        title: '文章正文',
        keepAlive: false,
        isAuth: false,
      },
    },
    {
      path: '/admin',
      name: 'AdminPage',
      component: () => import('@/views/admin/AdminPage.vue'),
      children: [
        {
          path: 'index',
          name: 'AdminIndex',
          component: () => import('@/views/admin/MyIndex.vue'),
          meta: {
            title: '个人中心',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'role',
          name: 'Role',
          component: () => import('@/views/admin/auth/AdminRole.vue'),
          meta: {
              title: '角色管理',
              keepAlive: false,
              isAuth: true,
          },
      },
      {
          path: 'user',
          name: 'User',
          component: () => import('@/views/admin/auth/AdminUser.vue'),
          meta: {
              title: '用户管理',
              keepAlive: false,
              isAuth: true,
          },
      },
      ],
    },
    // {
    //   path: '/about',
    //   name: 'about',
    //   // route level code-splitting
    //   // this generates a separate chunk (About.[hash].js) for this route
    //   // which is lazy-loaded when the route is visited.
    //   component: () => import('../views/AboutView.vue'),
    // },
  ],
})

export default router
