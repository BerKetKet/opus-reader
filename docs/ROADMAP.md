# Roadmap: vertical slices

Each slice: goal, scope, acceptance. All slices end with `tools/gates.sh` passing and a docs/slices/SNN_REPORT.md. Screen ids are in docs/CANONICAL_SCREENS.md; behaviours in docs/INTERACTION_SPEC.md.

**S00 Skeleton.** Four Gradle modules, version catalog, AppContainer, NavHost with placeholder tabs, debug source set, `tools/gates.sh`, guard. Accept: gates pass, app launches on the phone, guard prints GUARD OK.

**S01 Design system.** Decode Kindle resources (skill kindle-resources). Tokens for dark and light. Fonts. Components: top bar, search pill, labelled bottom tabs, list row, chevron row, checkbox row, toggle row, chip, cover tile (check badge, SAMPLE and NEW ribbons, progress bar), resume cover, sliders, and the sheet (peek and expanded, drag, scrim, back). Debug gallery. Accept: component UI tests; each component's crop diffs under 0.5% against the reference crop.

**S02 Data and import.** Room schema and repositories; EPUB import by picker and share intent (metadata, cover, TOC, spine); debug seeding; generated test EPUBs including a malformed one. Accept: import works, data survives restart, bad files fail gracefully.

**S03 Library.** Tabs shell, resume cover, grid, list, collections view, search, filter sheet, view and sort sheet, long-press menu, collections CRUD, everything persisted. Screens D01, D05-D08. Accept: a UI test per spec line; fidelity reported.

**S04 Engine API and fake.** `:engine` interface, page model, FakeReaderEngine paginating real EPUB text in Bookerly, hit testing, contract tests. Accept: open an imported book, turn pages, restart, same locator.

**S05 Reader chrome.** Immersive reader, swipe pager, tap zones, chrome toggle, scrubber with preview, bookmark ribbon, Contents, overflow menu, page thumbnails. D10, D11.

**S06 Selection and annotations.** Selection handles and toolbar, highlights, notes, bookmarks, annotations screen with filter, sort and export, in-book search. D12-D16.

**S07 Display settings.** Sheet with peek (Font) and expanded (other tabs). Every control acts live on the page: font, size, spacing, margins, alignment, colours, themes with save, brightness, orientation, scroll mode, ruler, progress, clock, volume keys, animation, text-to-speech. D17-D20, R03-R14.

**S08 Home and Gutenberg.** Shelves, chips, details sheet, downloads, import to library. D02.

**S09 More, Settings, Notifications.** Sync now (local backup and restore), reading stats, lists, notebooks, settings, font manager, notification feed. D03, D04, D09.

**S10 Light theme.** System theme following. L01-L09, R01, R02, R11-R14.

**S11 Fidelity pass.** All canonical screens against references with honest thresholds: calibrate tokens and components, typography alignment, sheet states, scrim. Report per screen, never trade honesty for score.

**S12 Hardening.** Error and empty states, process death and rotation, large EPUB performance, accessibility, release build check, backup of the database.
