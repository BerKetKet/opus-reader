---
name: natural-layout
description: Keeps the UI real: no pasted screenshot pixels, no fixture branches, no deformed or cut-off elements. Use before and after building any screen, and whenever a reference shows something cut off, covered or half scrolled.
---

# Real UI, natural layout

1. A reference is a frozen viewport of a scrollable, layered screen. Note which parts are cut by the edge, the bottom bar, a sheet or the scrim. That is viewport, not design.
2. Build the complete element in a real container. Give the container more data than the viewport shows. Reproduce the reference view with `scrollToItem(index, offsetPx)`.
3. Fixture mode supplies data and state only. There is no UI code that behaves differently in fixture mode.
4. Never use a crop of a reference as content. If you catch yourself cutting a PNG out of a reference to place it in the app, stop: build the composable instead, measure with tools/measure.py, and diff.
5. Gates, in order: `python3 tools/guard.py .` (must print GUARD OK), the fidelity loop, the liveness check (change a string, re-capture, the region must change), and the scroll test (`adb shell input swipe 540 1800 540 600 300` to the end; capture top, mid, end into docs/fidelity/scroll/; nothing deformed or cut except by the viewport and the real bottom bar).
6. Mask only a strip where a reference element is cut or covered. The visible part must still match.
