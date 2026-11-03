#!/usr/bin/env python3 -I
"""Prices crafted Foundry items from their real recipes (tools/recipes/*.json, from ports #163), with the economy model's own rule
(00_model.js / EconModel): cost = sum over inputs of qty * (LABOUR + input price), per unit made; LABOUR = 0.25 tw. Hand-crafted items
add no machine energy (machine_stats.json covers machines only). A #tag ingredient is priced only through TAGS below: the vanilla
member the tag is named for. A recipe with any unpriced input is skipped, never guessed. Usage: gen_derived_prices.py prices.json gates.json out.json"""
import json, glob, sys
LABOUR = 0.25
TAGS = {"c:ingots/gold": "minecraft:gold_ingot", "c:plates/copper": "minecraft:copper_ingot"}  # a plate is one pressed ingot
prices = json.load(open(sys.argv[1])); gates = json.load(open(sys.argv[2]))
out = {}
for f in sorted(glob.glob("tools/recipes/*.json")):
    r = json.load(open(f)); rid = r["result"]["id"]; n = r["result"].get("count", 1)
    cost = 0.0; ok = True
    for ing in r["ingredients"]:
        key = TAGS.get(ing[1:]) if ing.startswith("#") else ing
        p = prices.get(key) if key else None
        if p is None: ok = False; break
        cost += LABOUR + p[0]
    if not ok or rid not in gates: print("skipped", rid); continue
    price = round(cost / n, 2)
    out[rid] = [price, max(8, round(256 / price ** 0.5)), gates[rid][0]]  # usual stock follows the price, as EconData.good
    print(rid, out[rid])
json.dump(out, open(sys.argv[3], "w"), indent=0)
