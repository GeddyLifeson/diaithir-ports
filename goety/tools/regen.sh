#!/bin/bash
# regenerates all data and assets from the upstream tree: tools/regen.sh <upstream-dir>
cd "$(dirname "$0")/.."
python3 tools/gen.py "$1" && python3 tools/worldgen.py "$1" && python3 tools/audiofx.py "$1"
