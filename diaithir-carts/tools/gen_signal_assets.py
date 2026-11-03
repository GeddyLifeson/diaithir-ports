#!/usr/bin/env python3
"""Assets and data of the block signal (adds to the lang, age gate and Codex files gen_assets.py writes)."""
import json, os, random
from PIL import Image
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'); R = os.path.join(ROOT, 'src/main/resources'); ID = 'diaithircarts'
def w(path, data):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(data, open(p, 'w'), indent=1)
def tex(name, lamp):
    random.seed(name); im = Image.new('RGB', (64, 64))
    for x in range(64):
        for y in range(64):
            g = 70 + random.randint(-6, 6); im.putpixel((x, y), (g, g + 2, g + 8))
    for i in range(64):
        for t in range(3):
            for xy in ((i, t), (i, 63 - t), (t, i), (63 - t, i)): im.putpixel(xy, (36, 38, 46))
    if lamp:
        c = lamp
        for x in range(18, 46):
            for y in range(10, 38):
                if (x - 32) ** 2 + (y - 24) ** 2 < 196: im.putpixel((x, y), c)
    p = os.path.join(R, f'assets/{ID}/textures/block/{name}.png'); im.save(p)
tex('signal_side', None); tex('signal_green', (60, 220, 90)); tex('signal_red', (230, 50, 40))
for c in ('green', 'red'):
    w(f'assets/{ID}/models/block/signal_{c}.json', {'parent': 'minecraft:block/orientable', 'textures': {'top': f'{ID}:block/signal_side', 'front': f'{ID}:block/signal_{c}', 'side': f'{ID}:block/signal_side'}})
ys = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
w(f'assets/{ID}/blockstates/block_signal.json', {'variants': {f'facing={f},red={r}': ({'model': f'{ID}:block/signal_{"red" if r == "true" else "green"}', 'y': y} if y else {'model': f'{ID}:block/signal_{"red" if r == "true" else "green"}'}) for f, y in ys.items() for r in ('false', 'true')}})
w(f'assets/{ID}/items/block_signal.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/signal_green'}})
w(f'data/{ID}/loot_table/blocks/block_signal.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:block_signal'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w(f'data/{ID}/recipe/block_signal.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['GRG', 'III', ' I '],
  'key': {'G': 'minecraft:glass_pane', 'R': 'minecraft:redstone_torch', 'I': 'minecraft:iron_ingot'}, 'result': {'id': f'{ID}:block_signal', 'count': 1}})
lp = f'{R}/assets/{ID}/lang/en_us.json'; l = json.load(open(lp)); l[f'block.{ID}.block_signal'] = 'Block Signal'; json.dump(l, open(lp, 'w'), indent=1)
ap = f'{R}/data/{ID}/age_gate.json'; a = json.load(open(ap)); a['signal'] = {'age': 5, 'items': [f'{ID}:block_signal']}; json.dump(a, open(ap, 'w'), indent=2)
cp = os.path.join(ROOT, '..', 'library-sync/diaithircarts.json'); c = json.load(open(cp))
if not any(e['id'] == f'{ID}:block_signal' for e in c):
    c.append({'id': f'{ID}:block_signal', 'name': 'Block Signal', 'text': 'Faces a rail and guards the stretch of connected track from there, up to 32 rails each way, round curves and slopes. Red and a redstone signal of 15 while any minecart is on that stretch, green and 0 otherwise. Wire it to a station rail or a powered rail upstream to keep trains apart. Age V.'})
    json.dump(c, open(cp, 'w'), indent=1)
