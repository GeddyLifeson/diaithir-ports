#!/usr/bin/env python3
"""Fills gaps in the Biomes O' Plenty re-creation with data-driven blocks, written from the public feature lists
(behaviour and general look only; no code or art copied). Adds rows to compat_ids.json under the diaithir_overworld_biomes
namespace (kinds the CompatBlocks loader already knows), and writes blockstates, models, items, loot, lang, tags and
procedural 16 px textures. Re-runnable; own code, MIT. Run: python3 -I tools/gen_flora_gap.py"""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(HERE, "src/main/resources")
NS = "diaithir_overworld_biomes"
A = f"{RES}/assets/{NS}"
D = f"{RES}/data/{NS}"
rows = []
lang = {}


def put(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False)


def pretty(s):
    return " ".join(x.capitalize() for x in s.split("_"))


def loot(n, double=False):
    e = {"type": "minecraft:item", "name": f"{NS}:{n}"}
    if double:
        e["functions"] = [{"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [
            {"condition": "minecraft:block_state_property", "block": f"{NS}:{n}", "properties": {"type": "double"}}]}]
    put(f"{D}/loot_table/blocks/{n}.json", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [e], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def item(n, model=None):
    put(f"{A}/items/{n}.json", {"model": {"type": "minecraft:model", "model": model or f"{NS}:block/{n}"}})


def add(n, kind, eng=None):
    rows.append({"id": n, "kind": kind})
    lang[f"block.{NS}.{n}"] = eng or pretty(n)


def stairs_state(model):
    v = {}
    for fd, b in {"east": 0, "south": 90, "west": 180, "north": 270}.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                m = model + ("" if shape == "straight" else "_inner" if shape.startswith("inner") else "_outer")
                if half == "bottom":
                    y = b + (-90 if shape.endswith("left") else 0); e = {"model": m, "uvlock": True}
                else:
                    y = b + (90 if shape.endswith("right") else 0); e = {"model": m, "x": 180, "uvlock": True}
                y %= 360
                if y:
                    e["y"] = y
                v[f"facing={fd},half={half},shape={shape}"] = e
    return v


def shapes(base, tex, kinds=("stairs", "slab"), wall=False, top=None):
    """stairs/slab/wall for a block whose texture is `tex` (a resource id)."""
    t = {"bottom": tex, "top": top or tex, "side": tex, "particle": tex}
    if "stairs" in kinds:
        n = f"{base}_stairs"
        add(n, "stairs")
        for suf, par in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
            put(f"{A}/models/block/{n}{suf}.json", {"parent": f"minecraft:block/{par}", "textures": t})
        put(f"{A}/blockstates/{n}.json", {"variants": stairs_state(f"{NS}:block/{n}")})
        item(n); loot(n)
    if "slab" in kinds:
        n = f"{base}_slab"
        add(n, "slab")
        put(f"{A}/models/block/{n}.json", {"parent": "minecraft:block/slab", "textures": t})
        put(f"{A}/models/block/{n}_top.json", {"parent": "minecraft:block/slab_top", "textures": t})
        put(f"{A}/models/block/{n}_double.json", {"parent": "minecraft:block/cube_all", "textures": {"all": tex}})
        put(f"{A}/blockstates/{n}.json", {"variants": {"type=bottom": {"model": f"{NS}:block/{n}"}, "type=top": {"model": f"{NS}:block/{n}_top"},
                                                       "type=double": {"model": f"{NS}:block/{n}_double"}}})
        item(n); loot(n, True)
    if wall:
        n = f"{base}_wall"
        add(n, "wall")
        w = {"wall": tex}
        for part in ("post", "side", "side_tall"):
            put(f"{A}/models/block/{n}_{part}.json", {"parent": f"minecraft:block/template_wall_{part}", "textures": w})
        put(f"{A}/models/item/{n}.json", {"parent": "minecraft:block/wall_inventory", "textures": w})
        ms = [{"when": {"up": "true"}, "apply": {"model": f"{NS}:block/{n}_post"}}]
        for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
            ms.append({"when": {d: "low"}, "apply": {"model": f"{NS}:block/{n}_side", "y": y, "uvlock": True}})
            ms.append({"when": {d: "tall"}, "apply": {"model": f"{NS}:block/{n}_side_tall", "y": y, "uvlock": True}})
        put(f"{A}/blockstates/{n}.json", {"multipart": ms})
        item(n, f"{NS}:item/{n}"); loot(n)


def cube(n, tex_name, img, kind="plain", eng=None):
    add(n, kind, eng)
    put(f"{A}/models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{tex_name}"}})
    put(f"{A}/blockstates/{n}.json", {"variants": {"": {"model": f"{NS}:block/{n}"}}})
    item(n); loot(n)
    save(img, tex_name)


def save(img, name):
    p = f"{A}/textures/block/{name}.png"
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def clamp(v):
    return max(0, min(255, int(v)))


def noise(base, seed, amp=14, size=16):
    r = random.Random(seed)
    im = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            k = r.randint(-amp, amp)
            im.putpixel((x, y), tuple(clamp(c + k) for c in base) + (255,))
    return im


def bricks(base, seed):
    im = noise(base, seed, 8)
    mortar = tuple(clamp(c * 0.6) for c in base) + (255,)
    for y in range(16):
        for x in range(16):
            off = 4 if (y // 4) % 2 else 0
            if y % 4 == 3 or (x + off) % 8 == 0:
                im.putpixel((x, y), mortar)
    return im


def plant(n, seed, stem, bloom, kind="bush"):
    """16x16 cross-model plant sprite"""
    r = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if kind == "grass":
        for x0 in (2, 5, 8, 11, 13):
            h = r.randint(7, 14)
            for y in range(16 - h, 16):
                x = x0 + (1 if y < 16 - h // 2 and r.random() < 0.5 else 0)
                im.putpixel((min(x, 15), y), tuple(clamp(c + r.randint(-14, 14)) for c in stem) + (255,))
    elif kind == "flower":
        for x0, hh in ((4, 9), (8, 12), (12, 8)):
            for y in range(16 - hh, 16):
                im.putpixel((x0, y), tuple(clamp(c + r.randint(-10, 10)) for c in stem) + (255,))
            top = 16 - hh
            for dx, dy in ((0, 0), (-1, 0), (1, 0), (0, -1), (0, 1)):
                if 0 <= x0 + dx < 16 and 0 <= top + dy < 16:
                    im.putpixel((x0 + dx, top + dy), bloom + (255,))
            im.putpixel((x0, top), tuple(clamp(c * 0.8) for c in bloom) + (255,))
    else:  # bush
        for y in range(4, 16):
            for x in range(2, 14):
                if (x - 8) ** 2 / 36 + (y - 10) ** 2 / 36 <= 1 and r.random() < 0.85:
                    im.putpixel((x, y), tuple(clamp(c + r.randint(-18, 18)) for c in stem) + (255,))
    save(im, n)
    add(n, "plant")
    put(f"{A}/models/block/{n}.json", {"parent": "minecraft:block/cross", "textures": {"cross": f"{NS}:block/{n}"}})
    put(f"{A}/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:block/{n}"}})
    put(f"{A}/blockstates/{n}.json", {"variants": {"": {"model": f"{NS}:block/{n}"}}})
    item(n, f"{NS}:item/{n}"); loot(n)


def build():
    # stairs, slabs and walls for the sandstones and smooth/cut forms the base re-creation already has
    for c in ("black", "orange", "white"):
        shapes(f"{c}_sandstone", f"biomesoplenty:block/{c}_sandstone", wall=True)
        shapes(f"smooth_{c}_sandstone", f"biomesoplenty:block/smooth_{c}_sandstone")
        shapes(f"cut_{c}_sandstone", f"biomesoplenty:block/cut_{c}_sandstone", kinds=("slab",))
    # rose quartz family
    shapes("rose_quartz", "biomesoplenty:block/rose_quartz_block")
    rq = Image.open(f"{RES}/assets/biomesoplenty/textures/block/rose_quartz_block.png").convert("RGBA")
    br = bricks((226, 170, 182), "rqb")
    cube("rose_quartz_bricks", "rose_quartz_bricks", br)
    shapes("rose_quartz_bricks", f"{NS}:block/rose_quartz_bricks")
    # mineral and ground blocks
    cube("salt_block", "salt_block", noise((232, 230, 224), "salt", 9))
    cube("salt_bricks", "salt_bricks", bricks((226, 224, 216), "saltb")); shapes("salt_bricks", f"{NS}:block/salt_bricks")
    cube("ash_block", "ash_block", noise((92, 90, 92), "ash", 12))
    cube("ash_bricks", "ash_bricks", bricks((84, 82, 86), "ashb")); shapes("ash_bricks", f"{NS}:block/ash_bricks")
    cube("brimstone_block", "brimstone_block", noise((200, 168, 40), "brim", 22))
    cube("brimstone_bricks", "brimstone_bricks", bricks((190, 150, 38), "brimb")); shapes("brimstone_bricks", f"{NS}:block/brimstone_bricks")
    cube("peat_block", "peat_block", noise((70, 50, 34), "peat", 10))
    cube("peat_bricks", "peat_bricks", bricks((74, 54, 38), "peatb")); shapes("peat_bricks", f"{NS}:block/peat_bricks")
    # flora
    G = (88, 130, 56)
    plant("bush", "bush", (62, 108, 44), (0, 0, 0))
    plant("dead_grass", "dg", (150, 124, 78), (0, 0, 0), "grass")
    plant("desert_grass", "dsg", (186, 170, 96), (0, 0, 0), "grass")
    plant("dune_grass", "dng", (150, 176, 92), (0, 0, 0), "grass")
    plant("sea_oats", "so", (176, 168, 96), (0, 0, 0), "grass")
    plant("heather", "he", G, (156, 72, 156), "flower")
    plant("thistle", "th", G, (170, 70, 190), "flower")
    plant("fireweed", "fw", G, (214, 70, 120), "flower")
    plant("marsh_marigold", "mm", G, (236, 196, 40), "flower")
    plant("purple_aster", "pa", G, (130, 90, 200), "flower")
    plant("white_clover", "wc", G, (238, 238, 232), "flower")
    plant("tundra_moss", "tm", (86, 118, 80), (0, 0, 0), "grass")


build()
p = f"{RES}/data/{NS}/compat_ids.json"
ids = json.load(open(p, encoding="utf-8"))
mine = ids.get(NS, [])
have = {r["id"] for r in mine}
for r in rows:
    if r["id"] not in have:
        mine.append(r)
ids[NS] = mine
with open(p, "w", encoding="utf-8") as f:
    json.dump(ids, f, indent=1, ensure_ascii=False)
lp = f"{A}/lang/en_us.json"
cur = json.load(open(lp, encoding="utf-8"))
cur.update(lang)
with open(lp, "w", encoding="utf-8") as f:
    json.dump(cur, f, indent=1, ensure_ascii=False)


def tag(path, ids_):
    full = f"{RES}/data/minecraft/tags/{path}.json"
    vals = json.load(open(full))["values"] if os.path.exists(full) else []
    for i in ids_:
        if i not in vals:
            vals.append(i)
    put(full, {"values": vals})


for kind, tg in (("stairs", "stairs"), ("slab", "slabs"), ("wall", "walls")):
    ids_ = [f"{NS}:{r['id']}" for r in rows if r["kind"] == kind]
    tag(f"block/{tg}", ids_); tag(f"item/{tg}", ids_)
tag("block/mineable/pickaxe", [f"{NS}:{r['id']}" for r in rows if r["kind"] in ("stairs", "slab", "wall", "plain")])
print(len(rows), "gap blocks")
