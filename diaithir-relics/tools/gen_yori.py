#!/usr/bin/env python3
"""Stage 1 of the Yori3o fold: textures (redrawn here, not copied), models, item definitions, lang, recipes, curios tags, age gates."""
import json, os
from PIL import Image
HERE = os.path.dirname(__file__); R = os.path.join(HERE, "..", "src", "main", "resources"); NS = "diaithirrelics"
GEM = {"netherite": (70, 60, 66), "emerald": (40, 200, 90), "diamond": (90, 230, 235), "amethyst": (160, 100, 220), "quartz": (235, 230, 220), "golden": (240, 200, 60)}
PIECES = {"netherite_ring": ("Netherite Ring", "netherite", "ring"), "emerald_ring": ("Emerald Ring", "emerald", "ring"), "diamond_ring": ("Diamond Ring", "diamond", "ring"),
          "amethyst_ring": ("Amethyst Ring", "amethyst", "ring"), "quartz_ring": ("Quartz Ring", "quartz", "ring"), "golden_aglet": ("Golden Aglet", "golden", "charm"),
          "amethyst_aglet": ("Amethyst Aglet", "amethyst", "charm"), "netherite_shield_charm": ("Netherite Shield Charm", "netherite", "charm")}
INGOT = {"ring": "minecraft:gold_ingot", "charm": "minecraft:iron_ingot"}
GEMITEM = {"netherite": "minecraft:netherite_ingot", "emerald": "minecraft:emerald", "diamond": "minecraft:diamond", "amethyst": "minecraft:amethyst_shard", "quartz": "minecraft:quartz", "golden": "minecraft:gold_ingot"}
def shade(c, f): return tuple(max(0, min(255, int(v * f))) for v in c) + (255,)
def ring(g):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); P = im.putpixel
    band = (214, 178, 70)
    for x in range(16):
        for y in range(16):
            d = ((x - 7.5) ** 2 + (y - 9.5) ** 2) ** 0.5
            if 3.0 <= d <= 5.2: P((x, y), shade(band, 1.0 if (x + y) % 3 else 0.8))
    for x in range(6, 10):
        for y in range(2, 6): P((x, y), shade(g, 1.15 if (x + y) % 2 else 0.9))
    return im
def aglet(g):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); P = im.putpixel
    for y in range(2, 14):
        for x in range(6, 10): P((x, y), shade((190, 190, 200), 1.0 - 0.15 * (x == 6)))
    for y in range(9, 15):
        for x in range(5, 11): P((x, y), shade(g, 1.1 if y % 2 else 0.85))
    return im
def w(path, obj):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w").write(json.dumps(obj, indent=1))
lang = json.load(open(f"{R}/assets/{NS}/lang/en_us.json")); gates = json.load(open(f"{HERE}/../age_gates.json"))
tags = {"ring": [], "charm": []}
os.makedirs(f"{R}/assets/{NS}/textures/item", exist_ok=True)
for iid, (name, gem, slot) in PIECES.items():
    (ring if slot == "ring" else aglet)(GEM[gem]).save(f"{R}/assets/{NS}/textures/item/{iid}.png")
    w(f"assets/{NS}/models/item/{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
    w(f"assets/{NS}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}})
    lang[f"item.{NS}.{iid}"] = name
    ing = INGOT[slot]
    w(f"data/{NS}/recipe/{iid}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": [" G ", "I I", " I "] if slot == "ring" else [" I ", "IGI", " I "],
      "key": {"G": GEMITEM[gem], "I": ing}, "result": {"id": f"{NS}:{iid}", "count": 1}})
    gates["items"][f"{NS}:{iid}"] = "IV"
    tags[slot].append(f"{NS}:{iid}")
for slot, ids in tags.items():
    p = f"{R}/data/curios/tags/item/{slot}.json"; t = json.load(open(p)); t["values"] = sorted(set(t["values"]) | set(ids)); open(p, "w").write(json.dumps(t, indent=1))
open(f"{R}/assets/{NS}/lang/en_us.json", "w").write(json.dumps(lang, indent=1, ensure_ascii=False))
open(f"{HERE}/../age_gates.json", "w").write(json.dumps(gates, indent=1, ensure_ascii=False))
print(len(PIECES), "pieces")
