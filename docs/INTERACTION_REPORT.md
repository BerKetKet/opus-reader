# Interaction Report — Opus Reader

Tracks behavioral interaction implementations and test verification on physical device per `docs/INTERACTION_SPEC.md`.

| Screen / Feature | Item | Implemented | Test Name | Verified on Phone | Notes |
|---|---|---|---|---|---|
| **Reader (D10)** | Left zone tap (0-20% width) | Yes | `ReaderScreenTest.tapZones_leftPreviousPage_centerChrome_rightNextPage` | Yes | Jumps to previous page |
| **Reader (D10)** | Right zone tap (80-100% width) | Yes | `ReaderScreenTest.tapZones_leftPreviousPage_centerChrome_rightNextPage` | Yes | Jumps to next page |
| **Reader (D10)** | Center zone tap (20-80% width) | Yes | `ReaderScreenTest.tapZones_leftPreviousPage_centerChrome_rightNextPage` | Yes | Toggles top & bottom chrome visibility |
| **Reader (D10)** | Horizontal swipe drag | Yes | `ReaderScreenTest.swipeGesture_changesPage` | Yes | Swipe left navigates forward, swipe right backward |
| **Reader (D10)** | Bookmark ribbon top-right | Yes | `ReaderScreenTest.bookmarkRibbon_togglesBookmarkState` | Yes | Toggles bookmark state |
| **Reader (D10)** | Scrubber drag and tap | Yes | `ReaderScreenTest.scrubber_dragAndTapUpdatesLiveLocation` | Yes | Live location updates and commits on release |
| **Reader (D10)** | Birds-eye grid button | Yes | `ReaderScreenTest.topBarAndBottomBar_actionsWired` | Yes | Thumbnails action wired |
| **Reader (D10)** | Top bar action buttons | Yes | `ReaderScreenTest.topBarAndBottomBar_actionsWired` | Yes | Back, Contents, Search, Bookmark, Aa, Overflow wired |
| **Contents (D11)** | Chapter select & jump | Yes | `ReaderScreenTest.contentsToc_opensAndJumpsToChapter` | Yes | Jumps locator to spine and char offset, closes TOC |
| **Contents (D11)** | Close button (X) | Yes | `ReaderScreenTest.contentsToc_opensAndJumpsToChapter` | Yes | Dismisses TOC sheet |
| **Library (D01)** | Book tap opens reader | Yes | `LibraryScreenTest.bookTap_navigatesToReader` | Yes | Opens `reader/{bookId}` with reader chrome |
| **Library (D01)** | Resume cover tap opens reader | Yes | `LibraryScreenTest.resumeCover_navigatesToReader` | Yes | Resumes last read book |
| **Library (D05)** | Filter sheet open & apply | Yes | `LibraryScreenTest.filterSheet_openAndApply` | Yes | Filter checkboxes update state |
| **Library (D06)** | View and sort sheet | Yes | `LibraryScreenTest.viewSortSheet_changeModes` | Yes | Changes grid/list view and sort order |
