# Keuwlsoft Serial Protocol Specification 📡

This document details the reverse-engineered serial communication protocol used by the original **Bluetooth Electronics** application (Keuwlsoft) and natively supported by **BElectronicsVP**.

---

## 1. Protocol Architecture & Framing

Communication is bidirectional and packet-based, using standard ASCII over stream-based serial connections (Bluetooth Classic SPP or BLE UART).

### Framing Rule
Every telemetry packet begins and ends with an asterisk delimiter (`*`, ASCII `0x2A`):

```text
*<IDENTIFIER><PAYLOAD>*
```

* **Start Delimiter**: `*`
* **Identifier**: A single case-sensitive alphanumeric character (e.g. `A`-`Z`, `a`-`z`, `0`-`9`) designating the target widget ID.
* **Payload**: ASCII string representing numbers, text, or multi-channel values.
* **End Delimiter**: `*`

> **Noise Handling**: Any byte received outside the asterisks (such as connection noise, bootloader messages, or unformatted `Serial.println()` output) is safely ignored by the parser state machine.

---

## 2. Telemetry: Microcontroller ➡️ Phone (Incoming)

### A. Numeric Telemetry (Gauges, Bars, Values)
Updates analog dial gauges, progress bars, and numeric indicator displays.

| Example Packet | Identifier | Parsed Value | Target Widget |
| :--- | :---: | :---: | :--- |
| `*A125*` | `A` | `125` | Dial Gauge `A` set to 125 |
| `*B-40*` | `B` | `-40` | Temperature Gauge `B` set to -40 |
| `*S78.5*` | `S` | `78.5` | Slider/Bar `S` set to 78.5 |

* **Range Clamping**: The widget defines `min` and `max` values in its configuration. If the received value exceeds the range, it is clamped to the boundary without crashing.

### B. Text & Status Displays
Updates string labels, status indicators, and notification boxes.

| Example Packet | Identifier | Displayed Text | Target Widget |
| :--- | :---: | :--- | :--- |
| `*TSystem Ready*` | `T` | `System Ready` | Text Display `T` |
| `*EERR: Overheat!*` | `E` | `ERR: Overheat!` | Text Display `E` |

### C. Multi-Channel Color (RGB LED Widgets)
Used by multi-color status lights and RGB lamps.

```text
*R<red>G<green>B<blue>*
```
* Each channel ranges from `0` to `255`.
* Example: `*R255G128B0*` sets the color to orange.

### D. Graph / Oscilloscope Channels
Time-series data points sent at periodic intervals.
* Example: `*G45*` followed by `*G48*`, `*G52*` plots sequential points on Chart `G`.

---

## 3. Controls: Phone ➡️ Microcontroller (Outbound)

Control events are generated when the user interacts with dashboard widgets.

### A. Buttons (Momentary & Latching)
* **Momentary Button**:
  * Touch Down: Transmits configured `Press Char` (e.g., `'1'` or `'A'`).
  * Touch Up: Transmits configured `Release Char` (e.g., `'0'` or `'a'`).
* **Latching / Toggle Switch**:
  * State ON: Transmits configured ON command (e.g., `'H'`).
  * State OFF: Transmits configured OFF command (e.g., `'L'`).

### B. Sliders & Knobs
* When dragged, transmits the current value formatted according to widget settings:
  ```text
  *<ID><INTEGER_VALUE>*
  ```
  Example: `*K180*` (servo angle 180°).
* **Throttling**: To prevent buffer overflow on microcontrollers with limited UART buffers (like the 64-byte Arduino Uno hardware buffer), transmissions are rate-limited to a configurable interval (typically 50–100 ms).

### C. Directional Pads & Joysticks
* Transmits directional characters:
  * Forward: `F`
  * Reverse: `B`
  * Left: `L`
  * Right: `R`
  * Center/Stop: `S`

---

## 4. Transport Layer Implementation

### Bluetooth Classic (SPP - RFCOMM)
* **UUID**: `00001101-0000-1000-8000-00805F9B34FB` (Standard SerialPortServiceClass_UUID).
* **Reflection Fallback**: For devices that fail standard SDP negotiation, BElectronicsVP automatically falls back to an RFCOMM channel 1 direct socket (`createRfcommSocket(1)`).
* **Baud Rates**: 9600 (default for HC-05/06), 38400, 57600, 115200.

### Bluetooth Low Energy (BLE UART)
* **Service UUID**: `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` (Nordic UART Service)
* **TX Characteristic (Phone ➡️ Device)**: `6E400002-B5A3-F393-E0A9-E50E24DCCA9E` (Write without response)
* **RX Characteristic (Device ➡️ Phone)**: `6E400003-B5A3-F393-E0A9-E50E24DCCA9E` (Notify)
* **MTU Chunking**: In BLE mode, outbound packets larger than 18 bytes are chunked into 18-byte segments to guarantee delivery over default 20-byte MTU channels.
