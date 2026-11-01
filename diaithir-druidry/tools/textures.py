#!/usr/bin/env python3
"""Compose the 16x16 textures of diaithir-druidry from vanilla sprites and flat colours. MIT.

Usage: python3 -I tools/textures.py <minecraft client or merged jar>

Nothing is read from any other mod. Vanilla item sprites are recoloured to a school palette and set on a
slate tile with a bronze rim and an ogham tally (one stroke per spell tier), after the Celtic magic rule:
grey standing-stone, bronze and gold, red and blue enamel studs, soft green / silver-blue / amber glow.
"""
import hashlib
import io
import os
import random
import sys
import zipfile

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import spells as SP  # noqa: E402

OUT = os.path.join(ROOT, "src", "main", "resources", "assets", SP.NS, "textures")

SLATE = (88, 92, 96)
SLATE_D = (58, 62, 64)
SLATE_L = (116, 120, 120)
BRONZE = (196, 120, 60)
BRONZE_H = (228, 164, 88)
BRONZE_D = (120, 70, 36)
GOLD = (232, 176, 52)
RED = (150, 44, 38)
BLUE = (42, 62, 130)
MOSS = (74, 98, 58)

GLOW = {
    "green": ((40, 96, 64), (110, 214, 140), (200, 250, 205)),
    "silver": ((50, 80, 110), (150, 196, 230), (225, 240, 250)),
    "amber": ((120, 64, 20), (240, 168, 64), (255, 226, 150)),
}
SCHOOL_GLOW = {"druid": "green", "ovate": "silver", "bard": "amber", "filid": "silver",
               "fitech": "amber", "sidhe": "silver", "smith": "amber"}
WOOD = {"druid": (118, 86, 56), "ovate": (94, 60, 48), "bard": (150, 112, 76), "filid": (132, 100, 62),
        "fitech": (84, 56, 46), "sidhe": (176, 160, 140), "smith": (104, 72, 48)}

GLYPH = {  # spell id -> vanilla item sprite used as the glyph
    "thornlash": "sweet_berries", "dewdrop": "slime_ball", "briar_snare": "string", "oakskin": "leather",
    "grove_mending": "bone_meal", "stag_leap": "rabbit_foot", "mistletoe_spear": "arrow",
    "sight_spark": "amethyst_shard", "far_sight": "ender_eye", "fate_thread": "lead", "veil_step": "ender_pearl",
    "salmon_knowing": "salmon", "watch_hour": "spyglass", "foretold_blow": "echo_shard",
    "sharp_note": "copper_ingot", "lullaby": "feather", "lament": "bell", "laughter": "golden_carrot",
    "horn_cry": "goat_horn", "banner_song": "name_tag", "storm_chord": "glowstone_dust",
    "verse_blow": "writable_book", "ward_verse": "turtle_scute", "satire": "ink_sac", "truth_word": "book",
    "hundred_verses": "paper", "ban_oath": "bone", "curse_pall": "fermented_spider_eye",
    "crow_strike": "iron_sword", "battle_cry": "feather", "raven_leap": "phantom_membrane", "battle_fury": "redstone",
    "talon_wheel": "flint", "field_terror": "rotten_flesh", "ford_washer": "netherite_sword",
    "fairy_mist": "snowball", "glamour_veil": "glass_bottle", "hill_step": "chorus_fruit", "folk_gift": "honeycomb",
    "silver_branch": "iron_nugget", "folk_hour": "clock_00", "hail_fall": "prismarine_shard",
    "forge_spark": "flint_and_steel", "quench": "water_bucket", "hot_iron": "iron_ingot", "tempering": "iron_pickaxe",
    "slag_cloud": "coal", "forge_breath": "fire_charge", "molten_rain": "lava_bucket",
}


def load_sprites(jarpath):
    z = zipfile.ZipFile(jarpath)
    cache = {}

    def get(name):
        if name in cache:
            return cache[name]
        p = f"assets/minecraft/textures/item/{name}.png"
        try:
            im = Image.open(io.BytesIO(z.read(p))).convert("RGBA")
            if im.size[0] != im.size[1]:
                im = im.crop((0, 0, im.size[0], im.size[0]))
        except KeyError:
            im = None
        cache[name] = im
        return im
    return get


def lerp(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def ramp(stops, t):
    if t < 0.5:
        return lerp(stops[0], stops[1], t * 2)
    return lerp(stops[1], stops[2], (t - 0.5) * 2)


def recolor(im, stops, boost=1.0):
    im = im.convert("RGBA")
    out = Image.new("RGBA", im.size)
    px, po = im.load(), out.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            lum = max(0.0, min(1.0, (lum - 0.5) * boost + 0.5))
            c = ramp(stops, lum)
            po[x, y] = (*c, a)
    return out


def rng_for(name):
    return random.Random(int(hashlib.sha1(name.encode()).hexdigest()[:8], 16))


def tile(name):
    """Slate tile with bronze rim, studs and moss in the crevices."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = im.load()
    rnd = rng_for(name)
    for y in range(16):
        for x in range(16):
            corner = (x in (0, 15)) and (y in (0, 15))
            if corner:
                continue
            edge = x in (0, 15) or y in (0, 15)
            if edge:
                p[x, y] = BRONZE_H if (y == 0 or x == 0) else BRONZE_D
            else:
                c = SLATE
                r = rnd.random()
                if r < 0.18:
                    c = SLATE_D
                elif r < 0.30:
                    c = SLATE_L
                elif r < 0.34:
                    c = MOSS
                p[x, y] = (*c, 255)
    for x, y in ((1, 1), (14, 14)):
        p[x, y] = (*(RED if (x, y) == (1, 1) else BLUE), 255)
    p[1, 14] = (*BRONZE, 255)
    p[14, 1] = (*BRONZE, 255)
    for x in range(1, 15):
        p[x, 1] = (*BRONZE, 255) if p[x, 1][:3] not in (RED, BLUE) else p[x, 1]
    return im


def spell_icon(sd, sc, get):
    im = tile(sd["id"])
    glow = GLOW[SCHOOL_GLOW[sc["key"]]]
    sprite = get(GLYPH.get(sd["id"], "paper")) or get("paper")
    g = recolor(sprite, glow, boost=1.25).resize((9, 9), Image.LANCZOS)
    # dark halo so the glyph reads on slate
    halo = Image.new("RGBA", (11, 11), (0, 0, 0, 0))
    ga = g.split()[3].point(lambda v: 150 if v > 40 else 0)
    shadow = Image.new("RGBA", g.size, (*SLATE_D, 255))
    halo.paste(shadow, (1, 1), ga)
    im.alpha_composite(halo, (2, 1))
    im.alpha_composite(g, (3, 2))
    p = im.load()
    # ogham tally: a stem across the bottom, one stroke per tier (1..4)
    for x in range(3, 13):
        p[x, 12] = (*BRONZE_D, 255)
    tier = sd["spell"]["tier"] + 1
    for i in range(tier):
        x = 4 + i * 2
        for y in (11, 12, 13):
            p[x, y] = (*glow[1], 255) if y != 12 else (*glow[2], 255)
    return im


def book_icon(sc, get):
    glow = GLOW[SCHOOL_GLOW[sc["key"]]]
    base = recolor(get("book"), ((28, 38, 30), MOSS, (206, 196, 164)), boost=1.1)
    p = base.load()
    # bronze clasp on the spine and corner caps
    for y in range(2, 14):
        if p[3, y][3]:
            p[3, y] = (*BRONZE, 255)
    for x, y in ((4, 2), (13, 13)):
        if p[x, y][3]:
            p[x, y] = (*BRONZE_H, 255)
    # carved glow channel in the centre of the cover: a short spiral
    for (x, y) in ((8, 7), (9, 7), (9, 8), (9, 9), (8, 9), (7, 9), (7, 8), (8, 8)):
        if p[x, y][3]:
            p[x, y] = (*glow[1], 255)
    if p[8, 8][3]:
        p[8, 8] = (*glow[2], 255)
    return base


def scroll_icon(sc, get):
    glow = GLOW[SCHOOL_GLOW[sc["key"]]]
    base = recolor(get("paper"), ((120, 92, 60), (210, 190, 150), (244, 232, 196)), boost=1.0)
    p = base.load()
    for y in range(16):
        for x in range(16):
            if p[x, y][3] and (x + y) % 11 == 0:
                p[x, y] = (*BRONZE, 255)
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        p[x, y] = (*glow[1], 255)
    p[7, 7] = (*glow[2], 255)
    p[5, 5] = (*RED, 255)
    p[10, 10] = (*BLUE, 255)
    return base


def staff_icon(sc, n, get):
    key = sc["key"]
    glow = GLOW[SCHOOL_GLOW[key]]
    wood = WOOD[key]
    shaft = recolor(get("stick"), (lerp(wood, (0, 0, 0), 0.45), wood, lerp(wood, (255, 255, 255), 0.35)), boost=1.2)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    im.alpha_composite(shaft)
    p = im.load()
    metal = BRONZE if n == 1 else GOLD
    metal_h = BRONZE_H if n == 1 else (250, 214, 110)
    metal_d = BRONZE_D if n == 1 else (150, 106, 28)
    # head: a ring of knotwork at the top right, glow gem inside
    ring = [(11, 1), (12, 1), (13, 2), (14, 3), (14, 4), (13, 5), (12, 5), (11, 4), (10, 3), (10, 2)]
    for i, (x, y) in enumerate(ring):
        p[x, y] = (*(metal_h if i < 4 else metal if i < 7 else metal_d), 255)
    p[12, 3] = (*glow[1], 255)
    p[12, 2] = (*glow[2], 255)
    p[11, 3] = (*glow[0], 255)
    p[13, 3] = (*glow[1], 255)
    p[11, 5] = (*metal_d, 255)
    p[10, 6] = (*metal, 255)
    p[9, 7] = (*metal_h, 255)
    # bands on the shaft
    bands = [(7, 9), (5, 11)] if n == 1 else [(8, 8), (6, 10), (4, 12)]
    for x, y in bands:
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            if p[x + dx, y + dy][3]:
                p[x + dx, y + dy] = (*metal, 255)
    if n == 2:
        p[14, 2] = (*RED, 255)
        p[10, 4] = (*BLUE, 255)
        p[12, 5] = (*metal_h, 255)
    return im


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    get = load_sprites(sys.argv[1])
    missing = set()
    for sc in SP.SCHOOLS:
        key = sc["key"]
        for sd in sc["spells"]:
            g = GLYPH.get(sd["id"], "paper")
            if get(g) is None:
                missing.add(g)
            path = os.path.join(OUT, "spell", sd["id"] + ".png")
            os.makedirs(os.path.dirname(path), exist_ok=True)
            spell_icon(sd, sc, get).save(path)
        for kind, fn in (("spell_book", lambda: book_icon(sc, get)), ("spell_scroll", lambda: scroll_icon(sc, get))):
            path = os.path.join(OUT, "item", kind, key + ".png")
            os.makedirs(os.path.dirname(path), exist_ok=True)
            fn().save(path)
        for n in (1, 2):
            path = os.path.join(OUT, "item", "staff", f"{key}_{n}.png")
            os.makedirs(os.path.dirname(path), exist_ok=True)
            staff_icon(sc, n, get).save(path)
    print("missing vanilla sprites (fell back to paper):", sorted(missing))


if __name__ == "__main__":
    main()
