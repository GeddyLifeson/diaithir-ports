"""Generates the data-driven parts of the goety module from the upstream 1.20 checkout (path as argv[1]).
Writes goety_spec/*.json, models, blockstates, textures, item definitions, lang, loot tables, tags, recipes,
age_gates.json and names_map.json.  Run: python3 -I tools/gen.py <upstream-dir>"""
import glob, json, os, re, shutil, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import upstream as U
import naming

UP = sys.argv[1]
ROOT = os.path.normpath(os.path.join(HERE, ".."))
RES = os.path.join(ROOT, "src/main/resources")
A = os.path.join(RES, "assets/goety")
D = os.path.join(RES, "data/goety")
UPAS = [os.path.join(UP, "src/main/resources/assets/goety"), os.path.join(UP, "src/generated/resources/assets/goety")]
UPA = UPAS[0]
def upa(*parts):
    for b in UPAS:
        p = os.path.join(b, *parts)
        if os.path.exists(p): return p
    return os.path.join(UPA, *parts)
UPD = os.path.join(UP, "src/main/resources/data/goety")
ID = "goety"

def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        if isinstance(obj, str):
            f.write(obj)
        else:
            json.dump(obj, f, indent=1, ensure_ascii=False)

# ---------------------------------------------------------------- selection
STAGE = int(os.environ.get("GOETY_STAGE", "1"))
FAM = re.compile(r"")  # every plain block family; functional blocks are excluded by EXCL
EXCL = re.compile(r'(altar|pedestal|brazier|sarcophagus|throne|chest|lamp|lantern|torch|candle|statue|urn|gravestone|ossuary|blower|glass|cage|sign|sapling|potted|bush|firewood|jug|mirror|chandelier|sconce|bars|carpet|crystal|idol|decree|vine|grass|fern|pit|shrine|tall_skull|moss_|window|plinth_door|_light$|jade_light)')
CUBE_OK = {"ShadeStoneBlock", "CursedMetalBlock", "PaleSteelBlock", "DarkMetalBlock", "JadeStoneBlock", "StoneOreBlock"}

def like_for(i):
    if re.search(r'(planks|bookshelf)', i) or re.search(r'(button|fence|gate|trapdoor|door|plate)$', i):
        return "oak_planks"
    if re.search(r'(_log|_wood)$', i): return "oak_log"
    if "leaves" in i: return "oak_leaves"
    if re.search(r'(dirt|detritus)', i): return "dirt"
    if "emerald" in i or "diamond_mold" in i: return "emerald_block"
    if re.search(r'(^|_)end_|purpur', i): return "end_stone_bricks"
    if "jade_ore" in i: return "emerald_ore"
    if "jade" in i: return "emerald_block"
    if re.search(r'(cursed_metal|pale_steel|dark_metal)', i): return "iron_block"
    if "shade" in i: return "deepslate_bricks"
    if "crypt" in i: return "stone_bricks"
    if "ominous" in i: return "blackstone"
    if "marble" in i: return "calcite"
    if "highrock" in i: return "andesite"
    if "crag" in i: return "cobblestone"
    if "silt" in i: return "sandstone"
    return "stone"

def wood_set(i):
    return re.sub(r'_(door|trapdoor|fence_gate|pressure_plate|button)$', '', i)

def block_kind(r):
    fn, a, i = r["fn"], r["args"], r["id"]
    if fn == "registerStairs": return "stairs"
    if fn in ("registerSlabs", "registerShadeSlabs", "registerCryptSlabs"): return "slab"
    if fn == "registerWalls": return "wall"
    if fn != "register": return None
    if re.search(r'Sign|Chest|sapling\(|FlowerPot|LootTableType.EMPTY', a): 
        return None
    for pat, k in (("DoorBlock(", "door"), ("TrapDoorBlock(", "trapdoor"), ("FenceGateBlock(", "gate"),
                   ("PressurePlateBlock(", "plate"), ("woodenButton(", "button"), ("FenceBlock(", "fence"),
                   ("RotatedPillarBlock(", "pillar"), ("pillar(", "pillar"), ("fireProofLog(", "pillar")):
        if pat in a:
            if k == "door" and "TrapDoor" in a: continue
            return k
    if "leaves(" in a: return "leaves"
    m = re.search(r'(\w+)::new', a)
    if m:
        return "cube" if m.group(1) in CUBE_OK else None
    if "new Block(" in a or "new BookshelfBlock(" in a: return "cube"
    return None

blocks_up = U.blocks(UP)
field_id = {r["field"]: r["id"] for r in blocks_up}
bspec, bkinds = [], {}
for r in blocks_up:
    i = r["id"]
    if not FAM.search(i) or EXCL.search(i): continue
    k = block_kind(r)
    if not k: continue
    e = {"id": i, "kind": k, "like": like_for(i), "set": wood_set(i) if k in ("door", "trapdoor", "gate", "plate", "button") else ""}
    if k in ("stairs", "slab", "wall"):
        m = re.search(r',\s*(\w+)\s*$', r["args"].strip()) if r["fn"] in ("registerStairs", "registerSlabs", "registerWalls") else None
        base = field_id.get(m.group(1)) if m else None
        if r["fn"] == "registerShadeSlabs": base = "shade_stone"
        if r["fn"] == "registerCryptSlabs": base = "crypt_stone"
        if not base: continue
        e["base"] = base
        e["like"] = like_for(base)
        if e["like"] == "oak_log": e["like"] = "oak_planks"
    e["axe"] = e["like"] in ("oak_planks", "oak_log", "oak_leaves")
    e["shovel"] = e["like"] == "dirt"
    bspec.append(e); bkinds[i] = e
# keep only derived blocks whose base exists
ids = set(bkinds)
bspec = [e for e in bspec if "base" not in e or e["base"] in ids]
bkinds = {e["id"]: e for e in bspec}

PLAIN_ITEMS = []
items_up = U.items(UP)
for r in items_up:
    if r["cls"] in ("ItemBase", "SimpleFoiledItem") or (r["cls"] == "Item" and r["id"] not in (
            "feet_of_frog", "cooked_feet_of_frog", "end_mud_bottle", "end_mineral_bottle", "pedestal_dummy", "cooking_ladle")):
        if r["id"] in ("ominous_saddle",): continue
        PLAIN_ITEMS.append(r["id"])
TOOLS = [("dark_sword", "sword"), ("dark_shovel", "shovel"), ("dark_pickaxe", "pickaxe"), ("dark_axe", "axe"), ("dark_hoe", "hoe")]
ARMOR = [("black_iron_" + p, p) for p in ("helmet", "chestplate", "leggings", "boots")]

# ---------------------------------------------------------------- specs
write(os.path.join(RES, "goety_spec/blocks.json"), bspec)
write(os.path.join(RES, "goety_spec/items.json"), {"plain": PLAIN_ITEMS, "tools": TOOLS, "armor": ARMOR})

# ---------------------------------------------------------------- assets
missing = set()
def cp(src, dst):
    if os.path.exists(src):
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        shutil.copy(src, dst)
        if os.path.exists(src + ".mcmeta"): shutil.copy(src + ".mcmeta", dst + ".mcmeta")
        return True
    missing.add(os.path.basename(src)); return False

done_models = set()
def copy_model(ref):
    """ref like goety:block/x or goety:item/x"""
    if not ref.startswith("goety:") or ref in done_models: return
    done_models.add(ref)
    p = ref.split(":", 1)[1]
    src = upa("models", p + ".json")
    if not os.path.exists(src): missing.add("models/" + p); return
    j = json.load(open(src, encoding="utf8"))
    dst = os.path.join(A, "models", p + ".json")
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copy(src, dst)
    if "parent" in j: copy_model(j["parent"])
    for t in (j.get("textures") or {}).values():
        if isinstance(t, str) and t.startswith("goety:"):
            cp(upa("textures", t.split(":", 1)[1] + ".png"), os.path.join(A, "textures", t.split(":", 1)[1] + ".png"))
        elif isinstance(t, str) and not t.startswith("#") and ":" not in t:
            pass

def walk_models(o):
    if isinstance(o, dict):
        for k, v in o.items():
            if k == "model" and isinstance(v, str): copy_model(v)
            else: walk_models(v)
    elif isinstance(o, list):
        for v in o: walk_models(v)

def tag(path, values, replace=False):
    write(os.path.join(RES, "data", path + ".json"), {"replace": replace, "values": sorted(set(values))})
def item_def(i, model=None):
    write(os.path.join(A, "items", i + ".json"), {"model": {"type": "minecraft:model", "model": model or f"goety:item/{i}"}})

for i, e in bkinds.items():
    src = upa("blockstates", i + ".json")
    if os.path.exists(src):
        shutil.copy(src, os.path.join(A, "blockstates", i + ".json")) if os.path.isdir(os.path.join(A, "blockstates")) else (os.makedirs(os.path.join(A, "blockstates"), exist_ok=True), shutil.copy(src, os.path.join(A, "blockstates", i + ".json")))
        walk_models(json.load(open(src, encoding="utf8")))
    else: missing.add("blockstates/" + i)
    if os.path.exists(upa("models/item", i + ".json")):
        copy_model(f"goety:item/{i}")
    else:
        write(os.path.join(A, "models/item", i + ".json"), {"parent": f"goety:block/{i}"})
    item_def(i)
for i in PLAIN_ITEMS + [t[0] for t in TOOLS] + [a[0] for a in ARMOR]:
    copy_model(f"goety:item/{i}")
    item_def(i)
# armour equipment assets (textures: 64x64 humanoid layers of the original)
for fam in {a[0].rsplit("_", 1)[0] for a in ARMOR}:
    cp(upa(f"textures/models/armor/{fam}_armor.png"), os.path.join(A, f"textures/entity/equipment/humanoid/{fam}.png"))
    cp(upa(f"textures/models/armor/{fam}_armor_layer.png"), os.path.join(A, f"textures/entity/equipment/humanoid_leggings/{fam}.png"))
    write(os.path.join(A, f"equipment/{fam}.json"), {"layers": {"humanoid": [{"texture": f"goety:{fam}"}], "humanoid_leggings": [{"texture": f"goety:{fam}"}]}})

# ---------------------------------------------------------------- entities (stage 2)
ENTITIES = {  # id: (texture files under textures/entity/servants, vanilla egg used for the item sprite, undead?)
    "zombie_servant": (["zombie/zombie_servant"], "zombie", True),
    "husk_servant": (["zombie/husk_servant"], "husk", True),
    "skeleton_servant": (["skeleton/skeleton_servant"], "skeleton", True),
    "stray_servant": (["skeleton/stray_servant"], "stray", True),
    "wither_skeleton_servant": (["skeleton/wither_skeleton_servant"], "wither_skeleton", True),
    "spider_servant": (["spider/spider_servant"], "spider", False),
    "cave_spider_servant": (["spider/cave_spider_servant"], "cave_spider", False),
}
for eid, (texs, egg, und) in ENTITIES.items():
    for t in texs:
        cp(upa("textures/entity/servants", t + ".png"), os.path.join(A, "textures/entity/servants", t + ".png"))
    item_def(eid + "_spawn_egg", f"minecraft:item/{egg}_spawn_egg")
tag("minecraft/tags/entity_type/undead", [f"{ID}:{e}" for e, v in ENTITIES.items() if v[2]])
tag("minecraft/tags/entity_type/skeletons", [f"{ID}:{e}" for e in ENTITIES if "skeleton" in e or "stray" in e])
tag("minecraft/tags/entity_type/arthropod", [f"{ID}:{e}" for e in ENTITIES if "spider" in e])
tag("minecraft/tags/entity_type/zombies", [f"{ID}:{e}" for e in ENTITIES if "zombie" in e or "husk" in e])
tag("minecraft/tags/entity_type/aquatic", []) if False else None
write(os.path.join(RES, "goety_spec/entities.json"), list(ENTITIES))

# ---------------------------------------------------------------- names
en = U.lang(UP)
names, allmiss = {}, set()
def name_for(kind, i):
    e = en.get(f"{kind}.goety.{i}") or en.get(f"item.goety.{i}") or en.get(f"block.goety.{i}") or i.replace("_", " ").title()
    d, miss = naming.diaithiris(e)
    allmiss.update(miss)
    names[i] = {"diaithiris": d, "english": e}
    return d
lang = {}
for i in bkinds: lang[f"block.goety.{i}"] = name_for("block", i)
for i in PLAIN_ITEMS + [t[0] for t in TOOLS] + [a[0] for a in ARMOR]: lang[f"item.goety.{i}"] = name_for("item", i)
for eid in ENTITIES:
    e = en.get(f"entity.goety.{eid}") or eid.replace("_", " ").title()
    d, miss = naming.diaithiris(e); allmiss.update(miss)
    lang[f"entity.goety.{eid}"] = d
    lang[f"item.goety.{eid}_spawn_egg"] = d + " " + naming.diaithiris("Summoning Egg")[0].lower()
    names[eid] = {"diaithiris": d, "english": e, "entity": True}
lang["itemGroup.goety"] = naming.diaithiris("Goety")[0]
write(os.path.join(A, "lang/en_us.json"), lang)
write(os.path.join(ROOT, "names_map.json"), names)

# ---------------------------------------------------------------- loot tables
def drop(i, extra=None):
    entry = {"type": "minecraft:item", "name": f"goety:{i}"}
    if extra: entry["functions"] = extra
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}], "entries": [entry]}], "random_sequence": f"goety:blocks/{i}"}
for i, e in bkinds.items():
    k = e["kind"]
    if k == "leaves": continue
    t = drop(i)
    if k == "slab":
        t = drop(i, [{"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [{"condition": "minecraft:block_state_property", "block": f"goety:{i}", "properties": {"type": "double"}}]}])
    elif k == "door":
        t["pools"][0]["conditions"].append({"condition": "minecraft:block_state_property", "block": f"goety:{i}", "properties": {"half": "lower"}})
    if i == "jade_ore": t = drop("jade")
    write(os.path.join(D, "loot_table/blocks", i + ".json"), t)

# ---------------------------------------------------------------- tags
pick = [f"{ID}:{i}" for i, e in bkinds.items() if not e["axe"] and not e["shovel"] and e["kind"] != "leaves"]
tag("minecraft/tags/block/mineable/shovel", [f"{ID}:{i}" for i, e in bkinds.items() if e["shovel"]])
axe = [f"{ID}:{i}" for i, e in bkinds.items() if e["axe"] and e["kind"] != "leaves"]
tag("minecraft/tags/block/mineable/pickaxe", pick)
tag("minecraft/tags/block/mineable/axe", axe)
tag("minecraft/tags/block/mineable/hoe", [f"{ID}:{i}" for i, e in bkinds.items() if e["kind"] == "leaves"])
for kind, name in (("stairs", "stairs"), ("slab", "slabs"), ("wall", "walls"), ("fence", "fences"), ("gate", "fence_gates"), ("button", "buttons"), ("door", "doors"), ("trapdoor", "trapdoors"), ("plate", "pressure_plates"), ("leaves", "leaves")):
    vs = [f"{ID}:{i}" for i, e in bkinds.items() if e["kind"] == kind]
    wood = [f"{ID}:{i}" for i, e in bkinds.items() if e["kind"] == kind and e["axe"]]
    if vs:
        tag(f"minecraft/tags/block/{name}", vs); tag(f"minecraft/tags/item/{name}", vs)
    if kind in ("stairs", "slab", "fence", "gate", "button", "door", "trapdoor", "plate") and wood:
        tag(f"minecraft/tags/block/wooden_{name}", wood); tag(f"minecraft/tags/item/wooden_{name}", wood)
logs = [f"{ID}:{i}" for i, e in bkinds.items() if e["kind"] == "pillar" and e["like"] == "oak_log"]
planks = [f"{ID}:{i}" for i in bkinds if i.endswith("_planks")]
tag("minecraft/tags/block/logs", logs); tag("minecraft/tags/item/logs", logs)
tag("minecraft/tags/block/planks", planks); tag("minecraft/tags/item/planks", planks)
tag("minecraft/tags/block/logs_that_burn", logs); tag("minecraft/tags/item/logs_that_burn", logs)
tag("minecraft/tags/item/bookshelf_books", [])  if False else None
tag(f"{ID}/tags/item/dark_metal_materials", [f"{ID}:dark_ingot"])
tag(f"{ID}/tags/item/cursed_metal_materials", [f"{ID}:cursed_ingot"])

CTAGS = set(open(os.path.join(HERE, "c_item_tags.txt")).read().split())
CTAGS.add("gems/jade")
def tagname(t):
    t = t.replace("forge:", "c:")
    if t == "c:string": t = "c:strings"
    return t
# copy upstream item tags (filtered to what exists)
UPT = os.path.join(UPD, "tags/items")
def selected(v):
    if v.startswith("#"):
        t = tagname(v[1:])
        if t.split(":")[0] not in ("minecraft", "goety", "c"): return None
        return "#" + t if (not t.startswith("c:") or t[2:] in CTAGS) else None
    if v.startswith("goety:"): return v if v in sel else None
    return v if v.startswith("minecraft:") else None
sel = {f"{ID}:{i}" for i in list(bkinds) + PLAIN_ITEMS + [t[0] for t in TOOLS] + [a[0] for a in ARMOR]}
for f in glob.glob(os.path.join(UPT, "**/*.json"), recursive=True):
    j = json.load(open(f, encoding="utf8"))
    vals = []
    for v in j.get("values", []):
        v = v["id"] if isinstance(v, dict) else v
        v = selected(v)
        if v: vals.append(v)
    rel = os.path.relpath(f, UPT)
    write(os.path.join(D, "tags/item", rel), {"replace": False, "values": vals})
# goety tags that resolve to nothing: recipes using them cannot be crafted yet
TAGVALS = {}
for f in glob.glob(os.path.join(D, "tags/item/**/*.json"), recursive=True):
    TAGVALS["goety:" + os.path.relpath(f, os.path.join(D, "tags/item"))[:-5]] = json.load(open(f))["values"]
def tag_empty(t, depth=0):
    vals = TAGVALS.get(t)
    if vals is None: return not t.startswith("minecraft:") and not t.startswith("c:")
    return all(v.startswith("#") and v[1:].startswith("goety:") and depth < 8 and tag_empty(v[1:], depth + 1) for v in vals)
write(os.path.join(RES, "data/c/tags/item/gems/jade.json"), {"replace": False, "values": ["goety:jade"]})

# ---------------------------------------------------------------- recipes
sel = {f"{ID}:{i}" for i in list(bkinds) + PLAIN_ITEMS + [t[0] for t in TOOLS] + [a[0] for a in ARMOR]}
def ing(x):
    if isinstance(x, list):
        out = [ing(y) for y in x]
        if any(o is None for o in out): return None
        return out
    if "item" in x: return x["item"]
    if "tag" in x:
        t = tagname(x["tag"])
        if t.startswith("c:") and t[2:] not in CTAGS: return None
        return "#" + t
    return None
def item_ok(s):
    if isinstance(s, list): return all(item_ok(y) for y in s)
    if s.startswith("#"): return not (s[1:].startswith("goety:") and tag_empty(s[1:]))
    if s.startswith("goety:"): return s in sel
    return s.startswith("minecraft:")
nrec = 0
for f in glob.glob(os.path.join(UPD, "recipes/**/*.json"), recursive=True):
    j = json.load(open(f, encoding="utf8"))
    t = j.get("type", "")
    if j.get("conditions"): continue
    out = None
    try:
        if t == "minecraft:crafting_shaped":
            key = {k: ing(v) for k, v in j["key"].items()}
            res = j["result"]
            out = {"type": t, "category": j.get("category", "misc"), "key": key, "pattern": j["pattern"],
                   "result": {"id": res["item"], "count": res.get("count", 1)}}
            ins = list(key.values())
        elif t == "minecraft:crafting_shapeless":
            ins = [ing(v) for v in j["ingredients"]]
            res = j["result"]
            out = {"type": t, "category": j.get("category", "misc"), "ingredients": ins, "result": {"id": res["item"], "count": res.get("count", 1)}}
        elif t == "minecraft:stonecutting":
            ins = [ing(j["ingredient"])]
            out = {"type": t, "ingredient": ins[0], "result": {"id": j["result"], "count": j.get("count", 1)}}
        elif t in ("minecraft:smelting", "minecraft:blasting"):
            ins = [ing(j["ingredient"])]
            res = j["result"]
            out = {"type": t, "category": "misc", "ingredient": ins[0], "experience": j.get("experience", 0.1), "cookingtime": j.get("cookingtime", 200),
                   "result": {"id": res if isinstance(res, str) else res["item"]}}
        else: continue
    except (KeyError, TypeError):
        continue
    if any(x is None for x in ins) or not all(item_ok(x) for x in ins): continue
    rid = out["result"]["id"]
    if rid not in sel: continue
    write(os.path.join(D, "recipe", os.path.splitext(os.path.basename(f))[0] + ".json"), out)
    nrec += 1

# ---------------------------------------------------------------- age gate + misc
ages = {}
for i in list(bkinds) + PLAIN_ITEMS + [t[0] for t in TOOLS] + [a[0] for a in ARMOR]:
    ages[f"{ID}:{i}"] = "V"
write(os.path.join(ROOT, "age_gates.json"), {"_doc": "Same shape as DiaithirRPG content/tech_ages.json. The whole Goety namespace is Age V and up (dark rites, the Cailleach's Samhain craft).", "namespaces": {ID: "V"}, "items": {}})
write(os.path.join(RES, "pack.mcmeta"), {"pack": {"description": "Goety assets", "pack_format": 1}}) if False else None
print("blocks", len(bspec), "items", len(PLAIN_ITEMS), "recipes", nrec, "missing", len(missing), sorted(missing)[:15], "unnamed", sorted(allmiss))
