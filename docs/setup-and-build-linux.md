# Linux Setup & APK Packaging Guide 🐧

This guide walks you through setting up all necessary development dependencies on Linux and packaging the **BElectronicsVP** Android APK using the automated build script.

---

## 📋 Table of Contents

1. [Prerequisites & System Requirements](#1-prerequisites--system-requirements)
2. [Step 1: Installing Java Development Kit (JDK 21)](#2-step-1-installing-java-development-kit-jdk-21)
3. [Step 2: Installing the Android SDK](#3-step-2-installing-the-android-sdk)
   * [Option A: Headless via Command-Line Tools (Recommended for Terminal / Servers)](#option-a-headless-via-command-line-tools-recommended)
   * [Option B: Via Android Studio (GUI)](#option-b-via-android-studio-gui)
4. [Step 3: Configuring Environment Variables](#4-step-3-configuring-environment-variables)
5. [Step 4: USB Debugging & udev Rules (For Phone Deployment)](#5-step-4-usb-debugging--udev-rules)
6. [Step 5: Verifying Dependencies & Building the APK](#6-step-5-verifying-dependencies--building-the-apk)
7. [Step 6: Installing onto Your Device](#7-step-6-installing-onto-your-device)
8. [Troubleshooting & FAQ](#8-troubleshooting--faq)

---

## 1. Prerequisites & System Requirements

* **Operating System**: Any modern 64-bit Linux distribution (Ubuntu 22.04+, Debian 12+, Arch Linux, Fedora 39+).
* **RAM**: Minimum 4 GB (8 GB recommended for Gradle daemon).
* **Disk Space**: At least 3 GB free for JDK, Android SDK components, and build artifacts.

---

## 2. Step 1: Installing Java Development Kit (JDK 21)

BElectronicsVP is written in Kotlin 2.0 and targets Java 21 bytecode. Ensure you install a full JDK (including `javac`), not merely a JRE.

### Ubuntu / Debian / Pop!_OS / Linux Mint
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk git curl
```

### Arch Linux / Manjaro / EndeavourOS
```bash
sudo pacman -Syu --needed jdk21-openjdk git curl
```
If multiple Java versions exist, select JDK 21 as default:
```bash
sudo archlinux-java set java-21-openjdk
```

### Fedora / RHEL / AlmaLinux
```bash
sudo dnf install -y java-21-openjdk-devel git curl
```

### Verify Java Installation
Run the following commands to ensure Java 21 and the compiler are recognized:
```bash
java -version
javac -version
```
Expected output:
```text
openjdk version "21.0.x" ...
javac 21.0.x
```

---

## 3. Step 2: Installing the Android SDK

You can choose either headless command-line tools or the full Android Studio IDE.

### Option A: Headless via Command-Line Tools (Recommended)

This lightweight method installs only the necessary SDK tools without the heavy IDE:

1. **Create the SDK directory structure**:
   ```bash
   mkdir -p "$HOME/Android/Sdk/cmdline-tools"
   cd "$HOME/Android/Sdk/cmdline-tools"
   ```

2. **Download official Google Command Line Tools**:
   ```bash
   curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
   unzip -q cmdline-tools.zip
   mv cmdline-tools latest
   rm cmdline-tools.zip
   ```

3. **Install Android Platform 34 and Build Tools 34**:
   ```bash
   export ANDROID_HOME="$HOME/Android/Sdk"
   export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

   # Accept SDK licenses
   yes | sdkmanager --licenses

   # Install API 34 platform, build-tools, and adb platform-tools
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   ```

### Option B: Via Android Studio (GUI)

1. Download Android Studio from [developer.android.com/studio](https://developer.android.com/studio).
2. Extract and launch `bin/studio.sh`.
3. Complete the setup wizard.
4. Open **Settings** ➡️ **Languages & Frameworks** ➡️ **Android SDK**:
   * Under **SDK Platforms**: Check **Android 14.0 ("UpsideDownCake" / API 34)**.
   * Under **SDK Tools**: Check **Android SDK Build-Tools 34**, **Android SDK Command-line Tools**, and **Android SDK Platform-Tools**.
   * Click **Apply** to download.
   * Note the SDK path displayed at the top (usually `/home/<user>/Android/Sdk`).

---

## 4. Step 3: Configuring Environment Variables

Add the following lines to your shell profile file (`~/.bashrc` or `~/.zshrc`):

```bash
# Android SDK Environment Variables
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
```

Apply the changes immediately in your current terminal:
```bash
source ~/.bashrc   # or source ~/.zshrc
```

Verify `adb` is in your PATH:
```bash
adb --version
```

---

## 5. Step 4: USB Debugging & udev Rules

To allow your computer to recognize your physical Android phone when connected via USB:

1. **Install Android udev rules**:
   * **Ubuntu / Debian**:
     ```bash
     sudo apt install -y android-sdk-platform-tools-common
     ```
   * **Arch Linux**:
     ```bash
     sudo pacman -S android-udev
     ```
   * **Fedora**:
     ```bash
     sudo dnf install -y android-tools
     ```

2. **Enable USB Debugging on your phone**:
   * Open **Settings** ➡️ **About phone** ➡️ Tap **Build number** 7 times to enable Developer options.
   * Open **Settings** ➡️ **Developer options** ➡️ Enable **USB debugging**.

3. **Verify phone connection**:
   Connect your phone with a USB cable and run:
   ```bash
   adb devices
   ```
   *A prompt will appear on your phone asking "Allow USB debugging?". Check "Always allow from this computer" and tap **Allow**.*

---

## 6. Step 5: Verifying Dependencies & Building the APK

BElectronicsVP includes an intelligent build script that audits your system and compiles the APK:

### 1. Run Dependency Audit
Check whether your environment has all required packages:
```bash
./build-apk.sh --check
```
You should see a status table like:
```text
+-------------------------+------------+--------------------------------------------+
| Component               | Status     | Notes / Location                           |
+-------------------------+------------+--------------------------------------------+
| Java Runtime (JDK 17+)  | [OK]       | openjdk version "21.0.2"                   |
| Java Compiler (javac)   | [OK]       | JDK Development Kit found                  |
| Android SDK             | [OK]       | /home/user/Android/Sdk                     |
| Platform API 34         | [OK]       | platforms/android-34 present               |
| Build Tools 34.0.0+     | [OK]       | build-tools/34.x present                   |
| Android ADB Tool        | [OK]       | Ready for USB deployment                   |
+-------------------------+------------+--------------------------------------------+
```

### 2. Build the Debug APK
```bash
./build-apk.sh
```
The script will compile the code, bundle Compose assets, package the APK, and output it to `dist/`:
```text
✓ APK successfully generated and packaged!
  Location: dist/BElectronicsVP-v1.0.0-debug.apk
  Size:     16.21 MB
  SHA-256:  ...
```

### 3. Build Options
| Command | Action |
|---|---|
| `./build-apk.sh` | Compile standard Debug APK (pre-signed, ready for phone) |
| `./build-apk.sh -c` | Clean cache and rebuild from scratch |
| `./build-apk.sh -r` | Compile unsigned Release APK |
| `./build-apk.sh -i` | Build APK and immediately deploy onto connected phone via ADB |
| `./build-apk.sh -h` | Display usage manual |

---

## 7. Step 6: Installing onto Your Device

### Method A: Single-Command ADB Deployment
If your phone is connected via USB with debugging enabled:
```bash
./build-apk.sh -i
```
Or manually:
```bash
adb install -r dist/BElectronicsVP-v1.0.0-debug.apk
```

### Method B: Manual Sideload
1. Transfer `dist/BElectronicsVP-v1.0.0-debug.apk` to your phone via USB cable, Google Drive, Nextcloud, or email.
2. Open your phone's file manager, tap the `.apk` file, and tap **Install**.
3. If prompted by Android, allow "Install unknown apps" for your file manager.

---

## 8. Troubleshooting & FAQ

### Issue: `SDK location not found. Define location with an ANDROID_HOME environment variable`
**Fix**: Ensure `local.properties` contains your SDK path:
```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

### Issue: `Unsupported class file major version 65`
**Fix**: You may be running an older Gradle daemon or JDK mismatch. Run:
```bash
./gradlew --stop
./build-apk.sh -c
```

### Issue: `adb: device unauthorized`
**Fix**: Unlock your phone screen and accept the "Allow USB debugging" prompt from your computer.
