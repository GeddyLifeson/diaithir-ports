#!/usr/bin/env python3 -I
"""Swap the module(s) inside a folded suite for rebuilt module jars.

usage: patch_module.py BASE.jar OUT.jar --rm-id ID ... --rm-prefix PATH_PREFIX ... --rm-identical OLD.jar ... --add NEW.jar ...
--rm-identical removes every entry byte-identical to the same path in OLD.jar (the module's previous build), so merged files stay.
Removes the [[mods]]/[[dependencies.ID]] blocks of every --rm-id and every entry under every --rm-prefix, then copies the
entries of each --add jar in (its mods.toml blocks are appended, services files are unioned). A same-path entry that already
exists and differs is reported and the existing one is kept (resolve by hand)."""
import re, sys, zipfile

TOML = "META-INF/neoforge.mods.toml"


def blocks(text):
    parts = re.split(r"(?m)^(?=\[\[)", text)
    return parts[0], parts[1:]


def head(b):
    m = re.match(r"\[\[([^\]]+)\]\]", b)
    return m.group(1) if m else ""


def main(a):
    base, out = a[0], a[1]
    rm_ids, rm_pre, adds, ident, cur = [], [], [], [], None
    for x in a[2:]:
        if x.startswith("--"): cur = x
        elif cur == "--rm-id": rm_ids.append(x)
        elif cur == "--rm-prefix": rm_pre.append(x)
        elif cur == "--add": adds.append(x)
        elif cur == "--rm-identical": ident.append(x)
    zb = zipfile.ZipFile(base)
    files = {i.filename: (i, zb.read(i.filename)) for i in zb.infolist() if not i.filename.endswith("/")}
    gone = [n for n in files if any(n.startswith(p) for p in rm_pre)]
    for oj in ident:  # drop every entry that is byte-identical to the old module jar (merged files such as tags differ and stay)
        zo = zipfile.ZipFile(oj)
        for n in zo.namelist():
            if n.endswith("/") or n in (TOML, "META-INF/MANIFEST.MF") or n not in files or n in gone: continue
            if files[n][1] == zo.read(n): gone.append(n)
    for n in gone: del files[n]
    pre, bl = blocks(files[TOML][1].decode())
    keep = []
    for b in bl:
        h = head(b)
        mid = re.search(r"modId\s*=\s*[\"']([^\"']+)", b)
        if h == "mods" and mid and mid.group(1) in rm_ids: continue
        if h.startswith("dependencies.") and h.split(".", 1)[1] in rm_ids: continue
        keep.append(b)
    existing = {re.search(r"modId\s*=\s*[\"']([^\"']+)", b).group(1) for b in keep if head(b) == "mods" and re.search(r"modId", b)}
    conflicts = []
    for aj in adds:
        za = zipfile.ZipFile(aj)
        for i in za.infolist():
            n = i.filename
            if n.endswith("/") or n in ("META-INF/MANIFEST.MF",): continue
            d = za.read(n)
            if n == TOML:
                _, nb = blocks(d.decode())
                for b in nb:
                    h = head(b); mid = re.search(r"modId\s*=\s*[\"']([^\"']+)", b)
                    if h == "mods" and mid:
                        if mid.group(1) in existing: sys.exit(f"modId {mid.group(1)} already in {base}")
                        existing.add(mid.group(1))
                    if not b.endswith("\n\n"): b = b.rstrip("\n") + "\n\n"
                    keep.append(b)
            elif n in files:
                if files[n][1] == d: continue
                if n.startswith("META-INF/services/"):
                    lines = files[n][1].decode().splitlines()
                    for l in d.decode().splitlines():
                        if l not in lines: lines.append(l)
                    files[n] = (files[n][0], ("\n".join(lines) + "\n").encode())
                elif n.endswith(".json") and ("/tags/" in n or "/data_maps/" in n):
                    import json
                    o, w = json.loads(files[n][1]), json.loads(d)
                    if "values" in o and isinstance(o["values"], list):
                        for v in w.get("values", []):
                            if v not in o["values"]: o["values"].append(v)
                    elif isinstance(o.get("values"), dict):
                        o["values"].update(w.get("values", {}))
                    files[n] = (files[n][0], json.dumps(o, indent=2).encode())
                else: conflicts.append(n)
            else:
                zi = zipfile.ZipInfo(n, (2026, 11, 2, 0, 0, 0)); zi.compress_type = zipfile.ZIP_DEFLATED
                files[n] = (zi, d)
    keep = [b if b.endswith("\n\n") else b.rstrip("\n") + "\n\n" for b in keep]
    files[TOML] = (files[TOML][0], (pre + "".join(keep)).rstrip("\n").encode() + b"\n")
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zo:
        for n, (i, d) in sorted(files.items()):
            zi = zipfile.ZipInfo(n, (2026, 11, 2, 0, 0, 0)); zi.compress_type = zipfile.ZIP_DEFLATED; zi.external_attr = 0o644 << 16
            zo.writestr(zi, d)
    print(f"removed {len(gone)} entries, ids {rm_ids}; conflicts kept: {conflicts}")


if __name__ == "__main__":
    main(sys.argv[1:])
