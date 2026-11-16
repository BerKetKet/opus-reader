# Slice S02 Report: Data and Import

## Gates Output
```text
UNIT    4 tests  0 failed  0 skipped  app.opusreader.design.DesignTest
UNIT    1 tests  0 failed  0 skipped  app.opusreader.engine.LocatorTest
UNIT    6 tests  0 failed  0 skipped  app.opusreader.data.BookEntityTest
UNIT    1 tests  0 failed  0 skipped  app.opusreader.AppUnitTest
UNIT TOTAL: 12 tests, 0 failed, 0 skipped
DEVICE    4 tests  0 failed  0 skipped  app.opusreader.design.ComponentGalleryTest
DEVICE    2 tests  0 failed  0 skipped  app.opusreader.data.DatabaseRestartTest
DEVICE    2 tests  0 failed  0 skipped  app.opusreader.MainActivityTest
DEVICE TOTAL: 8 tests, 0 failed, 0 skipped
TEST SUMMARY OK
GUARD OK
GATES OK
```

## Executed Test Details
- `BookEntityTest` (6 behavioral unit tests in `:data`):
  1. `validEpub_parsesMetadataSpineAndTocCorrectly`: Parses real EPUB zip archive, extracts metadata (title, author, ID), builds ordered spine items, parses NCX table of contents, and extracts cover image bytes.
  2. `minimalEpub_parsesCleanlyWithoutCoverOrNcx`: Verifies EPUBs without NCX or cover fallback to single-spine chapter navigation gracefully.
  3. `malformedEpub_corruptZip_throwsGracefully`: Asserts corrupted zip files throw `MalformedEpubException` with a clear message and do not crash.
  4. `malformedEpub_missingContainer_throwsGracefully`: Asserts archives missing `META-INF/container.xml` throw `MalformedEpubException`.
  5. `malformedEpub_missingOpf_throwsGracefully`: Asserts archives whose container points to a missing OPF package file throw `MalformedEpubException`.
  6. `annotationEntity_creationAndDefaults`: Asserts annotation data structures (highlights, notes, bookmarks) and default yellow color.
- `DatabaseRestartTest` (2 connected device tests on physical Samsung Galaxy M32):
  1. `dataSurvivesProcessRestart`: Proves complete data survival across process restarts. Opens database 1, inserts books, spine items, TOC entries, annotations, and updates reading position (`spineIndex = 1`, `charOffset = 350`, `progressPercent = 65%`). Closes database 1 to simulate process death. Opens fresh database 2 instance at the same path and verifies 100% of book metadata, spine items, TOC entries, annotations, and reading progress are preserved.
  2. `epubImport_successAndErrorHandling`: Imports a real EPUB via `EpubImporter` on device. Verifies database records, cover file extracted to internal storage, spine items, and TOC. Verifies malformed/corrupt files return failure results gracefully without leaving orphaned database rows or files.
- `ComponentGalleryTest`: 4 connected device tests in `:design`.
- `MainActivityTest`: 2 connected device tests in `:app`.

## Implemented in `:data`
1. **Room Relational Database (`OpusDatabase` v2):**
   - `BookEntity`: ID, title, author, file path, cover path, reading position (`spineIndex`, `charOffset`, `progressPercent`), flags (`isDownloaded`, `isSample`, `isNew`), collections, and timestamps.
   - `SpineItemEntity`: Book ID, relative href, media type, order index, and linear flag.
   - `TocEntryEntity`: Book ID, entry title, href, order index, and hierarchical level.
   - `AnnotationEntity`: Highlights, bookmarks, notes with offsets, text snippet, note text, and color hex.
2. **DAOs:**
   - `BookDao`: Reactive Flow queries for all books, downloaded books, collections, and progress updater.
   - `SpineDao`: Bulk insert, ordered spine item retrieval, cascading deletions.
   - `TocDao`: Bulk insert, ordered table of contents retrieval.
   - `AnnotationDao`: Insert, delete, and query by book.
3. **EPUB Ingestion Engine:**
   - `EpubParser`: Clean XML and ZIP streaming parser for `container.xml`, `content.opf`, and `toc.ncx` / EPUB 3 nav documents.
   - `EpubImporter`: Coordinates safe copying to internal storage (`files/epubs/`), cover extraction (`files/covers/`), and atomic Room transactions.
4. **DataStore Settings (`SettingsRepository`):**
   - Reactive preferences for reader themes (Sepia, Green, Black, White), font family (Bookerly, Ember), font size (8-32pt), line spacing, continuous scrolling, brightness, and library view/sort options.
5. **Test EPUB Generator (`tools/generate_test_epubs.py`):**
   - Generates valid, minimal, corrupt zip, missing container, and missing OPF test files.

## Inferred vs Missing
- **Inferred:** EPUB 2 NCX was prioritized as the primary TOC format for test fixtures; EPUB 3 `<nav>` fallback generates TOC from spine items if not present.
- **Missing:** Automatic folder synchronization (scheduled for later slices when background sync is added).
