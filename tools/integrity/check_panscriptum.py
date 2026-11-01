#!/usr/bin/env python3 -I
"""Panscriptum <-> pack match. Usage: check_panscriptum.py <DiaithirRPG_root> [packmap_dir] [--min-pct N]
Errors: a Panscriptum world whose dimension is not in the pack's dimension registry; worlds/slots/shelves out of step.
Info: share of pack items, blocks, entities, biomes, structures, dimensions and enchantments that have a library entry (the 1:1 gap)."""
import sys, json, csv, collections, os
args = [a for a in sys.argv[1:] if not a.startswith("--")]
minpct = float(sys.argv[sys.argv.index("--min-pct") + 1]) if "--min-pct" in sys.argv else 0
if minpct: args = [a for a in args if a != str(sys.argv[sys.argv.index("--min-pct") + 1])]
rpg = args[0]; pm = args[1] if len(args) > 1 else "/mnt/project-files/packmap"
E = []
worlds = json.load(open(f"{rpg}/content/omniverse/worlds.json"))
slots = json.load(open(f"{rpg}/content/omniverse/slots.json"))
shelves = json.load(open(f"{rpg}/content/omniverse/shelves.json"))
dims = {r["id"]: r["has_library_entry"] for r in csv.DictReader(open(f"{pm}/dimension_to_library.tsv"), delimiter="\t")}
wid = {w["id"] for w in worlds}
if len(wid) != len(worlds): E.append("duplicate world ids")
if wid != set(slots): E.append(f"worlds vs slots differ: {len(wid ^ set(slots))} ids")
for d, n in collections.Counter(w["dimension"] for w in worlds).items():
    if d not in dims: E.append(f"world dimension {d} ({n} worlds) not in the pack dimension registry")
srcs = {w["source"] for w in worlds}
for s in sorted(srcs - set(shelves)): E.append(f"world source '{s}' has no shelf")
print(f"worlds {len(worlds)}, slots {len(slots)}, shelves {len(shelves)}, dimensions used {len({w['dimension'] for w in worlds})}")
worst = 100
for kind in ("item", "block", "entity", "biome", "structure", "dimension", "enchantment"):
    rows = list(csv.DictReader(open(f"{pm}/{kind}_to_library.tsv"), delimiter="\t"))
    got = sum(r["has_library_entry"] == "1" for r in rows)
    pct = 100 * got / max(1, len(rows)); worst = min(worst, pct)
    print(f"{kind}: {got}/{len(rows)} have a library entry ({pct:.1f}%)")
if worst < minpct: E.append(f"coverage {worst:.1f}% below {minpct}%")
for e in E: print("ERROR", e)
print("ERRORS", len(E)); sys.exit(1 if E else 0)
