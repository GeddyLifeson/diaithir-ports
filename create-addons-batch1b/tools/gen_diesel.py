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
        for pw in ("true", "false"):
            for wl in ("true", "false"):
                v[f"facing={facing},powered={pw},waterlogged={wl}"] = {"model": f"{NS}:block/{m}", **rot}
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

main()
