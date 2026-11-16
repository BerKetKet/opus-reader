#!/bin/sh
set -e
cd "$(dirname "$0")/.."
rm -rf */build/test-results */build/outputs/androidTest-results
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew connectedDebugAndroidTest
python3 tools/test_summary.py .
python3 tools/guard.py .
echo "GATES OK"
