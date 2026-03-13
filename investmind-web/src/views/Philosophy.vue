<template>
  <div class="min-h-screen py-6 px-4">
    <div class="max-w-5xl mx-auto">
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-primary mb-2">投资理念文档</h1>
        <p class="text-secondary">
          直接用自然语言描述或修正你的投资思路，系统会自动结构化存储并生成完整文档。
        </p>
      </div>

      <div class="card-static p-4 mb-6">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="5"
          maxlength="5000"
          show-word-limit
          placeholder="例如：我偏好中低风险，核心是先判断行业景气和估值，再分批建仓；单票不超过15%，跌破核心逻辑或估值透支就卖出。"
        />
        <div class="mt-4 flex flex-wrap items-center justify-between gap-3">
          <div class="text-sm text-muted">
            当前版本：<span class="text-primary font-medium">v{{ versionNo }}</span>
          </div>
          <div class="flex gap-2">
            <el-button class="btn-secondary" :loading="loadingDoc" @click="fetchDocument">
              刷新文档
            </el-button>
            <el-button type="primary" :loading="ingesting" @click="submitNaturalLanguage">
              更新理念
            </el-button>
          </div>
        </div>
      </div>

      <div class="card-glass rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-primary">文档视图</h2>
          <span class="text-xs text-muted">自动生成 · 基于结构化字段</span>
        </div>
        <div
          v-if="documentHtml"
          class="prose prose-invert max-w-none text-secondary"
          v-html="documentHtml"
        ></div>
        <div v-else class="text-muted py-10 text-center">
          暂无文档内容，请先输入你的投资理念。
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { userApi } from '../api/user'

const inputText = ref('')
const versionNo = ref(1)
const markdown = ref('')
const ingesting = ref(false)
const loadingDoc = ref(false)

const documentHtml = computed(() => {
  if (!markdown.value) return ''
  return DOMPurify.sanitize(marked.parse(markdown.value))
})

const fetchDocument = async () => {
  loadingDoc.value = true
  try {
    const res = await userApi.getPhilosophyDocument()
    if (res.code === 200 && res.data) {
      markdown.value = res.data.documentMarkdown || ''
      versionNo.value = res.data.versionNo || versionNo.value
    }
  } catch (error) {
    ElMessage.error('获取理念文档失败')
  } finally {
    loadingDoc.value = false
  }
}

const submitNaturalLanguage = async () => {
  const text = inputText.value.trim()
  if (!text) {
    ElMessage.warning('请输入理念内容')
    return
  }

  ingesting.value = true
  try {
    const res = await userApi.ingestPhilosophy(text)
    if (res.code === 200) {
      ElMessage.success('理念已更新')
      versionNo.value = res.data?.versionNo || versionNo.value
      inputText.value = ''
      await fetchDocument()
    }
  } catch (error) {
    ElMessage.error('更新理念失败')
  } finally {
    ingesting.value = false
  }
}

onMounted(() => {
  fetchDocument()
})
</script>
