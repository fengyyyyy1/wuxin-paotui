import { request } from '../utils/request';
import type {
  BindWechatPhoneRequest,
  UpdateUserProfileRequest,
  UserInfo,
  WeChatLoginRequest,
  WeChatLoginResponse
} from '../types/user';

export function wechatLogin(data: WeChatLoginRequest): Promise<WeChatLoginResponse> {
  return request<WeChatLoginResponse, WeChatLoginRequest>({
    url: '/api/user/wechat/login',
    method: 'POST',
    data,
    auth: false
  });
}

export function getCurrentUser(): Promise<UserInfo> {
  return request<UserInfo>({
    url: '/api/user/me'
  });
}

export function getUserProfile(): Promise<UserInfo> {
  return request<UserInfo>({
    url: '/api/user/profile'
  });
}

export function updateUserProfile(data: UpdateUserProfileRequest): Promise<void> {
  return request<void, UpdateUserProfileRequest>({
    url: '/api/user/profile',
    method: 'PUT',
    data
  });
}

export function bindWechatPhone(data: BindWechatPhoneRequest): Promise<UserInfo> {
  return request<UserInfo, BindWechatPhoneRequest>({
    url: '/api/user/phone/bind',
    method: 'POST',
    data
  });
}
