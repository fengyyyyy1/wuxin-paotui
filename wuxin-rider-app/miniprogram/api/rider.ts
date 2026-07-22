import type { PageResult } from '../types/common';
import type { RiderApplyRequest, RiderApplyResult, RiderProfile, RiderRanking, RiderStatistics } from '../types/rider';
import type { RiderOrder, RiderOrderDetail } from '../types/order';
import { request } from '../utils/request';

export function applyRider(data: RiderApplyRequest): Promise<RiderApplyResult> { return request({ url: '/api/rider/apply', method: 'POST', data }); }
export function getRiderProfile(): Promise<RiderProfile> { return request({ url: '/api/rider/profile' }); }
export function getHall(pageNum: number, pageSize = 10): Promise<PageResult<RiderOrder>> { return request({ url: `/api/rider/order/hall?pageNum=${pageNum}&pageSize=${pageSize}` }); }
export function getMyOrders(pageNum: number, pageSize = 10, status?: number): Promise<PageResult<RiderOrder>> {
  const suffix = status == null ? '' : `&status=${status}`;
  return request({ url: `/api/rider/order/my?pageNum=${pageNum}&pageSize=${pageSize}${suffix}` });
}
export function getRiderOrderDetail(id: number): Promise<RiderOrderDetail> { return request({ url: `/api/rider/order/${id}` }); }
export function acceptOrder(id: number): Promise<unknown> { return request({ url: `/api/rider/order/accept/${id}`, method: 'POST' }); }
export function finishOrder(id: number): Promise<unknown> { return request({ url: `/api/rider/order/finish/${id}`, method: 'POST' }); }
export function giveUpOrder(id: number): Promise<unknown> { return request({ url: `/api/rider/order/give-up/${id}`, method: 'POST' }); }
export function getStatistics(riderId: number): Promise<RiderStatistics> { return request({ url: `/api/rider/${riderId}/statistics` }); }
export function getRanking(type: 'today' | 'week' | 'month' | 'total', limit = 50): Promise<RiderRanking[]> { return request({ url: `/api/rider/ranking?type=${type}&limit=${limit}` }); }
