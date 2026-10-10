# Bluetooth Communication Specification

## Purpose

Provides hardware connectivity over Bluetooth Classic (SPP) and Bluetooth Low Energy (BLE) to exchange telemetry and control commands with microcontrollers.

## Requirements

### Requirement: Bluetooth Device Discovery
The system SHALL scan for nearby paired and unadvertised Bluetooth Classic and BLE devices while requesting all necessary Android runtime permissions.

#### Scenario: User scans for devices with granted permissions
- **WHEN** user initiates device scan and grants Bluetooth permissions
- **THEN** system displays list of available Bluetooth Classic and BLE peripheral devices with their MAC address and signal strength

#### Scenario: User denies Bluetooth permissions
- **WHEN** user denies runtime permissions on Android 12+
- **THEN** system displays explanatory rationale dialog guiding the user to app settings without crashing

### Requirement: Paired Device Search and Filtering
The system SHALL provide real-time search filtering across paired and discovered Bluetooth devices.

#### Scenario: Filter devices by name or MAC address
- **WHEN** user types a query into the device search bar
- **THEN** system instantly filters the list showing only matching devices by name or MAC address with device type tags

### Requirement: Bluetooth Classic Connection Lifecycle
The system SHALL establish and maintain an RFCOMM SPP socket connection with automatic fallback to channel 1 reflection.

#### Scenario: Successful connection to HC-05 device
- **WHEN** user selects a paired HC-05 device from the list
- **THEN** system connects via RFCOMM socket, exposes connection StateFlow as Connected, and starts background I/O streams

#### Scenario: Connection dropped unexpectedly
- **WHEN** the remote Bluetooth device powers off or moves out of range
- **THEN** system detects socket closure, notifies user with a toast or snackbar, and transitions StateFlow to Disconnected

### Requirement: Universal BLE GATT Discovery and MTU Negotiation
The system SHALL dynamically resolve BLE UART services across Nordic UART (NUS), legacy HM-10, and custom profiles, negotiating maximum MTU for modern ESP32 microcontrollers.

#### Scenario: Connect to modern ESP32 running Nordic UART service
- **WHEN** user connects to an ESP32 advertising Nordic UART Service UUID 6E400001
- **THEN** system discovers TX and RX characteristics, requests MTU of 512 bytes, enables notifications, and transitions to Connected

#### Scenario: Connect to custom or legacy BLE peripheral
- **WHEN** user connects to a BLE device without standard Nordic UUID
- **THEN** system scans all services for Notify and Write characteristics as an automatic fallback rather than failing connection

### Requirement: Auto-Reconnect and In-Dashboard Quick Connect
The system SHALL remember the last connected device and enable single-tap reconnection directly inside the active dashboard screen.

#### Scenario: Tap reconnect chip from dashboard top bar
- **WHEN** user taps the disconnected status chip in the dashboard header
- **THEN** system initiates direct connection to the last synced device in the background without opening connection modal

#### Scenario: Automatic connection on dashboard open
- **WHEN** user opens a dashboard with auto-reconnect preference enabled
- **THEN** system automatically establishes connection to the last saved device and displays connection progress spinner
