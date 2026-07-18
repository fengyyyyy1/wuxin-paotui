export type MerchantAuditStatus = 0 | 1 | 2
export type EnabledStatus = 0 | 1

export interface AdminMerchantPageQuery {
  pageNum?: number
  pageSize?: number
  auditStatus?: MerchantAuditStatus
  merchantStatus?: EnabledStatus
  keyword?: string
}

export interface AdminMerchantSummary {
  merchantId: number
  userId: number
  merchantName: string
  contactName: string
  contactPhone: string
  auditStatus: MerchantAuditStatus
  auditStatusText: string
  merchantStatus: EnabledStatus
  merchantStatusText: string
  storeId: number
  storeName: string
  storeStatus: EnabledStatus
  storeStatusText: string
  businessStatus: EnabledStatus
  businessStatusText: string
  applyTime: string
  auditTime: string | null
}

export interface AdminMerchantDetail extends AdminMerchantSummary {
  username: string
  nickname: string | null
  avatar: string | null
  userPhone: string | null
  userStatus: number
  businessLicense: string | null
  idCardFront: string | null
  idCardBack: string | null
  auditAdminId: number | null
  auditAdminUsername: string | null
  auditRemark: string | null
  rejectReason: string | null
  storeLogo: string | null
  storeDescription: string | null
  storePhone: string | null
  province: string | null
  city: string | null
  district: string | null
  detailAddress: string | null
  latitude: number | null
  longitude: number | null
  openTime: string | null
  closeTime: string | null
  updateTime: string
}

export interface ApproveMerchantRequest {
  auditRemark: string
}

export interface RejectMerchantRequest {
  reason: string
}

export interface MerchantStatusOperationRequest {
  reason: string
}

export interface AdminMerchantOperation {
  merchantId: number
  auditStatus: MerchantAuditStatus
  auditStatusText: string
  merchantStatus: EnabledStatus
  merchantStatusText: string
  storeStatus: EnabledStatus
  storeStatusText: string
  businessStatus: EnabledStatus
  businessStatusText: string
  auditAdminId: number | null
  auditTime: string | null
  auditRemark: string | null
  rejectReason: string | null
  operationTime: string
}
