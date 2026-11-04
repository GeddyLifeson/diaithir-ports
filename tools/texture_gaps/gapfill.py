#!/usr/bin/env python3
"""Fills unresolved model texture references and missing parent models of the assembled pack.
Reads every jar in MODS_DIR (our jars plus the downloaded mods), finds model textures that exist nowhere and parent
models that exist nowhere, and writes stand-ins as a resource overlay in OUT (kubejs/assets in the pack, which the game
loads as a resource pack, so no new jar). Never overwrites a file a jar already has. Own code, MIT.
Usage: python3 -I tools/texture_gaps/gapfill.py MODS_DIR OUT [--skip-ns NS ...] [--report FILE]
Texture stand-in, best first: same texture under the 1.21 name (blocks/ -> block/, items/ -> item/), same file name in the
same mod, closest name in the same mod and folder, else a flat tile in the model's own colour. Parent stand-in: closest
existing model in the same mod, else cube_all / item/generated on the closest texture."""
import difflib, glob, io, json, os, re, sys, zipfile, colorsys, hashlib
from PIL import Image

args = sys.argv[1:]
skip = set()
report = None
while "--skip-ns" in args:
    i = args.index("--skip-ns"); skip.add(args[i + 1]); del args[i:i + 2]
if "--report" in args:
    i = args.index("--report"); report = args[i + 1]; del args[i:i + 2]
MODS, OUT = args

tex, models, png = {}, {}, {}
for j in sorted(glob.glob(MODS + "/*.jar")):
    z = zipfile.ZipFile(j)
    for n in z.namelist():
        m = re.match(r"assets/([^/]+)/(textures|models)/(.*)\.(png|json)$", n)
        if not m:
            continue
        ns, kind, p = m.group(1), m.group(2), m.group(3)
        if kind == "textures" and m.group(4) == "png":
            tex[f"{ns}:{p}"] = (z, n)
        elif kind == "models" and m.group(4) == "json":
            try:
                models[f"{ns}:{p}"] = json.loads(z.read(n))
            except Exception:
                pass
nm = lambda r: r if ":" in r else "minecraft:" + r
ext = lambda t: t.startswith(("minecraft:", "neoforge:", "builtin"))
by_ns = {}
for k in tex:
    by_ns.setdefault(k.split(":")[0], []).append(k)
mby_ns = {}
for k in models:
    mby_ns.setdefault(k.split(":")[0], []).append(k)

miss_tex, miss_par = {}, {}
for mid, m in models.items():
    if mid.split(":")[0] in skip:
        continue
    for v in (m.get("textures") or {}).values():
        if isinstance(v, str) and not v.startswith("#"):
            t = nm(v)
            if not ext(t) and t not in tex and not re.search(r"/_[a-z]+_(/|$)", t):
                miss_tex.setdefault(t, set()).add(mid)
    p = m.get("parent")
    if p:
        p = nm(p)
        if not ext(p) and p not in models:
            miss_par.setdefault(p, set()).add(mid)


def closest(key, pool, cut):
    ns, p = key.split(":", 1)
    folder = p.split("/")[0]
    c = [k for k in pool if k.split(":", 1)[1].split("/")[0] == folder] or pool
    r = difflib.get_close_matches(key, c, 1, cut)
    return r[0] if r else None


def first_frame(img):
    w, h = img.size
    return img.crop((0, 0, w, w)) if h > w else img


HUES = [("ember", 18, .8, .8), ("magma", 12, .85, .8), ("frost", 200, .35, .85), ("ice", 195, .35, .9), ("glow", 170, .6, .85),
        ("biolum", 150, .6, .75), ("crystal", 185, .5, .85), ("spore", 285, .45, .65), ("fung", 300, .4, .6), ("rust", 25, .6, .5),
        ("void", 270, .5, .22), ("ash", 0, 0, .4), ("meteor", 15, .3, .3), ("vent", 10, .25, .3), ("moss", 100, .5, .45),
        ("stone", 0, 0, .5), ("compass", 45, .5, .85), ("paste", 40, .1, .85), ("construction", 35, .15, .7), ("pipe", 205, .2, .55)]


def flat(key):
    """A seeded 64 px noise tile (block) or round sprite (item) in a colour taken from the name."""
    import random
    name = key.split(":", 1)[1]
    rnd = random.Random(key)
    h, s, v = (int(hashlib.md5(key.encode()).hexdigest()[:4], 16) % 360, .3, .6)
    for w, hh, ss, vv in HUES:
        if w in name:
            h, s, v = hh, ss, vv
            break
    n = 64
    cell = [[rnd.uniform(-1, 1) for _ in range(9)] for _ in range(9)]
    im = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    item = name.split("/")[0] in ("item", "items")
    for y in range(n):
        for x in range(n):
            gx, gy = x / 8, y / 8
            i, j, fx, fy = int(gx), int(gy), gx % 1, gy % 1
            nv = (cell[j][i] * (1 - fx) + cell[j][i + 1] * fx) * (1 - fy) + (cell[j + 1][i] * (1 - fx) + cell[j + 1][i + 1] * fx) * fy
            vv = max(0, min(1, v + nv * .12 + rnd.uniform(-.04, .04)))
            if item:
                d = ((x - 31.5) ** 2 + (y - 31.5) ** 2) ** .5
                if d > 22: continue
                vv = max(0, min(1, vv * (1.25 - d / 44) * (0.45 if d > 20 else 1)))
            r, g, b = colorsys.hsv_to_rgb(h / 360, s, vv)
            im.putpixel((x, y), (int(r * 255), int(g * 255), int(b * 255), 255))
    return im


rows = []
def put(path, data):
    p = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    if isinstance(data, Image.Image):
        data.save(p)
    else:
        with open(p, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=1); f.write("\n")

for t, users in sorted(miss_tex.items()):
    ns, p = t.split(":", 1)
    pool = by_ns.get(ns, [])
    alts = [t.replace(":blocks/", ":block/"), t.replace(":items/", ":item/"), t.replace("/blocks/", "/block/"), t.replace("/items/", "/item/")]
    src = next((a for a in alts if a != t and a in tex), None)
    how = "renamed"
    if not src:
        b = p.rsplit("/", 1)[-1]
        same = [k for k in pool if k.rsplit("/", 1)[-1] == b]
        src, how = (same[0], "same-name") if same else (None, "")
    if not src:
        base = p.rsplit("/", 1)[-1]
        sib = [k for k in tex if k.split(":")[0] != ns and k.split(":")[0].startswith(ns) and k.rsplit("/", 1)[-1].replace("_", "") == base.replace("_", "")]
        if sib:
            src, how = sib[0], "versioned-mod"
    if not src and pool:
        src, how = closest(t, pool, 0.6), "closest"
    if src:
        z, n = tex[src]
        try:
            im = first_frame(Image.open(io.BytesIO(z.read(n))).convert("RGBA"))
        except Exception:
            im, how, src = flat(t), "flat", None
    else:
        im, how = flat(t), "flat"
    put(f"{ns}/textures/{p}.png", im)
    rows.append(("texture", t, how, src or "", sorted(users)[0]))

for p, users in sorted(miss_par.items()):
    ns, path = p.split(":", 1)
    pool = [k for k in mby_ns.get(ns, []) if k != p]
    src = closest(p, pool, 0.75)
    if src:
        put(f"{ns}/models/{path}.json", models[src]); how = "sibling model"
    else:
        tp = by_ns.get(ns, [])
        want = next(iter(sorted(users)))
        tk = closest(f"{ns}:{'block' if '/block/' in '/'+path else 'item'}/{path.rsplit('/',1)[-1]}", tp, 0.5)
        if tk and path.startswith("item/"):
            data = {"parent": "minecraft:item/generated", "textures": {"layer0": tk}}
        elif tk:
            data = {"parent": "minecraft:block/cube_all", "textures": {"all": tk}}
        else:
            put(f"{ns}/textures/block/_gap_{path.rsplit('/',1)[-1]}.png", flat(p))
            data = {"parent": "minecraft:block/cube_all", "textures": {"all": f"{ns}:block/_gap_{path.rsplit('/',1)[-1]}"}}
        put(f"{ns}/models/{path}.json", data); how = "cube/generated"; src = tk or ""
    rows.append(("parent", p, how, src, sorted(users)[0]))

print(len(miss_tex), "textures,", len(miss_par), "parents filled")
if report:
    with open(report, "w") as f:
        f.write("kind\tmissing\tfix\tsource\tfirst_user\n")
        for r in rows: f.write("\t".join(r) + "\n")
