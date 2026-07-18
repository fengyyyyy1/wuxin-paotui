import type { PageResult } from '@/types/api'
import type {
  AdminMerchantDetail,
  AdminMerchantOperation,
  AdminMerchantPageQuery,
  AdminMerchantSummary,
  ApproveMerchantRequest,
  MerchantStatusOperationRequest,
  RejectMerchantRequest,
} from '@/types/merchant'
import { request } from '@/utils/http'

const baseUrl = '/admin/merchant'

export function getAdminMerchantPage(
  params: AdminMerchantPageQuery,
): Promise<PageResult<AdminMerchantSummary>> {
  return request<PageResult<AdminMerchantSummary>>({
    method: 'GET',
    url: `${baseUrl}/page`,
    params,
  })
}

export function getAdminMerchantDetail(merchantId: number): Promise<AdminMerchantDetail> {
  return request<AdminMerchantDetail>({
    method: 'GET',
    url: `${baseUrl}/${merchantId}`,
  })
}

export function approveMerchant(
  merchantId: number,
  data: ApproveMerchantRequest,
): Promise<AdminMerchantOperation> {
  return request<AdminMerchantOperation>({
    method: 'POST',
    url: `${baseUrl}/${merchantId}/approve`,
    data,
  })
}

export function rejectMerchant(
  merchantId: number,
  data: RejectMerchantRequest,
): Promise<AdminMerchantOperation> {
  return request<AdminMerchantOperation>({
    method: 'POST',
    url: `${baseUrl}/${merchantId}/reject`,
    data,
  })
}

export function enableMerchant(merchantId: number): Promise<AdminMerchantOperation> {
  return request<AdminMerchantOperation>({
    method: 'POST',
    url: `${baseUrl}/${merchantId}/enable`,
  })
}

export function disableMerchant(
  merchantId: number,
  data: MerchantStatusOperationRequest,
): Promise<AdminMerchantOperation> {
  return request<AdminMerchantOperation>({
    method: 'POST',
    url: `${baseUrl}/${merchantId}/disable`,
    data,
  })
}
