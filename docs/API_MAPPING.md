# API 映射文档

## 概述

本文档记录 OPS 平台所有微服务的 API 端点映射关系。

## 前端到后端映射

前端通过 Nginx 或直接访问后端服务。生产环境配置 `VITE_API_BASE_URL=http://localhost:8083`。

## 服务 API 映射

### 1. ops-service (8083)

基础路径: `/ops`

| 前端调用 | 后端路径 | 方法 | 功能 |
|----------|----------|------|------|
| `/api/auth/login` | `/ops/auth/login` | POST | 登录 |
| `/api/auth/logout` | `/ops/auth/logout` | POST | 登出 |
| `/api/dashboard/overview` | `/ops/dashboard/overview` | GET | 仪表盘概览 |
| `/api/dashboard/actions` | `/ops/dashboard/actions` | GET | 最近操作 |
| `/api/dashboard/activity` | `/ops/dashboard/activity` | GET | 活动日志 |
| `/api/heal/strategies` | `/ops/heal/strategies` | GET/POST | 自愈策略 |
| `/api/heal/strategies/:type` | `/ops/heal/strategies/:type` | DELETE | 删除策略 |
| `/api/heal/execute/:type` | `/ops/heal/execute/:type` | POST | 执行自愈 |
| `/api/releases` | `/ops/releases` | GET/POST | 灰度发布 |
| `/api/releases/:id` | `/ops/releases/:id` | GET | 发布详情 |
| `/api/releases/:id/traffic` | `/ops/releases/:id/traffic` | PUT | 调整流量 |
| `/api/releases/:id/complete` | `/ops/releases/:id/complete` | POST | 完成发布 |
| `/api/releases/:id/rollback` | `/ops/releases/:id/rollback` | POST | 回滚发布 |
| `/api/cert/info` | `/ops/cert/info` | GET | 证书信息 |
| `/api/cert/rotate` | `/ops/cert/rotate` | POST | 轮换证书 |
| `/api/cert/check` | `/ops/cert/check` | GET | 检查证书 |
| `/api/users` | `/ops/users` | GET | 用户列表 |
| `/api/system/config` | `/ops/system/config` | GET | 系统配置 |
| `/api/metrics` | `/ops/metrics` | GET | 指标数据 |
| `/api/health` | `/ops/health` | GET | 健康检查 |
| `/api/loggers` | `/ops/loggers` | GET | 日志级别 |
| `/api/env` | `/ops/env` | GET | 环境变量 |

### 2. workflow-service (8091)

基础路径: `/workflow`

| 前端调用 | 后端路径 | 方法 | 功能 |
|----------|----------|------|------|
| `/api/workflow` | `/workflow` | GET | 工作流列表 |
| `/api/workflow` | `/workflow` | POST | 创建工作流 |
| `/api/workflow/:id` | `/workflow/:id` | GET | 工作流详情 |
| `/api/workflow/:id` | `/workflow/:id` | PUT | 更新工作流 |
| `/api/workflow/:id` | `/workflow/:id` | DELETE | 删除工作流 |
| `/api/workflow/:id/execute` | `/workflow/:id/execute` | POST | 执行工作流 |
| `/api/workflow/:id/executions` | `/workflow/:id/executions` | GET | 执行历史 |

### 3. knowledge-service (8092)

基础路径: `/knowledge`

| 前端调用 | 后端路径 | 方法 | 功能 |
|----------|----------|------|------|
| `/api/knowledge/bases` | `/knowledge/bases` | GET | 知识库列表 |
| `/api/knowledge/bases` | `/knowledge/bases` | POST | 创建知识库 |
| `/api/knowledge/bases/:id` | `/knowledge/bases/:id` | GET | 知识库详情 |
| `/api/knowledge/bases/:id` | `/knowledge/bases/:id` | PUT | 更新知识库 |
| `/api/knowledge/bases/:id` | `/knowledge/bases/:id` | DELETE | 删除知识库 |
| `/api/knowledge/bases/:id/documents` | `/knowledge/bases/:id/documents` | GET | 文档列表 |
| `/api/knowledge/bases/:id/documents` | `/knowledge/bases/:id/documents` | POST | 添加文档 |
| `/api/knowledge/documents/:id` | `/knowledge/documents/:id` | DELETE | 删除文档 |
| `/api/knowledge/search` | `/knowledge/search` | GET | 搜索 |

### 4. notification-service (8094)

基础路径: `/notification`

| 前端调用 | 后端路径 | 方法 | 功能 |
|----------|----------|------|------|
| `/api/notification` | `/notification` | GET | 通知列表 |
| `/api/notification` | `/notification` | POST | 发送通知 |
| `/api/notification/:id` | `/notification/:id` | GET | 通知详情 |
| `/api/notification/:id/read` | `/notification/:id/read` | PUT | 标记已读 |
| `/api/notification/read-all` | `/notification/read-all` | PUT | 全部已读 |
| `/api/notification/:id` | `/notification/:id` | DELETE | 删除通知 |
| `/api/notification/unread-count` | `/notification/unread-count` | GET | 未读数量 |

## API 统一封装

前端通过 `src/utils/request.js` 封装 axios：

```javascript
// 请求示例
import request from '@/utils/request'

// 登录
request({
  url: '/ops/auth/login',
  method: 'post',
  data: { username, password }
})

// 获取列表
request({
  url: '/workflow',
  method: 'get',
  params: { page: 1, size: 10 }
})
```

## 认证 Token

所有需要认证的 API 需要在请求头中携带 Token：

```javascript
// request.js 自动添加 Token
config.headers['Authorization'] = `Bearer ${token}`
```
