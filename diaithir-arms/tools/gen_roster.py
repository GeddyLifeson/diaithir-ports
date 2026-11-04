#!/usr/bin/env python3
"""Spartan-Weaponry-style roster (survey part 2, clean-room from behaviour): six extra melee shapes in every sword material.
Writes data/diaithirarms/roster.json, models, item definitions, recipes, lang, tags, age gates and procedurally drawn textures."""
import json, os
from PIL import Image
R = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
NS = "diaithirarms"
MATS = {"steel": ("#c:ingots/steel", "V"), "tempered": ("diaithirarms:tempered_ingot", "VI"), "composite": ("diaithirarms:ceramic_plate", "VII"),
        "rodos": ("diaithirarms:rodos_alloy_ingot", "RODOS"), "future": ("diaithirarms:star_lattice", "RODOS")}
# kind: (english, damage bonus over 1, attack speed modifier, extra reach, pattern, uses-per-material hint)
KINDS = {
    "dagger":    ("Dagger", 1.0, -1.6, 0.0, ["M", "|"]),
    "rapier":    ("Rapier", 1.5, -2.0, 0.5, ["  M", " M ", "|  "]),
    "katana":    ("Katana", 2.5, -2.2, 0.0, ["  M", " M ", "|  "][::1] if False else [" M", "M ", "| "]),
    "glaive":    ("Glaive", 3.0, -2.8, 1.0, [" MM", " | ", "|  "]),
    "halberd":   ("Halberd", 4.0, -3.0, 1.0, ["MM ", "M| ", " | "]),
    "warhammer": ("Warhammer", 5.0, -3.2, 0.0, ["MMM", "MMM", " | "]),
}
def palette(mat):
    im = Image.open(f"{R}/assets/{NS}/textures/item/{mat}_sword.png").convert("RGBA")
    px = sorted({p for p in im.getdata() if p[3] > 0}, key=lambda p: sum(p[:3]))
    return px[0], px[len(px) // 2], px[-1]
WOOD = ((74, 48, 24, 255), (110, 74, 38, 255))
def draw(kind, mat):
    dark, mid, light = palette(mat)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); P = im.putpixel
    def line(pts, c):
        for x, y in pts: P((x, 15 - y), c)
    diag = [(i, i) for i in range(16)]
    if kind == "dagger":
        line([(i, i) for i in range(5, 12)], mid); line([(i + 1, i) for i in range(6, 12)], light); line([(3, 4), (4, 3), (4, 5), (5, 4)], dark); line([(1, 1), (2, 2), (3, 3)], WOOD[0])
    elif kind == "rapier":
        line([(i, i) for i in range(5, 15)], light); line([(i, i + 1) for i in range(5, 14)], mid)
        line([(3, 5), (4, 4), (5, 3), (4, 5), (5, 4), (3, 4), (4, 3)], dark); line([(1, 1), (2, 2), (3, 3)], WOOD[1])
    elif kind == "katana":
        line([(i, i) for i in range(4, 15)], light); line([(i + 1, i) for i in range(4, 14)], mid); line([(14, 15), (15, 15)], mid)
        line([(3, 4), (4, 3)], dark); line([(0, 0), (1, 1), (2, 2), (3, 3)], WOOD[0])
    elif kind == "glaive":
        line([(i, i) for i in range(0, 10)], WOOD[1]); line([(i, i + 1) for i in range(0, 9)], WOOD[0])
        line([(i, i) for i in range(10, 15)], light); line([(i + 1, i) for i in range(10, 14)], mid); line([(9, 11), (8, 12), (10, 12)], mid)
    elif kind == "halberd":
        line([(i, i) for i in range(0, 12)], WOOD[1]); line([(i, i + 1) for i in range(0, 11)], WOOD[0])
        line([(12, 13), (13, 14), (14, 15)], light); line([(9, 11), (10, 10), (11, 9), (12, 8), (13, 7)], mid); line([(10, 12), (11, 11), (12, 10), (13, 9), (14, 8)], light)
    elif kind == "warhammer":
        line([(i, i) for i in range(0, 11)], WOOD[1]); line([(i, i + 1) for i in range(0, 10)], WOOD[0])
        for x in range(8, 14):
            for y in range(8, 14):
                if abs((x - 11) - (y - 11)) <= 3: P((x, 15 - y), mid if (x + y) % 2 else light)
        line([(8, 8), (9, 9)], dark)
    return im
def save(p, s): os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w").write(s)
def merge_tag(path, ids):
    try: t = json.load(open(path))
    except Exception: t = {"replace": False, "values": []}
    t["values"] = sorted(set(t["values"]) | set(ids)); save(path, json.dumps(t, indent=1))
items = []
lang = json.load(open(f"{R}/assets/{NS}/lang/en_us.json"))
gates = json.load(open(f"{os.path.dirname(__file__)}/../age_gates.json"))
for mat, (ing, age) in MATS.items():
    for k, (en, dmg, spd, reach, pat) in KINDS.items():
        iid = f"{mat}_{k}"
        items.append(dict(id=iid, mat=mat, damage=dmg, speed=spd, reach=reach))
        draw(k, mat).save(_ := f"{R}/assets/{NS}/textures/item/{iid}.png") if os.makedirs(f"{R}/assets/{NS}/textures/item", exist_ok=True) is None else None
        save(f"{R}/assets/{NS}/models/item/{iid}.json", json.dumps({"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/{iid}"}}, indent=1))
        save(f"{R}/assets/{NS}/items/{iid}.json", json.dumps({"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}}, indent=1))
        n = 2 if k == "warhammer" else 1
        key = {"M": ing, "|": "#c:rods/wooden"}
        if k == "dagger": key = {"M": ing, "|": "#c:rods/wooden"}
        save(f"{R}/data/{NS}/recipe/{iid}.json", json.dumps({"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pat, "key": key, "result": {"id": f"{NS}:{iid}", "count": 1}}, indent=1))
        lang[f"item.{NS}.{iid}"] = f"{mat.capitalize()} {en}"
        gates["items"][f"{NS}:{iid}"] = age
ids = [f"{NS}:{i['id']}" for i in items]
save(f"{R}/data/{NS}/roster.json", json.dumps({"items": items}, indent=1))
save(f"{R}/assets/{NS}/lang/en_us.json", json.dumps(lang, indent=1, ensure_ascii=False))
save(f"{os.path.dirname(__file__)}/../age_gates.json", json.dumps(gates, indent=1, ensure_ascii=False))
for t in ("swords", "enchantable/sword", "enchantable/melee_weapon", "enchantable/sharp_weapon", "enchantable/weapon", "enchantable/durability"):
    merge_tag(f"{R}/data/minecraft/tags/item/{t}.json", ids)
print(len(items), "roster items")
