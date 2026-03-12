# InvestMind Web - 智投进化平台前端

> Vue 3 + Element Plus + Gemini 风格设计的现代化投研平台前端

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - 基于 Vue 3 的组件库
- **Tailwind CSS** - 原子化 CSS 框架
- **Pinia** - Vue 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP 客户端

## 设计风格

采用 **Google Gemini** 风格设计：

- 🎨 **渐变背景** - 蓝紫色动态渐变动画
- 💎 **玻璃拟态** - 毛玻璃效果卡片 (Glassmorphism)
- ✨ **圆角设计** - 大圆角 (rounded-2xl/3xl) 现代感
- 🌟 **光晕效果** - 柔和的光影和发光效果
- 🎯 **简洁现代** - 干净的布局，突出内容

## 项目结构

```
investmind-web/
├── public/
│   └── favicon.svg           # 网站图标
├── src/
│   ├── api/
│   │   └── report.js         # API 接口封装
│   ├── assets/
│   │   └── style.css         # 全局样式（Gemini风格）
│   ├── components/           # 组件目录
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── views/
│   │   ├── Home.vue          # 首页（产品优点展示）
│   │   ├── Reports.vue       # 报告列表
│   │   ├── ReportDetail.vue  # 报告详情/修正
│   │   └── About.vue         # 关于页面
│   ├── App.vue               # 根组件
│   └── main.js               # 入口文件
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
└── postcss.config.js
```

## 核心页面

### 1. 首页 (Home.vue)
展示产品优点，Gemini 风格设计：
- **Hero Section** - 动态渐变背景 + 浮动动画
- **产品优点卡片** - 6大核心优势展示
- **工作流程图** - AI生成 → 人工修正 → 向量化 → 模型学习
- **技术架构** - 双存储架构和RAG增强说明

### 2. 报告中心 (Reports.vue)
- 报告列表展示
- 统计卡片（总报告/已修正/已向量化/已学习）
- 筛选功能（类型、日期范围）
- 向量管理抽屉（批量向量化、相似内容检索）

### 3. 报告详情 (ReportDetail.vue)
- AI生成内容与人工修正内容对比展示
- 在线修正功能（弹窗编辑）
- 向量化操作
- 元信息展示（时间、评分、标签）

## 快速开始

### 安装依赖

```bash
cd investmind-web
npm install
```

### 开发环境

```bash
npm run dev
```

访问 `http://localhost:3000`

### 生产构建

```bash
npm run build
```

构建产物在 `dist` 目录

## 后端 API 配置

在 `vite.config.js` 中配置代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true
    }
  }
}
```

## 界面预览

### 首页设计特点
1. **动态渐变背景** - 蓝紫色渐变动画
2. **浮动装饰球** - 背景模糊圆形，营造深度感
3. **玻璃卡片** - 半透明 + 模糊效果
4. **渐变文字** - 标题使用渐变色
5. **悬停动效** - 卡片悬停上浮 + 阴影增强

### 报告中心特点
1. **统计面板** - 4个关键指标卡片
2. **玻璃筛选栏** - 透明背景筛选器
3. **状态标签** - 已修正/已向量化/已学习标识
4. **向量管理** - 抽屉式侧边栏

### 详情页特点
1. **左右对比** - AI内容和人工修正并排展示
2. **修正弹窗** - 集成评分、标签、原因输入
3. **操作按钮** - 向量化、重新修正等操作

## 主题色

```css
/* 渐变色 */
--gradient-primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
--gradient-gemini: linear-gradient(135deg, #667eea, #764ba2, #f093fb, #4facfe);

/* 主色调 */
--primary-blue: #4285f4;
--primary-purple: #9c27b0;
--primary-pink: #e91e63;
--primary-cyan: #00bcd4;
```

## 浏览器支持

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 许可证

MIT License
