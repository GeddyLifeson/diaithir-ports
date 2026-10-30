"""Writes weapon_types.json, item definitions, models, textures, lang, recipes, tags, age_gates.json, names_map.json and library entries
for spartan-weaponry from the upstream checkout (path as argv[1]). Run: python3 -I tools/gen.py <upstream-dir>"""
import json, os, re, shutil, sys

UP = sys.argv[1]
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = os.path.join(ROOT, "src/main/resources")
ID = "spartanweaponry"
UPRES = os.path.join(UP, "src/main/resources/assets/spartanweaponry")
LIBOUT = sys.argv[2] if len(sys.argv) > 2 else None

# type id: (english, diaithiris root, damage base, damage multiplier, attack speed, kind, reach, sweep, flags, model base)
# kind: sword (sword properties), axe (axe properties), plain
T = {
 "dagger":          ("Dagger", "Biudag", 2.5, 1.0, 2.5, "sword", 0, 0, ["backstab"]),
 "parrying_dagger": ("Parrying Dagger", "Biudagdìun", 2.5, 1.0, 2.5, "sword", 0, 0, []),
 "longsword":       ("Longsword", "Klaidemfauda", 4.5, 1.5, 1.4, "sword", 0, 1, []),
 "katana":          ("Katana", "Klaidemgeiri", 3.5, 0.5, 2.0, "sword", 0, 1, []),
 "saber":           ("Saber", "Klaidemkrum", 3.5, 0.5, 1.6, "sword", 0, 1, []),
 "rapier":          ("Rapier", "Klaidemtauna", 2.0, 0.5, 2.4, "sword", 0, 0, ["unarmoured"]),
 "greatsword":      ("Greatsword", "Klaidemmòru", 4.0, 1.5, 1.4, "sword", 1, 3, []),
 "battle_hammer":   ("Battle Hammer", "Òrdukad", 5.0, 2.0, 0.8, "plain", 0, 0, ["knockback", "nausea"]),
 "warhammer":       ("Warhammer", "Òrdukogad", 4.0, 1.5, 1.1, "plain", 0, 0, []),
 "spear":           ("Spear", "Slieg", 5.5, 0.5, 1.4, "plain", 1, 0, []),
 "halberd":         ("Halberd", "Sliegtuog", 5.0, 1.5, 1.2, "plain", 1, 0, ["shield_breach"]),
 "pike":            ("Pike", "Giedas", 4.0, 1.0, 1.4, "plain", 2, 0, []),
 "lance":           ("Lance", "Sliegmòru", 4.0, 1.0, 1.0, "plain", 1, 1, ["riding"]),
 "battleaxe":       ("Battleaxe", "Tuogkad", 4.0, 2.0, 1.0, "axe", 0, 0, []),
 "flanged_mace":    ("Flanged Mace", "Slakunbili", 3.0, 1.5, 1.2, "plain", 0, 0, ["undead"]),
 "glaive":          ("Glaive", "Landkabur", 4.0, 1.5, 1.0, "sword", 1, 2, []),
 "quarterstaff":    ("Quarterstaff", "Bauta", 3.0, 1.5, 1.4, "plain", 0, 2, []),
 "scythe":          ("Scythe", "Korrunfauda", 5.0, 1.0, 1.0, "plain", 0, 0, []),
 # thrown in the original; melee only here until the throwing entities are ported
 "throwing_knife":  ("Throwing Knife", "Skiantilig", 1.5, 1.0, 2.5, "sword", 0, 0, ["throwing"]),
 "tomahawk":        ("Tomahawk", "Tuogchata", 2.0, 1.5, 0.9, "plain", 0, 0, ["throwing"]),
 "javelin":         ("Javelin", "Sliegtilig", 1.5, 1.0, 1.2, "plain", 0, 0, ["throwing"]),
 "boomerang":       ("Boomerang", "Maidetildid", 4.0, 1.0, 2.0, "plain", 0, 0, ["throwing"]),
}
# material id, item prefix, english, Diaithiris root, Age
M = [("wood", "wooden", "Wooden", "fiud", 1), ("stone", "stone", "Stone", "klach", 1),
     ("copper", "copper", "Copper", "uma", 2), ("bronze", "bronze", "Bronze", "krèuma", 3),
     ("iron", "iron", "Iron", "iarund", 4)]
ROMAN = {1: "I", 2: "II", 3: "III", 4: "IV", 5: "V"}
MATING = {"wood": "#minecraft:planks", "stone": "#minecraft:stone_tool_materials", "copper": "minecraft:copper_ingot",
          "bronze": "#c:ingots/bronze", "iron": "minecraft:iron_ingot"}
# singles: id, english, name, material, type(for base model), kind, base, mult, speed, age, flags
S = {
 "wooden_club":   ("Wooden Club", "Komund", "wood", "club", 4.0, 1.0, 1.3, 1, ["nausea"]),
 "studded_club":  ("Studded Club", "Komundiarund", "iron", "club", 4.0, 1.0, 1.3, 4, ["nausea"]),
 "cestus":        ("Cestus", "Dòrnulietur", "leather", "cestus", 2.0, 0.5, 3.5, 1, []),
 "studded_cestus":("Studded Cestus", "Dòrnuiarund", "iron", "cestus", 2.0, 0.5, 3.5, 4, []),
}
BASIC = {"simple_handle": ("Simple Handle", "Làimsigfeiri"), "handle": ("Handle", "Làimsig"),
         "simple_pole": ("Simple Pole", "Kaburfeiri"), "pole": ("Pole", "Kabur")}
MATNAME = {m[0]: m[3] for m in M}; MATNAME["leather"] = "lietur"
SWEEP = {0: 0.0, 1: 0.01, 2: 0.5, 3: 1.0}
REACH = {0: 0.0, 1: 1.5, 2: 2.5}

def cap(s): return s[:1].upper() + s[1:]

types, names, ages = [], {}, {}
items = []  # id, model base, texture
for t, (en, root, base, mult, spd, kind, reach, sweep, flags) in T.items():
    for mid, pre, men, mroot, age in M:
        iid = f"{pre}_{t}"
        items.append(iid)
        types.append({"id": iid, "type": t, "material": mid, "damage": base, "mult": mult, "speed": spd, "kind": kind,
                      "reach": REACH[reach], "sweep": SWEEP[sweep], "flags": flags})
        names[iid] = (f"{root} {mroot}", f"{men} {en}")
        ages[iid] = age
for iid, (en, root, mat, t, base, mult, spd, age, flags) in S.items():
    kind = "plain"
    items.append(iid)
    types.append({"id": iid, "type": t, "material": mat, "damage": base, "mult": mult, "speed": spd, "kind": kind,
                  "reach": 0.0, "sweep": 0.0, "flags": flags})
    names[iid] = (root, en); ages[iid] = age
for iid, (en, root) in BASIC.items():
    names[iid] = (root, en); ages[iid] = 1
json.dump(types, open(f"{RES}/data/{ID}/weapon_types.json" if os.makedirs(f"{RES}/data/{ID}", exist_ok=True) is None else "", "w"), indent=1)

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w", encoding="utf8").write(obj if isinstance(obj, str) else json.dumps(obj, indent=1, ensure_ascii=False))

# models, item definitions, textures
for tdef in types:
    iid, t = tdef["id"], tdef["type"]
    w(f"{RES}/assets/{ID}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:item/{iid}"}})
    w(f"{RES}/assets/{ID}/models/item/{iid}.json", {"parent": f"{ID}:item/base/{t}", "textures": {"layer0": f"{ID}:item/{iid}"}})
    src = f"{UPRES}/textures/item/{iid}.png"
    dst = f"{RES}/assets/{ID}/textures/item/{iid}.png"
    os.makedirs(os.path.dirname(dst), exist_ok=True); shutil.copy(src, dst)
for iid in BASIC:
    w(f"{RES}/assets/{ID}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:item/{iid}"}})
    w(f"{RES}/assets/{ID}/models/item/{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{ID}:item/{iid}"}})
    shutil.copy(f"{UPRES}/textures/item/{iid}.png", f"{RES}/assets/{ID}/textures/item/{iid}.png")
used = {t["type"] for t in types}
for t in used:
    os.makedirs(f"{RES}/assets/{ID}/models/item/base", exist_ok=True)
    shutil.copy(f"{UPRES}/models/item/base/{t}.json", f"{RES}/assets/{ID}/models/item/base/{t}.json")

# lang: Diaithiris names are the en_us display names
lang = {f"item.{ID}.{k}": v[0] for k, v in names.items()}
lang["itemGroup.spartanweaponry"] = "Armaid Spartan"
w(f"{RES}/assets/{ID}/lang/en_us.json", lang)
w(f"{ROOT}/names_map.json", {k: {"diaithiris": v[0], "english": v[1]} for k, v in names.items()})
w(f"{ROOT}/age_gates.json", {"_doc": "Same shape as DiaithirRPG content/tech_ages.json: namespaces and per-item entries (items beat namespaces). Metals map to Ages: wood/stone/leather I, copper II, bronze III, iron IV.",
                             "namespaces": {ID: "I"}, "items": {f"{ID}:{k}": ROMAN[v] for k, v in ages.items()}})

# tags
w(f"{RES}/data/{ID}/tags/item/handles.json", {"values": [f"{ID}:handle", f"{ID}:simple_handle"]})
w(f"{RES}/data/{ID}/tags/item/poles.json", {"values": [f"{ID}:pole", f"{ID}:simple_pole"]})
w(f"{RES}/data/c/tags/item/ingots/bronze.json", {"values": []})  # exists even without a bronze mod; other mods add to it
w(f"{RES}/data/{ID}/tags/item/bronze_materials.json", {"values": [{"id": "#c:ingots/bronze", "required": False}]})
sw = [f"{ID}:{t['id']}" for t in types if t["sweep"] > 0]
w(f"{RES}/data/minecraft/tags/item/swords.json", {"values": sw})
axes = [f"{ID}:{t['id']}" for t in types if t["kind"] == "axe"]
w(f"{RES}/data/minecraft/tags/item/axes.json", {"values": axes})

# recipes from upstream ModRecipeProvider
src = open(f"{UP}/src/main/java/com/oblivioussp/spartanweaponry/data/ModRecipeProvider.java", encoding="utf8").read()
VAL = {"ModItemTags.HANDLES": f"#{ID}:handles", "ModItems.HANDLE.get()": f"{ID}:handle", "ModItems.POLE.get()": f"{ID}:pole",
       "ModItemTags.POLES": f"#{ID}:poles", "stick": "minecraft:stick", "planks": "#minecraft:planks", "string": "minecraft:string"}
def camel2snake(n): return re.sub(r"(?<!^)([A-Z])", r"_\1", n).lower()
recipes = {}
for m in re.finditer(r"private void recipe(\w+)\(Consumer<FinishedRecipe> consumer,[^)]*\)\s*\{(.*?)\n\t\}", src, re.S):
    t = camel2snake(m.group(1))
    if t not in T: continue
    body = m.group(2)
    key = {}
    for k, v in re.findall(r"\.define\('(.)', (.+?)\)(?=\.\w)", body):
        v = v.strip()
        if v == "data.getMaterialTag()": key[k] = "MAT"
        elif v in VAL: key[k] = VAL[v]
        else: raise SystemExit(f"unmapped {t} {k} {v}")
    pat = re.findall(r"\.pattern\(\"([^\"]*)\"\)", body)
    recipes[t] = (pat, key)
assert set(recipes) == set(T), set(T) - set(recipes)
def ing(v): return v
for tdef in types:
    if tdef["id"] not in {f"{p}_{t}" for t in T for _, p, *_ in M}: continue
    pat, key = recipes[tdef["type"]]
    mat = MATING[tdef["material"]]
    k2 = {k: (mat if v == "MAT" else v) for k, v in key.items()}
    w(f"{RES}/data/{ID}/recipe/{tdef['id']}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "key": k2,
                                                    "pattern": pat, "result": {"id": f"{ID}:{tdef['id']}"}})
def shaped(iid, pat, key): w(f"{RES}/data/{ID}/recipe/{iid}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "key": key, "pattern": pat, "result": {"id": f"{ID}:{iid}"}})
def shapeless(iid, ings, count=1, name=None): w(f"{RES}/data/{ID}/recipe/{name or iid}.json", {"type": "minecraft:crafting_shapeless", "category": "equipment", "ingredients": ings, "result": {"id": f"{ID}:{iid}", "count": count}})
shapeless("simple_handle", ["#c:rods/wooden", "#minecraft:leaves"] if False else ["minecraft:stick", "minecraft:short_grass"])
shapeless("handle", ["minecraft:stick", "minecraft:string"], name="handle_from_string")
shapeless("handle", ["minecraft:stick"] * 4 + ["#minecraft:wool"], 4, "handle_from_wool")
shapeless("handle", ["minecraft:stick"] * 4 + ["minecraft:leather"], 4, "handle_from_leather")
shaped("simple_pole", ["/", "#", "/"], {"#": f"{ID}:simple_handle", "/": "minecraft:stick"})
shaped("pole", ["| ", "|#", "| "], {"|": "minecraft:stick", "#": "minecraft:string"})
w(f"{RES}/data/{ID}/recipe/pole_from_wool.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "key": {"|": "minecraft:stick", "#": "#minecraft:wool"}, "pattern": ["|||", "|||", "||#"], "result": {"id": f"{ID}:pole", "count": 4}})
w(f"{RES}/data/{ID}/recipe/pole_from_leather.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "key": {"|": "minecraft:stick", "#": "minecraft:leather"}, "pattern": ["|||", "|||", "||#"], "result": {"id": f"{ID}:pole", "count": 4}})
shaped("wooden_club", [" #", "# "], {"#": "#minecraft:logs"})
shaped("studded_club", ["C#"], {"C": f"{ID}:wooden_club", "#": "minecraft:iron_ingot"})
shaped("cestus", ["lo"], {"l": "minecraft:leather", "o": "#minecraft:wool"})
shaped("studded_cestus", ["C#"], {"C": f"{ID}:cestus", "#": "minecraft:iron_ingot"})

# library entries (original wording)
lib = [["Armaid Spartan", "concept", 1, "weapons",
        "Twenty-two weapon patterns in wood, stone, copper, bronze and iron, from daggers to pikes. Metal sets the Age: wood and stone I, copper II, bronze III, iron IV. Long weapons reach further, heavy ones swing slowly, and each pattern has its own habit in a fight."]]
fam = {
 "dagger": "Quick and light; a hit from behind does triple damage.",
 "parrying_dagger": "A short blade with a guard, quick to swing.",
 "longsword": "A balanced blade that sweeps nearby foes.",
 "katana": "A thin curved blade, fast, with a small sweep.",
 "saber": "A curved cavalry blade, quick with a small sweep.",
 "rapier": "A thrusting blade; triple damage against a target wearing no armour.",
 "greatsword": "Slow, long and heavy; sweeps a wide arc.",
 "battle_hammer": "The heaviest blow of the set; it knocks back and may sicken the target.",
 "warhammer": "A hammer with a spike on the back, strong and steady.",
 "spear": "A reaching thrust, longer than the hand allows.",
 "halberd": "Axe, spike and hook on a pole; it breaks a raised shield.",
 "pike": "The longest reach of all, but slow to turn.",
 "lance": "A rider's weapon; double damage while mounted.",
 "battleaxe": "A broad two-handed axe that also fells wood.",
 "flanged_mace": "A flanged head that hits the dead hard.",
 "glaive": "A blade on a pole; long reach and a good sweep.",
 "quarterstaff": "A plain staff that sweeps and costs little to make.",
 "scythe": "A reaper's blade turned to war, heavy hits at close range.",
 "throwing_knife": "Made for throwing; for now it fights in the hand only.",
 "tomahawk": "A small axe for the belt; for now it fights in the hand only.",
 "javelin": "A light spear for throwing; for now it fights in the hand only.",
 "boomerang": "A returning stick; for now it fights in the hand only.",
}
for t, d in fam.items():
    lib.append([names[f"wooden_{t}"][0].split()[0] if False else T[t][1], "item", 1, "weapons", d + " Crafted from a handle or pole and 1 to 8 pieces of the metal or wood."])
if LIBOUT: json.dump(lib, open(LIBOUT, "w", encoding="utf8"), ensure_ascii=False, indent=1)
print(len(types), "weapons", len(recipes), "patterns")
