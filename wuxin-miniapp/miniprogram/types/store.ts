export interface StoreListItem {
  storeId: number;
  merchantId: number;
  storeName: string;
  storeLogo: string | null;
  storeDescription: string | null;
  storePhone: string | null;
  district: string | null;
  detailAddress: string | null;
  businessStatus: 0 | 1;
  businessStatusText: string | null;
  openTime: string | null;
  closeTime: string | null;
}

export interface StoreDetail extends StoreListItem {
  merchantName: string | null;
  province: string | null;
  city: string | null;
  latitude: number | null;
  longitude: number | null;
}

export interface StoreListQuery {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  district?: string;
  businessStatus?: 0 | 1;
}
