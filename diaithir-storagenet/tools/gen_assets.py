#!/usr/bin/env python3
"""Writes the original 16x16 textures, models, blockstates, item definitions, lang, recipes, age gate and library entries for
diaithir-storagenet. Everything is drawn here from scratch: dark worn steel, rivets, coloured status bands (a factory look)."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirstoragenet"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "models/block", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "recipe"), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)

STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (34, 35, 38)
RIVET = (122, 126, 132)
YEL = (240, 190, 30)
BLK = (30, 30, 30)
rnd = random.Random(11)

COL = {"controller": (230, 120, 30), "cable": (200, 200, 70), "import_cable": (80, 200, 110), "export_cable": (80, 150, 230),
       "filter_cable": (200, 90, 220), "terminal": (80, 200, 230), "crafting_terminal": (240, 190, 30)}


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


def band(im, y0, y1, color):
    px = im.load()
    for y in range(y0, y1):
        for x in range(1, 15):
            px[x, y] = color + (255,)


def hazard(im, y0, y1):
    px = im.load()
    for y in range(y0, y1):
        for x in range(1, 15):
            px[x, y] = (YEL if ((x + y) // 2) % 2 == 0 else BLK) + (255,)


def save(im, name):
    im.save(os.path.join(A, "textures/block", name + ".png"))


def screen(color, lines):
    im = plate()
    px = im.load()
    for y in range(3, 12):
        for x in range(3, 13):
            px[x, y] = (18, 22, 26, 255)
    for y in range(4, 11, 2):
        for x in range(4, 12 - (y % 3) * 2):
            px[x, y] = color + (255,)
    if lines:
        for x in range(4, 12):
            px[x, 11] = color + (255,)
    return im


# controller: a squat reactor-style box, orange core window on every side
side = plate()
px = side.load()
for y in range(5, 11):
    for x in range(5, 11):
        d = max(abs(x - 7.5), abs(y - 7.5))
        px[x, y] = ((255, 220, 140) if d < 1.5 else COL["controller"] if d < 2.6 else EDGE) + (255,)
hazard(side, 12, 14)
save(side, "controller_side")
top = plate()
px = top.load()
for y in range(4, 12):
    for x in range(4, 12):
        px[x, y] = EDGE + (255,) if (x in (4, 11) or y in (4, 11)) else COL["controller"] + (255,)
save(top, "controller_top")

# cables: a bright pressed strip on steel, one colour per kind
for k in ("cable", "import_cable", "export_cable", "filter_cable"):
    im = plate()
    px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = STEEL[(x + y) % 2] + (255,)
    for x in range(16):
        px[x, 7] = COL[k] + (255,)
        px[x, 8] = COL[k] + (255,)
    for x in range(0, 16, 4):
        px[x, 6] = EDGE + (255,)
        px[x, 9] = EDGE + (255,)
    save(im, k)

# terminals
for k, lines in (("terminal", True), ("crafting_terminal", False)):
    save(screen(COL[k], lines), k + "_front")
    s = plate()
    band(s, 12, 14, COL[k])
    save(s, k + "_side")
    t = plate()
    if k == "crafting_terminal":
        p = t.load()
        for i in range(4, 12):
            for j in (4, 8, 11):
                p[i, j] = EDGE + (255,)
                p[j, i] = EDGE + (255,)
    save(t, k + "_top")


def wj(path, obj):
    with open(os.path.join(A, path), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


# models
wj("models/block/controller.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": f"{NS}:block/controller_top", "bottom": f"{NS}:block/controller_top", "side": f"{NS}:block/controller_side"}})
wj("blockstates/controller.json", {"variants": {"": {"model": f"{NS}:block/controller"}}})
wj("items/controller.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/controller"}})
for k in ("terminal", "crafting_terminal"):
    wj(f"models/block/{k}.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": f"{NS}:block/{k}_top", "bottom": f"{NS}:block/{k}_top", "front": f"{NS}:block/{k}_front", "side": f"{NS}:block/{k}_side"}})
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        v = {"model": f"{NS}:block/{k}"}
        if y:
            v["y"] = y
        variants[f"facing={facing}"] = v
    wj(f"blockstates/{k}.json", {"variants": variants})
    wj(f"items/{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{k}"}})

FACE = lambda: {f: {"texture": "#all"} for f in ("north", "south", "east", "west", "up", "down")}
wj("models/block/cable_core.json", {"textures": {"particle": "#all"}, "elements": [{"from": [6, 6, 6], "to": [10, 10, 10], "faces": FACE()}]})
wj("models/block/cable_arm.json", {"textures": {"particle": "#all"}, "elements": [{"from": [6, 6, 0], "to": [10, 10, 6], "faces": FACE()}]})
wj("models/block/cable_inventory.json", {"parent": "minecraft:block/block", "textures": {"particle": "#all"}, "elements": [
    {"from": [6, 6, 6], "to": [10, 10, 10], "faces": FACE()},
    {"from": [6, 6, 0], "to": [10, 10, 6], "faces": FACE()},
    {"from": [6, 6, 10], "to": [10, 10, 16], "faces": FACE()}]})
ROT = {"north": {}, "east": {"y": 90}, "south": {"y": 180}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
for k in ("cable", "import_cable", "export_cable", "filter_cable"):
    tex = {"all": f"{NS}:block/{k}"}
    for part in ("core", "arm", "inventory"):
        wj(f"models/block/{k}_{part}.json", {"parent": f"{NS}:block/cable_{part}", "textures": tex})
    multipart = [{"apply": {"model": f"{NS}:block/{k}_core"}}]
    for d, r in ROT.items():
        multipart.append({"when": {d: "true"}, "apply": dict({"model": f"{NS}:block/{k}_arm"}, **r)})
    wj(f"blockstates/{k}.json", {"multipart": multipart})
    wj(f"items/{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{k}_inventory"}})

NAMES = {
    "controller": ("Vorkaneth", "Master block. Every cable run that touches it forms one network. Use bare-handed to read the network's size and item count. Only one controller per network. Age VI."),
    "cable": ("Ilthuvar", "Cable. Joins itself to other network blocks and to chests, barrels and other inventories, which become the network's storage. Age V."),
    "import_cable": ("Draskovel", "Import cable. Every few ticks it pulls items from the inventories touching it into the network. Use an item on it to set a filter, a stick to flip whitelist and blacklist, sneak with an empty hand to clear. Inventories touching it are not used as storage. Age V."),
    "export_cable": ("Ekthurmael", "Export cable. Every few ticks it pushes items from the network into the inventories touching it. With a whitelist filter it sends only those items. Inventories touching it are not used as storage. Age V."),
    "filter_cable": ("Pelquanor", "Filter cable. Stops the network storing anything but whitelisted items (or anything on a blacklist) in the inventories touching it. Whitelisted items are stored there first. Age V."),
    "terminal": ("Zhaldorith", "Access terminal, Age VI. Opens a grid of everything in the network. Left click takes a stack, right click one, shift click sends it to your inventory; click with an item on the cursor or shift click your own items to store them. Sort by name, count or mod, and search with /diaithirstoragenet search text (or @mod)."),
    "crafting_terminal": ("Quenvarosh", "Crafting terminal, Age VI. A crafting table that refills its grid from the whole network: when a grid slot runs empty after a craft it takes one more of that item from storage. Place or hold the first items yourself."),
}
lang = {f"block.{NS}.{k}": v[0] for k, v in NAMES.items()}
wj("lang/en_us.json", lang)

with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v[0], "text": v[1]} for k, v in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")

with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 5, "items": [f"{NS}:cable", f"{NS}:import_cable", f"{NS}:export_cable", f"{NS}:filter_cable"]},
               "tier2": {"age": 6, "items": [f"{NS}:controller", f"{NS}:terminal", f"{NS}:crafting_terminal"]}}, f, indent=2)
    f.write("\n")


def recipe(name, obj, count=1):
    obj["result"] = {"id": f"{NS}:{name}", "count": count}
    obj["category"] = "misc"
    with open(os.path.join(D, "recipe", name + ".json"), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def shaped(name, pattern, key, count=1):
    recipe(name, {"type": "minecraft:crafting_shaped", "key": key, "pattern": pattern}, count)


def shapeless(name, ings):
    recipe(name, {"type": "minecraft:crafting_shapeless", "ingredients": ings})


shaped("cable", ["III", "CCC", "III"], {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot"}, 6)
shaped("controller", ["IRI", "DCD", "IRI"], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "D": "minecraft:diamond", "C": "minecraft:chest"})
shapeless("import_cable", [f"{NS}:cable", "minecraft:hopper"])
shapeless("export_cable", [f"{NS}:cable", "minecraft:dropper"])
shapeless("filter_cable", [f"{NS}:cable", "minecraft:comparator"])
shaped("terminal", ["IGI", "GCG", "IRI"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass_pane", "C": f"{NS}:cable", "R": "minecraft:redstone"})
shapeless("crafting_terminal", [f"{NS}:terminal", "minecraft:crafting_table"])
print("ok")
