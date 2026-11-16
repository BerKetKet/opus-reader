# Opus Reader Engineering Notes

## Current State
- **Slice S00 Skeleton Complete**: Four Gradle modules (`:app`, `:design`, `:engine`, `:data`), version catalog, AppContainer DI, singleTask deep-linking, debug fixtures foundation. Tagged `s00-done`.
- **Slice S01 Design System Complete**: Design tokens (`docs/DESIGN_TOKENS.md`, `OpusColors`, `OpusTypography`, `OpusDimensions`, `OpusTheme`), Bookerly and Amazon Ember fonts, 12 core design components (`OpusTopBar`, `OpusSearchPill`, `OpusBottomNavBar`, `OpusListRow`, `OpusChevronRow`, `OpusCheckboxRow`, `OpusToggleRow`, `OpusChip`, `OpusCoverTile`, `OpusResumeCover`, `OpusSlider`, `OpusBottomSheet`), `ComponentGalleryScreen`, 4 behavioral unit tests in `:design`, 4 connected Compose UI tests on device. Tagged `s01-done`.
- **Slice S02 Data and Import Complete**: Room database v2 (`BookEntity`, `SpineItemEntity`, `TocEntryEntity`, `AnnotationEntity`), DAOs (`BookDao`, `SpineDao`, `TocDao`, `AnnotationDao`), `LibraryRepository`, `SettingsRepository` (DataStore), streaming `EpubParser` & `EpubImporter` with cover image extraction, test EPUB generator (`tools/generate_test_epubs.py`), 6 behavioral unit tests in `:data`, 2 connected device tests on physical Samsung Galaxy M32 proving 100% data survival across process restarts and graceful malformed EPUB error handling. Tagged `s02-done`.

## What Is Verified
- **Architecture Guard:** `python3 tools/guard.py .` -> `GUARD OK`.
- **All Gates Passed:** `tools/gates.sh` passes completely with executed test summaries:
  - Unit tests: 12 tests total, 0 failed.
  - Device tests on physical Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi): 8 tests total, 0 failed.
- **Process Restart Survival:** Proved that closing the database instance and reopening a new connection against the same disk file preserves book metadata, spine items, TOC entries, annotations, and reading progress (`spineIndex`, `charOffset`, `progressPercent`).
- **Graceful Error Handling:** Proved that corrupt zips, missing container.xml, or missing OPFs return clean `Result.failure` without leaving broken files or records.

## What Is Broken
- None in S00, S01, or S02 scope.

## Next Steps
- Slice S03 Library:
  - Library feature screens (D01 Grid, D05 Filter Sheet, D06 View & Sort Sheet, D07 List, D08 Collections).
  - Built strictly with `:design` components (no refPx, no hardcoded spacers, no default no-op callbacks).
  - `LibraryViewModel` exposing single `LibraryUiState` and handling events.
  - Floating `OpusResumeCover` at bottom-left opening last-read book.
  - Long-press book context sheet (collection assignment, mark read/unread, delete).
  - Replace placeholder unit test in `:app` (`AppUnitTest.kt`) with behavioral ViewModel unit tests.
  - Connected UI tests for Library interactions.
