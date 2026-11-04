#!/usr/bin/env python3
"""Writes the original 16x16 textures, models, blockstates, item definitions, lang, recipes, sample package definitions, age gate and
library entry for the Package Mod port. Everything is drawn here from scratch in the Industrial Factory look (worn steel, rivets, coloured status bands).
No texture or model from The Package Mod is used."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirautomationcrafting"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
for d in ("recipe", "package_defs"):
    os.makedirs(os.path.join(D, d), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)

STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (34, 35, 38)
RIVET = (122, 126, 132)
YEL = (240, 190, 30)
BLK = (30, 30, 30)
rnd = random.Random(23)
COL = {"researcher": (90, 200, 230), "packager": (240, 190, 30), "unpackager": (230, 120, 30)}


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


def fill(im, x0, y0, x1, y1, c):
    px = im.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = c + (255,)


def save(im, kind, name):
    im.save(os.path.join(A, "textures", kind, name + ".png"))


def wj(path, obj):
    with open(os.path.join(A, path), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


for k, c in COL.items():
    side = plate()
    fill(side, 3, 11, 13, 13, c)
    save(side, "block", k + "_side")
    top = plate()
    if k == "researcher":      # a lens window
        fill(top, 5, 5, 11, 11, EDGE)
        fill(top, 6, 6, 10, 10, c)
    elif k == "packager":      # a crate lid, arrows in
        fill(top, 4, 4, 12, 12, EDGE)
        fill(top, 5, 5, 11, 11, STEEL[1])
        fill(top, 7, 5, 9, 11, c)
        fill(top, 5, 7, 11, 9, c)
    else:                      # a hatch, arrows out
        fill(top, 4, 4, 12, 12, EDGE)
        for i in range(8):
            for j in range(8):
                if (i + j) % 4 in (0, 1):
                    top.load()[4 + i, 4 + j] = c + (255,)
    save(top, "block", k + "_top")
    wj(f"models/block/pkgmod_{k}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": f"{NS}:block/{k}_top", "bottom": f"{NS}:block/{k}_top", "side": f"{NS}:block/{k}_side"}})
    wj(f"blockstates/pkgmod_{k}.json", {"variants": {"": {"model": f"{NS}:block/pkgmod_{k}"}}})
    wj(f"items/pkgmod_{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/pkgmod_{k}"}})

# crate item: steel box with a yellow strap
crate = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
fill(crate, 2, 4, 14, 13, STEEL[1])
fill(crate, 2, 4, 14, 5, EDGE)
fill(crate, 2, 12, 14, 13, EDGE)
fill(crate, 7, 4, 9, 13, YEL)
for x, y in ((3, 6), (12, 6), (3, 11), (12, 11)):
    crate.load()[x, y] = RIVET + (255,)
save(crate, "item", "pkgmod_package")
# reader item: a handheld slate with a blue screen
rd = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
fill(rd, 4, 2, 12, 14, STEEL[3])
fill(rd, 5, 3, 11, 9, (18, 22, 26))
fill(rd, 6, 4, 10, 5, COL["researcher"])
fill(rd, 6, 6, 9, 7, COL["researcher"])
fill(rd, 6, 11, 10, 12, YEL)
save(rd, "item", "pkgmod_reader")
for n in ("pkgmod_package", "pkgmod_reader"):
    wj(f"models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}"}})
    wj(f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})

NAMES = {
    "block.pkgmod_researcher": ("Researcher", "Put in the items and packages a package needs and it is researched for the whole server. Pick what to research with a Package Reader. Hoppers and pipes work; by hand, use items on it. Age V."),
    "block.pkgmod_packager": ("Packager", "Pick a researched package with a Package Reader, feed it the inputs, and it makes a package crate. Take crates out empty-handed or with a hopper. Age V."),
    "block.pkgmod_unpackager": ("Unpackager", "Put a package crate in and it hands back the goods inside, many at once. Take them empty-handed or with a hopper. Age V."),
    "item.pkgmod_package": ("Package", "A sealed crate made by a Packager. Unpack it in an Unpackager."),
    "item.pkgmod_reader": ("Package Reader", "Use on a bench to pick the next package it can work on. Sneak and use to clear the bench and get its items back."),
}
wj("lang/en_us.json", {f"{k.split('.')[0]}.{NS}.{k.split('.')[1]}": v[0] for k, v in NAMES.items()})
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k.split('.')[1]}", "name": v[0], "text": v[1]} for k, v in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")
with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 5, "items": [f"{NS}:pkgmod_researcher", f"{NS}:pkgmod_packager", f"{NS}:pkgmod_unpackager", f"{NS}:pkgmod_reader"]}}, f, indent=2)
    f.write("\n")


def recipe(name, obj):
    obj["result"] = {"id": f"{NS}:{name}", "count": 1}
    obj["category"] = "misc"
    with open(os.path.join(D, "recipe", name + ".json"), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def shaped(name, pattern, key):
    recipe(name, {"type": "minecraft:crafting_shaped", "key": key, "pattern": pattern})


shaped("pkgmod_researcher", ["IGI", "RCR", "III"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass", "R": "minecraft:redstone", "C": "minecraft:crafting_table"})
shaped("pkgmod_packager", ["ICI", "RHR", "III"], {"I": "minecraft:iron_ingot", "C": "minecraft:chest", "R": "minecraft:redstone", "H": "minecraft:hopper"})
shaped("pkgmod_unpackager", ["IHI", "RCR", "III"], {"I": "minecraft:iron_ingot", "C": "minecraft:chest", "R": "minecraft:redstone", "H": "minecraft:hopper"})
shaped("pkgmod_reader", ["GGG", "IRI", "III"], {"G": "minecraft:glass_pane", "I": "minecraft:iron_ingot", "R": "minecraft:redstone"})


def pkg(name, label, result, inputs, items, prereqs=(), packs=()):
    obj = {"name": label, "result": {"item": result[0], "count": result[1]},
           "inputs": [{"item": i, "count": n} for i, n in inputs],
           "research": {"items": [{"item": i, "count": n} for i, n in items],
                        "packs": [{"item": p, "count": n} for p, n in packs], "prereqs": list(prereqs)}}
    with open(os.path.join(D, "package_defs", name + ".json"), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


# Sample packages only; modpack makers add their own as datapack files (same job as the original's packageadder.json).
pkg("torches", "Torch", ("minecraft:torch", 256), [("minecraft:coal", 64), ("minecraft:stick", 64)], [("minecraft:redstone", 8)])
pkg("iron_blocks", "Iron block", ("minecraft:iron_block", 64), [("minecraft:iron_ingot", 576)], [("minecraft:iron_ingot", 16)], prereqs=[f"{NS}:torches"])
print("ok")
