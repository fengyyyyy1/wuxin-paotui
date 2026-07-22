import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'
import { pinia } from '@/stores'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { guestOnly: true, title: '管理员登录' },
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: 'Dashboard', permission: 'dashboard:view' } },
        { path: 'orders', name: 'orders', component: () => import('@/views/order/OrderCenterView.vue'), meta: { title: '订单中心', permission: 'order:view' } },
        { path: 'users', name: 'users', component: () => import('@/views/user/UserCenterView.vue'), meta: { title: '用户中心', permission: 'user:view' } },
        { path: 'riders', name: 'riders', component: () => import('@/views/rider/RiderCenterView.vue'), meta: { title: '骑手中心', permission: 'rider:view' } },
        { path: 'merchants', name: 'merchant-list', component: () => import('@/views/merchant/MerchantListView.vue'), meta: { title: '商家中心', permission: 'merchant:view' } },
        { path: 'merchants/:merchantId', name: 'merchant-detail', component: () => import('@/views/merchant/MerchantDetailView.vue'), meta: { title: '商家详情', permission: 'merchant:view' } },
        { path: 'products', name: 'products', component: () => import('@/views/product/ProductCenterView.vue'), meta: { title: '商品中心', permission: 'product:view' } },
        { path: 'operations', name: 'operations', component: () => import('@/views/operation/OperationCenterView.vue'), meta: { title: '运营中心', permission: 'operation:view' } },
        { path: 'finance', name: 'finance', component: () => import('@/views/finance/FinanceCenterView.vue'), meta: { title: '财务中心', permission: 'finance:view' } },
        { path: 'configs', name: 'configs', component: () => import('@/views/config/SystemConfigView.vue'), meta: { title: '系统配置', permission: 'config:view' } },
        { path: 'permissions', name: 'permissions', component: () => import('@/views/permission/PermissionCenterView.vue'), meta: { title: '权限管理', permission: 'rbac:view' } },
        { path: 'logs', name: 'logs', component: () => import('@/views/log/LogCenterView.vue'), meta: { title: '日志中心', permission: 'log:view' } },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '页面不存在' },
    },
  ],
})

function loginRedirect(to: RouteLocationNormalized) {
  return { name: 'login', query: { redirect: to.fullPath } }
}

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) return loginRedirect(to)
    if (!authStore.adminVerified) {
      const verified = await authStore.restoreSession()
      if (!verified) return loginRedirect(to)
    }
    if (!authStore.isAdmin) {
      authStore.clearSession()
      return loginRedirect(to)
    }
    const permission = typeof to.meta.permission === 'string' ? to.meta.permission : undefined
    if (!authStore.hasPermission(permission)) return { name: 'dashboard' }
  }
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    if (!authStore.adminVerified) {
      const verified = await authStore.restoreSession()
      if (!verified) return true
    }
    if (authStore.isAdmin) return { name: 'dashboard' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} - 五鑫跑腿` : '五鑫跑腿总控台'
})

export default router
