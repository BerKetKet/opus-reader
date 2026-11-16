# Slice S03 Plan: Library

## Goal
Implement the full Library experience conforming to canonical screens D01, D05, D06, D07, and D08. Built purely from `:design` tokens and components, backed by `LibraryViewModel` and persisted Room + DataStore state.

## Scope
1. **Screens & Views:**
   - **D01 (Grid View):** 3-column responsive grid with `OpusCoverTile`s, progress bars, download badges, search pill, filter chips ("Downloaded", "Unread", "Books", "Samples"), and sort/view button.
   - **D05 (Filter Sheet):** Bottom sheet displaying filter options with live item counts, checkboxes (`OpusCheckboxRow`), and "Done" action button.
   - **D06 (View & Sort Sheet):** Bottom sheet allowing toggling between Grid, List, and Collections view modes, and sorting by Recent, Title, or Author.
   - **D07 (List View):** Linear list with `OpusListRow` displaying book covers, titles, authors, reading progress, and trailing options menu.
   - **D08 (Collections View):** Collections screen showing user-created collections, book counts per collection, drill-down into collection contents, and "New Collection" creation dialog.
   - **Book Context Sheet:** Triggered on book long-press. Options: "Mark as read/unread", "Add to collection", "Remove download", "Delete book".
   - **Floating Resume Cover:** `OpusResumeCover` anchored at bottom-left showing the cover of the most recently read book. Tapping navigates directly to the reader at saved locator.
2. **State & Architecture:**
   - `LibraryViewModel` in `:app`: Exposes single `StateFlow<LibraryUiState>`, interacts only with `LibraryRepository` and `SettingsRepository`.
   - Data isolation: Feature composables consume only `BookUiModel` and callback lambdas. No `:data` imports in `LibraryScreen.kt`.
   - Strict design tokens: Zero `.refPx`, zero hardcoded spacer heights (using `OpusDimensions.Spacing*`), zero default no-op callbacks.
3. **Verification & Testing:**
   - Behavioral unit tests replacing placeholder `AppUnitTest.kt`: Filter toggling, search query matching, sort order application, collection creation/deletion, read status toggle, and sheet dismissals.
   - Connected UI tests on device (`LibraryScreenTest.kt`): Rendering grid, switching to list view, opening/dismissing filter sheet, typing search query, and tapping resume cover.
