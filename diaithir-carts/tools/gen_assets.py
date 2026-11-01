#!/usr/bin/env python3
"""Writes the original 16x16 textures, models, blockstates, item definitions, lang, recipes, loot, tags, age gate and library entries for
diaithir-carts. Everything is drawn here from scratch: worn concrete sleepers and coloured steel rails, a factory-yard look."""
import json, math, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithircarts"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
for d in ("recipe", "loot_table/blocks"):
    os.makedirs(os.path.join(D, d), exist_ok=True)
os.makedirs(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable"), exist_ok=True)
os.makedirs(os.path.join(ROOT, "data", "minecraft", "tags", "item"), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)
rnd = random.Random(7)

YEL = (240, 190, 30)
BLK = (28, 28, 30)
PAL = {
    "bronze": {"rail": [(150, 88, 40), (196, 120, 60), (226, 150, 80)], "sleeper": [(70, 66, 62), (58, 55, 52)], "bolt": (230, 200, 140), "mid": (196, 120, 60), "on": (255, 170, 60)},
    "steel": {"rail": [(120, 130, 142), (170, 180, 192), (205, 214, 224)], "sleeper": [(50, 54, 60), (40, 44, 50)], "bolt": (110, 150, 210), "mid": (60, 110, 170), "on": (90, 190, 255)},
    "fast": {"rail": [(160, 168, 178), (225, 230, 236), (250, 252, 255)], "sleeper": [(36, 38, 42), (28, 30, 34)], "bolt": (210, 60, 50), "mid": (210, 50, 40), "on": (255, 90, 70)},
}


def rail_image(pal, corner, mid=None, hazard=False):
    """mid: None, 'dash' (dashes between the rails), 'strip' (a solid strip), or a colour tuple for the strip."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for y in range(16):
        for x in range(16):
            cx, cy = x + 0.5, y + 0.5
            if corner:
                r = math.hypot(16 - cx, 16 - cy)
                th = math.atan2(16 - cy, 16 - cx)  # 0 along the bottom edge .. pi/2 along the right edge
                lat = 16 - r
                alo = th * 8
                if r > 16.5 or r < 0.5:
                    continue
            else:
                lat, alo = cx, cy
            hw = 1.15 if corner else 1.0
            is_rail = abs(lat - 5) <= hw or abs(lat - 11) <= hw
            sleeper = 2.0 <= lat <= 14.0 and 1 <= (alo % 4) < 3 and not (corner and (r < 2 or r > 14.5))
            c = None
            if sleeper:
                c = pal["sleeper"][rnd.randrange(2)]
                if hazard and not corner and (lat < 3.3 or lat > 12.7):
                    c = YEL if int(alo + lat) % 2 == 0 else BLK
            if mid is not None and abs(lat - 8) <= 1.0 and not is_rail:
                if mid == "dash":
                    if (alo % 4) < 2:
                        c = pal["mid"]
                else:
                    c = mid
            if is_rail:
                c = pal["rail"][0 if abs(lat - 5) > 0.45 and abs(lat - 11) > 0.45 else (2 if rnd.random() < 0.2 else 1)]
                if sleeper and abs(lat - 5) < 0.7 and (alo % 4) > 1.5 and (alo % 4) < 2.5:
                    c = pal["bolt"]
            if c is not None:
                px[x, y] = tuple(c) + (255,)
    return im


def save(im, path):
    im.save(os.path.join(A, path))


for t, pal in PAL.items():
    save(rail_image(pal, False, "dash" if t == "steel" else None, hazard=(t == "fast")), f"textures/block/{t}_rail.png")
    save(rail_image(pal, True, "dash" if t == "steel" else None, hazard=(t == "fast")), f"textures/block/{t}_rail_corner.png")
    save(rail_image(pal, False, (70, 72, 76), hazard=(t == "fast")), f"textures/block/{t}_powered_rail.png")
    save(rail_image(pal, False, pal["on"], hazard=(t == "fast")), f"textures/block/{t}_powered_rail_on.png")

# station: steel rails, a hazard bar across the middle and a red or green signal square
for on in (False, True):
    im = rail_image(PAL["steel"], False, None)
    px = im.load()
    for y in range(6, 10):
        for x in range(2, 14):
            px[x, y] = (YEL if ((x + y) // 2) % 2 == 0 else BLK) + (255,)
    sig = (60, 220, 90) if on else (220, 50, 40)
    for y in range(7, 9):
        for x in range(7, 9):
            px[x, y] = sig + (255,)
    save(im, f"textures/block/station_rail{'_on' if on else ''}.png")

# items: a chain of oval links on a diagonal, and cutters (red handles, steel jaws)
chain = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
cp = chain.load()
OUT = (38, 40, 44)
LINK = [(120, 130, 142), (180, 190, 202)]
for i, (cx, cy) in enumerate(((3, 12), (6, 9), (9, 6), (12, 3))):
    wide = i % 2 == 0
    pts = [(cx + dx, cy + dy) for dx in range(-2, 3) for dy in range(-2, 3)]
    for (x, y) in pts:
        dx, dy = x - cx, y - cy
        ring = (dx * dx / (4.0 if wide else 1.5) + dy * dy / (1.5 if wide else 4.0))
        if 0.6 <= ring <= 1.4 and 0 <= x < 16 and 0 <= y < 16:
            cp[x, y] = LINK[(x + y) % 2] + (255,)
        elif 1.4 < ring <= 2.2 and 0 <= x < 16 and 0 <= y < 16 and cp[x, y][3] == 0:
            cp[x, y] = OUT + (255,)
save(chain, "textures/item/coupling_chain.png")

cut = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
cu = cut.load()
for k in range(9):  # two blades crossing
    for (x, y, col) in ((13 - k, 2 + k - 1, (190, 198, 210)), (12 - k, 2 + k + 1, (150, 158, 170))):
        if 0 <= x < 16 and 0 <= y < 16:
            cu[x, y] = col + (255,)
for k in range(4, 9):  # the handles
    for (x, y) in ((k - 4 + 1, k + 4 - 1), (k - 4, k + 4)):
        if 0 <= x < 16 and 0 <= y < 16:
            cu[x, y] = (200, 50, 40, 255)
for (x, y) in ((8, 8), (9, 8), (8, 9)):
    cu[x, y] = (255, 220, 120, 255)
for (x, y) in ((13, 1), (14, 2), (14, 1)):
    cu[x, y] = (230, 236, 244, 255)
save(cut, "textures/item/chain_cutters.png")


def wj(path, obj, base=A):
    full = os.path.join(base, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def block_model(name, parent, tex):
    wj(f"models/block/{name}.json", {"parent": parent, "textures": {"rail": f"{NS}:block/{tex}"}})


FLAT, CURVE = "minecraft:block/rail_flat", "minecraft:block/rail_curved"
NE, SW = "minecraft:block/template_rail_raised_ne", "minecraft:block/template_rail_raised_sw"
SLOPES = {"ascending_east": ("ne", 90), "ascending_north": ("ne", 0), "ascending_south": ("sw", 0), "ascending_west": ("sw", 90)}


def v(model, y=0):
    d = {"model": f"{NS}:block/{model}"}
    if y:
        d["y"] = y
    return d


def item_def(name, model_name, tex):
    wj(f"items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
    wj(f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:{tex}"}})


for t in PAL:
    # plain rail
    block_model(f"{t}_rail", FLAT, f"{t}_rail")
    block_model(f"{t}_rail_corner", CURVE, f"{t}_rail_corner")
    block_model(f"{t}_rail_raised_ne", NE, f"{t}_rail")
    block_model(f"{t}_rail_raised_sw", SW, f"{t}_rail")
    var = {"shape=north_south": v(f"{t}_rail"), "shape=east_west": v(f"{t}_rail", 90),
           "shape=south_east": v(f"{t}_rail_corner"), "shape=south_west": v(f"{t}_rail_corner", 90),
           "shape=north_west": v(f"{t}_rail_corner", 180), "shape=north_east": v(f"{t}_rail_corner", 270)}
    for s, (k, y) in SLOPES.items():
        var[f"shape={s}"] = v(f"{t}_rail_raised_{k}", y)
    wj(f"blockstates/{t}_rail.json", {"variants": var})
    item_def(f"{t}_rail", None, f"block/{t}_rail")


def powered(name, off_tex, on_tex):
    var = {}
    for on, tex in ((False, off_tex), (True, on_tex)):
        sfx = "_on" if on else ""
        block_model(f"{name}{sfx}", FLAT, tex)
        block_model(f"{name}{sfx}_raised_ne", NE, tex)
        block_model(f"{name}{sfx}_raised_sw", SW, tex)
        p = "powered=" + ("true" if on else "false")
        var[f"{p},shape=north_south"] = v(f"{name}{sfx}")
        var[f"{p},shape=east_west"] = v(f"{name}{sfx}", 90)
        for s, (k, y) in SLOPES.items():
            var[f"{p},shape={s}"] = v(f"{name}{sfx}_raised_{k}", y)
    wj(f"blockstates/{name}.json", {"variants": var})
    item_def(name, None, f"block/{off_tex}")


for t in PAL:
    powered(f"{t}_powered_rail", f"{t}_powered_rail", f"{t}_powered_rail_on")
powered("station_rail", "station_rail", "station_rail_on")
item_def("coupling_chain", None, "item/coupling_chain")
item_def("chain_cutters", None, "item/chain_cutters")

NAMES = {
    "bronze_rail": ("Brundarok", "Bronze rail. Carts ride it up to 0.6 blocks per tick, half again vanilla speed. Curves, slopes and junctions work like a normal rail. Age III."),
    "bronze_powered_rail": ("Brundavoss", "Bronze powered rail. Boosts a cart when powered, stops it when not. Caps at 0.6 blocks per tick. Age III."),
    "steel_rail": ("Sorvanquil", "Steel rail. Carts ride it up to 0.9 blocks per tick, more than twice vanilla speed. Age V."),
    "steel_powered_rail": ("Sorvaleth", "Steel powered rail. A stronger boost than bronze, same stop when unpowered. Caps at 0.9 blocks per tick. Age V."),
    "fast_rail": ("Zephrakund", "High-speed rail. Carts ride it up to 1.2 blocks per tick, three times vanilla speed. Keep curves gentle. Age VI."),
    "fast_powered_rail": ("Zephravoss", "High-speed powered rail. The strongest boost, caps at 1.2 blocks per tick. Age VI."),
    "station_rail": ("Haldurmek", "Station rail. Stops a cart that rolls onto it and holds it until the rail is powered, or until its timer ends. Use it bare-handed to cycle the timer: signal only, 2, 5, 15 or 60 seconds. A coupled follower follows its leader instead. Age V."),
    "coupling_chain": ("Quelvarith", "Coupling chain. Use it on one cart, then on a second: the first leads, the second follows at a fixed spacing and is pulled along, so a row of carts runs as a train. A link breaks if the carts get more than 8 blocks apart. Age V."),
    "chain_cutters": ("Snithrekel", "Chain cutters. Sneak and use on a cart to cut its link and get the chain back. Age V."),
}
lang = {}
for k, v_ in NAMES.items():
    lang[("item." if k in ("coupling_chain", "chain_cutters") else "block.") + f"{NS}.{k}"] = v_[0]
wj("lang/en_us.json", lang)

with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v_[0], "text": v_[1]} for k, v_ in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")

with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 3, "items": [f"{NS}:bronze_rail", f"{NS}:bronze_powered_rail"]},
               "tier2": {"age": 5, "items": [f"{NS}:steel_rail", f"{NS}:steel_powered_rail", f"{NS}:station_rail", f"{NS}:coupling_chain", f"{NS}:chain_cutters"]},
               "tier3": {"age": 6, "items": [f"{NS}:fast_rail", f"{NS}:fast_powered_rail"]}}, f, indent=2)
    f.write("\n")

RAILS = [k for k in NAMES if k.endswith("rail")]
wj("data/minecraft/tags/block/rails.json", {"values": [f"{NS}:{k}" for k in RAILS]}, ROOT)
wj("data/minecraft/tags/item/rails.json", {"values": [f"{NS}:{k}" for k in RAILS]}, ROOT)
wj("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:{k}" for k in RAILS]}, ROOT)
for k in RAILS:
    wj(f"loot_table/blocks/{k}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{k}"}],
                                                                           "conditions": [{"condition": "minecraft:survives_explosion"}]}]}, D)


def recipe(name, obj, count=1):
    obj["result"] = {"id": f"{NS}:{name}", "count": count}
    obj["category"] = "misc"
    wj(f"recipe/{name}.json", obj, D)


def shaped(name, pattern, key, count=1):
    recipe(name, {"type": "minecraft:crafting_shaped", "key": key, "pattern": pattern}, count)


shaped("bronze_rail", ["C C", "CSC", "C C"], {"C": "minecraft:copper_ingot", "S": "minecraft:stick"}, 16)
shaped("steel_rail", ["RRR", "RIR", "RRR"], {"R": f"{NS}:bronze_rail", "I": "minecraft:iron_ingot"}, 8)
shaped("fast_rail", ["RRR", "RDR", "RRR"], {"R": f"{NS}:steel_rail", "D": "minecraft:diamond"}, 8)
for t in PAL:
    shaped(f"{t}_powered_rail", ["G G", "GTG", " R "], {"G": "minecraft:gold_ingot", "T": f"{NS}:{t}_rail", "R": "minecraft:redstone"}, 2)
recipe("station_rail", {"type": "minecraft:crafting_shapeless", "ingredients": [f"{NS}:steel_powered_rail", "minecraft:clock"]})
shaped("coupling_chain", [" N ", "NIN", " N "], {"N": "minecraft:iron_nugget", "I": "minecraft:iron_ingot"}, 2)
shaped("chain_cutters", ["I I", " S ", "C C"], {"I": "minecraft:iron_ingot", "S": "minecraft:stick", "C": "minecraft:copper_ingot"})
print("ok")
