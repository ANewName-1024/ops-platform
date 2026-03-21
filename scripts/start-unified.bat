@echo off
REM ========================================
REM   OPS 平台统一启动脚本 (架构方案)
REM ========================================
REM 启动顺序：
REM   1. 端口检测与清理
REM   2. Nginx (端口 80)
REM   3. Gateway (端口 8080)
REM   4. 各微服务 (8081/8083/8092)
REM ========================================

echo.
echo ========================================
echo   OPS Platform Unified Launcher
echo ========================================
echo.

setlocal enabledelayedexpansion

REM === 配置 ===
set GATEWAY_PORT=8080
set OPS_PORT=8083

REM === Step 1: 端口检测 ===
echo [Step 1/4] Checking ports...

REM 检查 Gateway 端口
netstat -ano | findstr ":%GATEWAY_PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo   [WARNING] Port %GATEWAY_PORT% (Gateway) is in use
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%GATEWAY_PORT% " ^| findstr "LISTENING"') do (
        echo   [ACTION] Killing PID %%a
        taskkill /F /PID %%a >nul 2>&1
    )
) else (
    echo   [OK] Port %GATEWAY_PORT% (Gateway) is free
)

REM 检查 OPS 端口
netstat -ano | findstr ":%OPS_PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo   [WARNING] Port %OPS_PORT% (OPS) is in use
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%OPS_PORT% " ^| findstr "LISTENING"') do (
        echo   [ACTION] Killing PID %%a
        taskkill /F /PID %%a >nul 2>&1
    )
) else (
    echo   [OK] Port %OPS_PORT% (OPS) is free
)

REM 等待端口释放
ping -n 3 127.0.0.1 >nul 2>&1

REM === Step 2: 启动 Nginx ===
echo [Step 2/4] Starting Nginx...
where nginx >nul 2>&1
if %errorlevel%==0 (
    nginx
    echo   [OK] Nginx started
) else (
    echo   [SKIP] Nginx not found, assuming running
)

REM === Step 3: 启动 Gateway ===
echo [Step 3/4] Starting Gateway...
start "gateway" cmd /k "cd /d D:\.openclaw\workspace\temp-project\gateway && mvn spring-boot:run -DskipTests"

REM === Step 4: 启动 OPS Service ===
echo [Step 4/4] Starting OPS Service...
start "ops-service" cmd /k "cd /d D:\.openclaw\workspace\temp-project\ops-service && mvn spring-boot:run -DskipTests"

echo.
echo ========================================
echo   Waiting for services to start...
echo ========================================

REM 等待服务启动
set /a count=0
:wait_gateway
set /a count+=1
if %count% GTR 20 (
    echo   [WARNING] Gateway startup timeout
    goto wait_ops
)
netstat -ano | findstr ":%GATEWAY_PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo   [OK] Gateway is ready (port %GATEWAY_PORT%)
    goto wait_ops
)
ping -n 2 127.0.0.1 >nul 2>&1
echo   Waiting for Gateway... !count!/20
goto wait_gateway

:wait_ops
set /a count=0
:wait_ops_loop
set /a count+=1
if %count% GTR 20 (
    echo   [WARNING] OPS Service startup timeout
    goto done
)
netstat -ano | findstr ":%OPS_PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo   [OK] OPS Service is ready (port %OPS_PORT%)
    goto done
)
ping -n 2 127.0.0.1 >nul 2>&1
echo   Waiting for OPS Service... !count!/20
goto wait_ops_loop

:done
echo.
echo ========================================
echo   All services started!
echo ========================================
echo.
echo   Access Points:
echo   - Frontend:  http://localhost/
echo   - Gateway:   http://localhost:8080
echo   - OPS API:   http://localhost:8083/ops
echo.
echo   API Endpoints (via Gateway):
echo   - /ops/**         -> OPS Service
echo   - /user/**       -> User Service
echo   - /knowledge/**  -> Knowledge Service
echo ========================================
echo.

endlocal
pause
