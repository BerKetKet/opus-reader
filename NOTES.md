# Opus Reader Engineering Notes

## Current State
- **Slice S00 Skeleton Complete**: Four Gradle modules (`:app`, `:design`, `:engine`, `:data`), version catalog, AppContainer DI, singleTask deep-linking, debug fixtures foundation. Tagged `s00-done`.
- **Slice S01 Design System Complete**: Design tokens (`docs/DESIGN_TOKENS.md`, `OpusColors`, `OpusTypography`, `OpusDimensions`, `OpusTheme`), Bookerly and Amazon Ember fonts, 12 core design components (`OpusTopBar`, `OpusSearchPill`, `OpusBottomNavBar`, `OpusListRow`, `OpusChevronRow`, `OpusCheckboxRow`, `OpusToggleRow`, `OpusChip`, `OpusCoverTile`, `OpusResumeCover`, `OpusSlider`, `OpusBottomSheet`), `ComponentGalleryScreen`, 4 behavioral unit tests in `:design`, 4 connected Compose UI tests on device. Tagged `s01-done`.

## What Is Verified
- **Architecture Guard:** `python3 tools/guard.py .` -> `GUARD OK`.
- **All Gates Passed:** `tools/gates.sh` passes completely with executed test summaries:
  - Unit tests: 7 tests total, 0 failed.
  - Device tests on physical Samsung Galaxy M32 (`32001776e8e82623`, 1080x2340 @ 420 dpi): 6 tests total, 0 failed.
- **Tokens and Components:** Authoritative design tokens sourced from `docs/kindle_res` and reference crops; real Bookerly serif and Amazon Ember fonts; sheet gestures (drag, peek, expanded, scrim dismiss, back handler) verified with zero semantic descendant merging bugs.

## What Is Broken
- None in S00 or S01 scope.

## Next Steps
- Slice S02 Data and Import:
  - Room database (entities for books, spine items, TOC entries, reading progress, annotations).
  - EPUB parser and importer (container.xml, content.opf, toc.ncx / Nav, cover extraction).
  - DataStore settings repository.
  - Test EPUB generator script creating valid, minimal, and malformed test EPUBs.
  - Behavioral tests proving data survives process restarts, and malformed EPUBs fail gracefully.
