# Proposal: BElectronicsVP Modern Rewrite

## Why

The legendary Android app *Bluetooth Electronics* (by Keuwlsoft) has been a foundational tool for millions of electronics hobbyists, makers, and students prototyping with Arduino, ESP32, and PIC microcontrollers. However, the original application was built with legacy Android SDKs, monolithic Java code, and outdated UI patterns. On modern devices (Android 12+), it suffers from permission crashes (`BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`), lacks responsive scaling, and is closed to community contributions.

BElectronicsVP revives and modernizes this classic tool as an open-source, community-driven native Android application written in Kotlin and Jetpack Compose. It preserves 100% backwards compatibility with existing Arduino sketches and Keuwlsoft serial protocols while providing a reactive UI, open JSON panel sharing, and comprehensive OpenSpec documentation.

## What Changes

- **Complete Native Modernization**: Re-architect the application in 100% Kotlin using Jetpack Compose, Material 3, and Kotlin Coroutines/Flow, replacing the decompiled 14k-line monolithic Java activity.
- **Modern Bluetooth Engine**: Implement robust Bluetooth Classic (SPP RFCOMM) and Bluetooth Low Energy (BLE) connectivity with full support for runtime permissions on modern Android versions (API 26 through API 35+).
- **Reverse-Engineered Protocol Engine**: Reconstruct the exact Keuwlsoft bidirectional serial protocol (`*<ID><VALUE>*`) so existing hardware projects and Arduino sketches work out of the box without code modifications.
- **Dynamic Panel & Widget Canvas**: Provide a responsive, customizable dashboard supporting buttons, sliders, switches, analog gauges, text displays, and charts with open JSON import/export.
- **Open Documentation & Public Repository**: Initialize the public GitHub repository `BElectronicsVP`, providing detailed architectural documentation, protocol specifications, and Arduino quickstart sketches.

## Capabilities

### New Capabilities

- `bluetooth-communication`: Handles device scanning, pairing, connection lifecycle management for Bluetooth Classic (SPP) and BLE, connection state broadcasting via Flow, and runtime permission handling.
- `serial-protocol`: Encapsulates bidirectional serial message framing, parsing of asterisk-delimited commands (`*<ID><VAL>*`), message batching, and routing to registered UI components.
- `panel-management`: Defines panel layout models, widget configuration (ID bindings, ranges, labels), drag-and-drop editor canvas, and JSON import/export schema for sharing dashboards.

### Modified Capabilities

*(None - greenfield rewrite change)*

## Impact

- **Target Platform**: Android 8.0+ (API level 26 minimum, API level 35 target).
- **Core Dependencies**: Kotlin 2.x, Jetpack Compose Material 3, Coroutines & Flow, AndroidX Lifecycle, Kotlinx Serialization.
- **Hardware Compatibility**: HC-05, HC-06, JDY-30, ESP32 (Bluetooth Classic SPP & BLE), HC-08, HM-10.
- **Ecosystem**: Public open-source repository at `BElectronicsVP` with OpenSpec specifications.
