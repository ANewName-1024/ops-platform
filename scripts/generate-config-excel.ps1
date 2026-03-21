# Configuration Excel Generator
# Auto scan project configs and generate Excel inventory

param(
    [string]$OutputPath = "D:\.openclaw\workspace\temp-project\docs\CONFIG_INVENTORY.xlsx",
    [string]$ProjectRoot = "D:\.openclaw\workspace\temp-project"
)

# Sensitive keywords
$SensitiveKeywords = @(
    "password", "passwd", "pwd", "secret", "token", "key", 
    "apikey", "api_key", "access_key", "private_key",
    "credential", "auth", "jwt_secret", "encryption_key"
)

# Config file extensions
$ConfigExtensions = @("*.yml", "*.yaml", "*.properties", "*.json")

# Service list
$Services = @{
    "ops-service" = @{
        "Port" = 8083
        "Description" = "Ops Service"
    }
    "workflow-service" = @{
        "Port" = 8091
        "Description" = "Workflow Service"
    }
    "knowledge-service" = @{
        "Port" = 8092
        "Description" = "Knowledge Service"
    }
    "notification-service" = @{
        "Port" = 8094
        "Description" = "Notification Service"
    }
    "chat-service" = @{
        "Port" = 8093
        "Description" = "Chat Service"
    }
}

function Get-ParameterType {
    param([string]$Value)
    
    if ($Value -match "^\d+$") { return "Number" }
    if ($Value -match "^(true|false)$") { return "Boolean" }
    if ($Value -match "^\{.*\}$") { return "JSON" }
    if ($Value -match "^/.*/$") { return "Path" }
    return "String"
}

function Test-IsSensitive {
    param([string]$Key, [string]$Value)
    
    $keyLower = $Key.ToLower()
    
    foreach ($keyword in $SensitiveKeywords) {
        if ($keyLower -match $keyword) { return $true }
    }
    
    if ($Value.Length -gt 20 -and $Value -match "^[A-Za-z0-9+/=]+$") { return $true }
    
    return $false
}

function Get-MaskedValue {
    param([string]$Value)
    
    if ($Value.Length -le 4) { return "****" }
    return $Value.Substring(0, 2) + "****" + $Value.Substring($Value.Length - 2)
}

function Parse-YamlContent {
    param([string]$Content, [string]$FileName)
    
    $params = @()
    $lines = $Content -split "`n"
    $currentPath = ""
    
    foreach ($line in $lines) {
        if ($line -match "^\s*#" -or $line -match "^\s*$") { continue }
        
        if ($line -match "^\s*([^:]+):\s*(.*)$") {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            
            if ($value -match "^\[.*\]$") {
                $value = "[Array]"
            }
            
            if ($value -ne "") {
                $fullKey = if ($currentPath) { "$currentPath.$key" } else { $key }
                
                $isSensitive = Test-IsSensitive -Key $key -Value $value
                
                $params += [PSCustomObject]@{
                    File = $FileName
                    Parameter = $fullKey
                    Value = if ($isSensitive) { Get-MaskedValue -Value $value } else { $value }
                    OriginalValue = $value
                    Type = Get-ParameterType -Value $value
                    IsSensitive = $isSensitive
                    Description = ""
                }
            }
        }
    }
    
    return $params
}

# Scan config files
Write-Host "Scanning configuration files..." -ForegroundColor Cyan

$allParams = @()

foreach ($service in $Services.Keys) {
    $servicePath = Join-Path $ProjectRoot $service
    
    if (Test-Path $servicePath) {
        Write-Host "Scanning: $service" -ForegroundColor Green
        
        foreach ($ext in $ConfigExtensions) {
            $files = Get-ChildItem -Path $servicePath -Filter $ext -Recurse -ErrorAction SilentlyContinue
            
            foreach ($file in $files) {
                $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
                if (-not $content) { continue }
                
                $relativePath = $file.FullName.Replace($ProjectRoot, "").Replace("\", "/")
                
                if ($file.Extension -eq ".yml" -or $file.Extension -eq ".yaml") {
                    $parsed = Parse-YamlContent -Content $content -FileName $relativePath
                    $allParams += $parsed
                }
            }
        }
    }
}

# Add service info
$serviceInfo = @()
foreach ($service in $Services.Keys) {
    $serviceInfo += [PSCustomObject]@{
        File = "System"
        Parameter = "$service.port"
        Value = $Services[$service].Port
        OriginalValue = $Services[$service].Port
        Type = "Number"
        IsSensitive = $false
        Description = $Services[$service].Description
    }
}

# Merge data
$allData = $serviceInfo + $allParams

# Export to CSV (Excel compatible)
$csvPath = $OutputPath -replace "\.xlsx$", ".csv"
$allData | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8

Write-Host "CSV file generated: $csvPath" -ForegroundColor Green
Write-Host "Total parameters: $($allData.Count)" -ForegroundColor Cyan
Write-Host "Sensitive parameters: $(($allData | Where-Object { $_.IsSensitive }).Count)" -ForegroundColor Yellow
Write-Host "Done!" -ForegroundColor Green
