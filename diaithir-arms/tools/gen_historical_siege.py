"""Adds the historical siege weapons (one placeable engine block per real design) from the roster.
Writes data/diaithirarms/historical_siege.json, blockstates, block and item models, placeholder textures, lang, age gates.
Loading and firing: see Siege.java. No recipes: the Workshop Bench in diaithir-gear makes them. Run: python3 -I tools/gen_historical_siege.py"""
import csv, json, os
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = f"{ROOT}/src/main/resources"; NS = "diaithirarms"; A = f"{RES}/assets/{NS}"
ROS = f"{ROOT}/../spartan-weaponry/roster/WEAPONS_ROSTER.csv"
# roster name -> (id, kind, ammo item, ammo count, damage, power, speed, volley, breaks, fire, colour)
# kinds: bolt (flat, fast, damage), stone (lobbed, explodes on impact), shell (powder-fired, explodes on impact), volley (many shells or fire
# arrows), flame (burning cone), ram (breaks the blocks in front). power = explosion radius in blocks; breaks = shot breaks blocks.
ARROW, STONE, POWDER, COAL = "minecraft:arrow", "minecraft:cobblestone", "minecraft:gunpowder", "minecraft:coal"
WOOD, IRON, BRONZE = (120, 90, 55), (95, 100, 108), (150, 110, 60)
D = {
 "Scorpio": ("scorpio", "bolt", ARROW, 1, 9, 0, 3.5, 1, False, False, WOOD),
 "Ballista": ("ballista", "bolt", ARROW, 2, 14, 0, 4.0, 1, False, False, WOOD),
 "Polybolos": ("polybolos", "bolt", ARROW, 3, 7, 0, 3.5, 3, False, False, WOOD),
 "Cheiroballistra": ("cheiroballistra", "bolt", ARROW, 1, 10, 0, 3.8, 1, False, False, WOOD),
 "Springald": ("springald", "bolt", ARROW, 2, 13, 0, 4.0, 1, False, False, WOOD),
 "Onager": ("onager", "stone", STONE, 1, 4, 1.5, 1.6, 1, True, False, WOOD),
 "Trebuchet": ("trebuchet", "stone", STONE, 2, 6, 2.5, 2.2, 1, True, False, WOOD),
 "Battering ram": ("battering_ram", "ram", None, 0, 10, 0, 0, 1, True, False, WOOD),
 "Greek fire siphon": ("greek_fire_siphon", "flame", COAL, 1, 2, 0, 0, 1, False, True, BRONZE),
 "Hwacha": ("hwacha", "volley", ARROW, 4, 6, 0, 2.6, 12, False, True, WOOD),
 "Bombard": ("bombard", "shell", POWDER, 3, 8, 2.5, 2.0, 1, True, False, IRON),
 "Great bombard (Mons Meg)": ("mons_meg", "shell", POWDER, 5, 10, 4.0, 2.2, 1, True, False, IRON),
 "Dardanelles Gun": ("dardanelles_gun", "shell", POWDER, 6, 10, 4.5, 2.2, 1, True, False, BRONZE),
 "Culverin": ("culverin", "shell", POWDER, 2, 8, 2.0, 3.0, 1, True, False, BRONZE),
 "Swivel gun": ("swivel_gun", "shell", POWDER, 1, 6, 1.0, 3.0, 1, False, False, BRONZE),
 "Carronade": ("carronade", "shell", POWDER, 2, 8, 2.0, 2.4, 1, True, False, IRON),
 "Napoleon 12-pounder": ("napoleon_12_pounder", "shell", POWDER, 3, 9, 2.5, 3.0, 1, True, False, BRONZE),
 "Gribeauval 12-pounder": ("gribeauval_12_pounder", "shell", POWDER, 3, 9, 2.5, 3.0, 1, True, False, BRONZE),
 "Armstrong breech-loading gun": ("armstrong_gun", "shell", POWDER, 4, 10, 3.0, 3.2, 1, True, False, IRON),
 "Krupp 8.8 cm FlaK 36": ("flak_36", "shell", POWDER, 3, 9, 2.5, 4.0, 1, True, False, IRON),
 "Ordnance QF 25-pounder": ("qf_25_pounder", "shell", POWDER, 4, 10, 3.0, 3.4, 1, True, False, IRON),
 "M101 105 mm howitzer": ("m101_howitzer", "shell", POWDER, 5, 11, 3.5, 3.2, 1, True, False, IRON),
 "M777 howitzer": ("m777_howitzer", "shell", POWDER, 6, 12, 4.0, 3.6, 1, True, False, IRON),
 "Mortar (Stokes)": ("stokes_mortar", "shell", POWDER, 2, 8, 2.0, 1.8, 1, True, False, IRON),
 "Katyusha BM-13": ("katyusha_bm_13", "volley", POWDER, 8, 8, 1.5, 2.6, 8, True, False, IRON),
 "Paris Gun": ("paris_gun", "shell", POWDER, 8, 14, 5.0, 4.5, 1, True, False, IRON),
 "Schwerer Gustav": ("schwerer_gustav", "shell", POWDER, 10, 16, 6.0, 4.5, 1, True, False, IRON),
}
def W(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w", encoding="utf8").write(json.dumps(o, indent=1, ensure_ascii=False))

rows = list(csv.DictReader(open(ROS, encoding="utf8")))
out, lang, gates = [], {}, {}
for r in rows:
    if r["category"] == "siege" and r["name"] not in D: r["batch"] = "later"  # siege towers: structures, not engines
    if r["name"] not in D: continue
    iid, kind, ammo, cnt, dmg, power, speed, volley, breaks, fire, col = D[r["name"]]
    r["batch"] = "siege"; r["id"] = iid
    out.append({"id": iid, "kind": kind, "ammo": ammo, "ammoCount": cnt, "damage": dmg, "power": power, "speed": speed, "volley": volley, "breaks": breaks, "fire": fire})
    lang[f"block.{NS}.{iid}"] = r["name"]; gates[f"{NS}:{iid}"] = r["age"]
    W(f"{A}/blockstates/{iid}.json", {"variants": {f"facing={f},loaded={l}": {"model": f"{NS}:block/{iid}" + ("_loaded" if l == "true" else ""), "y": y}
                                                  for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)) for l in ("false", "true")}})
    for suf in ("", "_loaded"):
        W(f"{A}/models/block/{iid}{suf}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{iid}{suf}"}})
    W(f"{A}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{iid}"}})
    os.makedirs(f"{A}/textures/block", exist_ok=True)
    for suf, mark in (("", (60, 60, 60)), ("_loaded", (220, 160, 40))):
        im = Image.new("RGBA", (16, 16), col + (255,)); d = ImageDraw.Draw(im)
        d.rectangle([0, 0, 15, 15], outline=tuple(c // 2 for c in col)); d.line([(2, 13), (13, 2)], fill=mark, width=2); d.ellipse([10, 1, 14, 5], fill=mark)
        im.save(f"{A}/textures/block/{iid}{suf}.png")
W(f"{RES}/data/{NS}/historical_siege.json", out)
lp = f"{A}/lang/en_us.json"; L = json.load(open(lp, encoding="utf8")); L.update(lang); W(lp, L)
gp = f"{ROOT}/age_gates.json"; G = json.load(open(gp, encoding="utf8")); G["items"].update(gates); W(gp, G)
npth = f"{ROOT}/names_map.json"; N = json.load(open(npth, encoding="utf8")); N.update({k.split(".")[-1]: {"diaithiris": None, "english": v} for k, v in lang.items()}); W(npth, N)
cols = ["id", "name", "category", "origin", "era", "age", "batch", "source"]
with open(ROS, "w", newline="", encoding="utf8") as f:
    w = csv.DictWriter(f, cols, extrasaction="ignore"); w.writeheader(); w.writerows(rows)
print(len(out), "siege engines")
