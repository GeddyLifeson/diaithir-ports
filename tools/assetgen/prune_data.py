#!/usr/bin/env python3
"""Deletes data files (recipes, advancements, loot tables) that mention a Create item or block that does not ship.
Run from tools/assetgen after gen.py. Prints what it removed. Idempotent."""
import json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen, design
D = os.path.abspath(HERE + "/../../src/main/resources/data")
me = gen
shipped_items = {i for i in gen.ITEMS if design.shipped_item(me, i)}
all_ids = set(gen.ITEMS) | set(gen.BLOCKS) | set(gen.REG["fluids"])
shipped = set(shipped_items) | {b for b in gen.BLOCKS if design.shipped_block(me, b)} | set(gen.REG["fluids"])
dropped = all_ids - shipped

def strings(o, key=None):
    if isinstance(o, dict):
        for k, v in o.items():
            yield from strings(v, k)
    elif isinstance(o, list):
        for v in o:
            yield from strings(v, key)
    elif isinstance(o, str):
        yield key, o

def mentions_dropped(path):
    try:
        data = json.load(open(path))
    except Exception:
        return False
    for key, v in strings(data):
        if key in ("type", "tag", "criteria", "trigger") or v.startswith("#"):
            continue
        if v.startswith("create:") and v[7:] in dropped:
            return True
    return False

removed = {}
for sub in ("recipe", "advancement", "loot_table"):
    root = f"{D}/create/{sub}"
    if not os.path.isdir(root):
        continue
    for dp, _, fs in os.walk(root):
        for f in fs:
            p = os.path.join(dp, f)
            rel = os.path.relpath(p, D)
            name = f[:-5]
            drop = mentions_dropped(p)
            if sub == "advancement" and "/recipes/" not in p.replace(os.sep, "/") + "/":
                drop = True   # Create's own tutorial advancements are not shipped
            if sub == "advancement" and not drop:
                try:
                    for rid in json.load(open(p)).get("rewards", {}).get("recipes", []):
                        if rid.startswith("create:") and not os.path.exists(f"{D}/create/recipe/{rid[7:]}.json"):
                            drop = True
                except Exception:
                    pass
            if sub == "loot_table" and not drop:
                # loot tables are named after the block
                drop = name in dropped and "blocks" in dp
            if drop:
                removed.setdefault(sub, []).append(rel)
                if "--dry" not in sys.argv:
                    os.remove(p)
for k, v in removed.items():
    print(k, len(v))
if "--list" in sys.argv:
    for k, v in removed.items():
        for r in v:
            print(" ", r)
