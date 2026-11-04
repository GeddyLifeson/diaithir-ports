"""Adds the historical throwables and sling-type launchers (one item per real design) from the roster.
Writes data/diaithirarms/historical_thrown.json, item definitions, models, 16x16 placeholder textures, lang, age gates, ammo tags.
No recipes: the Workshop Bench in diaithir-gear makes them. Run: python3 -I tools/gen_historical_thrown.py"""
import csv, json, os
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
RES = f"{ROOT}/src/main/resources"; NS = "diaithirarms"; A = f"{RES}/assets/{NS}"
ROS = f"{ROOT}/../spartan-weaponry/roster/WEAPONS_ROSTER.csv"
# roster name -> (id, kind, damage, power, fuse ticks, speed, effect ticks, effect amp, durability or stack, cooldown, ammo tag, colour)
# kinds: sling and blowgun use ammo from a tag; grenade explodes on its fuse (no block damage); charge is a breaching charge (breaks
# blocks); molotov bursts into fire on impact; bolas and net slow the target on impact.
D = {
 "Sling": ("sling", "sling", 3.0, 0, 0, 1.6, 0, 0, 200, 12, "sling_ammo", (150, 110, 70)),
 "Balearic sling": ("balearic_sling", "sling", 4.5, 0, 0, 2.0, 0, 0, 300, 10, "sling_ammo", (170, 130, 80)),
 "Inca sling (huaraca)": ("inca_sling_huaraca", "sling", 4.0, 0, 0, 1.9, 0, 0, 260, 11, "sling_ammo", (190, 70, 60)),
 "Bolas": ("bolas", "bolas", 1.0, 0, 0, 1.3, 100, 3, 16, 20, None, (120, 100, 80)),
 "Blowgun": ("blowgun", "blowgun", 1.5, 0, 0, 2.2, 100, 0, 200, 15, "blowgun_ammo", (110, 160, 90)),
 "Retiarius net": ("retiarius_net", "net", 0.0, 0, 0, 1.0, 200, 5, 8, 40, None, (190, 190, 170)),
 "Grenado (hand grenade)": ("grenado", "grenade", 0.0, 1.2, 60, 1.1, 0, 0, 16, 20, None, (60, 60, 60)),
 "Stielhandgranate (Model 24)": ("stielhandgranate", "grenade", 0.0, 2.0, 80, 1.3, 0, 0, 16, 20, None, (90, 110, 80)),
 "Mills bomb (No. 36M)": ("mills_bomb", "grenade", 0.0, 2.0, 80, 1.3, 0, 0, 16, 20, None, (70, 90, 60)),
 "Mk 2 grenade (\"pineapple\")": ("mk2_grenade", "grenade", 0.0, 2.0, 100, 1.3, 0, 0, 16, 20, None, (80, 100, 60)),
 "F1 grenade": ("f1_grenade", "grenade", 0.0, 2.2, 90, 1.3, 0, 0, 16, 20, None, (100, 110, 70)),
 "RGD-33 grenade": ("rgd_33_grenade", "grenade", 0.0, 2.0, 90, 1.3, 0, 0, 16, 20, None, (110, 100, 70)),
 "M67 grenade": ("m67_grenade", "grenade", 0.0, 2.5, 100, 1.3, 0, 0, 16, 20, None, (80, 90, 60)),
 "Molotov cocktail": ("molotov_cocktail", "molotov", 0.0, 0, 0, 1.2, 0, 0, 16, 20, None, (200, 120, 40)),
 "Satchel charge": ("satchel_charge", "charge", 0.0, 4.0, 60, 0.9, 0, 0, 4, 30, None, (130, 100, 60)),
 "Bangalore torpedo": ("bangalore_torpedo", "charge", 0.0, 3.0, 80, 0.8, 0, 0, 4, 30, None, (120, 120, 120)),
}
AGE = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7}
def W(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w", encoding="utf8").write(json.dumps(o, indent=1, ensure_ascii=False))

rows = list(csv.DictReader(open(ROS, encoding="utf8")))
for r in rows:
    if r["name"] == "Net and trident (retiarius)": r["name"] = "Retiarius net"
    if r["name"] == "Inca bolas (liwi)": r["batch"] = "duplicate"
out, lang, gates = [], {}, {}
for r in rows:
    if r["name"] not in D: continue
    iid, kind, dmg, power, fuse, speed, eticks, eamp, dur, cd, ammo, col = D[r["name"]]
    r["batch"] = "ranged-thrown"; r["id"] = D[r["name"]][0]
    out.append({"id": iid, "kind": kind, "damage": dmg, "power": power, "fuse": fuse, "speed": speed, "effectTicks": eticks, "effectAmp": eamp,
                "durability": dur if ammo else 0, "stack": 1 if ammo else dur, "cooldown": cd, "ammo": ammo})
    lang[f"item.{NS}.{iid}"] = r["name"]; gates[f"{NS}:{iid}"] = r["age"]
    W(f"{A}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}})
    W(f"{A}/models/item/{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im); dark = tuple(c // 2 for c in col)
    if kind in ("sling", "blowgun"): d.line([(3, 12), (12, 3)], fill=col, width=2); d.ellipse([2, 11, 4, 13], fill=dark)
    elif kind in ("bolas", "net"): d.ellipse([3, 3, 7, 7], fill=col); d.ellipse([9, 9, 13, 13], fill=col); d.line([(5, 5), (11, 11)], fill=dark)
    elif kind == "molotov": d.rectangle([5, 6, 10, 13], fill=col, outline=dark); d.rectangle([7, 2, 8, 5], fill=dark)
    elif kind == "charge": d.rectangle([3, 5, 12, 12], fill=col, outline=dark); d.line([(8, 5), (10, 2)], fill=dark)
    else: d.ellipse([4, 5, 11, 13], fill=col, outline=dark); d.rectangle([6, 2, 9, 5], fill=dark)
    os.makedirs(f"{A}/textures/item", exist_ok=True); im.save(f"{A}/textures/item/{iid}.png")
W(f"{RES}/data/{NS}/historical_thrown.json", out)
W(f"{RES}/data/{NS}/tags/item/sling_ammo.json", {"values": ["minecraft:flint", "minecraft:clay_ball", "minecraft:gravel", "minecraft:cobblestone"]})
W(f"{RES}/data/{NS}/tags/item/blowgun_ammo.json", {"values": ["minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:pumpkin_seeds", "minecraft:melon_seeds"]})
lp = f"{A}/lang/en_us.json"; L = json.load(open(lp, encoding="utf8")); L.update(lang); L[f"entity.{NS}.thrown_weapon"] = "Thrown weapon"; W(lp, L)
gp = f"{ROOT}/age_gates.json"; G = json.load(open(gp, encoding="utf8")); G["items"].update(gates); W(gp, G)
npth = f"{ROOT}/names_map.json"; N = json.load(open(npth, encoding="utf8")); N.update({k.split(".")[-1]: {"diaithiris": None, "english": v} for k, v in lang.items()}); W(npth, N)
cols = ["id", "name", "category", "origin", "era", "age", "batch", "source"]
with open(ROS, "w", newline="", encoding="utf8") as f:
    w = csv.DictWriter(f, cols, extrasaction="ignore"); w.writeheader(); w.writerows(rows)
print(len(out), "thrown and launcher designs")
