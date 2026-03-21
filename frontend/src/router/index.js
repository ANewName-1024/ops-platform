import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Login from '../views/Login.vue'
import MainLayout from '../views/layout/MainLayout.vue'

const routes = [
  // 登录
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, title: '登录' }
  },
  
  // 主布局
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      // 首页/仪表盘
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Index.vue'),
        meta: { title: '仪表盘' }
      },
      
      // 知识库
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('../views/knowledge/Index.vue'),
        meta: { title: '知识库' }
      },
      
      // 工作流
      {
        path: 'workflow',
        name: 'Workflow',
        component: () => import('../views/workflow/Index.vue'),
        meta: { title: '工作流' }
      },
      
      // 自愈中心
      {
        path: 'autoheal',
        name: 'AutoHeal',
        component: () => import('../views/autoheal/Index.vue'),
        meta: { title: '自愈中心' }
      },
      
      // 灰度发布
      {
        path: 'grayrelease',
        name: 'GrayRelease',
        component: () => import('../views/grayrelease/Index.vue'),
        meta: { title: '灰度发布' }
      },
      
      // 监控中心
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('../views/monitor/Index.vue'),
        meta: { title: '监控中心' }
      },
      
      // AI 助手
      {
        path: 'ai',
        name: 'AI',
        component: () => import('../views/ai/Index.vue'),
        meta: { title: 'AI 助手' }
      },
      
      // 通知中心
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('../views/notification/Index.vue'),
        meta: { title: '通知中心' }
      },
      
      // 运维中心
      {
        path: 'ops',
        name: 'Ops',
        component: () => import('../views/ops/Index.vue'),
        meta: { title: '运维中心' }
      },
      
      // 证书管理
      {
        path: 'certificate',
        name: 'Certificate',
        component: () => import('../views/certificate/Index.vue'),
        meta: { title: '证书管理' }
      },
      
      // 系统管理
      {
        path: 'admin',
        redirect: '/admin/users'
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'admin/roles',
        name: 'AdminRoles',
        component: () => import('../views/admin/Roles.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'admin/system',
        name: 'AdminSystem',
        component: () => import('../views/admin/System.vue'),
        meta: { title: '系统配置' }
      }
    ]
  },
  
  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - OPS平台` : 'OPS平台'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 如果没有 token，跳转到登录页
    if (!userStore.isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // 检查权限
    if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
      next({ path: '/dashboard' })
      return
    }
  }
  
  // 已登录用户访问登录页，跳转到首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    next({ path: '/dashboard' })
    return
  }
  
  next()
})

export default router
