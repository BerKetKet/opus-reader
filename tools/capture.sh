#!/bin/sh
set -e
OUT="${1:?usage: capture.sh out.png}"
SIZE=$(adb shell wm size | tail -1 | sed 's/.*: //')
DENS=$(adb shell wm density | tail -1 | sed 's/.*: //')
[ "$SIZE" = "1080x2340" ] && [ "$DENS" = "420" ] || { echo "device must be 1080x2340 at 420 dpi, got $SIZE $DENS"; exit 1; }
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1200 >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false >/dev/null
sleep 1
adb exec-out screencap -p > "$OUT"
