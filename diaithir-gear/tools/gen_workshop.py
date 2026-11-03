#!/usr/bin/env python3
"""Writes src/main/resources/data/diaithirgear/workshop/*.json: materials, starter melee/tool/bow blueprints and one blueprint per gun in diaithir-firearms.
Usage: gen_workshop.py [guns.json]   (default ../diaithir-firearms/src/main/resources/guns.json)"""
import json, os, sys

OUT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/data/diaithirgear/workshop")
GUNS = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "../../diaithir-firearms/src/main/resources/guns.json")
AGES = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "RODOS": 8, "FUTURE": 9}

COLORS = dict(clay=0xB5694A, wood=0x8B6B3E, bone=0xE6DFC8, flint=0x4A4A52, leather=0x7A4A2A, string=0xDDDDCC, copper=0xC8734A, bronze=0xA8803A, iron=0xC9CCD0, steel=0x9AA6B2, gunmetal=0x4F5963, tempered=0x7FA3C4, ceramic=0xE8E2D4, rodos_alloy=0xD0A640, star_lattice=0x8FE0FF)

def mat(id, age, h, t, d, f, roles, items):
    return dict(id=id, age=age, hardness=h, toughness=t, density=d, flex=f, color=COLORS[id], roles=roles, items=items)

METAL = ["head", "guard", "pot", "barrel", "lock"]
materials = [
    mat("wood", 1, 2, 5, 0.65, 7, ["handle", "stock", "grip", "limb", "pot"], ["#minecraft:planks"]),
    mat("bone", 1, 3, 3, 1.9, 3, ["handle", "head", "guard", "grip"], ["minecraft:bone"]),
    mat("clay", 1, 3, 2, 1.8, 0, ["pot"], ["minecraft:clay_ball", "minecraft:brick"]),
    mat("flint", 1, 4, 1, 2.6, 0, ["head"], ["minecraft:flint"]),
    mat("leather", 1, 1, 6, 0.9, 9, ["binding", "grip"], ["minecraft:leather"]),
    mat("string", 1, 1, 4, 1.0, 8, ["binding", "bowstring"], ["minecraft:string"]),
    mat("copper", 2, 3, 4, 8.96, 5, ["head", "guard", "pot"], ["#c:ingots/copper"]),
    mat("bronze", 3, 5, 5, 8.8, 4, ["head", "guard", "pot", "barrel"], ["#c:ingots/bronze"]),
    mat("iron", 4, 6, 5, 7.87, 3, METAL, ["#c:ingots/iron"]),
    mat("steel", 5, 8, 6, 7.85, 3, METAL, ["#c:ingots/steel"]),
    mat("gunmetal", 6, 7, 7, 8.7, 3, ["barrel", "lock", "guard"], ["#c:ingots/gunmetal"]),
    mat("tempered", 6, 9, 7, 7.85, 3, METAL, ["diaithirarms:tempered_ingot"]),
    mat("ceramic", 7, 9, 3, 3.9, 1, ["head", "pot"], ["diaithirarms:ceramic_plate"]),
    mat("rodos_alloy", 8, 10, 9, 5.0, 4, METAL, ["diaithirarms:rodos_alloy_ingot"]),
    mat("star_lattice", 9, 10, 10, 2.5, 6, METAL, ["diaithirarms:star_lattice"]),
]

def slot(name, role, count, litres, d, u, s):
    return dict(name=name, role=role, count=count, litres=litres, damage=d, durability=u, speed=s)

def bp(id, station, base, age, kind, dmg, spd, dur, ref, slots, imp=2):
    return dict(id=id, station=station, base=base, age=age, kind=kind, damage=dmg, speed=spd, durability=dur, ref_mass_kg=ref, improvements=imp, slots=slots)

blade = lambda c, l: slot("head", "head", c, l, 1.0, 0.6, 0.0)
grip = lambda l: slot("handle", "handle", 1, l, 0.1, 0.4, 1.0)
guard = slot("guard", "guard", 1, 0.05, 0.0, 0.3, 0.0)
bind = slot("binding", "binding", 1, 0.02, 0.0, 0.4, 0.5)
IR = "minecraft:iron_"
blueprints = [
    bp("sword", "smithy", IR + "sword", 1, "melee", 7.0, -2.4, 400, 1.2, [blade(2, 0.15), guard, grip(0.2), bind]),
    bp("dagger", "smithy", IR + "sword", 1, "melee", 4.5, -1.6, 300, 0.4, [blade(1, 0.06), grip(0.1), bind], 1),
    bp("axe", "smithy", IR + "axe", 1, "melee", 9.0, -3.1, 400, 1.8, [blade(3, 0.2), grip(0.3), bind]),
    bp("spear", "smithy", IR + "spear", 1, "melee", 5.0, -2.8, 350, 1.5, [blade(1, 0.08), grip(0.9), bind]),
    bp("pickaxe", "smithy", IR + "pickaxe", 1, "tool", 3.0, -2.8, 450, 1.8, [blade(3, 0.2), grip(0.3), bind]),
    bp("shovel", "smithy", IR + "shovel", 1, "tool", 3.5, -3.0, 450, 1.2, [blade(1, 0.15), grip(0.3), bind]),
    bp("bow", "workshop", "minecraft:bow", 1, "ranged", 1.0, 0.0, 400, 0.8, [slot("limbs", "limb", 2, 0.4, 1.0, 0.6, 1.0), slot("string", "bowstring", 1, 0.01, 0.5, 0.5, 0.5), slot("grip", "grip", 1, 0.03, 0.0, 0.3, 0.5)]),
    bp("crossbow", "workshop", "minecraft:crossbow", 3, "ranged", 1.0, 0.0, 450, 2.5, [slot("stock", "stock", 2, 0.8, 0.3, 0.5, 0.5), slot("prod", "head", 2, 0.2, 1.0, 0.6, 0.5), slot("string", "bowstring", 1, 0.01, 0.5, 0.5, 0.5), slot("lock", "lock", 1, 0.02, 0.0, 0.4, 0.5)]),
]

# guns: slot recipe by class (barrel + lock + stock), counts scale with size; damage/spread come from barrel and lock
SHAPE = {"musket": (2, 1, 2), "pistol": (1, 1, 1), "smg": (2, 1, 1), "rifle": (2, 2, 2), "sniper": (3, 2, 2), "shotgun": (2, 2, 2), "lmg": (3, 3, 2), "launcher": (3, 2, 2), "energy": (2, 3, 1)}
g = json.load(open(GUNS))
for x in g["guns"]:
    b, l, s = SHAPE[x["class"]]
    mass = {"musket": 4.0, "pistol": 1.0, "smg": 2.5, "rifle": 3.5, "sniper": 5.5, "shotgun": 3.5, "lmg": 8.0, "launcher": 6.0, "energy": 3.0}[x["class"]]
    blueprints.append(dict(bp(x["id"], "gunsmith", "diaithirfirearms:" + x["id"], AGES[x["age"].upper()], "gun", 1.0, 0.0, 1000, mass,
                         [slot("barrel", "barrel", b, 0.05 * b, 1.0, 0.5, 0.0), slot("lock", "lock", l, 0.03 * l, 0.4, 0.5, 0.5), slot("stock", "stock", s, 0.2 * s, 0.0, 0.3, 1.0)], 2), model="gun"))

# ---- historical melee weapons (spartan-weaponry, ports #182): same parts as the matching core blueprint -----------------------
IN = os.path.join(os.path.dirname(__file__), "inputs")
ROMAN = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "RODOS": 8, "FUTURE": 9}
ARCH = {}
for t in "saber longsword katana greatsword rapier".split(): ARCH[t] = "sword"
for t in "dagger parrying_dagger throwing_knife".split(): ARCH[t] = "dagger"
for t in "spear javelin pike lance glaive halberd quarterstaff".split(): ARCH[t] = "spear"
for t in "tomahawk battleaxe warhammer battle_hammer flanged_mace club boomerang".split(): ARCH[t] = "axe"
core = {b["id"]: b for b in blueprints}
ages = json.load(open(os.path.join(IN, "historical_ages.json")))
hist = []
for w in json.load(open(os.path.join(IN, "historical_weapons.json"))):
    a = ARCH.get(w["type"], "axe"); c = core[a]
    hist.append(dict(c, id="hw_" + w["id"], base="spartanweaponry:" + w["id"], age=ROMAN.get(ages.get(w["id"], "IV").upper(), 4), damage=round(w["damage"] + 2.0, 2), speed=round(w["speed"] - 4.0, 2), model=a))

# ---- everyday tools and kitchen items (diaithir-furnishings, ports #185) -------------------------------------------------------
SMITHY_CATS = {"metalwork", "general", "mining", "farm", "household", "cookware", "utensil"}
VESSEL = {"cookware": (3, 0.6, True), "storage": (2, 0.8, False), "tableware": (1, 0.2, False), "lighting": (1, 0.1, False)}
everyday = []
for it in json.load(open(os.path.join(IN, "everyday.json")))["items"]:
    cat = it["category"]
    if cat == "furniture":  # a wooden frame and a fixing; the module has no recipes of its own
        slots = [slot("frame", "handle", 4, 0.8, 0.0, 0.4, 1.0), slot("fixing", "binding", 1, 0.02, 0.0, 0.2, 0.5)]; model = "shovel"
    elif cat in VESSEL:
        n, l, handle = VESSEL[cat]
        slots = [slot("pot", "pot", n, l, 0.5, 0.7, 0.0)] + ([slot("handle", "handle", 1, 0.1, 0.0, 0.3, 1.0)] if handle else [])
        model = "vessel"
    elif cat == "utensil":
        slots = [slot("head", "head", 1, 0.04, 0.5, 0.6, 0.0), slot("handle", "handle", 1, 0.05, 0.0, 0.4, 1.0)]; model = "shovel"
    else:
        slots = [slot("head", "head", 1, 0.1, 0.5, 0.6, 0.0), slot("handle", "handle", 1, 0.2, 0.0, 0.3, 1.0), slot("binding", "binding", 1, 0.02, 0.0, 0.2, 0.5)]; model = "shovel"
    b = bp("ev_" + it["id"], "smithy" if cat in SMITHY_CATS else "workshop", "diaithirfurnishings:" + it["id"], ROMAN[it["age"].upper()], "item", 1.0, 0.0, max(it["durability"], 100), 1.0, slots, 1)
    b["model"] = model
    everyday.append(b)

os.makedirs(OUT, exist_ok=True)
json.dump(materials, open(os.path.join(OUT, "materials_base.json"), "w"), indent=1)
json.dump([b for b in blueprints if b["kind"] != "gun"], open(os.path.join(OUT, "blueprints_arms.json"), "w"), indent=1)
json.dump([b for b in blueprints if b["kind"] == "gun"], open(os.path.join(OUT, "blueprints_guns.json"), "w"), indent=1)
improvements = [
    dict(id="keen", item="minecraft:flint", damage=0.5, durability=0, name="Keen edge"),
    dict(id="balanced", item="minecraft:iron_nugget", damage=0.25, durability=40, name="Balanced"),
    dict(id="wrapped", item="minecraft:leather", damage=0.0, durability=80, name="Wrapped grip"),
    dict(id="fine", item="minecraft:gold_nugget", damage=0.0, durability=120, name="Fine finish"),
]
json.dump(improvements, open(os.path.join(OUT, "improvements_base.json"), "w"), indent=1)
json.dump(hist, open(os.path.join(OUT, "blueprints_historical.json"), "w"), indent=1)
json.dump(everyday, open(os.path.join(OUT, "blueprints_everyday.json"), "w"), indent=1)
json.dump({"materials": ["materials_base.json"], "improvements": ["improvements_base.json"], "blueprints": ["blueprints_arms.json", "blueprints_guns.json", "blueprints_historical.json", "blueprints_everyday.json"]}, open(os.path.join(OUT, "index.json"), "w"), indent=1)
print(len(materials), "materials", len(blueprints) + len(hist) + len(everyday), "blueprints")
