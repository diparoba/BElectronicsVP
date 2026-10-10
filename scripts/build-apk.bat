@echo off
rem ==============================================================================
rem BElectronicsVP - Automated APK Build Script (Windows Batch)
rem ==============================================================================

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
cd /d "%PROJECT_ROOT%"

rem Prefer running PowerShell implementation for rich diagnostics if available
where powershell >nul 2>&1
if %ERRORLEVEL% equ 0 (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%build-apk.ps1" %*
    exit /b %ERRORLEVEL%
)

echo [BElectronicsVP] PowerShell not detected. Running standard batch fallback...

rem Check Java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install JDK 17 or JDK 21 (winget install Microsoft.OpenJDK.21)
    exit /b 1
)

rem Check Android SDK
if "%ANDROID_HOME%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
        echo sdk.dir=!ANDROID_HOME:\=/! > local.properties
    ) else if not exist "local.properties" (
        echo [ERROR] ANDROID_HOME environment variable is not set.
        echo Please install the Android SDK or Android Studio.
        exit /b 1
    )
)

echo Building Debug APK with Gradle...
call gradlew.bat :app:assembleDebug
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Gradle build failed.
    exit /b %ERRORLEVEL%
)

if not exist "dist" mkdir "dist"
copy /y "app\build\outputs\apk\debug\app-debug.apk" "dist\BElectronicsVP-v1.0.0-debug.apk" >nul

echo.
echo ==============================================================================
echo Build Successful!
echo Output APK: dist\BElectronicsVP-v1.0.0-debug.apk
echo ==============================================================================
endlocal
