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
      meta: {
        guestOnly: true,
        title: '管理员登录',
      },
    },
    {
      path: '/',
      component: AdminLayout,
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: '',
          redirect: '/dashboard',
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: {
            title: '后台首页',
          },
        },
        {
          path: 'merchants',
          name: 'merchant-list',
          component: () => import('@/views/merchant/MerchantListView.vue'),
          meta: {
            title: '商家管理',
          },
        },
        {
          path: 'merchants/:merchantId',
          name: 'merchant-detail',
          component: () => import('@/views/merchant/MerchantDetailView.vue'),
          meta: {
            title: '商家详情',
          },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: {
        title: '页面不存在',
      },
    },
  ],
})

function loginRedirect(to: RouteLocationNormalized) {
  return {
    name: 'login',
    query: {
      redirect: to.fullPath,
    },
  }
}

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)

  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      return loginRedirect(to)
    }

    if (!authStore.adminVerified) {
      const verified = await authStore.restoreSession()
      if (!verified) {
        return loginRedirect(to)
      }
    }

    if (!authStore.isAdmin) {
      authStore.clearSession()
      return loginRedirect(to)
    }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    if (!authStore.adminVerified) {
      const verified = await authStore.restoreSession()
      if (!verified) {
        return true
      }
    }

    if (authStore.isAdmin) {
      return {
        name: 'dashboard',
      }
    }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} - 五鑫跑腿` : '五鑫跑腿总控端'
})

export default router
