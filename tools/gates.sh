#!/bin/sh
set -e
cd "$(dirname "$0")/.."
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew connectedDebugAndroidTest
python3 tools/guard.py .
echo "GATES OK"
