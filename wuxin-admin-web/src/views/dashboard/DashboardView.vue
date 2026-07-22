<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsCoreOption } from 'echarts/core'
import { Refresh } from '@element-plus/icons-vue'

import { getDashboard } from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import type { DashboardData, RankingItem } from '@/types/admin'

const loading = ref(false)
const data = ref<DashboardData | null>(null)
const orderChartElement = ref<HTMLDivElement>()
const revenueChartElement = ref<HTMLDivElement>()
echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

let orderChart: ECharts | null = null
let revenueChart: ECharts | null = null

const metrics: Array<{ key: keyof DashboardData; label: string; money?: boolean }> = [
  { key: 'todayOrders', label: '今日订单' },
  { key: 'todayRevenue', label: '今日营业额', money: true },
  { key: 'todayDeliveries', label: '今日配送' },
  { key: 'newUsers', label: '新增用户' },
  { key: 'newRiders', label: '新增骑手' },
  { key: 'newMerchants', label: '新增商家' },
  { key: 'pendingRiders', label: '待审核骑手' },
  { key: 'pendingMerchants', label: '待审核商家' },
  { key: 'pendingRefunds', label: '待处理退款' },
]

async function load(): Promise<void> {
  loading.value = true
  try {
    data.value = await getDashboard()
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts(): void {
  if (!data.value || !orderChartElement.value || !revenueChartElement.value) return
  orderChart ??= echarts.init(orderChartElement.value)
  revenueChart ??= echarts.init(revenueChartElement.value)
  const dates = data.value.orderTrend.map((item) => item.date.slice(5))
  orderChart.setOption(lineOption(dates, data.value.orderTrend.map((item) => item.value), '#18a058', '订单量'))
  revenueChart.setOption(lineOption(dates, data.value.revenueTrend.map((item) => item.value), '#d97706', '营业额'))
}

function lineOption(dates: string[], values: number[], color: string, name: string): EChartsCoreOption {
  return {
    animationDuration: 400,
    grid: { left: 48, right: 18, top: 28, bottom: 34 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, boundaryGap: false, axisLine: { lineStyle: { color: '#dfe3e8' } } },
    yAxis: { type: 'value', minInterval: name === '订单量' ? 1 : undefined, splitLine: { lineStyle: { color: '#eef1f4' } } },
    series: [{ name, type: 'line', data: values, smooth: true, symbolSize: 7, itemStyle: { color }, lineStyle: { width: 3, color }, areaStyle: { color: `${color}18` } }],
  }
}

function formatMetric(metric: (typeof metrics)[number]): string {
  const value = Number(data.value?.[metric.key] ?? 0)
  return metric.money ? `¥${value.toFixed(2)}` : value.toLocaleString()
}

function rankingValue(value: unknown, money = false): string {
  const item = value as RankingItem
  return money ? `¥${Number(item.value).toFixed(2)}` : `${item.count} 单`
}

function resize(): void { orderChart?.resize(); revenueChart?.resize() }

onMounted(() => { void load(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); orderChart?.dispose(); revenueChart?.dispose() })
</script>

<template>
  <div v-loading="loading" class="page-shell">
    <PageHeader title="Dashboard" subtitle="平台实时运营概览，数据来自真实业务库">
      <template #actions><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></template>
    </PageHeader>

    <section class="metric-grid dashboard-metrics">
      <div v-for="metric in metrics" :key="metric.key" class="metric-item">
        <span>{{ metric.label }}</span><strong>{{ formatMetric(metric) }}</strong>
      </div>
    </section>

    <section class="chart-grid">
      <div class="content-panel chart-panel"><div class="section-heading"><h2>订单走势</h2><span class="muted">近 7 天</span></div><div ref="orderChartElement" class="chart"></div></div>
      <div class="content-panel chart-panel"><div class="section-heading"><h2>营业额走势</h2><span class="muted">近 7 天</span></div><div ref="revenueChartElement" class="chart"></div></div>
    </section>

    <section class="ranking-grid">
      <div class="content-panel"><div class="section-heading"><h2>热门商品 TOP10</h2></div><el-table :data="data?.topProducts ?? []" size="small"><el-table-column type="index" width="48" /><el-table-column prop="name" label="商品" show-overflow-tooltip /><el-table-column label="销量" width="90"><template #default="{ row }">{{ rankingValue(row) }}</template></el-table-column></el-table></div>
      <div class="content-panel"><div class="section-heading"><h2>热门商家 TOP10</h2></div><el-table :data="data?.topMerchants ?? []" size="small"><el-table-column type="index" width="48" /><el-table-column prop="name" label="门店" show-overflow-tooltip /><el-table-column label="营业额" width="110"><template #default="{ row }">{{ rankingValue(row, true) }}</template></el-table-column></el-table></div>
      <div class="content-panel"><div class="section-heading"><h2>优秀骑手 TOP10</h2></div><el-table :data="data?.topRiders ?? []" size="small"><el-table-column type="index" width="48" /><el-table-column prop="name" label="骑手" show-overflow-tooltip /><el-table-column label="完成" width="90"><template #default="{ row }">{{ rankingValue(row) }}</template></el-table-column></el-table></div>
    </section>

    <section class="content-panel table-panel"><div class="section-heading table-heading"><h2>最近订单</h2><RouterLink to="/orders"><el-button link type="primary">查看全部</el-button></RouterLink></div><el-table :data="data?.recentOrders ?? []"><el-table-column prop="orderNo" label="订单号" min-width="190" /><el-table-column prop="orderTypeText" label="类型" width="100" /><el-table-column prop="userName" label="用户" min-width="120" /><el-table-column prop="storeName" label="商家" min-width="150" show-overflow-tooltip /><el-table-column prop="statusText" label="状态" width="130" /><el-table-column label="金额" width="110"><template #default="{ row }"><span class="money">¥{{ Number(row.totalAmount).toFixed(2) }}</span></template></el-table-column><el-table-column prop="createTime" label="下单时间" min-width="170" /></el-table></section>

    <section class="content-panel"><div class="section-heading"><h2>系统公告</h2><RouterLink to="/operations"><el-button link type="primary">公告管理</el-button></RouterLink></div><el-empty v-if="!data?.notices.length" description="暂无已发布公告" :image-size="72" /><div v-else class="notice-list"><div v-for="notice in data.notices" :key="notice.id" class="notice-row"><strong>{{ notice.title }}</strong><span>{{ notice.content }}</span><time>{{ notice.publishTime || '未设置发布时间' }}</time></div></div></section>
  </div>
</template>

<style scoped>
.dashboard-metrics { margin-top: 20px; grid-template-columns: repeat(5, minmax(0, 1fr)); }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.chart { height: 300px; }
.ranking-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.ranking-grid .content-panel { min-width: 0; padding: 16px; }
.table-heading { padding: 16px 18px 0; }
.notice-list { display: grid; gap: 0; }
.notice-row { display: grid; grid-template-columns: minmax(150px, 220px) minmax(240px, 1fr) 180px; padding: 14px 0; align-items: center; gap: 18px; border-top: 1px solid #edf0f3; }
.notice-row:first-child { border-top: 0; }
.notice-row span, .notice-row time { overflow: hidden; color: #697386; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 1100px) { .dashboard-metrics { grid-template-columns: repeat(3, minmax(0, 1fr)); } .ranking-grid { grid-template-columns: 1fr; } }
@media (max-width: 760px) { .dashboard-metrics, .chart-grid { grid-template-columns: 1fr; } .notice-row { grid-template-columns: 1fr; gap: 6px; } }
</style>
