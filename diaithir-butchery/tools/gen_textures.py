"""Procedural 16x16 pixel art for diaithir-butchery items and station blocks. Run: python3 -I tools/gen_textures.py
Realistic materials: raw meat with fat and bone, browned cooked twins, tiered knives, Celtic wood/stone stations (Age II-III),
Factorio-style riveted industrial machines (cold room, vacuum packer, steam cannery, vat grower, stasis case)."""
import math, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(os.path.dirname(HERE), "src/main/resources/assets/diaithirbutchery/textures")
LIGHT = (-0.55, -0.65, 0.5)

def clamp(v): return max(0, min(255, int(v)))
def mul(c, f): return tuple(clamp(v * f) for v in c[:3])
def mix(a, b, t): return tuple(clamp(a[i] * (1 - t) + b[i] * t) for i in range(3))

class Cv:
    def __init__(s): s.p = [[None] * 16 for _ in range(16)]
    def set(s, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < 16 and 0 <= y < 16 and c is not None: s.p[y][x] = tuple(c[:3])
    def get(s, x, y): return s.p[y][x] if 0 <= x < 16 and 0 <= y < 16 else None
    def img(s):
        im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for y in range(16):
            for x in range(16):
                if s.p[y][x]: im.putpixel((x, y), (*s.p[y][x], 255))
        return im
    def blob(s, cx, cy, rx, ry, col, ang=0.0, rng=None, noise=9, power=2.0):
        """Shaded rounded blob (superellipse, rotated), light from upper left."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(16):
            for x in range(16):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u, v = (dx * ca + dy * sa) / rx, (-dx * sa + dy * ca) / ry
                d = abs(u) ** power + abs(v) ** power
                if d <= 1:
                    h = math.sqrt(max(0, 1 - d))
                    nx, ny = u * (1 - h), v * (1 - h)
                    l = 1.0 + 0.45 * (-(nx * 0.7 + ny * 0.7)) * 1.2 + 0.12 * (h - 0.5)
                    n = rng.uniform(-noise, noise) if rng else 0
                    s.set(x, y, tuple(clamp(c * l + n) for c in col))
    def rect(s, x0, y0, x1, y1, col):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1): s.set(x, y, col)
    def line(s, x0, y0, x1, y1, col):
        n = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(n + 1): s.set(round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), col)
    def outline(s, f=0.55, only_inside=True):
        res = []
        for y in range(16):
            for x in range(16):
                c = s.p[y][x]
                if c and any(s.get(x + dx, y + dy) is None for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))): res.append((x, y, mul(c, f)))
        for x, y, c in res: s.set(x, y, c)
    def mask_pixels(s): return [(x, y) for y in range(16) for x in range(16) if s.p[y][x]]

def cook(c):  # raw colour -> browned
    return mix(mul(c, 0.55), (120, 70, 35), 0.45)

# ---------------- items ----------------
RED = (176, 52, 56); DARK = (128, 34, 42); FAT = (240, 226, 200); BONE = (232, 222, 196)

def marble(cv, rng, fatc, n):
    px = cv.mask_pixels()
    for _ in range(n):
        x, y = rng.choice(px)
        for k in range(rng.randint(1, 3)):
            if cv.get(x + k, y) and rng.random() < .9: cv.set(x + k, y, mix(cv.get(x + k, y), fatc, .75))

def fat_rim(cv, side, fatc, thick=1):
    """Fat cap along one side: 'top','bottom','left','right'."""
    for x, y in cv.mask_pixels():
        t = 0
        for k in range(1, thick + 1):
            dx, dy = {"top": (0, -k), "bottom": (0, k), "left": (-k, 0), "right": (k, 0)}[side]
            if cv.get(x + dx, y + dy) is None: t = 1
        if t: cv.set(x, y, mix(fatc, cv.get(x, y), .25))

def grill(cv, n=3, col=(55, 30, 18)):
    px = cv.mask_pixels()
    if not px: return
    ys = [p[1] for p in px]; lo, hi = min(ys), max(ys)
    for i in range(n):
        off = lo + (hi - lo) * (i + 1) / (n + 1)
        for x, y in px:
            if abs((y - off) - (x - 8) * 0.55) < 0.55: cv.set(x, y, mix(cv.get(x, y), col, .75))

def crust(cv, rng):
    for x, y in cv.mask_pixels():
        if rng.random() < .22: cv.set(x, y, mul(cv.get(x, y), rng.choice((.8, 1.15))))
    cv.outline(.65)

def bone_end(cv, x, y, r=1):
    cv.blob(x + .5, y + .5, r + 1, r + 1, BONE, power=2)

def draw_meat(name, cooked, rng):
    cv = Cv()
    base = RED
    if name == "haunch":
        cv.blob(8.5, 8.5, 6.2, 5.2, (178, 54, 56), -0.5, rng); fat_rim(cv, "top", FAT); marble(cv, rng, FAT, 5)
        cv.line(2, 12, 5, 9, BONE); cv.blob(2.5, 12.5, 1.5, 1.5, BONE)
    elif name == "shoulder":
        cv.blob(8, 8.5, 6.4, 5, (172, 62, 60), 0.4, rng); fat_rim(cv, "bottom", FAT); marble(cv, rng, FAT, 7)
        cv.blob(11.5, 7, 1.5, 1.5, BONE)
    elif name == "loin":
        cv.blob(8, 8.5, 6.8, 3.8, (190, 66, 68), 0.15, rng); fat_rim(cv, "top", FAT, 2); marble(cv, rng, FAT, 4)
    elif name == "ribs":
        cv.blob(8, 8.5, 6.4, 5.4, (184, 92, 80), 0.1, rng)
        for x in (4, 7, 10, 13): cv.line(x, 4, x - 1, 13, BONE)
        fat_rim(cv, "top", FAT)
    elif name == "flank":
        cv.blob(8, 8.5, 6.6, 4.4, (166, 66, 66), -0.25, rng, power=2.6)
        for i in range(-5, 6, 2): cv.line(8 + i - 2, 11, 8 + i + 2, 6, mix(cv.get(8, 8) or RED, DARK, .5))
        fat_rim(cv, "bottom", FAT)
    elif name == "tenderloin":
        cv.blob(8, 8.5, 7, 2.9, (204, 84, 88), -0.45, rng); marble(cv, rng, FAT, 2); cv.set(13, 4, FAT); cv.set(2, 12, FAT)
    elif name == "neck":
        cv.blob(8, 8.5, 6, 4.6, (160, 66, 62), 0.7, rng); cv.blob(5.5, 9, 1.6, 1.6, BONE); cv.set(5, 9, (150, 40, 40)); marble(cv, rng, FAT, 4)
    elif name == "tail":
        for i in range(8):
            t = i / 7; cv.blob(3 + t * 10, 12 - t * 8 - math.sin(t * 3) * 1, 2.2 - t * 1.0, 2.0 - t * .8, mix((170, 100, 84), (120, 70, 60), t), rng=rng, noise=6)
        cv.set(14, 3, BONE)
    elif name == "hock":
        cv.blob(7, 8, 5, 4.4, (172, 80, 70), 0.3, rng); fat_rim(cv, "top", FAT); cv.rect(11, 6, 14, 7, BONE); cv.blob(14.5, 6.5, 1.5, 1.8, BONE); cv.set(9, 7, FAT)
    elif name == "fish_fillet":
        cv.blob(8, 8.5, 7, 3.6, (238, 140, 118), -0.2, rng, power=2.4)
        for i in range(-5, 6, 2): cv.line(8 + i, 5, 8 + i - 1, 11, (250, 205, 190))
        cv.line(2, 8, 14, 7, (205, 90, 80))
        for x, y in cv.mask_pixels():
            if y > 10: cv.set(x, y, (225, 215, 205))
    elif name == "bird_breast":
        cv.blob(8, 8.5, 6, 5, (236, 176, 152), 0.3, rng); cv.line(5, 5, 11, 12, (250, 212, 190)); marble(cv, rng, (250, 225, 200), 3)
        for x, y in cv.mask_pixels():
            if y < 6 and x > 8: cv.set(x, y, mix(cv.get(x, y), (240, 205, 120), .45))
    elif name == "drumstick":
        cv.blob(6.5, 6.5, 4.6, 4.2, (214, 130, 80), 0.7, rng); cv.line(9, 9, 13, 13, BONE); cv.line(10, 8, 14, 12, mul(BONE, .85)); cv.blob(14, 14, 1.4, 1.4, BONE); cv.blob(13, 14.5, 1.2, 1.2, BONE)
        for x, y in cv.mask_pixels():
            if cv.get(x, y)[0] > 200 and y < 5: cv.set(x, y, mix(cv.get(x, y), (240, 195, 110), .4))
    elif name == "wing":
        cv.blob(6, 9, 5, 3.4, (226, 168, 128), 0.55, rng); cv.blob(11, 5.5, 3.4, 2.1, (210, 150, 110), -0.7, rng); cv.line(2, 12, 6, 9, (200, 130, 100)); cv.blob(13.5, 3.5, 1.3, 1.3, BONE)
    elif name == "scraps":
        for cx, cy, rx, ry, a in ((5, 6, 3.2, 2.5, .4), (11, 5, 2.8, 2.2, -.5), (8, 11, 4, 2.6, .1), (13, 11, 2, 1.8, 0)):
            cv.blob(cx, cy, rx, ry, rng.choice(((170, 58, 58), (186, 82, 76), (150, 50, 54))), a, rng)
        fat_rim(cv, "top", FAT)
    # offal
    elif name == "liver":
        cv.blob(8, 8.5, 6.6, 4.8, (106, 38, 44), 0.25, rng, 6); cv.blob(5, 6, 3, 2, (135, 55, 58), .5); cv.line(8, 4, 8, 12, (70, 25, 32)); cv.set(11, 6, (200, 130, 140)); cv.set(10, 7, (200, 130, 140))
    elif name == "heart":
        cv.blob(6, 8, 4, 4.4, (160, 44, 58), 0.2, rng, 6); cv.blob(10.5, 8, 3.8, 4.2, (150, 40, 56), -0.2, rng, 6); cv.blob(8, 12, 3, 2.6, (140, 36, 52)); cv.rect(7, 2, 8, 5, (220, 190, 120)); cv.set(8, 2, (190, 140, 80)); cv.line(5, 7, 10, 12, (115, 28, 42)); cv.set(5, 5, (210, 120, 130))
    elif name == "kidney":
        cv.blob(8, 8.5, 5.4, 5, (128, 56, 54), 0, rng, 6); cv.blob(10.5, 8.5, 2.2, 2.4, (0, 0, 0)); 
        for y in range(16):
            for x in range(16):
                if (x + .5 - 11) ** 2 + (y + .5 - 8.5) ** 2 < 3.5: cv.p[y][x] = None
        cv.set(11, 8, (210, 170, 130)); fat_rim(cv, "bottom", FAT)
    elif name == "tripe":
        cv.blob(8, 8.5, 6.6, 5, (222, 202, 172), 0, rng, 6)
        for y in range(4, 13, 2):
            for x in range(3, 14, 2):
                if cv.get(x, y) and (x + y) % 4 == 0: cv.set(x, y, mul(cv.get(x, y), .82)); cv.set(x + 1, y + 1, mul(cv.get(x, y), 1.0))
    elif name == "tongue":
        cv.blob(8, 8.5, 7, 3.5, (196, 98, 106), -0.3, rng, 6, power=2.3)
        for x, y in cv.mask_pixels():
            if (x * 3 + y * 5) % 4 == 0: cv.set(x, y, mul(cv.get(x, y), 1.14))
        for x in range(10, 15):
            if cv.get(x, 5): cv.set(x, 5, (235, 225, 215))
    elif name == "brain":
        cv.blob(8, 8, 6, 5, (216, 150, 160), 0, rng, 5)
        cv.line(8, 3, 8, 13, (170, 100, 115))
        for y in range(4, 13, 2):
            for dx in (-3, 3):
                cv.set(8 + dx, y, (178, 108, 122)); cv.set(8 + dx * 1.5, y + 1, (178, 108, 122))
    else:
        raise KeyError(name)
    if cooked:
        for x, y in cv.mask_pixels():
            c = cv.get(x, y)
            if c[0] > 215 and c[1] > 190 and c[2] > 160 and name not in ("tripe",):  # bone/fat stays pale, slightly golden
                cv.set(x, y, mix(c, (200, 160, 90), .35))
            else: cv.set(x, y, cook(c) if name not in ("fish_fillet", "bird_breast", "drumstick", "wing", "tripe") else mix(mul(c, .8), (170, 110, 50), .45))
        crust(cv, rng)
        if name not in ("scraps", "tail", "tripe", "brain", "kidney"): grill(cv, 3)
    else:
        cv.outline(.6)
    return cv

def draw_misc(name, cooked, rng):
    cv = Cv()
    if name == "mince":
        for _ in range(26):
            x, y = rng.randint(2, 13), rng.randint(5, 13)
            if (x - 8) ** 2 / 36 + (y - 10) ** 2 / 14 < 1 or y > 8 and abs(x - 8) < 5 - (y - 8) * .1:
                cv.blob(x + .5, y + .5, 1.9, 1.7, rng.choice(((190, 70, 76), (170, 58, 64), (205, 100, 100), (225, 190, 170))), rng=rng, noise=8)
        cv.outline(.65)
    elif name == "sausage":
        for i in range(3):
            cv.blob(8, 3.8 + i * 4.2, 6.4, 2.2, (176, 84, 68), 0.1 * (i - 1), rng, 5, power=2.4)
        for x, y in cv.mask_pixels():
            if (x + y) % 5 == 0: cv.set(x, y, mix(cv.get(x, y), (230, 190, 170), .4))
        for y in (1, 5, 9): cv.set(8, y, (220, 215, 200))
        cv.outline(.6)
    elif name == "cured_meat":
        cv.blob(8, 8.5, 6.6, 4.6, (122, 44, 50), 0.2, rng, 6); fat_rim(cv, "top", (232, 214, 188)); marble(cv, rng, (230, 200, 190), 5)
        for x, y in cv.mask_pixels():
            if rng.random() < .1: cv.set(x, y, (225, 220, 210))
        cv.line(1, 8, 4, 8, (190, 160, 110)); cv.set(4, 8, (200, 170, 120)); cv.outline(.6)
    elif name == "jerky":
        cv.blob(8, 8.5, 7, 3, (92, 52, 36), -0.5, rng, 7, power=3.2)
        for x, y in cv.mask_pixels():
            if (x * 2 + y) % 5 == 0: cv.set(x, y, mul(cv.get(x, y), 1.3))
            if rng.random() < .12: cv.set(x, y, mix(cv.get(x, y), (230, 225, 215), .4))
        cv.outline(.6)
    elif name == "lard":
        cv.rect(3, 4, 12, 12, (244, 238, 220))
        for y in range(4, 13):
            for x in range(3, 13): cv.set(x, y, mul((246, 240, 224), 1.0 - (y - 4) * .02 - rng.random() * .03))
        cv.rect(3, 4, 12, 5, (252, 250, 238)); cv.rect(12, 5, 12, 12, (222, 214, 190)); cv.line(3, 12, 12, 12, (205, 196, 170)); cv.set(6, 8, (232, 218, 180)); cv.outline(.78)
    elif name == "canned_meat":
        cv.rect(4, 3, 11, 13, (172, 176, 182))
        for y in range(3, 14):
            for x in range(4, 12): cv.set(x, y, mul((176, 180, 186), 1.18 - (x - 4) * .06))
        cv.rect(4, 3, 11, 3, (215, 218, 222)); cv.rect(4, 4, 11, 4, (120, 124, 130)); cv.rect(4, 12, 11, 12, (120, 124, 130)); cv.rect(4, 13, 11, 13, (215, 218, 222))
        cv.rect(5, 6, 10, 10, (186, 56, 50)); cv.rect(5, 6, 10, 6, (226, 190, 70)); cv.set(7, 8, (240, 230, 210)); cv.set(8, 8, (240, 230, 210)); cv.set(8, 9, (240, 230, 210)); cv.outline(.6)
    elif name == "grown_cut":
        cv.blob(8, 8.5, 6.2, 4.8, (228, 140, 150), 0.2, rng, 5)
        for x, y in cv.mask_pixels():
            if (x + y * 2) % 4 == 0: cv.set(x, y, mix(cv.get(x, y), (250, 205, 205), .5))
        for x, y in ((4, 6), (11, 10), (7, 11)): cv.set(x, y, (140, 235, 215))
        cv.set(10, 5, (140, 235, 215)); cv.outline(.62)
    elif name == "raw_hide":
        cv.blob(8, 8, 6.8, 5.6, (150, 106, 72), 0.3, rng, 10, power=2.8)
        for x, y in cv.mask_pixels():
            if rng.random() < .25: cv.set(x, y, mul(cv.get(x, y), rng.choice((.8, 1.2))))
            if cv.get(x + 1, y) is None and rng.random() < .5: cv.set(x, y, (205, 170, 130))
        for x, y in ((2, 4), (13, 12)): cv.p[y][x] = None
        cv.blob(5.5, 5.5, 1.5, 1.5, (120, 80, 55)); cv.outline(.6)
    elif name == "sinew":
        for i, c in enumerate(((232, 222, 200), (214, 202, 176), (240, 232, 214), (200, 186, 158))):
            for t in range(18):
                x = 1 + t * .85 + i * .5; y = 3 + i * 3 + math.sin(t * .6 + i) * 1.2
                cv.set(x, y, mul(c, 1.0 - (t % 3) * .05))
        cv.blob(3, 8, 1.8, 2.2, (214, 150, 140)); cv.blob(13, 8, 1.8, 2.2, (214, 150, 140)); cv.outline(.7)
    elif name == "tallow":
        cv.blob(8, 9, 5.8, 4.6, (238, 228, 196), 0, rng, 4, 2.2); cv.blob(8, 5, 4.4, 2.2, (250, 244, 220), 0, None, 0); cv.set(6, 8, (225, 205, 150)); cv.set(10, 10, (225, 205, 150)); cv.outline(.75)
    elif name == "blood_flask":
        cv.rect(7, 1, 8, 4, (200, 215, 220)); cv.rect(6, 1, 9, 1, (150, 110, 70)); cv.rect(6, 2, 9, 2, (120, 84, 52))
        cv.blob(8, 10, 5.6, 4.8, (150, 22, 28), 0, rng, 4)
        for x, y in cv.mask_pixels():
            if y < 8 and cv.get(x, y)[0] < 220 and y > 4: cv.set(x, y, (178, 212, 222))
        cv.rect(5, 8, 11, 8, (215, 235, 240)); cv.set(5, 10, (240, 130, 130)); cv.set(5, 11, (240, 130, 130)); cv.set(6, 9, (240, 90, 90)); cv.outline(.55)
    elif name == "marrow_bone":
        cv.rect(4, 6, 11, 9, BONE)
        for y in range(6, 10):
            for x in range(4, 12): cv.set(x, y, mul(BONE, 1.05 - (y - 6) * .09))
        cv.blob(3.5, 5.5, 2.2, 2.2, (238, 230, 206)); cv.blob(3.5, 10.5, 2.2, 2.2, (232, 222, 196)); cv.blob(12.5, 5.5, 2.2, 2.2, (238, 230, 206)); cv.blob(12.5, 10.5, 2.2, 2.2, (216, 204, 176))
        cv.rect(1, 6, 2, 9, None) if False else None
        cv.set(2, 8, (216, 160, 90)); cv.set(2, 7, (190, 70, 70)); cv.set(14, 8, (190, 70, 70)); cv.set(14, 7, (216, 160, 90)); cv.outline(.65)
    elif name == "carcass":
        cv.line(8, 1, 8, 3, (150, 150, 158)); cv.blob(8, 8, 4.6, 6.4, (180, 78, 72), 0, rng, 7)
        fat_rim(cv, "top", FAT, 2)
        for y in range(5, 13, 2):
            cv.line(5, y, 8, y + 1, BONE); cv.line(11, y, 8, y + 1, BONE)
        cv.line(8, 3, 8, 14, (230, 205, 190)); cv.blob(5.5, 14, 1.6, 1.4, (170, 66, 62)); cv.blob(10.5, 14, 1.6, 1.4, (170, 66, 62)); cv.outline(.58)
    else:
        raise KeyError(name)
    return cv

# ---------------- knives ----------------
BLADES = {"flint_knife": ((86, 90, 98), (150, 156, 164)), "copper_knife": ((176, 100, 60), (226, 150, 100)), "bronze_knife": ((150, 112, 46), (212, 168, 80)),
          "butcher_knife": ((140, 144, 152), (206, 210, 218)), "steel_knife": ((170, 178, 190), (238, 244, 250)), "tempered_knife": ((96, 112, 160), (170, 190, 240)),
          "ceramic_knife": ((226, 226, 214), (255, 255, 248)), "diamond_edge_knife": ((70, 190, 205), (190, 252, 255)), "crystal_thread_knife": ((140, 100, 210), (214, 186, 255)),
          "shard_knife": ((196, 70, 170), (255, 170, 230))}

def draw_knife(name, rng):
    cv = Cv()
    dark, lite = BLADES[name]
    handle = (92, 60, 36) if name not in ("ceramic_knife",) else (60, 60, 66)
    # diagonal blade from lower-left handle to upper-right tip
    blade = [(x, y) for x, y in [(5, 10), (6, 9), (7, 8), (8, 7), (9, 6), (10, 5), (11, 4), (12, 3), (13, 2)]]
    for i, (x, y) in enumerate(blade):
        w = 2 if i < 7 else 1
        cv.set(x, y, lite)
        cv.set(x, y + 1, mix(dark, lite, .5)); 
        if w == 2: cv.set(x + 1, y + 1, dark)
        cv.set(x - 1, y, dark) if False else None
    cv.set(14, 1, lite); cv.set(13, 3, dark); cv.set(12, 4, dark)
    for x, y in cv.mask_pixels():
        if rng.random() < .1: cv.set(x, y, mix(cv.get(x, y), (255, 255, 255), .2))
    # edge belly
    cv.set(6, 10, dark); cv.set(7, 9, dark)
    # guard + handle
    guard = (110, 100, 90) if name in ("flint_knife",) else mul(dark, .8)
    cv.set(4, 10, guard); cv.set(5, 11, guard); cv.set(4, 11, mul(guard, .8))
    for i, (x, y) in enumerate(((3, 11), (2, 12), (1, 13), (0, 14))):
        cv.set(x, y, mul(handle, 1.15)); cv.set(x, y + 1, mul(handle, .8)); cv.set(x + 1, y + 1, handle) if i < 3 else None
    cv.set(3, 12, handle); cv.set(2, 13, mul(handle, .85)); cv.set(1, 14, mul(handle, .8))
    if name == "flint_knife":  # knapped flakes + cord wrap
        cv.set(8, 6, (60, 64, 72)); cv.set(10, 4, (60, 64, 72)); cv.set(3, 12, (200, 180, 130)); cv.set(2, 13, (170, 150, 105))
    if name in ("diamond_edge_knife", "crystal_thread_knife", "shard_knife"):
        cv.set(9, 5, (255, 255, 255)); cv.set(12, 3, (255, 255, 255))
    cv.outline(.5)
    return cv

def draw_cleaver():
    cv = Cv(); steel, lite = (150, 156, 166), (222, 228, 236)
    for y in range(3, 11):
        for x in range(4, 14): cv.set(x, y, mix(lite, steel, (x - 4 + (y - 3)) / 16))
    cv.rect(4, 10, 13, 10, (110, 114, 122)); cv.rect(4, 3, 13, 3, (240, 244, 248))
    cv.set(12, 5, (60, 60, 66)); cv.set(11, 5, (60, 60, 66))
    cv.rect(2, 6, 3, 7, (90, 90, 96))
    cv.rect(0, 5, 3, 8, (96, 62, 38)); cv.rect(0, 5, 3, 5, (130, 88, 54)); cv.rect(0, 8, 3, 8, (66, 42, 26)); cv.set(1, 6, (190, 190, 196)); cv.set(1, 7, (190, 190, 196))
    cv.outline(.5)
    return cv

# ---------------- blocks ----------------
def noisy(cv, rng, col, n=8):
    for y in range(16):
        for x in range(16): cv.set(x, y, mul(col, 1 + rng.uniform(-n, n) / 100 * 3))

def planks(cv, rng, base, rows=4, vertical=False):
    for r in range(rows):
        shade = rng.uniform(.85, 1.1); lo, hi = r * 16 // rows, (r + 1) * 16 // rows
        for a in range(lo, hi):
            for b in range(16):
                x, y = (a, b) if vertical else (b, a)
                grain = 1 + (.06 if (b + r * 3) % 5 == 0 else 0) + rng.uniform(-.04, .04)
                edge = .72 if a == hi - 1 else 1.12 if a == lo else 1
                cv.set(x, y, mul(base, shade * grain * edge))
        k = rng.randint(2, 12)
        x, y = (lo + 1, k) if vertical else (k, lo + 1)
        cv.set(x, y, mul(base, .6))

def rivets(cv, pts, col=(70, 72, 76), hi=(190, 192, 196)):
    for x, y in pts: cv.set(x, y, col); cv.set(x - 1, y - 1, hi) if False else None

def steel_plate(cv, rng, base, frame, top=False):
    for y in range(16):
        for x in range(16): cv.set(x, y, mul(base, 1 + rng.uniform(-.04, .04) + (0.05 if y < 8 else 0)))
    for i in range(16):
        for c, f in ((0, 1.3), (15, .65)):
            cv.set(i, c, mul(frame, f)); cv.set(c, i, mul(frame, 1.3 if c == 0 else .65))
    for i in range(1, 15):
        cv.set(i, 1, mul(frame, 1.05)); cv.set(i, 14, mul(frame, .8)); cv.set(1, i, mul(frame, 1.05)); cv.set(14, i, mul(frame, .8))
    for p in ((2, 2), (13, 2), (2, 13), (13, 13)): cv.set(*p, (60, 60, 64)); cv.set(p[0] + 1, p[1] + 1, mul(frame, 1.2)) if False else None

YEL = (232, 176, 40)
def draw_station(sid, part, rng):
    cv = Cv(); top = part == "top"
    if sid == "drying_rack":
        if top:
            planks(cv, rng, (150, 108, 66), 8, True)
            for x in (3, 7, 11): cv.rect(x, 0, x, 15, (86, 58, 36))
        else:
            noisy(cv, rng, (120, 86, 52), 4); cv.rect(0, 0, 1, 15, (96, 66, 40)); cv.rect(14, 0, 15, 15, (96, 66, 40)); cv.rect(0, 0, 15, 1, (110, 76, 46)); cv.rect(0, 14, 15, 15, (96, 66, 40))
            for x in (4, 7, 10): cv.line(x, 1, x, 14, (170, 56, 52) if x == 7 else (130, 50, 46)); cv.rect(x - 1, 5, x, 11, (150, 52, 50))
            cv.rect(2, 3, 13, 3, (190, 160, 110))
    elif sid == "rotisserie":
        if top:
            planks(cv, rng, (140, 100, 62), 8, True)
            cv.rect(2, 7, 13, 8, (60, 60, 64)); cv.rect(1, 6, 1, 9, (60, 60, 64)); cv.rect(14, 6, 14, 9, (60, 60, 64))
        else:
            noisy(cv, rng, (112, 80, 52), 4); cv.rect(0, 0, 15, 1, (90, 62, 38)); cv.rect(0, 14, 15, 15, (70, 46, 30))
            cv.rect(1, 6, 14, 7, (60, 60, 64)); cv.blob(8, 7, 4.5, 3.0, (150, 84, 48)); cv.blob(8, 6, 3.5, 1.8, (190, 120, 66))
            cv.rect(3, 11, 12, 13, (210, 96, 36)); cv.rect(5, 12, 10, 13, (255, 190, 70)); cv.set(7, 12, (255, 235, 150))
    elif sid == "salting_barrel":
        if top:
            noisy(cv, rng, (140, 104, 62), 4)
            for y in range(16):
                for x in range(16):
                    d = math.hypot(x - 7.5, y - 7.5)
                    if d > 7.2: cv.set(x, y, (92, 92, 98))
                    elif d > 5.8: cv.set(x, y, (150, 108, 64))
                    else: cv.set(x, y, mul((226, 226, 218), 1 + rng.uniform(-.07, .07)))
            cv.set(5, 6, (255, 255, 255)); cv.set(9, 9, (200, 200, 195))
        else:
            for x in range(16):
                f = 1 + (0.1 if x % 4 == 0 else 0) * -1 + rng.uniform(-.04, .04)
                for y in range(16): cv.set(x, y, mul((148, 104, 62), (.78 if x % 4 == 3 else 1.04 if x % 4 == 0 else 1) * (1 + rng.uniform(-.04, .04))))
            for y in (2, 3, 12, 13):
                for x in range(16): cv.set(x, y, mul((88, 90, 96), 1.2 if y in (2, 12) else .85))
            cv.set(5, 8, (60, 40, 25)); cv.set(11, 6, (60, 40, 25))
            for x in (3, 9): cv.set(x, 4, (225, 225, 215))
    elif sid == "meat_grinder":
        if top:
            steel_plate(cv, rng, (120, 124, 130), (92, 96, 102))
            cv.blob(8, 8, 4.6, 4.6, (70, 72, 78), power=2); cv.blob(8, 8, 3.4, 3.4, (40, 40, 44), power=2); cv.line(8, 6, 8, 10, (140, 140, 148)); cv.line(6, 8, 10, 8, (140, 140, 148))
        else:
            noisy(cv, rng, (104, 108, 114), 3); cv.rect(0, 0, 15, 1, (150, 154, 160)); cv.rect(0, 14, 15, 15, (70, 72, 78))
            cv.blob(8, 8, 3.6, 3.6, (60, 62, 68)); cv.blob(8, 8, 2.0, 2.0, (30, 30, 34))
            for p in ((2, 3), (13, 3), (2, 12), (13, 12)): cv.set(*p, (180, 184, 190)); cv.set(p[0], p[1] + 1, (60, 60, 66))
            cv.rect(11, 10, 14, 11, (150, 40, 44)); cv.rect(0, 7, 3, 8, (70, 60, 50))
    elif sid == "sausage_stuffer":
        if top:
            planks(cv, rng, (134, 98, 60), 4)
            cv.blob(8, 8, 4.4, 4.4, (170, 174, 182)); cv.blob(8, 8, 2.8, 2.8, (60, 62, 68)); cv.set(6, 6, (230, 232, 236))
        else:
            planks(cv, rng, (126, 92, 56), 4, True)
            cv.rect(5, 2, 10, 13, (150, 154, 162)); cv.rect(5, 2, 6, 13, (200, 204, 210)); cv.rect(10, 2, 10, 13, (100, 104, 112)); cv.rect(5, 2, 10, 2, (110, 114, 122))
            cv.rect(7, 13, 8, 14, (80, 82, 88)); cv.rect(11, 7, 14, 8, (176, 84, 70))
    elif sid == "rendering_cauldron":
        if top:
            for y in range(16):
                for x in range(16):
                    d = math.hypot(x - 7.5, y - 7.5)
                    if d > 7.4: cv.set(x, y, (28, 28, 32))
                    elif d > 5.6: cv.set(x, y, mul((58, 58, 64), 1 + rng.uniform(-.1, .1)))
                    else: cv.set(x, y, mul((226, 212, 160), 1 + rng.uniform(-.06, .06) - d * .02))
            cv.set(6, 6, (255, 246, 210)); cv.set(10, 9, (255, 246, 210)); cv.set(8, 5, (190, 160, 90))
        else:
            noisy(cv, rng, (46, 46, 52), 5)
            cv.rect(0, 0, 15, 2, (74, 74, 82)); cv.rect(0, 3, 15, 3, (24, 24, 28))
            for x in range(16): cv.set(x, 6, mul((60, 60, 66), 1 + (x % 5) * .04))
            for x, y in ((3, 8), (7, 10), (12, 9), (5, 12), (10, 13)): cv.set(x, y, (82, 82, 90))
            cv.rect(0, 13, 15, 15, (200, 90, 30)); cv.rect(0, 15, 15, 15, (250, 160, 40)); cv.set(4, 13, (255, 200, 70)); cv.set(11, 13, (255, 200, 70))
    elif sid == "smokehouse":
        if top:
            for y in range(16):
                for x in range(16):
                    r = (x // 4 + y // 4) % 2
                    cv.set(x, y, mul((110, 106, 100) if r else (92, 88, 84), 1 + rng.uniform(-.1, .1)))
                    if x % 4 == 0 or y % 4 == 0: cv.set(x, y, (58, 56, 54))
            cv.rect(6, 6, 9, 9, (40, 38, 38)); cv.rect(7, 7, 8, 8, (20, 20, 20))
        else:
            for y in range(16):
                for x in range(16):
                    off = 4 if (y // 4) % 2 else 0
                    cv.set(x, y, mul((104, 98, 92), 1 + rng.uniform(-.1, .08) - 0.04 * ((x + off) % 8 > 6)))
                    if y % 4 == 0 or (x + off) % 8 == 0: cv.set(x, y, (62, 58, 56))
            cv.rect(5, 5, 10, 15, (20, 18, 18)); cv.rect(6, 4, 9, 4, (30, 28, 28))
            for x in range(5, 11): cv.set(x, 12, (200, 90, 30)) if x in (6, 8) else None
            cv.set(7, 13, (240, 150, 40)); cv.set(8, 13, (200, 90, 30)); cv.set(6, 14, (180, 70, 25))
            for x, y in ((3, 1), (12, 2), (2, 3)): cv.set(x, y, (50, 46, 44))
    elif sid == "steam_cannery":
        base = (124, 80, 52)
        steel_plate(cv, rng, base, (92, 58, 36))
        if top:
            cv.blob(8, 8, 4.5, 4.5, (176, 180, 184)); cv.blob(8, 8, 3, 3, (84, 88, 94)); cv.set(6, 6, (230, 232, 236))
        else:
            cv.rect(3, 4, 12, 4, (206, 210, 214)); cv.rect(3, 10, 12, 10, (80, 84, 90))
            cv.blob(8, 7, 2.2, 2.2, (216, 218, 216)); cv.set(8, 6, (30, 30, 34)); cv.set(8, 7, (30, 30, 34)); cv.set(9, 7, (190, 40, 40))
            cv.rect(5, 12, 10, 13, (220, 150, 40)); cv.rect(5, 12, 10, 12, YEL)
            cv.set(4, 4, (230, 120, 40)); cv.set(11, 11, (230, 230, 230))
    elif sid == "cold_room":
        steel_plate(cv, rng, (150, 190, 214), (96, 130, 156))
        if top:
            for x in range(3, 13, 2): cv.line(x, 3, x, 12, (214, 238, 250)); 
            cv.rect(3, 3, 12, 3, (230, 246, 255)); cv.rect(3, 12, 12, 12, (90, 130, 160))
        else:
            cv.rect(3, 3, 12, 12, (186, 220, 238)); cv.rect(3, 3, 12, 3, (230, 248, 255)); cv.rect(3, 12, 12, 12, (110, 150, 176)); cv.rect(3, 3, 3, 12, (230, 248, 255)); cv.rect(12, 3, 12, 12, (110, 150, 176))
            cv.rect(10, 6, 10, 9, (60, 70, 80)); cv.set(5, 5, (255, 255, 255)); cv.set(6, 10, (255, 255, 255)); cv.set(7, 11, (240, 252, 255))
            cv.rect(0, 14, 15, 14, YEL) if False else None
    elif sid == "vacuum_packer":
        steel_plate(cv, rng, (176, 180, 186), (128, 132, 140))
        if top:
            cv.rect(3, 4, 12, 11, (60, 64, 72)); cv.rect(3, 4, 12, 4, (110, 114, 122)); cv.rect(4, 7, 11, 8, (30, 30, 34)); cv.rect(4, 7, 11, 7, (255, 200, 60))
        else:
            cv.rect(3, 3, 12, 8, (46, 56, 66)); cv.rect(3, 3, 12, 3, (120, 150, 170)); cv.set(5, 5, (170, 220, 240)); cv.set(6, 4, (200, 240, 255))
            cv.rect(3, 10, 12, 12, (96, 100, 108)); cv.set(4, 11, (60, 200, 90)); cv.set(6, 11, (230, 70, 60)); cv.rect(8, 11, 11, 11, (50, 50, 56))
            cv.rect(0, 15, 15, 15, YEL) if False else None
    elif sid == "vat_grower":
        steel_plate(cv, rng, (82, 96, 104), (56, 68, 76))
        if top:
            cv.blob(8, 8, 5.2, 5.2, (70, 190, 160)); cv.blob(8, 8, 3.4, 3.4, (150, 245, 215))
            for p in ((6, 6), (10, 9), (8, 10)): cv.set(*p, (230, 255, 245))
        else:
            for y in range(3, 13):
                for x in range(3, 13): cv.set(x, y, mul((60, 170, 140), 1.25 - (y - 3) * .06 + rng.uniform(-.04, .04)))
            cv.rect(3, 3, 12, 3, (34, 44, 50)); cv.rect(3, 12, 12, 12, (34, 44, 50)); cv.rect(3, 3, 3, 12, (34, 44, 50)); cv.rect(12, 3, 12, 12, (34, 44, 50))
            for p in ((6, 10), (9, 8), (7, 6), (10, 11)): cv.set(*p, (200, 255, 235))
            cv.rect(7, 8, 8, 9, (225, 140, 150))
    elif sid == "stasis_case":
        steel_plate(cv, rng, (74, 62, 108), (48, 40, 76))
        if top:
            cv.blob(8, 8, 4.6, 4.6, (140, 110, 230)); cv.blob(8, 8, 2.6, 2.6, (220, 200, 255))
        else:
            for y in range(3, 13):
                for x in range(3, 13): cv.set(x, y, mul((120, 96, 210), 1.3 - abs(x - 8) * .06 - (y - 3) * .03 + rng.uniform(-.03, .03)))
            cv.rect(3, 3, 12, 3, (30, 26, 50)); cv.rect(3, 12, 12, 12, (30, 26, 50)); cv.rect(3, 3, 3, 12, (30, 26, 50)); cv.rect(12, 3, 12, 12, (30, 26, 50))
            cv.line(5, 11, 10, 4, (230, 220, 255)); cv.set(10, 5, (255, 255, 255)); cv.rect(7, 7, 8, 8, (225, 140, 150))
    else:
        raise KeyError(sid)
    return cv

# ---------------- driver ----------------
def item_cv(name):
    rng = random.Random(name)
    if name == "butcher_cleaver": return draw_cleaver()
    if name.endswith("_knife"): return draw_knife(name, rng)
    if name.startswith("cooked_"): return draw_meat(name[7:], True, rng)
    try: return draw_meat(name, False, rng)
    except KeyError: return draw_misc(name, False, rng)

def station_img(sid, part): return station_cv(sid, part).img()
def station_cv(sid, part): return draw_station(sid, part, random.Random(sid + part))

def write_all(items, stations, out=OUT):
    os.makedirs(f"{out}/item", exist_ok=True); os.makedirs(f"{out}/block", exist_ok=True)
    for n in items: item_cv(n).img().save(f"{out}/item/{n}.png")
    for s in stations:
        for part in ("side", "top"): station_cv(s, part).img().save(f"{out}/block/{s}_{part}.png")

def contact_sheet(items, stations, path, scale=6):
    cols = 12; cells = [("item", n, item_cv(n).img()) for n in items] + [("block", f"{s}_{p}", station_cv(s, p).img()) for s in stations for p in ("side", "top")]
    rows = (len(cells) + cols - 1) // cols; pad = 2
    sheet = Image.new("RGBA", (cols * (16 * scale + pad), rows * (16 * scale + pad)), (70, 70, 74, 255))
    for i, (_, n, im) in enumerate(cells):
        sheet.alpha_composite(im.resize((16 * scale,) * 2, Image.NEAREST), ((i % cols) * (16 * scale + pad), (i // cols) * (16 * scale + pad)))
    os.makedirs(os.path.dirname(path), exist_ok=True); sheet.save(path)

STATIONS = ["drying_rack", "salting_barrel", "meat_grinder", "sausage_stuffer", "rendering_cauldron", "smokehouse", "steam_cannery", "cold_room", "vacuum_packer", "vat_grower", "stasis_case"]

if __name__ == "__main__":
    import sys
    items = sorted(f[:-4] for f in os.listdir(f"{OUT}/item") if f.endswith(".png"))
    write_all(items, STATIONS)
    if len(sys.argv) > 1: contact_sheet(items, STATIONS, sys.argv[1])
