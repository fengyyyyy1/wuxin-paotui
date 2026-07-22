export interface MerchantApplyRequest {
  merchantName: string; contactName: string; contactPhone: string; businessLicense?: string; idCardFront?: string; idCardBack?: string;
  storeName: string; storeLogo?: string; storeDescription?: string; storePhone: string; province?: string; city?: string; district?: string; detailAddress: string;
  latitude?: number; longitude?: number; openTime?: string; closeTime?: string;
}
export interface MerchantApplyResult { merchantId: number; storeId: number; auditStatus: number; auditStatusText: string; applyTime: string; }
export interface MerchantProfile {
  merchantId: number; merchantName: string; contactName: string; contactPhone: string; businessLicense: string | null; auditStatus: number; auditStatusText: string; auditRemark: string | null; rejectReason: string | null; auditTime: string | null; merchantStatus: number; merchantStatusText: string;
  storeId: number; storeName: string; storeLogo: string | null; storeDescription: string | null; storePhone: string; province: string | null; city: string | null; district: string | null; detailAddress: string; latitude: number | null; longitude: number | null; businessStatus: number; businessStatusText: string; openTime: string | null; closeTime: string | null; storeStatus: number; storeStatusText: string; createTime: string; updateTime: string;
}
export interface StoreUpdateRequest { storeName: string; storeLogo?: string; storeDescription?: string; storePhone: string; province?: string; city?: string; district?: string; detailAddress: string; latitude?: number; longitude?: number; openTime?: string; closeTime?: string; }
