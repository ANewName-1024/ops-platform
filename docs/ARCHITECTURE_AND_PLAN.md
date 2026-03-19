# OpenClaw AI Platform 完整架构设计与重构计划

**版本：1.0**
**日期：2026-03-19**
**架构师：小爪**

---

## 一、完整架构设计

```
+-----------------------------------------------------------------------------+
|                                    User / Client                             |
+----------------------------------------+------------------------------------
                                         |
                                         v
+----------------------------------------+------------------------------------
|                      WAF + Load Balancer                                    |
|                     (防护 + 负载均衡)                                       |
+----------------------------------------+------------------------------------
                                         |
                                         v
+-----------------------------------------------------------------------------+
|                         API Gateway (8080)                                   |
|              (路由 + 鉴权 + 限流 + 熔断 + 审计)                              |
+----------------------------------------+------------------------------------
                                         |
     +---------------+---------------+---+---------------+---------------+
     |               |               |   |               |               |
     v               v               v   v               v               v
+---------+   +-----------+   +---------+   +---------+   +--------+   +--------+
|workflow |   |knowledge  |   |    ai    |   |   chat  |   | notify |   | gateway|
|  (8081) |   |  (8082)  |   |  (8083)  |   |  (8084)  |   | (8085) |   | admin  |
+---------+   +-----------+   +---------+   +---------+   +--------+   +--------+
     |               |               |   |               |               |
     +---------------+---------------+---+---------------+---------------+
                                         |
                                         v
+-----------------------------------------------------------------------------+
|                             Infrastructure Layer                            |
+-----------------------------------------------------------------------------+
|  +----------+  +----------+  +----------+  +----------+  +----------+   |
|  |PostgreSQL|  |  Redis  |  |  MinIO   |  |  Kafka   |  |   ES    |   |
|  |  主从架构 |  |  集群   |  | 对象存储  |  |  集群   |  |  搜索   |   |
|  +----------+  +----------+  +----------+  +----------+  +----------+   |
|                                                                           |
|  +--------------------------------------------------------------------+   |
|  |                        Nacos (8848)                               |   |
|  |  +-----------------------------------------------------------+    |   |
|  |  |     Service Discovery (服务注册发现)                       |    |   |
|  |  |     Configuration Management (配置中心)                  |    |   |
|  |  +-----------------------------------------------------------+    |   |
|  +--------------------------------------------------------------------+   |
|                                                                           |
|  +--------------------------------------------------------------------+   |
|  |          Observability (监控/日志/链路)                             |   |
|  |        Prometheus + Grafana + Loki + SkyWalking                   |   |
|  +--------------------------------------------------------------------+   |
+-----------------------------------------------------------------------------+
```

---

## 二、技术栈

| 层级 | 技术 |
|------|------|
| **服务治理** | Nacos 2.x (服务注册 + 配置中心) |
| **网关** | Spring Cloud Gateway |
| **服务框架** | Spring Boot 3.4 |
| **数据库** | PostgreSQL 15+ |
| **缓存** | Redis 7+ |
| **消息队列** | Kafka 3.x |
| **对象存储** | MinIO / 阿里云 OSS |
| **搜索** | Elasticsearch 8+ |
| **AI** | Spring AI + OpenAI/Claude/国产大模型 |
| **监控** | Prometheus + Grafana |
| **日志** | Loki + Grafana |
| **链路** | SkyWalking |
| **部署** | Docker + Kubernetes |

---

## 三、重构详细步骤

### Phase 1: 代码重构 (Week 1-2)

#### Week 1: 包路径重构

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 1 | 备份代码 | git tag v1.0-backup | 标签创建成功 |
| Day 1 | 创建包结构 | mkdir com.openclaw.ai.workflow | 目录创建 |
| Day 1 | 创建包结构 | mkdir com.openclaw.ai.knowledge | 目录创建 |
| Day 1 | 创建包结构 | mkdir com.openclaw.ai.chat | 目录创建 |
| Day 1 | 创建包结构 | mkdir com.openclaw.ai.notify | 目录创建 |
| Day 1 | 创建包结构 | mkdir com.openclaw.ai.common | 目录创建 |
| Day 2 | 移动工作流代码 | git mv workflow/* | 文件移动成功 |
| Day 2 | 移动知识库代码 | git mv knowledge/* | 文件移动成功 |
| Day 2 | 移动对话代码 | git mv chat/* | 文件移动成功 |
| Day 2 | 移动通知代码 | git mv notify/* | 文件移动成功 |
| Day 3 | 更新 import | sed 's/com.example/com.openclaw.ai/g' | 编译通过 |
| Day 3 | 更新 pom.xml | 修改 groupId | mvn compile |
| Day 4 | 提取接口 | 创建 IWorkflowService | 接口创建 |
| Day 4 | 提取接口 | 创建 IKnowledgeService | 接口创建 |
| Day 4 | 提取接口 | 创建 IChatService | 接口创建 |
| Day 5 | 提取接口 | 创建 INotifyService | 接口创建 |
| Day 5 | 统一异常 | 创建 BusinessException | 异常统一 |

#### Week 2: 模块化

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 6 | 提取 common | 移动公共类到 common | 编译通过 |
| Day 6 | 提取 config | 移动配置到 common | 编译通过 |
| Day 7 | 提取 util | 移动工具类到 common | 编译通过 |
| Day 7 | 提取 model | 移动 DTO/VO 到 common | 编译通过 |
| Day 8 | 创建接口 | 定义各模块接口 | 接口定义完成 |
| Day 8 | 实现接口 | Service 实现接口 | 实现完成 |
| Day 9 | 测试验证 | 单元测试 | 测试通过 |
| Day 10| 文档更新 | 更新 README | 文档完成 |

---

### Phase 2: 服务拆分准备 (Week 3-4)

#### Week 3: Nacos 集成

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 11| 安装 Nacos | docker run nacos/nacos-server | 服务启动 |
| Day 11| 配置 Nacos | bootstrap.yml | 配置完成 |
| Day 12| 服务注册 | @EnableDiscoveryClient | 注册成功 |
| Day 12| 配置中心 | Nacos Config 集成 | 配置读取成功 |
| Day 13| 命名空间 | 创建 namespace | 命名空间创建 |
| Day 13| 配置分组 | 创建 group | 分组创建 |
| Day 14| 多环境配置 | dev/prod 配置 | 环境隔离成功 |
| Day 15| 配置刷新 | @RefreshScope | 动态刷新成功 |

#### Week 4: 服务拆分

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 16| 创建 Maven 多模块 | pom.xml 结构 | 结构创建 |
| Day 17| 拆分 workflow-service | 独立模块 | 模块创建 |
| Day 17| 拆分 knowledge-service | 独立模块 | 模块创建 |
| Day 18| 拆分 ai-service | 独立模块 | 模块创建 |
| Day 18| 拆分 chat-service | 独立模块 | 模块创建 |
| Day 19| 拆分 notify-service | 独立模块 | 模块创建 |
| Day 20| 服务间调用 | Feign Client | 调用成功 |

---

### Phase 3: 数据层重构 (Week 5-6)

#### Week 5: 数据库设计

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 21| 用户表 | users | 建表成功 |
| Day 21| 知识库表 | knowledge_bases | 建表成功 |
| Day 22| 文档表 | documents | 建表成功 |
| Day 22| 对话表 | chat_sessions | 建表成功 |
| Day 23| 工作流表 | workflows | 建表成功 |
| Day 23| 任务表 | workflow_tasks | 建表成功 |
| Day 24| JPA 实体 | @Entity 映射 | 映射成功 |
| Day 24| Repository | Data JPA | CRUD 成功 |

#### Week 6: 缓存与队列

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 25| Redis 集成 | Cache | 缓存生效 |
| Day 25| 会话缓存 | Session | 会话缓存 |
| Day 26| Kafka 集成 | Producer | 消息发送 |
| Day 26| Kafka 集成 | Consumer | 消息消费 |
| Day 27| ES 集成 | Elasticsearch | 搜索生效 |
| Day 27| 文档索引 | Index | 索引创建 |
| Day 28| 数据迁移 | 迁移脚本 | 迁移成功 |

---

### Phase 4: 安全与部署 (Week 7-8)

#### Week 7: 安全加固

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 29| JWT 优化 | Token 验证 | 验证通过 |
| Day 29| 签名验签 | API 签名 | 签名验证 |
| Day 30| 字段加密 | @Encrypted | 加密生效 |
| Day 30| 限流 | RateLimiter | 限流生效 |
| Day 31| 熔断 | CircuitBreaker | 熔断生效 |
| Day 31| 审计日志 | AuditLog | 日志记录 |

#### Week 8: 容器化

| 天 | 任务 | 具体操作 | 验证 |
|----|------|----------|------|
| Day 32| Dockerfile | 各服务 Dockerfile | 镜像构建 |
| Day 33| docker-compose | 本地编排 | 本地启动 |
| Day 34| K8s YAML | Deployment | K8s 部署 |
| Day 34| K8s YAML | Service | 服务暴露 |
| Day 35| Helm Chart | 打包 | Chart 完成 |
| Day 35| CI/CD | GitHub Actions | 流水线完成 |

---

## 四、里程碑

| 阶段 | 周期 | 交付物 | 验收标准 |
|------|------|--------|----------|
| Phase 1 | Week 1-2 | 重构后单体应用 | 编译通过，API 正常 |
| Phase 2 | Week 3-4 | 微服务基础 | Nacos 注册，配置生效 |
| Phase 3 | Week 5-6 | 数据层完成 | CRUD 正常，缓存生效 |
| Phase 4 | Week 7-8 | 可部署版本 | Docker 镜像，K8s 部署 |

**预计总周期：8 周 (40 工作日)**

---

## 五、回滚计划

| 阶段 | 回滚命令 |
|------|----------|
| Phase 1 | git reset --hard Phase1-Start |
| Phase 2 | git reset --hard Phase2-Start |
| Phase 3 | git reset --hard Phase3-Start |
| Phase 4 | git reset --hard Phase4-Start |

---

*架构设计：小爪*
