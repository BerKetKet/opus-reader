# Interaction spec

Tags: (R) visible in a reference screenshot. (K) standard Kindle Android behaviour from convention, not in the screenshots; verify against the real Kindle app on the user's phone when possible. (M) Material/Android convention. Anything (K) or (M) is "inferred" in reports.

## Global
- Back button: closes the top dialog or sheet, else pops the screen. On a main tab other than Home it goes to Home, on Home it exits. (K)
- Every sheet and dialog closes by scrim tap, drag-down on the sheet, and system back. Sheets slide up and down with a short animation and follow the finger while dragging. (R: handle shown) (K)
- Every tappable thing has a pressed state and at least a 48dp target. (M)
- Every list, grid, carousel and sheet body scrolls. (R)
- Bottom tabs switch tabs and keep each tab's scroll position and state. Tapping the active tab scrolls to top. (K)
- Resume cover (bottom-left): shows the last-read book, opens it at the saved location, hidden inside the reader and under sheets. (R: shown) (K)
- Persistence (Room and DataStore, survives process death): library and read state, last location per book, bookmarks, highlights and notes, collections, sort, filter and view mode, reader settings and themes, notification read state, settings checkboxes.

## Library (D01, D05-D08)
- Cover or row tap opens the book at last location. (R)
- Long-press: a bottom sheet menu: Mark as read/unread, Add to collection, Remove download, Book details. (K)
- Search bar filters live; clear button when non-empty. (K)
- Filter sheet: toggles change counts and results live; DONE closes. Section headers collapse and expand (chevron). (R)
- View and sort sheet: Grid/List/Collections switch the view; Sort and Group by Series apply live and persist. (R)
- Check badge: downloaded state; tap offers remove download. (K)
- Pull down: sync. (K)
- Collections: create (dialog), rename, delete, open a collection, add books from the long-press menu, sort. (R: empty state and plus) (K)

## Home (D02)
- Chips filter the shelves. Shelves scroll horizontally. See All opens Library. (R)
- Gutenberg cover opens a details sheet with Download, Read, progress and cancel. (inferred)
- Bell opens Notifications; the badge clears once opened. (R)

## More and Settings (D03, D04)
- Sync now runs a visible sync state, then updates "last synced". (R: highlighted row) (K)
- Reading Challenges, Your Lists, Notebooks open real screens (a simple but working version each); Notebooks lists annotations across all books. (R)
- Checkboxes persist. Device name opens an edit dialog. Local file access and import open the file picker. Manage fonts lets the user add a TTF or OTF, which then appears in the font carousel. (R)

## Notifications (D09)
- Tap opens the target. Swipe sideways dismisses an item. Trash clears all after a confirmation. (R: trash) (K)

## Reader (D10-D13, D17-D20)
- Swipe left and right turns pages with the page-turn animation setting. Tap zones: left edge previous, right edge next, centre toggles the chrome. (K)
- Long-press a word starts a selection with draggable handles and a toolbar: highlight colours, Add note, Dictionary, Search, Share, Copy. (K) Tapping an existing highlight offers change colour, add note, delete. Links are followable. (K)
- The reader talks to this through new `ReaderEngine` methods (hit test, selection range, selection text). `FakeReaderEngine` implements word hit-testing over its plain text. Do not touch the real engine work.
- Scrubber: dragging shows the page-card preview and live location; chapter tick marks are tappable; the grid icon opens page thumbnails. (R) (K)
- Bookmark ribbon toggles with a haptic tick. (R)
- Top bar: back, Contents, Search, Notebook (Annotations), Aa, overflow (menu: Go to location, Book details, Sync to furthest page, Share). (R icons) (K menu)
- Display settings sheet: all controls act live on the page behind it.
  - Font carousel and size slider restyle the page immediately.
  - Brightness slider changes window brightness; Auto follows the system.
  - Page colour, margins, alignment, continuous scrolling (vertical scroll mode) and orientation lock all work. (R)
  - Themes apply a preset; Custom plus Save asks for a name and stores it. (R)
  - More tab: reading ruler (draggable), reading progress choices, visible clock, volume keys turn pages, page-turn animation, annotation menu on or off. Assistive reader uses Android text-to-speech. (R)
- Contents: tap jumps and closes; the current chapter scrolls into view. (R)
- In-book search: debounced search, results with snippet and location, tap jumps and highlights the match with next and previous; IME search action works. (R: empty state) (K)
- Annotations: tap jumps to the location; the dots menu offers Edit note, Change colour, Delete, Share; star toggles favourite; Add note opens an editor; filter and sort apply live; Export shares Markdown or text through the Android share sheet. (R)

## Done means
Each item above has a Compose UI test (with test tags) or an adb-driven smoke flow, and a row in docs/INTERACTION_REPORT.md: item, implemented, test name, verified on the phone, notes. Resting-state pixels must not change because of interaction work.
