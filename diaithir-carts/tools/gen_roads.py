#!/usr/bin/env python3
"""Adds the six road surfaces (vehicles/roads.csv) to diaithir-carts: 16x16 textures drawn here, cube models, blockstates, item definitions,
English lang, recipes, loot, mineable tags and Age gates. Safe to re-run. Usage: python3 -I tools/gen_roads.py"""
import csv, json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.join(HERE, '..', 'src', 'main', 'resources'); NS = 'diaithircarts'
A, D = f'{ROOT}/assets/{NS}', f'{ROOT}/data/{NS}'
rnd = random.Random(11)
AGE = {'I': 1, 'II': 2, 'III': 3, 'IV': 4, 'V': 5, 'VI': 6, 'VII': 7}
roads = list(csv.DictReader(open(f'{HERE}/../vehicles/roads.csv')))

def w(path, d): os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=2) + '\n')

def tex(rid):
    im = Image.new('RGB', (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            n = rnd.randint(-10, 10)
            if rid == 'dirt_track': c = (112, 84, 56); c = (c[0] + n, c[1] + n, c[2] + n // 2) if (x + y) % 5 else (96, 70, 46)
            elif rid == 'gravel_road': g = rnd.choice((120, 140, 100, 160)); c = (g, g - 4, g - 10)
            elif rid == 'cobbled_road':
                edge = x % 8 in (0,) or (y % 8 == 0) or ((x + 4) % 8 == 0 and (y // 8) % 2 == 1) or (x % 8 == 4 and False)
                c = (66, 62, 58) if (x % 8 == 0 or y % 8 == 0) else (126 + n, 122 + n, 116 + n)
            elif rid == 'macadam_road': g = 86 + n * 2; c = (g, g, g + 2)
            elif rid == 'asphalt_road': g = 36 + n // 2; c = (g, g, g + 2)
            else:
                g = 168 + n; c = (g, g, g - 2)
                if x == 0 or y == 0: c = (120, 120, 118)
            px[x, y] = tuple(max(0, min(255, v)) for v in c)
    os.makedirs(f'{A}/textures/block', exist_ok=True); im.save(f'{A}/textures/block/{rid}.png')

lang = json.load(open(f'{A}/lang/en_us.json')); gate = json.load(open(f'{D}/age_gate.json'))
pick = json.load(open(f'{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json')); shovel = []
RECIPES = {  # placeholder recipes from vanilla items; Foundry/tech chains replace the inputs later
 'dirt_track': (['DD', 'DD'], {'D': 'minecraft:dirt'}), 'gravel_road': (['GG', 'GG'], {'G': 'minecraft:gravel'}),
 'cobbled_road': (['CC', 'CC'], {'C': 'minecraft:cobblestone'}), 'macadam_road': (['GC', 'CG'], {'G': 'minecraft:gravel', 'C': 'minecraft:cobblestone'}),
 'asphalt_road': (['GK', 'KG'], {'G': 'minecraft:gravel', 'K': 'minecraft:coal'}), 'concrete_motorway': (['CC', 'CC'], {'C': 'minecraft:gray_concrete'})}
ages = {}
for r in roads:
    rid = r['id']; tex(rid)
    w(f'{A}/models/block/{rid}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{rid}'}})
    w(f'{A}/blockstates/{rid}.json', {'variants': {'': {'model': f'{NS}:block/{rid}'}}})
    w(f'{A}/items/{rid}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/{rid}'}})
    lang[f'block.{NS}.{rid}'] = r['name']
    pat, key = RECIPES[rid]
    w(f'{D}/recipe/{rid}.json', {'type': 'minecraft:crafting_shaped', 'key': key, 'pattern': pat, 'result': {'id': f'{NS}:{rid}', 'count': 4}, 'category': 'building'})
    w(f'{D}/loot_table/blocks/{rid}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{rid}'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    ages.setdefault(AGE[r['age']], []).append(f'{NS}:{rid}')
    (shovel if rid in ('dirt_track', 'gravel_road') else pick['values']).append(f'{NS}:{rid}') if f'{NS}:{rid}' not in pick['values'] else None
for a, items in ages.items(): gate[f'roads_age{a}'] = {'age': a, 'items': items}
w(f'{A}/lang/en_us.json', lang); w(f'{D}/age_gate.json', gate); w(f'{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json', pick)
w(f'{ROOT}/data/minecraft/tags/block/mineable/shovel.json', {'values': shovel})
w(f'{HERE}/../vehicles/road_speed.json', {r['id']: float(r['speed_factor']) for r in roads})
print('roads written', len(roads))
