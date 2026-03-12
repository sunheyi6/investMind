import request from '../utils/request'

// 报告相关 API
export const reportApi = {
  // 生成报告
  generate(data) {
    return request.post('/reports/generate', data)
  },

  // 修正报告
  correct(data) {
    return request.post('/reports/correct', data)
  },

  // 获取报告详情
  getById(id) {
    return request.get(`/reports/${id}`)
  },

  // 根据日期获取报告
  getByDate(date) {
    return request.get(`/reports/date/${date}`)
  },

  // 获取今日报告
  getToday() {
    return request.get('/reports/today')
  },

  // 分页查询报告列表
  list(params) {
    return request.post('/reports/list', params)
  },

  // 生成今日报告（快捷接口）
  generateToday(useHistoricalContent = true) {
    return request.post(`/reports/today/generate?useHistoricalContent=${useHistoricalContent}`)
  }
}

// 向量相关 API
export const vectorApi = {
  // 检查向量数据库连接
  health() {
    return request.get('/vectors/health')
  },

  // 获取待向量化报告
  getPending() {
    return request.get('/vectors/pending')
  },

  // 向量化指定报告
  vectorize(reportId) {
    return request.post(`/vectors/report/${reportId}`)
  },

  // 批量向量化
  batchVectorize() {
    return request.post('/vectors/batch')
  },

  // 检索相似内容
  search(query, topK = 5) {
    return request.get('/vectors/search', { params: { query, topK } })
  }
}

// 统计相关 API
export const statsApi = {
  // 获取统计数据概览
  getOverview() {
    return request.get('/stats/overview')
  },

  // 获取模型学习进度
  getLearningProgress() {
    return request.get('/stats/learning-progress')
  }
}

export default request
