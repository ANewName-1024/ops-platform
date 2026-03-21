@echo off
REM OPS 平台生产环境启动脚本
REM 使用方法: start-prod.bat

echo ========================================
echo   OPS 平台生产环境启动
echo ========================================

setlocal

REM 配置
set SERVICE_NAME=ops-service
set JAR_FILE=D:\.openclaw\workspace\temp-project\ops-service\target\ops-service-2.2.0.jar
set PORT=8083
set LOG_FILE=D:\.openclaw\workspace\temp-project\ops-service\logs\ops-service.log

REM 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java，请先安装 JDK
    pause
    exit /b 1
)

REM 检查 JAR 文件
if not exist "%JAR_FILE%" (
    echo [错误] JAR 文件不存在: %JAR_FILE%
    echo 请先运行: mvn clean package -DskipTests
    pause
    exit /b 1
)

REM 创建日志目录
if not exist "D:\.openclaw\workspace\temp-project\ops-service\logs" (
    mkdir "D:\.openclaw\workspace\temp-project\ops-service\logs"
)

REM 杀掉旧进程
echo [1/4] 检查并停止旧进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    echo 停止旧进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

REM 启动服务
echo [2/4] 启动服务...
start "ops-service" cmd /k "cd /d D:\.openclaw\workspace\temp-project\ops-service && java -jar %JAR_FILE% --server.port=%PORT%"

REM 等待启动
echo [3/4] 等待服务启动...
set /a count=0
:wait_loop
timeout /t 2 /nobreak >nul
set /a count+=1
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    if %count% LSS 20 (
        goto wait_loop
    )
    echo [警告] 服务启动超时，但仍可能正在启动
) else (
    echo [成功] 服务已启动
)

REM 健康检查
echo [4/4] 执行健康检查...
curl -s http://localhost:%PORT%/ops/health >nul 2>&1
if errorlevel 1 (
    echo [警告] 健康检查失败，请访问 http://localhost:%PORT%/actuator/health 查看状态
) else (
    echo [成功] 健康检查通过
)

echo.
echo ========================================
echo   服务启动完成
echo ========================================
echo   后端地址: http://localhost:%PORT%
echo   前端地址: http://localhost
echo   日志文件: %LOG_FILE%
echo ========================================

endlocal
pause
