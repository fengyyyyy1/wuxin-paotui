<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  Document, Fold, Goods, House, Lock, Money, OfficeBuilding, Promotion,
  Setting, SwitchButton, Tickets, User,
} from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const sidebarWidth = computed(() => (isCollapsed.value ? '72px' : '224px'))
const displayName = computed(
  () => authStore.userInfo?.nickname || authStore.userInfo?.username || '管理员',
)

const menuItems = computed(() => [
  { path: '/dashboard', label: 'Dashboard', icon: House, permission: 'dashboard:view' },
  { path: '/orders', label: '订单中心', icon: Tickets, permission: 'order:view' },
  { path: '/users', label: '用户中心', icon: User, permission: 'user:view' },
  { path: '/riders', label: '骑手中心', icon: User, permission: 'rider:view' },
  { path: '/merchants', label: '商家中心', icon: OfficeBuilding, permission: 'merchant:view' },
  { path: '/products', label: '商品中心', icon: Goods, permission: 'product:view' },
  { path: '/operations', label: '运营中心', icon: Promotion, permission: 'operation:view' },
  { path: '/finance', label: '财务中心', icon: Money, permission: 'finance:view' },
  { path: '/configs', label: '系统配置', icon: Setting, permission: 'config:view' },
  { path: '/permissions', label: '权限管理', icon: Lock, permission: 'rbac:view' },
  { path: '/logs', label: '日志中心', icon: Document, permission: 'log:view' },
].filter((item) => authStore.hasPermission(item.permission)))

function logout(): void {
  authStore.logout()
  void router.replace({ name: 'login' })
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside :width="sidebarWidth" class="admin-sidebar">
      <div class="brand" :class="{ 'brand--collapsed': isCollapsed }">
        <div class="brand__mark">五鑫</div>
        <div v-if="!isCollapsed" class="brand__text">
          <strong>五鑫跑腿</strong>
          <span>总控管理后台</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="admin-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="admin-main" :style="{ marginLeft: sidebarWidth }">
      <el-header class="admin-header">
        <el-button
          text
          :icon="Fold"
          aria-label="切换侧边栏"
          title="切换侧边栏"
          @click="isCollapsed = !isCollapsed"
        />
        <div class="admin-header__actions">
          <span class="admin-name">{{ displayName }}</span>
          <el-button :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="admin-content">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
}

.admin-sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 10;
  overflow: hidden;
  color: #ecf2f8;
  background: #172033;
  border-right: 1px solid #2a3549;
  transition: width 180ms ease;
}

.brand {
  display: flex;
  height: 72px;
  padding: 0 18px;
  align-items: center;
  border-bottom: 1px solid #2a3549;
}

.brand--collapsed {
  justify-content: center;
  padding: 0;
}

.brand__mark {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  color: #172033;
  font-size: 13px;
  font-weight: 750;
  place-items: center;
  background: #f7c948;
  border-radius: 6px;
}

.brand__text {
  display: flex;
  min-width: 0;
  margin-left: 12px;
  flex-direction: column;
  gap: 3px;
  white-space: nowrap;
}

.brand__text strong {
  font-size: 16px;
  letter-spacing: 0;
}

.brand__text span {
  color: #9da9ba;
  font-size: 12px;
}

.admin-menu {
  height: calc(100vh - 72px);
  overflow-y: auto;
  border-right: 0;
}

.admin-sidebar :deep(.el-menu) {
  background: transparent;
}

.admin-sidebar :deep(.el-menu-item) {
  height: 48px;
  margin: 6px 10px;
  color: #b7c1d0;
  border-radius: 6px;
}

.admin-sidebar :deep(.el-menu-item:hover),
.admin-sidebar :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: #2a3549;
}

.admin-main {
  min-width: 0;
  transition: margin-left 180ms ease;
}

.admin-header {
  display: flex;
  position: sticky;
  top: 0;
  z-index: 8;
  height: 64px;
  padding: 0 24px;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e4e8ef;
}

.admin-header__actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-name {
  max-width: 180px;
  overflow: hidden;
  color: #4b5567;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-content {
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: #f5f7fa;
}

@media (max-width: 720px) {
  .admin-sidebar {
    width: 72px !important;
  }

  .brand {
    justify-content: center;
    padding: 0;
  }

  .brand__text {
    display: none;
  }

  .admin-main {
    margin-left: 72px !important;
  }

  .admin-header {
    padding: 0 14px;
  }

  .admin-name {
    display: none;
  }

  .admin-content {
    padding: 18px 14px;
  }
}
</style>
