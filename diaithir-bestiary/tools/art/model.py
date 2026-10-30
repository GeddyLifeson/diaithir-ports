"""Model + texture builder: bones and cubes with painted materials -> GeckoLib geo.json, animation.json, png, glowmask.

Units are Blockbench model units (16 per block, y up from the feet, the creature's front toward -z, +x its left).
Every cube face is painted at 4 texels per unit (64 px per block) and packed into one atlas with per-face UVs.
"""
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image

from pix import Face, PX, ramp, seed_of

FACES = ("north", "south", "east", "west", "up", "down")
FACE_LIGHT = {"up": 0.06, "north": 0.0, "west": 0.02, "east": -0.03, "south": -0.05, "down": -0.13}
SIDE_ORDER = {"north": 0, "west": 1, "south": 2, "east": 3}


class Cube:
    def __init__(self, bone, origin, size, mat, rot=None, pivot=None, deco=None, hide=(), light=0.0, relief=1.0,
                 bevel=1.0, outline=0.0, cavity=0.35, mirror_of=None, tag="", form=0.12):
        self.bone, self.origin, self.size, self.mat = bone, list(origin), list(size), mat
        self.rot, self.pivot = rot, pivot
        self.deco = deco or {}
        self.hide = set(hide)
        self.light, self.relief, self.bevel, self.outline, self.cavity, self.form = light, relief, bevel, outline, cavity, form
        self.mirror_of = mirror_of
        self.tag = tag
        self.uid = None


class Bone:
    def __init__(self, model, name, pivot, parent=None, rot=None):
        self.model, self.name, self.pivot, self.parent, self.rot = model, name, list(pivot), parent, rot
        self.cubes = []

    def cube(self, origin, size, mat, **kw):
        c = Cube(self, origin, size, mat, **kw)
        c.uid = len(self.model.cubes)
        self.model.cubes.append(c)
        self.cubes.append(c)
        return c


def mirror_vec(v):
    return [-v[0]] + list(v[1:])


class Model:
    def __init__(self, name):
        self.name = name
        self.bones = []
        self.cubes = []
        self.by_name = {}

    def bone(self, name, pivot, parent=None, rot=None):
        b = Bone(self, name, pivot, parent, rot)
        self.bones.append(b)
        self.by_name[name] = b
        return b

    def pair(self, name, pivot, parent=None, rot=None):
        """Two bones name_l (+x) and name_r (-x). Returns a helper whose cube() adds a cube and its exact mirror."""
        pl, pr = parent if isinstance(parent, tuple) else (parent, parent)
        l = self.bone(name + "_l", pivot, pl, rot)
        r = self.bone(name + "_r", mirror_vec(pivot), pr, None if rot is None else [rot[0], -rot[1], -rot[2]])
        return Pair(l, r)

    # ---------------------------------------------------------------- painting + packing
    def paint(self):
        out = {}
        for c in self.cubes:
            if c.mirror_of is not None:
                continue
            out[c.uid] = self._paint_cube(c)
        for c in self.cubes:
            if c.mirror_of is not None:
                src = out[c.mirror_of.uid]
                m = {}
                swap = {"east": "west", "west": "east"}
                for fn, (rgba, glow) in src.items():
                    m[swap.get(fn, fn)] = (rgba[:, ::-1].copy(), glow[:, ::-1].copy())
                out[c.uid] = m
        return out

    def _paint_cube(self, c):
        sx, sy, sz = c.size
        dims = {"north": (sx, sy), "south": (sx, sy), "east": (sz, sy), "west": (sz, sy), "up": (sx, sz), "down": (sx, sz)}
        faces = {}
        perim_total = sx + sz + sx + sz
        for fn in FACES:
            wu, hu = dims[fn]
            w, h = max(1, round(wu * PX)), max(1, round(hu * PX))
            if fn in c.hide:
                faces[fn] = None
                continue
            perim = 0.0
            if fn in SIDE_ORDER:
                perim = {"north": 0, "west": sx, "south": sx + sz, "east": 2 * sx + sz}[fn]
            f = Face(w, h, fn, seed_of(self.name, c.uid, fn, c.tag), wu, hu, perim, perim_total)
            f.cube = c
            c.mat(f)
            for key in ("all", "sides" if fn in SIDE_ORDER else None, fn):
                if key and key in c.deco:
                    for d in (c.deco[key] if isinstance(c.deco[key], (list, tuple)) else [c.deco[key]]):
                        d(f)
            faces[fn] = f.finish(relief=c.relief, bevel=c.bevel, outline=c.outline, face_light=FACE_LIGHT[fn] + c.light,
                                 cavity=c.cavity, form=c.form)
        # hidden faces get a flat colour sampled from the cube's own front
        ref = next((v for v in faces.values() if v is not None), None)
        for fn in FACES:
            if faces[fn] is None:
                col = ref[0][..., :3].reshape(-1, 3).mean(axis=0) * 0.6 if ref is not None else np.array([30, 30, 30])
                a = np.zeros((2, 2, 4), np.uint8)
                a[..., :3] = col.astype(np.uint8)
                a[..., 3] = 255
                faces[fn] = (a, np.zeros_like(a))
        return faces

    def build(self, outdir, width_hint=None):
        painted = self.paint()
        rects = []
        for c in self.cubes:
            for fn in FACES:
                rgba, glow = painted[c.uid][fn]
                rects.append((c.uid, fn, rgba.shape[1], rgba.shape[0]))
        W, H, pos = pack(rects, width_hint)
        atlas = np.zeros((H, W, 4), np.uint8)
        gatlas = np.zeros((H, W, 4), np.uint8)
        for (uid, fn, w, h), (x, y) in zip(rects, pos):
            rgba, glow = painted[uid][fn]
            # one texel of edge bleed around every face
            pad = np.pad(rgba, ((1, 1), (1, 1), (0, 0)), mode="edge")
            atlas[y - 1:y + h + 1, x - 1:x + w + 1][...] = pad
            atlas[y:y + h, x:x + w] = rgba
            gatlas[y:y + h, x:x + w] = glow
        self.atlas, self.glow = atlas, gatlas
        self.uvs = {(uid, fn): (x, y, w, h) for (uid, fn, w, h), (x, y) in zip(rects, pos)}
        self.size_px = (W, H)
        return self

    # ---------------------------------------------------------------- json
    def geo(self):
        W, H = self.size_px
        bones = []
        ext = [0, 0, 0, 0]
        for b in self.bones:
            o = {"name": b.name, "pivot": b.pivot}
            if b.parent:
                o["parent"] = b.parent
            if b.rot:
                o["rotation"] = b.rot
            cubes = []
            for c in b.cubes:
                d = {"origin": c.origin, "size": c.size}
                if c.rot:
                    d["rotation"] = c.rot
                    d["pivot"] = c.pivot or [c.origin[0] + c.size[0] / 2, c.origin[1] + c.size[1] / 2, c.origin[2] + c.size[2] / 2]
                d["uv"] = {fn: {"uv": [self.uvs[(c.uid, fn)][0], self.uvs[(c.uid, fn)][1]],
                                "uv_size": [self.uvs[(c.uid, fn)][2], self.uvs[(c.uid, fn)][3]]} for fn in FACES}
                cubes.append(d)
                ext[0] = max(ext[0], abs(c.origin[0]), abs(c.origin[0] + c.size[0]))
                ext[1] = max(ext[1], abs(c.origin[2]), abs(c.origin[2] + c.size[2]))
                ext[2] = max(ext[2], c.origin[1] + c.size[1])
            if cubes:
                o["cubes"] = cubes
            bones.append(o)
        wide = max(ext[0], ext[1]) / 8 + 1
        tall = ext[2] / 16
        return {"format_version": "1.12.0", "minecraft:geometry": [{
            "description": {"identifier": "geometry." + self.name, "texture_width": W, "texture_height": H,
                            "visible_bounds_width": round(wide * 1.6, 1), "visible_bounds_height": round(tall + 1.5, 1),
                            "visible_bounds_offset": [0, round(tall / 2, 2), 0]},
            "bones": bones}]}

    def write(self, assets_root, anims):
        root = Path(assets_root)
        (root / "geckolib/models/entity").mkdir(parents=True, exist_ok=True)
        (root / "geckolib/animations/entity").mkdir(parents=True, exist_ok=True)
        (root / "textures/entity").mkdir(parents=True, exist_ok=True)
        (root / "geckolib/models/entity" / (self.name + ".geo.json")).write_text(json.dumps(self.geo(), indent=1) + "\n")
        (root / "geckolib/animations/entity" / (self.name + ".animation.json")).write_text(
            json.dumps({"format_version": "1.8.0", "animations": {k: v.json() if hasattr(v, "json") else v for k, v in anims.items()}}, indent=1) + "\n")
        Image.fromarray(self.atlas, "RGBA").save(root / "textures/entity" / (self.name + ".png"), optimize=True)
        gp = root / "textures/entity" / (self.name + "_glowmask.png")
        # always written, even when empty: GeckoLib's auto glow layer asks for <name>_glowmask.png and a missing file draws magenta
        Image.fromarray(self.glow, "RGBA").save(gp, optimize=True)


class Pair:
    def __init__(self, l, r):
        self.l, self.r = l, r

    def cube(self, origin, size, mat, rot=None, pivot=None, **kw):
        """A cube on the left bone (+x) and its mirror image on the right bone."""
        a = self.l.cube(origin, size, mat, rot=rot, pivot=pivot, **kw)
        mo = [-origin[0] - size[0]] + list(origin[1:])
        mrot = None if rot is None else [rot[0], -rot[1], -rot[2]]
        mpiv = None if pivot is None else mirror_vec(pivot)
        if rot is not None and pivot is None:
            pass
        kw2 = dict(kw)
        b = self.r.cube(mo, size, mat, rot=mrot, pivot=mpiv, mirror_of=a, **kw2)
        return a


def pack(rects, width_hint=None):
    """Shelf pack (w, h) rectangles with a one texel gutter. Returns (W, H, positions)."""
    best = None
    order = sorted(range(len(rects)), key=lambda i: (-rects[i][3], -rects[i][2]))
    for W in ([width_hint] if width_hint else [256, 384, 512, 640, 768, 1024, 1280, 1536, 2048]):
        if any(r[2] + 2 > W for r in rects):
            continue
        pos = [None] * len(rects)
        x, y, shelf = 1, 1, 0
        for i in order:
            w, h = rects[i][2], rects[i][3]
            if x + w + 1 > W:
                x, y, shelf = 1, y + shelf + 2, 0
            pos[i] = (x, y)
            x += w + 2
            shelf = max(shelf, h)
        H = y + shelf + 1
        H = (H + 7) // 8 * 8
        score = W * H * (1 + 0.15 * abs(math.log(W / H)))
        if best is None or score < best[0]:
            best = (score, W, H, pos)
    return best[1], best[2], best[3]


# ------------------------------------------------------------------ animation

class Anim:
    """Sampled sine and keyframe animation. Rotation in degrees (GeckoLib sign convention), position in model units."""

    def __init__(self, length, loop=True, steps=16):
        self.length, self.loop, self.steps = length, loop, steps
        self.ch = {}

    def _add(self, bone, kind, axis, fn):
        self.ch.setdefault((bone, kind), []).append((axis, fn))

    def rot(self, bone, axis, fn):
        self._add(bone, "rotation", axis, fn)
        return self

    def pos(self, bone, axis, fn):
        self._add(bone, "position", axis, fn)
        return self

    def sway(self, bone, axis, amp, cycles=1, phase=0.0, off=0.0):
        L = self.length
        return self.rot(bone, axis, lambda t: off + amp * math.sin(2 * math.pi * (cycles * t / L + phase)))

    def bob(self, bone, amp, cycles=1, phase=0.0, axis=1, off=0.0):
        L = self.length
        return self.pos(bone, axis, lambda t: off + amp * math.sin(2 * math.pi * (cycles * t / L + phase)))

    def hold(self, bone, axis, value):
        return self.rot(bone, axis, lambda t: value)

    def keys(self, bone, axis, pts, kind="rotation"):
        """Eased keyframes [(time, value), ...] across the animation."""
        pts = sorted(pts)

        def fn(t):
            if t <= pts[0][0]:
                return pts[0][1]
            for (t0, v0), (t1, v1) in zip(pts, pts[1:]):
                if t <= t1:
                    k = (t - t0) / max(t1 - t0, 1e-6)
                    k = k * k * (3 - 2 * k)
                    return v0 + (v1 - v0) * k
            return pts[-1][1]
        self._add(bone, kind, axis, fn)
        return self

    def value(self, bone, kind, t):
        v = [0.0, 0.0, 0.0]
        for axis, fn in self.ch.get((bone, kind), []):
            v[axis] += fn(t)
        return v

    def json(self):
        times = [round(self.length * i / self.steps, 4) for i in range(self.steps + 1)]
        bones = {}
        for (bone, kind) in self.ch:
            bones.setdefault(bone, {})[kind] = {str(t): [round(x, 3) for x in self.value(bone, kind, t)] for t in times}
        o = {"loop": self.loop, "animation_length": self.length, "bones": bones}
        if not self.loop:
            o["loop"] = False
        return o
