# API 缺失检查报告

## 检查日期
2026-03-21

## 总体状态

| 页面 | API 状态 | 备注 |
|------|----------|------|
| 登录 | ✅ 完整 | |
| 仪表盘 | ✅ 完整 | |
| 知识库 | ✅ 完整 | |
| 工作流 | ✅ 完整 | |
| **灰度发布** | ⚠️ **部分缺失** | 按钮未连接 API |
| **自愈中心** | ⚠️ **部分缺失** | 创建功能未连接 |
| 证书管理 | ⚠️ 使用 Mock 数据 | |
| 用户管理 | ✅ 完整 | |
| 角色管理 | ✅ 完整 | |
| 系统配置 | ✅ 完整 | |
| 通知中心 | ✅ 完整 | |
| AI助手 | ✅ 完整 | |
| 运维中心 | ✅ 完整 | |

---

## 需要修复的问题

### 1. 灰度发布页面 (grayrelease/Index.vue)

**问题**: 按钮未连接 API

| 按钮 | 功能 | 状态 | 修复方式 |
|------|------|------|----------|
| 新建发布 | 创建发布 | ❌ 未连接 | 调用 `createRelease()` |
| 详情 | 查看详情 | ❌ 未连接 | 调用 `getRelease()` |
| 回滚 | 回滚发布 | ❌ 未连接 | 调用 `rollbackRelease()` |
| 流量调整 | 调整流量 | ❌ 未连接 | 调用 `updateTraffic()` |
| 完成发布 | 完成发布 | ❌ 未连接 | 调用 `completeRelease()` |

**修复方案**:
```javascript
// 添加 import
import { getReleases, createRelease, getRelease, updateTraffic, completeRelease, rollbackRelease } from '../../api/autoheal'

// 新建发布
const handleCreateRelease = async () => {
  await createRelease(releaseForm)
}

// 回滚
const handleRollback = async (row) => {
  await rollbackRelease(row.id)
}
```

---

### 2. 自愈中心页面 (autoheal/Index.vue)

**问题**: 添加策略按钮未连接 API

| 按钮 | 功能 | 状态 |
|------|------|------|
| 添加策略 | 创建策略 | ❌ 未连接 |
| 编辑策略 | 编辑策略 | ❌ 未连接 |

**修复方案**:
```javascript
import { createHealStrategy } from '../../api/autoheal'

const handleCreateStrategy = async () => {
  await createHealStrategy(strategyForm)
}
```

---

### 3. 证书管理页面 (certificate/Index.vue)

**问题**: 使用 Mock 数据，未调用真实 API

| 按钮 | 功能 | 状态 |
|------|------|------|
| 刷新状态 | 刷新证书 | ⚠️ Mock |
| 详情 | 查看详情 | ⚠️ Mock |
| 续期 | 续期证书 | ⚠️ Mock |

**修复方案**:
```javascript
import { getCertificateInfo, rotateCertificate } from '../../api/ops'

// 使用真实 API
const loadData = async () => {
  const res = await getCertificateInfo()
  certificates.value = res.certificates || []
}
```

---

## 后端 API 清单

### ops-service (8083)

| API | 方法 | 前端使用情况 |
|-----|------|-------------|
| `/ops/auth/login` | POST | ✅ 登录 |
| `/ops/dashboard/overview` | GET | ✅ 仪表盘 |
| `/ops/dashboard/actions` | GET | ✅ 仪表盘 |
| `/ops/dashboard/activity` | GET | ✅ 仪表盘 |
| `/ops/heal/strategies` | GET | ✅ 自愈列表 |
| `/ops/heal/strategies` | POST | ❌ 未连接 |
| `/ops/heal/strategies/{type}` | DELETE | ✅ 自愈删除 |
| `/ops/heal/execute/{type}` | POST | ✅ 执行自愈 |
| `/ops/releases` | GET | ✅ 灰度列表 |
| `/ops/releases` | POST | ❌ 未连接 |
| `/ops/releases/{id}` | GET | ❌ 未连接 |
| `/ops/releases/{id}/traffic` | PUT | ❌ 未连接 |
| `/ops/releases/{id}/complete` | POST | ❌ 未连接 |
| `/ops/releases/{id}/rollback` | POST | ❌ 未连接 |
| `/ops/cert/info` | GET | ⚠️ Mock数据 |
| `/ops/cert/rotate` | POST | ⚠️ Mock数据 |
| `/ops/users` | GET | ✅ 用户列表 |
| `/ops/system/config` | GET | ✅ 系统配置 |

---

## 修复优先级

| 优先级 | 页面 | 修复工作量 |
|--------|------|-----------|
| P0 | 灰度发布 | 中等 |
| P1 | 自愈中心 | 简单 |
| P2 | 证书管理 | 简单 |

---

## 修复后测试

修复完成后，运行以下测试:

```powershell
# API 测试
.\scripts\test-api.ps1

# 检查以下 API 是否正常:
# - POST /ops/releases
# - GET /ops/releases/{id}
# - PUT /ops/releases/{id}/traffic
# - POST /ops/releases/{id}/complete
# - POST /ops/releases/{id}/rollback
# - POST /ops/heal/strategies
```
