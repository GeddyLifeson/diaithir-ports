"""Pixel painting core for the Bestiary creature art (numpy + Pillow only).

A Face is one cube face painted at 4 texels per model unit (64 px per block). Painting is layered: each layer
sets a colour ramp, a tone (position in the ramp), a height (relief) and optionally emission. At the end the
height field is lit from the upper left, edges are bevelled, and every pixel is snapped to a palette ramp, so
the result is clean banded pixel art with no random noise.
"""
import hashlib
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

N = 7          # colours per ramp
PX = 4         # texels per model unit


def hexc(s):
    s = s.lstrip("#")
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4))


def ramp(base, lo=0.40, hi=1.40, cool=(-16, -9, 20), warm=(18, 11, -14)):
    """Seven colours from shadow to highlight around a base colour; shadows lean cool, highlights warm."""
    base = np.array(base if not isinstance(base, str) else hexc(base), float)
    out = []
    for k in range(N):
        f = lo + (hi - lo) * k / (N - 1)
        c = base * f
        if f < 1:
            c = c + np.array(cool) * (1 - f) / (1 - lo)
        else:
            c = c + np.array(warm) * (f - 1) / (hi - 1)
        out.append(np.clip(c, 0, 255))
    return np.array(out)


def ramp_of(colors):
    """A ramp from an explicit list of colours (hex strings or tuples), stretched to N steps."""
    cs = np.array([hexc(c) if isinstance(c, str) else c for c in colors], float)
    xs = np.linspace(0, len(cs) - 1, N)
    return np.stack([np.interp(xs, np.arange(len(cs)), cs[:, i]) for i in range(3)], axis=1)


def seed_of(*parts):
    return int.from_bytes(hashlib.md5("|".join(map(str, parts)).encode()).digest()[:4], "little")


# ------------------------------------------------------------------ noise

def _up(grid, h, w):
    im = Image.fromarray(grid.astype(np.float32), "F").resize((w, h), Image.BICUBIC)
    return np.asarray(im)


def vnoise(h, w, cell, seed, aspect=1.0):
    """Smooth value noise in 0..1 with features about `cell` pixels wide (and cell*aspect tall)."""
    r = np.random.default_rng(seed)
    gw, gh = max(2, int(np.ceil(w / cell)) + 2), max(2, int(np.ceil(h / (cell * aspect))) + 2)
    g = r.random((gh, gw))
    big = _up(g, int(gh * cell * aspect), int(gw * cell))
    return np.clip(big[:h, :w], 0, 1)


def fbm(h, w, cell, seed, octaves=3, aspect=1.0):
    tot = np.zeros((h, w))
    amp, s = 1.0, 0.0
    for o in range(octaves):
        tot += amp * vnoise(h, w, max(1.5, cell / (2 ** o)), seed + 101 * o, aspect)
        s += amp
        amp *= 0.5
    return tot / s


def voronoi(h, w, n, seed, xs=None, ys=None):
    """Cellular pattern: returns (cell id, distance to nearest, distance to second nearest)."""
    r = np.random.default_rng(seed)
    pts = np.stack([r.random(n) * w, r.random(n) * h], axis=1)
    yy, xx = np.mgrid[0:h, 0:w]
    d = np.stack([np.hypot(xx - p[0], yy - p[1]) for p in pts])
    order = np.sort(d, axis=0)
    return d.argmin(axis=0), order[0], order[1] if n > 1 else order[0] * 2


def blur(a, r):
    """Box blur (twice, approximating a gaussian) of radius r."""
    if r <= 0:
        return a
    k = 2 * r + 1
    out = a
    for _ in range(2):
        p = np.pad(out, ((0, 0), (r, r)), mode="edge")
        c = np.cumsum(p, axis=1)
        c = np.concatenate([np.zeros((c.shape[0], 1)), c], axis=1)
        out = (c[:, k:] - c[:, :-k]) / k
        p = np.pad(out, ((r, r), (0, 0)), mode="edge")
        c = np.cumsum(p, axis=0)
        c = np.concatenate([np.zeros((1, c.shape[1])), c], axis=0)
        out = (c[k:, :] - c[:-k, :]) / k
    return out


def dilate(m, r=1):
    out = m.copy()
    for _ in range(r):
        p = np.pad(out, 1)
        out = p[1:-1, 1:-1] | p[:-2, 1:-1] | p[2:, 1:-1] | p[1:-1, :-2] | p[1:-1, 2:]
    return out


def erode(m, r=1):
    return ~dilate(~m, r)


# ------------------------------------------------------------------ the face

class Face:
    def __init__(self, w, h, name, seed, wu, hu, perim=0.0, perim_total=1.0):
        self.w, self.h, self.name, self.seed = w, h, name, seed
        self.wu, self.hu = wu, hu                      # size in model units
        self.rng = np.random.default_rng(seed)
        self.gy, self.gx = np.mgrid[0:h, 0:w]
        self.X = (self.gx + 0.5) / PX                  # unit coords across the face
        self.Y = (self.gy + 0.5) / PX
        self.P = perim + self.X                        # continuous around the cube for the four side faces
        self.P_total = perim_total
        self.ramps = []
        self.rid = np.zeros((h, w), np.int16)
        self.tone = np.full((h, w), 0.6)
        self.H = np.zeros((h, w))
        self.emit = np.zeros((h, w))
        self.ecol = np.zeros((h, w, 3))
        self.alpha = np.ones((h, w), bool)
        self.nolight = np.zeros((h, w), bool)          # skip relief and bevel (eyes, glints)
        self.ramps.append(ramp((128, 128, 128)))

    # -- layering
    def _rid(self, r):
        for i, x in enumerate(self.ramps):
            if x is r:
                return i
        self.ramps.append(r)
        return len(self.ramps) - 1

    def paint(self, mask, rp, tone=None, h=None, add_h=None, emit=False, nolight=False, cut=False):
        if mask is None:
            mask = np.ones((self.h, self.w), bool)
        mask = np.asarray(mask, bool)
        if cut:
            self.alpha[mask] = False
            return mask
        self.alpha[mask] = True
        self.rid[mask] = self._rid(rp)
        if tone is not None:
            self.tone[mask] = (tone if np.isscalar(tone) else np.asarray(tone)[mask])
        if h is not None:
            self.H[mask] = (h if np.isscalar(h) else np.asarray(h)[mask])
        if add_h is not None:
            self.H[mask] += (add_h if np.isscalar(add_h) else np.asarray(add_h)[mask])
        self.emit[mask] = 1.0 if emit else 0.0
        if emit:
            self.ecol[mask] = rp[N - 2]
        self.nolight[mask] = nolight
        return mask

    # -- shape masks (hard edged pixel shapes)
    def shape(self, fn, ss=3):
        im = Image.new("L", (self.w * ss, self.h * ss), 0)
        fn(ImageDraw.Draw(im), ss)
        a = np.asarray(im.resize((self.w, self.h), Image.BOX))
        return a >= 128

    def rect(self, x0, y0, x1, y1):
        m = np.zeros((self.h, self.w), bool)
        m[max(0, int(y0)):max(0, int(y1)), max(0, int(x0)):max(0, int(x1))] = True
        return m

    def ellipse(self, cx, cy, rx, ry):
        return (((self.gx + 0.5 - cx) / max(rx, .01)) ** 2 + ((self.gy + 0.5 - cy) / max(ry, .01)) ** 2) <= 1.0

    def line(self, pts, width=1.0, ss=3):
        def fn(d, s):
            p = [(x * s, y * s) for x, y in pts]
            d.line(p, fill=255, width=max(1, int(round(width * s))), joint="curve")
            r = width * s / 2
            for x, y in p:
                d.ellipse((x - r, y - r, x + r, y + r), fill=255)
        return self.shape(fn, ss)

    def poly(self, pts, ss=3):
        return self.shape(lambda d, s: d.polygon([(x * s, y * s) for x, y in pts], fill=255), ss)

    def ring(self, cx, cy, r, width, ss=3):
        d = np.hypot(self.gx + 0.5 - cx, self.gy + 0.5 - cy)
        return (d <= r + width / 2) & (d >= r - width / 2)

    # -- finishing
    def finish(self, relief=1.0, bevel=1.0, outline=0.0, face_light=0.0, cavity=0.35, lightdir=(-0.6, -0.8), form=0.12):
        H = self.H
        gx = np.zeros_like(H)
        gy = np.zeros_like(H)
        gx[:, 1:-1] = (H[:, 2:] - H[:, :-2]) / 2
        gy[1:-1, :] = (H[2:, :] - H[:-2, :]) / 2
        s = -(gx * lightdir[0] + gy * lightdir[1])
        s = np.clip(s, -1.2, 1.2)
        tone = self.tone + 0.20 * relief * s + face_light
        if form:
            tone = tone + form * (0.5 - self.gx / max(1, self.w - 1)) * 0.9 + form * (0.5 - self.gy / max(1, self.h - 1)) * 0.7 * (~self.nolight) * 1.0
        if cavity:
            ao = np.clip(blur(H, 2) - H, 0, 2)
            tone = tone - cavity * ao * 0.5
        # bevel: bright top and left rim, dark bottom and right rim
        if bevel:
            t = np.zeros((self.h, self.w))
            if self.h > 3 and self.w > 3:
                t[0, :] += 0.12
                t[:, 0] += 0.08
                t[-1, :] -= 0.16
                t[:, -1] -= 0.12
                t[1, 1:-1] += 0.03
                t[-2, 1:-1] -= 0.04
            tone = tone + bevel * t * (~self.nolight)
        if outline:
            t = np.zeros((self.h, self.w))
            t[0, :] = t[-1, :] = t[:, 0] = t[:, -1] = 1
            tone = tone - outline * t
        idx = np.clip(np.rint(tone * (N - 1)), 0, N - 1).astype(int)
        R = np.stack([np.pad(r, ((0, 0), (0, 0))) for r in self.ramps])
        col = R[self.rid, idx]
        # glow spill onto neighbours
        if self.emit.any():
            halo = np.clip(blur(self.emit, 2) * 1.6, 0, 1) * (1 - self.emit)
            ec = np.zeros_like(col)
            for c in range(3):
                ec[..., c] = blur(self.ecol[..., c] * self.emit, 2) / np.maximum(blur(self.emit, 2), 1e-3)
            col = col + (ec - col) * (halo[..., None] * 0.32)
        rgba = np.zeros((self.h, self.w, 4), np.uint8)
        rgba[..., :3] = np.clip(col, 0, 255).astype(np.uint8)
        rgba[..., 3] = np.where(self.alpha, 255, 0)
        glow = np.zeros_like(rgba)
        em = (self.emit > 0.5) & self.alpha
        glow[em, :3] = rgba[em, :3]
        glow[em, 3] = 255
        return rgba, glow
