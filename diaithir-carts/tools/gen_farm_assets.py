#!/usr/bin/env python3
"""(S6940) Assets and data of the reaper block (adds to the lang, age gate, tags and library files the other generators write). Run: python3 -I tools/gen_farm_assets.py"""
import json, os, random
from PIL import Image
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'); R = os.path.join(ROOT, 'src/main/resources'); ID = 'diaithircarts'
S = 64
def w(path, data):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(data, open(p, 'w'), indent=1)
def rj(path):
    return json.load(open(os.path.join(R, path)))
def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c)
def plate(seed, front=False):
    """Worn dirty steel with rivets and rust; front gets the cutter bar: a row of teeth under a hazard-yellow stripe."""
    r = random.Random(seed); im = Image.new('RGB', (S, S)); px = im.load()
    for x in range(S):
        for y in range(S):
            px[x, y] = shade((92, 96, 102), r.uniform(0.85, 1.1))
            if x % 32 in (0, 31) or y % 32 in (0, 31): px[x, y] = shade(px[x, y], 0.6)
    for x in range(8, S, 16):
        for y in (8, S - 9):
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1): px[x + dx, y + dy] = shade((92, 96, 102), 1.4 if dx + dy < 0 else 0.55)
    for _ in range(80): px[r.randrange(S), r.randrange(S)] = (120 + r.randrange(40), 70, 40)
    if front:
        for x in range(S):
            for y in range(14, 22): px[x, y] = (230, 185, 30) if (x + y) // 6 % 2 == 0 else (35, 35, 35)
            tooth = (x % 8) * (1 if (x // 8) % 2 == 0 else -1)
            for y in range(36, 56):
                if y - 36 < 20 - abs((x % 8) - 4) * 4: px[x, y] = shade((150, 154, 160), 1.2 if (x % 8) < 4 else 0.8)
    return im
TEX = os.path.join(R, f'assets/{ID}/textures/block'); os.makedirs(TEX, exist_ok=True)
plate(11, True).save(f'{TEX}/reaper_front.png'); plate(12).save(f'{TEX}/reaper_side.png'); plate(13).save(f'{TEX}/reaper_top.png')
w(f'assets/{ID}/models/block/reaper.json', {'parent': 'minecraft:block/orientable', 'textures': {'front': f'{ID}:block/reaper_front', 'side': f'{ID}:block/reaper_side', 'top': f'{ID}:block/reaper_top'}})
w(f'assets/{ID}/blockstates/reaper.json', {'variants': {f'facing={f}': {'model': f'{ID}:block/reaper', 'y': y} for f, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270))}})
w(f'assets/{ID}/items/reaper.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/reaper'}})
w(f'data/{ID}/loot_table/blocks/reaper.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:reaper'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w(f'data/{ID}/recipe/reaper.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['SSS', 'IHI', 'ICI'],
  'key': {'S': 'minecraft:shears', 'I': 'minecraft:iron_ingot', 'H': 'minecraft:hopper', 'C': 'minecraft:copper_ingot'}, 'result': {'id': f'{ID}:reaper', 'count': 1}})
for rel in ('data/minecraft/tags/block/mineable/pickaxe.json', 'data/minecraft/tags/block/needs_iron_tool.json'):
    t = rj(rel)
    if f'{ID}:reaper' not in t['values']: t['values'].append(f'{ID}:reaper')
    w(rel, t)
lp = f'assets/{ID}/lang/en_us.json'; l = rj(lp); l[f'block.{ID}.reaper'] = 'Reaper'; w(lp, l)
ap = f'data/{ID}/age_gate.json'; a = rj(ap); a['farm'] = {'age': 5, 'items': [f'{ID}:reaper']}; w(ap, a)
cp = os.path.join(ROOT, '..', 'library-sync/diaithircarts.json'); c = json.load(open(cp))
if not any(e['id'] == f'{ID}:reaper' for e in c):
    c.append({'id': f'{ID}:reaper', 'name': 'Reaper', 'text': 'A steel cutter bar for the edge of a field. Every 2 seconds it cuts every fully grown crop in the strip in front of it (5 wide, 5 deep, at its own height), replants each at the first growth stage, and pushes the harvest into the chest, barrel, hopper or storage port behind it; with no room it spills the harvest on the ground. Use it bare-handed to turn it. Redstone switches it off. Age V.'})
json.dump(c, open(cp, 'w'), indent=1)
