# Proposal: Application Branding & Project Information

## Why

When users build and install BElectronicsVP on their Android devices, the application must be immediately recognizable and distinctly branded. Currently, the app utilizes generic system icons and lacks an in-app "About / Project Information" interface. Users who still have the original 2017 Keuwlsoft *Bluetooth Electronics* installed cannot easily tell them apart in the launcher, and users have no direct way inside the app to inspect the project version, read about its reverse-engineered open-source heritage, or navigate to the official GitHub repository.

## What Changes

* **Custom Vector Launcher Iconography**: Create dedicated Android adaptive and legacy launcher icons (`ic_launcher`, `ic_launcher_round`, `ic_launcher_foreground`, `ic_launcher_background`) featuring an electric cyan and deep slate circuit/bluetooth badge.
* **In-App "About BElectronicsVP" Dialog**: Implement an interactive Compose dialog/bottom sheet accessible from the TopAppBar in both the dashboard list and active dashboard screens.
* **Official Repository Link & Attribution**: The About dialog will provide one-tap redirection to `https://github.com/diparoba/BElectronicsVP`, display maintainer attribution to Diego Robalino (`@diparoba`), acknowledge the original Keuwlsoft application, and state the MIT License.
* **Android Resources & String Externalization**: Define `res/values/strings.xml` and `res/values/colors.xml` containing official metadata, URLs, and labels.

## Capabilities

### New Capabilities
- `app-branding`: Defines requirements for visual application identity, custom launcher icons, and the in-app project information / repository attribution interface.

### Modified Capabilities
*(None - no behavioral requirements of existing bluetooth or protocol capabilities are altered.)*

## Impact

* **Impacted Module**: `:app`
* **New Resources**: Vector drawables, adaptive mipmap icons, and string resources under `app/src/main/res/`.
* **UI Components**: TopAppBar actions in `DashboardListScreen` and `DashboardDetailScreen`, new `AboutDialog.kt` composable.
* **Manifest**: Updated `android:icon` and `android:roundIcon` references in `app/src/main/AndroidManifest.xml`.
