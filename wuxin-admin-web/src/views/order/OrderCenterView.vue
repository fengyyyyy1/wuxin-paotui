<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'

import { cancelOrder, completeOrder, getOrderDetail, getOrders } from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import type { OrderDetail, OrderQuery, OrderRow } from '@/types/admin'

const auth = useAuthStore()
const route = useRoute()
const loading = ref(false)
const rows = ref<OrderRow[]>([])
const total = ref(0)
const detail = ref<OrderDetail | null>(null)
const drawer = ref(false)
const filters = reactive({ keyword: '', orderType: '', status: '', abnormalOnly: false, dates: [] as string[] })
const page = reactive({ pageNum: 1, pageSize: 20 })
const related = reactive<{ userId?: number; riderId?: number; merchantId?: number }>({})

function positiveId(value: unknown): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value
  const result = Number(raw)
  return Number.isInteger(result) && result > 0 ? result : undefined
}

function query(): OrderQuery {
  const result: OrderQuery = { ...page }
  if (filters.keyword.trim()) result.keyword = filters.keyword.trim()
  if (filters.orderType !== '') result.orderType = Number(filters.orderType)
  if (filters.status !== '') result.status = Number(filters.status)
  if (filters.abnormalOnly) result.abnormalOnly = true
  if (related.userId) result.userId = related.userId
  if (related.riderId) result.riderId = related.riderId
  if (related.merchantId) result.merchantId = related.merchantId
  if (filters.dates.length === 2) { result.startTime = `${filters.dates[0]}T00:00:00`; result.endTime = `${filters.dates[1]}T23:59:59` }
  return result
}

async function load(): Promise<void> {
  loading.value = true
  try { const result = await getOrders(query()); rows.value = result.records; total.value = result.total }
  finally { loading.value = false }
}
function search(): void { page.pageNum = 1; void load() }
function reset(): void { Object.assign(filters, { keyword: '', orderType: '', status: '', abnormalOnly: false, dates: [] }); Object.keys(related).forEach((key) => delete related[key as keyof typeof related]); page.pageNum = 1; void load() }
async function showDetail(id: number): Promise<void> { detail.value = await getOrderDetail(id); drawer.value = true }
async function operate(action: 'cancel' | 'complete'): Promise<void> {
  if (!detail.value) return
  const label = action === 'cancel' ? '取消订单' : '人工完成订单'
  try {
    const { value } = await ElMessageBox.prompt(`请输入${label}原因，操作将写入订单日志`, label, { inputPattern: /^.{2,255}$/, inputErrorMessage: '请输入2至255个字符', confirmButtonText: '确认执行', cancelButtonText: '返回' })
    detail.value = action === 'cancel' ? await cancelOrder(detail.value.orderId, value) : await completeOrder(detail.value.orderId, value)
    ElMessage.success(`${label}成功`); await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') throw error
  }
}
const statusType = (value: unknown) => { const row = value as OrderRow; return row.abnormal ? 'danger' : row.status === 4 ? 'success' : row.status === 5 ? 'info' : 'warning' }
onMounted(() => {
  related.userId = positiveId(route.query.userId)
  related.riderId = positiveId(route.query.riderId)
  related.merchantId = positiveId(route.query.merchantId)
  void load()
})
</script>

<template>
  <div class="page-shell">
    <PageHeader title="订单中心" subtitle="统一管理跑腿订单与商品订单，所有状态操作写入审计日志"><template #actions><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button><el-button disabled>导出 Excel（预留）</el-button></template></PageHeader>
    <section class="content-panel"><el-form class="filter-grid" @submit.prevent><el-form-item label="关键词"><el-input v-model="filters.keyword" clearable placeholder="订单号、用户、手机号、门店" @keyup.enter="search" /></el-form-item><el-form-item label="订单类型"><el-select v-model="filters.orderType" clearable><el-option label="全部" value="" /><el-option label="跑腿订单" :value="0" /><el-option label="商品订单" :value="1" /></el-select></el-form-item><el-form-item label="订单状态"><el-select v-model="filters.status" clearable><el-option label="全部" value="" /><el-option v-for="item in [{v:0,l:'待接单'},{v:1,l:'已接单'},{v:2,l:'配送中'},{v:3,l:'待确认'},{v:4,l:'已完成'},{v:5,l:'已取消'},{v:6,l:'商家制作中'},{v:7,l:'待骑手接单'},{v:8,l:'待退款'}]" :key="item.v" :label="item.l" :value="item.v" /></el-select></el-form-item><el-form-item label="下单日期"><el-date-picker v-model="filters.dates" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" /></el-form-item><el-form-item label="异常订单"><el-switch v-model="filters.abnormalOnly" active-text="仅看异常" /></el-form-item><div class="filter-actions"><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button @click="reset">重置</el-button></div></el-form></section>
    <section class="content-panel table-panel"><el-table v-loading="loading" :data="rows" row-key="orderId" border><el-table-column prop="orderNo" label="订单号" min-width="190" fixed="left" /><el-table-column prop="orderTypeText" label="类型" width="100" /><el-table-column prop="userName" label="用户" min-width="120" /><el-table-column prop="riderName" label="骑手" min-width="120" /><el-table-column prop="storeName" label="商家" min-width="150" show-overflow-tooltip /><el-table-column prop="goodsName" label="内容" min-width="180" show-overflow-tooltip /><el-table-column label="状态" width="130"><template #default="{ row }"><el-tag :type="statusType(row)">{{ row.statusText }}</el-tag></template></el-table-column><el-table-column label="金额" width="110"><template #default="{ row }"><span class="money">¥{{ Number(row.totalAmount).toFixed(2) }}</span></template></el-table-column><el-table-column prop="createTime" label="下单时间" min-width="170" /><el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="showDetail(row.orderId)">详情</el-button></template></el-table-column></el-table><div class="pagination-bar"><el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total" :page-sizes="[20,50,100]" layout="total, sizes, prev, pager, next" @current-change="load" @size-change="search" /></div></section>
    <el-drawer v-model="drawer" title="订单详情" size="720px"><template v-if="detail"><el-descriptions :column="2" border><el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item><el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item><el-descriptions-item label="用户">{{ detail.userName }}</el-descriptions-item><el-descriptions-item label="骑手">{{ detail.riderName || '-' }}</el-descriptions-item><el-descriptions-item label="门店">{{ detail.storeName || '-' }}</el-descriptions-item><el-descriptions-item label="支付">{{ detail.payStatus === 1 ? '已支付' : '未支付' }}</el-descriptions-item><el-descriptions-item label="商品金额">¥{{ Number(detail.productAmount || 0).toFixed(2) }}</el-descriptions-item><el-descriptions-item label="配送费">¥{{ Number(detail.deliveryFee || 0).toFixed(2) }}</el-descriptions-item><el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item></el-descriptions><div v-if="auth.hasPermission('order:manage')" class="drawer-actions"><el-button type="danger" plain :disabled="[4,5,8].includes(detail.status)" @click="operate('cancel')">取消订单</el-button><el-button type="success" plain :disabled="![2,3].includes(detail.status)" @click="operate('complete')">人工完成</el-button><el-button disabled>退款审核（预留）</el-button></div><h3>商品明细</h3><el-table :data="detail.items" size="small" border><el-table-column prop="productName" label="商品" /><el-table-column prop="productPrice" label="单价" width="100" /><el-table-column prop="quantity" label="数量" width="80" /><el-table-column prop="subtotal" label="小计" width="100" /></el-table><h3>订单日志</h3><el-timeline><el-timeline-item v-for="log in detail.logs" :key="log.id" :timestamp="log.createTime"><strong>{{ log.operatorType }}</strong> {{ log.remark }}（{{ log.oldStatus }} → {{ log.newStatus }}）</el-timeline-item></el-timeline></template></el-drawer>
  </div>
</template>

<style scoped>
.drawer-actions { display: flex; margin: 18px 0; gap: 10px; }
h3 { margin: 24px 0 12px; font-size: 16px; }
</style>
