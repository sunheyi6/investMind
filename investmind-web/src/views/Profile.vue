<template>
  <div class="min-h-screen bg-primary">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-primary mb-2">个人中心</h1>
        <p class="text-secondary">管理您的账户信息和投资偏好</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左侧：用户信息卡片 -->
        <div class="lg:col-span-1">
          <div class="card-glass rounded-2xl p-6">
            <div class="text-center">
              <div class="w-24 h-24 rounded-full gradient-gold flex items-center justify-center mx-auto mb-4">
                <el-icon class="text-primary text-4xl"><User /></el-icon>
              </div>
              <h3 class="text-xl font-bold text-primary mb-1">{{ userInfo?.nickname || userInfo?.username }}</h3>
              <p class="text-secondary text-sm">{{ userInfo?.email || '未设置邮箱' }}</p>
              <div class="mt-4">
                <el-tag :type="userInfo?.userType === 'ADMIN' ? 'danger' : 'success'" effect="dark">
                  {{ userInfo?.userType === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-tag>
              </div>
            </div>

            <div class="mt-6 pt-6 border-t border-default">
              <div class="flex justify-between items-center mb-3">
                <span class="text-secondary text-sm">用户名</span>
                <span class="text-primary">{{ userInfo?.username }}</span>
              </div>
              <div class="flex justify-between items-center mb-3">
                <span class="text-secondary text-sm">最后登录</span>
                <span class="text-primary text-sm">{{ formatDate(userInfo?.lastLoginTime) }}</span>
              </div>
            </div>

            <div class="mt-6 space-y-2">
              <router-link to="/philosophy">
                <el-button class="w-full" type="primary" plain>
                  <el-icon class="mr-1"><Edit /></el-icon>
                  配置投资理念
                </el-button>
              </router-link>
              <router-link to="/reports">
                <el-button class="w-full">
                  <el-icon class="mr-1"><Document /></el-icon>
                  查看报告
                </el-button>
              </router-link>
            </div>
          </div>
        </div>

        <!-- 右侧：编辑表单 -->
        <div class="lg:col-span-2">
          <el-tabs v-model="activeTab" type="border-card" class="profile-tabs">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="profile">
              <div class="card-glass rounded-2xl p-6">
                <h3 class="text-lg font-semibold text-primary mb-6">编辑资料</h3>

                <el-form
                  ref="profileFormRef"
                  :model="profileForm"
                  :rules="profileRules"
                  label-position="top"
                  @submit.prevent="handleUpdateProfile"
                >
                  <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="profileForm.nickname" placeholder="设置您的昵称" />
                  </el-form-item>

                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="profileForm.email" placeholder="设置您的邮箱" />
                  </el-form-item>

                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="profileForm.phone" placeholder="设置您的手机号" />
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">
                      保存修改
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <div class="card-glass rounded-2xl p-6">
                <h3 class="text-lg font-semibold text-primary mb-6">修改密码</h3>

                <el-form
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  label-position="top"
                  @submit.prevent="handleChangePassword"
                >
                  <el-form-item label="当前密码" prop="oldPassword">
                    <el-input
                      v-model="passwordForm.oldPassword"
                      type="password"
                      placeholder="输入当前密码"
                      show-password
                    />
                  </el-form-item>

                  <el-form-item label="新密码" prop="newPassword">
                    <el-input
                      v-model="passwordForm.newPassword"
                      type="password"
                      placeholder="输入新密码"
                      show-password
                    />
                  </el-form-item>

                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      placeholder="确认新密码"
                      show-password
                    />
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                      修改密码
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth.js'
import { userApi } from '../api/user'
import dayjs from 'dayjs'

const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)
const activeTab = ref('profile')

// 资料表单
const profileFormRef = ref(null)
const profileLoading = ref(false)

const profileForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const profileRules = {
  nickname: [{ max: 64, message: '昵称长度不能超过64', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ max: 20, message: '手机号长度不能超过20', trigger: 'blur' }]
}

// 密码表单
const passwordFormRef = ref(null)
const passwordLoading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 128, message: '密码长度在6-128之间', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 128, message: '密码长度在6-128之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 获取用户资料
const fetchProfile = async () => {
  try {
    const res = await userApi.getProfile()
    if (res.data) {
      profileForm.nickname = res.data.nickname || ''
      profileForm.email = res.data.email || ''
      profileForm.phone = res.data.phone || ''
    }
  } catch (error) {
    console.error('获取用户资料失败:', error)
  }
}

// 更新资料
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    profileLoading.value = true

    const res = await userApi.updateProfile(profileForm)
    if (res.code === 200) {
      ElMessage.success('资料更新成功')
      // 更新store中的用户信息
      authStore.user.nickname = profileForm.nickname
      authStore.user.email = profileForm.email
    }
  } catch (error) {
    console.error('更新资料失败:', error)
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功，请重新登录')
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '从未登录'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-tabs :deep(.el-tabs__header) {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-default);
}

.profile-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--accent-gold);
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--accent-gold);
}
</style>
