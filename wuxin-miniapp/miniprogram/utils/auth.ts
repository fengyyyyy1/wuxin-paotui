import { ROUTES } from '../constants/routes';
import { STORAGE_KEYS } from '../constants/storage';
import type { UserInfo } from '../types/user';

function getRuntimeApp(): IAppOption | null {
  try {
    const app = getApp<IAppOption>();
    return app && app.globalData ? app : null;
  } catch {
    return null;
  }
}

export function getToken(): string | null {
  return wx.getStorageSync(STORAGE_KEYS.token) || null;
}

export function setToken(token: string): void {
  wx.setStorageSync(STORAGE_KEYS.token, token);
}

export function getStoredUserInfo(): UserInfo | null {
  return wx.getStorageSync(STORAGE_KEYS.userInfo) || null;
}

export function setStoredUserInfo(userInfo: UserInfo): void {
  wx.setStorageSync(STORAGE_KEYS.userInfo, userInfo);
}

export function getStoredNewUser(): boolean | null {
  const value = wx.getStorageSync(STORAGE_KEYS.newUser);
  return typeof value === 'boolean' ? value : null;
}

export function setStoredNewUser(newUser: boolean): void {
  wx.setStorageSync(STORAGE_KEYS.newUser, newUser);
}

export function clearAuth(): void {
  wx.removeStorageSync(STORAGE_KEYS.token);
  wx.removeStorageSync(STORAGE_KEYS.userInfo);
  wx.removeStorageSync(STORAGE_KEYS.newUser);
  wx.removeStorageSync(STORAGE_KEYS.cartCount);
  const app = getRuntimeApp();
  if (app) {
    app.globalData.token = null;
    app.globalData.userInfo = null;
    app.globalData.newUser = null;
    app.globalData.cartCount = 0;
  }
}

export function persistAuth(token: string, userInfo: UserInfo, newUser?: boolean | null): void {
  setToken(token);
  setStoredUserInfo(userInfo);
  if (typeof newUser === 'boolean') {
    setStoredNewUser(newUser);
  }
  const app = getRuntimeApp();
  if (app) {
    app.globalData.token = token;
    app.globalData.userInfo = userInfo;
    app.globalData.newUser = typeof newUser === 'boolean' ? newUser : getStoredNewUser();
  }
}

export function redirectToLogin(): void {
  const pages = getCurrentPages();
  const currentRoute = pages.length > 0 ? `/${pages[pages.length - 1].route}` : '';
  if (currentRoute === ROUTES.login) {
    return;
  }
  wx.redirectTo({ url: ROUTES.login });
}
