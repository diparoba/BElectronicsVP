# Design: BElectronicsVP Modern Rewrite

## Context

The legacy implementation `com.keuwl.arduinobluetooth` centered around a 13,875-line monolithic Java Activity (`BlueToothMainActivity.java`) combined with manual Android Canvas drawing classes (`a.java`, `b.java`). It relied on synchronous blocking socket reads, legacy RFCOMM reflection, hardcoded HM-10 BLE UUIDs (`0000FFE0`), and pre-Android 12 Bluetooth permissions.

See `proposal.md` for overall motivation.

## Goals / Non-Goals

**Goals:**
- Provide a clean, modular Android project using Kotlin and Jetpack Compose.
- Establish robust Bluetooth Classic (SPP) and Universal BLE communication layers supporting modern ESP32 chips (ESP32-S3/C3/C6).
- Implement an exact, battle-tested parser for the Keuwlsoft asterisk protocol (`*<ID><VAL>*`).
- Support responsive, drag-and-drop dashboard design with customizable widgets (buttons, sliders, gauges, labels, graphs).
- Provide real-time device search, auto-reconnect, and interactive in-dashboard connection controls.
- Support complete dashboard lifecycle (create, rename, clone, delete) and native Android Share Sheet export/import.
- Provide comprehensive project documentation, architecture guides, and Arduino sample sketches.

**Non-Goals:**
- Cross-platform support (iOS/Desktop is out of scope; focus is 100% Android native with Kotlin & Compose).
- Legacy Android 4.x/5.x/6.x/7.x backwards compatibility (minSdk is set to 26 / Android 8.0 Oreo).
- Proprietary advertising SDKs, tracking, or in-app purchases.

## Decisions

### 1. Multi-Module Project Architecture
- **Decision**: Split the Android project into cohesive feature and core modules:
  - `:core:bluetooth` (Hardware communication, permissions, connection state, universal BLE resolver)
  - `:core:protocol` (Framing, parsing, and serialization)
  - `:core:model` (Panel, widget, and configuration data models)
  - `:feature:panels` (Dashboard rendering, widget canvas, state binding, CRUD manager)
  - `:feature:editor` (Visual drag-and-drop layout designer)
  - `:app` (Navigation, theme, application entry point)
- **Rationale**: Isolates Bluetooth complexity from UI rendering and allows unit testing of the serial parser without Android framework dependencies.
- **Alternatives Considered**: Monolithic `:app` module (rejected to prevent regression to legacy monolithic architecture).

### 2. UI Framework: Jetpack Compose + Material 3
- **Decision**: Build the UI entirely in Jetpack Compose, using custom Compose `Canvas` only where high-frequency needle drawing (e.g., analog gauges) is required.
- **Rationale**: Declarative UI drastically reduces state synchronization bugs between Bluetooth streams and UI widgets. Compose enables seamless dark/light mode and dynamic screen scaling.
- **Alternatives Considered**: XML Layouts + custom `View` hierarchy (rejected as outdated and verbose).

### 3. Asynchronous I/O via Kotlin Coroutines & Flow
- **Decision**: Model Bluetooth input and output streams using Coroutines and `SharedFlow`/`StateFlow`:
  - `connectionState: StateFlow<ConnectionState>` (Disconnected, Connecting, Connected, Error)
  - `incomingFrames: SharedFlow<ProtocolFrame>` (Stream of parsed `*ID...*` frames)
- **Rationale**: Replaces error-prone manual `Thread` management and `Thread.sleep()` calls with structured concurrency and backpressure handling.

### 4. Protocol Parser State Machine
- **Decision**: Implement a zero-allocation byte-level streaming state machine that identifies asterisk bounds `*`, extracts the single-character or multi-character tag identifier, and emits typed frame objects (`NumericFrame`, `ColorFrame`, `RawStringFrame`).
- **Rationale**: Microcontroller serial streams are subject to noise, packet fragmentation, and baud rate jitter. A streaming state machine ensures complete frame delivery without memory leaks.

### 5. Universal BLE GATT Service Resolver
- **Decision**: Implement an adaptive BLE resolver that replaces the legacy app's hardcoded `0000FFE0` check:
  - Priority 1: Nordic UART Service (NUS) UUID `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` (industry standard for modern ESP32).
  - Priority 2: Legacy HM-10 UUID `0000FFE0-0000-1000-8000-00805F9B34FB`.
  - Priority 3: Dynamic characteristic scanner (discovers any characteristic with `PROPERTY_NOTIFY` and `PROPERTY_WRITE`).
  - Negotiates 512-byte MTU (`requestMtu(512)`) to eliminate the 18-byte transmission ceiling.
- **Rationale**: Allows seamless out-of-the-box connectivity with all ESP32 BLE sketches (Arduino-ESP32, NimBLE, ESP-IDF).

### 6. Zero-Friction Connection UX & Auto-Reconnect
- **Decision**: Persist last connected device details in Jetpack DataStore and surface an interactive status chip directly inside the dashboard header:
  - If disconnected, single tap on `[🔴 ESP32 | Reconnect]` establishes background connection without exiting the dashboard.
  - Optional auto-reconnect upon dashboard launch.
  - Paired device picker includes real-time search filtering by device name and MAC address.
- **Rationale**: Completely eliminates the tedious 5-step connection ritual of the original application.

### 7. Dashboard CRUD & Native Android Share Sheet
- **Decision**: Support dashboard creation, renaming, cloning, and deletion. Export panels as JSON and invoke `Intent.ACTION_SEND` (Android Share Sheet) for instant sharing via WhatsApp, Drive, Telegram, and Quick Share.
- **Rationale**: Solves the closed proprietary panel storage of the original app, making community panel sharing effortless.

## Risks / Trade-offs

- **[Risk] Bluetooth socket variations across Android vendors** → **Mitigation**: Implement standard RFCOMM SPP socket creation first, with an automated fallback to channel 1 reflection (`createRfcommSocket(1)`) if standard discovery fails, mirroring the decompiled app's proven fallback.
- **[Risk] High-frequency telemetry overwhelming UI frame rate** → **Mitigation**: Introduce configurable throttle/sample intervals on the incoming Flow before passing updates to Compose state.
- **[Risk] Android 12+ Bluetooth runtime permission denials** → **Mitigation**: Implement educational rationale dialogs before requesting `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.
