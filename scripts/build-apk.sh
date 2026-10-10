#!/usr/bin/env bash
# ==============================================================================
# BElectronicsVP - Automated APK Build & Dependency Checker (Linux / macOS)
# ==============================================================================
# This script validates development dependencies (JDK, Android SDK, platform tools)
# and packages the Android APK into the 'dist/' folder.
# ==============================================================================

set -euo pipefail

# Color palette
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Determine project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

# Default parameters
BUILD_TYPE="debug"
CLEAN_FIRST=false
AUTO_INSTALL=false
CHECK_ONLY=false

print_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "  ____  _____ _           _                   _         __     ______  "
    echo " | __ )| ____| | ___  ___| |_ _ __ ___  _ __ (_) ___ ___\\ \\   / /  _ \\ "
    echo " |  _ \\|  _| | |/ _ \\/ __| __| '__/ _ \\| '_ \\| |/ __/ __|\\ \\ / /| |_) |"
    echo " | |_) | |___| |  __/ (__| |_| | | (_) | | | | | (__\\__ \\ \\ V / |  __/ "
    echo " |____/|_____|_|\\___|\\___|\\__|_|  \\___/|_| |_|_|\\___|___/  \\_/  |_|    "
    echo -e "${NC}"
    echo -e "${BOLD}BElectronicsVP Android Packaging & Dependency Checker (Linux)${NC}\n"
}

usage() {
    echo -e "Usage: ${BOLD}./build-apk.sh [OPTIONS]${NC}"
    echo ""
    echo "Options:"
    echo "  -d, --debug       Build Debug APK (default, pre-signed with debug key)"
    echo "  -r, --release     Build Release APK (unsigned)"
    echo "  -c, --clean       Perform a clean Gradle build before assembling"
    echo "  -i, --install     Automatically install APK via adb to a connected device"
    echo "  --check           Only check environment dependencies without building"
    echo "  -h, --help        Display this help message"
    echo ""
    echo "Examples:"
    echo "  ./build-apk.sh              # Check dependencies and build debug APK"
    echo "  ./build-apk.sh --check      # Verify if all tools are installed"
    echo "  ./build-apk.sh -c           # Clean build"
    echo "  ./build-apk.sh -i           # Build and install to phone via ADB"
    exit 0
}

# Parse command line flags
while [[ $# -gt 0 ]]; do
    case "$1" in
        -d|--debug)
            BUILD_TYPE="debug"
            shift
            ;;
        -r|--release)
            BUILD_TYPE="release"
            shift
            ;;
        -c|--clean)
            CLEAN_FIRST=true
            shift
            ;;
        -i|--install)
            AUTO_INSTALL=true
            shift
            ;;
        --check)
            CHECK_ONLY=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo -e "${RED}Error: Unknown argument '$1'${NC}"
            usage
            ;;
    esac
done

print_banner

echo -e "${BOLD}Checking required system dependencies...${NC}\n"

ERRORS_FOUND=0

# Detect Linux Distribution if available
DISTRO="unknown"
if [[ -f "/etc/os-release" ]]; then
    # shellcheck disable=SC1091
    DISTRO_ID=$(grep -E '^ID=' /etc/os-release | cut -d'=' -f2 | tr -d '"' || true)
    DISTRO="${DISTRO_ID}"
fi

# 1. Check Java Runtime
JAVA_STATUS="MISSING"
JAVA_VERSION_TEXT=""
if command -v java >/dev/null 2>&1; then
    RAW_VERSION=$(java -version 2>&1 | head -n 1)
    JAVA_VERSION_TEXT="${RAW_VERSION}"
    # Verify Java version >= 17
    MAJOR_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
    if [[ -n "${MAJOR_VER}" && "${MAJOR_VER}" -ge 17 ]]; then
        JAVA_STATUS="OK"
    else
        JAVA_STATUS="OUTDATED"
    fi
fi

# 2. Check Java Compiler (javac)
JAVAC_STATUS="MISSING"
if command -v javac >/dev/null 2>&1; then
    JAVAC_STATUS="OK"
fi

# 3. Check Android SDK
SDK_DIR=""
SDK_STATUS="MISSING"
if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then
    SDK_DIR="${ANDROID_HOME}"
elif [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}" ]]; then
    SDK_DIR="${ANDROID_SDK_ROOT}"
elif [[ -f "local.properties" ]]; then
    LOCAL_SDK=$(grep -E '^sdk.dir=' local.properties | cut -d'=' -f2 | tr -d '\r' || true)
    if [[ -d "${LOCAL_SDK}" ]]; then
        SDK_DIR="${LOCAL_SDK}"
    fi
fi

# Probe standard Linux paths if not set
if [[ -z "${SDK_DIR}" ]]; then
    COMMON_PATHS=(
        "$HOME/Android/Sdk"
        "$HOME/Library/Android/sdk"
        "/opt/android-sdk"
        "/usr/lib/android-sdk"
    )
    for p in "${COMMON_PATHS[@]}"; do
        if [[ -d "$p" ]]; then
            SDK_DIR="$p"
            echo "sdk.dir=${SDK_DIR}" > local.properties
            break
        fi
    done
fi

if [[ -n "${SDK_DIR}" && -d "${SDK_DIR}" ]]; then
    SDK_STATUS="OK"
fi

# 4. Check Android Platform 34 and Build Tools
PLATFORM_STATUS="MISSING"
BUILD_TOOLS_STATUS="MISSING"
if [[ "${SDK_STATUS}" == "OK" ]]; then
    if [[ -d "${SDK_DIR}/platforms/android-34" ]]; then
        PLATFORM_STATUS="OK"
    fi
    if compgen -G "${SDK_DIR}/build-tools/34.*" > /dev/null; then
        BUILD_TOOLS_STATUS="OK"
    fi
fi

# 5. Check ADB
ADB_STATUS="OPTIONAL"
if command -v adb >/dev/null 2>&1 || [[ -n "${SDK_DIR}" && -x "${SDK_DIR}/platform-tools/adb" ]]; then
    ADB_STATUS="OK"
fi

# Print Diagnostic Summary Table
echo -e "+-------------------------+------------+--------------------------------------------+"
echo -e "| Component               | Status     | Notes / Location                           |"
echo -e "+-------------------------+------------+--------------------------------------------+"

# Java
if [[ "${JAVA_STATUS}" == "OK" ]]; then
    printf "| %-23s | ${GREEN}%-10s${NC} | %-42s |\n" "Java Runtime (JDK 17+)" "[OK]" "${JAVA_VERSION_TEXT:0:42}"
elif [[ "${JAVA_STATUS}" == "OUTDATED" ]]; then
    printf "| %-23s | ${YELLOW}%-10s${NC} | %-42s |\n" "Java Runtime (JDK 17+)" "[OUTDATED]" "JDK 17+ required (found older version)"
    ERRORS_FOUND=$((ERRORS_FOUND + 1))
else
    printf "| %-23s | ${RED}%-10s${NC} | %-42s |\n" "Java Runtime (JDK 17+)" "[MISSING]" "java command not found"
    ERRORS_FOUND=$((ERRORS_FOUND + 1))
fi

# Javac
if [[ "${JAVAC_STATUS}" == "OK" ]]; then
    printf "| %-23s | ${GREEN}%-10s${NC} | %-42s |\n" "Java Compiler (javac)" "[OK]" "JDK Development Kit found"
else
    printf "| %-23s | ${RED}%-10s${NC} | %-42s |\n" "Java Compiler (javac)" "[MISSING]" "Install a full JDK, not just JRE"
    ERRORS_FOUND=$((ERRORS_FOUND + 1))
fi

# Android SDK
if [[ "${SDK_STATUS}" == "OK" ]]; then
    printf "| %-23s | ${GREEN}%-10s${NC} | %-42s |\n" "Android SDK" "[OK]" "${SDK_DIR:0:42}"
else
    printf "| %-23s | ${RED}%-10s${NC} | %-42s |\n" "Android SDK" "[MISSING]" "ANDROID_HOME or local.properties unset"
    ERRORS_FOUND=$((ERRORS_FOUND + 1))
fi

# Android Platform API 34
if [[ "${PLATFORM_STATUS}" == "OK" ]]; then
    printf "| %-23s | ${GREEN}%-10s${NC} | %-42s |\n" "Platform API 34" "[OK]" "platforms/android-34 present"
else
    printf "| %-23s | ${RED}%-10s${NC} | %-42s |\n" "Platform API 34" "[MISSING]" "Install 'platforms;android-34'"
    ERRORS_FOUND=$((ERRORS_FOUND + 1))
fi

# Build Tools 34
if [[ "${BUILD_TOOLS_STATUS}" == "OK" ]]; then
    printf "| %-23s | ${GREEN}%-10s${NC} | %-42s |\n" "Build Tools 34.0.0+" "[OK]" "build-tools/34.x present"
else
    printf "| %-23s | ${RED}%-10s${NC} | %-42s |\n" "Build Tools 34.0.0+" "[MISSING]" "Install 'build-tools;34.0.0'"
    ERRORS_FOUND=$((ERRORS_FOUND + 1))
fi

# ADB
if [[ "${ADB_STATUS}" == "OK" ]]; then
    printf "| %-23s | ${GREEN}%-10s${NC} | %-42s |\n" "Android ADB Tool" "[OK]" "Ready for USB deployment"
else
    printf "| %-23s | ${YELLOW}%-10s${NC} | %-42s |\n" "Android ADB Tool" "[OPTIONAL]" "Install platform-tools for adb deploy"
fi

echo -e "+-------------------------+------------+--------------------------------------------+\n"

# If dependencies are missing, guide user to fix them
if [[ "${ERRORS_FOUND}" -gt 0 ]]; then
    echo -e "${RED}${BOLD}Missing dependencies detected! (${ERRORS_FOUND} errors)${NC}"
    echo -e "Please review the step-by-step setup guide: ${BOLD}docs/setup-and-build-linux.md${NC}\n"

    echo -e "${BOLD}Quick installation instructions for your distribution:${NC}"
    case "${DISTRO}" in
        ubuntu|debian|pop|mint)
            echo -e "  ${CYAN}# 1. Install OpenJDK 21:${NC}"
            echo -e "  sudo apt update && sudo apt install -y openjdk-21-jdk"
            echo -e "  ${CYAN}# 2. Install Android Command Line Tools / SDK:${NC}"
            echo -e "  sudo apt install -y android-sdk adb"
            ;;
        arch|manjaro|endeavouros)
            echo -e "  ${CYAN}# 1. Install OpenJDK 21:${NC}"
            echo -e "  sudo pacman -S jdk21-openjdk android-tools"
            echo -e "  ${CYAN}# 2. Install Android SDK (via AUR or Android Studio):${NC}"
            echo -e "  yay -S android-sdk android-sdk-platform-tools android-platform-34"
            ;;
        fedora|rhel|centos)
            echo -e "  ${CYAN}# 1. Install OpenJDK 21 & tools:${NC}"
            echo -e "  sudo dnf install -y java-21-openjdk-devel android-tools"
            ;;
        *)
            echo -e "  Install OpenJDK 21 and the Android SDK (Platform 34, Build-Tools 34.0.0)."
            ;;
    esac
    echo -e "\nAlternatively, install ${BOLD}Android Studio${NC} from https://developer.android.com/studio"
    echo -e "It includes JDK, SDK Manager, and platforms out of the box."
    exit 1
fi

echo -e "${GREEN}${BOLD}✓ All development dependencies are satisfied!${NC}\n"

if [[ "${CHECK_ONLY}" == true ]]; then
    echo "Check completed successfully. Exiting."
    exit 0
fi

# Ensure Gradle wrapper is executable
chmod +x ./gradlew

# Step: Gradle Assembly
echo -e "${CYAN}Compiling and Packaging APK...${NC}"

GRADLE_TASKS=()
if [[ "${CLEAN_FIRST}" == true ]]; then
    GRADLE_TASKS+=("clean")
fi

if [[ "${BUILD_TYPE}" == "debug" ]]; then
    GRADLE_TASKS+=(":app:assembleDebug")
    EXPECTED_SOURCE="app/build/outputs/apk/debug/app-debug.apk"
    TARGET_FILENAME="BElectronicsVP-v1.0.0-debug.apk"
else
    GRADLE_TASKS+=(":app:assembleRelease")
    EXPECTED_SOURCE="app/build/outputs/apk/release/app-release-unsigned.apk"
    TARGET_FILENAME="BElectronicsVP-v1.0.0-release-unsigned.apk"
fi

echo -e "  Running: ${BOLD}./gradlew ${GRADLE_TASKS[*]}${NC}"
./gradlew "${GRADLE_TASKS[@]}"

# Step: Verification and Distribution Copy
if [[ ! -f "${EXPECTED_SOURCE}" ]]; then
    FALLBACK_RELEASE="app/build/outputs/apk/release/app-release.apk"
    if [[ -f "${FALLBACK_RELEASE}" ]]; then
        EXPECTED_SOURCE="${FALLBACK_RELEASE}"
        TARGET_FILENAME="BElectronicsVP-v1.0.0-release.apk"
    else
        echo -e "${RED}Error: Expected APK artifact not found at ${EXPECTED_SOURCE}${NC}"
        exit 1
    fi
fi

mkdir -p dist
DIST_PATH="dist/${TARGET_FILENAME}"
cp -f "${EXPECTED_SOURCE}" "${DIST_PATH}"

APK_SIZE_BYTES=$(wc -c < "${DIST_PATH}")
APK_SIZE_MB=$(awk "BEGIN {printf \"%.2f\", ${APK_SIZE_BYTES}/1048576}")
SHA256_HASH=$(sha256sum "${DIST_PATH}" | awk '{print $1}')

echo -e "\n${GREEN}✓ APK successfully generated and packaged!${NC}"
echo -e "  Location: ${BOLD}${DIST_PATH}${NC}"
echo -e "  Size:     ${BOLD}${APK_SIZE_MB} MB${NC} (${APK_SIZE_BYTES} bytes)"
echo -e "  SHA-256:  ${BOLD}${SHA256_HASH}${NC}"

# Step: Optional ADB Installation
if [[ "${AUTO_INSTALL}" == true ]]; then
    echo -e "\n${CYAN}Deploying to Connected Device...${NC}"
    if ! command -v adb >/dev/null 2>&1 && [[ ! -x "${SDK_DIR}/platform-tools/adb" ]]; then
        echo -e "  ${YELLOW}Warning: 'adb' tool not found. Skipping auto-install.${NC}"
    else
        ADB_CMD="adb"
        if ! command -v adb >/dev/null 2>&1; then
            ADB_CMD="${SDK_DIR}/platform-tools/adb"
        fi
        echo -e "  Checking connected devices..."
        DEVICE_COUNT=$("${ADB_CMD}" devices | grep -v "List of devices" | grep -c "device$" || true)
        if [[ "${DEVICE_COUNT}" -gt 0 ]]; then
            echo -e "  Installing ${DIST_PATH} onto connected device..."
            "${ADB_CMD}" install -r "${DIST_PATH}"
            echo -e "  ${GREEN}✓ Installation complete!${NC}"
        else
            echo -e "  ${YELLOW}No authorized device found with 'adb devices'. Connect your device with USB Debugging enabled.${NC}"
        fi
    fi
else
    echo -e "\n${BOLD}Installation Instructions:${NC}"
    echo -e "  To install directly on your phone via USB/Wi-Fi:"
    echo -e "  ${BOLD}adb install -r ${DIST_PATH}${NC}"
    echo -e "  Or copy ${BOLD}${DIST_PATH}${NC} to phone storage and tap to install."
fi

echo -e "\n${GREEN}${BOLD}==============================================================================${NC}"
echo -e "${GREEN}${BOLD}Build Finished Successfully! ⚡ Ready for testing and distribution.${NC}"
echo -e "${GREEN}${BOLD}==============================================================================${NC}"
