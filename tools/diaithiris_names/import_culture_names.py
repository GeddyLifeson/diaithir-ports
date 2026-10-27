"""Copy the per-culture lexicons from DiaithirRPG content/names into the mod's assets, trimmed to what the lookup reads.

    python3 -I import_culture_names.py DIAITHIRRPG_CONTENT_NAMES OUT_ASSETS_DIR
"""
import json
import os
import sys

src, out = sys.argv[1:3]
os.makedirs(os.path.join(out, "names", "langs"), exist_ok=True)
dump = lambda o, p: json.dump(o, open(os.path.join(out, "names", p), "w", encoding="utf-8"), ensure_ascii=False, separators=(",", ":"))
units = json.load(open(os.path.join(src, "units.json"), encoding="utf-8"))
dump({k: {"lang": v["lang"]} for k, v in units.items()}, "units.json")
items = json.load(open(os.path.join(src, "items.json"), encoding="utf-8"))
dump({"phrases": items["phrases"], "ids": items["ids"], "adj": items["adj"]}, "items.json")
for lang in sorted({v["lang"] for v in units.values()} - {"diaithiris"}):
    d = json.load(open(os.path.join(src, "langs", lang + ".json"), encoding="utf-8"))
    dump({k: d.get(k, "") for k in ("order", "link", "script", "w")}, "langs/%s.json" % lang)
