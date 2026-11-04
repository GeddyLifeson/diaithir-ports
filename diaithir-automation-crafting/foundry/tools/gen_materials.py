#!/usr/bin/env python3
"""Writes art, models, blockstates, lang, tags, Age gates and Foundry station recipes for every built stage of the building-materials table
(src/main/resources/data/diaithir_materials/materials.json). Run by gen_assets.py after it writes the shared age_gates.json.
Every `built` row needs a SPEC entry here, and every SPEC entry needs a built row (checked)."""
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
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
SR = os.path.join(D, "diaithir_stations", "station_recipe")
os.makedirs(SR, exist_ok=True)
for sub in ("item", "block"):
    os.makedirs(os.path.join(ROOT, "data", "c", "tags", sub), exist_ok=True)

HB, WB, SM, SF, AS, FB = "station.hearth_bench", "station.wright_bench", "station.smeltery", "station.steam_forge", "machine.assembler", "station.fabricator"
M = "minecraft:"
# id: (title, base colour, station, stage, time_s, steam watts, [(ingredient, count)], result count)
SPEC = {
    "timber_post_beam": ("Post and Beam Timber", (140, 104, 62), HB, "part", 4, 0, [("#minecraft:logs", 1)], 2),
    "timber_sawn": ("Sawn Pegged Timber", (176, 138, 84), HB, "part", 4, 0, [(NS + ":timber_post_beam", 2), (M + "stick", 2)], 3),
    "timber_engineered": ("Engineered Timber", (190, 150, 96), AS, "assembly", 8, 0, [(NS + ":timber_sawn", 4), (M + "slime_ball", 1)], 4),
    "masonry_mud_brick": ("Mud Brick", (120, 92, 66), HB, "process", 4, 0, [(M + "mud", 2), (M + "wheat", 1)], 4),
    "masonry_fired_brick": ("Fired Brick", (168, 84, 60), HB, "process", 8, 0, [(NS + ":masonry_mud_brick", 2)], 2),
    "masonry_cut_stone": ("Cut Stone", (150, 150, 146), HB, "part", 6, 0, [(M + "cobblestone", 4)], 4),
    "masonry_block": ("Masonry Block", (160, 160, 158), SM, "part", 6, 0, [(NS + ":cement_powder", 1), (M + "gravel", 2), (M + "sand", 1)], 4),
    "glass_blown": ("Blown Glass", (180, 215, 225), HB, "process", 6, 0, [(M + "sand", 2), ("#minecraft:coals", 1)], 2),
    "glass_sheet": ("Sheet Glass", (190, 222, 232), WB, "process", 6, 0, [(NS + ":glass_blown", 2), (M + "sand", 1)], 2),
    "glass_plate": ("Plate Glass", (196, 228, 238), SF, "process", 5, 20000, [(NS + ":glass_sheet", 1), (M + "sand", 3)], 2),
    "glass_float": ("Float Glass", (204, 234, 244), AS, "assembly", 6, 0, [(NS + ":glass_plate", 2), (M + "sand", 1)], 4),
    "glass_laminated": ("Laminated Glass", (170, 210, 235), AS, "assembly", 8, 0, [(NS + ":glass_float", 2), (M + "slime_ball", 1)], 2),
    "lining_lime_plaster": ("Lime Plaster", (222, 218, 205), HB, "part", 4, 0, [(M + "calcite", 1), (M + "sand", 2)], 4),
    "lining_gypsum_plaster": ("Gypsum Plaster", (236, 234, 226), SM, "part", 4, 0, [(M + "bone_meal", 2), (M + "sand", 2)], 4),
    "lining_board": ("Plasterboard", (226, 226, 220), SF, "process", 4, 10000, [(NS + ":lining_gypsum_plaster", 2), (M + "paper", 2)], 4),
    "lining_fire_board": ("Fire-Rated Board", (214, 200, 196), AS, "assembly", 6, 0, [(NS + ":lining_board", 2), (NS + ":glass_sheet", 1)], 2),
    "roofing_thatch": ("Thatch", (196, 170, 90), HB, "part", 3, 0, [(M + "wheat", 4)], 2),
    "roofing_slate": ("Slate Roofing", (70, 78, 90), HB, "part", 5, 0, [(M + "cobbled_deepslate", 2)], 4),
    "roofing_tile": ("Clay Roof Tile", (178, 92, 62), WB, "part", 6, 0, [(M + "clay_ball", 4)], 4),
    "roofing_metal_sheet": ("Metal Roofing Sheet", (150, 158, 166), SF, "part", 4, 20000, [(M + "iron_ingot", 2)], 4),
    "roofing_insulated_panel": ("Insulated Roof Panel", (200, 205, 210), AS, "assembly", 6, 0, [(NS + ":roofing_metal_sheet", 2), (NS + ":insulation_foam", 1)], 2),
    "road_flagstone": ("Flagstone Paving", (146, 144, 138), HB, "part", 4, 0, [(M + "stone", 2)], 4),
    "road_cobble": ("Cobble Road", (118, 118, 116), HB, "part", 4, 0, [(M + "cobblestone", 2), (M + "gravel", 1)], 4),
    "road_macadam": ("Macadam Road", (96, 96, 98), SM, "part", 6, 0, [(M + "gravel", 4), ("#minecraft:coals", 1)], 4),
    "road_asphalt": ("Asphalt", (44, 44, 48), SF, "process", 5, 30000, [(M + "gravel", 2), ("#minecraft:coals", 2), (M + "sand", 1)], 4),
    "road_concrete": ("Concrete Road Slab", (130, 132, 136), AS, "assembly", 6, 0, [(NS + ":concrete_portland", 2), (M + "gravel", 1)], 4),
    "insulation_turf": ("Turf Insulation", (86, 120, 62), HB, "part", 4, 0, [(M + "wheat", 2), (M + "dirt", 1)], 2),
    "insulation_rock_wool": ("Rock Wool", (150, 150, 128), SM, "process", 6, 0, [(M + "basalt", 2)], 4),
    "insulation_glass_wool": ("Glass Wool", (236, 220, 120), SF, "process", 5, 15000, [(NS + ":glass_blown", 2), (M + "string", 2)], 4),
    "insulation_foam": ("Foam Insulation Board", (236, 228, 160), AS, "assembly", 6, 0, [(M + "slime_ball", 2), (M + "blaze_powder", 1)], 4),
    "insulation_aerogel": ("Aerogel Panel", (200, 222, 240), FB, "assembly", 10, 0, [(NS + ":insulation_glass_wool", 2), (M + "amethyst_shard", 1)], 2),
    "steel_wrought_bar": ("Wrought Iron Bar Stock", (92, 88, 84), WB, "part", 6, 0, [(M + "iron_ingot", 2), ("#minecraft:coals", 1)], 2),
    "steel_rolled_iron": ("Rolled Iron Section", (86, 90, 96), SM, "part", 8, 0, [(M + "iron_ingot", 3), ("#minecraft:coals", 1)], 4),
    "steel_plate_angle": ("Steel Plate and Angle", (108, 114, 122), SF, "part", 5, 40000, [("#c:ingots/steel", 2)], 4),
    "steel_i_beam": ("Steel I-Beam", (96, 102, 112), AS, "assembly", 8, 0, [(NS + ":steel_plate_angle", 3)], 2),
    "steel_light_gauge": ("Light-Gauge Steel Framing", (170, 180, 190), AS, "assembly", 6, 0, [(NS + ":steel_plate_angle", 1)], 6),
    "steel_alloy_lattice": ("Alloy Lattice Frame", (70, 130, 150), FB, "assembly", 12, 0, [("#c:plates/nanoalloy", 2), (NS + ":steel_i_beam", 1)], 2),
    # concrete line, built in the first pass
    "concrete_pozzolan": ("Pozzolan Concrete", (150, 138, 120), HB, "process", 6, 0, [(M + "sand", 2), (M + "calcite", 1), (M + "tuff", 1)], 2),
    "cement_powder": ("Cement Powder", None, SM, "part", 8, 0, [(M + "clay_ball", 2), (M + "calcite", 1)], 2),
    "concrete_portland": ("Portland Concrete", (140, 142, 145), SF, "process", 5, 20000, [(NS + ":cement_powder", 2), (M + "gravel", 2), (M + "sand", 2)], 4),
    "concrete_reinforced": ("Reinforced Concrete", (132, 134, 138), AS, "assembly", 6, 0, [(NS + ":concrete_portland", 4), (M + "iron_bars", 2)], 4),
    "concrete_precast": ("Precast Concrete Slab", (156, 158, 162), AS, "assembly", 8, 0, [(NS + ":concrete_reinforced", 4), ("#c:ingots/steel", 1)], 4),
}
built = [r for r in rows.values() if r["built"]]
assert {r["id"] for r in built} == set(SPEC), ({r["id"] for r in built} ^ set(SPEC))
TAGS = {"stone/cut": ["masonry_cut_stone"], "timber/beam": ["timber_sawn"], "beams/steel": ["steel_i_beam"], "glass_blocks": [i for i in SPEC if i.startswith("glass_")],
        "wallboard": [i for i in SPEC if i.startswith("lining_")], "roofing": [i for i in SPEC if i.startswith("roofing_")],
        "road_surface": [i for i in SPEC if i.startswith("road_")], "insulation": [i for i in SPEC if i.startswith("insulation_")],
        "concrete": [i for i in SPEC if i.startswith("concrete_")], "cement": ["cement_powder"]}


def wr(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def texture(name, base, cls):
    grain = {"timber": 12, "masonry": 14, "concrete": 8, "steel": 6, "glass": 3, "lining": 4, "roofing": 12, "road": 14, "insulation": 14}[cls]
    im = Image.new("RGBA", (S, S))
    px = im.load()
    for y in range(S):
        for x in range(S):
            n = rnd.randint(-grain, grain)
            px[x, y] = tuple(max(0, min(255, c + n)) for c in base) + (255,)
    d = ImageDraw.Draw(im)
    dark = tuple(max(0, c - 55) for c in base) + (255,)
    light = tuple(min(255, c + 40) for c in base) + (255,)
    if cls == "timber":
        for y in range(4, S, 8):
            d.line([0, y + rnd.randint(-1, 1), S, y + rnd.randint(-1, 1)], fill=dark)
        if name == "timber_post_beam":
            d.ellipse([26, 26, 38, 38], outline=dark, width=2)
    elif cls == "masonry" or name in ("road_flagstone", "road_cobble"):
        for y in range(0, S, 16):
            d.line([0, y, S, y], fill=dark, width=2)
            for x in range(8 if (y // 16) % 2 else 0, S, 16):
                d.line([x, y, x, y + 16], fill=dark, width=2)
    elif cls == "steel":
        d.rectangle([0, 0, S - 1, S - 1], outline=dark, width=3)
        for cx, cy in ((7, 7), (56, 7), (7, 56), (56, 56)):
            d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=light, outline=dark)
        if name == "steel_i_beam":
            d.rectangle([6, 6, 57, 17], fill=light, outline=dark)
            d.rectangle([6, 46, 57, 57], fill=light, outline=dark)
        elif name in ("steel_light_gauge", "steel_alloy_lattice"):
            for k in range(8, S, 14):
                d.line([k, 0, k, S], fill=dark, width=2)
                d.line([0, k, S, k], fill=dark)
            if name == "steel_alloy_lattice":
                d.line([0, 0, S, S], fill=light, width=2)
                d.line([0, S, S, 0], fill=light, width=2)
    elif cls == "glass":
        im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        d.rectangle([0, 0, S - 1, S - 1], outline=base + (255,), width=3)
        d.line([10, 54, 54, 10], fill=base + (140,), width=3)
        d.line([22, 56, 56, 22], fill=base + (140,), width=2)
    elif cls == "roofing":
        if name == "roofing_thatch":
            for _ in range(160):
                x, y = rnd.randint(0, S - 1), rnd.randint(0, S - 8)
                d.line([x, y, x + rnd.randint(-2, 2), y + 8], fill=dark if rnd.random() < .5 else light)
        else:
            for y in range(0, S, 12):
                for x in range(6 if (y // 12) % 2 else 0, S + 12, 12):
                    d.arc([x - 6, y - 6, x + 6, y + 6], 0, 180, fill=dark, width=2)
    elif cls == "road":
        if name == "road_asphalt":
            for _ in range(120):
                d.point((rnd.randint(0, S - 1), rnd.randint(0, S - 1)), fill=light)
        elif name == "road_concrete":
            d.line([32, 0, 32, S], fill=dark, width=2)
        elif name == "road_macadam":
            for _ in range(80):
                x, y = rnd.randint(2, 61), rnd.randint(2, 61)
                d.rectangle([x, y, x + 2, y + 2], fill=light)
    elif cls == "insulation":
        for _ in range(220):
            x, y = rnd.randint(0, S - 1), rnd.randint(0, S - 1)
            d.line([x, y, x + rnd.randint(-5, 5), y + rnd.randint(-5, 5)], fill=light if rnd.random() < .5 else dark)
    elif cls == "lining":
        d.rectangle([0, 0, S - 1, S - 1], outline=dark, width=2)
        if name in ("lining_board", "lining_fire_board"):
            d.line([32, 0, 32, S], fill=dark)
    elif cls == "concrete":
        if name == "concrete_reinforced":
            for k in range(8, S, 16):
                d.line([k, 0, k, S], fill=(110, 70, 50, 255), width=2)
                d.line([0, k, S, k], fill=(110, 70, 50, 255), width=2)
        if name == "concrete_precast":
            d.rectangle([0, 0, S - 1, S - 1], outline=dark, width=2)
            for cx in (12, 51):
                d.ellipse([cx - 3, 10, cx + 3, 16], fill=dark)
        if name == "concrete_pozzolan":
            for _ in range(40):
                x, y = rnd.randint(2, 61), rnd.randint(2, 61)
                d.rectangle([x, y, x + 1, y + 1], fill=(80, 70, 66, 255))
    return im


lang, ages = {}, {}
for name, (title, base, *_rest) in SPEC.items():
    r = rows[name]
    if name == "cement_powder":
        im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        d.polygon([(8, 52), (32, 14), (56, 52)], fill=(176, 176, 172, 255), outline=(60, 60, 60, 255))
        for _ in range(60):
            d.point((rnd.randint(14, 50), rnd.randint(30, 50)), fill=(150, 150, 146, 255))
        im.save(os.path.join(A, "textures", "item", name + ".png"))
        wr(os.path.join(A, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": NS + ":item/" + name}})
        wr(os.path.join(A, "items", name + ".json"), {"model": {"type": "minecraft:model", "model": NS + ":item/" + name}})
        lang["item.%s.%s" % (NS, name)] = title
    else:
        texture(name, base, r["class"]).save(os.path.join(A, "textures", "block", name + ".png"))
        model = {"parent": "minecraft:block/cube_all", "textures": {"all": NS + ":block/" + name}}
        if r["class"] == "glass":
            model["render_type"] = "minecraft:cutout"
        wr(os.path.join(A, "models", "block", name + ".json"), model)
        wr(os.path.join(A, "blockstates", name + ".json"), {"variants": {"": {"model": NS + ":block/" + name}}})
        wr(os.path.join(A, "items", name + ".json"), {"model": {"type": "minecraft:model", "model": NS + ":block/" + name}})
        lang["block.%s.%s" % (NS, name)] = title
    ages[NS + ":" + name] = r["age"]
wr(os.path.join(A, "lang", "en_us.json"), lang)

for tag, ids in TAGS.items():
    vals = [NS + ":" + i for i in ids]
    wr(os.path.join(ROOT, "data", "c", "tags", "item", tag + ".json"), {"values": vals})
    if tag != "cement":
        wr(os.path.join(ROOT, "data", "c", "tags", "block", tag + ".json"), {"values": vals})
wr(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable", "pickaxe.json"), {"values": [NS + ":" + i for i in SPEC if i != "cement_powder" and rows[i]["class"] != "timber"]})
wr(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable", "axe.json"), {"values": [NS + ":" + i for i in SPEC if rows[i]["class"] == "timber"]})


def ing(x, n):
    return {"tag": x[1:], "count": n} if x.startswith("#") else {"item": x, "count": n}


for name, (title, base, station, stage, time_s, w, ings, count) in SPEC.items():
    body = {"station": station, "stage": stage, "time_s": time_s, "ingredients": [ing(x, n) for x, n in ings], "result": {"id": NS + ":" + name, "count": count}}
    if w:
        body["power"], body["w"] = "steam", float(w)
    wr(os.path.join(SR, name + ".json"), body)

by_age = {}
for k, a in ages.items():
    by_age.setdefault(a, []).append(k)
wr(os.path.join(D, "age_gate.json"), {"tier%d" % AGE_NUM[a]: {"age": AGE_NUM[a], "items": sorted(v)} for a, v in sorted(by_age.items(), key=lambda kv: AGE_NUM[kv[0]])})
gp = os.path.join(FOUNDRY, "diaithir-data", "age_gates.json")
allg = json.load(open(gp))
allg["items"].update(ages)
wr(gp, allg)
# Codex: one entry per stage, text built only from the table's numbers (no invented lore); same shape as the worldgen codex
CLS = {"timber": "Timber", "masonry": "Masonry", "concrete": "Concrete", "steel": "Iron and steel", "glass": "Glass", "lining": "Wall lining",
       "roofing": "Roofing", "road": "Road surface", "insulation": "Insulation"}
codex = []
for name, (title, _b, station, stage, time_s, w, ings, count) in SPEC.items():
    r = rows[name]
    bits = ["density %g kg/m3" % r["density_kg_m3"], "compressive strength %g MPa" % r["compressive_mpa"], "tensile strength %g MPa" % r["tensile_mpa"],
            "heat conductivity %g W/(m K)" % r["conductivity_w_mk"],
            ("fire resistance %g min" % r["fire_min"]) if r["fire_min"] else "no fire resistance"]
    if r["flammability"]:
        bits.append("burns")
    if r["road_speed"]:
        bits.append("walking speed +%d%%" % round(r["road_speed"] * 100))
    codex.append({"id": NS + ":" + name, "kind": "material", "name": title, "class": CLS[r["class"]], "age": r["age"],
                  "text": CLS[r["class"]] + ", Age " + r["age"] + ": " + ", ".join(bits) + ".", "station": station,
                  "quest": NS + ":build/" + name, "connects": ["recipes:" + NS + ":" + name] + (["tags:c:" + t for t, ids in TAGS.items() if name in ids])})
wr(os.path.join(D, "codex", "materials.json"), codex)
print("materials ok", len(built), "built stages")
