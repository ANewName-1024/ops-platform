# Initialize Sample Workflows

$workflows = @(
    @{
        name = "Deploy Frontend"
        description = "Build and deploy frontend to production server"
        steps = @(
            @{
                order = 1
                name = "Build Frontend"
                type = "BUILD"
                config = @{ command = "npm run build" }
            },
            @{
                order = 2
                name = "Deploy to Server"
                type = "DEPLOY"
                config = @{ target = "D:/temp/frontend" }
            },
            @{
                order = 3
                name = "Verify Deployment"
                type = "VERIFY"
                config = @{ url = "http://localhost" }
            }
        )
    },
    @{
        name = "Database Backup"
        description = "Backup PostgreSQL database to backup server"
        steps = @(
            @{
                order = 1
                name = "Create Backup"
                type = "SCRIPT"
                config = @{ command = "pg_dump" }
            },
            @{
                order = 2
                name = "Transfer to Backup Server"
                type = "TRANSFER"
                config = @{ destination = "backup-server:/backups" }
            },
            @{
                order = 3
                name = "Verify Backup"
                type = "VERIFY"
                config = @{ check = "file_size" }
            }
        )
    },
    @{
        name = "Service Restart"
        description = "Safely restart a microservice"
        steps = @(
            @{
                order = 1
                name = "Check Service Health"
                type = "CHECK"
                config = @{ endpoint = "/health" }
            },
            @{
                order = 2
                name = "Stop Service"
                type = "STOP"
                config = @{ service = "java" }
            },
            @{
                order = 3
                name = "Start Service"
                type = "START"
                config = @{ command = "java -jar service.jar" }
            },
            @{
                order = 4
                name = "Verify Health"
                type = "VERIFY"
                config = @{ endpoint = "/health" }
            }
        )
    },
    @{
        name = "SSL Certificate Renewal"
        description = "Renew SSL certificate and deploy to servers"
        steps = @(
            @{
                order = 1
                name = "Request New Certificate"
                type = "SCRIPT"
                config = @{ command = "certbot renew" }
            },
            @{
                order = 2
                name = "Deploy to Nginx"
                type = "DEPLOY"
                config = @{ path = "/etc/nginx/ssl" }
            },
            @{
                order = 3
                name = "Reload Nginx"
                type = "SCRIPT"
                config = @{ command = "nginx -s reload" }
            },
            @{
                order = 4
                name = "Verify Certificate"
                type = "VERIFY"
                config = @{ check = "ssl_expiry" }
            }
        )
    },
    @{
        name = "Log Cleanup"
        description = "Clean old logs to free disk space"
        steps = @(
            @{
                order = 1
                name = "List Old Logs"
                type = "CHECK"
                config = @{ path = "logs/" }
            },
            @{
                order = 2
                name = "Archive Logs"
                type = "ARCHIVE"
                config = @{ destination = "archive/" }
            },
            @{
                order = 3
                name = "Delete Old Files"
                type = "DELETE"
                config = @{ days = 30 }
            }
        )
    }
)

Write-Host "=== Initializing Sample Workflows ===" -ForegroundColor Cyan

foreach ($wf in $workflows) {
    Write-Host "Creating: $($wf.name)" -ForegroundColor Yellow
    try {
        $body = @{
            name = $wf.name
            description = $wf.description
            steps = $wf.steps
        } | ConvertTo-Json -Depth 10
        
        Invoke-RestMethod -Uri "http://localhost:8091/workflow" -Method Post -ContentType "application/json" -Body $body | Out-Null
        Write-Host "  OK" -ForegroundColor Green
    } catch {
        Write-Host "  FAIL: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
