#!/usr/bin/env python3
"""Writes blockstates, models, item models, lang, recipes, loot tables and tags for every row of
src/main/resources/data/diaithirwoodvariants/woods.json. Run from the module folder:  python3 -I tools/gen.py
Adding a wood = adding a row to woods.json and running this. Own code, MIT."""
import json, os, shutil

ID = "diaithirwoodvariants"
HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(HERE, "src/main/resources")
woods = json.load(open(f"{RES}/data/{ID}/woods.json", encoding="utf-8"))["woods"]

PIECES = [("chair", "Sèiter"), ("stool", "Kriepun"), ("bench", "Beingi"), ("table", "Bòird"), ("wall", "Balda"),
          ("vertical_planks", "Klàr dìrech"), ("framed_planks", "Klàr kèisi")]

for sub in ("assets", "data/" + ID + "/recipe", "data/" + ID + "/loot_table", "data/minecraft"):
    shutil.rmtree(os.path.join(RES, sub), ignore_errors=True)


def put(path, obj):
    p = os.path.join(RES, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)
        f.write("\n")


def box(f, t, tex, top=None, bottom=None):
    faces = {}
    for side in ("north", "south", "east", "west"):
        faces[side] = {"texture": tex}
    faces["up"] = {"texture": top or tex}
    faces["down"] = {"texture": bottom or top or tex}
    return {"from": f, "to": t, "faces": faces}


def legs(b):
    return box(b[:3], b[3:], "#log", "#log_top")


CHAIR = [(2, 7, 2, 14, 9, 14, "p"), (2, 0, 2, 4, 7, 4, "l"), (12, 0, 2, 14, 7, 4, "l"), (2, 0, 12, 4, 7, 14, "l"),
         (12, 0, 12, 14, 7, 14, "l"), (2, 9, 12, 14, 16, 14, "p")]
STOOL = [(3, 7, 3, 13, 9, 13, "p"), (3, 0, 3, 5, 7, 5, "l"), (11, 0, 3, 13, 7, 5, "l"), (3, 0, 11, 5, 7, 13, "l"),
         (11, 0, 11, 13, 7, 13, "l")]
BENCH = [(0, 7, 3, 16, 9, 13, "p"), (1, 0, 4, 3, 7, 6, "l"), (13, 0, 4, 15, 7, 6, "l"), (1, 0, 10, 3, 7, 12, "l"),
         (13, 0, 10, 15, 7, 12, "l")]
TABLE = [(0, 13, 0, 16, 16, 16, "p"), (1, 0, 1, 4, 13, 4, "l"), (12, 0, 1, 15, 13, 4, "l"), (1, 0, 12, 4, 13, 15, "l"),
         (12, 0, 12, 15, 13, 15, "l")]


def elements(spec):
    out = []
    for *c, kind in spec:
        out.append(legs(c) if kind == "l" else box(c[:3], c[3:], "#planks"))
    return out


def textures(w):
    return {"planks": w["planks_texture"], "log": w["log_texture"], "log_top": w["log_top_texture"],
            "particle": w["planks_texture"]}


def rec(result, count, pattern=None, key=None, shapeless=None, w=None):
    r = {"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": w["mod"]}]}
    if shapeless:
        r.update({"type": "minecraft:crafting_shapeless", "category": "building", "ingredients": shapeless,
                  "result": {"id": f"{ID}:{result}", "count": count}})
    else:
        r.update({"type": "minecraft:crafting_shaped", "category": "building", "key": key, "pattern": pattern,
                  "result": {"id": f"{ID}:{result}", "count": count}})
    return r


lang = {}
walls, axe = [], []
for w in woods:
    P, S = w["planks"], "#c:rods/wooden"
    for suffix, dia in PIECES:
        name = f"{w['id']}_{suffix}"
        axe.append(f"{ID}:{name}")
        lang[f"block.{ID}.{name}"] = f"{dia} {w['gen']}"
        put(f"data/{ID}/loot_table/blocks/{name}.json", {
            "type": "minecraft:block", "random_sequence": f"{ID}:blocks/{name}",
            "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}],
                       "entries": [{"type": "minecraft:item", "name": f"{ID}:{name}"}]}]})
        put(f"assets/{ID}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:item/{name}"
                                                          if suffix == "wall" else f"{ID}:block/{name}"}})
        mdl = f"{ID}:block/{name}"
        if suffix in ("chair", "bench"):
            spec = CHAIR if suffix == "chair" else BENCH
            put(f"assets/{ID}/models/block/{name}.json", {"textures": textures(w), "elements": elements(spec)})
            put(f"assets/{ID}/blockstates/{name}.json", {"variants": {
                f"facing={d}": {"model": mdl, "y": y} for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
        elif suffix in ("stool", "table"):
            spec = STOOL if suffix == "stool" else TABLE
            put(f"assets/{ID}/models/block/{name}.json", {"textures": textures(w), "elements": elements(spec)})
            put(f"assets/{ID}/blockstates/{name}.json", {"variants": {"": {"model": mdl}}})
        elif suffix == "wall":
            walls.append(f"{ID}:{name}")
            t = {"wall": w["planks_texture"]}
            for part in ("post", "side", "side_tall"):
                put(f"assets/{ID}/models/block/{name}_{part}.json", {"parent": f"minecraft:block/template_wall_{part}", "textures": t})
            put(f"assets/{ID}/models/item/{name}.json", {"parent": "minecraft:block/wall_inventory", "textures": t})
            ms = [{"when": {"up": "true"}, "apply": {"model": f"{mdl}_post"}}]
            for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
                ms.append({"when": {d: "low"}, "apply": {"model": f"{mdl}_side", "y": y, "uvlock": True}})
                ms.append({"when": {d: "tall"}, "apply": {"model": f"{mdl}_side_tall", "y": y, "uvlock": True}})
            put(f"assets/{ID}/blockstates/{name}.json", {"multipart": ms})
        elif suffix == "vertical_planks":
            tex = {"all": w["planks_texture"], "particle": w["planks_texture"]}
            faces = {s: {"texture": "#all", "rotation": 90, "cullface": s} for s in ("north", "south", "east", "west")}
            faces["up"] = {"texture": "#all", "cullface": "up"}
            faces["down"] = {"texture": "#all", "cullface": "down"}
            put(f"assets/{ID}/models/block/{name}.json", {"textures": tex, "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}]})
            put(f"assets/{ID}/blockstates/{name}.json", {"variants": {"": {"model": mdl}}})
        else:
            put(f"assets/{ID}/models/block/{name}.json", {"parent": "minecraft:block/cube_column",
                "textures": {"side": w["planks_texture"], "end": w["log_top_texture"], "particle": w["planks_texture"]}})
            put(f"assets/{ID}/blockstates/{name}.json", {"variants": {"": {"model": mdl}}})
    put(f"data/{ID}/recipe/{w['id']}_chair.json", rec(f"{w['id']}_chair", 1, ["S  ", "SPP", "S S"], {"S": S, "P": P}, w=w))
    put(f"data/{ID}/recipe/{w['id']}_stool.json", rec(f"{w['id']}_stool", 2, ["PP", "SS"], {"S": S, "P": P}, w=w))
    put(f"data/{ID}/recipe/{w['id']}_bench.json", rec(f"{w['id']}_bench", 2, ["PPP", "S S"], {"S": S, "P": P}, w=w))
    put(f"data/{ID}/recipe/{w['id']}_table.json", rec(f"{w['id']}_table", 1, ["PPP", "S S", "S S"], {"S": S, "P": P}, w=w))
    put(f"data/{ID}/recipe/{w['id']}_wall.json", rec(f"{w['id']}_wall", 6, ["PPP", "PPP"], {"P": P}, w=w))
    put(f"data/{ID}/recipe/{w['id']}_vertical_planks.json", rec(f"{w['id']}_vertical_planks", 3, shapeless=[P, P, P], w=w))
    put(f"data/{ID}/recipe/{w['id']}_framed_planks.json", rec(f"{w['id']}_framed_planks", 4, ["PS", "SP"], {"S": S, "P": P}, w=w))

put(f"assets/{ID}/lang/en_us.json", lang)
put("data/minecraft/tags/block/mineable/axe.json", {"values": axe})
put("data/minecraft/tags/block/walls.json", {"values": walls})
put("data/minecraft/tags/item/walls.json", {"values": walls})
print(len(woods), "woods,", len(axe), "blocks")
