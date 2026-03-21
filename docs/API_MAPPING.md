# 页面按钮与 API 对应关系

## 整体架构

```
浏览器 → Nginx(:80) → Gateway(:8080) → 各微服务
```

## 路由映射 (Gateway)

| 前端路径 | Gateway 路由 | 目标服务 | 端口 |
|----------|-------------|----------|------|
| `/ops/**` | → | ops-service | 8083 |
| `/user/**` | → | user-service | 8081 |
| `/chat/**` | → | chat-service | 8093 |
| `/knowledge/**` | → | knowledge-service | 8092 |
| `/workflow/**` | → | workflow-service | 8091 |
| `/notification/**` | → | notification-service | 8094 |

---

## 页面与 API 对应表

### 1. 登录页面 (Login.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 登录 | `/ops/auth/login` | POST | ops-service |
| 注册 | `/ops/auth/register` | POST | ops-service |

### 2. 仪表盘 (dashboard/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 获取概览数据 | `/ops/dashboard/overview` | GET | ops-service |
| 获取统计信息 | `/ops/dashboard/stats` | GET | ops-service |
| 获取最近任务 | `/ops/tasks` | GET | ops-service |
| 获取环境变量 | `/ops/env` | GET | ops-service |

### 3. 用户管理 (admin/Users.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 用户列表 | `/user` | GET | user-service |
| 创建用户 | `/user` | POST | user-service |
| 编辑用户 | `/user/{id}` | PUT | user-service |
| 删除用户 | `/user/{id}` | DELETE | user-service |
| 更新状态 | `/user/{id}/status` | PUT | user-service |

### 4. 角色管理 (admin/Roles.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 角色列表 | `/user/roles` | GET | user-service |
| 创建角色 | `/user/roles` | POST | user-service |
| 编辑角色 | `/user/roles/{id}` | PUT | user-service |
| 删除角色 | `/user/roles/{id}` | DELETE | user-service |

### 5. 系统配置 (admin/System.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 获取配置 | `/ops/system/config` | GET | ops-service |
| 保存配置 | `/ops/system/config` | POST | ops-service |

### 6. 知识库 (knowledge/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 知识库列表 | `/knowledge/bases` | GET | knowledge-service |
| 创建知识库 | `/knowledge/bases` | POST | knowledge-service |
| 编辑知识库 | `/knowledge/bases/{id}` | PUT | knowledge-service |
| 删除知识库 | `/knowledge/bases/{id}` | DELETE | knowledge-service |
| 文档列表 | `/knowledge/bases/{id}/documents` | GET | knowledge-service |
| 添加文档 | `/knowledge/bases/{id}/documents` | POST | knowledge-service |
| 删除文档 | `/knowledge/documents/{id}` | DELETE | knowledge-service |
| 搜索 | `/knowledge/search` | GET | knowledge-service |

### 7. 工作流 (workflow/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 工作流列表 | `/workflow` | GET | workflow-service |
| 创建工作流 | `/workflow` | POST | workflow-service |
| 执行工作流 | `/workflow/{id}/execute` | POST | workflow-service |
| 删除工作流 | `/workflow/{id}` | DELETE | workflow-service |
| 执行历史 | `/workflow/{id}/executions` | GET | workflow-service |

### 8. 自愈中心 (autoheal/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 策略列表 | `/ops/autoheal/strategies` | GET | ops-service |
| 创建策略 | `/ops/autoheal/strategies` | POST | ops-service |
| 编辑策略 | `/ops/autoheal/strategies/{id}` | PUT | ops-service |
| 删除策略 | `/ops/autoheal/strategies/{id}` | DELETE | ops-service |
| 触发自愈 | `/ops/autoheal/trigger/{id}` | POST | ops-service |

### 9. 灰度发布 (grayrelease/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 发布列表 | `/ops/grayrelease` | GET | ops-service |
| 创建发布 | `/ops/grayrelease` | POST | ops-service |
| 执行发布 | `/ops/grayrelease/{id}/release` | POST | ops-service |
| 回滚 | `/ops/grayrelease/{id}/rollback` | POST | ops-service |

### 10. 监控中心 (monitor/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 获取指标 | `/ops/monitor/metrics` | GET | ops-service |
| 获取告警 | `/ops/monitor/alerts` | GET | ops-service |
| 获取服务状态 | `/ops/monitor/services` | GET | ops-service |

### 11. AI 助手 (ai/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 发送消息 | `/chat/send` | POST | chat-service |
| 获取会话列表 | `/chat/sessions` | GET | chat-service |
| 创建会话 | `/chat/sessions` | POST | chat-service |
| 获取历史 | `/chat/history/{id}` | GET | chat-service |
| 删除会话 | `/chat/sessions/{id}` | DELETE | chat-service |
| 健康检查 | `/chat/health` | GET | chat-service |

### 12. 通知中心 (notification/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 通知列表 | `/notification` | GET | notification-service |
| 标记已读 | `/{id}/read` | PUT | notification-service |
| 全部已读 | `/read-all` | PUT | notification-service |
| 删除通知 | `/{id}` | DELETE | notification-service |
| 发送通知 | `/notification` | POST | notification-service |
| 未读数量 | `/notification/unread-count` | GET | notification-service |

### 13. 运维中心 (ops/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 任务列表 | `/ops/tasks` | GET | ops-service |
| 创建任务 | `/ops/tasks` | POST | ops-service |
| 执行脚本 | `/ops/scripts/execute` | POST | ops-service |
| 日志查询 | `/ops/logs` | GET | ops-service |
| 环境配置 | `/ops/env` | GET | ops-service |

### 14. 证书管理 (certificate/Index.vue)

| 按钮/操作 | API 路径 | 方法 | 服务 |
|-----------|----------|------|------|
| 证书列表 | `/ops/certificates` | GET | ops-service |
| 创建证书 | `/ops/certificates` | POST | ops-service |
| 续期证书 | `/{id}/renew` | POST | ops-service |
| 删除证书 | `/{id}` | DELETE | ops-service |

---

## API 模块文件对照

| 前端 API 文件 | 对应页面 | 基础路径 |
|---------------|----------|----------|
| `api/auth.js` | Login.vue | `/ops/auth` |
| `api/dashboard.js` | dashboard/Index.vue | `/ops/dashboard`, `/ops/env` |
| `api/user.js` | admin/Users.vue, admin/Roles.vue | `/user` |
| `api/knowledge.js` | knowledge/Index.vue | `/knowledge` |
| `api/workflow.js` | workflow/Index.vue | `/workflow` |
| `api/autoheal.js` | autoheal/Index.vue | `/ops/autoheal` |
| `api/ops.js` | ops/Index.vue, certificate/Index.vue | `/ops/tasks`, `/ops/scripts`, `/ops/certificates` |
| `api/ai.js` | ai/Index.vue | `/chat` |
| `api/notification.js` | notification/Index.vue | `/notification` |

---

## 注意事项

1. **跨域问题**: 所有 API 通过 Gateway 统一转发，避免跨域
2. **认证**: 除登录/健康检查外，其他 API 需要 JWT Token
3. **错误处理**: 401 错误会触发跳转登录页面
4. **Nginx 代理**: 前端请求 → Nginx(:80) → Gateway(:8080) → 目标服务
