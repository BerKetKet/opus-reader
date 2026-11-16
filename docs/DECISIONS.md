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
