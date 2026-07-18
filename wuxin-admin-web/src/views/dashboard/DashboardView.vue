<script setup lang="ts">
import { OfficeBuilding, UserFilled } from '@element-plus/icons-vue'

import PageHeader from '@/components/PageHeader.vue'
</script>

<template>
  <div class="page-shell">
    <PageHeader title="后台首页" subtitle="总控端运行概览">
      <template #actions>
        <el-tag type="success" effect="plain">V1.6 基础架构</el-tag>
      </template>
    </PageHeader>

    <section class="overview-grid" aria-label="模块概览">
      <article class="overview-item">
        <el-icon :size="22"><OfficeBuilding /></el-icon>
        <div>
          <span>商家审核</span>
          <strong>后台接口已就绪</strong>
        </div>
      </article>
      <article class="overview-item">
        <el-icon :size="22"><UserFilled /></el-icon>
        <div>
          <span>管理员权限</span>
          <strong>RBAC 已启用</strong>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.overview-grid {
  display: grid;
  margin-top: 24px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.overview-item {
  display: flex;
  min-height: 112px;
  padding: 24px;
  align-items: center;
  gap: 18px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
}

.overview-item :deep(.el-icon) {
  color: #9a7412;
}

.overview-item div {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.overview-item span {
  color: #697386;
  font-size: 13px;
}

.overview-item strong {
  font-size: 17px;
  letter-spacing: 0;
}

@media (max-width: 760px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
