"""Fails (exit 1) unless the culture table names every id the scan found: 100% of items, blocks without an item, entities,
dimensions, biomes and structures.  usage: check_coverage.py INPUT_DIR CULTURE_JSON"""
import json, sys

src, table = sys.argv[1:3]
t = json.load(open(table, encoding="utf8"))
FILES = {"item": "items.json", "block": "blocks_only.json", "entity": "entities.json",
         "dimension": "dimensions.json", "biome": "biomes.json", "structure": "structures.json"}
bad = 0
for kind, fn in FILES.items():
    ids = json.load(open(f"{src}/{fn}", encoding="utf8"))
    missing = [i for i in ids if not t.get(kind, {}).get(i)]
    print(f"{kind}: {len(ids) - len(missing)}/{len(ids)}", ("MISSING " + ", ".join(missing[:5])) if missing else "")
    bad += len(missing)
sys.exit(1 if bad else 0)
