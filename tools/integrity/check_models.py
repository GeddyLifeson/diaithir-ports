"""Find model texture and parent references that nothing in the pack supplies (checklist B9, B10).

Usage: python3 -I tools/integrity/check_models.py <pack_root> [namespace ...]
Reads every hosted jar, kubejs/assets and resourcepacks. Mods linked by .pw.toml are not
downloaded, so a namespace owned by a linked mod shows as unresolved: judge those separately.
"""
import glob
import json
import os
import sys
import zipfile
from collections import Counter

root = sys.argv[1]
only = set(sys.argv[2:])
os.chdir(root)

have = set()
models = []  # (source, namespace, id, json text)


def add(path, read, source):
    parts = path.split("/")
    if len(parts) < 4 or parts[0] != "assets":
        return
    ns, kind, rest = parts[1], parts[2], "/".join(parts[3:])
    if kind == "textures" and rest.endswith(".png"):
        have.add(f"{ns}:{rest[:-4]}")
    elif kind == "models" and rest.endswith(".json"):
        have.add(f"{ns}:models/{rest[:-5]}")
        models.append((source, ns, rest[:-5], read))


for j in sorted(glob.glob("mods/*.jar") + glob.glob("client/*.jar")):
    z = zipfile.ZipFile(j)
    for n in z.namelist():
        add(n, lambda z=z, n=n: z.read(n), os.path.basename(j))
for base in ["kubejs"] + glob.glob("resourcepacks/*") + glob.glob("client/resourcepacks/*"):
    if os.path.isdir(base):
        for p in glob.glob(base + "/**/assets/**/*", recursive=True) + (glob.glob(base + "/assets/**/*", recursive=True) if base == "kubejs" else []):
            if os.path.isfile(p):
                rel = p[p.index("assets/"):]
                add(rel, lambda p=p: open(p, "rb").read(), base)
    elif base.endswith(".zip"):
        pass
for zp in glob.glob("resourcepacks/*.zip") + glob.glob("client/resourcepacks/*.zip"):
    z = zipfile.ZipFile(zp)
    for n in z.namelist():
        add(n, lambda z=z, n=n: z.read(n), os.path.basename(zp))


def norm(ref, kind):
    ref = ref.lstrip("#")
    if ":" not in ref:
        ref = "minecraft:" + ref
    ns, rest = ref.split(":", 1)
    return ns, f"{ns}:{rest}" if kind == "tex" else f"{ns}:models/{rest}"


missing_tex, missing_parent = Counter(), Counter()
samples = {}
for source, ns, mid, read in models:
    try:
        data = json.loads(read())
    except Exception:
        continue
    refs = []
    if isinstance(data.get("parent"), str):
        refs.append(("parent", data["parent"]))
    for v in (data.get("textures") or {}).values():
        if isinstance(v, str) and not v.startswith("#"):
            refs.append(("tex", v))
    for kind, ref in refs:
        rns, key = norm(ref, kind)
        if rns == "minecraft" or (only and rns not in only):
            continue
        if key not in have and not key.startswith(("minecraft:models/builtin", "minecraft:models/item/generated")):
            (missing_tex if kind == "tex" else missing_parent)[rns] += 1
            samples.setdefault(rns, []).append(f"{source}:{ns}:{mid} -> {ref}")

print(f"{len(have)} ids supplied, {len(models)} models read")
print("unresolved textures", sum(missing_tex.values()), missing_tex.most_common(15))
print("unresolved parents ", sum(missing_parent.values()), missing_parent.most_common(15))
for ns in only:
    for line in samples.get(ns, [])[:10]:
        print(ns, line)
