#!/usr/bin/env python3
"""Writes the 64 px Industrial Factory textures, models, blockstates, item definitions, lang, recipes, age gate and library entries for
diaithir-routing-networks. All art is drawn here from scratch (worn dark steel, rivets, hazard bands, amber lamps); no Factorization
or Factorio art is used or traced."""
import json, os, random
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirrouting"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "models/block", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "recipe"), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "..", "library-sync"), exist_ok=True)

S = 64
STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (30, 31, 34)
RIVET = (130, 134, 140)
YEL = (240, 190, 30)
BLK = (28, 28, 28)
AMBER = (255, 170, 40)
DARK = (14, 17, 21)
rnd = random.Random(61)


def plate():
    im = Image.new("RGBA", (S, S))
    px = im.load()
    for y in range(S):
        for x in range(S):
            base = STEEL[rnd.choice((0, 0, 1, 1, 3))]
            n = rnd.randint(-4, 4)
            px[x, y] = tuple(max(0, min(255, c + n)) for c in base) + (255,)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, S - 1, S - 1], outline=EDGE + (255,), width=2)
    d.rectangle([2, 2, S - 3, S - 3], outline=STEEL[2] + (255,), width=1)
    for cx, cy in ((6, 6), (57, 6), (6, 57), (57, 57)):
        d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=RIVET + (255,), outline=EDGE + (255,))
    return im


def hazard(im, y0, y1):
    d = ImageDraw.Draw(im)
    for y in range(y0, y1):
        for x in range(4, S - 4):
            d.point((x, y), fill=(YEL if ((x + y) // 4) % 2 == 0 else BLK) + (255,))


def save(im, name):
    im.save(os.path.join(A, "textures", "block", name + ".png"))


def lamp(d, cx, cy, col=AMBER):
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=EDGE + (255,))
    d.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], fill=col + (255,))
    d.ellipse([cx - 1, cy - 2, cx + 1, cy], fill=(255, 235, 170, 255))


# Day Barrel: a riveted steel drum, hoops, a window showing the stored stack
bs = plate()
d = ImageDraw.Draw(bs)
for y in (10, 50):
    d.rectangle([3, y, S - 4, y + 4], fill=STEEL[3] + (255,), outline=EDGE + (255,))
d.rectangle([20, 22, 43, 40], fill=DARK + (255,), outline=EDGE + (255,), width=2)
for i in range(3):
    d.line([22, 27 + i * 5, 41, 27 + i * 5], fill=(40, 46, 54, 255))
hazard(bs, 55, 60)
save(bs, "foundry_barrel_side")
bt = plate()
d = ImageDraw.Draw(bt)
d.ellipse([10, 10, 53, 53], fill=STEEL[3] + (255,), outline=EDGE + (255,), width=2)
d.ellipse([18, 18, 45, 45], fill=DARK + (255,), outline=STEEL[2] + (255,), width=2)
d.rectangle([26, 26, 37, 37], fill=YEL + (255,), outline=EDGE + (255,))
save(bt, "foundry_barrel_top")

# Sieve: a mesh grille on the front, a slotted back
sf = plate()
d = ImageDraw.Draw(sf)
d.rectangle([12, 12, 51, 51], fill=DARK + (255,), outline=EDGE + (255,), width=2)
for i in range(14, 50, 5):
    d.line([i, 13, i, 50], fill=STEEL[2] + (255,))
    d.line([13, i, 50, i], fill=STEEL[2] + (255,))
save(sf, "foundry_sieve_front")
ss = plate()
d = ImageDraw.Draw(ss)
d.rectangle([10, 26, 53, 37], fill=DARK + (255,), outline=EDGE + (255,), width=2)
hazard(ss, 52, 58)
lamp(d, 32, 14)
save(ss, "foundry_sieve_side")

# Router: six-way box, one amber lamp and an arrow cross on every face
rt = plate()
d = ImageDraw.Draw(rt)
d.rectangle([12, 12, 51, 51], fill=STEEL[3] + (255,), outline=EDGE + (255,), width=2)
d.line([32, 16, 32, 47], fill=AMBER + (255,), width=3)
d.line([16, 32, 47, 32], fill=AMBER + (255,), width=3)
for (a, b, c) in (((32, 14), (27, 21), (37, 21)), ((32, 49), (27, 42), (37, 42)), ((14, 32), (21, 27), (21, 37)), ((49, 32), (42, 27), (42, 37))):
    d.polygon([a, b, c], fill=AMBER + (255,), outline=EDGE + (255,))
d.ellipse([27, 27, 37, 37], fill=EDGE + (255,))
d.ellipse([29, 29, 35, 35], fill=(255, 235, 170, 255))
save(rt, "foundry_router")


def wj(path, obj):
    with open(os.path.join(A, path), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


wj("models/block/foundry_barrel.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {"top": f"{NS}:block/foundry_barrel_top", "bottom": f"{NS}:block/foundry_barrel_top", "side": f"{NS}:block/foundry_barrel_side"}})
wj("models/block/foundry_sieve.json", {"parent": "minecraft:block/cube", "textures": {
    "north": f"{NS}:block/foundry_sieve_front", "south": f"{NS}:block/foundry_sieve_side", "east": f"{NS}:block/foundry_sieve_side",
    "west": f"{NS}:block/foundry_sieve_side", "up": f"{NS}:block/foundry_sieve_side", "down": f"{NS}:block/foundry_sieve_side",
    "particle": f"{NS}:block/foundry_sieve_side"}})
wj("models/block/foundry_router.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/foundry_router"}})
wj("blockstates/foundry_barrel.json", {"variants": {"": {"model": f"{NS}:block/foundry_barrel"}}})
wj("blockstates/foundry_router.json", {"variants": {"": {"model": f"{NS}:block/foundry_router"}}})
rot = {"north": {}, "east": {"y": 90}, "south": {"y": 180}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
wj("blockstates/foundry_sieve.json", {"variants": {f"facing={k}": {"model": f"{NS}:block/foundry_sieve", **v} for k, v in rot.items()}})
for k in ("foundry_barrel", "foundry_sieve", "foundry_router"):
    wj(f"items/{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{k}"}})

NAMES = {
    "foundry_barrel": ("Day Barrel", "Day Barrel. Holds one kind of item, up to 64 stacks. Use an item on it to store it, use it empty-handed to take a stack (sneak: one item). Pipes and inserters fill it from the top and sides and empty it from the bottom. Contents spill when it is broken. Ported from the Factorization barrel idea (MIT)."),
    "foundry_sieve": ("Sieve", "Sieve. Place it against an inventory and it shows only the items that pass its filter to everything else that touches it, both in and out. Sneak-use with an item adds it to the filter, sneak-use empty-handed clears it. Four pairs: two items of one mod in a pair match that whole mod. A redstone signal inverts it. Ported from the Factorization sieve idea (MIT)."),
    "foundry_router": ("Sorting Router", "Sorting Router. Whatever is pushed into one face leaves by the others. An item goes to the faces whose filter matches it, else to faces with no filter; a face with a filter that does not match never gets it. Sneak-use a face with an item to add it to that face's filter, sneak-use empty-handed to clear it. Feeds splitters and lanes."),
}
wj("lang/en_us.json", {f"block.{NS}.{k}": v[0] for k, v in NAMES.items()})

with open(os.path.join(HERE, "..", "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v[0], "text": v[1]} for k, v in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")

with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 5, "items": [f"{NS}:foundry_barrel", f"{NS}:foundry_sieve", f"{NS}:foundry_router"]}}, f, indent=2)
    f.write("\n")


def shaped(name, pattern, key):
    obj = {"type": "minecraft:crafting_shaped", "key": key, "pattern": pattern, "result": {"id": f"{NS}:{name}", "count": 1}, "category": "misc"}
    with open(os.path.join(D, "recipe", name + ".json"), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


shaped("foundry_barrel", ["III", "ICI", "III"], {"I": "minecraft:iron_ingot", "C": "minecraft:chest"})
shaped("foundry_sieve", ["III", "BHB", "III"], {"I": "minecraft:iron_ingot", "B": "minecraft:iron_bars", "H": "minecraft:hopper"})
shaped("foundry_router", ["IRI", "HDH", "IRI"], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "H": "minecraft:hopper", "D": "minecraft:dropper"})
print("ok")
