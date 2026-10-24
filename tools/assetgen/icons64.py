"""64x item icons, soft lit from the upper left with a dark outline, same engine as the block art."""
import math
import numpy as np
from PIL import Image
from art import *

OUT_C = (30, 22, 20)


def _finish(c, grime=0.0, wear=0.65):
    im = c.im.resize((S, S), Image.LANCZOS)
    a = np.array(im).astype(np.float32)
    alpha = a[..., 3]
    mask = alpha > 40
    # 1px dark outline outside the silhouette
    pad = np.pad(mask, 1)
    near = pad[:-2, 1:-1] | pad[2:, 1:-1] | pad[1:-1, :-2] | pad[1:-1, 2:]
    ring = near & ~mask
    a[..., :3] = weather(a[..., :3], alpha, "icon" + str(int(a[..., :3].sum()) % 9973), wear, border=False)
    out = a.copy()
    out[ring] = [OUT_C[0], OUT_C[1], OUT_C[2], 235]
    ys, xs = np.where(out[..., 3] > 40)
    if len(ys):
        dy = int(round(32 - (ys.min() + ys.max()) / 2)); dx = int(round(32 - (xs.min() + xs.max()) / 2))
        out = np.roll(np.roll(out, dy, axis=0), dx, axis=1)
    # soft inner shade: darken the pixel row/col at lower right silhouette edge
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGBA")


def _grad(c, pts, base, top=1.2, bot=0.8):
    c.poly(pts, base)


def ingot(color, name="ingot"):
    c = Cv(None, True)
    top = [(10, 26), (36, 18), (54, 26), (28, 34)]
    front = [(10, 26), (28, 34), (28, 48), (10, 40)]
    side = [(28, 34), (54, 26), (54, 40), (28, 48)]
    c.poly([(x + 1, y + 2.5) for x, y in front + []], (0, 0, 0), 0)
    c.poly(front, sh(color, 0.95)); c.poly(side, sh(color, 0.66)); c.poly(top, sh(color, 1.22))
    c.poly([(14, 26.5), (36, 20.5), (46, 25), (26, 31)], sh(color, 1.42))        # inset top bevel
    c.poly([(16, 27), (36, 22), (42, 25), (24, 30)], sh(color, 1.12))
    c.line(10, 26, 28, 34, sh(color, 1.5), 1.0)
    c.line(28, 34, 28, 48, sh(color, 0.55), 0.9)
    c.line(10, 26, 36, 18, sh(color, 1.45), 1.0)
    c.line(36, 18, 54, 26, sh(color, 1.0), 0.9)
    c.poly([(11, 31), (27, 38.5), (27, 40), (11, 33)], sh(color, 1.18))
    return _finish(c)


def nugget(color, name="nugget"):
    c = Cv(None, True)
    for (cx, cy, r) in ((24, 38, 9), (38, 36, 8), (32, 26, 8)):
        c.ellipse(cx - r, cy - r * 0.8, cx + r, cy + r * 0.9, sh(color, 0.7))
        c.ellipse(cx - r, cy - r * 0.9, cx + r - 1.5, cy + r * 0.7, color)
        c.ellipse(cx - r * 0.6, cy - r * 0.75, cx + r * 0.1, cy - r * 0.1, sh(color, 1.45))
    return _finish(c)


def sheet(color, name="sheet"):
    c = Cv(None, True)
    top = [(8, 24), (40, 14), (58, 24), (26, 34)]
    front = [(8, 24), (26, 34), (26, 40), (8, 30)]
    side = [(26, 34), (58, 24), (58, 30), (26, 40)]
    c.poly(front, sh(color, 0.8)); c.poly(side, sh(color, 0.58)); c.poly(top, sh(color, 1.15))
    c.poly([(11, 24.5), (39, 16.5), (52, 23), (25, 31)], sh(color, 1.3))
    for (x, y) in ((16, 25), (44, 24), (30, 29), (30, 20)):
        c.ellipse(x - 1.4, y - 1.0, x + 1.4, y + 1.0, sh(color, 0.6)); c.ellipse(x - 1.4, y - 1.3, x + 0.4, y + 0.2, sh(color, 1.6))
    c.line(8, 24, 40, 14, sh(color, 1.6), 1.0)
    return _finish(c)


def crushed(color, name="crushed"):
    r = rng(name)
    c = Cv(None, True)
    pts = [(24, 44, 11), (42, 44, 10), (33, 33, 11), (20, 32, 8), (46, 32, 8), (32, 22, 7)]
    for (cx, cy, rr) in pts:
        n = 7
        poly = [(cx + rr * (0.7 + 0.3 * r.random()) * math.cos(a), cy + rr * 0.75 * (0.7 + 0.3 * r.random()) * math.sin(a)) for a in np.linspace(0, 2 * math.pi, n, endpoint=False)]
        c.poly([(x + 1, y + 1.5) for x, y in poly], sh(color, 0.5))
        c.poly(poly, sh(color, 0.9 + r.random() * 0.25))
        c.poly([(cx - rr * 0.5, cy - rr * 0.1), (cx - rr * 0.1, cy - rr * 0.55), (cx + rr * 0.3, cy - rr * 0.35), (cx - rr * 0.1, cy)], sh(color, 1.45))
    return _finish(c)


def flour(color, name="flour"):
    c = Cv(None, True)
    c.ellipse(8, 28, 56, 54, sh(color, 0.7))
    pts = [(8, 44), (16, 30), (28, 20), (36, 20), (48, 30), (56, 44), (44, 52), (20, 52)]
    c.poly(pts, color)
    c.poly([(16, 40), (22, 28), (30, 21), (32, 26), (24, 38)], sh(color, 1.2))
    c.poly([(40, 48), (50, 40), (54, 44), (44, 52)], sh(color, 0.8))
    return _finish(c, wear=0.15)


def dough(color, name="dough"):
    c = Cv(None, True)
    c.ellipse(8, 22, 56, 54, sh(color, 0.72))
    c.ellipse(8, 18, 54, 48, color)
    c.ellipse(16, 22, 36, 34, sh(color, 1.18))
    c.ellipse(22, 25, 30, 29, sh(color, 1.38))
    for (x, y) in ((40, 38), (30, 42), (22, 40)):
        c.ellipse(x - 2, y - 1.2, x + 2, y + 1.2, sh(color, 0.8))
    return _finish(c, wear=0.15)


def bucket(fluid, name="bucket"):
    c = Cv(None, True)
    c.poly([(14, 24), (50, 24), (44, 54), (20, 54)], sh(STEEL, 0.9))
    c.poly([(14, 24), (22, 24), (24, 54), (20, 54)], sh(STEEL_L, 0.9))
    c.poly([(44, 24), (50, 24), (44, 54), (40, 54)], sh(STEEL, 0.55))
    c.ellipse(13, 20, 51, 30, sh(STEEL_D, 0.8))
    c.ellipse(16, 22, 48, 29, fluid)
    c.ellipse(20, 23, 34, 26.5, sh(fluid, 1.4))
    c.line(14, 24, 50, 24, sh(STEEL_L, 1.1), 1.4)
    c.line(16, 22, 32, 8, STEEL_D, 2.0); c.line(48, 22, 32, 8, STEEL_D, 2.0)
    c.line(16, 22, 32, 8, sh(STEEL, 1.4), 1.0)
    c.rect(18, 36, 46, 38, sh(STEEL_D, 0.8), 140)
    return _finish(c)


def goggles(color, name="goggles"):
    c = Cv(None, True)
    c.rect(4, 26, 60, 33, RUBBER); c.rect(4, 26, 60, 28, sh(RUBBER, 1.4))
    for cx in (20, 44):
        c.ellipse(cx - 13, 21, cx + 13, 47, sh(BRASS, 0.55))
        c.ellipse(cx - 13, 20, cx + 12.5, 45.5, BRASS)
        c.ellipse(cx - 9.5, 24, cx + 9.5, 42, sh(STEEL_D, 0.7))
        c.ellipse(cx - 8.5, 25, cx + 8.5, 41, (92, 174, 200))
        c.ellipse(cx - 7, 26.5, cx - 0.5, 32, (210, 240, 250))
        c.rect(cx - 13, 29, cx - 11, 32, sh(BRASS, 1.3))
    c.rect(30, 30, 34, 36, sh(BRASS, 0.8))
    return _finish(c)


def wrench(color, name="wrench"):
    c = Cv(None, True)
    c.poly([(14, 52), (20, 56), (50, 26), (44, 20)], sh(STEEL, 1.0))
    c.poly([(14, 52), (17, 50), (47, 20), (44, 20)], sh(STEEL_L, 1.05))
    c.poly([(20, 56), (50, 26), (50, 29), (22, 57)], sh(STEEL, 0.55))
    # head with open jaw
    c.ellipse(40, 6, 62, 28, sh(BRASS, 0.7)); c.ellipse(39, 5, 60, 26, BRASS)
    c.ellipse(46, 12, 54, 20, (0, 0, 0), 0)
    c.poly([(48, 4), (60, 4), (58, 16), (50, 16)], (0, 0, 0), 0)
    c.poly([(47, 6), (52, 6), (52, 15), (47, 15)], sh(STEEL_D, 0.6))
    c.ellipse(41, 8, 49, 14, sh(BRASS, 1.35), 120)
    c.rect(18, 46, 24, 48, HAZ_Y)
    return _finish(c)


def belt_connector(color, name="beltc"):
    c = Cv(None, True)
    for k in (0, 1, 2):
        c.ellipse(10 + k * 2, 12 + k * 3, 54 - k * 2, 54 - k * 3 + 2, RUBBER if k % 2 == 0 else sh(RUBBER, 1.5))
        c.ellipse(14 + k * 2, 16 + k * 3, 50 - k * 2, 50 - k * 3 + 2, (0, 0, 0), 0)
    c.ellipse(10, 12, 54, 54, sh(RUBBER, 1.15))
    c.ellipse(17, 19, 47, 47, (0, 0, 0), 0)
    c.ellipse(16, 18, 48, 48, sh(RUBBER, 0.55))
    c.ellipse(24, 24, 40, 40, (0, 0, 0), 0)
    c.rect(26, 8, 38, 18, STEEL_L); c.outline_rect(26, 8, 38, 18, OUTL, 0.8, 255)
    c.rivet(29, 13, 1.1, STEEL); c.rivet(35, 13, 1.1, STEEL)
    return _finish(c)


def whisk(color, name="whisk"):
    c = Cv(None, True)
    c.rect(28, 38, 36, 60, WOOD); c.rect(28, 38, 31, 60, sh(WOOD, 1.3)); c.rect(34, 38, 36, 60, sh(WOOD, 0.65))
    c.rect(26, 34, 38, 40, BRASS)
    for k in range(-3, 4):
        c.line(32, 34, 32 + k * 6.5, 10, STEEL_L, 1.5)
        c.line(32, 34, 32 + k * 6.5, 10, sh(STEEL_L, 0.6), 0.5)
    c.ellipse(14, 6, 50, 16, (0, 0, 0), 0)
    c.line(14, 14, 50, 14, STEEL_L, 1.6)
    return _finish(c)


def sandpaper(color, name="sandpaper"):
    r = rng(name)
    c = Cv(None, True)
    base = (196, 112, 78) if "red" in name else (194, 160, 104)
    c.poly([(10, 16), (46, 8), (56, 44), (20, 52)], base)
    c.poly([(10, 16), (46, 8), (47, 12), (11, 20)], sh(base, 1.12))
    c.poly([(20, 52), (56, 44), (56, 47), (20, 55)], sh(base, 0.65))
    for _ in range(90):
        t, u = r.random(), r.random()
        x = 10 + 36 * t + 10 * u; y = 16 - 8 * t + 36 * u
        c.rect(x, y, x + 1.3, y + 1.3, sh(base, 0.6 + r.random() * 0.9))
    return _finish(c, wear=0.15)


def propeller(color, name="prop"):
    c = Cv(None, True)
    for k in range(4):
        blade = _rot_pts([(32, 30), (50, 12), (56, 20), (36, 34)], 32, 32, k * 90)
        c.poly(blade, sh(STEEL_L, 0.9 - 0.1 * (k % 2)))
    c.ellipse(26, 26, 38, 38, BRASS)
    return _finish(c)


def _rot_pts(pts, cx, cy, ang):
    a = math.radians(ang)
    return [(cx + (x - cx) * math.cos(a) - (y - cy) * math.sin(a), cy + (x - cx) * math.sin(a) + (y - cy) * math.cos(a)) for x, y in pts]


def generic_gear(color, name="gear"):
    c = Cv(None, True)
    pts = []
    for i in range(24):
        rr = 26 if i % 3 != 2 else 20
        for a in (i * 15 - 3, i * 15 + 3):
            pts.append((32 + rr * math.cos(math.radians(a)), 32 + rr * math.sin(math.radians(a))))
    c.poly(pts, sh(color, 0.8))
    c.ellipse(10, 10, 54, 54, sh(color, 1.0))
    c.ellipse(16, 16, 48, 48, sh(color, 0.8)); c.ellipse(20, 20, 44, 44, color)
    c.ellipse(26, 26, 38, 38, sh(STEEL_D, 0.5))
    c.ellipse(14, 12, 34, 24, sh(color, 1.35), 140)
    return _finish(c)


def quartz(color, name="quartz"):
    c = Cv(None, True)
    polished = name.startswith("polished")
    col = (238, 150, 168)
    if polished:
        pts = [(32, 8), (50, 22), (50, 42), (32, 56), (14, 42), (14, 22)]
        c.poly(pts, sh(col, 0.85))
        c.poly([(32, 8), (50, 22), (32, 30), (14, 22)], sh(col, 1.25))
        c.poly([(14, 22), (32, 30), (32, 56), (14, 42)], sh(col, 1.0))
        c.poly([(32, 30), (50, 22), (50, 42), (32, 56)], sh(col, 0.72))
        c.poly([(20, 22), (32, 14), (38, 18), (26, 26)], (255, 230, 236), 150)
    else:
        pts = [(24, 56), (14, 34), (24, 10), (36, 18), (46, 36), (40, 56)]
        c.poly(pts, sh(col, 0.8))
        c.poly([(24, 10), (36, 18), (30, 36), (14, 34)], sh(col, 1.2))
        c.poly([(14, 34), (30, 36), (24, 56)], sh(col, 0.95))
        c.poly([(30, 36), (36, 18), (46, 36), (40, 56), (24, 56)], sh(col, 0.7))
        c.poly([(22, 14), (28, 18), (22, 30), (18, 32)], (255, 226, 232), 130)
    return _finish(c, wear=0.15)


def tube(color, name="tube"):
    c = Cv(None, True)
    c.rect(24, 6, 40, 52, (180, 214, 226), 150)
    c.rect(24, 6, 29, 52, (230, 246, 252), 150)
    c.rect(22, 48, 42, 58, BRASS); c.rect(22, 48, 42, 50, sh(BRASS, 1.4)); c.rect(22, 56, 42, 58, sh(BRASS, 0.6))
    c.rect(26, 22, 38, 46, (238, 150, 168), 220); c.rect(28, 26, 36, 30, sh((238, 150, 168), 1.4))
    c.line(32, 6, 32, 22, STEEL_D, 1.2)
    c.ellipse(24, 4, 40, 12, (180, 214, 226), 200)
    return _finish(c)


MAKERS = [
    ("quartz", quartz), ("electron_tube", tube),
    ("bucket", None), ("ingot", ingot), ("nugget", nugget), ("sheet", sheet), ("flour", flour), ("dough", dough), ("crushed", crushed), ("raw_zinc", crushed),
    ("goggles", goggles), ("wrench", wrench), ("belt_connector", belt_connector), ("whisk", whisk), ("sand_paper", sandpaper), ("andesite_alloy", ingot), ("propeller", propeller),
]


def color_of(name):
    for k, v in (("zinc", ZINC), ("brass", BRASS), ("copper", COPPER), ("golden", (196, 160, 58)), ("gold", (196, 160, 58)), ("iron", (186, 188, 192)), ("andesite", (150, 156, 146)),
                 ("osmium", (120, 156, 196)), ("platinum", (196, 234, 238)), ("silver", (214, 218, 224)), ("tin", (206, 200, 178)), ("lead", (92, 96, 124)),
                 ("quicksilver", (176, 172, 206)), ("aluminum", (172, 196, 218)), ("uranium", (114, 166, 62)), ("nickel", (176, 186, 154)), ("flour", (236, 228, 200)),
                 ("dough", (226, 204, 152)), ("honey", (240, 176, 40)), ("chocolate", (104, 62, 40))):
        if k in name:
            return v
    return STEEL


def make(name):
    col = color_of(name)
    if "bucket" in name:
        return bucket(col, name)
    if "rose_quartz" in name:
        return quartz(col, name)
    for k, fn in MAKERS:
        if k in name and fn:
            return fn(col, name)
    return generic_gear(col, name)
