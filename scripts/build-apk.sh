#!/usr/bin/env bash
# ==============================================================================
# BElectronicsVP - Automated APK Build & Packaging Script
# ==============================================================================
# This script compiles, packages, and verifies the Android APK for BElectronicsVP.
# Output binaries are placed in the 'dist/' folder at the project root.
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

print_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "  ____  _____ _           _                   _         __     ______  "
    echo " | __ )| ____| | ___  ___| |_ _ __ ___  _ __ (_) ___ ___\\ \\   / /  _ \\ "
    echo " |  _ \\|  _| | |/ _ \\/ __| __| '__/ _ \\| '_ \\| |/ __/ __|\\ \\ / /| |_) |"
    echo " | |_) | |___| |  __/ (__| |_| | | (_) | | | | | (__\\__ \\ \\ V / |  __/ "
    echo " |____/|_____|_|\\___|\\___|\\__|_|  \\___/|_| |_|_|\\___|___/  \\_/  |_|    "
    echo -e "${NC}"
    echo -e "${BOLD}BElectronicsVP Android APK Packaging Utility${NC}\n"
}

usage() {
    echo -e "Usage: ${BOLD}./scripts/build-apk.sh [OPTIONS]${NC}"
    echo ""
    echo "Options:"
    echo "  -d, --debug       Build Debug APK (default, pre-signed with debug key)"
    echo "  -r, --release     Build Release APK (unsigned)"
    echo "  -c, --clean       Perform a clean Gradle build before assembling"
    echo "  -i, --install     Automatically install APK via adb to a connected device"
    echo "  -h, --help        Display this help message"
    echo ""
    echo "Examples:"
    echo "  ./scripts/build-apk.sh              # Build debug APK"
    echo "  ./scripts/build-apk.sh -c           # Clean and build debug APK"
    echo "  ./scripts/build-apk.sh -r           # Build release APK"
    echo "  ./scripts/build-apk.sh -i           # Build and install to connected phone"
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

# Step 1: Check Java Environment
echo -e "${CYAN}[1/5] Checking Java Environment...${NC}"
if ! command -v java >/dev/null 2>&1; then
    echo -e "${RED}Error: Java is not found in PATH.${NC}"
    echo "Please install JDK 17 or JDK 21 (e.g. OpenJDK 21) and ensure JAVA_HOME is configured."
    exit 1
fi

JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -n 1)
echo -e "  Found: ${GREEN}${JAVA_VERSION_OUTPUT}${NC}"

# Step 2: Check Android SDK Environment
echo -e "\n${CYAN}[2/5] Checking Android SDK...${NC}"
SDK_DIR=""

if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then
    SDK_DIR="${ANDROID_HOME}"
elif [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}" ]]; then
    SDK_DIR="${ANDROID_SDK_ROOT}"
elif [[ -f "local.properties" ]]; then
    SDK_DIR=$(grep -E '^sdk.dir=' local.properties | cut -d'=' -f2 | tr -d '\r' || true)
fi

# Fallback auto-detection for common SDK paths
if [[ -z "${SDK_DIR}" || ! -d "${SDK_DIR}" ]]; then
    COMMON_PATHS=(
        "$HOME/Android/Sdk"
        "$HOME/Library/Android/sdk"
        "/opt/android-sdk"
        "/usr/lib/android-sdk"
    )
    for p in "${COMMON_PATHS[@]}"; do
        if [[ -d "$p" ]]; then
            SDK_DIR="$p"
            echo -e "  ${YELLOW}Detected Android SDK at: ${SDK_DIR}${NC}"
            echo "sdk.dir=${SDK_DIR}" > local.properties
            echo -e "  Created ${GREEN}local.properties${NC} with detected SDK location."
            break
        fi
    done
fi

if [[ -z "${SDK_DIR}" || ! -d "${SDK_DIR}" ]]; then
    echo -e "${RED}Error: Android SDK could not be located.${NC}"
    echo "Please set ANDROID_HOME or configure sdk.dir in local.properties."
    echo "Example: export ANDROID_HOME=$HOME/Android/Sdk"
    exit 1
fi
echo -e "  Android SDK: ${GREEN}${SDK_DIR}${NC}"

# Ensure Gradle wrapper is executable
chmod +x ./gradlew

# Step 3: Gradle Assembly
echo -e "\n${CYAN}[3/5] Compiling and Packaging APK...${NC}"

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

# Step 4: Verification and Distribution Copy
echo -e "\n${CYAN}[4/5] Verifying Package Artifacts...${NC}"

if [[ ! -f "${EXPECTED_SOURCE}" ]]; then
    # In case release output had a slightly different name
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

echo -e "  ${GREEN}✓ APK successfully generated and packaged!${NC}"
echo -e "  Location: ${BOLD}${DIST_PATH}${NC}"
echo -e "  Size:     ${BOLD}${APK_SIZE_MB} MB${NC} (${APK_SIZE_BYTES} bytes)"
echo -e "  SHA-256:  ${BOLD}${SHA256_HASH}${NC}"

# Step 5: Optional ADB Installation
echo -e "\n${CYAN}[5/5] Deployment & Installation${NC}"
if [[ "${AUTO_INSTALL}" == true ]]; then
    if ! command -v adb >/dev/null 2>&1; then
        echo -e "  ${YELLOW}Warning: 'adb' tool not found in PATH. Skipping auto-install.${NC}"
    else
        echo -e "  Checking connected devices via adb..."
        DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -c "device$" || true)
        if [[ "${DEVICE_COUNT}" -gt 0 ]]; then
            echo -e "  Installing ${DIST_PATH} onto connected device..."
            adb install -r "${DIST_PATH}"
            echo -e "  ${GREEN}✓ Installation complete!${NC}"
        else
            echo -e "  ${YELLOW}No authorized device found with 'adb devices'. Connect your device with USB Debugging enabled.${NC}"
        fi
    fi
else
    echo -e "  To install directly on your Android phone via USB/Wi-Fi:"
    echo -e "  ${BOLD}adb install -r ${DIST_PATH}${NC}"
    echo -e "  Or simply copy ${BOLD}${DIST_PATH}${NC} to your phone storage and open it to install."
fi

echo -e "\n${GREEN}${BOLD}==============================================================================${NC}"
echo -e "${GREEN}${BOLD}Build Finished Successfully! ⚡ Ready for testing and distribution.${NC}"
echo -e "${GREEN}${BOLD}==============================================================================${NC}"
