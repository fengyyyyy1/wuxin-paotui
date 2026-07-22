import type { LoginResult, PasswordLoginRequest } from '../types/auth';
import { request } from '../utils/request';
export const passwordLogin = (data: PasswordLoginRequest): Promise<LoginResult> => request({ url: '/api/user/login', method: 'POST', data, auth: false });
export const wechatLogin = (code: string): Promise<LoginResult> => request({ url: '/api/user/wechat/login', method: 'POST', data: { code }, auth: false });
