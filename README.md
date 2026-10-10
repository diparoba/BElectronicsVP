# BElectronicsVP ⚡

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-Documented-00BCD4.svg)](openspec/changes/modern-rewrite-belectronics/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**BElectronicsVP** is an open-source, modernized rewrite of the legendary **Bluetooth Electronics** Android application originally created by Keuwlsoft. 

For years, *Bluetooth Electronics* was the go-to mobile dashboard for makers, students, and engineers prototyping with Arduino, ESP32, PIC, and Raspberry Pi. However, the original closed-source app was frozen in time: written as a monolithic Java activity using pre-Android 12 Bluetooth APIs, causing crashes on modern devices due to missing runtime permissions (`BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`), and relying on legacy fixed-coordinate canvas rendering.

BElectronicsVP revives this classic tool from the ground up using **Kotlin**, **Jetpack Compose**, and **Coroutines/Flow**, while maintaining **100% backwards compatibility** with the reverse-engineered Keuwlsoft serial protocol.

---

## 🌟 Key Features

* **100% Protocol Backwards Compatibility**: Works immediately with your existing Arduino sketches and hardware without changing a single line of microcode (`*<ID><VALUE>*`).
* **Modern Bluetooth Stack**: Full support for both **Bluetooth Classic (SPP RFCOMM)** (HC-05, HC-06, JDY-30) and **Bluetooth Low Energy (BLE)** (ESP32, HC-08, HM-10) with Android 12+ runtime permissions.
* **Declarative Jetpack Compose UI**: Clean, responsive layout engine with Material 3 styling, adaptive orientation support, and dark/light themes.
* **Rich Widget Palette**: Buttons, momentary pads, sliders, analog needle gauges, LED indicators, text displays, and real-time graph plotters.
* **Open JSON Panel Sharing**: Store, version control, and share custom dashboards with open `.json` / `.bep` schemas—no proprietary lock-in.
* **Documented with OpenSpec**: Every specification, protocol contract, and architectural design is tracked and structured for collaborative development.

---

## 📐 Architecture

BElectronicsVP follows Clean Architecture and Android modularity:

```
+-----------------------------------------------------------+
|                      :app (Presentation)                  |
|  - Jetpack Compose Screens (PanelScreen, EditorScreen)    |
|  - Widget Composables (Buttons, Gauges, Sliders, Graphs)  |
|  - Material 3 Design System & Device Connection Dialog    |
+-----------------------------+-----------------------------+
                              |
                              v
+-----------------------------------------------------------+
|                    :feature-panels                        |
|  - PanelManager (CRUD operations, dynamic state binding)  |
|  - Open JSON Panel Parser & Serialization                 |
+-----------------------------+-----------------------------+
                              |
                              v
+-----------------------------------------------------------+
|                    :core-protocol                         |
|  - Framing State Machine (*<ID><VALUE>*)                  |
|  - Streaming zero-allocation serial parser                |
|  - Outbound command serializer with rate limiting         |
+-----------------------------+-----------------------------+
                              |
                              v
+-----------------------------------------------------------+
|                   :core-bluetooth                         |
|  - Bluetooth Classic (SPP RFCOMM with Channel 1 fallback) |
|  - Bluetooth Low Energy (BLE GATT UART service)           |
|  - StateFlow connection state & permission helpers        |
+-----------------------------------------------------------+
```

---

## 📡 Serial Protocol at a Glance

The app uses the proven Keuwlsoft asterisk delimiter protocol:

### Microcontroller ➡️ Phone (Incoming Telemetry)
```text
*<WIDGET_ID><VALUE>*
```
* `*A125*` ➡️ Sets value of widget `A` (e.g. gauge or slider) to `125`.
* `*TTemperature: 24.5 C*` ➡️ Updates text display widget `T`.
* `*R255G120B0*` ➡️ Sets color widget to RGB `(255, 120, 0)`.

### Phone ➡️ Microcontroller (Outbound Controls)
* **Buttons**: Transmit custom strings or characters upon press and release (e.g., `'1'` on press, `'0'` on release).
* **Sliders**: Transmit formatted strings with current slider values (e.g., `*S75*` or custom prefixes).

👉 **[Read the complete Protocol Specification](docs/protocol-specification.md)**

---

## 🛠️ Microcontroller Quickstart (Arduino & ESP32)

We provide tested, ready-to-flash example sketches for:
* **Arduino Uno / Nano with HC-05 or HC-06 module**: SoftwareSerial telemetry and command receiving.
* **ESP32 (Bluetooth Classic SPP)**: Native `BluetoothSerial` implementation without external modules.
* **ESP32 (Bluetooth Low Energy / BLE UART)**: Low energy service with notification streaming.

👉 **[Explore Arduino & ESP32 Examples](docs/arduino-quickstart.md)**

---

## 🚀 Packaging & Installing the APK

BElectronicsVP provides an automated script to build, verify, and package the Android APK directly into a `dist/` directory:

### Prerequisites
* **Java**: JDK 17 or JDK 21 (e.g. OpenJDK 21)
* **Android SDK**: API 34 platform & build-tools (set via `ANDROID_HOME` or automatically detected by the script)

### Quick Build
To compile and package the Debug APK (pre-signed with debug key, ready to sideload on any Android device):
```bash
./build-apk.sh
```
Or execute directly from `scripts/`:
```bash
./scripts/build-apk.sh
```

### Script Options
| Option | Description |
|---|---|
| `-d, --debug` | Build Debug APK (default, ready to install on phones) |
| `-r, --release` | Build Release APK (unsigned) |
| `-c, --clean` | Run `./gradlew clean` before compiling |
| `-i, --install` | Automatically install APK onto a connected device via `adb` |
| `-h, --help` | Show command usage and options |

### Output Artifacts
Packaged APKs are output to the `dist/` directory at the project root:
* **Debug APK**: `dist/BElectronicsVP-v1.0.0-debug.apk`
* **Release APK**: `dist/BElectronicsVP-v1.0.0-release-unsigned.apk`

### Installing to Android Device
Via ADB:
```bash
adb install -r dist/BElectronicsVP-v1.0.0-debug.apk
```
Or simply copy `dist/BElectronicsVP-v1.0.0-debug.apk` to your phone storage (USB, email, Google Drive) and open it to install.

---

## 📋 OpenSpec Development

This project uses [OpenSpec](openspec/) to ensure all features, protocols, and tasks are transparently planned and documented:

* **Main Specifications**:
  * [Bluetooth Communication Spec](openspec/specs/bluetooth-communication/spec.md)
  * [Serial Protocol Spec](openspec/specs/serial-protocol/spec.md)
  * [Panel Management Spec](openspec/specs/panel-management/spec.md)
* **Archived Modern Rewrite Change**: [`openspec/changes/archive/2026-10-10-modern-rewrite-belectronics/`](openspec/changes/archive/2026-10-10-modern-rewrite-belectronics/)
  * [Proposal](openspec/changes/archive/2026-10-10-modern-rewrite-belectronics/proposal.md)
  * [Design Architecture](openspec/changes/archive/2026-10-10-modern-rewrite-belectronics/design.md)
  * [Task Checklist (17/17 Complete)](openspec/changes/archive/2026-10-10-modern-rewrite-belectronics/tasks.md)

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
All reverse engineering was performed cleanly for interoperability, research, and modern device accessibility.

