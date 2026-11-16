# Opus Reader Engineering Notes

## Current State
- **Slice S00 Skeleton Complete**: Four Gradle modules (`:app`, `:design`, `:engine`, `:data`), version catalog, AppContainer DI, singleTask deep-linking, debug fixtures foundation. Tagged `s00-done`.
- **Slice S01 Design System Complete**: Design tokens (`docs/DESIGN_TOKENS.md`, `OpusColors`, `OpusTypography`, `OpusDimensions`, `OpusTheme`), Bookerly and Amazon Ember fonts, 12 core design components (`OpusTopBar`, `OpusSearchPill`, `OpusBottomNavBar`, `OpusListRow`, `OpusChevronRow`, `OpusCheckboxRow`, `OpusToggleRow`, `OpusChip`, `OpusCoverTile`, `OpusResumeCover`, `OpusSlider`, `OpusBottomSheet`), `ComponentGalleryScreen`, 4 behavioral unit tests in `:design`, 4 connected Compose UI tests on device. Tagged `s01-done`.
- **Slice S02 Data and Import Complete**: Room database v2 (`BookEntity`, `SpineItemEntity`, `TocEntryEntity`, `AnnotationEntity`), DAOs (`BookDao`, `SpineDao`, `TocDao`, `AnnotationDao`), `LibraryRepository`, `SettingsRepository` (DataStore), streaming `EpubParser` & `EpubImporter` with cover image extraction, test EPUB generator (`tools/generate_test_epubs.py`), 6 behavioral unit tests in `:data`, 2 connected device tests on physical Samsung Galaxy M32 proving 100% data survival across process restarts and graceful malformed EPUB error handling. Tagged `s02-done`.
- **Slice S03 Library Feature Complete**: Canonical screens D01 (Grid View), D05 (Filter Sheet), D06 (View & Sort Sheet), D07 (List View), D08 (Collections View), Book context menu sheet, and floating `OpusResumeCover`. Single `LibraryUiState` driven by `LibraryViewModel` and persisted in Room and DataStore. Zero `.refPx`, zero hardcoded spacers, zero default no-op callbacks, zero `:data` imports in feature screen. 6 behavioral unit tests in `:app`, 5 connected Compose device tests on physical phone. Tagged `s03-done`.

## What Is Verified
- **Architecture Guard:** `python3 tools/guard.py .` -> `GUARD OK`.
- **All Gates Passed:** `tools/gates.sh` passes completely across all modules:
  - Unit tests: 17 tests total, 0 failed, 0 skipped.
  - Device tests on physical Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi): 11 tests total, 0 failed, 0 skipped.
- **Physical Phone Verification:** Tested directly on Samsung Galaxy M32 device:
  - Live filter chips toggle content instantly.
  - Search input filters books by title/author with clear action.
  - View & Sort sheet switches between Grid, List, and Collections views and persists preference.
  - Bottom sheets dismiss cleanly via scrim, drag, and system back without merging child semantics.
  - Floating resume cover opens recently read book.
  - Collections creation and book tagging work and persist.

## What Is Broken
- None across S00, S01, S02, and S03.

## Next Steps
- Slice S04 Engine API and Fake:
  - `:engine` ReaderEngine API.
  - Page model, pagination in Bookerly font, hit testing, and contract tests.
