# Serial Protocol Specification

## Purpose

Defines bidirectional message framing, parsing, and routing compatible with Keuwlsoft Bluetooth Electronics asterisk protocol.

## Requirements

### Requirement: Inbound Asterisk Frame Parsing
The system SHALL parse incoming byte streams into discrete command frames matching the asterisk-delimited structure `*<ID><VALUE>*`.

#### Scenario: Receive numeric value for widget
- **WHEN** incoming stream delivers `*A125*`
- **THEN** system dispatches value `125` to registered widget with identifier `A`

#### Scenario: Receive multi-channel color payload
- **WHEN** incoming stream delivers `*R255G120B0*`
- **THEN** system parses RGB channels and updates corresponding color indicator widget

#### Scenario: Malformed or fragmented packets
- **WHEN** incoming data arrives fragmented across multiple read chunks or has noisy bytes outside asterisks
- **THEN** system buffers valid fragments, discards garbage bytes, and emits only complete framed packets

### Requirement: Outbound Command Formatting
The system SHALL serialize widget actions into configured command strings before writing to the Bluetooth output stream.

#### Scenario: Button press and release
- **WHEN** user presses button configured with press command `1` and release command `0`
- **THEN** system transmits `1` upon touch down and `0` upon touch release to the microcontroller

#### Scenario: Slider value adjustment
- **WHEN** user drags a slider configured with prefix `*S` and suffix `*` to position 75
- **THEN** system transmits `*S75*` through the serial output stream with rate limiting to prevent buffer overflow
