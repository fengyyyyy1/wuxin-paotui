import type { LoginRequest, LoginResponse } from '@/types/auth'
import { request } from '@/utils/http'

export function login(data: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>({
    method: 'POST',
    url: '/user/login',
    data,
  })
}
