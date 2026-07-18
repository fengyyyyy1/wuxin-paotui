import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { verifyAdminMerchantPermission } from '@/api/adminMerchant'
import { login as loginRequest } from '@/api/auth'
import { ApiError } from '@/utils/http'
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
  const isAdmin = ref(false)
  const adminVerified = ref(false)
  const loading = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value))

  function saveSession(nextToken: string, nextUserInfo: UserInfo): void {
    token.value = nextToken
    userInfo.value = nextUserInfo
    setToken(nextToken)
    setStoredUserInfo(nextUserInfo)
  }

  function clearSession(): void {
    token.value = null
    userInfo.value = null
    isAdmin.value = false
    adminVerified.value = false
    clearAuthStorage()
  }

  async function verifyAdminPermission(skipErrorMessage = false): Promise<boolean> {
    if (!token.value) {
      clearSession()
      return false
    }

    try {
      await verifyAdminMerchantPermission(skipErrorMessage)
      isAdmin.value = true
      adminVerified.value = true
      return true
    } catch (error) {
      isAdmin.value = false
      adminVerified.value = false
      if (error instanceof ApiError && (error.code === 401 || error.code === 403)) {
        clearSession()
        return false
      }
      throw error
    }
  }

  async function login(payload: LoginRequest): Promise<void> {
    if (loading.value) {
      return
    }

    loading.value = true
    try {
      const result = await loginRequest(payload)
      saveSession(result.token, result.userInfo)

      const verified = await verifyAdminPermission(true)
      if (!verified) {
        clearSession()
        throw new ApiError(403, '当前账号无管理员权限')
      }
    } catch (error) {
      if (error instanceof ApiError && error.code === 403) {
        clearSession()
        throw new ApiError(403, '当前账号无管理员权限')
      }
      throw error
    } finally {
      loading.value = false
    }
  }

  function logout(): void {
    clearSession()
  }

  async function restoreSession(): Promise<boolean> {
    token.value = getToken()
    userInfo.value = getStoredUserInfo()
    isAdmin.value = false
    adminVerified.value = false

    if (!token.value) {
      clearSession()
      return false
    }

    return verifyAdminPermission()
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    isAdmin,
    adminVerified,
    loading,
    login,
    logout,
    restoreSession,
    clearSession,
    verifyAdminPermission,
  }
})
