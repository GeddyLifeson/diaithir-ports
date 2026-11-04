"""Writes textures, models, blockstates, loot, crafting and the sample brews for diaithir-brewing. Run: python3 -I tools/gen_assets.py"""
import json
import os
import random
from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "diaithirbrewing"
S = 64


def put(path, obj):
    path = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=1)


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c)


def wood(tone, seed):
    """Oak staves: vertical planks, grain streaks, dark seams."""
    r = random.Random(seed)
    img = Image.new("RGB", (S, S))
    px = img.load()
    stave = S // 8
    base = [shade(tone, r.uniform(0.85, 1.12)) for _ in range(9)]
    for x in range(S):
        for y in range(S):
            k = x // stave
            grain = 0.93 + 0.14 * r.random() + 0.06 * ((y + k * 7) % 11 == 0)
            c = shade(base[k], grain)
            if x % stave == 0:
                c = shade(c, 0.55)
            px[x, y] = c
    return img


def band(img, y0, h, iron=(70, 72, 78)):
    px = img.load()
    r = random.Random(y0)
    for x in range(S):
        for y in range(y0, y0 + h):
            edge = 1.25 if y == y0 else 0.7 if y == y0 + h - 1 else 1.0
            px[x, y] = shade(iron, edge * r.uniform(0.9, 1.1))
    for x in range(4, S, 16):
        for dy in (-1, 0, 1):
            for dx in (-1, 0, 1):
                px[x + dx, y0 + h // 2 + dy] = shade(iron, 1.5 if dx + dy < 0 else 0.5)


def make(name, tone, bands):
    side = wood(tone, 1)
    for y in bands:
        band(side, y, 5)
    side.save(os.path.join(ROOT, f"assets/{NS}/textures/block/{name}_side.png"))
    top = wood(tone, 2)
    px = top.load()
    c = S / 2 - 0.5
    for x in range(S):
        for y in range(S):
            d = ((x - c) ** 2 + (y - c) ** 2) ** 0.5
            if d > S / 2 - 4:
                px[x, y] = shade((70, 72, 78), 1.2 if d < S / 2 - 2 else 0.8)
            elif abs(d - 9) < 1:
                px[x, y] = shade(px[x, y], 0.7)
    top.save(os.path.join(ROOT, f"assets/{NS}/textures/block/{name}_top.png"))
    bottom = wood(shade(tone, 0.8), 3)
    bottom.save(os.path.join(ROOT, f"assets/{NS}/textures/block/{name}_bottom.png"))


def steel(seed, rim=False):
    """Worn dirty steel plate with rivets (Factorio look); rim=True draws the open pot top with dark water."""
    r = random.Random(seed)
    img = Image.new("RGB", (S, S))
    px = img.load()
    for x in range(S):
        for y in range(S):
            px[x, y] = shade((92, 96, 102), r.uniform(0.85, 1.1) * (0.9 if (x // 16 + y // 16) % 2 else 1.0))
            if x % 16 in (0, 15) or y % 16 in (0, 15):
                px[x, y] = shade(px[x, y], 0.6)
    for x in range(8, S, 16):
        for y in range(8, S, 16):
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    px[x + dx, y + dy] = shade((92, 96, 102), 1.4 if dx + dy < 0 else 0.55)
    for _ in range(90):
        px[r.randrange(S), r.randrange(S)] = (120 + r.randrange(40), 70, 40)
    if rim:
        c = S / 2 - 0.5
        for x in range(S):
            for y in range(S):
                d = ((x - c) ** 2 + (y - c) ** 2) ** 0.5
                if d < S / 2 - 6:
                    px[x, y] = shade((38, 62, 74), r.uniform(0.9, 1.1))
                elif d < S / 2 - 4:
                    px[x, y] = shade((60, 62, 66), 0.8)
    return img


def jar(contents):
    """16x16-style item art drawn at 64 px: glass jar, steel lid, coloured contents (None = empty)."""
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    px = img.load()
    for x in range(16, 48):
        for y in range(18, 56):
            edge = x in (16, 47) or y in (18, 55)
            px[x, y] = (200, 225, 235, 255) if edge else ((contents + (255,)) if contents and y > 26 else (170, 205, 220, 90))
    for x in range(14, 50):
        for y in range(8, 18):
            px[x, y] = shade((92, 96, 102), 1.2 if y == 8 else 0.8 if y == 17 else 1.0) + (255,)
    return img


os.makedirs(os.path.join(ROOT, f"assets/{NS}/textures/block"), exist_ok=True)
os.makedirs(os.path.join(ROOT, f"assets/{NS}/textures/item"), exist_ok=True)
CANNED = {"jar": None, "canned_meat": (150, 60, 45), "canned_fish": (190, 130, 90), "canned_vegetables": (90, 150, 60), "canned_fruit": (200, 60, 90)}
for n, col in CANNED.items():
    jar(col).save(os.path.join(ROOT, f"assets/{NS}/textures/item/{n}.png"))
    put(f"assets/{NS}/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}"}})
    put(f"assets/{NS}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
steel(4).save(os.path.join(ROOT, f"assets/{NS}/textures/block/canning_pot_side.png"))
steel(5, rim=True).save(os.path.join(ROOT, f"assets/{NS}/textures/block/canning_pot_top.png"))
steel(6).save(os.path.join(ROOT, f"assets/{NS}/textures/block/canning_pot_bottom.png"))
make("keg", (150, 105, 60), (8, 51))
make("fermenting_barrel", (112, 78, 46), (6, 29, 53))
# oak_cask art comes from the textures thread (64 px); only generate a placeholder if it is missing
if not os.path.exists(os.path.join(ROOT, f"assets/{NS}/textures/block/oak_cask_side.png")):
    make("oak_cask", (170, 135, 82), (10, 27, 46))

# (Diaithiris, English)
NAMES = {"keg": ("Boidellienda", "Keg"), "fermenting_barrel": ("Tundagrùduiri", "Fermenting Barrel"), "oak_cask": ("", "Oak Cask"), "canning_pot": ("", "Canning Pot")}
lang = {}
for n, (dia, _en) in NAMES.items():
    lang[f"block.{NS}.{n}"] = _en  # English main name; the Diaithiris name is shown in grey by the pack (assets/diaithirbrewing/diaithir_names/names.json, written by the names thread)
    put(f"assets/{NS}/models/block/{n}.json", {
        "parent": "minecraft:block/cube_bottom_top",
        "textures": {"bottom": f"{NS}:block/{n}_bottom", "side": f"{NS}:block/{n}_side", "top": f"{NS}:block/{n}_top"}})
    put(f"assets/{NS}/blockstates/{n}.json", {"variants": {"": {"model": f"{NS}:block/{n}"}}})
    put(f"assets/{NS}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{n}"}})
    put(f"data/{NS}/loot_table/blocks/{n}.json", {
        "type": "minecraft:block", "random_sequence": f"{NS}:blocks/{n}",
        "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}],
                   "entries": [{"type": "minecraft:item", "name": f"{NS}:{n}"}]}]})
for n, en in {"jar": "Glass Jar", "canned_meat": "Canned Meat", "canned_fish": "Canned Fish", "canned_vegetables": "Canned Vegetables", "canned_fruit": "Canned Fruit"}.items():
    lang[f"item.{NS}.{n}"] = en
lang[f"block.{NS}.ready"] = "%2$s x %1$s ready to draw"
lang[f"block.{NS}.fermenting"] = "Fermenting: %s%% (cellar %s/%s)"
lang[f"block.{NS}.filling"] = "Contents: %s / %s"
put(f"assets/{NS}/lang/en_us.json", lang)

put(f"data/{NS}/recipe/keg.json", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "key": {"P": "#minecraft:planks", "I": "minecraft:iron_ingot"},
    "pattern": ["PIP", "P P", "PIP"], "result": {"id": f"{NS}:keg"}})
put(f"data/{NS}/recipe/fermenting_barrel.json", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "key": {"P": "#minecraft:planks", "I": "minecraft:iron_ingot", "S": "#minecraft:wooden_slabs"},
    "pattern": ["PIP", "PSP", "PIP"], "result": {"id": f"{NS}:fermenting_barrel"}})
put(f"data/{NS}/recipe/oak_cask.json", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "key": {"P": "minecraft:oak_planks", "I": "minecraft:iron_ingot", "B": f"{NS}:fermenting_barrel"},
    "pattern": ["PIP", "PBP", "PIP"], "result": {"id": f"{NS}:oak_cask"}})
put(f"data/{NS}/recipe/canning_pot.json", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "key": {"I": "minecraft:iron_ingot", "C": "minecraft:cauldron"},
    "pattern": ["I I", "ICI", "III"], "result": {"id": f"{NS}:canning_pot"}})
put(f"data/{NS}/recipe/jar.json", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "key": {"G": "minecraft:glass", "I": "minecraft:iron_nugget"},
    "pattern": ["I", "G", "G"], "result": {"id": f"{NS}:jar", "count": 4}})
put(f"data/minecraft/tags/block/mineable/axe.json", {"values": [f"{NS}:keg", f"{NS}:fermenting_barrel", f"{NS}:oak_cask"]})
put(f"data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:canning_pot"]})
put(f"data/{NS}/tags/item/preserved.json", {"values": [f"{NS}:{n}" for n in CANNED if n != "jar"]})

# Canning recipes ship in the jar: a bucket of water (brine or syrup, bucket returned), the food, and jars; heat from below. food.canning, Age V.
for n, ing, ticks in (("canned_meat", {"minecraft:cooked_beef": 3}, 1200), ("canned_fish", {"minecraft:cooked_cod": 3}, 1200),
                      ("canned_vegetables", {"minecraft:carrot": 2, "minecraft:potato": 2}, 900), ("canned_fruit", {"minecraft:apple": 3, "minecraft:sugar": 1}, 900)):
    put(f"data/{NS}/{NS}/{n}.json", {"station": "canning_pot", "base": "minecraft:water_bucket", "base_returns": "minecraft:bucket",
        "ingredients": ing, "ticks": ticks, "drink": f"{NS}:{n}", "servings": 4, "container": f"{NS}:jar"})
put(f"data/{NS}/age_gate.json", {"tier1": {"age": 5, "items": [f"{NS}:canning_pot", f"{NS}:jar", f"{NS}:canned_meat", f"{NS}:canned_fish", f"{NS}:canned_vegetables", f"{NS}:canned_fruit"]}})

# Sample brew in the data format (the alcohol thread writes the real catalogue the same way); kept out of the jar.
put("../../../tools/sample/data/diaithirbrewing/diaithirbrewing/selftest.json", {
    "station": "keg", "base": "minecraft:water_bucket", "base_returns": "minecraft:bucket",
    "ingredients": {"minecraft:wheat": 3}, "ticks": 100, "drink": "minecraft:honey_bottle", "servings": 4, "abv": 5.0})

# Ageing sample: a cask takes finished young drinks as its ingredients and gives the aged drink (no base).
put("../../../tools/sample/data/diaithirbrewing/diaithirbrewing/selftest_aged.json", {
    "station": "oak_cask", "ingredients": {"minecraft:honey_bottle": 4}, "ticks": 600, "drink": "minecraft:honey_bottle", "servings": 4, "abv": 12.5})
