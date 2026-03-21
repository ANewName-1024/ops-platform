# Knowledge Base Initialization Script

$data = Get-Content "D:\.openclaw\workspace\temp-project\docs\knowledge-sample.json" | ConvertFrom-Json

Write-Host "=== Initializing Knowledge Base ===" -ForegroundColor Cyan

foreach ($kb in $data.knowledgeBases) {
    Write-Host "Creating KB: $($kb.name)" -ForegroundColor Yellow
    
    $kbResult = Invoke-RestMethod -Uri "http://localhost:8092/knowledge/bases" -Method Post -ContentType "application/json" -Body (@{
        name = $kb.name
        description = $kb.description
    } | ConvertTo-Json)
    
    $kbId = $kbResult.id
    Write-Host "  KB ID: $kbId" -ForegroundColor Green
    
    foreach ($doc in $kb.documents) {
        Write-Host "  Adding: $($doc.title)" -ForegroundColor Cyan
        try {
            Invoke-RestMethod -Uri "http://localhost:8092/knowledge/bases/$kbId/documents" -Method Post -ContentType "application/json" -Body (@{
                title = $doc.title
                content = $doc.content
                type = $doc.type
            } | ConvertTo-Json) | Out-Null
            Write-Host "    OK" -ForegroundColor Green
        } catch {
            Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
