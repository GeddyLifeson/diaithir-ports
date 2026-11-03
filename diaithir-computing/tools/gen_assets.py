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

# ---- Cable, Disk Drive, Disk
def plain(name, base, accent):
    random.seed(name)
    im = Image.new('RGB', (64, 64))
    for x in range(64):
        for y in range(64):
            g = base + random.randint(-5, 5); im.putpixel((x, y), (g, g + 3, g + 8))
    for i in range(64):
        for t in range(3):
            for xy in ((i, t), (i, 63 - t), (t, i), (63 - t, i)): im.putpixel(xy, (36, 40, 48))
    if accent:
        for x in range(12, 52):
            for y in range(26, 32): im.putpixel((x, y), (20, 20, 24))   # the disk slot
        for x in range(46, 52): im.putpixel((x, 44), accent); im.putpixel((x, 45), accent)
    p = os.path.join(R, f'assets/{ID}/textures/block/{name}.png'); os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p)
plain('cable', 110, None); plain('disk_drive', 74, (90, 220, 120))
w(f'assets/{ID}/models/block/cable.json', {'parent': 'minecraft:block/block', 'textures': {'all': f'{ID}:block/cable', 'particle': f'{ID}:block/cable'},
  'elements': [{'from': [4, 4, 4], 'to': [12, 12, 12], 'faces': {d: {'texture': '#all'} for d in ('north', 'east', 'south', 'west', 'up', 'down')}}]})
w(f'assets/{ID}/blockstates/cable.json', {'variants': {'': {'model': f'{ID}:block/cable'}}})
w(f'assets/{ID}/items/cable.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/cable'}})
w(f'assets/{ID}/models/block/disk_drive.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{ID}:block/disk_drive'}})
w(f'assets/{ID}/blockstates/disk_drive.json', {'variants': {'': {'model': f'{ID}:block/disk_drive'}}})
w(f'assets/{ID}/items/disk_drive.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/disk_drive'}})
im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
for x in range(4, 28):
    for y in range(4, 28): im.putpixel((x, y), (60, 80, 140, 255))
for x in range(10, 22):
    for y in range(4, 12): im.putpixel((x, y), (200, 200, 210, 255))
for x in range(9, 23):
    for y in range(16, 26): im.putpixel((x, y), (235, 235, 225, 255))
p = os.path.join(R, f'assets/{ID}/textures/item/disk.png'); os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p)
w(f'assets/{ID}/models/item/disk.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{ID}:item/disk'}})
w(f'assets/{ID}/items/disk.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:item/disk'}})
lang.update({f'block.{ID}.cable': 'Cable', f'block.{ID}.disk_drive': 'Disk Drive', f'item.{ID}.disk': 'Disk'})
json.dump(lang, open(lp, 'w'), indent=1)
w(f'data/{ID}/recipe/cable.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['III', 'RRR', 'III'],
  'key': {'I': 'minecraft:iron_nugget', 'R': 'minecraft:redstone'}, 'result': {'id': f'{ID}:cable', 'count': 6}})
w(f'data/{ID}/recipe/disk_drive.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['III', 'IHI', 'IRI'],
  'key': {'I': 'minecraft:iron_ingot', 'H': 'minecraft:hopper', 'R': 'minecraft:redstone'}, 'result': {'id': f'{ID}:disk_drive', 'count': 1}})
w(f'data/{ID}/recipe/disk.json', {'type': 'minecraft:crafting_shapeless', 'category': 'redstone',
  'ingredients': ['minecraft:paper', 'minecraft:redstone', 'minecraft:iron_nugget'], 'result': {'id': f'{ID}:disk', 'count': 1}})
for n in ('cable', 'disk_drive'):
    w(f'data/{ID}/loot_table/blocks/{n}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:{n}'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w('data/minecraft/tags/block/mineable/pickaxe.json', {'values': [f'{ID}:reckoner', f'{ID}:turtle', f'{ID}:cable', f'{ID}:disk_drive']})
w('data/minecraft/tags/block/needs_stone_tool.json', {'values': [f'{ID}:reckoner', f'{ID}:turtle', f'{ID}:disk_drive']})
w(f'data/{ID}/age_gate.json', {'reckoner': {'age': 6, 'items': [f'{ID}:reckoner']}, 'turtle': {'age': 7, 'items': [f'{ID}:turtle']},
  'cable': {'age': 6, 'items': [f'{ID}:cable']}, 'disk_drive': {'age': 6, 'items': [f'{ID}:disk_drive', f'{ID}:disk']}})
