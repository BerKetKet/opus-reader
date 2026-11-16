# S01 Plan: Design System

## Goal
Build a measured, faithful Kindle design system in `:design` consisting of authoritative tokens (derived from `docs/kindle_res` and reference screenshots), real fonts (Bookerly & Amazon Ember), 12 core UI components, an interactive component gallery, and thorough behavioral tests.

## Phase 1: Tokens Documentation & Implementation
1. Write `docs/DESIGN_TOKENS.md` mapping every token name, value, and authoritative source (decompiled XML or reference screen ID).
2. Implement tokens in `:design`:
   - `OpusColors.kt`: Dark, Light, Reading canvas themes (White, Sepia, Green, Black), Scrim, Accent, Dividers.
   - `OpusTypography.kt`: Bookerly serif family, Amazon Ember sans family, explicit line heights, `includeFontPadding = false`.
   - `OpusDimensions.kt`: Spacing, touch targets (>=48dp), radius, bar heights, cover tile sizes.

## Phase 2: Components Implementation in `:design`
1. `OpusTopBar`: Title bar supporting Bookerly title, leading/trailing action icons.
2. `OpusSearchPill`: Rounded search bar with search icon, placeholder text, clear button.
3. `OpusBottomNavBar`: Bottom navigation with 3 labelled tabs (Home, Library, More).
4. `OpusListRow`: Generic list item with leading icon/content, title, subtitle, trailing action.
5. `OpusChevronRow`: Navigation setting row with title, optional value subtitle, trailing chevron.
6. `OpusCheckboxRow`: Interactive row with title, description, and Kindle checkbox.
7. `OpusToggleRow`: Interactive row with title, description, and Kindle switch.
8. `OpusChip`: Filter chip with selected/unselected states.
9. `OpusCoverTile`: Book cover image container supporting downloaded check badge, SAMPLE and NEW ribbons, and reading progress bar.
10. `OpusResumeCover`: Floating bottom-left mini cover showing last read progress.
11. `OpusSlider`: Kindle stepped/continuous slider with left/right icons or text labels.
12. `OpusBottomSheet`: Reusable sheet with peek and expanded states, drag handle, scrim dismiss, back handler, and animated height changes.

## Phase 3: Gallery & Tests
1. `ComponentGalleryScreen.kt`: Interactive gallery exercising all 12 components.
2. Compose UI tests in `:design`:
   - `OpusComponentsTest.kt`: Behavioral verification for all 12 components (clicks, selections, toggles, slider value changes, sheet dismiss).
3. Unit test in `:design`:
   - Behavioral validation replacing constant assertion.
4. Execute `tools/gates.sh` and verify with physical device.
5. Generate `docs/slices/S01_REPORT.md` with gate evidence and tag `s01-done`.
