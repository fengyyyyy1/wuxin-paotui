import { ROUTES } from '../constants/routes';
import { STORAGE } from '../constants/storage';
import type { UserInfo } from '../types/auth';
import type { MerchantProfile } from '../types/merchant';

export const getToken = (): string | null => wx.getStorageSync<string>(STORAGE.token) || null;
export const getUserInfo = (): UserInfo | null => wx.getStorageSync<UserInfo>(STORAGE.userInfo) || null;
export const getMerchantProfile = (): MerchantProfile | null => wx.getStorageSync<MerchantProfile>(STORAGE.merchantProfile) || null;
export function saveAuth(token: string, userInfo: UserInfo): void { wx.setStorageSync(STORAGE.token, token); wx.setStorageSync(STORAGE.userInfo, userInfo); }
export function saveMerchantProfile(profile: MerchantProfile): void { wx.setStorageSync(STORAGE.merchantProfile, profile); }
export function clearAuth(): void { wx.removeStorageSync(STORAGE.token); wx.removeStorageSync(STORAGE.userInfo); wx.removeStorageSync(STORAGE.merchantProfile); }
export function redirectToLogin(): void { const pages = getCurrentPages(); const current = pages.length ? pages[pages.length - 1].route : ''; if (current !== ROUTES.login.slice(1)) wx.reLaunch({ url: ROUTES.login }); }
