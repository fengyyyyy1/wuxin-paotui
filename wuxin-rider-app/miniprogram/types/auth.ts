export interface UserInfo {
  id: number;
  username: string;
  nickname: string | null;
  avatar: string | null;
  phone: string | null;
  gender: number;
}

export interface LoginResult { token: string; userInfo: UserInfo; newUser?: boolean; }
export interface PasswordLoginRequest { username: string; password: string; }
