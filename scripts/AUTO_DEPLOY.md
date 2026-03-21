# 自动化部署说明

## 概述

本项目提供自动化部署能力，支持一键构建、部署、配置清单生成。

## 脚本列表

| 脚本 | 功能 |
|------|------|
| `auto-deploy.ps1` | 一键自动化部署 |
| `generate-config-excel.ps1` | 生成配置参数Excel清单 |

## 使用方法

### 1. 一键部署 (构建 + 启动所有服务)

```powershell
cd D:\.openclaw\workspace\temp-project\scripts
.\auto-deploy.ps1 -All
```

### 2. 仅构建

```powershell
.\auto-deploy.ps1 -Build
```

### 3. 仅部署 (需要先构建)

```powershell
.\auto-deploy.ps1 -Deploy
```

### 4. 生成配置清单

```powershell
.\generate-config-excel.ps1
```

## 输出文件

- `docs/CONFIG_INVENTORY.csv` - 完整配置参数清单
- `logs/*.log` - 服务日志

## 配置参数统计

运行脚本后会显示:
- 总参数数量
- 敏感参数数量
- 配置文件数量

## 敏感参数识别

脚本会自动识别以下敏感参数:
- password, passwd, pwd
- secret, token, key
- api_key, access_key
- credential, auth
- jwt_secret, encryption_key

敏感参数在导出时会自动脱敏显示。

## 前置要求

- JDK 21+
- Maven 3.9+
- PowerShell 5.1+

## 服务启动顺序

1. 停止现有服务
2. 依次启动各服务:
   - ops-service (8083)
   - workflow-service (8091)
   - knowledge-service (8092)
   - notification-service (8094)
   - chat-service (8093)

## 健康检查

部署完成后会自动进行健康检查，确认所有服务正常运行。
