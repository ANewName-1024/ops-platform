@echo off
REM OPS 平台服务健康检查脚本
REM 使用方法: health-check.bat [interval]
REM interval: 检查间隔(秒)，默认 30

setlocal enabledelayedexpansion

set SERVICE_NAME=ops-service
set PORT=8083
set CHECK_INTERVAL=30
if not "%~1"=="" set CHECK_INTERVAL=%~1

echo ========================================
echo   OPS 平台服务健康检查
echo ========================================
echo 检查间隔: %CHECK_INTERVAL% 秒
echo 后端端口: %PORT%
echo.
echo 按 Ctrl+C 停止检查
echo ========================================

:check_loop
REM 获取当前时间
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do (
    set CURRENT_DATE=%%a/%%b/%%c
)
for /f "tokens=1-2 delims=: " %%a in ('time /t') do (
    set CURRENT_TIME=%%a:%%b
)

REM 检查端口
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo [%CURRENT_DATE% %CURRENT_TIME%] [错误] 服务未运行，尝试启动...
    call start-prod.bat
) else (
    REM 健康检查
    curl -s http://localhost:%PORT%/ops/health >nul 2>&1
    if errorlevel 1 (
        echo [%CURRENT_DATE% %CURRENT_TIME%] [警告] 健康检查失败
    ) else (
        echo [%CURRENT_DATE% %CURRENT_TIME%] [正常] 服务运行中
    )
)

REM 等待下次检查
timeout /t %CHECK_INTERVAL% /nobreak >nul
goto check_loop

endlocal
