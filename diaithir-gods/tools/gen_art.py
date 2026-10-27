#!/usr/bin/env python3
"""
Draws every texture of Diaithir Gods from scratch with Pillow.

No upstream (RPG Gods) texture, model or sound is used. All art is original:
slate-grey stone, oak wood, bronze and parchment, trimmed with a plaited
"braid" border that stands in for Celtic knotwork.

Usage: python3 tools/gen_art.py   (writes into src/main/resources/assets/diaithir_gods)
"""
import math
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "diaithir_gods")
TEX = os.path.join(ROOT, "textures")

# ---------------------------------------------------------------- palette
STONE = [(58, 63, 66), (78, 84, 87), (96, 103, 105), (118, 125, 125), (140, 146, 143)]
MOSS = (74, 96, 66)
OAK = [(70, 48, 30), (96, 68, 42), (122, 90, 56), (146, 110, 70)]
BRONZE = [(70, 46, 28), (112, 74, 38), (156, 108, 52), (196, 146, 76)]
PARCH = [(150, 126, 84), (186, 162, 116), (214, 196, 152), (232, 218, 176)]
INK = (52, 36, 28, 255)
GOLD = (206, 164, 62, 255)
EMBER = [(120, 28, 12), (196, 64, 18), (240, 128, 30), (255, 204, 80)]


def save(img, *path):
    full = os.path.join(TEX, *path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    img.save(full)


def rgba(c, a=255):
    return (c[0], c[1], c[2], a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + ((c[3],) if len(c) > 3 else (255,))


def noise_fill(img, box, palette, seed, weights=None):
    """Fills a box with speckled colour from the palette."""
    rnd = random.Random(seed)
    x0, y0, x1, y1 = box
    px = img.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            c = rnd.choices(palette, weights=weights)[0]
            px[x, y] = rgba(c)


def stone_block(size, seed, courses=2, moss=False):
    """A block of rough stone, cut into rows of bricks."""
    img = Image.new("RGBA", (size, size))
    rnd = random.Random(seed)
    noise_fill(img, (0, 0, size, size), STONE[1:4], seed, weights=[3, 4, 2])
    d = ImageDraw.Draw(img)
    row = size // courses
    for r in range(courses):
        y = r * row
        d.line([(0, y), (size, y)], fill=rgba(STONE[0]))
        off = (row // 2) if r % 2 else 0
        for x in range(off, size, size // 2):
            d.line([(x, y), (x, y + row - 1)], fill=rgba(STONE[0]))
        # light edge under the mortar
        d.line([(0, y + 1), (size, y + 1)], fill=rgba(STONE[3]))
    for _ in range(size // 2):
        x, y = rnd.randrange(size), rnd.randrange(size)
        img.putpixel((x, y), rgba(STONE[0]))
    if moss:
        for _ in range(size):
            x, y = rnd.randrange(size), rnd.randrange(size // 2, size)
            img.putpixel((x, y), rgba(MOSS))
    return img


def braid(draw, x0, y0, w, h, colour_a, colour_b, period=8):
    """Plaited band: two strands that cross over and under each other."""
    mid = y0 + h / 2.0
    amp = (h - 2) / 2.0
    for x in range(x0, x0 + w):
        t = (x - x0) / period * 2 * math.pi
        ya = mid + amp * math.sin(t)
        yb = mid - amp * math.sin(t)
        over_a = math.cos(t) > 0
        first, second = (colour_b, colour_a) if over_a else (colour_a, colour_b)
        yf, ys = (yb, ya) if over_a else (ya, yb)
        for yy, cc in ((ys, second), (yf, first)):
            iy = int(round(yy))
            draw.point((x, iy), fill=cc)
            draw.point((x, iy + 1), fill=cc)


def bevel_panel(img, box, fill, light, dark):
    d = ImageDraw.Draw(img)
    x0, y0, x1, y1 = box
    d.rectangle([x0, y0, x1, y1], fill=rgba(fill))
    d.line([(x0, y0), (x1, y0)], fill=rgba(light))
    d.line([(x0, y0), (x0, y1)], fill=rgba(light))
    d.line([(x0, y1), (x1, y1)], fill=rgba(dark))
    d.line([(x1, y0), (x1, y1)], fill=rgba(dark))


# ---------------------------------------------------------------- blocks
def make_blocks():
    save(stone_block(16, 11, courses=2), "block", "brazier_stone.png")
    # trim: stone with a braid band across the middle
    img = stone_block(16, 12, courses=1)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 5, 15, 10], fill=rgba(STONE[0]))
    braid(d, 0, 5, 16, 6, rgba(STONE[4]), rgba(STONE[2]), period=8)
    save(img, "block", "brazier_trim.png")
    # bronze bowl
    img = Image.new("RGBA", (16, 16))
    noise_fill(img, (0, 0, 16, 16), BRONZE[1:], 13, weights=[2, 5, 2])
    d = ImageDraw.Draw(img)
    for y in (3, 8, 13):
        d.line([(0, y), (15, y)], fill=rgba(BRONZE[0]))
    save(img, "block", "brazier_bronze.png")
    # ash
    img = Image.new("RGBA", (16, 16))
    noise_fill(img, (0, 0, 16, 16), [(54, 52, 50), (74, 72, 68), (96, 94, 88), (38, 36, 34)], 14, weights=[4, 4, 2, 2])
    save(img, "block", "ash.png")
    # embers: four animation frames stacked vertically
    frames = Image.new("RGBA", (16, 16 * 4))
    for f in range(4):
        fr = Image.new("RGBA", (16, 16))
        noise_fill(fr, (0, 0, 16, 16), [(46, 40, 36), (58, 50, 44)], 15 + f)
        rnd = random.Random(100 + f)
        for _ in range(60):
            x, y = rnd.randrange(16), rnd.randrange(16)
            fr.putpixel((x, y), rgba(rnd.choice(EMBER)))
        frames.paste(fr, (0, 16 * f))
    save(frames, "block", "embers.png")
    with open(os.path.join(TEX, "block", "embers.png.mcmeta"), "w") as f:
        f.write('{\n  "animation": {\n    "frametime": 6,\n    "interpolate": true\n  }\n}\n')


# ---------------------------------------------------------------- items
def make_items():
    # altar item: a standing stone with a small carved figure on a slab
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 13, 13, 14], fill=rgba(STONE[2]))
    d.line([(2, 13), (13, 13)], fill=rgba(STONE[4]))
    d.line([(2, 15), (13, 15)], fill=rgba(STONE[0]))
    d.rectangle([5, 6, 10, 12], fill=rgba(STONE[3]))
    d.rectangle([6, 2, 9, 5], fill=rgba(STONE[3]))
    d.line([(6, 2), (9, 2)], fill=rgba(STONE[4]))
    d.line([(10, 6), (10, 12)], fill=rgba(STONE[1]))
    d.point((7, 3), fill=rgba(STONE[0]))
    d.point((8, 3), fill=rgba(STONE[0]))
    braid(d, 5, 9, 6, 3, rgba(STONE[1]), rgba(STONE[4]), period=4)
    save(img, "item", "altar.png")

    def rod(d, x, y0, y1):
        d.line([(x, y0), (x, y1)], fill=rgba(OAK[2]))
        d.line([(x + 1, y0), (x + 1, y1)], fill=rgba(OAK[0]))
        d.point((x, y0 - 1), fill=rgba(OAK[3]))
        d.point((x, y1 + 1), fill=rgba(OAK[3]))

    # closed scroll: rolled parchment on two rods
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 3, 11, 12], fill=rgba(PARCH[2]))
    d.line([(4, 3), (4, 12)], fill=rgba(PARCH[3]))
    d.line([(11, 3), (11, 12)], fill=rgba(PARCH[0]))
    d.line([(5, 6), (10, 6)], fill=INK)
    d.line([(5, 8), (9, 8)], fill=INK)
    d.line([(5, 10), (10, 10)], fill=INK)
    rod(d, 3, 2, 13)
    rod(d, 11, 2, 13)
    save(img, "item", "scroll.png")

    # open scroll: wide sheet with a braid header
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 3, 14, 12], fill=rgba(PARCH[2]))
    d.line([(1, 3), (14, 3)], fill=rgba(PARCH[3]))
    d.line([(1, 12), (14, 12)], fill=rgba(PARCH[0]))
    braid(d, 2, 4, 12, 3, INK, rgba(BRONZE[2]), period=6)
    d.line([(3, 8), (12, 8)], fill=INK)
    d.line([(3, 10), (10, 10)], fill=INK)
    d.rectangle([0, 2, 1, 13], fill=rgba(OAK[2]))
    d.rectangle([14, 2, 15, 13], fill=rgba(OAK[2]))
    d.line([(1, 2), (1, 13)], fill=rgba(OAK[0]))
    d.line([(15, 2), (15, 13)], fill=rgba(OAK[0]))
    save(img, "item", "scroll_open.png")


# ---------------------------------------------------------------- statue skins
def skin(slim, seed):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rnd = random.Random(seed)

    def fill(box):
        noise_fill(img, box, STONE[1:5], rnd.randrange(10 ** 6), weights=[3, 4, 3, 1])

    arm_w = 3 if slim else 4
    # head 8x8x8 unwrap at (0,0), body at (16,16), arms and legs
    fill((0, 0, 32, 16))
    fill((16, 16, 40, 32))
    fill((40, 16, 40 + arm_w * 2 + 8, 32))
    fill((0, 16, 16, 32))
    fill((32, 48, 32 + arm_w * 2 + 8, 64))
    fill((16, 48, 32, 64))
    d = ImageDraw.Draw(img)
    # face (front of the head at 8..16, 8..16)
    d.rectangle([10, 11, 11, 12], fill=rgba(STONE[0]))
    d.rectangle([13, 11, 14, 12], fill=rgba(STONE[0]))
    d.line([(11, 14), (13, 14)], fill=rgba(STONE[0]))
    # hair band around the head
    braid(d, 8, 8, 8, 2, rgba(STONE[0]), rgba(STONE[4]), period=4)
    # belt on the body front (20..28, 20..32)
    braid(d, 20, 28, 8, 3, rgba(STONE[0]), rgba(STONE[4]), period=4)
    # clothing overlay layers: lightly weathered so they read as a carved robe
    for box in ((16, 32, 40, 48), (0, 32, 16, 48), (0, 48, 16, 64)):
        noise_fill(img, box, [STONE[2], STONE[3]], rnd.randrange(10 ** 6))
    return img


def make_skins():
    save(skin(False, 21), "altar", "statue.png")
    save(skin(True, 22), "altar", "statue_slim.png")


# ---------------------------------------------------------------- GUI
def parchment(img, box, seed):
    noise_fill(img, box, PARCH[1:4], seed, weights=[2, 6, 3])


def stone_fill(img, box, seed):
    noise_fill(img, box, STONE[0:4], seed, weights=[2, 4, 4, 1])


def slot(img, x, y):
    bevel_panel(img, (x, y, x + 17, y + 17), (60, 56, 50), (30, 28, 25), (150, 140, 120))
    d = ImageDraw.Draw(img)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=(74, 68, 60, 255))


def button_face(img, x, y, w, h, hover):
    base = STONE[3] if hover else STONE[2]
    bevel_panel(img, (x, y, x + w - 1, y + h - 1), base, STONE[4], STONE[0])


def favor_textures():
    # --- main frame: 256 x 170
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    stone_fill(img, (0, 0, 256, 168), 31)
    parchment(img, (24, 41, 232, 160), 32)
    d.rectangle([24, 41, 231, 160], outline=rgba(PARCH[0]))
    # header plaque (23,13 .. 231,41)
    stone_fill(img, (23, 13, 231, 41), 33)
    braid(d, 24, 36, 206, 4, rgba(STONE[0]), rgba(STONE[4]), period=10)
    parchment(img, (176, 15, 224, 33), 34)
    d.rectangle([176, 15, 223, 32], outline=rgba(STONE[0]))
    # top and side borders
    braid(d, 24, 3, 208, 6, rgba(STONE[0]), rgba(STONE[4]), period=12)
    for x0 in (2, 234):
        braid(d, x0, 40, 4, 1, rgba(STONE[0]), rgba(STONE[4]))
    d.rectangle([0, 0, 255, 167], outline=rgba(STONE[0]))
    d.rectangle([1, 1, 254, 166], outline=rgba(STONE[4], 140))
    d.line([(0, 167), (255, 167)], fill=rgba(STONE[0]))
    # side frames are 24px wide pillars with a bronze stud pattern
    for x0 in (0, 232):
        d.rectangle([x0 + 4, 8, x0 + 19, 160], outline=rgba(STONE[0]))
        for yy in range(16, 160, 20):
            d.ellipse([x0 + 9, yy, x0 + 14, yy + 5], fill=rgba(BRONZE[2]), outline=rgba(BRONZE[0]))

    # --- widgets sheet
    w = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(w)
    # tabs: 6 x (unselected rows 4..32, selected rows 32..64)
    for i in range(6):
        x = i * 28
        bevel_panel(w, (x + 1, 4, x + 26, 31), STONE[1], STONE[3], STONE[0])
        bevel_panel(w, (x + 1, 32, x + 26, 63), PARCH[2], PARCH[3], PARCH[0])
        d.line([(x + 1, 63), (x + 26, 63)], fill=rgba(PARCH[2]))
    # page tabs: 4 x 42 wide, unselected 64..92, selected 96..128
    for i in range(4):
        x = i * 42
        bevel_panel(w, (x + 1, 64, x + 40, 91), STONE[1], STONE[3], STONE[0])
        bevel_panel(w, (x + 1, 96, x + 40, 127), PARCH[2], PARCH[3], PARCH[0])
    # tab arrows 12x17: right (0,130) left (12,130), hover +17
    for hover in (0, 1):
        for i in range(2):
            x, y = i * 12, 130 + hover * 17
            bevel_panel(w, (x, y, x + 11, y + 16), STONE[3] if hover else STONE[2], STONE[4], STONE[0])
            tri = [(x + 8, y + 3), (x + 8, y + 13), (x + 3, y + 8)] if i == 1 else [(x + 3, y + 3), (x + 3, y + 13), (x + 8, y + 8)]
            d.polygon(tri, fill=rgba(PARCH[3]))
    # perk tooltip scroll 40 x 48 at (31,132)
    px, py = 31, 132
    parchment(w, (px, py, px + 40, py + 48), 41)
    d.rectangle([px, py, px + 39, py + 47], outline=rgba(PARCH[0]))
    d.rectangle([px + 1, py + 1, px + 38, py + 46], outline=rgba(PARCH[3], 160))
    for cx, cy in ((px + 3, py + 3), (px + 36, py + 3), (px + 3, py + 44), (px + 36, py + 44)):
        d.ellipse([cx - 1, cy - 1, cx + 1, cy + 1], fill=rgba(BRONZE[2]))
    # offering arrow (113,130, 12x9)
    for i in range(12):
        d.point((113 + i, 134), fill=rgba(INK))
    d.polygon([(121, 130), (121, 138), (125, 134)], fill=rgba(INK))
    # page icons 18x18 at (125 + 18i, 130)
    def icon_frame(i):
        x = 125 + 18 * i
        d.ellipse([x + 1, 131, x + 16, 146], fill=rgba(STONE[2]), outline=rgba(STONE[0]))
        return x
    x = icon_frame(0)  # summary: a face
    d.ellipse([x + 5, 134, x + 12, 142], fill=rgba(PARCH[2]))
    x = icon_frame(1)  # offerings: bowl
    d.arc([x + 4, 134, x + 13, 143], 0, 180, fill=rgba(PARCH[3]))
    d.line([(x + 4, 138), (x + 13, 138)], fill=rgba(PARCH[3]))
    x = icon_frame(2)  # sacrifices: blade
    d.line([(x + 5, 143), (x + 12, 134)], fill=rgba(PARCH[3]), width=2)
    x = icon_frame(3)  # perks: star
    d.polygon([(x + 9, 133), (x + 11, 138), (x + 15, 138), (x + 12, 141), (x + 13, 145), (x + 9, 143), (x + 5, 145), (x + 6, 141), (x + 3, 138), (x + 7, 138)], fill=rgba(GOLD))
    # scrollbar handles 12x15 (0,165) enabled, (0,180) disabled
    bevel_panel(w, (0, 165, 11, 179), STONE[3], STONE[4], STONE[0])
    d.line([(3, 171), (8, 171)], fill=rgba(STONE[0]))
    d.line([(3, 173), (8, 173)], fill=rgba(STONE[0]))
    bevel_panel(w, (0, 180, 11, 194), STONE[1], STONE[2], STONE[0])
    # perk frames: tint layer (0,196), bg (22,196), cooldown overlay (44,196); fancy +22
    for fancy in (0, 1):
        y = 196 + 22 * fancy
        d.rectangle([0, y, 21, y + 21], fill=(255, 255, 255, 255) if not fancy else (255, 255, 255, 255))
        d.rectangle([2, y + 2, 19, y + 19], fill=(0, 0, 0, 0))
        # background plate (drawn over the tint)
        d.rectangle([22, y, 43, y + 21], fill=(0, 0, 0, 0))
        d.rectangle([24, y + 2, 41, y + 19], fill=rgba(STONE[1]))
        d.rectangle([24, y + 2, 41, y + 19], outline=rgba(STONE[0]))
        d.rectangle([25, y + 3, 40, y + 18], outline=rgba(STONE[4], 150))
        if fancy:
            for cx, cy in ((24, y + 2), (41, y + 2), (24, y + 19), (41, y + 19)):
                d.point((cx, cy), fill=GOLD)
            braid(d, 25, y, 16, 2, rgba(GOLD), rgba(BRONZE[1]), period=4)
        d.rectangle([44, y, 65, y + 21], fill=(20, 18, 16, 200))
    # tint layer must show colour at the edges: a ring around the plate
    for fancy in (0, 1):
        y = 196 + 22 * fancy
        d.rectangle([0, y, 21, y + 21], outline=(255, 255, 255, 255))
        d.rectangle([1, y + 1, 20, y + 20], outline=(255, 255, 255, 255))
    # favour-level boundary pieces (0,240) stretch, (8,240) edge
    d.rectangle([0, 240, 7, 247], fill=(110, 60, 140, 70))
    d.rectangle([8, 240, 15, 247], fill=(110, 60, 140, 70))
    d.line([(8, 240), (8, 247)], fill=(110, 60, 140, 220))
    # strikethrough (0,250, 18x2)
    d.rectangle([0, 250, 17, 251], fill=(150, 30, 24, 255))
    # scroll track (188,0,14x120)
    bevel_panel(w, (188, 0, 201, 119), PARCH[1], PARCH[0], PARCH[3])
    d.line([(194, 4), (194, 115)], fill=rgba(PARCH[0]))
    # preview pane (202,0,54x80)
    bevel_panel(w, (202, 0, 255, 79), STONE[0], STONE[1], STONE[0])
    d.rectangle([204, 2, 253, 77], fill=rgba(STONE[1]))
    braid(d, 204, 72, 50, 5, rgba(STONE[0]), rgba(STONE[3]), period=10)
    save(w, "gui", "favor", "favor_widgets.png")
    # favor.png final write
    save(img, "gui", "favor", "favor.png")


def altar_textures():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # main panel 224 x 202
    stone_fill(img, (0, 0, 224, 202), 51)
    d.rectangle([0, 0, 223, 201], outline=rgba(STONE[0]))
    d.rectangle([1, 1, 222, 200], outline=rgba(STONE[4], 140))
    braid(d, 6, 194, 212, 5, rgba(STONE[0]), rgba(STONE[4]), period=12)
    # player inventory grid: 3 rows at (31,119), hotbar at (31,177)
    for r in range(3):
        for c in range(9):
            slot(img, 31 + c * 18, 119 + r * 18)
    for c in range(9):
        slot(img, 31 + c * 18, 177)
    # equipment slots (inventory-style) at the item tab positions
    for (sx, sy) in ((61, 7), (61, 25), (61, 43), (61, 61), (81, 7), (81, 25), (81, 61)):
        slot(img, sx - 1, sy - 1)
    # icons 16x16: reset (0,202) preset (16,202) slab (48,202); gender (0,218)/(16,218); slim (32,218)/(48,218)
    def glyph(x, y, kind):
        g = ImageDraw.Draw(img)
        g.rectangle([x, y, x + 15, y + 15], fill=(0, 0, 0, 0))
        if kind == "reset":
            g.arc([x + 3, y + 3, x + 12, y + 12], 30, 330, fill=rgba(PARCH[3]), width=2)
            g.polygon([(x + 10, y + 1), (x + 14, y + 5), (x + 9, y + 6)], fill=rgba(PARCH[3]))
        elif kind == "preset":
            for i in range(3):
                g.line([(x + 3, y + 4 + 4 * i), (x + 12, y + 4 + 4 * i)], fill=rgba(PARCH[3]), width=2)
        elif kind == "slab":
            g.rectangle([x + 2, y + 9, x + 13, y + 13], fill=rgba(STONE[3]), outline=rgba(STONE[0]))
        elif kind == "female":
            g.ellipse([x + 4, y + 2, x + 11, y + 9], outline=rgba(GOLD), width=2)
            g.line([(x + 8, y + 9), (x + 8, y + 14)], fill=rgba(GOLD), width=2)
            g.line([(x + 5, y + 12), (x + 11, y + 12)], fill=rgba(GOLD), width=2)
        elif kind == "male":
            g.ellipse([x + 3, y + 5, x + 10, y + 12], outline=rgba(GOLD), width=2)
            g.line([(x + 9, y + 6), (x + 13, y + 2)], fill=rgba(GOLD), width=2)
            g.line([(x + 9, y + 2), (x + 13, y + 2)], fill=rgba(GOLD), width=2)
            g.line([(x + 13, y + 2), (x + 13, y + 6)], fill=rgba(GOLD), width=2)
        elif kind == "slim":
            g.rectangle([x + 6, y + 2, x + 9, y + 14], fill=rgba(PARCH[3]))
        elif kind == "wide":
            g.rectangle([x + 4, y + 2, x + 11, y + 14], fill=rgba(PARCH[3]))
    glyph(0, 202, "reset")
    glyph(16, 202, "preset")
    glyph(48, 202, "slab")
    glyph(0, 218, "female")
    glyph(16, 218, "male")
    glyph(32, 218, "slim")
    glyph(48, 218, "wide")
    save(img, "gui", "altar", "altar.png")

    w = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(w)
    # tabs: unselected rows 0..30 (drawn from v=0), selected v=32..63
    for i in range(2):
        x = i * 28
        bevel_panel(w, (x + 1, 2, x + 26, 29), STONE[1], STONE[3], STONE[0])
        bevel_panel(w, (x + 1, 32, x + 26, 63), STONE[2], STONE[4], STONE[0])
    # part buttons 70x14 at (25,130) normal, (25,144) hover
    for hover in (0, 1):
        button_face(w, 25, 130 + 14 * hover, 70, 14, hover)
    # icon buttons 16x16 at (97,130) normal and (97,146) hover
    for hover in (0, 1):
        button_face(w, 97, 130 + 16 * hover, 16, 16, hover)
    # preview pane 54x80 at (168,130)
    bevel_panel(w, (168, 130, 221, 209), STONE[0], STONE[1], STONE[0])
    d.rectangle([170, 132, 219, 207], fill=rgba(STONE[1]))
    braid(d, 170, 202, 50, 5, rgba(STONE[0]), rgba(STONE[3]), period=10)
    save(w, "gui", "altar", "altar_widgets.png")


def main():
    for sub in ("block", "item", "altar", "gui/favor", "gui/altar"):
        os.makedirs(os.path.join(TEX, *sub.split("/")), exist_ok=True)
    make_blocks()
    make_items()
    make_skins()
    favor_textures()
    altar_textures()
    print("textures written to", os.path.abspath(TEX))


if __name__ == "__main__":
    main()
