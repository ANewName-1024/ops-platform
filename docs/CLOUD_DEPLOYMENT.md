# 混合云部署方案

## 1. 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                        互联网                                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              │         CDN / 域名           │
              └──────────────┬──────────────┘
                             │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│  云服务器      │   │  云服务器      │   │  云服务器      │
│  (前端+Nginx) │   │  (PostgreSQL) │   │  (对象存储)   │
│  8.137.116.121│   │  8.137.116.121│   │               │
│  :80, :443    │   │  :8432        │   │               │
└───────────────┘   └───────────────┘   └───────────────┘
        │                     │
        │    SSH 隧道         │
        │    (可选)          │
        └─────────────────────┘
              │
              ▼
┌───────────────┐
│  本地服务器    │
│  (后端服务)   │
│  192.168.2.32 │
│  :8083, :8091 │
│  :8092, :8094 │
└───────────────┘
```

## 2. 部署方案

### 2.1 前端部署 (云服务器)

| 组件 | 位置 | 端口 |
|------|------|------|
| Nginx | 云服务器 | 80, 443 |
| 前端静态文件 | /var/www/html/frontend | - |

### 2.2 后端部署 (本地服务器)

| 服务 | 端口 | 说明 |
|------|------|------|
| ops-service | 8083 | 运维服务 |
| workflow-service | 8091 | 工作流服务 |
| knowledge-service | 8092 | 知识库服务 |
| notification-service | 8094 | 通知服务 |

### 2.3 数据库 (云服务器)

| 组件 | 位置 | 端口 |
|------|------|------|
| PostgreSQL | 云服务器 | 8432 |

## 3. 实现步骤

### 3.1 云服务器配置 (8.137.116.121)

```bash
# 1. 安装 Nginx
apt update
apt install nginx

# 2. 创建前端目录
mkdir -p /var/www/html/frontend

# 3. 配置 Nginx
cat > /etc/nginx/sites-available/ops-platform << 'EOF'
server {
    listen 80;
    server_name ops.yourdomain.com;
    
    location / {
        root /var/www/html/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理到本地服务器 (可选，通过内网穿透)
    location /api/ {
        proxy_pass http://192.168.2.32:8083/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# 4. 启用配置
ln -s /etc/nginx/sites-available/ops-platform /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx
```

### 3.2 本地服务器配置

```powershell
# 1. 构建前端
cd D:\.openclaw\workspace\temp-project\frontend
npm run build

# 2. 上传到云服务器
scp -P 2222 -r dist/* root@8.137.116.121:/var/www/html/frontend/

# 3. 启动后端服务
java -jar ops-service-2.2.0.jar --server.port=8083
java -jar workflow-service-2.2.0.jar --server.port=8091
java -jar knowledge-service-2.2.0.jar --server.port=8092
java -jar notification-service-2.2.0.jar --server.port=8094
```

## 4. 内网穿透方案

### 4.1 使用 frp (推荐)

**云服务器 (frps)**:
```bash
# 安装 frp
wget https://github.com/fatedier/frp/releases/download/v0.51.0/frp_0.51.0_linux_amd64.tar.gz
tar -xzf frp_0.51.0_linux_amd64.tar.gz

# 配置 frps.ini
[common]
bind_port = 7000
vhost_http_port = 8080

# 启动
./frps -c frps.ini
```

**本地服务器 (frpc)**:
```ini
# 配置 frpc.ini
[common]
server_addr = 8.137.116.121
server_port = 7000

[ops-service]
type = http
local_port = 8083
custom_domains = ops.yourdomain.com

[workflow-service]
type = http
local_port = 8091
custom_domains = workflow.yourdomain.com
```

### 4.2 使用 Cloudflare Tunnel

```bash
# 安装 cloudflared
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /usr/local/bin/cloudflared
chmod +x /usr/local/bin/cloudflared

# 登录
cloudflared tunnel login

# 创建隧道
cloudflared tunnel create ops-platform

# 配置隧道
cloudflared tunnel route dns ops-platform ops.yourdomain.com

# 启动
cloudflared tunnel run --url http://localhost:8083
```

## 5. 部署脚本

### 5.1 部署前端到云端

```powershell
# deploy-frontend.ps1
param(
    [string]$Server = "8.137.116.121",
    [int]$Port = 2222,
    [string]$User = "root",
    [string]$RemotePath = "/var/www/html/frontend"
)

$localPath = "D:\.openclaw\workspace\temp-project\frontend\dist"

Write-Host "=== Deploying Frontend to Cloud Server ===" -ForegroundColor Cyan

# 1. 构建前端
Write-Host "Building frontend..." -ForegroundColor Yellow
Push-Location "D:\.openclaw\workspace\temp-project\frontend"
npm run build
Pop-Location

# 2. 上传到云服务器
Write-Host "Uploading to server..." -ForegroundColor Yellow
$files = Get-ChildItem -Path $localPath -Recurse -File
foreach ($file in $files) {
    $relativePath = $file.FullName.Replace($localPath, "").Replace("\", "/")
    $remoteFile = "$RemotePath$relativePath"
    
    Write-Host "  Uploading: $relativePath" -ForegroundColor Cyan
    scp -P $Port $file.FullName "$User@$Server`:$remoteFile"
}

Write-Host ""
Write-Host "Frontend deployed successfully!" -ForegroundColor Green
Write-Host "Access: http://$Server" -ForegroundColor Cyan
```

### 5.2 启动本地后端服务

```powershell
# start-backend.ps1
$services = @(
    @{ Name = "ops-service"; Port = 8083; Jar = "ops-service-2.2.0.jar" },
    @{ Name = "workflow-service"; Port = 8091; Jar = "workflow-service-2.2.0.jar" },
    @{ Name = "knowledge-service"; Port = 8092; Jar = "knowledge-service-2.2.0.jar" },
    @{ Name = "notification-service"; Port = 8094; Jar = "notification-service-2.2.0.jar" }
)

$projectRoot = "D:\.openclaw\workspace\temp-project"

foreach ($svc in $services) {
    $jarPath = "$projectRoot\$($svc.Name)\target\$($svc.Jar)"
    
    Write-Host "Starting $($svc.Name) on port $($svc.Port)..." -ForegroundColor Yellow
    
    Start-Process -FilePath "java" -ArgumentList "-jar", $jarPath, "--server.port=$($svc.Port)" -WindowStyle Hidden
}

Write-Host "All services started!" -ForegroundColor Green
```

## 6. 访问方式

### 方案A: 前端直连本地后端 (当前)

- 前端: http://192.168.2.32 (本地Nginx)
- 后端: 本地服务端口

### 方案B: 前端部署到云端 + 内网穿透

- 前端: http://8.137.116.121 (云端Nginx)
- 后端: 通过 frp 暴露到云端域名

### 方案C: 混合部署

- 前端 + Nginx: 云服务器 (8.137.116.121)
- 后端API: 本地服务器 (192.168.2.32)
- 数据库: 云服务器 (8.137.116.121:8432)

## 7. 安全考虑

1. **HTTPS**: 使用 Let's Encrypt 配置 SSL 证书
2. **防火墙**: 仅开放必要端口 (80, 443, 2222)
3. **认证**: 前端通过 JWT Token 访问后端
4. **内网穿透**: 使用 frp 认证和加密传输
