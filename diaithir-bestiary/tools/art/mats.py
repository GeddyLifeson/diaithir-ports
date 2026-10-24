"""Materials: each factory returns fn(face) that fills a whole face (base colour ramp, tone, relief height)."""
import math

import numpy as np

from pix import N, ramp, ramp_of, fbm, vnoise, voronoi, blur, dilate, erode
import motifs as M


def _grad(f):
    return (f.gy / max(1, f.h - 1))


def flat(base, tone=0.6, lo=0.40, hi=1.40):
    rp = ramp(base, lo, hi)

    def fn(f):
        f.paint(None, rp, tone, 0.0)
    fn.rp = rp
    return fn


def stone(base=(116, 118, 114), moss=0.0, crack=1.0, cell=15, tone=0.56, lo=0.48, hi=1.3, tint=None):
    """Standing-stone: mottled grey, carved joints, pits and optional lichen/moss in the low places."""
    rp = ramp(base, lo, hi)
    mrp = ramp((70, 108, 50), 0.42, 1.3)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 9, s, 3)
        k = max(2, int(h * w / (cell * cell)))
        cid, d1, d2 = voronoi(h, w, k, s + 7)
        e = d2 - d1
        off = np.random.default_rng(s + 9).uniform(-0.07, 0.07, k + 1)
        t = tone + 0.20 * (n - 0.5) * 2 + off[cid]
        H = (n - 0.5) * 1.2 + off[cid] * 6
        f.paint(None, rp, t, H)
        if crack:
            cr = e < 1.25
            f.paint(cr, rp, t - 0.2, H - 1.1)
        pit = fbm(h, w, 3, s + 5, 1) > 0.80
        f.paint(pit, rp, t - 0.18, H - 0.6)
        if moss:
            mm = fbm(h, w, 7, s + 3, 3) + 0.45 * _grad(f) ** 1.5 + 0.18 * (e < 2.4) > 1.0 - moss
            f.paint(mm, mrp, 0.45 + 0.3 * fbm(h, w, 4, s + 11, 2), H * 0.6 + 0.5)
    fn.rp = rp
    return fn


def metal_hammered(rp, cell=6.0, dome=0.55, tone=0.58, streak=True):
    """Bronze or gold sheet: clean hammer facets, a soft diagonal sheen and a few bright streaks."""
    def fn(f):
        h, w, s = f.h, f.w, f.seed
        k = max(3, int(h * w / (cell * cell)))
        cid, d1, d2 = voronoi(h, w, k, s + 3)
        r = np.random.default_rng(s + 4).uniform(-0.012, 0.012, k + 1)
        H = dome * (1 - np.clip(d1 / (cell * 0.8), 0, 1) ** 2)
        diag = ((f.gx / max(1, w)) * 0.5 + (f.gy / max(1, h)) * 0.5)
        sheen = 0.06 * np.cos(diag * 5.0 + (f.seed % 7))
        f.paint(None, rp, tone + r[cid] + sheen, H)
        if streak and w > 8 and h > 8:
            rr = np.random.default_rng(s + 8)
            for _ in range(max(1, int(w * h / 500))):
                x, y = rr.uniform(0, w), rr.uniform(0, h)
                L = rr.uniform(2, 5)
                f.paint(f.line([(x, y), (x + L, y - L * 0.6)], 1.0, ss=1), rp, 0.92, None)
    fn.rp = rp
    return fn


def plate(base=(92, 98, 104), panel=(6, 6), rivets=True, rust=0.25, wear=True, seam=1.0, tone=0.58, lo=0.42, hi=1.38,
          rustc=(128, 66, 32), grime=0.5, scratch=4):
    """Industrial painted steel plate: panel seams, rivets, scratches, edge wear, rust blooms and soot low down."""
    rp = ramp(base, lo, hi)
    rr = ramp(rustc, 0.4, 1.35)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 14, s, 3)
        t = tone + 0.10 * (n - 0.5) * 2 + 0.10 * (0.5 - _grad(f)) * grime
        f.paint(None, rp, t, 0.0)
        pw, ph = int(panel[0] * 4), int(panel[1] * 4)
        if seam and pw > 2 and ph > 2:
            for x in range(pw, w - 1, pw):
                f.paint(f.rect(x, 0, x + 1, h), rp, 0.12, -1.0)
                f.paint(f.rect(x + 1, 0, x + 2, h), rp, t[:, :1].mean() + 0.14, 0.2)
            for y in range(ph, h - 1, ph):
                f.paint(f.rect(0, y, w, y + 1), rp, 0.12, -1.0)
                f.paint(f.rect(0, y + 1, w, y + 2), rp, t.mean() + 0.14, 0.2)
        if rivets and pw > 6 and ph > 6:
            for x0 in range(0, w, pw):
                for y0 in range(0, h, ph):
                    for (cx, cy) in ((x0 + 2, y0 + 2), (x0 + pw - 3, y0 + 2), (x0 + 2, y0 + ph - 3), (x0 + pw - 3, y0 + ph - 3)):
                        if 1 <= cx < w - 1 and 1 <= cy < h - 1:
                            M.rivet(f, cx, cy, rp)
        if rust:
            rm = (fbm(h, w, 6, s + 5, 3) + 0.5 * _grad(f) ** 2 > 1.05 - rust)
            f.paint(rm, rr, 0.45 + 0.25 * fbm(h, w, 3, s + 6, 2), 0.3)
        if scratch:
            M.scratches(f, rp, scratch, tone=0.86)
    fn.rp = rp
    return fn


def paint_plate(color, **kw):
    kw.setdefault("rust", 0.18)
    return plate(color, **kw)


def brass(base=(184, 142, 62), panel=(5, 5), **kw):
    return plate(base, panel=panel, rust=0.0, grime=0.7, lo=0.38, hi=1.5, scratch=6, tone=0.6, **kw)


def chrome(base=(178, 186, 196), ringlen=6):
    """A cylinder: horizontal banding across the width, grooves along the length."""
    rp = ramp(base, 0.32, 1.35)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        x = (f.gx + 0.5) / w
        cyl = 0.30 + 0.62 * np.sin(np.pi * np.clip(x * 0.85 + 0.04, 0, 1)) ** 1.2
        t = cyl + 0.05 * (fbm(h, w, 8, s, 2) - 0.5)
        f.paint(None, rp, t, np.sin(np.pi * x) * 1.2)
        for y in range(2, h - 1, int(ringlen * 4)):
            f.paint(f.rect(0, y, w, y + 1), rp, 0.1, -1.0)
            f.paint(f.rect(0, y + 1, w, y + 2), rp, 0.85, 0.4)
    fn.rp = rp
    return fn


def cloth(base, fold=2.5, weave=True, tone=0.58, lo=0.42, hi=1.38, vertical=True, amp=0.16):
    """Woven cloth with soft folds. fold = fold spacing in model units."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        P = f.P * 4.0 if vertical else f.gy / 1.0
        sp = fold * 4
        ph = fbm(1, w + 400, 40, s, 2)[0, :w] if vertical else 0
        fx = np.sin(2 * math.pi * (P / sp + 0.35 * (vertical and fbm(h, w, 20, s + 2, 2))))
        t = tone + amp * fx * (0.5 + 0.5 * _grad(f)) + 0.05 * (fbm(h, w, 18, s + 3, 2) - 0.5)
        H = 0.6 * fx
        if weave:
            wv = ((f.gx + f.gy) % 2 == 0)
            t = t + 0.035 * wv
        f.paint(None, rp, t, H)
    fn.rp = rp
    return fn


def fur(base, clump=3.0, tone=0.58, lo=0.42, hi=1.4, contrast=0.2, steps=3):
    """Short soft fur: stretched noise quantised into a few tones, with a lighter tip."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = vnoise(h, w, clump * 4, s, aspect=2.4)
        q = np.floor(n * steps) / steps
        t = tone + contrast * (q - 0.5) * 2 + 0.08 * (0.5 - _grad(f))
        f.paint(None, rp, t, q * 0.8)
    fn.rp = rp
    return fn


def skin(base, tone=0.6, lo=0.42, hi=1.4, mottle=0.02, flush=None):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 5, s, 2)
        f.paint(None, rp, tone + mottle * (n - 0.5) * 2, 0.15 * n)
    fn.rp = rp
    return fn


def bark(base=(96, 68, 44), lo=0.4, hi=1.35, crack=0.55):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = vnoise(h, w, 6, s, aspect=5.0)
        n2 = vnoise(h, w, 14, s + 4, aspect=7.0)
        k = max(2, w // 7)
        cid, d1, d2 = voronoi(1, w * 3, k * 3, s + 1)
        t = 0.55 + 0.22 * (n - 0.5) * 2 + 0.1 * (n2 - 0.5)
        H = (n - 0.5) * 2
        f.paint(None, rp, t, H)
        ridge = (np.abs(n2 - 0.5) < 0.04)
        f.paint(ridge, rp, t - 0.25, H - 1.2)
        kn = fbm(h, w, 4, s + 9, 1) > 0.82
        f.paint(kn, rp, 0.2, -1.0)
    fn.rp = rp
    return fn


def scales(base, size=4.0, tone=0.58, lo=0.38, hi=1.45, shine=0.9, outline=True):
    """Overlapping rounded scales in staggered rows (size in model pixels... texels)."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w = f.h, f.w
        sz = size
        row = np.floor(f.gy / (sz * 0.75)).astype(int)
        xoff = np.where(row % 2 == 0, 0.0, sz / 2)
        col = np.floor((f.gx + xoff) / sz).astype(int)
        cx = (col + 0.5) * sz - xoff
        cy = (row + 0.5) * sz * 0.75 + sz * 0.1
        d = np.hypot(f.gx + 0.5 - cx, (f.gy + 0.5 - cy) * 1.1) / (sz * 0.62)
        dome = np.clip(1 - d, 0, 1)
        r = np.random.default_rng(f.seed)
        var = (np.sin(col * 12.9898 + row * 78.233) * 43758.5453) % 1.0
        t = tone + (var - 0.5) * 0.10 + 0.05 * (0.5 - f.gy / max(1, h))
        f.paint(None, rp, t, dome * 1.2)
        if outline:
            edge = d > 0.86
            f.paint(edge, rp, t - 0.26, -0.4)
        # a glint on the upper left of each scale
        gl = (np.hypot(f.gx + 0.5 - (cx - sz * 0.16), f.gy + 0.5 - (cy - sz * 0.18)) < sz * 0.1) & (var > 0.4)
        f.paint(gl, rp, 0.95, 1.2, nolight=True)
    fn.rp = rp
    return fn


def cel(base, tone=0.6, grad=0.14, lo=0.40, hi=1.4, bands=3, relief=0.0):
    """Cartoon surface: a soft top-to-bottom gradient snapped into a few flat bands."""
    rp = ramp(base, lo, hi)

    def fn(f):
        g = _grad(f)
        t = tone + grad * (0.5 - g)
        t = np.round(t * bands * 2) / (bands * 2) if bands else t
        f.paint(None, rp, t, 0.0)
    fn.rp = rp
    return fn


def charred(base=(46, 38, 38), glow=M.GLOW_AMBER, cracks=0.5, cell=16):
    """Cracked basalt with glowing seams."""
    rp = ramp(base, 0.5, 1.6)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        k = max(3, int(h * w / (cell * cell)))
        cid, d1, d2 = voronoi(h, w, k, s + 3)
        e = d2 - d1
        off = np.random.default_rng(s + 4).uniform(-0.06, 0.06, k + 1)
        n = fbm(h, w, 8, s, 3)
        t = 0.52 + 0.16 * (n - 0.5) * 2 + off[cid] + 0.06 * (0.5 - _grad(f))
        f.paint(None, rp, t, (n - 0.5) + off[cid] * 6)
        seam = e < 1.3
        seam &= fbm(h, w, 14, s + 9, 2) < 0.4 + cracks * 0.55
        f.paint(seam, glow, 0.5 + 0.3 * n, -1.0, emit=True)
    fn.rp = rp
    return fn


def pale(base=(214, 208, 200), veins=True, tone=0.6, lo=0.45, hi=1.2, tint=(110, 120, 150)):
    """Waxy, bloodless skin with faint veins and wrinkles."""
    rp = ramp(base, lo, hi, cool=(-22, -12, 16), warm=(8, 4, -6))
    vr = ramp(tint, 0.5, 1.2)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 10, s, 3)
        f.paint(None, rp, tone + 0.07 * (n - 0.5) * 2 + 0.05 * (0.5 - _grad(f)), 0.2 * n)
        if veins:
            v = fbm(h, w, 7, s + 5, 3)
            m = np.abs(v - 0.5) < 0.022
            f.paint(m, vr, 0.45, -0.3)
    fn.rp = rp
    return fn


def burlap(base=(150, 122, 84), lo=0.4, hi=1.35, tone=0.58, stitch=(40, 32, 26)):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        wx = (f.gx % 2 == 0) ^ (f.gy % 4 < 2)
        t = tone + 0.06 * wx + 0.10 * (fbm(h, w, 10, s, 3) - 0.5)
        f.paint(None, rp, t, wx * 0.5 + 0.0)
    fn.rp = rp
    return fn


def bone(base=(206, 196, 170), lo=0.42, hi=1.25):
    rp = ramp(base, lo, hi, cool=(-16, -10, 6), warm=(10, 8, -6))

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 12, s, 3)
        t = 0.62 + 0.12 * (n - 0.5) * 2 - 0.12 * _grad(f) ** 2
        f.paint(None, rp, t, 0.4 * n)
        cr = np.abs(fbm(h, w, 9, s + 4, 2) - 0.5) < 0.018
        f.paint(cr, rp, 0.22, -0.8)
    fn.rp = rp
    return fn


def flesh(base=(122, 48, 48), lo=0.35, hi=1.5):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 7, s, 3)
        t = 0.52 + 0.28 * (n - 0.5) * 2
        f.paint(None, rp, t, n * 1.0)
        gl = (n > 0.72)
        f.paint(gl, rp, 0.93, 1.2, nolight=True)
    fn.rp = rp
    return fn


def glass(rp, tone=0.55):
    def fn(f):
        g = _grad(f)
        f.paint(None, rp, tone + 0.2 * (0.5 - g), 0.0)
    fn.rp = rp
    return fn


def glow(rp, tone=0.62, core=True):
    """A fully emissive surface with a bright core."""
    def fn(f):
        f.paint(None, rp, tone, 0.0, emit=True)
        if core and f.w > 4 and f.h > 4:
            f.paint(f.rect(f.w * 0.25, f.h * 0.25, f.w * 0.75, f.h * 0.75), rp, 0.95, 0.4, emit=True, nolight=True)
    fn.rp = rp
    return fn


def hair(base, tone=0.56, lo=0.38, hi=1.45, strand=3.0, steps=3, contrast=0.22):
    """Vertical strands in a few clean tones with the odd bright highlight strand."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = vnoise(h, w, strand, s, aspect=7.0)
        q = np.floor(n * steps) / steps
        t = tone + contrast * (q - 0.5) * 2 + 0.10 * (0.5 - f.gy / max(1, h))
        f.paint(None, rp, t, q * 0.8)
        hl = (vnoise(h, w, strand * 1.6, s + 5, aspect=9.0) > 0.78)
        f.paint(hl, rp, 0.92, 0.9)
    fn.rp = rp
    return fn


def foliage(base, size=8.0, tone=0.58, cut=True, jitter=1.0, lo=0.38, hi=1.45, vein=True):
    """Overlapping hanging leaves; with cut=True the gaps between leaves are transparent (ragged hems)."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        r = np.random.default_rng(s)
        f.paint(None, rp, tone - 0.1, 0.0)
        if cut:
            f.paint(None, rp, cut=True)
        rows = int(h / (size * 0.55)) + 3
        for row in range(rows):
            y = row * size * 0.55 - size * 0.3
            xoff = (row % 2) * size / 2
            for x in np.arange(-size, w + size, size * 0.95):
                cx = x + xoff + r.uniform(-jitter, jitter)
                cy = y + r.uniform(-jitter, jitter)
                pts = [(cx, cy - size * 0.32), (cx + size * 0.46, cy + size * 0.05), (cx + size * 0.2, cy + size * 0.55),
                       (cx, cy + size * 0.78), (cx - size * 0.2, cy + size * 0.55), (cx - size * 0.46, cy + size * 0.05)]
                m = f.poly(pts)
                tn = tone + r.uniform(-0.12, 0.14)
                f.paint(m, rp, tn, 0.7)
                f.paint(m & (f.gx < cx - size * 0.05), rp, tn + 0.1, 0.9)
                if vein:
                    f.paint(f.line([(cx, cy - size * 0.15), (cx, cy + size * 0.6)], 1.0, ss=1) & m, rp, tn - 0.2, 0.2)
        # leaves cut at the very top row stay solid so the cloak keeps its top edge
        f.paint(f.rect(0, 0, w, 2), rp, tone - 0.05, 0.4)
    fn.rp = rp
    return fn


def horn(base=(198, 184, 150), ring=3.0, lo=0.36, hi=1.3):
    """Ridged keratin: horizontal growth rings with a lit left edge."""
    rp = ramp(base, lo, hi, cool=(-18, -10, 4), warm=(10, 8, -6))

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        k = np.sin(f.gy / ring * math.pi) * 0.5 + 0.5
        t = 0.55 + 0.16 * k - 0.1 * (f.gy / max(1, h)) + 0.05 * (fbm(h, w, 8, s, 2) - 0.5)
        f.paint(None, rp, t, k * 0.8)
    fn.rp = rp
    return fn


def leather(base=(120, 80, 48), stitch=True, tone=0.56, lo=0.36, hi=1.35):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 7, s, 3)
        f.paint(None, rp, tone + 0.07 * (n - 0.5) * 2 + 0.06 * (0.5 - _grad(f)), n * 0.5)
        if stitch and w > 8 and h > 8:
            for xx in range(3, w - 3, 4):
                f.paint(f.rect(xx, 2, xx + 2, 3), rp, 0.86, 0.3)
                f.paint(f.rect(xx, h - 3, xx + 2, h - 2), rp, 0.86, 0.3)
    fn.rp = rp
    return fn


def moon(rp, tone=0.7):
    """A glowing moon surface: bright body with darker craters."""
    def fn(f):
        n = fbm(f.h, f.w, 6, f.seed, 2)
        f.paint(None, rp, tone + 0.10 * (n - 0.5) * 2 + 0.12 * (0.5 - f.gx / max(1, f.w)), 0.0, emit=True)
        M.craters(f, rp, n=max(1, f.w * f.h // 150))
    fn.rp = rp
    return fn


def flame(rp, cut_top=True, tone_hot=0.95, tone_cool=0.42):
    """Emissive flame: hot at the bottom, cooler and ragged at the tip."""
    def fn(f):
        h, w, s = f.h, f.w, f.seed
        g = f.gy / max(1, h - 1)
        wob = vnoise(h, w, 5, s, aspect=2.0)
        t = tone_hot - (tone_hot - tone_cool) * np.clip(g * 0.9 + (wob - 0.5) * 0.5, 0, 1)
        t = 1.0 - (1.0 - t)
        t = np.flip(t, axis=0) if False else t
        f.paint(None, rp, np.clip(tone_cool + (tone_hot - tone_cool) * (g * 0.85 + (wob - 0.5) * 0.3), 0, 1), 0.0, emit=True)
        if cut_top and h > 6:
            edge = np.abs(f.gx - w / 2) / (w / 2 + 0.01)
            f.paint((f.gy < (1 - g) * 0) | (edge > 1.0 - (1 - g) * 0.0) & False, rp, cut=True)
            top = f.gy < (wob * h * 0.25) * (np.abs(f.gx - w / 2) / (w / 2 + 1) + 0.2)
            f.paint(top, rp, cut=True)
    fn.rp = rp
    return fn


def fin(base, ribs=4.0, lo=0.4, hi=1.5, cut=True):
    """A thin membrane with radiating ribs and a ragged edge."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        rib = (np.sin(f.gx / ribs * math.pi * 2) > 0.65)
        t = 0.62 - 0.22 * (f.gy / max(1, h)) + 0.08 * rib
        f.paint(None, rp, t, rib * 0.6)
        if cut:
            edge = f.gy > h - 1 - (np.abs(np.sin(f.gx / ribs * math.pi)) * 3.0)
            f.paint(edge, rp, cut=True)
    fn.rp = rp
    return fn


def tread(base=(40, 42, 46), lo=0.4, hi=1.5, lug=4):
    """Rubber and steel track pad: vertical cleats."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        k = (f.gx // lug) % 2
        f.paint(None, rp, 0.5 + 0.1 * k, k * 0.9)
        f.paint(f.rect(0, 0, w, 2), rp, 0.78, 0.8)
    fn.rp = rp
    return fn


def gear(base=(180, 110, 60)):
    rp = ramp(base, 0.36, 1.5)

    def fn(f):
        h, w = f.h, f.w
        f.paint(None, rp, 0.5, 0.0)
        cx, cy = w / 2, h / 2
        R = min(w, h) / 2
        d = np.hypot(f.gx + .5 - cx, f.gy + .5 - cy)
        ang = np.arctan2(f.gy + .5 - cy, f.gx + .5 - cx)
        teeth = (np.cos(ang * 8) > 0)
        body = d < R * 0.86
        tooth = (d < R * 0.99) & teeth
        f.paint(~(body | tooth), rp, cut=True)
        f.paint(body | tooth, rp, 0.6, 0.6)
        f.paint((d < R * 0.55) & (d > R * 0.3), rp, 0.3, -0.8)
        f.paint(d <= R * 0.3, rp, 0.72, 1.0)
        f.paint(d <= R * 0.1, rp, 0.15, -0.8)
    fn.rp = rp
    return fn


def shroud(base, tone=0.5, rag=7, fold=2.0, lo=0.34, hi=1.6, hem=True, thread=True):
    """Funeral cloth: dark folds and a ragged torn hem (transparent between the tatters)."""
    rp = ramp(base, lo, hi, cool=(-10, -6, 18))

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        P = f.P * 4.0
        fx = np.sin(2 * math.pi * (P / (fold * 4) + 0.5 * fbm(h, w, 14, s + 2, 2)))
        t = tone + 0.15 * fx * (0.4 + 0.6 * _grad(f)) + 0.04 * (fbm(h, w, 9, s, 2) - 0.5)
        f.paint(None, rp, t, 0.5 * fx)
        if hem and f.name in ("north", "south", "east", "west") and h > rag * 2:
            r = np.random.default_rng(s + 21)
            depth = np.clip((r.random(w) ** 2.2) * rag * 1.2, 0, rag)
            depth = np.maximum(depth, np.roll(depth, 1) * 0.6)
            cutm = f.gy >= (h - depth[f.gx])
            f.paint(cutm, rp, cut=True)
        if thread:
            tr = (np.abs(fbm(h, w, 9, s + 33, 2) - 0.5) < 0.012)
            f.paint(tr, rp, 0.2, -0.2)
    fn.rp = rp
    return fn


def gauze(base=(190, 190, 196), holes=0.35):
    """A torn veil: a loose weave with holes."""
    rp = ramp(base, 0.4, 1.2)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        wv = ((f.gx % 3 == 0) | (f.gy % 3 == 0))
        n = fbm(h, w, 5, s, 2)
        f.paint(None, rp, 0.62, 0.0)
        f.paint(~wv | (n > 1 - holes), rp, cut=True)
        f.paint(wv & (n < 0.5), rp, 0.5, 0.2)
    fn.rp = rp
    return fn


def straw(base=(214, 184, 90), lo=0.4, hi=1.4):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = vnoise(h, w, 2.0, s, aspect=9.0)
        q = np.floor(n * 4) / 4
        f.paint(None, rp, 0.5 + 0.28 * (q - 0.5) * 2 + 0.08 * (0.5 - f.gy / max(1, h)), q * 0.8)
    fn.rp = rp
    return fn


def wood(base=(104, 76, 50), lo=0.36, hi=1.35, plank=0):
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = vnoise(h, w, 3.0, s, aspect=8.0)
        q = np.floor(n * 3) / 3
        f.paint(None, rp, 0.5 + 0.2 * (q - 0.5) * 2 + 0.06 * (0.5 - f.gx / max(1, w)), q * 0.8)
        g = fbm(h, w, 5, s + 5, 1) > 0.88
        f.paint(g, rp, 0.2, -0.8)
    fn.rp = rp
    return fn


def membrane(base, vein, ribs=4, tone=0.6, lo=0.4, hi=1.4, scallops=4, scallop_depth=5, trailing="top"):
    """A dragon wing membrane: tinted skin, glowing veins fanning from the root, scalloped trailing edge."""
    rp = ramp(base, lo, hi)

    def fn(f):
        h, w, s = f.h, f.w, f.seed
        n = fbm(h, w, 9, s, 2)
        f.paint(None, rp, tone + 0.12 * (n - 0.5) * 2 + 0.08 * (f.gx / max(1, w)), 0.3 * n)
        if f.name in ("up", "down") and w > 12 and h > 8:
            ry = h * 0.85 if trailing == "top" else h * 0.15
            for k in range(ribs):
                yy = (k + 0.5) / ribs * h
                f.paint(f.line([(0, ry), (w - 1, yy)], 1.0, ss=1), vein, 0.6, -0.4, emit=True)
            f.paint(f.rect(0, 0, w, 1), rp, 0.3, 0.6) if False else None
            # trailing edge scallops
            sc = w / scallops
            for k in range(scallops):
                cx = (k + 0.5) * sc
                d = np.hypot((f.gx + 0.5 - cx) / (sc / 2), (f.gy + 0.5 - (0 if trailing == "top" else h)) / scallop_depth)
                f.paint(d < 1.0, rp, cut=True)
    fn.rp = rp
    return fn
