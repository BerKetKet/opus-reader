# S00 Plan: Skeleton

## Goal
Build the initial multi-module Gradle project skeleton, version catalog, AppContainer, NavHost with placeholder tabs, debug source set, verify `tools/gates.sh` passes, app launches on the phone, and guard prints `GUARD OK`.

## Modules
- `:design`: Android library for tokens, fonts, and shared UI components (isolated from :data).
- `:engine`: Android library for ReaderEngine API and page models (isolated from :data and UI).
- `:data`: Android library for Room database, repositories, DataStore, and catalog.
- `:app`: Android application module depending on `:design`, `:engine`, and `:data`.

## Data Model & DI
- `AppContainer` interface and implementation providing app-level dependencies.
- Empty Room database and initial repository placeholders.

## Navigation & UI
- `OpusNavHost` with placeholder screens for Home, Library, More.
- Shell navigation structure.

## Debug Fixtures
- Debug source set with deep link support `opus://fixture/<id>` and singleTask MainActivity.

## Testing & Verification
- Unit test in each module.
- Instrumentation test verifying activity launch.
- `tools/gates.sh` full execution.
- Device run on Samsung Galaxy M32 (`32001776e8e82623`).
- `python3 tools/guard.py .` -> `GUARD OK`.
