# Tasks: Dashboard 1:1 Grid Engine, Orientation Lock & Widget Customization

## 1. Data Model & Orientation Lifecycle

- [x] 1.1 Update `PanelModel.kt` with `PanelOrientation` (LANDSCAPE, PORTRAIT) and base density, updating `PanelSerialization.kt` and verifying tests pass with `./gradlew :core-model:test`.
- [x] 1.2 Update `DashboardListScreen.kt` creation dialog to include orientation selection (Horizontal Landscape default, Vertical Portrait). Verify panel creation persistence in `PanelRepository`.
- [x] 1.3 Enforce Activity screen orientation lock in `DashboardDetailScreen.kt` via `DisposableEffect` (lock to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` or `SCREEN_ORIENTATION_PORTRAIT`, restore to `SCREEN_ORIENTATION_UNSPECIFIED` on exit). Verify orientation transitions.

## 2. Strict 1:1 Square Grid Engine & Corner Drag Resizing

- [x] 2.1 Refactor `PanelCanvas.kt` coordinate system to enforce strict 1:1 square cells ($S = H / R$ in landscape, $S = W / C$ in portrait) dynamically fitting the device viewport aspect ratio without stretching. Verify grid overlay drawing.
- [x] 2.2 Add interactive corner drag handles to widgets in Edit Mode to resize width and height in discrete 1:1 cell units with live snapping preview. Verify canvas drag and resize gestures.
- [x] 2.3 Add Grid Density selector dialog to `DashboardDetailScreen` for switching between Coarse, Standard, Fine, and Custom cell density. Verify grid density recomputation.

## 3. Widget Catalog & Polymorphic Configuration Sheets

- [x] 3.1 Implement Floating Action Button (+) and `ModalBottomSheet` in `DashboardDetailScreen` for browsing and adding widgets to the canvas. Verify new widget placement.
- [x] 3.2 Implement Button configuration dialog supporting label, momentary vs. toggle mode, press command, release command, cell dimensions, and deletion. Verify button command dispatch.
- [x] 3.3 Implement Slider configuration dialog supporting label, min/max values, custom prefix (e.g. `V: ` or `*S`), suffix (e.g. `\n` or `*`), live preview, cell dimensions, and deletion. Verify slider command dispatch.
- [x] 3.4 Implement Switch, Gauge, TextDisplay, and Graph configuration dialogs supporting telemetry channel IDs (`A`-`Z`), units, limits, and deletion. Verify telemetry binding and saving.
- [x] 3.5 Verify full application build and unit tests with `./gradlew test assembleDebug` and package APK with `./scripts/build-apk.sh`.

## Workflow follow-up

- Sync specs and archive change upon implementation completion.
