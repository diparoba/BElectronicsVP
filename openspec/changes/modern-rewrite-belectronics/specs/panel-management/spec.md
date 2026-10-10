# Spec Delta: Panel Management

## Purpose

Enables users to design, render, customize, and export interactive telemetry dashboards composed of buttons, sliders, gauges, and graphs.

## ADDED Requirements

### Requirement: Dashboard Lifecycle Management
The system SHALL provide full management for dashboards including creating, naming, cloning, and deleting panels.

#### Scenario: Create new named panel
- **WHEN** user taps the add dashboard button and enters a title
- **THEN** system initializes an empty dashboard with the specified title and displays it in the dashboard list

#### Scenario: Rename existing dashboard
- **WHEN** user selects rename on a dashboard card and saves a new title
- **THEN** system updates the title in local storage and refreshes the dashboard list

#### Scenario: Delete dashboard with confirmation
- **WHEN** user requests dashboard deletion and confirms the alert dialog
- **THEN** system deletes the dashboard from storage and removes it from the screen

#### Scenario: Duplicate dashboard
- **WHEN** user selects duplicate on an existing dashboard
- **THEN** system creates an exact copy of widgets and layout with a new ID and appended title

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

### Requirement: Native Android Share Sheet and File Import
The system SHALL export panel configurations into JSON files, invoke the native Android share sheet for one-tap sharing, and import valid panels.

#### Scenario: Share panel via Android Share Sheet
- **WHEN** user taps share dashboard
- **THEN** system exports the panel JSON to cache storage and launches Android ACTION_SEND share sheet for messaging and drive apps

#### Scenario: Import external panel file
- **WHEN** user selects an external `.json` or `.bep` file from device storage
- **THEN** system parses and validates schema, adds the panel to user storage, and shows success notification
