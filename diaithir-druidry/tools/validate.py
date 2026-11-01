#!/usr/bin/env python3
"""Static checks: every JSON parses, every texture/model a file points at exists, every spell has lang + icon. MIT."""
import glob, json, os, sys
root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
res = os.path.join(root, "src/main/resources")
ns = "diaithir_druidry"
bad = []
for f in glob.glob(res + "/**/*.json", recursive=True):
    try:
        json.load(open(f, encoding="utf8"))
    except Exception as e:
        bad.append((f, str(e)))
lang = json.load(open(f"{res}/assets/{ns}/lang/en_us.json", encoding="utf8"))
for f in glob.glob(f"{res}/data/{ns}/spell/*.json"):
    sid = os.path.basename(f)[:-5]
    for k in (f"spell.{ns}.{sid}.name", f"spell.{ns}.{sid}.description"):
        if k not in lang: bad.append((f, "missing lang " + k))
    if not os.path.exists(f"{res}/assets/{ns}/textures/spell/{sid}.png"): bad.append((f, "missing icon"))
for f in glob.glob(f"{res}/assets/{ns}/items/**/*.json", recursive=True):
    m = json.load(open(f))["model"]["model"].split(":")[1]
    mf = f"{res}/assets/{ns}/models/{m}.json"
    if not os.path.exists(mf): bad.append((f, "missing model " + m)); continue
    t = json.load(open(mf))["textures"]["layer0"].split(":")[1]
    if not os.path.exists(f"{res}/assets/{ns}/textures/{t}.png"): bad.append((mf, "missing texture " + t))
for f in glob.glob(f"{res}/data/{ns}/tags/spell/**/*.json", recursive=True):
    for v in json.load(open(f))["values"]:
        i = v["id"] if isinstance(v, dict) else v
        if i.startswith("#"): continue
        if not os.path.exists(f"{res}/data/{ns}/spell/{i.split(':')[1]}.json"): bad.append((f, "unknown spell " + i))
print("problems:", len(bad))
for b in bad: print(b)
sys.exit(1 if bad else 0)
