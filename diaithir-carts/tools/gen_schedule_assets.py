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
