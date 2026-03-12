<template>
  <div class="min-h-screen bg-primary">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-primary mb-2">投资理念配置</h1>
        <p class="text-secondary">定制您的投资风格，让AI更好地理解您的偏好</p>
      </div>

      <div class="card-glass rounded-2xl p-8">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
        >
          <!-- 风险偏好 -->
          <el-form-item label="风险偏好" prop="riskPreference">
            <el-radio-group v-model="form.riskPreference" size="large">
              <el-radio-button label="CONSERVATIVE">保守型</el-radio-button>
              <el-radio-button label="MODERATE">稳健型</el-radio-button>
              <el-radio-button label="AGGRESSIVE">积极型</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- 投资风格 -->
          <el-form-item label="投资风格" prop="investmentStyle">
            <el-radio-group v-model="form.investmentStyle" size="large">
              <el-radio-button label="VALUE">价值投资</el-radio-button>
              <el-radio-button label="GROWTH">成长投资</el-radio-button>
              <el-radio-button label="BLEND">混合型</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- 投资期限 -->
          <el-form-item label="投资期限" prop="investmentHorizon">
            <el-radio-group v-model="form.investmentHorizon" size="large">
              <el-radio-button label="SHORT">短期（&lt;1年）</el-radio-button>
              <el-radio-button label="MEDIUM">中期（1-3年）</el-radio-button>
              <el-radio-button label="LONG">长期（&gt;3年）</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- 偏好行业 -->
          <el-form-item label="偏好行业" prop="preferredSectors">
            <el-select
              v-model="form.preferredSectors"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入您关注的行业"
              class="w-full"
            >
              <el-option
                v-for="sector in sectorOptions"
                :key="sector.value"
                :label="sector.label"
                :value="sector.value"
              />
            </el-select>
          </el-form-item>

          <!-- 回避行业 -->
          <el-form-item label="回避行业" prop="avoidSectors">
            <el-select
              v-model="form.avoidSectors"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入您回避的行业"
              class="w-full"
            >
              <el-option
                v-for="sector in sectorOptions"
                :key="sector.value"
                :label="sector.label"
                :value="sector.value"
              />
            </el-select>
          </el-form-item>

          <!-- 投资理念描述 -->
          <el-form-item label="投资理念详细描述" prop="philosophyDescription">
            <el-input
              v-model="form.philosophyDescription"
              type="textarea"
              :rows="4"
              placeholder="描述您的投资理念，例如：您如何看待市场波动？什么是有价值的投资？"
            />
          </el-form-item>

          <!-- 策略备注 -->
          <el-form-item label="策略备注" prop="strategyNotes">
            <el-input
              v-model="form.strategyNotes"
              type="textarea"
              :rows="3"
              placeholder="描述您的投资策略，例如：选股标准、仓位管理方法等"
            />
          </el-form-item>

          <!-- 风险管理 -->
          <el-form-item label="风险管理方法" prop="riskManagement">
            <el-input
              v-model="form.riskManagement"
              type="textarea"
              :rows="3"
              placeholder="描述您的风险管理方法，例如：止损原则、分散策略等"
            />
          </el-form-item>

          <!-- AI学习设置 -->
          <el-divider />

          <el-form-item>
            <div class="flex items-center justify-between w-full">
              <div>
                <div class="text-primary font-medium">允许AI学习我的投资偏好</div>
                <div class="text-secondary text-sm">开启后，AI将从您的修正报告中学习您的投资风格</div>
              </div>
              <el-switch
                v-model="form.aiLearningEnabled"
                :active-value="1"
                :inactive-value="0"
                size="large"
              />
            </div>
          </el-form-item>

          <!-- 学习迭代次数 -->
          <div v-if="philosophyData.learningIterations > 0" class="mb-6 p-4 bg-card rounded-lg">
            <div class="flex items-center justify-between">
              <span class="text-secondary">AI学习迭代次数</span>
              <el-tag type="success" effect="dark">{{ philosophyData.learningIterations }} 次</el-tag>
            </div>
          </div>

          <!-- 操作按钮 -->
          <el-form-item class="mt-8">
            <div class="flex gap-4">
              <el-button type="primary" size="large" :loading="loading" @click="handleSave">
                保存配置
              </el-button>
              <el-button size="large" @click="resetForm">
                重置
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'

const formRef = ref(null)
const loading = ref(false)
const philosophyData = ref({})

// 行业选项
const sectorOptions = [
  { label: '科技', value: '科技' },
  { label: '金融', value: '金融' },
  { label: '医药', value: '医药' },
  { label: '消费', value: '消费' },
  { label: '新能源', value: '新能源' },
  { label: '房地产', value: '房地产' },
  { label: '汽车', value: '汽车' },
  { label: '通信', value: '通信' },
  { label: '能源', value: '能源' },
  { label: '材料', value: '材料' },
  { label: '工业', value: '工业' },
  { label: '公用事业', value: '公用事业' }
]

// 表单数据
const form = reactive({
  riskPreference: '',
  investmentStyle: '',
  investmentHorizon: '',
  preferredSectors: [],
  avoidSectors: [],
  philosophyDescription: '',
  strategyNotes: '',
  riskManagement: '',
  aiLearningEnabled: 1
})

const rules = {
  riskPreference: [{ required: true, message: '请选择风险偏好', trigger: 'change' }],
  investmentStyle: [{ required: true, message: '请选择投资风格', trigger: 'change' }],
  investmentHorizon: [{ required: true, message: '请选择投资期限', trigger: 'change' }]
}

// 获取投资理念配置
const fetchPhilosophy = async () => {
  try {
    const res = await userApi.getPhilosophy()
    if (res.data) {
      philosophyData.value = res.data
      // 填充表单
      Object.assign(form, {
        riskPreference: res.data.riskPreference || '',
        investmentStyle: res.data.investmentStyle || '',
        investmentHorizon: res.data.investmentHorizon || '',
        preferredSectors: res.data.preferredSectors ? res.data.preferredSectors.split(',') : [],
        avoidSectors: res.data.avoidSectors ? res.data.avoidSectors.split(',') : [],
        philosophyDescription: res.data.philosophyDescription || '',
        strategyNotes: res.data.strategyNotes || '',
        riskManagement: res.data.riskManagement || '',
        aiLearningEnabled: res.data.aiLearningEnabled ?? 1
      })
    }
  } catch (error) {
    console.error('获取投资理念配置失败:', error)
  }
}

// 保存配置
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    // 转换数据格式
    const data = {
      ...form,
      preferredSectors: form.preferredSectors.join(','),
      avoidSectors: form.avoidSectors.join(',')
    }

    const res = await userApi.updatePhilosophy(data)
    if (res.code === 200) {
      ElMessage.success('投资理念配置保存成功')
    }
  } catch (error) {
    console.error('保存投资理念配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  fetchPhilosophy()
}

onMounted(() => {
  fetchPhilosophy()
})
</script>
