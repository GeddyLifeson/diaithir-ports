#!/usr/bin/env python3
"""Writes the original 16x16 textures, models, blockstates, item definitions and loot tables for diaithir-chunkloaders.
Run from the module root: python3 tools/gen_assets.py. All art is drawn here from flat colours (no source images)."""
import json, os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "diaithirchunkloaders"
TIERS = {1: ("velhorn", (222, 176, 40)), 2: ("marduvain", (204, 72, 52)), 3: ("ixthuran", (70, 150, 220))}
STEEL, STEEL_L, STEEL_D, RIVET, BLACK = (74, 80, 86), (112, 120, 128), (42, 46, 51), (150, 156, 160), (24, 26, 29)


def new(): return Image.new("RGBA", (16, 16), STEEL + (255,))


def noise(img, seed):
    r = random.Random(seed)
    for x in range(16):
        for y in range(16):
            if r.random() < 0.18:
                p = img.getpixel((x, y)); d = r.choice((-7, 6))
                img.putpixel((x, y), tuple(max(0, min(255, c + d)) for c in p[:3]) + (255,))


def frame(img):
    for i in range(16):
        for a, b in ((i, 0), (i, 15), (0, i), (15, i)): img.putpixel((a, b), STEEL_D + (255,))
        for a, b in ((i, 1), (1, i)):
            if 1 <= i <= 14: img.putpixel((a, b), STEEL_L + (255,))
    for a, b in ((2, 2), (13, 2), (2, 13), (13, 13)): img.putpixel((a, b), RIVET + (255,))


def side(tier, lit):
    name, col = TIERS[tier]
    img = new(); noise(img, tier); frame(img)
    for x in range(2, 14):  # hazard band, diagonal black and accent stripes
        for y in range(6, 10):
            img.putpixel((x, y), (col if (x + y) % 4 < 2 else BLACK) + (255,))
    for y in (5, 10):
        for x in range(2, 14): img.putpixel((x, y), STEEL_D + (255,))
    for x in range(4, 12):  # vent slits, glowing when the loader burns
        for y in (12, 13):
            glow = tuple(min(255, c + 30) for c in col)
            img.putpixel((x, y), (glow if lit and x % 2 == 0 else BLACK) + (255,))
    for x in range(4, 12, 2):
        img.putpixel((x, 3), (col if lit else STEEL_D) + (255,))
    return img


def top(tier, lit):
    name, col = TIERS[tier]
    img = new(); noise(img, 10 + tier); frame(img)
    for i in range(4, 12):
        for a, b in ((i, 4), (i, 11), (4, i), (11, i)): img.putpixel((a, b), STEEL_D + (255,))
    for x in range(5, 11):
        for y in range(5, 11): img.putpixel((x, y), BLACK + (255,))
    c = tuple(min(255, v + 35) for v in col) if lit else tuple(v // 3 for v in col)
    for x in range(6, 10):
        for y in range(6, 10): img.putpixel((x, y), c + (255,))
    if lit:
        for x, y in ((5, 7), (10, 8), (7, 5), (8, 10)): img.putpixel((x, y), tuple(v // 2 for v in col) + (255,))
    return img


def seal():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); col = TIERS[2][1]
    for x in range(16):
        for y in range(16):
            d = (x - 7.5) ** 2 + (y - 8.5) ** 2
            if d <= 30: img.putpixel((x, y), (STEEL_L if d > 20 else STEEL) + (255,))
            if d <= 9: img.putpixel((x, y), (col if (x + y) % 4 < 2 else BLACK) + (255,))
    for x, y in ((7, 1), (8, 1), (6, 2), (9, 2), (6, 3), (9, 3)): img.putpixel((x, y), RIVET + (255,))
    return img


def write(path, data):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(data, f, indent=2); f.write("\n")


tex = os.path.join(ROOT, "assets", NS, "textures")
os.makedirs(os.path.join(tex, "block"), exist_ok=True); os.makedirs(os.path.join(tex, "item"), exist_ok=True)
for t, (name, _) in TIERS.items():
    for lit in (False, True):
        sfx = "_lit" if lit else ""
        side(t, lit).save(os.path.join(tex, "block", f"loader_{t}_side{sfx}.png"))
        top(t, lit).save(os.path.join(tex, "block", f"loader_{t}_top{sfx}.png"))
        write(f"assets/{NS}/models/block/loader_{t}{sfx}.json", {"parent": "minecraft:block/cube_column", "textures": {
            "end": f"{NS}:block/loader_{t}_top{sfx}", "side": f"{NS}:block/loader_{t}_side{sfx}", "particle": f"{NS}:block/loader_{t}_side"}})
    write(f"assets/{NS}/blockstates/chunk_loader_{t}.json", {"variants": {
        "lit=false": {"model": f"{NS}:block/loader_{t}"}, "lit=true": {"model": f"{NS}:block/loader_{t}_lit"}}})
    write(f"assets/{NS}/items/chunk_loader_{t}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/loader_{t}"}})
    write(f"data/{NS}/loot_table/blocks/chunk_loader_{t}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{NS}:chunk_loader_{t}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
seal().save(os.path.join(tex, "item", "cart_seal.png"))
write(f"assets/{NS}/models/item/cart_seal.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/cart_seal"}})
write(f"assets/{NS}/items/cart_seal.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/cart_seal"}})
write("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:chunk_loader_{t}" for t in TIERS]})
write("data/minecraft/tags/block/needs_stone_tool.json", {"values": [f"{NS}:chunk_loader_{t}" for t in TIERS]})
