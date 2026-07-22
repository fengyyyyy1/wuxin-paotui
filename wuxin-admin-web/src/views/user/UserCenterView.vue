<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

import { getUsers, updateUserStatus } from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import type { UserQuery, UserRow } from '@/types/admin'

const router = useRouter()
const auth = useAuthStore()
const rows = ref<UserRow[]>([])
const total = ref(0)
const loading = ref(false)
const selected = ref<UserRow | null>(null)
const filters = reactive({ keyword: '', status: '' })
const page = reactive({ pageNum: 1, pageSize: 20 })
function query(): UserQuery {
  const q: UserQuery = { ...page }
  if (filters.keyword.trim()) q.keyword = filters.keyword.trim()
  if (filters.status !== '') q.status = Number(filters.status)
  return q
}
async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await getUsers(query())
    rows.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}
function search(): void {
  page.pageNum = 1
  void load()
}
function reset(): void {
  filters.keyword = ''
  filters.status = ''
  search()
}
function showDetail(value: unknown): void { selected.value = value as UserRow }
async function toggle(value: unknown): Promise<void> {
  const row = value as UserRow
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(
    `确认${next ? '启用' : '禁用'}用户“${row.nickname || row.username}”？`,
    '用户状态确认',
    { type: 'warning' },
  )
  await updateUserStatus(row.userId, next)
  await load()
}
function orders(value: unknown): void {
  const row = value as UserRow
  void router.push({ path: '/orders', query: { userId: row.userId } })
}
onMounted(() => void load())
</script>
<template>
  <div class="page-shell">
    <PageHeader title="用户中心" subtitle="用户状态修改将立即影响用户端登录与下单"
      ><template #actions
        ><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></template
      ></PageHeader
    >
    <section class="content-panel">
      <el-form class="filter-grid" @submit.prevent
        ><el-form-item label="关键词"
          ><el-input
            v-model="filters.keyword"
            clearable
            placeholder="昵称、账号、手机号"
            @keyup.enter="search" /></el-form-item
        ><el-form-item label="状态"
          ><el-select v-model="filters.status" clearable
            ><el-option label="全部" value="" /><el-option label="启用" :value="1" /><el-option
              label="禁用"
              :value="0" /></el-select
        ></el-form-item>
        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="search">查询</el-button
          ><el-button @click="reset">重置</el-button
          >
        </div></el-form
      >
    </section>
    <section class="content-panel table-panel">
      <el-table v-loading="loading" :data="rows" border
        ><el-table-column label="用户" min-width="210" fixed="left"
          ><template #default="{ row }"
            ><div class="user-cell">
              <el-avatar :size="38" :src="row.avatar || undefined">{{
                (row.nickname || row.username).slice(0, 1)
              }}</el-avatar>
              <div>
                <strong>{{ row.nickname || '未设置昵称' }}</strong
                ><span>{{ row.username }}</span>
              </div>
            </div></template
          ></el-table-column
        ><el-table-column prop="userId" label="用户ID" width="90" /><el-table-column
          prop="phone"
          label="手机号"
          width="140"
        /><el-table-column prop="orderCount" label="订单数" width="100" /><el-table-column
          label="消费金额"
          width="130"
          ><template #default="{ row }"
            ><span class="money">¥{{ Number(row.consumptionAmount).toFixed(2) }}</span></template
          ></el-table-column
        ><el-table-column prop="lastLoginTime" label="最近登录" min-width="170"
          ><template #default="{ row }">{{ row.lastLoginTime || '-' }}</template></el-table-column
        ><el-table-column prop="createTime" label="注册时间" min-width="170" /><el-table-column
          label="状态"
          width="90"
          ><template #default="{ row }"
            ><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{
              row.status === 1 ? '启用' : '禁用'
            }}</el-tag></template
          ></el-table-column
        ><el-table-column label="操作" width="230" fixed="right"
          ><template #default="{ row }"
            ><el-button link type="primary" @click="showDetail(row)">详情</el-button
            ><el-button link type="primary" @click="orders(row)">全部订单</el-button
            ><el-button
              v-if="auth.hasPermission('user:manage')"
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggle(row)"
              >{{ row.status === 1 ? '禁用' : '启用' }}</el-button
            ></template
          ></el-table-column
        ></el-table
      >
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </section>
    <el-drawer :model-value="Boolean(selected)" title="用户详情" size="520px" @update:model-value="(value: boolean) => { if (!value) selected = null }">
      <el-descriptions v-if="selected" :column="1" border>
        <el-descriptions-item label="用户ID">{{ selected.userId }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ selected.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ selected.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ selected.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单数量">{{ selected.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="消费金额">¥{{ Number(selected.consumptionAmount).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="最近登录">{{ selected.lastLoginTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selected.createTime }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">{{ selected.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="selected" class="drawer-actions"><el-button type="primary" @click="orders(selected)">查看全部订单</el-button></div>
    </el-drawer>
  </div>
</template>
<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.user-cell div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.user-cell strong,
.user-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.drawer-actions { display: flex; margin-top: 18px; justify-content: flex-end; }
.user-cell span {
  color: #697386;
  font-size: 12px;
}
</style>
