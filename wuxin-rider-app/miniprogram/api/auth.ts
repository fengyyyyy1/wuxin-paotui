import type { LoginResult, PasswordLoginRequest } from '../types/auth';
import { request } from '../utils/request';

export function passwordLogin(data: PasswordLoginRequest): Promise<LoginResult> { return request({ url: '/api/user/login', method: 'POST', data, auth: false }); }
export function wechatLogin(code: string): Promise<LoginResult> { return request({ url: '/api/user/wechat/login', method: 'POST', data: { code }, auth: false }); }
export function bindPhone(code: string): Promise<import('../types/auth').UserInfo> { return request({ url: '/api/user/phone/bind', method: 'POST', data: { code } }); }
