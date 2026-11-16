# Slice S04 Plan: Engine API and Fake

## Goal
Implement `:engine` module with `ReaderEngine` interface, page model, and `FakeReaderEngine` capable of parsing and paginating real EPUB text in Bookerly with hit testing, progress calculation, drawing, and in-book search.

## Scope
1. **Engine Model & Interface (`:engine`)**:
   - `ReaderEngine` interface adhering to `docs/ARCHITECTURE.md`.
   - Page model: `Locator`, `LocatorRange`, `WordBox`, `LinkBox`, `Page`, `BookSource`, `ReaderSettings`, `BookInfo`, `Progress`, `PageTheme`, `SearchHit`, `TocEntry`.
2. **FakeReaderEngine**:
   - Parses real EPUB zip contents (spine XHTML/HTML items, metadata, TOC) without depending on `:data`.
   - Paginates text into `Page` objects using Android `Paint` / `Typeface` (Bookerly / Serif), calculating exact bounding `RectF` for each word.
   - Computes `hitTest(page, x, y)` returning `Locator`.
   - Computes `rectsFor(page, range)` returning bounding boxes for selections/highlights.
   - Implements `draw(page, canvas, theme)` for canvas rendering.
   - Implements `search(query)` returning occurrences with snippets.
   - Supports page navigation (`pageAt`, `next`, `previous`) across spines and locators.
3. **Contract Tests (`engine/src/test/`)**:
   - Contract test suite testing `FakeReaderEngine`:
     - Open real generated test EPUB.
     - Verify TOC and spine navigation.
     - Page flipping forward and back across spine boundaries.
     - Exact `Locator` preservation and re-anchoring across engine recreation.
     - Word hit testing returning exact word and bounding box.
     - Search returning matching hits with locators.
4. **Gates & Report**:
   - Run `tools/gates.sh` verifying all tests pass.
   - Create `docs/slices/S04_REPORT.md` with:
     ```markdown
     ## Fidelity
     NOT RUN: no canonical screen in this slice
     ```
   - Verify with `python3 tools/report_check.py docs/slices/S04_REPORT.md`.
