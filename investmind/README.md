# InvestMind 智投进化平台

> 投资理念模型自我迭代的 AI 投研平台

## 项目简介

InvestMind 是一个**AI + 人工协同**的投资研究平台，核心目标是让投资分析模型能够持续自我优化、逐步贴合人工的投资理念。

### 核心流程

```
AI生成报告 → 人工修正 → 数据双存储 → 模型迭代学习
     ↓            ↓          ↓               ↓
MySQL存储    MySQL+向量库   向量数据库      RAG增强生成
```

### 核心功能

1. **自动化产出**：AI 每日生成 Markdown 格式的投资分析报告（约500-800字）
2. **人工干预**：对 AI 生成的报告进行内容修正、优化
3. **模型进化**：将人工修正后的优质报告内容向量化，存入向量数据库
4. **智能增强**：后续 AI 生成报告时，检索向量库中的优质内容，实现模型自我演变

## 技术架构

### 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 |
| ORM框架 | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 向量数据库 | Milvus 2.x |
| 大模型 | OpenAI API / 其他兼容 API |
| 构建工具 | Maven |

### 项目结构

```
investmind/
├── src/main/java/com/investmind/
│   ├── InvestMindApplication.java    # 启动类
│   ├── config/                       # 配置类
│   │   ├── MyBatisPlusConfig.java    # MyBatis-Plus配置
│   │   ├── WebConfig.java            # Web配置（跨域）
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── controller/                   # 控制器层
│   │   ├── InvestReportController.java # 报告管理API
│   │   ├── VectorController.java     # 向量管理API
│   │   └── StatsController.java      # 统计API
│   ├── service/                      # 服务层
│   │   ├── InvestReportService.java  # 报告服务接口
│   │   ├── LLMService.java           # 大模型服务接口
│   │   ├── VectorService.java        # 向量服务接口
│   │   └── impl/                     # 服务实现
│   ├── model/                        # 实体类
│   │   ├── InvestReport.java         # 投资报告实体
│   │   ├── ReportCorrectionLog.java  # 修正记录实体
│   │   ├── VectorIndexRecord.java    # 向量索引记录实体
│   │   └── SystemConfig.java         # 系统配置实体
│   ├── repository/                   # Mapper层
│   │   ├── InvestReportMapper.java
│   │   ├── ReportCorrectionLogMapper.java
│   │   ├── VectorIndexRecordMapper.java
│   │   └── SystemConfigMapper.java
│   └── dto/                          # 数据传输对象
│       ├── ReportRequest.java
│       ├── ReportResponse.java
│       └── ApiResponse.java
├── src/main/resources/
│   ├── application.yml               # 应用配置
│   ├── db/schema.sql                 # 数据库初始化脚本
│   └── mapper/                       # MyBatis XML映射文件
└── pom.xml                           # Maven依赖
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Milvus 2.x (可选，用于向量存储)

### 1. 初始化数据库

```bash
# 创建数据库
mysql -u root -p < src/main/resources/db/schema.sql
```

### 2. 配置应用

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/investmind?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

investmind:
  ai:
    api-key: your-openai-api-key      # 大模型 API Key
    base-url: https://api.openai.com/v1
    model: gpt-4
  milvus:
    host: localhost                   # 向量数据库地址
    port: 19530
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

启动后访问：`http://localhost:8080/api`

## API 接口文档

### 报告管理接口

#### 生成报告
```http
POST /api/reports/generate
Content-Type: application/json

{
  "reportDate": "2024-01-15",
  "reportType": "DAILY",
  "useHistoricalContent": true
}
```

#### 人工修正报告
```http
POST /api/reports/correct
Content-Type: application/json

{
  "reportId": 1,
  "humanContent": "修正后的报告内容...",
  "correctedBy": "分析师A",
  "correctionReason": "优化策略建议部分",
  "qualityScore": 4.5,
  "tags": "牛市,科技股"
}
```

#### 获取报告详情
```http
GET /api/reports/{id}
```

#### 根据日期获取报告
```http
GET /api/reports/date/2024-01-15
```

#### 获取今日报告
```http
GET /api/reports/today
```

#### 分页查询报告列表
```http
POST /api/reports/list
Content-Type: application/json

{
  "reportType": "DAILY",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "pageNum": 1,
  "pageSize": 10
}
```

### 向量管理接口

#### 检查向量数据库连接
```http
GET /api/vectors/health
```

#### 获取待向量化报告
```http
GET /api/vectors/pending
```

#### 向量化指定报告
```http
POST /api/vectors/report/{reportId}
```

#### 批量向量化
```http
POST /api/vectors/batch
```

#### 检索相似内容
```http
GET /api/vectors/search?query=投资策略&topK=5
```

### 统计接口

#### 获取统计数据概览
```http
GET /api/stats/overview
```

#### 获取模型学习进度
```http
GET /api/stats/learning-progress
```

## 数据库设计

### 投资报告表 (invest_report)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| report_date | DATE | 报告日期 |
| report_type | VARCHAR(32) | 报告类型：DAILY/WEEKLY/MONTHLY |
| ai_content | TEXT | AI生成的原始报告 |
| ai_generate_time | DATETIME | AI生成时间 |
| human_content | TEXT | 人工修正后的报告 |
| human_correct_time | DATETIME | 人工修正时间 |
| corrected_by | VARCHAR(64) | 修正人 |
| is_vectorized | TINYINT | 是否已向量化 |
| is_used_for_learning | TINYINT | 是否已用于学习 |
| quality_score | DECIMAL(3,2) | 质量评分 |
| tags | VARCHAR(512) | 标签 |

## 核心流程说明

### 1. 报告生成流程

1. 用户调用生成接口或系统自动触发
2. 系统检索向量库中的历史优质内容（可选）
3. 构建增强提示词
4. 调用大模型生成报告
5. 保存到 MySQL

### 2. 人工修正流程

1. 用户获取 AI 生成的报告
2. 对内容进行修正、评分、打标签
3. 系统记录修正日志
4. 重置向量化状态，等待向量化

### 3. 向量化流程

1. 系统定时或手动触发向量化任务
2. 查询所有已修正但未向量化的报告
3. 调用 Embedding API 获取向量
4. 存入 Milvus 向量数据库
5. 更新 MySQL 中的向量化状态

### 4. 模型学习流程

1. AI 生成新报告时，先检索向量库
2. 获取相似的历史优质内容
3. 构建增强提示词
4. 生成更贴合人工风格的新报告

## 后续优化方向

1. **定时任务**：添加 Spring Scheduler 实现每日自动报告生成
2. **前端界面**：开发 Web 前端，提供报告展示、修正界面
3. **多模型支持**：支持切换不同大模型（Claude、文心一言等）
4. **报告模板**：支持自定义报告模板
5. **数据分析**：添加更多统计维度和可视化
6. **权限管理**：添加用户认证和权限控制

## 许可证

MIT License
