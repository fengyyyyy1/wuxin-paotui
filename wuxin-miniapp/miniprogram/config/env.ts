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

export const USE_MOCK_WECHAT_LOGIN = IS_DEVELOPMENT_ENV;

export const MOCK_WECHAT_LOGIN_CODE = 'mock-code-new-user';

export const LOCAL_MOCK_WECHAT_LOGIN_STORAGE_KEY = 'WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_LOGIN';

export const USE_MOCK_WECHAT_PHONE_BIND = IS_DEVELOPMENT_ENV;

export const MOCK_WECHAT_PHONE_CODE = 'mock-phone-code-13800000003';

export const LOCAL_MOCK_WECHAT_PHONE_STORAGE_KEY = 'WUXIN_MINIAPP_DEV_USE_MOCK_WECHAT_PHONE';
