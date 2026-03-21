# OPS 平台配置文档

## 架构概览

```
┌─────────────────────────────────────────────────┐
│                  用户浏览器                      │
└─────────────────┬───────────────────────────────┘
                  │ http://localhost:80
                  ▼
┌─────────────────────────────────────────────────┐
│  Nginx (端口 80)                                 │
│  - 静态文件服务                                  │
│  - API 代理到 Gateway                            │
└─────────────────┬───────────────────────────────┘
                  │ /ops/** -> localhost:8083
                  ▼
┌─────────────────────────────────────────────────┐
│  Gateway (端口 8080)                             │
│  - 统一入口                                      │
│  - 路由到各微服务                                │
└─────────────────┬───────────────────────────────┘
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
   ┌────────┐ ┌────────┐ ┌────────┐
   │  OPS   │ │ User   │ │Knowl-  │
   │ :8083  │ │ :8081  │ │edge:8092│
   └────────┘ └────────┘ └────────┘
```

## 服务地址

| 服务 | 地址 | 端口 | 状态 |
|------|------|------|------|
| 前端 (Nginx) | http://192.168.2.32 | 80 | 需保持运行 |
| 后端 (ops-service) | http://192.168.2.32:8083 | 8083 | 需保持运行 |

## 登录凭据
- 用户名: `admin`
- 密码: `Admin@123456`

## 快速启动

### 开发模式
```bash
cd D:\.openclaw\workspace\temp-project\ops-service
mvn spring-boot:run -DskipTests
```

### 生产模式 (推荐)
```bash
D:\.openclaw\workspace\temp-project\scripts\start-prod.bat
```

### 启动顺序
1. 确保 Nginx 运行中 (端口 80)
2. 运行生产启动脚本
3. 访问 http://192.168.2.32

## 前端配置

### .env 文件位置
`D:\.openclaw\workspace\temp-project\frontend\.env`

### 正确配置
```
# API 通过 nginx 代理
VITE_API_BASE_URL=/ops
```

### ⚠️ 注意事项
- **不要**使用 `http://192.168.2.32:8083` - 会导致跨域问题
- **必须**使用 `/ops` - 通过 nginx 代理到后端

## 服务管理脚本

### 脚本位置
`D:\.openclaw\workspace\temp-project\scripts\`

| 脚本 | 功能 |
|------|------|
| start-prod.bat | 生产模式启动 (JAR 包运行，更稳定) |
| start-services.bat | 开发模式启动 (Maven 运行) |
| health-check.bat | 健康检查 (自动检测服务状态) |

### 使用健康检查
```bash
D:\.openclaw\workspace\temp-project\scripts\health-check.bat 30
```
参数 30 表示每 30 秒检查一次

## Nginx 配置

### 配置文件
`D:\.openclaw\workspace\conf\nginx.conf`

### 核心配置
```nginx
# 前端静态文件
location / {
    root   D:/temp/frontend;
    index  index.html;
    try_files $uri $uri/ /index.html;
}

# API 代理到后端
location /ops/ {
    proxy_pass http://127.0.0.1:8083/ops/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

## 服务状态检查

### 检查后端
```bash
netstat -ano | findstr "8083"
```

### 检查 Nginx
```bash
netstat -ano | findstr ":80"
```

### 测试登录
```bash
curl -X POST http://localhost:8083/ops/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"Admin@123456\"}"
```

### 测试前端页面
```bash
curl http://localhost
```

## 端口管理

### 端口使用规范

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | 前端页面 |
| 8080 | Gateway | API 网关 |
| 8081 | User Service | 用户服务 |
| 8083 | OPS Service | 运维服务 |
| 8092 | Knowledge Service | 知识库服务 |

### 端口管理脚本

```bash
# 列出所有相关端口
D:\.openclaw\workspace\temp-project\scripts\port-manager.bat list

# 检查端口占用
D:\.openclaw\workspace\temp-project\scripts\port-manager.bat check 8083

# 杀掉占用端口的进程
D:\.openclaw\workspace\temp-project\scripts\port-manager.bat kill 8083
```

### 智能启动脚本

推荐使用 `start-smart.bat`，会自动检测并清理端口冲突：

```bash
D:\.openclaw\workspace\temp-project\scripts\start-smart.bat
```

### 避免端口冲突的原则

1. **启动前检查** - 始终使用端口管理脚本检查占用
2. **单一入口** - 使用统一的启动脚本
3. **正确关闭** - 先杀进程再启动新服务
4. **等待释放** - 杀掉进程后等待 2-3 秒再启动

## 常见问题

### Q: 登录失败怎么办？
1. 检查后端是否运行: `netstat -ano | findstr "8083"`
2. 检查 .env 配置是否为 `/ops`
3. 重启后端服务

### Q: 页面加载失败？
1. 检查 Nginx 是否运行
2. 检查前端文件是否部署: `D:\temp\frontend\index.html`
3. 查看浏览器控制台错误

### Q: 后端服务总是停止？
1. 使用生产模式启动 (start-prod.bat)
2. 运行健康检查脚本 (health-check.bat)

### Q: 端口被占用怎么办？
1. 运行 `port-manager.bat kill 8083` 杀掉占用进程
2. 或使用 `start-smart.bat` 自动清理
