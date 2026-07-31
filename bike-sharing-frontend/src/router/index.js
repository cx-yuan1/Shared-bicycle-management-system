import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

/**
 * 路由配置
 */

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  // 统一登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  // 注册页面
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  // 用户端布局路由
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'scan-ride',
        name: 'UserScanRide',
        component: () => import('@/views/user/ScanRide.vue'),
        meta: { title: '扫码用车' }
      },
      {
        path: 'return-bike',
        name: 'UserReturnBike',
        component: () => import('@/views/user/ReturnBike.vue'),
        meta: { title: '还车' }
      },
      {
        path: 'orders',
        name: 'UserOrders',
        component: () => import('@/views/user/OrderList.vue'),
        meta: { title: '用车订单' }
      },
      {
        path: 'repairs',
        name: 'UserRepairs',
        component: () => import('@/views/user/RepairList.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 管理员端布局路由
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'user-manage',
        name: 'AdminUserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'bike-manage',
        name: 'AdminBikeManage',
        component: () => import('@/views/admin/BikeManage.vue'),
        meta: { title: '单车管理' }
      },
      {
        path: 'station-manage',
        name: 'AdminStationManage',
        component: () => import('@/views/admin/StationManage.vue'),
        meta: { title: '站点管理' }
      },
      {
        path: 'repair-manage',
        name: 'AdminRepairManage',
        component: () => import('@/views/admin/RepairManage.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'order-manage',
        name: 'AdminOrderManage',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '共享单车管理系统'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    const userInfo = localStorage.getItem('userInfo')
    
    if (!token || !userInfo) {
      // 未登录，跳转到统一登录页
      next('/login')
      return
    }
    
    const user = JSON.parse(userInfo)
    
    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin) {
      if (user.role !== 'ADMIN') {
        ElMessage.error('您没有管理员权限')
        next('/user/home')
        return
      }
    }
    
    // 如果是普通用户试图访问管理员页面
    if (to.path.startsWith('/admin') && user.role !== 'ADMIN') {
      ElMessage.error('您没有管理员权限')
      next('/user/home')
      return
    }
    
    // 如果是管理员试图访问用户页面，重定向到管理员首页
    if (to.path.startsWith('/user') && user.role === 'ADMIN') {
      next('/admin/dashboard')
      return
    }
  }
  
  next()
})

export default router
