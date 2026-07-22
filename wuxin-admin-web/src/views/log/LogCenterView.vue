<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'

import { getOperationLogs } from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import type { LogQuery, OperationLogItem } from '@/types/admin'

const loading = ref(false)
const rows = ref<OperationLogItem[]>([])
const total = ref(0)
const detail = ref<OperationLogItem | null>(null)
const filters = reactive({ keyword: '', adminUserId: '', moduleCode: '', resultStatus: '', dates: [] as string[] })
const page = reactive({ pageNum: 1, pageSize: 20 })

const modules = ['auth', 'dashboard', 'order', 'user', 'rider', 'merchant', 'product', 'operation', 'finance', 'config', 'rbac']

function query(): LogQuery {
  const result: LogQuery = { ...page }
  if (filters.keyword.trim()) result.keyword = filters.keyword.trim()
  if (filters.adminUserId) result.adminUserId = Number(filters.adminUserId)
  if (filters.moduleCode) result.moduleCode = filters.moduleCode
  if (filters.resultStatus !== '') result.resultStatus = Number(filters.resultStatus)
  if (filters.dates.length === 2) {
    result.startTime = `${filters.dates[0]}T00:00:00`
    result.endTime = `${filters.dates[1]}T23:59:59`
  }
  return result
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const result = await getOperationLogs(query())
    rows.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function search(): void { page.pageNum = 1; void load() }
function reset(): void {
  Object.assign(filters, { keyword: '', adminUserId: '', moduleCode: '', resultStatus: '', dates: [] })
  search()
}
function openDetail(value: unknown): void { detail.value = value as OperationLogItem }

onMounted(() => void load())
</script>

<template>
  <div class="page-shell">
    <PageHeader title="日志中心" subtitle="集中查询管理员登录、审核、状态变更和配置操作记录">
      <template #actions><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></template>
    </PageHeader>

    <section class="content-panel">
      <el-form class="filter-grid" @submit.prevent="search">
        <el-form-item label="关键词"><el-input v-model="filters.keyword" clearable placeholder="操作、目标或路径" /></el-form-item>
        <el-form-item label="管理员ID"><el-input v-model="filters.adminUserId" clearable inputmode="numeric" /></el-form-item>
        <el-form-item label="业务模块"><el-select v-model="filters.moduleCode" clearable><el-option label="全部" value="" /><el-option v-for="item in modules" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="执行结果"><el-select v-model="filters.resultStatus" clearable><el-option label="全部" value="" /><el-option label="成功" :value="1" /><el-option label="失败" :value="0" /></el-select></el-form-item>
        <el-form-item label="操作日期"><el-date-picker v-model="filters.dates" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /></el-form-item>
        <div class="filter-actions"><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button @click="reset">重置</el-button></div>
      </el-form>
    </section>

    <section class="content-panel table-panel">
      <el-table v-loading="loading" :data="rows" row-key="id" border>
        <el-table-column prop="createTime" label="时间" min-width="170" fixed="left" />
        <el-table-column prop="adminUsername" label="管理员" min-width="120" />
        <el-table-column prop="moduleCode" label="模块" width="110" />
        <el-table-column prop="operationName" label="操作" min-width="170" />
        <el-table-column prop="targetType" label="目标类型" width="130" />
        <el-table-column prop="targetId" label="目标ID" min-width="110" />
        <el-table-column prop="requestIp" label="IP" min-width="140" />
        <el-table-column label="结果" width="90"><template #default="{ row }"><el-tag :type="row.resultStatus === 1 ? 'success' : 'danger'">{{ row.resultStatus === 1 ? '成功' : '失败' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">详情</el-button></template></el-table-column>
      </el-table>
      <div class="pagination-bar"><el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next" @current-change="load" @size-change="search" /></div>
    </section>

    <el-drawer :model-value="Boolean(detail)" title="操作日志详情" size="680px" @update:model-value="(value: boolean) => { if (!value) detail = null }">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="管理员">{{ detail.adminUsername }}（{{ detail.adminUserId }}）</el-descriptions-item>
          <el-descriptions-item label="执行结果">{{ detail.resultStatus === 1 ? '成功' : '失败' }}</el-descriptions-item>
          <el-descriptions-item label="模块">{{ detail.moduleCode }}</el-descriptions-item>
          <el-descriptions-item label="操作编码">{{ detail.operationCode }}</el-descriptions-item>
          <el-descriptions-item label="目标">{{ detail.targetType || '-' }} / {{ detail.targetId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="请求" :span="2">{{ detail.requestMethod || '-' }} {{ detail.requestPath || '-' }}</el-descriptions-item>
          <el-descriptions-item label="请求IP" :span="2">{{ detail.requestIp || '-' }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2">{{ detail.errorMessage || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h3>变更前</h3><pre>{{ detail.beforeData || '-' }}</pre>
        <h3>变更后</h3><pre>{{ detail.afterData || '-' }}</pre>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
h3 { margin: 22px 0 10px; font-size: 15px; }
pre { max-height: 260px; margin: 0; padding: 14px; overflow: auto; color: #dce5ef; background: #172033; border-radius: 6px; white-space: pre-wrap; word-break: break-word; }
</style>
