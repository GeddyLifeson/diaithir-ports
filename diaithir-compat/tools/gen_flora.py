#!/usr/bin/env python3
"""Writes the Age I wild-flora assets and data of Diaithir Compat: textures, blockstates, models, item definitions, lang,
loot tables, worldgen features, biome modifiers and earth_ranges/flora_*.json.

  python3 tools/gen_flora.py [--client-jar PATH]

Textures are built from vanilla textures in the Minecraft client jar (grey tinted sprites are coloured, static ones are
recoloured and cut down), so no outside art is used. Plants, items and ranges are the tables below; the Java side
(Flora.java) registers the same ids, and --check lists ids that disagree.

Sources for names and ranges: Zohary, Hopf and Weiss, Domestication of Plants in the Old World, 4th ed. 2012 (wild
einkorn, emmer, barley, lentil, pea, chickpea and fig: Fertile Crescent, southeast Anatolia for the chickpea);
Wikipedia species pages for Dryas octopetala, Betula nana, Salix herbacea, Artemisia frigida, Ephedra distachya, Empetrum
nigrum, Calluna vulgaris, Hippophae rhamnoides, Saxifraga oppositifolia and Pinus cembra; regions and indicator status
from AGE_I_FLORA.csv (DiaithirRPG world/earth/age1); 15 ka biome table from TERRAIN.md section 6.
"""
import argparse, io, json, os, random, sys, zipfile
from PIL import Image
import colorsys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_earth_ranges as G

ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithircompat"
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)

# ---------------------------------------------------------------- sprites
class Jar:
    def __init__(self, path):
        self.z = zipfile.ZipFile(path)
    def get(self, name):
        return Image.open(io.BytesIO(self.z.read("assets/minecraft/textures/%s.png" % name))).convert("RGBA")

def px(im):
    return [(x, y) for y in range(im.height) for x in range(im.width) if im.getpixel((x, y))[3] > 0]

def tint(im, rgb, gain=1.3):
    out = im.copy()
    for x, y in px(im):
        r, g, b, a = im.getpixel((x, y))
        l = (r + g + b) / 3 / 255 * gain
        out.putpixel((x, y), tuple(min(255, int(c * l)) for c in rgb) + (a,))
    return out

def recolor(im, f):
    out = im.copy()
    for x, y in px(im):
        r, g, b, a = im.getpixel((x, y))
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        nr, ng, nb = f(h * 360, s, v, (x, y))
        out.putpixel((x, y), (int(nr * 255), int(ng * 255), int(nb * 255), a))
    return out

def hsv(h, s, v):
    return colorsys.hsv_to_rgb((h % 360) / 360, max(0, min(1, s)), max(0, min(1, v)))

def shift(dh=0, sf=1.0, vf=1.0, only=None):
    def f(h, s, v, p):
        if only and not only(h, s, v):
            return colorsys.hsv_to_rgb(h / 360, s, v)
        return hsv(h + dh, s * sf, v * vf)
    return f

def squash(im, rows, width=None):
    """Cut the sprite down to `rows` pixel rows, standing on the ground line."""
    pts = px(im)
    x0 = min(p[0] for p in pts); x1 = max(p[0] for p in pts) + 1
    y0 = min(p[1] for p in pts); y1 = max(p[1] for p in pts) + 1
    crop = im.crop((x0, y0, x1, y1))
    w = width or crop.width
    crop = crop.resize((w, rows), Image.NEAREST)
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    out.paste(crop, ((16 - w) // 2, 16 - rows))
    return out

def dot(im, x, y, c, size=1):
    for dx in range(size):
        for dy in range(size):
            if 0 <= x + dx < 16 and 0 <= y + dy < 16:
                im.putpixel((x + dx, y + dy), tuple(c) + (255,))

def flower(im, cx, cy, petal, centre):
    for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
        dot(im, cx + dx, cy + dy, petal)
    dot(im, cx, cy, centre)

def line(im, x0, y0, x1, y1, c):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        dot(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), c)

def top(im):
    return min(p[1] for p in px(im))

def sprite_dryas(j):
    im = squash(tint(j.get("block/fern"), (62, 105, 52), 1.15), 6, 13)
    t = top(im)
    flower(im, 4, t - 2, (250, 250, 245), (232, 190, 40)); flower(im, 11, t - 1, (250, 250, 245), (232, 190, 40))
    return im

def sprite_betula_nana(j):
    im = squash(tint(j.get("block/bush"), (92, 126, 58), 1.2), 10, 13)
    t = top(im)
    for x in (4, 7, 10): dot(im, x, t, (120, 100, 70))     # bare twig tips
    dot(im, 6, t + 2, (190, 190, 150)); dot(im, 9, t + 3, (190, 190, 150))  # catkins
    return im

def sprite_salix_herbacea(j):
    im = squash(tint(j.get("block/fern"), (96, 150, 70), 1.2), 5, 12)
    t = top(im)
    dot(im, 5, t - 1, (205, 205, 200), 1); dot(im, 9, t - 2, (205, 205, 200), 1); dot(im, 9, t - 1, (205, 205, 200), 1)
    return im

def sprite_artemisia(j):
    im = tint(j.get("block/short_grass"), (140, 160, 128), 1.3)
    im = recolor(im, lambda h, s, v, p: hsv(h, s * 0.9, min(1, v * 1.08)))
    return squash(im, 11, 13)

def sprite_ephedra(j):
    return recolor(j.get("block/dead_bush"), lambda h, s, v, p: hsv(88, 0.55, 0.30 + v * 0.70))

def sprite_empetrum(j):
    def f(h, s, v, p):
        if h < 30 or h > 330:            # berries
            return (0.10, 0.08, 0.16)
        return hsv(h, s * 0.9, v * 0.62)
    return squash(recolor(j.get("block/sweet_berry_bush_stage3"), f), 8, 14)

def sprite_calluna(j):
    im = squash(tint(j.get("block/bush"), (78, 90, 48), 1.1), 10, 14)
    rnd = random.Random(5); t = top(im)
    for _ in range(14):
        dot(im, rnd.randint(2, 13), rnd.randint(t - 1, t + 3), (168, 70, 150) if rnd.random() < 0.7 else (200, 110, 180))
    return im

def sprite_hippophae(j):
    def f(h, s, v, p):
        if h < 30 or h > 330:
            return (0.93, 0.50, 0.08)
        return hsv(95, 0.28, 0.35 + v * 0.7)
    return recolor(j.get("block/sweet_berry_bush_stage3"), f)

def sprite_saxifraga(j):
    im = squash(tint(j.get("block/fern"), (60, 96, 60), 1.1), 4, 12)
    t = top(im)
    for cx, cy in ((4, t - 1), (8, t - 2), (11, t - 1)):
        flower(im, cx, cy, (176, 70, 190), (240, 230, 120))
    return im

def cereal(j, hue, sat, val, rows, awns):
    im = recolor(j.get("block/wheat_stage7"), lambda h, s, v, p: hsv(hue, sat * s / 0.57, v * val))
    if rows < 16: im = squash(im, rows, 12)
    t = top(im)
    for x in awns:                        # awns
        line(im, x, t + 1, x + 1, t - 3, tuple(int(c * 255) for c in hsv(hue + 8, sat * 0.8, min(1, val + 0.1))))
    return im

def sprite_einkorn(j): return cereal(j, 52, 0.62, 0.82, 14, (4, 8, 11))
def sprite_emmer(j): return cereal(j, 36, 0.66, 0.78, 15, (3, 6, 9, 12))
def sprite_barley(j): return cereal(j, 66, 0.55, 0.88, 14, (3, 5, 7, 9, 11, 13))

def legume(j, rgb, pod, flower_c, rows):
    im = squash(tint(j.get("block/fern"), rgb, 1.2), rows, 13)
    t = top(im)
    for (x, y) in ((4, t + 2), (10, t + 3)):
        dot(im, x, y, pod, 1); dot(im, x, y + 1, pod, 1); dot(im, x + 1, y + 1, pod, 1)
    dot(im, 7, t - 1, flower_c); dot(im, 8, t, flower_c)
    return im

def sprite_lentil(j): return legume(j, (118, 150, 64), (186, 160, 100), (190, 170, 225), 9)
def sprite_pea(j): return legume(j, (80, 150, 60), (96, 180, 70), (245, 245, 240), 12)
def sprite_chickpea(j): return legume(j, (112, 138, 78), (206, 190, 120), (240, 235, 235), 10)

def sprite_leaves_cembra(j): return tint(j.get("block/spruce_leaves"), (66, 106, 78), 1.35)
def sprite_leaves_fig(j): return tint(j.get("block/oak_leaves"), (104, 150, 54), 1.35)

def sprite_sapling_cembra(j): return recolor(j.get("block/spruce_sapling"), shift(8, 0.85, 1.35))
def sprite_sapling_fig(j): return recolor(j.get("block/oak_sapling"), shift(-8, 0.8, 1.2))

def item_grain(hue, sat, val):
    return lambda j: recolor(j.get("item/wheat_seeds"), lambda h, s, v, p: hsv(hue, sat, v * val))

def item_berry(rgb):
    return lambda j: recolor(j.get("item/sweet_berries"), lambda h, s, v, p: tuple(c / 255 * (0.55 + v * 0.75) for c in rgb))

ITEMS = {  # id: (name, sprite, nutrition, saturation)
    "wild_einkorn_grain": ("Wild Einkorn Grain", item_grain(48, 0.55, 1.1), 2, 0.3),
    "wild_emmer_grain": ("Wild Emmer Grain", item_grain(34, 0.62, 0.95), 2, 0.3),
    "wild_barley_grain": ("Wild Barley Grain", item_grain(60, 0.45, 1.15), 2, 0.3),
    "wild_lentils": ("Wild Lentils", item_grain(18, 0.55, 0.85), 2, 0.4),
    "wild_peas": ("Wild Peas", item_grain(100, 0.55, 1.0), 2, 0.4),
    "wild_chickpeas": ("Wild Chickpeas", item_grain(42, 0.4, 1.15), 2, 0.4),
    "wild_figs": ("Wild Figs", item_berry((120, 60, 105)), 3, 0.4),
    "crowberries": ("Crowberries", item_berry((30, 24, 52)), 2, 0.2),
    "sea_buckthorn_berries": ("Sea Buckthorn Berries", item_berry((240, 140, 25)), 2, 0.2),
    "cembra_pine_nuts": ("Cembra Pine Nuts", item_grain(30, 0.3, 1.25), 2, 0.5),
}

# ---------------------------------------------------------------- the plants
PLANT, TREE = "plant", "tree"
SNOWY = ["snowy_plains", "snowy_taiga", "snowy_slopes", "grove", "meadow", "stony_peaks", "frozen_peaks", "jagged_peaks"]
STEPPE = ["plains", "meadow", "windswept_hills", "windswept_savanna", "savanna", "savanna_plateau", "sunflower_plains", "windswept_gravelly_hills"]
DRYLAND = ["plains", "savanna", "windswept_savanna", "savanna_plateau", "windswept_hills", "forest", "meadow", "sunflower_plains"]

# id: dict(kind, name, sci, sprite, regions (names in gen_earth_ranges.R), biomes, rarity, count, drops, note)
P = {
 "dryas_octopetala": dict(name="Mountain Avens", sci="Dryas octopetala", sprite=sprite_dryas, regions=["Europe", "N Asia"],
    biomes=SNOWY + ["taiga"], rarity=2, count=14, note="AGE_I_FLORA.csv: Europe 494 occurrences, N Asia 8 (74 sites); classes 24-27 of the 15 ka table"),
 "betula_nana": dict(name="Dwarf Birch", sci="Betula nana", sprite=sprite_betula_nana, regions=["Europe", "N Asia"],
    biomes=["snowy_plains", "snowy_taiga", "taiga", "meadow", "grove", "old_growth_pine_taiga", "old_growth_spruce_taiga", "plains"], rarity=2, count=14,
    note="AGE_I_FLORA.csv: Europe 1222, N Asia 4 (108 sites); shrub tundra and steppe tundra"),
 "salix_herbacea": dict(name="Dwarf Willow", sci="Salix herbacea", sprite=sprite_salix_herbacea, regions=["Europe", "N Asia", "N America"],
    biomes=["snowy_plains", "snowy_taiga", "snowy_slopes", "meadow", "grove", "stony_peaks", "taiga"], rarity=3, count=12,
    note="Salix has 1234 sites in the CSV (Europe, N America, N Asia); the arctic-alpine dwarf willow is the tundra indicator"),
 "artemisia_frigida": dict(name="Steppe Sage", sci="Artemisia frigida", sprite=sprite_artemisia, regions=["Europe", "N Asia", "N America", "SW Asia", "E/S Asia", "N Africa coast"],
    biomes=STEPPE + ["desert", "snowy_plains", "taiga"], rarity=2, count=18,
    note="Artemisia is the second most common taxon in the CSV (1340 sites); steppe tundra class 22 and the dry steppes"),
 "ephedra_distachya": dict(name="Joint-fir", sci="Ephedra distachya", sprite=sprite_ephedra, regions=["Europe", "N Asia", "N America", "E/S Asia", "SW Asia", "N Africa coast", "S America"],
    biomes=["desert", "windswept_savanna", "savanna_plateau", "plains", "badlands", "windswept_hills", "meadow", "savanna", "stony_shore"], rarity=3, count=10,
    note="Ephedra 202 sites plus the distachya and fragilis types (CSV); cold steppe and desert"),
 "empetrum_nigrum": dict(name="Crowberry", sci="Empetrum nigrum", sprite=sprite_empetrum, regions=["Europe", "N Asia", "N America", "Patagonia and Pampas"],
    biomes=["snowy_plains", "snowy_taiga", "taiga", "windswept_hills", "meadow", "old_growth_pine_taiga", "old_growth_spruce_taiga", "grove"], rarity=2, count=12,
    drops=("crowberries", 1, 2), note="Empetrum 129 sites (Europe 1413 occurrences, N America 55, S America 40, N Asia 4)"),
 "calluna_vulgaris": dict(name="Heather", sci="Calluna vulgaris", sprite=sprite_calluna, regions=["Europe"],
    biomes=["plains", "taiga", "old_growth_pine_taiga", "windswept_hills", "meadow", "forest", "birch_forest", "snowy_taiga"], rarity=2, count=16,
    note="Calluna vulgaris 183 sites, Europe only in the CSV"),
 "hippophae_rhamnoides": dict(name="Sea Buckthorn", sci="Hippophae rhamnoides", sprite=sprite_hippophae, regions=["Europe", "N Asia", "SW Asia", "E/S Asia"],
    biomes=["plains", "meadow", "taiga", "windswept_hills", "old_growth_pine_taiga"], rarity=3, count=8, drops=("sea_buckthorn_berries", 1, 3),
    note="Hippophae rhamnoides 148 sites and Hippophae 128 (Europe, N Asia, SW Asia, E/S Asia)"),
 "saxifraga_oppositifolia": dict(name="Purple Saxifrage", sci="Saxifraga oppositifolia", sprite=sprite_saxifraga, regions=["Europe", "N America", "N Asia"],
    biomes=["snowy_slopes", "stony_peaks", "frozen_peaks", "jagged_peaks", "snowy_plains", "meadow", "grove"], rarity=2, count=12,
    note="Saxifraga oppositifolia-type 102 sites (Europe 490, N America 49, N Asia 19)"),
 "wild_einkorn": dict(name="Wild Einkorn", sci="Triticum monococcum subsp. aegilopoides", sprite=sprite_einkorn, regions=["Fertile Crescent"],
    biomes=DRYLAND, rarity=3, count=14, drops=("wild_einkorn_grain", 1, 3), crop=True,
    note="Wild einkorn: Fertile Crescent hills, Anatolia to the Zagros (Zohary, Hopf and Weiss 2012); SW Asia only"),
 "wild_emmer": dict(name="Wild Emmer", sci="Triticum turgidum subsp. dicoccoides", sprite=sprite_emmer, regions=["Fertile Crescent"],
    biomes=DRYLAND, rarity=3, count=14, drops=("wild_emmer_grain", 1, 3), crop=True,
    note="Wild emmer: Levant, southeast Anatolia and the Zagros arc (Zohary, Hopf and Weiss 2012); SW Asia only"),
 "wild_barley": dict(name="Wild Barley", sci="Hordeum vulgare subsp. spontaneum", sprite=sprite_barley, regions=["SW Asia"],
    biomes=DRYLAND + ["desert"], rarity=3, count=16, drops=("wild_barley_grain", 1, 3), crop=True,
    note="Wild barley: Levant to the Zagros and wider SW Asia (Zohary, Hopf and Weiss 2012); SW Asia only"),
 "wild_lentil": dict(name="Wild Lentil", sci="Lens culinaris subsp. orientalis", sprite=sprite_lentil, regions=["SW Asia"],
    biomes=DRYLAND, rarity=4, count=10, drops=("wild_lentils", 1, 3), crop=True,
    note="Wild lentil: Anatolia, Levant and Iran (Zohary, Hopf and Weiss 2012); SW Asia only"),
 "wild_pea": dict(name="Wild Pea", sci="Pisum sativum subsp. elatius", sprite=sprite_pea, regions=["SW Asia"],
    biomes=DRYLAND, rarity=4, count=10, drops=("wild_peas", 1, 3), crop=True,
    note="Wild pea: Levant, Anatolia and the Zagros foothills (Zohary, Hopf and Weiss 2012); SW Asia only"),
 "wild_chickpea": dict(name="Wild Chickpea", sci="Cicer reticulatum", sprite=sprite_chickpea, regions=["SE Anatolia"],
    biomes=DRYLAND, rarity=4, count=10, drops=("wild_chickpeas", 1, 3), crop=True,
    note="Cicer reticulatum, the wild ancestor, grows only in southeast Anatolia (Zohary, Hopf and Weiss 2012); SW Asia only"),
}

TREES = {
 "cembra_pine": dict(name="Swiss Stone Pine", sci="Pinus cembra", leaves=sprite_leaves_cembra, sapling=sprite_sapling_cembra, log="spruce_log",
    regions=["Alps and Carpathians"], biomes=["taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga", "grove", "snowy_taiga"], rarity=3,
    drop=("cembra_pine_nuts", 0.04), shape="spruce", note="Pinus cembra 84 sites, Europe 1000 occurrences (CSV); the species grows in the Alps and Carpathians, the Siberian stone pine is Pinus sibirica and gets no block"),
 "wild_fig": dict(name="Wild Fig", sci="Ficus carica", leaves=sprite_leaves_fig, sapling=sprite_sapling_fig, log="oak_log",
    regions=["SW Asia"], biomes=["forest", "plains", "savanna", "windswept_savanna", "windswept_hills", "savanna_plateau"], rarity=5,
    drop=("wild_figs", 0.08), shape="oak", note="Ficus-type 19 occurrences in SW Asia (CSV); the common fig is native to SW Asia"),
}

# regions that gen_earth_ranges does not have
G.EXTRA["Fertile Crescent"] = [([31, 40], [34, 48])]
G.EXTRA["SE Anatolia"] = [([36, 39], [37, 42])]
G.EXTRA["Alps and Carpathians"] = [([43, 50], [5, 27])]
G.R = dict(G.REG, **G.EXTRA)

def jdump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2); f.write("\n")

def save_png(im, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)

def state(name, **props):
    d = {"Name": name}
    if props: d["Properties"] = props
    return d

def survives():
    return {"condition": "minecraft:survives_explosion"}

def plant_loot(bid, drops, crop):
    pools = []
    if not crop:
        pools.append({"rolls": 1.0, "bonus_rolls": 0.0, "conditions": [survives()], "entries": [{"type": "minecraft:item", "name": f"{NS}:{bid}"}]})
    if drops:
        item, lo, hi = drops
        pools.append({"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:item", "name": f"{NS}:{item}",
            "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}, "add": False},
                          {"function": "minecraft:explosion_decay"}]}]})
    return {"type": "minecraft:block", "pools": pools, "random_sequence": f"{NS}:blocks/{bid}"}

def leaves_loot(bid, sapling, extra):
    silk = {"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
        {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}
    item, chance = extra
    return {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{bid}", "pools": [
        {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "conditions": [silk], "name": f"{NS}:{bid}"},
            {"type": "minecraft:item", "conditions": [survives(), {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.05, 0.0625, 0.083333336, 0.1]}], "name": f"{NS}:{sapling}"}]}]},
        {"rolls": 1.0, "bonus_rolls": 0.0, "conditions": [{"condition": "minecraft:inverted", "term": silk}], "entries": [
            {"type": "minecraft:item", "conditions": [{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]}],
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1.0, "max": 2.0}, "add": False}, {"function": "minecraft:explosion_decay"}], "name": "minecraft:stick"}]},
        {"rolls": 1.0, "bonus_rolls": 0.0, "conditions": [{"condition": "minecraft:inverted", "term": silk}, {"condition": "minecraft:random_chance", "chance": chance}],
         "entries": [{"type": "minecraft:item", "name": f"{NS}:{item}"}]}]}

def placed(feature, rarity, rid, count=None, spread=None, tree=None):
    pl = [{"type": "minecraft:rarity_filter", "chance": rarity}, {"type": "minecraft:in_square"}]
    if tree:
        pl += [{"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"}]
    else:
        pl += [{"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}]
    pl += [{"type": f"{NS}:earth_region", "id": f"{NS}:{rid}"}, {"type": "minecraft:biome"}]
    if tree:
        pl += [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive", "state": state(f"{NS}:{tree}_sapling")}}]
    else:
        pl += [{"type": "minecraft:count", "count": count},
               {"type": "minecraft:random_offset", "xz_spread": {"type": "minecraft:trapezoid", "min": -7, "max": 7, "plateau": 0}, "y_spread": {"type": "minecraft:trapezoid", "min": -1, "max": 1, "plateau": 0}},
               {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "blocks": ["minecraft:air", "minecraft:short_grass", "minecraft:fern"]}}]
    return {"feature": f"{NS}:{feature}", "placement": pl}

def biome_modifier(feature, biomes):
    return {"type": "neoforge:add_features", "biomes": ["minecraft:" + b for b in biomes], "features": f"{NS}:{feature}", "step": "vegetal_decoration"}

def block_assets(bid, sprite_img, cross=True, leaves=False):
    save_png(sprite_img, os.path.join(ASSETS, "textures", "block", bid + ".png"))
    jdump(os.path.join(ASSETS, "blockstates", bid + ".json"), {"variants": {"": {"model": f"{NS}:block/{bid}"}}})
    if leaves:
        jdump(os.path.join(ASSETS, "models", "block", bid + ".json"), {"parent": "minecraft:block/leaves", "textures": {"all": f"{NS}:block/{bid}"}})
        jdump(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": f"{NS}:block/{bid}"})
        jdump(os.path.join(ASSETS, "items", bid + ".json"), {"model": {"type": "minecraft:model", "model": f"{NS}:block/{bid}"}})
    else:
        jdump(os.path.join(ASSETS, "models", "block", bid + ".json"), {"parent": "minecraft:block/cross", "textures": {"cross": f"{NS}:block/{bid}"}})
        jdump(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:block/{bid}"}})
        jdump(os.path.join(ASSETS, "items", bid + ".json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{bid}"}})

def tree_feature(t, spec):
    log = state("minecraft:" + spec["log"], axis="y")
    leaves = state(f"{NS}:{t}_leaves", distance="7", persistent="false", waterlogged="false")
    below = {"type": "minecraft:rule_based_state_provider", "rules": [{"if_true": {"type": "minecraft:not", "predicate": {"type": "minecraft:matching_block_tag", "tag": "minecraft:cannot_replace_below_tree_trunk"}},
             "then": {"type": "minecraft:simple_state_provider", "state": state("minecraft:dirt")}}]}
    if spec["shape"] == "spruce":
        fol = {"type": "minecraft:spruce_foliage_placer", "offset": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 2},
               "radius": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3}, "trunk_height": {"type": "minecraft:uniform", "min_inclusive": 1, "max_inclusive": 2}}
        trunk = {"type": "minecraft:straight_trunk_placer", "base_height": 6, "height_rand_a": 3, "height_rand_b": 1}
        size = {"type": "minecraft:two_layers_feature_size", "limit": 2, "lower_size": 0, "upper_size": 2}
    else:
        fol = {"type": "minecraft:blob_foliage_placer", "height": 2, "offset": 0, "radius": 2}
        trunk = {"type": "minecraft:straight_trunk_placer", "base_height": 3, "height_rand_a": 1, "height_rand_b": 0}
        size = {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1}
    return {"type": "minecraft:tree", "config": {"below_trunk_provider": below, "decorators": [], "foliage_placer": fol,
            "foliage_provider": {"type": "minecraft:simple_state_provider", "state": leaves}, "ignore_vines": True, "minimum_size": size,
            "trunk_placer": trunk, "trunk_provider": {"type": "minecraft:simple_state_provider", "state": log}}}

def ranges(rid, regions, note, name, sci):
    return {"_comment": f"Natural placement of {name} ({sci}) is limited to the boxes (lat, lon degrees). Generated by tools/gen_flora.py",
            "ranges": [{"entity": f"{NS}:{rid}", "regions": regions, "note": note, "boxes": G.boxes(regions)}]}

def build(jar):
    lang = {}
    block_ids, item_ids = [], []
    for pid, s in P.items():
        im = s["sprite"](jar)
        block_assets(pid, im)
        lang[f"block.{NS}.{pid}"] = f"{s['name']} ({s['sci']})"
        block_ids.append(pid)
        jdump(os.path.join(DATA, "loot_table", "blocks", pid + ".json"), plant_loot(pid, s.get("drops"), s.get("crop")))
        jdump(os.path.join(DATA, "worldgen", "configured_feature", pid + ".json"), {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider", "state": state(f"{NS}:{pid}")}}})
        jdump(os.path.join(DATA, "worldgen", "placed_feature", pid + ".json"), placed(pid, max(1, s["rarity"] - 1), pid, count=s["count"] * 2))
        jdump(os.path.join(DATA, "neoforge", "biome_modifier", pid + ".json"), biome_modifier(pid, s["biomes"]))
        jdump(os.path.join(ROOT, "data", "diaithir", "earth_ranges", f"flora_{pid}.json"), ranges(pid, s["regions"], s["note"], s["name"], s["sci"]))
    for t, s in TREES.items():
        block_assets(f"{t}_leaves", s["leaves"](jar), leaves=True)
        block_assets(f"{t}_sapling", s["sapling"](jar))
        lang[f"block.{NS}.{t}_leaves"] = f"{s['name']} Leaves"
        lang[f"block.{NS}.{t}_sapling"] = f"{s['name']} Sapling ({s['sci']})"
        block_ids += [f"{t}_leaves", f"{t}_sapling"]
        jdump(os.path.join(DATA, "loot_table", "blocks", f"{t}_leaves.json"), leaves_loot(f"{t}_leaves", f"{t}_sapling", s["drop"]))
        jdump(os.path.join(DATA, "loot_table", "blocks", f"{t}_sapling.json"), plant_loot(f"{t}_sapling", None, False))
        jdump(os.path.join(DATA, "worldgen", "configured_feature", t + ".json"), tree_feature(t, s))
        jdump(os.path.join(DATA, "worldgen", "placed_feature", t + ".json"), placed(t, s["rarity"], t, tree=t))
        jdump(os.path.join(DATA, "neoforge", "biome_modifier", t + ".json"), biome_modifier(t, s["biomes"]))
        jdump(os.path.join(ROOT, "data", "diaithir", "earth_ranges", f"flora_{t}.json"), ranges(t, s["regions"], s["note"], s["name"], s["sci"]))
    for iid, (name, spr, nut, sat) in ITEMS.items():
        save_png(spr(jar), os.path.join(ASSETS, "textures", "item", iid + ".png"))
        jdump(os.path.join(ASSETS, "models", "item", iid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
        jdump(os.path.join(ASSETS, "items", iid + ".json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}})
        lang[f"item.{NS}.{iid}"] = name
        item_ids.append(iid)
    lang["itemGroup.dummy"] = None
    del lang["itemGroup.dummy"]
    jdump(os.path.join(ASSETS, "lang", "en_us.json"), dict(sorted(lang.items())))
    # tags: leaves, saplings, and plants trees may grow through
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "block", "leaves.json"), {"values": [f"{NS}:{t}_leaves" for t in TREES]})
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "block", "saplings.json"), {"values": [f"{NS}:{t}_sapling" for t in TREES]})
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "item", "leaves.json"), {"values": [f"{NS}:{t}_leaves" for t in TREES]})
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "item", "saplings.json"), {"values": [f"{NS}:{t}_sapling" for t in TREES]})
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "block", "replaceable_by_trees.json"), {"values": [f"{NS}:{p}" for p in P]})
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable", "axe.json"), {"values": [f"{NS}:{t}_leaves" for t in TREES]})
    jdump(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable", "hoe.json"), {"values": [f"{NS}:{t}_leaves" for t in TREES]})
    return block_ids, item_ids

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--client-jar", default="/root/.gradle/caches/neoformruntime/artifacts/minecraft_26.1.2_client.jar")
    ap.add_argument("--sheet", help="write a contact sheet PNG of all sprites here")
    a = ap.parse_args()
    b, i = build(Jar(a.client_jar))
    print("blocks", len(b), "items", len(i))
    print(json.dumps({"blocks": b, "items": i}))
    if a.sheet:
        names = [os.path.join(ASSETS, "textures", "block", x + ".png") for x in b] + [os.path.join(ASSETS, "textures", "item", x + ".png") for x in i]
        sh = Image.new("RGBA", (len(names) * 70, 70), (90, 150, 200, 255))
        for n, p in enumerate(names):
            sh.paste(Image.open(p).convert("RGBA").resize((64, 64), Image.NEAREST), (n * 70 + 3, 3), Image.open(p).convert("RGBA").resize((64, 64), Image.NEAREST))
        sh.save(a.sheet)
