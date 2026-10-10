# Spec Delta: Panel Management

## MODIFIED Requirements

### Requirement: Dashboard Lifecycle Management
The system SHALL provide full management for dashboards including creating with selectable orientation, naming, cloning, and deleting panels.

#### Scenario: Create new named panel
- **WHEN** user taps the add dashboard button and enters a title
- **THEN** system initializes an empty dashboard with the specified title and displays it in the dashboard list

#### Scenario: Create new named panel with orientation
- **WHEN** user taps the add dashboard button and enters a title and selects an orientation (Horizontal Landscape default, or Vertical Portrait)
- **THEN** system initializes an empty dashboard with the specified title, orientation, and default grid dimensions, displaying it in the dashboard list

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
The system SHALL render responsive dashboards composed of strict 1:1 square cells dynamically fitting the viewport aspect ratio, locking physical screen orientation to match panel configuration.

#### Scenario: Display active panel widgets
- **WHEN** user opens an existing control panel
- **THEN** system renders all configured widgets mapped to whole 1:1 square grid cells without stretching or distortion

#### Scenario: Rotate screen orientation
- **WHEN** user changes device orientation between portrait and landscape
- **THEN** system dynamically recomputes canvas scaling maintaining widget aspect ratios and touch zones

#### Scenario: Lock screen orientation to panel orientation
- **WHEN** user navigates into a dashboard configured as Horizontal (Landscape) or Vertical (Portrait)
- **THEN** system locks the Android activity orientation to the panel's orientation and dynamically scales column or row counts to fill the device screen

#### Scenario: Restore orientation on exit
- **WHEN** user navigates back from a dashboard to the dashboard list
- **THEN** system restores the Android activity orientation to follow standard device sensor rotation

### Requirement: Visual Panel Editor
The system SHALL provide an interactive visual panel editor supporting widget catalog addition via floating action button, 1:1 cell movement and corner drag resizing, and dedicated widget configuration sheets.

#### Scenario: Add and configure button widget
- **WHEN** user drags a new button into the panel and sets its label to "LED" and transmit char to "a"
- **THEN** system adds button to panel model and binds touch events to serial command transmission

#### Scenario: Open widget catalog via floating action button
- **WHEN** user toggles Edit Mode and clicks the floating action button (+)
- **THEN** system opens a widget catalog bottom sheet displaying available controls (Button, Slider, Switch) and telemetry widgets (Gauge, Display, Graph)

#### Scenario: Add widget from catalog
- **WHEN** user selects a widget type from the catalog
- **THEN** system instantiates the widget on the canvas at the first available 1:1 grid coordinate and opens its property editor

#### Scenario: Resize widget using corner drag handle
- **WHEN** user drags the bottom-right corner handle of a widget in Edit Mode
- **THEN** system resizes the widget width and height snapping in discrete 1:1 square cell increments

#### Scenario: Configure momentary button with press and release commands
- **WHEN** user configures a button widget with press command "F", release command "S", and momentary mode
- **THEN** system transmits "F" on touch down and "S" on touch up during live operation

#### Scenario: Configure slider with range and custom prefix
- **WHEN** user configures a slider with min 0, max 9, prefix "V: ", and suffix "\n"
- **THEN** system limits slider values from 0 to 9 and transmits formatted strings such as "V: 5\n" during live interaction

#### Scenario: Change grid density
- **WHEN** user opens grid settings and selects a new density preset (Coarse, Standard, Fine, or Custom)
- **THEN** system recalculates the square cell size and redraws the canvas grid overlay without displacing widgets beyond screen bounds
