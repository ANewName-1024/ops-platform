@echo off
REM OPS 平台智能启动脚本 (带端口检测)
REM 使用方法: start-smart.bat

echo ========================================
echo   OPS 平台智能启动
echo ========================================

setlocal

set PORT=8083
set MAX_WAIT=30

REM 1. 检查端口占用
echo [1/4] 检查端口 %PORT% 占用情况...
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo 发现端口 %PORT% 已被占用，正在清理...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
        echo 杀掉旧进程 PID: %%a
        taskkill /F /PID %%a >nul 2>&1
    )
    REM 等待端口释放
    echo 等待端口释放...
    ping -n 3 127.0.0.1 >nul 2>&1
) else (
    echo 端口 %PORT% 可用
)

REM 2. 检查并停止旧服务
echo [2/4] 检查 Java 进程...
wmic process where "name='java.exe'" get processid,commandline 2>nul | findstr "ops-service" >nul 2>&1
if %errorlevel%==0 (
    echo 发现旧 OPS Service 进程
    for /f "tokens=2" %%a in ('wmic process where "name='java.exe'" get processid^,commandline /format:list ^| findstr "processid"') do (
        echo 尝试停止进程 %%a
        taskkill /F /PID %%a >nul 2>&1
    )
)

REM 3. 启动服务
echo [3/4] 启动 ops-service...
cd /d D:\.openclaw\workspace\temp-project\ops-service
start "ops-service" cmd /k "mvn spring-boot:run -DskipTests"

REM 4. 等待并验证
echo [4/4] 等待服务启动...
set /a count=0
:wait_loop
set /a count+=1
if %count% GTR %MAX_WAIT% (
    echo [警告] 启动超时，请手动检查
    goto done
)
ping -n 2 127.0.0.1 >nul 2>&1
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    echo [成功] 服务已启动 (端口 %PORT%)
    goto done
) else (
    echo 等待启动中... (%count%/%MAX_WAIT%)
    goto wait_loop
)

:done
echo.
echo ========================================
echo   启动完成
echo ========================================
echo   后端: http://localhost:%PORT%
echo   前端: http://localhost
echo ========================================

endlocal
pause
