# API Functional Test Script
# Run API tests using curl

param(
    [string]$BaseUrl = "http://localhost:8083",
    [string]$Token = ""
)

$Green = "#[92m"
$Yellow = "#[93m"
$Red = "#[91m"
$Reset = "#[0m"

function Write-TestResult {
    param([string]$Name, [bool]$Pass, [string]$Message = "")
    
    if ($Pass) {
        Write-Host "[PASS] $Name" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $Name - $Message" -ForegroundColor Red
    }
}

# Get token if not provided
if (-not $Token) {
    Write-Host "Getting auth token..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/auth/login" -Method Post -ContentType "application/json" -Body (@{username="admin";password="Admin@123456"} | ConvertTo-Json)
    $Token = $response.token
    Write-Host "Token obtained: $($Token.Substring(0, 20))..." -ForegroundColor Green
}

$headers = @{
    "Authorization" = "Bearer $Token"
    "Content-Type" = "application/json"
}

$passed = 0
$failed = 0

Write-Host "`n=== OPS Platform API Tests ===" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl`n" -ForegroundColor Yellow

# ==================== Auth Tests ====================

Write-Host "--- Auth Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/auth/login" -Method Post -ContentType "application/json" -Body (@{username="admin";password="wrongpass"} | ConvertTo-Json) -ErrorAction SilentlyContinue
    Write-TestResult "AUTH-001 Wrong Password" $false "Should fail"
    $failed++
} catch {
    Write-TestResult "AUTH-001 Wrong Password" $true
    $passed++
}

# ==================== Dashboard Tests ====================

Write-Host "`n--- Dashboard Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/dashboard/overview" -Method Get -Headers $headers
    if ($response) {
        Write-TestResult "DASH-001 Dashboard Overview" $true
        $passed++
    }
} catch {
    Write-TestResult "DASH-001 Dashboard Overview" $false $_.Exception.Message
    $failed++
}

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/dashboard/actions" -Method Get -Headers $headers
    Write-TestResult "DASH-002 Dashboard Actions" $true
    $passed++
} catch {
    Write-TestResult "DASH-002 Dashboard Actions" $false $_.Exception.Message
    $failed++
}

# ==================== Workflow Tests ====================

Write-Host "`n--- Workflow Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8091/workflow" -Method Get
    Write-TestResult "WORK-001 List Workflows" $true
    $passed++
} catch {
    Write-TestResult "WORK-001 List Workflows" $false $_.Exception.Message
    $failed++
}

try {
    $body = @{
        name = "Test Workflow"
        description = "API Test"
    } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8091/workflow" -Method Post -Headers $headers -Body $body
    Write-TestResult "WORK-002 Create Workflow" $true
    $passed++
} catch {
    Write-TestResult "WORK-002 Create Workflow" $false $_.Exception.Message
    $failed++
}

# ==================== Knowledge Tests ====================

Write-Host "`n--- Knowledge Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8092/knowledge/bases" -Method Get
    Write-TestResult "KNOW-001 List Knowledge Bases" $true
    $passed++
} catch {
    Write-TestResult "KNOW-001 List Knowledge Bases" $false $_.Exception.Message
    $failed++
}

try {
    $body = @{
        name = "Test KB"
        description = "API Test"
    } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8092/knowledge/bases" -Method Post -Headers $headers -Body $body
    Write-TestResult "KNOW-002 Create Knowledge Base" $true
    $passed++
} catch {
    Write-TestResult "KNOW-002 Create Knowledge Base" $false $_.Exception.Message
    $failed++
}

# ==================== Notification Tests ====================

Write-Host "`n--- Notification Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8094/notification" -Method Get
    Write-TestResult "NOTI-001 List Notifications" $true
    $passed++
} catch {
    Write-TestResult "NOTI-001 List Notifications" $false $_.Exception.Message
    $failed++
}

try {
    $body = @{
        title = "Test Notification"
        content = "API Test"
    } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "http://localhost:8094/notification" -Method Post -Headers $headers -Body $body
    Write-TestResult "NOTI-002 Send Notification" $true
    $passed++
} catch {
    Write-TestResult "NOTI-002 Send Notification" $false $_.Exception.Message
    $failed++
}

# ==================== AutoHeal Tests ====================

Write-Host "`n--- AutoHeal Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/heal/strategies" -Method Get -Headers $headers
    Write-TestResult "HEAL-001 List Strategies" $true
    $passed++
} catch {
    Write-TestResult "HEAL-001 List Strategies" $false $_.Exception.Message
    $failed++
}

# ==================== GrayRelease Tests ====================

Write-Host "`n--- GrayRelease Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/releases" -Method Get -Headers $headers
    Write-TestResult "GRAY-001 List Releases" $true
    $passed++
} catch {
    Write-TestResult "GRAY-001 List Releases" $false $_.Exception.Message
    $failed++
}

# ==================== Certificate Tests ====================

Write-Host "`n--- Certificate Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/cert/info" -Method Get -Headers $headers
    Write-TestResult "CERT-001 Certificate Info" $true
    $passed++
} catch {
    Write-TestResult "CERT-001 Certificate Info" $false $_.Exception.Message
    $failed++
}

# ==================== User Tests ====================

Write-Host "`n--- User Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/users" -Method Get -Headers $headers
    Write-TestResult "USER-001 List Users" $true
    $passed++
} catch {
    Write-TestResult "USER-001 List Users" $false $_.Exception.Message
    $failed++
}

# ==================== System Config Tests ====================

Write-Host "`n--- System Config Tests ---" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/ops/system/config" -Method Get -Headers $headers
    Write-TestResult "SYS-001 Get System Config" $true
    $passed++
} catch {
    Write-TestResult "SYS-001 Get System Config" $false $_.Exception.Message
    $failed++
}

# ==================== Summary ====================

Write-Host "`n=== Test Summary ===" -ForegroundColor Cyan
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red

if ($failed -eq 0) {
    Write-Host "`nAll tests passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`nSome tests failed!" -ForegroundColor Red
    exit 1
}
