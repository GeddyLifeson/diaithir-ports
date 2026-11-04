#!/usr/bin/env python3
"""Assets and data of the timetable item (adds to the lang, age gate and Codex files gen_assets.py writes)."""
import json, os, random
from PIL import Image
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'); R = os.path.join(ROOT, 'src/main/resources'); ID = 'diaithircarts'
def w(path, data):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(data, open(p, 'w'), indent=1)
random.seed('timetable'); im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
for x in range(3, 13):
    for y in range(1, 15):
        g = 205 + random.randint(-8, 8); im.putpixel((x, y), (g, g - 12, g - 50, 255))
for y in range(1, 15): im.putpixel((3, y), (110, 70, 40, 255))
for y in (4, 7, 10, 13):
    for x in range(5, 12): im.putpixel((x, y), (60, 50, 40, 255))
im.save(f'{R}/assets/{ID}/textures/item/timetable.png')
w(f'assets/{ID}/models/item/timetable.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{ID}:item/timetable'}})
w(f'assets/{ID}/items/timetable.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:item/timetable'}})
w(f'data/{ID}/recipe/timetable.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ['minecraft:book', 'minecraft:copper_ingot'], 'result': {'id': f'{ID}:timetable', 'count': 1}})
lp = f'{R}/assets/{ID}/lang/en_us.json'; l = json.load(open(lp)); l[f'item.{ID}.timetable'] = 'Timetable'; json.dump(l, open(lp, 'w'), indent=1)
ap = f'{R}/data/{ID}/age_gate.json'; a = json.load(open(ap)); a['tier2']['items'] = sorted(set(a['tier2']['items'] + [f'{ID}:timetable'])); json.dump(a, open(ap, 'w'), indent=2)
cp = os.path.join(ROOT, '..', 'library-sync/diaithircarts.json'); c = json.load(open(cp))
if not any(e['id'] == f'{ID}:timetable' for e in c):
    c.append({'id': f'{ID}:timetable', 'name': 'Timetable', 'text': 'Use on station rails in order to write the stops of a run, then use on a cart to give it the schedule; it loops. A scheduled cart stops only at its next stop and runs only with a rider aboard (a player, villager or other crew) until Age VII, when schedules run with no crew. Sneak and use in the air to wipe it. Age V.'})
    json.dump(c, open(cp, 'w'), indent=1)

random.seed('module'); im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
for x in range(2, 14):
    for y in range(2, 14):
        g = 70 + random.randint(-5, 5); im.putpixel((x, y), (g, g + 4, g + 14, 255))
for i in range(4, 12, 2):
    for k in (1, 14): im.putpixel((i, k), (200, 170, 70, 255)); im.putpixel((k, i), (200, 170, 70, 255))
for x in range(6, 10):
    for y in range(6, 10): im.putpixel((x, y), (60, 220, 140, 255))
im.save(f'{R}/assets/{ID}/textures/item/autonomy_module.png')
w(f'assets/{ID}/models/item/autonomy_module.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{ID}:item/autonomy_module'}})
w(f'assets/{ID}/items/autonomy_module.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:item/autonomy_module'}})
w(f'data/{ID}/recipe/autonomy_module.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['GRG', 'RCR', 'GRG'],
  'key': {'G': 'minecraft:gold_ingot', 'R': 'minecraft:redstone', 'C': 'minecraft:comparator'}, 'result': {'id': f'{ID}:autonomy_module', 'count': 1}})
w(f'data/{ID}/tags/entity_type/autonomy_capable.json', {'replace': False, 'values': ['minecraft:minecart', 'minecraft:chest_minecart', 'minecraft:hopper_minecart', 'minecraft:furnace_minecart', 'minecraft:tnt_minecart']})
l = json.load(open(lp)); l[f'item.{ID}.autonomy_module'] = 'Autonomy Module'; json.dump(l, open(lp, 'w'), indent=1)
a = json.load(open(ap)); a['autonomy'] = {'age': 7, 'items': [f'{ID}:autonomy_module']}; json.dump(a, open(ap, 'w'), indent=2)
c = json.load(open(cp))
if not any(e['id'] == f'{ID}:autonomy_module' for e in c):
    c.append({'id': f'{ID}:autonomy_module', 'name': 'Autonomy Module', 'text': 'Use on a vehicle whose type is capable of autonomy (the entity type tag diaithircarts:autonomy_capable) to fit it. A scheduled vehicle with a module runs with no crew, and its crew jobs can be retired. Without one it needs a rider aboard. Age VII.'})
    json.dump(c, open(cp, 'w'), indent=1)

random.seed('reader'); im = Image.new('RGB', (64, 64))
for x in range(64):
    for y in range(64):
        g = 78 + random.randint(-6, 6); im.putpixel((x, y), (g, g + 4, g + 10))
for i in range(64):
    for t_ in range(3):
        for xy in ((i, t_), (i, 63 - t_), (t_, i), (63 - t_, i)): im.putpixel(xy, (36, 38, 46))
for x in range(22, 42):
    for y in range(22, 42): im.putpixel((x, y), (60, 150, 230))
im.save(f'{R}/assets/{ID}/textures/block/cart_reader.png')
w(f'assets/{ID}/models/block/cart_reader.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{ID}:block/cart_reader'}})
w(f'assets/{ID}/blockstates/cart_reader.json', {'variants': {f'power={n}': {'model': f'{ID}:block/cart_reader'} for n in range(16)}})
w(f'assets/{ID}/items/cart_reader.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/cart_reader'}})
w(f'data/{ID}/loot_table/blocks/cart_reader.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:cart_reader'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w(f'data/{ID}/recipe/cart_reader.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['IRI', 'RQR', 'III'],
  'key': {'I': 'minecraft:iron_ingot', 'R': 'minecraft:redstone', 'Q': 'minecraft:quartz'}, 'result': {'id': f'{ID}:cart_reader', 'count': 1}})
l = json.load(open(lp)); l[f'block.{ID}.cart_reader'] = 'Cart Reader'; json.dump(l, open(lp, 'w'), indent=1)
a = json.load(open(ap)); a['tier2']['items'] = sorted(set(a['tier2']['items'] + [f'{ID}:cart_reader'])); json.dump(a, open(ap, 'w'), indent=2)
c = json.load(open(cp))
if not any(e['id'] == f'{ID}:cart_reader' for e in c):
    c.append({'id': f'{ID}:cart_reader', 'name': 'Cart Reader', 'text': 'Put it under a rail. Gives a redstone signal for the cart above it: 0 none, 1 a cart with no schedule, 2 to 13 a scheduled cart heading for that stop (signal minus 2), 14 a scheduled cart with no crew waiting for one, 15 an autonomous cart. Age V.'})
    json.dump(c, open(cp, 'w'), indent=1)
