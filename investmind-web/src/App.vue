<template>
  <div class="min-h-screen bg-primary">
    <nav class="fixed top-0 left-0 right-0 z-50 bg-secondary border-b border-default">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-14">
          <router-link :to="isLoggedIn ? '/advisor' : '/'" class="flex items-center space-x-3">
            <div class="w-8 h-8 rounded-lg gradient-gold flex items-center justify-center">
              <el-icon class="text-primary text-lg"><TrendCharts /></el-icon>
            </div>
            <span class="text-lg font-semibold text-primary">InvestMind</span>
          </router-link>

          <div class="hidden md:flex items-center space-x-1">
            <router-link
              v-for="item in filteredNavItems"
              :key="item.path"
              :to="item.path"
              class="px-4 py-2 rounded-lg text-secondary hover:text-primary hover:bg-card transition-all duration-200"
              :class="{ 'text-gold bg-card': $route.path === item.path }"
            >
              {{ item.name }}
            </router-link>
          </div>

          <!-- 用户菜单 -->
          <div class="flex items-center gap-4">
            <template v-if="isLoggedIn">
              <el-dropdown trigger="click" class="hidden md:block">
                <div class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-card cursor-pointer transition-all">
                  <div class="w-8 h-8 rounded-full gradient-gold flex items-center justify-center">
                    <el-icon class="text-primary text-lg"><User /></el-icon>
                  </div>
                  <span class="text-primary text-sm font-medium">{{ userInfo?.nickname || userInfo?.username }}</span>
                  <el-icon class="text-secondary"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="$router.push('/philosophy')">
                      <el-icon class="mr-2"><Edit /></el-icon>
                      投资理念
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">
                      <el-icon class="mr-2"><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <div class="hidden md:flex items-center gap-2">
                <router-link to="/login">
                  <el-button type="primary" text>登录</el-button>
                </router-link>
                <router-link to="/register">
                  <el-button type="primary">注册</el-button>
                </router-link>
              </div>
            </template>

            <button
              class="md:hidden p-2 rounded-lg text-secondary hover:text-primary hover:bg-card"
              @click="mobileMenuOpen = !mobileMenuOpen"
            >
              <el-icon class="text-xl"><Menu /></el-icon>
            </button>
          </div>
        </div>

        <div v-show="mobileMenuOpen" class="md:hidden py-3 space-y-1 border-t border-default">
          <router-link
            v-for="item in filteredNavItems"
            :key="item.path"
            :to="item.path"
            class="block px-4 py-2 rounded-lg text-secondary hover:text-primary hover:bg-card transition-all"
            @click="mobileMenuOpen = false"
          >
            {{ item.name }}
          </router-link>

          <!-- 移动端用户菜单 -->
          <div v-if="isLoggedIn" class="border-t border-default mt-3 pt-3">
            <div class="px-4 py-2 text-primary font-medium">
              {{ userInfo?.nickname || userInfo?.username }}
            </div>
            <router-link
              to="/philosophy"
              class="block px-4 py-2 rounded-lg text-secondary hover:text-primary hover:bg-card transition-all"
              @click="mobileMenuOpen = false"
            >
              投资理念
            </router-link>
            <button
              class="w-full text-left px-4 py-2 rounded-lg text-secondary hover:text-primary hover:bg-card transition-all"
              @click="handleLogout(); mobileMenuOpen = false"
            >
              退出登录
            </button>
          </div>
          <div v-else class="border-t border-default mt-3 pt-3 space-y-1">
            <router-link
              to="/login"
              class="block px-4 py-2 rounded-lg text-secondary hover:text-primary hover:bg-card transition-all"
              @click="mobileMenuOpen = false"
            >
              登录
            </router-link>
            <router-link
              to="/register"
              class="block px-4 py-2 rounded-lg text-gold hover:text-gold-light hover:bg-card transition-all"
              @click="mobileMenuOpen = false"
            >
              注册
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <main class="pt-14">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer v-if="showFooter" class="border-t border-default mt-16">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
          <div class="flex items-center space-x-3">
            <div class="w-6 h-6 rounded gradient-gold flex items-center justify-center">
              <el-icon class="text-primary text-sm"><TrendCharts /></el-icon>
            </div>
            <span class="text-muted text-sm">© 2024 InvestMind · 智投进化平台</span>
          </div>
          <div class="flex items-center space-x-4 text-muted text-sm">
            <span>AI + 人工协同</span>
            <span class="w-1 h-1 rounded-full bg-muted"></span>
            <span>持续自我优化</span>
            <span class="w-1 h-1 rounded-full bg-muted"></span>
            <span>模型自我演变</span>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './stores/auth.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const mobileMenuOpen = ref(false)

const isLoggedIn = computed(() => authStore.isLoggedIn)
const userInfo = computed(() => authStore.userInfo)

const navItems = [
  { name: '智能问答', path: '/advisor', public: false },
  { name: '我的投资理念', path: '/philosophy', public: false }
]

// 过滤导航项（根据登录状态）
const filteredNavItems = computed(() => {
  return navItems.filter(item => item.public || isLoggedIn.value)
})

const showFooter = computed(() => route.path !== '/advisor')

const handleLogout = async () => {
  try {
    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}
</script>
