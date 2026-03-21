# Initialize Self-Healing Strategies

$strategies = @(
    @{
        alertType = "HIGH_CPU"
        action = "Restart Service"
        script = "taskkill /F /IM java.exe"
        enabled = $true
    },
    @{
        alertType = "HIGH_MEMORY"
        action = "Clear Cache"
        script = "redis-cli FLUSHALL"
        enabled = $true
    },
    @{
        alertType = "DISK_FULL"
        action = "Clean Logs"
        script = "Remove-Item logs/*.log -Force"
        enabled = $true
    },
    @{
        alertType = "DB_CONNECTION"
        action = "Restart DB"
        script = "systemctl restart postgresql"
        enabled = $false
    },
    @{
        alertType = "SERVICE_DOWN"
        action = "Auto Restart"
        script = "java -jar service.jar"
        enabled = $true
    }
)

Write-Host "=== Adding Self-Healing Strategies ===" -ForegroundColor Cyan

foreach ($s in $strategies) {
    Write-Host "Adding: $($s.alertType)" -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "http://localhost:8083/ops/heal/strategies" -Method Post -ContentType "application/json" -Body ($s | ConvertTo-Json) | Out-Null
        Write-Host "  OK" -ForegroundColor Green
    } catch {
        Write-Host "  FAIL: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
