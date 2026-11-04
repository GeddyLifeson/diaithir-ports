"""Adds the historical bows and crossbows (one item per real design) from ../spartan-weaponry/roster/WEAPONS_ROSTER.csv.
Writes data/diaithirarms/historical_arms.json, item definitions, models, textures (copies of the nearest tier), lang, age gates.
No recipes: the Workshop Bench in diaithir-gear makes them. Run: python3 -I tools/gen_historical.py"""
import csv, json, os, re, shutil, unicodedata

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = f"{ROOT}/src/main/resources"; NS = "diaithirarms"; A = f"{RES}/assets/{NS}"
ROS = f"{ROOT}/../spartan-weaponry/roster/WEAPONS_ROSTER.csv"
TIER = {"bow": {1: "bone", 2: "copper", 3: "bronze", 4: "mail", 5: "steel"}, "crossbow": {4: "mail", 5: "steel"}}
# design -> (damage multiplier relative to a vanilla bow, durability); draw weight / power of the real design
STAT = {
 "self_bow": (0.9, 280), "nubian_self_bow": (0.95, 300), "composite_bow": None, "yumi": (1.15, 380), "longbow_english": (1.3, 450),
 "plains_bow": (1.0, 340), "korean_horn_bow_gakgung": (1.2, 400), "hungarian_magyar_bow": (1.15, 380), "cretan_short_bow": (1.0, 340),
 "bhutanese_bamboo_bow": (1.05, 360),
 "gastraphetes": (1.1, 300), "repeating_crossbow_zhuge_nu": (0.8, 400), "arbalest_steel_crossbow": (1.5, 600),
 "stonebow_bullet_crossbow": (1.2, 500), "cranequin_crossbow": (1.45, 560), "tibetan_crossbow": (1.1, 450),
}
AGEN = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5}
def slug(n):
    s = unicodedata.normalize("NFKD", n.lower()).encode("ascii", "ignore").decode()
    s = re.sub(r"[^a-z0-9]+", "_", s.replace("(", " ").replace(")", " ")).strip("_")
    return s
def W(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w", encoding="utf8").write(json.dumps(o, indent=1, ensure_ascii=False))

rows = list(csv.DictReader(open(ROS, encoding="utf8")))
items, lang, gates = [], {}, {}
for r in rows:
    if r["category"] not in ("bow", "crossbow") or r["batch"] == "duplicate": continue
    iid = slug(r["name"]); st = STAT.get(iid, "missing")
    if st is None: r["batch"] = "existing"; continue
    if st == "missing": raise SystemExit("no stats for " + iid)
    r["batch"] = "ranged-bow"; kind = r["category"]; tier = TIER[kind][AGEN[r["age"]]]
    items.append({"id": iid, "kind": kind, "mat": tier, "slot": "", "damage": 0, "speed": 0, "mult": st[0], "shield": 0, "spear": [], "durability": st[1]})
    lang[f"item.{NS}.{iid}"] = r["name"]; gates[f"{NS}:{iid}"] = r["age"]
    W(f"{A}/items/{iid}.json", json.loads(open(f"{A}/items/{tier}_{kind}.json").read().replace(f"{tier}_{kind}", iid)))
    for f in os.listdir(f"{A}/models/item"):
        if f.startswith(f"{tier}_{kind}"):
            nf = f.replace(f"{tier}_{kind}", iid)
            open(f"{A}/models/item/{nf}", "w").write(open(f"{A}/models/item/{f}").read().replace(f"{tier}_{kind}", iid))
    for f in os.listdir(f"{A}/textures/item"):
        if f.startswith(f"{tier}_{kind}"): shutil.copy(f"{A}/textures/item/{f}", f"{A}/textures/item/{f.replace(f'{tier}_{kind}', iid)}")
W(f"{RES}/data/{NS}/historical_arms.json", items)
lp = f"{A}/lang/en_us.json"; L = json.load(open(lp, encoding="utf8")); L.update(lang); W(lp, L)
gp = f"{ROOT}/age_gates.json"; G = json.load(open(gp, encoding="utf8")); G["items"].update(gates); W(gp, G)
npth = f"{ROOT}/names_map.json"; N = json.load(open(npth, encoding="utf8")); N.update({k.split(".")[-1]: {"diaithiris": None, "english": v} for k, v in lang.items()}); W(npth, N)
cols = ["id", "name", "category", "origin", "era", "age", "batch", "source"]
for r in rows: r.setdefault("id", slug(r["name"]))
with open(ROS, "w", newline="", encoding="utf8") as f:
    w = csv.DictWriter(f, cols, extrasaction="ignore"); w.writeheader(); w.writerows(rows)
print(len(items), "bows and crossbows")
