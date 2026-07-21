import {
  bindWechatPhone,
  wechatLogin,
  getUserProfile
} from '../api/index';
import { ROUTES } from '../constants/routes';
import type { UserInfo } from '../types/user';
import {
  clearAuth,
  getStoredNewUser,
  getStoredUserInfo,
  getToken,
  persistAuth
} from '../utils/auth';
import { clearCartSummary } from './cart';
import { getWechatLoginCode } from '../utils/wechat-login-mode';
import { getMockWechatPhoneCode, isMockWechatPhoneBindEnabled } from '../utils/wechat-phone-mode';

export interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  newUser: boolean | null;
  isLoggedIn: boolean;
}

const authState: AuthState = {
  token: null,
  userInfo: null,
  newUser: null,
  isLoggedIn: false
};

function syncAppGlobalData(): void {
  try {
    const app = getApp<IAppOption>();
    if (!app || !app.globalData) {
      return;
    }
    app.globalData.token = authState.token;
    app.globalData.userInfo = authState.userInfo;
    app.globalData.newUser = authState.newUser;
  } catch {
    // App.onLaunch may run before getApp() can safely return the instance.
  }
}

export function getAuthState(): AuthState {
  return { ...authState };
}

export function restoreSession(): AuthState {
  authState.token = getToken();
  authState.userInfo = getStoredUserInfo();
  authState.newUser = getStoredNewUser();
  authState.isLoggedIn = Boolean(authState.token && authState.userInfo);

  syncAppGlobalData();

  return getAuthState();
}

export async function login(): Promise<AuthState> {
  const code = await getWechatLoginCode();
  const response = await wechatLogin({ code });
  persistAuth(response.token, response.userInfo, response.newUser);
  return restoreSession();
}

export async function verifySession(): Promise<boolean> {
  restoreSession();
  if (!authState.token) {
    return false;
  }

  try {
    await refreshProfile();
    return true;
  } catch {
    return false;
  }
}

export async function refreshProfile(): Promise<UserInfo> {
  restoreSession();
  if (!authState.token) {
    throw new Error('未登录或登录已过期');
  }

  const userInfo = await getUserProfile();
  persistAuth(authState.token, userInfo, authState.newUser);
  restoreSession();
  return userInfo;
}

export async function bindPhoneWithCode(code: string): Promise<UserInfo> {
  restoreSession();
  if (!authState.token) {
    throw new Error('未登录或登录已过期');
  }

  const userInfo = await bindWechatPhone({ code });
  persistAuth(authState.token, userInfo, authState.newUser);
  restoreSession();
  return userInfo;
}

export async function bindMockPhone(): Promise<UserInfo> {
  if (!isMockWechatPhoneBindEnabled()) {
    throw new Error('当前未启用本地Mock手机号绑定');
  }
  return bindPhoneWithCode(getMockWechatPhoneCode());
}

export function clearSession(): void {
  clearAuth();
  clearCartSummary();
  restoreSession();
}

export function logout(): void {
  clearSession();
  wx.redirectTo({ url: ROUTES.login });
}
