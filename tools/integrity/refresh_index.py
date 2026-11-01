#!/usr/bin/env python3 -I
"""Re-sync index.toml and pack.toml with the files on disk (packwiz refresh without packwiz).
Usage: python3 -I refresh_index.py <pack_root>. Rehashes changed files, drops missing ones, adds new files under kubejs/ config/ mods/ resourcepacks/."""
import sys, os, re, hashlib, tomllib
R = sys.argv[1]
sha = lambda p: hashlib.sha256(open(p, "rb").read()).hexdigest()
old = tomllib.load(open(f"{R}/index.toml", "rb"))["files"]
ent = {f["file"]: f for f in old if os.path.exists(f"{R}/{f['file']}")}
for top in ("kubejs", "config", "mods", "resourcepacks"):
    for dp, _, fs in os.walk(f"{R}/{top}"):
        for fn in fs:
            rel = os.path.relpath(f"{dp}/{fn}", R).replace(os.sep, "/")
            if rel not in ent: ent[rel] = {"file": rel, **({"metafile": True} if rel.endswith(".pw.toml") else {})}
out = ['hash-format = "sha256"', ""]
for rel in sorted(ent):
    e = ent[rel]
    out += ["[[files]]", f'file = "{rel}"', f'hash = "{sha(f"{R}/{rel}")}"'] + (["metafile = true"] if e.get("metafile") else []) + [""]
open(f"{R}/index.toml", "w", newline="\n").write("\n".join(out))
p = open(f"{R}/pack.toml").read()
p = re.sub(r'(\[index\][^\[]*hash = ")[0-9a-f]+', r"\g<1>" + sha(f"{R}/index.toml"), p)
open(f"{R}/pack.toml", "w").write(p)
print(len(ent), "files indexed")
