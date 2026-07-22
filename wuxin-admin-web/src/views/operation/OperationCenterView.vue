<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteBanner,
  deleteNotice,
  deleteRecommendation,
  getBanners,
  getNotices,
  getRecommendations,
  saveBanner,
  saveNotice,
  saveRecommendation,
} from '@/api/adminConsole'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import type {
  BannerItem,
  BannerPayload,
  NoticeItem,
  NoticePayload,
  RecommendationItem,
  RecommendationPayload,
} from '@/types/admin'

const auth = useAuthStore()
const loading = ref(false)
const tab = ref('banners')
const banners = ref<BannerItem[]>([])
const notices = ref<NoticeItem[]>([])
const recommendations = ref<RecommendationItem[]>([])
const bannerDialog = ref(false)
const noticeDialog = ref(false)
const recommendationDialog = ref(false)
const editingId = ref<number>()
const bannerForm = reactive<BannerPayload>({
  title: '',
  subtitle: null,
  imageUrl: '',
  targetType: 'NONE',
  targetValue: null,
  sort: 0,
  status: 1,
  startTime: null,
  endTime: null,
})
const noticeForm = reactive<NoticePayload>({
  noticeType: 'SYSTEM',
  title: '',
  content: '',
  status: 0,
  publishTime: null,
  expireTime: null,
})
const recommendationForm = reactive<RecommendationPayload>({
  recommendationType: 'STORE',
  targetId: 0,
  titleOverride: null,
  sort: 0,
  status: 1,
  startTime: null,
  endTime: null,
})
async function load(): Promise<void> {
  loading.value = true
  try {
    ;[banners.value, notices.value, recommendations.value] = await Promise.all([
      getBanners(),
      getNotices(),
      getRecommendations(),
    ])
  } finally {
    loading.value = false
  }
}
function editBanner(value?: unknown): void {
  const row = value as BannerItem | undefined
  editingId.value = row?.id
  Object.assign(
    bannerForm,
    row
      ? {
          title: row.title,
          subtitle: row.subtitle,
          imageUrl: row.imageUrl,
          targetType: row.targetType,
          targetValue: row.targetValue,
          sort: row.sort,
          status: row.status,
          startTime: row.startTime,
          endTime: row.endTime,
        }
      : {
          title: '',
          subtitle: null,
          imageUrl: '',
          targetType: 'NONE',
          targetValue: null,
          sort: 0,
          status: 1,
          startTime: null,
          endTime: null,
        },
  )
  bannerDialog.value = true
}
function editNotice(value?: unknown): void {
  const row = value as NoticeItem | undefined
  editingId.value = row?.id
  Object.assign(
    noticeForm,
    row
      ? {
          noticeType: row.noticeType,
          title: row.title,
          content: row.content,
          status: row.status,
          publishTime: row.publishTime,
          expireTime: row.expireTime,
        }
      : {
          noticeType: 'SYSTEM',
          title: '',
          content: '',
          status: 0,
          publishTime: null,
          expireTime: null,
        },
  )
  noticeDialog.value = true
}
function editRecommendation(value?: unknown): void {
  const row = value as RecommendationItem | undefined
  editingId.value = row?.id
  Object.assign(
    recommendationForm,
    row
      ? {
          recommendationType: row.recommendationType,
          targetId: row.targetId,
          titleOverride: row.titleOverride,
          sort: row.sort,
          status: row.status,
          startTime: row.startTime,
          endTime: row.endTime,
        }
      : {
          recommendationType: 'STORE',
          targetId: 0,
          titleOverride: null,
          sort: 0,
          status: 1,
          startTime: null,
          endTime: null,
        },
  )
  recommendationDialog.value = true
}
async function submitBanner(): Promise<void> {
  await saveBanner({ ...bannerForm }, editingId.value)
  bannerDialog.value = false
  ElMessage.success('Banner已保存')
  await load()
}
async function submitNotice(): Promise<void> {
  await saveNotice({ ...noticeForm }, editingId.value)
  noticeDialog.value = false
  ElMessage.success('公告已保存')
  await load()
}
async function submitRecommendation(): Promise<void> {
  await saveRecommendation({ ...recommendationForm }, editingId.value)
  recommendationDialog.value = false
  ElMessage.success('首页推荐已保存')
  await load()
}
async function remove(type: 'banner' | 'notice' | 'recommendation', id: number): Promise<void> {
  await ElMessageBox.confirm('确认删除该配置？删除后用户端将不再展示。', '删除确认', {
    type: 'warning',
  })
  if (type === 'banner') await deleteBanner(id)
  else if (type === 'notice') await deleteNotice(id)
  else await deleteRecommendation(id)
  ElMessage.success('已删除')
  await load()
}
onMounted(() => void load())
</script>
<template>
  <div class="page-shell">
    <PageHeader title="运营中心" subtitle="Banner、公告与首页推荐均由数据库驱动并实时下发用户端"
      ><template #actions
        ><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button
        ><el-button disabled>活动配置（预留）</el-button></template
      ></PageHeader
    ><el-tabs v-model="tab" class="content-panel operation-tabs"
      ><el-tab-pane label="Banner 管理" name="banners"
        ><div class="section-heading">
          <h2>首页 Banner</h2>
          <el-button
            v-if="auth.hasPermission('operation:manage')"
            type="primary"
            :icon="Plus"
            @click="editBanner()"
            >新增 Banner</el-button
          >
        </div>
        <el-table :data="banners" border
          ><el-table-column prop="id" label="ID" width="70" /><el-table-column
            label="图片"
            width="140"
            ><template #default="{ row }"
              ><el-image class="banner-thumb" :src="row.imageUrl" fit="cover"
                ><template #error><div class="image-error">图片不可用</div></template></el-image
              ></template
            ></el-table-column
          ><el-table-column prop="title" label="标题" min-width="160" /><el-table-column
            prop="targetType"
            label="跳转类型"
            width="110"
          /><el-table-column
            prop="targetValue"
            label="跳转目标"
            min-width="180"
            show-overflow-tooltip
          /><el-table-column prop="sort" label="排序" width="80" /><el-table-column
            label="状态"
            width="90"
            ><template #default="{ row }"
              ><el-tag :type="row.status === 1 ? 'success' : 'info'">{{
                row.status === 1 ? '启用' : '禁用'
              }}</el-tag></template
            ></el-table-column
          ><el-table-column label="操作" width="130"
            ><template #default="{ row }"
              ><el-button v-if="auth.hasPermission('operation:manage')" link :icon="Edit" @click="editBanner(row)">编辑</el-button
              ><el-button v-if="auth.hasPermission('operation:manage')" link type="danger" :icon="Delete" @click="remove('banner', row.id)"
                >删除</el-button
              ></template
            ></el-table-column
          ></el-table
        ></el-tab-pane
      ><el-tab-pane label="公告管理" name="notices"
        ><div class="section-heading">
          <h2>系统公告</h2>
          <el-button
            v-if="auth.hasPermission('operation:manage')"
            type="primary"
            :icon="Plus"
            @click="editNotice()"
            >新增公告</el-button
          >
        </div>
        <el-table :data="notices" border
          ><el-table-column prop="id" label="ID" width="70" /><el-table-column
            prop="noticeType"
            label="对象"
            width="110"
          /><el-table-column prop="title" label="标题" min-width="180" /><el-table-column
            prop="content"
            label="内容"
            min-width="280"
            show-overflow-tooltip
          /><el-table-column prop="publishTime" label="发布时间" width="170" /><el-table-column
            label="状态"
            width="90"
            ><template #default="{ row }"
              ><el-tag
                :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'"
                >{{ ['草稿', '已发布', '已下线'][row.status] }}</el-tag
              ></template
            ></el-table-column
          ><el-table-column label="操作" width="130"
            ><template #default="{ row }"
              ><el-button v-if="auth.hasPermission('operation:manage')" link :icon="Edit" @click="editNotice(row)">编辑</el-button
              ><el-button v-if="auth.hasPermission('operation:manage')" link type="danger" :icon="Delete" @click="remove('notice', row.id)"
                >删除</el-button
              ></template
            ></el-table-column
          ></el-table
        ></el-tab-pane
      ><el-tab-pane label="首页推荐" name="recommendations"
        ><div class="section-heading">
          <h2>推荐商家、商品与分类</h2>
          <el-button
            v-if="auth.hasPermission('operation:manage')"
            type="primary"
            :icon="Plus"
            @click="editRecommendation()"
            >新增推荐</el-button
          >
        </div>
        <el-table :data="recommendations" border
          ><el-table-column prop="id" label="ID" width="70" /><el-table-column
            prop="recommendationType"
            label="类型"
            width="130"
          /><el-table-column prop="targetId" label="目标ID" width="100" /><el-table-column
            prop="targetName"
            label="目标"
            min-width="180"
          /><el-table-column
            prop="titleOverride"
            label="展示标题"
            min-width="150"
          /><el-table-column prop="sort" label="排序" width="80" /><el-table-column
            label="状态"
            width="90"
            ><template #default="{ row }"
              ><el-tag :type="row.status === 1 ? 'success' : 'info'">{{
                row.status === 1 ? '启用' : '禁用'
              }}</el-tag></template
            ></el-table-column
          ><el-table-column label="操作" width="130"
            ><template #default="{ row }"
              ><el-button v-if="auth.hasPermission('operation:manage')" link :icon="Edit" @click="editRecommendation(row)">编辑</el-button
              ><el-button
                v-if="auth.hasPermission('operation:manage')"
                link
                type="danger"
                :icon="Delete"
                @click="remove('recommendation', row.id)"
                >删除</el-button
              ></template
            ></el-table-column
          ></el-table
        ></el-tab-pane
      ></el-tabs
    >
    <el-dialog
      v-model="bannerDialog"
      :title="editingId ? '编辑 Banner' : '新增 Banner'"
      width="620px"
      ><el-form label-position="top"
        ><div class="dialog-grid">
          <el-form-item label="标题"
            ><el-input v-model="bannerForm.title" maxlength="100" /></el-form-item
          ><el-form-item label="副标题"
            ><el-input v-model="bannerForm.subtitle" maxlength="200" /></el-form-item
          ><el-form-item class="full" label="图片 HTTPS 地址"
            ><el-input v-model="bannerForm.imageUrl" /></el-form-item
          ><el-form-item label="跳转类型"
            ><el-select v-model="bannerForm.targetType"
              ><el-option
                v-for="v in ['NONE', 'STORE', 'PRODUCT', 'PAGE', 'URL']"
                :key="v"
                :label="v"
                :value="v" /></el-select></el-form-item
          ><el-form-item label="跳转目标"
            ><el-input v-model="bannerForm.targetValue" /></el-form-item
          ><el-form-item label="排序"><el-input-number v-model="bannerForm.sort" /></el-form-item
          ><el-form-item label="状态"
            ><el-switch
              v-model="bannerForm.status"
              :active-value="1"
              :inactive-value="0" /></el-form-item
          ><el-form-item label="开始时间"
            ><el-date-picker
              v-model="bannerForm.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="结束时间"
            ><el-date-picker
              v-model="bannerForm.endTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
          /></el-form-item></div></el-form
      ><template #footer
        ><el-button @click="bannerDialog = false">取消</el-button
        ><el-button type="primary" @click="submitBanner">保存</el-button></template
      ></el-dialog
    >
    <el-dialog v-model="noticeDialog" :title="editingId ? '编辑公告' : '新增公告'" width="620px"
      ><el-form label-position="top"
        ><div class="dialog-grid">
          <el-form-item label="公告对象"
            ><el-select v-model="noticeForm.noticeType"
              ><el-option
                v-for="v in ['SYSTEM', 'USER', 'MERCHANT', 'RIDER']"
                :key="v"
                :label="v"
                :value="v" /></el-select></el-form-item
          ><el-form-item label="状态"
            ><el-select v-model="noticeForm.status"
              ><el-option label="草稿" :value="0" /><el-option label="发布" :value="1" /><el-option
                label="下线"
                :value="2" /></el-select></el-form-item
          ><el-form-item class="full" label="标题"
            ><el-input v-model="noticeForm.title" maxlength="120" /></el-form-item
          ><el-form-item class="full" label="内容"
            ><el-input
              v-model="noticeForm.content"
              type="textarea"
              :rows="7"
              maxlength="20000"
              show-word-limit /></el-form-item
          ><el-form-item label="发布时间"
            ><el-date-picker
              v-model="noticeForm.publishTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="过期时间"
            ><el-date-picker
              v-model="noticeForm.expireTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
          /></el-form-item></div></el-form
      ><template #footer
        ><el-button @click="noticeDialog = false">取消</el-button
        ><el-button type="primary" @click="submitNotice">保存</el-button></template
      ></el-dialog
    >
    <el-dialog
      v-model="recommendationDialog"
      :title="editingId ? '编辑首页推荐' : '新增首页推荐'"
      width="620px"
      ><el-form label-position="top"
        ><div class="dialog-grid">
          <el-form-item label="推荐类型"
            ><el-select v-model="recommendationForm.recommendationType"
              ><el-option label="推荐商家" value="STORE" /><el-option
                label="推荐商品"
                value="PRODUCT" /><el-option label="热门商品" value="HOT_PRODUCT" /><el-option
                label="首页分类"
                value="CATEGORY" /></el-select></el-form-item
          ><el-form-item label="目标ID"
            ><el-input-number v-model="recommendationForm.targetId" :min="1" /></el-form-item
          ><el-form-item label="展示标题"
            ><el-input v-model="recommendationForm.titleOverride" maxlength="100" /></el-form-item
          ><el-form-item label="排序"
            ><el-input-number v-model="recommendationForm.sort" /></el-form-item
          ><el-form-item label="状态"
            ><el-switch
              v-model="recommendationForm.status"
              :active-value="1"
              :inactive-value="0" /></el-form-item
          ><el-form-item label="开始时间"
            ><el-date-picker
              v-model="recommendationForm.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="结束时间"
            ><el-date-picker
              v-model="recommendationForm.endTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
          /></el-form-item></div></el-form
      ><template #footer
        ><el-button @click="recommendationDialog = false">取消</el-button
        ><el-button type="primary" @click="submitRecommendation">保存</el-button></template
      ></el-dialog
    >
  </div>
</template>
<style scoped>
.operation-tabs {
  padding: 16px 20px 20px;
}
.banner-thumb {
  width: 112px;
  height: 58px;
  border-radius: 4px;
}
.image-error {
  display: grid;
  height: 100%;
  color: #999;
  background: #f2f4f6;
  place-items: center;
}
.dialog-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}
.dialog-grid .full {
  grid-column: 1/-1;
}
@media (max-width: 680px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }
  .dialog-grid .full {
    grid-column: auto;
  }
}
</style>
