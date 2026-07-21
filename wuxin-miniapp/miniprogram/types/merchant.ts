export interface MerchantApplyRequest {
  merchantName: string;
  contactName: string;
  contactPhone: string;
  businessLicense?: string | null;
  idCardFront?: string | null;
  idCardBack?: string | null;
  storeName: string;
  storeLogo?: string | null;
  storeDescription?: string | null;
  storePhone: string;
  province?: string | null;
  city?: string | null;
  district?: string | null;
  detailAddress: string;
  latitude?: number | null;
  longitude?: number | null;
  openTime?: string | null;
  closeTime?: string | null;
}

export interface MerchantApplyResponse {
  merchantId: number;
  storeId: number;
  auditStatus: 0 | 1 | 2;
  auditStatusText: string;
  applyTime: string;
}
