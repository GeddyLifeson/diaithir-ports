#!/usr/bin/env python3
"""Writes every original texture, model, blockstate, item definition, loot table, recipe, tag, lang entry, age gate and library entry
for diaithir-wiretech. Run from the module root: python3 tools/gen_assets.py. All art is drawn here from flat colours in a worn
industrial look: dark plate, hazard stripes, rivets, hot orange glow. No asset of any other mod is used or copied."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
LIB = os.path.join(HERE, "..", "..", "library-sync", "wiretech.json")
NS = "diaithir_wiretech"

WIRES = {"copper": ("Kellivane", (184, 115, 51)), "electrum": ("Ostrimeth", (230, 207, 110)), "steel": ("Varkhulon", (141, 147, 156))}
MACHINES = {"crusher": "Gruvandel", "arc_furnace": "Vorthazek"}
ROTORS = {"hand_crank": "Tunwiq", "windmill": "Aelvenhorn", "water_wheel": "Sholmarra"}
SHEET = {"iron": (150, 154, 160), "copper": (190, 118, 74), "steel": (96, 100, 112), "electrum": (214, 186, 92)}
SCAFFOLD = {"iron": (132, 136, 142), "steel": (84, 88, 100)}
AUGMENTS = {"speed_augment": "Varnoqith", "efficiency_augment": "Thremuvael", "chance_augment": "Ostrivaen"}
PLATE, PLATE_L, PLATE_D = (78, 80, 86), (112, 114, 120), (44, 45, 50)
HAZ_Y, HAZ_K = (236, 188, 44), (30, 30, 32)
BLACK, RIVET = (18, 19, 22), (176, 178, 184)
FIRE, FIRE2 = (240, 110, 30), (255, 200, 70)
ARC, ARC2 = (120, 190, 255), (230, 245, 255)


def A(c): return tuple(c) + (255,)
def clamp(v): return max(0, min(255, v))
def shade(c, d): return tuple(clamp(v + d) for v in c)


def plate(base, seed, rivets=True):
    img = Image.new("RGBA", (16, 16), A(base)); r = random.Random(seed)
    for x in range(16):
        for y in range(16):
            if r.random() < 0.22: img.putpixel((x, y), A(shade(base, r.choice((-10, 8)))))
    dark, light = shade(base, -34), shade(base, 30)
    for i in range(16):
        for p in ((i, 0), (i, 15), (0, i), (15, i)): img.putpixel(p, A(dark))
        if 1 <= i <= 14: img.putpixel((i, 1), A(light)); img.putpixel((1, i), A(light))
    if rivets:
        for p in ((2, 2), (13, 2), (2, 13), (13, 13)): img.putpixel(p, A(RIVET))
    return img


def hazard(img, y0, y1):
    for y in range(y0, y1):
        for x in range(2, 14): img.putpixel((x, y), A(HAZ_Y if (x + y) // 2 % 2 == 0 else HAZ_K))


def casing(kind, seed):
    img = plate(PLATE, seed)
    if kind == "side": hazard(img, 12, 14)
    elif kind == "top":
        for y in range(4, 12, 2):
            for x in range(4, 12): img.putpixel((x, y), A(BLACK))
    elif kind == "bottom":
        for i in range(3, 13):
            for p in ((i, 3), (i, 12), (3, i), (12, i)): img.putpixel(p, A(PLATE_D))
    return img


def window(img):
    for x in range(3, 13):
        for y in range(3, 11): img.putpixel((x, y), A(BLACK))
    for x in range(2, 14): img.putpixel((x, 2), A(PLATE_D)); img.putpixel((x, 11), A(PLATE_D))
    for y in range(2, 12): img.putpixel((2, y), A(PLATE_D)); img.putpixel((13, y), A(PLATE_D))


def front(kind, lit):
    img = plate(PLATE, 4); window(img); hazard(img, 12, 14)
    steel, steel_l = (112, 118, 126), (160, 166, 174)
    if kind == "crusher":
        for cx in (5, 9):  # two toothed rollers
            for y in range(3, 11):
                img.putpixel((cx, y), A(steel)); img.putpixel((cx + 1, y), A(steel_l))
                if y % 2 == 0: img.putpixel((cx - 1, y), A(steel)); img.putpixel((cx + 2, y), A(steel))
        if lit:
            for y in range(3, 11): img.putpixel((7, y), A(FIRE)); img.putpixel((8, y), A((120, 50, 16)))
    else:
        for x in (4, 11):  # twin electrodes over a hearth
            for y in range(3, 8): img.putpixel((x, y), A(steel_l)); img.putpixel((x + 1 if x == 4 else x - 1, y), A(steel))
        for x in range(4, 12): img.putpixel((x, 9), A((90, 44, 34))); img.putpixel((x, 10), A((130, 56, 44)))
        if lit:
            for y in range(4, 9): img.putpixel((5 if y % 2 else 6, y), A(ARC)); img.putpixel((10 if y % 2 else 9, y), A(ARC))
            for x in range(6, 10): img.putpixel((x, 8), A(ARC2))
            for x in range(5, 11): img.putpixel((x, 9), A(FIRE2 if x % 2 else FIRE))
    for x in range(3, 13, 3): img.putpixel((x, 14), A(HAZ_Y if lit else (90, 78, 30)))
    return img


def dynamo_side():
    img = plate((64, 62, 68), 7, rivets=False)
    for y in range(3, 13):
        for x in range(2, 14):
            if y % 2 == 1: img.putpixel((x, y), A((200, 120, 52) if x % 4 else (150, 84, 34)))
            else: img.putpixel((x, y), A((120, 66, 28)))
    for x in range(2, 14): img.putpixel((x, 2), A(PLATE_L)); img.putpixel((x, 13), A(PLATE_D))
    return img


def dynamo_end():
    img = plate((70, 72, 78), 8)
    cx = 7.5
    for x in range(16):
        for y in range(16):
            d = (x - cx) ** 2 + (y - cx) ** 2
            if d <= 20: img.putpixel((x, y), A((50, 52, 58)))
            if d <= 9: img.putpixel((x, y), A((176, 178, 184)))
            if d <= 2: img.putpixel((x, y), A(BLACK))
    return img


def sheetmetal(col):
    img = plate(col, sum(col))
    dark = shade(col, -40)
    for i in range(2, 14): img.putpixel((i, 7), A(dark)); img.putpixel((7, i), A(dark)); img.putpixel((8, i), A(shade(col, 28)))
    return img


def scaffold(col):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); dark = shade(col, -40)
    for i in range(16):
        for t in (0, 1, 7, 8, 14, 15):
            img.putpixel((i, t), A(col if t in (0, 7, 14) else dark)); img.putpixel((t, i), A(col if t in (0, 7, 14) else dark))
    for i in range(16):  # diagonal brace
        img.putpixel((i, i), A(shade(col, 14)));
    for p in ((0, 0), (7, 7), (14, 14), (0, 14), (14, 0)): img.putpixel(p, A(RIVET))
    return img


def metal_tile(col, seed): return plate(col, seed, rivets=False)


def wood_tile():
    img = Image.new("RGBA", (16, 16), A((120, 88, 52))); r = random.Random(11)
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), A(shade((120, 88, 52), -14 if y % 4 == 0 else r.choice((-6, 0, 6)))))
    return img


def cloth_tile():
    img = Image.new("RGBA", (16, 16), A((214, 208, 190))); r = random.Random(12)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0: img.putpixel((x, y), A((190, 184, 166)))
            elif r.random() < 0.1: img.putpixel((x, y), A((226, 220, 204)))
    return img


def insulator_tile():
    img = Image.new("RGBA", (16, 16), A((196, 176, 150)))
    for y in range(16):
        if y % 4 == 3:
            for x in range(16): img.putpixel((x, y), A((150, 130, 106)))
    return img


def item_img(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def dust(col):
    img = item_img(); r = random.Random(sum(col))
    for y in range(6, 14):
        w = (y - 5) * 1.0 + 1
        for x in range(16):
            if abs(x - 7.5) <= w and abs(x - 7.5) <= 6.5 and r.random() < 0.97:
                img.putpixel((x, y), A(shade(col, r.choice((-24, -10, 0, 0, 12)))))
    for x, y in ((7, 5), (8, 5), (6, 6)): img.putpixel((x, y), A(col))
    return img


def slag():
    img = item_img(); r = random.Random(5)
    for y in range(5, 13):
        for x in range(3, 13):
            if (x - 7.5) ** 2 / 20 + (y - 8.5) ** 2 / 14 <= 1: img.putpixel((x, y), A(shade((72, 66, 70), r.choice((-14, 0, 10)))))
    for p in ((5, 7), (9, 9), (7, 6)): img.putpixel(p, A((200, 90, 40)))
    return img


def ingot(base):
    img = item_img(); light, dark = shade(base, 40), shade(base, -66)
    for y in range(5, 12):
        for x in range(2 + (11 - y) // 2, 13 + (11 - y) // 2):
            if 1 <= x <= 14: img.putpixel((x, y), A(base))
    for x in range(4, 15): img.putpixel((min(14, x), 5), A(light))
    for y in range(5, 12):
        xs = [x for x in range(16) if img.getpixel((x, y))[3]]
        if xs: img.putpixel((xs[0], y), A(light)); img.putpixel((xs[-1], y), A(dark))
    for x in range(16):
        if img.getpixel((x, 11))[3]: img.putpixel((x, 11), A(dark))
    return img


def coil(col):
    img = item_img(); dark = shade(col, -60); light = shade(col, 40)
    cx = cy = 7.5
    for x in range(16):
        for y in range(16):
            d = (x - cx) ** 2 + (y - cy) ** 2
            if d <= 30: img.putpixel((x, y), A(col if int(d) % 5 else dark))
            if d <= 7: img.putpixel((x, y), A((58, 58, 64)))
            if d <= 2: img.putpixel((x, y), A(BLACK))
    for p in ((4, 4), (5, 3), (3, 6)): img.putpixel(p, A(light))
    for y in range(10, 16): img.putpixel((12, y), A(col))
    return img


def augment(kind):
    img = item_img(); card, edge, pin = (46, 50, 56), (88, 94, 102), (214, 176, 70)
    for x in range(3, 13):
        for y in range(2, 14): img.putpixel((x, y), A(card))
    for x in range(3, 13): img.putpixel((x, 2), A(edge)); img.putpixel((x, 13), A(edge))
    for y in range(2, 14): img.putpixel((3, y), A(edge)); img.putpixel((12, y), A(edge))
    for x in (4, 6, 8, 10): img.putpixel((x, 14), A(pin)); img.putpixel((x, 15), A(pin))
    if kind == "speed_augment":
        c = (232, 96, 48)
        for i in range(4):
            for d in range(3): img.putpixel((4 + i + d, 4 + i), A(c)); img.putpixel((4 + i + d, 11 - i), A(c))
    elif kind == "efficiency_augment":
        c = (80, 200, 110)
        for y in range(4, 12): img.putpixel((7, y), A(c)); img.putpixel((8, y), A(c))
        for i in range(3): img.putpixel((5 + i, 8 - i), A(c)); img.putpixel((10 - i, 8 - i), A(c))
        for x in range(5, 11): img.putpixel((x, 11), A(c))
    else:
        c = (110, 150, 240)
        for x in range(5, 11):
            for y in range(5, 11): img.putpixel((x, y), A(c))
        for p in ((6, 6), (9, 6), (7, 8), (6, 9), (9, 9)): img.putpixel(p, A((20, 30, 60)))
    return img


def write(path, data):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(data, f, indent=2); f.write("\n")


def png(path, img):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True); img.save(path)


def item_def(name, model): write(f"assets/{NS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:{model}"}})


def flat_item(name, img):
    png(f"assets/{NS}/textures/item/{name}.png", img)
    write(f"assets/{NS}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    item_def(name, f"item/{name}")


def loot(name):
    write(f"data/{NS}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{NS}:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def shaped(name, pattern, key, result, count=1):
    write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": key, "pattern": pattern,
                                            "result": {"id": result, "count": count}})


def mrec(kind, name, inputs, result=None, bonus=None, chance=0.0, energy=2400, time=200):
    d = {"type": f"{NS}:{kind}", "inputs": [{"ingredient": i, "count": c} for i, c in inputs], "energy": energy, "time": time}
    if result: d["result"] = {"id": result[0], "count": result[1]}
    if bonus: d["bonus"] = {"id": bonus, "count": 1}; d["bonus_chance"] = chance
    write(f"data/{NS}/recipe/{kind}/{name}.json", d)


def box(frm, to, tex, faces=None):
    f = {d: {"uv": [0, 0, 16, 16], "texture": "#" + tex} for d in ("north", "south", "east", "west", "up", "down")}
    return {"from": frm, "to": to, "faces": f}


def single_variant(name, model, **rot):
    v = {"model": f"{NS}:{model}"}; v.update(rot); return v


def main():
    lang, items_all, lib = {}, [], []
    nm = lambda n: lang.setdefault(f"block.{NS}.{n}", n)

    # ---- casing and machine textures
    for k in ("side", "top", "bottom"): png(f"assets/{NS}/textures/block/casing_{k}.png", casing(k, {"side": 1, "top": 2, "bottom": 3}[k]))
    machine_names = []
    for kind, disp in MACHINES.items():
        machine_names.append(kind)
        png(f"assets/{NS}/textures/block/{kind}_front.png", front(kind, False))
        png(f"assets/{NS}/textures/block/{kind}_front_lit.png", front(kind, True))
        for lit in (False, True):
            write(f"assets/{NS}/models/block/{kind}{'_lit' if lit else ''}.json", {
                "parent": "minecraft:block/orientable_with_bottom",
                "textures": {"top": f"{NS}:block/casing_top", "bottom": f"{NS}:block/casing_bottom", "side": f"{NS}:block/casing_side",
                             "front": f"{NS}:block/{kind}_front{'_lit' if lit else ''}"}})
        variants = {}
        for fc, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
            for act in ("false", "true"):
                v = {"model": f"{NS}:block/{kind}{'_lit' if act == 'true' else ''}"}
                if y: v["y"] = y
                variants[f"active={act},facing={fc}"] = v
        write(f"assets/{NS}/blockstates/{kind}.json", {"variants": variants})
        item_def(kind, f"block/{kind}"); loot(kind)
        lang[f"block.{NS}.{kind}"] = disp

    # ---- dynamo
    png(f"assets/{NS}/textures/block/dynamo_side.png", dynamo_side()); png(f"assets/{NS}/textures/block/dynamo_end.png", dynamo_end())
    write(f"assets/{NS}/models/block/dynamo.json", {"parent": "minecraft:block/cube_column", "textures": {
        "end": f"{NS}:block/dynamo_end", "side": f"{NS}:block/dynamo_side"}})
    write(f"assets/{NS}/blockstates/dynamo.json", {"variants": {"": {"model": f"{NS}:block/dynamo"}}})
    item_def("dynamo", "block/dynamo"); loot("dynamo"); lang[f"block.{NS}.dynamo"] = "Zephrikon"

    # ---- rotors: element models, facing north, rotated by blockstate
    png(f"assets/{NS}/textures/block/crank_metal.png", metal_tile((70, 72, 80), 21))
    png(f"assets/{NS}/textures/block/rotor_wood.png", wood_tile()); png(f"assets/{NS}/textures/block/rotor_cloth.png", cloth_tile())
    models = {
        "hand_crank": {"textures": {"m": f"{NS}:block/crank_metal", "particle": f"{NS}:block/crank_metal"}, "elements": [
            box([4, 0, 4], [12, 4, 12], "m"), box([7, 4, 7], [9, 11, 9], "m"), box([7, 10, 7], [9, 12, 15], "m"), box([6, 9, 13], [10, 15, 15], "m")]},
        "windmill": {"textures": {"w": f"{NS}:block/rotor_wood", "c": f"{NS}:block/rotor_cloth", "particle": f"{NS}:block/rotor_wood"}, "elements": [
            box([6, 6, 5], [10, 10, 11], "w"), box([0, 7, 7], [6, 9, 8], "c"), box([10, 7, 7], [16, 9, 8], "c"),
            box([7, 0, 7], [9, 6, 8], "c"), box([7, 10, 7], [9, 16, 8], "c"), box([7, 7, 3], [9, 9, 5], "w")]},
        "water_wheel": {"textures": {"w": f"{NS}:block/rotor_wood", "particle": f"{NS}:block/rotor_wood"}, "elements": [
            box([6, 6, 4], [10, 10, 12], "w"), box([0, 7, 5], [16, 9, 6], "w"), box([7, 0, 5], [9, 16, 6], "w"),
            box([0, 5, 4], [3, 11, 7], "w"), box([13, 5, 4], [16, 11, 7], "w"), box([5, 0, 4], [11, 3, 7], "w"), box([5, 13, 4], [11, 16, 7], "w")]},
    }
    for r, disp in ROTORS.items():
        write(f"assets/{NS}/models/block/{r}.json", models[r])
        write(f"assets/{NS}/blockstates/{r}.json", {"variants": {f"facing={fc}": single_variant(r, f"block/{r}", **({"y": y} if y else {}))
                                                               for fc, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
        item_def(r, f"block/{r}"); loot(r); lang[f"block.{NS}.{r}"] = disp

    # ---- connectors
    png(f"assets/{NS}/textures/block/insulator.png", insulator_tile())
    cn = []
    for w, (disp, col) in WIRES.items():
        png(f"assets/{NS}/textures/block/connector_{w}.png", metal_tile(col, sum(col)))
        name = f"{w}_connector"; cn.append(name)
        write(f"assets/{NS}/models/block/{name}.json", {"textures": {"m": f"{NS}:block/connector_{w}", "i": f"{NS}:block/insulator",
                                                                      "particle": f"{NS}:block/connector_{w}"},
                                                         "elements": [box([5, 0, 5], [11, 3, 11], "m"), box([7, 3, 7], [9, 9, 9], "m"),
                                                                      box([6, 9, 6], [10, 12, 10], "i"), box([7, 12, 7], [9, 14, 9], "m")]})
        rot = {"up": {}, "down": {"x": 180}, "north": {"x": 90}, "east": {"x": 90, "y": 90}, "south": {"x": 90, "y": 180}, "west": {"x": 90, "y": 270}}
        write(f"assets/{NS}/blockstates/{name}.json", {"variants": {f"facing={f}": single_variant(name, f"block/{name}", **r) for f, r in rot.items()}})
        item_def(name, f"block/{name}"); loot(name); lang[f"block.{NS}.{name}"] = f"{disp} Link"
        flat_item(f"{w}_wire_coil", coil(col)); lang[f"item.{NS}.{w}_wire_coil"] = f"{disp} Spool"

    # ---- building blocks
    bn = []
    for m, col in SHEET.items():
        n = f"sheetmetal_{m}"; bn.append(n); png(f"assets/{NS}/textures/block/{n}.png", sheetmetal(col))
        write(f"assets/{NS}/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{n}"}})
        write(f"assets/{NS}/blockstates/{n}.json", {"variants": {"": {"model": f"{NS}:block/{n}"}}})
        item_def(n, f"block/{n}"); loot(n); lang[f"block.{NS}.{n}"] = f"Plynthar Sheet ({m.capitalize()})"
    sc = []
    for m, col in SCAFFOLD.items():
        n = f"{m}_scaffolding"; sc.append(n); png(f"assets/{NS}/textures/block/{n}.png", scaffold(col))
        write(f"assets/{NS}/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "render_type": "minecraft:cutout", "textures": {"all": f"{NS}:block/{n}"}})
        write(f"assets/{NS}/blockstates/{n}.json", {"variants": {"": {"model": f"{NS}:block/{n}"}}})
        item_def(n, f"block/{n}"); loot(n); lang[f"block.{NS}.{n}"] = f"Strandel Scaffold ({m.capitalize()})"

    # ---- items
    mats = {"iron_dust": ((190, 194, 200), "Iron Dust", dust), "copper_dust": ((196, 112, 70), "Copper Dust", dust), "gold_dust": ((240, 200, 60), "Gold Dust", dust),
            "coal_dust": ((44, 44, 48), "Coal Dust", dust), "slag": (None, "Slag", None), "electrum_ingot": ((230, 207, 110), "Electrum Ingot", ingot),
            "steel_ingot": ((120, 126, 138), "Steel Ingot", ingot)}
    for n, (c, d, fn) in mats.items():
        flat_item(n, slag() if n == "slag" else fn(c)); lang[f"item.{NS}.{n}"] = d
    for n, d in AUGMENTS.items():
        flat_item(n, augment(n)); lang[f"item.{NS}.{n}"] = d + " Augment"
    lang[f"itemGroup.{NS}"] = "Diaithir Wiretech"
    write(f"assets/{NS}/lang/en_us.json", dict(sorted(lang.items())))

    # ---- tags
    pick = machine_names + ["dynamo", "hand_crank"] + cn + bn + sc
    write("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:{n}" for n in pick]})
    write("data/minecraft/tags/block/mineable/axe.json", {"values": [f"{NS}:windmill", f"{NS}:water_wheel"]})
    write("data/minecraft/tags/block/needs_stone_tool.json", {"values": [f"{NS}:{n}" for n in machine_names + ["dynamo"] + bn + sc]})
    write("data/minecraft/tags/block/climbable.json", {"values": [f"{NS}:{n}" for n in sc]})
    for d, its in (("dusts/iron", ["iron_dust"]), ("dusts/copper", ["copper_dust"]), ("dusts/gold", ["gold_dust"]), ("dusts/coal", ["coal_dust"]),
                   ("ingots/electrum", ["electrum_ingot"]), ("ingots/steel", ["steel_ingot"]),
                   ("dusts", ["iron_dust", "copper_dust", "gold_dust", "coal_dust"]), ("ingots", ["electrum_ingot", "steel_ingot"])):
        write(f"data/c/tags/item/{d}.json", {"values": [f"{NS}:{i}" for i in its]})

    # ---- age gate (numeric ages; everything is Age IV)
    all_ids = machine_names + ["dynamo"] + list(ROTORS) + cn + [f"{w}_wire_coil" for w in WIRES] + bn + sc + list(mats) + list(AUGMENTS)
    write(f"data/{NS}/age_gate.json", {"age4": {"age": 4, "items": [f"{NS}:{n}" for n in all_ids]}})

    # ---- crafting
    I, C, R, S = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:redstone", "minecraft:stick"
    ing = {"copper": C, "electrum": f"{NS}:electrum_ingot", "steel": f"{NS}:steel_ingot"}
    for w, i in ing.items():
        shaped(f"craft/{w}_connector", [" B ", "CCC"], {"B": "minecraft:brick", "C": i}, f"{NS}:{w}_connector", 2)
        shaped(f"craft/{w}_wire_coil", ["CCC", "CSC", "CCC"], {"C": i, "S": S}, f"{NS}:{w}_wire_coil", 4)
    shaped("craft/hand_crank", ["  S", " I ", "III"], {"S": S, "I": I}, f"{NS}:hand_crank")
    shaped("craft/windmill", ["WSW", "SIS", "WSW"], {"W": "#minecraft:wool", "S": S, "I": I}, f"{NS}:windmill")
    shaped("craft/water_wheel", ["PSP", "SIS", "PSP"], {"P": "#minecraft:planks", "S": S, "I": I}, f"{NS}:water_wheel")
    shaped("craft/dynamo", ["CCC", "IRI", "III"], {"C": C, "I": I, "R": R}, f"{NS}:dynamo")
    shaped("craft/crusher", ["IFI", "RPR", "ICI"], {"I": I, "F": "minecraft:flint", "R": R, "P": "minecraft:piston", "C": C}, f"{NS}:crusher")
    shaped("craft/arc_furnace", ["ICI", "RBR", "IGI"], {"I": I, "C": f"{NS}:copper_wire_coil", "R": R, "B": "minecraft:blast_furnace", "G": "minecraft:gold_ingot"},
           f"{NS}:arc_furnace")
    for m, col in SHEET.items():
        src = {"iron": I, "copper": C, "steel": f"{NS}:steel_ingot", "electrum": f"{NS}:electrum_ingot"}[m]
        shaped(f"craft/sheetmetal_{m}", ["II", "II"], {"I": src}, f"{NS}:sheetmetal_{m}", 4)
    for m in SCAFFOLD:
        src = I if m == "iron" else f"{NS}:steel_ingot"
        shaped(f"craft/{m}_scaffolding", ["III", " S ", "S S"], {"I": src, "S": S}, f"{NS}:{m}_scaffolding", 6)
    shaped("craft/speed_augment", ["RGR", "GCG", "RGR"], {"R": R, "G": "minecraft:gold_ingot", "C": C}, f"{NS}:speed_augment")
    shaped("craft/efficiency_augment", ["RCR", "CLC", "RCR"], {"R": R, "C": C, "L": "minecraft:lapis_lazuli"}, f"{NS}:efficiency_augment")
    shaped("craft/chance_augment", ["RQR", "QAQ", "RQR"], {"R": R, "Q": "minecraft:quartz", "A": "minecraft:amethyst_shard"}, f"{NS}:chance_augment")
    for dname, ingr in (("iron", I), ("copper", C), ("gold", "minecraft:gold_ingot")):
        for typ, cook in (("smelting", 200), ("blasting", 100)):
            write(f"data/{NS}/recipe/{typ}/{dname}_dust.json", {"type": f"minecraft:{typ}", "category": "misc", "ingredient": f"{NS}:{dname}_dust",
                                                               "result": {"id": ingr}, "experience": 0.7, "cookingtime": cook})

    # ---- machine recipes (data driven; datapacks and KubeJS add more of the same shape)
    d = lambda n: f"{NS}:{n}_dust"
    mrec("crusher", "iron_ore", [("#c:ores/iron", 1)], (d("iron"), 2), d("copper"), 0.1)
    mrec("crusher", "copper_ore", [("#c:ores/copper", 1)], (d("copper"), 2), d("iron"), 0.1)
    mrec("crusher", "gold_ore", [("#c:ores/gold", 1)], (d("gold"), 2), "minecraft:redstone", 0.15)
    mrec("crusher", "raw_iron", [("#c:raw_materials/iron", 1)], (d("iron"), 1), None, 0, energy=1200, time=100)
    mrec("crusher", "raw_copper", [("#c:raw_materials/copper", 1)], (d("copper"), 1), None, 0, energy=1200, time=100)
    mrec("crusher", "raw_gold", [("#c:raw_materials/gold", 1)], (d("gold"), 1), None, 0, energy=1200, time=100)
    mrec("crusher", "coal", [("minecraft:coal", 1)], (d("coal"), 1), None, 0, energy=800, time=80)
    mrec("crusher", "charcoal", [("minecraft:charcoal", 1)], (d("coal"), 1), None, 0, energy=800, time=80)
    mrec("crusher", "cobblestone", [("minecraft:cobblestone", 1)], ("minecraft:gravel", 1), None, 0, energy=800, time=80)
    mrec("crusher", "gravel", [("minecraft:gravel", 1)], ("minecraft:sand", 1), "minecraft:flint", 0.1, energy=800, time=80)
    for n, ingr in (("iron", I), ("copper", C), ("gold", "minecraft:gold_ingot")):
        mrec("arc_furnace", f"{n}_dust", [(d(n), 1)], (ingr, 1), None, 0, energy=1200, time=60)
        mrec("arc_furnace", f"raw_{n}", [(f"#c:raw_materials/{n}", 1)], (ingr, 1), f"{NS}:slag", 0.25, energy=1600, time=80)
    mrec("arc_furnace", "steel", [(I, 1), (d("coal"), 1)], (f"{NS}:steel_ingot", 1), f"{NS}:slag", 0.3, energy=4000, time=150)
    mrec("arc_furnace", "electrum", [("minecraft:gold_ingot", 1), (C, 1)], (f"{NS}:electrum_ingot", 2), None, 0, energy=3000, time=120)
    mrec("arc_furnace", "glass", [("#minecraft:sand", 1)], ("minecraft:glass", 1), None, 0, energy=900, time=50)

    # ---- library entries (invented display names, English ids, no em dashes)
    def L(i, n, t): lib.append({"id": f"{NS}:{i}", "name": n, "text": t})
    for w, (disp, col) in WIRES.items():
        r, tp, ls = {"copper": (16, 256, "1.5%"), "electrum": (24, 1024, "0.8%"), "steel": (32, 4096, "0.4%")}[w]
        L(f"{w}_connector", f"{disp} Link", f"Wire connector. Sits on the face of a block. Take FE from a generator beside it, hold up to 6 {w} wires, and feed any machine beside it. Wires reach {r} blocks and lose {ls} of the FE per block.")
        L(f"{w}_wire_coil", f"{disp} Spool", f"Wire for {w} connectors. Use it on one connector, then on a second one within {r} blocks. Carries {tp} FE per tick. Sneak and use to clear the start.")
    L("hand_crank", "Tunwiq", "Hand crank. Use it with an empty hand to turn it for one second. A dynamo beside it turns the torque into FE.")
    L("windmill", "Aelvenhorn", "Windmill. Needs a mostly open 5 by 5 frame around it, facing the way the block faces. Gives more torque higher up and in rain. A dynamo beside it makes FE.")
    L("water_wheel", "Sholmarra", "Water wheel. Gives torque for each side that touches flowing water. A dynamo beside it makes FE.")
    L("dynamo", "Zephrikon", "Dynamo. Turns the torque of hand cranks, windmills and water wheels beside it into FE and pushes it into connectors or machines beside it.")
    L("crusher", "Gruvandel", "Crusher. Feed it ore and FE from a wire and it grinds the ore to dust, sometimes with a bonus item. Has a GUI, FE bar, redstone control and per-face side config.")
    L("arc_furnace", "Vorthazek", "Arc furnace. Smelts dust and raw ore to ingots fast, makes steel from iron and coal dust, and electrum from gold and copper. Slag comes out as a bonus. Needs FE from a wire.")
    for m in SHEET: L(f"sheetmetal_{m}", f"Plynthar Sheet ({m.capitalize()})", f"Plain {m} sheetmetal block for building.")
    for m in SCAFFOLD: L(f"{m}_scaffolding", f"Strandel Scaffold ({m.capitalize()})", f"Open {m} scaffolding. Climbable, and light passes through it.")
    L("iron_dust", "Iron Dust", "Ground iron ore. Smelt it or put it in the arc furnace to get an ingot.")
    L("copper_dust", "Copper Dust", "Ground copper ore. Smelt it or put it in the arc furnace to get an ingot.")
    L("gold_dust", "Gold Dust", "Ground gold ore. Smelt it or put it in the arc furnace to get an ingot.")
    L("coal_dust", "Coal Dust", "Ground coal. Arc furnace additive for making steel.")
    L("slag", "Slag", "Waste from the arc furnace. Good for nothing yet.")
    L("electrum_ingot", "Electrum Ingot", "Alloy of gold and copper from the arc furnace. Used for electrum wire and connectors.")
    L("steel_ingot", "Steel Ingot", "Iron hardened with coal dust in the arc furnace. Used for steel wire and connectors.")
    L("speed_augment", "Varnoqith Augment", "Augment. Put up to 4 in a machine's augment slot: each adds 50% speed for a little extra FE per job.")
    L("efficiency_augment", "Thremuvael Augment", "Augment. Put up to 4 in a machine's augment slot: each cuts the FE cost of a job by 15%.")
    L("chance_augment", "Ostrivaen Augment", "Augment. Put up to 4 in a machine's augment slot: each raises the chance of bonus outputs by half again.")
    assert not any("—" in e["text"] or "—" in e["name"] for e in lib)
    assert len(lib) == len(all_ids), (len(lib), len(all_ids))
    for p in (LIB, "/mnt/project-files/library-sync/wiretech.json"):
        try:
            os.makedirs(os.path.dirname(p), exist_ok=True)
            with open(p, "w") as f:
                f.write("[\n" + ",\n".join("  " + json.dumps(e, ensure_ascii=False) for e in lib) + "\n]\n")
        except OSError as ex:
            print("could not write", p, ex)


main()
