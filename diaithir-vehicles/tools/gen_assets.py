#!/usr/bin/env python3
"""Writes the original textures, models, blockstates, item definitions, lang, recipes, loot, tags, age gate and library entry for diaithir-vehicles.
Art is drawn here from scratch: a riveted steel drum with a hazard band (fuel tank) and a pressed-steel can (jerry can)."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirvehicles"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
for d in ("recipe", "loot_table/blocks"):
    os.makedirs(os.path.join(D, d), exist_ok=True)
for d in ("tags/fluid", "tags/block/mineable"):
    os.makedirs(os.path.join(ROOT, "data", "diaithir" if d == "tags/fluid" else "minecraft", d), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)
rnd = random.Random(11)


def wj(rel, obj, base=A):
    p = os.path.join(base, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def steel(shade):
    return tuple(max(0, min(255, int(c + rnd.randint(-5, 5)))) for c in shade)


def tank_texture():
    im = Image.new("RGBA", (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            base = (88, 96, 108) if (x % 5) else (74, 82, 94)  # vertical plate seams
            band = 6 <= y <= 9
            if band:
                c = (232, 178, 26) if ((x + y) // 2) % 2 == 0 else (30, 30, 32)
            else:
                c = steel(base)
            if x in (0, 15) or y in (0, 15):
                c = (52, 58, 66, 255)[:3]
            if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
                c = (176, 184, 196)
            px[x, y] = (*c, 255)
    return im


def can_texture():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for y in range(3, 15):
        for x in range(3, 13):
            c = steel((70, 104, 56)) if 5 <= y else steel((90, 124, 74))
            if x in (3, 12) or y in (3, 14):
                c = (36, 56, 30)
            if y in (8, 9) and 5 <= x <= 10:
                c = (220, 200, 90)  # X-ribs shown as bands
            px[x, y] = (*c, 255)
    for x in range(5, 10):  # handle and spout
        px[x, 2] = (40, 40, 44, 255)
    px[10, 2] = px[11, 1] = (60, 60, 64, 255)
    return im


tank_texture().save(os.path.join(A, "textures/block/fuel_tank.png"))
can_texture().save(os.path.join(A, "textures/item/jerry_can.png"))

wj("models/block/fuel_tank.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/fuel_tank"}})
wj("models/item/fuel_tank.json", {"parent": f"{NS}:block/fuel_tank"})
wj("models/item/jerry_can.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/jerry_can"}})
wj("blockstates/fuel_tank.json", {"variants": {"": {"model": f"{NS}:block/fuel_tank"}}})
for n in ("fuel_tank", "jerry_can"):
    wj(f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
wj("lang/en_us.json", {"block.diaithirvehicles.fuel_tank": "Fuel Tank", "item.diaithirvehicles.jerry_can": "Jerry Can"})

wj("loot_table/blocks/fuel_tank.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:fuel_tank"}],
                                                                         "conditions": [{"condition": "minecraft:survives_explosion"}]}]}, D)
wj("recipe/fuel_tank.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"I": "minecraft:iron_ingot", "B": "minecraft:bucket"},
                              "pattern": ["III", "IBI", "III"], "result": {"id": f"{NS}:fuel_tank", "count": 1}}, D)
wj("recipe/jerry_can.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"I": "minecraft:iron_ingot", "N": "minecraft:iron_nugget"},
                              "pattern": ["NI ", "I I", "III"], "result": {"id": f"{NS}:jerry_can", "count": 1}}, D)
wj("tags/block/mineable/pickaxe.json", {"values": [f"{NS}:fuel_tank"]}, os.path.join(ROOT, "data", "minecraft"))
wj("tags/block/needs_stone_tool.json", {"values": [f"{NS}:fuel_tank"]}, os.path.join(ROOT, "data", "minecraft"))
# The fuel fluid tag: what the pack's fuel capability accepts. Optional entries, so it loads with no fuel mod present.
wj("tags/fluid/fuel.json", {"values": [{"id": "#c:" + k, "required": False} for k in ("gasoline", "diesel", "biodiesel", "ethanol", "kerosene", "creosote")]}, os.path.join(ROOT, "data", "diaithir"))
wj("age_gate.json", {"tier1": {"age": 5, "items": [f"{NS}:fuel_tank", f"{NS}:jerry_can"]}}, D)
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([
        {"id": f"{NS}:fuel_tank", "name": "Fuel Tank", "text": "Steel tank holding 8,000 L of one fuel. Fill and empty it with jerry cans or any fluid pipe. Shows up to every vehicle, ship and aircraft pump through the shared fuel capability. Age V."},
        {"id": f"{NS}:jerry_can", "name": "Jerry Can", "text": "Holds 20 L of one fuel. Use on a tank to pour in, sneak-use to draw out, right-click a vehicle to refuel it. Age V."},
    ], f, indent=1)
    f.write("\n")

# ---- tracked chassis (survey p1: Flan's tank-track fold, clean-room) ----
def chassis_icon():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for x in range(1, 15):
        for y in range(4, 9):
            px[x, y] = steel((88, 100, 80)) + (255,)  # olive hull
        for y in (10, 11, 12, 13):
            px[x, y] = steel((34, 36, 40)) + (255,)  # tracks
    for x in range(5, 11):
        for y in range(2, 4):
            px[x, y] = steel((100, 112, 90)) + (255,)  # turret
    px[11, 3] = px[12, 3] = px[13, 3] = (60, 64, 60, 255)  # gun
    for x in range(2, 14, 3):
        px[x, 11] = (110, 116, 126, 255)  # road wheels
    return im


chassis_icon().save(os.path.join(A, "textures/item/tracked_chassis.png"))
wj("models/item/tracked_chassis.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/tracked_chassis"}})
wj("items/tracked_chassis.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/tracked_chassis"}})
lang = json.load(open(os.path.join(A, "lang/en_us.json")))
lang["item.diaithirvehicles.tracked_chassis"] = "Tracked Chassis"
lang["entity.diaithirvehicles.tracked_chassis"] = "Tracked Chassis"
wj("lang/en_us.json", lang)
wj("recipe/tracked_chassis.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"I": "minecraft:iron_block", "T": f"{NS}:fuel_tank", "F": "minecraft:furnace", "P": "minecraft:piston"},
                                   "pattern": ["IFI", "ITI", "PPP"], "result": {"id": f"{NS}:tracked_chassis", "count": 1}}, D)
gate = json.load(open(os.path.join(D, "age_gate.json")))
gate["tier2"] = {"age": 6, "items": [f"{NS}:tracked_chassis"]}
wj("age_gate.json", gate, D)
lib = json.load(open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json")))
lib = [e for e in lib if e["id"] != f"{NS}:tracked_chassis"] + [{"id": f"{NS}:tracked_chassis", "name": "Tracked Chassis", "text": "A 12 tonne light tank hull on two tracks, 36 km/h top speed, 300 kW diesel from its own 600 L tank. Sit in it: W and S drive, A and D steer, and standing still it pivots on the spot. Climbs a full block. Age VI."}]
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump(lib, f, indent=1)
    f.write("\n")
