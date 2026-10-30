#!/usr/bin/env python3
"""Compose the textures of diaithir-classes from vanilla sprites and flat colours (MIT).

Usage: python3 -I tools/textures.py <dir that holds assets/minecraft/textures of the vanilla client>
Vanilla sprites are recoloured by luminance ramps (tier metal) mixed with the class colour; rings, charms, emblems,
rune stones and spell tiles are drawn from flat colours. Nothing is read from any other mod.
"""
import hashlib
import os
import random
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import content as C  # noqa: E402

OUT = os.path.join(ROOT, "src/main/resources/assets", C.NS, "textures")
VAN = os.path.join(sys.argv[1], "assets/minecraft/textures") if len(sys.argv) > 1 else None

TIER_RAMP = {
    "bronze": ((70, 38, 18), (196, 120, 60), (244, 196, 120)),
    "steel": ((44, 50, 62), (150, 158, 172), (236, 242, 250)),
    "composite": ((40, 40, 46), (122, 114, 106), (216, 206, 192)),
}
WOOD_RAMP = ((52, 34, 22), (120, 86, 56), (190, 150, 104))
LEATHER_RAMP = ((56, 36, 24), (130, 86, 50), (196, 146, 96))
SLATE = (88, 92, 96)
SLATE_D = (58, 62, 64)
SLATE_L = (122, 126, 126)
BRONZE = (196, 120, 60)
BRONZE_H = (228, 164, 88)
BRONZE_D = (120, 70, 36)
GOLD = (232, 176, 52)
GLOW = {"green": ((40, 96, 64), (110, 214, 140), (200, 250, 205)), "silver": ((50, 80, 110), (150, 196, 230), (225, 240, 250)),
        "amber": ((120, 64, 20), (240, 168, 64), (255, 226, 150))}


def lerp(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def ramp(stops, t):
    return lerp(stops[0], stops[1], t * 2) if t < 0.5 else lerp(stops[1], stops[2], (t - 0.5) * 2)


_cache = {}


def sprite(name):
    if name not in _cache:
        _cache[name] = Image.open(os.path.join(VAN, "item", name + ".png")).convert("RGBA")
    return _cache[name]


def recolor(im, stops, mix=None, mixamt=0.0, contrast=1.15, seed=""):
    out = Image.new("RGBA", im.size)
    px, po = im.load(), out.load()
    rnd = random.Random(int(hashlib.sha1(seed.encode()).hexdigest()[:8], 16))
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            lum = max(0.0, min(1.0, (lum - 0.5) * contrast + 0.5 + rnd.uniform(-0.03, 0.03)))
            c = ramp(stops, lum)
            if mix is not None and 0.25 < lum < 0.8:
                c = lerp(c, mix, mixamt)
            po[x, y] = (*c, a)
    return out


def save(im, rel):
    p = os.path.join(OUT, rel + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)


def is_wood(r, g, b):
    mx, mn = max(r, g, b), min(r, g, b)
    return mx - mn > 28 and r >= g >= b


def recolor_bow(im, tk, cls):
    out = im.copy()
    px = out.load()
    cc = C.CLASSES[cls]["rgb"]
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a and is_wood(r, g, b):
                lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                c = ramp(TIER_RAMP[tk], max(0.0, min(1.0, lum * 1.1)))
                px[x, y] = (*lerp(c, cc, 0.3), a)
    return out


def armour_icon(piece, tk, cls):
    return recolor(sprite(f"iron_{piece}"), TIER_RAMP[tk], C.CLASSES[cls]["rgb"], 0.32, seed=f"{cls}{piece}{tk}")


def trim(im, cls, points):
    cc = C.CLASSES[cls]["rgb"]
    p = im.load()
    for (x, y), f in points:
        if p[x, y][3]:
            p[x, y] = (*lerp(cc, (255, 255, 255), f), 255)
    return im


def dagger_icon(tk, cls):
    base = recolor(sprite("iron_sword"), TIER_RAMP[tk], C.CLASSES[cls]["rgb"], 0.3, seed=f"{cls}dagger{tk}")
    bb = base.getbbox()
    crop = base.crop(bb)
    w, h = crop.size
    crop = crop.resize((max(1, int(w * 0.72)), max(1, int(h * 0.72))), Image.NEAREST)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    im.alpha_composite(crop, (max(0, 14 - crop.size[0]), 15 - crop.size[1] if crop.size[1] < 16 else 0))
    return im


def sling_icon(tk, cls):
    base = recolor(sprite("lead"), LEATHER_RAMP, C.CLASSES[cls]["rgb"], 0.25, seed=f"{cls}sling{tk}")
    p = base.load()
    c = TIER_RAMP[tk]
    for (x, y), t in (((7, 7), 1.0), ((8, 7), 0.8), ((7, 8), 0.6), ((8, 8), 0.4)):
        p[x, y] = (*ramp(c, t), 255)
    return base


def crozier_icon(tk, cls):
    shaft = recolor(sprite("stick"), WOOD_RAMP, C.CLASSES[cls]["rgb"], 0.2, contrast=1.2, seed=f"{cls}croz{tk}")
    p = shaft.load()
    c = TIER_RAMP[tk]
    hook = [(11, 1), (12, 1), (13, 2), (14, 3), (14, 4), (13, 5), (12, 5), (11, 4)]
    for i, (x, y) in enumerate(hook):
        p[x, y] = (*ramp(c, 0.9 - i * 0.07), 255)
    p[12, 3] = (*C.CLASSES[cls]["rgb"], 255)
    for (x, y) in ((10, 6), (9, 7)):
        p[x, y] = (*ramp(c, 0.55), 255)
    return shaft


def emblem_icon(cls):
    cc = C.CLASSES[cls]["rgb"]
    dark, light = lerp(cc, (0, 0, 0), 0.45), lerp(cc, (255, 255, 255), 0.45)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = im.load()
    for y in range(16):
        for x in range(16):
            d = (x - 7.5) ** 2 + (y - 7.5) ** 2
            if d <= 49:
                p[x, y] = (*(dark if d > 36 else cc), 255)
    glyph = {
        "fian": [(7, 3), (8, 3), (7, 4), (8, 4), (7, 5), (8, 5), (7, 6), (8, 6), (7, 7), (8, 7), (7, 8), (8, 8), (6, 9), (9, 9), (7, 10), (8, 10)],
        "sleagh": [(4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8), (10, 7), (10, 9), (9, 6), (9, 10), (4, 7), (4, 9)],
        "ceilg": [(5, 4), (6, 5), (7, 6), (8, 7), (9, 8), (10, 9), (6, 4), (7, 5), (8, 6), (9, 7), (10, 8), (5, 9), (6, 10)],
        "cleirech": [(7, 3), (8, 3), (7, 4), (8, 4), (7, 5), (8, 5), (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7), (11, 7),
                     (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8), (7, 9), (8, 9), (7, 10), (8, 10), (7, 11), (8, 11)],
    }[cls]
    for x, y in glyph:
        p[x, y] = (*light, 255)
    return im


OGHAM_RIGHT = {"beith": 1, "luis": 2, "fearn": 3, "sail": 4, "nion": 5}
OGHAM_LEFT = {"uath": 1, "dair": 2, "tinne": 3, "coll": 4, "iodhadh": 5}


def rune_icon(rid):
    glow = GLOW[("green", "silver", "amber")[sum(map(ord, rid)) % 3]]
    base = recolor(sprite("flint"), (SLATE_D, SLATE, SLATE_L), contrast=1.0, seed=rid)
    # stone: widen to a standing slab
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = im.load()
    rnd = random.Random(int(hashlib.sha1(rid.encode()).hexdigest()[:8], 16))
    for y in range(1, 15):
        for x in range(4, 12):
            if (x in (4, 11) and y in (1, 14)) or (y == 1 and x in (5, 10)):
                continue
            r = rnd.random()
            c = SLATE_D if (x in (4, 11) or y == 14) else (SLATE_L if r < 0.12 else SLATE)
            p[x, y] = (*c, 255)
    for y in range(3, 13):
        p[7, y] = (*glow[1], 255)
    p[7, 3] = (*glow[2], 255)
    strokes = OGHAM_RIGHT.get(rid) or OGHAM_LEFT.get(rid)
    side = 1 if rid in OGHAM_RIGHT else -1
    if strokes is None:  # ruis: crossing strokes
        for y in (5, 8, 11):
            for dx in (-2, -1, 1, 2):
                p[7 + dx, y] = (*glow[1], 255)
        return im
    for i in range(strokes):
        y = 4 + i * 2
        for dx in (1, 2, 3):
            p[7 + side * dx, y] = (*glow[1], 255)
    return im


def ring_icon(tk, kind, sockets):
    c = TIER_RAMP[tk]
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = im.load()
    if kind == "ring":
        pts = []
        for y in range(16):
            for x in range(16):
                d = ((x - 7.5) / 5.2) ** 2 + ((y - 9) / 5.6) ** 2
                if 0.55 <= d <= 1.0:
                    t = 0.9 - 0.5 * ((x + y) / 30.0)
                    p[x, y] = (*ramp(c, max(0.05, min(0.95, t))), 255)
        # socket studs along the top
        xs = {1: [8], 2: [6, 9], 3: [5, 8, 11]}[min(sockets, 3)]
        for x in xs:
            for dy in (0, 1):
                p[x, 2 + dy] = (*GLOW["silver"][1 + dy % 2 if False else 1], 255)
            p[x, 2] = (*GLOW["silver"][2], 255)
            p[x, 3] = (*GLOW["silver"][1], 255)
    else:
        for y in range(2, 14):  # cord
            if y < 6:
                p[7 - (6 - y) // 2, y] = (*LEATHER_RAMP[1], 255)
                p[8 + (6 - y) // 2, y] = (*LEATHER_RAMP[1], 255)
        for y in range(16):  # diamond pendant
            for x in range(16):
                if abs(x - 7.5) + abs(y - 10) <= 5:
                    p[x, y] = (*ramp(c, 0.85 - 0.5 * ((x + y - 10) / 20.0 + 0.3)), 255)
        xs = {1: [7], 2: [6, 9]}[min(sockets, 2)]
        for x in xs:
            p[x, 10] = (*GLOW["silver"][2], 255)
            p[x, 11] = (*GLOW["silver"][1], 255)
    return im


def spell_tile(sid):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = im.load()
    rnd = random.Random(int(hashlib.sha1(sid.encode()).hexdigest()[:8], 16))
    for y in range(16):
        for x in range(16):
            if x in (0, 15) and y in (0, 15):
                continue
            if x in (0, 15) or y in (0, 15):
                p[x, y] = (*(BRONZE_H if (y == 0 or x == 0) else BRONZE_D), 255)
            else:
                r = rnd.random()
                p[x, y] = (*(SLATE_D if r < 0.18 else SLATE_L if r < 0.3 else SLATE), 255)
    return im


GLYPH = {  # spell id stem -> vanilla item sprite
    "fian_shout": "goat_horn", "fian_charge": "iron_boots", "fian_spear_cast": "iron_spear", "fian_ground_blow": "iron_pickaxe",
    "sleagh_volley": "arrow", "sleagh_net_trap": "string", "sleagh_sling_shot": "flint", "sleagh_hawk_eye": "spyglass",
    "ceilg_shadow_cloak": "phantom_membrane", "ceilg_ambush": "iron_sword", "ceilg_knife_fan": "iron_nugget", "ceilg_mark_prey": "ender_eye",
    "cleirech_mercy": "honeycomb", "cleirech_aegis": "turtle_scute", "cleirech_smite": "blaze_powder", "cleirech_sanctuary": "bell",
    "fian_rage": "redstone", "sleagh_keen": "feather", "ceilg_bleed": "spider_eye", "cleirech_ward": "gold_nugget",
}
SCHOOL_GLOW = {"fian": "amber", "sleagh": "green", "ceilg": "silver", "cleirech": "amber"}


def spell_icon(sp):
    sid = sp["id"]
    stem = sid.rsplit("_", 1)[0] if sid.rsplit("_", 1)[-1] in ("bronze", "steel", "composite") else sid
    glow = GLOW[SCHOOL_GLOW[sp["cls"]]]
    im = spell_tile(sid)
    g = recolor(sprite(GLYPH.get(stem, "paper")), glow, contrast=1.25).resize((9, 9), Image.LANCZOS)
    halo = Image.new("RGBA", (11, 11), (0, 0, 0, 0))
    halo.paste(Image.new("RGBA", g.size, (*SLATE_D, 255)), (1, 1), g.split()[3].point(lambda v: 150 if v > 40 else 0))
    im.alpha_composite(halo, (2, 1))
    im.alpha_composite(g, (3, 2))
    p = im.load()
    for x in range(3, 13):
        p[x, 12] = (*BRONZE_D, 255)
    tier = sp["spell"]["tier"] + 1
    if sid.rsplit("_", 1)[-1] in ("bronze", "steel", "composite"):
        tier = {"bronze": 1, "steel": 2, "composite": 3}[sid.rsplit("_", 1)[-1]]
    for i in range(tier):
        x = 4 + i * 2
        for y in (11, 12, 13):
            p[x, y] = (*(glow[1] if y != 12 else glow[2]), 255)
    return im


def main():
    if VAN is None:
        print(__doc__)
        sys.exit(1)
    for cls in C.CLASSES:
        save(emblem_icon(cls), f"item/{cls}_emblem")
        for tk, _, n in C.TIERS:
            for piece in C.SLOTS:
                save(armour_icon(piece, tk, cls), f"item/{cls}_{piece}_{tk}")
            if cls == "fian":
                save(recolor(sprite("iron_sword"), TIER_RAMP[tk], C.CLASSES[cls]["rgb"], 0.3, seed=f"fs{tk}"), f"item/fian_sword_{tk}")
                save(recolor(sprite("iron_spear"), TIER_RAMP[tk], C.CLASSES[cls]["rgb"], 0.3, seed=f"fsp{tk}"), f"item/fian_spear_{tk}")
                save(recolor(sprite("iron_spear_in_hand"), TIER_RAMP[tk], C.CLASSES[cls]["rgb"], 0.3, seed=f"fsph{tk}"), f"item/fian_spear_{tk}_in_hand")
            if cls == "sleagh":
                save(recolor_bow(sprite("bow"), tk, cls), f"item/sleagh_bow_{tk}")
                for i in range(3):
                    save(recolor_bow(sprite(f"bow_pulling_{i}"), tk, cls), f"item/sleagh_bow_{tk}_pulling_{i}")
                save(sling_icon(tk, cls), f"item/sleagh_sling_{tk}")
            if cls == "ceilg":
                save(dagger_icon(tk, cls), f"item/ceilg_dagger_{tk}")
            if cls == "cleirech":
                save(crozier_icon(tk, cls), f"item/cleirech_crozier_{tk}")
    for r in C.RUNES:
        save(rune_icon(r[0]), f"item/rune_{r[0]}")
    for rid, kind, tk, sockets, slot in C.RINGS:
        save(ring_icon(tk, kind, sockets), f"item/{rid}")
    for sp in C.SPELLS:
        save(spell_icon(sp), f"spell/{sp['id']}")
    print("textures written to", OUT)


if __name__ == "__main__":
    main()
