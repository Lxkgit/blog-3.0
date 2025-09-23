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
      path: '/document',
      name: 'Document',
      component: () => import('@/views/home/doc/Document.vue'),
      meta: {
        title: '文章正文',
        keepAlive: false,
        isAuth: false,
      },
    },
    {
      path: '/catalog/:id',
      name: 'Catalog',
      component: () => import('@/views/home/doc/Catalog.vue'),
      meta: {
        title: '笔记目录',
        keepAlive: false,
        isAuth: false,
      },
    },
    {
      path: '/detail/section/:id',
      name: 'SectionDetail',
      component: () => import('@/views/home/doc/SectionDetail.vue'),
      meta: {
        title: '笔记正文',
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
        {
          path: 'article',
          name: 'Article',
          component: () => import('@/views/admin/article/AdminArticle.vue'),
          meta: {
            title: '文章管理',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'article/editor',
          name: 'ArticleEditor',
          component: () => import('@/views/admin/article/ArticleEditor.vue'),
          meta: {
            title: '文章编辑',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'article/type',
          name: 'articleType',
          component: () => import('@/views/admin/article/type/AdminArticleType.vue'),
          meta: {
            title: ' 文章分类',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'article/label',
          name: 'articleLabel',
          component: () => import('@/views/admin/article/label/AdminArticleLabel.vue'),
          meta: {
            title: ' 文章标签',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'diary',
          name: 'Diary',
          component: () => import('@/views/admin/diary/AdminDiary.vue'),
          meta: {
            title: '日记管理',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'doc',
          name: 'Doc',
          component: () => import('@/views/admin/doc/AdminDoc.vue'),
          meta: {
            title: '文档管理',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'doc/editor',
          name: 'DocEditor',
          component: () => import('@/views/admin/doc/DocEditor.vue'),
          meta: {
            title: '文章编辑',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'tool/task',
          name: 'Task',
          component: () => import('@/views/admin/tool/Task.vue'),
          meta: {
            title: '定时任务',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'tool/calendar',
          name: 'Calendar',
          component: () => import('@/views/admin/tool/Calendar.vue'),
          meta: {
            title: '日历',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'file',
          name: 'File',
          component: () => import('@/views/admin/file/UserFile.vue'),
          meta: {
            title: '个人云盘',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'device',
          name: 'Device',
          component: () => import('@/views/admin/device/ServiceManagement.vue'),
          meta: {
            title: '服务器设备',
            keepAlive: false,
            isAuth: true,
          },
        },
         {
          path: 'log/task',
          name: 'TaskLog',
          component: () => import('@/views/admin/log/TaskLog.vue'),
          meta: {
            title: '定时任务日志',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'setting/web',
          name: 'WebSetting',
          component: () => import('@/views/admin/setting/WebSetting.vue'),
          meta: {
            title: '网站设置',
            keepAlive: false,
            isAuth: true,
          },
        },
        {
          path: 'setting/user',
          name: 'UserSetting',
          component: () => import('@/views/admin/setting/UserSetting.vue'),
          meta: {
            title: '个人设置',
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
