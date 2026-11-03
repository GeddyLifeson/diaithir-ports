#!/usr/bin/env python3
"""Writes textures (drawn here), models, blockstates, item definitions, lang, loot, recipes, mineable tags and Age gates for the fuel station blocks. Safe to re-run."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.join(HERE, '..', 'src', 'main', 'resources'); NS = 'diaithirunits'
A, D = f'{ROOT}/assets/{NS}', f'{ROOT}/data/{NS}'
rnd = random.Random(5)
BLOCKS = {  # id: (name, age, base colour, kind, recipe)
 'fuel_barrel': ('Fuel Barrel', 2, (120, 84, 48), 'barrel', (['P', 'I', 'P'], {'P': 'minecraft:oak_planks', 'I': 'minecraft:iron_nugget'})),
 'hand_pump': ('Hand Pump', 3, (96, 70, 46), 'pump', (['I', 'P', 'B'], {'I': 'minecraft:iron_ingot', 'P': 'minecraft:piston', 'B': 'minecraft:bucket'})),
 'fuel_tank': ('Fuel Tank', 6, (150, 154, 160), 'tank', (['III', 'IBI', 'III'], {'I': 'minecraft:iron_ingot', 'B': 'minecraft:bucket'})),
 'fuel_pump_station': ('Fuel Pump Station', 6, (190, 40, 36), 'pump', (['III', 'IPI', 'IBI'], {'I': 'minecraft:iron_ingot', 'P': 'minecraft:piston', 'B': 'minecraft:bucket'})),
 'loading_rack': ('Loading Rack', 7, (230, 190, 40), 'pump', (['III', 'PBP', 'III'], {'I': 'minecraft:iron_ingot', 'P': 'minecraft:piston', 'B': 'minecraft:bucket'})),
}

def w(path, d): os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=2) + '\n')

def tex(rid, base, kind):
    im = Image.new('RGB', (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            n = rnd.randint(-8, 8); c = tuple(max(0, min(255, v + n)) for v in base)
            if kind == 'barrel' and y % 5 == 0: c = (60, 60, 64)
            if kind == 'tank' and (x % 8 == 0 or y % 8 == 0): c = (70, 74, 80)
            if kind == 'pump' and (4 <= x <= 11 and 3 <= y <= 7): c = (30, 34, 40)
            if kind == 'pump' and y == 12: c = (40, 40, 44)
            px[x, y] = c
    os.makedirs(f'{A}/textures/block', exist_ok=True); im.save(f'{A}/textures/block/{rid}.png')

lang, gate, pick, axe = {}, {}, [], []
for rid, (name, age, base, kind, (pat, key)) in BLOCKS.items():
    tex(rid, base, kind)
    w(f'{A}/models/block/{rid}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{rid}'}})
    w(f'{A}/blockstates/{rid}.json', {'variants': {'': {'model': f'{NS}:block/{rid}'}}})
    w(f'{A}/items/{rid}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/{rid}'}})
    lang[f'block.{NS}.{rid}'] = name
    w(f'{D}/recipe/{rid}.json', {'type': 'minecraft:crafting_shaped', 'key': key, 'pattern': pat, 'result': {'id': f'{NS}:{rid}', 'count': 1}, 'category': 'misc'})
    w(f'{D}/loot_table/blocks/{rid}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{rid}'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    gate.setdefault(f'age{age}', {'age': age, 'items': []})['items'].append(f'{NS}:{rid}')
    (axe if rid in ('fuel_barrel', 'hand_pump') else pick).append(f'{NS}:{rid}')
w(f'{A}/lang/en_us.json', lang); w(f'{D}/age_gate.json', gate)
w(f'{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json', {'values': pick}); w(f'{ROOT}/data/minecraft/tags/block/mineable/axe.json', {'values': axe})
print('station assets written')
