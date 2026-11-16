---
name: design-system-first
description: Builds look and feel as tokens and components in :design, measured once, reused everywhere. Use when adding or changing any visual element, colour, text style or sheet.
---

# Design system first

1. Values come from docs/kindle_res (decoded dimens, colours, styles, layouts) first, and from the reference screenshots second. Write each token with its source in a comment-free table in docs/DESIGN_TOKENS.md (name, value, source file or reference id).
2. Tokens: colours (dark, light, reading themes), type styles (family, size, line height, letter spacing), spacing, shapes, elevation and scrim. Only `:design` converts px to dp.
3. Components: stateless, parameters for content and callbacks, no default callbacks. Each has a gallery entry, a Compose UI test, and a diff of its crop against the reference crop.
4. The sheet is one component: peek and expanded anchors, drag, fling, scrim, back handling, tab-driven height changes.
5. Features compose components. If a feature needs a one-off measurement, the component is wrong; fix the component.
6. Typography: set explicit line height and `includeFontPadding = false`. Verify with tools/compare_typography.py (band alignment, delta y and delta h).
