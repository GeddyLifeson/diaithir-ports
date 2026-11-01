#!/usr/bin/env python3 -I
"""Static integrity + optimisation audit for the Diaithir packwiz pack.
Usage: python3 -I check_pack.py <pack_root> [--items pack_item_ids.tsv] [--out report.json]
Exit 1 if any ERROR. Re-runnable, no network."""
import sys, os, re, json, hashlib, tomllib, subprocess, argparse, collections

ap = argparse.ArgumentParser()
ap.add_argument("root")
ap.add_argument("--items", default="/mnt/project-files/pack_item_ids.tsv")
ap.add_argument("--out", default="")
a = ap.parse_args()
R = a.root
E, W, I = [], [], []
def err(c, m): E.append((c, m))
def warn(c, m): W.append((c, m))

def sha(p):
    h = hashlib.sha256()
    with open(p, "rb") as f:
        for b in iter(lambda: f.read(1 << 20), b""): h.update(b)
    return h.hexdigest()

# 1 index.toml vs files
idx = tomllib.load(open(f"{R}/index.toml", "rb"))
listed = set()
for f in idx.get("files", []):
    p = f["file"]; listed.add(p)
    fp = f"{R}/{p}"
    if not os.path.exists(fp): err("index-missing", p); continue
    if f.get("hash") and sha(fp) != f["hash"]: err("index-hash", p)
pt = tomllib.load(open(f"{R}/pack.toml", "rb"))
if sha(f"{R}/index.toml") != pt["index"]["hash"]: err("pack-index-hash", "pack.toml index hash stale")
for top in ("config", "mods", "kubejs", "resourcepacks", "client"):
    for dp, _, fs in os.walk(f"{R}/{top}"):
        for fn in fs:
            rel = os.path.relpath(f"{dp}/{fn}", R)
            if rel not in listed: warn("unindexed", rel)

# 2 mods
names = collections.defaultdict(list); files = collections.Counter(); cfids = collections.defaultdict(list)
sides = collections.Counter()
for fn in sorted(os.listdir(f"{R}/mods")):
    if not fn.endswith(".pw.toml"): continue
    try: t = tomllib.load(open(f"{R}/mods/{fn}", "rb"))
    except Exception as e: err("mod-toml", f"{fn}: {e}"); continue
    for k in ("name", "filename", "download"):
        if k not in t: err("mod-field", f"{fn} lacks {k}")
    d = t.get("download", {})
    if not d.get("hash") and "update" not in t: err("mod-nohash", fn)
    if d.get("url") and not d["url"].startswith("https://"): err("mod-url", fn)
    files[t.get("filename")] += 1
    sides[t.get("side", "both")] += 1
    slug = re.sub(r"[-_ ]?\d.*$", "", t.get("filename", fn).replace(".jar", "")).lower()
    slug = re.sub(r"(neoforge|forge|fabric|mc)?$", "", slug).strip("-_ ")
    names[slug].append(fn)
    pid = t.get("update", {}).get("curseforge", {}).get("project-id")
    if pid: cfids[pid].append(fn)
for k, v in files.items():
    if v > 1: err("mod-dup-filename", k)
for k, v in cfids.items():
    if len(v) > 1: err("mod-dup-curseforge-project", f"{k}: {v}")
for k, v in names.items():
    if len(v) > 1: warn("mod-dup-slug", f"{k}: {v}")
for j in os.listdir(f"{R}/mods"):
    if j.endswith(".jar"): I.append(("local-jar", f"mods/{j} {os.path.getsize(f'{R}/mods/{j}')>>20} MB"))
I.append(("mod-count", f"{sum(files.values())} pw.toml, sides {dict(sides)}"))

# 3 kubejs
rhino_bad = [(r"\[\s*\.\.\.", "array spread (Rhino syntax error)"), (r"\?\.", "optional chaining"),
             (r"\?\?", "nullish coalescing"), (r"\bBigInt\b|\bimport\s+[\w{]", "ES module/BigInt")]
jscount = 0
for dp, _, fs in os.walk(f"{R}/kubejs"):
    for fn in fs:
        p = f"{dp}/{fn}"; rel = os.path.relpath(p, R)
        if fn.endswith(".json"):
            try: json.load(open(p, encoding="utf-8-sig"))
            except Exception as e: err("json-invalid", f"{rel}: {e}")
        elif fn.endswith(".js"):
            jscount += 1
            s = open(p, encoding="utf-8", errors="replace").read()
            for rx, m in rhino_bad:
                for mm in re.finditer(rx, re.sub(r"//.*|/\*.*?\*/|'[^'\n]*'|\"[^\"\n]*\"|`[^`]*`", "", s, flags=re.S)):
                    err("rhino-unsafe", f"{rel}: {m}"); break
            if os.path.getsize(p) > 200_000: warn("big-script", f"{rel} {os.path.getsize(p)>>10} KB")
            if "server_scripts" in rel and re.search(r"ServerEvents\.tick|PlayerEvents\.tick|LevelEvents\.tick", s):
                warn("tick-handler", f"{rel}: per-tick handler, check cost")
            if re.search(r"kick\(|\.disconnect\(", s): warn("player-kick", f"{rel}: kubejs-26-gotchas")
    # syntax via node (generic JS; Rhino-only constructs may false-positive)
for dp, _, fs in os.walk(f"{R}/kubejs"):
    for fn in fs:
        if fn.endswith(".js"):
            r = subprocess.run(["node", "--check", f"{dp}/{fn}"], capture_output=True, text=True)
            if r.returncode: warn("node-syntax", f"{os.path.relpath(dp+'/'+fn,R)}: {r.stderr.strip().splitlines()[0:4]}")
I.append(("kubejs-js", str(jscount)))

# 4 data references
items = set()
if os.path.exists(a.items):
    for l in open(a.items, encoding="utf-8"): items.add(l.split("\t")[0].strip())
def refs(o):
    if isinstance(o, dict):
        for k, v in o.items():
            if k in ("item", "id") and isinstance(v, str) and ":" in v and not v.startswith("#"): yield v
            elif k == "result" and isinstance(v, str): yield v
            else: yield from refs(v)
    elif isinstance(o, list):
        for v in o: yield from refs(v)
rec = 0; missing = collections.Counter(); miss_ex = {}
for dp, _, fs in os.walk(f"{R}/kubejs/data"):
    if not re.search(r"[\\/]recipes?([\\/]|$)", dp): continue
    for fn in fs:
        if not fn.endswith(".json"): continue
        rec += 1
        try: o = json.load(open(f"{dp}/{fn}", encoding="utf-8-sig"))
        except Exception: continue
        for r in refs(o):
            if items and r not in items:
                missing[r] += 1; miss_ex.setdefault(r, os.path.relpath(f"{dp}/{fn}", R))
for r, n in missing.most_common():
    ns = r.split(":")[0]
    warn("recipe-unknown-item", f"{r} x{n} e.g. {miss_ex[r]}")
I.append(("recipes", str(rec)))

# 5 assets
big = []
for dp, _, fs in os.walk(f"{R}/kubejs/assets"):
    for fn in fs:
        p = f"{dp}/{fn}"; sz = os.path.getsize(p)
        if sz > 1 << 20: big.append((sz, os.path.relpath(p, R)))
for sz, p in sorted(big, reverse=True)[:20]: warn("big-asset", f"{p} {sz>>10} KB")

res = {"errors": E, "warnings": W, "info": I}
print(f"ERRORS {len(E)}  WARNINGS {len(W)}")
for c, m in E: print("ERROR", c, m)
cnt = collections.Counter(c for c, _ in W); print("warning kinds", dict(cnt))
for c, m in W[:60]: print("warn", c, m)
for c, m in I: print("info", c, m)
if a.out: json.dump(res, open(a.out, "w"), indent=1)
sys.exit(1 if E else 0)
