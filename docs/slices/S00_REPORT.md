# S00 Report: Skeleton

**Date:** November 16, 2026  
**Branch:** `s00-skeleton`  
**Tag:** `s00-done`  
**Target Device:** Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi)

---

## 1. Executive Summary & Verification

The S00 multi-module skeleton has been established with strict module boundary enforcement, Gradle version catalog, manual dependency injection via `AppContainer`, placeholder tab navigation in `OpusNavHost`, deep-linking with `launchMode="singleTask"`, and automated test coverage.

### Verified on Hardware:
- **Build & Assemble:** Clean compilation across all 4 modules (`:app`, `:design`, `:engine`, `:data`).
- **Unit Tests:** `testDebugUnitTest` executed and passed on all modules.
- **Lint Check:** `lintDebug` passed with 0 errors.
- **Instrumentation Tests:** `connectedDebugAndroidTest` ran and passed on physical device `32001776e8e82623` (`TrebleDroid with GApps - 13`).
- **App Launch Verified via Dumpsys:**
  ```
  topResumedActivity=ActivityRecord{b951a2a u0 app.opusreader/.MainActivity} t975}
  ResumedActivity: ActivityRecord{b951a2a u0 app.opusreader/.MainActivity} t975}
  ```
- **Deep Link Routing:** `opus://fixture/home` delivered via `onNewIntent` to running singleTask instance without relaunch.
- **Screen Capture:** Captured and verified at `docs/fidelity/s00_app.png`.
- **Architectural Guard:** `python3 tools/guard.py .` printed `GUARD OK`.

---

## 2. Gate Evidence (`tools/gates.sh` / `tools/test_summary.py`)

```
UNIT    1 tests  0 failed  0 skipped  app.opusreader.design.DesignTest
UNIT    1 tests  0 failed  0 skipped  app.opusreader.engine.LocatorTest
UNIT    1 tests  0 failed  0 skipped  app.opusreader.data.BookEntityTest
UNIT    1 tests  0 failed  0 skipped  app.opusreader.AppUnitTest
UNIT TOTAL: 4 tests, 0 failed, 0 skipped
DEVICE    2 tests  0 failed  0 skipped  app.opusreader.MainActivityTest
DEVICE TOTAL: 2 tests, 0 failed, 0 skipped
TEST SUMMARY OK
GUARD OK
GATES OK
```

---

## 3. Fidelity Numbers Per Screen

In S00, screens are skeleton/placeholder containers. Pixel fidelity matching begins in slice S01 (design system tokens/components) and continues per screen as features land. Baseline skeleton capture: `docs/fidelity/s00_app.png`.

---

## 4. Inferred vs Missing

### Inferred:
- Basic Material bottom navigation bar used as temporary skeleton shell until `:design` tokens and styled bottom tab bar are built in S01.
- Deep link format `opus://fixture/<id>` routed to `MainActivity` (singleTask) and handled by `DebugFixtureController`.

### Missing:
- Design tokens and components (`:design` components to be built in S01).
- Room entities, DAOs, and EPUB import logic (`:data` to be built in S02).
- Reader engine implementation (`:engine` fake engine in S04).
