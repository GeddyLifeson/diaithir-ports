#!/usr/bin/env python3
"""Writes the assets, recipe, loot table, tags, age gate and Library row for the gantry split. Usage: gen_assets.py (from anywhere)."""
import json, os
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirgantries"


def w(path, obj):
    p = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


# The split has the same block entity type as Create's gantry shaft, so Create's own renderer/visual draws it; the block model is empty like Create's.
w(f"assets/{NS}/blockstates/gantry_split.json", {"variants": {"": {"model": "minecraft:block/air"}}})
w(f"assets/{NS}/models/item/gantry_split.json", {"parent": "create:block/gantry_shaft_single"})
w(f"assets/{NS}/items/gantry_split.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/gantry_split"}})
w(f"assets/{NS}/lang/en_us.json", {f"block.{NS}.gantry_split": "Gantry split"})
w(f"data/{NS}/recipe/gantry_split.json", {
    "type": "minecraft:crafting_shaped",
    "category": "misc",
    "key": {"A": "create:andesite_alloy", "I": "#c:nuggets/iron", "R": "#c:dusts/redstone"},
    "pattern": [" A ", "IRI", " A "],
    "result": {"id": f"{NS}:gantry_split", "count": 8}})
w(f"data/{NS}/loot_table/blocks/gantry_split.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:gantry_split"}],
                                                                                          "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
w("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:gantry_split"]})
w(f"data/{NS}/age_gate.json", {"gantry": {"age": 5, "items": [f"{NS}:gantry_split"]}})
lib = os.path.join(HERE, "..", "..", "library-sync", NS + ".json")
with open(lib, "w") as f:
    json.dump([{"id": f"{NS}:gantry_split", "name": "Gantry split",
                "text": "A gantry shaft that cuts a gantry into separate runs. Place it in a line of gantry shafts: it works as a shaft for carriages and power, but a moving carriage does not drag the next split along. Looks like a plain shaft until it is redrawn. Age V."}], f, indent=1)
    f.write("\n")
print("ok")
