#!/usr/bin/env python3
"""Draws every procedural texture of the Macaw-style set: door/trapdoor overlays, window glass, rope, roof shingles
and paving patterns. Deterministic (seeded), 16 px, own art, MIT. Output is committed under
src/main/resources/assets/diaithirwoodvariants/textures/block. Run: python3 -I tools/gen_textures.py"""
import os, random
from PIL import Image

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(HERE, "src/main/resources/assets/diaithirwoodvariants/textures/block")
os.makedirs(OUT, exist_ok=True)

ROOFS = {  # colour -> (base, dark, light)
    "white": (222, 222, 218), "orange": (214, 118, 42), "magenta": (178, 70, 168), "light_blue": (92, 150, 208),
    "yellow": (216, 184, 58), "lime": (120, 178, 52), "pink": (216, 130, 160), "gray": (84, 88, 92),
    "light_gray": (150, 152, 152), "cyan": (40, 130, 144), "purple": (112, 60, 160), "blue": (52, 70, 160),
    "brown": (110, 74, 44), "green": (72, 112, 48), "red": (160, 54, 46), "black": (36, 36, 40)}
PALETTES = {"stone": (132, 132, 130), "sand": (208, 190, 140), "brick": (150, 70, 56), "slate": (70, 78, 90),
            "moss": (92, 112, 76), "clay": (186, 108, 72)}
PATTERNS = ["flagstone", "cobble", "brick", "herringbone", "hexagon", "basket", "stack", "round"]


def clamp(v):
    return max(0, min(255, int(v)))


def shade(c, f):
    return tuple(clamp(x * f) for x in c)


def save(img, name):
    img.save(os.path.join(OUT, name + ".png"))


def rgba(w=16, h=16):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


BRACE = (38, 26, 16, 235)
GLASS = (176, 214, 238, 120)
FRAME = (60, 42, 26, 255)


def line(img, x0, y0, x1, y1, col, thick=1):
    n = max(abs(x1 - x0), abs(y1 - y0)) or 1
    for i in range(n + 1):
        x = round(x0 + (x1 - x0) * i / n)
        y = round(y0 + (y1 - y0) * i / n)
        for dx in range(thick):
            for dy in range(thick):
                if 0 <= x + dx < img.width and 0 <= y + dy < img.height:
                    img.putpixel((x + dx, y + dy), col)


def rect(img, x0, y0, x1, y1, col, fill=False):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            if fill or x in (x0, x1) or y in (y0, y1):
                if 0 <= x < img.width and 0 <= y < img.height:
                    img.putpixel((x, y), col)


def doors():
    # barn: frame and a big X, bottom and top halves line up into one door
    for half in ("top", "bottom"):
        im = rgba(); rect(im, 0, 0, 15, 15, BRACE)
        if half == "top":
            rect(im, 0, 0, 15, 15, (0, 0, 0, 0)); rect(im, 1, 1, 14, 15, BRACE); line(im, 1, 1, 14, 15, BRACE, 2)
        else:
            rect(im, 0, 0, 15, 15, (0, 0, 0, 0)); rect(im, 1, 0, 14, 14, BRACE); line(im, 1, 0, 14, 14, BRACE, 2)
        save(im, f"door_barn_{half}")
    # stable: horizontal split with dark seam and hinges; top half has a small opening frame
    im = rgba(); rect(im, 1, 3, 14, 12, FRAME); rect(im, 3, 5, 12, 10, (0, 0, 0, 0), True)
    for y in range(5, 11):
        for x in range(3, 13):
            im.putpixel((x, y), (20, 14, 8, 200))
    line(im, 0, 15, 15, 15, BRACE, 1); save(im, "door_stable_top")
    im = rgba(); line(im, 0, 0, 15, 0, BRACE, 1); rect(im, 1, 3, 14, 13, BRACE); rect(im, 2, 4, 13, 12, (0, 0, 0, 0), True)
    save(im, "door_stable_bottom")
    # cottage: two raised panels (bottom), glazed panel with cross (top)
    im = rgba(); rect(im, 2, 2, 13, 13, FRAME)
    for x in range(3, 13):
        for y in range(3, 13):
            im.putpixel((x, y), GLASS)
    line(im, 8, 2, 8, 13, FRAME); line(im, 2, 7, 13, 7, FRAME); save(im, "door_cottage_top")
    im = rgba(); rect(im, 2, 2, 13, 7, BRACE); rect(im, 2, 9, 13, 13, BRACE); save(im, "door_cottage_bottom")
    # glass: wide glazed panel top, plain bottom rail
    im = rgba(); rect(im, 1, 1, 14, 15, FRAME)
    for x in range(2, 14):
        for y in range(2, 16):
            im.putpixel((x, y), GLASS)
    save(im, "door_glass_top")
    im = rgba(); rect(im, 1, 0, 14, 14, FRAME)
    for x in range(2, 14):
        for y in range(1, 8):
            im.putpixel((x, y), GLASS)
    line(im, 1, 8, 14, 8, FRAME); save(im, "door_glass_bottom")
    # shoji: paper lattice over the whole door
    for half in ("top", "bottom"):
        im = rgba()
        for x in range(1, 15):
            for y in range(0, 16):
                im.putpixel((x, y), (240, 232, 208, 215))
        for x in (1, 5, 9, 14):
            line(im, x, 0, x, 15, FRAME)
        for y in (0, 5, 10, 15):
            line(im, 1, y, 14, y, FRAME)
        save(im, f"door_shoji_{half}")


def trapdoors():
    im = rgba()  # beach: slats with gaps
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                im.putpixel((x, y), BRACE)
    save(im, "trap_beach")
    im = rgba(); rect(im, 0, 0, 15, 15, BRACE)
    for x in range(3, 15, 4):
        line(im, x, 1, x, 14, (25, 20, 18, 255), 1)
    save(im, "trap_barred")
    im = rgba(); rect(im, 1, 1, 14, 14, FRAME)
    for x in range(2, 14):
        for y in range(2, 14):
            im.putpixel((x, y), GLASS)
    line(im, 8, 1, 8, 14, FRAME); save(im, "trap_glass")
    im = rgba(); rect(im, 0, 0, 15, 15, BRACE); rect(im, 4, 4, 11, 11, (0, 0, 0, 0), True); rect(im, 4, 4, 11, 11, BRACE)
    line(im, 4, 4, 11, 11, BRACE); line(im, 11, 4, 4, 11, BRACE); save(im, "trap_cottage")


def windows():
    im = rgba()
    r = random.Random("glass")
    for x in range(16):
        for y in range(16):
            a = 105 + r.randint(-8, 8)
            im.putpixel((x, y), (178, 216, 238, a))
    for i in range(3, 13):  # glint
        im.putpixel((i, 15 - i), (255, 255, 255, 190))
    save(im, "window_glass")
    im = rgba()
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), (255, 255, 255, 255) if x in (7, 8) else (0, 0, 0, 0))
    save(im, "window_edge")


def rope():
    r = random.Random("rope")
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            twist = ((x + y) // 2) % 2
            base = (168, 138, 88) if twist else (140, 110, 66)
            im.putpixel((x, y), tuple(clamp(c + r.randint(-6, 6)) for c in base) + (255,))
    save(im, "rope")


def roofs():
    for name, base in ROOFS.items():
        r = random.Random("roof" + name)
        im = Image.new("RGBA", (16, 16))
        for y in range(16):
            row = y // 4
            off = 4 if row % 2 else 0
            for x in range(16):
                xx = (x + off) % 8
                f = 1.0 - 0.12 * (3 - (y % 4)) / 3
                if y % 4 == 3:
                    f *= 0.62
                elif xx == 0 or xx == 7:
                    f *= 0.82
                f *= 1 + r.uniform(-0.05, 0.05)
                im.putpixel((x, y), shade(base, f) + (255,))
        save(im, f"roof_{name}")


def pave(pattern, base, seed):
    r = random.Random(seed)
    im = Image.new("RGBA", (16, 16))
    grout = shade(base, 0.55)
    cells = [[-1] * 16 for _ in range(16)]
    cid = 0

    def fill(x0, y0, x1, y1):
        nonlocal cid
        for x in range(x0, x1):
            for y in range(y0, y1):
                cells[x % 16][y % 16] = cid
        cid += 1

    if pattern == "flagstone":
        for (x0, y0, x1, y1) in [(0, 0, 9, 6), (9, 0, 16, 9), (0, 6, 5, 16), (5, 6, 9, 16), (9, 9, 16, 16)]:
            fill(x0, y0, x1, y1)
    elif pattern == "cobble":
        for y0 in range(0, 16, 5):
            for x0 in range(0, 16, 5):
                fill(x0 + r.randint(0, 1), y0 + r.randint(0, 1), x0 + 5, y0 + 5)
    elif pattern == "brick":
        for row in range(4):
            off = 4 if row % 2 else 0
            for k in (-1, 0, 1):
                fill(off + k * 8, row * 4, off + k * 8 + 8, row * 4 + 4)
    elif pattern == "herringbone":
        for i in range(0, 16, 4):
            for j in range(0, 16, 8):
                fill(i, j + (i // 4 % 2) * 4, i + 4, j + (i // 4 % 2) * 4 + 4)
                fill(i, j + (1 - i // 4 % 2) * 4, i + 4, j + (1 - i // 4 % 2) * 4 + 4)
    elif pattern == "hexagon":
        for row in range(4):
            for k in range(-1, 3):
                x0 = k * 8 + (4 if row % 2 else 0)
                fill(x0, row * 4, x0 + 8, row * 4 + 4)
    elif pattern == "basket":
        for bx in range(0, 16, 8):
            for by in range(0, 16, 8):
                if (bx + by) // 8 % 2:
                    for k in range(0, 8, 4):
                        fill(bx + k, by, bx + k + 4, by + 8)
                else:
                    for k in range(0, 8, 4):
                        fill(bx, by + k, bx + 8, by + k + 4)
    elif pattern == "stack":
        for row in range(4):
            for k in range(0, 16, 8):
                fill(k, row * 4, k + 8, row * 4 + 4)
    else:  # round
        for cy in (4, 12):
            for cx in (4, 12):
                for x in range(16):
                    for y in range(16):
                        if (x - cx) ** 2 + (y - cy) ** 2 <= 14 and cells[x][y] < 0:
                            cells[x][y] = cid
                cid += 1
    tone = {}
    for x in range(16):
        for y in range(16):
            c = cells[x][y]
            if c < 0:
                col = grout
            else:
                tone.setdefault(c, r.uniform(0.86, 1.12))
                col = shade(base, tone[c] * (1 + r.uniform(-0.04, 0.04)))
                # darker edge on the cell's lower right
                if cells[(x + 1) % 16][y] != c or cells[x][(y + 1) % 16] != c:
                    col = shade(col, 0.82)
            im.putpixel((x, y), col + (255,))
    return im


def paving():
    for pal, base in PALETTES.items():
        for pat in PATTERNS:
            save(pave(pat, base, pal + pat), f"paving_{pat}_{pal}")


def decor():
    r = random.Random("decor")
    im = rgba()  # cupboard front: two doors with knobs
    rect(im, 1, 1, 14, 14, FRAME); line(im, 8, 1, 8, 14, FRAME)
    for x in range(2, 14):
        for y in range(2, 14):
            if x != 8:
                im.putpixel((x, y), (30, 20, 12, 70))
    im.putpixel((7, 8), (212, 176, 80, 255)); im.putpixel((9, 8), (212, 176, 80, 255))
    save(im, "decor_cupboard_front")
    im = rgba()  # bookcase front: three shelves of books
    cols = [(150, 40, 40), (40, 90, 150), (60, 130, 70), (190, 150, 50), (110, 60, 130)]
    for shelf in range(3):
        y0 = 1 + shelf * 5
        x = 1
        while x < 15:
            w = r.randint(1, 2)
            c = r.choice(cols)
            for xx in range(x, min(x + w, 15)):
                for yy in range(y0, y0 + 4):
                    im.putpixel((xx, yy), c + (255,))
            x += w
        line(im, 0, y0 + 4, 15, y0 + 4, FRAME)
    save(im, "decor_bookcase_front")
    im = rgba()  # counter drawer front
    rect(im, 1, 2, 14, 7, FRAME); rect(im, 1, 9, 14, 14, FRAME)
    im.putpixel((7, 4), (212, 176, 80, 255)); im.putpixel((8, 4), (212, 176, 80, 255))
    im.putpixel((7, 11), (212, 176, 80, 255)); im.putpixel((8, 11), (212, 176, 80, 255))
    save(im, "decor_counter_front")


if __name__ == "__main__":
    doors(); trapdoors(); windows(); rope(); roofs(); paving(); decor()
    print(len(os.listdir(OUT)), "textures")
