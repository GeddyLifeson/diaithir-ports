#!/usr/bin/env python3
"""Generates the rail yard data from data/rolling_stock.csv: yards.json (read by the mod), station recipes, age gate, item models, lang.
Run from diaithir-carts: python3 tools/gen_rolling_stock.py"""
import csv, json, os, math
R = "src/main/resources"
NS = "diaithircarts"
import re
rows = list(csv.DictReader(open("data/rolling_stock.csv")))
OLD = [p["item"].split(":")[1] for p in json.load(open(f"{R}/data/{NS}/yards.json"))["parts"]] if os.path.exists(f"{R}/data/{NS}/yards.json") else []
PRIMITIVE_NARROW = {"wag_wagonway", "wag_tub", "loco_horse_gin"}   # early wooden wagonways and tub trams ran on light narrow track
for r in rows: r["gauge"] = r.get("gauge") or ("narrow" if r["id"] in PRIMITIVE_NARROW else "standard")

# Narrow-gauge twins of the freight and passenger types that really ran on narrow track: a lighter wagon with 40% of the payload (axle load 10 t against 25 t) (Gauge.NARROW).
NARROW_TYPES = ["wag_boxcar", "wag_open_hopper", "wag_tank", "wag_flatcar", "wag_gondola", "wag_stock", "wag_caboose", "wag_log", "wag_ore",
                "pas_compartment", "pas_open_saloon", "pas_baggage", "pas_mail", "pas_observation", "pas_dining"]
byid = {r["id"]: r for r in rows}
for i in NARROW_TYPES:
    b = byid[i]
    rows.append({"id": "n" + i, "category": b["category"], "name": "Narrow-gauge " + b["name"][0].lower() + b["name"][1:], "year": b["year"],
                 "mass_kg": str(int(int(b["mass_kg"]) * 0.5)), "power_w_or_capacity": str(int(int(b["power_w_or_capacity"]) * 0.4)), "source": b["source"], "gauge": "narrow"})


# Every standard- and narrow-gauge class harvested from Wikipedia (tools/harvest_stock.py), any country. Classes with no date even in the article are skipped.
EST = {"loco_steam": (60000, 1_000_000), "loco_diesel": (90000, 1_500_000), "loco_electric": (100000, 3_000_000), "mu": (45000, 1_000_000),
       "passenger": (35000, 60), "freight": (22000, 70000)}
EST_NARROW = {"loco_steam": (22000, 150_000), "loco_diesel": (20000, 250_000), "loco_electric": (30000, 300_000), "mu": (15000, 200_000),
              "passenger": (9000, 24), "freight": (6000, 25000)}
SPEC = {}   # vehicle id -> spec row from the harvest (wheel arrangement, cylinders, valve gear, boiler, fuel, drivers, prime mover, transmission, role)
have = {r["source"] for r in rows}
used = {r["id"] for r in rows}
skipped = 0
RAW = os.environ.get("STOCK_RAW", "data/stock_raw.csv")
for h in (csv.DictReader(open(RAW)) if os.path.exists(RAW) else []):
    if int(h["year"]) == 0 or h["source"] in have: skipped += 1; continue
    slug = re.sub(r"[^a-z0-9]+", "_", h["name"].lower()).strip("_")[:40] or "stock"
    pre = "loco_" if h["category"].startswith("loco") else "unit_" if h["category"] == "mu" else "wag_" if h["category"] == "freight" else "pas_"
    i = pre + slug; n = 2
    while i in used: i = f"{pre}{slug}_{n}"; n += 1
    used.add(i)
    est = (EST_NARROW if h["gauge"] == "narrow" else EST)[h["category"]]
    mass = int(h["mass_kg"]) or est[0]
    pw = int(h["power_w"]) or est[1]
    if h["gauge"] == "narrow": mass = min(mass, 90000)
    rows.append({"id": i, "category": h["category"], "name": h["name"], "year": h["year"], "mass_kg": str(mass), "power_w_or_capacity": str(pw), "source": h["source"], "gauge": h["gauge"]})
    SPEC[i] = h
ENGINE = {"loco_steam": "steam_piston", "loco_primitive": "steam_piston", "loco_diesel": "diesel_engine", "loco_electric": "electric_motor"}   # engine classes of diaithir-units' fuel table
CREW = {"loco_steam": 2, "loco_diesel": 1, "loco_electric": 1, "loco_primitive": 1, "mu": 1}

def age(y):
    y = int(y)
    return 3 if y < -40 else 4 if y < 1780 else 5 if y < 1930 else 6 if y < 2000 else 7
def tier(y):
    y = int(y)
    return "WORKSHOP" if y < 1830 else "ERECTING_SHOP" if y < 1900 else "ASSEMBLY_LINE" if y < 1990 else "AUTOMATED"
def size(kg):
    return "xs" if kg < 5000 else "s" if kg < 25000 else "m" if kg < 100000 else "l"
FACT = {"xs": 0.05, "s": 0.5, "m": 1.0, "l": 2.5}
WB, SF, AS = "station.wright_bench", "station.steam_forge", "machine.assembler"
IRON, COPPER, PLANK = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:oak_planks"

# Parts are built from the vehicle's spec, not only its size, so two similar classes need different parts. Each part is its own slot kind.
# station: where it is made (wright bench by hand, steam forge, assembler from the machine Age); base mass kg, power W, structural hp, ingredients at size m.
PARTS = {}   # part id -> {kind, mass, power, hp, station, stage, ing, time, age, name}
def part(pid, mass, power, hp, station, ing, sz, name, y, scale=1.0):
    f = FACT[sz] * scale
    ing = [(i, math.ceil(n * max(f, 0.25) * (1 if f <= 1 else 2.5)) if f > 0.25 else n) for i, n in ing]
    P = PARTS.setdefault(pid, {"mass": round(mass * f), "power": round(power * f), "hp": hp, "station": station, "stage": "assembly" if station == AS else "part",
                              "ing": ing, "time": int(10 + 10 * f), "age": 9, "name": name})
    P["age"] = min(P["age"], age(y))
    return pid

def whyte(w):
    """(lead axles, driving axles, trailing axles, is_tank) from a Whyte notation like 4-6-2 or 2-8-0T; articulated engines add their groups."""
    tank = bool(re.search(r"\d(?:T|ST|PT|WT|RT|F)\b", w)) or w.upper().endswith("T")
    g = [int(x) for x in re.findall(r"\d+", w)]
    if len(g) < 3: return None
    if len(g) == 3: lead, drv, trail = g
    else: lead, trail, drv = g[0], g[-1], sum(g[1:-1])
    return lead // 2, max(1, drv // 2), trail // 2, tank
def dband(mm):
    return "sm" if 0 < mm < 1300 else "lg" if mm >= 1700 else "md"
def pband(w):
    return "a" if w < 300_000 else "b" if w < 1_000_000 else "c" if w < 2_000_000 else "d" if w < 3_500_000 else "e"
def vgear(v):
    v = v.lower()
    for k in ("walschaerts", "stephenson", "baker", "caprotti", "southern", "joy", "gooch", "allan", "hackworth", "heusinger", "bulleid", "franklin"):
        if k in v: return k
    return "plain"
VG_EXTRA = {"walschaerts": [("minecraft:iron_chain", 2)], "stephenson": [("minecraft:iron_chain", 1)], "baker": [("minecraft:iron_chain", 3)],
            "caprotti": [("minecraft:redstone", 4)], "southern": [("minecraft:iron_chain", 2)], "joy": [("minecraft:iron_chain", 1)],
            "bulleid": [("minecraft:redstone", 6)], "franklin": [("minecraft:redstone", 4)]}
def vname(vk, sz):
    return f"{sz.upper()} {vk} cylinder"
def engine_family(pm):
    m = re.sub(r"[^a-z0-9 ]", " ", pm.lower()).split()
    return (m[0] + (m[1] if len(m) > 1 and not m[1].isdigit() and len(m[1]) < 6 else ""))[:12] if m else "diesel"
def trans_of(sp, pw):
    t = (sp.get("transmission", "") + " " + sp.get("role", "")).lower()
    return "hydraulic" if "hydraul" in t else "mechanical" if re.search(r"mechan|gear", t) else "electric" if "electric" in t else ("electric" if pw > 600_000 else "mechanical")
FREIGHT_SUB = ("hopper", "tank", "box", "van", "flat", "gondola", "open", "reefer", "stock", "log", "ore", "brake", "crane", "well", "auto", "container", "cement", "coal")
PASS_SUB = ("sleeper", "dining", "observation", "baggage", "mail", "dome", "parlor", "lounge", "compartment", "saloon", "pullman", "coach")
def sub(name, opts, default):
    n = name.lower()
    return next((o for o in opts if o in n), default)
def is_shunter(name, sp):
    return bool(re.search(r"switch|shunt|yard|\bSW\d|\bNW\d", name + " " + sp.get("role", ""), re.I))

def spec_stages(r, sz, y):
    """The stage list of the build chain for one vehicle: [(stage id, [(part id, count)])]."""
    cat, g, nm = r["category"], r["gauge"], r["name"]; sp = SPEC.get(r["id"], {}); pw = int(r["power_w_or_capacity"]); mass = int(r["mass_kg"])
    lt = "light" if g == "narrow" else "std"
    coupler = ("coupling", [(part(f"coupler_{lt}_{sz}", 120, 0, 80, SF, [(IRON, 3), ("minecraft:iron_chain", 1)], sz, f"{sz.upper()} {lt} coupler", y, 0.5 if g == "narrow" else 1), 1)])
    def bogies(n=2):
        return ("frame", [(part(f"bogie_{lt}_{sz}", 4000, 0, 60, SF, [(IRON, 12), ("minecraft:iron_chain", 2)], sz, f"{sz.upper()} {lt} bogie", y, 0.5 if g == "narrow" else 1), n)])
    def cab():
        return ("cab", [(part(f"cab_{sz}", 1800, 0, 20, WB, [(PLANK, 6), ("minecraft:glass_pane", 4), (IRON, 2)], sz, f"{sz.upper()} cab", y), 1)])
    if cat in ("freight", "passenger"):
        what = sub(nm, FREIGHT_SUB if cat == "freight" else PASS_SUB, "general" if cat == "freight" else "coach")
        body = part(f"body_{what}_{sz}", 8000, 0, 30, WB, [(PLANK, 12), (IRON, 4)] + ([("minecraft:bucket", 1)] if what == "tank" else [("minecraft:hopper", 1)] if what == "hopper" else [("minecraft:glass_pane", 6)] if cat == "passenger" else []), sz, f"{sz.upper()} {what} body", y)
        if mass < 10000 or sz == "xs":
            frame = ("frame", [(part(f"axle_wagon_{lt}_{sz}", 600, 0, 40, WB, [(IRON, 3), (PLANK, 2)], sz, f"{sz.upper()} {lt} wheelset", y), 2)])
        else: frame = bogies()
        return [frame, ("body", [(body, 1)]), coupler]
    if cat == "mu":
        out = [bogies(), ("body", [(part(f"body_unit_{sz}", 8000, 0, 30, WB, [(PLANK, 10), (IRON, 4), ("minecraft:glass_pane", 8)], sz, f"{sz.upper()} unit body", y), 1)])]
        out.append(power_stage(r, sz, y, sp, pw, "diesel" if re.search(r"diesel|petrol|hydraul", (sp.get("prime_mover", "") + sp.get("transmission", "")).lower()) else "electric"))
        return out + [coupler]
    if cat == "loco_steam":
        w = whyte(sp.get("wheel") or nm)
        lead, drv, trail, tank = w or ((0, 2, 0, True) if sz == "xs" else (0, 3, 0, True) if sz == "s" else (1, 3, 0, False) if sz == "m" else (2, 4, 2, False))
        db = dband(int(sp.get("driver_mm") or 0) or 1500)
        axles = []
        if lead: axles.append((part(f"axle_lead_{lt}_{sz}", 500, 0, 30, WB, [(IRON, 3), (PLANK, 1)], sz, f"{sz.upper()} leading axle", y), lead))
        axles.append((part(f"axle_drive_{db}_{lt}_{sz}", 900, 0, 50, SF, [(IRON, 4 if db != "lg" else 6), (PLANK, 1)], sz, f"{sz.upper()} {db} driving axle", y), drv))
        if trail: axles.append((part(f"axle_trail_{lt}_{sz}", 500, 0, 30, WB, [(IRON, 3), (PLANK, 1)], sz, f"{sz.upper()} trailing axle", y), trail))
        bar = float(sp.get("boiler_bar") or 0) or 12
        pt = "lo" if bar < 10 else "md" if bar < 15 else "hi" if bar < 20 else "ss"
        fuel = "oil" if "oil" in sp.get("fuel", "") else "wood" if "wood" in sp.get("fuel", "") else "coal"
        cu = {"lo": 4, "md": 8, "hi": 12, "ss": 16}[pt]
        boiler = part(f"boiler_{pt}_{fuel}_{sz}", 15000, 1_200_000 if pt != "lo" else 700_000, 80, SF, [(IRON, 24), (COPPER, cu), ("minecraft:furnace", 1), ("minecraft:bucket", 1 if fuel != "oil" else 2)], sz, f"{sz.upper()} {pt} pressure {fuel} boiler", y)
        vg = vgear(sp.get("valve_gear", ""))
        cyl = part(f"cyl_{vg}_{sz}", 1500, 0, 50, SF, [(IRON, 6), ("minecraft:piston", 1)] + VG_EXTRA.get(vg, []), sz, vname(vg, sz), y)
        ncyl = int(sp.get("cylinders") or 2) or 2
        out = [("frame", axles), ("power", [(boiler, 1)]), ("cylinders", [(cyl, ncyl)]), cab()]
        if tank: out.append(("tank", [(part(f"tank_{sz}", 4000, 0, 30, SF, [(IRON, 10), ("minecraft:bucket", 2)], sz, f"{sz.upper()} water tank", y), 1)]))
        else: out.append(("tender", [(part(f"tender_{lt}_{sz}", 12000, 0, 40, WB, [(IRON, 16), (PLANK, 8), ("minecraft:bucket", 2)], sz, f"{sz.upper()} tender", y, 0.5 if g == "narrow" else 1), 1)]))
        return out + [coupler]
    if cat == "loco_diesel":
        return [bogies(), power_stage(r, sz, y, sp, pw, "diesel"), cab(), coupler]
    if cat == "loco_electric":
        return [bogies(), power_stage(r, sz, y, sp, pw, "electric"), cab(), coupler]
    if cat == "loco_primitive":
        return [("frame", [(part(f"axle_wagon_{lt}_{sz}", 600, 0, 40, WB, [(IRON, 3), (PLANK, 2)], sz, f"{sz.upper()} wheelset", y), 4)]), ("power", [(part(f"boiler_lo_coal_{sz}", 15000, 700_000, 80, SF, [(IRON, 24), (COPPER, 4), ("minecraft:furnace", 1), ("minecraft:bucket", 1)], sz, f"{sz.upper()} lo pressure coal boiler", y), 1)])]
    return [("frame", [(part(f"axle_wagon_{lt}_{sz}", 600, 0, 40, WB, [(IRON, 3), (PLANK, 2)], sz, f"{sz.upper()} wheelset", y), 4)]), ("body", [(part(f"body_general_{sz}", 8000, 0, 30, WB, [(PLANK, 12), (IRON, 4)], sz, f"{sz.upper()} general body", y), 1)])]

def power_stage(r, sz, y, sp, pw, kind):
    pb = pband(pw)
    if kind == "diesel":
        fam = engine_family(sp.get("prime_mover", ""))
        eng = part(f"engine_{fam}_{pb}_{sz}", 9000, 1_500_000, 70, AS, [(IRON, 24), ("minecraft:piston", 6), ("minecraft:redstone", 8 + 2 * "abcde".index(pb)), ("minecraft:bucket", 1)], sz, f"{sz.upper()} {fam} diesel engine ({pb})", y)
        tr = trans_of(sp, pw)
        ing = {"electric": [(IRON, 8), (COPPER, 16)], "hydraulic": [(IRON, 12), ("minecraft:bucket", 1), ("minecraft:piston", 2)], "mechanical": [(IRON, 14), ("minecraft:iron_chain", 2)]}[tr]
        return ("power", [(eng, 1), (part(f"trans_{tr}_{sz}", 3000, 0, 50, AS if tr == "electric" else SF, ing, sz, f"{sz.upper()} {tr} transmission", y), 1)])
    pt = (sp.get("prime_mover", "") + " " + sp.get("transmission", "") + " " + r["name"]).lower()
    col = "battery" if "battery" in pt else "third_rail" if re.search(r"third rail|third-rail|dc 600|750 v", pt) else "overhead"
    motor = part(f"motor_{pb}_{sz}", 5000, 3_000_000, 60, AS, [(IRON, 16), (COPPER, 16), ("minecraft:redstone", 8 + 2 * "abcde".index(pb))], sz, f"{sz.upper()} traction motor ({pb})", y)
    cing = {"overhead": [(COPPER, 6), (IRON, 3)], "third_rail": [(IRON, 6), (COPPER, 2)], "battery": [(COPPER, 8), ("minecraft:redstone", 8), (IRON, 4)]}[col]
    return ("power", [(motor, 1), (part(f"collector_{col}", 400, 0, 15, SF if col != "battery" else AS, cing, "s" if sz in ("xs", "s") else "m", f"{col.replace('_', ' ')} collector", y), 1)])

parts_used, yards, items, cranes = set(), [], [], []
for r in rows:
    cat, y = r["category"], int(r["year"])
    if cat == "crane":
        cranes.append({"id": r["id"], "name": r["name"], "lift_kg": int(r["power_w_or_capacity"]), "age": age(y),
                       "speed": round(1 + math.log10(max(int(r["power_w_or_capacity"]), 10)) / 3, 2)}); continue
    if cat == "primitive_yard": continue
    sz = size(int(r["mass_kg"]))
    sl = []
    for sid, slots in spec_stages(r, sz, y):
        sl.append({"id": sid, "slots": [{"kind": k, "count": n} for k, n in slots], "base_ticks": 100 if sid == "power" else 60, "ghost": f"{NS}:yard/{sid}"})
    yards.append({"id": f"yard_{r['id']}", "tier": tier(y), "site": "GROUND", "result": f"{NS}:{r['id']}", "gauge": r["gauge"], "stages": sl, "age": age(y), "footprint": [5, 20, 6]})
    items.append({"id": r["id"], "name": r["name"], "age": age(y), "category": cat, "mass_kg": int(r["mass_kg"]), "rated": int(r["power_w_or_capacity"]),
                  "gauge": r["gauge"], "crew": CREW.get(cat, 0), "engine": ENGINE.get(cat, "")})
partdefs = [{"item": f"{NS}:{pid}", "kind": pid, "mass_kg": P["mass"], "power_w": P["power"], "hp": P["hp"], "min_age": P["age"], "block": "", "name": P["name"]}
            for pid, P in sorted(PARTS.items())]
controllers = [("rail_workshop", "WORKSHOP", "Rail Workshop", 3), ("rail_erecting_shop", "ERECTING_SHOP", "Erecting Shop", 4),
               ("rail_assembly_line", "ASSEMBLY_LINE", "Rail Assembly Line", 6), ("rail_automated_yard", "AUTOMATED", "Automated Rail Yard", 7)]
out = {"parts": partdefs, "yards": yards, "controllers": [{"id": c[0], "tier": c[1], "name": c[2], "age": c[3]} for c in controllers], "cranes": cranes, "vehicles": items}
json.dump(out, open(f"{R}/data/{NS}/yards.json", "w"), indent=1)
# drop the files of parts an earlier run generated and this one no longer uses
for old in set(OLD) - set(PARTS):
    for f in (f"{R}/data/{NS}/diaithir_stations/station_recipe/{old}.json", f"{R}/assets/{NS}/models/item/{old}.json", f"{R}/assets/{NS}/items/{old}.json"):
        if os.path.exists(f): os.remove(f)
# station recipes
rd = f"{R}/data/{NS}/diaithir_stations/station_recipe"; os.makedirs(rd, exist_ok=True)
def recipe(rid, station, stage, ing, result, t):
    d = {"station": station, "stage": stage, "time_s": t, "operator": False,
         "ingredients": [{"item": i, "count": max(1, min(64, n))} for i, n in ing][:9], "result": {"id": f"{NS}:{rid}", "count": 1}}
    if station == "station.steam_forge": d.update({"power": "steam", "w": 20000})
    json.dump(d, open(f"{rd}/{rid}.json", "w"), indent=1)
for pid, P in PARTS.items():
    recipe(pid, P["station"], P["stage"], P["ing"], None, P["time"])
# magical fuel (MagicFuel.java): retrofit items, and the tag of what a fitted vehicle burns
recipe("mana_firebox", SF, "part", [("minecraft:copper_ingot", 8), ("minecraft:amethyst_shard", 8), ("minecraft:glowstone_dust", 4), ("minecraft:furnace", 1)], None, 30)
recipe("mana_cell", AS, "assembly", [("minecraft:copper_ingot", 12), ("minecraft:amethyst_shard", 12), ("minecraft:redstone", 8), ("minecraft:iron_ingot", 4)], None, 40)
os.makedirs(f"{R}/data/{NS}/tags/item", exist_ok=True)
json.dump({"replace": False, "values": [{"id": i, "required": False} for i in (
    "botania:mana_pearl", "botania:mana_diamond", "botania:mana_powder", "botania:manasteel_ingot", "ars_nouveau:source_gem", "ars_nouveau:source_gem_block",
    "theurgy:sal_ammoniac_crystal", "minecraft:amethyst_shard")]}, open(f"{R}/data/{NS}/tags/item/magic_fuel.json", "w"), indent=1)
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
byage.setdefault(4, []).extend([f"{NS}:mana_firebox", f"{NS}:mana_cell"])
for c in cranes: byage.setdefault(c["age"], []).append(f"{NS}:{c['id']}")
for v in items: byage.setdefault(v["age"], []).append(f"{NS}:{v['id']}")
for a, its in sorted(byage.items()): gate[f"yards_age{a}"] = {"age": a, "items": sorted(its)}
json.dump(gate, open(f"{R}/data/{NS}/age_gate.json", "w"), indent=2)

# item models + lang
ids = [("mana_firebox", "Mana Firebox"), ("mana_cell", "Mana Cell")] + [(pd["item"].split(":")[1], pd["name"].title()) for pd in partdefs] + [(c[0], c[2]) for c in controllers] + [(c["id"], c["name"]) for c in cranes] + [(v["id"], v["name"]) for v in items]
mi, ii = f"{R}/assets/{NS}/models/item", f"{R}/assets/{NS}/items"
os.makedirs(mi, exist_ok=True)
lang = json.load(open(f"{R}/assets/{NS}/lang/en_us.json"))
blocks = {c[0] for c in controllers} | {c["id"] for c in cranes}
mb, bs = f"{R}/assets/{NS}/models/block", f"{R}/assets/{NS}/blockstates"
os.makedirs(mb, exist_ok=True); os.makedirs(bs, exist_ok=True)
for i, n in ids:
    if i in blocks:
        top = f"{NS}:block/{i}"
        json.dump({"parent": "minecraft:block/cube_all", "textures": {"all": top}}, open(f"{mb}/{i}.json", "w"))
        json.dump({"variants": {"": {"model": f"{NS}:block/{i}"}}}, open(f"{bs}/{i}.json", "w"))
        json.dump({"model": {"type": "minecraft:model", "model": f"{NS}:block/{i}"}}, open(f"{ii}/{i}.json", "w"))
        lang[f"block.{NS}.{i}"] = n
        continue
    tex = f"{NS}:item/{i}"
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": tex}}, open(f"{mi}/{i}.json", "w"))
    json.dump({"model": {"type": "minecraft:model", "model": f"{NS}:item/{i}"}}, open(f"{ii}/{i}.json", "w"))
    lang[f"item.{NS}.{i}"] = n
json.dump(lang, open(f"{R}/assets/{NS}/lang/en_us.json", "w"), indent=1, ensure_ascii=False)
print(len(partdefs), "parts", len(yards), "yards", len(cranes), "cranes", len(items), "vehicles")
