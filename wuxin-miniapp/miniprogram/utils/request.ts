import { API_BASE_URL, REQUEST_TIMEOUT } from '../config/env';
import type { HttpMethod, Result } from '../types/common';
import { clearAuth, getToken, redirectToLogin } from './auth';

type RequestPayload = string | WechatMiniprogram.IAnyObject | ArrayBuffer;

interface RequestOptions<TRequest extends RequestPayload> {
  url: string;
  method?: HttpMethod;
  data?: TRequest;
  auth?: boolean;
}

export class RequestError extends Error {
  readonly code: number;

  constructor(code: number, message: string) {
    super(message);
    this.name = 'RequestError';
    this.code = code;
  }
}

let handlingUnauthorized = false;

export function request<TResponse, TRequest extends RequestPayload = WechatMiniprogram.IAnyObject>(
  options: RequestOptions<TRequest>
): Promise<TResponse> {
  const token = getToken();
  const needAuth = options.auth !== false;
  const header: Record<string, string> = {
    'content-type': 'application/json'
  };

  if (needAuth && token) {
    header.Authorization = `Bearer ${token}`;
  }

  return new Promise<TResponse>((resolve, reject) => {
    wx.request<Result<TResponse>>({
      url: `${API_BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: REQUEST_TIMEOUT,
      success(response) {
        const result = response.data;
        if (!result || typeof result.code !== 'number') {
          reject(new RequestError(response.statusCode, '服务器响应异常'));
          return;
        }

        if (response.statusCode === 401 || result.code === 401) {
          clearAuth();
          if (!handlingUnauthorized) {
            handlingUnauthorized = true;
            redirectToLogin();
            setTimeout(() => {
              handlingUnauthorized = false;
            }, 800);
          }
          reject(new RequestError(401, result.message || '未登录或登录已过期'));
          return;
        }

        if (response.statusCode === 403 || result.code === 403) {
          reject(new RequestError(403, result.message || '无访问权限'));
          return;
        }

        if (response.statusCode < 200 || response.statusCode >= 300 || result.code !== 200) {
          reject(new RequestError(result.code, result.message || '请求失败'));
          return;
        }

        resolve(result.data);
      },
      fail(error) {
        const detail = error.errMsg || '';
        const message = detail.toLowerCase().includes('timeout')
          ? '请求超时，请检查网络后重试'
          : '网络异常，请稍后重试';
        reject(new RequestError(0, message));
      }
    });
  });
}
