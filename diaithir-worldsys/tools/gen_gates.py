#!/usr/bin/env python3 -I
"""Writes economy/gates.json from the progression graph: item id -> [opens_age, nodes]. opens_age is the earliest Age of any node
making the item; nodes counts how many graph nodes make it (tiers of one item, e.g. gear a1..a9). Real graph rows only."""
import json, sys
g = json.load(open(sys.argv[1]))
ages = g["ages"]
out = {}
for n in g["nodes"]:
    it = n.get("item")
    if not it:
        continue
    cur = out.get(it)
    if cur is None:
        out[it] = [n["age"], 1]
    else:
        if ages.index(n["age"]) < ages.index(cur[0]):
            cur[0] = n["age"]
        cur[1] += 1
json.dump(dict(sorted(out.items())), open(sys.argv[2], "w"), indent=0)
print(len(out), "items")
