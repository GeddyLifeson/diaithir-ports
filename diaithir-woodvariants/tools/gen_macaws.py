#!/usr/bin/env python3
"""Macaw-style building set, written from the public feature lists of the Macaw's mods (bridges, doors, windows, fences,
roofs, trapdoors, paths, furniture): behaviour only, no code or art copied. Run by tools/gen.py (runpy), which passes the
shared helpers. Every wood row with "macaws": true gets the 25 wood pieces below; roofs and pavings come from
macaws.json. Own code, MIT."""
import json, os

# globals injected by gen.py: ID, SRC, put, woods, lang, axe, doors, trapdoors, optional, rec_cond

def merge_tag(path, values):
    p = os.path.join(RES, path)
    cur = []
    if os.path.exists(p):
        cur = json.load(open(p, encoding="utf-8")).get("values", [])
    seen = {(v if isinstance(v, str) else v["id"]) for v in cur}
    for v in values:
        k = v if isinstance(v, str) else v["id"]
        if k not in seen:
            cur.append(v); seen.add(k)
    put(path, {"values": cur})


def box(f, t, tex, faces=None, top=None, bottom=None):
    fs = {}
    for side in ("north", "south", "east", "west"):
        fs[side] = {"texture": tex}
    fs["up"] = {"texture": top or tex}
    fs["down"] = {"texture": bottom or top or tex}
    if faces:
        fs = {k: v for k, v in fs.items() if k in faces}
    return {"from": f, "to": t, "faces": fs}


def overlay(f, t, tex, faces):
    return {"from": f, "to": t, "faces": {s: {"texture": tex} for s in faces}}


def tex_of(w):
    return {"planks": w["planks_texture"], "log": w["log_texture"], "log_top": w["log_top_texture"], "particle": w["planks_texture"],
            "rope": f"{ID}:block/rope"}


def mirror_z(boxes):
    return [dict(b, **{"from": [b["from"][0], b["from"][1], 16 - b["to"][2]], "to": [b["to"][0], b["to"][1], 16 - b["from"][2]]}) for b in boxes]


# ---------------------------------------------------------------- fences
def fence_parts(kind):
    P, L = "#planks", "#log"
    if kind == "picket":
        post = [box([6, 0, 6], [10, 16, 10], L, top="#log_top")]
        side = [box([7, 3, 0], [9, 5, 8], P), box([7, 10, 0], [9, 12, 8], P),
                box([7, 0, 0], [9, 14, 2], P), box([7, 0, 3], [9, 14, 5], P), box([7, 0, 6], [9, 14, 8], P)]
    elif kind == "stockade":
        post = [box([5, 0, 5], [11, 16, 11], L, top="#log_top")]
        side = [box([6, 0, z], [10, 14 if (z // 2) % 2 else 16, z + 2], P) for z in (0, 2, 4, 6)]
    elif kind == "horse":
        post = [box([6, 0, 6], [10, 16, 10], L, top="#log_top")]
        side = [box([7, 2, 0], [9, 4, 8], P), box([7, 7, 0], [9, 9, 8], P), box([7, 12, 0], [9, 14, 8], P)]
    elif kind == "wired":
        post = [box([7, 0, 7], [9, 16, 9], L, top="#log_top")]
        side = [box([7.5, y, 0], [8.5, y + 1, 8], "#rope") for y in (3, 8, 13)]
    else:  # pyramid
        post = [box([6, 0, 6], [10, 14, 10], L, top="#log_top"), box([5, 14, 5], [11, 16, 11], P), box([7, 16, 7], [9, 18, 9], P)]
        side = [box([7, 4, 0], [9, 6, 8], P), box([7, 10, 0], [9, 12, 8], P)]
    return post, side


FENCES = ["picket", "stockade", "horse", "wired", "pyramid"]


def mk_fence(w, n, kind):
    mdl = f"{ID}:block/{n}"
    post, side = fence_parts(kind)
    put(f"assets/{ID}/models/block/{n}_post.json", {"textures": tex_of(w), "elements": post})
    put(f"assets/{ID}/models/block/{n}_side.json", {"textures": tex_of(w), "elements": side})
    put(f"assets/{ID}/models/block/{n}_inventory.json", {"textures": tex_of(w), "elements": post + side + mirror_z(side)})
    ms = [{"apply": {"model": f"{mdl}_post"}}]
    for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        ms.append({"when": {d: "true"}, "apply": {"model": f"{mdl}_side", "y": y}})
    put(f"assets/{ID}/blockstates/{n}.json", {"multipart": ms})
    put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{mdl}_inventory"}})


# ---------------------------------------------------------------- doors and trapdoors
DOORS = ["barn", "stable", "cottage", "glass", "shoji"]
TRAPS = ["beach", "barred", "glass", "cottage"]


def mk_door(w, n, kind):
    mdl = f"{ID}:block/{n}"
    for half in ("bottom", "top"):
        for op in (False, True):
            x0 = 13 if op else 0
            el = [box([x0, 0, 0], [x0 + 3, 16, 16], "#planks"),
                  overlay([x0 - 0.02, 0, 0], [x0 + 3.02, 16, 16], f"#ov_{half}", ("east", "west"))]
            t = dict(tex_of(w)); t[f"ov_{half}"] = f"{ID}:block/door_{kind}_{half}"
            for hinge in ("left", "right"):
                put(f"assets/{ID}/models/block/{n}_{half}_{hinge}{'_open' if op else ''}.json", {"textures": t, "elements": el})
    put(f"assets/{ID}/models/item/{n}.json", {"textures": {"all": w["planks_texture"], "particle": w["planks_texture"], f"ov": f"{ID}:block/door_{kind}_bottom"},
        "elements": [box([3, 0, 7], [13, 16, 9], "#all"), overlay([2.98, 0, 6.98], [13.02, 16, 9.02], "#ov", ("north", "south"))]})
    put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:item/{n}"}})
    v = {}
    for fd, base in (("east", 0), ("south", 90), ("west", 180), ("north", 270)):
        for half in ("lower", "upper"):
            hn = "bottom" if half == "lower" else "top"
            for hinge in ("left", "right"):
                for op in (False, True):
                    y = (base + (0 if not op else (90 if hinge == "left" else 270))) % 360
                    e = {"model": f"{mdl}_{hn}_{hinge}" + ("_open" if op else "")}
                    if y:
                        e["y"] = y
                    v[f"facing={fd},half={half},hinge={hinge},open={str(op).lower()}"] = e
    put(f"assets/{ID}/blockstates/{n}.json", {"variants": v})


def mk_trap(w, n, kind):
    mdl = f"{ID}:block/{n}"
    t = dict(tex_of(w)); t["ov"] = f"{ID}:block/trap_{kind}"
    parts = {
        "bottom": ([0, 0, 0], [16, 3, 16], ("up", "down"), 1, 2),
        "top": ([0, 13, 0], [16, 16, 16], ("up", "down"), 1, 2),
        "open": ([0, 0, 13], [16, 16, 16], ("north", "south"), 2, 1)}
    for part, (f, to, faces, _, _) in parts.items():
        f2 = list(f); t2 = list(to)
        ax = 1 if faces[0] == "up" else 2
        f2[ax] -= 0.02; t2[ax] += 0.02
        put(f"assets/{ID}/models/block/{n}_{part}.json", {"textures": t, "elements": [box(f, to, "#planks"), overlay(f2, t2, "#ov", faces)]})
    put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{mdl}_bottom"}})
    v = {}
    for fd, y in (("north", 0), ("south", 180), ("east", 90), ("west", 270)):
        for half in ("bottom", "top"):
            for op in (False, True):
                if not op:
                    e = {"model": f"{mdl}_{half}"}
                    if y:
                        e["y"] = y
                elif half == "bottom":
                    e = {"model": f"{mdl}_open"}
                    if y:
                        e["y"] = y
                else:
                    e = {"model": f"{mdl}_open", "x": 180, "y": (y + 180) % 360}
                v[f"facing={fd},half={half},open={str(op).lower()}"] = e
    put(f"assets/{ID}/blockstates/{n}.json", {"variants": v})


# ---------------------------------------------------------------- windows (panes with a wood frame)
WINDOWS = ["window", "four_pane_window", "barred_window"]


def window_parts(kind):
    glass = "#glass"
    if kind == "barred_window":
        post = [box([7, 0, 7], [9, 16, 9], "#planks")]
        side = [box([7, 0, 0], [9, 16, 2], "#planks"), box([7, 0, 3], [9, 16, 5], "#planks"), box([7, 14, 0], [9, 16, 8], "#planks"),
                box([7, 0, 0], [9, 2, 8], "#planks")]
        inv = [box([0, 0, 7], [2, 16, 9], "#planks"), box([4, 0, 7], [6, 16, 9], "#planks"), box([8, 0, 7], [10, 16, 9], "#planks"),
               box([12, 0, 7], [14, 16, 9], "#planks"), box([0, 14, 7], [16, 16, 9], "#planks"), box([0, 0, 7], [16, 2, 9], "#planks")]
        return post, side, inv
    post = [box([7, 0, 7], [9, 16, 9], "#planks")]
    side = [box([7.5, 2, 0], [8.5, 14, 8], glass), box([7, 14, 0], [9, 16, 8], "#planks"), box([7, 0, 0], [9, 2, 8], "#planks")]
    inv = [box([0, 2, 7.5], [16, 14, 8.5], glass), box([0, 14, 7], [16, 16, 9], "#planks"), box([0, 0, 7], [16, 2, 9], "#planks"),
           box([0, 2, 7], [2, 14, 9], "#planks"), box([14, 2, 7], [16, 14, 9], "#planks")]
    if kind == "four_pane_window":
        side.append(box([7, 7, 0], [9, 9, 8], "#planks"))
        post = [box([7, 0, 7], [9, 16, 9], "#planks")]
        inv += [box([0, 7, 7], [16, 9, 9], "#planks"), box([7, 2, 7], [9, 14, 9], "#planks")]
    return post, side, inv


def mk_window(w, n, kind):
    mdl = f"{ID}:block/{n}"
    t = dict(tex_of(w)); t["glass"] = f"{ID}:block/window_glass"
    post, side, inv = window_parts(kind)
    put(f"assets/{ID}/models/block/{n}_post.json", {"textures": t, "elements": post})
    put(f"assets/{ID}/models/block/{n}_side.json", {"textures": t, "elements": side})
    put(f"assets/{ID}/models/block/{n}_inventory.json", {"textures": t, "elements": inv})
    ms = [{"apply": {"model": f"{mdl}_post"}}]
    for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        ms.append({"when": {d: "true"}, "apply": {"model": f"{mdl}_side", "y": y}})
    put(f"assets/{ID}/blockstates/{n}.json", {"multipart": ms})
    put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{mdl}_inventory"}})


# ---------------------------------------------------------------- bridges
BRIDGES = ["plank_bridge", "log_bridge", "rope_bridge"]


def mk_bridge(w, n, kind):
    mdl = f"{ID}:block/{n}"
    t = tex_of(w)
    if kind == "plank_bridge":
        deck = [box([0, 12, 0], [16, 16, 16], "#planks"), box([0, 10, 1], [16, 12, 3], "#log", top="#log_top"), box([0, 10, 13], [16, 12, 15], "#log", top="#log_top")]
        rail = [box([0, 16, 0], [2, 22, 2], "#log", top="#log_top"), box([14, 16, 0], [16, 22, 2], "#log", top="#log_top"), box([2, 19, 0.5], [14, 21, 1.5], "#planks")]
    elif kind == "log_bridge":
        deck = [box([0, 12, 0], [16, 16, 4], "#log", top="#log_top"), box([0, 12, 4], [16, 16, 8], "#log", top="#log_top"),
                box([0, 12, 8], [16, 16, 12], "#log", top="#log_top"), box([0, 12, 12], [16, 16, 16], "#log", top="#log_top")]
        rail = [box([0, 16, 0], [3, 24, 3], "#log", top="#log_top"), box([13, 16, 0], [16, 24, 3], "#log", top="#log_top"), box([3, 20, 0], [13, 23, 3], "#log", top="#log_top")]
    else:
        deck = [box([0, 13, z], [16, 15, z + 3], "#planks") for z in (0, 4, 8, 12)] + [box([2, 12, 0], [3, 13, 16], "#rope"), box([13, 12, 0], [14, 13, 16], "#rope")]
        rail = [box([0, 15, 0], [2, 21, 2], "#log", top="#log_top"), box([14, 15, 0], [16, 21, 2], "#log", top="#log_top"),
                box([2, 18, 0.5], [14, 19, 1.5], "#rope"), box([2, 15, 0.5], [14, 16, 1.5], "#rope")]
    put(f"assets/{ID}/models/block/{n}_deck.json", {"textures": t, "elements": deck})
    put(f"assets/{ID}/models/block/{n}_rail.json", {"textures": t, "elements": rail})
    put(f"assets/{ID}/models/block/{n}_inventory.json", {"textures": t, "elements": deck + rail + mirror_z(rail)})
    ms = [{"apply": {"model": f"{mdl}_deck"}}]
    for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        ms.append({"when": {d: "true"}, "apply": {"model": f"{mdl}_rail", "y": y}})
    put(f"assets/{ID}/blockstates/{n}.json", {"multipart": ms})
    put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{mdl}_inventory"}})


# ---------------------------------------------------------------- decorative furniture (boxes match DecorBlock.java)
DECOR = ["coffee_table", "end_table", "cupboard", "counter", "bookcase"]
DECOR_FRONT = {"end_table": ("counter", [3, 2, 2.98]), "cupboard": ("cupboard", [1, 0, 0.98]), "counter": ("counter", [0, 0, -0.02]), "bookcase": ("bookcase", [0, 0, -0.02])}


def decor_elements(kind):
    if kind == "coffee_table":
        return [box([1, 7, 1], [15, 9, 15], "#planks")] + [box([x, 0, z], [x + 2, 7, z + 2], "#log", top="#log_top") for x, z in ((2, 2), (12, 2), (2, 12), (12, 12))]
    if kind == "end_table":
        return [box([2, 13, 2], [14, 16, 14], "#planks"), box([3, 2, 3], [13, 13, 13], "#planks")] + \
               [box([x, 0, z], [x + 2, 2, z + 2], "#log", top="#log_top") for x, z in ((3, 3), (11, 3), (3, 11), (11, 11))]
    if kind == "cupboard":
        return [box([1, 0, 1], [15, 16, 15], "#planks")]
    if kind == "counter":
        return [box([0, 0, 0], [16, 14, 16], "#planks"), box([0, 14, 0], [16, 16, 16], "#log", top="#log_top")]
    return [box([0, 0, 0], [16, 16, 16], "#planks")]


def mk_decor(w, n, kind):
    mdl = f"{ID}:block/{n}"
    t = tex_of(w)
    el = decor_elements(kind)
    if kind in DECOR_FRONT:
        tex, _ = DECOR_FRONT[kind]
        t["front"] = f"{ID}:block/decor_{tex}_front"
        body = el[0]
        f, to = list(body["from"]), list(body["to"])
        el = el + [overlay([f[0] + 0.5 if kind == "end_table" else f[0], f[1] if kind != "end_table" else 3, f[2] - 0.02],
                           [to[0] - 0.5 if kind == "end_table" else to[0], to[1] if kind != "end_table" else 13, f[2] + 0.02], "#front", ("north",))]
    put(f"assets/{ID}/models/block/{n}.json", {"textures": t, "elements": el})
    put(f"assets/{ID}/blockstates/{n}.json", {"variants": {
        f"facing={d}": {"model": mdl, "y": y} for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": mdl}})


# ---------------------------------------------------------------- recipes
def cond(w):
    if w["mod"] == "minecraft":
        return []
    if w["mod"] == "biomesoplenty":
        return [{"type": "neoforge:or", "values": [{"type": "neoforge:mod_loaded", "modid": "biomesoplenty"},
                                                   {"type": "neoforge:mod_loaded", "modid": "diaithir_overworld_biomes"}]}]
    return [{"type": "neoforge:mod_loaded", "modid": w["mod"]}]


def shaped(w, result, count, pattern, key):
    r = {"type": "minecraft:crafting_shaped", "category": "building", "key": key, "pattern": pattern, "result": {"id": f"{ID}:{result}", "count": count}}
    if cond(w):
        r["neoforge:conditions"] = cond(w)
    return r


def shapeless(w, result, ings):
    r = {"type": "minecraft:crafting_shapeless", "category": "building", "ingredients": ings, "result": {"id": f"{ID}:{result}", "count": 1}}
    if cond(w):
        r["neoforge:conditions"] = cond(w)
    return r


def wood_recipes(w):
    i = w["id"]; P = w["planks"]; L = w["log"]; S = "#c:rods/wooden"
    G = "minecraft:glass_pane"; GL = "minecraft:glass"
    recs = {
        f"{i}_picket_fence": shaped(w, f"{i}_picket_fence", 3, ["P P", "SSS", "P P"], {"P": P, "S": S}),
        f"{i}_stockade_fence": shaped(w, f"{i}_stockade_fence", 6, ["PPP", "PPP", "S S"], {"P": P, "S": S}),
        f"{i}_horse_fence": shaped(w, f"{i}_horse_fence", 3, ["SSS", "P P", "SSS"], {"P": P, "S": S}),
        f"{i}_wired_fence": shaped(w, f"{i}_wired_fence", 3, ["PTP", "SSS"], {"P": P, "S": S, "T": "minecraft:string"}),
        f"{i}_pyramid_fence": shaped(w, f"{i}_pyramid_fence", 3, ["SPS", "PSP"], {"P": P, "S": S}),
        f"{i}_barn_door": shapeless(w, f"{i}_barn_door", [f"{ID}:{i}_plank_door", S]),
        f"{i}_stable_door": shapeless(w, f"{i}_stable_door", [f"{ID}:{i}_plank_door", P]),
        f"{i}_cottage_door": shapeless(w, f"{i}_cottage_door", [f"{ID}:{i}_plank_door", G]),
        f"{i}_glass_door": shapeless(w, f"{i}_glass_door", [f"{ID}:{i}_plank_door", GL]),
        f"{i}_shoji_door": shapeless(w, f"{i}_shoji_door", [f"{ID}:{i}_plank_door", "minecraft:paper"]),
        f"{i}_beach_trapdoor": shapeless(w, f"{i}_beach_trapdoor", [f"{ID}:{i}_plank_trapdoor", S]),
        f"{i}_barred_trapdoor": shapeless(w, f"{i}_barred_trapdoor", [f"{ID}:{i}_plank_trapdoor", "minecraft:iron_bars"]),
        f"{i}_glass_trapdoor": shapeless(w, f"{i}_glass_trapdoor", [f"{ID}:{i}_plank_trapdoor", G]),
        f"{i}_cottage_trapdoor": shapeless(w, f"{i}_cottage_trapdoor", [f"{ID}:{i}_plank_trapdoor", "minecraft:paper"]),
        f"{i}_window": shaped(w, f"{i}_window", 4, ["SGS", "SGS"], {"S": S, "G": G}),
        f"{i}_four_pane_window": shapeless(w, f"{i}_four_pane_window", [f"{ID}:{i}_window", S]),
        f"{i}_barred_window": shapeless(w, f"{i}_barred_window", [f"{ID}:{i}_window", S, S]),
        f"{i}_plank_bridge": shaped(w, f"{i}_plank_bridge", 2, ["SPS", "PPP"], {"P": P, "S": S}),
        f"{i}_log_bridge": shaped(w, f"{i}_log_bridge", 2, ["LLL", "SPS"], {"L": L, "S": S, "P": P}),
        f"{i}_rope_bridge": shaped(w, f"{i}_rope_bridge", 2, ["T T", "PPP"], {"P": P, "T": "minecraft:string"}),
        f"{i}_coffee_table": shaped(w, f"{i}_coffee_table", 1, ["PPP", "SSS"], {"P": P, "S": S}),
        f"{i}_end_table": shaped(w, f"{i}_end_table", 1, ["PPP", "SDS"], {"P": P, "S": S, "D": f"{ID}:{i}_plank_door"}),
        f"{i}_cupboard": shaped(w, f"{i}_cupboard", 1, ["PPP", "PDP", "PPP"], {"P": P, "D": f"{ID}:{i}_plank_door"}),
        f"{i}_counter": shaped(w, f"{i}_counter", 2, ["LLL", "PSP", "PPP"], {"P": P, "S": S, "L": L}),
        f"{i}_bookcase": shaped(w, f"{i}_bookcase", 1, ["PPP", "BBB", "PPP"], {"P": P, "B": "minecraft:book"}),
    }
    return recs


# ---------------------------------------------------------------- roofs and pavings
def stairs_state(model):
    v = {}
    base = {"east": 0, "south": 90, "west": 180, "north": 270}
    for fd, b in base.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                m = model + ("" if shape == "straight" else "_inner" if shape.startswith("inner") else "_outer")
                if half == "bottom":
                    y = b + (-90 if shape.endswith("left") and shape != "straight" else 0)
                    e = {"model": m, "uvlock": True}
                else:
                    y = b + (90 if shape.endswith("right") and shape != "straight" else 0)
                    e = {"model": m, "x": 180, "uvlock": True}
                y %= 360
                if y:
                    e["y"] = y
                v[f"facing={fd},half={half},shape={shape}"] = e
    return v


def loot(name, double=False):
    pool = {"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": f"{ID}:{name}"}]}
    if double:
        pool["entries"][0]["functions"] = [{"function": "minecraft:set_count", "count": 2, "add": False,
            "conditions": [{"condition": "minecraft:block_state_property", "block": f"{ID}:{name}", "properties": {"type": "double"}}]}]
    return {"type": "minecraft:block", "random_sequence": f"{ID}:blocks/{name}", "pools": [pool]}


def pretty(s):
    return " ".join(x.capitalize() for x in s.split("_"))


def roof_and_paving(cfg):
    names = []
    for c in cfg["roof_colors"]:
        t = {"bottom": f"{ID}:block/roof_{c}", "top": f"{ID}:block/roof_{c}", "side": f"{ID}:block/roof_{c}", "particle": f"{ID}:block/roof_{c}"}
        b, s, sl = f"{c}_roof", f"{c}_roof_stairs", f"{c}_roof_slab"
        put(f"assets/{ID}/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": t["side"]}})
        put(f"assets/{ID}/blockstates/{b}.json", {"variants": {"": {"model": f"{ID}:block/{b}"}}})
        for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
            put(f"assets/{ID}/models/block/{s}{suffix}.json", {"parent": f"minecraft:block/{parent}", "textures": t})
        put(f"assets/{ID}/blockstates/{s}.json", {"variants": stairs_state(f"{ID}:block/{s}")})
        put(f"assets/{ID}/models/block/{sl}.json", {"parent": "minecraft:block/slab", "textures": t})
        put(f"assets/{ID}/models/block/{sl}_top.json", {"parent": "minecraft:block/slab_top", "textures": t})
        put(f"assets/{ID}/blockstates/{sl}.json", {"variants": {"type=bottom": {"model": f"{ID}:block/{sl}"}, "type=top": {"model": f"{ID}:block/{sl}_top"},
                                                                 "type=double": {"model": f"{ID}:block/{b}"}}})
        for n in (b, s, sl):
            put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:block/{n}"}})
            put(f"data/{ID}/loot_table/blocks/{n}.json", loot(n, n == sl))
            names.append(n)
        lang[f"block.{ID}.{b}"] = f"{pretty(c)} Roof"
        lang[f"block.{ID}.{s}"] = f"{pretty(c)} Roof Stairs"
        lang[f"block.{ID}.{sl}"] = f"{pretty(c)} Roof Slab"
        terra = f"minecraft:{c}_terracotta"
        put(f"data/{ID}/recipe/{b}.json", {"type": "minecraft:crafting_shaped", "category": "building", "key": {"T": terra}, "pattern": ["TT", "TT"],
                                          "result": {"id": f"{ID}:{b}", "count": 4}})
        put(f"data/{ID}/recipe/{s}.json", {"type": "minecraft:crafting_shaped", "category": "building", "key": {"R": f"{ID}:{b}"}, "pattern": ["R  ", "RR ", "RRR"],
                                          "result": {"id": f"{ID}:{s}", "count": 4}})
        put(f"data/{ID}/recipe/{sl}.json", {"type": "minecraft:crafting_shaped", "category": "building", "key": {"R": f"{ID}:{b}"}, "pattern": ["RRR"],
                                           "result": {"id": f"{ID}:{sl}", "count": 6}})
    paving = []
    for pal, src in cfg["paving_palettes"].items():
        for pat in cfg["paving_patterns"]:
            n = f"{pat}_paving_{pal}"
            put(f"assets/{ID}/models/block/{n}.json", {"textures": {"all": f"{ID}:block/paving_{pat}_{pal}", "particle": f"{ID}:block/paving_{pat}_{pal}"},
                "elements": [box([0, 0, 0], [16, 15, 16], "#all")]})
            put(f"assets/{ID}/blockstates/{n}.json", {"variants": {"": {"model": f"{ID}:block/{n}"}}})
            put(f"assets/{ID}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:block/{n}"}})
            put(f"data/{ID}/loot_table/blocks/{n}.json", loot(n))
            put(f"data/{ID}/recipe/{n}.json", {"type": "minecraft:stonecutting", "ingredient": src, "result": {"id": f"{ID}:{n}", "count": 1}})
            lang[f"block.{ID}.{n}"] = f"{pretty(pal)} {pretty(pat)} Paving"
            paving.append(n)
    return names, paving


def run():
    cfg = json.load(open(os.path.join(SRC, f"data/{ID}/macaws.json"), encoding="utf-8"))
    pieces = [(f, f"{f}_fence", f"{f.capitalize()} Fence", mk_fence) for f in FENCES]
    pieces += [(d, f"{d}_door", f"{d.capitalize()} Door", mk_door) for d in DOORS]
    pieces += [(t, f"{t}_trapdoor", f"{t.capitalize()} Trapdoor", mk_trap) for t in TRAPS]
    pieces += [(k, k, pretty(k), mk_window) for k in WINDOWS]
    pieces += [(k, k, pretty(k), mk_bridge) for k in BRIDGES]
    pieces += [(k, k, pretty(k), mk_decor) for k in DECOR]
    fences, bars, bridges, count = [], [], [], 0
    for w in woods:
        if not w.get("macaws"):
            continue
        for kind, suffix, eng, fn in pieces:
            n = f"{w['id']}_{suffix}"
            fn(w, n, kind)
            lang[f"block.{ID}.{n}"] = f"{w['name']} {eng}"
            axe.append(f"{ID}:{n}")
            conds = [{"condition": "minecraft:survives_explosion"}]
            if suffix.endswith("_door"):
                conds.insert(0, {"condition": "minecraft:block_state_property", "block": f"{ID}:{n}", "properties": {"half": "lower"}})
                doors.append(f"{ID}:{n}")
            if suffix.endswith("_trapdoor"):
                trapdoors.append(f"{ID}:{n}")
            if suffix.endswith("_fence"):
                fences.append(f"{ID}:{n}")
            if suffix in WINDOWS:
                bars.append(f"{ID}:{n}")
            if suffix in BRIDGES:
                bridges.append(f"{ID}:{n}")
            lt = {"type": "minecraft:block", "random_sequence": f"{ID}:blocks/{n}",
                  "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": conds, "entries": [{"type": "minecraft:item", "name": f"{ID}:{n}"}]}]}
            if w["mod"] != "minecraft":
                lt = dict({"neoforge:conditions": cond(w)}, **lt)
            put(f"data/{ID}/loot_table/blocks/{n}.json", lt)
            count += 1
        for name, r in wood_recipes(w).items():
            put(f"data/{ID}/recipe/{name}.json", r)
    rn, pn = roof_and_paving(cfg)
    merge_tag("data/minecraft/tags/block/fences.json", optional(fences))
    merge_tag("data/minecraft/tags/block/wooden_fences.json", optional(fences))
    merge_tag("data/minecraft/tags/item/fences.json", optional(fences))
    merge_tag("data/minecraft/tags/item/wooden_fences.json", optional(fences))
    merge_tag("data/minecraft/tags/block/bars.json", optional(bars))
    merge_tag("data/minecraft/tags/block/mineable/pickaxe.json", [f"{ID}:{n}" for n in rn + pn])
    merge_tag("data/minecraft/tags/block/stairs.json", [f"{ID}:{n}" for n in rn if n.endswith("_stairs")])
    merge_tag("data/minecraft/tags/block/slabs.json", [f"{ID}:{n}" for n in rn if n.endswith("_slab")])
    merge_tag("data/minecraft/tags/item/stairs.json", [f"{ID}:{n}" for n in rn if n.endswith("_stairs")])
    merge_tag("data/minecraft/tags/item/slabs.json", [f"{ID}:{n}" for n in rn if n.endswith("_slab")])
    merge_tag(f"data/{ID}/tags/block/bridges.json", optional(bridges))
    print(count, "macaws wood blocks,", len(rn), "roof blocks,", len(pn), "pavings")


run()
