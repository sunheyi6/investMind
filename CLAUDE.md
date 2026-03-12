# CLAUDE.md - InvestMind 智投进化平台

> 本文件为 Claude Code 提供项目上下文，帮助 AI 助手更好地理解和协助开发。

## 项目概述

**InvestMind（智投进化平台）** 是一个 AI + 人工协同的投资研究平台，核心目标是让投资分析模型能够持续自我优化、逐步贴合人工的投资理念。

核心流程：
```
AI生成报告 → 人工修正 → 数据双存储 → 模型迭代学习
     ↓            ↓          ↓               ↓
MySQL存储    MySQL+向量库   向量数据库      RAG增强生成
```

## 技术栈

### 后端 (investmind/)
- **框架**: Spring Boot 3.2.0
- **JDK**: Java 17
- **ORM**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0
- **向量数据库**: Milvus 2.x
- **构建工具**: Maven
- **其他**: Lombok, Fastjson2, HttpClient5

### 前端 (investmind-web/)
- **框架**: Vue 3.4 + Composition API
- **构建工具**: Vite 5
- **UI 组件库**: Element Plus 2.5
- **CSS 框架**: Tailwind CSS 3.4
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **HTTP 客户端**: Axios
- **工具库**: Dayjs, Marked, DOMPurify

## 目录结构

```
InvestMind/
├── CLAUDE.md                 # 本项目文件
├── investmind/               # Java 后端
│   ├── src/main/java/com/investmind/
│   │   ├── InvestMindApplication.java
│   │   ├── config/           # 配置类
│   │   ├── controller/       # REST API 控制器
│   │   ├── service/          # 业务逻辑层
│   │   ├── model/            # 实体类
│   │   ├── repository/       # Mapper 接口
│   │   └── dto/              # 数据传输对象
│   ├── src/main/resources/
│   │   ├── application.yml   # 应用配置
│   │   ├── db/schema.sql     # 数据库初始化脚本
│   │   └── mapper/           # MyBatis XML
│   └── pom.xml
├── investmind-web/           # Vue 前端
│   ├── src/
│   │   ├── api/              # API 接口封装
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 公共组件
│   │   ├── router/           # 路由配置
│   │   ├── views/            # 页面视图
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
└── .claude/                  # Claude Code 配置
```

## 开发规范

### 后端规范
- 包名: `com.investmind.*`
- 实体类使用 Lombok 注解简化代码
- 数据库实体使用驼峰命名，对应下划线字段名
- Controller 层返回统一格式 `ApiResponse<T>`
- 服务层接口与实现分离，实现类以 `Impl` 结尾

### 前端规范
- 使用 Vue 3 Composition API + `<script setup>` 语法
- 组件命名使用 PascalCase
- API 接口统一封装在 `src/api/` 目录
- 样式优先使用 Tailwind CSS，复杂样式使用 scoped CSS
- 遵循 Gemini 设计风格：渐变背景、玻璃拟态、大圆角

### Git 提交规范
```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
perf: 性能优化
test: 测试相关
chore: 构建/工具相关
```

## 常用命令

### 后端
```bash
cd investmind
mvn spring-boot:run              # 开发模式启动
mvn clean package                # 打包
mvn clean compile                # 编译
```

### 前端
```bash
cd investmind-web
npm install                      # 安装依赖
npm run dev                      # 开发服务器 (端口 3000)
npm run build                    # 生产构建
npm run preview                  # 预览生产构建
```

### 数据库
```bash
# 初始化数据库
mysql -u root -p < investmind/src/main/resources/db/schema.sql
```

## 配置说明

### 后端配置 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/investmind?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password

investmind:
  ai:
    api-key: your-api-key        # 大模型 API Key
    base-url: https://api.openai.com/v1
    model: gpt-4
  milvus:
    host: localhost              # 向量数据库
    port: 19530
```

### 前端代理配置 (vite.config.js)
开发环境已配置代理：
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 核心功能模块

### 1. 报告管理
- **InvestReportController**: `/api/reports/*`
  - `POST /generate` - AI 生成报告
  - `POST /correct` - 人工修正报告
  - `GET /{id}` - 获取报告详情
  - `GET /today` - 获取今日报告
  - `POST /list` - 分页查询报告列表

### 2. 向量管理
- **VectorController**: `/api/vectors/*`
  - `GET /health` - 检查向量数据库连接
  - `GET /pending` - 获取待向量化报告
  - `POST /report/{reportId}` - 向量化指定报告
  - `POST /batch` - 批量向量化
  - `GET /search` - 检索相似内容

### 3. 统计分析
- **StatsController**: `/api/stats/*`
  - `GET /overview` - 统计数据概览
  - `GET /learning-progress` - 模型学习进度

## 数据模型

### 核心实体
- **InvestReport** - 投资报告（AI生成内容、人工修正内容、质量评分等）
- **ReportCorrectionLog** - 修正记录日志
- **VectorIndexRecord** - 向量索引记录
- **SystemConfig** - 系统配置

## 开发注意事项

1. **向量数据库**: Milvus 是可选组件，如未配置相关功能将不可用
2. **大模型 API**: 需要配置有效的 API Key 才能生成报告
3. **跨域**: 后端已配置 CORS，前端开发代理已配置
4. **数据库**: 首次启动前需执行 schema.sql 初始化表结构

## 后续优化方向

- [ ] 定时任务：Spring Scheduler 实现每日自动报告生成
- [ ] 多模型支持：支持 Claude、文心一言等
- [ ] 报告模板：自定义报告模板
- [ ] 数据分析：更多统计维度和可视化
- [ ] 权限管理：用户认证和权限控制
- [ ] 测试覆盖：单元测试和集成测试

## 参考资源

- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [Milvus 文档](https://milvus.io/docs)
