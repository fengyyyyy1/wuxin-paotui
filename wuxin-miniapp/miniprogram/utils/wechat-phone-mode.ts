import {
  LOCAL_MOCK_WECHAT_PHONE_STORAGE_KEY,
  MOCK_WECHAT_PHONE_CODE,
  USE_MOCK_WECHAT_PHONE_BIND
} from '../config/env';

function isReleaseMiniProgram(): boolean {
  try {
    return wx.getAccountInfoSync().miniProgram.envVersion === 'release';
  } catch {
    return false;
  }
}

export function isMockWechatPhoneBindEnabled(): boolean {
  if (isReleaseMiniProgram()) {
    return false;
  }
  return USE_MOCK_WECHAT_PHONE_BIND || wx.getStorageSync(LOCAL_MOCK_WECHAT_PHONE_STORAGE_KEY) === true;
}

export function getMockWechatPhoneCode(): string {
  return MOCK_WECHAT_PHONE_CODE;
}
