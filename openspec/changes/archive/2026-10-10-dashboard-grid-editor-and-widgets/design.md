# Design: Dashboard 1:1 Grid Engine, Orientation Lock & Widget Customization

## Context

See `proposal.md` for motivation and background.

Currently, `PanelCanvas.kt` computes cell dimensions as `availableWidth / cols` and `availableHeight / rows`. Because columns and rows are independently set to fixed counts (12 and 8), cells become rectangular on different device aspect ratios, distorting controls. Furthermore, `initialX` and `initialY` are calculated in DP but fed into `IntOffset` which expects raw screen pixels, causing severe layout bunching on high-density devices. Finally, the application does not constrain screen orientation per dashboard, leading to layout scrambling when users rotate devices.

## Goals / Non-Goals

**Goals:**
* Implement strict 1:1 square cell rendering where $W_{\text{cell}} == H_{\text{cell}}$ in physical and DP space.
* Dynamically fit the viewport aspect ratio by fixing the base density along the primary axis (rows in landscape, columns in portrait) and adapting the secondary axis to fill the screen.
* Enforce Android Activity-level orientation locking matching the panel's configuration (`LANDSCAPE` or `PORTRAIT`), restoring sensor rotation upon screen exit.
* Provide interactive bottom-right corner drag handles on widgets in Edit Mode to resize width and height snapping to whole 1:1 grid cells.
* Provide an animated Floating Action Button (+) opening a Modal Bottom Sheet to browse and add widgets.
* Provide specialized configuration dialogs for all widget types, including dual press/release commands for buttons and customizable prefix/ranges for sliders.
* Enable grid density switching (Coarse, Standard, Fine, Custom).

**Non-Goals:**
* Free-form non-grid floating coordinates (all widgets strictly align to whole 1:1 square cells).
* Automated widget packing or auto-flow (widgets retain their explicit grid positions).

## Decisions

### Decision 1: Strict 1:1 Square Grid Math with Viewport Aspect Fitting
* **Decision**: 
  - For **Landscape**: Base density is fixed by row count $R$ (default 8). Square cell size $S = H_{\text{available}} / R$. Column count is dynamically calculated: $C = \lfloor W_{\text{available}} / S \rfloor$.
  - For **Portrait**: Base density is fixed by column count $C$ (default 8). Square cell size $S = W_{\text{available}} / C$. Row count is dynamically calculated: $R = \lfloor H_{\text{available}} / S \rfloor$.
  - All widget coordinates and dimensions $(X, Y, W, H)$ are integer cell units.
* **Rationale**: Guarantees that circular gauges, square buttons, and uniform controls never distort regardless of device aspect ratio (e.g., 16:9, 18:9, 20:9, 4:3).
* **Alternatives Considered**: Arbitrary independent $X$ and $Y$ scaling was rejected because it causes severe aspect ratio distortion.

### Decision 2: Activity Orientation Enforcement via Compose Lifecycle
* **Decision**: In `DashboardDetailScreen`, use a `DisposableEffect(panel?.orientation)` to set `activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` for landscape panels, or `SCREEN_ORIENTATION_PORTRAIT` for portrait panels. On disposal, reset to `SCREEN_ORIENTATION_UNSPECIFIED`.
* **Rationale**: Guarantees consistent viewport aspect ratio across both design mode and live operations.
* **Alternatives Considered**: Manifest-level static orientation locking was rejected because it would prevent having both portrait and landscape dashboards in the same app.

### Decision 3: Dual-Mode Resizing (Canvas Handle + Dialog Inputs)
* **Decision**: Provide interactive corner drag handles on the canvas in Edit Mode with live bounding box preview, alongside numeric steppers in the widget property sheets.
* **Rationale**: Direct manipulation is fast and intuitive on tablets and touchscreens, while numeric steppers allow precise fine-tuning.

### Decision 4: Material 3 Modal Bottom Sheet for Widget Catalog
* **Decision**: Trigger an expandable `ModalBottomSheet` from a Floating Action Button in Edit Mode.
* **Rationale**: Avoids cluttering the canvas while editing and accommodates future widget types cleanly.

### Decision 5: Backward-Compatible Data Model Migration
* **Decision**: Extend `PanelModel` with `orientation: PanelOrientation = PanelOrientation.LANDSCAPE` and `baseDensity: Int = 8`. Existing panel JSON files missing these fields will automatically deserialize to landscape standard density without breaking.
* **Rationale**: Maintains seamless compatibility with previously created and imported panels.

## Risks / Trade-offs

* **[Risk]** Existing dashboards created on older grid coordinates might have widgets positioned outside the 1:1 viewport boundary.  
  **Mitigation**: Clamp widget $(X, Y)$ and $(W, H)$ to $(0 \le X + W \le C)$ and $(0 \le Y + H \le R)$ during layout rendering and editing.
* **[Risk]** Rapid orientation switching could cause flickering if not cleaned up properly.  
  **Mitigation**: Safely guard activity casting with `(context as? Activity)` and restore original orientation in `onDispose`.
