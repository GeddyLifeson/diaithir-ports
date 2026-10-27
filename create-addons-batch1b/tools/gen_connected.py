"""Generates assets, loot, recipes and tags for the Create: Connected port. All textures are drawn by art.py (original)."""
import json, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import art

NS = "create_connected"
ROOT = os.path.join(os.path.dirname(__file__), "..", "connected", "src", "main", "resources")
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=1)

def png(path, im):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)

# id, display (en), side face texture builder, end face builder, powered accent, vertical item
TEXTURES = {
    "parallel_gearbox": (lambda n: art.machine_face(n, art.ANDESITE, art.HAZ_Y, 4), lambda n: art.machine_top(n, art.ANDESITE)),
    "six_way_gearbox": (lambda n: art.machine_face(n, art.ANDESITE, art.HAZ_Y, 1), lambda n: art.machine_top(n, art.ANDESITE)),
    "brass_gearbox": (lambda n: art.machine_face(n, art.BRASS, art.HAZ_Y, 4), lambda n: art.machine_top(n, art.BRASS)),
    "inverted_clutch": (lambda n: art.machine_face(n, art.ANDESITE, (120, 150, 210), 0), lambda n: art.machine_top(n, art.ANDESITE)),
    "inverted_gearshift": (lambda n: art.machine_face(n, art.ANDESITE, (120, 150, 210), 3), lambda n: art.machine_top(n, art.ANDESITE)),
    "brake": (lambda n: art.machine_face(n, art.STEEL, art.HAZ_Y, 2), lambda n: art.machine_top(n, art.STEEL)),
}
BLOCKS = {
    "parallel_gearbox": dict(en="Parallel Gearbox", vertical="vertical_parallel_gearbox", en_v="Vertical Parallel Gearbox", powered=False),
    "six_way_gearbox": dict(en="6-way Gearbox", vertical="vertical_six_way_gearbox", en_v="Vertical 6-way Gearbox", powered=False),
    "brass_gearbox": dict(en="Brass Gearbox", vertical="vertical_brass_gearbox", en_v="Vertical Brass Gearbox", powered=False),
    "inverted_clutch": dict(en="Inverted Clutch", powered=True),
    "inverted_gearshift": dict(en="Inverted Gearshift", powered=True),
    "brake": dict(en="Brake", powered=True),
}
# recipes: shapeless ingredient lists; results are ours, ingredient ids are the Diaithir Create port's
RECIPES = {
    "parallel_gearbox": ["create:gearbox", "create:cogwheel", "create:cogwheel"],
    "six_way_gearbox": ["create:gearbox", "create:gearbox", "create:brass_ingot", "create:cogwheel"],
    "brass_gearbox": ["create:brass_casing", "create:cogwheel", "create:large_cogwheel"],
    "inverted_clutch": ["create:clutch", "minecraft:redstone_torch"],
    "inverted_gearshift": ["create:gearshift", "minecraft:redstone_torch"],
    "brake": ["create:andesite_casing", "create:shaft", "minecraft:iron_ingot", "minecraft:redstone"],
}

def main():
    lang = {f"itemGroup.{NS}.main": "Create: Connected"}
    mineable = []
    for bid, b in BLOCKS.items():
        side, end = TEXTURES[bid]
        png(f"{A}/textures/block/{bid}.png", side(bid))
        png(f"{A}/textures/block/{bid}_end.png", end(bid + "_end"))
        models = {"": bid}
        model = {"parent": "minecraft:block/cube_column", "textures": {"side": f"{NS}:block/{bid}", "end": f"{NS}:block/{bid}_end"}}
        w(f"{A}/models/block/{bid}.json", model)
        if b["powered"]:
            png(f"{A}/textures/block/{bid}_powered.png", art.machine_face(bid + "_p", art.ANDESITE if bid != "brake" else art.STEEL, (110, 220, 130), {"inverted_clutch": 0, "inverted_gearshift": 3, "brake": 2}[bid]))
            w(f"{A}/models/block/{bid}_powered.json", {"parent": "minecraft:block/cube_column", "textures": {"side": f"{NS}:block/{bid}_powered", "end": f"{NS}:block/{bid}_end"}})
        variants = {}
        for axis, rot in (("x", {"x": 90, "y": 90}), ("y", {}), ("z", {"x": 90})):
            if b["powered"]:
                for pw in ("false", "true"):
                    variants[f"axis={axis},powered={pw}"] = {"model": f"{NS}:block/{bid}" + ("_powered" if pw == "true" else ""), **rot}
            else:
                variants[f"axis={axis}"] = {"model": f"{NS}:block/{bid}", **rot}
        w(f"{A}/blockstates/{bid}.json", {"variants": variants})
        w(f"{A}/items/{bid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{bid}"}})
        lang[f"block.{NS}.{bid}"] = b["en"]
        mineable.append(f"{NS}:{bid}")
        if b.get("vertical"):
            v = b["vertical"]
            w(f"{A}/items/{v}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{bid}", "tints": []}} if False else {"model": {"type": "minecraft:model", "model": f"{NS}:block/{bid}"}})
            lang[f"item.{NS}.{v}"] = b["en_v"]
            lang[f"block.{NS}.{bid}"] = b["en"]
            # horizontal boxes drop the vertical item, vertical ones the base item
            def pool(item, axis_cond):
                return {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item}],
                        "conditions": [{"condition": "minecraft:block_state_property", "block": f"{NS}:{bid}", "properties": {"axis": axis_cond}}, {"condition": "minecraft:survives_explosion"}]}
            loot = {"type": "minecraft:block", "pools": [pool(f"{NS}:{bid}", "y")] + [pool(f"{NS}:{v}", a) for a in ("x", "z")]}
        else:
            loot = {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{bid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]}
        w(f"{D}/loot_table/blocks/{bid}.json", loot)
        w(f"{D}/recipe/{bid}.json", {"type": "minecraft:crafting_shapeless", "category": "redstone" if b["powered"] else "misc",
                                       "ingredients": RECIPES[bid], "result": {"id": f"{NS}:{bid}", "count": 1}})
        if b.get("vertical"):
            w(f"{D}/recipe/{v}_from_{bid}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [f"{NS}:{bid}"], "result": {"id": f"{NS}:{v}", "count": 1}})
            w(f"{D}/recipe/{bid}_from_{v}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [f"{NS}:{v}"], "result": {"id": f"{NS}:{bid}", "count": 1}})
    w(f"{A}/lang/en_us.json", lang)
    w(f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json", {"values": mineable})

main()

def cross():
    lang = json.load(open(f"{A}/lang/en_us.json"))
    png(f"{A}/textures/block/cross_connector_arm.png", art.shaft_side("cc_arm", art.STEEL))
    png(f"{A}/textures/block/cross_connector_core.png", art.metal_flat("cc_core", art.BRASS, 0.3))
    def arm(f, t):
        return {"from": f, "to": t, "faces": {k: {"uv": [6, 0, 10, 16] if k in ("north", "south", "east", "west") else [6, 6, 10, 10], "texture": "#arm"} for k in ("north", "south", "east", "west", "up", "down")}}
    core = {"from": [5, 5, 5], "to": [11, 11, 11], "faces": {k: {"uv": [0, 0, 16, 16], "texture": "#core"} for k in ("north", "south", "east", "west", "up", "down")}}
    w(f"{A}/models/block/cross_connector.json", {"textures": {"arm": f"{NS}:block/cross_connector_arm", "core": f"{NS}:block/cross_connector_core", "particle": f"{NS}:block/cross_connector_core"},
        "elements": [core, arm([0, 6, 6], [16, 10, 10]), arm([6, 6, 0], [10, 10, 16])]})
    w(f"{A}/blockstates/cross_connector.json", {"variants": {"axis=y": {"model": f"{NS}:block/cross_connector"}, "axis=x": {"model": f"{NS}:block/cross_connector", "x": 90, "y": 90}, "axis=z": {"model": f"{NS}:block/cross_connector", "x": 90}}})
    w(f"{A}/items/cross_connector.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/cross_connector"}})
    lang[f"block.{NS}.cross_connector"] = "Cross Connector"
    drop = lambda item: {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item}], "conditions": [{"condition": "minecraft:survives_explosion"}]}
    for kind, name in (("andesite", "Andesite"), ("brass", "Brass")):
        bid = f"{kind}_encased_cross_connector"
        w(f"{A}/models/block/{bid}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"create:block/{kind}_casing"}})
        w(f"{A}/blockstates/{bid}.json", {"variants": {f"axis={a}": {"model": f"{NS}:block/{bid}"} for a in "xyz"}})
        w(f"{A}/items/{bid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{bid}"}})
        lang[f"block.{NS}.{bid}"] = f"{name} Encased Cross Connector"
        w(f"{D}/loot_table/blocks/{bid}.json", {"type": "minecraft:block", "pools": [drop(f"{NS}:cross_connector"), drop(f"create:{kind}_casing")]})
    w(f"{D}/loot_table/blocks/cross_connector.json", {"type": "minecraft:block", "pools": [drop(f"{NS}:cross_connector")]})
    w(f"{D}/recipe/cross_connector.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ["create:shaft", "create:shaft", "create:brass_ingot"], "result": {"id": f"{NS}:cross_connector", "count": 1}})
    w(f"{A}/lang/en_us.json", lang)
    pick = json.load(open(f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json"))
    pick["values"] += [f"{NS}:cross_connector", f"{NS}:andesite_encased_cross_connector", f"{NS}:brass_encased_cross_connector"]
    w(f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json", pick)
cross()
