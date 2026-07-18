export interface LoginRequest {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string | null
  avatar: string | null
  phone: string | null
  gender: number | null
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}
