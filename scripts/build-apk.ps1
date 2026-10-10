# ==============================================================================
# BElectronicsVP - Automated APK Build & Dependency Checker (Windows PowerShell)
# ==============================================================================

[CmdletBinding()]
param(
    [switch]$Debug = $true,
    [switch]$Release = $false,
    [switch]$Clean = $false,
    [switch]$Install = $false,
    [switch]$CheckOnly = $false,
    [switch]$Help = $false
)

$ErrorActionPreference = "Stop"

if ($Help) {
    Write-Host @"
BElectronicsVP Android Packaging & Dependency Checker (Windows)

Usage: .\build-apk.ps1 [OPTIONS]

Options:
  -Debug        Build Debug APK (default, pre-signed with debug key)
  -Release      Build Release APK (unsigned)
  -Clean        Perform a clean Gradle build before assembling
  -Install      Automatically install APK via adb to a connected device
  -CheckOnly    Only check environment dependencies without building
  -Help         Display this help message

Examples:
  .\build-apk.ps1             # Check dependencies and build debug APK
  .\build-apk.ps1 -CheckOnly  # Verify if all tools are installed
  .\build-apk.ps1 -Clean      # Clean and rebuild
  .\build-apk.ps1 -Install    # Build and deploy to connected Android phone
"@
    exit 0
}

# Resolve project root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\.."
Set-Location $ProjectRoot

Write-Host @"
  ____  _____ _           _                   _         __     ______  
 | __ )| ____| | ___  ___| |_ _ __ ___  _ __ (_) ___ ___\ \   / /  _ \ 
 |  _ \|  _| | |/ _ \/ __| __| '__/ _ \| '_ \| |/ __/ __|\ \ / /| |_) |
 | |_) | |___| |  __/ (__| |_| | | (_) | | | | | (__\__ \ \ V / |  __/ 
 |____/|_____|_|\___|\___|\__|_|  \___/|_| |_|_|\___|___/  \_/  |_|    

BElectronicsVP Android Packaging & Dependency Checker (Windows)
"@ -ForegroundColor Cyan

Write-Host "`nChecking required Windows system dependencies...`n" -ForegroundColor White

$ErrorsFound = 0

# 1. Check Java
$JavaCmd = Get-Command java -ErrorAction SilentlyContinue
$JavaStatus = "MISSING"
$JavaVersionText = ""

if ($JavaCmd) {
    $rawVersion = & java -version 2>&1 | Select-Object -First 1
    $JavaVersionText = "$rawVersion"
    if ($rawVersion -match '"([0-9]+)') {
        $majorVer = [int]$matches[1]
        if ($majorVer -ge 17) {
            $JavaStatus = "OK"
        } else {
            $JavaStatus = "OUTDATED"
        }
    } else {
        $JavaStatus = "OK"
    }
}

# 2. Check Java Compiler (javac)
$JavacCmd = Get-Command javac -ErrorAction SilentlyContinue
$JavacStatus = if ($JavacCmd) { "OK" } else { "MISSING" }

# 3. Check Android SDK
$SdkDir = ""
$SdkStatus = "MISSING"

if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) {
    $SdkDir = $env:ANDROID_HOME
} elseif ($env:ANDROID_SDK_ROOT -and (Test-Path $env:ANDROID_SDK_ROOT)) {
    $SdkDir = $env:ANDROID_SDK_ROOT
} elseif (Test-Path "local.properties") {
    $propLine = Get-Content "local.properties" | Where-Object { $_ -match "^sdk\.dir=(.*)" }
    if ($propLine) {
        $extracted = $propLine -replace "^sdk\.dir=", "" -replace "\\\\", "\"
        if (Test-Path $extracted) {
            $SdkDir = $extracted
        }
    }
}

# Fallback: probe %LOCALAPPDATA%\Android\Sdk
if (-not $SdkDir) {
    $defaultWinSdk = "$env:LOCALAPPDATA\Android\Sdk"
    if (Test-Path $defaultWinSdk) {
        $SdkDir = $defaultWinSdk
        "sdk.dir=$($defaultWinSdk.Replace('\', '/'))" | Out-File -FilePath "local.properties" -Encoding utf8
        Write-Host "Detected SDK at: $SdkDir (created local.properties)" -ForegroundColor Yellow
    }
}

if ($SdkDir -and (Test-Path $SdkDir)) {
    $SdkStatus = "OK"
}

# 4. Check Platform 34 and Build Tools 34
$PlatformStatus = "MISSING"
$BuildToolsStatus = "MISSING"

if ($SdkStatus -eq "OK") {
    if (Test-Path "$SdkDir\platforms\android-34") {
        $PlatformStatus = "OK"
    }
    $tools34 = Get-ChildItem "$SdkDir\build-tools" -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "34.*" }
    if ($tools34) {
        $BuildToolsStatus = "OK"
    }
}

# 5. Check ADB
$AdbCmd = Get-Command adb -ErrorAction SilentlyContinue
$AdbStatus = "OPTIONAL"
if ($AdbCmd -or ($SdkDir -and (Test-Path "$SdkDir\platform-tools\adb.exe"))) {
    $AdbStatus = "OK"
}

# Render Table
Write-Host "+-------------------------+------------+--------------------------------------------+"
Write-Host "| Component               | Status     | Notes / Location                           |"
Write-Host "+-------------------------+------------+--------------------------------------------+"

# Helper for formatted output
function Write-TableRow($name, $status, $color, $note) {
    $displayNote = if ($note.Length -gt 42) { $note.Substring(0, 39) + "..." } else { $note.PadRight(42) }
    Write-Host "| " -NoNewline
    Write-Host ("{0,-23}" -f $name) -NoNewline
    Write-Host " | " -NoNewline
    Write-Host ("{0,-10}" -f "[$status]") -ForegroundColor $color -NoNewline
    Write-Host " | " -NoNewline
    Write-Host $displayNote -NoNewline
    Write-Host " |"
}

if ($JavaStatus -eq "OK") {
    Write-TableRow "Java Runtime (JDK 17+)" "OK" Green $JavaVersionText
} elseif ($JavaStatus -eq "OUTDATED") {
    Write-TableRow "Java Runtime (JDK 17+)" "OUTDATED" Yellow "JDK 17+ required (found older version)"
    $ErrorsFound++
} else {
    Write-TableRow "Java Runtime (JDK 17+)" "MISSING" Red "java command not found"
    $ErrorsFound++
}

if ($JavacStatus -eq "OK") {
    Write-TableRow "Java Compiler (javac)" "OK" Green "JDK Development Kit found"
} else {
    Write-TableRow "Java Compiler (javac)" "MISSING" Red "Install full JDK, not just JRE"
    $ErrorsFound++
}

if ($SdkStatus -eq "OK") {
    Write-TableRow "Android SDK" "OK" Green $SdkDir
} else {
    Write-TableRow "Android SDK" "MISSING" Red "ANDROID_HOME or local.properties unset"
    $ErrorsFound++
}

if ($PlatformStatus -eq "OK") {
    Write-TableRow "Platform API 34" "OK" Green "platforms/android-34 present"
} else {
    Write-TableRow "Platform API 34" "MISSING" Red "Install 'platforms;android-34'"
    $ErrorsFound++
}

if ($BuildToolsStatus -eq "OK") {
    Write-TableRow "Build Tools 34.0.0+" "OK" Green "build-tools/34.x present"
} else {
    Write-TableRow "Build Tools 34.0.0+" "MISSING" Red "Install 'build-tools;34.0.0'"
    $ErrorsFound++
}

if ($AdbStatus -eq "OK") {
    Write-TableRow "Android ADB Tool" "OK" Green "Ready for USB deployment"
} else {
    Write-TableRow "Android ADB Tool" "OPTIONAL" Yellow "Install platform-tools for adb deploy"
}

Write-Host "+-------------------------+------------+--------------------------------------------+`n"

if ($ErrorsFound -gt 0) {
    Write-Host "Missing dependencies detected! ($ErrorsFound errors)" -ForegroundColor Red
    Write-Host "Please review the Windows setup guide: docs\setup-and-build-windows.md`n" -ForegroundColor Yellow

    Write-Host "Quick Windows installation via winget:" -ForegroundColor Cyan
    Write-Host "  winget install Microsoft.OpenJDK.21"
    Write-Host "  winget install Google.AndroidStudio"
    Write-Host "`nOr download directly from https://developer.android.com/studio"
    exit 1
}

Write-Host "All development dependencies are satisfied!`n" -ForegroundColor Green

if ($CheckOnly) {
    Write-Host "Check completed successfully."
    exit 0
}

# Determine build tasks
$gradleTasks = @()
if ($Clean) {
    $gradleTasks += "clean"
}

$targetFilename = ""
$expectedSource = ""

if ($Release) {
    $gradleTasks += ":app:assembleRelease"
    $expectedSource = "app\build\outputs\apk\release\app-release-unsigned.apk"
    $targetFilename = "BElectronicsVP-v1.0.0-release-unsigned.apk"
} else {
    $gradleTasks += ":app:assembleDebug"
    $expectedSource = "app\build\outputs\apk\debug\app-debug.apk"
    $targetFilename = "BElectronicsVP-v1.0.0-debug.apk"
}

Write-Host "Compiling and Packaging APK with Gradle..." -ForegroundColor Cyan
Write-Host "Running: .\gradlew.bat $($gradleTasks -join ' ')`n"

& .\gradlew.bat $gradleTasks

if (-not (Test-Path $expectedSource)) {
    $fallbackRelease = "app\build\outputs\apk\release\app-release.apk"
    if (Test-Path $fallbackRelease) {
        $expectedSource = $fallbackRelease
        $targetFilename = "BElectronicsVP-v1.0.0-release.apk"
    } else {
        Write-Error "Expected APK artifact not found at $expectedSource"
        exit 1
    }
}

if (-not (Test-Path "dist")) {
    New-Item -ItemType Directory -Path "dist" | Out-Null
}

$distPath = "dist\$targetFilename"
Copy-Item -Path $expectedSource -Destination $distPath -Force

$item = Get-Item $distPath
$sizeMB = [math]::Round($item.Length / 1MB, 2)
$hash = (Get-FileHash -Path $distPath -Algorithm SHA256).Hash.ToLower()

Write-Host "`nAPK successfully generated and packaged!" -ForegroundColor Green
Write-Host "  Location: $distPath" -ForegroundColor White
Write-Host "  Size:     $sizeMB MB ($($item.Length) bytes)" -ForegroundColor White
Write-Host "  SHA-256:  $hash" -ForegroundColor White

if ($Install) {
    Write-Host "`nDeploying to connected Android device..." -ForegroundColor Cyan
    $adbExe = if ($AdbCmd) { "adb" } else { "$SdkDir\platform-tools\adb.exe" }
    
    $devices = & $adbExe devices | Where-Object { $_ -match "\bdevice$" }
    if ($devices) {
        Write-Host "Installing $distPath via ADB..."
        & $adbExe install -r $distPath
        Write-Host "Installation complete!" -ForegroundColor Green
    } else {
        Write-Host "No authorized Android device detected. Enable USB Debugging on your phone." -ForegroundColor Yellow
    }
} else {
    Write-Host "`nInstallation Instructions:" -ForegroundColor White
    Write-Host "  To install directly via USB/Wi-Fi:"
    Write-Host "  adb install -r $distPath" -ForegroundColor Yellow
    Write-Host "  Or copy $distPath to phone storage and tap to install."
}

Write-Host "`n==============================================================================" -ForegroundColor Green
Write-Host "Build Finished Successfully! Ready for testing and distribution." -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
