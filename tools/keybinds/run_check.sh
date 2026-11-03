#!/bin/sh
# Local keybind gate (no GitHub Actions). Usage: run_check.sh JAR_DIR PACK_ROOT
# Extracts every KeyMapping from the jars, then fails on any conflict with PACK_ROOT/options.txt applied.
set -e
d=$(dirname "$0"); t=$(mktemp)
python3 -I "$d/extract_keymappings.py" "$1" "$t"
python3 -I "$d/check_keybinds.py" "$t" "$2"
