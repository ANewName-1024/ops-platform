# OpenClaw Ops Platform

**企业级智能运维平台**

[![License](https://img.shields.io/badge/license-Commercial-blue.svg)](LICENSE_COMMERCIAL)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](REQUIREMENTS.md)

## 简介

OpenClaw Ops Platform 是一款企业级智能运维平台，提供监控、告警、运维调度、自动修复、灰度发布、权限管理等核心功能。

## 功能特性

- 📊 **监控模块** - JVM/数据库/系统指标采集，Prometheus 存储，Grafana 可视化
- 🔔 **告警系统** - 阈值/趋势告警规则，多渠道通知
- ⚙️ **运维调度** - 定时/延迟任务，Shell/PowerShell 脚本执行
- 🔧 **自动修复** - 自愈策略引擎，故障自动恢复
- 🚀 **灰度发布** - 流量调控，版本管理，快速回滚
- 🔐 **权限管理** - RBAC 角色权限模型

## 技术栈

- Java 21 + Spring Boot 3.4.0
- Spring Cloud 微服务
- PostgreSQL 数据库
- Prometheus + Grafana
- ELK (Elasticsearch + Logstash + Kibana)

## 快速开始

```bash
# 克隆项目
git clone https://github.com/openclaw/ops-platform.git

# 编译
mvn clean package

# 启动服务
cd ops-service
mvn spring-boot:run
```

访问 http://localhost:8083

## 许可证

### 非商业用途
本软件采用 **Apache 2.0** 开源许可证（仅限非商业用途）。

### 商业用途
**商业使用需要付费授权。** 请查看 [LICENSE_COMMERCIAL](LICENSE_COMMERCIAL) 了解详情。

| 企业规模 | 年度许可费 |
|----------|------------|
| 小型企业 (1-50人) | ¥ 30,000/年 |
| 中型企业 (51-200人) | ¥ 80,000/年 |
| 大型企业 (201-500人) | ¥ 150,000/年 |

**联系我们：** https://github.com/ANewName-1024

## 文档

- [需求列表](REQUIREMENTS.md)
- [权限模型](docs/PERMISSION_MODEL.md)
- [API 文档](API.md)

## 版本

- **v1.0.0** - 首个正式版本 (2026-03-19)

---

© 2026 OpenClaw Team. All Rights Reserved.
