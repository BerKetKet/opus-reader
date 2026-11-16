# Findings on the first UI draft (verified by looking at the images, not from the old report)

## The old audit report is not authoritative
- It says "matches exactly" for screens that visibly differ (sheets, reader chrome, display settings).
- Its Kindle descriptions include things absent from the references: TOC tabs (Chapters/Pages/Bookmarks), a font dropdown, a 14-step size slider, a Boldness slider.
- It has no layout measurements. Colour was sampled at one point per screen.
- docs/comparison_pairs/pairs/ mixes two numbering schemes (pair_01_home and pair_01_library_grid both exist). Delete it and regenerate under the canonical ids in CANONICAL_SCREENS.md.
- Pairs compare different content (Read (16) vs Read (3), different books, different covers), so no pixel comparison is meaningful until Opus renders the same fixture data.
- Bad Kindle captures: notifications (a spinner), text selection and dictionary (menu not captured, only the page). Use docs/reference/09_notifications.png and treat text selection as inferred.

## Structural gaps (highest impact first)
1. **Sheets.** Kindle uses bottom-anchored modal sheets over the live screen with a dark scrim, a drag handle and rounded top corners: library filter, view and sort, annotation filter, sort, share, and the Aa display settings. Opus draws full-screen top-anchored panels with "DONE". All must become bottom sheets.
2. **Display settings (Aa).** Kindle: bottom sheet over the visible page, tabs Font | Layout | Themes | More, brightness bar pinned at the bottom, font carousel (not chips), size slider with A to A ends. Opus: full screen.
3. **Reader chrome visible.** Kindle: page shrinks into a framed card with the neighbouring pages peeking at both sides, a blue frame, bookmark ribbon, book title (or sample banner), "Location N of M · P%" centred, a scrubber with a grid icon at left. Opus: ordinary page with a slim bar.
4. **Reading canvas.** Kindle is immersive: no status bar, no nav bar, true #000000 in the black theme. Opus shows the status bar and uses #141414.
5. **Typography.** Kindle titles (Library, Home, More, Collections, Notifications, Settings, Annotations) are bold serif (Bookerly family); UI text is a different sans than Opus's Roboto-like one. Row text sizes in Opus are visibly smaller.
6. **Bottom nav.** Kindle has labelled tabs, active tab in blue with a different icon set. Opus is icon-only.
7. **Search field.** Kindle: full-radius pill, light outline, larger magnifier. Opus: small-radius rectangle.
8. **Collections.** Kindle: a Library view with tab bar, search and sort, no back arrow. Opus: a pushed screen with back arrow and no tab bar.
9. **Settings.** Kindle: flat rows, chevrons for navigation and square blue checkboxes (SD card, audiobooks, Sync). Opus invents section headers and uses switches.
10. **Home.** Kindle chips: Explore with gradient ring, All selected with blue outline, others outlined pills, trailing chevron. Opus: white-filled selected chip.
11. **TOC.** Kindle: centred title and author, X at right, all text white, white left bar and bold for the current chapter, deeper indents. Opus: left-aligned header, blue bar, grey text.
12. **Annotations.** Kindle: centred title, share and sort icons, a bordered filter button, chapter groups with counts, star, uppercase meta line, yellow quote bar, blue ADD NOTE. Opus: coloured dots and note cards.
13. **In-book search.** Kindle: only an underlined SEARCH field, empty-state text and keyboard. Opus: titled screen with an outlined field.
14. **Notifications.** Kindle rows have a leading icon or thumbnail, bold title, body, date. Opus is text only with an X.
15. **Light theme.** Kindle follows the system theme. 9 light references exist, plus reader light chrome. Opus has none.
16. **Covers.** Opus draws flat coloured tiles with title text. Kindle shows real covers with a downloaded check at bottom-right, SAMPLE and NEW corner ribbons.
17. **Resume cover.** Both place it bottom-left over the nav. Match its size and offset exactly.
