import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // Getters
  const isLoggedIn = computed(() => !!accessToken.value)
  const userInfo = computed(() => user.value)

  // Actions
  const setAuth = (authData) => {
    accessToken.value = authData.accessToken
    refreshToken.value = authData.refreshToken
    user.value = authData.user

    localStorage.setItem('accessToken', authData.accessToken)
    localStorage.setItem('refreshToken', authData.refreshToken)
    localStorage.setItem('user', JSON.stringify(authData.user))
  }

  const clearAuth = () => {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  const login = async (credentials) => {
    const response = await authApi.login(credentials)
    if (response.data) {
      setAuth(response.data)
    }
    return response
  }

  const register = async (userData) => {
    const response = await authApi.register(userData)
    if (response.data) {
      setAuth(response.data)
    }
    return response
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } finally {
      clearAuth()
    }
  }

  const refreshAccessToken = async () => {
    try {
      const response = await authApi.refreshToken({
        refreshToken: refreshToken.value
      })
      if (response.data) {
        accessToken.value = response.data.accessToken
        localStorage.setItem('accessToken', response.data.accessToken)
      }
      return response.data
    } catch (error) {
      clearAuth()
      throw error
    }
  }

  return {
    accessToken,
    refreshToken,
    user,
    isLoggedIn,
    userInfo,
    setAuth,
    clearAuth,
    login,
    register,
    logout,
    refreshAccessToken
  }
})
