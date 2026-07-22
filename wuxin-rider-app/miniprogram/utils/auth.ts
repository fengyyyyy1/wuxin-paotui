import { ROUTES } from '../constants/routes';
import { STORAGE } from '../constants/storage';
import type { UserInfo } from '../types/auth';
import type { RiderProfile } from '../types/rider';

export function getToken(): string | null { return wx.getStorageSync<string>(STORAGE.token) || null; }
export function getUserInfo(): UserInfo | null { return wx.getStorageSync<UserInfo>(STORAGE.userInfo) || null; }
export function getRiderProfile(): RiderProfile | null { return wx.getStorageSync<RiderProfile>(STORAGE.riderProfile) || null; }

export function saveAuth(token: string, userInfo: UserInfo): void {
  wx.setStorageSync(STORAGE.token, token);
  wx.setStorageSync(STORAGE.userInfo, userInfo);
}

export function saveRiderProfile(profile: RiderProfile): void {
  wx.setStorageSync(STORAGE.riderProfile, profile);
}

export function clearAuth(): void {
  wx.removeStorageSync(STORAGE.token);
  wx.removeStorageSync(STORAGE.userInfo);
  wx.removeStorageSync(STORAGE.riderProfile);
}

export function redirectToLogin(): void {
  const pages = getCurrentPages();
  const current = pages.length ? pages[pages.length - 1].route : '';
  if (current !== ROUTES.login.slice(1)) {
    wx.reLaunch({ url: ROUTES.login });
  }
}
