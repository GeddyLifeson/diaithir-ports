#!/usr/bin/env python3 -I
"""Fold unmodified open-licence mod jars into one jar; every modId stays, so no id migration.

usage: fold_open_mods.py OUT.jar LICENCE_SPDX_EXPR MOD.jar...
Per mod, root LICENSE/NOTICE/pack-icon files move to licenses/<modId>/ and its access transformer to META-INF/<modId>.at.cfg
(declared with [[accessTransformers]]); then patch_module.py merges the jars. Reports every conflict it keeps."""
import io, os, re, sys, tempfile, zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import patch_module

TOML, AT = "META-INF/neoforge.mods.toml", "META-INF/accesstransformer.cfg"
MOVE = re.compile(r"^(LICENSE|LICENCE|NOTICE|COPYING|icon\.png|logo\.png)", re.I)


def prep(src, dst):
    zi = zipfile.ZipFile(src)
    toml = zi.read(TOML).decode()
    mid = re.search(r"(?m)^\s*modId\s*=\s*[\"']([^\"']+)", toml).group(1)
    moved = {n: f"licenses/{mid}/{n}" for n in zi.namelist() if MOVE.match(n)}
    for n, new in moved.items():
        toml = re.sub(r"(logoFile\s*=\s*[\"'])" + re.escape(n), lambda m: m.group(1) + new, toml)
    if AT in zi.namelist():
        moved[AT] = f"META-INF/{mid}.at.cfg"
        toml = toml.rstrip("\n") + f'\n\n[[accessTransformers]]\nfile="{moved[AT]}"\n'
    with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zo:
        for i in zi.infolist():
            if i.is_dir(): continue
            zo.writestr(moved.get(i.filename, i.filename), toml.encode() if i.filename == TOML else zi.read(i.filename))
    return mid


def main(a):
    out, lic, jars = a[0], a[1], a[2:]
    with tempfile.TemporaryDirectory() as t:
        prepped = []
        for j in jars:
            p = os.path.join(t, os.path.basename(j))
            print(prep(j, p), os.path.basename(j))
            prepped.append(p)
        patch_module.main([prepped[0], out] + ["--add"] + prepped[1:])
    z = zipfile.ZipFile(out)
    data = {i.filename: z.read(i.filename) for i in z.infolist()}
    data[TOML] = re.sub(r"(?m)^license\s*=.*$", f'license="{lic}"', data[TOML].decode(), count=1).encode()
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zo:
        for n, d in sorted(data.items()):
            zo.writestr(zipfile.ZipInfo(n, (2026, 11, 2, 0, 0, 0)), d, zipfile.ZIP_DEFLATED)


if __name__ == "__main__":
    main(sys.argv[1:])
