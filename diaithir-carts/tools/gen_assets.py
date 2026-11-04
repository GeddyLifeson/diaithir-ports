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

# one-way: steel rails with a white arrow pointing up the image (north); the blockstate rotates it
im = rail_image(PAL["steel"], False, None)
px = im.load()
for i in range(5):
    w = i
    for x in range(8 - w, 9 + w):
        px[x, 4 + i] = (245, 245, 245, 255)
for y in range(9, 13):
    for x in (7, 8):
        px[x, y] = (245, 245, 245, 255)
save(im, "textures/block/one_way_rail.png")

# destination rail: steel rails with a hazard-free coloured bar across the middle; 17 channels (0 = any, 1..16 = dye colours), lit and unlit
DYES = [(249, 255, 254), (249, 128, 29), (199, 78, 189), (58, 179, 218), (254, 216, 61), (128, 199, 31), (243, 139, 170), (71, 79, 82),
        (157, 157, 151), (22, 156, 156), (137, 50, 184), (60, 68, 170), (131, 84, 50), (94, 124, 22), (176, 46, 38), (29, 29, 33)]
for ch in range(17):
    for on in (False, True):
        im = rail_image(PAL["steel"], False, None)
        px = im.load()
        col = (150, 156, 164) if ch == 0 else DYES[ch - 1]
        if on:
            col = tuple(min(255, int(c * 1.0 + 40)) for c in col)
        for y in range(6, 10):
            for x in range(2, 14):
                edge = y in (6, 9) or x in (2, 13)
                px[x, y] = (tuple(int(c * 0.55) for c in col) if edge and not on else (255, 255, 255) if edge else col) + (255,)
        if ch == 0:
            for x in range(5, 11):
                px[x, 7] = px[x, 8] = (60, 64, 70, 255)
        save(im, f"textures/block/destination_rail_{ch}{'_on' if on else ''}.png")

# cart loader: a steel box; the front has a hopper mouth and an arrow (in = load into the cart, out = unload from it)
def loader_face(kind):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for y in range(16):
        for x in range(16):
            base = 96 + rnd.randrange(-6, 7)
            edge = x in (0, 15) or y in (0, 15)
            px[x, y] = ((60, 66, 74) if edge else (base, base + 8, base + 18)) + (255,)
    if kind == "top":
        return im
    if kind == "side":
        for y in (4, 11):
            for x in range(1, 15):
                px[x, y] = (70, 76, 86, 255)
        return im
    for y in range(4, 12):
        for x in range(3, 13):
            px[x, y] = (24, 26, 30, 255)
    arrow = (240, 190, 30, 255) if kind == "front_out" else (90, 190, 255, 255)
    # arrow pointing out of the mouth (up) or into it (down)
    for i in range(4):
        for x in range(8 - i, 8 + i):
            px[x, (5 + i) if kind == "front_out" else (10 - i)] = arrow
    for y in range(5, 11):
        px[7, y] = px[8, y] = arrow
    return im


for k in ("top", "side", "front_in", "front_out"):
    save(loader_face(k), f"textures/block/cart_loader_{k}.png")

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
block_model("one_way_rail", FLAT, "one_way_rail")
wj("blockstates/one_way_rail.json", {"variants": {f"facing={f}": v("one_way_rail", y) for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
item_def("one_way_rail", None, "block/one_way_rail")
var = {}
for ch in range(17):
    for on in (False, True):
        tex = f"destination_rail_{ch}{'_on' if on else ''}"
        block_model(tex, FLAT, tex)
        block_model(tex + "_raised_ne", NE, tex)
        block_model(tex + "_raised_sw", SW, tex)
        p = f"channel={ch},powered=" + ("true" if on else "false")
        var[f"{p},shape=north_south"] = v(tex)
        var[f"{p},shape=east_west"] = v(tex, 90)
        for sh, (k, y) in SLOPES.items():
            var[f"{p},shape={sh}"] = v(f"{tex}_raised_{k}", y)
wj("blockstates/destination_rail.json", {"variants": var})
item_def("destination_rail", None, "block/destination_rail_0")
for mode, front in (("load", "front_in"), ("unload", "front_out")):
    wj(f"models/block/cart_loader_{mode}.json", {"parent": "minecraft:block/orientable", "textures": {
        "top": f"{NS}:block/cart_loader_top", "front": f"{NS}:block/cart_loader_{front}", "side": f"{NS}:block/cart_loader_side"}})
wj("blockstates/cart_loader.json", {"variants": {f"facing={f},unload={u}": v(f"cart_loader_{'unload' if u == 'true' else 'load'}", y)
                                               for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)) for u in ("false", "true")}})
wj("items/cart_loader.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/cart_loader_load"}})
item_def("coupling_chain", None, "item/coupling_chain")
item_def("chain_cutters", None, "item/chain_cutters")

# rail yard controller block and the four yard part items
def yard_face(kind):
    im = Image.new("RGBA", (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            b = 88 + rnd.randrange(-6, 7)
            edge = x in (0, 15) or y in (0, 15)
            px[x, y] = ((48, 54, 62) if edge else (b, b + 6, b + 14)) + (255,)
    if kind == "front":
        for y in range(3, 8):
            for x in range(3, 13):
                px[x, y] = (24, 28, 34, 255)
        for x in range(4, 12):
            px[x, 5] = (240, 190, 30, 255) if x % 2 == 0 else (28, 28, 30, 255)
        for y in range(10, 14):
            for x in range(2, 14):
                px[x, y] = (240, 190, 30, 255) if ((x + y) // 2) % 2 == 0 else (28, 28, 30, 255)
    if kind == "top":
        for x in range(2, 14):
            for y in (4, 8, 12):
                px[x, y] = (58, 64, 74, 255)
    return im


for k in ("top", "side", "front"):
    save(yard_face(k), f"textures/block/rail_yard_controller_{k}.png")
wj("models/block/rail_yard_controller.json", {"parent": "minecraft:block/cube_column", "textures": {"end": f"{NS}:block/rail_yard_controller_top", "side": f"{NS}:block/rail_yard_controller_front"}})
wj("blockstates/rail_yard_controller.json", {"variants": {"": v("rail_yard_controller")}})
wj("items/rail_yard_controller.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/rail_yard_controller"}})


def part_icon(name):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    S = [(120, 130, 142), (176, 186, 198), (70, 76, 86)]
    if name == "cart_chassis":
        for x in range(2, 14):
            for y in (5, 6, 11, 12):
                px[x, y] = S[1] if y in (5, 11) else S[0]
        for y in range(5, 13):
            px[2, y] = px[13, y] = S[0]
            if y % 3 == 0:
                for x in range(3, 13):
                    px[x, y] = S[2]
    elif name == "wheelset":
        for dx in (4, 11):
            for yy in range(16):
                for xx in range(16):
                    r = math.hypot(xx - dx, yy - 8)
                    if r <= 3.6 and r >= 1.2:
                        px[xx, yy] = S[1] if r < 2.5 else S[0]
                    elif r < 1.2:
                        px[xx, yy] = (240, 190, 30, 255)
        for x in range(4, 12):
            px[x, 8] = S[2]
    else:
        col = (150, 104, 60) if name == "freight_body" else (96, 100, 108)
        for x in range(2, 14):
            for y in range(4, 13):
                px[x, y] = tuple(max(0, c - 22 * (y % 3 == 0)) for c in col) + (255,)
        for x in range(2, 14):
            px[x, 4] = S[1] + (255,)
        if name == "bulk_body":
            for x in range(4, 12):
                for y in range(10, 13):
                    px[x, y] = (30, 32, 36, 255)
    return im


for n in ("cart_chassis", "wheelset", "freight_body", "bulk_body"):
    save(part_icon(n), f"textures/item/{n}.png")
    item_def(n, None, f"item/{n}")


NAMES = {
    "bronze_rail": ("Brundarok", "Bronze rail. Carts ride it up to 0.6 blocks per tick, half again vanilla speed. Curves, slopes and junctions work like a normal rail. Age III."),
    "bronze_powered_rail": ("Brundavoss", "Bronze powered rail. Boosts a cart when powered, stops it when not. Caps at 0.6 blocks per tick. Age III."),
    "steel_rail": ("Sorvanquil", "Steel rail. Carts ride it up to 0.9 blocks per tick, more than twice vanilla speed. Age V."),
    "steel_powered_rail": ("Sorvaleth", "Steel powered rail. A stronger boost than bronze, same stop when unpowered. Caps at 0.9 blocks per tick. Age V."),
    "fast_rail": ("Zephrakund", "High-speed rail. Carts ride it up to 1.2 blocks per tick, three times vanilla speed. Keep curves gentle. Age VI."),
    "fast_powered_rail": ("Zephravoss", "High-speed powered rail. The strongest boost, caps at 1.2 blocks per tick. Age VI."),
    "station_rail": ("Haldurmek", "Station rail. Stops a cart that rolls onto it and holds it until the rail is powered, or until its timer ends. Use it bare-handed to cycle the timer: signal only, 2, 5, 15 or 60 seconds. A coupled follower follows its leader instead. Age V."),
    "one_way_rail": ("Eskavorn", "One-way rail. A steel-speed rail with an arrow, set to the way you face when you place it. A cart rolling against the arrow is stopped dead; carts going with it pass as on any rail. Place it straight, on a flat run. Age V."),
    "destination_rail": ("Tarvelmok", "Destination rail. A steel-speed detector rail with a coloured bar. Label a cart by using a dye on it (sneak with an empty hand to wipe the label); use a dye on the rail to choose which label it reads, an empty hand to make it read every cart. It gives a redstone signal only while a cart with a matching label is on it, so a row of them sorts a mixed line: wire each to a powered rail or junction. Age V."),
    "cart_loader": ("Voskarmeth", "Cart loader. Put its back against a chest, barrel or hopper and its front toward a rail: it moves items between that container and a container cart (chest, hopper) standing on the rail, a stack every 8 ticks. Use it bare-handed to switch between loading and unloading; a redstone signal switches it off. Age V."),
    "rail_yard_controller": ("Hardemvoss", "Rail yard controller. Choose a freight or bulk yard bare-handed (sneak to change), install cart chassis, two wheelsets and a body in that order by using them on it, and it builds each stage by itself. Bare-handed once complete it rolls a coupled train of three carts onto a rail within 3 blocks; with no rail nothing is spent. Parts need Age III. A real-time block, no turns.", ),
    "cart_chassis": ("Fremvak", "Cart chassis. First part of a yard build: the frame the wheelsets and the body go on. Age III."),
    "wheelset": ("Rotamvek", "Wheelset. Two go on each cart after the chassis. Age III."),
    "freight_body": ("Grathmark", "Freight body. The last part of a freight build: three chest carts roll out. Age III."),
    "bulk_body": ("Skarvund", "Bulk body. The last part of a bulk build: three hopper carts roll out. Age III."),
    "coupling_chain": ("Quelvarith", "Coupling chain. Use it on one cart, then on a second: the first leads, the second follows at a fixed spacing and is pulled along, so a row of carts runs as a train. A link breaks if the carts get more than 8 blocks apart. Age V."),
    "chain_cutters": ("Snithrekel", "Chain cutters. Sneak and use on a cart to cut its link and get the chain back. Age V."),
}
lang = {}
for k, v_ in NAMES.items():
    lang[("item." if k in ("coupling_chain", "chain_cutters", "cart_chassis", "wheelset", "freight_body", "bulk_body") else "block.") + f"{NS}.{k}"] = v_[0]
wj("lang/en_us.json", lang)

with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v_[0], "text": v_[1]} for k, v_ in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")

with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 3, "items": [f"{NS}:bronze_rail", f"{NS}:bronze_powered_rail", f"{NS}:rail_yard_controller", f"{NS}:cart_chassis", f"{NS}:wheelset", f"{NS}:freight_body", f"{NS}:bulk_body"]},
               "tier2": {"age": 5, "items": [f"{NS}:steel_rail", f"{NS}:steel_powered_rail", f"{NS}:station_rail", f"{NS}:one_way_rail", f"{NS}:destination_rail", f"{NS}:cart_loader", f"{NS}:coupling_chain", f"{NS}:chain_cutters"]},
               "tier3": {"age": 6, "items": [f"{NS}:fast_rail", f"{NS}:fast_powered_rail"]}}, f, indent=2)
    f.write("\n")

RAILS = [k for k in NAMES if k.endswith("rail")]
PICK = RAILS + ["cart_loader", "rail_yard_controller"]
wj("data/minecraft/tags/block/rails.json", {"values": [f"{NS}:{k}" for k in RAILS]}, ROOT)
wj("data/minecraft/tags/item/rails.json", {"values": [f"{NS}:{k}" for k in RAILS]}, ROOT)
wj("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:{k}" for k in PICK]}, ROOT)
for k in PICK:
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
recipe("one_way_rail", {"type": "minecraft:crafting_shapeless", "ingredients": [f"{NS}:steel_rail", "minecraft:iron_ingot"]})
recipe("destination_rail", {"type": "minecraft:crafting_shapeless", "ingredients": [f"{NS}:steel_rail", "minecraft:redstone", "minecraft:lapis_lazuli"]})
shaped("cart_loader", ["I I", "IHI", " R "], {"I": "minecraft:iron_ingot", "H": "minecraft:hopper", "R": "minecraft:redstone"})
shaped("rail_yard_controller", ["ISI", "SHS", "ICI"], {"I": "minecraft:iron_ingot", "S": f"{NS}:steel_rail", "H": "minecraft:crafter", "C": "minecraft:clock"})
shaped("cart_chassis", ["I I", "III", "I I"], {"I": "minecraft:iron_ingot"})
shaped("wheelset", ["I I", "SIS", "I I"], {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}, 2)
shaped("freight_body", ["P P", "PCP", "III"], {"P": "minecraft:oak_planks", "I": "minecraft:iron_ingot", "C": "minecraft:chest"})
shaped("bulk_body", ["I I", "IHI", " I "], {"I": "minecraft:iron_ingot", "H": "minecraft:hopper"})
shaped("coupling_chain", [" N ", "NIN", " N "], {"N": "minecraft:iron_nugget", "I": "minecraft:iron_ingot"}, 2)
shaped("chain_cutters", ["I I", " S ", "C C"], {"I": "minecraft:iron_ingot", "S": "minecraft:stick", "C": "minecraft:copper_ingot"})
print("ok")
