import type { UserInfo } from '@/types/auth'

export const TOKEN_KEY = 'wuxin_admin_token'
export const USER_INFO_KEY = 'wuxin_admin_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getStoredUserInfo(): UserInfo | null {
  const value = localStorage.getItem(USER_INFO_KEY)
  if (!value) {
    return null
  }

  try {
    return JSON.parse(value) as UserInfo
  } catch {
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

export function setStoredUserInfo(userInfo: UserInfo): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export function clearAuthStorage(): void {
  removeToken()
  localStorage.removeItem(USER_INFO_KEY)
}
