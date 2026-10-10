# Tasks: BElectronicsVP Modern Rewrite

## 1. Project Initialization & Documentation

- [x] 1.1 Create root `README.md` introducing BElectronicsVP, its history as a reverse-engineered modern rewrite of Keuwlsoft Bluetooth Electronics, architecture, and OpenSpec documentation. Verify formatting and links.
- [x] 1.2 Create `docs/protocol-specification.md` documenting the reverse-engineered serial protocol (`*<ID><VALUE>*`), data types, and timing constraints. Verify file exists and is linked in README.
- [x] 1.3 Create `docs/arduino-quickstart.md` with complete, working sketches for Arduino Uno (HC-05/06) and ESP32. Verify code syntax and serial command parity.
- [x] 1.4 Initialize local git repository, commit the project documentation, and create the public GitHub repository `BElectronicsVP` via `gh repo create`. Verify remote push succeeds.

## 2. Core Protocol Engine

- [x] 2.1 Create `core-protocol` module with `ProtocolFrame` sealed class and streaming asterisk state machine parser. Verify unit tests pass for intact, fragmented, and corrupted packets.
- [x] 2.2 Implement outbound command serializer supporting button touch-down/up characters and slider values. Verify unit tests for rate limiting and payload formatting.

## 3. Bluetooth Communication Layer

- [x] 3.1 Implement runtime permission helper handling Android 12+ (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`) with rationale dialogs. Verify permission flow logic.
- [x] 3.2 Implement Bluetooth Classic (SPP) connection manager using Coroutines and `StateFlow`, including RFCOMM fallback to channel 1. Verify connection state transitions.
- [x] 3.3 Implement Universal Bluetooth Low Energy (BLE) client with Nordic UART (NUS) support, legacy fallback, dynamic characteristic resolver, and MTU 512 negotiation for ESP32. Verify characteristic discovery and notify streams.
- [x] 3.4 Implement paired device search and real-time filtering helper. Verify filter by name and MAC address.
- [x] 3.5 Implement auto-reconnection and last-device persistence store. Verify state recall and reconnection triggers.

## 4. Panel Data Models & JSON Serialization

- [x] 4.1 Define `PanelModel`, `WidgetConfig`, and concrete widget descriptors (`Button`, `Slider`, `Switch`, `Gauge`, `TextDisplay`, `Graph`) with `@Serializable`. Verify round-trip JSON serialization tests.
- [x] 4.2 Implement panel persistence manager providing create, save, clone, and export/import operations for `.json` and `.bep` panel files. Verify import validation.

## 5. UI Presentation & Jetpack Compose Canvas

- [x] 5.1 Implement Material 3 dashboard list screen with CRUD operations (create, rename, clone, delete) and Android Share Sheet export/import integration. Verify UI actions and file sharing.
- [x] 5.2 Implement active dashboard screen with in-header interactive connection chip (1-tap reconnect without exiting panel) and device picker dialog with real-time search. Verify connection transitions.
- [x] 5.3 Implement interactive Compose widget components: `ButtonWidget`, `SliderWidget`, `GaugeWidget` (custom Canvas needle rendering), `TextDisplayWidget`, and `GraphWidget`. Verify state binding to protocol events.
- [x] 5.4 Implement `PanelCanvas` layout container supporting orientation rotation, dynamic coordinate scaling, and drag-and-drop editor mode. Verify widget placement and drag handling.

## Workflow follow-up

- Sync and archive OpenSpec change upon full implementation.
