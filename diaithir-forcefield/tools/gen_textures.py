#!/usr/bin/env python3
"""Writes the original 16x16 textures, models, blockstates, item definitions and lang for diaithir-forcefield.
Everything is drawn here from scratch: dark worn steel plates, rivets, yellow and orange hazard accents (a factory look)."""
import json, os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "diaithirforcefield"
A = os.path.join(ROOT, "assets", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)

STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (34, 35, 38)
RIVET = (122, 126, 132)
YEL = (240, 190, 30)
ORG = (222, 120, 24)
BLK = (30, 30, 30)
rnd = random.Random(7)


def plate(w=16, h=16):
    im = Image.new("RGBA", (w, h))
    px = im.load()
    for y in range(h):
        for x in range(w):
            px[x, y] = STEEL[rnd.choice((0, 0, 1, 1, 3))] + (255,)
    for i in range(w):
        for j in (0, h - 1):
            px[i, j] = EDGE + (255,)
            px[j, i] = EDGE + (255,)
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[cx, cy] = RIVET + (255,)
    return im


def hazard(im, y0, y1):
    px = im.load()
    for y in range(y0, y1):
        for x in range(16):
            px[x, y] = (YEL if ((x + y) // 2) % 2 == 0 else BLK) + (255,)


def save(im, path):
    im.save(os.path.join(A, path))


# projector
side = plate()
hazard(side, 12, 15)
save(side, "textures/block/projector_side.png")

top = plate()
px = top.load()
for y in range(4, 12, 2):
    for x in range(3, 13):
        px[x, y] = (22, 23, 25, 255)
save(top, "textures/block/projector_top.png")

bottom = plate()
save(bottom, "textures/block/projector_bottom.png")


def front(active):
    im = plate()
    px = im.load()
    cx = cy = 7.5
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d < 2.5:
                px[x, y] = ((255, 236, 150) if active else (90, 62, 28)) + (255,)
            elif d < 4.2:
                px[x, y] = (ORG if active else (110, 70, 24)) + (255,)
            elif d < 5.2:
                px[x, y] = EDGE + (255,)
    return im


save(front(False), "textures/block/projector_front.png")
save(front(True), "textures/block/projector_front_active.png")

# field: translucent amber panel, brighter frame and a sparse grid, so a shell reads as a skin of cells
fld = Image.new("RGBA", (16, 16), (240, 170, 40, 46))
px = fld.load()
for i in range(16):
    for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
        px[x, y] = (255, 205, 80, 150)
for i in (5, 10):
    for j in range(16):
        px[i, j] = (255, 190, 60, 70)
        px[j, i] = (255, 190, 60, 70)
save(fld, "textures/block/field.png")


# module icons: a card with a coloured band and a glyph
def card(color, glyph):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for y in range(2, 14):
        for x in range(2, 14):
            px[x, y] = STEEL[(x + y) % 2 + 1] + (255,)
    for i in range(2, 14):
        for (x, y) in ((i, 2), (i, 13), (2, i), (13, i)):
            px[x, y] = EDGE + (255,)
    for x in range(3, 13):
        px[x, 3] = color + (255,)
        px[x, 4] = color + (255,)
    for (x, y) in glyph:
        px[x, y] = color + (255,)
    px[3, 12] = RIVET + (255,)
    px[12, 12] = RIVET + (255,)
    return im


def ring(cx, cy, r):
    return [(x, y) for x in range(16) for y in range(16) if r - 0.6 <= ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 <= r + 0.4]


G = {
    "range_module": ring(7.5, 8.5, 2) + ring(7.5, 8.5, 4),
    "strength_module": [(x, y) for x in range(5, 11) for y in range(6, 12) if x in (5, 10) or y in (6, 11)] + [(7, 8), (8, 8), (7, 9), (8, 9)],
    "mob_filter_module": [(5 + i, 6 + i) for i in range(6)] + [(10 - i, 6 + i) for i in range(6)],
    "guest_list_module": [(x, y) for x in range(5, 11) for y in (6, 8, 10)],
    "capacitor_module": [(x, y) for x in (6, 9) for y in range(6, 12)] + [(7, 8), (8, 8)],
    "sting_module": [(8, 6), (8, 7), (7, 8), (8, 8), (9, 8), (8, 9), (8, 10), (7, 10), (9, 10)] + [(6, 7), (10, 7), (6, 9), (10, 9)],
}
COL = {
    "range_module": (80, 200, 230),
    "strength_module": ORG,
    "mob_filter_module": (110, 200, 90),
    "guest_list_module": YEL,
    "capacitor_module": (230, 80, 70),
    "sting_module": (200, 90, 220),
}
for k, g in G.items():
    save(card(COL[k], g), f"textures/item/{k}.png")

# models
def wj(path, obj):
    with open(os.path.join(A, path), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


for name, fr in (("projector", "projector_front"), ("projector_active", "projector_front_active")):
    wj(f"models/block/{name}.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": f"{NS}:block/projector_top", "bottom": f"{NS}:block/projector_bottom", "front": f"{NS}:block/{fr}", "side": f"{NS}:block/projector_side"}})
wj("models/block/field.json", {"parent": "minecraft:block/cube_all", "textures": {"all": {"force_translucent": True, "sprite": f"{NS}:block/field"}}})
variants = {}
for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for act in ("false", "true"):
        v = {"model": f"{NS}:block/projector" + ("_active" if act == "true" else "")}
        if y:
            v["y"] = y
        variants[f"active={act},facing={facing}"] = v
wj("blockstates/projector.json", {"variants": variants})
wj("blockstates/field.json", {"variants": {"": {"model": f"{NS}:block/field"}}})
wj("items/projector.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/projector"}})
for k in G:
    wj(f"models/item/{k}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{k}"}})
    wj(f"items/{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{k}"}})

NAMES = {
    "block.diaithirforcefield.projector": "Skardovel",
    "block.diaithirforcefield.field": "Skarnith",
    "item.diaithirforcefield.range_module": "Uvrelath",
    "item.diaithirforcefield.strength_module": "Torvakhet",
    "item.diaithirforcefield.mob_filter_module": "Veshurel",
    "item.diaithirforcefield.guest_list_module": "Anquith",
    "item.diaithirforcefield.capacitor_module": "Zhoveldra",
    "item.diaithirforcefield.sting_module": "Kreshmael",
}
wj("lang/en_us.json", NAMES)

# recipes
RD = os.path.join(ROOT, "data", NS, "recipe")
os.makedirs(RD, exist_ok=True)


def recipe(name, pattern, key, result, count=1):
    with open(os.path.join(RD, name + ".json"), "w") as f:
        json.dump({"type": "minecraft:crafting_shaped", "category": "misc", "key": key, "pattern": pattern,
                   "result": {"id": f"{NS}:{result}", "count": count}}, f, indent=2)
        f.write("\n")


recipe("projector", ["III", "RDR", "ICI"], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "D": "minecraft:diamond", "C": "minecraft:copper_ingot"}, "projector")
CENTER = {"range_module": "minecraft:ender_pearl", "strength_module": "minecraft:obsidian", "mob_filter_module": "minecraft:spider_eye",
          "guest_list_module": "minecraft:book", "capacitor_module": "minecraft:redstone_block", "sting_module": "minecraft:blaze_powder"}
for k, c in CENTER.items():
    recipe(k, ["III", "ICI", "III"], {"I": "minecraft:iron_ingot", "C": c}, k)
print("ok")
