#!/usr/bin/env python3
"""Generates every block/item model, blockstate, texture, lang and sound of the Diaithir Create port.
All output is original; nothing is read from or derived from the upstream asset tree except resource *names*
that the code requires (they are discovered from registry and partial-model dumps)."""
import json, os, re, sys, shutil, itertools
sys.path.insert(0, os.path.dirname(__file__))
from tex import *
from look import *
import art
import icons
import icons64
import design

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(HERE + "/../..")
RES = ROOT + "/src/main/resources"
A = RES + "/assets/create"
REG = json.load(open(HERE + "/data/registries.json"))
PARTIALS = [l.strip() for l in open(HERE + "/data/partials.txt") if l.strip()]
BLOCKS = {b["id"]: b for b in REG["blocks"]}
ITEMS = {i["id"]: i for i in REG["items"]}

FUV = {
    "up": lambda x0, y0, z0, x1, y1, z1: (x0, z0, x1, z1),
    "down": lambda x0, y0, z0, x1, y1, z1: (x0, 16 - z1, x1, 16 - z0),
    "north": lambda x0, y0, z0, x1, y1, z1: (16 - x1, 16 - y1, 16 - x0, 16 - y0),
    "south": lambda x0, y0, z0, x1, y1, z1: (x0, 16 - y1, x1, 16 - y0),
    "west": lambda x0, y0, z0, x1, y1, z1: (z0, 16 - y1, z1, 16 - y0),
    "east": lambda x0, y0, z0, x1, y1, z1: (16 - z1, 16 - y1, 16 - z0, 16 - y0),
}

def _span(a, b):
    if a >= 0 and b <= 16 and a <= b:
        return a, b
    lo, hi = min(a, b), max(a, b)
    if hi - lo >= 16:
        lo, hi = 0, 16
    else:
        w = hi - lo
        lo = min(max(lo, 0), 16 - w); hi = lo + w
    return (lo, hi) if a <= b else (hi, lo)

def fit_uv(face, e):
    u0, v0, u1, v1 = FUV[face](*e["from"], *e["to"])
    u0, u1 = _span(u0, u1)
    v0, v1 = _span(v0, v1)
    return [round(u0, 3), round(v0, 3), round(u1, 3), round(v1, 3)]

def slotify(obj):
    """26.1 only resolves face textures given as #slot; turn literal ids into slots."""
    if not isinstance(obj, dict) or "elements" not in obj:
        return obj
    import copy
    obj = copy.deepcopy(obj)
    tex = obj.setdefault("textures", {})
    rev = {}
    for k, v in tex.items():
        if isinstance(v, str) and not v.startswith("#"):
            rev.setdefault(v, k)
    for e in obj["elements"]:
        for fn, fd in e.get("faces", {}).items():
            if "uv" not in fd and (min(e["from"]) < 0 or max(e["to"]) > 16):
                fd["uv"] = fit_uv(fn, e)
            t = fd.get("texture")
            if isinstance(t, str) and not t.startswith("#"):
                if t not in rev:
                    rev[t] = "t%d" % len(rev)
                    tex[rev[t]] = t
                fd["texture"] = "#" + rev[t]
    return obj

def wj(path, obj):
    if "/models/" in path:
        obj = slotify(obj)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w"), indent=1)

def wtex(rel, im):
    if not rel.startswith(("item/", "particle/", "entity/", "gui/", "special/", "misc/", "models/", "block/", "fluid/", "map/", "trims/")):
        rel = "block/" + rel
    save(im, f"{A}/textures/{rel}.png")

def model(rel, obj): wj(f"{A}/models/{rel}.json", obj)
def bstate(id, obj): wj(f"{A}/blockstates/{id}.json", obj)
def idef(id, obj): wj(f"{A}/items/{id}.json", obj)

def T(name): return f"create:block/{name}"

def box(frm, to, tex="#0", rot=None, faces=None, cull=False):
    e = {"from": list(frm), "to": list(to), "faces": {}}
    for f in ("north", "south", "east", "west", "up", "down"):
        d = {"texture": tex}
        e["faces"][f] = d
    if rot: e["rotation"] = rot
    return e

def emodel(textures, elements, parent="minecraft:block/block"):
    m = {"parent": parent, "textures": textures, "elements": elements}
    return m

def cube_all(tex): return {"parent": "minecraft:block/cube_all", "textures": {"all": tex}}
def cube_col(side, end): return {"parent": "minecraft:block/cube_column", "textures": {"side": side, "end": end}}

# ---------------------------------------------------------------- classification
STONE_SUFFIX = ("_stairs", "_slab", "_wall")
def base_of(id):
    for s in STONE_SUFFIX:
        if id.endswith(s):
            b = id[:-len(s)]
            for cand in (b, b + "s", b.replace("_brick", "_bricks"), b.replace("_shingle", "_shingles"), b.replace("_tile", "_tiles")):
                if cand in BLOCKS and cand != id: return cand
    return id

def stone_info(id):
    for pal in STONES:
        m = re.fullmatch(rf"(polished_cut_|cut_|small_|layered_)?{pal}(_bricks|_pillar|_brick)?(_stairs|_slab|_wall)?", id)
        if m:
            pre, suf = m.group(1), m.group(2)
            if suf == "_pillar": kind = "pillar"
            elif pre == "small_": kind = "small"
            elif pre == "layered_": kind = "layered"
            elif pre == "polished_cut_": kind = "polished"
            elif suf in ("_bricks", "_brick"): kind = "bricks"
            elif pre == "cut_": kind = "cut"
            else: kind = "raw"
            return pal, kind
    return None

def classify(id):
    b = BLOCKS[id]
    cls = b["class"].split(".")[-1]
    props = b["props"]
    if cls in ("StairBlock", "WeatheringCopperStairBlock"): return "stairs"
    if cls in ("SlabBlock", "WeatheringCopperSlabBlock"): return "slab"
    if cls == "WallBlock": return "wall"
    if cls in ("ConnectedGlassPaneBlock", "IronBarsBlock"): return "pane"
    if cls in ("SlidingDoorBlock",): return "sdoor"
    if cls in ("TrainTrapdoorBlock",): return "trapdoor"
    if cls == "MetalLadderBlock": return "ladder"
    if cls == "ConnectedPillarBlock": return "pillar"
    return "other"

def colour_of(id):
    for d in DYES:
        if id.startswith(d + "_"): return d, id[len(d) + 1:]
    return None, id

def stone_tex_name(id):
    s = stone_info(base_of(id))
    return s

# ---------------------------------------------------------------- textures
MAKE = {}
def ensure_stone_textures():
    for pal, col in STONES.items():
        for kind in ("raw", "cut", "polished", "bricks", "small", "layered"):
            wtex(f"{pal}_{kind}", art.stone_tex(f"{pal}{kind}", col, "raw" if kind == "raw" else kind))
        wtex(f"{pal}_pillar_side", art.stone_tex(pal + "ps", col, "pillar_side"))
        wtex(f"{pal}_pillar_top", art.stone_tex(pal + "pt", col, "pillar_top"))

def block_color(id):
    cname, rest = colour_of(id)
    if cname: return DYES[cname]
    for k, v in (("brass", PAL["brass"]), ("copper", PAL["copper"]), ("andesite", PAL["andesite"]), ("zinc", PAL["zinc"]),
                 ("rose_quartz", (230, 150, 160)), ("netherite", (70, 60, 70)), ("industrial_iron", PAL["steel"]),
                 ("weathered_iron", (110, 96, 84)), ("iron", PAL["iron"]), ("wooden", PAL["wood"]), ("cardboard", (176, 140, 96)),
                 ("shadow", (40, 40, 50)), ("radiance", (230, 230, 180)), ("railway", (110, 80, 60)), ("experience", (140, 220, 60)),
                 ("track", (96, 80, 70)), ("blaze", (200, 90, 30)), ("honey", (240, 190, 40)), ("chocolate", (100, 60, 40))):
        if k in id: return v
    return PAL["steel"]

def tex_for_block(id):
    """Return PIL image for the main face of a block."""
    s = stone_info(id)
    if s:
        pal, kind = s
        return None
    cname, rest = colour_of(id)
    col = block_color(id)
    if "casing" in id: return casing(col, id)
    if "glass" in id or "window" in id:
        return glass(PAL["steel"] if "iron" not in id else shade(col, 0.9) if "weathered" not in id else col, id,
                     "tiled" if "tiled" in id else "h" if "horizontal" in id else "v" if "vertical" in id else "frame") if not id.endswith(("oak_window", "oak_window_pane")) and not any(id.startswith(w + "_window") for w in ("spruce","birch","jungle","acacia","mangrove","crimson","warped","cherry","bamboo")) else wood_window(id)
    if "table_cloth" in id or "sail" in id: return weave(col, id)
    if "shingle" in id: return shingles(shade_ox(id), id)
    if "copper_tile" in id: return tiles(shade_ox(id), id)
    if "ore" in id: return ore((70, 70, 74) if "deepslate" in id else (128, 128, 128), id, PAL["zinc"])
    if id == "raw_zinc_block": return art.ore_tex(id, (118, 126, 134), (176, 190, 204))
    if id.endswith("_block") or id in ("andesite_alloy_block",): return plate(col, id, False)
    if "pipe" in id: return pipe_tex(col if "copper" in id or "fluid" in id else PAL["copper"], id)
    if "gear" in id or "cog" in id: return gear_tex(col, id)
    return machine(col, id)

def shade_ox(id):
    lvl = 0
    for i, k in enumerate(("exposed", "weathered", "oxidized")):
        if k in id: lvl = i + 1
    return tint_oxid(lvl)

def wood_window(id):
    base = {"oak": (170, 130, 80), "spruce": (110, 80, 48), "birch": (200, 186, 130), "jungle": (150, 106, 76), "acacia": (176, 96, 56),
            "dark_oak": (66, 44, 24), "mangrove": (120, 50, 50), "crimson": (110, 50, 76), "warped": (40, 120, 110), "cherry": (230, 170, 180),
            "bamboo": (200, 190, 70)}
    w = next((v for k, v in base.items() if id.startswith(k)), PAL["wood"])
    return glass(w, id)

# ---------------------------------------------------------------- blockstate helpers
YROT = {"north": 0, "east": 90, "south": 180, "west": 270}
def variant(m, x=0, y=0):
    d = {"model": m}
    if x: d["x"] = x
    if y: d["y"] = y
    return d

def rot_variants(id, mdl, props=None):
    props = props if props is not None else BLOCKS[id]["props"]
    axes = [p for p in ("axis",) if p in props]
    facing = props.get("facing")
    out = {}
    if "axis" in props and facing is None:
        vals = props["axis"]
        for a in vals:
            x, y = {"y": (0, 0), "z": (90, 0), "x": (90, 90)}[a]
            out[f"axis={a}"] = variant(mdl, x, y)
    elif facing is not None and "axis" not in props:
        for f in facing:
            if f == "up": out["facing=up"] = variant(mdl)
            elif f == "down": out["facing=down"] = variant(mdl, 180)
            elif len(facing) >= 5:
                out[f"facing={f}"] = variant(mdl, 90, YROT[f])
            else:
                out[f"facing={f}"] = variant(mdl, 0, YROT[f])
    elif facing is not None and "axis" in props:
        for a in props["axis"]:
            for f in facing:
                out[f"axis={a},facing={f}"] = variant(mdl, 0, YROT.get(f, 0))
    else:
        out[""] = variant(mdl)
    return {"variants": out}

def stairs_state(id, base):
    out = {}
    for facing, ybase in (("east", 0), ("south", 90), ("west", 180), ("north", 270)):
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                mdl = f"create:block/{id}" + {"straight": "", "inner_left": "_inner", "inner_right": "_inner", "outer_left": "_outer", "outer_right": "_outer"}[shape]
                y = ybase
                if shape in ("inner_left", "outer_left"): y = (ybase + 270) % 360
                x = 0
                if half == "top":
                    x = 180
                    if shape in ("straight",): pass
                    if shape in ("inner_left", "outer_left"): y = ybase
                    else: y = (ybase + 90) % 360
                    if shape == "straight": y = ybase
                d = {"model": mdl}
                if x: d["x"] = x
                if y: d["y"] = y
                if x or y: d["uvlock"] = True
                out[f"facing={facing},half={half},shape={shape}"] = d
    return {"variants": out}

def wall_state(id):
    parts = [{"when": {"up": "true"}, "apply": {"model": f"create:block/{id}_post"}}]
    for side, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for h, m in (("low", "_side"), ("tall", "_side_tall")):
            a = {"model": f"create:block/{id}{m}", "uvlock": True}
            if y: a["y"] = y
            parts.append({"when": {side: h}, "apply": a})
    return {"multipart": parts}

def pane_state(id, props):
    post, side, sidealt, noside, noalt = (f"create:block/{id}_{k}" for k in ("post", "side", "side_alt", "noside", "noside_alt"))
    parts = [{"apply": {"model": post}}]
    for d, y, m in (("north", 0, side), ("east", 0, side), ("south", 0, sidealt), ("west", 0, sidealt)):
        a = {"model": m}
        if d == "east": a["y"] = 90
        if d == "west": a["y"] = 90
        parts.append({"when": {d: "true"}, "apply": a})
    for d, m, y in (("north", noside, 0), ("east", noalt, 0), ("south", noalt, 90), ("west", noside, 270)):
        a = {"model": m}
        if y: a["y"] = y
        parts.append({"when": {d: "false"}, "apply": a})
    return {"multipart": parts}

# ---------------------------------------------------------------- hand modelled shapes
def bar_y(t=4, y0=0, y1=16, tex="#0"):
    a, b = 8 - t / 2, 8 + t / 2
    return box([a, y0, a], [b, y1, b], tex)

def cog_elements(radius, thick=3, teeth=True, tex="#0"):
    lo, hi = 8 - thick / 2, 8 + thick / 2
    r = radius
    els = [box([8 - r + 1.5, lo, 8 - r + 1.5], [8 + r - 1.5, hi, 8 + r - 1.5], tex)]
    if teeth:
        els.append(box([8 - r - 1, lo, 8 - 2], [8 + r + 1, hi, 8 + 2], tex))
        els.append(box([8 - 2, lo, 8 - r - 1], [8 + 2, hi, 8 + r + 1], tex))
        for ang in (45, -45):
            els.append(box([8 - r - 0.5, lo, 8 - 2], [8 + r + 0.5, hi, 8 + 2], tex, {"origin": [8, 8, 8], "axis": "y", "angle": ang}))
    return els

def gen_kinetic_models():
    # shaft
    wtex("shaft", gear_tex(PAL["steel"], "shaft"))
    wtex("shaft_end", plate(PAL["dark_steel"], "shaftend", False))
    sh = emodel({"0": T("shaft"), "particle": T("shaft")}, [bar_y(4, 0, 16)])
    model("block/shaft", sh)
    model("block/shaft_half", emodel({"0": T("shaft"), "particle": T("shaft")}, [bar_y(4, 0, 8)]))
    model("block/powered_shaft", emodel({"0": T("shaft"), "1": T("powered_band"), "particle": T("shaft")}, [bar_y(4, 0, 16), box([5, 4, 5], [11, 12, 11], "#1")]))
    wtex("powered_band", casing(PAL["paint_yellow"], "poweredband"))
    # cogwheels
    wtex("cogwheel", gear_tex(PAL["andesite"], "cogwheel"))
    cog = cog_elements(5, 3)
    model("block/cogwheel_shaftless", emodel({"0": T("cogwheel"), "particle": T("cogwheel")}, cog))
    model("block/cogwheel_shaft", emodel({"0": T("shaft"), "particle": T("shaft")}, [bar_y(4, 0, 16)]))
    model("block/cogwheel", emodel({"0": T("cogwheel"), "1": T("shaft"), "particle": T("cogwheel")}, cog + [bar_y(4, 0, 16, "#1")]))
    big = cog_elements(10, 3)
    model("block/large_cogwheel_shaftless", emodel({"0": T("cogwheel"), "particle": T("cogwheel")}, big))
    model("block/large_cogwheel", emodel({"0": T("cogwheel"), "1": T("shaft"), "particle": T("cogwheel")}, big + [bar_y(4, 0, 16, "#1")]))
    # water wheels
    wtex("water_wheel", plate(PAL["wood"], "waterwheel", False))
    wood = {"0": T("water_wheel"), "1": T("shaft"), "particle": T("water_wheel")}
    spokes = []
    for ang in (0, 45, 90, 135):
        spokes.append(box([-14, 6, 6.5], [30, 10, 9.5], "#0", {"origin": [8, 8, 8], "axis": "y", "angle": ang} if ang else None) if ang == 0 else
                      box([-14, 6, 6.5], [30, 10, 9.5], "#0", {"origin": [8, 8, 8], "axis": "y", "angle": ang if ang <= 45 else ang - 180 if ang > 90 else ang}))
    paddles = []
    import math as _m
    for k in range(8):
        a = k * 45 + 22.5
        cx, cz = 8 + 22 * _m.cos(_m.radians(a)), 8 + 22 * _m.sin(_m.radians(a))
        paddles.append(box([cx - 5, 5, cz - 1.5], [cx + 5, 11, cz + 1.5], "#0", {"origin": [cx, 8, cz], "axis": "y", "angle": 22.5 if False else 0}))
    # keep paddles inside the legal -16..32 range
    paddles = [p for p in paddles if all(-16 <= v <= 32 for v in p["from"] + p["to"])]
    wheel = spokes + paddles + [bar_y(6, 0, 16, "#1")]
    model("block/water_wheel/wheel", emodel(wood, wheel))
    model("block/water_wheel/block", emodel(wood, [bar_y(6, 0, 16, "#1")]))
    model("block/water_wheel/item", emodel(wood, wheel, "minecraft:block/block"))
    model("block/large_water_wheel/block", emodel(wood, [bar_y(6, 0, 16, "#1")]))
    model("block/large_water_wheel/block_extension", emodel(wood, [bar_y(6, 0, 16, "#1")]))
    model("block/large_water_wheel/item", emodel(wood, wheel))
    # encased shafts and cogs: casing column with axle end
    for mat, c in (("andesite", PAL["andesite"]), ("brass", PAL["brass"])):
        wtex(f"{mat}_casing_end", casing(shade(c, 0.85), mat + "end"))
    # press
    wtex("press", machine(PAL["steel"], "press", PAL["paint_yellow"], PAL["dark_steel"]))
    wtex("press_head", plate(PAL["dark_steel"], "presshead"))
    pm = {"0": T("press"), "1": T("press_head"), "particle": T("press")}
    model("block/mechanical_press/block", emodel(pm, [box([0, 10, 0], [16, 16, 16]), box([0, 0, 0], [3, 10, 3]), box([13, 0, 0], [16, 10, 3]),
                                                     box([0, 0, 13], [3, 10, 16]), box([13, 0, 13], [16, 10, 16])]))
    model("block/mechanical_press/head", emodel(pm, [box([3, 4, 3], [13, 10, 13], "#1"), box([6.5, 10, 6.5], [9.5, 16, 9.5], "#1")]))
    model("block/mechanical_press/item", emodel(pm, [box([0, 10, 0], [16, 16, 16]), box([0, 0, 0], [3, 10, 3]), box([13, 0, 0], [16, 10, 3]), box([0, 0, 13], [3, 10, 16]),
                                                    box([13, 0, 13], [16, 10, 16]), box([3, 4, 3], [13, 10, 13], "#1")]))
    model("block/mechanical_press/head_partial_unused", emodel(pm, []))
    # basin
    wtex("basin", plate(PAL["andesite"], "basin"))
    bm = {"0": T("basin"), "particle": T("basin")}
    model("block/basin/block", emodel(bm, [box([2, 0, 2], [14, 2, 14]), box([0, 2, 0], [16, 12, 2]), box([0, 2, 14], [16, 12, 16]), box([0, 2, 2], [2, 12, 14]), box([14, 2, 2], [16, 12, 14])]))
    # belt segments (flat conveyor bed along Z, 12 wide)
    wtex("belt", belt_tex()); wtex("belt_scroll", belt_tex(2)); wtex("belt_end", plate(PAL["dark_steel"], "beltend"))
    bt = {"0": T("belt"), "1": T("belt_end"), "particle": T("belt")}
    def bed(z0=0, z1=16): return box([2, 0, z0], [14, 3, z1], "#0")
    model("block/belt/particle", emodel(bt, [box([5, 0, 5], [11, 1, 11])]))
    for nm, els in (("middle", [bed()]), ("start", [bed(0, 16), box([2, 0, 0], [14, 4, 3], "#1")]), ("end", [bed(0, 16), box([2, 0, 13], [14, 4, 16], "#1")]),
                    ("middle_bottom", [bed()]), ("start_bottom", [bed()]), ("end_bottom", [bed()])):
        model(f"block/belt/{nm}", emodel(bt, els))
    for nm in ("diagonal_middle", "diagonal_start", "diagonal_end"):
        model(f"block/belt/{nm}", emodel(bt, [box([2, -2, -6], [14, 1, 22], "#0", {"origin": [8, 0, 8], "axis": "x", "angle": 45})]))
    model("block/belt_pulley", emodel(bt, [box([2, 0, 0], [14, 4, 4], "#1"), box([4, 4, 1], [12, 10, 3], "#1")]))
    for c in ("andesite", "brass"):
        for ax in ("x", "z"):
            model(f"block/belt_cover/{c}_belt_cover_{ax}", emodel({"0": T(f"{c}_casing"), "particle": T(f"{c}_casing")}, [box([2, 3, 0], [14, 5, 16]) if ax == "z" else box([0, 3, 2], [16, 5, 14])]))

def belt_tex(shift=0):
    im = img(); 
    for y in range(16):
        for x in range(16):
            base = PAL["rubber"]
            if (y + shift) % 4 == 0: base = shade(base, 1.5)
            px(im, x, y, base)
    for x in (0, 1, 14, 15):
        for y in range(16): px(im, x, y, shade(PAL["steel"], 0.8))
    return im


# ---------------------------------------------------------------- driver: blocks
AXIS_CUBES = ("encased_shaft", "encased_cogwheel", "encased_large_cogwheel", "gearbox", "clutch", "gearshift", "chain_drive", "chain_gearshift", "speed_controller",
              "rotation_speed_controller", "sequenced_gearshift", "creative_motor", "steam_engine", "mechanical_piston", "radial_chassis", "linear_chassis", "sail")

def base_tex_ref(id):
    """texture reference (create:block/...) of the 'all' face for a block."""
    bid = base_of(id)
    s = stone_info(bid)
    if s:
        pal, kind = s
        return T(f"{pal}_pillar_side") if kind == "pillar" else T(f"{pal}_{kind}")
    return T(bid)

def gen_blocks():
    ensure_stone_textures()
    done_tex = set()
    me = sys.modules[__name__]
    for id, b in BLOCKS.items():
        if not design.shipped_block(me, id):
            bstate(id, {"variants": {"": {"model": "minecraft:block/air"}}})
            continue
        kind = classify(id)
        props = b["props"]
        s = stone_info(base_of(id))
        bid = base_of(id)
        # texture
        if not s and bid not in done_tex:
            done_tex.add(bid)
            im = tex_for_block(bid)
            if im is not None: wtex(bid, im)
        if b["shape"] == "INVISIBLE" or id.startswith("copycat"):
            bstate(id, {"variants": {"": {"model": "minecraft:block/air"}}})
            continue
        tref = base_tex_ref(id)
        if kind == "stairs":
            tx = {"bottom": tref, "top": tref, "side": tref}
            model(f"block/{id}", {"parent": "minecraft:block/stairs", "textures": tx})
            model(f"block/{id}_inner", {"parent": "minecraft:block/inner_stairs", "textures": tx})
            model(f"block/{id}_outer", {"parent": "minecraft:block/outer_stairs", "textures": tx})
            bstate(id, stairs_state(id, bid))
        elif kind == "slab":
            tx = {"bottom": tref, "top": tref, "side": tref}
            model(f"block/{id}", {"parent": "minecraft:block/slab", "textures": tx})
            model(f"block/{id}_top", {"parent": "minecraft:block/slab_top", "textures": tx})
            model(f"block/{id}_double", cube_all(tref))
            bstate(id, {"variants": {"type=bottom": {"model": f"create:block/{id}"}, "type=top": {"model": f"create:block/{id}_top"}, "type=double": {"model": f"create:block/{id}_double"}}})
        elif kind == "wall":
            tx = {"wall": tref}
            model(f"block/{id}_post", {"parent": "minecraft:block/template_wall_post", "textures": tx})
            model(f"block/{id}_side", {"parent": "minecraft:block/template_wall_side", "textures": tx})
            model(f"block/{id}_side_tall", {"parent": "minecraft:block/template_wall_side_tall", "textures": tx})
            model(f"block/{id}_inventory", {"parent": "minecraft:block/wall_inventory", "textures": tx})
            bstate(id, wall_state(id))
        elif kind == "pane":
            gt = T(id if not id.endswith("_pane") else id[:-5]) if False else T(id)
            wtex(id, tex_for_block(id.replace("_pane", "")) or plate(PAL["steel"], id))
            tx = {"pane": T(id), "edge": T(id)}
            for k, par in (("post", "template_glass_pane_post"), ("side", "template_glass_pane_side"), ("side_alt", "template_glass_pane_side_alt"),
                           ("noside", "template_glass_pane_noside"), ("noside_alt", "template_glass_pane_noside_alt")):
                model(f"block/{id}_{k}", {"parent": f"minecraft:block/{par}", "textures": tx})
            bstate(id, pane_state(id, props))
        elif kind == "trapdoor":
            tx = {"texture": T(id)}
            for k in ("bottom", "top", "open"):
                model(f"block/{id}_{k}", {"parent": f"minecraft:block/template_trapdoor_{k}", "textures": tx})
            v = {}
            for facing, y in (("north", 0), ("south", 180), ("east", 90), ("west", 270)):
                for half in ("bottom", "top"):
                    for op in ("false", "true"):
                        if op == "true":
                            m, x, yy = f"{id}_open", 0, (y + 180) % 360 if half == "bottom" or True else y
                            d = {"model": f"create:block/{m}"}
                            if half == "top": d["x"] = 180; d["y"] = y if False else (y)
                            else: d["y"] = (y + 180) % 360 if False else y
                            d["y"] = y
                            if half == "top": d["x"] = 180; d["y"] = (y + 180) % 360
                            if d["y"] == 0: d.pop("y")
                        else:
                            d = {"model": f"create:block/{id}_{half}"}
                            if y: d["y"] = y
                        v[f"facing={facing},half={half},open={op}"] = d
            bstate(id, {"variants": v})
        elif kind == "ladder":
            model(f"block/{id}", {"parent": "minecraft:block/ladder", "textures": {"particle": T(id), "texture": T(id)}})
            bstate(id, {"variants": {f"facing={f}": variant(f"create:block/{id}", 0, {"north": 0, "east": 90, "south": 180, "west": 270}[f]) for f in props["facing"]}})
        elif kind == "sdoor":
            model(f"block/{id}", emodel({"0": T(id), "particle": T(id)}, [box([0, 0, 0], [16, 16, 3])]))
            v = {}
            for facing in props["facing"]:
                v[f"facing={facing}"] = variant(f"create:block/{id}", 0, YROT[facing])
            bstate(id, {"variants": v})
        elif kind == "pillar":
            if s:
                pal = s[0]
                model(f"block/{id}", cube_col(T(f"{pal}_pillar_side"), T(f"{pal}_pillar_top")))
            else:
                wtex(id + "_top", plate(block_color(id), id + "top")); model(f"block/{id}", cube_col(T(id), T(id + "_top")))
            bstate(id, rot_variants(id, f"create:block/{id}"))
        else:
            gen_other(id, b)

def gen_other(id, b):
    props = b["props"]
    if id in ("shaft", "powered_shaft"):
        bstate(id, rot_variants(id, f"create:block/{id}")); return
    if id in ("cogwheel", "large_cogwheel"):
        bstate(id, rot_variants(id, f"create:block/{id}")); return
    if id in ("water_wheel", "large_water_wheel"):
        bstate(id, rot_variants(id, f"create:block/{id}/block")); return
    if id == "water_wheel_structure":
        model("block/water_wheel_structure", {"parent": "minecraft:block/air"}); bstate(id, {"variants": {"": {"model": "minecraft:block/air"}}}); return
    if id == "mechanical_press":
        bstate(id, rot_variants(id, "create:block/mechanical_press/block")); return
    if id == "basin":
        bstate(id, {"variants": {"": {"model": "create:block/basin/block"}}}); return
    if id == "belt":
        bstate(id, {"variants": {"": {"model": "create:block/belt/particle"}}}); return
    if any(id.endswith(k) for k in AXIS_CUBES) or id.endswith("_encased_shaft") or "encased" in id and "axis" in props:
        col = block_color(id)
        side = T(id); end = T(id + "_end")
        wtex(id + "_end", casing(shade(col, 0.8), id + "e", PAL["dark_steel"]))
        model(f"block/{id}", cube_col(side, end))
        bstate(id, rot_variants(id, f"create:block/{id}")); return
    sid = base_tex_ref(id)
    if "casing" in id or id.endswith("_block") or "ore" in id or "glass" in id or "window" in id or "tiles" in id or "shingles" in id or "quartz" in id or id.startswith("layered"):
        m = cube_all(sid)
    else:
        m = cube_all(sid)
    model(f"block/{id}", m)
    if id in ("honey", "chocolate"):
        bstate(id, {"variants": {"": {"model": "minecraft:block/water"}}}); return
    bstate(id, rot_variants(id, f"create:block/{id}"))

# ---------------------------------------------------------------- items
FLAT_BLOCK_KINDS = ("pane", "ladder", "sdoor", "trapdoor")
HANDHELD = ("wrench", "sword", "whisk", "sand_paper", "potato", "wand", "worldshaper", "extendo", "schematic_and_quill")

# unobtainable items whose special models are fetched by block entity renderers at load time
RENDERER_BOUND = {"potato_cannon", "extendo_grip", "linked_controller", "handheld_worldshaper", "wand_of_symmetry"}

def gen_items():
    me = sys.modules[__name__]
    for id, it in ITEMS.items():
        if not design.shipped_item(me, id) and id not in RENDERER_BOUND:
            idef(id, {"model": {"type": "minecraft:model", "model": "minecraft:item/barrier"}})
            continue
        if it.get("blockItem"):
            if id not in BLOCKS:
                alias = {"vertical_gearbox": "gearbox", "empty_blaze_burner": "blaze_burner"}.get(id)
                if alias:
                    idef(id, {"model": {"type": "minecraft:model", "model": f"create:block/{alias}"}})
                else:
                    wtex("item/" + id, icons64.make(id))
                    model(f"item/{id}", {"parent": "minecraft:item/generated", "textures": {"layer0": f"create:item/{id}"}})
                    idef(id, {"model": {"type": "minecraft:model", "model": f"create:item/{id}"}})
                continue
            kind = classify(id)
            if id.startswith("copycat"):
                idef(id, {"model": {"type": "minecraft:model", "model": "create:item/" + id}})
                im = machine(PAL["brass"], id); wtex("item/" + id, im)
                model(f"item/{id}", {"parent": "minecraft:item/generated", "textures": {"layer0": f"create:item/{id}"}}); continue
            if kind in FLAT_BLOCK_KINDS or id in ("belt", "rope", "pulley_magnet", "fake_track", "water_wheel_structure", "track", "flap_display"):
                src = f"{A}/textures/block/{base_of(id)}.png"
                if os.path.exists(src): shutil.copy(src, f"{A}/textures/item/{id}.png") if os.path.isdir(f"{A}/textures/item") else (os.makedirs(f"{A}/textures/item", exist_ok=True), shutil.copy(src, f"{A}/textures/item/{id}.png"))
                else: wtex("item/" + id, machine(block_color(id), id))
                model(f"item/{id}", {"parent": "minecraft:item/generated", "textures": {"layer0": f"create:item/{id}"}})
                idef(id, {"model": {"type": "minecraft:model", "model": f"create:item/{id}"}}); continue
            if kind == "wall": mdl = f"create:block/{id}_inventory"
            elif id == "water_wheel": mdl = "create:block/water_wheel/item"
            elif id == "large_water_wheel": mdl = "create:block/large_water_wheel/item"
            elif id == "mechanical_press": mdl = "create:block/mechanical_press/item"
            elif id == "basin": mdl = "create:block/basin/block"
            elif id in ("honey", "chocolate"): mdl = "create:item/" + id
            else: mdl = f"create:block/{id}"
            idef(id, {"model": {"type": "minecraft:model", "model": mdl}})
            continue
        # plain items
        wtex("item/" + id, icons64.make(id))
        if os.path.exists(f"{A}/models/item/{id}.json"):
            idef(id, {"model": {"type": "minecraft:model", "model": f"create:item/{id}"}}); continue
        par = "minecraft:item/handheld" if any(k in id for k in HANDHELD) else "minecraft:item/generated"
        model(f"item/{id}", {"parent": par, "textures": {"layer0": f"create:item/{id}"}})
        special = {"wrench": "create:model/wrench", "potato_cannon": "create:model/potato_cannon", "extendo_grip": "create:model/extendo_grip",
                   "linked_controller": "create:model/linked_controller", "handheld_worldshaper": "create:model/handheld_worldshaper",
                   "wand_of_symmetry": "create:model/wand_of_symmetry"}
        if id in special:
            idef(id, {"model": {"type": special[id]}})
        elif id == "sand_paper" or id == "red_sand_paper":
            idef(id, {"model": {"type": "create:model/sand_paper", "model": f"create:item/{id}"}})
        else:
            idef(id, {"model": {"type": "minecraft:model", "model": f"create:item/{id}"}})
    # buckets etc. handled above; special sub-models used by custom item renderers
    for nm in ("wrench", "potato_cannon", "extendo_grip", "linked_controller", "handheld_worldshaper", "wand_of_symmetry"):
        base = {"wrench": "wrench", "potato_cannon": "potato_cannon", "extendo_grip": "extendo_grip", "linked_controller": "linked_controller",
                "handheld_worldshaper": "handheld_worldshaper", "wand_of_symmetry": "wand_of_symmetry"}[nm]
        model(f"item/{nm}/item", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"create:item/{base}"}})
        for sub in ("gear", "cog", "core", "bits", "button", "torch"):
            wtex(f"item/{nm}_{sub}", icons64.generic_gear(art.BRASS, nm + sub))
            model(f"item/{nm}/{sub}", {"parent": "minecraft:item/generated", "textures": {"layer0": f"create:item/{nm}_{sub}"}})
    for id in ("honey", "chocolate"):
        wtex("item/" + id, icons64.make(id + "_bucket"))
        model(f"item/{id}", {"parent": "minecraft:item/generated", "textures": {"layer0": f"create:item/{id}"}})

def material_color_f(id): return icons.material_color(id)

# ---------------------------------------------------------------- partials
def gen_partials():
    wtex("partial_generic", machine(PAL["steel"], "partial", PAL["paint_yellow"], PAL["dark_steel"]))
    wtex("partial_brass", plate(PAL["brass"], "partialbrass"))
    wtex("partial_wood", plate(PAL["wood"], "partialwood", False))
    wtex("partial_rope", weave((160, 140, 96), "rope"))
    wtex("partial_glow", speckle((255, 220, 120), "glow", 0.05))
    wtex("partial_red", speckle((200, 40, 40), "pred", 0.05))
    wtex("partial_white", speckle((236, 236, 236), "pwhite", 0.03))
    wtex("partial_yellow", speckle((230, 200, 40), "pyellow", 0.03))
    wtex("partial_cardboard", plate((176, 140, 96), "pcard", False))
    for p in PARTIALS:
        ns, path = p.split(":", 1)
        out = f"{A}/models/{path}.json"
        if os.path.exists(out): continue
        name = path.split("/")[-1]
        tx = "partial_generic"
        m = None
        if "valve_handle" in path:
            c = path.split("/")[-1].replace("_valve_handle", "")
            wtex(path.split("/")[-1] + "_tex", plate(DYES.get(c, PAL["copper"]), c + "vh", False))
            tx = name + "_tex"
            m = [box([6, 0, 6], [10, 5, 10]), box([1, 5, 7], [15, 8, 9]), box([7, 5, 1], [9, 8, 15])]
        elif path == "block/valve_handle":
            m = [box([6, 0, 6], [10, 5, 10]), box([1, 5, 7], [15, 8, 9]), box([7, 5, 1], [9, 8, 15])]
        elif path.startswith("block/toolbox/lid"):
            c = name; wtex("toolbox_lid_" + c, plate(DYES.get(c, PAL["steel"]), "tb" + c)); tx = "toolbox_lid_" + c
            m = [box([1, 9, 3], [15, 13, 13])]
        elif "toolbox/drawer" in path:
            m = [box([2, 1, 4], [14, 7, 14])]
        elif any(k in path for k in ("rope", "hose")) and "coil" not in path:
            tx = "partial_rope"; m = [box([7, 0, 7], [9, 16, 9])]
        elif "coil" in path:
            tx = "partial_rope"; m = [box([3, 2, 3], [13, 14, 13])]
        elif "magnet" in path:
            tx = "partial_brass"; m = [box([3, 0, 3], [13, 6, 13])]
        elif any(k in path for k in ("glow", "flame", "bulb_light")):
            tx = "partial_glow"; m = [box([4, 4, 4], [12, 12, 12])]
        elif "red" in path:
            tx = "partial_red"; m = [box([4, 4, 4], [12, 12, 12])]
        elif any(k in path for k in ("cog", "gear", "wheel", "propeller", "inner")) and "shaft" not in path:
            m = cog_elements(5.5, 3, True, "#0"); 
        elif any(k in path for k in ("shaft", "pole", "rod", "linkage", "tongue", "pin")):
            m = [bar_y(4, 0, 16)]
        elif any(k in path for k in ("blade", "flap", "lid", "arrow", "line", "dotted", "indicator", "pointer", "dial", "flag", "segment", "tie", "plane", "overlay", "tray", "hatch", "panel", "arm", "belt")):
            m = [box([1, 0, 1], [15, 2, 15])]
        elif path.startswith("item/package") or "rare_" in path:
            wd = re.search(r"(\d+)x(\d+)", path)
            w, d = (int(wd.group(1)), int(wd.group(2))) if wd else (10, 10)
            hh = 8 if not wd else 10
            tx = "partial_cardboard"
            m = [box([8 - w / 2, 0, 8 - d / 2], [8 + w / 2, hh, 8 + d / 2])]
            if "rigging" in path: m.append(box([8 - w / 2 - 0.5, hh - 3, 8 - 1], [8 + w / 2 + 0.5, hh - 2, 8 + 1]))
        elif path.startswith("entity/crafting_blueprint"):
            m = [box([1, 1, 7.5], [15, 15, 8.5])]; tx = "partial_white"
        elif "hat" in path:
            m = [box([4, 0, 4], [12, 4, 12]), box([5, 4, 5], [11, 8, 11])]
        elif "coupling" in path:
            m = [box([6, 6, 0], [10, 10, 8])]
        else:
            m = [box([3, 3, 3], [13, 13, 13])]
        textures = {"0": T(tx) if not path.startswith("item/") else f"create:block/{tx}", "particle": T(tx)}
        model(path, emodel(textures, m, "minecraft:block/block" if not path.startswith("item/") else "minecraft:item/generated" if False else "minecraft:block/block"))

# ---------------------------------------------------------------- lang
def title(s): return " ".join(w.capitalize() for w in s.replace("_", " ").split())

def gen_lang():
    lang = {}
    me = sys.modules[__name__]
    for id in BLOCKS:
        if design.shipped_block(me, id): lang[f"block.create.{id}"] = title(id)
    for id, it in ITEMS.items():
        if design.shipped_item(me, id): lang[f"item.create.{id}"] = title(id)
    for e in REG["entities"]: lang[f"entity.create.{e}"] = title(e)
    for f in REG["fluids"]: lang[f"fluid_type.create.{f}"] = title(f)
    lang["itemGroup.create.base"] = "Create: Industrial"
    lang["itemGroup.create.palettes"] = "Create: Palettes"
    keys = collect_lang_keys()
    for k in keys:
        if k not in lang: lang[k] = pretty_key(k)
    wj(f"{A}/lang/en_us.json", dict(sorted(lang.items())))

def collect_lang_keys():
    keys = set()
    src = ROOT + "/src/main/java"
    pat1 = re.compile(r'"(create\.[a-z0-9_.]+)"')
    pat2 = re.compile(r'(?:translate|translateDirect|text|translatable|partialTranslate|translateAsLangKey)\(\s*"([a-z0-9_.]+)"')
    for dp, _, fs in os.walk(src):
        for f in fs:
            if not f.endswith(".java"): continue
            t = open(os.path.join(dp, f), errors="ignore").read()
            for m in pat1.finditer(t): keys.add(m.group(1))
            for m in pat2.finditer(t):
                k = m.group(1)
                if "." in k: keys.add("create." + k)
    return {k for k in keys if not k.endswith(".")}

def pretty_key(k):
    last = k.split(".")[-1]
    if last.isdigit(): last = k.split(".")[-2] + " " + last
    return title(last)

# ---------------------------------------------------------------- sounds / particles
def gen_sounds():
    import snd
    ev = {}
    made = set()
    for s in REG["sounds"]:
        base = re.sub(r"_compounded_\d+$", "", s)
        if base not in made:
            snd.synth(base, f"{A}/sounds"); made.add(base)
        ev[s] = {"sounds": [f"create:{base}"], "subtitle": f"create.subtitle.{base}"}
    wj(f"{A}/sounds.json", ev)

def gen_particles():
    for p in REG["particles"]:
        im = img(8) if False else img(16)
        r = rng(p)
        cx = cy = 7.5
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - cx, y - cy)
                if d < 6:
                    c = (255, 255, 255) if "air" not in p else (220, 230, 240)
                    px(im, x, y, c, int(255 * (1 - d / 6) ** 0.7))
        wtex(f"particle/{p}", im)
        wj(f"{A}/particles/{p}.json", {"textures": [f"create:{p}"]})

def write_shipped():
    me = sys.modules[__name__]
    ids = sorted(i for i in ITEMS if design.shipped_item(me, i))
    out = RES + "/data/create/shipped_items.txt"
    os.makedirs(os.path.dirname(out), exist_ok=True)
    open(out, "w").write("\n".join(ids) + "\n")

def main():
    if os.path.isdir(A): shutil.rmtree(A)
    os.makedirs(A)
    gen_blocks()
    gen_partials()
    gen_items()
    design.run(sys.modules[__name__])
    gen_lang()
    write_shipped()
    gen_particles()
    if "--nosound" not in sys.argv: gen_sounds()
    import extra
    extra.run(sys.modules[__name__])

if __name__ == "__main__":
    main()
