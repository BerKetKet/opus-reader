---
name: pixel-fidelity-loop
description: Measures a real screen or component against its Kindle reference with tools/capture.sh, tools/pixdiff.py and tools/compare_typography.py and fixes the largest differences. Use after building any screen and throughout slice S11.
---

# Pixel fidelity loop

1. Open the fixture: `adb shell am start -a android.intent.action.VIEW -d opus://fixture/<id>`. Fixtures supply data and state only.
2. Capture: `tools/capture.sh docs/fidelity/<id>_app.png`.
3. Diff: `python3 tools/pixdiff.py <reference> docs/fidelity/<id>_app.png docs/fidelity/<id> --bottom 24` with `--mask` for the reading-text area, keyboard and cover images. Bands entirely inside a mask are ignored. For sheets over the reader, the page behind is an empty page so reading text does not pollute the diff.
4. Fix in this order: band positions (components and tokens, not per-screen spacers), colours (including the dark-mode surface tint and scrim), text size and line height, letter spacing, icons.
5. Liveness: re-capture with a changed string or font scale; the pixels must change. Scroll test: top, middle and end captures in docs/fidelity/scroll/.
6. Thresholds (outside masks): at most 2% of pixels differ by more than 8 per channel, band edges within 1 px, colours exact. Report honest numbers; if a screen cannot reach them with real UI, say why. Regenerate docs/FIDELITY_REPORT.md from the tools, never by hand.
