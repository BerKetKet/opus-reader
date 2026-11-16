# Slice S02 Plan: Data and Import

## Goal
Implement a robust, clean data layer in `:data` supporting EPUB parsing and ingestion, Room persistence with full relational schemas for books, spine items, table of contents, reading progress, and annotations, along with DataStore settings persistence and graceful failure handling.

## Scope
1. **Room Entities & Schema:**
   - `BookEntity`: Book metadata, cover path, reading position (spineIndex, charOffset), progress percentage, sample/new/download flags, timestamps, collection.
   - `SpineItemEntity`: Spine item href, media type, reading order index, linear flag.
   - `TocEntryEntity`: Table of contents entry title, target href, reading order, nesting level.
   - `AnnotationEntity`: User highlights, bookmarks, and notes with offsets, text snippet, note, color, timestamp.
2. **DAOs & Database:**
   - `BookDao`: CRUD, reactive Flow queries, progress updates, collection tagging.
   - `SpineDao`: Bulk insert, ordered spine item retrieval.
   - `TocDao`: Bulk insert, ordered TOC navigation tree retrieval.
   - `AnnotationDao`: Insert, delete, query by book, query by type.
   - `OpusDatabase`: Version 2 Room database with schema definitions and migrations/pre-population hooks.
3. **EPUB Importer (`EpubImporter`):**
   - Stream/file based Zip parser.
   - XML parsing for `META-INF/container.xml`, OPF package document (metadata, manifest, spine).
   - Cover extraction: identifies cover image from meta `name="cover"` or manifest `properties="cover-image"` and writes to app internal storage.
   - TOC parsing: EPUB 2 NCX (`toc.ncx`) and EPUB 3 Navigation Document (`nav.xhtml`).
   - Transactional import saving book and structure into Room.
   - Error handling: Gracefully handles corrupt zips, missing container.xml, missing OPF, or invalid XML.
4. **DataStore Settings (`SettingsRepository`):**
   - Reader settings: Font size, font family (Bookerly/Ember), theme index (Dark, Light, Sepia, Green), continuous scroll, line height, margins.
5. **Test EPUB Generation:**
   - Generation of valid, minimal, and malformed test EPUBs for automated verification.
6. **Testing & Process Restart Survival:**
   - Behavioral test suite testing parser, corrupt files, Room operations, and database re-instantiation across simulated process restarts.
