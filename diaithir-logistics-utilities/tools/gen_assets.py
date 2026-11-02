#!/usr/bin/env python3
"""Writes the original 64x64 textures, models, blockstates, item definitions, lang, recipes and age gate for diaithir-logistics-utilities.
Everything is drawn here from scratch in the Industrial Factory style: worn dark steel, rivets, hazard bands, a cyan courier lamp."""
import json, os, random
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirlogistics"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "recipe"), exist_ok=True)

S = 64
STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (34, 35, 38)
RIVET = (122, 126, 132)
YEL = (240, 190, 30)
BLK = (30, 30, 30)
CYAN = (90, 220, 240)
rnd = random.Random(23)


def plate():
    im = Image.new("RGBA", (S, S))
    px = im.load()
    for y in range(S):
        for x in range(S):
            px[x, y] = STEEL[rnd.choice((0, 0, 1, 1, 3, 2 if rnd.random() < 0.15 else 0))] + (255,)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, S - 1, S - 1], outline=EDGE + (255,), width=2)
    for cx, cy in ((6, 6), (57, 6), (6, 57), (57, 57)):
        d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=RIVET + (255,), outline=EDGE + (255,))
    return im


def hazard(im, y0, y1):
    px = im.load()
    for y in range(y0, y1):
        for x in range(2, S - 2):
            px[x, y] = (YEL if ((x + y) // 6) % 2 == 0 else BLK) + (255,)


def lamp(im, cx, cy, r):
    d = ImageDraw.Draw(im)
    d.ellipse([cx - r - 2, cy - r - 2, cx + r + 2, cy + r + 2], fill=EDGE + (255,))
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=CYAN + (255,))
    d.ellipse([cx - r // 2, cy - r // 2, cx, cy], fill=(220, 250, 255, 255))


def save(im, sub, name):
    im.save(os.path.join(A, "textures", sub, name + ".png"))


side = plate()
hazard(side, 44, 54)
save(side, "block", "foundry_depot_side")
top = plate()
lamp(top, 32, 32, 12)
d = ImageDraw.Draw(top)
d.rectangle([10, 10, 53, 53], outline=EDGE + (255,), width=2)
save(top, "block", "foundry_depot_top")
bottom = plate()
save(bottom, "block", "foundry_depot_bottom")

ghost = Image.new("RGBA", (S, S), (90, 220, 240, 70))
gd = ImageDraw.Draw(ghost)
gd.rectangle([0, 0, S - 1, S - 1], outline=(150, 240, 255, 200), width=2)
for k in range(8, S, 8):
    gd.line([(k, 2), (k, S - 3)], fill=(150, 240, 255, 90))
    gd.line([(2, k), (S - 3, k)], fill=(150, 240, 255, 90))
save(ghost, "block", "foundry_ghost")

wand = Image.new("RGBA", (S, S), (0, 0, 0, 0))
wd = ImageDraw.Draw(wand)
wd.line([(14, 52), (44, 22)], fill=STEEL[2] + (255,), width=6)
wd.line([(14, 52), (44, 22)], fill=EDGE + (255,), width=2)
wd.rectangle([8, 46, 20, 58], fill=YEL + (255,), outline=BLK + (255,), width=2)
wd.polygon([(44, 22), (52, 14), (56, 18), (48, 26)], fill=EDGE + (255,))
wd.ellipse([44, 6, 60, 22], fill=CYAN + (255,), outline=EDGE + (255,), width=2)
wd.ellipse([48, 9, 53, 14], fill=(220, 250, 255, 255))
save(wand, "item", "foundry_plan_wand")


def w(path, obj):
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


w(os.path.join(A, "models/block/foundry_depot.json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": f"{NS}:block/foundry_depot_top", "bottom": f"{NS}:block/foundry_depot_bottom", "side": f"{NS}:block/foundry_depot_side"}})
w(os.path.join(A, "models/block/foundry_ghost.json"), {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/foundry_ghost"}})
w(os.path.join(A, "models/item/foundry_plan_wand.json"), {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/foundry_plan_wand"}})
for b in ("foundry_depot", "foundry_ghost"):
    w(os.path.join(A, f"blockstates/{b}.json"), {"variants": {"": {"model": f"{NS}:block/{b}"}}})
w(os.path.join(A, "items/foundry_depot.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:block/foundry_depot"}})
w(os.path.join(A, "items/foundry_plan_wand.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/foundry_plan_wand"}})
w(os.path.join(A, "lang/en_us.json"), {
    "block.%s.foundry_depot" % NS: "Courier Depot",
    "block.%s.foundry_ghost" % NS: "Ghost Block",
    "item.%s.foundry_plan_wand" % NS: "Plan Wand"})
w(os.path.join(D, "recipe/foundry_depot.json"), {"type": "minecraft:crafting_shaped", "key": {
    "I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "H": "minecraft:hopper", "B": "minecraft:chest"},
    "pattern": ["ICI", "BHB", "III"], "result": {"id": f"{NS}:foundry_depot", "count": 1}, "category": "misc"})
w(os.path.join(D, "recipe/foundry_plan_wand.json"), {"type": "minecraft:crafting_shaped", "key": {
    "A": "minecraft:amethyst_shard", "I": "minecraft:iron_ingot", "S": "minecraft:stick"},
    "pattern": ["  A", " I ", "S  "], "result": {"id": f"{NS}:foundry_plan_wand", "count": 1}, "category": "misc"})
w(os.path.join(D, "age_gate.json"), {"tier1": {"age": 6, "items": [f"{NS}:foundry_depot", f"{NS}:foundry_plan_wand"]}})
