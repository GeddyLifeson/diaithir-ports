#!/usr/bin/env python3
"""Writes the textures, models, lang, recipe, loot, tags and age gate of diaithir-computing (S-id claimed in Global-Repo)."""
import json, os, random
from PIL import Image
R = os.path.join(os.path.dirname(__file__), '..', 'src/main/resources')
ID = 'diaithircomputing'
def w(path, data):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w') as f: json.dump(data, f, indent=1)
def tex(name, lit):
    random.seed(name)
    im = Image.new('RGB', (64, 64))
    for x in range(64):
        for y in range(64):
            g = 78 + random.randint(-6, 6)
            im.putpixel((x, y), (g, g + 4, g + 10))
    for i in range(64):   # riveted frame
        for t in range(3):
            for xy in ((i, t), (i, 63 - t), (t, i), (63 - t, i)): im.putpixel(xy, (40, 44, 52))
    col = (255, 190, 60) if lit else (90, 150, 110)
    for row in range(10, 54, 6):   # a screen of text lines
        for x in range(10, 54):
            if (x * 7 + row * 3) % 11 < 8 and x < 54 - (row * 5) % 17: im.putpixel((x, row), col); im.putpixel((x, row + 1), col)
    p = os.path.join(R, f'assets/{ID}/textures/block/{name}.png'); os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p)
tex('reckoner', False); tex('reckoner_on', True)
for n in ('reckoner', 'reckoner_on'):
    w(f'assets/{ID}/models/block/{n}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{ID}:block/{n}'}})
w(f'assets/{ID}/blockstates/reckoner.json', {'variants': {'powered=false': {'model': f'{ID}:block/reckoner'}, 'powered=true': {'model': f'{ID}:block/reckoner_on'}}})
w(f'assets/{ID}/items/reckoner.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/reckoner'}})
w(f'assets/{ID}/lang/en_us.json', {f'block.{ID}.reckoner': 'Reckoner'})
w(f'data/{ID}/recipe/reckoner.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['IGI', 'RBR', 'III'],
  'key': {'I': 'minecraft:iron_ingot', 'G': 'minecraft:glass_pane', 'R': 'minecraft:redstone', 'B': 'minecraft:writable_book'},
  'result': {'id': f'{ID}:reckoner', 'count': 1}})
w(f'data/{ID}/loot_table/blocks/reckoner.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:reckoner'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w('data/minecraft/tags/block/mineable/pickaxe.json', {'values': [f'{ID}:reckoner']})
w('data/minecraft/tags/block/needs_stone_tool.json', {'values': [f'{ID}:reckoner']})
w(f'data/{ID}/age_gate.json', {'reckoner': {'age': 6, 'items': [f'{ID}:reckoner']}})

# ---- Wayfarer (turtle): orientable block, front shows a lit visor
def turtle_tex(name, kind):
    random.seed(name); im = Image.new('RGB', (64, 64))
    for x in range(64):
        for y in range(64):
            g = 92 + random.randint(-6, 6); im.putpixel((x, y), (g + 18, g + 6, g - 14))
    for i in range(64):
        for t in range(3):
            for xy in ((i, t), (i, 63 - t), (t, i), (63 - t, i)): im.putpixel(xy, (50, 38, 22))
    if kind == 'front':
        for x in range(12, 52):
            for y in range(20, 36): im.putpixel((x, y), (255, 190, 60) if (x + y) % 9 else (255, 230, 140))
    p = os.path.join(R, f'assets/{ID}/textures/block/{name}.png'); im.save(p)
for n, k in (('turtle_side', 'side'), ('turtle_top', 'top'), ('turtle_front', 'front')): turtle_tex(n, k)
w(f'assets/{ID}/models/block/turtle.json', {'parent': 'minecraft:block/orientable', 'textures': {'top': f'{ID}:block/turtle_top', 'front': f'{ID}:block/turtle_front', 'side': f'{ID}:block/turtle_side'}})
w(f'assets/{ID}/blockstates/turtle.json', {'variants': {'facing=north': {'model': f'{ID}:block/turtle'}, 'facing=east': {'model': f'{ID}:block/turtle', 'y': 90},
  'facing=south': {'model': f'{ID}:block/turtle', 'y': 180}, 'facing=west': {'model': f'{ID}:block/turtle', 'y': 270}}})
w(f'assets/{ID}/items/turtle.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/turtle'}})
lp = os.path.join(R, f'assets/{ID}/lang/en_us.json'); lang = json.load(open(lp)); lang[f'block.{ID}.turtle'] = 'Wayfarer'; json.dump(lang, open(lp, 'w'), indent=1)
w(f'data/{ID}/recipe/turtle.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['IPI', 'ICI', 'IBI'],
  'key': {'I': 'minecraft:iron_ingot', 'P': 'minecraft:iron_pickaxe', 'C': f'{ID}:reckoner', 'B': 'minecraft:chest'}, 'result': {'id': f'{ID}:turtle', 'count': 1}})
w(f'data/{ID}/loot_table/blocks/turtle.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:turtle'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w('data/minecraft/tags/block/mineable/pickaxe.json', {'values': [f'{ID}:reckoner', f'{ID}:turtle']})
w('data/minecraft/tags/block/needs_stone_tool.json', {'values': [f'{ID}:reckoner', f'{ID}:turtle']})
w(f'data/{ID}/age_gate.json', {'reckoner': {'age': 6, 'items': [f'{ID}:reckoner']}, 'turtle': {'age': 7, 'items': [f'{ID}:turtle']}})
