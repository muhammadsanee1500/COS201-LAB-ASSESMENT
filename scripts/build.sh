#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build
if command -v javac >/dev/null 2>&1; then
  javac --release 21 -encoding UTF-8 -d build src/*.java tests/*.java
else
  java -jar /workspace/.tools/ecj-3.42.0.jar -21 -encoding UTF-8 -d build src/*.java tests/*.java
fi
