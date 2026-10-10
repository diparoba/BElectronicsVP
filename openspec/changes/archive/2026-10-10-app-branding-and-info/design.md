# Design: Application Branding & Project Information

## Context

See `proposal.md` for problem motivation and background.

Currently, `:app` lacks dedicated Android resource folders (`res/drawable`, `res/mipmap-*`, `res/values`). The application manifest directly points to `@android:drawable/stat_sys_data_bluetooth`, leading to generic system iconography. Furthermore, existing screens lack a designated entry point for inspecting project metadata, developer attribution, and open-source links.

## Goals / Non-Goals

**Goals:**
* Create adaptive vector icons (`ic_launcher.xml` and `ic_launcher_round.xml`) with clean dark slate background and electric cyan glyphs.
* Provide an interactive Compose `AboutDialog` component displaying version `v1.0.0`, open-source credits, and technical capabilities.
* Enable one-tap navigation to the official GitHub repository (`https://github.com/diparoba/BElectronicsVP`) via Android's `ACTION_VIEW` browser intent.
* Wire the Info action button into `DashboardListScreen` and `DashboardDetailScreen` TopAppBars.

**Non-Goals:**
* Introducing in-app WebViews or third-party web browsing libraries.
* Altering the application ID (`com.belectronicsvp`) or Bluetooth engine behaviors.
* Adding proprietary tracking, analytics, or remote telemetry.

## Decisions

### Decision 1: Vector XML Adaptive Icons vs Raster PNGs
* **Decision**: Author the launcher icon using Android Vector Drawables (`res/drawable/ic_launcher_foreground.xml` and `ic_launcher_background.xml`) coupled with `res/mipmap-anydpi-v26/ic_launcher.xml`.
* **Rationale**: Vector drawables provide pixel-perfect scaling across all device densities (mdpi through xxxhdpi) while keeping the APK lightweight.
* **Alternatives Considered**: Multi-resolution PNGs were rejected due to increased asset footprint and maintenance burden.

### Decision 2: Compose `AlertDialog` vs Dedicated About Activity
* **Decision**: Implement the About interface as a Compose `AlertDialog` component (`AboutDialog.kt`).
* **Rationale**: Keeps navigation lightweight, maintains dark theme styling, loads instantaneously, and requires no AndroidManifest activity declarations.
* **Alternatives Considered**: A separate `AboutActivity` or navigation destination would introduce unnecessary routing friction.

### Decision 3: Safe `ACTION_VIEW` Browser Intent
* **Decision**: Dispatch the GitHub URL using `Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))` wrapped in `runCatching`.
* **Rationale**: Respects the user's preferred browser while protecting against missing activity handler exceptions.

## Risks / Trade-offs

* **[Risk]** Device has no web browser installed to handle `ACTION_VIEW`.  
  **Mitigation**: Wrap the intent call in `runCatching` and present a Toast explaining the fallback URL.
* **[Risk]** Adaptive icons on older Android versions (API < 26).  
  **Mitigation**: Provide standard fallback drawable resources ensuring backward compatibility down to Android 8.0 (API 26 minSdk).
