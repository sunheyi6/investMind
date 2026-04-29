import request from '../utils/request'

export const aiApi = {
  chat(question) {
    return request.post('/ai/chat', { question }, { timeout: 90000 })
  }
}
