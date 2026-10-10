# Windows Setup & APK Packaging Guide 🪟

This guide walks you through setting up all required development dependencies on **Windows 10 and 11** and packaging the **BElectronicsVP** Android APK using PowerShell or Batch scripts.

---

## 📋 Table of Contents

1. [Prerequisites & System Requirements](#1-prerequisites--system-requirements)
2. [Quick Setup via Windows Package Manager (`winget`)](#2-quick-setup-via-windows-package-manager-winget)
3. [Manual Installation Alternative](#3-manual-installation-alternative)
   * [Step 1: Installing Java Development Kit (JDK 21)](#step-1-installing-java-development-kit-jdk-21)
   * [Step 2: Installing Android SDK via Android Studio](#step-2-installing-android-sdk-via-android-studio)
4. [Step 3: Configuring Windows Environment Variables](#4-step-3-configuring-windows-environment-variables)
5. [Step 4: Installing Google USB Driver for Phone Deployment](#5-step-4-installing-google-usb-driver-for-phone-deployment)
6. [Step 5: Verifying Dependencies & Packaging the APK](#6-step-5-verifying-dependencies--packaging-the-apk)
7. [Step 6: Installing onto Your Android Device](#7-step-6-installing-onto-your-android-device)
8. [Troubleshooting & Windows FAQ](#8-troubleshooting--windows-faq)

---

## 1. Prerequisites & System Requirements

* **Operating System**: Windows 10 (version 1909 or higher) or Windows 11 (64-bit).
* **RAM**: Minimum 4 GB (8 GB or more recommended).
* **Disk Space**: At least 3.5 GB free for JDK, Android SDK components, and build output.

---

## 2. Quick Setup via Windows Package Manager (`winget`)

The fastest way to install all prerequisites on modern Windows 10/11 is using Microsoft's official `winget` tool.

Open **PowerShell** as Administrator and run:

```powershell
# 1. Install OpenJDK 21 (Eclipse Temurin)
winget install EclipseAdoptium.Temurin.21.JDK --accept-package-agreements --accept-source-agreements

# 2. Install Android Studio (includes Android SDK, SDK Manager, and ADB)
winget install Google.AndroidStudio --accept-package-agreements --accept-source-agreements

# 3. Install Git for Windows
winget install Git.Git --accept-package-agreements --accept-source-agreements
```

> *Tip: After running `winget`, close and restart your terminal to reload newly registered environment paths.*

---

## 3. Manual Installation Alternative

If you prefer installing tools manually without `winget`:

### Step 1: Installing Java Development Kit (JDK 21)
1. Download OpenJDK 21 from [Eclipse Adoptium Temurin](https://adoptium.net/temurin/releases/?version=21) (choose **Windows x64 .msi**).
2. Run the installer and make sure to enable the option **"Set JAVA_HOME variable"** and **"Add to PATH"**.
3. Verify in a new Command Prompt or PowerShell:
   ```cmd
   java -version
   javac -version
   ```

### Step 2: Installing Android SDK via Android Studio
1. Download Android Studio from [developer.android.com/studio](https://developer.android.com/studio).
2. Run the setup wizard and choose standard installation.
3. Once opened, go to **More Actions** ➡️ **SDK Manager** (or **Settings** ➡️ **Languages & Frameworks** ➡️ **Android SDK**):
   * Under **SDK Platforms**: Check **Android 14.0 ("UpsideDownCake" / API 34)**.
   * Under **SDK Tools**:
     * Check **Android SDK Build-Tools 34** (specifically 34.0.0).
     * Check **Android SDK Platform-Tools**.
     * Check **Google USB Driver**.
   * Click **Apply** to install the components.
4. Note your SDK path, typically:
   ```text
   C:\Users\<YourUsername>\AppData\Local\Android\Sdk
   ```

---

## 4. Step 3: Configuring Windows Environment Variables

Setting `ANDROID_HOME` allows Gradle and command-line scripts to automatically locate your SDK:

1. Press `Win + R`, type `sysdm.cpl`, and press **Enter** to open System Properties.
2. Go to the **Advanced** tab and click **Environment Variables**.
3. Under **User variables** (or System variables):
   * Click **New...**:
     * **Variable name**: `ANDROID_HOME`
     * **Variable value**: `C:\Users\<YourUsername>\AppData\Local\Android\Sdk`
4. Under **User variables**, select `Path` and click **Edit...**:
   * Click **New** and add: `%ANDROID_HOME%\platform-tools`
   * Click **New** and add: `%ANDROID_HOME%\cmdline-tools\latest\bin`
5. Click **OK** on all dialogs to save.

### Verify in PowerShell
Open a new PowerShell window and run:
```powershell
adb --version
```
If `adb` outputs its version, your path configuration is complete!

---

## 5. Step 4: Installing Google USB Driver for Phone Deployment

To connect your phone to Windows for direct APK installation:

1. In Android Studio, ensure **Google USB Driver** is checked under **SDK Tools**.
2. Connect your Android phone to your PC via a USB cable.
3. On your phone:
   * Go to **Settings** ➡️ **About phone** ➡️ Tap **Build number** 7 times.
   * Go to **Settings** ➡️ **Developer options** ➡️ Turn ON **USB debugging**.
4. If Windows Device Manager shows an exclamation mark next to your device:
   * Right-click device ➡️ **Update driver** ➡️ **Browse my computer for drivers**.
   * Select: `C:\Users\<YourUsername>\AppData\Local\Android\Sdk\extras\google\usb_driver`.
5. Run in PowerShell:
   ```powershell
   adb devices
   ```
   *Unlock your phone and tap **"Always allow from this computer"** when prompted.*

---

## 6. Step 5: Verifying Dependencies & Packaging the APK

BElectronicsVP includes native Windows packaging scripts (`build-apk.bat` and `build-apk.ps1`) that perform full environment diagnostics:

### 1. Verify Dependencies
Open PowerShell or Command Prompt in the repository folder:

```powershell
.\build-apk.ps1 -CheckOnly
```
*Or via Command Prompt / Double-click:*
```cmd
build-apk.bat
```

The script will display a diagnostic table:
```text
+-------------------------+------------+--------------------------------------------+
| Component               | Status     | Notes / Location                           |
+-------------------------+------------+--------------------------------------------+
| Java Runtime (JDK 17+)  | [OK]       | openjdk version "21.0.x"                   |
| Java Compiler (javac)   | [OK]       | JDK Development Kit found                  |
| Android SDK             | [OK]       | C:\Users\user\AppData\Local\Android\Sdk    |
| Platform API 34         | [OK]       | platforms\android-34 present               |
| Build Tools 34.0.0+     | [OK]       | build-tools\34.x present                   |
| Android ADB Tool        | [OK]       | Ready for USB deployment                   |
+-------------------------+------------+--------------------------------------------+

✓ All development dependencies are satisfied!
```

### 2. Build the Debug APK
```powershell
.\build-apk.ps1
```
*Or simply run `build-apk.bat` in CMD.*

The script will compile the project and save the output APK to the `dist\` folder:
```text
✓ APK successfully generated and packaged!
  Location: dist\BElectronicsVP-v1.0.0-debug.apk
  Size:     16.21 MB
  SHA-256:  ...
```

### 3. Available Script Options (PowerShell)
| Option | Action |
|---|---|
| `.\build-apk.ps1` | Build standard Debug APK (default, pre-signed for phones) |
| `.\build-apk.ps1 -Clean` | Clean Gradle build cache before building |
| `.\build-apk.ps1 -Release` | Build unsigned Release APK |
| `.\build-apk.ps1 -Install` | Build APK and automatically deploy to connected phone via ADB |
| `.\build-apk.ps1 -CheckOnly` | Audit installed tools without starting build |
| `.\build-apk.ps1 -Help` | Display manual and parameters |

---

## 7. Step 6: Installing onto Your Android Device

### Option 1: Automatic ADB Deployment
Connect your phone via USB with USB debugging enabled, then execute:
```powershell
.\build-apk.ps1 -Install
```
Or manually:
```cmd
adb install -r dist\BElectronicsVP-v1.0.0-debug.apk
```

### Option 2: Copy to Phone via Windows Explorer
1. Connect your phone via USB and choose **"File Transfer"** (MTP) mode on your phone.
2. In Windows File Explorer, open your phone's storage (e.g. `Download` folder).
3. Drag and drop `dist\BElectronicsVP-v1.0.0-debug.apk` into your phone.
4. On your phone, open the Files app, tap the APK, and choose **Install**.

---

## 8. Troubleshooting & Windows FAQ

### Issue: PowerShell displays `File cannot be loaded because running scripts is disabled on this system`
**Fix**: Windows restricts script execution by default. Temporarily bypass execution policy for the current session:
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```
Then rerun `.\build-apk.ps1`.

### Issue: `SDK location not found. Define location with an ANDROID_HOME environment variable`
**Fix**: Ensure `local.properties` exists in the project root with forward slashes:
```text
sdk.dir=C:/Users/YourUsername/AppData/Local/Android/Sdk
```
The `build-apk.ps1` script will automatically create this for you if it detects your SDK in `%LOCALAPPDATA%\Android\Sdk`.

### Issue: `adb devices` shows device as `unauthorized`
**Fix**: Unlock your phone screen, unplug and replug the USB cable, and tap **"Allow USB debugging"** on your phone's display.
