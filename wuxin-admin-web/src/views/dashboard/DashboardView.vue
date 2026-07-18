<script setup lang="ts">
import { computed } from 'vue'
import { OfficeBuilding, SwitchButton } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const displayName = computed(
  () => authStore.userInfo?.nickname || authStore.userInfo?.username || '管理员',
)

function logout(): void {
  authStore.logout()
  void router.replace({ name: 'login' })
}
</script>

<template>
  <div class="page-shell">
    <PageHeader title="后台首页" subtitle="总控端管理工作台">
      <template #actions>
        <el-tag type="success" effect="plain">V1.6 开发中</el-tag>
      </template>
    </PageHeader>

    <section class="welcome-panel" aria-label="管理员信息">
      <div>
        <span class="welcome-panel__label">欢迎回来</span>
        <h2>{{ displayName }}</h2>
        <p>当前已接入真实登录、Token保存、管理员权限验证和路由守卫。</p>
      </div>
      <el-button :icon="SwitchButton" @click="logout">退出登录</el-button>
    </section>

    <section class="entry-grid" aria-label="后台入口">
      <RouterLink class="entry-item" to="/merchants">
        <el-icon :size="22"><OfficeBuilding /></el-icon>
        <div>
          <span>商家管理</span>
          <strong>进入商家审核与状态管理</strong>
        </div>
      </RouterLink>
    </section>
  </div>
</template>

<style scoped>
.welcome-panel {
  display: flex;
  margin-top: 24px;
  padding: 28px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
}

.welcome-panel__label {
  color: #697386;
  font-size: 13px;
}

.welcome-panel h2 {
  margin: 6px 0 8px;
  color: #172033;
  font-size: 24px;
  letter-spacing: 0;
}

.welcome-panel p {
  margin: 0;
  color: #697386;
  font-size: 14px;
  line-height: 1.6;
}

.entry-grid {
  display: grid;
  margin-top: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.entry-item {
  display: flex;
  min-height: 112px;
  padding: 24px;
  align-items: center;
  gap: 18px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease;
}

.entry-item:hover {
  border-color: #d4a017;
  box-shadow: 0 10px 28px rgb(23 32 51 / 8%);
}

.entry-item :deep(.el-icon) {
  color: #9a7412;
}

.entry-item div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
}

.entry-item span {
  color: #697386;
  font-size: 13px;
}

.entry-item strong {
  color: #172033;
  font-size: 17px;
  letter-spacing: 0;
}

@media (max-width: 760px) {
  .welcome-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .entry-grid {
    grid-template-columns: 1fr;
  }
}
</style>
