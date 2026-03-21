# Start Backend Services Locally

param(
    [switch]$All,
    [string]$Service = ""
)

$projectRoot = "D:\.openclaw\workspace\temp-project"

$services = @(
    @{
        Name = "ops-service"
        Port = 8083
        Jar = "ops-service-2.2.0.jar"
        Description = "Ops Service"
    },
    @{
        Name = "workflow-service"
        Port = 8091
        Jar = "workflow-service-2.2.0.jar"
        Description = "Workflow Service"
    },
    @{
        Name = "knowledge-service"
        Port = 8092
        Jar = "knowledge-service-2.2.0.jar"
        Description = "Knowledge Service"
    },
    @{
        Name = "notification-service"
        Port = 8094
        Jar = "notification-service-2.2.0.jar"
        Description = "Notification Service"
    }
)

Write-Host "=== Starting Backend Services ===" -ForegroundColor Cyan
Write-Host ""

# Check if specific service requested
if ($Service) {
    $services = $services | Where-Object { $_.Name -eq $Service }
    if (-not $services) {
        Write-Host "Service not found: $Service" -ForegroundColor Red
        Write-Host "Available services: $($services.Name -join ', ')" -ForegroundColor Yellow
        exit 1
    }
}

foreach ($svc in $services) {
    $jarPath = "$projectRoot\$($svc.Name)\target\$($svc.Jar)"
    
    # Check if jar exists
    if (-not (Test-Path $jarPath)) {
        Write-Host "JAR not found: $jarPath" -ForegroundColor Red
        continue
    }
    
    # Check if port is already in use
    $portInUse = Get-NetTCPConnection -LocalPort $svc.Port -ErrorAction SilentlyContinue
    if ($portInUse) {
        Write-Host "$($svc.Name) (port $($svc.Port)): Already running" -ForegroundColor Yellow
        continue
    }
    
    Write-Host "Starting $($svc.Name) on port $($svc.Port)..." -ForegroundColor Green
    
    # Start service in background
    Start-Process -FilePath "java" -ArgumentList "-jar", $jarPath, "--server.port=$($svc.Port)" -WindowStyle Hidden
}

Write-Host ""
Write-Host "All services started!" -ForegroundColor Green

# Show status
Write-Host ""
Write-Host "Service Status:" -ForegroundColor Cyan
Write-Host "-------------------"

foreach ($svc in $services) {
    $running = Get-NetTCPConnection -LocalPort $svc.Port -ErrorAction SilentlyContinue
    if ($running) {
        Write-Host "$($svc.Name): Running on port $($svc.Port)" -ForegroundColor Green
    } else {
        Write-Host "$($svc.Name): Not running" -ForegroundColor Red
    }
}
