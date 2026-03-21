# Auto Deployment Script
# Automated deployment with config validation

param(
    [switch]$Build,
    [switch]$Deploy,
    [switch]$GenerateReport,
    [switch]$All
)

$ProjectRoot = "D:\.openclaw\workspace\temp-project"
$Services = @(
    @{ Name = "ops-service"; Port = 8083; Jar = "ops-service-2.2.0.jar" },
    @{ Name = "workflow-service"; Port = 8091; Jar = "workflow-service-2.2.0.jar" },
    @{ Name = "knowledge-service"; Port = 8092; Jar = "knowledge-service-2.2.0.jar" },
    @{ Name = "notification-service"; Port = 8094; Jar = "notification-service-2.2.0.jar" },
    @{ Name = "chat-service"; Port = 8093; Jar = "chat-service-2.2.0.jar" }
)

function Write-Step {
    param([string]$Message)
    Write-Host "`n[STEP] $Message" -ForegroundColor Cyan
}

function Stop-ServiceByPort {
    param([int]$Port)
    
    $connections = netstat -ano | Select-String ":$Port\s+LISTENING"
    if ($connections) {
        $pid = ($connections -split "\s+")[-1]
        Write-Host "  Stopping process on port $Port (PID: $pid)" -ForegroundColor Yellow
        taskkill /F /PID $pid 2>$null
        Start-Sleep -Seconds 2
    }
}

function Build-Service {
    param([string]$ServiceName)
    
    Write-Host "  Building $ServiceName..." -ForegroundColor Green
    $servicePath = Join-Path $ProjectRoot $ServiceName
    
    Push-Location $servicePath
    mvn clean package -DskipTests -q
    $result = $LASTEXITCODE
    Pop-Location
    
    if ($result -ne 0) {
        Write-Host "  Build failed: $ServiceName" -ForegroundColor Red
        return $false
    }
    
    Write-Host "  Build success: $ServiceName" -ForegroundColor Green
    return $true
}

function Start-ServiceJar {
    param([string]$ServiceName, [int]$Port, [string]$JarName)
    
    $jarPath = Join-Path $ProjectRoot "$ServiceName\target\$JarName"
    
    if (-not (Test-Path $jarPath)) {
        Write-Host "  JAR not found: $jarPath" -ForegroundColor Red
        return $false
    }
    
    Write-Host "  Starting $ServiceName on port $Port..." -ForegroundColor Green
    
    $logPath = "$ProjectRoot\logs\$ServiceName.log"
    $logDir = Split-Path $logPath -Parent
    if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force }
    
    Start-Process -FilePath "java" -ArgumentList "-jar", $jarPath, "--server.port=$Port" -WindowStyle Hidden -RedirectStandardOutput $logPath
    
    Start-Sleep -Seconds 5
    
    # Check if service is running
    $running = netstat -ano | Select-String ":$Port\s+LISTENING"
    if ($running) {
        Write-Host "  Service started: $ServiceName (port $Port)" -ForegroundColor Green
        return $true
    } else {
        Write-Host "  Service failed to start: $ServiceName" -ForegroundColor Red
        return $false
    }
}

function Test-ServiceHealth {
    param([string]$ServiceName, [int]$Port)
    
    $url = "http://localhost:$Port/$ServiceName/health"
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            return $true
        }
    } catch {
        return $false
    }
    return $false
}

function Generate-ConfigReport {
    Write-Step "Generating Configuration Report"
    
    & "$ProjectRoot\scripts\generate-config-excel.ps1"
    
    Write-Host "  Report generated" -ForegroundColor Green
}

# Main execution
if ($All -or $Build) {
    Write-Step "Building All Services"
    
    foreach ($svc in $Services) {
        $buildResult = Build-Service -ServiceName $svc.Name
        if (-not $buildResult) {
            Write-Host "Build failed for $($svc.Name)" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host "`nAll services built successfully!" -ForegroundColor Green
}

if ($All -or $Deploy) {
    Write-Step "Deploying All Services"
    
    # Stop existing services
    Write-Host "Stopping existing services..." -ForegroundColor Yellow
    foreach ($svc in $Services) {
        Stop-ServiceByPort -Port $svc.Port
    }
    
    Start-Sleep -Seconds 3
    
    # Start services
    Write-Host "Starting services..." -ForegroundColor Yellow
    foreach ($svc in $Services) {
        Start-ServiceJar -ServiceName $svc.Name -Port $svc.Port -JarName $svc.Jar
    }
    
    # Wait for services to be ready
    Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    # Health check
    Write-Host "`nHealth Check:" -ForegroundColor Cyan
    $allHealthy = $true
    foreach ($svc in $Services) {
        $healthy = Test-ServiceHealth -ServiceName $svc.Name -Port $svc.Port
        $status = if ($healthy) { "OK" } else { "FAILED" }
        $color = if ($healthy) { "Green" } else { "Red" }
        
        Write-Host "  $($svc.Name):$($svc.Port) - $status" -ForegroundColor $color
        
        if (-not $healthy) { $allHealthy = $false }
    }
    
    if ($allHealthy) {
        Write-Host "`nAll services deployed successfully!" -ForegroundColor Green
    } else {
        Write-Host "`nSome services failed to deploy!" -ForegroundColor Red
    }
}

if ($All -or $GenerateReport) {
    Generate-ConfigReport
}

Write-Host "`n=== Deployment Complete ===" -ForegroundColor Cyan
