import { passwordLogin, wechatLogin } from '../api/auth';
import { getMerchantProfile as fetchMerchantProfile } from '../api/merchant';
import { MOCK_WECHAT_LOGIN_STORAGE_KEY } from '../config/env';
import { ROUTES } from '../constants/routes';
import { MERCHANT_AUDIT, MERCHANT_STATUS } from '../constants/status';
import type { PasswordLoginRequest, UserInfo } from '../types/auth';
import type { MerchantProfile } from '../types/merchant';
import { clearAuth, getMerchantProfile, getToken, getUserInfo, saveAuth, saveMerchantProfile } from '../utils/auth';
import { RequestError } from '../utils/request';

export interface AuthState { token: string | null; userInfo: UserInfo | null; merchantProfile: MerchantProfile | null; }
let state: AuthState = { token: null, userInfo: null, merchantProfile: null };
function sync(): void { try { const app = getApp<IAppOption>(); if (app?.globalData) { app.globalData.token = state.token; app.globalData.userInfo = state.userInfo; app.globalData.merchantProfile = state.merchantProfile; } } catch { return; } }
export function restoreSession(): AuthState { state = { token: getToken(), userInfo: getUserInfo(), merchantProfile: getMerchantProfile() }; sync(); return { ...state }; }
export async function refreshMerchantProfile(): Promise<MerchantProfile> { const profile = await fetchMerchantProfile(); saveMerchantProfile(profile); restoreSession(); return profile; }
export async function routeByIdentity(): Promise<void> { try { const profile = await refreshMerchantProfile(); if (profile.auditStatus === MERCHANT_AUDIT.approved && profile.merchantStatus === MERCHANT_STATUS.enabled && profile.storeStatus === MERCHANT_STATUS.enabled) wx.reLaunch({ url: ROUTES.dashboard }); else wx.reLaunch({ url: ROUTES.review }); } catch (error) { if (error instanceof RequestError && error.code === 404) { wx.reLaunch({ url: ROUTES.apply }); return; } throw error; } }
export async function loginWithPassword(payload: PasswordLoginRequest): Promise<void> { const result = await passwordLogin(payload); saveAuth(result.token, result.userInfo); restoreSession(); await routeByIdentity(); }
export async function loginWithWechat(): Promise<void> { const useMock = wx.getStorageSync<boolean>(MOCK_WECHAT_LOGIN_STORAGE_KEY) === true; const code = useMock ? 'mock-code-test001' : await new Promise<string>((resolve, reject) => wx.login({ success: r => r.code ? resolve(r.code) : reject(new Error('未获取到微信登录凭证')), fail: reject })); const result = await wechatLogin(code); saveAuth(result.token, result.userInfo); restoreSession(); await routeByIdentity(); }
export async function requireApprovedMerchant(): Promise<boolean> { if (!getToken()) { wx.reLaunch({ url: ROUTES.login }); return false; } try { const profile = await refreshMerchantProfile(); if (profile.auditStatus !== MERCHANT_AUDIT.approved || profile.merchantStatus !== MERCHANT_STATUS.enabled || profile.storeStatus !== MERCHANT_STATUS.enabled) { wx.reLaunch({ url: ROUTES.review }); return false; } return true; } catch (error) { if (error instanceof RequestError && error.code === 404) { wx.reLaunch({ url: ROUTES.apply }); return false; } throw error; } }
export function logout(): void { clearAuth(); restoreSession(); wx.reLaunch({ url: ROUTES.login }); }
