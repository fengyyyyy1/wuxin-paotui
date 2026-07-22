import type { PageResult } from '../types/common';
import type { MerchantOrder, MerchantOrderDetail } from '../types/order';
import { request } from '../utils/request';
export interface OrderQuery { pageNum?: number; pageSize?: number; status?: number; keyword?: string; startTime?: string; endTime?: string; }
function queryString(query: OrderQuery): string { return Object.entries(query).filter(([, value]) => value !== undefined && value !== '').map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`).join('&'); }
export const getMerchantOrders = (query: OrderQuery): Promise<PageResult<MerchantOrder>> => request({ url: `/api/merchant/order/page?${queryString(query)}` });
export const getMerchantOrderDetail = (id: number): Promise<MerchantOrderDetail> => request({ url: `/api/merchant/order/${id}` });
export const acceptMerchantOrder = (id: number): Promise<unknown> => request({ url: `/api/merchant/order/${id}/accept`, method: 'POST' });
export const rejectMerchantOrder = (id: number, reason: string): Promise<unknown> => request({ url: `/api/merchant/order/${id}/reject`, method: 'POST', data: { reason } });
export const readyMerchantOrder = (id: number): Promise<unknown> => request({ url: `/api/merchant/order/${id}/ready`, method: 'POST' });
