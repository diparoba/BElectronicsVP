# Spec Delta: Application Branding & Project Information

## Purpose

Provides official application visual identity, customized launcher icons, and an in-app project information dialog with links to the open-source repository and maintainer credits.

## ADDED Requirements

### Requirement: Dedicated Application Iconography
The system SHALL provide customized adaptive and standard launcher icons distinguishing the app on the user's home screen and app drawer.

#### Scenario: Launcher display on home screen
- **WHEN** user views the installed application on their device launcher or app drawer
- **THEN** system displays the official BElectronicsVP icon with electric cyan and dark slate styling instead of generic system icons

### Requirement: In-App Project Information Dialog
The system SHALL provide an interactive project information interface accessible from top-level application navigation bars.

#### Scenario: User opens About dialog
- **WHEN** user taps the info icon on the dashboard list or active dashboard header
- **THEN** system opens the About dialog displaying version v1.0.0, open-source edition status, developer credits, and feature summary

### Requirement: Official Repository Navigation
The system SHALL provide an action within the project information dialog to launch the official GitHub repository in an external browser.

#### Scenario: User navigates to official GitHub repository
- **WHEN** user clicks the "View on GitHub" button inside the About dialog
- **THEN** system dispatches an ACTION_VIEW intent opening `https://github.com/diparoba/BElectronicsVP` in the system browser
