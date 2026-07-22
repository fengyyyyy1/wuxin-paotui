<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import {
  getCategories,
  getProducts,
  updateProductFlags,
  updateProductStatus,
} from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import type { CategoryRow, ProductQuery, ProductRow } from '@/types/admin'

const auth = useAuthStore()
const route = useRoute()
const tab = ref('products')
const rows = ref<ProductRow[]>([])
const categories = ref<CategoryRow[]>([])
const total = ref(0)
const loading = ref(false)
const filters = reactive({ keyword: '', productStatus: '', recommended: false, hot: false })
const page = reactive({ pageNum: 1, pageSize: 20 })
const storeId = ref<number>()
function query(): ProductQuery {
  const q: ProductQuery = { ...page }
  if (filters.keyword.trim()) q.keyword = filters.keyword.trim()
  if (filters.productStatus !== '') q.productStatus = Number(filters.productStatus)
  if (filters.recommended) q.recommended = true
  if (filters.hot) q.hot = true
  if (storeId.value) q.storeId = storeId.value
  return q
}
async function load(): Promise<void> {
  loading.value = true
  try {
    const [products, cats] = await Promise.all([getProducts(query()), getCategories()])
    rows.value = products.records
    total.value = products.total
    categories.value = cats
  } finally {
    loading.value = false
  }
}
function search(): void {
  page.pageNum = 1
  void load()
}
function reset(): void {
  Object.assign(filters, { keyword: '', productStatus: '', recommended: false, hot: false })
  storeId.value = undefined
  search()
}
async function status(value: unknown): Promise<void> {
  const row = value as ProductRow
  const next = row.productStatus === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${next ? '上架' : '下架'}“${row.productName}”？`, '商品状态')
  await updateProductStatus(row.productId, next)
  ElMessage.success('商品状态已更新')
  await load()
}
async function flags(value: unknown, key: 'recommended' | 'hot', checked: unknown): Promise<void> {
  const row = value as ProductRow
  const enabled = Boolean(checked)
  await updateProductFlags(
    row.productId,
    key === 'recommended' ? enabled : row.recommended,
    key === 'hot' ? enabled : row.hot,
  )
  ElMessage.success('运营标记已更新')
  await load()
}
onMounted(() => {
  const value = Number(Array.isArray(route.query.storeId) ? route.query.storeId[0] : route.query.storeId)
  storeId.value = Number.isInteger(value) && value > 0 ? value : undefined
  void load()
})
</script>
<template>
  <div class="page-shell">
    <PageHeader title="商品中心" subtitle="商品上下架与首页运营标记实时影响用户端展示"
      ><template #actions
        ><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></template
      ></PageHeader
    ><el-tabs v-model="tab" class="content-panel product-tabs"
      ><el-tab-pane label="全部商品" name="products"
        ><el-form class="filter-grid"
          ><el-form-item label="关键词"
            ><el-input
              v-model="filters.keyword"
              clearable
              placeholder="商品或门店"
              @keyup.enter="search" /></el-form-item
          ><el-form-item label="上下架"
            ><el-select v-model="filters.productStatus" clearable
              ><el-option label="全部" value="" /><el-option label="已上架" :value="1" /><el-option
                label="已下架"
                :value="0" /></el-select></el-form-item
          ><el-form-item label="运营筛选"
            ><div class="inline-actions">
              <el-checkbox v-model="filters.recommended">推荐</el-checkbox
              ><el-checkbox v-model="filters.hot">热门</el-checkbox>
            </div></el-form-item
          >
          <div class="filter-actions">
            <el-button type="primary" :icon="Search" @click="search">查询</el-button
            ><el-button @click="reset">重置</el-button
            >
          </div></el-form
        ><el-table v-loading="loading" :data="rows" border class="product-table"
          ><el-table-column label="商品" min-width="230" fixed="left"
            ><template #default="{ row }"
              ><div class="product-cell">
                <el-image :src="row.productImage" fit="cover"
                  ><template #error><div class="image-error">无图</div></template></el-image
                >
                <div>
                  <strong>{{ row.productName }}</strong
                  ><span>ID {{ row.productId }}</span>
                </div>
              </div></template
            ></el-table-column
          ><el-table-column prop="storeName" label="门店" min-width="160" /><el-table-column
            prop="categoryName"
            label="分类"
            min-width="120"
          /><el-table-column label="价格" width="100"
            ><template #default="{ row }"
              ><span class="money">¥{{ Number(row.price).toFixed(2) }}</span></template
            ></el-table-column
          ><el-table-column prop="stock" label="库存" width="90" /><el-table-column
            prop="sales"
            label="销量"
            width="90"
          /><el-table-column label="状态" width="90"
            ><template #default="{ row }"
              ><el-tag :type="row.productStatus === 1 ? 'success' : 'info'">{{
                row.productStatus === 1 ? '上架' : '下架'
              }}</el-tag></template
            ></el-table-column
          ><el-table-column label="推荐" width="90"
            ><template #default="{ row }"
              ><el-switch
                :model-value="row.recommended"
                :disabled="!auth.hasPermission('product:manage')"
                @change="flags(row, 'recommended', $event)" /></template></el-table-column
          ><el-table-column label="热门" width="90"
            ><template #default="{ row }"
              ><el-switch
                :model-value="row.hot"
                :disabled="!auth.hasPermission('product:manage')"
                @change="flags(row, 'hot', $event)" /></template></el-table-column
          ><el-table-column label="操作" width="90" fixed="right"
            ><template #default="{ row }"
              ><el-button
                v-if="auth.hasPermission('product:manage')"
                link
                :type="row.productStatus === 1 ? 'danger' : 'success'"
                @click="status(row)"
                >{{ row.productStatus === 1 ? '下架' : '上架' }}</el-button
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
          /></div></el-tab-pane
      ><el-tab-pane label="分类" name="categories"
        ><el-table :data="categories" border
          ><el-table-column prop="categoryId" label="分类ID" width="100" /><el-table-column
            prop="storeName"
            label="门店"
            min-width="180"
          /><el-table-column prop="categoryName" label="分类名称" min-width="160" /><el-table-column
            prop="productCount"
            label="商品数"
            width="100"
          /><el-table-column prop="sort" label="排序" width="90" /><el-table-column
            label="状态"
            width="100"
            ><template #default="{ row }"
              ><el-tag :type="row.status === 1 ? 'success' : 'info'">{{
                row.status === 1 ? '启用' : '禁用'
              }}</el-tag></template
            ></el-table-column
          ></el-table
        ></el-tab-pane
      ></el-tabs
    >
  </div>
</template>
<style scoped>
.product-tabs {
  padding: 16px 20px 20px;
}
.product-table {
  margin-top: 18px;
}
.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-cell .el-image {
  width: 48px;
  height: 48px;
  flex: 0 0 48px;
  border-radius: 4px;
}
.product-cell div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}
.product-cell strong,
.product-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-cell span {
  color: #697386;
  font-size: 12px;
}
.image-error {
  display: grid;
  height: 100%;
  color: #999;
  background: #f2f4f6;
  place-items: center;
}
</style>
