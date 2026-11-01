"""Decals and motifs drawn on a Face: Celtic knotwork, spirals, triskele, ogham, studs; industrial rivets, vents, hazard."""
import math

import numpy as np

from pix import N, ramp, ramp_of, fbm, blur, dilate, erode

# shared palettes ---------------------------------------------------------------------------
BRONZE = ramp_of(["#2e180c", "#5e3418", "#96581f", "#c2823a", "#e2ab58", "#f6d38e"])
GOLD = ramp_of(["#44280c", "#7c4f14", "#b8801f", "#e0ac38", "#f6d466", "#fff2b4"])
SILVER = ramp_of(["#232a3c", "#4a566e", "#8290aa", "#b8c4dc", "#e6eefc", "#ffffff"])
STONE = ramp((118, 120, 116), lo=.45, hi=1.3)
SLATE = ramp((84, 92, 98), lo=.42, hi=1.3)
MOSS = ramp((72, 106, 52), lo=.42, hi=1.3)
OAK = ramp((112, 78, 48), lo=.42, hi=1.35)
GLOW_GREEN = ramp_of(["#1c5a38", "#2f9a58", "#5ee08a", "#a8ffc6", "#e8fff0"])
GLOW_SILVER = ramp_of(["#40608a", "#7aa6d8", "#bcd8f4", "#eef6ff", "#ffffff"])
GLOW_AMBER = ramp_of(["#8a3a10", "#e0781c", "#ffb040", "#ffe08a", "#fff6d0"])
GLOW_CYAN = ramp_of(["#106a80", "#2ab8d8", "#70e8ff", "#c8f8ff", "#ffffff"])
GLOW_RED = ramp_of(["#701810", "#d03a20", "#ff7040", "#ffc080", "#fff0d0"])
ENAMEL_RED = ramp((176, 36, 34), lo=.4, hi=1.5)
ENAMEL_BLUE = ramp((36, 70, 160), lo=.4, hi=1.6)


def tube(f, pts, wd, rp, edge=None, hb=1.0, hi_shift=0.2):
    """A rounded cord along a polyline: dark edge, body, light core and a small highlight. Heights rise to the middle."""
    edge = edge if edge is not None else rp
    f.paint(f.line(pts, wd + 1.6), edge, 0.12, hb * 0.2)
    f.paint(f.line(pts, wd), rp, 0.46, hb * 0.7)
    f.paint(f.line(pts, max(1.0, wd * 0.62)), rp, 0.64, hb)
    up = [(x, y - wd * hi_shift) for x, y in pts]
    f.paint(f.line(up, max(1.0, wd * 0.26)) & f.line(pts, wd * 0.62), rp, 0.9, hb * 1.1)


def twist(f, y0, y1, rp, edge=None, period=None, wd=None, phase=0.0, x0=0, x1=None, hb=1.0):
    """Two-strand Celtic twist (plait) running left to right in the band y0..y1 (pixels)."""
    x1 = f.w if x1 is None else x1
    band = y1 - y0
    wd = wd or max(2.0, band * 0.34)
    A = max(0.5, (band - wd) / 2 - 0.2)
    cy = (y0 + y1) / 2
    period = period or band * 1.7
    xs = np.arange(x0 - period, x1 + period, 0.5)
    strands = []
    for k in (0, 1):
        ys = cy + A * np.sin(2 * math.pi * (xs / period + phase) + k * math.pi)
        strands.append(list(zip(xs, ys)))
    for k in (0, 1):
        tube(f, strands[k], wd, rp, edge, hb)
    # crossings: at x where sin = 0 (every half period); alternate which strand is on top
    m0 = math.floor((x0 - period) / (period / 2))
    for m in range(m0, int((x1 + period) / (period / 2)) + 2):
        xc = (m * 0.5 - phase) * period
        top = m % 2
        seg = [(x, y) for (x, y) in strands[top] if abs(x - xc) <= period * 0.2]
        if len(seg) > 1:
            tube(f, seg, wd, rp, edge, hb * 1.15)


def ring_chain(f, y0, y1, rp, edge=None, x0=0, x1=None, wd=None, hb=1.0):
    """A chain of interlocked rings (alternating over and under)."""
    x1 = f.w if x1 is None else x1
    band = y1 - y0
    r = band / 2 - 0.9
    wd = wd or max(2.0, band * 0.26)
    step = r * 1.25
    cy = (y0 + y1) / 2
    cx = x0 + r
    i = 0
    centers = []
    while cx - r < x1:
        centers.append(cx)
        cx += step
    ringpts = lambda c: [(c + r * math.cos(a), cy + r * 0.92 * math.sin(a)) for a in np.linspace(0, 2 * math.pi, 40)]
    for c in centers[::2]:
        tube(f, ringpts(c), wd, rp, edge, hb)
    for c in centers[1::2]:
        tube(f, ringpts(c), wd, rp, edge, hb)
    # each overlap: the ring on the left goes over at the top crossing, under at the bottom
    for i, c in enumerate(centers[:-1]):
        c2 = centers[i + 1]
        a = ringpts(c) if i % 2 == 0 else ringpts(c2)
        sel = [(x, y) for (x, y) in a if abs(x - (c + c2) / 2) < step * 0.55 and y < cy]
        if len(sel) > 1:
            tube(f, sel, wd, rp, edge, hb * 1.15)


def spiral(f, cx, cy, r, turns=2.0, wd=1.6, rp=None, edge=None, hb=1.0, flip=False, a0=0.0):
    rp = rp if rp is not None else GOLD
    pts = []
    n = int(60 * turns)
    for i in range(n + 1):
        t = i / n
        a = a0 + t * turns * 2 * math.pi * (-1 if flip else 1)
        rr = r * (0.12 + 0.88 * (1 - t))
        pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    tube(f, pts, wd, rp, edge, hb)


def triskele(f, cx, cy, r, wd=1.8, rp=None, edge=None, hb=1.0, rot=90.0, emit=None, sweep=1.15):
    """Three spiral arms with a dot at each end; emit paints them as glowing channels instead of cord."""
    rp = rp if rp is not None else GOLD
    for k in range(3):
        base = math.radians(rot + 120 * k)
        pts = []
        n = 40
        for i in range(n + 1):
            t = i / n
            a = base - t * sweep * math.pi
            rr = r * (0.22 + 0.62 * t)
            pts.append((cx + rr * math.cos(a), cy - rr * math.sin(a)))
        if emit is not None:
            f.paint(f.line(pts, wd), emit, 0.6, -0.5, emit=True)
        else:
            tube(f, pts, wd, rp, edge, hb)
        x, y = pts[-1]
        if emit is not None:
            f.paint(f.ellipse(x, y, wd * 0.9, wd * 0.9), emit, 0.85, -0.5, emit=True)
        else:
            f.paint(f.ellipse(x, y, wd * 0.95 + 0.4, wd * 0.95 + 0.4), rp, 0.25, 0.8)
            f.paint(f.ellipse(x, y, wd * 0.95, wd * 0.95), rp, 0.7, 1.4)


def ogham(f, x, y0, y1, rp, strokes, wd=1.0, horizontal=False, emit=True, stem=True, spacing=3.0):
    """Ogham: a stem line with groups of strokes. strokes is a string of 'l' (left), 'r' (right), 'c' (across), 'd' (diagonal)."""
    def pt(a, b):
        return (b, a) if horizontal else (a, b)
    if stem:
        m = f.line([pt(x, y0), pt(x, y1)], wd)
        f.paint(m, rp, 0.55, -0.6, emit=emit)
    yy = y0 + spacing * 0.8
    for ch in strokes:
        if ch == " ":
            yy += spacing * 0.6
            continue
        L = spacing * 1.2
        if ch == "l":
            seg = [pt(x - L, yy), pt(x, yy)]
        elif ch == "r":
            seg = [pt(x, yy), pt(x + L, yy)]
        elif ch == "c":
            seg = [pt(x - L, yy), pt(x + L, yy)]
        else:
            seg = [pt(x - L * 0.7, yy + L * 0.5), pt(x + L * 0.7, yy - L * 0.5)]
        f.paint(f.line(seg, wd), rp, 0.55, -0.6, emit=emit)
        yy += spacing
        if yy > y1:
            break


def stud(f, cx, cy, r, enamel, rim=BRONZE):
    f.paint(f.ellipse(cx, cy, r + 0.9, r + 0.9), rim, 0.5, 1.2)
    f.paint(f.ellipse(cx, cy, r, r), enamel, 0.55, 1.6)
    f.paint(f.rect(cx - r * 0.45 - 0.5, cy - r * 0.55 - 0.5, cx - r * 0.45 + 0.6, cy - r * 0.55 + 0.6), enamel, 0.98, 2.0, nolight=True)


def rivet(f, cx, cy, rp, r=1.1, h=1.2):
    f.paint(f.ellipse(cx, cy, r + 0.7, r + 0.7), rp, 0.25, 0.0)
    f.paint(f.ellipse(cx, cy, r, r), rp, 0.7, h)
    f.paint(f.rect(cx - 0.5, cy - 0.7, cx + 0.5, cy), rp, 0.97, h + 0.4, nolight=True)


def rivet_rows(f, rp, inset=2.0, step=None):
    for cx in ((inset, f.w - inset) if step is None else range(int(inset), int(f.w - inset) + 1, int(step))):
        for cy in ((inset, f.h - inset)):
            if inset < f.w and inset < f.h:
                rivet(f, cx, cy, rp)


def band(f, y0, y1, rp, tone=0.55, h=0.8, emit=False):
    f.paint(f.rect(0, y0, f.w, y1), rp, tone, h, emit=emit)


def frame(f, thick, rp, tone=0.55, h=0.8):
    m = f.rect(0, 0, f.w, f.h) & ~f.rect(thick, thick, f.w - thick, f.h - thick)
    f.paint(m, rp, tone, h)


def hazard(f, y0, y1, a, b, step=6):
    for y in range(int(y0), int(y1)):
        for x in range(f.w):
            k = ((x + y) // step) % 2
            f.paint(f.rect(x, y, x + 1, y + 1), a if k == 0 else b, 0.58, 0.2)


def vent(f, x0, y0, x1, y1, rp, dark, slats=None, glow=None):
    """A grille: dark slots with raised slats between them. glow: an emissive ramp shining through the slots."""
    f.paint(f.rect(x0 - 1, y0 - 1, x1 + 1, y1 + 1), rp, 0.3, 1.0)
    n = slats or max(2, int((y1 - y0) // 3))
    step = (y1 - y0) / n
    for i in range(n):
        ya, yb = y0 + i * step, y0 + i * step + step * 0.62
        if glow is not None:
            f.paint(f.rect(x0, ya, x1, yb), glow, 0.55, -1.0, emit=True)
        else:
            f.paint(f.rect(x0, ya, x1, yb), dark, 0.05, -1.2)
        f.paint(f.rect(x0, yb, x1, y0 + (i + 1) * step), rp, 0.7, 0.9)


def scratches(f, rp, n=5, seed=0, tone=0.85, length=(3, 9)):
    r = np.random.default_rng(f.seed + seed)
    for _ in range(n):
        x, y = r.uniform(0, f.w), r.uniform(0, f.h)
        L = r.uniform(*length)
        a = r.uniform(-0.5, 0.5) + (0 if r.random() < 0.6 else 1.2)
        m = f.line([(x, y), (x + L * math.cos(a), y + L * math.sin(a))], 1.0, ss=1)
        f.paint(m, rp, tone, None)


def stars(f, n=None, rp=None, seed=0, big=0.25, emit=True):
    """Scattered glowing stars: single pixels and a few plus-shaped ones."""
    rp = rp if rp is not None else GLOW_SILVER
    r = np.random.default_rng(f.seed + 77 + seed)
    n = n if n is not None else max(2, int(f.w * f.h / 140))
    for _ in range(n):
        x, y = int(r.integers(2, max(3, f.w - 2))), int(r.integers(2, max(3, f.h - 2)))
        if r.random() < big and f.w > 8 and f.h > 8:
            f.paint(f.rect(x - 1, y, x + 2, y + 1), rp, 0.75, 0, emit=emit)
            f.paint(f.rect(x, y - 1, x + 1, y + 2), rp, 0.95, 0, emit=emit)
        else:
            f.paint(f.rect(x, y, x + 1, y + 1), rp, 0.8, 0, emit=emit)


def craters(f, rp, n=2, seed=0):
    r = np.random.default_rng(f.seed + 5 + seed)
    for _ in range(n):
        x, y, rad = r.uniform(3, max(3.5, f.w - 3)), r.uniform(3, max(3.5, f.h - 3)), r.uniform(1.8, 3.2)
        f.paint(f.ellipse(x, y, rad, rad * 0.9), rp, 0.5, -0.3, emit=True)


def stitches(f, pts, rp, step=3.0, size=1.6, cross=True, wd=1.0):
    """Thread stitches along a line: short ticks across it (or X marks)."""
    (x0, y0), (x1, y1) = pts
    L = math.hypot(x1 - x0, y1 - y0)
    n = max(1, int(L / step))
    dx, dy = (x1 - x0) / L, (y1 - y0) / L
    nx, ny = -dy, dx
    for i in range(n + 1):
        cx, cy = x0 + dx * i * step, y0 + dy * i * step
        f.paint(f.line([(cx - nx * size, cy - ny * size), (cx + nx * size, cy + ny * size)], wd, ss=1), rp, 0.12, -0.5)
        if cross:
            f.paint(f.line([(cx - nx * size + dx * 1.2, cy - ny * size + dy * 1.2), (cx + nx * size - dx * 1.2, cy + ny * size - dy * 1.2)], wd, ss=1), rp, 0.12, -0.5)


def teeth(f, x0, y0, x1, y1, rp, count=9, h=2, dark=None):
    """A row of small pointed teeth in a mouth rectangle."""
    if dark is not None:
        f.paint(f.rect(x0, y0, x1, y1), dark, 0.1, -1.0)
    step = (x1 - x0) / count
    for i in range(count):
        xa = x0 + i * step
        f.paint(f.rect(xa + 0.3, y0, xa + step - 0.3, y0 + h), rp, 0.88, 0.5, nolight=True)
        f.paint(f.rect(xa + 0.3, y1 - h, xa + step - 0.3, y1), rp, 0.84, 0.5, nolight=True)
