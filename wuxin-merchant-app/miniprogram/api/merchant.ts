import type { MerchantApplyRequest, MerchantApplyResult, MerchantProfile, StoreUpdateRequest } from '../types/merchant';
import { request } from '../utils/request';
export const applyMerchant = (data: MerchantApplyRequest): Promise<MerchantApplyResult> => request({ url: '/api/merchant/apply', method: 'POST', data });
export const getMerchantProfile = (): Promise<MerchantProfile> => request({ url: '/api/merchant/me' });
export const updateStore = (data: StoreUpdateRequest): Promise<void> => request({ url: '/api/merchant/store', method: 'PUT', data });
export const updateBusinessStatus = (businessStatus: number): Promise<void> => request({ url: '/api/merchant/store/business-status', method: 'PUT', data: { businessStatus } });
