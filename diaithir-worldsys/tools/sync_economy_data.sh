#!/bin/sh
# Re-syncs the economy data from DiaithirRPG (content/economy/kubejs/data/diaithir, the generators' output), then re-applies the Age I/II vendor fill and
# the recipe-derived prices. Usage: tools/sync_economy_data.sh <DiaithirRPG checkout> (run from diaithir-worldsys). Afterwards regenerate the goldens with
# tools/economy/golden_*.js in DiaithirRPG, copy them to src/test/resources, update the sizes in EconDataTest and run the tests.
set -e
SRC="$1/content/economy/kubejs/data/diaithir"; D=src/main/resources/data/diaithirworldsys/economy
for f in currencies drink_value prices vendors vendors_age2 vendors_age3 vendors_age4 vendors_age5 vendors_age6 vendors_age7 vendors_age8 vendors_age9; do cp "$SRC/$f.json" "$D/$f.json"; done
cp "$D/drink_value.json" src/test/resources/drink_value.json
python3 -I tools/gen_vendor_fill.py
python3 -I tools/gen_derived_prices.py "$D/prices.json" "$D/gates.json" "$D/derived_prices.json"
