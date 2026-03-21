# Cloud Deployment Scripts

## 1. Deploy Frontend to Cloud Server

param(
    [string]$Server = "8.137.116.121",
    [int]$SshPort = 2222,
    [string]$User = "root",
    [string]$RemotePath = "/var/www/html/frontend"
)

$projectRoot = "D:\.openclaw\workspace\temp-project"
$frontendPath = "$projectRoot\frontend"
$distPath = "$frontendPath\dist"

Write-Host "=== Deploy Frontend to Cloud Server ===" -ForegroundColor Cyan

# Step 1: Build frontend
Write-Host "1. Building frontend..." -ForegroundColor Yellow
Push-Location $frontendPath
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}
Pop-Location
Write-Host "Build completed!" -ForegroundColor Green

# Step 2: Check dist folder
if (-not (Test-Path $distPath)) {
    Write-Host "Dist folder not found!" -ForegroundColor Red
    exit 1
}

# Step 3: Upload to cloud server
Write-Host "2. Uploading to cloud server..." -ForegroundColor Yellow

# Create remote directory
$createDirCmd = "mkdir -p $RemotePath"
ssh -p $SshPort -i C:\Users\Administrator\aliyun_key.pem $User@$Server $createDirCmd

# Upload files
$files = Get-ChildItem -Path $distPath -Recurse -File
$total = $files.Count
$current = 0

foreach ($file in $files) {
    $relativePath = $file.FullName.Replace($distPath, "").Replace("\", "/")
    $remoteFile = "$RemotePath$relativePath"
    $remoteDir = Split-Path $remoteFile -Parent
    
    $current++
    Write-Host "  [$current/$total] $relativePath" -ForegroundColor Cyan
    
    # Create directory if needed
    ssh -p $SshPort -i C:\Users\Administrator\aliyun_key.pem $User@$Server "mkdir -p $remoteDir" 2>$null
    
    # Upload file
    scp -P $SshPort -i C:\Users\Administrator\aliyun_key.pem $file.FullName "$User@$Server`:$remoteFile"
}

Write-Host ""
Write-Host "3. Testing deployment..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://$Server" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "Deployment successful!" -ForegroundColor Green
        Write-Host "Access: http://$Server" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Warning: Could not verify deployment" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Done ===" -ForegroundColor Green
