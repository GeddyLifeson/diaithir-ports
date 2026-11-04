#!/usr/bin/env python3
# S6894 gen_circuit_assets.py: circuit network textures, models, blockstates, lang, recipes and age gate (run after gen_assets.py).
"""Writes the textures, models, blockstates, items, loot, lang, recipes and age gate entries of the circuit network blocks (package net.diaithir.wiretech.circuit).
Run AFTER gen_assets.py, from the module root: python3 tools/gen_circuit_assets.py. It only adds files and merges keys into en_us.json and age_gate.json.
Crafting is kept off the grid as far as the Ages allow: mechanical parts are hand-assembled (Age III, before any machine exists), relay parts are made from
mechanical ones in the arc furnace, electronic parts from relay ones in the arc furnace. All art is drawn here in the module's worn industrial look."""
import json, os, random, zlib
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithir_wiretech"
BLACK, RIVET = (18, 19, 22), (176, 178, 184)
RED, GREEN = (214, 58, 48), (74, 190, 84)
TIERS = {  # id: (display, age, plate colour, accent)
    "mechanical": ("Mechanical", 3, (122, 92, 60), (200, 160, 80)),
    "relay": ("Relay", 4, (96, 100, 108), (214, 120, 60)),
    "electronic": ("Electronic", 6, (62, 70, 82), (110, 200, 235)),
}
KINDS = {  # id: (display, acts, has_input, has_output, lit_variant, glyph)
    "constant_combinator": ("Constant Combinator", False, False, True, False, "dots"),
    "arithmetic_combinator": ("Arithmetic Combinator", False, True, True, False, "plus"),
    "decider_combinator": ("Decider Combinator", False, True, True, False, "gt"),
    "selector_combinator": ("Selector Combinator", False, True, True, False, "bars"),
    "circuit_reader": ("Circuit Reader", False, False, True, False, "eye"),
    "circuit_gate": ("Circuit Gate", True, True, False, True, "slash"),
    "circuit_lamp": ("Circuit Lamp", True, True, False, True, "bulb"),
    "circuit_speaker": ("Circuit Speaker", True, True, False, True, "rings"),
}
MECH_CENTRE = {"constant_combinator": "minecraft:paper", "arithmetic_combinator": "minecraft:copper_ingot", "decider_combinator": "minecraft:iron_ingot",
               "selector_combinator": "minecraft:hopper", "circuit_reader": "minecraft:glass_pane", "circuit_gate": "minecraft:iron_bars",
               "circuit_lamp": "minecraft:lantern", "circuit_speaker": "minecraft:bell"}


def A(c): return tuple(c) + (255,)
def clamp(v): return max(0, min(255, v))
def shade(c, d): return tuple(clamp(v + d) for v in c)


def write(path, data):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(data, f, indent=2); f.write("\n")


def png(path, img):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
    if not os.path.exists(path): img.save(path)  # keep the 64 px art drop; these 16 px images are only placeholders for a fresh checkout


def plate(base, seed):
    img = Image.new("RGBA", (16, 16), A(base)); r = random.Random(seed)
    for x in range(16):
        for y in range(16):
            if r.random() < 0.22: img.putpixel((x, y), A(shade(base, r.choice((-10, 8)))))
    dark, light = shade(base, -34), shade(base, 30)
    for i in range(16):
        for p in ((i, 0), (i, 15), (0, i), (15, i)): img.putpixel(p, A(dark))
        if 1 <= i <= 14: img.putpixel((i, 1), A(light)); img.putpixel((1, i), A(light))
    for p in ((2, 2), (13, 2), (2, 13), (13, 13)): img.putpixel(p, A(RIVET))
    return img


def glyph(img, g, col, lit):
    d = ImageDraw.Draw(img)
    c = A(shade(col, 40) if lit else col)
    if g == "dots": [d.rectangle([x, 7, x + 1, 8], fill=c) for x in (4, 7, 10)]
    elif g == "plus": d.rectangle([7, 4, 8, 11], fill=c); d.rectangle([4, 7, 11, 8], fill=c)
    elif g == "gt": d.line([5, 4, 10, 7], fill=c, width=2); d.line([10, 8, 5, 11], fill=c, width=2)
    elif g == "bars": [d.rectangle([4, y, 4 + w, y + 1], fill=c) for y, w in ((4, 7), (7, 4), (10, 2))]
    elif g == "eye": d.ellipse([4, 5, 11, 10], outline=c); d.rectangle([7, 7, 8, 8], fill=c)
    elif g == "slash": d.line([5, 11, 10, 4], fill=c, width=2)
    elif g == "bulb": d.ellipse([5, 4, 10, 9], fill=c); d.rectangle([6, 10, 9, 11], fill=c)
    elif g == "rings": d.ellipse([3, 3, 12, 12], outline=c); d.ellipse([6, 6, 9, 9], outline=c)


def pins(img, has_in, has_out, side):
    d = ImageDraw.Draw(img)
    if (side == "back" and has_in) or (side == "front" and has_out):
        d.rectangle([3, 12, 4, 13], fill=A(RED)); d.rectangle([11, 12, 12, 13], fill=A(GREEN))


def coil(col):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); dark = shade(col, -60); light = shade(col, 40)
    for x in range(16):
        for y in range(16):
            d = (x - 7.5) ** 2 + (y - 7.5) ** 2
            if d <= 30: img.putpixel((x, y), A(col if int(d) % 5 else dark))
            if d <= 7: img.putpixel((x, y), A((58, 58, 64)))
            if d <= 2: img.putpixel((x, y), A(BLACK))
    for p in ((4, 4), (5, 3), (3, 6)): img.putpixel(p, A(light))
    for y in range(10, 16): img.putpixel((12, y), A(col))
    return img


def box_model(name, tex):
    write(f"assets/{NS}/models/block/{name}.json", {"parent": "minecraft:block/cube", "textures": tex})


def shaped(name, pattern, key, result, count=1):
    write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": key, "pattern": pattern, "result": {"id": result, "count": count}})


def mrec(name, inputs, result, count=1, energy=1800, time=100):
    write(f"data/{NS}/recipe/arc_furnace/{name}.json", {"type": f"{NS}:arc_furnace", "inputs": [{"ingredient": i, "count": c} for i, c in inputs],
                                                         "energy": energy, "time": time, "result": {"id": result, "count": count}})


def names_all(tiers, kinds):
    return [f"{NS}:{t}_{k}" for t in tiers for k in kinds]


def main():
    lang, gate = {}, {}
    for t, (tdisp, age, col, acc) in TIERS.items():
        png(f"assets/{NS}/textures/block/circuit_{t}_side.png", plate(col, age * 7 + 1))
        png(f"assets/{NS}/textures/block/circuit_{t}_top.png", plate(shade(col, 12), age * 7 + 2))
        names = []
        for k, (kdisp, acts, hin, hout, lit_v, g) in KINDS.items():
            name = f"{t}_{k}"; names.append(f"{NS}:{name}")
            for side, glyph_on in (("front", True), ("back", False)):
                for lit in ((False, True) if (lit_v and side == "front") else (False,)):
                    img = plate(shade(col, -6 if side == "back" else 0), zlib.crc32(f'{t}{k}{side}'.encode()) % 997)
                    if glyph_on: glyph(img, g, acc, lit)
                    pins(img, hin, hout, side)
                    if lit: ImageDraw.Draw(img).rectangle([2, 2, 13, 3], fill=A(shade(acc, 60)))
                    png(f"assets/{NS}/textures/block/circuit_{name}_{side}{'_lit' if lit else ''}.png", img)
            base = {"down": f"{NS}:block/circuit_{t}_top", "up": f"{NS}:block/circuit_{t}_top", "east": f"{NS}:block/circuit_{t}_side", "west": f"{NS}:block/circuit_{t}_side",
                    "south": f"{NS}:block/circuit_{name}_back", "particle": f"{NS}:block/circuit_{t}_side"}
            box_model(name, dict(base, north=f"{NS}:block/circuit_{name}_front"))
            if lit_v: box_model(name + "_lit", dict(base, north=f"{NS}:block/circuit_{name}_front_lit"))
            variants = {}
            for lit in ("false", "true"):
                for fc, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
                    m = name + ("_lit" if lit_v and lit == "true" else "")
                    v = {"model": f"{NS}:block/{m}"}
                    if y: v["y"] = y
                    variants[f"facing={fc},lit={lit}"] = v
            write(f"assets/{NS}/blockstates/{name}.json", {"variants": variants})
            write(f"assets/{NS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}})
            write(f"data/{NS}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}],
                                                                                              "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
            lang[f"block.{NS}.{name}"] = f"{tdisp} {kdisp}"
            # recipes: mechanical by hand, relay and electronic as upgrades at the arc furnace
            if t == "mechanical":
                shaped(f"craft/{name}", ["PKP", "NSN", "PPP"], {"P": "#minecraft:planks", "K": MECH_CENTRE[k], "N": "minecraft:iron_nugget", "S": "minecraft:string"}, f"{NS}:{name}")
            elif t == "relay":
                mrec(name, [(f"{NS}:mechanical_{k}", 1), ("minecraft:redstone", 2)], f"{NS}:{name}")
            else:
                mrec(name, [(f"{NS}:relay_{k}", 1), ("minecraft:quartz", 2)], f"{NS}:{name}", energy=3000, time=150)
        gate[f"circuit_{t}"] = {"age": age, "items": names}
        for cname, c in (("red", RED), ("green", GREEN)):
            wire = f"{t}_{cname}_circuit_wire"; gate[f"circuit_{t}"]["items"].append(f"{NS}:{wire}")
            png(f"assets/{NS}/textures/item/{wire}.png", coil(shade(c, {"mechanical": -25, "relay": 0, "electronic": 30}[t])))
            write(f"assets/{NS}/models/item/{wire}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{wire}"}})
            write(f"assets/{NS}/items/{wire}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{wire}"}})
            lang[f"item.{NS}.{wire}"] = f"{tdisp} {cname.capitalize()} Circuit Wire"
            dye = f"minecraft:{cname}_dye"
            if t == "mechanical": shaped(f"craft/{wire}", ["SSS", "SDS", "SSS"], {"S": "minecraft:string", "D": dye}, f"{NS}:{wire}", 4)
            elif t == "relay": mrec(wire, [(f"{NS}:copper_wire_coil", 1), (dye, 1)], f"{NS}:{wire}", 2, energy=1200, time=60)
            else: mrec(wire, [(f"{NS}:steel_wire_coil", 1), (dye, 1)], f"{NS}:{wire}", 2, energy=1800, time=80)
    # merge lang and age gate
    lp = os.path.join(ROOT, f"assets/{NS}/lang/en_us.json"); cur = json.load(open(lp)); cur.update(lang)
    write(f"assets/{NS}/lang/en_us.json", dict(sorted(cur.items())))
    gp = os.path.join(ROOT, f"data/{NS}/age_gate.json"); g = json.load(open(gp)); g.update(gate)
    write(f"data/{NS}/age_gate.json", g)
    # block tags: merge into the real mineable tags (pickaxe for relay and electronic, axe for the wooden mechanical tier)
    for tag, want in (("pickaxe", [n for n in names_all(TIERS, KINDS) if "mechanical" not in n]), ("axe", [n for n in names_all(TIERS, KINDS) if "mechanical" in n])):
        tp = os.path.join(ROOT, f"data/minecraft/tags/block/mineable/{tag}.json")
        d = json.load(open(tp)) if os.path.exists(tp) else {"values": []}
        d["values"] = d["values"] + [n for n in want if n not in d["values"]]
        write(f"data/minecraft/tags/block/mineable/{tag}.json", d)

if __name__ == "__main__":
    main()
