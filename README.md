# OpenClaw Ops Platform

**企业级智能运维平台**

[![License](https://img.shields.io/badge/license-Commercial-blue.svg)](LICENSE_COMMERCIAL)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](REQUIREMENTS.md)

## 简介

OpenClaw Ops Platform 是一款企业级智能运维平台，提供监控、告警、运维调度、自动修复、灰度发布、权限管理等核心功能。

## 功能特性

- 📊 **监控模块** - JVM/数据库/系统指标采集
- 🔔 **告警系统** - 阈值告警规则，多渠道通知
- ⚙️ **运维调度** - 定时任务，脚本执行
- 🔧 **自动修复** - 自愈策略引擎，故障自动恢复
- 🚀 **灰度发布** - 流量调控，版本管理，快速回滚
- 🔐 **权限管理** - RBAC 角色权限模型
- 📚 **知识库** - 文档管理与搜索
- 💬 **AI 对话** - 智能运维助手

## 技术栈

- Java 21 + Spring Boot 3.4.0
- Spring Cloud 微服务架构
- Vue 3 + Element Plus 前端
- PostgreSQL 数据库
- Nginx 反向代理

## 架构

```
┌─────────────────────────────────────────────────┐
│                  用户浏览器                      │
└─────────────────┬───────────────────────────────┘
                  │ http://192.168.2.32:80
                  ▼
┌─────────────────────────────────────────────────┐
│  Nginx (端口 80) - 前端静态文件                  │
└─────────────────┬───────────────────────────────┘
                  │ API 请求
        ┌─────────┼─────────┬─────────┐
        ▼         ▼         ▼         ▼
   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
   │  OPS   │ │Workflow│ │Knowledge│ │Notifi- │
   │ :8083  │ │ :8091  │ │ :8092  │ │ cation │
   └────────┘ └────────┘ └────────┘ │ :8094  │
                                    └────────┘
```

## 服务列表

| 服务 | 端口 | 说明 |
|------|------|------|
| nginx | 80 | 前端页面 |
| gateway | 8080 | API 网关 (可选) |
| user-service | 8081 | 用户服务 |
| ops-service | 8083 | 运维服务 |
| workflow-service | 8091 | 工作流服务 |
| knowledge-service | 8092 | 知识库服务 |
| chat-service | 8093 | AI 对话服务 |
| notification-service | 8094 | 通知服务 |

## 快速开始

### 前置要求

| 组件 | 要求 |
|------|------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| PostgreSQL | 8.137.116.121:8432 |

### 启动服务

```bash
# 1. 打包所有服务
cd temp-project
mvn clean package -DskipTests

# 2. 启动各服务 (生产模式)
java -jar ops-service/target/ops-service-2.2.0.jar --server.port=8083
java -jar workflow-service/target/workflow-service-2.2.0.jar --server.port=8091
java -jar knowledge-service/target/knowledge-service-2.2.0.jar --server.port=8092
java -jar notification-service/target/notification-service-2.2.0.jar --server.port=8094

# 3. 部署前端
cd frontend
npm run build
Copy-Item -Recurse dist\* D:\temp\frontend\
```

### 访问

- 前端: http://192.168.2.32
- 登录: admin / Admin@123456

## 前端模块

| 模块 | 路径 | 说明 |
|------|------|------|
| 仪表盘 | /dashboard | 统计概览 |
| 知识库 | /knowledge | 文档管理 |
| 工作流 | /workflow | 流程编排 |
| 自愈中心 | /autoheal | 自动修复 |
| 灰度发布 | /grayrelease | 版本管理 |
| 监控中心 | /monitor | 指标监控 |
| AI 助手 | /ai | 智能对话 |
| 通知中心 | /notification | 消息通知 |
| 运维中心 | /ops | 任务调度 |
| 证书管理 | /certificate | SSL 证书 |
| 用户管理 | /admin/users | 用户管理 |
| 角色管理 | /admin/roles | 角色权限 |
| 系统配置 | /admin/system | 系统设置 |

## API

### ops-service (8083)

| API | 方法 | 说明 |
|-----|------|------|
| `/ops/auth/login` | POST | 登录认证 |
| `/ops/dashboard/overview` | GET | 仪表盘数据 |
| `/ops/heal/strategies` | GET/POST | 自愈策略 |
| `/ops/releases` | GET/POST | 灰度发布 |
| `/ops/cert/info` | GET | 证书信息 |
| `/ops/users` | GET | 用户列表 |
| `/ops/system/config` | GET | 系统配置 |

### workflow-service (8091)

| API | 方法 | 说明 |
|-----|------|------|
| `/workflow` | GET/POST | 工作流列表/创建 |
| `/workflow/{id}` | GET/PUT/DELETE | 工作流详情/更新/删除 |
| `/workflow/{id}/execute` | POST | 执行工作流 |

### knowledge-service (8092)

| API | 方法 | 说明 |
|-----|------|------|
| `/knowledge/bases` | GET/POST | 知识库列表/创建 |
| `/knowledge/bases/{id}` | GET/PUT/DELETE | 知识库操作 |
| `/knowledge/bases/{id}/documents` | GET/POST | 文档列表/添加 |
| `/knowledge/documents/{id}` | DELETE | 删除文档 |
| `/knowledge/search` | GET | 搜索 |

### notification-service (8094)

| API | 方法 | 说明 |
|-----|------|------|
| `/notification` | GET/POST | 通知列表/发送 |
| `/notification/{id}/read` | PUT | 标记已读 |
| `/notification/read-all` | PUT | 全部已读 |
| `/notification/unread-count` | GET | 未读数量 |

## 文档

- [配置文档](CONFIG.md)
- [部署说明](DEPLOY.md)
- [需求列表](REQUIREMENTS.md)
- [权限模型](docs/PERMISSION_MODEL.md)

## 版本

- **v1.0.0** - 首个正式版本 (2026-03-21)

---

© 2026 OpenClaw Team. All Rights Reserved.
