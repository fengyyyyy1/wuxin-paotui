import { API_BASE_URL, REQUEST_TIMEOUT } from '../config/env';
import type { HttpMethod, Result } from '../types/common';
import { clearAuth, getToken, redirectToLogin } from './auth';

type Payload = string | WechatMiniprogram.IAnyObject | ArrayBuffer;
interface Options<T extends Payload> { url: string; method?: HttpMethod; data?: T; auth?: boolean; }

export class RequestError extends Error {
  readonly code: number;
  constructor(code: number, message: string) { super(message); this.name = 'RequestError'; this.code = code; }
}

let redirecting = false;

export function request<TResponse, TRequest extends Payload = WechatMiniprogram.IAnyObject>(options: Options<TRequest>): Promise<TResponse> {
  const token = getToken();
  const header: Record<string, string> = { 'content-type': 'application/json' };
  if (options.auth !== false && token) header.Authorization = `Bearer ${token}`;

  return new Promise((resolve, reject) => {
    wx.request<Result<TResponse>>({
      url: `${API_BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: REQUEST_TIMEOUT,
      success(response) {
        const result = response.data;
        if (!result || typeof result.code !== 'number') { reject(new RequestError(response.statusCode, '服务器响应异常')); return; }
        if (response.statusCode === 401 || result.code === 401) {
          clearAuth();
          if (!redirecting) { redirecting = true; redirectToLogin(); setTimeout(() => { redirecting = false; }, 800); }
          reject(new RequestError(401, result.message || '登录已过期'));
          return;
        }
        if (response.statusCode < 200 || response.statusCode >= 300 || result.code !== 200) {
          reject(new RequestError(result.code, result.message || '请求失败'));
          return;
        }
        resolve(result.data);
      },
      fail(error) {
        reject(new RequestError(0, error.errMsg.toLowerCase().includes('timeout') ? '请求超时，请稍后重试' : '网络异常，请稍后重试'));
      }
    });
  });
}

export function errorMessage(error: unknown): string { return error instanceof Error ? error.message : '操作失败，请稍后重试'; }
