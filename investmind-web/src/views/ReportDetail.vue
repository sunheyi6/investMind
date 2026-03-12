<template>
  <div class="min-h-screen py-6 px-4">
    <div class="max-w-5xl mx-auto">
      <div class="mb-4">
        <el-button 
          text 
          class="text-muted hover:text-primary"
          @click="$router.back()"
        >
          <el-icon class="mr-1"><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="text-4xl text-muted animate-spin"><Loading /></el-icon>
      </div>

      <template v-else-if="report">
        <div class="card-static p-6 mb-6">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-2">
                <span class="tag" :class="getTypeTagClass(report.reportType)">
                  {{ report.reportTypeName }}
                </span>
                <span class="text-muted text-sm">{{ report.reportDate }}</span>
              </div>
              <h1 class="text-xl md:text-2xl font-bold text-primary">
                {{ report.reportDate }} 投资分析报告
              </h1>
            </div>
            
            <div class="flex items-center gap-2">
              <span v-if="report.isCorrected" class="tag tag-green">
                <el-icon class="mr-1"><Check /></el-icon>已修正
              </span>
              <span v-if="report.isVectorized" class="tag tag-purple">
                <el-icon class="mr-1"><DataAnalysis /></el-icon>已向量化
              </span>
              <span v-if="report.isUsedForLearning" class="tag tag-gold">
                <el-icon class="mr-1"><Star /></el-icon>已学习
              </span>
            </div>
          </div>

          <div class="divider my-4"></div>

          <div class="flex flex-wrap gap-6 text-sm">
            <div v-if="report.aiGenerateTime">
              <span class="text-muted">AI生成时间:</span>
              <span class="text-secondary ml-2">{{ formatDateTime(report.aiGenerateTime) }}</span>
            </div>
            <div v-if="report.humanCorrectTime">
              <span class="text-muted">修正时间:</span>
              <span class="text-secondary ml-2">{{ formatDateTime(report.humanCorrectTime) }}</span>
            </div>
            <div v-if="report.correctedBy">
              <span class="text-muted">修正人:</span>
              <span class="text-secondary ml-2">{{ report.correctedBy }}</span>
            </div>
            <div v-if="report.qualityScore">
              <span class="text-muted">质量评分:</span>
              <el-rate 
                :model-value="report.qualityScore" 
                disabled 
                class="ml-2"
                size="small"
              />
            </div>
          </div>

          <div v-if="report.tags" class="mt-4 flex flex-wrap gap-2">
            <span 
              v-for="tag in report.tags.split(',')" 
              :key="tag"
              class="tag tag-gray"
            >
              {{ tag.trim() }}
            </span>
          </div>
        </div>

        <div class="grid lg:grid-cols-2 gap-6">
          <div class="card-static p-5">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-base font-semibold text-primary flex items-center">
                <el-icon class="mr-2 text-blue"><Cpu /></el-icon>
                AI 生成内容
              </h3>
              <span class="tag tag-gray">原始</span>
            </div>
            <div class="max-h-[500px] overflow-y-auto pr-2">
              <div class="text-secondary text-sm leading-relaxed whitespace-pre-wrap">
                {{ report.aiContent }}
              </div>
            </div>
          </div>

          <div class="card-static p-5">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-base font-semibold text-primary flex items-center">
                <el-icon class="mr-2 text-purple"><User /></el-icon>
                人工修正内容
              </h3>
              <div class="flex items-center gap-2">
                <span v-if="report.humanContent" class="tag tag-green">已修正</span>
                <span v-else class="tag tag-gray">待修正</span>
                <el-button 
                  type="primary" 
                  size="small"
                  @click="showCorrectDialog = true"
                >
                  <el-icon class="mr-1"><EditPen /></el-icon>
                  {{ report.humanContent ? '重新修正' : '立即修正' }}
                </el-button>
              </div>
            </div>
            <div v-if="report.humanContent" class="max-h-[500px] overflow-y-auto pr-2">
              <div class="text-secondary text-sm leading-relaxed whitespace-pre-wrap">
                {{ report.humanContent }}
              </div>
            </div>
            <div v-else class="flex flex-col items-center justify-center py-16 text-muted">
              <el-icon class="text-3xl mb-2"><EditPen /></el-icon>
              <p class="text-sm">暂无人工修正内容</p>
            </div>
          </div>
        </div>

        <div class="card-static p-4 mt-6 flex flex-wrap items-center justify-between gap-4">
          <div class="text-muted text-sm">
            报告ID: {{ report.id }}
          </div>
          <div class="flex items-center gap-3">
            <el-button 
              v-if="!report.isVectorized && report.humanContent"
              type="warning"
              :loading="vectorizing"
              @click="vectorizeReport"
            >
              <el-icon class="mr-1"><DataAnalysis /></el-icon>
              向量化
            </el-button>
            <el-button 
              type="primary"
              @click="showCorrectDialog = true"
            >
              <el-icon class="mr-1"><EditPen /></el-icon>
              修正报告
            </el-button>
          </div>
        </div>
      </template>

      <div v-else class="card-static py-16 text-center">
        <el-icon class="text-5xl text-muted mb-4"><DocumentDelete /></el-icon>
        <p class="text-muted">报告不存在</p>
      </div>
    </div>

    <el-dialog
      v-model="showCorrectDialog"
      title="人工修正报告"
      width="700px"
      destroy-on-close
    >
      <div class="space-y-4">
        <el-alert
          title="修正说明"
          description="请基于AI生成的内容，进行人工修正和优化。修正后的内容将用于模型学习。"
          type="info"
          show-icon
          :closable="false"
        />
        
        <el-input
          v-model="correctForm.humanContent"
          type="textarea"
          :rows="12"
          placeholder="请输入修正后的报告内容..."
        />
        
        <div class="grid grid-cols-2 gap-4">
          <el-input
            v-model="correctForm.correctedBy"
            placeholder="修正人"
            prefix-icon="User"
          />
          <el-input
            v-model="correctForm.correctionReason"
            placeholder="修正原因"
            prefix-icon="InfoFilled"
          />
        </div>
        
        <div class="flex items-center gap-4">
          <span class="text-muted text-sm">质量评分:</span>
          <el-rate v-model="correctForm.qualityScore" allow-half />
        </div>
        
        <el-input
          v-model="correctForm.tags"
          placeholder="标签（用逗号分隔，如：牛市,科技股,策略）"
          prefix-icon="CollectionTag"
        />
      </div>
      
      <template #footer>
        <el-button class="btn-secondary" @click="showCorrectDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="correcting"
          @click="submitCorrection"
        >
          提交修正
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { reportApi, vectorApi } from '../api/report'
import dayjs from 'dayjs'

const route = useRoute()
const reportId = route.params.id

const report = ref(null)
const loading = ref(false)
const vectorizing = ref(false)
const showCorrectDialog = ref(false)
const correcting = ref(false)

const correctForm = ref({
  reportId: null,
  humanContent: '',
  correctedBy: '',
  correctionReason: '',
  qualityScore: 0,
  tags: ''
})

const fetchReport = async () => {
  loading.value = true
  try {
    const res = await reportApi.getById(reportId)
    if (res.code === 200) {
      report.value = res.data
      correctForm.value.reportId = res.data.id
      correctForm.value.humanContent = res.data.humanContent || res.data.aiContent
      correctForm.value.correctedBy = res.data.correctedBy || ''
      correctForm.value.qualityScore = res.data.qualityScore || 0
      correctForm.value.tags = res.data.tags || ''
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取报告详情失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (datetime) => {
  return datetime ? dayjs(datetime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const getTypeTagClass = (type) => {
  const map = {
    'DAILY': 'tag-blue',
    'WEEKLY': 'tag-purple',
    'MONTHLY': 'tag-gold'
  }
  return map[type] || 'tag-gray'
}

const submitCorrection = async () => {
  if (!correctForm.value.humanContent.trim()) {
    ElMessage.warning('请输入修正内容')
    return
  }
  
  correcting.value = true
  try {
    const res = await reportApi.correct(correctForm.value)
    if (res.code === 200) {
      ElMessage.success('修正成功')
      showCorrectDialog.value = false
      fetchReport()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('修正失败')
  } finally {
    correcting.value = false
  }
}

const vectorizeReport = async () => {
  vectorizing.value = true
  try {
    const res = await vectorApi.vectorize(reportId)
    if (res.code === 200) {
      ElMessage.success('向量化成功')
      fetchReport()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('向量化失败')
  } finally {
    vectorizing.value = false
  }
}

onMounted(() => {
  fetchReport()
})
</script>
