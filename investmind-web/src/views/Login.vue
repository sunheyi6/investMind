<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <!-- Logo区域 -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 rounded-2xl gradient-gold flex items-center justify-center mx-auto mb-4 shadow-lg">
          <el-icon class="text-primary text-3xl"><TrendCharts /></el-icon>
        </div>
        <h1 class="text-2xl font-bold text-primary mb-2">欢迎回来</h1>
        <p class="text-secondary">登录 InvestMind 智投进化平台</p>
      </div>

      <!-- 登录表单 -->
      <div class="card-glass rounded-2xl p-8 shadow-xl">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <div class="flex items-center justify-between mb-6">
            <el-checkbox v-model="rememberMe" class="text-secondary">
              记住我
            </el-checkbox>
            <a href="#" class="text-gold hover:text-gold-light text-sm transition-colors">
              忘记密码？
            </a>
          </div>

          <el-button
            type="primary"
            size="large"
            class="w-full"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="mt-6 text-center">
          <span class="text-secondary text-sm">还没有账号？</span>
          <router-link to="/register" class="text-gold hover:text-gold-light text-sm font-medium ml-1 transition-colors">
            立即注册
          </router-link>
        </div>
      </div>

      <!-- 返回首页 -->
      <div class="text-center mt-6">
        <router-link to="/" class="text-muted hover:text-secondary text-sm transition-colors flex items-center justify-center gap-1">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度在3-64之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 128, message: '密码长度在6-128之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    await authStore.login(form)
    ElMessage.success('登录成功')

    // 跳转到首页或之前的页面
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>
