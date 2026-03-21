@echo off
REM OPS 平台服务启动脚本
REM 使用方法: start-services.bat

echo ========================================
echo   OPS 平台服务启动
echo ========================================

REM 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java，请先安装 JDK
    pause
    exit /b 1
)

REM 启动后端服务
echo [1/1] 启动 ops-service...
start "ops-service" cmd /k "cd /d D:\.openclaw\workspace\temp-project\ops-service && mvn spring-boot:run -DskipTests"

echo.
echo 服务启动中，请等待...
timeout /t 10 /nobreak >nul

REM 检查服务状态
netstat -ano | findstr "8083" >nul
if errorlevel 1 (
    echo [警告] 服务可能未启动成功，请检查
) else (
    echo.
    echo ========================================
    echo   服务启动成功！
    echo ========================================
    echo   后端: http://localhost:8083
    echo   前端: http://localhost
)

pause
