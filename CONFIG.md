# OPS 平台配置文档

## 架构概览

```
┌─────────────────────────────────────────────────┐
│                  用户浏览器                      │
└─────────────────┬───────────────────────────────┘
                  │ http://192.168.2.32:80
                  ▼
┌─────────────────────────────────────────────────┐
│  Nginx (端口 80)                                 │
│  - 静态文件服务                                  │
└─────────────────┬───────────────────────────────┘
                  │
        ┌─────────┼─────────┬─────────┐
        ▼         ▼         ▼         ▼
   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
   │  OPS   │ │ User   │ │Workflow│ │Knowl-  │
   │ :8083  │ │ :8081  │ │ :8091  │ │ :8092  │
   └────────┘ └────────┘ └────────┘ └────────┘
        │                   │         │
        ▼                   ▼         ▼
   ┌────────┐         ┌────────┐ ┌────────┐
   │Notifi- │         │ Chat   │ │Knowledge
   │ cation │         │ :8093  │ │ :8092  │
   │ :8094  │         └────────┘ └────────┘
   └────────┘
```

## 当前运行状态

| 服务 | 地址 | 端口 | 状态 |
|------|------|------|------|
| 前端 (Nginx) | http://192.168.2.32 | 80 | ✅ 运行中 |
| ops-service | http://192.168.2.32 | 8083 | ✅ 运行中 |
| workflow-service | http://192.168.2.32 | 8091 | ✅ 运行中 |
| knowledge-service | http://192.168.2.32 | 8092 | ✅ 运行中 |
| notification-service | http://192.168.2.32 | 8094 | ✅ 运行中 |

## 登录凭据
- 用户名: `admin`
- 密码: `Admin@123456`

## 快速启动

### 生产模式 (推荐)
```bash
# 启动所有服务
java -jar D:\.openclaw\workspace\temp-project\ops-service\target\ops-service-2.2.0.jar --server.port=8083
java -jar D:\.openclaw\workspace\temp-project\workflow-service\target\workflow-service-2.2.0.jar --server.port=8091
java -jar D:\.openclaw\workspace\temp-project\knowledge-service\target\knowledge-service-2.2.0.jar --server.port=8092
java -jar D:\.openclaw\workspace\temp-project\notification-service\target\notification-service-2.2.0.jar --server.port=8094
```

### 打包命令
```bash
cd D:\.openclaw\workspace\temp-project\[service-name]
mvn clean package -DskipTests
```

## 前端配置

### .env 文件位置
`D:\.openclaw\workspace\temp-project\frontend\.env`

### 当前配置
```
VITE_API_BASE_URL=http://localhost:8083
VITE_TOKEN_KEY=ops_token
```

### 构建部署
```bash
cd D:\.openclaw\workspace\temp-project\frontend
npm run build
Copy-Item -Recurse dist\* D:\temp\frontend\
```

## 端口规划

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | 前端页面 |
| 8080 | Gateway | API 网关 (可选) |
| 8081 | user-service | 用户服务 |
| 8083 | ops-service | 运维服务 |
| 8091 | workflow-service | 工作流服务 |
| 8092 | knowledge-service | 知识库服务 |
| 8093 | chat-service | AI对话服务 |
| 8094 | notification-service | 通知服务 |

## 数据库

- 主机: 8.137.116.121
- 端口: 8432
- 数据库: business_db
- 用户: business
- 密码: NewPass2024

## API 端点

### ops-service (8083)
| API | 方法 | 说明 |
|-----|------|------|
| `/ops/auth/login` | POST | 登录 |
| `/ops/dashboard/overview` | GET | 仪表盘概览 |
| `/ops/heal/strategies` | GET | 自愈策略 |
| `/ops/releases` | GET | 灰度发布 |
| `/ops/cert/info` | GET | 证书信息 |
| `/ops/users` | GET | 用户列表 |
| `/ops/system/config` | GET | 系统配置 |

### workflow-service (8091)
| API | 方法 | 说明 |
|-----|------|------|
| `/workflow` | GET | 工作流列表 |
| `/workflow` | POST | 创建工作流 |
| `/workflow/{id}` | GET | 工作流详情 |
| `/workflow/{id}` | PUT | 更新工作流 |
| `/workflow/{id}` | DELETE | 删除工作流 |
| `/workflow/{id}/execute` | POST | 执行工作流 |

### knowledge-service (8092)
| API | 方法 | 说明 |
|-----|------|------|
| `/knowledge/bases` | GET | 知识库列表 |
| `/knowledge/bases` | POST | 创建知识库 |
| `/knowledge/bases/{id}` | PUT | 更新知识库 |
| `/knowledge/bases/{id}` | DELETE | 删除知识库 |
| `/knowledge/bases/{id}/documents` | GET | 文档列表 |
| `/knowledge/documents/{id}` | DELETE | 删除文档 |
| `/knowledge/search` | GET | 搜索 |

### notification-service (8094)
| API | 方法 | 说明 |
|-----|------|------|
| `/notification` | GET | 通知列表 |
| `/notification` | POST | 发送通知 |
| `/notification/{id}/read` | PUT | 标记已读 |
| `/notification/read-all` | PUT | 全部标记已读 |
| `/notification/unread-count` | GET | 未读数量 |

## Nginx 配置

### 配置文件
`D:\.openclaw\workspace\conf\nginx.conf`

### 前端静态文件部署位置
`D:\temp\frontend\`

## 常见问题

### Q: 数据库连接失败
- 原因: PostgreSQL 连接数满
- 解决: SSH 到服务器重启 PostgreSQL
```bash
ssh -p 2222 -i C:\Users\Administrator\aliyun_key.pem root@8.137.116.121
systemctl restart postgresql
```

### Q: 服务启动失败
- 检查端口是否被占用: `netstat -ano | findstr "端口"`
- 杀掉占用进程: `taskkill /F /PID <PID>`

### Q: JAR 文件无法运行
- 确保 POM 配置了 repackage:
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <mainClass>com.example.xxx.XXXApplication</mainClass>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
