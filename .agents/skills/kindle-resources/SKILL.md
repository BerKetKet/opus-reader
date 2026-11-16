---
name: kindle-resources
description: Decodes resources from the user's own installed Kindle app to read exact dimensions, colours, styles and layouts. Use at the start of the design system slice and whenever a token needs an authoritative value.
---

# Kindle resources (personal analysis only)

This reads the user's own copy of the app to learn measurements. Nothing from it is copied into the app except values written as our own tokens. Never publish or redistribute it.

1. `adb shell pm path com.amazon.kindle`, then `adb pull` the base APK to /tmp.
2. Decode with apktool (or jadx): `apktool d base.apk -o /tmp/kindle_res`.
3. Read `res/values/dimens.xml`, `colors.xml`, `styles.xml`, `themes.xml` and the layouts for the screens in docs/CANONICAL_SCREENS.md (for example the display settings layout and its bottom sheet behaviour).
4. Save only text extracts needed for reference to docs/kindle_res/ (small files, no assets).
5. Cross-check against the reference screenshots with tools/measure.py. Where they disagree, the screenshot wins for what is visible and the resource explains why (for example elevation tint and scrim colour).
