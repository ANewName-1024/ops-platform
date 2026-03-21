@echo off
REM OPS 平台端口与进程管理工具
REM 使用方法: port-manager.bat [command]
REM command: list | kill [port] | check [port]

setlocal

set COMMAND=%1

if "%COMMAND%"=="" set COMMAND=list
if "%COMMAND%"=="list" goto list
if "%COMMAND%"=="check" goto check
if "%COMMAND%"=="kill" goto kill

:usage
echo 用法: port-manager.bat [command]
echo.
echo 命令:
echo   list           - 列出所有相关端口
echo   check [port]  - 检查端口是否被占用
echo   kill [port]   - 杀掉占用指定端口的进程
echo.
echo 示例:
echo   port-manager.bat list
echo   port-manager.bat check 8083
echo   port-manager.bat kill 8083
goto end

:list
echo ========================================
echo   OPS 平台端口状态
echo ========================================
echo.
echo 端口    状态      进程ID
echo ------  --------  --------
netstat -ano | findstr ":80 :8080 :8081 :8082 :8083 :8090 :8091 :8092 "
echo.
echo 常用端口:
echo   80    - Nginx 前端
echo   8080  - Gateway 网关
echo   8081  - User Service
echo   8083  - OPS Service
echo   8092  - Knowledge Service
goto end

:check
set PORT=%2
if "%PORT%"=="" (
    echo 请指定端口号
    goto end
)
echo 检查端口 %PORT%...
netstat -ano | findstr ":%PORT% " | findstr "LISTENING"
if %errorlevel%==0 (
    echo [占用] 端口 %PORT% 已被占用
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
        echo 进程ID: %%a
    )
) else (
    echo [可用] 端口 %PORT% 可用
)
goto end

:kill
set PORT=%2
if "%PORT%"=="" (
    echo 请指定端口号
    goto end
)
echo 尝试杀掉占用端口 %PORT% 的进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    echo 发现进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if %errorlevel%==0 (
        echo [成功] 已杀掉进程 %%a
    ) else (
        echo [失败] 无法杀掉进程 %%a
    )
)
goto end

:end
endlocal
