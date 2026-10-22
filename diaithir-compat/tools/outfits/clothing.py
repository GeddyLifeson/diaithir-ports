"""Clothing parts kit for Diaithir villagers: tunics, robes, parkas, wraps, kilts, aprons, cloaks, hoods, belts, boots,
armour, headwear, jewellery and body paint, drawn in body space (skin.py) and painted fresh at 64x (512x512 on the
player-skin layout MCA Reborn's villagers use).

Painting follows the approved creature method (Factorio style, STYLE.md in the shared Blender kit): every material is a
height field (hide wrinkles, fur strands, woven cloth, beaten bark, leather, beads, bone, feathers), lit from each face's
upper left, with bevels where one part sits on another, contact grime, Blender-baked occlusion (ao_bake.py) tinted
warm dirt brown, dust low on the legs, then muted and warmed. Colours come from the culture's own row in
culture_dress.csv (garment, ornament, pigment) and the kit palette (leather, bone, amber, greenstone).

An outfit is a list of pieces painted in order, later over earlier. A piece is a layer (base or outer), a mask in body
space, a material and colour, optional pattern overlays and a lift (how far it stands off what is under it).
"""
import re, zlib
from random import Random

import numpy as np
from PIL import Image

import skin
from skin import S

# ------------------------------------------------------------------ colours (sRGB 0-255)
# From the shared kit palette (kit.PALETTE), plus ornament materials named in culture_dress.csv.
LEATHER = (118, 86, 56)        # kit timber
DIRT = (98, 74, 50)            # kit dirt
SAND = (176, 150, 104)         # kit sand
ORNAMENT = {"amber": (196, 124, 40), "greenstone": (82, 116, 86), "shale": (54, 50, 48), "ivory": (228, 216, 190),
            "teeth": (226, 218, 198), "tooth": (226, 218, 198), "canine": (226, 218, 198), "bone": (214, 202, 176),
            "eggshell": (236, 228, 210), "shell": (232, 222, 204), "dentalium": (236, 230, 214), "olivella": (220, 206, 182),
            "columbella": (214, 190, 160), "seed": (92, 62, 40), "stone": (128, 120, 108), "pebble": (150, 70, 50)}
PIPECLAY = (228, 224, 214)
GRIME = np.array([30, 24, 18], np.float32)   # kit.grime tint
LIGHT = np.array([-0.5, -0.6, 0.9]) / np.linalg.norm([-0.5, -0.6, 0.9])  # texture space: x right, y down, z out
MUTE, WARM = 0.74, np.array([1.04, 0.98, 0.9])


def mix(a, b, t):
    return tuple(round(x * (1 - t) + y * t) for x, y in zip(a, b))


def shade(c, k):
    return tuple(max(0, min(255, round(v * k))) for v in c)


def hexrgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


# ------------------------------------------------------------------ height fields per material (texture space)
def blur(a, r, axis):
    if r < 1:
        return a
    pad = [(0, 0)] * a.ndim
    pad[axis] = (r + 1, r)
    c = np.cumsum(np.pad(a, pad, mode="edge"), axis=axis)
    hi = np.take(c, np.arange(2 * r + 1, c.shape[axis]), axis=axis)
    lo = np.take(c, np.arange(0, c.shape[axis] - 2 * r - 1), axis=axis)
    return (hi - lo) / (2 * r + 1)


def soft(a, r):
    return blur(blur(a, r, 0), r, 1)


def norm(a):
    return (a - a.mean()) / (a.std() + 1e-6)


def height(kind, h, w, rng):
    n = rng.random((h, w)).astype(np.float32)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    if kind == "hide":                         # soft wrinkles across, worn patches
        return 0.35 * norm(blur(n, 3 * S, 1)) * 0.6 + 0.25 * norm(soft(n, S)) + 0.08 * norm(soft(n, 1))
    if kind == "fur":                          # strands hanging down, clumped
        strands = norm(blur(n, int(1.5 * S), 0))
        return 0.45 * strands + 0.3 * norm(soft(rng.random((h, w)), 2 * S)) + 0.1 * norm(n)
    if kind == "cloth":                        # woven: warp and weft, soft folds hanging down
        p = max(2, S // 3)
        weave = np.sin(xx * np.pi / p) * np.sin(yy * np.pi / p)
        return 0.22 * weave + 0.35 * norm(blur(n, 3 * S, 0)) + 0.06 * norm(n)
    if kind == "bark":                         # beaten bark-cloth: long fibres with mallet marks across
        fib = norm(blur(n, 3 * S, 0))
        mallet = np.sin(yy * np.pi / (0.75 * S)) * 0.5 + 0.5
        return 0.3 * fib + 0.18 * mallet + 0.15 * norm(soft(n, S))
    if kind == "leather":                      # smooth, a little grain, scuffs
        return 0.12 * norm(soft(n, 2)) + 0.18 * norm(soft(n, 2 * S)) - 0.3 * (soft(n, S) > 0.56)
    if kind == "bone":
        cracks = np.clip(1 - np.abs(soft(n, S) - 0.5) * 40, 0, 1)
        return 0.25 * norm(soft(n, 2 * S)) - 0.4 * cracks
    if kind == "feather":                      # rows of scalloped tips
        row = 2 * S
        off = (yy // row) % 2 * 0.75 * S
        t = (yy + 0.5 * S * np.abs(np.sin((xx + off) * np.pi / (1.5 * S)))) % row / row
        return 0.9 * (1 - t) + 0.12 * norm(blur(n, S, 0))
    if kind == "plaster":
        return 0.3 * norm(soft(n, S)) + 0.1 * norm(n)
    if kind == "paint":                        # pigment on skin: thin, brushed
        return 0.05 * norm(blur(n, S, 1))
    return 0.1 * norm(soft(n, 1))              # bead, metal-free ornaments: smooth


STRENGTH = {"hide": 1.6, "fur": 1.6, "cloth": 1.4, "bark": 1.6, "leather": 1.2, "bone": 1.6, "feather": 2.2,
            "plaster": 1.4, "paint": 0.6, "bead": 2.0}
SHINE = {"bead": 1.0, "bone": 0.4, "leather": 0.35}
KINDS = list(STRENGTH)


# ------------------------------------------------------------------ body-space masks
ARMS, LEGS = ("rarm", "larm"), ("rleg", "lleg")


def jag(f, amp=0.25, seed=0):
    """Ragged edge offset (px) along a face, so cut hide and fur edges are never ruler straight."""
    t = f.round * 3.1 + seed
    return amp * (np.sin(t) * 0.6 + np.sin(t * 2.3 + 1.7) * 0.3 + np.sin(t * 5.1) * 0.1)


def covering(f, hem, sleeve, neck=True):
    """A garment on the body down to hem (y px) and on the arms down to sleeve; below 12 it carries on down the legs."""
    if f.name == "bottom" and f.part != "head":
        return np.zeros(f.shape, bool)
    if f.part == "body":
        m = f.Y >= hem
        if neck and f.name == "front":
            m &= ~((np.abs(f.X) < 1.9) & (f.Y > 22.4 + 0.55 * np.abs(f.X)))
        return m
    if f.part in ARMS:
        return (f.Y >= sleeve) if np.min(sleeve) < 24 else np.zeros(f.shape, bool)
    if f.part in LEGS:
        return (f.Y >= hem) & (f.name != "top")
    return np.zeros(f.shape, bool)


def band(f, parts, lo, hi, faces=("front", "back", "left", "right")):
    if f.part not in parts or f.name not in faces:
        return np.zeros(f.shape, bool)
    return (f.Y >= lo) & (f.Y < hi)


def cloak(f, hem):
    """Outer layer: hangs from the shoulders down the back and sides, falls over the front edges of the chest."""
    z = np.zeros(f.shape, bool)
    if f.part == "body":
        if f.name in ("back", "top"):
            return np.ones(f.shape, bool)
        if f.name in ("left", "right"):
            return f.Z < 1.3
        if f.name == "front":
            return (np.abs(f.X) > 2.9 + 0.08 * (24 - f.Y)) & (f.Y > 15)
        return z
    if f.part in ARMS:
        outside = (f.name == "right" and f.part == "rarm") or (f.name == "left" and f.part == "larm")
        return (f.Y >= 14 + jag(f, 0.3)) & (outside or f.name in ("top", "back"))
    if f.part in LEGS:
        cut = f.Y >= hem + jag(f, 0.35, 2)
        if f.name == "back":
            return cut
        outside = (f.name == "right" and f.part == "rleg") or (f.name == "left" and f.part == "lleg")
        return cut & outside & (f.Z < 0.2)
    return z


def collar(f, lo=22.6):
    if f.part != "body":
        return np.zeros(f.shape, bool)
    return (f.Y >= lo + jag(f, 0.25, 5)) | (f.name == "top")


def hood(f):
    if f.part != "head" or f.name == "bottom":
        return np.zeros(f.shape, bool)
    if f.name == "front":
        return ~((np.abs(f.X) < 3.0) & (f.Y < 30.4) & (f.Y > 24.2))
    return np.ones(f.shape, bool)


def hood_edge(f):
    if f.part != "head":
        return np.zeros(f.shape, bool)
    if f.name == "front":
        ring = (np.abs(f.X) < 3.9) & (f.Y < 31.2) & ~((np.abs(f.X) < 3.0) & (f.Y < 30.4))
        return ring
    if f.name in ("left", "right"):
        return f.Z > 3.3 + jag(f, 0.25, 9) * 0.5
    return np.zeros(f.shape, bool)


def cap(f, lo):
    if f.part != "head" or f.name == "bottom":
        return np.zeros(f.shape, bool)
    return (f.Y >= lo) | (f.name == "top")


def beads_on_curve(f, ycurve, spacing, r, faces=("front",), parts=("body",)):
    """Beads (dome heights) on a string whose height is ycurve(x) across the face: returns mask, dome and bead index."""
    if f.part not in parts or f.name not in faces:
        return None
    u = f.X if f.name in ("front", "back", "top") else f.Z
    k = np.round(u / spacing)
    cu = k * spacing
    d = np.hypot(u - cu, f.Y - ycurve(cu)) if f.name != "top" else np.hypot(u - cu, f.Z - ycurve(cu))
    m = d < r
    return m, np.sqrt(np.clip(1 - (d / r) ** 2, 0, 1)), k


# ------------------------------------------------------------------ pieces
class Piece:
    def __init__(s, layer, where, kind, colour, lift=0.0, patterns=(), name=""):
        s.layer, s.where, s.kind, s.colour, s.lift, s.patterns, s.name = layer, where, kind, colour, lift, patterns, name


class Beads:
    """A bead string or pendant: drawn as domes, not a flat mask."""
    def __init__(s, layer, fn, colours, kind="bead", lift=1.2):
        s.layer, s.fn, s.colours, s.kind, s.lift = layer, fn, colours, kind, lift


def p_stripes(col, period=2.6, width=0.55, off=0.0):
    return lambda f: (((f.Y + off) % period) < width, col, 0.15)


def p_hem(col, hem, w=0.7):
    return lambda f: ((f.Y >= hem + 0.25) & (f.Y < hem + 0.25 + w) & f.side, col, 0.25)


def p_zigzag(col, hem, h=0.9):
    def fn(f):
        tri = np.abs(((f.round / 0.9) % 2) - 1) * h
        return ((f.Y >= hem + 0.3 + tri) & (f.Y < hem + 0.3 + tri + 0.45) & f.side, col, 0.2)
    return fn


def p_chevron(col, period=2.8):
    def fn(f):
        if f.name not in ("front", "back") or f.part != "body":
            return (np.zeros(f.shape, bool), col, 0)
        return (((f.Y + np.abs(f.X) * 0.9) % period) < 0.5, col, 0.15)
    return fn


def p_dots(col, step=1.6, r=0.28):
    def fn(f):
        u = f.round
        row = np.floor(f.Y / step)
        uu = (u + (row % 2) * step / 2) % step - step / 2
        vv = (f.Y % step) - step / 2
        return (f.side & (np.hypot(uu, vv) < r), col, 0.12)
    return fn


def p_panels(col):
    def fn(f):                                  # sewn panels: seam lines with a stitch every half pixel
        seam = ((f.round % 2.7) < 0.14) & f.side
        stitch = seam & ((f.Y % 0.5) < 0.25)
        return (seam | stitch, col, -0.2)
    return fn


def p_grid(col, step=2.0, w=0.24):
    def fn(f):                                  # painted bark-cloth: lines and squares
        a = ((f.round % step) < w) | ((f.Y % step) < w)
        inner = (np.abs((f.round % step) - step / 2) < 0.3) & (np.abs((f.Y % step) - step / 2) < 0.3)
        return (f.side & (a | inner), col, 0.08)
    return fn


def p_lacing(col):
    def fn(f):                                  # front laced shut: a seam with cross ties
        if f.name != "front" or f.part != "body":
            return (np.zeros(f.shape, bool), col, 0)
        x = np.abs(f.X)
        seam = x < 0.12
        tie = (np.abs(x - (0.6 - np.abs((f.Y % 1.2) - 0.6))) < 0.12) & (x < 0.7)
        return (seam | tie, col, 0.35)
    return fn


# ------------------------------------------------------------------ outfits from culture_dress.csv
def palette(row):
    main, accent, trim = hexrgb(row["main"]), hexrgb(row["accent"]), hexrgb(row["trim"])
    o = row["ornament"].lower()
    beads = [c for k, c in ORNAMENT.items() if re.search(r"\b" + k, o)] or [accent]
    return {"main": main, "dark": shade(main, 0.72), "accent": accent, "trim": trim, "leather": mix(shade(main, 0.62), LEATHER, 0.45),
            "fur": mix(shade(main, 1.3), (200, 186, 160), 0.35), "fibre": mix(SAND, main, 0.3), "beads": beads,
            "bark": mix((150, 118, 82), main, 0.35), "bone": ORNAMENT["bone"]}


def style(row):
    g = row["garments"].lower()
    fibre = bool(re.search(r"fibre|flax|linen|palm|agave|reed", g))
    hide = bool(re.search(r"hide|skin|fur|leather", g))
    return {
        "cut": "parka" if "parka" in g else "robe" if "robe" in g else "tunic" if re.search(r"tunic|coat", g)
        else "cloak" if re.search(r"cloak|kaross", g) else "wrap" if re.search(r"wrap|kilt|apron|skirt|loincloth", g) else "garments",
        "cloak": bool(re.search(r"cloak|kaross", g)), "kilt": "kilt" in g, "apron": "apron" in g, "loincloth": "loincloth" in g,
        "trousers": "trousers" in g, "leggings": "legging" in g, "sandals": "sandal" in g, "skirt": "skirt" in g,
        "bark": "bark" in g, "fibre": fibre, "hide": hide, "fur": "fur" in g, "hooded": "hood" in g or "parka" in g,
        "cord": "cord" in g,
    }


def ornament(row):
    o = row["ornament"].lower()
    return {"pendant": "pendant" in o, "headband": "headband" in o, "cap": "cap" in o, "paint": bool(re.search(r"paint|ochre", o)),
            "pipeclay": "pipeclay" in o, "antler": "antler" in o, "plaster": "plaster" in o, "lacquer": "lacquer" in o}


def outfit(row, gender, look):
    """Pieces for one culture, gender and look: 0, 1, 2 (everyday) or ruler, warrior, priest."""
    rng = Random(zlib.crc32(f"{row['culture']}/{gender}/{look}".encode()))
    crng = Random(zlib.crc32(row["culture"].encode()))         # culture-wide choices: same across its people
    P, st, orn = palette(row), style(row), ornament(row)
    female = gender == "female"
    cut = st["cut"]
    main_kind = "fur" if st["fur"] and cut == "parka" else "bark" if st["bark"] and not st["hide"] else \
        "cloth" if st["fibre"] and not st["hide"] else "hide"
    second_kind = "cloth" if st["fibre"] else "bark" if st["bark"] else "hide"
    second_col = P["fibre"] if second_kind == "cloth" else P["bark"] if second_kind == "bark" else P["dark"]
    deco = crng.choice({"hide": ["panels", "hem", "zigzag", "dots"], "fur": ["panels", "hem"], "cloth": ["stripes", "chevron", "zigzag"],
                        "bark": ["grid", "stripes", "dots"]}[main_kind])
    if look == 1:
        deco = crng.choice([d for d in ["panels", "hem", "zigzag", "dots", "stripes"] if d != deco])
    pieces = []

    def pattern(hem, col=None):
        col = col or P["trim"]
        return {"panels": [p_panels(shade(P["main"], 0.6)), p_hem(col, hem)], "hem": [p_hem(col, hem), p_hem(P["accent"], hem + 0.9, 0.35)],
                "zigzag": [p_zigzag(col, hem)], "dots": [p_dots(col), p_hem(col, hem)], "stripes": [p_stripes(col)],
                "chevron": [p_chevron(col), p_hem(col, hem)], "grid": [p_grid(shade(col, 0.8))]}[deco]

    worker = look == 2
    role = look if isinstance(look, str) else None
    cold = cut in ("parka", "tunic", "robe")

    # legs and feet
    if st["trousers"] or (st["leggings"] and not (female and cut == "robe")):
        pieces.append(Piece("base", lambda f: covering(f, 2.0, 99, False) & np.isin(f.part, LEGS) if f.part in LEGS else np.zeros(f.shape, bool),
                            "fur" if cut == "parka" and st["fur"] else "hide", P["dark"] if cut != "parka" else shade(P["main"], 0.85), 0.2,
                            [p_panels(shade(P["main"], 0.5))] if cut == "parka" else [], "legs"))
    if cold or st["leggings"]:
        top = 4.5 if cut == "parka" else 2.4
        pieces.append(Piece("base", lambda f, t=top: (band(f, LEGS, -1, t + jag(f, 0.2, 4)) | ((f.part in LEGS) & (f.name == "bottom"))),
                            "leather", shade(P["leather"], 0.85), 0.6, [p_hem(P["fur"], top - 0.9, 0.6)] if cut == "parka" else [], "boots"))
    elif st["sandals"]:
        pieces.append(Piece("base", lambda f: band(f, LEGS, 0, 0.5) | ((f.part in LEGS) & (f.name == "bottom")) |
                            (band(f, LEGS, 0.5, 2.2) & (np.abs(f.X + (2 if f.part == "rleg" else -2)) < 0.25) & (f.name == "front")),
                            "leather", P["leather"], 0.4, [], "sandals"))

    # main garment
    if cut in ("parka", "tunic", "robe", "garments"):
        hem = {"parka": 7.0, "tunic": 9.0, "robe": 3.5, "garments": 9.5}[cut] - (2.5 if female and cut != "robe" else 0)
        if role == "priest":
            hem = 2.5
        sleeve = {"parka": 13.0, "tunic": 14.5, "robe": 15.0, "garments": 19.5}[cut]
        if worker or look == 1 and cut != "parka":
            sleeve = 19.5
        kind = main_kind if cut != "garments" else "hide"
        pats = pattern(hem) + ([p_lacing(P["leather"])] if cut in ("tunic", "parka") and not role == "priest" else [])
        pieces.append(Piece("base", lambda f, h=hem, sl=sleeve: covering(f, h + jag(f, 0.25, 1), sl + jag(f, 0.2, 3)), kind,
                            P["main"], 0.5, pats, "top"))
        if cut == "parka":                       # fur cuffs and hem ruff
            pieces.append(Piece("base", lambda f: band(f, ARMS, 12.8, 14.0 + jag(f, 0.3, 6)) | band(f, ("body",) + LEGS, hem, hem + 1.1 + jag(f, 0.3, 7)),
                                "fur", P["fur"], 0.9, [], "ruff"))
        if cut == "garments" and st["fibre"]:    # fibre wrap skirt over the hide shirt
            pieces.append(Piece("base", lambda f: band(f, ("body",) + LEGS, 5.5 + jag(f, 0.3, 8), 13.0), "cloth", P["fibre"], 0.6,
                                [p_stripes(P["trim"], 1.8, 0.35)], "skirt"))
    else:                                        # cloak and wrap cultures: lower body wrapped, chest bare on men
        lo = 5.0 if st["kilt"] or cut == "wrap" else 7.0
        if st["skirt"]:                           # fibre skirt: hanging strands with gaps
            pieces.append(Piece("base", lambda f: band(f, ("body",) + LEGS, 3.5, 13.0) & (((f.round * 3.3) % 1) < 0.72 + 0.28 * (f.Y > 10)),
                                "cloth", P["fibre"], 0.7, [], "skirt"))
        elif st["apron"] or st["loincloth"]:
            w = 3.2 if st["apron"] else 2.1
            pieces.append(Piece("base", lambda f, w=w: (band(f, ("body",), 11.5, 13.2)) | ((np.abs(f.X) < w) & band(f, LEGS + ("body",), lo + jag(f, 0.3, 2), 13.2, ("front", "back"))),
                                second_kind if second_kind != "hide" else "hide", P["main"], 0.5, pattern(lo), "apron"))
        else:
            pieces.append(Piece("base", lambda f: band(f, ("body",) + LEGS, lo + jag(f, 0.3, 2), 13.4), main_kind if cut == "wrap" else "hide",
                                P["main"] if cut == "wrap" else P["dark"], 0.5, pattern(lo), "wrap"))
        if female:
            pieces.append(Piece("base", lambda f: band(f, ("body",), 16.2 + jag(f, 0.2, 3), 21.6), second_kind if cut == "wrap" else "hide",
                                P["main"], 0.4, pattern(16.2), "chest wrap"))
    if role == "priest":                          # ritual robe over everything: long, plain, painted dots
        robe = mix(P["main"], (226, 214, 190), 0.3)
        sign = crng.choice([p_dots(P["trim"], 1.4, 0.25), p_chevron(P["trim"], 2.2), p_stripes(P["trim"], 3.0, 0.4), p_zigzag(P["trim"], 15.0)])
        pieces.append(Piece("base", lambda f: covering(f, 2.5 + jag(f, 0.25, 11), 14.5), main_kind if main_kind != "fur" else "hide",
                            robe, 0.6, [sign, p_hem(P["trim"], 2.5, 0.8), p_hem(P["accent"], 3.5, 0.35)], "ritual robe"))

    # belt, cord, strap
    if st["cord"] or main_kind in ("cloth", "bark"):
        pieces.append(Piece("base", lambda f: band(f, ("body",), 12.3, 12.9), "cloth", shade(P["fibre"], 0.8), 0.9,
                            [lambda f: ((f.round % 0.5) < 0.18, shade(P["fibre"], 0.6), 0.1)], "cord"))
    elif cut not in ("cloak",) or role:
        pieces.append(Piece("base", lambda f: band(f, ("body",), 12.0, 13.2), "leather", P["leather"], 1.0, [], "belt"))
    if worker:                                    # carrying strap across the chest, over the left shoulder
        pieces.append(Piece("base", lambda f: (f.part == "body") & (np.abs((f.Y - 13) - (f.X + 4) * 1.25 * (1 if f.name == "front" else -1 if f.name == "back" else 0)) < 0.6) & np.isin(f.name, ["front", "back"]) |
                            ((f.part == "body") & (f.name == "top") & (f.X > 1.5) & (f.X < 3)),
                            "leather", shade(P["leather"], 0.9), 1.0, [], "strap"))

    # warrior's armour, bracers and body paint
    if role == "warrior":
        armour_col = mix(P["bone"], P["main"], 0.25) if crng.random() < 0.5 else shade(P["leather"], 1.1)
        plate = "bone" if armour_col != shade(P["leather"], 1.1) else "leather"

        def slats(f):
            if f.part != "body" or f.name not in ("front", "back"):
                return np.zeros(f.shape, bool)
            return (f.Y > 14.2) & (f.Y < 22.6) & (np.abs(f.X) < 3.7) & ((f.Y % 1.4) > 0.16)
        pieces.append(Piece("base", slats, plate, armour_col, 1.2,
                            [lambda f: ((np.abs((f.X % 1.6) - 0.8) < 0.1) & (f.part == "body"), P["leather"], 0.3)], "slat armour"))
        pieces.append(Piece("outer", lambda f: (f.part in ARMS) & ((f.name == "top") | ((f.Y > 21.4 + jag(f, 0.2, 2)) & f.side)), plate, armour_col, 1.0,
                            [], "shoulder guards"))
        pieces.append(Piece("base", lambda f: band(f, ARMS, 13.0, 16.4), "leather", P["leather"], 0.8,
                            [lambda f: ((f.Y % 0.8) < 0.18, shade(P["leather"], 0.6), 0.2)], "bracers"))

    # cloaks
    wants_cloak = (st["cloak"] and look != 2) or role == "ruler" or (look == 1 and cut in ("tunic", "robe"))
    if wants_cloak and not (st["cloak"] and look == 1 and not role):
        hem = 4.0 if role == "ruler" else 7.0
        ck = "fur" if role == "ruler" or st["fur"] else "hide"
        col = shade(P["main"], 0.9) if not role == "ruler" else mix(P["main"], P["trim"], 0.25)
        pats = [p_panels(shade(col, 0.6))] if ck == "hide" else []
        if role == "ruler":
            pats += [lambda f: (f.side & (f.Y < hem + 1.4) & (f.Y >= hem) | ((f.part == "body") & (f.name == "front")), P["trim"], 0.2)]
        pieces.append(Piece("outer", lambda f, h=hem: cloak(f, h), ck, col, 0.6, pats, "cloak"))
        pieces.append(Piece("outer", collar, "fur", P["fur"], 1.0, [], "collar"))

    # hoods and headwear (outer head layer)
    if cut == "parka" and look != 1 and role not in ("priest",):
        pieces.append(Piece("outer", hood, main_kind, P["main"], 0.6, [p_panels(shade(P["main"], 0.6))], "hood"))
        pieces.append(Piece("outer", hood_edge, "fur", P["fur"], 1.1, [], "hood ruff"))
    elif role == "ruler":
        if cold:
            pieces.append(Piece("outer", lambda f: cap(f, 29.0 + jag(f, 0.25, 3)), "fur", P["fur"], 1.0, [], "fur hat"))
        else:
            pieces.append(Piece("outer", lambda f: band(f, ("head",), 29.0, 32.6) & ((f.Y < 30.2) | ((f.round % 1.1) < 0.55) & (f.Y < 30.2 + 2.6 * np.abs(np.sin(f.round * 2.86)))),
                                "feather", P["trim"], 1.0, [lambda f: ((f.Y < 30.2), P["leather"], 0.3)], "feather crown"))
    elif role == "priest":
        if orn["antler"]:
            pieces.append(Piece("outer", lambda f: cap(f, 30.0) | ((f.part == "head") & np.isin(f.name, ["left", "right"]) & (f.Y > 27.5) &
                                (np.abs((f.Z - 0.0) - (f.Y - 30) * 0.8) < 0.45)), "bone", P["bone"], 1.2, [], "antler frontlet"))
        elif orn["plaster"]:
            pieces.append(Piece("outer", lambda f: cap(f, 29.6), "plaster", (222, 214, 196), 1.0, [p_dots(P["trim"], 1.3, 0.2)], "plastered cap"))
        else:
            pieces.append(Piece("outer", lambda f: cap(f, 29.2 + jag(f, 0.2, 1)), "hide" if main_kind != "cloth" else "cloth",
                                mix(P["main"], (226, 214, 190), 0.3), 0.6, [p_hem(P["trim"], 29.3, 0.5)], "priest cap"))
    if (orn["headband"] or orn["cap"]) and role != "priest" or (look == 2 and not cold):
        if orn["cap"] and role != "warrior":
            pieces.append(Beads("outer", lambda f: beads_cap(f), P["beads"]))
        else:
            pieces.append(Piece("outer", lambda f: band(f, ("head",), 29.0, 29.9), "leather", P["leather"], 0.8, [], "headband"))
            pieces.append(Beads("outer", lambda f: beads_on_curve(f, lambda u: 29.45 + 0 * u, 0.8, 0.34, ("front", "back", "left", "right"), ("head",)), P["beads"]))

    # jewellery
    strands = 3 if role == "ruler" else 1 if look in (0, 1) or role == "priest" else 0
    for k in range(strands):
        drop = 1.4 + 0.9 * k
        pieces.append(Beads("base", lambda f, d=drop: beads_on_curve(f, lambda u, d=d: 23.6 - d * (1 - (u / 4.2) ** 2), 0.62 - 0.05 * (k == 0), 0.3), P["beads"]))
    if orn["pendant"] and look != 2 or role == "priest":
        pieces.append(Beads("base", pendant, P["beads"][-1:], "bone" if "bone" in row["ornament"].lower() else "bead"))
    if role == "ruler":                             # arm rings
        pieces.append(Beads("base", lambda f: beads_on_curve(f, lambda u: 15.0 + 0 * u, 0.55, 0.3, ("front", "back", "left", "right"), ARMS), P["beads"]))

    # pigment on skin (painted last, only where nothing is worn)
    if role == "warrior" or (orn["paint"] and look == 0):
        col = PIPECLAY if orn["pipeclay"] and role != "warrior" else P["trim"]
        pieces.append(Piece("base", lambda f: band(f, ARMS, 17.0, 17.6) | band(f, ARMS, 18.4, 19.0) | band(f, ARMS, 19.8, 20.4), "paint", col, 0.0, [], "arm paint"))
        pieces.append(Piece("base", lambda f: (f.part == "body") & (f.name == "front") & ((f.Y + np.abs(f.X) * 0.7) % 2.2 < 0.5) & (f.Y > 15), "paint", col, 0.0, [], "chest paint"))
    if role in ("warrior", "priest"):
        col = P["trim"] if role == "warrior" else PIPECLAY
        pieces.append(Piece("base", face_paint(role), "paint", col, 0.0, [], "face paint"))
    return pieces


def beads_cap(f):
    if f.part != "head" or f.name == "bottom":
        return None
    u = f.round if f.side else f.X
    v = f.Y if f.side else f.Z
    k = np.round(u / 0.7) + 1000 * np.round(v / 0.7)
    d = np.hypot(u - np.round(u / 0.7) * 0.7, v - np.round(v / 0.7) * 0.7)
    m = (d < 0.3) & ((f.Y >= 29.6) | (f.name == "top"))
    return m, np.sqrt(np.clip(1 - (d / 0.3) ** 2, 0, 1)), k


def pendant(f):
    if f.part != "body" or f.name != "front":
        return None
    d = np.hypot(f.X / 0.55, (f.Y - 20.2) / 1.0)
    m = d < 1
    return m, np.sqrt(np.clip(1 - d ** 2, 0, 1)), np.zeros(f.shape)


def face_paint(role):
    def fn(f):
        if f.part != "head" or f.name != "front":
            return np.zeros(f.shape, bool)
        x, y = np.abs(f.X), f.Y
        if role == "warrior":                       # two bars under each eye and one across the brow
            return ((x > 1.0) & (x < 3.6) & (((y > 26.0) & (y < 26.4)) | ((y > 25.3) & (y < 25.7)))) | ((y > 29.8) & (y < 30.3) & (x < 3.5))
        return (np.hypot(x - 2.0, y - 26.0) < 0.3) | (np.hypot(x - 1.2, y - 25.6) < 0.3) | (np.hypot(x - 2.8, y - 25.6) < 0.3) | \
               ((x < 0.25) & (y > 28.3) & (y < 30.3))
    return fn


# ------------------------------------------------------------------ painter
def light(H, strength):
    gy, gx = np.gradient(H)
    nrm = np.dstack([-gx * strength, -gy * strength, np.ones_like(H)])
    nrm /= np.linalg.norm(nrm, axis=2, keepdims=True)
    return nrm @ LIGHT


def paint(pieces, ao, seed):
    """Paints the outfit onto a 512x512 RGBA texture. ao: 512x512 float occlusion from ao_bake.py."""
    out = np.zeros((skin.SIZE, skin.SIZE, 4), np.float32)
    rng = np.random.default_rng(seed)
    for f in skin.faces():
        h, w = f.shape
        col = np.zeros((h, w, 3), np.float32)
        kind = np.full((h, w), -1, np.int16)
        lift = np.zeros((h, w), np.float32)
        bead = np.zeros((h, w), np.float32)
        shine = np.zeros((h, w), np.float32)
        on = np.zeros((h, w), bool)
        for p in pieces:
            if p.layer != f.layer:
                continue
            if isinstance(p, Beads):
                r = p.fn(f)
                if r is None:
                    continue
                m, dome, k = r
                if not m.any():
                    continue
                pick = (np.abs(k).astype(np.int64) * 7919) % len(p.colours)
                tint = 0.9 + 0.2 * ((np.abs(k).astype(np.int64) * 104729) % 7) / 6
                c = np.array(p.colours, np.float32)[pick] * tint[..., None]
                col[m] = c[m]
                kind[m] = KINDS.index(p.kind)
                lift[m] = lift[m] + p.lift * dome[m] + 0.3
                bead[m] = dome[m]
                shine[m] = SHINE.get(p.kind, 0.5)
                on |= m
                continue
            m = p.where(f)
            if m is None or not np.any(m):
                continue
            m = np.asarray(m, bool) & np.ones((h, w), bool)
            c = np.broadcast_to(np.array(p.colour, np.float32), (h, w, 3)).copy()
            extra = np.zeros((h, w), np.float32)
            for pat in p.patterns:
                pm, pc, pl = pat(f)
                pm = np.asarray(pm, bool) & np.ones((h, w), bool)
                c[pm] = pc
                extra[pm] += pl
            col[m] = c[m]
            kind[m] = KINDS.index(p.kind)
            base = np.where(on, lift, 0)
            lift[m] = (base + p.lift + extra)[m] if p.lift > 0 else (lift + extra)[m]
            bead[m] = 0
            shine[m] = SHINE.get(p.kind, 0.0)
            on |= m
        if not on.any():
            continue
        H = np.zeros((h, w), np.float32)
        strength = np.ones((h, w), np.float32)
        for i, k in enumerate(KINDS):
            sel = kind == i
            if sel.any():
                H[sel] = height(k, h, w, rng)[sel]
                strength[sel] = STRENGTH[k]
        L = soft(lift, max(1, S // 4))
        Ht = H * 0.5 + L * S * 0.35 + bead * S * 0.25
        lit = light(Ht, 1.0) * 0.5 + light(H, 1.0) * 0.5 * strength / 1.6
        yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
        form = (1.08 - 0.16 * yy / max(h - 1, 1) - 0.06 * xx / max(w - 1, 1)) if f.side else np.full((h, w), 1.04, np.float32)
        hue = 1 + 0.06 * norm(soft(rng.random((h, w)), 2 * S))       # uneven dye and tanning
        c = col * (0.55 + 0.55 * lit)[..., None] * (form * hue)[..., None]
        c += 70 * (np.clip(lit - 0.86, 0, 1) * 6 * shine)[..., None]  # glints on beads, bone, leather
        contact = np.clip((soft(L, S) - L) * 1.6, 0, 0.6)               # grime where one part sits on another
        occl = np.clip(ao[f.sl] if f.layer == "base" or True else 1, 0, 1)
        dark = np.clip(1 - (0.62 * (1 - occl) + contact), 0, 1)
        c = c * dark[..., None] + GRIME * (1 - dark)[..., None] * 0.6
        if f.part in ("rleg", "lleg"):                                  # dust and mud low on the legs
            mud = np.clip((3.2 - f.Y) / 3.2 + 0.25 * norm(soft(rng.random((h, w)), S)) * 0.4, 0, 0.55)
            c = c * (1 - mud[..., None]) + np.array(DIRT, np.float32) * mud[..., None]
        grey = c.mean(-1, keepdims=True)
        c = np.clip((grey + (c - grey) * MUTE) * WARM, 6, 248)          # no pure black, no pure white
        out[f.sl][on] = np.dstack([c, np.full((h, w), 255, np.float32)])[on]
    return Image.fromarray(out.round().astype(np.uint8), "RGBA")


def load_ao(path):
    im = np.asarray(Image.open(path).convert("RGBA"), np.float32) / 255
    return np.where(im[..., 3] > 0, im[..., 0], 1.0)


LOOKS = (0, 1, 2, "ruler", "warrior", "priest")


def render(row, gender, look, ao):
    return paint(outfit(row, gender, look), ao, zlib.crc32(f"{row['culture']}/{gender}/{look}".encode()))
