# Canonical screens (source of truth for pairing)

Reference = the Kindle image to match. All 1080x2340 at 420 dpi (dp = px / 2.625). Never edit references.
Ids are stable; use them in filenames, fixtures and reports: `docs/fidelity/<id>_{ref,app,diff}.png`.

| id | screen | reference file | notes |
|---|---|---|---|
| D01 | Library grid | docs/reference/01_library-grid.png | |
| D02 | Home | docs/reference/02_home.png | |
| D03 | More | docs/reference/03_more.png | |
| D04 | Settings | docs/reference/04_settings.png | |
| D05 | Library filter sheet | docs/reference/05_library-filter-sheet.png | bottom sheet over dimmed Library |
| D06 | Library view and sort sheet | docs/reference/06_library-view-sort-sheet.png | |
| D07 | Library list | docs/reference/07_library-list.png | |
| D08 | Collections (empty) | docs/reference/08_collections.png | |
| D09 | Notifications | docs/reference/09_notifications.png | ignore the audit's spinner capture |
| D10 | Reader chrome visible (dark) | docs/reference/10_reader-chrome-visible.png | scrubber card mode |
| D11 | Contents | docs/reference/11_contents-toc.png | |
| D12 | In-book search | docs/reference/12_reader-search.png | keyboard is system UI; mask it |
| D13 | Annotations | docs/reference/13_annotations.png | |
| D14 | Annotations filter sheet | docs/reference/14_annotations-filter-sheet.png | |
| D15 | Annotations sort sheet | docs/reference/15_annotations-sort-sheet.png | |
| D16 | Annotations share sheet | docs/reference/16_annotations-share-via-email.png | content replaced by Export, keep geometry |
| D17 | Display settings: Font | docs/reference/17_display-settings-font-tab.png | |
| D18 | Display settings: Layout | docs/reference/18_display-settings-layout-tab.png | |
| D19 | Display settings: Themes | docs/reference/19_display-settings-themes-tab.png | |
| D20 | Display settings: More | docs/reference/20_display-settings-more-tab.png | |
| L01-L09 | Light versions of D01-D09 | docs/kindle_audit/light_01..09_*.png | follows system theme |
| R01 | Reader chrome visible (light) | docs/kindle_audit/reader_light_chrome_visible.png | |
| R02 | Reader chrome hidden (light) | docs/kindle_audit/reader_light_chrome_hidden.png | |
| R03-R06 | Reading canvas white, sepia, green, black | docs/kindle_audit/theme_{white,sepia,green,black}_reading.png | page colour and margins only; mask text |
| R07-R10 | Chrome visible per theme | docs/kindle_audit/theme_{white,sepia,green,black}_chrome_visible.png | |
| R11-R14 | Display settings tabs, light | docs/kindle_audit/reader_light_settings_{font,layout,themes,more}_tab.png | |

## Inferred (no usable Kindle reference): text selection menu and dictionary card, book details, reader overflow menu, page thumbnails, search results, new-collection dialog, annotation item menu, note editor, device-name dialog.
Build them in the same visual language, list them in docs/PROGRESS.md as "inferred", and exclude them from pass/fail.
