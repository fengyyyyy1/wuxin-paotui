import type { EnabledStatus, MerchantAuditStatus } from '@/types/merchant'

export type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'
export type MerchantOperationType = 'approve' | 'reject' | 'enable' | 'disable'

interface StatusMeta {
  text: string
  type: TagType
}

export interface MerchantOperationAction {
  type: MerchantOperationType
  label: string
  buttonType: TagType
}

export const auditStatusMap: Record<MerchantAuditStatus, StatusMeta> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '审核通过', type: 'success' },
  2: { text: '审核驳回', type: 'danger' },
}

export const enabledStatusMap: Record<EnabledStatus, StatusMeta> = {
  0: { text: '禁用', type: 'info' },
  1: { text: '启用', type: 'success' },
}

export const businessStatusMap: Record<EnabledStatus, StatusMeta> = {
  0: { text: '休息中', type: 'info' },
  1: { text: '营业中', type: 'success' },
}

export const storeStatusMap = enabledStatusMap

export function statusMeta<T extends number>(
  map: Record<T, StatusMeta>,
  value: T | null | undefined,
  fallbackText?: string | null,
): StatusMeta {
  if (value !== null && value !== undefined && value in map) {
    return map[value]
  }
  return {
    text: fallbackText || '未知状态',
    type: 'info',
  }
}

export function isPendingAuditStatus(value: MerchantAuditStatus): boolean {
  return value === 0
}

export function getMerchantOperationActions(
  auditStatus: MerchantAuditStatus,
  merchantStatus: EnabledStatus,
): MerchantOperationAction[] {
  if (auditStatus === 0) {
    return [
      { type: 'approve', label: '审核通过', buttonType: 'success' },
      { type: 'reject', label: '审核拒绝', buttonType: 'danger' },
    ]
  }

  if (auditStatus === 1 && merchantStatus === 1) {
    return [{ type: 'disable', label: '禁用商家', buttonType: 'warning' }]
  }

  if (auditStatus === 1 && merchantStatus === 0) {
    return [{ type: 'enable', label: '启用商家', buttonType: 'success' }]
  }

  return []
}
