#!/usr/bin/env python3
"""Static checks (MIT):
 1. every JSON parses;
 2. every key used in a spell JSON is a field of the Spell Engine API classes (javap of the shipped jar), recursively,
    because Spell Engine's Gson silently drops unknown keys;
 3. each spell has lang name, description and icon; each item has definition, model, texture; each recipe result exists.
Usage: python3 -I tools/validate.py <spell_engine jar>
"""
import glob
import json
import os
import re
import subprocess
import sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
res = os.path.join(root, "src/main/resources")
ns = "diaithir_classes"
jar = sys.argv[1] if len(sys.argv) > 1 else "/home/user/srv/mods/spell_engine-neoforge-1.10.9+26.1.2.jar"
bad = []
_cache = {}


def fields(cls):
    if cls in _cache:
        return _cache[cls]
    out = subprocess.run(["javap", "-p", "-cp", jar, cls], capture_output=True, text=True).stdout
    f = {}
    for line in out.splitlines():
        m = re.match(r"\s+(?:public|private|protected)?\s*(?:static )?(?:final )?([\w.$<>,\[\] ?]+?) (\w+);$", line)
        if m and " static " not in line:
            f[m.group(2)] = m.group(1).strip()
    _cache[cls] = f
    return f


PRIMS = {"int", "float", "double", "boolean", "long", "java.lang.String", "java.lang.Boolean", "java.lang.Float", "java.lang.Integer",
         "net.minecraft.world.entity.EquipmentSlot", "net.minecraft.resources.Identifier", "java.lang.Double"}


def check(obj, cls, path):
    if isinstance(obj, dict):
        fs = fields(cls)
        if not fs:
            return  # enum / external type: opaque
        for k, v in obj.items():
            if k not in fs:
                bad.append((path, f"unknown key '{k}' in {cls}"))
                continue
            t = fs[k]
            m = re.match(r"java\.util\.List<(.+)>$", t)
            if m:
                inner = m.group(1)
                for i, e in enumerate(v if isinstance(v, list) else [v]):
                    if inner not in PRIMS:
                        check(e, inner, f"{path}.{k}[{i}]")
            elif t not in PRIMS and not t.startswith("java.util.Map"):
                check(v, t, f"{path}.{k}")


for f in glob.glob(res + "/**/*.json", recursive=True):
    try:
        json.load(open(f, encoding="utf8"))
    except Exception as e:
        bad.append((f, str(e)))
lang = json.load(open(f"{res}/assets/{ns}/lang/en_us.json", encoding="utf8"))
for f in sorted(glob.glob(f"{res}/data/{ns}/spell/*.json")):
    sid = os.path.basename(f)[:-5]
    check(json.load(open(f)), "net.spell_engine.api.spell.Spell", sid)
    for k in (f"spell.{ns}.{sid}.name", f"spell.{ns}.{sid}.description"):
        if k not in lang:
            bad.append((f, "missing lang " + k))
    if not os.path.exists(f"{res}/assets/{ns}/textures/spell/{sid}.png"):
        bad.append((f, "missing icon"))
for f in glob.glob(f"{res}/data/{ns}/spell_assignments/*.json"):
    check(json.load(open(f)), "net.spell_engine.api.spell.container.SpellContainer", os.path.basename(f))
    for s in json.load(open(f))["spell_ids"]:
        if not os.path.exists(f"{res}/data/{ns}/spell/{s.split(':')[1]}.json"):
            bad.append((f, "unknown spell " + s))
items = set()
for f in glob.glob(f"{res}/assets/{ns}/items/*.json"):
    iid = os.path.basename(f)[:-5]
    items.add(iid)
    if f"item.{ns}.{iid}" not in lang:
        bad.append((f, "missing item name"))
    for m in re.findall(rf'"{ns}:(item/[\w/]+)"', open(f).read()):
        mf = f"{res}/assets/{ns}/models/{m}.json"
        if not os.path.exists(mf):
            bad.append((f, "missing model " + m))
            continue
        t = json.load(open(mf))["textures"]["layer0"].split(":")[1]
        if not os.path.exists(f"{res}/assets/{ns}/textures/{t}.png"):
            bad.append((mf, "missing texture " + t))
for f in glob.glob(f"{res}/data/{ns}/recipe/*.json"):
    r = json.load(open(f))["result"]["id"].split(":")[1]
    if r not in items:
        bad.append((f, "recipe result without item " + r))
print("problems:", len(bad))
for b in bad:
    print(b)
sys.exit(1 if bad else 0)
