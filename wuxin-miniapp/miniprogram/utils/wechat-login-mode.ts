import {
  LOCAL_MOCK_WECHAT_LOGIN_STORAGE_KEY,
  MOCK_WECHAT_LOGIN_CODE,
  USE_MOCK_WECHAT_LOGIN
} from '../config/env';
import { RequestError } from './request';

function isReleaseMiniProgram(): boolean {
  try {
    return wx.getAccountInfoSync().miniProgram.envVersion === 'release';
  } catch {
    return false;
  }
}

export function isMockWechatLoginEnabled(): boolean {
  if (isReleaseMiniProgram()) {
    return false;
  }
  return USE_MOCK_WECHAT_LOGIN || wx.getStorageSync(LOCAL_MOCK_WECHAT_LOGIN_STORAGE_KEY) === true;
}

export async function getWechatLoginCode(): Promise<string> {
  if (isMockWechatLoginEnabled()) {
    return MOCK_WECHAT_LOGIN_CODE;
  }

  try {
    const result = await wx.login();
    if (!result.code) {
      throw new RequestError(400, '微信登录凭证获取失败');
    }
    return result.code;
  } catch (error) {
    if (error instanceof RequestError) {
      throw error;
    }
    throw new RequestError(0, '微信登录失败，请稍后重试');
  }
}
