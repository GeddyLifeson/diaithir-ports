#!/usr/bin/env python3
"""Writes the original textures, models, blockstate, item definitions, lang, recipes, loot, tags, age gate and library entries for
diaithir-vehicles-crayfish. All art is drawn here from scratch (nothing from MrCrayfish's Vehicle Mod): a red pump cabinet with a hazard foot,
a chain-and-hook hitch, a ball-peen repair hammer, and a flatbed icon for the tank trailer."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirvehiclescrayfish"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
MC = os.path.join(ROOT, "data", "minecraft")
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
rnd = random.Random(5)


def wj(rel, obj, base=A):
    p = os.path.join(base, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def jit(c, n=5):
    return tuple(max(0, min(255, v + rnd.randint(-n, n))) for v in c)


def pump_tex():
    im = Image.new("RGBA", (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            c = jit((176, 40, 36))
            if y >= 13:
                c = (232, 178, 26) if ((x + y) // 2) % 2 == 0 else (30, 30, 32)  # hazard foot
            elif 2 <= y <= 6 and 3 <= x <= 12:
                c = jit((40, 52, 44), 3)  # display
                if y == 4 and 4 <= x <= 11:
                    c = (110, 230, 130)
            elif 9 <= y <= 11 and 4 <= x <= 11:
                c = jit((60, 62, 68))  # nozzle cradle
            if x in (0, 15) or y == 0:
                c = (96, 22, 20)
            px[x, y] = (*c, 255)
    return im


def hitch_tex():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for i in range(3, 13):  # chain run
        px[i, 8] = (150, 156, 166, 255) if i % 2 else (90, 96, 106, 255)
        px[i, 9] = (110, 116, 126, 255)
    for (x, y) in ((2, 7), (2, 8), (2, 9), (3, 10), (13, 6), (13, 7), (13, 8), (12, 5)):
        px[x, y] = (210, 214, 224, 255)  # hook and ring
    return im


def hammer_tex():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for i in range(7):  # handle, diagonal
        px[3 + i, 12 - i] = (122, 84, 46, 255)
        px[4 + i, 12 - i] = (96, 64, 34, 255)
    for x in range(8, 14):
        for y in range(2, 6):
            px[x, y] = jit((150, 156, 166), 6) + (255,)
    px[13, 3] = px[13, 4] = (210, 214, 224, 255)
    return im


def trailer_tex():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    for x in range(1, 15):
        for y in range(5, 10):
            px[x, y] = jit((96, 104, 116)) + (255,)
        px[x, 5] = (60, 66, 76, 255)
    for cx in (4, 11):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1), (-1, 0), (0, -1), (-1, 1), (1, -1)):
            px[cx + dx, 12 + dy] = (24, 24, 26, 255)
    for x in range(0, 3):
        px[x, 9] = (176, 184, 196, 255)
    return im


pump_tex().save(os.path.join(A, "textures/block/gas_pump.png"))
hitch_tex().save(os.path.join(A, "textures/item/trailer_hitch.png"))
hammer_tex().save(os.path.join(A, "textures/item/repair_hammer.png"))
trailer_tex().save(os.path.join(A, "textures/item/tank_trailer.png"))

wj("models/block/gas_pump.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/gas_pump"}})
wj("models/item/gas_pump.json", {"parent": f"{NS}:block/gas_pump"})
for n in ("trailer_hitch", "repair_hammer", "tank_trailer"):
    wj(f"models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}"}})
wj("blockstates/gas_pump.json", {"variants": {"": {"model": f"{NS}:block/gas_pump"}}})
for n in ("gas_pump", "trailer_hitch", "repair_hammer", "tank_trailer"):
    wj(f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
wj("lang/en_us.json", {"block.diaithirvehiclescrayfish.gas_pump": "Fuel Pump", "item.diaithirvehiclescrayfish.trailer_hitch": "Trailer Hitch",
                       "item.diaithirvehiclescrayfish.repair_hammer": "Repair Hammer", "item.diaithirvehiclescrayfish.tank_trailer": "Tank Trailer",
                       "entity.diaithirvehiclescrayfish.tank_trailer": "Tank Trailer"})
wj("loot_table/blocks/gas_pump.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:gas_pump"}],
                                                                       "conditions": [{"condition": "minecraft:survives_explosion"}]}]}, D)


def shaped(name, key, pattern, out):
    wj(f"recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": key, "pattern": pattern, "result": {"id": f"{NS}:{out}", "count": 1}}, D)


shaped("gas_pump", {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "G": "minecraft:glass_pane", "H": "minecraft:hopper"}, ["IGI", "IRI", "IHI"], "gas_pump")
shaped("trailer_hitch", {"I": "minecraft:iron_ingot", "C": "minecraft:iron_chain"}, ["I I", "ICI", " C "], "trailer_hitch")
shaped("repair_hammer", {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}, ["III", " SI", " S "], "repair_hammer")
wj("recipe/tank_trailer.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ["diaithirbridges:carrier_tank", "minecraft:minecart", "minecraft:minecart", f"{NS}:trailer_hitch"],
                                "result": {"id": f"{NS}:tank_trailer", "count": 1}}, D)
wj("tags/block/mineable/pickaxe.json", {"values": [f"{NS}:gas_pump"]}, MC)
wj("tags/block/needs_stone_tool.json", {"values": [f"{NS}:gas_pump"]}, MC)
wj("age_gate.json", {"tier1": {"age": 5, "items": [f"{NS}:gas_pump", f"{NS}:trailer_hitch", f"{NS}:repair_hammer", f"{NS}:tank_trailer"]}}, D)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([
        {"id": f"{NS}:gas_pump", "name": "Fuel Pump", "text": "Stands on a Carrier Tank. Use it to take the hose, then use a vehicle or tank trailer to pump 50 L per use. The hose lets go if you walk 10 m away. Age V."},
        {"id": f"{NS}:trailer_hitch", "name": "Trailer Hitch", "text": "Use on a trailer to hook it to yourself or unhook it; use a vehicle with a tow bar to pass the trailer to it. Age V."},
        {"id": f"{NS}:repair_hammer", "name": "Repair Hammer", "text": "Each blow gives a damaged vehicle or trailer 5 health back. Age V."},
        {"id": f"{NS}:tank_trailer", "name": "Tank Trailer", "text": "Two-wheel fuel bowser with a 2,000 L tank. Towed with a hitch, filled by a pump, emptied into cans and vehicles. It takes crash damage and snaps free if the tow bar gets too far. Age V."},
    ], f, indent=1)
    f.write("\n")
