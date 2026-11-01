"""Generates the 16x16 textures of the converter and reservoir blocks (original art, MIT). Run from the module root: python3 tools/gen_textures.py"""
from PIL import Image
import os

OUT = "src/main/resources/assets/diaithirbridges/textures/block"
STEEL = (74, 80, 86); DARK = (44, 48, 53); LIGHT = (122, 130, 138); RIVET = (160, 166, 172)
YEL = (222, 168, 34); BLK = (28, 28, 30)


def base():
    im = Image.new("RGB", (16, 16), STEEL)
    px = im.load()
    for i in range(16):
        for e in (0, 15):
            px[i, e] = DARK; px[e, i] = DARK
        px[i, 1] = LIGHT if i not in (0, 15) else DARK
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[x, y] = RIVET
    return im


def hazard(im, y0, y1):
    px = im.load()
    for y in range(y0, y1):
        for x in range(2, 14):
            px[x, y] = YEL if ((x + y) // 2) % 2 == 0 else BLK


def front(kind, rev):
    im = base(); px = im.load()
    hazard(im, 3, 5); hazard(im, 11, 13)
    col = (60, 180, 90) if kind == "eu" else (80, 150, 220)
    col2 = (230, 110, 40)
    a, b = (col2, col) if rev else (col, col2)   # left lamp = input colour, right lamp = output colour
    for dx in range(3):
        for dy in range(3):
            px[3 + dx, 6 + dy] = a
            px[10 + dx, 6 + dy] = b
    for x in range(6, 10):            # arrow from input to output
        px[x, 7] = LIGHT
    px[9, 6] = LIGHT; px[9, 8] = LIGHT
    return im


def side(kind):
    im = base(); px = im.load()
    for x in range(3, 13):
        px[x, 7] = DARK; px[x, 8] = DARK
    for x in range(3, 13, 3):
        px[x, 9] = LIGHT
    return im


def top():
    im = base(); px = im.load()
    for x in range(4, 12):
        for y in range(4, 12):
            px[x, y] = DARK if (x in (4, 11) or y in (4, 11)) else (58, 63, 68)
    return im


def tank(fill):
    im = base(); px = im.load()
    for x in range(4, 12):
        for y in range(3, 14):
            px[x, y] = BLK if (x in (4, 11) or y in (3, 13)) else (36, 40, 44)
    if fill:
        for x in range(5, 11):
            for y in range(7, 13):
                px[x, y] = (232, 120, 40) if y > 8 else (250, 190, 70)
    return im


os.makedirs(OUT, exist_ok=True)
for kind, name in (("eu", "claochladairdealain"), ("air", "claochladairanalach")):
    front(kind, False).save(f"{OUT}/{name}_front_a.png")
    front(kind, True).save(f"{OUT}/{name}_front_b.png")
    side(kind).save(f"{OUT}/{name}_side.png")
    top().save(f"{OUT}/{name}_top.png")
tank(True).save(f"{OUT}/amarlionna_side.png")
top().save(f"{OUT}/amarlionna_top.png")
