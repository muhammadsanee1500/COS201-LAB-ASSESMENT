#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/build.sh
java -cp build CourseManagerTest
python3 tests/console_test.py
