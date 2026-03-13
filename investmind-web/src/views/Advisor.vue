<template>
  <div class="h-[calc(100vh-3.5rem)] px-3 py-3 md:px-4 md:py-4">
    <div class="h-full mx-auto max-w-7xl grid grid-cols-1 md:grid-cols-[280px_minmax(0,1fr)] gap-3 md:gap-4">
      <aside class="card-static h-full flex flex-col overflow-hidden">
        <div class="p-3 border-b border-default">
          <el-button class="w-full" type="primary" @click="createConversation">新建会话</el-button>
        </div>

        <div class="flex-1 overflow-y-auto p-2 space-y-2">
          <button
            v-for="session in conversations"
            :key="session.id"
            class="w-full text-left rounded-lg border transition-all px-3 py-2"
            :class="session.id === activeConversationId
              ? 'border-amber-400/60 bg-amber-500/10'
              : 'border-default bg-secondary/50 hover:bg-card hover:border-slate-500'"
            @click="selectConversation(session.id)"
          >
            <div class="text-sm font-medium text-primary truncate">{{ session.title }}</div>
            <div class="text-xs text-muted mt-1">{{ formatTime(session.updatedAt) }}</div>
            <div class="text-xs text-secondary mt-1">{{ session.messages.length }} 条消息</div>
          </button>
        </div>
      </aside>

      <section class="card-static h-full flex flex-col overflow-hidden">
        <header class="px-4 py-3 border-b border-default flex items-center justify-between gap-2">
          <div>
            <h2 class="text-base md:text-lg font-semibold text-primary">{{ activeConversation?.title || '智能问答' }}</h2>
            <p class="text-xs md:text-sm text-secondary">
              基于你的投资理念回答问题
              <router-link to="/philosophy" class="text-gold ml-1">去完善理念</router-link>
            </p>
            <div class="mt-2 inline-flex items-center gap-2 text-xs text-secondary">
              <span class="px-2 py-1 rounded-full bg-amber-500/15 text-amber-300 border border-amber-400/30">
                角色：{{ assistantRole.name }}
              </span>
              <span>{{ assistantRole.tagline }}</span>
            </div>
          </div>
          <el-button
            v-if="activeConversation"
            type="danger"
            plain
            size="small"
            @click="removeConversation(activeConversation.id)"
          >
            删除会话
          </el-button>
        </header>

        <div ref="messageScroller" class="flex-1 overflow-y-auto px-4 py-4 space-y-4">
          <template v-if="activeConversation && activeConversation.messages.length">
            <div
              v-for="msg in activeConversation.messages"
              :key="msg.id"
              class="flex"
              :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-[90%] md:max-w-[78%] rounded-2xl px-4 py-3 whitespace-pre-wrap leading-7 text-sm md:text-[15px]"
                :class="msg.role === 'user'
                  ? 'gradient-gold text-slate-900 font-medium'
                  : 'bg-card border border-default text-primary'"
              >
                {{ msg.content }}
                <div class="mt-2 text-[11px] opacity-70">{{ formatTime(msg.createdAt) }}</div>
              </div>
            </div>
          </template>
          <div v-else class="h-full flex items-center justify-center text-muted text-sm">
            先输入一个投资问题开始对话。
          </div>
        </div>

        <footer class="border-t border-default p-3 md:p-4">
          <div class="card p-2 md:p-3">
            <div
              v-if="addMode"
              class="mb-2 rounded-lg border border-amber-400/30 bg-amber-500/10 px-3 py-2 text-xs text-amber-200"
            >
              理念保存模式：将先进行逻辑校验，再由你确认后保存到投资理念库。
            </div>
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              :autosize="{ minRows: 2, maxRows: 8 }"
              maxlength="1000"
              show-word-limit
              :disabled="asking"
              placeholder="输入问题，或用 /add 你的观点 保存到投资理念（Enter 发送）"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <div class="mt-3 flex items-center justify-end">
              <el-button type="primary" :loading="asking" @click="sendMessage">发送</el-button>
            </div>
          </div>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi } from '../api/ai'
import { userApi } from '../api/user'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const assistantRole = {
  name: 'InvestMind 投研教练',
  tagline: '先基于你的投资体系，再给出可执行建议'
}

const asking = ref(false)
const inputText = ref('')
const conversations = ref([])
const activeConversationId = ref('')
const messageScroller = ref(null)

const storageKey = computed(() => {
  const user = authStore.userInfo || {}
  const identity = user.id || user.username || 'guest'
  return `investmind_chat_sessions_${identity}`
})

const activeConversation = computed(() =>
  conversations.value.find((item) => item.id === activeConversationId.value)
)
const addMode = computed(() => isAddCommand(inputText.value))

const makeId = () => `${Date.now()}_${Math.random().toString(16).slice(2, 8)}`

const deriveTitle = (text) => {
  const content = (text || '').trim()
  if (!content) {
    return '新会话'
  }
  return content.length > 18 ? `${content.slice(0, 18)}...` : content
}

const isAddCommand = (text) => /^\/add(\s+|$)/i.test((text || '').trim())

const extractAddContent = (text) => (text || '').replace(/^\/add\s*/i, '').trim()

const extractJsonObject = (raw) => {
  const text = (raw || '').trim()
  const start = text.indexOf('{')
  const end = text.lastIndexOf('}')
  if (start >= 0 && end > start) {
    return text.slice(start, end + 1)
  }
  return ''
}

const formatTime = (value) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date
    .getMinutes()
    .toString()
    .padStart(2, '0')}`
}

const saveConversations = () => {
  localStorage.setItem(storageKey.value, JSON.stringify(conversations.value))
}

const scrollToBottom = async () => {
  await nextTick()
  const el = messageScroller.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

const appendAssistantMessage = (session, content) => {
  session.messages.push({
    id: makeId(),
    role: 'assistant',
    content,
    createdAt: new Date().toISOString()
  })
  session.updatedAt = new Date().toISOString()
  saveConversations()
  scrollToBottom()
}

const heuristicReview = (content) => {
  const issues = []
  const t = content || ''
  if (t.length < 8) {
    issues.push('观点过短，信息量不足，建议补充具体标准。')
  }
  if (/(稳赚|保本|无风险|100%|必涨|躺赢)/i.test(t)) {
    issues.push('包含绝对收益或无风险表述，不符合审慎投资逻辑。')
  }
  if (/(梭哈|满仓|all in|加杠杆|借钱炒股)/i.test(t)) {
    issues.push('存在过度集中或高杠杆倾向，风险过高。')
  }
  if (/短线/.test(t) && /长期/.test(t)) {
    issues.push('同时出现短线和长期目标，时间框架可能冲突。')
  }
  return {
    issues,
    suggestion: issues.length ? '建议补充风险边界、仓位规则和时间周期后再保存。' : '观点基本完整。'
  }
}

const reviewAddContent = async (content) => {
  const local = heuristicReview(content)
  try {
    const prompt = `
你是投资理念审阅助手。请审阅下面这条投资观点是否符合一般投资逻辑与风险常识。
只输出 JSON，不要输出其他内容，格式如下：
{"status":"ok|warn","issues":["问题1","问题2"],"suggestion":"一句建议"}

审阅维度：
1. 是否包含绝对收益承诺
2. 是否存在明显自相矛盾
3. 是否存在高风险且无风控约束的表达
4. 是否缺少关键执行信息（仓位、期限、止损/退出）

投资观点：
${content}
    `.trim()

    const res = await aiApi.chat(prompt)
    const raw = res?.data?.answer || ''
    const jsonText = extractJsonObject(raw)
    if (!jsonText) {
      return local
    }
    const parsed = JSON.parse(jsonText)
    const aiIssues = Array.isArray(parsed?.issues) ? parsed.issues.filter(Boolean) : []
    const merged = [...local.issues, ...aiIssues].filter((v, idx, arr) => arr.indexOf(v) === idx)
    return {
      issues: merged,
      suggestion: parsed?.suggestion || local.suggestion
    }
  } catch {
    return local
  }
}

const createConversation = () => {
  const now = new Date().toISOString()
  const session = {
    id: makeId(),
    title: '与投研教练对话',
    createdAt: now,
    updatedAt: now,
    messages: [
      {
        id: makeId(),
        role: 'assistant',
        content:
          `你好，我是${assistantRole.name}。\n\n我会严格基于你的投资理念来回答，不做脱离你体系的建议。\n你可以直接问我：\n1. 本周如何筛选候选股\n2. 当前仓位是否需要调整\n3. 某只股票是否符合你的买入标准`,
        createdAt: now
      }
    ]
  }
  conversations.value.unshift(session)
  activeConversationId.value = session.id
  saveConversations()
}

const selectConversation = (id) => {
  activeConversationId.value = id
  scrollToBottom()
}

const removeConversation = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除当前会话吗？删除后不可恢复。', '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  const list = conversations.value.filter((item) => item.id !== id)
  conversations.value = list

  if (!list.length) {
    createConversation()
    return
  }

  if (activeConversationId.value === id) {
    activeConversationId.value = list[0].id
  }

  saveConversations()
}

const loadConversations = () => {
  try {
    const raw = localStorage.getItem(storageKey.value)
    if (!raw) {
      createConversation()
      return
    }

    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed) || !parsed.length) {
      createConversation()
      return
    }

    conversations.value = parsed
    activeConversationId.value = parsed[0].id
  } catch {
    createConversation()
  }
}

const sendMessage = async () => {
  const question = inputText.value.trim()
  if (!question || asking.value) {
    return
  }

  if (!activeConversation.value) {
    createConversation()
  }

  const now = new Date().toISOString()
  const userMessage = {
    id: makeId(),
    role: 'user',
    content: question,
    createdAt: now
  }

  const session = activeConversation.value
  session.messages.push(userMessage)
  if (session.title === '与投研教练对话') {
    session.title = deriveTitle(question)
  }
  session.updatedAt = now
  inputText.value = ''
  saveConversations()
  scrollToBottom()

  asking.value = true
  try {
    if (isAddCommand(question)) {
      const content = extractAddContent(question)
      if (!content) {
        appendAssistantMessage(
          session,
          '请在 `/add` 后补充你的投资观点，例如：`/add 我偏好低估值高ROE，回撤超过10%减仓`'
        )
        ElMessage.warning('请在 /add 后输入要保存的观点')
        return
      }

      const review = await reviewAddContent(content)
      if (review.issues.length) {
        appendAssistantMessage(
          session,
          `检测到这条观点存在待确认问题：\n- ${review.issues.join('\n- ')}\n\n建议：${review.suggestion}`
        )
        try {
          await ElMessageBox.confirm(
            '这条观点存在潜在逻辑或风险问题。是否确认继续进入保存确认？',
            '发现待确认问题',
            {
              type: 'warning',
              confirmButtonText: '继续',
              cancelButtonText: '先修改'
            }
          )
        } catch {
          return
        }
      }

      try {
        await ElMessageBox.confirm('确认将这条观点保存到你的投资理念中吗？', '确认保存', {
          type: 'info',
          confirmButtonText: '确认保存',
          cancelButtonText: '取消'
        })
      } catch {
        appendAssistantMessage(session, '已取消保存。你可以修改后再次使用 /add 提交。')
        return
      }

      const res = await userApi.ingestPhilosophy(content)
      const version = res?.data?.versionNo
      const hint = version
        ? `已将这条观点存储到你的投资理念中，当前版本 v${version}。你可以继续使用 /add 持续迭代。`
        : '已将这条观点存储到你的投资理念中。你可以继续使用 /add 持续迭代。'
      appendAssistantMessage(session, hint)
      ElMessage.success('已存储到你的投资理念')
      return
    }

    const res = await aiApi.chat(question)
    const answer = res?.data?.answer || '暂时没有拿到回答，请稍后再试。'
    appendAssistantMessage(session, answer)
  } catch (error) {
    appendAssistantMessage(session, '请求失败，请稍后重试。')
    ElMessage.error('提问失败，请稍后再试')
  } finally {
    asking.value = false
  }
}

onMounted(() => {
  loadConversations()
  scrollToBottom()
})

watch(
  () => activeConversation.value?.messages?.length,
  () => {
    scrollToBottom()
  }
)
</script>
