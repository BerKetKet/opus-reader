# Opus Reader Architecture & Design Decisions

## S03b Fidelity Retrofit Decisions

1. **Gate Evidence & Report Check**:
   - `tools/report_check.py` enforces presence of `## Fidelity` section and percentage coverage for all implemented canonical screen IDs.
   - `tools/test_summary.py` enforces non-zero test counts and zero test failures across both UNIT and DEVICE suites for gate approval.

2. **Real UI Fixtures vs Production Isolation**:
   - Fixture seed data and reference cover bitmap assets reside exclusively in `app/src/debug/`.
   - Production and release source sets contain zero fixture flags, no synthetic conditionals, and a stub `DebugFixtureController`.
   - String resource overrides (`Search Kindle`) reside in `app/src/debug/res/values/strings.xml` to avoid modifying production UI text code.

3. **Kindle Content Alignment**:
   - Removed artificial chip row on D01 Library Grid to match canonical reference.
   - Implemented collapsible sections in D05 Filter Sheet: "Read state" (Read, Unread) and "Content type" (Books, Samples, Docs).
   - Added persistent "Group by Series" switch, View Mode (Grid, List, Collections), and Sort Mode (Most Recent, Title, Author A-Z, Author Z-A) to D06 View & Sort Sheet.
   - Calibrated D08 empty state to match reference text, 788px divider line, and "CREATE A NEW COLLECTION" button.

4. **Geometry & Token Calibration**:
   - Grid spacing calibrated to 46.refPx column gap and 47.refPx row gap for zero-drift 3-column pitch matching 1080x2340 reference.
   - Pinned `ResumeCoverOffsetBottom` to 34.refPx to compensate for 6dp Material3 Card elevation shadow padding, placing resume cover bottom at y=2242.
   - Sheet scrim dismiss zone utilizes full `weight(1f)` area strictly above the sheet container to prevent occlusion or interference with child content taps.

## S04 Engine API and Fake Decisions

1. **Module Independence (Hard Rule)**:
   - `:engine` has zero dependencies on `:data` and zero dependencies on `:app`.
   - Built a self-contained `EpubExtractor` within `app.opusreader.engine.internal` using standard Java `DocumentBuilderFactory` DOM parsing and `java.util.zip.ZipFile`, avoiding Android-specific XML parsers that break JVM contract testing.

2. **Deterministic Pagination & Hit Testing**:
   - `FakeReaderEngine` segments real spine text into individual word tokens, measuring width and line heights with Bookerly typography scales.
   - Each word is tracked with an exact `WordBox` containing bounding `RectF` and `Locator(spineIndex, charOffset)`.
   - `hitTest(page, x, y)` implements dual-stage detection: exact bounding box intersection with character interpolation, falling back to proximity within 100px.
   - Page navigation (`pageAt`, `next`, `previous`) preserves exact `Locator` across engine recreation.


## S05 Reader Chrome Decisions

1. **Window Status & Navigation Bar Color Synchronization**:
   - On the test device, Compose content begins at `y=83` below the hardware status bar. Rather than inserting artificial status bar spacers that displace the top chrome, `ReaderScreen` synchronizes `window.statusBarColor = #232323` and `window.navigationBarColor = #141414` via `DisposableEffect`.
   - `OpusReaderTopBar` renders from `y=83..243`, placing icons centered at `y=163` (matching reference icons at `y=166.08` with 0 px offset).

2. **Bottom Bar Geometry & Gesture Bar Inset Alignment**:
   - With window-level navigation bar coloring (`#141414`), Compose content naturally terminates at `y=2276`.
   - `OpusReaderBottomBar` height is set to `275.refPx` (`y=2001..2276`). The 1px divider aligns at `y=2276`, and the scrubber track aligns at `y=2191.5`, achieving 0 px offset against reference `10_reader-chrome-visible.png`.

3. **Table of Contents (D11) Structure & Geometry**:
   - Screen D11 features no tabs row. Header contains Bookerly Bold title, author, and close button (X), followed by a 1px divider.
   - Single-line chapters use a 132 refPx pitch (`OpusDimensions.ReaderContentsRowHeight`). Multi-line wrapped chapters use 185 refPx (`OpusDimensions.ReaderContentsMultiLineRowHeight`).
   - Chapter indents follow Kindle specification: Level 0 at 87 refPx, Level 1 at 129 refPx. Active reading chapter features a 4dp white indicator bar at the left margin.

4. **Interaction Zone Architecture**:
   - Tap zones: Left 20% navigates previous page, Right 20% navigates next page, Center 60% toggles chrome visibility.
   - Drag gestures: Horizontal drag threshold of 50px triggers page turn.
   - Scrubber: Continuous drag updates live location label; pointer release commits reading position to `ReaderEngine`.
