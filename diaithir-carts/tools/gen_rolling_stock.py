#!/usr/bin/env python3
"""Generates the rail yard data from data/rolling_stock.csv: yards.json (read by the mod), station recipes, age gate, item models, lang.
Run from diaithir-carts: python3 tools/gen_rolling_stock.py"""
import csv, json, os, math
R = "src/main/resources"
NS = "diaithircarts"
rows = list(csv.DictReader(open("data/rolling_stock.csv")))

def age(y):
    y = int(y)
    return 3 if y < -40 else 4 if y < 1780 else 5 if y < 1930 else 6 if y < 2000 else 7

def tier(y):
    y = int(y)
    return "WORKSHOP" if y < 1830 else "ERECTING_SHOP" if y < 1900 else "ASSEMBLY_LINE" if y < 1990 else "AUTOMATED"

def size(kg):
    return "xs" if kg < 5000 else "s" if kg < 25000 else "m" if kg < 100000 else "l"
FACT = {"xs": 0.05, "s": 0.5, "m": 1.0, "l": 2.5}
# kind: base mass kg, base power W, structural hp, station, stage, ingredients per unit factor
KINDS = {
    "wheelset": (600, 0, 40, "station.wright_bench", "part", [("minecraft:iron_ingot", 3), ("minecraft:oak_planks", 2)]),
    "body": (8000, 0, 30, "station.wright_bench", "part", [("minecraft:oak_planks", 12), ("minecraft:iron_ingot", 4)]),
    "cab": (1800, 0, 20, "station.wright_bench", "part", [("minecraft:oak_planks", 6), ("minecraft:glass_pane", 4), ("minecraft:iron_ingot", 2)]),
    "bogie": (4000, 0, 60, "station.steam_forge", "part", [("minecraft:iron_ingot", 12), ("minecraft:iron_chain", 2)]),
    "coupler": (120, 0, 80, "station.steam_forge", "part", [("minecraft:iron_ingot", 3), ("minecraft:iron_chain", 1)]),
    "pantograph": (400, 0, 15, "station.steam_forge", "part", [("minecraft:copper_ingot", 6), ("minecraft:iron_ingot", 3)]),
    "boiler": (15000, 1_200_000, 80, "machine.assembler", "assembly", [("minecraft:iron_ingot", 24), ("minecraft:copper_ingot", 8), ("minecraft:furnace", 1), ("minecraft:bucket", 1)]),
    "diesel_engine": (9000, 1_500_000, 70, "machine.assembler", "assembly", [("minecraft:iron_ingot", 24), ("minecraft:piston", 6), ("minecraft:redstone", 8), ("minecraft:bucket", 1)]),
    "traction_motor": (5000, 3_000_000, 60, "machine.assembler", "assembly", [("minecraft:iron_ingot", 16), ("minecraft:copper_ingot", 16), ("minecraft:redstone", 8)]),
}
def stages(cat):
    if cat == "primitive_wagon": return [("frame", [("wheelset", 4)]), ("body", [("body", 1)])]
    if cat == "loco_primitive": return [("frame", [("wheelset", 4)]), ("power", [("boiler", 1)])]
    base = [("frame", [("bogie", 2)])]
    if cat == "loco_steam": return base + [("power", [("boiler", 1)]), ("cab", [("cab", 1)]), ("coupling", [("coupler", 1)])]
    if cat == "loco_diesel": return base + [("power", [("diesel_engine", 1)]), ("cab", [("cab", 1)]), ("coupling", [("coupler", 1)])]
    if cat == "loco_electric": return base + [("power", [("traction_motor", 1), ("pantograph", 1)]), ("cab", [("cab", 1)]), ("coupling", [("coupler", 1)])]
    return base + [("body", [("body", 1)]), ("coupling", [("coupler", 1)])]

parts, yards, items, cranes = {}, [], [], []
part_age = {}
for r in rows:
    cat, y = r["category"], int(r["year"])
    if cat == "crane":
        cranes.append({"id": r["id"], "name": r["name"], "lift_kg": int(r["power_w_or_capacity"]), "age": age(y),
                       "speed": round(1 + math.log10(max(int(r["power_w_or_capacity"]), 10)) / 3, 2)}); continue
    if cat == "primitive_yard": continue
    sz = size(int(r["mass_kg"]))
    sl = []
    for sid, slots in stages(cat):
        ss = []
        for kind, n in slots:
            pid = f"{kind}_{sz}"
            parts.setdefault(pid, kind)
            part_age[pid] = min(part_age.get(pid, 9), age(y))
            ss.append({"kind": kind, "count": n})
        sl.append({"id": sid, "slots": ss, "base_ticks": 100 if sid == "power" else 60, "ghost": f"{NS}:yard/{sid}"})
    yards.append({"id": f"yard_{r['id']}", "tier": tier(y), "site": "GROUND", "result": f"{NS}:{r['id']}", "stages": sl, "age": age(y), "footprint": [5, 20, 6]})
    items.append({"id": r["id"], "name": r["name"], "age": age(y), "category": cat, "mass_kg": int(r["mass_kg"]), "rated": int(r["power_w_or_capacity"])})
partdefs = []
for pid, kind in sorted(parts.items()):
    sz = pid.rsplit("_", 1)[1]; m, p, hp, st, stage, ing = KINDS[kind]; f = FACT[sz]
    partdefs.append({"item": f"{NS}:{pid}", "kind": kind, "mass_kg": round(m * f), "power_w": round(p * f), "hp": hp, "min_age": part_age[pid],
                     "block": "", "name": f"{sz.upper()} {kind.replace('_', ' ')}"})
controllers = [("rail_workshop", "WORKSHOP", "Rail Workshop", 3), ("rail_erecting_shop", "ERECTING_SHOP", "Erecting Shop", 4),
               ("rail_assembly_line", "ASSEMBLY_LINE", "Rail Assembly Line", 6), ("rail_automated_yard", "AUTOMATED", "Automated Rail Yard", 7)]
out = {"parts": partdefs, "yards": yards, "controllers": [{"id": c[0], "tier": c[1], "name": c[2], "age": c[3]} for c in controllers], "cranes": cranes, "vehicles": items}
json.dump(out, open(f"{R}/data/{NS}/yards.json", "w"), indent=1)

# station recipes
rd = f"{R}/data/{NS}/diaithir_stations/station_recipe"; os.makedirs(rd, exist_ok=True)
def recipe(rid, station, stage, ing, result, t):
    d = {"station": station, "stage": stage, "time_s": t, "operator": False,
         "ingredients": [{"item": i, "count": max(1, min(64, n))} for i, n in ing][:9], "result": {"id": f"{NS}:{rid}", "count": 1}}
    if station == "station.steam_forge": d.update({"power": "steam", "w": 20000})
    json.dump(d, open(f"{rd}/{rid}.json", "w"), indent=1)
for pd in partdefs:
    pid = pd["item"].split(":")[1]; kind = pd["kind"]; f = FACT[pid.rsplit("_", 1)[1]]
    m, p, hp, st, stage, ing = KINDS[kind]
    recipe(pid, st, stage, [(i, math.ceil(n * max(f, 0.25) * (1 if f <= 1 else 2.5)) if f > 0.25 else n) for i, n in ing], None, int(10 + 10 * f))
for c in controllers:
    recipe(c[0], "station.wright_bench", "assembly", [("minecraft:iron_block", 4 * (c[3] - 2)), ("minecraft:oak_planks", 16), ("minecraft:redstone", 4 * (c[3] - 2))], None, 30)
for c in cranes:
    recipe(c["id"], "machine.assembler", "assembly",
           [("minecraft:iron_ingot", min(64, 4 + c["lift_kg"] // 1500)), ("minecraft:iron_chain", min(16, 2 + c["age"])), ("minecraft:oak_planks", 8) if c["age"] < 5 else ("minecraft:redstone", 16)], None, 40)

# age gate
gate = json.load(open(f"{R}/data/{NS}/age_gate.json"))
gate = {k: v for k, v in gate.items() if not k.startswith("yards_age")}
byage = {}
for pd in partdefs: byage.setdefault(pd["min_age"], []).append(pd["item"])
for c in controllers: byage.setdefault(c[3], []).append(f"{NS}:{c[0]}")
for c in cranes: byage.setdefault(c["age"], []).append(f"{NS}:{c['id']}")
for v in items: byage.setdefault(v["age"], []).append(f"{NS}:{v['id']}")
for a, its in sorted(byage.items()): gate[f"yards_age{a}"] = {"age": a, "items": sorted(its)}
json.dump(gate, open(f"{R}/data/{NS}/age_gate.json", "w"), indent=2)

# item models + lang
ids = [(pd["item"].split(":")[1], pd["name"].title()) for pd in partdefs] + [(c[0], c[2]) for c in controllers] + [(c["id"], c["name"]) for c in cranes] + [(v["id"], v["name"]) for v in items]
mi, ii = f"{R}/assets/{NS}/models/item", f"{R}/assets/{NS}/items"
os.makedirs(mi, exist_ok=True)
lang = json.load(open(f"{R}/assets/{NS}/lang/en_us.json"))
blocks = {c[0] for c in controllers} | {c["id"] for c in cranes}
mb, bs = f"{R}/assets/{NS}/models/block", f"{R}/assets/{NS}/blockstates"
os.makedirs(mb, exist_ok=True); os.makedirs(bs, exist_ok=True)
for i, n in ids:
    if i in blocks:
        top = "minecraft:block/iron_block" if i.startswith("rail_") else "minecraft:block/dark_oak_planks" if i.startswith("crane_") and i in ("crane_treadwheel", "crane_shear_legs", "crane_a_frame") else "minecraft:block/smooth_stone"
        json.dump({"parent": "minecraft:block/cube_all", "textures": {"all": top}}, open(f"{mb}/{i}.json", "w"))
        json.dump({"variants": {"": {"model": f"{NS}:block/{i}"}}}, open(f"{bs}/{i}.json", "w"))
        json.dump({"model": {"type": "minecraft:model", "model": f"{NS}:block/{i}"}}, open(f"{ii}/{i}.json", "w"))
        lang[f"block.{NS}.{i}"] = n
        continue
    tex = "minecraft:item/minecart" if i in {v["id"] for v in items} else "minecraft:item/iron_ingot"
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": tex}}, open(f"{mi}/{i}.json", "w"))
    json.dump({"model": {"type": "minecraft:model", "model": f"{NS}:item/{i}"}}, open(f"{ii}/{i}.json", "w"))
    lang[f"item.{NS}.{i}"] = n
json.dump(lang, open(f"{R}/assets/{NS}/lang/en_us.json", "w"), indent=1, ensure_ascii=False)
print(len(partdefs), "parts", len(yards), "yards", len(cranes), "cranes", len(items), "vehicles")
