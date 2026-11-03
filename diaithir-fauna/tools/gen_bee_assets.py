#!/usr/bin/env python3
"""Assets and data of the species bees (src/.../bees): textures, models, lang, recipes, tags, age gate, Codex text. Reads data/diaithirfauna/bees/species.json."""
import json, os, random
from PIL import Image
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
R = os.path.join(ROOT, 'src/main/resources'); ID = 'diaithirfauna'
S = json.load(open(f'{R}/data/{ID}/bees/species.json'))
def w(path, data, base=R):
    p = os.path.join(base, path); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(data, open(p, 'w'), indent=1)
def png(name, im):
    p = f'{R}/assets/{ID}/textures/{name}.png'; os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p)
def hexrgb(c): return ((c >> 16) & 255, (c >> 8) & 255, c & 255)
def comb(col):
    random.seed(str(col)); im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); px = im.load()
    for cy in range(10, 64, 18):
        for cx in range(8 + (9 if (cy // 18) % 2 else 0), 64, 18):
            for dx in range(-8, 9):
                for dy in range(-8, 9):
                    if abs(dx) * 0.9 + abs(dy) * 0.55 < 8.4 and 0 <= cx + dx < 64 and 0 <= cy + dy < 64:
                        edge = abs(dx) * 0.9 + abs(dy) * 0.55 > 7.2
                        g = random.randint(-10, 10)
                        c = tuple(max(0, min(255, v + g)) for v in col)
                        px[cx + dx, cy + dy] = tuple(int(v * 0.55) for v in col) + (255,) if edge else c + (255,)
    return im
def jar():
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); px = im.load()
    for x in range(20, 44):
        for y in range(14, 56):
            body = y >= 22
            if x in (20, 43) or y in (14, 55) or (y == 21 and not body):
                px[x, y] = (150, 190, 200, 255)
            elif body: px[x, y] = (190, 225, 235, 140)
    for x in range(24, 40):
        for y in range(8, 14): px[x, y] = (110, 80, 50, 255)
    return im
def apiary():
    random.seed(7); im = Image.new('RGB', (64, 64))
    for x in range(64):
        for y in range(64):
            g = random.randint(-8, 8); plank = (y // 16) % 2
            im.putpixel((x, y), (150 + g - 25 * plank, 105 + g - 18 * plank, 55 + g // 2))
    for x in range(64):
        for t in range(3): im.putpixel((x, t), (80, 55, 28)); im.putpixel((x, 63 - t), (80, 55, 28))
    for y in range(64):
        for t in range(3): im.putpixel((t, y), (80, 55, 28)); im.putpixel((63 - t, y), (80, 55, 28))
    for x in range(26, 38):
        for y in range(34, 58): im.putpixel((x, y), (30, 22, 14))   # the entrance
    return im
png('item/bee_jar', jar()); png('block/apiary', apiary())
for s in S:
    if s['id'] != 'meadow': png(f'item/comb_{s["id"]}', comb(hexrgb(int(s['color'], 16))))
lang = {f'block.{ID}.apiary': 'Apiary', f'item.{ID}.bee_jar': 'Bee Jar', f'item.{ID}.bee_jar.full': '%s Bee Jar'}
for s in S:
    lang[f'bee.{ID}.{s["id"]}'] = s['name'] + ' Bee'
    if s['id'] != 'meadow': lang[f'item.{ID}.comb_{s["id"]}'] = s['name'] + ' Comb'
lp = f'{R}/assets/{ID}/lang/en_us.json'; cur = json.load(open(lp)); cur.update(lang); json.dump(cur, open(lp, 'w'), indent=2)
def item(n, tex): 
    w(f'assets/{ID}/models/item/{n}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{ID}:item/{tex}'}})
    w(f'assets/{ID}/items/{n}.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:item/{n}'}})
item('bee_jar', 'bee_jar')
for s in S:
    if s['id'] != 'meadow': item(f'comb_{s["id"]}', f'comb_{s["id"]}')
w(f'assets/{ID}/models/block/apiary.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{ID}:block/apiary'}})
w(f'assets/{ID}/blockstates/apiary.json', {'variants': {'': {'model': f'{ID}:block/apiary'}}})
w(f'assets/{ID}/items/apiary.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/apiary'}})
w(f'data/{ID}/loot_table/blocks/apiary.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:apiary'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w('data/minecraft/tags/block/mineable/axe.json', {'values': [f'{ID}:apiary']})
w(f'data/{ID}/recipe/apiary.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['PPP', 'HBH', 'PPP'],
  'key': {'P': '#minecraft:planks', 'H': 'minecraft:honeycomb', 'B': 'minecraft:glass_bottle'}, 'result': {'id': f'{ID}:apiary', 'count': 1}})
w(f'data/{ID}/recipe/bee_jar.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': [' C ', 'G G', 'GGG'],
  'key': {'C': 'minecraft:copper_ingot', 'G': 'minecraft:glass'}, 'result': {'id': f'{ID}:bee_jar', 'count': 2}})
comb_tag = []
for s in S:
    if s['id'] == 'meadow': continue
    cid = f'{ID}:comb_{s["id"]}'; comb_tag.append(cid); o = s['out']
    if o['method'] == 'smelt':
        w(f'data/{ID}/recipe/comb_{s["id"]}_smelt.json', {'type': 'minecraft:smelting', 'category': 'misc', 'ingredient': cid, 'result': {'id': o['item']}, 'experience': 0.35, 'cookingtime': 200})
        w(f'data/{ID}/recipe/comb_{s["id"]}_blast.json', {'type': 'minecraft:blasting', 'category': 'misc', 'ingredient': cid, 'result': {'id': o['item'], 'count': 2 if o['count'] == 1 else o['count']}, 'experience': 0.35, 'cookingtime': 100})
    else:
        w(f'data/{ID}/recipe/comb_{s["id"]}_crush.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [cid], 'result': {'id': o['item'], 'count': o['count']}})
w('data/c/tags/item/combs.json', {'values': comb_tag + ['minecraft:honeycomb']})
w(f'data/{ID}/age_gate_bees.json', {f'age{a}': {'age': a, 'items': sorted([f'{ID}:comb_{s["id"]}' for s in S if s['age'] == a and s['id'] != 'meadow'] + ([f'{ID}:apiary', f'{ID}:bee_jar'] if a == 1 else []))} for a in sorted({s['age'] for s in S}) if True})
codex = [{'id': f'{ID}:apiary', 'name': 'Apiary', 'text': 'Holds up to three bees from jars and turns the flowers within five blocks into combs, one comb per bee every three minutes, by day and out of the rain. Empty hand takes the combs; sneak to take the bees out in jars. Age I.'},
         {'id': f'{ID}:bee_jar', 'name': 'Bee Jar', 'text': 'Use on a bee to catch it, on the ground to let it out. Carries its species. Age I.'}]
for s in S:
    if s['id'] == 'meadow': continue
    how = f', bred from a {s["cross"][0]} and a {s["cross"][1]} bee.' if s['cross'] else ' (wild).'
    use = ' Smelt it for the resource.' if s['out']['method'] == 'smelt' else ' Craft it to crush it.'
    codex.append({'id': f'{ID}:comb_{s["id"]}', 'name': s['name'] + ' Comb', 'text': f'Made by {s["name"]} bees{how}{use} Age {s["age"]}.'})
w('library-sync/diaithirfauna-bees.json', codex, base=os.path.join(ROOT, '..'))
