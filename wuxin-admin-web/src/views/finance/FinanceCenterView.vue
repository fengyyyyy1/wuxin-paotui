<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getFinance } from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import type { FinanceData } from '@/types/admin'

const loading=ref(false);const data=ref<FinanceData|null>(null)
const metrics:Array<{key:keyof FinanceData;label:string;rate?:boolean}>=[{key:'platformRevenue',label:'平台营业额'},{key:'todayIncome',label:'今日收入'},{key:'yesterdayIncome',label:'昨日收入'},{key:'monthIncome',label:'本月收入'},{key:'orderAmount',label:'累计订单金额'},{key:'platformCommission',label:'平台抽成统计'},{key:'merchantIncome',label:'商家收入统计'},{key:'riderIncome',label:'骑手收入（预留口径）'}]
async function load():Promise<void>{loading.value=true;try{data.value=await getFinance()}finally{loading.value=false}}
const money=(value:number|undefined)=>`¥${Number(value||0).toFixed(2)}`;const percent=(value:number|undefined)=>`${(Number(value||0)*100).toFixed(2)}%`
onMounted(()=>void load())
</script>
<template><div v-loading="loading" class="page-shell"><PageHeader title="财务中心" subtitle="基于已支付订单与数据库抽成配置实时统计，不形成财务总账"><template #actions><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></template></PageHeader><section class="metric-grid finance-metrics"><div v-for="metric in metrics" :key="metric.key" class="metric-item"><span>{{metric.label}}</span><strong>{{money(Number(data?.[metric.key]))}}</strong></div></section><section class="content-panel"><div class="section-heading"><h2>当前结算参数</h2><RouterLink to="/configs"><el-button link type="primary">系统配置</el-button></RouterLink></div><el-descriptions :column="3" border><el-descriptions-item label="平台抽成比例">{{percent(data?.platformCommissionRate)}}</el-descriptions-item><el-descriptions-item label="商家抽成比例">{{percent(data?.merchantCommissionRate)}}</el-descriptions-item><el-descriptions-item label="骑手奖励比例">{{percent(data?.riderRewardRate)}}</el-descriptions-item></el-descriptions><el-alert class="finance-note" type="info" :closable="false" show-icon title="当前为运营统计口径；退款、结算单、提现与对账将在生产财务阶段建立独立流水后启用。"/></section></div></template>
<style scoped>.finance-metrics{margin-top:20px;grid-template-columns:repeat(4,minmax(0,1fr))}.finance-note{margin-top:18px}@media(max-width:900px){.finance-metrics{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:560px){.finance-metrics{grid-template-columns:1fr}}</style>
