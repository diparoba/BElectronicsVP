# Proposal: Dashboard 1:1 Grid Engine, Orientation Lock & Widget Customization

## Why

In the current implementation, dashboard grid coordinates suffer from a density-scaling bug that causes widgets to bunch up and drift during drag interactions. Furthermore, the dashboard lacks an orientation enforcement mechanism, causing grids to distort when rotated or viewed on devices with differing aspect ratios. Users also cannot discover available widgets, resize widgets interactively on the canvas, customize grid density, or configure vital widget properties—such as separate press and release commands for buttons or prefix and value ranges for sliders, as originally offered by Keuwlsoft Bluetooth Electronics.

Re-architecting the layout engine around strict 1:1 square cells with dynamic aspect-ratio fitting, enforcing physical orientation lock per dashboard, and providing an intuitive widget catalog and deep configuration sheets will restore the exact workflow, responsiveness, and versatility expected from a modern Bluetooth telemetry suite.

## What Changes

* **Strict 1:1 Square Grid Coordinates**: Refactor the grid engine so every grid cell is mathematically square (1:1 aspect ratio) regardless of screen resolution, eliminating visual squashing or stretching.
* **Dynamic Viewport Aspect Fitting**: The grid dynamically fits the physical device's viewport by keeping base density fixed on the primary axis (e.g. 8 rows in landscape, 8 columns in portrait) and calculating the perpendicular axis to fill the entire screen.
* **Dashboard Orientation Selection & Hardware Lock**: Allow selecting between Horizontal (Landscape, default) and Vertical (Portrait) when creating a dashboard, and enforce strict `ActivityInfo` screen orientation lock when opening the dashboard in both live and edit modes.
* **Corner Drag Resizing & Snapping**: Enable direct interactive resizing on the canvas via bottom-right corner drag handles that snap cleanly to whole 1:1 grid cells.
* **Floating Action Button (+) Widget Catalog**: Introduce an animated floating action button in Edit Mode that opens a modal bottom sheet displaying all available widgets (Buttons, Sliders, Switches, Gauges, Text Displays, Graphs).
* **Comprehensive Widget Configuration Sheets**:
  - **Button**: Configure label, momentary vs. toggle mode, press command (`ACTION_DOWN`), and release command (`ACTION_UP`).
  - **Slider**: Configure label, min/max range, command prefix (e.g. `V: ` or `*S`), command suffix (e.g. `\n` or `*`), and live output preview.
  - **Switch**: Configure label, ON command, OFF command, and initial state.
  - **Gauge / Text Display / Graph**: Configure label, telemetry channel character (`A`-`Z`), min/max ranges, and unit labels.
  - All widgets include dimension steppers (width/height in grid cells) and widget deletion.
* **Grid Density Selector**: In Edit Mode, allow users to choose between Coarse, Standard, Fine, or Custom cell density.

## Capabilities

### New Capabilities
*(None - existing panel management capability will be evolved.)*

### Modified Capabilities
- `panel-management`: Updates Dashboard Lifecycle Management to support orientation selection and grid density options; updates Dynamic Dashboard Grid Rendering to enforce strict 1:1 square cells, dynamic aspect ratio viewport fitting, and hardware orientation lock; and updates Visual Panel Editor to include corner drag handles, FAB-triggered widget catalog, and comprehensive widget property configuration sheets.

## Impact

* **Affected Modules**: `:core-model`, `:app`
* **Data Model**: `PanelModel` adds `orientation: PanelOrientation`, `gridDensity`, and serialization compatibility; `WidgetModel` defaults align with integer grid cell spans.
* **UI Components**:
  - `PanelCanvas.kt`: Density-corrected coordinate system, 1:1 square cell calculations, corner resize handles.
  - `DashboardDetailScreen.kt`: Activity orientation locking, Edit Mode FAB, Widget Catalog Bottom Sheet, Widget Configuration Dialogs, Grid Density Dialog.
  - `DashboardListScreen.kt`: Creation dialog updated with orientation picker (Landscape default).
* **Serial Protocol Compatibility**: Maintains full parity with Keuwlsoft ASCII protocol (`*<ID><VAL>*`) as well as raw custom command streams (e.g. `V: 5\n`).
