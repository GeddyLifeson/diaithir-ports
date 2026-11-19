"""Licence, size and duplicate checks for the hosted jars (checklist B5, B6, B7).

Usage: python3 -I tools/integrity/check_licenses.py <pack_root> [MOD_REMOVALS.json]
Exit 1 on any ERROR. WARN lines are listed for a human decision.
"""
import glob
import json
import os
import re
import sys
import zipfile
from collections import defaultdict

LIMIT = 95_000_000
ARR = re.compile(r"all.?rights|proprietary|unknown", re.I)
OWN_MIXED = ("diaithir_rpg_open",)  # our own jar: MIT code, assets ours; reviewed
root = sys.argv[1]
here = os.path.dirname(os.path.abspath(__file__))
restricted = {k: v for k, v in json.load(open(os.path.join(here, "restricted_modids.json"))).items() if not k.startswith("_")}
removals = sys.argv[2] if len(sys.argv) > 2 else None
os.chdir(root)

errors, warns = [], []
jars = sorted(glob.glob("mods/*.jar") + glob.glob("client/*.jar") + glob.glob("server/*.jar"))
blob = "".join(open(p, errors="ignore").read() for p in glob.glob("mods/*.pw.toml"))
orphans = [j for j in jars if not j.startswith("mods/") and os.path.basename(j) not in blob]
for j in orphans:
    warns.append(f"hosted jar not linked by any .pw.toml (stale, excluded from checks): {j}")
jars = [j for j in jars if j not in orphans]
licences = open("LICENSES.md", encoding="utf8").read()

for j in jars:
    b = os.path.basename(j)
    if b not in licences:
        errors.append(f"B5 no LICENSES.md row: {j}")
    size = os.path.getsize(j)
    if size >= LIMIT:
        errors.append(f"B6 {size / 1e6:.1f} MB (limit 95 MB): {j}")

ages = [j for j in jars if "diaithir-ages" in os.path.basename(j)]
if len(ages) != 1:
    errors.append(f"B7 expected one ages jar, found {len(ages)}: {ages}")

mod_ids = defaultdict(set)
for j in jars:
    with zipfile.ZipFile(j) as z:
        for n in z.namelist():
            if n.endswith("mods.toml") and n.startswith("META-INF/"):
                t = z.read(n).decode("utf8", "ignore")
                for block in re.split(r"(?m)^\s*\[\[", t)[1:]:
                    if block.startswith("mods]]"):
                        m = re.search(r'modId\s*=\s*"([^"]+)"', block)
                        if m:
                            mod_ids[m.group(1)].add(os.path.basename(j))
            elif n.endswith("LICENSE-FIELD.txt"):
                t = z.read(n).decode("utf8", "ignore").strip()[:200]
                if ARR.search(t) and not any(k in n for k in OWN_MIXED):
                    errors.append(f"B15 ARR/unknown licence {t!r} inside {os.path.basename(j)}: {n}")
                elif t.lower().startswith("see "):
                    warns.append(f"licence {t!r} inside {os.path.basename(j)}: {n}")
for m, js in sorted(mod_ids.items()):
    if m in restricted:
        errors.append(f"B15 restricted modId {m} ({restricted[m]}) inside hosted jar: {sorted(js)}")
    if len(js) > 1:
        errors.append(f"B7 duplicate modId {m}: {sorted(js)}")

if removals:
    drop = json.load(open(removals))["drop"]
    names = {os.path.basename(p) for p in glob.glob("mods/*")}
    for k in drop:
        if k in names or k in blob:
            errors.append(f"B5 removed jar still in pack: {k}")

print(f"{len(jars)} hosted jars, {len(mod_ids)} modIds")
for w in warns:
    print("WARN", w)
for e in errors:
    print("ERROR", e)
sys.exit(1 if errors else 0)
