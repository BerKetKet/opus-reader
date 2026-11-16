# Opus Reader — Project Summary & Engineering Difficulties

**Date:** November 15, 2026  
**Branch:** `real-ui`  
**Test Device:** Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi, density 2.625)  
**Status:** Halted per user instruction for architectural audit and status reporting.

---

## 1. Executive Summary & What We Are Doing

The objective of this phase is to eliminate all artificial fidelity shortcuts and achieve **genuine pixel-accurate Compose UI** across all canonical screens (`D01` through `D20`) of Opus Reader, matching the Kindle Android reference screenshots under dark theme.

### The Problem Inherited
An earlier fidelity pass produced scores of 0.00% to 2.0% difference by stacking ~70 cropped screenshot slices (`fixture_*.png`) inside `if (isFixture)` branches using `ContentScale.FillBounds` at hardcoded heights. The real UI path was never rendered or measured, and the slices contained unscrubbed personal account data.

### The "Real UI Only" Standard
Under `AGENTS.md` and `.agents/skills/natural-layout/SKILL.md`:
1. **Zero Fake Fixture Branches**: Completely removed all `isFixture` flags and branches from UI composables.
2. **Zero Pasted Slices**: Purged all cropped reference PNGs from `app/src/main/res/drawable/`.
3. **One Code Path**: Both production and fixture modes execute the exact same composables; fixture mode only injects data and scroll state via `OpusNavHost.kt`.
4. **Kindle Framework Alignment**: Rather than guessing dp/sp values, we extracted the actual decompiled layout XMLs, styles, and dimensions from the Kindle base APK (`/tmp/kindle_res/resources/res/values/dimens.xml` and `/tmp/kindle_res/resources/res/layout/`).
5. **Honest Automated Verification**:
   - `tools/guard.py`: Refuses to allow any raster slices, `FillBounds`, or fixture branches (`GUARD OK` required).
   - **Liveness Check**: Dynamic toggle (`fontScale = 1.25f` via `opus://fixture/<id>?liveness=1`); the captured image must change or the run fails.
   - **Scroll Test**: Top, mid, and end captures saved to `docs/fidelity/scroll/`.
   - `tools/pixdiff.py` & `tools/compare_typography.py`: Objective delta measurement against reference screenshots on physical hardware.

---

## 2. Progress Summary (Screens D01 – D20)

| ID | Screen Name | Reference Image | Status | Diff / Offset | Liveness / Scroll | Commit |
|:---|:---|:---|:---:|:---:|:---:|:---|
| **D01** | Library Grid | `01_library-grid.png` | **PASS** | 2.32% bad \| 0 px offset | Liveness: PASS \| Scroll: PASS | `e0af30f` |
| **D02** | Home | `02_home.png` | **Calibrated** | 34.8% bad \| 0 px offset | Liveness: PASS \| Scroll: PASS | `7c91ac2` |
| **D03** | More | `03_more.png` | **Calibrated** | 5.69% bad \| 3 px offset | Liveness: PASS \| Scroll: PASS | `d927515` |
| **D04** | Settings | `04_settings.png` | **Calibrated** | 8.16% bad \| 2 px offset | Liveness: PASS \| Scroll: PASS | `8435c20` |
| **D05** | Library Filter Sheet | `05_library-filter-sheet.png` | **Calibrated** | 24.22% bad \| 0 px offset | Liveness: PASS | `f0ee83f` |
| **D06** | Library View & Sort Sheet | `06_library-view-sort-sheet.png` | **Calibrated** | 14.64% bad \| 0 px offset | Liveness: PASS | `8a5015b` |
| **D07** | Library List | `07_library-list.png` | **Calibrated** | 16.49% bad \| 28 px offset | Liveness: PASS \| Scroll: PASS | `ed7450f` |
| **D08** | Collections | `08_collections.png` | **PASS** | 3.11% bad \| 0 px offset | Liveness: PASS \| Scroll: PASS | `155ad46` |
| **D09** | Notifications | `09_notifications.png` | **Calibrated** | 15.06% bad \| 0 px top offset | Liveness: PASS \| Scroll: PASS | `6d81076` |
| **D10** | Reader Chrome Visible | `10_reader-chrome-visible.png` | **PASS** | 4.30% bad \| 0 px offset | Liveness: PASS | `9cc5774` |
| **D11** | Contents (TOC) | `11_contents-toc.png` | **Calibrated** | 4.81% bad \| 6 px offset | Liveness: PASS | `c6a410b` |
| **D12** | In-Book Search | `12_reader-search.png` | **PASS** | 1.90% bad \| 0 px offset | Liveness: PASS | `c329939` |
| **D13** | Annotations | `13_annotations.png` | **Calibrated** | 4.67% bad \| 3 px offset | Liveness: PASS \| Scroll: PASS | `a97e3d8` |
| **D14** | Annotations Filter Sheet | `14_annotations-filter-sheet.png` | **Calibrated** | 11.05% bad \| 2 px offset | Liveness: PASS | `f493103` |
| **D15** | Annotations Sort Sheet | `15_annotations-sort-sheet.png` | **Calibrated** | 9.11% bad \| 0 px offset | Liveness: PASS | `430c40b` |
| **D16** | Annotations Share Sheet | `16_annotations-share-via-email.png` | **Calibrated** | 6.16% bad \| 3 px offset | Liveness: PASS | `b59348b` |
| **D17** | Display Settings: Font | `17_display-settings-font-tab.png` | **Calibrated** | 10.86% bad \| 0 px offset | Liveness: PASS | `e5d57fe` |
| **D18** | Display Settings: Layout | `18_display-settings-layout-tab.png` | **Calibrated** | 0–1 px offset across all 19 bands | Liveness: PASS | Working tree |
| **D19** | Display Settings: Themes | `19_display-settings-themes-tab.png` | **Pending** | Rebuild structured | — | — |
| **D20** | Display Settings: More | `20_display-settings-more-tab.png` | **Pending** | Rebuild structured | — | — |

---

## 3. Core Difficulties & Technical Roadblocks

### Difficulty 1: The "Pasted Pixels" Legacy & Architectural Deficit
- **Symptom:** In the previous code, screens that scored well were not real UI components. For instance, the Display Settings Sheet and Annotations Share Sheet simply displayed a cropped PNG of the Kindle app stretched via `FillBounds`.
- **Engineering Impact:** Tearing out the slices revealed that the underlying composables either did not exist or had completely incorrect layouts. We had to architect real composables from scratch for font carousels, custom segmented sliders, multi-line chips, and modal sheets.

### Difficulty 2: Sheet State Duality (Collapsed vs. Expanded)
- **Symptom:** In Kindle, the display settings sheet (`aa_menu_v2_bottom_sheet.xml`) uses Android's `BottomSheetBehavior` with two distinct states:
  - In **D17 (Font Tab)**, the sheet sits at collapsed peek height (`h=1154px`, top at `y=1186`).
  - In **D18–D20 (Layout, Themes, More)**, switching tabs causes the sheet to automatically expand to near full-screen (`h=2056px`, top at `y=284`).
- **Initial Confusion:** Opus Reader initially attempted to use a single fixed container (`OpusBottomSheetContainer`), which introduced non-existent title bars ("Display Settings"), "DONE" buttons, and an incorrect fixed height.
- **Resolution:** We refactored `DisplaySettingsSheet.kt` to dynamically adjust height based on `selectedTab` and matched the exact decompiled dimensions (`aa_menu_v2_tab_layout_height = 44dp`, `aa_menu_v2_bottom_sheet_handle_padding_top = 7dp`, `aa_menu_v2_brightness_seekbar_container_height = 100dp`).

### Difficulty 3: Reading Canvas Text Interference in Diff & Offset Metrics
- **Symptom:** `tools/pixdiff.py`'s `worst_offset` metric scans all horizontal foreground bands on the screen. Because the reader engine paginates and renders text differently from Kindle's proprietary native C++ engine, reader text lines above modal sheets (e.g., between `y=328` and `y=1155` in D17) produced apparent offsets of 100px to 800px.
- **Resolution:** 
  1. We updated `tools/pixdiff.py` so that band offset calculation skips any band that is entirely within a `--mask` region.
  2. Following user guidance (*"it is okay if adjusting those settings doesn't correspond to any change... it might help to display an empty page for you to not be confused by the diff"*), we set the background for the settings routes in `OpusNavHost.kt` to a clean empty page (`Color.Black`), isolating the UI interaction elements from reader engine text differences.

### Difficulty 4: Color Science & Elevation Scrim in Dark Mode
- **Symptom:** In Kindle's dark theme, modal sheet overlays do not use standard semi-transparent black. Android Material elevation and Kindle's dark theme apply an elevation surface tint that turns pure `#000000` black into a warm dark gray (`#292A2B` or `#1A1B1C`).
- **Impact:** Standard Compose `Color.Black.copy(alpha = 0.6f)` produced a flat dark wash that caused 15–20% pixel diffs across the entire background.
- **Resolution:** By sampling decompiled drawables and physical screen pixels, we derived the exact overlay formula (`Color(0xFF444648).copy(alpha = 0.60f)`), bringing scrim-induced diffs down by over 50%.

### Difficulty 5: Android Activity Deep Link Routing & Liveness Testing
- **Symptom:** During automated fidelity runs, `python3 tools/run_fidelity.py` sends `opus://fixture/<id>?liveness=1` to verify that the UI responds to runtime state changes. The test reported `FAIL (STATIC)` on several screens because `diff_pct` was 0.00%.
- **Root Cause:** In `AndroidManifest.xml`, `MainActivity` lacked `android:launchMode="singleTask"`. Sending `adb shell am start -a VIEW -d ...` to an already running activity caused Android to drop the new intent instead of routing it to `onNewIntent(intent)`.
- **Resolution:** Added `android:launchMode="singleTask"` to `.MainActivity` in `app/src/main/AndroidManifest.xml`, enabling reliable real-time intent delivery and passing liveness verification.

### Difficulty 6: Typography Cascading Offsets (The 1-2 sp Problem)
- **Symptom:** Small typography mismatches (e.g. using `14.sp` instead of `13.sp`, or missing font padding flags) caused multi-line paragraphs to expand by 4–8px per line. In vertical columns, this cascaded downwards, pushing buttons and dividers 30px to 70px below their reference positions.
- **Resolution:** 
  - Created `tools/compare_typography.py`, which uses sequence alignment (dynamic programming) to match reference and app foreground bands, outputting exact `Delta y` and `Delta h` metrics.
  - Defined the `ui-typography-calibrator` subagent to automate cross-referencing between decompiled `/tmp/kindle_res/resources/res/values/dimens.xml` and Compose files.

---

## 4. Current State & Immediate Next Steps

All subagents have been halted. The repository is in a clean, consistent state with `tools/guard.py` passing (`GUARD OK`).

### Remaining Tasks to Complete Milestone:
1. **Commit D18 (Layout Tab)**: Band alignment is already at 0–1 px across all 19 bands; run device capture and commit.
2. **Implement D19 (Themes Tab)**: Build the 5 theme presets (Standard, Compact, Large, Low Vision, Custom) matching decompiled `aa_menu_v2_theme_view.xml`.
3. **Implement D20 (More Tab)**: Build Reading Progress options, Visible Clock, Page Turn Animation, and Popular Highlights rows.
4. **Final Comprehensive Run**: Execute `python3 tools/run_fidelity.py --all`, compile full `docs/FIDELITY_REPORT.md`, verify tests via `./gradlew assembleDebug testDebugUnitTest lintDebug`, and push to `real-ui`.
