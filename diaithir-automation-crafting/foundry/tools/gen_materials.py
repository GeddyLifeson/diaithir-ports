#!/usr/bin/env python3
"""Writes art, models, blockstates, lang, tags, Age gates and Foundry station recipes for the built stages of the building-materials table
(src/main/resources/data/diaithir_materials/materials.json). Run by gen_assets.py after it writes the shared age_gates.json."""
import json, os, random
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
FOUNDRY = os.path.normpath(os.path.join(HERE, ".."))
ROOT = os.path.join(FOUNDRY, "src", "main", "resources")
NS = "diaithir_materials"
A, D = os.path.join(ROOT, "assets", NS), os.path.join(ROOT, "data", NS)
AGE_NUM = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "VIII": 8, "RODOS": 9}
S = 64
rnd = random.Random(11)
rows = {r["id"]: r for r in json.load(open(os.path.join(D, "materials.json")))["rows"]}
built = [r for r in rows.values() if r["built"]]
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "diaithir_stations", "station_recipe"), exist_ok=True)
os.makedirs(os.path.join(ROOT, "data", "c", "tags", "item"), exist_ok=True)
os.makedirs(os.path.join(ROOT, "data", "c", "tags", "block"), exist_ok=True)


def wr(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def slab(base, grain):
    im = Image.new("RGBA", (S, S))
    px = im.load()
    for y in range(S):
        for x in range(S):
            n = rnd.randint(-grain, grain)
            px[x, y] = tuple(max(0, min(255, c + n)) for c in base) + (255,)
    return im


STYLE = {"concrete_pozzolan": ((150, 138, 120), 14), "concrete_portland": ((140, 142, 145), 8),
         "concrete_reinforced": ((132, 134, 138), 6), "concrete_precast": ((156, 158, 162), 4)}
TITLE = {"concrete_pozzolan": "Pozzolan Concrete", "concrete_portland": "Portland Concrete", "concrete_reinforced": "Reinforced Concrete",
         "concrete_precast": "Precast Concrete Slab", "cement_powder": "Cement Powder"}
lang, ages = {}, {}
for r in built:
    name = r["id"]
    if name == "cement_powder":
        im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        d.polygon([(8, 52), (32, 14), (56, 52)], fill=(176, 176, 172, 255), outline=(60, 60, 60, 255))
        for _ in range(60):
            x, y = rnd.randint(14, 50), rnd.randint(30, 50)
            d.point((x, y), fill=(150, 150, 146, 255))
        im.save(os.path.join(A, "textures", "item", name + ".png"))
        wr(os.path.join(A, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": NS + ":item/" + name}})
        wr(os.path.join(A, "items", name + ".json"), {"model": {"type": "minecraft:model", "model": NS + ":item/" + name}})
        lang["item.%s.%s" % (NS, name)] = TITLE[name]
    else:
        base, grain = STYLE[name]
        im = slab(base, grain)
        d = ImageDraw.Draw(im)
        if name == "concrete_reinforced":  # rebar grid showing at the face
            for k in range(8, S, 16):
                d.line([k, 0, k, S], fill=(110, 70, 50, 255), width=2)
                d.line([0, k, S, k], fill=(110, 70, 50, 255), width=2)
        if name == "concrete_precast":  # formwork panel joints and lifting anchors
            d.rectangle([0, 0, S - 1, S - 1], outline=(96, 98, 102, 255), width=2)
            for cx in (12, 51):
                d.ellipse([cx - 3, 10, cx + 3, 16], fill=(80, 82, 86, 255))
        if name == "concrete_pozzolan":  # volcanic ash flecks
            for _ in range(40):
                x, y = rnd.randint(2, 61), rnd.randint(2, 61)
                d.rectangle([x, y, x + 1, y + 1], fill=(80, 70, 66, 255))
        im.save(os.path.join(A, "textures", "block", name + ".png"))
        wr(os.path.join(A, "models", "block", name + ".json"), {"parent": "minecraft:block/cube_all", "textures": {"all": NS + ":block/" + name}})
        wr(os.path.join(A, "blockstates", name + ".json"), {"variants": {"": {"model": NS + ":block/" + name}}})
        wr(os.path.join(A, "items", name + ".json"), {"model": {"type": "minecraft:model", "model": NS + ":block/" + name}})
        lang["block.%s.%s" % (NS, name)] = TITLE[name]
    ages[NS + ":" + name] = r["age"]
wr(os.path.join(A, "lang", "en_us.json"), lang)

blocks = [NS + ":" + r["id"] for r in built if r["id"] != "cement_powder"]
wr(os.path.join(ROOT, "data", "c", "tags", "item", "concrete.json"), {"values": blocks})
wr(os.path.join(ROOT, "data", "c", "tags", "block", "concrete.json"), {"values": blocks})
wr(os.path.join(ROOT, "data", "c", "tags", "item", "cement.json"), {"values": [NS + ":cement_powder"]})
wr(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable", "pickaxe.json"), {"values": blocks})


def ing(x, n=1):
    return {"tag": x[1:], "count": n} if x.startswith("#") else {"item": x, "count": n}


def res(n, c):
    return {"id": NS + ":" + n, "count": c}


# Concrete chain across the Ages: each stage is made at the station of its time, from the stage before it
RECIPES = {
    "concrete_pozzolan": {"station": "station.hearth_bench", "stage": "process", "time_s": 6,
                          "ingredients": [ing("minecraft:sand", 2), ing("minecraft:calcite"), ing("minecraft:tuff")], "result": res("concrete_pozzolan", 2)},
    "cement_powder": {"station": "station.smeltery", "stage": "part", "time_s": 8,
                      "ingredients": [ing("minecraft:clay_ball", 2), ing("minecraft:calcite")], "result": res("cement_powder", 2)},
    "concrete_portland": {"station": "station.steam_forge", "stage": "process", "time_s": 5, "power": "steam", "w": 20000.0,
                          "ingredients": [ing(NS + ":cement_powder", 2), ing("minecraft:gravel", 2), ing("minecraft:sand", 2)], "result": res("concrete_portland", 4)},
    "concrete_reinforced": {"station": "machine.assembler", "stage": "assembly", "time_s": 6,
                            "ingredients": [ing(NS + ":concrete_portland", 4), ing("minecraft:iron_bars", 2)], "result": res("concrete_reinforced", 4)},
    "concrete_precast": {"station": "machine.assembler", "stage": "assembly", "time_s": 8,
                         "ingredients": [ing(NS + ":concrete_reinforced", 4), ing("#c:ingots/steel")], "result": res("concrete_precast", 4)},
}
for n, body in RECIPES.items():
    wr(os.path.join(D, "diaithir_stations", "station_recipe", n + ".json"), body)

by_age = {}
for k, a in ages.items():
    by_age.setdefault(a, []).append(k)
wr(os.path.join(D, "age_gate.json"), {"tier%d" % AGE_NUM[a]: {"age": AGE_NUM[a], "items": sorted(v)} for a, v in sorted(by_age.items(), key=lambda kv: AGE_NUM[kv[0]])})
gp = os.path.join(FOUNDRY, "diaithir-data", "age_gates.json")
allg = json.load(open(gp))
allg["items"].update(ages)
wr(gp, allg)
print("materials ok", len(built), "built stages")
