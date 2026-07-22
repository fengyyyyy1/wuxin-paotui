export type ApiEnvironment = 'dev' | 'test' | 'prod';

export const API_BASE_URLS: Record<ApiEnvironment, string> = {
  dev: 'http://localhost:8080',
  test: 'https://test-api.待配置域名',
  prod: 'https://api.待配置域名'
};

function detectApiEnvironment(): ApiEnvironment {
  try {
    const envVersion = wx.getAccountInfoSync().miniProgram.envVersion;
    if (envVersion === 'release') {
      return 'prod';
    }
    if (envVersion === 'trial') {
      return 'test';
    }
  } catch {
    return 'dev';
  }
  return 'dev';
}

export const API_ENV: ApiEnvironment = detectApiEnvironment();

export const API_BASE_URL = API_BASE_URLS[API_ENV];

export const IS_DEVELOPMENT_ENV = API_ENV === 'dev';

export const REQUEST_TIMEOUT = 15000;
export const MOCK_WECHAT_LOGIN_STORAGE_KEY = 'WUXIN_MERCHANT_DEV_MOCK_LOGIN';
