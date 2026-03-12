import request from '../utils/request'

export const userApi = {
  // 获取当前用户资料
  getProfile() {
    return request.get('/user/profile')
  },

  // 更新用户资料
  updateProfile(data) {
    return request.put('/user/profile', data)
  },

  // 修改密码
  changePassword(data) {
    return request.put('/user/password', data)
  },

  // 获取投资理念配置
  getPhilosophy() {
    return request.get('/user/philosophy')
  },

  // 更新投资理念配置
  updatePhilosophy(data) {
    return request.put('/user/philosophy', data)
  }
}
