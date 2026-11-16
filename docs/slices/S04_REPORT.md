# Slice S04 Report: Engine API and Fake

## Gates Output
```text
UNIT    4 tests  0 failed  0 skipped  app.opusreader.design.DesignTest
UNIT    7 tests  0 failed  0 skipped  app.opusreader.engine.FakeReaderEngineTest
UNIT    1 tests  0 failed  0 skipped  app.opusreader.engine.LocatorTest
UNIT    6 tests  0 failed  0 skipped  app.opusreader.data.BookEntityTest
UNIT    8 tests  0 failed  0 skipped  app.opusreader.AppUnitTest
UNIT TOTAL: 26 tests, 0 failed, 0 skipped
DEVICE    4 tests  0 failed  0 skipped  app.opusreader.design.ComponentGalleryTest
DEVICE    3 tests  0 failed  0 skipped  app.opusreader.engine.FakeReaderEngineDeviceTest
DEVICE    3 tests  0 failed  0 skipped  app.opusreader.data.DatabaseRestartTest
DEVICE    6 tests  0 failed  0 skipped  app.opusreader.LibraryScreenTest
DEVICE TOTAL: 16 tests, 0 failed, 0 skipped
TEST SUMMARY OK
GUARD OK
GATES OK
```

## Executed Test Details
- **Unit Tests (26 executed across modules, 0 failed):**
  - `:design`: 4 tests for typography scales (Bookerly & Ember), themes, and touch targets.
  - `:engine`: 8 tests:
    - `LocatorTest`: Verifies `Locator(spineIndex, charOffset)` structural equality and immutability.
    - `FakeReaderEngineTest`: 7 contract tests verifying:
      1. `open_validEpub_returnsAccurateBookInfoAndToc`: Extracts title ("Pride and Prejudice"), author ("Jane Austen"), 2 spine items, and TOC entries.
      2. `pageNavigation_nextAndPrevious_traversesBookSeamlessly`: Forward/backward pagination across spine boundaries and boundary edge null returns.
      3. `locatorPersistenceAndRestoration_survivesEngineRecreation`: Turns to page 2, records `page.start` locator, recreates engine instance, calls `pageAt(locator)`, and asserts identical page start/end bounds.
      4. `progress_calculatesCumulativePosition`: Verifies cumulative progress increases monotonically across spines.
      5. `search_findsOccurrencesAcrossSpines`: Searches for term ("truth"), returning search hits with accurate spine index and snippet text.
      6. `rectsFor_returnsBoundingBoxesForLocatorRange`: Computes word bounding boxes covering a multi-word locator range.
      7. `fallbackHandling_gracefullyPaginatesMissingOrNonExistentFile`: Gracefully provides fallback sample text without crashing if EPUB is missing or corrupt.
  - `:data`: 6 tests for EPUB parsing, metadata, TOC, corrupt archives, and annotations.
  - `:app`: 8 tests for ViewModel search, filters, sorting, collections, sheets, and process recreation survival.
- **Connected Device Tests (16 executed on physical Samsung Galaxy M32, 0 failed):**
  - `:design`: 4 tests for gallery components on physical screen.
  - `:engine`: 3 tests:
    - `hitTest_returnsAccurateWordLocatorOnDevice`: Tests touch coordinate hit testing inside word bounding box and proximity detection.
    - `rectsFor_returnsExactBoundingBoxesOnDevice`: Verifies valid non-empty float dimensions on device.
    - `draw_rendersToRealCanvasWithoutError`: Verifies rendering to a real Android `Canvas` / `Bitmap` without exceptions.
  - `:data`: 3 tests for Room and DataStore persistence across process restart.
  - `:app`: 6 tests for library UI, filtering, search, sheets, and scroll position preservation across tab switches.

## Implemented Architecture
- `ReaderEngine` interface strictly adhering to `docs/ARCHITECTURE.md`.
- Page model: `Locator(spineIndex, charOffset)`, `LocatorRange`, `WordBox`, `LinkBox`, `Page`, `BookSource`, `ReaderSettings`, `BookInfo`, `Progress`, `PageTheme`, `SearchHit`, `TocEntry`.
- `EpubExtractor`: Self-contained XML DOM parser and zip extractor inside `:engine` ensuring `:engine` has zero dependencies on `:data` or `:app`.
- `FakeReaderEngine`: Paginates EPUB text using Bookerly metrics, lines, and word boxes; supports word-level hit testing, progress tracking, range rect queries, search, and drawing.

## Fidelity
NOT RUN: no canonical screen in this slice

## Inferred vs Missing
- **Inferred:** Default phone viewport set to 1080x2202 px with 75px horizontal and 90px vertical margins (customizable via `setViewport`).
- **Missing:** Visual reader screen (Slice S05 will connect `ReaderEngine` to the immersive Compose reading surface).
