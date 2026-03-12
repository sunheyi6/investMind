<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <!-- Logo区域 -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 rounded-2xl gradient-gold flex items-center justify-center mx-auto mb-4 shadow-lg">
          <el-icon class="text-primary text-3xl"><TrendCharts /></el-icon>
        </div>
        <h1 class="text-2xl font-bold text-primary mb-2">创建账号</h1>
        <p class="text-secondary">加入 InvestMind 智投进化平台</p>
      </div>

      <!-- 注册表单 -->
      <div class="card-glass rounded-2xl p-8 shadow-xl">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="昵称（可选）"
              prefix-icon="Avatar"
              clearable
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="邮箱（可选）"
              prefix-icon="Message"
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

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="form.agreement" class="text-secondary">
              我已阅读并同意
              <a href="#" class="text-gold hover:text-gold-light">用户协议</a>
              和
              <a href="#" class="text-gold hover:text-gold-light">隐私政策</a>
            </el-checkbox>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="w-full"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form>

        <div class="mt-6 text-center">
          <span class="text-secondary text-sm">已有账号？</span>
          <router-link to="/login" class="text-gold hover:text-gold-light text-sm font-medium ml-1 transition-colors">
            立即登录
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

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 自定义确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 自定义协议验证
const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度在3-64之间', trigger: 'blur' }
  ],
  nickname: [
    { max: 64, message: '昵称长度不能超过64', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 128, message: '密码长度在6-128之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    // 构造注册数据
    const registerData = {
      username: form.username,
      password: form.password,
      email: form.email || null,
      nickname: form.nickname || null
    }

    await authStore.register(registerData)
    ElMessage.success('注册成功')

    // 跳转到首页
    router.push('/')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>
