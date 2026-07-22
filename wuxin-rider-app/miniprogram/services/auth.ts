import { passwordLogin, wechatLogin } from '../api/auth';
import { getRiderProfile as fetchRiderProfile } from '../api/rider';
import { IS_DEVELOPMENT_ENV, MOCK_WECHAT_LOGIN_STORAGE_KEY } from '../config/env';
import { ROUTES } from '../constants/routes';
import { RIDER_AUDIT, RIDER_STATUS } from '../constants/status';
import type { PasswordLoginRequest, UserInfo } from '../types/auth';
import type { RiderProfile } from '../types/rider';
import { RequestError } from '../utils/request';
import { clearAuth, getRiderProfile, getToken, getUserInfo, saveAuth, saveRiderProfile } from '../utils/auth';

export interface AuthState { token: string | null; userInfo: UserInfo | null; riderProfile: RiderProfile | null; }
let state: AuthState = { token: null, userInfo: null, riderProfile: null };

function sync(): void {
  try {
    const app = getApp<IAppOption>();
    if (app?.globalData) { app.globalData.token = state.token; app.globalData.userInfo = state.userInfo; app.globalData.riderProfile = state.riderProfile; }
  } catch { return; }
}

export function restoreSession(): AuthState {
  state = { token: getToken(), userInfo: getUserInfo(), riderProfile: getRiderProfile() };
  sync();
  return { ...state };
}

export async function loginWithPassword(payload: PasswordLoginRequest): Promise<void> {
  const result = await passwordLogin(payload); saveAuth(result.token, result.userInfo); restoreSession(); await routeByIdentity();
}

export async function loginWithWechat(): Promise<void> {
  const useMock = IS_DEVELOPMENT_ENV && wx.getStorageSync<boolean>(MOCK_WECHAT_LOGIN_STORAGE_KEY) === true;
  const code = useMock ? 'mock-code-test001' : await new Promise<string>((resolve, reject) => wx.login({ success: r => r.code ? resolve(r.code) : reject(new Error('未获取到微信登录凭证')), fail: reject }));
  const result = await wechatLogin(code); saveAuth(result.token, result.userInfo); restoreSession(); await routeByIdentity();
}

export async function refreshRiderProfile(): Promise<RiderProfile> {
  const profile = await fetchRiderProfile(); saveRiderProfile(profile); restoreSession(); return profile;
}

export async function routeByIdentity(): Promise<void> {
  try {
    const profile = await refreshRiderProfile();
    if (profile.auditStatus === RIDER_AUDIT.approved && profile.riderStatus === RIDER_STATUS.enabled) wx.reLaunch({ url: ROUTES.dashboard });
    else wx.reLaunch({ url: ROUTES.review });
  } catch (error) {
    if (error instanceof RequestError && error.code === 404) { wx.reLaunch({ url: ROUTES.apply }); return; }
    throw error;
  }
}

export async function requireApprovedRider(): Promise<boolean> {
  if (!getToken()) { wx.reLaunch({ url: ROUTES.login }); return false; }
  try {
    const profile = await refreshRiderProfile();
    if (profile.auditStatus !== RIDER_AUDIT.approved || profile.riderStatus !== RIDER_STATUS.enabled) { wx.reLaunch({ url: ROUTES.review }); return false; }
    return true;
  } catch (error) {
    if (error instanceof RequestError && error.code === 404) { wx.reLaunch({ url: ROUTES.apply }); return false; }
    throw error;
  }
}

export function logout(): void { clearAuth(); restoreSession(); wx.reLaunch({ url: ROUTES.login }); }
