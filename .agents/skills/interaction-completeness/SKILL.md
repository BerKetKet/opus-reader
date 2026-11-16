---
name: interaction-completeness
description: Makes every visible control and expected gesture actually work, persist and be tested. Use when building or reviewing behaviour of any screen, sheet or the reader.
---

# Interaction completeness

1. Work from docs/INTERACTION_SPEC.md. Every line is a requirement unless marked out of scope in docs/DECISIONS.md.
2. State lives in ViewModels backed by Room and DataStore. UI composables are stateless and take state and callbacks. A callback that defaults to `{}` and is never supplied is a bug.
3. Gestures: swipe page turn and tap zones in the reader, drag-to-dismiss sheets, long-press menus, swipe-to-dismiss rows, pull-to-sync. Use the standard Compose gesture APIs, with haptics where Kindle has them.
4. Settings must act live on what they control. A control that only changes its own look is not done.
5. Test every interaction: a Compose UI test with test tags, or an adb flow using `adb shell input tap/swipe` plus `uiautomator dump` to assert the result. List them in docs/INTERACTION_REPORT.md.
6. After any interaction change, run `python3 tools/run_fidelity.py --all` and confirm no resting-state screen got worse.
7. Never fake an interaction with a static image or a hard-coded result.
