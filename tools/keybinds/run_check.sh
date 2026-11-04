#!/bin/sh
# Local keybind gate (no GitHub Actions). Usage: run_check.sh JAR_DIR PACK_ROOT
# live_defaults.txt = real-client dump of every mapping (dump_live_keys.py); refresh it when mods change.
# Extracts every KeyMapping from the jars, then fails on any conflict with PACK_ROOT/options.txt applied.
set -e
d=$(dirname "$0"); t=$(mktemp)
python3 -I "$d/extract_keymappings.py" "$1" "$t"
python3 -I "$d/check_keybinds.py" "$t" "$2" "$d/live_defaults.txt"
