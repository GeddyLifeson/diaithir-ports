#!/usr/bin/env python3
"""Writes the original 16x16 textures, models, blockstates, item definitions, lang, recipes, age gate and library entries for
diaithir-conduits. Everything is drawn here from scratch: dark worn steel, rivets, hazard bands and glowing rings (a factory look)."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirconduits"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "recipe"), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)

STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (34, 35, 38)
RIVET = (122, 126, 132)
YEL = (240, 190, 30)
BLK = (30, 30, 30)
CYAN = (70, 210, 235)
VIOLET = (190, 90, 235)
WATER = (60, 120, 220)
SPARK = (250, 210, 40)
rnd = random.Random(23)


def plate():
    im = Image.new("RGBA", (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = STEEL[rnd.choice((0, 0, 1, 1, 3))] + (255,)
    for i in range(16):
        for j in (0, 15):
            px[i, j] = EDGE + (255,)
            px[j, i] = EDGE + (255,)
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[cx, cy] = RIVET + (255,)
    return im


def hazard(im, y0, y1):
    px = im.load()
    for y in range(y0, y1):
        for x in range(1, 15):
            px[x, y] = (YEL if ((x + y) // 2) % 2 == 0 else BLK) + (255,)


def save(im, folder, name):
    im.save(os.path.join(A, "textures", folder, name + ".png"))


def ring(im, color, r_out, r_in):
    px = im.load()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5)) if False else ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if r_in <= d <= r_out:
                px[x, y] = color + (255,)
            elif d < r_in:
                px[x, y] = (14, 18, 24, 255)
    # bright core
    for y in range(6, 10):
        for x in range(6, 10):
            px[x, y] = tuple(min(255, c + 70) for c in color) + (255,)


for tier, col in ((1, CYAN), (2, VIOLET)):
    side = plate()
    ring(side, col, 5.6, 3.4)
    hazard(side, 13, 15)
    save(side, "block", f"conduit_{tier}_side")
    top = plate()
    px = top.load()
    for y in range(4, 12):
        for x in range(4, 12):
            px[x, y] = EDGE + (255,) if (x in (4, 11) or y in (4, 11)) else col + (255,)
    for i in range(tier):
        px[2 + i * 2, 13] = col + (255,)
        px[3 + i * 2, 13] = col + (255,)
    save(top, "block", f"conduit_{tier}_top")

# cells: a glass window over the contents, one steel box each
fl = plate()
px = fl.load()
for y in range(3, 13):
    for x in range(4, 12):
        px[x, y] = (18, 22, 26, 255) if y < 6 else WATER + (255,)
for x in range(4, 12):
    px[x, 6] = (170, 200, 240, 255)
save(fl, "block", "fluid_cell_side")
bat = plate()
px = bat.load()
for y in range(3, 13):
    for x in range(4, 12):
        px[x, y] = (18, 22, 26, 255)
for (x, y) in ((8, 4), (7, 5), (7, 6), (6, 7), (7, 7), (8, 7), (8, 8), (9, 8), (8, 9), (8, 10), (7, 11)):
    px[x, y] = SPARK + (255,)
save(bat, "block", "energy_cell_side")
for k, col in (("fluid_cell", WATER), ("energy_cell", SPARK)):
    t = plate()
    px = t.load()
    for i in range(4, 12):
        for j in (4, 11):
            px[i, j] = col + (255,)
            px[j, i] = col + (255,)
    save(t, "block", k + "_top")

# the tuner: a steel handle and a glowing fork
tn = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = tn.load()
for i in range(3, 13):
    px[i, 15 - i] = STEEL[2] + (255,)
    px[i, 14 - i + (1 if i > 0 else 0)] = STEEL[0] + (255,)
for (x, y) in ((11, 1), (12, 1), (13, 2), (14, 3), (14, 4), (12, 2), (13, 3)):
    px[x, y] = CYAN + (255,)
px[13, 1] = (230, 255, 255, 255)
save(tn, "item", "tuner")


def wj(path, obj):
    with open(os.path.join(A, path), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


for k in ("conduit_1", "conduit_2", "fluid_cell", "energy_cell"):
    side = f"{NS}:block/{k}_side"
    top = f"{NS}:block/{k}_top" if not k.startswith("conduit") else f"{NS}:block/{k}_top"
    wj(f"models/block/{k}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {"top": top, "bottom": top, "side": side}})
    wj(f"blockstates/{k}.json", {"variants": {"": {"model": f"{NS}:block/{k}"}}})
    wj(f"items/{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{k}"}})
wj("models/item/tuner.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/tuner"}})
wj("items/tuner.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/tuner"}})

NAMES = {
    "block.conduit_1": ("conduit_1", "Vaurelith Conduit", "Conduit node, tier 1. Set its channel with the tuner. Every node on that channel trades items, fluids and FE, in any dimension. Sneak with an empty hand to step a face: off, send, receive. Send pulls from the block beside it, receive pushes into it. Age VI."),
    "block.conduit_2": ("conduit_2", "Ordhevane Conduit", "Conduit node, tier 2. Same as the tier 1 node with eight times the bandwidth per run. Age VII."),
    "block.fluid_cell": ("fluid_cell", "Sulmerith Cell", "Fluid cell. A plain tank that every face can fill and empty, handy at the end of a conduit. Use it to read the contents. Age VI."),
    "block.energy_cell": ("energy_cell", "Kaldruvane Cell", "Energy cell. A plain FE battery that every face can charge and drain, handy at the end of a conduit. Use it to read the contents. Age VI."),
    "item.tuner": ("tuner", "Thorvaneth Tuner", "Tuner. Use it on a conduit node to put it on your channel 1, then again to step up one channel; sneak to step down. Only the node's owner can tune it. Others join your channel only if you run /diaithirconduits trust. Age VI."),
}
lang = {f"{k.split('.')[0]}.{NS}.{v[0]}": v[1] for k, v in NAMES.items()}
wj("lang/en_us.json", lang)

with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{v[0]}", "name": v[1], "text": v[2]} for v in NAMES.values()], f, indent=1, ensure_ascii=False)
    f.write("\n")

with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 6, "items": [f"{NS}:conduit_1", f"{NS}:fluid_cell", f"{NS}:energy_cell", f"{NS}:tuner"]},
               "tier2": {"age": 7, "items": [f"{NS}:conduit_2"]}}, f, indent=2)
    f.write("\n")


def recipe(name, obj, count=1):
    obj["result"] = {"id": f"{NS}:{name}", "count": count}
    obj["category"] = "misc"
    with open(os.path.join(D, "recipe", name + ".json"), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def shaped(name, pattern, key, count=1):
    recipe(name, {"type": "minecraft:crafting_shaped", "key": key, "pattern": pattern}, count)


shaped("conduit_1", ["IRI", "EDE", "IRI"], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "E": "minecraft:ender_pearl", "D": "minecraft:diamond"})
shaped("conduit_2", ["DED", "ECE", "DED"], {"D": "minecraft:diamond", "E": "minecraft:ender_eye", "C": f"{NS}:conduit_1"})
shaped("fluid_cell", ["IGI", "GBG", "IGI"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass", "B": "minecraft:bucket"})
shaped("energy_cell", ["ICI", "CRC", "ICI"], {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "R": "minecraft:redstone_block"})
shaped("tuner", ["E", "I", "I"], {"E": "minecraft:ender_pearl", "I": "minecraft:iron_ingot"})
print("ok")
