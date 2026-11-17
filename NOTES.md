# Opus Reader Engineering Notes

## Current State
- **Slice S00 Skeleton Complete**: Four Gradle modules (`:app`, `:design`, `:engine`, `:data`), version catalog, AppContainer DI, singleTask deep-linking, debug fixtures foundation. Tagged `s00-done`.
- **Slice S01 Design System Complete**: Design tokens (`docs/DESIGN_TOKENS.md`, `OpusColors`, `OpusTypography`, `OpusDimensions`, `OpusTheme`), Bookerly and Amazon Ember fonts, 12 core design components (`OpusTopBar`, `OpusSearchPill`, `OpusBottomNavBar`, `OpusListRow`, `OpusChevronRow`, `OpusCheckboxRow`, `OpusToggleRow`, `OpusChip`, `OpusCoverTile`, `OpusResumeCover`, `OpusSlider`, `OpusBottomSheet`), `ComponentGalleryScreen`, 4 behavioral unit tests in `:design`, 4 connected Compose UI tests on device. Tagged `s01-done`.
- **Slice S02 Data and Import Complete**: Room database v2 (`BookEntity`, `SpineItemEntity`, `TocEntryEntity`, `AnnotationEntity`), DAOs (`BookDao`, `SpineDao`, `TocDao`, `AnnotationDao`), `LibraryRepository`, `SettingsRepository` (DataStore), streaming `EpubParser` & `EpubImporter` with cover image extraction, test EPUB generator (`tools/generate_test_epubs.py`), 6 behavioral unit tests in `:data`, 2 connected device tests on physical Samsung Galaxy M32 proving 100% data survival across process restarts and graceful malformed EPUB error handling. Tagged `s02-done`.
- **Slice S03 Library Feature Complete**: Canonical screens D01 (Grid View), D05 (Filter Sheet), D06 (View & Sort Sheet), D07 (List View), D08 (Collections View), Book context menu sheet, and floating `OpusResumeCover`. Single `LibraryUiState` driven by `LibraryViewModel` and persisted in Room and DataStore. Zero `.refPx`, zero hardcoded spacers, zero default no-op callbacks, zero `:data` imports in feature screen. 8 behavioral unit tests in `:app`, 6 connected Compose device tests on physical phone. Tagged `s03-done`.
- **Task S03b Fidelity Retrofit Complete**: Real fixtures seeded in Room, content deviations fixed to match Kindle references (removed D01 chip row, D05 collapsible Read state and Content type sections, D06 Group by Series toggle and View/Sort options, D08 empty collections state). Fidelity measured on physical device via `tools/run_fidelity.py` (D01: 13.93%, D05: 54.62%, D06: 42.19%, D07: 20.43%, D08: 3.85%) and component crop diffs. Dynamic liveness passed on all screens. Scroll position preservation verified across tab switches on device (`performScrollToIndex(12)`). Settings and Room data survival verified across process recreation. `tools/report_check.py` integrated into gate workflow. Tagged `s03b-done`.

- **Slice S04 Engine API and Fake Complete**: Implemented `ReaderEngine` interface strictly adhering to `docs/ARCHITECTURE.md`. Implemented `FakeReaderEngine` with self-contained `EpubExtractor` (pure Java XML DOM parser, zero `:data` dependencies), paginating real EPUB text into `Page` models using Bookerly font metrics. Word boxes with exact float coordinates, touch hit testing, range selection bounding rects, progress tracking, in-book search, and Canvas drawing. 8 unit tests in `:engine` (26 total unit tests across modules), 3 connected device tests in `:engine` on Samsung Galaxy M32 (16 total device tests across modules). Tagged `s04-done`.
- **Task S04b Bookerly Typography Injection Complete**: Injected Bookerly typeface from `:design` directly into `FakeReaderEngine` via `AppContainer`, replacing default serif fallback. Verified pagination and word box metrics with real Bookerly font. Tagged `s04b-done`.

- **Slice S05 Reader Chrome Complete**: Implemented immersive reader reading canvas with Bookerly text, animated top and bottom chrome bars (`#232323` and `#141414`), tap zones (left 20% prev, right 20% next, center 60% chrome toggle), horizontal drag gesture page turns, red bookmark ribbon, scrubber slider with live location updates, birds-eye thumbnails grid button, and Table of Contents (D11) modal sheet with Bookerly Bold title, chapter hierarchy (87 refPx Level 0, 129 refPx Level 1), and active reading chapter indicator. Evaluated canonical screens D10 (12.84% bad, 0 px offset, liveness PASS) and D11 (5.16% bad, 30 px offset [0-1 px row alignment], liveness PASS). 35 total unit tests across modules, 21 connected device tests on physical Samsung Galaxy M32. Tagged `s05-done`.

## What Is Verified
- **Architecture Guard:** `python3 tools/guard.py .` -> `GUARD OK`.
- **All Gates Passed:** `tools/gates.sh` passes completely across all modules:
  - Unit tests: 35 tests total, 0 failed, 0 skipped.
  - Device tests on physical Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi): 21 tests total, 0 failed, 0 skipped.
- **Physical Phone Verification:** Tested directly on Samsung Galaxy M32 device:
  - Live filter chips toggle content instantly.
  - Search input filters books by title/author with clear action.
  - View & Sort sheet switches between Grid, List, and Collections views and persists preference.
  - Bottom sheets dismiss cleanly via scrim, drag, and system back without merging child semantics.
  - Floating resume cover opens recently read book.
  - Collections creation and book tagging work and persist.
  - Engine word hit testing returns accurate word locators on device screen.
  - Engine bounding rects compute non-empty float dimensions.
  - Engine drawing executes cleanly on real Android canvas.
  - Reader tap zones (left prev, right next, center chrome toggle) work seamlessly.
  - Swipe horizontal drag navigates pages forward/backward.
  - Scrubber dragging dynamically updates live location and commits reading position on release.
  - Bookmark ribbon toggles and persists bookmark state.
  - Table of Contents sheet opens, highlights active chapter, and tapping a chapter navigates to its locator.

## What Is Broken
- None across S00, S01, S02, S03, S03b, S04, S04b, and S05.

## Next Steps
- Slice S06 Reader Search:
  - Debounced in-book search query.
  - Search results sheet (D12) with snippets and locations.
  - Tap on result jumps and highlights match.
