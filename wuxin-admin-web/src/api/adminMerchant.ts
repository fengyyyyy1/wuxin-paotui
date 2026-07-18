import type { PageResult } from '@/types/api'
import type {
  AdminMerchantOperation,
  AdminMerchantDetailVO,
  AdminMerchantPageQuery,
  AdminMerchantPageVO,
  ApproveMerchantRequest,
  MerchantStatusOperationRequest,
  RejectMerchantRequest,
} from '@/types/merchant'
import { request } from '@/utils/http'

const baseUrl = '/admin/merchant'

export function verifyAdminMerchantPermission(
  skipErrorMessage = false,
): Promise<PageResult<AdminMerchantPageVO>> {
  return request<PageResult<AdminMerchantPageVO>>({
    method: 'GET',
    url: `${baseUrl}/page`,
    params: {
      pageNum: 1,
      pageSize: 1,
    },
    skipErrorMessage,
  })
}

export function getMerchantPage(
  params: AdminMerchantPageQuery,
): Promise<PageResult<AdminMerchantPageVO>> {
  return request<PageResult<AdminMerchantPageVO>>({
    method: 'GET',
    url: `${baseUrl}/page`,
    params,
  })
}

export function getMerchantDetail(merchantId: number): Promise<AdminMerchantDetailVO> {
  return request<AdminMerchantDetailVO>({
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
