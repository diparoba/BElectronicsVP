# Spec Delta: Bluetooth Communication

## Purpose

Provides hardware connectivity over Bluetooth Classic (SPP) and Bluetooth Low Energy (BLE) to exchange telemetry and control commands with microcontrollers.

## ADDED Requirements

### Requirement: Bluetooth Device Discovery
The system SHALL scan for nearby paired and unadvertised Bluetooth Classic and BLE devices while requesting all necessary Android runtime permissions.

#### Scenario: User scans for devices with granted permissions
- **WHEN** user initiates device scan and grants Bluetooth permissions
- **THEN** system displays list of available Bluetooth Classic and BLE peripheral devices with their MAC address and signal strength

#### Scenario: User denies Bluetooth permissions
- **WHEN** user denies runtime permissions on Android 12+
- **THEN** system displays explanatory rationale dialog guiding the user to app settings without crashing

### Requirement: Bluetooth Classic Connection Lifecycle
The system SHALL establish and maintain an RFCOMM SPP socket connection with automatic fallback to channel 1 reflection.

#### Scenario: Successful connection to HC-05 device
- **WHEN** user selects a paired HC-05 device from the list
- **THEN** system connects via RFCOMM socket, exposes connection StateFlow as Connected, and starts background I/O streams

#### Scenario: Connection dropped unexpectedly
- **WHEN** the remote Bluetooth device powers off or moves out of range
- **THEN** system detects socket closure, notifies user with a toast or snackbar, and transitions StateFlow to Disconnected

### Requirement: Bluetooth Low Energy Connection
The system SHALL connect to BLE UART peripherals and negotiate appropriate MTU sizes for packet streaming.

#### Scenario: Connect to ESP32 BLE peripheral
- **WHEN** user connects to a BLE device advertising Nordic or standard UART service
- **THEN** system discovers characteristics, enables notifications on RX characteristic, and marks connection as ready
