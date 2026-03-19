# OpenClaw AI Platform 重构方案

**版本：1.0.0**
**日期：2026-03-19**
**架构师：小爪**

---

## 1. 当前问题分析

### 1.1 现有架构

```
┌─────────────────────────────────────────────────────────────┐
│                      ops-service (单体)                      │
├─────────────────────────────────────────────────────────────┤
│  WorkflowService  │ KnowledgeBaseService  │ AIService       │
│  AlertService   │ NotificationService    │ ChatService     │
│  (30+ Services in one package)                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 问题清单

| 问题 | 影响 | 严重度 |
|------|------|--------|
| 单体架构 | 扩展困难 | 🔴 高 |
| 包名混乱 | com.example vs com.openclaw | 🟡 中 |
| 代码耦合 | 修改困难 | 🔴 高 |
| 缺乏模块化 | 难以独立部署 | 🔴 高 |
| 文档不全 | 维护困难 | 🟡 中 |

---

## 2. 目标架构设计

### 2.1 微服务架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (8080)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ workflow-svc  │   │  knowledge-svc │   │   chat-svc   │
│   (8081)     │   │    (8082)     │   │   (8083)     │
└───────────────┘   └───────────────┘   └───────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐       │
│  │PostgreSQL│  │  Redis  │  │Elastic  │  │  Kafka  │       │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘       │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Nacos (8848)                               │   │
│  │   - Service Registration (服务注册发现)                 │   │
│  │   - Configuration Management (配置管理)                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

| 服务 | 职责 | 端口 | 依赖 |
|------|------|------|------|
| gateway | 路由、鉴权 | 8080 | - |
| workflow-service | 工作流编排 | 8081 | PostgreSQL, Redis |
| knowledge-service | 知识库、AI | 8082 | PostgreSQL, Redis, LLM API |
| chat-service | 对话管理 | 8083 | PostgreSQL, Redis |
| notification-service | 通知发送 | 8084 | 飞书/钉钉 API |
| admin-service | 用户、权限 | 8085 | PostgreSQL |

---

## 3. 重构执行计划

### Phase 1: 代码重构 (Week 1-2)

#### 1.1 包结构重组

```
com.openclaw.ops
├── workflow/          # 工作流模块
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
├── knowledge/         # 知识库模块
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
├── chat/             # 对话模块
│   ├── controller/
│   ├── service/
│   └── model/
└── common/           # 公共模块
    ├── config/
    ├── exception/
    └── util/
```

#### 1.2 任务清单

- [ ] 创建模块化包结构
- [ ] 移动现有代码到新包
- [ ] 提取公共组件到 common
- [ ] 统一异常处理
- [ ] 更新 Maven 模块

### Phase 2: 服务拆分 (Week 3-4)

#### 2.1 创建 Maven 多模块项目

```
ops-platform/
├── pom.xml (parent)
├── gateway/
├── workflow-service/
├── knowledge-service/
├── chat-service/
├── notification-service/
├── admin-service/
└── common/
```

#### 2.2 任务清单

- [ ] 创建各服务 pom.xml
- [ ] 配置 Spring Cloud
- [ ] 设置服务注册发现
- [ ] 配置负载均衡

### Phase 3: 数据层重构 (Week 5-6)

#### 3.1 数据库设计

```sql
-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 知识库表
CREATE TABLE knowledge_bases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 文档表
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT REFERENCES knowledge_bases(id),
    title VARCHAR(200),
    content TEXT,
    type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PROCESSING',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 对话会话表
CREATE TABLE chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    channel VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### 3.2 任务清单

- [ ] 设计数据库 schema
- [ ] 配置 JPA 实体
- [ ] 实现 Repository 层
- [ ] 数据迁移脚本

### Phase 4: API 重构 (Week 7-8)

#### 4.1 REST API 规范

```
Base URL: /api/v1

# 工作流
GET    /workflows          # 列表
POST   /workflows          # 创建
GET    /workflows/{id}    # 详情
PUT    /workflows/{id}    # 更新
DELETE /workflows/{id}    # 删除
POST   /workflows/{id}/execute  # 执行

# 知识库
GET    /knowledge/bases           # 知识库列表
POST   /knowledge/bases           # 创建知识库
GET    /knowledge/bases/{id}      # 知识库详情
POST   /knowledge/bases/{id}/docs # 导入文档
GET    /knowledge/bases/{id}/search # 语义搜索

# 对话
POST   /chat/sessions      # 创建会话
POST   /chat/send          # 发送消息
GET    /chat/history       # 历史记录
```

#### 4.2 任务清单

- [ ] 定义 API 规范
- [ ] 实现 REST Controller
- [ ] 添加请求验证
- [ ] 统一响应格式
- [ ] API 文档 (Swagger)

---

## 4. 技术选型

| 层级 | 技术 | 理由 |
|------|------|------|
| 服务框架 | Spring Boot 3.4 | 成熟稳定 |
| 服务注册与发现 | **Nacos** | 国产化、配置管理一体化 |
| 服务网格 | Spring Cloud Gateway | 简单易用 |
| 配置中心 | **Nacos Config** | 统一配置管理 |
| 消息队列 | Kafka / RabbitMQ | 高吞吐 |
| 缓存 | Redis | 性能优化 |
| 搜索 | Elasticsearch | 全文搜索 |
| 监控 | Prometheus + Grafana | 成熟方案 |

---

## 5. 风险与对策

| 风险 | 影响 | 对策 |
|------|------|------|
| 重构周期长 | 影响业务 | 分阶段交付 |
| 数据迁移 | 数据丢失 | 双重写入 |
| 服务间调用 | 复杂度 | 优先同步，后期异步 |
| 团队熟悉度 | 效率降低 | 文档 + 培训 |

---

## 6. 里程碑

| 阶段 | 周期 | 交付物 |
|------|------|--------|
| Phase 1 | Week 1-2 | 重构后的单体应用 |
| Phase 2 | Week 3-4 | 微服务基础架构 |
| Phase 3 | Week 5-6 | 数据层完成 |
| Phase 4 | Week 7-8 | API 规范化 |

**预计总周期：8 周**

---

*本方案由架构师团队编制*
