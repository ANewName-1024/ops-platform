# 端口规范

## 概述

本文档定义 OPS 平台所有服务的端口分配规范。

## 端口分配

### 基础设施

| 端口 | 服务 | 协议 | 说明 |
|------|------|------|------|
| 80 | Nginx | HTTP | 前端静态文件服务 |
| 443 | Nginx | HTTPS | HTTPS (可选) |
| 3306 | MySQL | TCP | 本地 MySQL (开发用) |
| 5432 | PostgreSQL | TCP | 本地 PostgreSQL |
| 6379 | Redis | TCP | 缓存服务 |
| 8848 | Nacos | HTTP | 服务注册/配置中心 |
| 9000 | Eureka | HTTP | 服务注册中心 (旧版) |

### 微服务

| 端口 | 服务 | 说明 |
|------|------|------|
| 8080 | gateway | API 网关 |
| 8081 | user-service | 用户服务 |
| 8082 | config-service | 配置服务 |
| 8083 | ops-service | 运维服务 (主服务) |
| 8091 | workflow-service | 工作流服务 |
| 8092 | knowledge-service | 知识库服务 |
| 8093 | chat-service | AI 对话服务 |
| 8094 | notification-service | 通知服务 |

### frp 内网穿透

| 端口 | 服务 | 说明 |
|------|------|------|
| 7000 | frps | frp 服务器 |
| 7001 | frps | frp HTTP |
| 9999 | frps | frp 客户端连接 |

## 端口规划原则

1. **基础设施**: 3306-8999
2. **核心服务**: 8080-8089
3. **扩展服务**: 8090-8999
4. **避免冲突**: 启动前检查端口占用

## 端口检查命令

### Windows

```cmd
netstat -ano | findstr "LISTENING" | findstr "80 |8080 |8083 |8091 |8092 |8093 |8094"
```

### 杀掉占用进程

```cmd
taskkill /F /PID <PID>
```

## 当前使用状态

```
TCP    0.0.0.0:80             LISTENING  # Nginx
TCP    0.0.0.0:8083           LISTENING  # ops-service
TCP    0.0.0.0:8091           LISTENING  # workflow-service
TCP    0.0.0.0:8092           LISTENING  # knowledge-service
TCP    0.0.0.0:8094           LISTENING  # notification-service
```
