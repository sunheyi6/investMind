import request from '../utils/request'

export const authApi = {
  // 用户注册
  register(data) {
    return request.post('/auth/register', data)
  },

  // 用户登录
  login(data) {
    return request.post('/auth/login', data)
  },

  // 刷新Token
  refreshToken(data) {
    return request.post('/auth/refresh', data)
  },

  // 用户登出
  logout() {
    return request.post('/auth/logout')
  }
}
