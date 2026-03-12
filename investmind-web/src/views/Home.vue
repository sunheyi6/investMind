<template>
  <div>
    <section class="relative min-h-[90vh] flex items-center px-4 overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-br from-primary via-secondary to-primary"></div>
      <div class="absolute inset-0 opacity-30">
        <div class="absolute top-20 left-10 w-72 h-72 bg-gold/10 rounded-full blur-3xl"></div>
        <div class="absolute bottom-20 right-10 w-96 h-96 bg-green/10 rounded-full blur-3xl"></div>
      </div>

      <div class="relative z-10 max-w-6xl mx-auto w-full">
        <div class="flex flex-col lg:flex-row items-center gap-12">
          <div class="flex-1 text-center lg:text-left">
            <div class="inline-flex items-center px-3 py-1.5 rounded-full bg-card border border-default mb-6">
              <span class="w-1.5 h-1.5 rounded-full bg-green mr-2"></span>
              <span class="text-secondary text-sm">AI 投研 2.0 时代</span>
            </div>

            <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-primary mb-6 leading-tight">
              InvestMind
              <span class="block text-gradient-gold mt-2">智投进化平台</span>
            </h1>

            <p class="text-lg md:text-xl text-secondary max-w-2xl mb-8 leading-relaxed">
              投资理念模型<strong class="text-primary">自我迭代</strong>的 AI 投研平台
              <br />
              <span class="text-muted">AI 生成 · 人工修正 · 双存储 · 持续进化</span>
            </p>

            <div class="flex flex-col sm:flex-row items-center gap-4">
              <router-link to="/reports">
                <el-button type="primary" size="large" class="px-8">
                  <el-icon class="mr-2"><Document /></el-icon>
                  开始体验
                </el-button>
              </router-link>
              <el-button 
                size="large" 
                class="btn-secondary px-8"
                @click="scrollToFeatures"
              >
                了解更多
                <el-icon class="ml-2"><ArrowDown /></el-icon>
              </el-button>
            </div>
          </div>

          <div class="flex-1 w-full max-w-md">
            <div class="grid grid-cols-2 gap-4">
              <div class="stat-card">
                <div class="text-2xl font-bold text-gold mb-1">AI+人工</div>
                <div class="text-muted text-sm">协同模式</div>
              </div>
              <div class="stat-card">
                <div class="text-2xl font-bold text-green mb-1">双存储</div>
                <div class="text-muted text-sm">数据架构</div>
              </div>
              <div class="stat-card">
                <div class="text-2xl font-bold text-blue mb-1">RAG</div>
                <div class="text-muted text-sm">增强生成</div>
              </div>
              <div class="stat-card">
                <div class="text-2xl font-bold text-purple mb-1">持续进化</div>
                <div class="text-muted text-sm">自我迭代</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="features" class="py-20 px-4">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-primary mb-4">为什么选择 InvestMind？</h2>
          <p class="text-secondary text-lg max-w-2xl mx-auto">
            突破传统投研工具的局限，实现 AI 与人工的深度融合，让投资分析模型持续进化
          </p>
        </div>

        <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-16">
          <div 
            v-for="(feature, index) in features" 
            :key="index"
            class="card p-6 cursor-pointer"
          >
            <div class="icon-box-lg mb-4" :class="feature.bgClass">
              <el-icon class="text-white text-xl">
                <component :is="feature.icon" />
              </el-icon>
            </div>

            <h3 class="text-lg font-semibold text-primary mb-2">{{ feature.title }}</h3>
            <p class="text-secondary text-sm leading-relaxed mb-4">{{ feature.description }}</p>

            <div class="flex flex-wrap gap-2">
              <span 
                v-for="tag in feature.tags" 
                :key="tag"
                class="tag tag-gray"
              >
                {{ tag }}
              </span>
            </div>
          </div>
        </div>

        <div class="card-static p-8">
          <h3 class="text-xl font-bold text-primary text-center mb-10">核心工作流程</h3>
          <div class="flex flex-col md:flex-row items-center justify-between gap-6">
            <div v-for="(step, index) in workflow" :key="index" class="flex-1 text-center">
              <div class="relative">
                <div class="w-14 h-14 mx-auto rounded-xl flex items-center justify-center text-primary text-xl font-bold mb-3"
                     :class="step.bgClass">
                  {{ index + 1 }}
                </div>
                <div v-if="index < workflow.length - 1" 
                     class="hidden md:block absolute top-7 left-[60%] w-[80%] h-px bg-border-color">
                </div>
              </div>
              <h4 class="text-base font-semibold text-primary mb-1">{{ step.title }}</h4>
              <p class="text-muted text-sm">{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="py-20 px-4">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-primary mb-4">强大的技术架构</h2>
          <p class="text-secondary text-lg">采用业界领先的技术栈，确保系统的稳定性和扩展性</p>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <div class="card-static p-6">
            <h3 class="text-lg font-bold text-primary mb-6 flex items-center">
              <el-icon class="mr-2 text-purple"><Collection /></el-icon>
              双存储架构
            </h3>
            <div class="space-y-4">
              <div class="flex items-start gap-4 p-4 rounded-lg bg-secondary">
                <div class="icon-box bg-blue/20">
                  <el-icon class="text-blue text-lg"><Coin /></el-icon>
                </div>
                <div>
                  <h4 class="text-primary font-medium mb-1">MySQL 结构化存储</h4>
                  <p class="text-muted text-sm">报告日期、AI原文、人工修正版、学习状态等结构化数据管理</p>
                </div>
              </div>
              <div class="flex items-start gap-4 p-4 rounded-lg bg-secondary">
                <div class="icon-box bg-purple/20">
                  <el-icon class="text-purple text-lg"><DataAnalysis /></el-icon>
                </div>
                <div>
                  <h4 class="text-primary font-medium mb-1">Milvus 向量数据库</h4>
                  <p class="text-muted text-sm">人工修正的优质内容向量化存储，支撑 AI 检索增强生成</p>
                </div>
              </div>
            </div>
          </div>

          <div class="card-static p-6">
            <h3 class="text-lg font-bold text-primary mb-6 flex items-center">
              <el-icon class="mr-2 text-gold"><Cpu /></el-icon>
              RAG 增强生成
            </h3>
            <div class="space-y-4">
              <div class="flex items-start gap-4 p-4 rounded-lg bg-secondary">
                <div class="icon-box bg-gold/20">
                  <el-icon class="text-gold text-lg"><Search /></el-icon>
                </div>
                <div>
                  <h4 class="text-primary font-medium mb-1">相似内容检索</h4>
                  <p class="text-muted text-sm">生成新报告前，自动检索历史优质修正内容</p>
                </div>
              </div>
              <div class="flex items-start gap-4 p-4 rounded-lg bg-secondary">
                <div class="icon-box bg-green/20">
                  <el-icon class="text-green text-lg"><MagicStick /></el-icon>
                </div>
                <div>
                  <h4 class="text-primary font-medium mb-1">提示词增强</h4>
                  <p class="text-muted text-sm">将历史最优逻辑融入提示词，生成更贴合的内容</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="py-20 px-4">
      <div class="max-w-4xl mx-auto">
        <div class="card-static p-10 text-center relative overflow-hidden">
          <div class="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-gold/5 rounded-full blur-3xl"></div>
          
          <div class="relative z-10">
            <h2 class="text-2xl md:text-3xl font-bold text-primary mb-4">
              开始您的智投进化之旅
            </h2>
            <p class="text-secondary mb-8 max-w-xl mx-auto">
              让 AI 成为您的投资研究助手，通过持续的人机协同，
              打造专属于您的投资分析模型
            </p>
            <router-link to="/reports">
              <el-button type="primary" size="large" class="px-10">
                <el-icon class="mr-2"><Rocket /></el-icon>
                立即开始使用
              </el-button>
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
const features = [
  {
    icon: 'MagicStick',
    bgClass: 'gradient-blue',
    title: 'AI 智能生成',
    description: '每日自动生成专业的投资分析报告，基于大语言模型，内容专业、结构清晰。',
    tags: ['Markdown格式', '500-800字', '专业分析']
  },
  {
    icon: 'EditPen',
    bgClass: 'gradient-purple',
    title: '人工精准修正',
    description: '对 AI 生成的报告进行内容修正和优化，注入您的投资理念和判断。',
    tags: ['内容修正', '质量评分', '修正日志']
  },
  {
    icon: 'DataAnalysis',
    bgClass: 'gradient-gold',
    title: '向量化存储',
    description: '人工修正后的优质内容自动向量化，存入 Milvus 向量数据库。',
    tags: ['Embedding', '向量检索', 'Milvus']
  },
  {
    icon: 'Refresh',
    bgClass: 'gradient-green',
    title: '模型自我进化',
    description: 'AI 生成新报告时，自动检索历史优质内容，实现 RAG 增强生成。',
    tags: ['RAG', '持续学习', '风格迁移']
  },
  {
    icon: 'Collection',
    bgClass: 'gradient-blue',
    title: '双存储架构',
    description: 'MySQL 管理结构化数据，向量库存储语义信息，兼顾查询效率。',
    tags: ['MySQL', '双数据库', '高效查询']
  },
  {
    icon: 'TrendCharts',
    bgClass: 'gradient-purple',
    title: '学习进度追踪',
    description: '实时监控模型学习进度，了解多少优质内容已被向量化。',
    tags: ['可视化', '进度追踪', '数据洞察']
  }
]

const workflow = [
  {
    bgClass: 'gradient-blue',
    title: 'AI 生成',
    desc: '每日自动生成投资报告'
  },
  {
    bgClass: 'gradient-purple',
    title: '人工修正',
    desc: '专家修正优化报告内容'
  },
  {
    bgClass: 'gradient-gold',
    title: '向量化',
    desc: '优质内容存入向量库'
  },
  {
    bgClass: 'gradient-green',
    title: '模型学习',
    desc: 'AI 检索学习持续进化'
  }
]

const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}
</script>
