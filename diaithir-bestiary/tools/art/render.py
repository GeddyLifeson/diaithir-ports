"""Offline software renderer for the creature geo.json + animation.json + texture (numpy only).

It follows GeckoLib's conventions: cube rotation (-x, -y, z) degrees applied Z then Y then X about the cube pivot,
bone rotation the same, box faces mapped like Blockbench, glowmask pixels drawn full bright. The render frame has
x flipped (GeckoLib negates x), so the creature's front (-z) faces the camera in the front view.
"""
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

FACE_DEF = {
    "north": ((0, 0, -1), [(1, 1, 0, 0, 0), (0, 1, 0, 1, 0), (0, 0, 0, 1, 1), (1, 0, 0, 0, 1)]),
    "south": ((0, 0, 1), [(0, 1, 1, 0, 0), (1, 1, 1, 1, 0), (1, 0, 1, 1, 1), (0, 0, 1, 0, 1)]),
    "east": ((1, 0, 0), [(1, 1, 1, 0, 0), (1, 1, 0, 1, 0), (1, 0, 0, 1, 1), (1, 0, 1, 0, 1)]),
    "west": ((-1, 0, 0), [(0, 1, 0, 0, 0), (0, 1, 1, 1, 0), (0, 0, 1, 1, 1), (0, 0, 0, 0, 1)]),
    "up": ((0, 1, 0), [(1, 1, 1, 0, 0), (0, 1, 1, 1, 0), (0, 1, 0, 1, 1), (1, 1, 0, 0, 1)]),
    "down": ((0, -1, 0), [(1, 0, 1, 0, 0), (0, 0, 1, 1, 0), (0, 0, 0, 1, 1), (1, 0, 0, 0, 1)]),
}


def rot_mat(rx, ry, rz):
    """GeckoLib: angles (-x, -y, z) degrees, matrix Rz * Ry * Rx in the render frame."""
    ax, ay, az = math.radians(-rx), math.radians(-ry), math.radians(rz)
    cx, sx, cy, sy, cz, sz = math.cos(ax), math.sin(ax), math.cos(ay), math.sin(ay), math.cos(az), math.sin(az)
    Rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    Rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return Rz @ Ry @ Rx


class Rig:
    def __init__(self, root, name):
        root = Path(root)
        g = json.loads((root / "geckolib/models/entity" / (name + ".geo.json")).read_text())["minecraft:geometry"][0]
        self.desc = g["description"]
        self.bones = {b["name"]: b for b in g["bones"]}
        self.order = [b["name"] for b in g["bones"]]
        self.tex = np.asarray(Image.open(root / "textures/entity" / (name + ".png")).convert("RGBA"))
        gp = root / "textures/entity" / (name + "_glowmask.png")
        self.glow = np.asarray(Image.open(gp).convert("RGBA")) if gp.exists() else None
        self.anims = json.loads((root / "geckolib/animations/entity" / (name + ".animation.json")).read_text())["animations"]
        self.name = name

    @staticmethod
    def _sample(ch, t, loop, length):
        keys = sorted((float(k), v) for k, v in ch.items())
        if loop and length:
            t = t % length
        if t <= keys[0][0]:
            return np.array(keys[0][1], float)
        for (t0, v0), (t1, v1) in zip(keys, keys[1:]):
            if t <= t1:
                k = (t - t0) / max(t1 - t0, 1e-9)
                return np.array(v0, float) * (1 - k) + np.array(v1, float) * k
        return np.array(keys[-1][1], float)

    def pose(self, anim=None, t=0.0):
        a = self.anims.get(anim) if anim else None
        out = {}

        def get(name):
            if name in out:
                return out[name]
            b = self.bones[name]
            piv = np.array([-b["pivot"][0], b["pivot"][1], b["pivot"][2]], float)
            rot = np.array(b.get("rotation", [0, 0, 0]), float)
            pos = np.zeros(3)
            if a and name in a.get("bones", {}):
                bd = a["bones"][name]
                if "rotation" in bd:
                    rot = rot + self._sample(bd["rotation"], t, a.get("loop"), a.get("animation_length"))
                if "position" in bd:
                    p = self._sample(bd["position"], t, a.get("loop"), a.get("animation_length"))
                    pos = np.array([-p[0], p[1], p[2]])
            R = rot_mat(*rot)
            off = piv - R @ piv + pos
            if b.get("parent") and b["parent"] in self.bones:
                PR, Po = get(b["parent"])
                R, off = PR @ R, PR @ off + Po
            out[name] = (R, off)
            return out[name]
        for n in self.order:
            get(n)
        return out

    def quads(self, pose):
        qs = []
        for bn in self.order:
            b = self.bones[bn]
            BR, Bo = pose[bn]
            for c in b.get("cubes", []):
                o, s = c["origin"], c["size"]
                xs = (-(o[0] + s[0]), -o[0])
                ys = (o[1], o[1] + s[1])
                zs = (o[2], o[2] + s[2])
                if "rotation" in c:
                    cp = c.get("pivot", [o[0] + s[0] / 2, o[1] + s[1] / 2, o[2] + s[2] / 2])
                    cpiv = np.array([-cp[0], cp[1], cp[2]], float)
                    CR = rot_mat(*c["rotation"])
                else:
                    cpiv, CR = np.zeros(3), np.eye(3)
                for fn, (nrm, corners) in FACE_DEF.items():
                    uv = c["uv"][fn]
                    u0, v0 = uv["uv"]
                    uw, vh = uv["uv_size"]
                    pts, tcs = [], []
                    for (xi, yi, zi, ss, tt) in corners:
                        p = np.array([xs[1 - xi], ys[yi], zs[zi]], float)
                        p = cpiv + CR @ (p - cpiv)
                        p = BR @ p + Bo
                        pts.append(p)
                        tcs.append((u0 + ss * uw, v0 + tt * vh))
                    n = np.array([-nrm[0], nrm[1], nrm[2]], float)
                    n = BR @ (CR @ n)
                    qs.append((np.array(pts), np.array(tcs), n, fn))
        return qs

    def render(self, anim=None, t=0.0, yaw=0.0, pitch=10.0, scale=8.0, size=(560, 640), ss=2, bg=(34, 36, 40), glow=True,
               center=None, flat=False):
        pose = self.pose(anim, t)
        qs = self.quads(pose)
        W, H = size[0] * ss, size[1] * ss
        sc = scale * ss
        cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
        cp, sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))
        Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
        Rp = np.array([[1, 0, 0], [0, cp, sp], [0, -sp, cp]])
        V = Rp @ Ry
        if center is None:
            center = np.array([0, self.desc["visible_bounds_height"] * 8 - 6, 0])
        zbuf = np.full((H, W), 1e9)
        img = np.zeros((H, W, 3), np.float32)
        img[...] = bg
        light = np.array([-0.35, 0.8, -0.5])
        light /= np.linalg.norm(light)
        tex, gl = self.tex, self.glow
        th, tw = tex.shape[:2]
        for pts, tcs, n, fn in qs:
            P = (pts - center) @ V.T
            nv = V @ n
            if nv[2] > 0.02:       # back face: the cube is closed, skip
                continue
            sx = W / 2 + P[:, 0] * sc
            syy = H / 2 - P[:, 1] * sc
            z = P[:, 2]
            shade = 1.0 if flat else 0.6 + 0.4 * max(0.0, float(np.dot(nv, light)))
            for i0, i1, i2 in ((0, 1, 2), (0, 2, 3)):
                x0, y0, x1, y1, x2, y2 = sx[i0], syy[i0], sx[i1], syy[i1], sx[i2], syy[i2]
                minx, maxx = int(max(0, math.floor(min(x0, x1, x2)))), int(min(W - 1, math.ceil(max(x0, x1, x2))))
                miny, maxy = int(max(0, math.floor(min(y0, y1, y2)))), int(min(H - 1, math.ceil(max(y0, y1, y2))))
                if maxx < minx or maxy < miny:
                    continue
                den = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
                if abs(den) < 1e-9:
                    continue
                gx, gy = np.meshgrid(np.arange(minx, maxx + 1) + 0.5, np.arange(miny, maxy + 1) + 0.5)
                l0 = ((y1 - y2) * (gx - x2) + (x2 - x1) * (gy - y2)) / den
                l1 = ((y2 - y0) * (gx - x2) + (x0 - x2) * (gy - y2)) / den
                l2 = 1 - l0 - l1
                m = (l0 >= -1e-6) & (l1 >= -1e-6) & (l2 >= -1e-6)
                if not m.any():
                    continue
                zz = l0 * z[i0] + l1 * z[i1] + l2 * z[i2]
                u = l0 * tcs[i0][0] + l1 * tcs[i1][0] + l2 * tcs[i2][0]
                v = l0 * tcs[i0][1] + l1 * tcs[i1][1] + l2 * tcs[i2][1]
                ui = np.clip(np.floor(u).astype(int), 0, tw - 1)
                vi = np.clip(np.floor(v).astype(int), 0, th - 1)
                col = tex[vi, ui]
                m &= col[..., 3] > 128
                sub = zbuf[miny:maxy + 1, minx:maxx + 1]
                m &= zz < sub
                if not m.any():
                    continue
                rgb = col[..., :3].astype(np.float32) * shade
                if glow and gl is not None:
                    g = gl[vi, ui]
                    gm = g[..., 3] > 128
                    rgb = np.where(gm[..., None], g[..., :3].astype(np.float32) * 1.08, rgb)
                sub[m] = zz[m]
                region = img[miny:maxy + 1, minx:maxx + 1]
                region[m] = rgb[m]
        im = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8))
        if ss > 1:
            im = im.resize(size, Image.LANCZOS)
        return im


def sheet(rig, path, title=None, size=(420, 520)):
    """Contact sheet: front, 3/4, side, back (idle); walk and attack frames; fly frame for flyers."""
    h = rig.desc["visible_bounds_height"]
    scale = size[1] / (h * 16 + 10) * 0.95
    tiles = [(lab, rig.render(None, 0, yaw, pit, scale, size)) for lab, yaw, pit in
             (("front", 0, 6), ("3/4", 38, 10), ("side", 90, 6), ("back", 180, 6))]
    ex = []
    for a, fr in (("walk", (0.25, 0.75)), ("attack", (0.3, 0.55)), ("fly", (0.25,))):
        if a in rig.anims:
            L = rig.anims[a].get("animation_length", 1.0)
            for f in fr:
                ex.append((f"{a} {f}", rig.render(a, L * f, 32, 10, scale, size)))
    ex = ex[:4]
    sw, sh = size
    rows = 1 + (1 if ex else 0)
    out = Image.new("RGB", (4 * sw, rows * sh + 26), (24, 25, 28))
    d = ImageDraw.Draw(out)
    d.text((8, 8), title or rig.name, fill=(230, 230, 230))
    for i, (lab, im) in enumerate(tiles):
        out.paste(im, (i * sw, 26))
        d.text((i * sw + 6, 30), lab, fill=(200, 200, 200))
    for i, (lab, im) in enumerate(ex):
        out.paste(im, (i * sw, 26 + sh))
        d.text((i * sw + 6, 30 + sh), lab, fill=(200, 200, 200))
    out.save(path)
    return out


def atlas_sheet(rig, path, bg=(70, 70, 76)):
    a = Image.fromarray(rig.tex)
    b = Image.new("RGBA", a.size, bg + (255,))
    b.alpha_composite(a)
    b.convert("RGB").save(path)
