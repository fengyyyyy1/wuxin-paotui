import type { MerchantApplyRequest, MerchantApplyResponse } from '../types/merchant';
import { request } from '../utils/request';

export function applyMerchant(data: MerchantApplyRequest): Promise<MerchantApplyResponse> {
  return request({ url: '/api/merchant/apply', method: 'POST', data });
}
