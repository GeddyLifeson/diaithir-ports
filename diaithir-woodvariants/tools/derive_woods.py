#!/usr/bin/env python3
"""Derives woods.json rows from the pack dump and the pack's mod jars.
Usage: python3 -I tools/derive_woods.py PACK_DUMP.json.gz MODS_DIR [MODS_DIR...]
A wood is any `<ns>:<name>_planks` item of a mod whose jar is in a MODS_DIR (so dump entries from jars of
other versions are ignored), whose planks texture can be followed through blockstate and model files.
Existing rows keep their hand-set names. Own code, MIT."""
import gzip, json, os, re, sys, zipfile, tomllib

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = f"{HERE}/src/main/resources/data/diaithirwoodvariants/woods.json"
NAMES = f"{HERE}/tools/names_dia.json"      # wood id -> genitive in Diaithiris (hand or job made)
SKIP_NS = {"minecraft", "chisel", "securitycraft", "bblcore"}
SKIP_WORDS = ("painted", "fireproof", "reinforced", "_base_", "_top_", "base_planks", "top_planks", "beam", "floorboard")
PREFIX = {"aether_ii": "aii", "biomesoplenty": "bop", "productivetrees": "pt", "regions_unexplored": "ru",
          "twilightforest": "tf", "tconstruct": "tc", "forbidden_arcanus": "fa", "iceandfire": "iaf",
          "ecologics": "eco", "integrateddynamics": "id", "silentgear": "sg", "evilcraft": "ec", "vampirism": "vp",
          "stellaris": "st"}


def load_jars(dirs):
    files, mods = {}, {}
    for d in dirs:
        for f in sorted(os.listdir(d)):
            if not f.endswith(".jar"):
                continue
            z = zipfile.ZipFile(os.path.join(d, f))
            names = set(z.namelist())
            try:
                t = tomllib.loads(z.read("META-INF/neoforge.mods.toml").decode("utf-8", "replace"))
                ids = [m["modId"] for m in t.get("mods", [])]
            except Exception:
                ids = []
            for n in names:
                m = re.match(r"assets/([^/]+)/", n)
                if m:
                    files.setdefault(m.group(1), []).append((z, names, ids[0] if ids else m.group(1)))
    return files, mods


def read(files, ns, path):
    if ns not in files:
        return None
    p = f"assets/{ns}/{path}"
    for z, names, _ in files[ns]:
        if p in names:
            return json.loads(z.read(p))
    return None


def owner(files, ns, path):
    p = f"assets/{ns}/{path}"
    for _, names, mod in files.get(ns, []):
        if p in names:
            return mod
    return None


def split(ref, ns):
    return ref.split(":", 1) if ":" in ref else (ns, ref)


def textures_of(files, ns, model, depth=0):
    """texture map of a model following parents; keys resolved through #refs"""
    ns, name = split(model, ns)
    m = read(files, ns, f"models/{name}.json")
    if not m or depth > 6:
        return {}
    t = {}
    if "parent" in m:
        pns, pn = split(m["parent"], ns)
        if pns != "minecraft":
            t.update(textures_of(files, pns, m["parent"], depth + 1))
    t.update(m.get("textures", {}))
    for k, v in list(t.items()):
        while isinstance(v, str) and v.startswith("#") and v[1:] in t and t[v[1:]] != v:
            v = t[v[1:]]
        t[k] = v
    return t


def first_model(state):
    if "variants" in state:
        v = next(iter(state["variants"].values()))
        v = v[0] if isinstance(v, list) else v
        return v["model"]
    if "multipart" in state:
        a = state["multipart"][0]["apply"]
        a = a[0] if isinstance(a, list) else a
        return a["model"]
    return None


def block_textures(files, ns, name):
    st = read(files, ns, f"blockstates/{name}.json")
    mdl = first_model(st) if st else None
    return textures_of(files, ns, mdl) if mdl else {}


def tex_ok(files, ref, ns):
    n, p = split(ref, ns)
    return n in files and any(f"assets/{n}/textures/{p}.png" in nm for _, nm, _m in files[n])


def pick(t, *keys):
    for k in keys:
        v = t.get(k)
        if isinstance(v, str) and not v.startswith("#"):
            return v
    return None


def main():
    dump = json.load(gzip.open(sys.argv[1]))
    files, mods = load_jars(sys.argv[2:])
    names = json.load(open(NAMES, encoding="utf-8")) if os.path.exists(NAMES) else {}
    old = {w["id"]: w for w in json.load(open(OUT, encoding="utf-8"))["woods"]} if os.path.exists(OUT) else {}
    rows, skipped = [], []
    for item in sorted(set(dump["items"])):
        ns, n = split(item, "")
        if not n.endswith("_planks") or ns in SKIP_NS or any(w in n for w in SKIP_WORDS) or ns not in files:
            continue
        base = n[:-7]
        pt = pick(block_textures(files, ns, n), "all", "particle", "texture", "side")
        if not pt or not tex_ok(files, pt, ns):
            skipped.append((item, "no planks texture"))
            continue
        lt, ltop, logid = None, None, None
        for cand in (f"{base}_log", f"{base}_stem", f"{base}_wood", f"{base}_hyphae"):
            t = block_textures(files, ns, cand)
            s, e = pick(t, "side", "all", "texture"), pick(t, "end", "top", "up", "all")
            if s and tex_ok(files, s, ns):
                lt, ltop, logid = s, (e if e and tex_ok(files, e, ns) else s), f"{ns}:{cand}"
                break
        wid = f"{PREFIX.get(ns, ns[:3])}_{base}"
        row = dict(id=wid, mod=owner(files, ns, f"blockstates/{n}.json"), name=" ".join(w.capitalize() for w in base.split("_")),
                   gen=names.get(wid) or old.get(wid, {}).get("gen") or " ".join(w.capitalize() for w in base.split("_")),
                   planks=item, log=logid or item, planks_texture=pt,
                   log_texture=lt or pt, log_top_texture=ltop or pt)
        rows.append(row)
    json.dump({"_comment": "One row per wood. Add a row, run tools/gen.py, rebuild. A row is skipped at load when its mod is not installed. tools/derive_woods.py rebuilds this list from the pack dump.",
               "woods": rows}, open(OUT, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    from collections import Counter
    print(len(rows), "woods", dict(Counter(r["mod"] for r in rows)))
    print("skipped:", skipped)


main()
