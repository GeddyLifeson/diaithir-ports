"""64x procedural art engine in the Industrial Factory style: soft-lit steel, bevelled raised panels, rivets,
vents, hazard striping, warm ambient occlusion and grime. Light comes from the upper left. All shapes are drawn at
4x and averaged down, so edges are soft like the baked pack. Nothing here reads any existing asset."""
import math, random, hashlib
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

S = 64
SS = 4
N = S * SS
OUTL = (28, 26, 30)
STEEL = (124, 128, 134)
STEEL_L = (176, 181, 188)
STEEL_D = (68, 70, 76)
HAZ_Y = (212, 164, 38)
HAZ_K = (34, 32, 34)
RUST = (132, 70, 34)
BRASS = (196, 154, 66)
COPPER = (200, 108, 58)
ANDESITE = (136, 140, 132)
ZINC = (156, 166, 176)
WOOD = (150, 108, 62)
RUBBER = (44, 44, 50)
GLASS = (140, 196, 214)
GLOW = (255, 190, 80)


def rng(name):
    return random.Random(int(hashlib.md5(name.encode()).hexdigest()[:8], 16))


def sh(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3])


def mixc(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3))


def vnoise(name, n=S, cell=8):
    """periodic value noise in [0,1], n x n"""
    r = np.random.RandomState(int(hashlib.md5(name.encode()).hexdigest()[:8], 16))
    g = r.rand(cell, cell)
    xs = np.linspace(0, cell, n, endpoint=False)
    xi = xs.astype(int) % cell
    xf = xs - np.floor(xs)
    xf = xf * xf * (3 - 2 * xf)
    xj = (xi + 1) % cell
    a = g[np.ix_(xi, xi)]
    b = g[np.ix_(xi, xj)]
    c = g[np.ix_(xj, xi)]
    d = g[np.ix_(xj, xj)]
    fx = xf[None, :]
    fy = xf[:, None]
    return (a * (1 - fx) + b * fx) * (1 - fy) + (c * (1 - fx) + d * fx) * fy


class Cv:
    """A 64x64 tile drawn at 4x."""

    def __init__(self, base=None, alpha=False):
        self.im = Image.new("RGBA", (N, N), (0, 0, 0, 0) if alpha or base is None else tuple(base[:3]) + (255,))
        self.d = ImageDraw.Draw(self.im)

    def P(self, v):
        return v * SS

    def rect(self, x0, y0, x1, y1, c, a=255):
        self.d.rectangle([x0 * SS, y0 * SS, x1 * SS - 1, y1 * SS - 1], fill=tuple(c[:3]) + (a,))

    def poly(self, pts, c, a=255):
        self.d.polygon([(x * SS, y * SS) for x, y in pts], fill=tuple(c[:3]) + (a,))

    def ellipse(self, x0, y0, x1, y1, c, a=255):
        self.d.ellipse([x0 * SS, y0 * SS, x1 * SS - 1, y1 * SS - 1], fill=tuple(c[:3]) + (a,))

    def line(self, x0, y0, x1, y1, c, w=1.0, a=255):
        self.d.line([x0 * SS, y0 * SS, x1 * SS, y1 * SS], fill=tuple(c[:3]) + (a,), width=max(1, int(w * SS)))

    def outline_rect(self, x0, y0, x1, y1, c=OUTL, w=0.8, a=200):
        for (ax, ay, bx, by) in ((x0, y0, x1, y0), (x0, y1 - w, x1, y1 - w), (x0, y0, x0, y1), (x1 - w, y0, x1 - w, y1)):
            self.rect(min(ax, bx), min(ay, by), max(ax + w, bx), max(ay + w, by), c, a)

    def bevel(self, x0, y0, x1, y1, base, w=1.6, raised=True, outline=True, grad=True):
        lo, hi = (0.66, 1.34) if raised else (1.3, 0.7)
        if grad:
            arr = np.zeros((int((y1 - y0) * SS), int((x1 - x0) * SS), 3), dtype=np.float32)
            yy = np.linspace(1.06, 0.92, arr.shape[0])[:, None]
            xx = np.linspace(1.03, 0.97, arr.shape[1])[None, :]
            f = (yy * xx)[..., None]
            arr[:] = np.array(base[:3], dtype=np.float32)
            arr = np.clip(arr * f, 0, 255).astype(np.uint8)
            self.im.paste(Image.fromarray(arr, "RGB"), (int(x0 * SS), int(y0 * SS)))
        else:
            self.rect(x0, y0, x1, y1, base)
        L, D = sh(base, hi), sh(base, lo)
        self.poly([(x0, y0), (x1, y0), (x1 - w, y0 + w), (x0 + w, y0 + w)], L)
        self.poly([(x0, y0), (x0 + w, y0 + w), (x0 + w, y1 - w), (x0, y1)], sh(base, 1.0 + (hi - 1) * 0.7))
        self.poly([(x0, y1), (x0 + w, y1 - w), (x1 - w, y1 - w), (x1, y1)], D)
        self.poly([(x1, y0), (x1, y1), (x1 - w, y1 - w), (x1 - w, y0 + w)], sh(base, 1.0 - (1 - lo) * 0.7))
        if outline:
            self.outline_rect(x0, y0, x1, y1, OUTL, 0.7, 170)

    def rivet(self, cx, cy, r=1.5, base=STEEL):
        self.ellipse(cx - r - 0.5, cy - r - 0.3, cx + r + 0.5, cy + r + 0.7, OUTL, 150)
        self.ellipse(cx - r, cy - r, cx + r, cy + r, sh(base, 0.95))
        self.ellipse(cx - r * 0.6, cy - r * 0.7, cx + r * 0.3, cy + r * 0.2, sh(base, 1.45))

    def rivets4(self, x0, y0, x1, y1, inset=3.2, r=1.4, base=STEEL):
        for (x, y) in ((x0 + inset, y0 + inset), (x1 - inset, y0 + inset), (x0 + inset, y1 - inset), (x1 - inset, y1 - inset)):
            self.rivet(x, y, r, base)

    def vents(self, x0, y0, x1, y1, n=4, base=STEEL_D):
        h = (y1 - y0) / n
        for i in range(n):
            y = y0 + i * h + h * 0.25
            self.rect(x0, y, x1, y + h * 0.5, sh(base, 0.45))
            self.rect(x0, y + h * 0.5, x1, y + h * 0.62, sh(STEEL, 1.15))

    def hazard(self, x0, y0, x1, y1, step=5.0):
        tmp = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        td = ImageDraw.Draw(tmp)
        td.rectangle([x0 * SS, y0 * SS, x1 * SS - 1, y1 * SS - 1], fill=HAZ_Y + (255,))
        k = -2 * N
        while k < 3 * N:
            td.polygon([(k, N * 2), (k + step * SS, N * 2), (k + step * SS + N * 2, 0), (k + N * 2, 0)], fill=HAZ_K + (255,))
            k += step * 2 * SS
        mask = Image.new("L", (N, N), 0)
        ImageDraw.Draw(mask).rectangle([x0 * SS, y0 * SS, x1 * SS - 1, y1 * SS - 1], fill=255)
        self.im.paste(tmp, (0, 0), mask)
        self.outline_rect(x0, y0, x1, y1, OUTL, 0.7, 220)
        self.rect(x0, y0, x1, y0 + 0.8, (255, 230, 140), 90)

    def gauge(self, cx, cy, r, glow=None):
        self.ellipse(cx - r - 0.7, cy - r - 0.7, cx + r + 0.7, cy + r + 0.7, STEEL_D)
        self.ellipse(cx - r, cy - r, cx + r, cy + r, (214, 210, 196))
        self.ellipse(cx - r * 0.85, cy - r * 0.85, cx + r * 0.85, cy + r * 0.85, (236, 232, 216))
        self.line(cx, cy, cx + r * 0.55, cy - r * 0.45, (170, 40, 36), 0.9)
        self.ellipse(cx - 0.8, cy - 0.8, cx + 0.8, cy + 0.8, STEEL_D)

    def lamp(self, cx, cy, r, c=GLOW):
        self.ellipse(cx - r - 1.6, cy - r - 1.6, cx + r + 1.6, cy + r + 1.6, c, 60)
        self.ellipse(cx - r - 0.7, cy - r - 0.7, cx + r + 0.7, cy + r + 0.7, OUTL)
        self.ellipse(cx - r, cy - r, cx + r, cy + r, c)
        self.ellipse(cx - r * 0.6, cy - r * 0.7, cx + r * 0.1, cy, (255, 250, 220))

    def glass(self, x0, y0, x1, y1, tint=GLASS, a=210):
        self.bevel(x0 - 1.2, y0 - 1.2, x1 + 1.2, y1 + 1.2, STEEL_D, 1.0, False)
        self.rect(x0, y0, x1, y1, tint, a)
        self.poly([(x0 + 1, y1 - 1), (x0 + (x1 - x0) * 0.55, y0 + 1), (x0 + (x1 - x0) * 0.7, y0 + 1), (x0 + 3, y1 - 1)], (255, 255, 255), 80)

    def cyl_v(self, x0, y0, x1, y1, base, spec=1.5):
        """vertical cylinder face: lit from the upper left"""
        w = int((x1 - x0) * SS)
        h = int((y1 - y0) * SS)
        xs = np.linspace(-1, 1, w)
        prof = 0.62 + 0.55 * np.clip(1 - ((xs + 0.38) ** 2) * 0.9, 0, 1) ** 1.2
        prof = prof + 0.35 * np.exp(-((xs + 0.42) / 0.12) ** 2) * (spec - 1 + 0.5)
        prof[-int(w * 0.08):] *= 0.78
        arr = np.clip(np.array(base[:3], dtype=np.float32)[None, None, :] * prof[None, :, None], 0, 255)
        arr = np.repeat(arr, h, axis=0).astype(np.uint8)
        self.im.paste(Image.fromarray(arr, "RGB"), (int(x0 * SS), int(y0 * SS)))

    def cyl_h(self, x0, y0, x1, y1, base):
        w = int((x1 - x0) * SS)
        h = int((y1 - y0) * SS)
        ys = np.linspace(-1, 1, h)
        prof = 0.6 + 0.55 * np.clip(1 - ((ys + 0.4) ** 2) * 0.9, 0, 1) ** 1.2
        prof = prof + 0.3 * np.exp(-((ys + 0.45) / 0.12) ** 2)
        prof[-int(h * 0.08):] *= 0.78
        arr = np.clip(np.array(base[:3], dtype=np.float32)[None, None, :] * prof[:, None, None], 0, 255)
        arr = np.repeat(arr, w, axis=1).astype(np.uint8)
        self.im.paste(Image.fromarray(arr, "RGB"), (int(x0 * SS), int(y0 * SS)))

    def finish(self, name, grime=0.5, rust=0.0, brushed=0.06, ao=0.22, keep_alpha=False):
        im = self.im.resize((S, S), Image.LANCZOS)
        a = np.array(im).astype(np.float32)
        alpha = a[..., 3]
        rgb = a[..., :3]
        r = np.random.RandomState(int(hashlib.md5(name.encode()).hexdigest()[:8], 16))
        if brushed:
            streak = (r.rand(S, 1) - 0.5) * 2 * brushed
            fine = (r.rand(S, S) - 0.5) * brushed
            rgb *= (1 + streak + fine)[..., None]
        if ao:
            yy, xx = np.mgrid[0:S, 0:S]
            e = np.minimum(np.minimum(xx, S - 1 - xx), np.minimum(yy, S - 1 - yy)).astype(np.float32)
            edge = np.clip(1 - e / 5.0, 0, 1) ** 1.5
            warm = np.array([0.86, 0.72, 0.58], dtype=np.float32)
            rgb = rgb * (1 - ao * edge[..., None] * (1 - warm[None, None, :]) * 2.4)
        if grime:
            n = vnoise(name + "gr", S, 8) * 0.6 + vnoise(name + "g2", S, 16) * 0.4
            m = np.clip((n - 0.62) * 4, 0, 1) * grime
            rgb = rgb * (1 - m[..., None] * 0.32) + np.array([60, 44, 30], dtype=np.float32) * (m[..., None] * 0.12)
        if rust:
            n = vnoise(name + "rs", S, 6) * 0.7 + vnoise(name + "r2", S, 12) * 0.3
            yy = np.linspace(0.25, 1.0, S)[:, None]
            m = np.clip((n * yy - 0.52) * 7, 0, 1) * rust
            rgb = rgb * (1 - m[..., None]) + np.array(RUST, dtype=np.float32) * m[..., None] * (0.8 + 0.4 * r.rand(S, S)[..., None])
        out = np.dstack([np.clip(rgb, 0, 255), alpha]).astype(np.uint8)
        return Image.fromarray(out, "RGBA")


# -------------------------------------------------------------------------- tile designs
def plate_tex(name, base=STEEL, rivets=True, grime=0.5, rust=0.0, seams=True):
    c = Cv(base)
    c.bevel(0.5, 0.5, 63.5, 63.5, base, 2.4, True, True)
    if seams:
        c.bevel(5, 5, 59, 59, sh(base, 1.02), 1.2, False, True, False)
    if rivets:
        c.rivets4(0, 0, 64, 64, 3.0, 1.2, base)
    return c.finish(name, grime, rust)


def casing_tex(name, base, trim=STEEL_D):
    """Create-style casing: coloured plates framed by dark steel corner brackets"""
    c = Cv(trim)
    c.bevel(0.5, 0.5, 63.5, 63.5, trim, 2.0, True)
    for (x0, y0, x1, y1) in ((5, 5, 31, 31), (33, 5, 59, 31), (5, 33, 31, 59), (33, 33, 59, 59)):
        c.bevel(x0, y0, x1, y1, base, 1.6, True)
        c.rivets4(x0, y0, x1, y1, 3.4, 1.0, base)
    for (x, y) in ((3.2, 3.2), (60.8, 3.2), (3.2, 60.8), (60.8, 60.8)):
        c.rivet(x, y, 1.5, trim)
    return c.finish(name, 0.45, 0.0)


def machine_face(name, base=STEEL, accent=HAZ_Y, variant=None):
    """riveted steel housing with a function-specific insert; variant picks the layout"""
    r = rng(name)
    v = variant if variant is not None else r.randrange(5)
    c = Cv(sh(base, 0.78))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 0.82), 2.2, True)
    for (x, y) in ((3.2, 3.2), (60.8, 3.2), (3.2, 60.8), (60.8, 60.8)):
        c.rivet(x, y, 1.4, base)
    c.bevel(7, 7, 57, 57, base, 1.5, True)
    if v == 0:      # vents over a status strip
        c.bevel(12, 12, 52, 34, STEEL_D, 1.2, False)
        c.vents(15, 15, 49, 31, 4)
        c.rect(12, 40, 52, 44, sh(accent, 0.9)); c.outline_rect(12, 40, 52, 44, OUTL, 0.6, 200)
        c.lamp(18, 51, 2.0, accent); c.lamp(26, 51, 2.0, (110, 220, 130))
    elif v == 1:    # gauge and two plates
        c.gauge(32, 25, 11)
        c.bevel(12, 41, 30, 53, sh(base, 0.9), 1.0, False)
        c.bevel(34, 41, 52, 53, sh(base, 0.9), 1.0, False)
        c.rivet(21, 47, 1.3, base); c.rivet(43, 47, 1.3, base)
    elif v == 2:    # hazard band
        c.bevel(11, 11, 53, 40, sh(base, 0.9), 1.2, False)
        c.line(14, 20, 50, 20, STEEL_D, 1.2); c.line(14, 31, 50, 31, STEEL_D, 1.2)
        c.hazard(11, 45, 53, 53, 4.5)
    elif v == 3:    # window with lamp bar
        c.glass(14, 13, 50, 33, (90, 150, 170), 235)
        c.rect(14, 38, 50, 40, accent); c.lamp(20, 49, 2, accent); c.lamp(32, 49, 2, (220, 90, 60)); c.lamp(44, 49, 2, (110, 220, 130))
    else:           # louvres
        for i in range(5):
            y = 12 + i * 8.5
            c.rect(12, y, 52, y + 4.5, sh(STEEL_D, 0.5)); c.rect(12, y + 4.5, 52, y + 5.6, sh(STEEL, 1.2))
    return c.finish(name, 0.55, 0.12)


def machine_top(name, base=STEEL):
    c = Cv(sh(base, 0.8))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 0.86), 2.2, True)
    c.bevel(8, 8, 56, 56, sh(base, 0.95), 1.3, False)
    c.rivets4(0, 0, 64, 64, 3.2, 1.3, base)
    c.vents(16, 16, 48, 48, 6)
    return c.finish(name, 0.5, 0.1)


def shaft_side(name, base=STEEL, collar=BRASS):
    """full tile; the model uses the middle columns, brass collars are painted at the ends and middle"""
    c = Cv(None)
    c.cyl_v(0, 0, 64, 64, sh(base, 1.0))
    for (y0, y1) in ((0, 7), (28.5, 35.5), (57, 64)):
        c.cyl_v(0, y0, 64, y1, collar)
        c.rect(0, y0, 64, y0 + 0.7, sh(collar, 1.4))
        c.rect(0, y1 - 0.8, 64, y1, sh(collar, 0.55))
    for y in (13, 21, 43, 51):
        c.rect(0, y, 64, y + 0.9, sh(base, 0.55))
    return c.finish(name, 0.35, 0.0, 0.03, 0.0)


def shaft_end(name, base=STEEL):
    c = Cv(sh(base, 0.5))
    c.ellipse(10, 10, 54, 54, BRASS)
    c.ellipse(13, 13, 51, 51, sh(base, 0.8))
    c.ellipse(16, 16, 48, 48, sh(base, 1.15))
    c.bevel(24, 24, 40, 40, sh(base, 0.95), 1.4, False)
    return c.finish(name, 0.2, 0, 0.03, 0.0)


def metal_flat(name, base, grime=0.35, rust=0.0, pattern=None):
    c = Cv(base)
    c.bevel(0.5, 0.5, 63.5, 63.5, base, 3.0, True)
    if pattern == "gear":
        for i in range(4):
            c.line(4, 12 + i * 16, 60, 12 + i * 16, sh(base, 0.7), 1.2)
    return c.finish(name, grime, rust)


def planks(name, base=WOOD, band=True, vertical=True):
    c = Cv(sh(base, 0.8))
    r = rng(name)
    n = 4
    w = 64 / n
    for i in range(n):
        shade_f = 0.9 + r.random() * 0.25
        if vertical:
            c.bevel(i * w + 0.4, 0.4, (i + 1) * w - 0.4, 63.6, sh(base, shade_f), 1.1, True)
            for k in range(3):
                x = i * w + 4 + r.random() * (w - 8)
                c.line(x, 4 + k * 20, x + 0.3, 14 + k * 20, sh(base, 0.7), 0.7, 120)
        else:
            c.bevel(0.4, i * w + 0.4, 63.6, (i + 1) * w - 0.4, sh(base, shade_f), 1.1, True)
    if band:
        for y in (6, 52):
            c.bevel(0, y, 64, y + 7, STEEL_D, 1.0, True)
            c.rivets4(0, y, 64, y + 7, 3.0, 0.9, STEEL) if False else None
            c.rivet(8, y + 3.5, 1.1, STEEL); c.rivet(56, y + 3.5, 1.1, STEEL); c.rivet(32, y + 3.5, 1.1, STEEL)
    return c.finish(name, 0.45, 0.0, 0.0, 0.18)


def stone_tex(name, base, kind):
    """natural stone families used for the decorative blocks; same lighting, flat facets"""
    r = rng(name + kind)
    c = Cv(base)
    n1 = vnoise(name + "a", S, 6)
    n2 = vnoise(name + "b", S, 16)
    arr = np.zeros((S, S, 3), dtype=np.float32)
    var = 0.16 if kind != "polished" else 0.05
    f = 1 + (n1 - 0.5) * var * 2 + (n2 - 0.5) * var
    arr[:] = np.array(base[:3], dtype=np.float32)
    arr *= f[..., None]
    im = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGB").resize((N, N), Image.BICUBIC).convert("RGBA")
    c.im = im
    c.d = ImageDraw.Draw(im)
    mort = sh(base, 0.5)
    if kind == "cut":
        c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 1.0), 2.4, True, True, False)
    elif kind == "polished":
        c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 1.04), 3.0, True, True, False)
        c.poly([(8, 20), (22, 8), (30, 8), (8, 30)], (255, 255, 255), 28)
    elif kind in ("bricks", "small"):
        rows = 4 if kind == "bricks" else 8
        h = 64 / rows
        for ri in range(rows):
            off = (32 if kind == "bricks" else 16) * (ri % 2)
            bw = 32 if kind == "bricks" else 16
            for bx in range(-1, 6):
                x0 = bx * bw + off
                c.bevel(max(0, x0) + 0.6, ri * h + 0.6, min(64, x0 + bw) - 0.6, (ri + 1) * h - 0.6, sh(base, 0.92 + r.random() * 0.2), 1.0, True, True, False) if x0 + bw > 0 and x0 < 64 else None
    elif kind == "layered":
        for y in range(0, 64, 16):
            c.rect(0, y, 64, y + 1.6, sh(base, 0.6)); c.rect(0, y + 1.6, 64, y + 3.4, sh(base, 1.2))
    elif kind == "pillar_side":
        for x in (0, 21, 42):
            c.bevel(x + 0.5, 0, x + 21.5, 64, sh(base, 1.0), 2.0, True, True, False)
    elif kind == "pillar_top":
        c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 0.95), 2.4, True)
        c.bevel(10, 10, 54, 54, sh(base, 1.05), 2.0, False)
    return c.finish(name + kind, 0.5, 0.0, 0.0, 0.2)


def ore_tex(name, base, fleck):
    r = rng(name)
    c = Cv(base)
    arr = np.array(Image.new("RGB", (S, S), base[:3])).astype(np.float32)
    f = 1 + (vnoise(name, S, 6) - 0.5) * 0.4 + (vnoise(name + "x", S, 14) - 0.5) * 0.25
    arr *= f[..., None]
    c.im = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGB").resize((N, N), Image.BICUBIC).convert("RGBA")
    c.d = ImageDraw.Draw(c.im)
    for _ in range(9):
        x, y = 6 + r.random() * 52, 6 + r.random() * 52
        w, h = 3 + r.random() * 5, 3 + r.random() * 4
        pts = [(x + math.cos(a) * w * (0.6 + r.random() * 0.5), y + math.sin(a) * h * (0.6 + r.random() * 0.5)) for a in np.linspace(0, 6.28, 7)[:-1]]
        c.poly(pts, sh(fleck, 0.65))
        c.poly([(px_ - 0.8, py_ - 0.8) for px_, py_ in pts], fleck)
    return c.finish(name, 0.5, 0.0, 0.0, 0.2)


def cloth_tex(name, base):
    c = Cv(base)
    for i in range(0, 64, 4):
        c.rect(i, 0, i + 2, 64, sh(base, 1.12)); c.rect(0, i, 64, i + 2, sh(base, 0.9), 120)
    c.bevel(0.5, 0.5, 63.5, 63.5, base, 2.0, True, True, False)
    return c.finish(name, 0.25, 0.0, 0.0, 0.2)


def glass_tex(name, frame=STEEL, pattern="frame"):
    c = Cv(None, True)
    c.rect(4, 4, 60, 60, GLASS, 70)
    c.bevel(0.5, 0.5, 63.5, 63.5, frame, 3.0, True, True)
    # hollow the middle again
    mask = Image.new("L", (N, N), 0)
    ImageDraw.Draw(mask).rectangle([5 * SS, 5 * SS, 59 * SS, 59 * SS], fill=255)
    clear = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    inner = Image.new("RGBA", (N, N), GLASS + (70,))
    c.im.paste(clear, (0, 0), mask)
    c.im.paste(inner, (0, 0), mask)
    if pattern in ("h", "tiled"): c.bevel(5, 30.5, 59, 33.5, frame, 1.0, True)
    if pattern in ("v", "tiled"): c.bevel(30.5, 5, 33.5, 59, frame, 1.0, True)
    c.poly([(9, 28), (28, 9), (34, 9), (9, 34)], (255, 255, 255), 70)
    c.rivets4(0, 0, 64, 64, 3.0, 1.1, frame)
    return c.finish(name, 0.2, 0, 0.02, 0.0)


def shingle_tex(name, base):
    c = Cv(base)
    for row in range(4):
        y = row * 16
        off = 8 * (row % 2)
        for bx in range(-1, 5):
            x = bx * 16 + off
            c.bevel(x + 0.6, y + 0.6, x + 15.4, y + 15.4, sh(base, 0.95 + 0.1 * ((bx + row) % 3) / 2), 1.2, True)
    return c.finish(name, 0.35, 0.0, 0.0, 0.2)


def tile_tex(name, base):
    c = Cv(base)
    for (x, y) in ((0, 0), (32, 0), (0, 32), (32, 32)):
        c.bevel(x + 0.8, y + 0.8, x + 31.2, y + 31.2, base, 2.0, True)
        c.rivet(x + 4.5, y + 4.5, 1.0, base)
        c.rivet(x + 27.5, y + 27.5, 1.0, base)
    return c.finish(name, 0.35, 0.0, 0.0, 0.18)


def pipe_tex(name, base=COPPER):
    c = Cv(None)
    c.cyl_h(0, 0, 64, 64, base)
    for x in (2, 30, 62):
        c.rect(x - 1.4, 0, x + 1.4, 64, sh(base, 0.58)); c.rect(x - 1.4, 0, x - 0.4, 64, sh(base, 1.35), 150)
    return c.finish(name, 0.3, 0.0, 0.03, 0.0)


def crate_tex(name, base):
    c = Cv(sh(base, 0.85))
    c.bevel(0.5, 0.5, 63.5, 63.5, base, 2.2, True)
    c.line(0, 0, 64, 64, sh(base, 0.65), 1.4); c.line(64, 0, 0, 64, sh(base, 0.65), 1.4)
    c.rivets4(0, 0, 64, 64, 3.4, 1.2, STEEL)
    return c.finish(name, 0.35, 0.0, 0.0, 0.2)


def belt_top(name, shift=0):
    """rubber belt with steel edge rails and stitched cross ribs (travels along v)"""
    c = Cv(RUBBER)
    for y in range(0, 64, 8):
        yy = (y + shift) % 64
        c.bevel(8, yy + 0.5, 56, yy + 6.5, sh(RUBBER, 1.12), 1.0, True, False)
        for x in range(14, 52, 7):
            c.rect(x, yy + 2.6, x + 3.6, yy + 3.3, (150, 140, 110), 200)
    c.bevel(0, 0, 8, 64, STEEL, 1.6, True)
    c.bevel(56, 0, 64, 64, STEEL, 1.6, True)
    for y in (8, 24, 40, 56):
        c.rivet(4, y, 1.1, STEEL); c.rivet(60, y, 1.1, STEEL)
    return c.finish(name, 0.3, 0.0, 0.0, 0.0)


def drum_tex(name):
    c = Cv(STEEL_D)
    c.cyl_h(0, 4, 64, 60, sh(STEEL, 0.9))
    for x in (6, 58):
        c.rect(x - 1, 4, x + 1, 60, sh(STEEL_D, 0.8))
    return c.finish(name, 0.35, 0.0, 0.03, 0.0)


def fluid_tex(name, c0, c1, flowing=False):
    """tileable tinted liquid with soft ripples"""
    n = vnoise(name, S, 4) * 0.6 + vnoise(name + "2", S, 8) * 0.4
    arr = np.zeros((S, S, 3), dtype=np.float32)
    for i in range(3):
        arr[..., i] = c0[i] + (c1[i] - c0[i]) * n
    yy, xx = np.mgrid[0:S, 0:S]
    rip = 0.5 + 0.5 * np.sin((xx * 0.5 + yy * 0.2 + n * 6))
    arr *= (0.9 + 0.2 * rip)[..., None]
    a = np.full((S, S), 215, dtype=np.float32)
    return Image.fromarray(np.dstack([np.clip(arr, 0, 255), a]).astype(np.uint8), "RGBA")


# -------------------------------------------------------------------------- emblems for machine faces
def _rot(pts, cx, cy, ang):
    a = math.radians(ang)
    return [(cx + (x - cx) * math.cos(a) - (y - cy) * math.sin(a), cy + (x - cx) * math.sin(a) + (y - cy) * math.cos(a)) for x, y in pts]


def emblem(c, kind, cx=32, cy=30, r=17):
    """round recessed plate with a function symbol; light from the upper left"""
    c.ellipse(cx - r - 1.2, cy - r - 1.2, cx + r + 1.2, cy + r + 1.2, OUTL, 200)
    c.ellipse(cx - r, cy - r, cx + r, cy + r, sh(STEEL_D, 0.62))
    c.ellipse(cx - r + 1.4, cy - r + 2.2, cx + r - 0.6, cy + r - 0.4, sh(STEEL_D, 0.5))
    if kind == "fan":
        for k in range(4):
            blade = _rot([(cx + 1.5, cy - 2.5), (cx + r - 3, cy - 6.5), (cx + r - 2, cy - 1), (cx + 1.5, cy + 1.5)], cx, cy, k * 90)
            c.poly([(x + 0.7, y + 0.8) for x, y in blade], (20, 20, 24))
            c.poly(blade, sh(STEEL_L, 0.95 - 0.08 * (k % 2)))
        c.ellipse(cx - 3.6, cy - 3.6, cx + 3.6, cy + 3.6, BRASS); c.ellipse(cx - 1.4, cy - 1.4, cx + 1.4, cy + 1.4, STEEL_D)
    elif kind == "saw":
        pts = []
        for i in range(32):
            rr = r - 1.6 if i % 2 == 0 else r - 4.2
            a = math.radians(i * 11.25)
            pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
        c.poly([(x + 0.6, y + 0.7) for x, y in pts], (20, 20, 24))
        c.poly(pts, STEEL_L)
        c.ellipse(cx - r + 6, cy - r + 6, cx + r - 6, cy + r - 6, sh(STEEL_L, 0.8))
        c.ellipse(cx - 3.5, cy - 3.5, cx + 3.5, cy + 3.5, STEEL_D); c.ellipse(cx - 1.6, cy - 1.6, cx + 1.6, cy + 1.6, BRASS)
    elif kind == "drill":
        c.poly([(cx - 9, cy - r + 3), (cx + 9, cy - r + 3), (cx + 2.6, cy + r - 2), (cx - 2.6, cy + r - 2)], STEEL_L)
        for i in range(5):
            y = cy - r + 6 + i * 5.2
            c.line(cx - 9 + i * 1.5, y, cx + 9 - i * 1.5, y + 3.5, STEEL_D, 1.4)
        c.rect(cx - 9, cy - r + 3, cx + 9, cy - r + 5, BRASS)
    elif kind in ("gauge", "gauge_stress"):
        c.ellipse(cx - r + 3, cy - r + 3, cx + r - 3, cy + r - 3, (222, 218, 202))
        c.ellipse(cx - r + 4.5, cy - r + 4.5, cx + r - 4.5, cy + r - 4.5, (240, 236, 220))
        for i in range(9):
            a = math.radians(200 + i * 17.5)
            c.line(cx + (r - 6) * math.cos(a), cy + (r - 6) * math.sin(a), cx + (r - 8.5) * math.cos(a), cy + (r - 8.5) * math.sin(a), (60, 56, 56), 0.9)
        needle = (40, 90, 200) if kind == "gauge_stress" else (190, 40, 36)
        c.line(cx, cy + 2, cx + 7, cy - 6, needle, 1.4)
        c.ellipse(cx - 1.6, cy - 1.0, cx + 1.6, cy + 2.2, STEEL_D)
    elif kind == "millstone":
        c.ellipse(cx - r + 2, cy - r + 2, cx + r - 2, cy + r - 2, (150, 148, 140))
        for k in range(8):
            c.line(cx, cy, cx + (r - 3) * math.cos(math.radians(k * 45 + 10)), cy + (r - 3) * math.sin(math.radians(k * 45 + 10)), (100, 98, 92), 1.3)
        c.ellipse(cx - 5, cy - 5, cx + 5, cy + 5, (118, 116, 110)); c.ellipse(cx - 2.4, cy - 2.4, cx + 2.4, cy + 2.4, STEEL_D)
    elif kind == "pump":
        c.ellipse(cx - r + 3, cy - r + 3, cx + r - 3, cy + r - 3, sh(COPPER, 0.9)); c.ellipse(cx - r + 7, cy - r + 7, cx + r - 7, cy + r - 7, sh(STEEL_D, 0.6))
        c.poly([(cx - 7, cy - 1), (cx + 1, cy - 1), (cx + 1, cy - 5), (cx + 8, cy + 1), (cx + 1, cy + 7), (cx + 1, cy + 3), (cx - 7, cy + 3)], (110, 190, 230))
    elif kind == "spout":
        c.poly([(cx - 10, cy - 12), (cx + 10, cy - 12), (cx + 3, cy - 2), (cx - 3, cy - 2)], STEEL_L)
        c.rect(cx - 3, cy - 2, cx + 3, cy + 3, sh(STEEL_L, 0.8))
        c.poly([(cx, cy + 4), (cx + 4.5, cy + 11), (cx - 4.5, cy + 11)], (110, 190, 230)); c.ellipse(cx - 4.5, cy + 8, cx + 4.5, cy + 13.5, (110, 190, 230))
    elif kind == "drain":
        c.ellipse(cx - r + 3, cy - r + 3, cx + r - 3, cy + r - 3, sh(STEEL, 0.9))
        for i in range(-3, 4):
            c.rect(cx - 10, cy + i * 3.6 - 0.8, cx + 10, cy + i * 3.6 + 0.8, (14, 14, 16))
    elif kind == "tank":
        c.rect(cx - 11, cy - 11, cx + 11, cy + 11, (80, 150, 190), 210); c.rect(cx - 11, cy, cx + 11, cy + 11, (60, 120, 170), 230)
        c.outline_rect(cx - 11, cy - 11, cx + 11, cy + 11, OUTL, 1.0, 255)
    elif kind == "crank":
        c.ellipse(cx - 12, cy - 12, cx + 12, cy + 12, sh(BRASS, 0.9)); c.ellipse(cx - 9, cy - 9, cx + 9, cy + 9, sh(STEEL_D, 0.6))
        c.line(cx - 10, cy, cx + 10, cy, BRASS, 2.4); c.line(cx, cy - 10, cx, cy + 10, BRASS, 2.4)
        c.ellipse(cx - 3, cy - 3, cx + 3, cy + 3, STEEL_L)
    elif kind == "whisk":
        for k in (-8, -4, 0, 4, 8):
            c.line(cx + k, cy - 4, cx + k * 1.7, cy - 14, STEEL_L, 1.2)
            c.line(cx + k, cy - 4, cx + k * 0.9, cy + 11, STEEL_L, 1.2)
        c.ellipse(cx - 12, cy - 5, cx + 12, cy + 3, STEEL_L, 0)
        c.rect(cx - 10, cy - 5, cx + 10, cy - 3.2, STEEL_L)
        c.rect(cx - 3, cy + 7, cx + 3, cy + 13, BRASS)
    elif kind == "press":
        c.rect(cx - 12, cy - 12, cx + 12, cy - 5, STEEL_L); c.rect(cx - 3, cy - 5, cx + 3, cy + 5, sh(STEEL_L, 0.85))
        c.poly([(cx - 11, cy + 5), (cx + 11, cy + 5), (cx + 11, cy + 11), (cx - 11, cy + 11)], BRASS)
    elif kind == "cog":
        pts = []
        for i in range(16):
            rr = r - 3 if i % 2 == 0 else r - 7
            a = math.radians(i * 22.5)
            pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
        c.poly(pts, BRASS); c.ellipse(cx - 4, cy - 4, cx + 4, cy + 4, sh(STEEL_D, 0.6))


def machine_emblem_face(name, emblem_kind, base=STEEL, accent=HAZ_Y):
    c = Cv(sh(base, 0.78))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 0.82), 2.2, True)
    for (x, y) in ((3.2, 3.2), (60.8, 3.2), (3.2, 60.8), (60.8, 60.8)):
        c.rivet(x, y, 1.4, base)
    c.bevel(6, 6, 58, 58, base, 1.5, True)
    emblem(c, emblem_kind)
    c.rect(14, 51, 50, 54, sh(accent, 0.9)); c.outline_rect(14, 51, 50, 54, OUTL, 0.6, 200)
    c.lamp(20, 58, 1.6, accent); c.lamp(44, 58, 1.6, (110, 220, 130))
    return c.finish(name, 0.55, 0.12)
