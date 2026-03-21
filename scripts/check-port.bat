@echo off
setlocal enabledelayedexpansion

set "PORT=%1"

if "%PORT%"=="" (
    echo ========================================
    echo   OPS Platform Port Status
    echo ========================================
    echo Port    Status      PID
    echo ------  --------    ------
    netstat -ano ^| findstr ":80 :8080 :8081 :8083 "
    goto :end
)

echo Checking port %PORT%...
netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"
if %errorlevel%==0 (
    echo [IN USE] Port %PORT% is occupied
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findlist "LISTENING"') do (
        echo PID: %%a
    )
) else (
    echo [FREE] Port %PORT% is available
)

:end
endlocal
