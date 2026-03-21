# OPS 平台服务端口规范

## 端口分配表

| 服务 | 端口 | 用途 | 状态 |
|------|------|------|------|
| Nginx | 80 | 前端入口 | 固定 |
| Gateway | 8080 | API 网关 | 固定 |
| user-service | 8081 | 用户服务 | 固定 |
| ops-service | 8083 | 运维服务 | 固定 |
| knowledge-service | 8092 | 知识库服务 | 固定 |
| workflow-service | 8093 | 工作流服务 | 固定 |

## 端口管理原则

### 开发环境
```
规则：
1. 所有外部请求通过 Gateway (8080)
2. Gateway 路由到各个微服务
3. 微服务之间内部调用
```

### 路由配置 (Gateway)
```
/ops/**    -> ops-service:8083
/user/**   -> user-service:8081
/knowledge/** -> knowledge-service:8092
/workflow/** -> workflow-service:8093
```

### 启动顺序
```
1. Nginx (端口 80)     - 前端入口
2. Gateway (端口 8080) - API 网关
3. 各微服务           - 业务服务
```

## 禁止事项
- 禁止直接暴露微服务到公网
- 禁止前端直接连接微服务端口
- 禁止本地开发绕过 Gateway
