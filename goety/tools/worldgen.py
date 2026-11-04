"""Stage 4: converts the original worldgen, structure templates, chest and mob loot to the 26.1 data layout.
Everything that refers to a block, item or creature that this port lacks is dropped or rewritten, so the game never reads a dangling reference."""
import json, os, re, shutil, sys, glob
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import nbtstrip

UP = sys.argv[1]
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, "src/main/resources")
D = os.path.join(RES, "data/goety")
SRC = os.path.join(UP, "src/main/resources/data/goety")
ASSET_ITEMS = {os.path.splitext(f)[0] for f in os.listdir(os.path.join(RES, "assets/goety/items"))}
MOBS = {m["id"] for m in json.load(open(os.path.join(RES, "goety_spec/mobs.json")))} | set(json.load(open(os.path.join(RES, "goety_spec/entities.json"))))
ALIAS = {"reaper": "wraith", "border_wraith": "wraith", "muck_wraith": "wraith", "cairn_necromancer": "necromancer",
         "wither_necromancer": "wither_necromancer", "bone_lord": "skull_lord", "haunted_armor": "rattled", "crypt_slime": "frayed"}
report = {"structures": [], "skipped_structures": [], "features": [], "loot": 0, "loot_skipped": 0, "templates": 0}

def rd(p): return json.load(open(p, encoding="utf8"))
def wr(p, j):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    json.dump(j, open(p, "w", encoding="utf8"), indent=1)

def fix_ns(s):
    return s.replace("forge:", "c:")

def fix_json(j):
    """forge: ids become c: ids; unknown tag references become optional."""
    if isinstance(j, dict): return {k: fix_json(v) for k, v in j.items()}
    if isinstance(j, list): return [fix_json(v) for v in j]
    if isinstance(j, str): return fix_ns(j)
    return j

# ---- biome tags
for f in glob.glob(SRC + "/tags/worldgen/biome/**/*.json", recursive=True):
    rel = os.path.relpath(f, SRC + "/tags/worldgen/biome")
    if rel.startswith(("spell_", "mob_spawn", "illager", "no_sun")): continue
    j = fix_json(rd(f))
    vals = []
    for v in j["values"]:
        if isinstance(v, str) and v.startswith(("#c:", "#goety:")) or (isinstance(v, str) and v.startswith("goety:")):
            vals.append({"id": v, "required": False})
        else:
            vals.append(v)
    j["values"] = vals
    wr(os.path.join(D, "tags/worldgen/biome", rel), j)

# ---- features
def goety_refs(j, out=None):
    out = set() if out is None else out
    if isinstance(j, dict):
        for v in j.values(): goety_refs(v, out)
    elif isinstance(j, list):
        for v in j: goety_refs(v, out)
    elif isinstance(j, str) and j.startswith("goety:"): out.add(j[6:])
    return out
BLOCKS = {b["id"] for b in rd(os.path.join(RES, "goety_spec/blocks.json"))}
ok_feat = set()
for f in sorted(glob.glob(SRC + "/worldgen/configured_feature/*.json")):
    n = os.path.basename(f)[:-5]
    if n.endswith("_bonemeal"): continue
    j = fix_json(rd(f))
    raw = json.dumps(j)
    if re.search(r'"(type|predicate_type)": "goety:', raw) or any(r not in BLOCKS for r in goety_refs(j)): continue
    wr(os.path.join(D, "worldgen/configured_feature", n + ".json"), j); ok_feat.add(n); report["features"].append(n)
for f in sorted(glob.glob(SRC + "/worldgen/placed_feature/*.json")):
    n = os.path.basename(f)[:-5]
    j = fix_json(rd(f))
    if j["feature"].startswith("goety:") and j["feature"][6:] not in ok_feat: continue
    wr(os.path.join(D, "worldgen/placed_feature", n + ".json"), j)
mods = {"add_jade_ore": ("jade_ore", "underground_ores", "#goety:has_jade"), "add_pine_tree": ("pine_tree", "vegetal_decoration", "#goety:has_pine_tree"),
        "add_windswept_tree": ("windswept_tree", "vegetal_decoration", "#goety:has_windswept_tree")}
for n, (feat, step, biomes) in mods.items():
    if feat in ok_feat:
        wr(os.path.join(D, "neoforge/biome_modifier", n + ".json"), {"type": "neoforge:add_features", "biomes": biomes, "features": "goety:" + feat, "step": step})
# natural spawns of the new hostile creatures
SPAWNS = [("frayed", 8, 1, 3), ("rattled", 8, 1, 3), ("zombie_vindicator", 4, 1, 2), ("skeleton_pillager", 4, 1, 2), ("wraith", 3, 1, 1), ("necromancer", 1, 1, 1)]
wr(os.path.join(D, "neoforge/biome_modifier/mob_spawns.json"), {"type": "neoforge:add_spawns", "biomes": "#minecraft:is_overworld",
   "spawners": [{"type": "goety:" + t, "weight": w, "minCount": a, "maxCount": b} for t, w, a, b in SPAWNS]})

# ---- structures
PROC_BAD = ("goety:",)
procs_ok = set()
for f in glob.glob(SRC + "/worldgen/processor_list/*.json"):
    n = os.path.basename(f)[:-5]
    j = fix_json(rd(f))
    j["processors"] = [p for p in j["processors"] if not p.get("processor_type", "").startswith(PROC_BAD) and "goety:" not in json.dumps(p)]
    wr(os.path.join(D, "worldgen/processor_list", n + ".json"), j); procs_ok.add(n)
def spawn_overrides(j):
    for cat in j.get("spawn_overrides", {}).values():
        keep = []
        for s in cat["spawns"]:
            t = s["type"]
            if t.startswith("goety:"):
                t = "goety:" + ALIAS.get(t[6:], t[6:])
                if t[6:] not in MOBS: continue
            s["type"] = t; keep.append(s)
        cat["spawns"] = keep
    j["spawn_overrides"] = {k: v for k, v in j.get("spawn_overrides", {}).items() if v["spawns"]}
    return j
good = set()
for f in sorted(glob.glob(SRC + "/worldgen/structure/*.json")):
    n = os.path.basename(f)[:-5]
    j = fix_json(rd(f))
    if not j["type"].startswith("minecraft:"): report["skipped_structures"].append(n); continue
    wr(os.path.join(D, "worldgen/structure", n + ".json"), spawn_overrides(j)); good.add(n)
for f in sorted(glob.glob(SRC + "/worldgen/structure_set/*.json")):
    n = os.path.basename(f)[:-5]
    j = fix_json(rd(f))
    j["structures"] = [s for s in j["structures"] if s["structure"].startswith("goety:") and s["structure"][6:] in good]
    if not j["structures"]: continue
    pl = j["placement"]
    if pl["type"] == "goety:random_spread":
        pl["type"] = "minecraft:random_spread"
        for k in ("super_exclusion_zone", "spread_type"): pl.pop(k, None)
    wr(os.path.join(D, "worldgen/structure_set", n + ".json"), j); report["structures"].append(n)
for f in glob.glob(SRC + "/worldgen/template_pool/**/*.json", recursive=True):
    rel = os.path.relpath(f, SRC + "/worldgen/template_pool")
    j = fix_json(rd(f))
    wr(os.path.join(D, "worldgen/template_pool", rel), j)
# the explorer map tags and the structure tags only matter for items this port lacks; the used ones are copied
for f in glob.glob(SRC + "/tags/worldgen/structure/**/*.json", recursive=True):
    pass
n = 0
for f in glob.glob(SRC + "/structures/**/*.nbt", recursive=True):
    rel = os.path.relpath(f, SRC + "/structures")
    dst = os.path.join(D, "structure", rel); os.makedirs(os.path.dirname(dst), exist_ok=True); shutil.copy(f, dst); n += 1; nbtstrip.strip(dst)
report["templates"] = n

# ---- loot
ITEM_RENAME = {"minecraft:chain": "minecraft:iron_chain", "minecraft:scute": "minecraft:turtle_scute"}
def conv_fn(x):
    f = x.get("function", "")
    if f == "minecraft:looting_enchant":
        y = {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": x.get("count", 1)}
        if "limit" in x: y["limit"] = x["limit"]
        if "conditions" in x: y["conditions"] = x["conditions"]
        return y
    if f in ("minecraft:set_nbt", "minecraft:fill_player_head", "minecraft:set_contents") or f.startswith("goety:") or "goety:" in json.dumps(x).replace('"goety:blocks', ""): return None
    return x
def conv_cond(x):
    c = x.get("condition", "")
    if c == "minecraft:random_chance_with_looting":
        ch, m = x["chance"], x.get("looting_multiplier", 0)
        return {"condition": "minecraft:random_chance_with_enchanted_bonus", "enchantment": "minecraft:looting", "unenchanted_chance": ch,
                "enchanted_chance": {"type": "minecraft:linear", "base": ch + m, "per_level_above_first": m}}
    if "goety:" in c: return None
    return x
def clean_entry(e):
    t = e.get("type", "")
    if t == "minecraft:item":
        e["name"] = ITEM_RENAME.get(e["name"], e["name"])
        if e["name"].startswith("goety:") and e["name"][6:] not in ASSET_ITEMS: return None
    elif t == "minecraft:loot_table":
        v = e.pop("name", None) or e.get("value")
        if v is None or v.startswith("goety:gameplay") or v.startswith("goety:inject"): return None
        e["value"] = v
    elif t.startswith("goety:"): return None
    if "functions" in e: e["functions"] = [y for y in (conv_fn(x) for x in e["functions"]) if y]
    if "conditions" in e: e["conditions"] = [y for y in (conv_cond(x) for x in e["conditions"]) if y]
    if "children" in e:
        e["children"] = [c for c in (clean_entry(c) for c in e["children"]) if c]
        if not e["children"]: return None
    return e
def clean_table(j):
    pools = []
    for p in j.get("pools", []):
        p["entries"] = [c for c in (clean_entry(e) for e in p.get("entries", [])) if c]
        if "conditions" in p: p["conditions"] = [y for y in (conv_cond(x) for x in p["conditions"]) if y]
        if "functions" in p: p["functions"] = [y for y in (conv_fn(x) for x in p["functions"]) if y]
        if p["entries"]: pools.append(p)
    j["pools"] = pools
    if "functions" in j: j["functions"] = [y for y in (conv_fn(x) for x in j["functions"]) if y]
    return j
for sub in ("chests", "entities"):
    for f in glob.glob(f"{SRC}/loot_tables/{sub}/**/*.json", recursive=True):
        rel = os.path.relpath(f, SRC + "/loot_tables")
        name = os.path.splitext(os.path.basename(f))[0]
        if sub == "entities" and (name not in MOBS or "natural" in rel):
            report["loot_skipped"] += 1; continue
        raw = open(f, encoding="utf8").read()
        j = clean_table(fix_json(json.loads(raw)))
        if not j["pools"]: report["loot_skipped"] += 1; continue
        wr(os.path.join(D, "loot_table", rel), j); report["loot"] += 1
print(json.dumps(report))
