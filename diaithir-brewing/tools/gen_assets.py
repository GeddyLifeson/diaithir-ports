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


os.makedirs(os.path.join(ROOT, f"assets/{NS}/textures/block"), exist_ok=True)
make("keg", (150, 105, 60), (8, 51))
make("fermenting_barrel", (112, 78, 46), (6, 29, 53))
# oak_cask art comes from the textures thread (64 px); only generate a placeholder if it is missing
if not os.path.exists(os.path.join(ROOT, f"assets/{NS}/textures/block/oak_cask_side.png")):
    make("oak_cask", (170, 135, 82), (10, 27, 46))

# (Diaithiris, English)
NAMES = {"keg": ("Boidellienda", "Keg"), "fermenting_barrel": ("Tundagrùduiri", "Fermenting Barrel"), "oak_cask": ("", "Oak Cask")}
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
put(f"data/minecraft/tags/block/mineable/axe.json", {"values": [f"{NS}:keg", f"{NS}:fermenting_barrel", f"{NS}:oak_cask"]})

# Sample brew in the data format (the alcohol thread writes the real catalogue the same way); kept out of the jar.
put("../../../tools/sample/data/diaithirbrewing/diaithirbrewing/selftest.json", {
    "station": "keg", "base": "minecraft:water_bucket", "base_returns": "minecraft:bucket",
    "ingredients": {"minecraft:wheat": 3}, "ticks": 100, "drink": "minecraft:honey_bottle", "servings": 4})

# Ageing sample: a cask takes finished young drinks as its ingredients and gives the aged drink (no base).
put("../../../tools/sample/data/diaithirbrewing/diaithirbrewing/selftest_aged.json", {
    "station": "oak_cask", "ingredients": {"minecraft:honey_bottle": 4}, "ticks": 600, "drink": "minecraft:honey_bottle", "servings": 4})
