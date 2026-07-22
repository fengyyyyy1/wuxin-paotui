<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import { getMerchantPage } from '@/api/adminMerchant'
import PageHeader from '@/components/PageHeader.vue'
import type {
  AdminMerchantPageQuery,
  AdminMerchantPageVO,
  EnabledStatus,
  MerchantAuditStatus,
} from '@/types/merchant'
import {
  auditStatusMap,
  businessStatusMap,
  enabledStatusMap,
  isPendingAuditStatus,
  statusMeta,
} from '@/utils/merchantStatus'

type FilterValue<T extends number> = T | ''

interface FilterForm {
  auditStatus: FilterValue<MerchantAuditStatus>
  merchantStatus: FilterValue<EnabledStatus>
  keyword: string
}

const DEFAULT_PAGE_NUM = 1
const DEFAULT_PAGE_SIZE = 10

const router = useRouter()
const filters = reactive<FilterForm>({
  auditStatus: '',
  merchantStatus: '',
  keyword: '',
})
const merchants = ref<AdminMerchantPageVO[]>([])
const pageNum = ref(DEFAULT_PAGE_NUM)
const pageSize = ref(DEFAULT_PAGE_SIZE)
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')
let requestSeq = 0
let active = true

const paginationDisabled = computed(() => loading.value)

function buildQuery(): AdminMerchantPageQuery {
  const query: AdminMerchantPageQuery = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
  }
  const keyword = filters.keyword.trim()

  if (filters.auditStatus !== '') {
    query.auditStatus = filters.auditStatus
  }
  if (filters.merchantStatus !== '') {
    query.merchantStatus = filters.merchantStatus
  }
  if (keyword) {
    query.keyword = keyword
  }

  return query
}

function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

async function loadMerchants(): Promise<void> {
  if (loading.value) {
    return
  }

  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await getMerchantPage(buildQuery())
    if (!active || seq !== requestSeq) {
      return
    }

    merchants.value = result.records ?? []
    total.value = Number(result.total ?? 0)
    pageNum.value = Number(result.pageNum ?? pageNum.value)
    pageSize.value = Number(result.pageSize ?? pageSize.value)
  } catch (error) {
    if (!active || seq !== requestSeq) {
      return
    }

    errorMessage.value =
      error instanceof Error ? error.message || '商家列表加载失败' : '商家列表加载失败'
  } finally {
    if (active && seq === requestSeq) {
      loading.value = false
    }
  }
}

function handleSearch(): void {
  if (loading.value) {
    return
  }
  pageNum.value = DEFAULT_PAGE_NUM
  void loadMerchants()
}

function handleReset(): void {
  if (loading.value) {
    return
  }
  filters.auditStatus = ''
  filters.merchantStatus = ''
  filters.keyword = ''
  pageNum.value = DEFAULT_PAGE_NUM
  pageSize.value = DEFAULT_PAGE_SIZE
  void loadMerchants()
}

function handleRefresh(): void {
  void loadMerchants()
}

function handlePageChange(nextPage: number): void {
  if (loading.value) {
    return
  }
  pageNum.value = nextPage
  void loadMerchants()
}

function handlePageSizeChange(nextSize: number): void {
  if (loading.value) {
    return
  }
  pageSize.value = nextSize
  pageNum.value = DEFAULT_PAGE_NUM
  void loadMerchants()
}

function goDetail(merchantId: number): void {
  void router.push({
    name: 'merchant-detail',
    params: {
      merchantId,
    },
  })
}

onMounted(() => {
  void loadMerchants()
})

onUnmounted(() => {
  active = false
  requestSeq += 1
})
</script>

<template>
  <div class="page-shell">
    <PageHeader title="商家管理" subtitle="查看商家申请分页数据，按后端真实条件筛选">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="handleRefresh">刷新</el-button>
      </template>
    </PageHeader>

    <section class="filter-panel" aria-label="商家筛选">
      <el-form class="filter-form" :model="filters" label-position="top" @submit.prevent>
        <el-form-item label="审核状态">
          <el-select
            v-model="filters.auditStatus"
            placeholder="全部"
            clearable
            :disabled="loading"
          >
            <el-option label="全部" value="" />
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核驳回" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="商家状态">
          <el-select
            v-model="filters.merchantStatus"
            placeholder="全部"
            clearable
            :disabled="loading"
          >
            <el-option label="全部" value="" />
            <el-option label="禁用" :value="0" />
            <el-option label="启用" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            maxlength="100"
            clearable
            placeholder="商家名称、联系人或手机号"
            :disabled="loading"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">
            查询
          </el-button>
          <el-button :disabled="loading" @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </section>

    <el-alert
      v-if="errorMessage"
      class="list-alert"
      type="error"
      :title="errorMessage"
      show-icon
      :closable="false"
    >
      <template #default>
        <el-button link type="primary" :disabled="loading" @click="handleRefresh">重新加载</el-button>
      </template>
    </el-alert>

    <section class="table-panel" aria-label="商家列表">
      <el-table
        v-loading="loading"
        :data="merchants"
        row-key="merchantId"
        border
        class="merchant-table"
        empty-text="暂无商家数据"
      >
        <el-table-column prop="merchantId" label="商家ID" width="96" fixed="left" />
        <el-table-column prop="merchantName" label="商家名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="storeName" label="店铺名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" min-width="140" />
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMeta(auditStatusMap, row.auditStatus, row.auditStatusText).type">
              {{ statusMeta(auditStatusMap, row.auditStatus, row.auditStatusText).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商家状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMeta(enabledStatusMap, row.merchantStatus, row.merchantStatusText).type">
              {{ statusMeta(enabledStatusMap, row.merchantStatus, row.merchantStatusText).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="营业状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMeta(businessStatusMap, row.businessStatus, row.businessStatusText).type">
              {{ statusMeta(businessStatusMap, row.businessStatus, row.businessStatusText).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="goDetail(row.merchantId)">查看详情</el-button>
              <el-button
                v-if="isPendingAuditStatus(row.auditStatus)"
                link
                type="warning"
                @click="goDetail(row.merchantId)"
              >
                审核
              </el-button>
              <el-button v-else link type="info" @click="goDetail(row.merchantId)">
                查看审核信息
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :disabled="paginationDisabled"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.filter-panel,
.table-panel {
  margin-top: 24px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
}

.filter-panel {
  padding: 18px 20px 4px;
}

.filter-form {
  display: grid;
  grid-template-columns: minmax(150px, 180px) minmax(150px, 180px) minmax(240px, 1fr) auto;
  gap: 16px;
  align-items: end;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.filter-actions {
  display: flex;
  margin-bottom: 16px;
  gap: 10px;
}

.list-alert {
  margin-top: 16px;
}

.table-panel {
  padding: 0;
  overflow: hidden;
}

.merchant-table {
  width: 100%;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.pagination-bar {
  display: flex;
  min-height: 64px;
  padding: 14px 18px;
  align-items: center;
  justify-content: flex-end;
  border-top: 1px solid #e1e6ed;
}

@media (max-width: 980px) {
  .filter-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-actions {
    align-self: end;
  }
}

@media (max-width: 680px) {
  .filter-form {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    justify-content: flex-start;
  }

  .pagination-bar {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
