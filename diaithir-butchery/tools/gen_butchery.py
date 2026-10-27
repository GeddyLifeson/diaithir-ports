"""Writes the butchery data, lang, item models, textures and recipes. Run from diaithir-butchery: python3 tools/gen_butchery.py"""
import json, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import names
from PIL import Image

MOD = "diaithirbutchery"
RES = "src/main/resources"
A = f"{RES}/assets/{MOD}"
D = f"{RES}/data/{MOD}"
CUTS = ["fish_fillet", "bird_breast", "drumstick", "wing", "haunch", "shoulder", "loin", "ribs", "flank", "tenderloin", "neck", "tail", "hock", "liver", "heart", "kidney", "tripe", "tongue", "brain", "scraps"]
PRODUCTS = ["mince", "sausage", "cured_meat", "jerky", "lard", "canned_meat", "grown_cut"]
STATIONS = {"drying_rack": 2, "salting_barrel": 2, "meat_grinder": 3, "sausage_stuffer": 3, "rendering_cauldron": 3, "smokehouse": 4, "steam_cannery": 5, "cold_room": 6, "vacuum_packer": 7, "vat_grower": 8, "stasis_case": 9}
KNIVES = {"flint_knife": [1, 0], "copper_knife": [2, 10], "bronze_knife": [3, 15], "butcher_knife": [3, 20], "steel_knife": [4, 30], "tempered_knife": [5, 40], "ceramic_knife": [6, 45],
          "diamond_edge_knife": [7, 60], "crystal_thread_knife": [8, 75], "shard_knife": [9, 100]}  # [Age (9 = Rodos), percent added to every yield]

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=1)

# body classes: stage -> item -> [min, max]
def cls(skin, gut, cut):
    return {"skin": skin, "gut": gut, "cut": cut}

BIG_GUT = {"blood_flask": [1, 3], "liver": [1, 1], "heart": [1, 1], "kidney": [2, 2], "tripe": [1, 1], "tallow": [1, 3]}
CLASSES = {
    "land_tiny": cls({"raw_hide": [1, 1]}, {"liver": [1, 1], "heart": [1, 1]}, {"haunch": [2, 2], "loin": [1, 1], "scraps": [1, 2], "minecraft:bone": [1, 2]}),
    "land_small": cls({"raw_hide": [1, 1], "sinew": [1, 2]}, {"blood_flask": [1, 1], "liver": [1, 1], "heart": [1, 1], "kidney": [1, 2], "tallow": [1, 1]},
                      {"haunch": [2, 2], "shoulder": [2, 2], "loin": [1, 2], "ribs": [1, 2], "scraps": [1, 3], "minecraft:bone": [2, 3]}),
    "land_medium": cls({"raw_hide": [1, 2], "sinew": [1, 3]}, {**BIG_GUT, "tongue": [1, 1]},
                       {"haunch": [2, 2], "shoulder": [2, 2], "loin": [2, 2], "ribs": [2, 3], "flank": [1, 2], "tenderloin": [1, 1], "neck": [1, 1], "hock": [2, 2],
                        "scraps": [2, 4], "minecraft:bone": [3, 5], "marrow_bone": [1, 2]}),
    "land_large": cls({"raw_hide": [2, 3], "sinew": [2, 4]}, {**BIG_GUT, "blood_flask": [2, 4], "tongue": [1, 1], "brain": [1, 1], "tallow": [2, 5]},
                      {"haunch": [2, 4], "shoulder": [2, 4], "loin": [3, 4], "ribs": [3, 5], "flank": [2, 3], "tenderloin": [2, 2], "neck": [2, 2], "hock": [4, 4], "tail": [1, 1],
                       "scraps": [4, 6], "minecraft:bone": [5, 8], "marrow_bone": [2, 3]}),
    "land_huge": cls({"raw_hide": [4, 6], "sinew": [4, 8]}, {**BIG_GUT, "blood_flask": [4, 8], "liver": [2, 3], "heart": [1, 2], "kidney": [2, 4], "tripe": [2, 3], "tongue": [1, 2], "brain": [1, 1], "tallow": [6, 12]},
                     {"haunch": [4, 6], "shoulder": [4, 6], "loin": [6, 8], "ribs": [6, 10], "flank": [4, 6], "tenderloin": [3, 4], "neck": [3, 4], "hock": [6, 8], "tail": [2, 3],
                      "scraps": [8, 12], "minecraft:bone": [10, 16], "marrow_bone": [4, 6]}),
    "predator": cls({"raw_hide": [1, 2], "sinew": [1, 3]}, {"blood_flask": [1, 2], "liver": [1, 1], "heart": [1, 1], "kidney": [1, 2], "tallow": [1, 2]},
                    {"haunch": [2, 2], "shoulder": [2, 2], "loin": [1, 2], "ribs": [1, 2], "scraps": [2, 3], "minecraft:bone": [3, 5]}),
    "bird_small": cls({"minecraft:feather": [1, 3]}, {"liver": [1, 1], "heart": [1, 1]}, {"bird_breast": [1, 2], "drumstick": [2, 2], "wing": [2, 2], "scraps": [1, 1], "minecraft:bone": [1, 2]}),
    "bird_large": cls({"minecraft:feather": [3, 6]}, {"blood_flask": [1, 1], "liver": [1, 1], "heart": [1, 1], "tallow": [1, 1]},
                      {"bird_breast": [2, 2], "drumstick": [2, 2], "wing": [2, 2], "neck": [1, 1], "scraps": [2, 3], "minecraft:bone": [3, 5]}),
    "fish_tiny": cls({}, {"liver": [1, 1]}, {"fish_fillet": [1, 2], "minecraft:bone": [1, 1]}),
    "fish_small": cls({}, {"liver": [1, 1]}, {"fish_fillet": [1, 2], "minecraft:bone": [1, 1]}),
    "fish_medium": cls({"raw_hide": [1, 1]}, {"blood_flask": [1, 1], "liver": [1, 1], "tallow": [1, 1]}, {"fish_fillet": [2, 4], "scraps": [1, 2], "minecraft:bone": [2, 3]}),
    "fish_large": cls({"raw_hide": [1, 2]}, {"blood_flask": [1, 2], "liver": [1, 2], "heart": [1, 1], "tallow": [2, 4]}, {"fish_fillet": [4, 8], "scraps": [2, 4], "minecraft:bone": [3, 6]}),
    "fish_huge": cls({"raw_hide": [3, 5]}, {"blood_flask": [3, 6], "liver": [2, 3], "heart": [1, 1], "tallow": [6, 12]}, {"fish_fillet": [10, 16], "scraps": [4, 8], "minecraft:bone": [8, 14]}),
    "reptile": cls({"raw_hide": [1, 2]}, {"blood_flask": [1, 1], "liver": [1, 1], "heart": [1, 1]}, {"loin": [1, 2], "tail": [1, 2], "scraps": [1, 2], "minecraft:bone": [2, 4]}),
    "monster_hide": cls({"raw_hide": [2, 4], "sinew": [2, 4]}, {"blood_flask": [1, 3], "tallow": [1, 3]}, {"scraps": [2, 4], "minecraft:bone": [3, 6]}),
}

ENTITIES = {
    "minecraft:cow": "land_large", "minecraft:mooshroom": "land_large", "minecraft:pig": "land_medium", "minecraft:sheep": "land_medium",
    "minecraft:goat": "land_small", "minecraft:horse": "land_large", "minecraft:donkey": "land_large", "minecraft:mule": "land_large",
    "minecraft:llama": "land_large", "minecraft:trader_llama": "land_large", "minecraft:camel": "land_huge", "minecraft:camel_husk": "land_huge",
    "minecraft:chicken": "bird_small", "minecraft:rabbit": "land_tiny", "minecraft:cod": "fish_small", "minecraft:salmon": "fish_medium",
    "minecraft:tropical_fish": "fish_tiny", "minecraft:squid": "fish_medium", "minecraft:glow_squid": "fish_medium", "minecraft:turtle": "reptile",
    "minecraft:fox": "predator", "minecraft:wolf": "none", "minecraft:cat": "none", "minecraft:ocelot": "predator", "minecraft:polar_bear": "predator",
    "minecraft:hoglin": "land_large", "minecraft:strider": "land_large", "minecraft:frog": "land_tiny", "minecraft:armadillo": "land_small",
    "minecraft:sniffer": "land_huge", "minecraft:panda": "none", "minecraft:parrot": "none", "minecraft:bee": "none", "minecraft:axolotl": "none",
    "minecraft:happy_ghast": "none", "minecraft:nautilus": "fish_medium", "minecraft:pufferfish": "none", "minecraft:ravager": "monster_hide",
    "minecraft:spider": "monster_hide", "minecraft:cave_spider": "none", "minecraft:zoglin": "none", "minecraft:iron_golem": "none",
    "minecraft:dolphin": "fish_large", "minecraft:villager": "none", "minecraft:wandering_trader": "none",
}



TOOLS = {f"{MOD}:{k}": {"bonus": v[1]} for k, v in KNIVES.items()}
TOOLS.update({"farmersdelight:flint_knife": {"bonus": 0}, "farmersdelight:iron_knife": {"bonus": 15}, "farmersdelight:golden_knife": {"bonus": 10}, "farmersdelight:diamond_knife": {"bonus": 40}, "farmersdelight:netherite_knife": {"bonus": 50}})
RAW = "#diaithirbutchery:raw_cuts"
COOKED = "#diaithirbutchery:cooked_cuts"
ANY = "#diaithirbutchery:meats"
def proc(i, o, ticks, count=1, shelf=0):
    return {"in": i, "out": o, "count": count, "ticks": ticks, "shelf": shelf}
M = f"{MOD}:"
STATION_DATA = {
    "drying_rack": {"age": 2, "process": [proc(M + c, M + "jerky", 2400, 1, 400) for c in ("flank", "ribs", "scraps", "neck", "tail")]},
    "salting_barrel": {"age": 2, "process": [proc(M + c, M + "cured_meat", 3600, 1, 600) for c in ("haunch", "shoulder", "loin", "hock", "neck")]},
    "meat_grinder": {"age": 3, "process": [proc(M + c, M + "mince", 60, 2) for c in ("scraps", "flank", "neck", "tail", "hock")]},
    "sausage_stuffer": {"age": 3, "process": [proc(M + "mince", M + "sausage", 80, 3, 200)]},
    "rendering_cauldron": {"age": 3, "process": [proc(M + "tallow", M + "lard", 200, 2, 500)]},
    "smokehouse": {"age": 4, "process": [proc(M + c, M + "cured_meat", 2400, 1, 800) for c in ("haunch", "shoulder", "loin", "ribs", "flank", "hock")]},
    "steam_cannery": {"age": 5, "process": [proc(COOKED, M + "canned_meat", 100, 1, 2400)]},
    "cold_room": {"age": 6, "process": [proc(RAW, "same", 40, 1, 400), proc(M + "mince", "same", 40, 1, 400), proc(M + "sausage", "same", 40, 1, 400)]},
    "vacuum_packer": {"age": 7, "process": [proc(RAW, "same", 40, 1, 1200), proc(COOKED, "same", 40, 1, 1200), proc(ANY, "same", 40, 1, 1200)]},
    "vat_grower": {"age": 8, "process": [proc(M + "marrow_bone", M + "grown_cut", 1200, 3, 300)]},
    "stasis_case": {"age": 9, "process": [proc(ANY, "same", 20, 1, 100000)]},
}
w(f"{D}/butchery/butchery.json", {"classes": CLASSES, "entities": ENTITIES, "tools": TOOLS, "stations": STATION_DATA})
w(f"{D}/tags/block/cold_containers.json", {"values": [M + "cold_room", M + "stasis_case"]})
w(f"{D}/tags/item/raw_cuts.json", {"values": [M + c for c in CUTS]})
w(f"{D}/tags/item/cooked_cuts.json", {"values": [M + "cooked_" + c for c in CUTS]})
w(f"{D}/tags/item/meats.json", {"values": ["#" + MOD + ":raw_cuts", "#" + MOD + ":cooked_cuts"] + [M + p for p in PRODUCTS if p != "lard"]})
# tags let modded creatures be assigned without editing this file, e.g. a pack adds #diaithirbutchery:land_huge entries; kept for datapacks
RAW_COLORS = {  # base colour per item for the placeholder textures; real art comes from the texture pack
    "raw_hide": (150, 110, 80), "sinew": (225, 215, 195), "tallow": (240, 232, 205), "blood_flask": (140, 20, 25), "marrow_bone": (235, 225, 200),
    "scraps": (170, 60, 60), "liver": (110, 40, 45), "heart": (150, 40, 55), "kidney": (130, 55, 55), "tripe": (215, 195, 170), "tongue": (190, 90, 100), "brain": (215, 150, 160),
    "fish_fillet": (235, 150, 130), "bird_breast": (230, 170, 150), "drumstick": (225, 160, 120), "wing": (225, 185, 150), "haunch": (180, 55, 55), "shoulder": (175, 65, 60),
    "loin": (190, 70, 70), "ribs": (185, 100, 85), "flank": (170, 70, 70), "tenderloin": (200, 85, 90), "neck": (165, 70, 65), "tail": (160, 90, 80), "hock": (170, 80, 70),
    "carcass": (170, 90, 80), "mince": (190, 80, 85), "sausage": (170, 90, 70), "cured_meat": (150, 60, 60), "jerky": (110, 60, 40), "lard": (245, 240, 225), "canned_meat": (170, 175, 180), "grown_cut": (225, 140, 150), "flint_knife": (110, 110, 120), "copper_knife": (190, 115, 70), "bronze_knife": (170, 130, 60), "steel_knife": (200, 205, 215), "tempered_knife": (150, 160, 190), "ceramic_knife": (235, 235, 225), "diamond_edge_knife": (120, 220, 230), "crystal_thread_knife": (170, 140, 230), "shard_knife": (230, 120, 200), "butcher_knife": (190, 190, 195), "butcher_cleaver": (170, 175, 180),
}
ITEMS = ["carcass", "butcher_knife", "butcher_cleaver"] + [k for k in KNIVES if k != "butcher_knife"] + ["raw_hide", "sinew", "tallow", "blood_flask", "marrow_bone"] + CUTS + ["cooked_" + c for c in CUTS] + PRODUCTS

def texture(name):
    base = name.removeprefix("cooked_")
    r, g, b = RAW_COLORS[base]
    if name.startswith("cooked_"):
        r, g, b = int(r * 0.6 + 40), int(g * 0.55 + 20), int(b * 0.5 + 10)
    rng = random.Random(name)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 6.5, (y - 7.5) / 5.0
            if dx * dx + dy * dy <= 1.0:
                n = rng.randint(-14, 14) + (10 if y < 6 else -10 if y > 10 else 0)
                img.putpixel((x, y), (max(0, min(255, r + n)), max(0, min(255, g + n)), max(0, min(255, b + n)), 255))
    os.makedirs(f"{A}/textures/item", exist_ok=True)
    img.save(f"{A}/textures/item/{name}.png")

lang = {"item.diaithirbutchery.carcass_of": names.CARCASS_OF, "block.diaithirbutchery.meat_hook": names.item("meat_hook"), "item.diaithirbutchery.meat_hook": names.item("meat_hook"),
        "message.diaithirbutchery.started": "Work has started; finish it with a knife.", "message.diaithirbutchery.unknown": "This carcass cannot be cut."}
for sp, nm in names.SPECIES.items():
    lang[f"diaithirbutchery.species.minecraft.{sp}"] = nm
for n in ITEMS:
    texture(n)
    w(f"{A}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{n}"}})
    w(f"{A}/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{n}"}})
    lang[f"item.{MOD}.{n}"] = names.item(n)
w(f"{A}/lang/en_us.json", lang)

# meat hook: a thin vertical hook, block + item model
w(f"{A}/blockstates/meat_hook.json", {"variants": {"": {"model": f"{MOD}:block/meat_hook"}}})
w(f"{A}/models/block/meat_hook.json", {"textures": {"0": "minecraft:block/iron_block", "particle": "minecraft:block/iron_block"}, "elements": [
    {"from": [7, 2, 7], "to": [9, 16, 9], "faces": {d: {"uv": [0, 0, 2, 14], "texture": "#0"} for d in ["north", "south", "east", "west"]}},
    {"from": [7, 0, 7], "to": [9, 4, 9], "faces": {d: {"uv": [0, 0, 2, 4], "texture": "#0"} for d in ["north", "south", "east", "west", "down"]}},
    {"from": [9, 0, 7], "to": [12, 2, 9], "faces": {d: {"uv": [0, 0, 3, 2], "texture": "#0"} for d in ["north", "south", "east", "down", "up"]}}]})
w(f"{A}/items/meat_hook.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/meat_hook"}})
w(f"{D}/loot_table/blocks/meat_hook.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{MOD}:meat_hook"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

for m in ("bronze", "steel"):  # empty here, filled by whatever mod supplies the metal in the pack; the common tag is optional
    w(f"{D}/tags/item/{m}_ingots.json", {"values": [{"id": f"#c:ingots/{m}", "required": False}]})
# recipes: cook every raw cut in furnace, smoker and campfire; knife, cleaver, hook
for c in CUTS:
    for kind, secs, folder in (("smelting", 200, "furnace"), ("smoking", 100, "smoker"), ("campfire_cooking", 600, "campfire")):
        w(f"{D}/recipe/{kind}_{c}.json", {"type": f"minecraft:{kind}", "category": "food", "ingredient": f"{MOD}:{c}", "result": {"id": f"{MOD}:cooked_{c}"}, "experience": 0.35, "cookingtime": secs})
w(f"{D}/recipe/butcher_knife.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": ["I", "S"], "key": {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}, "result": {"id": f"{MOD}:butcher_knife"}})
def shaped(name, ing):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": ["I", "S"], "key": {"I": ing, "S": "minecraft:stick"}, "result": {"id": f"{MOD}:{name}"}})
def upgrade(name, base, extra):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "equipment", "ingredients": [f"{MOD}:{base}"] + extra, "result": {"id": f"{MOD}:{name}"}})
shaped("flint_knife", "minecraft:flint"); shaped("copper_knife", "minecraft:copper_ingot"); shaped("bronze_knife", f"#{MOD}:bronze_ingots"); shaped("steel_knife", f"#{MOD}:steel_ingots")
upgrade("tempered_knife", "steel_knife", [f"#{MOD}:steel_ingots", "minecraft:blaze_powder"]); upgrade("ceramic_knife", "tempered_knife", ["minecraft:brick", "minecraft:brick"])
upgrade("diamond_edge_knife", "ceramic_knife", ["minecraft:diamond"]); upgrade("crystal_thread_knife", "diamond_edge_knife", ["minecraft:amethyst_shard", "minecraft:amethyst_shard"])
upgrade("shard_knife", "crystal_thread_knife", ["minecraft:echo_shard", "minecraft:echo_shard", "minecraft:echo_shard"])
w(f"{D}/recipe/butcher_cleaver.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": ["II", "II", " S"], "key": {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}, "result": {"id": f"{MOD}:butcher_cleaver"}})
w(f"{D}/recipe/meat_hook.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["C", "I"], "key": {"C": "minecraft:iron_chain", "I": "minecraft:iron_ingot"}, "result": {"id": f"{MOD}:meat_hook"}})
w(f"{D}/tags/item/butcher_tools.json", {"values": [f"{MOD}:{k}" for k in KNIVES] + [f"{MOD}:butcher_cleaver"] + [{"id": f"farmersdelight:{m}_knife", "required": False} for m in ("flint", "iron", "golden", "diamond", "netherite")]})
# stations: one cube model per station with its own coloured top texture
SCOL = {"drying_rack": (150, 110, 70), "salting_barrel": (130, 95, 60), "meat_grinder": (120, 125, 130), "sausage_stuffer": (140, 140, 145), "rendering_cauldron": (50, 50, 55),
        "smokehouse": (90, 70, 60), "steam_cannery": (160, 110, 70), "cold_room": (190, 215, 230), "vacuum_packer": (200, 205, 210), "vat_grower": (110, 190, 170), "stasis_case": (170, 140, 230)}
for sid, age in STATIONS.items():
    r, g, b = SCOL[sid]
    rng = random.Random(sid)
    for part, k in (("side", 0), ("top", 22)):
        img = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                n = rng.randint(-10, 10)
                edge = 28 if x in (0, 15) or y in (0, 15) else 0
                c = [max(0, min(255, v + n + k - edge)) for v in (r, g, b)]
                img.putpixel((x, y), (*c, 255))
        os.makedirs(f"{A}/textures/block", exist_ok=True)
        img.save(f"{A}/textures/block/{sid}_{part}.png")
    w(f"{A}/models/block/{sid}.json", {"parent": "minecraft:block/cube_column", "textures": {"end": f"{MOD}:block/{sid}_top", "side": f"{MOD}:block/{sid}_side"}})
    w(f"{A}/blockstates/{sid}.json", {"variants": {"": {"model": f"{MOD}:block/{sid}"}}})
    w(f"{A}/items/{sid}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{sid}"}})
    lang[f"block.{MOD}.{sid}"] = names.NAMES[sid]
    lang[f"item.{MOD}.{sid}"] = names.NAMES[sid]
    w(f"{D}/loot_table/blocks/{sid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{MOD}:{sid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
lang["message.diaithirbutchery.working"] = "Still working."
lang["message.diaithirbutchery.idle"] = "Nothing to collect. Use a cut or ingredient on it."
w(f"{A}/lang/en_us.json", lang)
# station recipes: from hand tools and stock materials, by Age
SR = {"drying_rack": (["SSS", "I I"], {"S": "minecraft:stick", "I": "minecraft:stick"}), "salting_barrel": (["PPP", "P P", "PPP"], {"P": "minecraft:oak_planks"}),
      "meat_grinder": (["I ", "BI"], {"I": "minecraft:iron_ingot", "B": "minecraft:bucket"}), "sausage_stuffer": (["I", "B", "I"], {"I": "minecraft:iron_ingot", "B": "minecraft:bucket"}),
      "rendering_cauldron": (["I I", "ICI", " I "], {"I": "minecraft:iron_ingot", "C": "minecraft:cauldron"}),
      "smokehouse": (["BBB", "BFB", "BBB"], {"B": "minecraft:bricks", "F": "minecraft:campfire"}),
      "steam_cannery": (["III", "ICI", "FFF"], {"I": "minecraft:iron_ingot", "C": "minecraft:cauldron", "F": "minecraft:furnace"}),
      "cold_room": (["III", "ISI", "III"], {"I": "minecraft:iron_ingot", "S": "minecraft:snow_block"}),
      "vacuum_packer": (["GGG", "IHI", "III"], {"G": "minecraft:glass", "I": "minecraft:iron_ingot", "H": "minecraft:hopper"}),
      "vat_grower": (["GGG", "GEG", "III"], {"G": "minecraft:glass", "I": "minecraft:iron_ingot", "E": "minecraft:emerald"}),
      "stasis_case": (["GGG", "GSG", "GGG"], {"G": "minecraft:glass", "S": "minecraft:echo_shard"})}
for sid, (pat, key) in SR.items():
    w(f"{D}/recipe/{sid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pat, "key": key, "result": {"id": f"{MOD}:{sid}"}})
print("ok", len(ITEMS), "items")
