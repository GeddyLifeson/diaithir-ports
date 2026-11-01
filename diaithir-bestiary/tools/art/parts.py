"""Reusable assemblies: oriented segments, antlers, torcs, crescents, rings."""
import math


def seg(bone, p0, p1, t, mat, z=0.0, d=None, over=0.5, **kw):
    """A cube spanning p0 -> p1 in the xy plane (model coords), thickness t, depth d, tilted about z."""
    dx, dy = p1[0] - p0[0], p1[1] - p0[1]
    L = math.hypot(dx, dy) + over
    d = t if d is None else d
    mx, my = (p0[0] + p1[0]) / 2, (p0[1] + p1[1]) / 2
    ang = math.degrees(math.atan2(dx, dy))
    return bone.cube([mx - t / 2, my - L / 2, z - d / 2], [t, L, d], mat, rot=[0, 0, ang], pivot=[mx, my, z], **kw)


def polyline(bone, pts, t0, t1, mat, z=0.0, d=None, **kw):
    n = len(pts) - 1
    out = []
    for i in range(n):
        t = t0 + (t1 - t0) * i / max(1, n - 1)
        out.append(seg(bone, pts[i], pts[i + 1], t, mat, z, d, **kw))
    return out


def arc(cx, cy, r, a0, a1, n):
    return [(cx + r * math.cos(math.radians(a0 + (a1 - a0) * i / n)), cy + r * math.sin(math.radians(a0 + (a1 - a0) * i / n))) for i in range(n + 1)]


def mirror_pts(pts):
    return [(-x, y) for x, y in pts]


def antler(pair, mat, main, tines, t0=2.0, t1=1.0, z=0.0, d=None, tt=1.1, **kw):
    """Left-side antler from a main beam polyline and a list of tine polylines; the pair mirrors it."""
    for i in range(len(main) - 1):
        t = t0 + (t1 - t0) * i / max(1, len(main) - 2)
        a, b = main[i], main[i + 1]
        dx, dy = b[0] - a[0], b[1] - a[1]
        L = math.hypot(dx, dy) + 0.5
        mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
        ang = math.degrees(math.atan2(dx, dy))
        pair.cube([mx - t / 2, my - L / 2, z - (d or t) / 2], [t, L, d or t], mat, rot=[0, 0, ang], pivot=[mx, my, z], **kw)
    for tn in tines:
        for i in range(len(tn) - 1):
            a, b = tn[i], tn[i + 1]
            dx, dy = b[0] - a[0], b[1] - a[1]
            L = math.hypot(dx, dy) + 0.4
            mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
            ang = math.degrees(math.atan2(dx, dy))
            t = tt * (1 - 0.35 * i / max(1, len(tn) - 1))
            pair.cube([mx - t / 2, my - L / 2, z - (d or t) / 2], [t, L, d or t], mat, rot=[0, 0, ang], pivot=[mx, my, z], **kw)
