# Arduino & ESP32 Quickstart Guide 🚀

This guide provides tested, ready-to-flash sketches demonstrating how to send telemetry and receive controls with **BElectronicsVP**.

---

## Example 1: Arduino Uno / Nano with HC-05 / HC-06

### Wiring Diagram
| HC-05 / HC-06 Pin | Arduino Pin | Notes |
| :--- | :--- | :--- |
| **VCC** | **5V** | Power supply |
| **GND** | **GND** | Common ground |
| **TXD** | **Pin 10 (RX)** | Direct connection |
| **RXD** | **Pin 11 (TX)** | Connect through voltage divider (5V ➡️ 3.3V) |

> ⚠️ **Important**: HC-05 RX operates at 3.3V logic. Use a simple voltage divider (e.g. 1kΩ and 2kΩ resistors) between Arduino Pin 11 and HC-05 RX to protect the module.

### Arduino Sketch (`BElectronics_HC05.ino`)

```cpp
#include <SoftwareSerial.h>

// SoftwareSerial: RX = Pin 10 (from HC-05 TX), TX = Pin 11 (to HC-05 RX)
SoftwareSerial btSerial(10, 11);

const int LED_PIN = 13;
const int SENSOR_PIN = A0;

unsigned long lastTelemetryTime = 0;
const unsigned long TELEMETRY_INTERVAL = 250; // Send telemetry every 250ms

void setup() {
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  // Initialize hardware serial for debugging and Bluetooth serial at 9600 baud
  Serial.begin(115200);
  btSerial.begin(9600);
  
  Serial.println(F("BElectronicsVP HC-05 Ready!"));
}

void loop() {
  // 1. Process incoming commands from Phone
  while (btSerial.available() > 0) {
    char c = btSerial.read();
    
    // Command '1' = Turn LED ON, '0' = Turn LED OFF
    if (c == '1') {
      digitalWrite(LED_PIN, HIGH);
      Serial.println(F("LED Turned ON"));
    } else if (c == '0') {
      digitalWrite(LED_PIN, LOW);
      Serial.println(F("LED Turned OFF"));
    }
  }

  // 2. Transmit non-blocking telemetry to Phone
  unsigned long currentMillis = millis();
  if (currentMillis - lastTelemetryTime >= TELEMETRY_INTERVAL) {
    lastTelemetryTime = currentMillis;

    int sensorVal = analogRead(SENSOR_PIN);
    
    // Format: *<WIDGET_ID><VALUE>*
    // Updates Gauge or Slider 'A'
    btSerial.print("*A");
    btSerial.print(sensorVal);
    btSerial.print("*");
    
    // Updates Text Display 'T' with voltage string
    float voltage = (sensorVal * 5.0) / 1023.0;
    btSerial.print("*T");
    btSerial.print(voltage, 2);
    btSerial.print(" V*");
  }
}
```

---

## Example 2: ESP32 Bluetooth Classic (Native SPP)

The ESP32 includes built-in Bluetooth Classic. No external modules are needed.

### ESP32 Sketch (`BElectronics_ESP32_Classic.ino`)

```cpp
#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run make menuconfig to enable it
#endif

BluetoothSerial SerialBT;

const int LED_PIN = 2; // Built-in LED on most ESP32 Dev Boards
unsigned long lastTelemetryTime = 0;
const unsigned long TELEMETRY_INTERVAL = 200;

// Buffer for parsing *IDvalue* slider commands
String inputBuffer = "";
bool parsingCommand = false;

void setup() {
  pinMode(LED_PIN, OUTPUT);
  Serial.begin(115200);

  // Advertised Bluetooth Device Name
  SerialBT.begin("BElectronics-ESP32");
  Serial.println(F("ESP32 Bluetooth Classic Started! Pair with 'BElectronics-ESP32'."));
}

void loop() {
  // 1. Receive incoming commands from BElectronicsVP
  while (SerialBT.available()) {
    char c = SerialBT.read();
    
    // Direct button commands: '1' or '0'
    if (c == '1') {
      digitalWrite(LED_PIN, HIGH);
    } else if (c == '0') {
      digitalWrite(LED_PIN, LOW);
    }
    
    // Delimited slider / parameter commands: *S125*
    if (c == '*') {
      if (parsingCommand) {
        // End of packet, process buffer
        if (inputBuffer.startsWith("S")) {
          int sliderValue = inputBuffer.substring(1).toInt();
          Serial.printf("Slider S set to: %d\n", sliderValue);
        }
        inputBuffer = "";
        parsingCommand = false;
      } else {
        // Start of packet
        parsingCommand = true;
        inputBuffer = "";
      }
    } else if (parsingCommand) {
      inputBuffer += c;
    }
  }

  // 2. Transmit telemetry to BElectronicsVP
  unsigned long currentMillis = millis();
  if (currentMillis - lastTelemetryTime >= TELEMETRY_INTERVAL) {
    lastTelemetryTime = currentMillis;

    // Simulate sensor or read ADC
    int potentiometer = analogRead(34); // ADC1_CH6 (GPIO 34)

    // Update Dial Gauge 'A'
    SerialBT.print("*A");
    SerialBT.print(potentiometer);
    SerialBT.print("*");
  }
}
```

---

## Best Practices for High-Performance Telemetry

1. **Never use `delay()` in `loop()`**: `delay()` halts serial reading and causes packet buffer overflows. Always use `millis()` timers.
2. **Rate Limit Transmissions**: Updating dials faster than 20–50 Hz (every 20–50 ms) wastes phone CPU and Bluetooth bandwidth. An update rate of **5 Hz to 20 Hz (50 ms – 200 ms)** is optimal for human perception.
3. **Keep Command IDs Short**: Use single characters (`A`, `B`, `C`) for maximum throughput.
