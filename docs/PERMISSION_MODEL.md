# 权限模型设计 (Permission Model)

## 1. 角色定义

| 角色 | 代码 | 描述 | 权限数量 |
|------|------|------|----------|
| 超级管理员 | ADMIN | 系统最高权限 | 全部 |
| 运维工程师 | OPS | 日常运维管理 | 15 |
| 开发人员 | DEVELOPER | 开发调试权限 | 8 |
| 只读用户 | READONLY | 仅查看权限 | 3 |

## 2. 权限清单

### 2.1 监控模块权限

| 权限代码 | 描述 | 角色 |
|----------|------|------|
| `monitor:view` | 查看监控数据 | ADMIN, OPS, DEVELOPER, READONLY |
| `monitor:metrics:query` | 查询指标 | ADMIN, OPS, DEVELOPER |
| `monitor:dashboard:edit` | 编辑仪表盘 | ADMIN, OPS |

### 2.2 告警模块权限

| 权限代码 | 描述 | 角色 |
|----------|------|------|
| `alert:view` | 查看告警 | ADMIN, OPS, DEVELOPER, READONLY |
| `alert:manage` | 管理告警规则 | ADMIN, OPS |
| `alert:acknowledge` | 确认告警 | ADMIN, OPS |
| `alert:resolve` | 解决告警 | ADMIN, OPS |
| `alert:configure` | 配置告警渠道 | ADMIN |

### 2.3 运维模块权限

| 权限代码 | 描述 | 角色 |
|----------|------|------|
| `ops:view` | 查看运维任务 | ADMIN, OPS, DEVELOPER, READONLY |
| `ops:manage` | 管理运维任务 | ADMIN, OPS |
| `ops:script:execute` | 执行脚本 | ADMIN, OPS |
| `ops:task:create` | 创建任务 | ADMIN, OPS |
| `ops:task:cancel` | 取消任务 | ADMIN, OPS |

### 2.4 自愈模块权限

| 权限代码 | 描述 | 角色 |
|----------|------|------|
| `heal:view` | 查看自愈策略 | ADMIN, OPS, READONLY |
| `heal:manage` | 管理自愈策略 | ADMIN |
| `heal:execute` | 执行自愈 | ADMIN |
| `heal:configure` | 配置自愈规则 | ADMIN |

### 2.5 灰度发布权限

| 权限代码 | 描述 | 角色 |
|----------|------|------|
| `release:view` | 查看发布 | ADMIN, OPS, DEVELOPER, READONLY |
| `release:manage` | 管理发布 | ADMIN, OPS |
| `release:create` | 创建发布 | ADMIN, OPS |
| `release:rollback` | 回滚发布 | ADMIN, OPS |

### 2.6 系统权限

| 权限代码 | 描述 | 角色 |
|----------|------|------|
| `system:user:manage` | 用户管理 | ADMIN |
| `system:role:manage` | 角色管理 | ADMIN |
| `system:config:view` | 查看配置 | ADMIN, OPS |
| `system:config:edit` | 修改配置 | ADMIN |
| `system:log:view` | 查看日志 | ADMIN, OPS, DEVELOPER |

## 3. 角色-权限映射

```
ADMIN ──────────────────────────────────────────────────────
  ├── monitor:view
  ├── monitor:metrics:query
  ├── monitor:dashboard:edit
  ├── alert:view
  ├── alert:manage
  ├── alert:acknowledge
  ├── alert:resolve
  ├── alert:configure
  ├── ops:view
  ├── ops:manage
  ├── ops:script:execute
  ├── ops:task:create
  ├── ops:task:cancel
  ├── heal:view
  ├── heal:manage
  ├── heal:execute
  ├── heal:configure
  ├── release:view
  ├── release:manage
  ├── release:create
  ├── release:rollback
  ├── system:user:manage
  ├── system:role:manage
  ├── system:config:view
  ├── system:config:edit
  └── system:log:view

OPS ───────────────────────────────────────────────────────
  ├── monitor:view
  ├── monitor:metrics:query
  ├── monitor:dashboard:edit
  ├── alert:view
  ├── alert:manage
  ├── alert:acknowledge
  ├── alert:resolve
  ├── ops:view
  ├── ops:manage
  ├── ops:script:execute
  ├── ops:task:create
  ├── ops:task:cancel
  ├── heal:view
  ├── release:view
  ├── release:manage
  ├── release:create
  ├── release:rollback
  ├── system:config:view
  └── system:log:view

DEVELOPER ─────────────────────────────────────────────────
  ├── monitor:view
  ├── monitor:metrics:query
  ├── alert:view
  ├── ops:view
  ├── ops:script:execute
  ├── release:view
  └── system:log:view

READONLY ─────────────────────────────────────────────────
  ├── monitor:view
  ├── alert:view
  └── release:view
```

## 4. API 端点权限要求

| 方法 | 路径 | 所需权限 |
|------|------|----------|
| GET | /actuator/prometheus | monitor:view |
| GET | /api/alerts | alert:view |
| POST | /api/alerts/rules | alert:manage |
| DELETE | /api/alerts/rules/{name} | alert:manage |
| POST | /api/alerts/{id}/acknowledge | alert:acknowledge |
| POST | /api/alerts/{id}/resolve | alert:resolve |
| GET | /ops/tasks | ops:view |
| POST | /ops/tasks | ops:task:create |
| POST | /ops/scripts/execute | ops:script:execute |
| GET | /ops/heal/strategies | heal:view |
| POST | /ops/heal/strategies | heal:manage |
| POST | /ops/heal/execute/{type} | heal:execute |
| GET | /ops/releases | release:view |
| POST | /ops/releases | release:create |
| POST | /ops/releases/{id}/rollback | release:rollback |
| GET | /ops/rbac/roles | system:role:manage |
| POST | /ops/rbac/users/{id}/roles | system:user:manage |
