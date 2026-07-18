<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
</script>

<template>
  <div class="page-shell">
    <PageHeader title="商家管理" subtitle="商家申请、审核和启停管理" />

    <section class="merchant-placeholder">
      <el-empty description="商家数据将在接口联调后展示" :image-size="96" />
    </section>
  </div>
</template>

<style scoped>
.merchant-placeholder {
  display: grid;
  min-height: 420px;
  margin-top: 24px;
  place-items: center;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
}
</style>
