"""The player-skin layout MCA Reborn's villager model uses, as 3D points: every texel of a 64x clothing texture knows
which part, layer and face it lies on and where that is on the body, so garments are drawn in body space and wrap round
the model the way cloth does.

Body space is in game pixels: feet at y 0, head top at y 32, the villager faces +z and x grows toward its left.
"""
import numpy as np

S = 8                       # texels per skin pixel: 64x64 layout at 512x512
SIZE = 64 * S
# part: base layer uv, outer layer uv, size (w, h, d), lowest corner (x, y, z)
PARTS = {
    "head": ((0, 0), (32, 0), (8, 8, 8), (-4, 24, -4)),
    "body": ((16, 16), (16, 32), (8, 12, 4), (-4, 12, -2)),
    "rarm": ((40, 16), (40, 32), (4, 12, 4), (-8, 12, -2)),
    "larm": ((32, 48), (48, 48), (4, 12, 4), (4, 12, -2)),
    "rleg": ((0, 16), (0, 32), (4, 12, 4), (-4, 0, -2)),
    "lleg": ((16, 48), (0, 48), (4, 12, 4), (0, 0, -2)),
}
INFLATE = {"head": 0.5}     # outer layer grows 0.5 px on the head, 0.25 elsewhere


def rects(u, v, w, h, d):
    """Face rectangles (x, y, width, height) in skin pixels, Minecraft's box unwrap."""
    return {"right": (u, v + d, d, h), "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h),
            "back": (u + 2 * d + w, v + d, w, h), "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d)}


class Face:
    """One face of one box: its texel rectangle at S and the body-space point (X, Y, Z) of every texel centre."""
    def __init__(s, part, layer, name, rect, size, lo, g):
        s.part, s.layer, s.name = part, layer, name
        x, y, fw, fh = rect
        s.sl = (slice(y * S, (y + fh) * S), slice(x * S, (x + fw) * S))
        w, h, d = size
        x0, y0, z0 = lo[0] - g, lo[1] - g, lo[2] - g
        x1, y1, z1 = lo[0] + w + g, lo[1] + h + g, lo[2] + d + g
        s.box = (x0, y0, z0, x1, y1, z1)
        s.rect = rect
        rows, cols = fh * S, fw * S
        b, a = np.mgrid[0:rows, 0:cols].astype(np.float32)
        X, Y, Z = s.point((a + 0.5) / cols, (b + 0.5) / rows)  # a, b: 0..1 across and down the face
        W, D = x1 - x0, z1 - z0
        s.X, s.Y, s.Z = X, Y, Z
        s.shape = a.shape
        s.side = name in ("front", "back", "right", "left")
        # distance round the part's girth, for things that repeat round a body (fringe, beads on a band)
        s.round = {"front": X - x0, "left": W + (z1 - Z), "back": W + D + (x1 - X), "right": 2 * W + D + (Z - z0)}.get(name, X)

    def point(s, a, b):
        """Body-space point at (a, b), 0..1 across and down the face as it lies in the texture."""
        x0, y0, z0, x1, y1, z1 = s.box
        W, H, D = x1 - x0, y1 - y0, z1 - z0
        one = np.ones_like(np.asarray(a, np.float32) + np.asarray(b, np.float32))
        return {"front": (x0 + a * W, y1 - b * H, z1 * one), "back": (x1 - a * W, y1 - b * H, z0 * one),
                "right": (x0 * one, y1 - b * H, z0 + a * D), "left": (x1 * one, y1 - b * H, z1 - a * D),
                "top": (x0 + a * W, y1 * one, z0 + b * D), "bottom": (x0 + a * W, y0 * one, z1 - b * D)}[s.name]


def faces():
    out = []
    for part, (base, outer, size, lo) in PARTS.items():
        for layer, uv in (("base", base), ("outer", outer)):
            g = 0.0 if layer == "base" else INFLATE.get(part, 0.25)
            for name, rect in rects(*uv, *size).items():
                out.append(Face(part, layer, name, rect, size, lo, g))
    return out
