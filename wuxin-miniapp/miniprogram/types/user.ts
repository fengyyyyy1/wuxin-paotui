export interface UserInfo {
  id: number;
  username: string;
  nickname: string | null;
  avatar: string | null;
  phone: string | null;
  gender: 0 | 1 | 2;
}

export interface WeChatLoginRequest {
  code: string;
}

export interface WeChatLoginResponse {
  token: string;
  userInfo: UserInfo;
  newUser: boolean;
}

export interface UpdateUserProfileRequest {
  nickname?: string | null;
  avatar?: string | null;
  gender: 0 | 1 | 2;
}

export interface BindWechatPhoneRequest {
  code: string;
}
