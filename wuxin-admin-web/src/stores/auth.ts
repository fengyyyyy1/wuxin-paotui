import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { login as loginRequest } from '@/api/auth'
import type { LoginRequest, UserInfo } from '@/types/auth'
import {
  clearAuthStorage,
  getStoredUserInfo,
  getToken,
  setStoredUserInfo,
  setToken,
} from '@/utils/storage'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getStoredUserInfo())
  const isAuthenticated = computed(() => Boolean(token.value))

  async function login(payload: LoginRequest): Promise<void> {
    const result = await loginRequest(payload)
    token.value = result.token
    userInfo.value = result.userInfo
    setToken(result.token)
    setStoredUserInfo(result.userInfo)
  }

  function logout(): void {
    token.value = null
    userInfo.value = null
    clearAuthStorage()
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    login,
    logout,
  }
})
