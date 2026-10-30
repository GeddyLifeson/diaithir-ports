"""Helpers for the rounded cartoon creatures: chamfered boxes, big expressive eyes, blush, mouths and fur tufts."""
import numpy as np

from pix import ramp, ramp_of

WHITE = ramp((250, 250, 255), lo=0.55, hi=1.02, cool=(-10, -6, 6), warm=(0, 0, 0))
INK = ramp_of(["#120c12", "#241a26", "#3a2c40", "#584a62"])
SHINE = ramp_of(["#ffffff", "#ffffff"])


def rbox(bone, origin, size, mat, r=1.5, eps=0.04, **kw):
    """A box with chamfered edges, built from three overlapping boxes (tiny offsets avoid z-fighting)."""
    x, y, z = origin
    w, h, d = size
    r = min(r, w / 2 - 0.3, h / 2 - 0.3, d / 2 - 0.3)
    if True:        # a painted face needs one continuous surface: a single cube with softened edges
        kw.setdefault("bevel", 1.6)
        kw.setdefault("form", 0.22)
        return [bone.cube([x, y, z], [w, h, d], mat, **kw)]
    out = []
    out.append(bone.cube([x + r, y, z + r], [w - 2 * r, h, d - 2 * r], mat, **kw))                       # top and bottom caps
    out.append(bone.cube([x, y + r, z + r + eps], [w, h - 2 * r, d - 2 * r - 2 * eps], mat, **kw))       # left and right
    out.append(bone.cube([x + r + eps, y + r + eps, z], [w - 2 * r - 2 * eps, h - 2 * r - 2 * eps, d], mat, **kw))    # front and back
    return out


def eye(f, cx, cy, rx, ry, pupil=None, shine=True, lid=0.0, look=(0.0, 0.0), iris=None, outline=True, lash=False):
    """A big cartoon eye: ink outline, white, a dark pupil with highlights. lid: fraction of the top covered (sleepy or grumpy)."""
    pupil = INK if pupil is None else pupil
    if outline:
        f.paint(f.ellipse(cx, cy, rx + 1.0, ry + 1.0), INK, 0.2, 0.2, nolight=True)
    f.paint(f.ellipse(cx, cy, rx, ry), WHITE, 0.82, 0.2, nolight=True)
    px, py = cx + look[0] * rx * 0.4, cy + look[1] * ry * 0.4
    if iris is not None:
        f.paint(f.ellipse(px, py, rx * 0.78, ry * 0.8), iris, 0.62, 0.1, nolight=True)
    f.paint(f.ellipse(px, py, rx * 0.55, ry * 0.6), pupil, 0.12, 0.0, nolight=True)
    if shine:
        f.paint(f.ellipse(px - rx * 0.22, py - ry * 0.28, max(0.8, rx * 0.2), max(0.8, ry * 0.2)), SHINE, 0.9, 0, nolight=True)
        f.paint(f.rect(px + rx * 0.14, py + ry * 0.22, px + rx * 0.14 + 1, py + ry * 0.22 + 1), SHINE, 0.9, 0, nolight=True)
    if lid > 0:
        m = f.ellipse(cx, cy, rx + 0.5, ry + 0.5) & (f.gy < cy - ry + 2 * ry * lid)
        f.paint(m, INK, 0.25, 0.3, nolight=True)


def blush(f, cx, cy, rx=2.5, ry=1.6, rp=None):
    rp = rp if rp is not None else ramp((238, 120, 130), lo=0.5, hi=1.2)
    f.paint(f.ellipse(cx, cy, rx, ry), rp, 0.7, 0.0, nolight=True)


def smile(f, cx, cy, w, curve=2.0, rp=None, wd=1.0):
    rp = rp if rp is not None else INK
    pts = [(cx - w / 2 + w * i / 10, cy + curve * (1 - ((i - 5) / 5) ** 2)) for i in range(11)]
    f.paint(f.line(pts, wd, ss=1), rp, 0.15, -0.4, nolight=True)


def frown(f, cx, cy, w, curve=2.0, rp=None, wd=1.0):
    rp = rp if rp is not None else INK
    pts = [(cx - w / 2 + w * i / 10, cy - curve * (1 - ((i - 5) / 5) ** 2) + curve) for i in range(11)]
    f.paint(f.line(pts, wd, ss=1), rp, 0.15, -0.4, nolight=True)
