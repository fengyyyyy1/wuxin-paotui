<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import {
  approveMerchant,
  disableMerchant,
  enableMerchant,
  getMerchantDetail,
  rejectMerchant,
} from '@/api/adminMerchant'
import PageHeader from '@/components/PageHeader.vue'
import type { AdminMerchantDetailVO } from '@/types/merchant'
import {
  auditStatusMap,
  businessStatusMap,
  enabledStatusMap,
  getMerchantOperationActions,
  type MerchantOperationType,
  statusMeta,
} from '@/utils/merchantStatus'

const route = useRoute()
const router = useRouter()
const detail = ref<AdminMerchantDetailVO | null>(null)
const loading = ref(false)
const errorMessage = ref('')
let requestSeq = 0
let active = true

interface OperationConfig {
  title: string
  description: string
  confirmText: string
  successMessage: string
  inputLabel?: string
  placeholder?: string
  minLength?: number
  maxLength?: number
  requiresInput: boolean
}

const operationDialogVisible = ref(false)
const operationType = ref<MerchantOperationType | null>(null)
const operationText = ref('')
const operationError = ref('')
const operationSubmitting = ref(false)

const operationConfigMap: Record<MerchantOperationType, OperationConfig> = {
  approve: {
    title: '审核通过',
    description: '确认该商家资料审核通过后，系统将以后端事务结果为准刷新详情。',
    confirmText: '确认通过',
    successMessage: '商家审核通过',
    inputLabel: '审核备注',
    placeholder: '请输入审核备注，如：审核通过，资料完整',
    maxLength: 255,
    requiresInput: true,
  },
  reject: {
    title: '审核拒绝',
    description: '拒绝后商家不会被启用，请填写明确原因，便于后续处理。',
    confirmText: '确认拒绝',
    successMessage: '商家审核拒绝',
    inputLabel: '拒绝原因',
    placeholder: '请输入拒绝原因，如：营业执照信息不清晰，请重新上传',
    minLength: 2,
    maxLength: 255,
    requiresInput: true,
  },
  enable: {
    title: '启用商家',
    description: '确认启用该商家？启用接口按后端真实定义提交，不修改审核结论。',
    confirmText: '确认启用',
    successMessage: '商家启用成功',
    requiresInput: false,
  },
  disable: {
    title: '禁用商家',
    description: '禁用后店铺将按后端规则同步禁用或停止营业，请填写操作原因。',
    confirmText: '确认禁用',
    successMessage: '商家禁用成功',
    inputLabel: '禁用原因',
    placeholder: '请输入禁用原因，如：违规处理',
    minLength: 2,
    maxLength: 255,
    requiresInput: true,
  },
}

function parseMerchantId(raw: string | string[] | undefined): number | null {
  const value = Number(Array.isArray(raw) ? raw[0] : raw)
  return Number.isInteger(value) && value > 0 ? value : null
}

const merchantId = computed(() => {
  return parseMerchantId(route.params.merchantId)
})

const availableOperations = computed(() => {
  if (!detail.value) {
    return []
  }

  return getMerchantOperationActions(detail.value.auditStatus, detail.value.merchantStatus)
})

const currentOperationConfig = computed(() => {
  return operationType.value ? operationConfigMap[operationType.value] : null
})

function displayValue(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return String(value)
}

function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ')
}

function formatBusinessHours(openTime: string | null, closeTime: string | null): string {
  if (!openTime && !closeTime) {
    return '-'
  }
  return `${openTime || '-'} 至 ${closeTime || '-'}`
}

function qualificationText(value: string | null): string {
  return value ? '查看资料' : '-'
}

async function loadDetail(targetMerchantId = merchantId.value): Promise<void> {
  if (loading.value) {
    return
  }

  if (!targetMerchantId) {
    errorMessage.value = '商家ID不合法'
    detail.value = null
    return
  }

  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await getMerchantDetail(targetMerchantId)
    if (!active || seq !== requestSeq) {
      return
    }
    detail.value = result
  } catch (error) {
    if (!active || seq !== requestSeq) {
      return
    }
    detail.value = null
    errorMessage.value =
      error instanceof Error ? error.message || '商家详情加载失败' : '商家详情加载失败'
  } finally {
    if (active && seq === requestSeq) {
      loading.value = false
    }
  }
}

function goBack(): void {
  void router.push({ name: 'merchant-list' })
}

function goOrders(): void {
  if (detail.value) void router.push({ path: '/orders', query: { merchantId: detail.value.merchantId } })
}

function goProducts(): void {
  if (detail.value?.storeId) void router.push({ path: '/products', query: { storeId: detail.value.storeId } })
}

function refreshDetail(): void {
  void loadDetail()
}

function openOperationDialog(type: MerchantOperationType): void {
  if (operationSubmitting.value || !detail.value) {
    return
  }
  operationType.value = type
  operationText.value = ''
  operationError.value = ''
  operationDialogVisible.value = true
}

function closeOperationDialog(): void {
  if (!operationSubmitting.value) {
    operationDialogVisible.value = false
  }
}

function resetOperationDialog(): void {
  if (operationSubmitting.value) {
    return
  }
  operationType.value = null
  operationText.value = ''
  operationError.value = ''
}

function validateOperationInput(config: OperationConfig): string | null {
  if (!config.requiresInput) {
    return null
  }

  const value = operationText.value.trim()
  if (!value) {
    return `${config.inputLabel || '操作原因'}不能为空`
  }

  if (config.minLength !== undefined && value.length < config.minLength) {
    return `${config.inputLabel || '操作原因'}长度不能少于${config.minLength}个字符`
  }

  if (config.maxLength !== undefined && value.length > config.maxLength) {
    return `${config.inputLabel || '操作原因'}长度不能超过${config.maxLength}个字符`
  }

  return null
}

async function submitOperation(): Promise<void> {
  if (operationSubmitting.value || !detail.value || !merchantId.value || !operationType.value) {
    return
  }

  const type = operationType.value
  const config = operationConfigMap[type]
  const validationMessage = validateOperationInput(config)
  if (validationMessage) {
    operationError.value = validationMessage
    return
  }

  const value = operationText.value.trim()
  operationSubmitting.value = true
  operationError.value = ''

  try {
    if (type === 'approve') {
      await approveMerchant(merchantId.value, { auditRemark: value })
    } else if (type === 'reject') {
      await rejectMerchant(merchantId.value, { reason: value })
    } else if (type === 'enable') {
      await enableMerchant(merchantId.value)
    } else {
      await disableMerchant(merchantId.value, { reason: value })
    }

    ElMessage.success(config.successMessage)
    operationDialogVisible.value = false
    await loadDetail()
  } catch (error) {
    operationError.value = error instanceof Error ? error.message : `${config.title}失败`
    ElMessage.error(operationError.value)
  } finally {
    operationSubmitting.value = false
  }
}

watch(
  () => route.params.merchantId,
  (rawMerchantId) => {
    if (rawMerchantId === undefined || rawMerchantId === '') {
      return
    }
    void loadDetail(parseMerchantId(rawMerchantId))
  },
  {
    immediate: true,
  },
)

onUnmounted(() => {
  active = false
  requestSeq += 1
})
</script>

<template>
  <div class="page-shell">
    <PageHeader title="商家详情" subtitle="查看商家申请、审核、店铺和资质资料">
      <template #actions>
        <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="refreshDetail">刷新</el-button>
      </template>
    </PageHeader>

    <el-alert
      v-if="errorMessage"
      class="detail-alert"
      type="error"
      :title="errorMessage"
      show-icon
      :closable="false"
    >
      <template #default>
        <el-button link type="primary" :disabled="loading" @click="refreshDetail">重新加载</el-button>
      </template>
    </el-alert>

    <section v-loading="loading" class="detail-panel">
      <el-empty v-if="!loading && !detail" description="商家详情不存在或加载失败" :image-size="96" />

      <template v-else-if="detail">
        <div class="operation-block">
          <div>
            <h2>审核与状态操作</h2>
            <p>操作成功后自动重新读取商家详情，页面不本地伪造成功状态。</p>
          </div>
          <div class="operation-actions">
            <template v-if="availableOperations.length">
              <el-button
                v-for="action in availableOperations"
                :key="action.type"
                :type="action.buttonType"
                :disabled="loading || operationSubmitting"
                @click="openOperationDialog(action.type)"
              >
                {{ action.label }}
              </el-button>
            </template>
            <el-tag v-else type="info">当前状态暂无可执行操作</el-tag>
          </div>
        </div>

        <div class="section-block">
          <div class="section-heading">
            <h2>商家基础信息</h2>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="商家ID">
              {{ detail.merchantId }}
            </el-descriptions-item>
            <el-descriptions-item label="商家名称">
              {{ displayValue(detail.merchantName) }}
            </el-descriptions-item>
            <el-descriptions-item label="商家状态">
              <el-tag
                :type="
                  statusMeta(enabledStatusMap, detail.merchantStatus, detail.merchantStatusText).type
                "
              >
                {{
                  statusMeta(enabledStatusMap, detail.merchantStatus, detail.merchantStatusText).text
                }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="联系人">
              {{ displayValue(detail.contactName) }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ displayValue(detail.contactPhone) }}
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatDateTime(detail.applyTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="申请用户ID">
              {{ detail.userId }}
            </el-descriptions-item>
            <el-descriptions-item label="用户账号">
              {{ displayValue(detail.username) }}
            </el-descriptions-item>
            <el-descriptions-item label="用户昵称">
              {{ displayValue(detail.nickname) }}
            </el-descriptions-item>
            <el-descriptions-item label="用户手机号">
              {{ displayValue(detail.userPhone) }}
            </el-descriptions-item>
            <el-descriptions-item label="用户状态">
              {{ displayValue(detail.userStatus) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(detail.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-block">
          <div class="section-heading">
            <h2>审核信息</h2>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="审核状态">
              <el-tag
                :type="statusMeta(auditStatusMap, detail.auditStatus, detail.auditStatusText).type"
              >
                {{ statusMeta(auditStatusMap, detail.auditStatus, detail.auditStatusText).text }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核管理员ID">
              {{ displayValue(detail.auditAdminId) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核管理员">
              {{ displayValue(detail.auditAdminUsername) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatDateTime(detail.auditTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核备注" :span="2">
              {{ displayValue(detail.auditRemark) }}
            </el-descriptions-item>
            <el-descriptions-item label="拒绝原因" :span="3">
              {{ displayValue(detail.rejectReason) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-block">
          <div class="section-heading">
            <h2>经营概览</h2>
            <div class="operation-actions">
              <el-button link type="primary" @click="goOrders">查看商家订单</el-button>
              <el-button link type="primary" :disabled="!detail.storeId" @click="goProducts">查看门店商品</el-button>
            </div>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="订单数量">{{ detail.orderCount }}</el-descriptions-item>
            <el-descriptions-item label="商品数量">{{ detail.productCount }}</el-descriptions-item>
            <el-descriptions-item label="已支付营业额">¥{{ Number(detail.revenueAmount || 0).toFixed(2) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-block">
          <div class="section-heading">
            <h2>店铺信息</h2>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="店铺ID">
              {{ displayValue(detail.storeId) }}
            </el-descriptions-item>
            <el-descriptions-item label="店铺名称">
              {{ displayValue(detail.storeName) }}
            </el-descriptions-item>
            <el-descriptions-item label="店铺状态">
              <el-tag :type="statusMeta(enabledStatusMap, detail.storeStatus, detail.storeStatusText).type">
                {{ statusMeta(enabledStatusMap, detail.storeStatus, detail.storeStatusText).text }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="店铺电话">
              {{ displayValue(detail.storePhone) }}
            </el-descriptions-item>
            <el-descriptions-item label="所在地区" :span="2">
              {{
                [detail.province, detail.city, detail.district].filter(Boolean).join(' / ') || '-'
              }}
            </el-descriptions-item>
            <el-descriptions-item label="详细地址" :span="3">
              {{ displayValue(detail.detailAddress) }}
            </el-descriptions-item>
            <el-descriptions-item label="经纬度" :span="3">
              {{ displayValue(detail.longitude) }}，{{ displayValue(detail.latitude) }}
            </el-descriptions-item>
            <el-descriptions-item label="店铺简介" :span="3">
              {{ displayValue(detail.storeDescription) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-block">
          <div class="section-heading">
            <h2>营业信息</h2>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="营业状态">
              <el-tag
                :type="
                  statusMeta(businessStatusMap, detail.businessStatus, detail.businessStatusText)
                    .type
                "
              >
                {{
                  statusMeta(businessStatusMap, detail.businessStatus, detail.businessStatusText)
                    .text
                }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="营业时间" :span="2">
              {{ formatBusinessHours(detail.openTime, detail.closeTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-block">
          <div class="section-heading">
            <h2>资质信息</h2>
          </div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="营业执照">
              <el-link
                v-if="detail.businessLicense"
                :href="detail.businessLicense"
                target="_blank"
                type="primary"
              >
                {{ qualificationText(detail.businessLicense) }}
              </el-link>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="身份证正面">
              <el-link v-if="detail.idCardFront" :href="detail.idCardFront" target="_blank" type="primary">
                {{ qualificationText(detail.idCardFront) }}
              </el-link>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="身份证反面">
              <el-link v-if="detail.idCardBack" :href="detail.idCardBack" target="_blank" type="primary">
                {{ qualificationText(detail.idCardBack) }}
              </el-link>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="店铺Logo" :span="3">
              <el-link v-if="detail.storeLogo" :href="detail.storeLogo" target="_blank" type="primary">
                查看Logo
              </el-link>
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </section>

    <el-dialog
      v-model="operationDialogVisible"
      :title="currentOperationConfig?.title || '商家操作'"
      width="480px"
      :close-on-click-modal="!operationSubmitting"
      :close-on-press-escape="!operationSubmitting"
      :show-close="!operationSubmitting"
      @closed="resetOperationDialog"
    >
      <template v-if="currentOperationConfig">
        <p class="operation-description">{{ currentOperationConfig.description }}</p>
        <el-form label-position="top" @submit.prevent="submitOperation">
          <el-form-item
            v-if="currentOperationConfig.requiresInput"
            :label="currentOperationConfig.inputLabel"
            :error="operationError"
          >
            <el-input
              v-model="operationText"
              type="textarea"
              :rows="4"
              :maxlength="currentOperationConfig.maxLength"
              show-word-limit
              :placeholder="currentOperationConfig.placeholder"
              :disabled="operationSubmitting"
              @input="operationError = ''"
            />
          </el-form-item>
          <el-alert
            v-else-if="operationError"
            type="error"
            :title="operationError"
            show-icon
            :closable="false"
          />
        </el-form>
      </template>
      <template #footer>
        <el-button :disabled="operationSubmitting" @click="closeOperationDialog">取消</el-button>
        <el-button type="primary" :loading="operationSubmitting" @click="submitOperation">
          {{ currentOperationConfig?.confirmText || '确认' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-alert {
  margin-top: 16px;
}

.detail-panel {
  min-height: 420px;
  margin-top: 24px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 8px;
}

.operation-block {
  display: flex;
  gap: 16px;
  padding-bottom: 22px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e6ebf2;
  align-items: center;
  justify-content: space-between;
}

.operation-block h2 {
  margin: 0;
  color: #172033;
  font-size: 18px;
  letter-spacing: 0;
}

.operation-block p {
  margin: 8px 0 0;
  color: #697386;
  font-size: 13px;
}

.operation-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.section-block + .section-block {
  margin-top: 26px;
}

.section-heading {
  display: flex;
  margin-bottom: 14px;
  align-items: center;
  justify-content: space-between;
}

.section-heading h2 {
  margin: 0;
  color: #172033;
  font-size: 18px;
  letter-spacing: 0;
}

.detail-panel :deep(.el-descriptions__label) {
  width: 128px;
  color: #697386;
  font-weight: 600;
}

.detail-panel :deep(.el-descriptions__content) {
  word-break: break-word;
}

.operation-description {
  margin: 0 0 16px;
  color: #4d5a6d;
  line-height: 1.7;
}

@media (max-width: 880px) {
  .detail-panel {
    padding: 18px;
  }

  .detail-panel :deep(.el-descriptions__table) {
    min-width: 760px;
  }

  .section-block {
    overflow-x: auto;
  }

  .operation-block {
    align-items: flex-start;
    flex-direction: column;
  }

  .operation-actions {
    justify-content: flex-start;
  }
}
</style>
