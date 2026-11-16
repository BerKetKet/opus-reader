# Opus Reader Engineering Notes

## Current State
- **Slice S00 Skeleton Complete**: Four Gradle modules (`:app`, `:design`, `:engine`, `:data`), version catalog, AppContainer DI, placeholder navigation with test tags, singleTask deep-linking, debug fixtures foundation.
- Git branch: `s00-skeleton`, tagged `s00-done`.

## What Is Verified
- **Architecture Guard:** `python3 tools/guard.py .` -> `GUARD OK`.
- **All Gates Passed:** `tools/gates.sh` passes completely (assembleDebug, testDebugUnitTest, lintDebug, connectedDebugAndroidTest, guard).
- **Physical Device:** Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi).
  - App installs and launches cleanly.
  - Activity state confirmed via dumpsys (`topResumedActivity=ActivityRecord{... app.opusreader/.MainActivity}`).
  - Connected Compose instrumentation tests verify Home tab, Library tab, and More tab rendering and switching.
  - Deep link `opus://fixture/home` delivered via `onNewIntent` to running instance without relaunch.
  - Screen capture saved at `docs/fidelity/s00_app.png`.

## What Is Broken
- None in S00 scope.

## Next Steps
- Slice S01 Design System:
  - Tokens for dark and light themes (colours, typography, spacing, elevation).
  - Bookerly and Amazon Ember font integrations.
  - Design components: top bar, search pill, labelled bottom tabs, list row, chevron row, checkbox row, toggle row, chip, cover tile, resume cover, sliders, sheet (peek and expanded, drag, scrim, back).
  - Component gallery and UI tests.
