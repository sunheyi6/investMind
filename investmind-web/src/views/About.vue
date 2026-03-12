<template>
  <div class="min-h-screen py-8 px-4">
    <div class="max-w-4xl mx-auto">
      <div class="text-center mb-12">
        <h1 class="text-3xl font-bold text-primary mb-3">关于 InvestMind</h1>
        <p class="text-muted">投资理念模型自我迭代的 AI 投研平台</p>
      </div>

      <div class="card-static p-6 mb-6">
        <h2 class="text-lg font-bold text-primary mb-4 flex items-center">
          <el-icon class="mr-2 text-gold"><InfoFilled /></el-icon>
          项目简介
        </h2>
        <div class="space-y-3 text-secondary text-sm leading-relaxed">
          <p>
            <strong class="text-primary">InvestMind（智投进化平台）</strong>是一个创新的 AI + 人工协同投研平台，
            旨在解决传统投资分析工具"一次性生成、无法持续优化"的痛点。
          </p>
          <p>
            平台的核心价值在于<strong class="text-primary">模型自我进化</strong>：通过人工对 AI 生成报告的修正，
            将优质内容向量化存储，后续 AI 生成时自动学习这些修正，使模型逐步贴合人工的投资理念和风格。
          </p>
        </div>
      </div>

      <div class="card-static p-6 mb-6">
        <h2 class="text-lg font-bold text-primary mb-6 flex items-center">
          <el-icon class="mr-2 text-gold"><Star /></el-icon>
          核心优势
        </h2>
        <div class="grid md:grid-cols-2 gap-4">
          <div class="flex items-start gap-3 p-4 rounded-lg bg-secondary">
            <div class="icon-box bg-blue/20">
              <el-icon class="text-blue"><Check /></el-icon>
            </div>
            <div>
              <h3 class="text-primary font-medium mb-1">持续进化</h3>
              <p class="text-muted text-sm">不同于传统 AI 工具的一次性输出，模型会随人工修正持续优化</p>
            </div>
          </div>
          <div class="flex items-start gap-3 p-4 rounded-lg bg-secondary">
            <div class="icon-box bg-purple/20">
              <el-icon class="text-purple"><Check /></el-icon>
            </div>
            <div>
              <h3 class="text-primary font-medium mb-1">风格迁移</h3>
              <p class="text-muted text-sm">AI 逐步学习并模仿人工的分析风格和表达方式</p>
            </div>
          </div>
          <div class="flex items-start gap-3 p-4 rounded-lg bg-secondary">
            <div class="icon-box bg-gold/20">
              <el-icon class="text-gold"><Check /></el-icon>
            </div>
            <div>
              <h3 class="text-primary font-medium mb-1">知识沉淀</h3>
              <p class="text-muted text-sm">所有修正内容结构化存储，形成可检索、可复用的知识库</p>
            </div>
          </div>
          <div class="flex items-start gap-3 p-4 rounded-lg bg-secondary">
            <div class="icon-box bg-green/20">
              <el-icon class="text-green"><Check /></el-icon>
            </div>
            <div>
              <h3 class="text-primary font-medium mb-1">人机协同</h3>
              <p class="text-muted text-sm">AI 负责快速生成，人工负责专业把关，发挥各自优势</p>
            </div>
          </div>
        </div>
      </div>

      <div class="card-static p-6 mb-6">
        <h2 class="text-lg font-bold text-primary mb-6 flex items-center">
          <el-icon class="mr-2 text-gold"><Platform /></el-icon>
          技术架构
        </h2>
        <div class="space-y-3">
          <div class="flex items-center justify-between py-3 border-b border-default">
            <span class="text-muted text-sm">后端框架</span>
            <span class="text-primary text-sm font-medium">Spring Boot 3.2 + MyBatis-Plus</span>
          </div>
          <div class="flex items-center justify-between py-3 border-b border-default">
            <span class="text-muted text-sm">前端框架</span>
            <span class="text-primary text-sm font-medium">Vue 3 + Element Plus + Tailwind CSS</span>
          </div>
          <div class="flex items-center justify-between py-3 border-b border-default">
            <span class="text-muted text-sm">数据库</span>
            <span class="text-primary text-sm font-medium">MySQL 8.0 + Milvus 向量数据库</span>
          </div>
          <div class="flex items-center justify-between py-3 border-b border-default">
            <span class="text-muted text-sm">AI 能力</span>
            <span class="text-primary text-sm font-medium">OpenAI API / Claude / 文心一言</span>
          </div>
          <div class="flex items-center justify-between py-3">
            <span class="text-muted text-sm">向量检索</span>
            <span class="text-primary text-sm font-medium">RAG (Retrieval-Augmented Generation)</span>
          </div>
        </div>
      </div>

      <div class="card-static p-6 text-center">
        <div class="w-12 h-12 mx-auto rounded-xl gradient-gold flex items-center justify-center mb-4">
          <el-icon class="text-primary text-2xl"><TrendCharts /></el-icon>
        </div>
        <h3 class="text-lg font-bold text-primary mb-1">InvestMind v1.0.0</h3>
        <p class="text-muted text-sm mb-3">智投进化平台</p>
        <p class="text-muted text-xs">© 2024 InvestMind. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
