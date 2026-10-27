"""Assets, data and recipes for the Create: Diesel Generators port. Textures are drawn by art.py (original)."""
import json, os, sys
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(__file__))
import art

NS = "createdieselgenerators"
ROOT = os.path.join(os.path.dirname(__file__), "..", "diesel", "src", "main", "resources")
A = f"{ROOT}/assets/{NS}"; D = f"{ROOT}/data/{NS}"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w"), indent=1)

def png(path, im):
    os.makedirs(os.path.dirname(path), exist_ok=True); im.save(path)

FLUIDS = {  # name: (dark, light, english)
    "crude_oil": ((22, 18, 16), (52, 44, 36), "Crude Oil"),
    "diesel": ((150, 120, 40), (196, 164, 70), "Diesel"),
    "gasoline": ((196, 150, 60), (232, 196, 110), "Gasoline"),
    "biodiesel": ((120, 140, 50), (168, 186, 90), "Biodiesel"),
    "ethanol": ((130, 160, 190), (186, 214, 232), "Ethanol"),
    "plant_oil": ((170, 150, 60), (218, 200, 110), "Plant Oil"),
}

def bucket(c):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    steel, dark = (150, 156, 164), (84, 90, 98)
    d.polygon([(3, 5), (13, 5), (12, 14), (4, 14)], fill=steel, outline=dark)
    d.rectangle([4, 6, 11, 8], fill=c)
    d.line([(3, 5), (13, 5)], fill=dark); d.arc([3, 0, 13, 9], 180, 360, fill=dark)
    return im

def main():
    lang = {f"itemGroup.{NS}.main": "Create: Diesel Generators", f"block.{NS}.diesel_engine": "Diesel Engine"}
    for n, (c0, c1, en) in FLUIDS.items():
        still = art.fluid_tex(n + "_still", c0, c1, False).convert("RGBA")
        flow = art.fluid_tex(n + "_flow", c0, c1, True).convert("RGBA")
        png(f"{A}/textures/block/{n}_still.png", still); png(f"{A}/textures/block/{n}_flow.png", flow)
        png(f"{A}/textures/item/{n}_bucket.png", bucket(c1))
        w(f"{A}/models/item/{n}_bucket.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}_bucket"}})
        w(f"{A}/items/{n}_bucket.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}_bucket"}})
        lang[f"fluid.{NS}.{n}"] = en
        lang[f"item.{NS}.{n}_bucket"] = en + " Bucket"
        lang[f"block.{NS}.{n}"] = en
    # engine
    png(f"{A}/textures/block/diesel_engine.png", art.machine_face("cdg_engine", art.STEEL, art.HAZ_Y, 2))
    png(f"{A}/textures/block/diesel_engine_end.png", art.machine_top("cdg_engine_end", art.STEEL))
    png(f"{A}/textures/block/diesel_engine_base.png", art.plate_tex("cdg_engine_base", art.STEEL_D))
    tex = {"body": f"{NS}:block/diesel_engine", "end": f"{NS}:block/diesel_engine_end", "base": f"{NS}:block/diesel_engine_base", "particle": f"{NS}:block/diesel_engine_end"}
    def box(f, t, tx, faces=None):
        return {"from": f, "to": t, "faces": {k: {"uv": [0, 0, 16, 16], "texture": tx} for k in ("north", "south", "east", "west", "up", "down")}}
    horiz = {"textures": tex, "elements": [box([0, 0, 0], [16, 4, 16], "#base"), box([0, 3, 3], [16, 13, 13], "#body"),
             box([6, 13, 6], [10, 16, 10], "#end")]}
    vert = {"textures": tex, "elements": [box([3, 0, 3], [13, 16, 13], "#body"), box([0, 4, 4], [16, 12, 12], "#base"), box([13, 6, 6], [16, 10, 10], "#end")]}
    w(f"{A}/models/block/diesel_engine.json", horiz); w(f"{A}/models/block/diesel_engine_vertical.json", vert)
    v = {}
    for facing, (m, rot) in {"east": ("diesel_engine", {}), "west": ("diesel_engine", {"y": 180}), "south": ("diesel_engine", {"y": 90}), "north": ("diesel_engine", {"y": 270}),
                              "up": ("diesel_engine_vertical", {}), "down": ("diesel_engine_vertical", {"x": 180})}.items():
        for lit in ("true", "false"):
          for pw in ("true", "false"):
            for wl in ("true", "false"):
                v[f"facing={facing},lit={lit},powered={pw},waterlogged={wl}"] = {"model": f"{NS}:block/{m}", **rot}
    w(f"{A}/blockstates/diesel_engine.json", {"variants": v})
    w(f"{A}/items/diesel_engine.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/diesel_engine"}})
    w(f"{A}/lang/en_us.json", lang)
    w(f"{D}/loot_table/blocks/diesel_engine.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:diesel_engine"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/recipe/diesel_engine.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["IPI", "BSB", "ICI"],
        "key": {"I": "minecraft:iron_ingot", "P": "minecraft:piston", "B": "create:brass_ingot", "S": "create:shaft", "C": "create:andesite_casing"},
        "result": {"id": f"{NS}:diesel_engine", "count": 1}})
    w(f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:diesel_engine"]})
    for n in FLUIDS:
        tag = {"crude_oil": "crude_oil", "plant_oil": "plantoil"}.get(n, n)
        w(f"{ROOT}/data/c/tags/fluid/{tag}.json", {"values": [f"{NS}:{n}", f"{NS}:flowing_{n}"]})
    w(f"{ROOT}/data/minecraft/tags/item/buckets.json", {"values": [f"{NS}:{n}_bucket" for n in FLUIDS]}) if False else None

def canister():
    """Placeholder art (textures thread replaces it)."""
    png(f"{A}/textures/block/canister.png", art.machine_face("cdg_canister", art.RUST, art.HAZ_K, 1))
    png(f"{A}/textures/block/canister_end.png", art.machine_top("cdg_canister_end", art.RUST))
    t = {"side": f"{NS}:block/canister", "end": f"{NS}:block/canister_end", "particle": f"{NS}:block/canister_end"}
    def m(frm, to, rot=None):
        return {"textures": t, "elements": [{"from": frm, "to": to, "faces": {
            "north": {"uv": [0, 0, 16, 16], "texture": "#side"}, "south": {"uv": [0, 0, 16, 16], "texture": "#side"},
            "east": {"uv": [0, 0, 16, 16], "texture": "#side"}, "west": {"uv": [0, 0, 16, 16], "texture": "#side"},
            "up": {"uv": [0, 0, 16, 16], "texture": "#end"}, "down": {"uv": [0, 0, 16, 16], "texture": "#end"}}}]}
    w(f"{A}/models/block/canister.json", m([2, 0, 2], [14, 16, 14]))
    w(f"{A}/blockstates/canister.json", {"variants": {
        "facing=up": {"model": f"{NS}:block/canister"}, "facing=down": {"model": f"{NS}:block/canister"},
        "facing=north": {"model": f"{NS}:block/canister", "x": 90}, "facing=south": {"model": f"{NS}:block/canister", "x": 90, "y": 180},
        "facing=east": {"model": f"{NS}:block/canister", "x": 90, "y": 90}, "facing=west": {"model": f"{NS}:block/canister", "x": 90, "y": 270}}})
    w(f"{A}/items/canister.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/canister"}})
    w(f"{D}/loot_table/blocks/canister.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:canister",
        "functions": [{"function": "minecraft:copy_components", "source": "minecraft:block_entity", "include": [f"{NS}:fluid_contents"]}]}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/recipe/canister.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": [" I ", "ICI", " I "],
        "key": {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot"}, "result": {"id": f"{NS}:canister", "count": 1}})
    path = f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json"
    d = json.load(open(path)); d["values"].append(f"{NS}:canister"); json.dump(d, open(path, "w"), indent=1)
    lp = f"{A}/lang/en_us.json"; l = json.load(open(lp)); l[f"block.{NS}.canister"] = "Canister"; json.dump(l, open(lp, "w"), indent=1)

def burner():
    png(f"{A}/textures/block/burner.png", art.machine_face("cdg_burner", art.STEEL_D, art.HAZ_Y, 3))
    png(f"{A}/textures/block/burner_top.png", art.machine_top("cdg_burner_top", art.STEEL_D))
    png(f"{A}/textures/block/burner_lit.png", art.machine_top("cdg_burner_lit", art.GLOW))
    for lit in ("", "_lit"):
        tp = f"{NS}:block/burner" + ("_lit" if lit else "_top")
        f = lambda t: {"uv": [0, 0, 16, 16], "texture": t}
        w(f"{A}/models/block/burner{lit}.json", {"textures": {"side": f"{NS}:block/burner", "top": tp, "particle": f"{NS}:block/burner"},
            "elements": [{"from": [1, 0, 1], "to": [15, 12, 15], "faces": {"north": f("#side"), "south": f("#side"), "east": f("#side"), "west": f("#side"), "up": f("#top"), "down": f("#side")}}]})
    v = {}
    for axis, rot in (("x", {"y": 90}), ("z", {})):
        for hl in ("none", "smouldering", "fading", "kindled", "seething"):
            for lit in ("true", "false"):
                v[f"axis={axis},heat_level={hl},lit={lit}"] = {"model": f"{NS}:block/burner" + ("_lit" if lit == "true" else ""), **rot}
    w(f"{A}/blockstates/burner.json", {"variants": v})
    w(f"{A}/items/burner.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/burner"}})
    w(f"{D}/loot_table/blocks/burner.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:burner"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/recipe/burner.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["ISI", "IBI", "III"],
        "key": {"I": "minecraft:iron_ingot", "S": "create:shaft", "B": "minecraft:bucket"}, "result": {"id": f"{NS}:burner", "count": 1}})
    path = f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json"
    d = json.load(open(path)); d["values"].append(f"{NS}:burner"); json.dump(d, open(path, "w"), indent=1)
    w(f"{ROOT}/data/create/tags/block/passive_boiler_heaters.json", {"values": []}) if False else None
    lp = f"{A}/lang/en_us.json"; l = json.load(open(lp)); l[f"block.{NS}.burner"] = "Burner"; json.dump(l, open(lp, "w"), indent=1)

def modular_and_upgrades():
    png(f"{A}/textures/block/modular_diesel_engine.png", art.machine_face("cdg_modular", art.STEEL, art.HAZ_Y, 4))
    png(f"{A}/textures/block/modular_diesel_engine_end.png", art.machine_top("cdg_modular_end", art.STEEL_D))
    f = lambda t: {"uv": [0, 0, 16, 16], "texture": t}
    w(f"{A}/models/block/modular_diesel_engine.json", {"textures": {"side": f"{NS}:block/modular_diesel_engine", "end": f"{NS}:block/modular_diesel_engine_end", "particle": f"{NS}:block/modular_diesel_engine"},
        "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": {"north": f("#end"), "south": f("#end"), "east": f("#side"), "west": f("#side"), "up": f("#side"), "down": f("#side")}}]})
    v = {}
    for facing, rot in (("north", {}), ("east", {"y": 90}), ("south", {"y": 180}), ("west", {"y": 270})):
        for pipe in ("true", "false"):
            for lit in ("true", "false"):
                for pw in ("true", "false"):
                    v[f"facing={facing},lit={lit},pipe={pipe},powered={pw}"] = {"model": f"{NS}:block/modular_diesel_engine", **rot}
    w(f"{A}/blockstates/modular_diesel_engine.json", {"variants": v})
    w(f"{A}/items/modular_diesel_engine.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/modular_diesel_engine"}})
    w(f"{D}/loot_table/blocks/modular_diesel_engine.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:modular_diesel_engine"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/recipe/modular_diesel_engine.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["ICI", "BSB", "ICI"],
        "key": {"I": "minecraft:iron_ingot", "C": "create:andesite_casing", "B": "create:brass_ingot", "S": "create:shaft"}, "result": {"id": f"{NS}:modular_diesel_engine", "count": 1}})
    for n, col, rec, en in (("engine_silencer", (90, 96, 104), ["III", "IPI", "III"], "Engine Silencer"), ("engine_turbocharger", (180, 140, 60), ["BPB", "ISI", "BPB"], "Engine Turbocharger")):
        im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
        d.ellipse([2, 2, 13, 13], fill=col, outline=(40, 42, 46)); d.ellipse([5, 5, 10, 10], fill=(40, 42, 46))
        png(f"{A}/textures/item/{n}.png", im)
        w(f"{A}/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}"}})
        w(f"{A}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
        key = {"I": "minecraft:iron_ingot", "P": "create:iron_sheet"} if n == "engine_silencer" else {"B": "create:brass_ingot", "P": "create:propeller", "I": "minecraft:iron_ingot", "S": "create:shaft"}
        w(f"{D}/recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": rec, "key": key, "result": {"id": f"{NS}:{n}", "count": 1}})
        lp = f"{A}/lang/en_us.json"; l = json.load(open(lp)); l[f"item.{NS}.{n}"] = en; json.dump(l, open(lp, "w"), indent=1)
    path = f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json"
    d = json.load(open(path)); d["values"].append(f"{NS}:modular_diesel_engine"); json.dump(d, open(path, "w"), indent=1)
    lp = f"{A}/lang/en_us.json"; l = json.load(open(lp)); l[f"block.{NS}.modular_diesel_engine"] = "Modular Diesel Engine"; json.dump(l, open(lp, "w"), indent=1)

def huge():
    png(f"{A}/textures/block/huge_diesel_engine.png", art.machine_face("cdg_huge", art.STEEL_D, art.HAZ_Y, 5))
    png(f"{A}/textures/block/huge_diesel_engine_end.png", art.machine_top("cdg_huge_end", art.STEEL))
    f = lambda t: {"uv": [0, 0, 16, 16], "texture": t}
    t = {"side": f"{NS}:block/huge_diesel_engine", "end": f"{NS}:block/huge_diesel_engine_end", "particle": f"{NS}:block/huge_diesel_engine"}
    w(f"{A}/models/block/huge_diesel_engine.json", {"textures": t, "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": {"north": f("#side"), "south": f("#side"), "east": f("#end"), "west": f("#side"), "up": f("#side"), "down": f("#side")}}]})
    v = {}
    for facing, rot in {"east": {}, "south": {"y": 90}, "west": {"y": 180}, "north": {"y": 270}, "up": {"z": 90}, "down": {"z": 270}}.items():
        for lit in ("true", "false"):
            for pw in ("true", "false"):
                for wl in ("true", "false"):
                    v[f"facing={facing},lit={lit},powered={pw},waterlogged={wl}"] = {"model": f"{NS}:block/huge_diesel_engine", **rot}
    w(f"{A}/blockstates/huge_diesel_engine.json", {"variants": v})
    w(f"{A}/items/huge_diesel_engine.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/huge_diesel_engine"}})
    w(f"{D}/loot_table/blocks/huge_diesel_engine.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:huge_diesel_engine"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/recipe/huge_diesel_engine.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["BEB", "ESE", "BEB"],
        "key": {"B": "create:brass_sheet", "E": f"{NS}:modular_diesel_engine", "S": "create:shaft"}, "result": {"id": f"{NS}:huge_diesel_engine", "count": 1}})
    path = f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json"
    d = json.load(open(path)); d["values"].append(f"{NS}:huge_diesel_engine"); json.dump(d, open(path, "w"), indent=1)
    lp = f"{A}/lang/en_us.json"; l = json.load(open(lp)); l[f"block.{NS}.huge_diesel_engine"] = "Huge Diesel Engine"; json.dump(l, open(lp, "w"), indent=1)

def tower():
    png(f"{A}/textures/block/distillation_tower.png", art.machine_face("cdg_tower", art.STEEL, art.HAZ_K, 6))
    png(f"{A}/textures/block/distillation_tower_top.png", art.machine_top("cdg_tower_top", art.STEEL_D))
    f = lambda t: {"uv": [0, 0, 16, 16], "texture": t}
    w(f"{A}/models/block/distillation_tower.json", {"textures": {"side": f"{NS}:block/distillation_tower", "top": f"{NS}:block/distillation_tower_top", "particle": f"{NS}:block/distillation_tower"},
        "elements": [{"from": [1, 0, 1], "to": [15, 16, 15], "faces": {"north": f("#side"), "south": f("#side"), "east": f("#side"), "west": f("#side"), "up": f("#top"), "down": f("#top")}}]})
    w(f"{A}/blockstates/distillation_tower.json", {"variants": {"": {"model": f"{NS}:block/distillation_tower"}}})
    w(f"{A}/items/distillation_tower.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/distillation_tower"}})
    w(f"{D}/loot_table/blocks/distillation_tower.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:distillation_tower"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/recipe/distillation_tower.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["BIB", "BCB", "BIB"],
        "key": {"B": "create:brass_sheet", "I": "minecraft:iron_ingot", "C": "create:fluid_tank"}, "result": {"id": f"{NS}:distillation_tower", "count": 2}})
    path = f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json"
    d = json.load(open(path)); d["values"].append(f"{NS}:distillation_tower"); json.dump(d, open(path, "w"), indent=1)
    lp = f"{A}/lang/en_us.json"; l = json.load(open(lp)); l[f"block.{NS}.distillation_tower"] = "Distillation Tower"; json.dump(l, open(lp, "w"), indent=1)

def recipes_and_worldgen():
    NSF = NS
    def ft(tag, amount): return {"type": "fluid_tag", "amount": amount, "fluid_tag": "#c:" + tag}
    def res(n, amount): return {"id": f"{NSF}:{n}", "amount": amount}
    w(f"{D}/recipe/mixing/distill_crude_oil.json", {"type": "create:mixing", "heat_requirement": "heated", "ingredients": [],
        "fluid_ingredients": [ft("crude_oil", 81000)], "fluid_results": [res("diesel", 40500), res("gasoline", 20250)]})
    w(f"{D}/recipe/mixing/plant_oil.json", {"type": "create:mixing", "heat_requirement": "heated",
        "ingredients": ["minecraft:wheat_seeds", "minecraft:wheat_seeds", "minecraft:wheat_seeds", "minecraft:wheat_seeds"], "fluid_results": [res("plant_oil", 8100)]})
    w(f"{D}/recipe/mixing/ethanol.json", {"type": "create:mixing", "heat_requirement": "heated",
        "ingredients": ["minecraft:sugar", "minecraft:sugar"], "fluid_ingredients": [{"type": "fluid_tag", "amount": 40500, "fluid_tag": "#c:water"}], "fluid_results": [res("ethanol", 20250)]})
    w(f"{D}/recipe/mixing/biodiesel.json", {"type": "create:mixing", "heat_requirement": "heated", "ingredients": [],
        "fluid_ingredients": [ft("plantoil", 40500), ft("ethanol", 20250)], "fluid_results": [res("biodiesel", 60750)]})
    w(f"{D}/worldgen/configured_feature/oil_spring.json", {"type": "minecraft:spring_feature", "config": {
        "state": {"Name": f"{NSF}:crude_oil", "Properties": {"falling": "true"}}, "requires_block_below": True, "rock_count": 4, "hole_count": 1,
        "valid_blocks": "#minecraft:base_stone_overworld"}})
    w(f"{D}/worldgen/placed_feature/oil_spring.json", {"feature": f"{NSF}:oil_spring", "placement": [
        {"type": "minecraft:rarity_filter", "chance": 6}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": -48}, "max_inclusive": {"absolute": 24}}},
        {"type": "minecraft:biome"}]})
    w(f"{D}/neoforge/biome_modifier/oil_spring.json", {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld", "features": f"{NSF}:oil_spring", "step": "underground_ores"})

main()
canister()
burner()
modular_and_upgrades()
huge()
tower()
recipes_and_worldgen()
