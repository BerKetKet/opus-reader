---
name: ui-fonts
description: Finds and installs the fonts that match the Kindle UI (serif titles, sans body) by testing candidates against reference crops. Use before building typography tokens or when text widths or weights differ from a reference.
---

# UI fonts

Kindle titles ("Library", "Home", "Settings", "Annotations") are bold serif in the Bookerly family. Body UI text is a different sans than Roboto, likely Amazon Ember. Verify, do not assume.

1. Serif: Bookerly files already exist in the engine folder (kindle_render/fonts/Bookerly-*.ttf). Copy the needed weights into the :design module (res/font). The user places the files in design-fonts/ at the repo root; move them from there.
2. Sans: if the Kindle app is installed on the phone, get its fonts from the user's own copy: `adb shell pm path com.amazon.kindle`, then `adb pull` the base APK and list *.ttf and *.otf inside it. This is for personal use only and must never be redistributed.
3. Test candidates: crop a text run from a reference (for example "Search Kindle" and the settings row titles). Render the same string with each candidate at the measured size, compare crop widths and pixel correlation, and keep the winner. Record the evidence in docs/DECISIONS.md.
4. Derive sizes from measured cap height and x-height, then check line height and letter spacing by overlay. Set `includeFontPadding = false` and explicit line heights.
5. If no candidate matches, use the closest open font, say so in docs/PROGRESS.md, and keep the residual diff visible in the report.
