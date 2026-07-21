export interface Address {
  id: number;
  userId?: number;
  receiverName: string;
  receiverPhone: string;
  province: string | null;
  city: string | null;
  district: string | null;
  detailAddress: string;
  latitude: number | null;
  longitude: number | null;
  isDefault: 0 | 1;
  createTime?: string;
  updateTime?: string;
  isDeleted?: 0 | 1;
}

export interface AddressRequest {
  receiverName: string;
  receiverPhone: string;
  province?: string | null;
  city?: string | null;
  district?: string | null;
  detailAddress: string;
  latitude?: number | null;
  longitude?: number | null;
  isDefault?: 0 | 1;
}
