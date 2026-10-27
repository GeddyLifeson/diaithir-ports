#!/bin/bash
# rebuild map, remap every cached addon jar, residual table. Usage: run_all.sh  (from repo root)
set -e
RT=$(cat tools/shim/runtime.txt)
python3 -I tools/shim/build_map.py tools/shim/map.json $RT -- addons-cache/*.jar
rm -f out/create*.jar
for j in addons-cache/*.jar; do n=$(basename "$j" | tr -d '[]+'); python3 -I tools/shim/remap.py "$j" "out/$n" tools/shim/map.json >/dev/null; done
python3 -I tools/shim/mcdiff.py tools/shim/residual.json out/create*.jar -- $RT
