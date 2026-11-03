"""Generates every asset of the module from the species table (Species.java mirrors it). Own art: procedural, no third-party pixels."""
import json, os, random
from PIL import Image
NS = "diaithirgellings"
R = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
SP = [("meadow","Meadow",0x5FCB4A,["#minecraft:is_forest","minecraft:plains","minecraft:meadow"],10),
("tide","Tide",0x4A8FE0,["#minecraft:is_river","#minecraft:is_beach","#minecraft:is_ocean"],10),
("rouge","Rouge",0xD94A4A,["minecraft:savanna","minecraft:badlands","minecraft:windswept_savanna"],8),
("violet","Violet",0x9A5AD0,["minecraft:swamp","minecraft:mangrove_swamp","minecraft:dark_forest"],8),
("amber","Amber",0xE6C23A,["minecraft:sunflower_plains","minecraft:cherry_grove","#minecraft:is_hill"],6),
("ink","Ink",0x2B2B3A,["#minecraft:is_overworld"],6),
("moss","Moss",0x3E9A5A,["#minecraft:is_jungle"],10),
("frost","Frost",0x9ADCF0,["#minecraft:is_taiga","minecraft:snowy_plains","minecraft:frozen_river","minecraft:ice_spikes"],10),
("dune","Dune",0xE0CE8A,["minecraft:desert","#minecraft:is_beach"],10),
("ember","Ember",0xF08A2A,["#minecraft:is_nether"],12),
("blush","Blush",0xF5A0C8,["#minecraft:is_overworld"],1)]
def w(path, data):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f: f.write(data if isinstance(data, str) else json.dumps(data, indent=1))
def rgb(c): return ((c>>16)&255, (c>>8)&255, c&255)
def shade(c, k): return tuple(max(0, min(255, int(v*k))) for v in c)
def png(path, img):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); img.save(p)
lang = {}
for sid, name, col, biomes, wt in SP:
    c = rgb(col); rnd = random.Random(sid)
    # entity skin on the vanilla slime layout: outer shell (0,0), inner body (0,16), eyes (32,0)/(32,4), mouth (32,8)
    im = Image.new("RGBA", (64, 32), (0,0,0,0))
    for x in range(0, 32):
        for y in range(0, 16):
            im.putpixel((x,y), (*shade(c, 0.9 + rnd.random()*0.2), 150))
    for x in range(0, 24):
        for y in range(16, 32):
            im.putpixel((x,y), (*shade(c, 0.6 + rnd.random()*0.15), 235))
    for (x,y) in ((32,0),(33,0),(32,1),(33,1),(32,4),(33,4),(32,5),(33,5)): im.putpixel((x,y), (255,255,255,255) if sid != "ink" else (230,230,255,255))
    for (x,y) in ((34,0),(35,1),(34,4),(35,5)): im.putpixel((x,y), (20,20,30,255))
    im.putpixel((32,8), (30,30,40,255))
    png(f"assets/{NS}/textures/entity/{sid}_gelling.png", im)
    # gel item 16x16: blob with highlight
    g = Image.new("RGBA", (16,16), (0,0,0,0))
    for x in range(16):
        for y in range(16):
            d = ((x-7.5)**2/36 + (y-9)**2/25)
            if d < 1: g.putpixel((x,y), (*shade(c, 1.15 - 0.45*d), 235))
    for (x,y) in ((5,6),(6,5),(5,5),(6,6)): g.putpixel((x,y), (255,255,255,230))
    png(f"assets/{NS}/textures/item/gel_{sid}.png", g)
    b = Image.new("RGBA", (16,16), (*shade(c,0.85), 200))
    for x in range(16):
        for y in range(16):
            if min(x,y,15-x,15-y) < 2: b.putpixel((x,y), (*shade(c,1.0), 230))
            elif rnd.random() < 0.12: b.putpixel((x,y), (*shade(c,1.2), 210))
    png(f"assets/{NS}/textures/block/gel_block_{sid}.png", b)
    w(f"assets/{NS}/items/gel_{sid}.json", {"model":{"type":"minecraft:model","model":f"{NS}:item/gel_{sid}"}})
    w(f"assets/{NS}/items/gel_block_{sid}.json", {"model":{"type":"minecraft:model","model":f"{NS}:block/gel_block_{sid}"}})
    w(f"assets/{NS}/models/item/gel_{sid}.json", {"parent":"minecraft:item/generated","textures":{"layer0":f"{NS}:item/gel_{sid}"}})
    w(f"assets/{NS}/models/block/gel_block_{sid}.json", {"parent":"minecraft:block/cube_all","render_type":"minecraft:translucent","textures":{"all":f"{NS}:block/gel_block_{sid}"}})
    w(f"assets/{NS}/blockstates/gel_block_{sid}.json", {"variants":{"":{"model":f"{NS}:block/gel_block_{sid}"}}})
    w(f"data/{NS}/loot_table/blocks/gel_block_{sid}.json", {"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"{NS}:gel_block_{sid}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
    w(f"data/{NS}/loot_table/entities/{sid}_gelling.json", {"type":"minecraft:entity","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"{NS}:gel_{sid}","functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":0,"max":2}},{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{"type":"minecraft:uniform","min":0,"max":1}}]}]}]})
    w(f"data/{NS}/recipe/gel_block_{sid}.json", {"type":"minecraft:crafting_shaped","pattern":["GGG","GGG","GGG"],"key":{"G":f"{NS}:gel_{sid}"},"result":{"id":f"{NS}:gel_block_{sid}"}})
    w(f"data/{NS}/recipe/gel_{sid}_from_block.json", {"type":"minecraft:crafting_shapeless","ingredients":[f"{NS}:gel_block_{sid}"],"result":{"id":f"{NS}:gel_{sid}","count":9}})
    for i, b in enumerate(biomes):  # a holder set is one tag or a list of ids, so one modifier per entry
        w(f"data/{NS}/neoforge/biome_modifier/{sid}_gelling_{i}.json", {"type":"neoforge:add_spawns","biomes":b,"spawners":{"type":f"{NS}:{sid}_gelling","weight":wt,"minCount":1,"maxCount":3}})
    lang[f"entity.{NS}.{sid}_gelling"] = f"{name} Gelling"
    lang[f"item.{NS}.gel_{sid}"] = f"{name} Gel"
    lang[f"block.{NS}.gel_block_{sid}"] = f"{name} Gel Block"
w(f"assets/{NS}/lang/en_us.json", dict(sorted(lang.items())))
