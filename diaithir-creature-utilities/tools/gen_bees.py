#!/usr/bin/env python3
"""Productive-bee-style species set, clean-room from the public feature description (many bee species, wild solitary bees
found in biomes, resource bees bred from them, a comb per species that smelts to its resource). Writes species.json
(the one source for Java and for this script), comb items, comb and bee textures, lang, smelting recipes, and one
biome modifier per wild biome tag. Textures are drawn here, procedural and seeded. Own code, MIT.
Run: python3 -I tools/gen_bees.py"""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(HERE, "src/main/resources")
NS = "diaithircreatureutilities"
A, D = f"{RES}/assets/{NS}", f"{RES}/data/{NS}"

H = lambda s: [int(s[i:i + 2], 16) for i in (1, 3, 5)]
# id, English name, body, stripe, wild biome tag (or None), parents (or None), smelts to (item, count)
S = [
    ("ashy_mining", "Ashy Mining Bee", "#9a9690", "#3a3836", "minecraft:is_forest", None, ("minecraft:flint", 2)),
    ("blue_banded", "Blue-Banded Bee", "#5f86c8", "#1c2a46", "c:is_plains", None, ("minecraft:blue_dye", 2)),
    ("chocolate_mining", "Chocolate Mining Bee", "#7a4b2c", "#2a170c", "minecraft:is_jungle", None, ("minecraft:cocoa_beans", 2)),
    ("digger", "Digger Bee", "#b9824c", "#46280f", "minecraft:is_river", None, ("minecraft:clay_ball", 2)),
    ("leafcutter", "Leafcutter Bee", "#6a9a42", "#22381a", "minecraft:is_taiga", None, ("minecraft:oak_sapling", 1)),
    ("mason", "Mason Bee", "#8a8f99", "#2e3138", "minecraft:is_mountain", None, ("minecraft:brick", 2)),
    ("carpenter", "Carpenter Bee", "#7a5230", "#1e1208", "minecraft:is_badlands", None, ("minecraft:stick", 4)),
    ("sweat", "Sweat Bee", "#3fa58c", "#123a30", "minecraft:is_savanna", None, ("minecraft:sugar", 2)),
    ("squash", "Squash Bee", "#e0a030", "#5a3a08", "c:is_swamp", None, ("minecraft:pumpkin_seeds", 2)),
    ("reed", "Reed Bee", "#a6b86a", "#3a4620", "minecraft:is_beach", None, ("minecraft:sugar_cane", 2)),
    ("hoarder", "Hoarder Bee", "#d6b24a", "#4a380a", "c:is_desert", None, ("minecraft:gold_nugget", 3)),
    ("nomad", "Nomad Bee", "#dcdcd6", "#5a5a58", "c:is_snowy", None, ("minecraft:snowball", 4)),
    ("lumber", "Lumber Bee", "#8a6a3a", "#2a1c0a", "minecraft:is_hill", None, ("minecraft:oak_log", 1)),
    ("aquatic", "Aquatic Bee", "#3a8ab8", "#0e2e44", "minecraft:is_ocean", None, ("minecraft:kelp", 3)),
    ("iron", "Iron Bee", "#c8c8c8", "#505050", None, ("ashy_mining", "mason"), ("minecraft:iron_nugget", 4)),
    ("copper", "Copper Bee", "#c8764a", "#5a2c14", None, ("digger", "mason"), ("minecraft:copper_nugget", 4)),
    ("gold", "Gold Bee", "#f0d040", "#6a5008", None, ("hoarder", "iron"), ("minecraft:gold_nugget", 5)),
    ("coal", "Coal Bee", "#3a3a3e", "#0e0e10", None, ("ashy_mining", "carpenter"), ("minecraft:coal", 1)),
    ("redstone", "Redstone Bee", "#c8281c", "#4a0a06", None, ("chocolate_mining", "copper"), ("minecraft:redstone", 3)),
    ("lapis", "Lapis Bee", "#2a50b8", "#0a1a48", None, ("blue_banded", "iron"), ("minecraft:lapis_lazuli", 3)),
    ("quartz", "Quartz Bee", "#ece6dc", "#8a8478", None, ("sweat", "iron"), ("minecraft:quartz", 2)),
    ("glowstone", "Glowstone Bee", "#f0c860", "#8a5a14", None, ("hoarder", "redstone"), ("minecraft:glowstone_dust", 3)),
    ("diamond", "Diamond Bee", "#58d8d0", "#126860", None, ("gold", "lapis"), ("minecraft:diamond", 1)),
    ("emerald", "Emerald Bee", "#38b858", "#0e4a1c", None, ("gold", "leafcutter"), ("minecraft:emerald", 1)),
    ("amethyst", "Amethyst Bee", "#9a6ad0", "#3c1c64", None, ("quartz", "lapis"), ("minecraft:amethyst_shard", 2)),
    ("obsidian", "Obsidian Bee", "#2a1c40", "#0a0414", None, ("coal", "lapis"), ("minecraft:obsidian", 1)),
    ("slimy", "Slimy Bee", "#78c850", "#2a5a1c", None, ("squash", "reed"), ("minecraft:slime_ball", 2)),
    ("blazing", "Blazing Bee", "#f09020", "#782800", None, ("glowstone", "copper"), ("minecraft:blaze_powder", 2)),
    ("magmatic", "Magmatic Bee", "#c84a18", "#380e04", None, ("blazing", "coal"), ("minecraft:magma_cream", 1)),
    ("ender", "Ender Bee", "#3a7c78", "#0a1c1c", None, ("obsidian", "quartz"), ("minecraft:ender_pearl", 1)),
    ("prismarine", "Prismarine Bee", "#58a898", "#184438", None, ("aquatic", "quartz"), ("minecraft:prismarine_shard", 2)),
    ("ancient", "Ancient Bee", "#5a4a44", "#1a1210", None, ("magmatic", "obsidian"), ("minecraft:netherite_scrap", 1)),
]


def clamp(v):
    return max(0, min(255, int(v)))


def put(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)
        f.write("\n")


def comb_tex(sp, body):
    r = random.Random("comb" + sp)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    wax = (232, 200, 120)
    for y in range(16):
        for x in range(16):
            # staggered hex cells: distance to nearest cell centre decides rim or fill
            row = y // 5
            cx = (x - (3 if row % 2 else 0)) % 6
            cy = y % 5
            inside = 1 <= cx <= 4 and 1 <= cy <= 3
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 > 56:
                continue
            col = body if inside else wax
            im.putpixel((x, y), tuple(clamp(c + r.randint(-12, 12)) for c in col) + (255,))
    return im


def bee_tex(sp, body, stripe):
    r = random.Random("bee" + sp)
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    dark = (24, 20, 18, 255)

    def paint(x0, y0, w, h, fn):
        for yy in range(h):
            for xx in range(w):
                c = fn(xx, yy)
                im.putpixel((x0 + xx, y0 + yy), tuple(clamp(v + r.randint(-6, 6)) for v in c[:3]) + (255,))

    along_y = lambda xx, yy: stripe if (yy // 3) % 2 else body   # stripes cross the 10-long axis drawn vertically
    along_x = lambda xx, yy: stripe if (xx // 3) % 2 else body
    paint(10, 0, 7, 10, along_y)       # top
    paint(17, 0, 7, 10, along_y)       # bottom
    paint(0, 10, 10, 7, along_x)       # right side
    paint(17, 10, 10, 7, along_x)      # left side
    paint(10, 10, 7, 7, lambda xx, yy: body)   # front
    paint(27, 10, 7, 7, lambda xx, yy: stripe)  # back
    for x, y in ((11, 13), (12, 13), (14, 13), (15, 13)):
        im.putpixel((x, y), dark)
    for (x0, y0) in ((2, 0), (2, 3)):    # antennae
        paint(x0, y0, 4, 3, lambda xx, yy: dark)
    paint(26, 7, 3, 3, lambda xx, yy: stripe)   # stinger
    for y0 in (1, 3, 5):                 # legs
        paint(26, y0, 6, 2, lambda xx, yy: dark)
    for yy in range(6):                  # wings, pale and see-through
        for xx in range(9):
            im.putpixel((xx, 18 + yy), (232, 240, 248, 170))
    return im


rows = []
for sp, name, body, stripe, biome, parents, out in S:
    rows.append({"id": sp, "name": name, "body": H(body), "stripe": H(stripe), "biome": biome, "parents": list(parents) if parents else None,
                 "smelts_to": out[0], "count": out[1]})
ids = {r["id"] for r in rows}
done = set()
for r in rows:  # parents must come earlier: the breeding tree is acyclic and every bred bee can be reached from wild ones
    if r["parents"]:
        assert all(p in done for p in r["parents"]), r["id"]
    done.add(r["id"])
put(f"{D}/bees/species.json", {"_comment": "One row per bee species. Wild species have a biome tag; bred species have two parents (a bee of each parent, bred with flowers, has a 1 in 2 chance of the child). Run tools/gen_bees.py after editing sources there.", "species": rows})

lang = {}
tags = {}
for r in rows:
    sp = r["id"]
    lang[f"item.{NS}.comb_{sp}"] = r["name"].replace(" Bee", "") + " Comb"
    lang[f"entity.{NS}.species_bee.{sp}"] = r["name"]
    put(f"{A}/items/comb_{sp}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/comb_{sp}"}})
    put(f"{A}/models/item/comb_{sp}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/comb_{sp}"}})
    for sub, im in (("item/comb_" + sp, comb_tex(sp, r["body"])), ("entity/bee/" + sp, bee_tex(sp, tuple(r["body"]), tuple(r["stripe"])))):
        p = f"{A}/textures/{sub}.png"
        os.makedirs(os.path.dirname(p), exist_ok=True)
        im.save(p)
    put(f"{D}/recipe/comb_{sp}_smelting.json", {"type": "minecraft:smelting", "category": "misc", "ingredient": f"{NS}:comb_{sp}",
        "result": {"id": r["smelts_to"], "count": r["count"]}, "experience": 0.2, "cookingtime": 200})
    if r["biome"]:
        tags.setdefault(r["biome"], []).append(sp)
for tag, sps in tags.items():
    n = tag.replace(":", "_")
    put(f"{D}/neoforge/biome_modifier/wild_bees_{n}.json", {"type": "neoforge:add_spawns", "biomes": "#" + tag,
        "spawners": {"type": f"{NS}:species_bee", "weight": 4, "minCount": 1, "maxCount": 2}})
lang[f"tooltip.{NS}.comb"] = "Smelt it for the bee's resource."
lang[f"itemGroup.{NS}.bees"] = "Bees"
lp = f"{A}/lang/en_us.json"
cur = json.load(open(lp, encoding="utf-8"))
cur.update(lang)
put(lp, cur)
print(len(rows), "species,", len(tags), "wild biome tags")
