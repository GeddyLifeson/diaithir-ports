"""Tiny isometric block-model renderer for contact sheets (vanilla gui angle, light from the upper left).
Handles element models, the vanilla cube parents and explicit uv. Used only to review our own art."""
import json, math, os, sys
import numpy as np
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(HERE + "/../../src/main/resources/assets/create")

BUILTIN = {
    "minecraft:block/cube_all": lambda t: {"elements": [_cube({k: "#all" for k in FACES})], "textures": t},
    "minecraft:block/cube_column": lambda t: {"elements": [_cube({"up": "#end", "down": "#end", "north": "#side", "south": "#side", "east": "#side", "west": "#side"})], "textures": t},
    "minecraft:block/cube_bottom_top": lambda t: {"elements": [_cube({"up": "#top", "down": "#bottom", "north": "#side", "south": "#side", "east": "#side", "west": "#side"})], "textures": t},
    "minecraft:block/cube": lambda t: {"elements": [_cube({k: "#" + k for k in FACES})], "textures": t},
}
def _el(f, t, m): return {"from": f, "to": t, "faces": {k: {"texture": v} for k, v in m.items()}}
_SIDES = {"north": "#side", "south": "#side", "east": "#side", "west": "#side"}
BUILTIN.update({
    "minecraft:block/slab": lambda t: {"elements": [_el([0, 0, 0], [16, 8, 16], {**_SIDES, "up": "#top", "down": "#bottom"})], "textures": t},
    "minecraft:block/stairs": lambda t: {"elements": [_el([0, 0, 0], [16, 8, 16], {**_SIDES, "up": "#top", "down": "#bottom"}),
                                                      _el([8, 8, 0], [16, 16, 16], {**_SIDES, "up": "#top", "down": "#bottom"})], "textures": t},
    "minecraft:block/wall_inventory": lambda t: {"elements": [_el([4, 0, 4], [12, 16, 12], {**_SIDES, "up": "#wall", "down": "#wall"}) if False else _el([4, 0, 4], [12, 16, 12], {k: "#wall" for k in FACES})], "textures": t},
    "minecraft:block/template_glass_pane_post": lambda t: {"elements": [_el([0, 0, 7], [16, 16, 9], {"north": "#pane", "south": "#pane", "east": "#edge", "west": "#edge", "up": "#edge", "down": "#edge"})], "textures": t},
})
FACES = ("north", "south", "east", "west", "up", "down")


def _cube(m):
    return {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {f: {"texture": t} for f, t in m.items()}}


def load_model(ref, depth=0):
    if ref in BUILTIN:
        return None
    name = ref.split(":")[-1]
    ns = ref.split(":")[0] if ":" in ref else "minecraft"
    if ns != "create":
        return None
    p = f"{ASSETS}/models/{name}.json"
    if not os.path.exists(p):
        return None
    return json.load(open(p))


def resolve(ref, depth=0):
    """returns (elements, textures)"""
    if depth > 6:
        return [], {}
    if not ref.startswith("create:") and ref in BUILTIN:
        return None
    m = load_model(ref)
    if m is None:
        return [], {}
    tex = {}
    elements = m.get("elements")
    parent = m.get("parent")
    if parent:
        if parent in BUILTIN:
            b = BUILTIN[parent](m.get("textures", {}))
            elements = elements or b["elements"]
            tex = dict(m.get("textures", {}))
        elif parent.startswith("create:"):
            pe, pt = resolve(parent, depth + 1)
            elements = elements or pe
            tex = dict(pt)
            tex.update(m.get("textures", {}))
        else:
            elements = elements or []
            tex = dict(m.get("textures", {}))
    else:
        tex = dict(m.get("textures", {}))
    return elements or [], tex


def tex_img(ref, cache={}):
    name = ref.split(":")[-1]
    if name in cache:
        return cache[name]
    p = f"{ASSETS}/textures/{name}.png"
    im = Image.open(p).convert("RGBA") if os.path.exists(p) else Image.new("RGBA", (16, 16), (255, 0, 255, 255))
    arr = np.array(im)
    if arr.shape[0] != arr.shape[1]:
        arr = arr[:arr.shape[1]]
    cache[name] = arr
    return arr


def rot_matrix(axis, ang):
    a = math.radians(ang)
    c, s = math.cos(a), math.sin(a)
    if axis == "x":
        return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])
    if axis == "y":
        return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


FACE_UV = {
    "up": lambda x0, y0, z0, x1, y1, z1: (x0, z0, x1, z1),
    "down": lambda x0, y0, z0, x1, y1, z1: (x0, 16 - z1, x1, 16 - z0),
    "north": lambda x0, y0, z0, x1, y1, z1: (16 - x1, 16 - y1, 16 - x0, 16 - y0),
    "south": lambda x0, y0, z0, x1, y1, z1: (x0, 16 - y1, x1, 16 - y0),
    "west": lambda x0, y0, z0, x1, y1, z1: (z0, 16 - y1, z1, 16 - y0),
    "east": lambda x0, y0, z0, x1, y1, z1: (16 - z1, 16 - y1, 16 - z0, 16 - y0),
}
NORMAL = {"up": (0, 1, 0), "down": (0, -1, 0), "north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0)}


def quad(face, f, t):
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {
        "up": [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
        "down": [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)],
        "north": [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)],
        "south": [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)],
        "west": [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)],
        "east": [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)],
    }[face]


def render(ref, size=256, pitch=30, yaw=225, bg=(36, 36, 40, 255), scale=None):
    els, tex = resolve(ref)

    def tref(t, depth=0):
        while isinstance(t, str) and t.startswith("#") and depth < 8:
            t = tex.get(t[1:], "")
            depth += 1
        return t

    img = np.zeros((size, size, 4), dtype=np.float32)
    img[:] = np.array(bg, dtype=np.float32)
    zbuf = np.full((size, size), -1e9, dtype=np.float32)
    R = rot_matrix("x", pitch) @ rot_matrix("y", yaw)
    sc = scale or size / 26.0
    for e in els:
        f, t = e["from"], e["to"]
        for face, fd in e.get("faces", {}).items():
            tx = tref(fd.get("texture", ""))
            if not tx:
                continue
            a = tex_img(tx)
            th, tw = a.shape[:2]
            uv = fd.get("uv") or FACE_UV[face](*f, *t)
            pts = np.array(quad(face, f, t), dtype=np.float64)
            nrm = np.array(NORMAL[face], dtype=np.float64)
            if "rotation" in e:
                ro = e["rotation"]
                Rm = rot_matrix(ro["axis"], ro["angle"])
                o = np.array(ro["origin"], dtype=np.float64)
                pts = (pts - o) @ Rm.T + o
                nrm = Rm @ nrm
            cen = np.array([8, 8, 8], dtype=np.float64)
            P = (pts - cen) @ R.T
            n = R @ nrm
            if n[2] < -0.02 and "rotation" not in e and False:
                continue
            scr = np.stack([size / 2 + P[:, 0] * sc, size / 2 - P[:, 1] * sc], axis=1)
            dep = P[:, 2]
            # light from the upper left toward the viewer
            L = np.array([-0.55, 0.75, 0.45])
            L /= np.linalg.norm(L)
            lit = 0.55 + 0.5 * max(0.0, float(n @ L))
            u0, v0, u1, v1 = uv
            corner_uv = [(u0, v0), (u1, v0), (u1, v1), (u0, v1)]
            for tri in ((0, 1, 2), (0, 2, 3)):
                A, B, C = scr[list(tri)]
                minx, maxx = int(max(0, math.floor(min(A[0], B[0], C[0])))), int(min(size - 1, math.ceil(max(A[0], B[0], C[0]))))
                miny, maxy = int(max(0, math.floor(min(A[1], B[1], C[1])))), int(min(size - 1, math.ceil(max(A[1], B[1], C[1]))))
                if maxx < minx or maxy < miny:
                    continue
                den = (B[1] - C[1]) * (A[0] - C[0]) + (C[0] - B[0]) * (A[1] - C[1])
                if abs(den) < 1e-9:
                    continue
                ys, xs = np.mgrid[miny:maxy + 1, minx:maxx + 1]
                px_, py_ = xs + 0.5, ys + 0.5
                w1 = ((B[1] - C[1]) * (px_ - C[0]) + (C[0] - B[0]) * (py_ - C[1])) / den
                w2 = ((C[1] - A[1]) * (px_ - C[0]) + (A[0] - C[0]) * (py_ - C[1])) / den
                w3 = 1 - w1 - w2
                m = (w1 >= -1e-4) & (w2 >= -1e-4) & (w3 >= -1e-4)
                z = w1 * dep[tri[0]] + w2 * dep[tri[1]] + w3 * dep[tri[2]]
                uu = w1 * corner_uv[tri[0]][0] + w2 * corner_uv[tri[1]][0] + w3 * corner_uv[tri[2]][0]
                vv = w1 * corner_uv[tri[0]][1] + w2 * corner_uv[tri[1]][1] + w3 * corner_uv[tri[2]][1]
                ti = np.clip((uu / 16 * tw).astype(int), 0, tw - 1)
                tj = np.clip((vv / 16 * th).astype(int), 0, th - 1)
                col = a[tj, ti].astype(np.float32)
                ok = m & (z > zbuf[miny:maxy + 1, minx:maxx + 1]) & (col[..., 3] > 20)
                sub = img[miny:maxy + 1, minx:maxx + 1]
                al = (col[..., 3:4] / 255.0)
                shaded = col[..., :3] * lit
                sub[ok] = np.concatenate([shaded[ok] * al[ok] + sub[ok][:, :3] * (1 - al[ok]), np.full((ok.sum(), 1), 255.0)], axis=1)
                zs = zbuf[miny:maxy + 1, minx:maxx + 1]
                zs[ok & (col[..., 3] > 200)] = z[ok & (col[..., 3] > 200)]
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def first_model(block_id):
    p = f"{ASSETS}/blockstates/{block_id}.json"
    if not os.path.exists(p):
        return None
    d = json.load(open(p))
    if "variants" in d:
        v = d["variants"]
        v = v[next(iter(v))]
        if isinstance(v, list):
            v = v[0]
        return v["model"]
    if "multipart" in d:
        part = d["multipart"][0]["apply"]
        if isinstance(part, list):
            part = part[0]
        return part["model"]
    return None


def item_model(item_id):
    p = f"{ASSETS}/items/{item_id}.json"
    if not os.path.exists(p):
        return None
    d = json.load(open(p))
    m = d.get("model", {})
    return m.get("model")


def sheet(entries, out, cols=6, cell=256, label=True):
    """entries: list of (label, ('model', ref) | ('icon', itemid))"""
    rows = (len(entries) + cols - 1) // cols
    W = Image.new("RGBA", (cols * cell, rows * (cell + 18)), (30, 30, 34, 255))
    d = ImageDraw.Draw(W)
    for i, (lab, (kind, ref)) in enumerate(entries):
        x, y = (i % cols) * cell, (i // cols) * (cell + 18)
        if kind == "model":
            im = render(ref, cell, scale=(cell / 62.0 if 'water_wheel' in ref else None))
        else:
            p = f"{ASSETS}/textures/item/{ref}.png"
            im = Image.new("RGBA", (cell, cell), (36, 36, 40, 255))
            if os.path.exists(p):
                ic = Image.open(p).convert("RGBA")
                big = ic.resize((cell * 3 // 4, cell * 3 // 4), Image.NEAREST)
                im.paste(big, (cell // 8, 2), big)
                sm = ic.resize((16, 16), Image.BOX).resize((cell // 4, cell // 4), Image.NEAREST)
                im.paste(sm, (cell - cell // 4 - 2, cell - cell // 4 - 2), sm)
            else:
                d2 = ImageDraw.Draw(im)
                d2.text((10, 10), "MISSING", fill=(255, 0, 255, 255))
        W.paste(im, (x, y + 18))
        d.text((x + 4, y + 3), lab, fill=(220, 220, 220, 255))
    W.save(out)


if __name__ == "__main__":
    ref = sys.argv[1]
    render(ref, 384).save(sys.argv[2])
