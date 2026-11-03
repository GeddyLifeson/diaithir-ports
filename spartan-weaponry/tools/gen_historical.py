"""Builds the historical-weapon batch of spartan-weaponry from roster/WEAPONS_ROSTER.csv (one item per real design).
Run: python3 -I tools/gen_historical.py   (writes data/<id>/historical_weapons.json, items, models, textures, lang, age gates; no recipes, the Smithy makes them)."""
import csv, json, os, re, shutil, unicodedata

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = os.path.join(ROOT, "src/main/resources")
ID = "spartanweaponry"
A = f"{RES}/assets/{ID}"
ROMAN = {1: "I", 2: "II", 3: "III", 4: "IV", 5: "V", 6: "VI", 7: "VII", 8: "VIII"}
AGEN = {v: k for k, v in ROMAN.items()}
MAT = {1: "stone", 2: "copper", 3: "bronze"}  # Age 4+ is iron/steel
ING = {"stone": "#minecraft:stone_tool_materials", "copper": "minecraft:copper_ingot", "bronze": "#c:ingots/bronze", "iron": "minecraft:iron_ingot"}
# model type -> (base damage, speed, kind, reach, sweep, flags); same numbers as the Spartan pattern table
T = {
 "dagger": (2.5, 2.5, "sword", 0, 0, ["backstab"]), "parrying_dagger": (2.5, 2.5, "sword", 0, 0, []),
 "longsword": (4.5, 1.4, "sword", 0, 1, []), "katana": (3.5, 2.0, "sword", 0, 1, []), "saber": (3.5, 1.6, "sword", 0, 1, []),
 "rapier": (2.0, 2.4, "sword", 0, 0, ["unarmoured"]), "greatsword": (4.0, 1.4, "sword", 1.0, 3, []),
 "battle_hammer": (5.0, 0.8, "plain", 0, 0, ["knockback", "nausea"]), "warhammer": (4.0, 1.1, "plain", 0, 0, []),
 "spear": (5.5, 1.4, "plain", 1.0, 0, []), "halberd": (5.0, 1.2, "plain", 1.0, 0, ["shield_breach"]),
 "pike": (4.0, 1.4, "plain", 2.0, 0, []), "lance": (4.0, 1.0, "plain", 1.0, 1, ["riding"]),
 "battleaxe": (4.0, 1.0, "axe", 0, 0, []), "flanged_mace": (3.0, 1.2, "plain", 0, 0, ["undead"]),
 "glaive": (4.0, 1.0, "sword", 1.0, 2, []), "quarterstaff": (3.0, 1.4, "plain", 0, 2, []),
 "club": (4.0, 1.3, "plain", 0, 0, ["nausea"]), "scythe": (5.0, 1.0, "plain", 0, 0, []),
 "throwing_knife": (1.5, 2.5, "sword", 0, 0, ["throwing"]), "tomahawk": (2.0, 0.9, "plain", 0, 0, ["throwing"]),
 "javelin": (1.5, 1.2, "plain", 0, 0, ["throwing"]), "boomerang": (4.0, 2.0, "plain", 0, 0, ["throwing"]),
}
CURVED = r"saber|sabre|scimitar|shamshir|talwar|kilij|cutlass|yatagan|dao\b|dadao|dha|kampilan|barong|parang|golok|shotel|takoba|khanda|karabela|shashka|falchion|messer|kopis|khopesh|sickle|hanger|briquet|pallasch|tegha|hwando|hussar"
def model_type(name, cat):
    n = name.lower()
    if cat == "sword":
        if re.search(r"katana|tachi|wakizashi|nodachi|zhanmadao|urumi|butterfly|nine-ring", n): return "katana"
        if re.search(r"rapier|smallsword|estoc|swordstick", n): return "rapier"
        if re.search(r"zweih|claymore", n): return "greatsword"
        if re.search(CURVED, n): return "saber"
        return "longsword"
    if cat == "dagger":
        if re.search(r"main-gauche|sai\b|sgian|dirk|swordbreaker|bagh", n): return "parrying_dagger"
        return "dagger"
    if cat == "spear":
        if re.search(r"pike|sarissa", n): return "pike"
        return "spear"
    if cat == "polearm":
        if re.search(r"sarissa|pike|spontoon|taiaha|staff|bo staff|jo staff|quarterstaff|tiger|uurga", n): return "pike" if re.search(r"sarissa|pike|spontoon", n) else "quarterstaff"
        if re.search(r"lance|mapuche|uhlan|winged", n): return "lance"
        if re.search(r"glaive|naginata|guandao|woldo|fauchard|voulge|bill|guisarme|ranseur|spetum|partisan|dagger-axe|ji \(|bardiche|godendag|tepoztopilli", n): return "glaive"
        return "halberd"
    if cat == "axe":
        if re.search(r"francisca|tomahawk|sagaris|tabar|hand axe|kama|ngulu|ngombe", n): return "tomahawk"
        return "battleaxe"
    if cat == "mace": return "flanged_mace"
    if cat == "hammer": return "warhammer"
    if cat == "club":
        if re.search(r"cestus|knuckle", n): return "club"
        return "club"
    if cat == "flail": return "battle_hammer"
    if cat == "thrown":
        if re.search(r"boomerang", n): return "boomerang"
        if re.search(r"knife|shuriken|chakram|kpinga", n): return "throwing_knife"
        if re.search(r"francisca|tomahawk", n): return "tomahawk"
        if re.search(r"pilum|plumbata|javelin|atlatl|angon", n): return "javelin"
    return None  # not a melee-pattern weapon: ranged and siege batches

def slug(name):
    s = unicodedata.normalize("NFKD", name.lower()).encode("ascii", "ignore").decode()
    s = re.sub(r"\(.*?\)", "", s).strip()
    return re.sub(r"[^a-z0-9]+", "_", s).strip("_")

HIST_PREV = {t["id"] for t in json.load(open(f"{RES}/data/{ID}/historical_weapons.json"))} if os.path.exists(f"{RES}/data/{ID}/historical_weapons.json") else set()
EXIST = {t["id"] for t in json.load(open(f"{RES}/data/{ID}/weapon_types.json"))}
rows = list(csv.DictReader(open(f"{ROOT}/roster/WEAPONS_ROSTER.csv", encoding="utf8")))
specs, lang, gates, names = [], {}, {}, {}
os.makedirs(f"{A}/items", exist_ok=True)
for r in rows:
    t = model_type(r["name"], r["category"])
    r["id"] = slug(r["name"]); r["batch"] = "melee" if t else {"bow": "ranged", "crossbow": "ranged", "sling": "ranged", "thrown": "ranged", "siege": "siege", "shield": "n/a", "trap": "later"}.get(r["category"], "later")
    if r["id"] in EXIST and r["id"] not in HIST_PREV: r["batch"] = "existing"; continue
    if not t: continue
    age = AGEN[r["age"]]; base, spd, kind, reach, sweep, flags = T[t]
    mat = MAT.get(age, "iron"); dmg = base + max(0, age - 4) * 0.5
    iid = r["id"]
    specs.append({"id": iid, "type": t, "material": mat, "damage": dmg, "mult": 1.0, "speed": spd, "kind": kind, "reach": float(reach), "sweep": {0: 0.0, 1: 0.01, 2: 0.5, 3: 1.0}[sweep], "flags": flags})
    lang[f"item.{ID}.{iid}"] = r["name"]; names[iid] = {"english": r["name"], "diaithiris": None}; gates[f"{ID}:{iid}"] = r["age"]
    def w(path, obj):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        open(path, "w", encoding="utf8").write(json.dumps(obj, indent=1, ensure_ascii=False))
    w(f"{A}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{ID}:item/{iid}"}})
    w(f"{A}/models/item/{iid}.json", {"parent": f"{ID}:item/base/{t}", "textures": {"layer0": f"{ID}:item/{iid}"}})
    pre = {"stone": "stone", "copper": "copper", "bronze": "bronze", "iron": "iron"}[mat]
    src = next(f"{A}/textures/item/{m}_{t}.png" for m in (pre, "iron", "wooden", "stone", "copper", "bronze", "studded")
               if os.path.exists(f"{A}/textures/item/{m}_{t}.png"))
    shutil.copy(src, f"{A}/textures/item/{iid}.png")
    # no crafting recipe: the Smithy in diaithir-gear (gen_workshop.py) is the only way to make these

json.dump(specs, open(f"{RES}/data/{ID}/historical_weapons.json", "w"), indent=1)
# lang merges into the existing file
lp = f"{A}/lang/en_us.json"; L = json.load(open(lp, encoding="utf8"))
L = {k: v for k, v in L.items() if not any(k == f"item.{ID}.{s['id']}" for s in specs)}; L.update(lang)
json.dump(L, open(lp, "w", encoding="utf8"), indent=1, ensure_ascii=False)
gp = f"{ROOT}/age_gates.json"; G = json.load(open(gp, encoding="utf8")); G["items"].update(gates); json.dump(G, open(gp, "w", encoding="utf8"), indent=1)
npth = f"{ROOT}/names_map.json"; N = json.load(open(npth, encoding="utf8")); N.update(names); json.dump(N, open(npth, "w", encoding="utf8"), indent=1, ensure_ascii=False)
# base models / textures for new types are already shipped; refresh roster with ids and batch
with open(f"{ROOT}/roster/WEAPONS_ROSTER.csv", "w", newline="", encoding="utf8") as f:
    cols = ["id", "name", "category", "origin", "era", "age", "batch", "source"]
    wr = csv.DictWriter(f, cols, extrasaction="ignore"); wr.writeheader(); wr.writerows(rows)
print(len(rows), "roster rows;", len(specs), "melee items")
