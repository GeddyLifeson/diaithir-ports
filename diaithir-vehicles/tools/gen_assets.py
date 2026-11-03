#!/usr/bin/env python3
"""Writes the original textures, models, blockstates, item definitions, lang, recipes, loot, tags, age gate and library entry for diaithir-vehicles.
Art is drawn here from scratch: a riveted steel drum with a hazard band (fuel tank) and a pressed-steel can (jerry can)."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirvehicles"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
for d in ("recipe", "loot_table/blocks"):
    os.makedirs(os.path.join(D, d), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)
rnd = random.Random(11)


def wj(rel, obj, base=A):
    p = os.path.join(base, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def steel(shade):
    return tuple(max(0, min(255, int(c + rnd.randint(-5, 5)))) for c in shade)


def tank_texture():
    im = Image.new("RGBA", (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            base = (88, 96, 108) if (x % 5) else (74, 82, 94)  # vertical plate seams
            band = 6 <= y <= 9
            if band:
                c = (232, 178, 26) if ((x + y) // 2) % 2 == 0 else (30, 30, 32)
            else:
                c = steel(base)
            if x in (0, 15) or y in (0, 15):
                c = (52, 58, 66, 255)[:3]
            if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
                c = (176, 184, 196)
            px[x, y] = (*c, 255)
    return im


def can_texture():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for y in range(3, 15):
        for x in range(3, 13):
            c = steel((70, 104, 56)) if 5 <= y else steel((90, 124, 74))
            if x in (3, 12) or y in (3, 14):
                c = (36, 56, 30)
            if y in (8, 9) and 5 <= x <= 10:
                c = (220, 200, 90)  # X-ribs shown as bands
            px[x, y] = (*c, 255)
    for x in range(5, 10):  # handle and spout
        px[x, 2] = (40, 40, 44, 255)
    px[10, 2] = px[11, 1] = (60, 60, 64, 255)
    return im


can_texture().save(os.path.join(A, "textures/item/jerry_can.png"))

wj("models/item/jerry_can.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/jerry_can"}})
for n in ("jerry_can",):
    wj(f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
wj("lang/en_us.json", {"item.diaithirvehicles.jerry_can": "Jerry Can"})

wj("recipe/jerry_can.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"I": "minecraft:iron_ingot", "N": "minecraft:iron_nugget"},
                              "pattern": ["NI ", "I I", "III"], "result": {"id": f"{NS}:jerry_can", "count": 1}}, D)
wj("age_gate.json", {"tier1": {"age": 5, "items": [f"{NS}:jerry_can"]}}, D)
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([
        {"id": f"{NS}:jerry_can", "name": "Jerry Can", "text": "Holds 20 L of one fuel. Use on a tank to pour in, sneak-use to draw out, right-click a vehicle to refuel it. Age V."},
    ], f, indent=1)
    f.write("\n")
