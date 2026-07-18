<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <main class="not-found">
    <div>
      <span>404</span>
      <h1>页面不存在</h1>
      <p>当前地址无对应的管理页面。</p>
      <el-button type="primary" @click="router.replace('/')">返回后台首页</el-button>
    </div>
  </main>
</template>

<style scoped>
.not-found {
  display: grid;
  min-height: 100vh;
  padding: 24px;
  color: #172033;
  place-items: center;
  background: #f5f7fa;
  text-align: center;
}

.not-found span {
  color: #9a7412;
  font-size: 72px;
  font-weight: 750;
  line-height: 1;
}

.not-found h1 {
  margin: 20px 0 8px;
  font-size: 28px;
  letter-spacing: 0;
}

.not-found p {
  margin: 0 0 24px;
  color: #697386;
}
</style>
