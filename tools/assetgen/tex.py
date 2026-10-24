"""Procedural texture primitives (Industrial Factory style palette). All art is generated here, nothing is copied."""
import random, math, hashlib
from PIL import Image
import numpy as np

def rng(name):
    return random.Random(int(hashlib.md5(name.encode()).hexdigest()[:8], 16))

def clamp(v): return max(0, min(255, int(v)))
def shade(c, f): return tuple(clamp(x * f) for x in c[:3])
def mix(a, b, t): return tuple(clamp(a[i] * (1 - t) + b[i] * t) for i in range(3))

PAL = dict(
    steel=(92, 94, 96), dark_steel=(60, 62, 66), paint_yellow=(196, 152, 40), copper=(196, 104, 56),
    brass=(190, 150, 70), andesite=(128, 130, 126), zinc=(150, 160, 168), iron=(176, 176, 178),
    wood=(150, 112, 66), rubber=(40, 40, 44), gold=(232, 196, 60), glass=(180, 210, 220),
)
DYES = dict(white=(236, 236, 236), orange=(240, 130, 30), magenta=(190, 70, 180), light_blue=(80, 160, 224),
            yellow=(230, 200, 40), lime=(110, 190, 30), pink=(238, 140, 170), gray=(80, 86, 90),
            light_gray=(158, 158, 152), cyan=(30, 140, 150), purple=(120, 50, 170), blue=(50, 60, 160),
            brown=(110, 70, 40), green=(80, 110, 40), red=(170, 40, 40), black=(24, 24, 28))
STONES = dict(granite=(150, 100, 86), diorite=(190, 190, 190), andesite=(136, 138, 134), calcite=(224, 224, 218),
              dripstone=(134, 106, 86), deepslate=(78, 78, 84), tuff=(108, 110, 98), asurine=(70, 100, 170),
              crimsite=(150, 52, 56), limestone=(206, 192, 150), ochrum=(214, 168, 56), scoria=(96, 60, 52),
              scorchia=(66, 56, 54), veridium=(70, 150, 120))

def img(size=16, fill=(0, 0, 0, 0)):
    return Image.new("RGBA", (size, size), fill)

def px(im, x, y, c, a=255):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), tuple(c[:3]) + (a,))

def speckle(base, name, var=0.10, size=16):
    r = rng(name)
    im = img(size)
    for y in range(size):
        for x in range(size):
            f = 1 + (r.random() - 0.5) * 2 * var
            px(im, x, y, shade(base, f))
    return im

def bevel(im, light=1.18, dark=0.8, w=1):
    s = im.width
    for i in range(w):
        for k in range(s - i):
            for (x, y, f) in ((k, i, light), (i, k, light), (s - 1 - k, s - 1 - i, dark), (s - 1 - i, s - 1 - k, dark)):
                if 0 <= x < s and 0 <= y < s:
                    p = im.getpixel((x, y))
                    px(im, x, y, shade(p, f), p[3])
    return im

def rivets(im, c=None, inset=2):
    s = im.width
    for (x, y) in ((inset, inset), (s - 1 - inset, inset), (inset, s - 1 - inset), (s - 1 - inset, s - 1 - inset)):
        p = im.getpixel((x, y))
        px(im, x, y, c or shade(p, 1.35), p[3])
        px(im, x + 1, y + 1, shade(p, 0.7), p[3])
    return im

def grime(im, name, amount=0.06):
    r = rng(name + "g")
    for _ in range(int(im.width * im.height * amount)):
        x, y = r.randrange(im.width), r.randrange(im.height)
        p = im.getpixel((x, y))
        if p[3]:
            px(im, x, y, shade(p, 0.75), p[3])
    return im

def plate(color, name, rivet=True):
    im = speckle(color, name, 0.05)
    bevel(im, 1.22, 0.78)
    if rivet: rivets(im)
    return grime(im, name)

def panel_lines(im, horizontal=True, vertical=False, at=(8,), f=0.78):
    for a in at:
        for k in range(im.width):
            if horizontal: p = im.getpixel((k, a)); px(im, k, a, shade(p, f), p[3])
            if vertical: p = im.getpixel((a, k)); px(im, a, k, shade(p, f), p[3])
    return im

def stone(color, name, kind):
    s = 16
    im = speckle(color, name + kind, 0.07 if kind != "polished" else 0.025)
    if kind == "cut":
        bevel(im, 1.1, 0.88)
    elif kind == "polished":
        bevel(im, 1.2, 0.8)
    elif kind in ("bricks", "small"):
        rows = 2 if kind == "bricks" else 4
        h = s // rows
        mort = shade(color, 0.62)
        for rrow in range(rows):
            y = rrow * h
            for x in range(s): px(im, x, y, mort)
            off = (s // (4 if kind == "bricks" else 2)) * (rrow % 2)
            bw = s // 2 if kind == "bricks" else s // 2
            for bx in range(off, s + bw, bw):
                for yy in range(y, y + h): px(im, bx % s, yy, mort)
        for x in range(s): px(im, x, s - 1, shade(color, 0.62))
    elif kind == "layered":
        for y in range(0, s, 4):
            for x in range(s):
                px(im, x, y, shade(color, 0.7)); px(im, x, y + 1, shade(color, 1.12))
    elif kind == "pillar_side":
        for x in (0, 1, 7, 8, 14, 15):
            for y in range(s):
                p = im.getpixel((x, y)); px(im, x, y, shade(p, 0.86 if x in (0, 7, 14) else 1.1))
    elif kind == "pillar_top":
        im = speckle(color, name + "t", 0.04)
        for i in range(2, 14):
            for (x, y) in ((i, 2), (i, 13), (2, i), (13, i)):
                px(im, x, y, shade(color, 0.7))
        bevel(im, 1.15, 0.85)
    return im

def ore(base, name, fleck=(200, 200, 210)):
    im = speckle(base, name, 0.07)
    r = rng(name + "ore")
    for _ in range(7):
        x, y = r.randrange(1, 14), r.randrange(1, 14)
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            px(im, x + dx, y + dy, fleck)
    return im

def casing(color, name, trim=PAL["steel"]):
    im = speckle(color, name, 0.05)
    for i in range(16):
        for t in (0, 1, 14, 15):
            px(im, i, t, shade(trim, 1.0 if t in (0, 15) else 1.18)); px(im, t, i, shade(trim, 1.0 if t in (0, 15) else 1.18))
    bevel(im, 1.2, 0.8)
    rivets(im, shade(color, 1.4), 3)
    return grime(im, name)

def glass(frame, name, pattern="frame"):
    im = img(16)
    for i in range(16):
        for k in (0, 15):
            px(im, i, k, frame); px(im, k, i, frame)
    for i in range(1, 15):
        for k in (1, 14):
            px(im, i, k, shade(frame, 1.2)); px(im, k, i, shade(frame, 0.85))
    for y in range(2, 14):
        for x in range(2, 14):
            px(im, x, y, PAL["glass"], 60)
    for k in range(3, 8):
        px(im, k, 12 - k + 3, (255, 255, 255), 150)
    if pattern == "tiled":
        for i in range(2, 14): px(im, 8, i, frame); px(im, i, 8, frame)
    if pattern == "h":
        for i in range(2, 14): px(im, i, 8, frame)
    if pattern == "v":
        for i in range(2, 14): px(im, 8, i, frame)
    return im

def weave(color, name):
    im = speckle(color, name, 0.04)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0: px(im, x, y, shade(color, 0.86))
            if (x - y) % 8 == 0: px(im, x, y, shade(color, 1.1))
    return bevel(im, 1.08, 0.9)

def shingles(color, name):
    im = speckle(color, name, 0.05)
    for row in range(4):
        y = row * 4
        for x in range(16):
            px(im, x, y + 3, shade(color, 0.62)); px(im, x, y, shade(color, 1.18))
        off = 4 * (row % 2)
        for bx in range(off, 16 + 4, 8):
            for yy in range(y, y + 4): px(im, bx % 16, yy, shade(color, 0.7))
    return im

def tiles(color, name):
    im = speckle(color, name, 0.05)
    for i in range(16):
        for k in (0, 7, 8, 15):
            px(im, i, k, shade(color, 0.66)); px(im, k, i, shade(color, 0.66))
    for q in ((1, 1), (9, 1), (1, 9), (9, 9)):
        for i in range(6):
            px(im, q[0] + i, q[1], shade(color, 1.2)); px(im, q[0], q[1] + i, shade(color, 1.2))
    return im

def tint_oxid(level):
    return [(196, 104, 56), (184, 120, 84), (96, 150, 120), (70, 160, 150)][level]

def glyph(im, name, color, x0=5, y0=5, n=6):
    """Deterministic 6x6 mirrored mark so each machine block is visually distinguishable."""
    r = rng(name + "glyph")
    for y in range(n):
        for x in range((n + 1) // 2):
            if r.random() < 0.5:
                px(im, x0 + x, y0 + y, color); px(im, x0 + n - 1 - x, y0 + y, color)
    return im

def machine(color, name, accent=None, trim=None):
    im = casing(color, name, trim or shade(color, 0.7))
    glyph(im, name, accent or shade(color, 1.5))
    return im

def pipe_tex(color, name):
    im = speckle(color, name, 0.05)
    for y in range(16):
        for x in range(16):
            f = 1 + 0.25 * math.cos((y - 7.5) / 7.5 * 1.4)
            p = im.getpixel((x, y)); px(im, x, y, shade(p, f))
    for x in (0, 1, 14, 15):
        for y in range(16): px(im, x, y, shade(color, 0.7))
    return im

def gear_tex(color, name):
    im = speckle(color, name, 0.05)
    for y in range(16):
        for x in range(16):
            if (x // 2 + y // 2) % 2 == 0:
                p = im.getpixel((x, y)); px(im, x, y, shade(p, 0.88))
    return bevel(im, 1.2, 0.8)

def save(im, path):
    import os
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)
