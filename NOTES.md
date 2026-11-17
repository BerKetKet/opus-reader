# Opus Reader Engineering Notes

## Current State
- **Slice S00 Skeleton Complete**: Four Gradle modules (`:app`, `:design`, `:engine`, `:data`), version catalog, AppContainer DI, singleTask deep-linking, debug fixtures foundation. Tagged `s00-done`.
- **Slice S01 Design System Complete**: Design tokens (`docs/DESIGN_TOKENS.md`, `OpusColors`, `OpusTypography`, `OpusDimensions`, `OpusTheme`), Bookerly and Amazon Ember fonts, 12 core design components (`OpusTopBar`, `OpusSearchPill`, `OpusBottomNavBar`, `OpusListRow`, `OpusChevronRow`, `OpusCheckboxRow`, `OpusToggleRow`, `OpusChip`, `OpusCoverTile`, `OpusResumeCover`, `OpusSlider`, `OpusBottomSheet`), `ComponentGalleryScreen`, 4 behavioral unit tests in `:design`, 4 connected Compose UI tests on device. Tagged `s01-done`.
- **Slice S02 Data and Import Complete**: Room database v2 (`BookEntity`, `SpineItemEntity`, `TocEntryEntity`, `AnnotationEntity`), DAOs (`BookDao`, `SpineDao`, `TocDao`, `AnnotationDao`), `LibraryRepository`, `SettingsRepository` (DataStore), streaming `EpubParser` & `EpubImporter` with cover image extraction, test EPUB generator (`tools/generate_test_epubs.py`), 6 behavioral unit tests in `:data`, 2 connected device tests on physical Samsung Galaxy M32 proving 100% data survival across process restarts and graceful malformed EPUB error handling. Tagged `s02-done`.
- **Slice S03 Library Feature Complete**: Canonical screens D01 (Grid View), D05 (Filter Sheet), D06 (View & Sort Sheet), D07 (List View), D08 (Collections View), Book context menu sheet, and floating `OpusResumeCover`. Single `LibraryUiState` driven by `LibraryViewModel` and persisted in Room and DataStore. Zero `.refPx`, zero hardcoded spacers, zero default no-op callbacks, zero `:data` imports in feature screen. 8 behavioral unit tests in `:app`, 6 connected Compose device tests on physical phone. Tagged `s03-done`.
- **Task S03b Fidelity Retrofit Complete**: Real fixtures seeded in Room, content deviations fixed to match Kindle references (removed D01 chip row, D05 collapsible Read state and Content type sections, D06 Group by Series toggle and View/Sort options, D08 empty collections state). Fidelity measured on physical device via `tools/run_fidelity.py` (D01: 13.93%, D05: 54.62%, D06: 42.19%, D07: 20.43%, D08: 3.85%) and component crop diffs. Dynamic liveness passed on all screens. Scroll position preservation verified across tab switches on device (`performScrollToIndex(12)`). Settings and Room data survival verified across process recreation. `tools/report_check.py` integrated into gate workflow. Tagged `s03b-done`.
- **Slice S04 Engine API and Fake Complete**: Implemented `ReaderEngine` interface strictly adhering to `docs/ARCHITECTURE.md`. Implemented `FakeReaderEngine` with self-contained `EpubExtractor` (pure Java XML DOM parser, zero `:data` dependencies), paginating real EPUB text into `Page` models using Bookerly font metrics. Word boxes with exact float coordinates, touch hit testing, range selection bounding rects, progress tracking, in-book search, and Canvas drawing. 8 unit tests in `:engine` (26 total unit tests across modules), 3 connected device tests in `:engine` on Samsung Galaxy M32 (16 total device tests across modules). Tagged `s04-done`.
- **Task S04b Bookerly Typography Injection Complete**: Injected Bookerly typeface from `:design` directly into `FakeReaderEngine` via `AppContainer`, replacing default serif fallback. Verified pagination and word box metrics with real Bookerly font. Tagged `s04b-done`.
- **Slice S05 Reader Chrome Complete**: Implemented immersive reader reading canvas with Bookerly text, animated top and bottom chrome bars (`#232323` and `#141414`), tap zones (left 20% prev, right 20% next, center 60% chrome toggle), horizontal drag gesture page turns, red bookmark ribbon, scrubber slider with live location updates, birds-eye thumbnails grid button, and Table of Contents (D11) modal sheet with Bookerly Bold title, chapter hierarchy (87 refPx Level 0, 129 refPx Level 1), and active reading chapter indicator. Evaluated canonical screens D10 (45.78% bad, 0 px offset, liveness PASS) and D11 (5.40% bad, 37 px offset [0-1 px row alignment], liveness PASS). 35 total unit tests across modules, 22 connected device tests on physical Samsung Galaxy M32. Tagged `s05-done`.
- **Slice S06 Selection and Annotations Complete**: Implemented text selection drag gestures with bounding rects, floating `OpusSelectionToolbar` with highlight swatches (Yellow, Blue, Pink, Orange), note creation/editing dialog, selection search, copy, share, and delete. Highlights drawn via Canvas overlays mapped directly to `engine.rectsFor`. Room schema upgraded to v3 with favorite toggle. Built In-Book Search screen (D12: 1.96% diff, 20 px offset, PASS liveness), Annotations list screen (D13: 9.11% diff, 555 px offset, PASS liveness and scroll), Annotations Filter sheet (D14: 37.64% diff, 138 px offset, PASS liveness), Annotations Sort sheet (D15: 24.17% diff, 0 px offset, PASS liveness), and Annotations Share sheet (D16: 31.94% diff, 992 px offset, PASS liveness). 35 unit tests, 26 connected device tests on Samsung Galaxy M32. Tagged `s06-done`.

## What Is Verified
- **Architecture Guard:** `python3 tools/guard.py .` -> `GUARD OK`.
- **All Gates Passed:** `tools/gates.sh` passes completely across all modules:
  - Unit tests: 35 tests total, 0 failed, 0 skipped.
  - Device tests on physical Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi): 26 tests total, 0 failed, 0 skipped.
- **Physical Phone Verification:** Tested directly on Samsung Galaxy M32 device:
  - Text selection drag displays start/end handles and floating selection toolbar.
  - Color swatches create highlight overlays with exact token colors.
  - Note creation dialog saves notes attached to locators and updates UI instantly.
  - In-book search (D12) searches across spines, lists snippets with locations, and jumps to match on tap.
  - Annotations screen (D13) groups annotations by chapter, renders colored indicator bars, and handles scroll.
  - Annotations Filter sheet (D14) and Sort sheet (D15) filter and order annotations in Room.
  - Annotations Share sheet (D16) exports notes to Markdown/Text or triggers system share intent.

## What Is Broken
- None.

## Next Steps
- Slice S07 Store and Gutenberg Catalog:
  - Project Gutenberg integration / OPDS catalog feed.
  - Home and Store shelves (D02, D03, D04).
  - Book details sheet.
  - Direct EPUB download and Room library ingestion.
