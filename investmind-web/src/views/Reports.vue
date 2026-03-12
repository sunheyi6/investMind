<template>
  <div class="min-h-screen py-6 px-4">
    <div class="max-w-7xl mx-auto">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-bold text-primary mb-1">报告中心</h1>
          <p class="text-muted text-sm">查看、生成和修正投资分析报告</p>
        </div>
        <div class="flex items-center gap-3">
          <el-button 
            type="primary" 
            :loading="generating"
            @click="generateTodayReport"
          >
            <el-icon class="mr-2"><Plus /></el-icon>
            生成今日报告
          </el-button>
          <el-button
            class="btn-secondary"
            @click="showVectorPanel = true"
          >
            <el-icon class="mr-2"><DataAnalysis /></el-icon>
            向量管理
          </el-button>
        </div>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="stat-card">
          <div class="text-2xl font-bold text-gold mb-1">{{ stats.total }}</div>
          <div class="text-muted text-sm">总报告数</div>
        </div>
        <div class="stat-card">
          <div class="text-2xl font-bold text-blue mb-1">{{ stats.corrected }}</div>
          <div class="text-muted text-sm">已修正</div>
        </div>
        <div class="stat-card">
          <div class="text-2xl font-bold text-purple mb-1">{{ stats.vectorized }}</div>
          <div class="text-muted text-sm">已向量化</div>
        </div>
        <div class="stat-card">
          <div class="text-2xl font-bold text-green mb-1">{{ stats.learned }}</div>
          <div class="text-muted text-sm">已学习</div>
        </div>
      </div>

      <div class="card-static p-4 mb-6">
        <div class="flex flex-wrap items-center gap-3">
          <el-select 
            v-model="queryParams.reportType" 
            placeholder="报告类型"
            class="w-32"
            clearable
          >
            <el-option label="日报" value="DAILY" />
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
          </el-select>
          
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="w-64"
            value-format="YYYY-MM-DD"
          />
          
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
          <el-button class="btn-secondary" @click="resetQuery">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="space-y-3">
        <div 
          v-for="report in reports" 
          :key="report.id"
          class="card p-4 cursor-pointer"
          @click="viewDetail(report.id)"
        >
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <span class="tag" :class="getTypeTagClass(report.reportType)">
                  {{ report.reportTypeName }}
                </span>
                <span class="text-muted text-sm">{{ report.reportDate }}</span>
                <span v-if="report.isCorrected" class="tag tag-green">已修正</span>
                <span v-if="report.isVectorized" class="tag tag-purple">已向量化</span>
                <span v-if="report.isUsedForLearning" class="tag tag-gold">已学习</span>
              </div>
              <p class="text-secondary text-sm line-clamp-2">{{ report.summary || '暂无摘要' }}</p>
            </div>
            
            <div class="flex items-center gap-4">
              <div v-if="report.qualityScore" class="flex items-center gap-1">
                <el-rate 
                  :model-value="report.qualityScore" 
                  disabled 
                  :max="5"
                  size="small"
                />
              </div>
              <el-button type="primary" text class="text-gold">
                查看详情
                <el-icon class="ml-1"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="reports.length === 0 && !loading" class="card-static py-16 text-center">
          <el-icon class="text-5xl text-muted mb-4"><Document /></el-icon>
          <p class="text-muted">暂无报告，点击上方按钮生成</p>
        </div>
      </div>

      <div class="flex justify-center mt-6">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-drawer
      v-model="showVectorPanel"
      title="向量管理"
      size="400px"
    >
      <div class="space-y-6">
        <div class="card-static p-4">
          <div class="flex items-center justify-between">
            <span class="text-secondary">Milvus 连接状态</span>
            <el-tag :type="vectorHealth ? 'success' : 'danger'" size="small">
              {{ vectorHealth ? '正常' : '异常' }}
            </el-tag>
          </div>
        </div>

        <div class="card-static p-4">
          <div class="flex items-center justify-between mb-4">
            <span class="text-primary font-medium">待向量化报告</span>
            <el-tag type="warning" size="small">{{ pendingReports.length }} 条</el-tag>
          </div>
          <el-button 
            type="primary" 
            class="w-full"
            :loading="vectorizing"
            @click="batchVectorize"
          >
            <el-icon class="mr-2"><DataAnalysis /></el-icon>
            批量向量化
          </el-button>
        </div>

        <div class="card-static p-4">
          <div class="text-primary font-medium mb-4">相似内容检索</div>
          <el-input
            v-model="searchQuery"
            placeholder="输入查询内容"
            class="mb-3"
          >
            <template #append>
              <el-button @click="searchSimilar">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
          <div v-if="searchResults.length > 0" class="space-y-2 max-h-60 overflow-y-auto">
            <div 
              v-for="(result, index) in searchResults" 
              :key="index"
              class="p-3 rounded-lg bg-secondary text-sm text-secondary"
            >
              {{ result.substring(0, 100) }}...
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { reportApi, vectorApi, statsApi } from '../api/report'

const router = useRouter()

const reports = ref([])
const total = ref(0)
const loading = ref(false)
const generating = ref(false)
const showVectorPanel = ref(false)
const vectorHealth = ref(false)
const pendingReports = ref([])
const vectorizing = ref(false)
const searchQuery = ref('')
const searchResults = ref([])

const stats = reactive({
  total: 0,
  corrected: 0,
  vectorized: 0,
  learned: 0
})

const queryParams = reactive({
  reportType: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 10
})

const dateRange = ref([])

const fetchReports = async () => {
  loading.value = true
  try {
    const res = await reportApi.list(queryParams)
    if (res.code === 200) {
      reports.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取报告列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await statsApi.getOverview()
    if (res.code === 200) {
      stats.total = res.data.total ?? res.data.totalReports ?? 0
      stats.corrected = res.data.corrected ?? res.data.correctedReports ?? 0
      stats.vectorized = res.data.vectorized ?? res.data.vectorizedReports ?? 0
      stats.learned = res.data.learned ?? res.data.learningReports ?? 0
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const generateTodayReport = async () => {
  generating.value = true
  try {
    const res = await reportApi.generateToday(true)
    if (res.code === 200) {
      ElMessage.success(res.message)
      fetchReports()
      fetchStats()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('生成报告失败')
  } finally {
    generating.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/report/${id}`)
}

const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startDate = dateRange.value[0]
    queryParams.endDate = dateRange.value[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
  queryParams.pageNum = 1
  fetchReports()
}

const resetQuery = () => {
  queryParams.reportType = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  queryParams.pageNum = 1
  fetchReports()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchReports()
}

const handlePageChange = (val) => {
  queryParams.pageNum = val
  fetchReports()
}

const getTypeTagClass = (type) => {
  const map = {
    'DAILY': 'tag-blue',
    'WEEKLY': 'tag-purple',
    'MONTHLY': 'tag-gold'
  }
  return map[type] || 'tag-gray'
}

const checkVectorHealth = async () => {
  try {
    const res = await vectorApi.health()
    vectorHealth.value = res.data
  } catch {
    vectorHealth.value = false
  }
}

const fetchPendingReports = async () => {
  try {
    const res = await vectorApi.getPending()
    if (res.code === 200) {
      pendingReports.value = res.data || []
    }
  } catch (error) {
    console.error('获取待向量化报告失败', error)
  }
}

const batchVectorize = async () => {
  if (pendingReports.value.length === 0) {
    ElMessage.warning('没有待向量化的报告')
    return
  }
  vectorizing.value = true
  try {
    const res = await vectorApi.batchVectorize()
    if (res.code === 200) {
      ElMessage.success(`成功向量化 ${res.data} 条报告`)
      fetchPendingReports()
      fetchStats()
    }
  } catch (error) {
    ElMessage.error('向量化失败')
  } finally {
    vectorizing.value = false
  }
}

const searchSimilar = async () => {
  if (!searchQuery.value.trim()) return
  try {
    const res = await vectorApi.search(searchQuery.value, 5)
    if (res.code === 200) {
      searchResults.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('检索失败')
  }
}

watch(showVectorPanel, (val) => {
  if (val) {
    checkVectorHealth()
    fetchPendingReports()
  }
})

onMounted(() => {
  fetchReports()
  fetchStats()
})
</script>



