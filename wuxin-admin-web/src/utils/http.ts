import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
} from 'axios'
import { ElMessage } from 'element-plus'

import type { Result } from '@/types/api'
import { clearAuthStorage, getToken } from '@/utils/storage'

export class ApiError extends Error {
  readonly code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

function redirectToLogin(): void {
  clearAuthStorage()
  if (window.location.pathname === '/login') {
    return
  }

  const redirect = `${window.location.pathname}${window.location.search}`
  window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
}

function handleApiError(code: number, message: string): never {
  if (code === 401) {
    redirectToLogin()
    throw new ApiError(code, message || '未登录或登录已过期')
  }

  if (code === 403) {
    ElMessage.error('无管理员权限')
    throw new ApiError(code, message || '无管理员权限')
  }

  ElMessage.error(message || '请求失败')
  throw new ApiError(code, message || '请求失败')
}

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => response,
  (error: AxiosError<Result<unknown>>) => {
    const status = error.response?.status
    const result = error.response?.data

    if (status === 401 || result?.code === 401) {
      return handleApiError(401, result?.message ?? '未登录或登录已过期')
    }
    if (status === 403 || result?.code === 403) {
      return handleApiError(403, result?.message ?? '无管理员权限')
    }

    const message = result?.message ?? (error.response ? '请求失败' : '网络连接异常')
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<Result<T>>(config)
  const result = response.data

  if (result.code !== 200) {
    return handleApiError(result.code, result.message)
  }
  if (result.data === null) {
    return undefined as T
  }
  return result.data
}

export default http
