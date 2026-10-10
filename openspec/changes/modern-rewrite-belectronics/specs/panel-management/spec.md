# Spec Delta: Panel Management

## Purpose

Enables users to design, render, customize, and export interactive telemetry dashboards composed of buttons, sliders, gauges, and graphs.

## ADDED Requirements

### Requirement: Dynamic Dashboard Grid Rendering
The system SHALL render responsive dashboards composed of customizable control and display widgets in Jetpack Compose.

#### Scenario: Display active panel widgets
- **WHEN** user opens an existing control panel
- **THEN** system renders all configured widgets at their normalized coordinates with current state values

#### Scenario: Rotate screen orientation
- **WHEN** user changes device orientation between portrait and landscape
- **THEN** system dynamically recomputes canvas scaling maintaining widget aspect ratios and touch zones

### Requirement: Visual Panel Editor
The system SHALL provide a drag-and-drop panel designer allowing adding, moving, resizing, and configuring widgets.

#### Scenario: Add and configure button widget
- **WHEN** user drags a new button into the panel and sets its label to "LED" and transmit char to "a"
- **THEN** system adds button to panel model and binds touch events to serial command transmission

### Requirement: Open JSON Panel Import and Export
The system SHALL serialize panels into portable, human-readable JSON files and import valid panel files from device storage.

#### Scenario: Export panel to JSON
- **WHEN** user selects export panel from the menu
- **THEN** system produces a validated `.json` file containing panel metadata, widget list, and communication parameters

#### Scenario: Import shared panel
- **WHEN** user selects an external `.json` panel file
- **THEN** system validates schema, imports the panel into local database, and displays it in the panel list
