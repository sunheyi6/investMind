import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 如果响应成功
    if (res.code === 200) {
      return res
    }

    // 显示错误消息
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  async (error) => {
    const { response } = error
    const authStore = useAuthStore()

    if (response) {
      const { status, data } = response

      switch (status) {
        case 401:
          // Token过期，尝试刷新
          if (authStore.refreshToken) {
            try {
              await authStore.refreshAccessToken()
              // 重试原请求
              return request(error.config)
            } catch (refreshError) {
              // 刷新失败，清除登录状态并跳转登录页
              authStore.clearAuth()
              window.location.href = '/login'
              ElMessage.error('登录已过期，请重新登录')
              return Promise.reject(refreshError)
            }
          } else {
            authStore.clearAuth()
            window.location.href = '/login'
            ElMessage.error('请先登录')
          }
          break

        case 403:
          ElMessage.error(data?.message || '无权访问')
          break

        case 404:
          ElMessage.error(data?.message || '资源不存在')
          break

        case 500:
          ElMessage.error(data?.message || '服务器错误')
          break

        default:
          ElMessage.error(data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

export default request
