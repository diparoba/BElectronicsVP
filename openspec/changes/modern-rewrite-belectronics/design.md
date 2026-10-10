# Design: BElectronicsVP Modern Rewrite

## Context

The legacy implementation `com.keuwl.arduinobluetooth` centered around a 13,875-line monolithic Java Activity (`BlueToothMainActivity.java`) combined with manual Android Canvas drawing classes (`a.java`, `b.java`). It relied on synchronous blocking socket reads, legacy RFCOMM reflection, and pre-Android 12 Bluetooth permissions.

See `proposal.md` for overall motivation.

## Goals / Non-Goals

**Goals:**
- Provide a clean, modular Android project using Kotlin and Jetpack Compose.
- Establish robust Bluetooth Classic (SPP) and BLE communication layers using Kotlin Coroutines and Flow.
- Implement an exact, battle-tested parser for the Keuwlsoft asterisk protocol (`*<ID><VAL>*`).
- Support responsive, drag-and-drop dashboard design with customizable widgets (buttons, sliders, gauges, labels, graphs).
- Provide human-readable JSON serialization for exporting and sharing dashboard configurations.
- Provide comprehensive project documentation, architecture guides, and Arduino sample sketches.

**Non-Goals:**
- Cross-platform support (iOS/Desktop is out of scope; focus is 100% Android native with Kotlin & Compose).
- Legacy Android 4.x/5.x/6.x/7.x backwards compatibility (minSdk is set to 26 / Android 8.0 Oreo).
- Proprietary advertising SDKs, tracking, or in-app purchases.

## Decisions

### 1. Multi-Module Project Architecture
- **Decision**: Split the Android project into cohesive feature and core modules:
  - `:core:bluetooth` (Hardware communication, permissions, connection state)
  - `:core:protocol` (Framing, parsing, and serialization)
  - `:core:model` (Panel, widget, and configuration data models)
  - `:feature:panels` (Dashboard rendering, widget canvas, state binding)
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

### 5. Open JSON Panel Schema
- **Decision**: Store and export panel layouts as standard JSON files (`.bep` / `.json`) using `kotlinx.serialization`.
- **Rationale**: The original app used an undocumented format. JSON allows users to backup, version control, and share panels directly through GitHub or messaging apps.

## Risks / Trade-offs

- **[Risk] Bluetooth socket variations across Android vendors** → **Mitigation**: Implement standard RFCOMM SPP socket creation first, with an automated fallback to channel 1 reflection (`createRfcommSocket(1)`) if standard discovery fails, mirroring the decompiled app's proven fallback.
- **[Risk] High-frequency telemetry overwhelming UI frame rate (e.g. fast sensor streams)** → **Mitigation**: Introduce configurable throttle/sample intervals on the incoming Flow before passing updates to Compose state.
- **[Risk] Android 12+ Bluetooth runtime permission denials** → **Mitigation**: Implement `Accompanist` or modern ActivityResultContracts with user-friendly educational rationale screens before requesting `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.
