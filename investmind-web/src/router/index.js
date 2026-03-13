import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import Home from '../views/Home.vue'
import Reports from '../views/Reports.vue'
import ReportDetail from '../views/ReportDetail.vue'
import About from '../views/About.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Philosophy from '../views/Philosophy.vue'
import Advisor from '../views/Advisor.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '首页' }
  },
  {
    path: '/reports',
    name: 'Reports',
    component: Reports,
    meta: { title: '报告列表', requiresAuth: true }
  },
  {
    path: '/report/:id',
    name: 'ReportDetail',
    component: ReportDetail,
    meta: { title: '报告详情', requiresAuth: true },
    props: true
  },
  {
    path: '/advisor',
    name: 'Advisor',
    component: Advisor,
    meta: { title: '智投问答', requiresAuth: true }
  },
  {
    path: '/about',
    name: 'About',
    component: About,
    meta: { title: '关于' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', guestOnly: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/philosophy',
    name: 'Philosophy',
    component: Philosophy,
    meta: { title: '投资理念', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - InvestMind` : 'InvestMind 智投进化平台'

  const authStore = useAuthStore()

  // 需要登录的页面
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }

  // 仅游客可访问的页面（登录后不能访问）
  if (to.meta.guestOnly && authStore.isLoggedIn) {
    next('/advisor')
    return
  }

  // 登录后不再进入首页/关于，统一进入问答页
  if (authStore.isLoggedIn && (to.path === '/' || to.path === '/about')) {
    next('/advisor')
    return
  }

  next()
})

export default router
