#!/usr/bin/env python3
"""Generates the Archimedes' Ships assets (64 px Industrial Factory textures, models, blockstates, items, lang, loot, recipes).
All textures are drawn here from scratch; none of the original mod's art is used.
Usage: gen_assets.py <diaithir-ships/src/main/resources>"""
import json, os, random, sys, zlib
from PIL import Image, ImageDraw

OUT = sys.argv[1]
NS = 'archimedes'
S = 64
COLORS = {  # dye colour -> rgb
    'white': (233, 236, 236), 'orange': (240, 118, 19), 'magenta': (189, 68, 179), 'light_blue': (58, 175, 217),
    'yellow': (248, 197, 39), 'lime': (112, 185, 25), 'pink': (237, 141, 172), 'gray': (62, 68, 71),
    'light_gray': (142, 142, 134), 'cyan': (21, 137, 145), 'purple': (121, 42, 172), 'blue': (53, 57, 157),
    'brown': (114, 71, 40), 'green': (84, 109, 27), 'red': (160, 39, 34), 'black': (20, 21, 25)}


def mul(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def noisy(base, seed, amt=10):
    rnd = random.Random(seed)
    im = Image.new('RGB', (S, S), base)
    px = im.load()
    for x in range(S):
        for y in range(S):
            d = rnd.randint(-amt, amt)
            px[x, y] = tuple(max(0, min(255, v + d)) for v in base)
    return im


def rivet(d, x, y, base):
    d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=mul(base, 0.55))
    d.ellipse([x - 1, y - 1, x + 1, y + 1], fill=mul(base, 1.35))
    d.point([x - 1, y - 1], fill=mul(base, 1.7))


def plate(base, seed, panels=2):
    im = noisy(base, seed)
    d = ImageDraw.Draw(im)
    step = S // panels
    for i in range(panels + 1):
        p = min(S - 1, i * step)
        d.line([(p, 0), (p, S)], fill=mul(base, 0.5))
        d.line([(0, p), (S, p)], fill=mul(base, 0.5))
        if p + 1 < S:
            d.line([(p + 1, 0), (p + 1, S)], fill=mul(base, 1.2))
            d.line([(0, p + 1), (S, p + 1)], fill=mul(base, 1.2))
    for i in range(panels):
        for j in range(panels):
            for dx in (6, step - 6):
                for dy in (6, step - 6):
                    rivet(d, i * step + dx, j * step + dy, base)
    d.rectangle([0, 0, S - 1, S - 1], outline=mul(base, 0.35))
    return im


def hazard(seed):
    im = noisy((226, 178, 28), seed, 6)
    d = ImageDraw.Draw(im)
    for k in range(-S, S * 2, 16):
        d.polygon([(k, 0), (k + 8, 0), (k + 8 - S, S), (k - S, S)], fill=(34, 34, 34))
    d.rectangle([0, 0, S - 1, S - 1], outline=(30, 30, 30), width=2)
    return im


def front_marker():
    base = (96, 104, 110)
    im = plate(base, 11, 1)
    d = ImageDraw.Draw(im)
    cx = cy = 32
    d.ellipse([cx - 22, cy - 22, cx + 22, cy + 22], outline=(205, 140, 40), width=4)
    d.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=(205, 140, 40), outline=(60, 40, 15))
    for a in range(8):
        import math
        t = a * math.pi / 4
        d.line([(cx + 6 * math.cos(t), cy + 6 * math.sin(t)), (cx + 28 * math.cos(t), cy + 28 * math.sin(t))], fill=(205, 140, 40), width=3)
    return im


def floater():
    base = (232, 108, 24)
    im = noisy(base, 21, 8)
    d = ImageDraw.Draw(im)
    for y in (0, 21, 42):
        d.rectangle([0, y, S - 1, y + 3], fill=(70, 74, 80))
        for x in (8, 32, 56):
            rivet(d, x, y + 1, (70, 74, 80))
    d.rectangle([0, 0, S - 1, S - 1], outline=(110, 50, 12))
    return im


def seat():
    base = (58, 62, 68)
    im = noisy(base, 31, 6)
    d = ImageDraw.Draw(im)
    for i in range(0, S, 16):
        for j in range(0, S, 16):
            d.rectangle([i + 2, j + 2, i + 13, j + 13], fill=mul(base, 1.25), outline=mul(base, 0.6))
            d.point([i + 7, j + 7], fill=(180, 180, 175))
    return im


def engine_front():
    base = (74, 78, 84)
    im = plate(base, 41, 1)
    d = ImageDraw.Draw(im)
    d.rectangle([14, 14, 49, 49], fill=(24, 24, 26), outline=(40, 40, 44), width=2)
    for y in range(18, 48, 6):
        d.rectangle([17, y, 46, y + 2], fill=(235, 120, 30))
    return im


def crate():
    base = (146, 104, 58)
    im = noisy(base, 51, 9)
    d = ImageDraw.Draw(im)
    for y in range(0, S, 16):
        d.line([(0, y), (S, y)], fill=mul(base, 0.55))
    for x in (0, S - 8):
        d.rectangle([x, 0, x + 7, S - 1], fill=(78, 84, 90))
        for y in (8, 32, 56):
            rivet(d, x + 4, y, (78, 84, 90))
    d.rectangle([0, 0, S - 1, S - 1], outline=(60, 40, 20))
    return im


def balloon(color):
    c = COLORS[color]
    im = noisy(c, zlib.crc32(color.encode()) & 0xffff, 5)
    d = ImageDraw.Draw(im)
    for p in (0, 21, 42, 63):
        d.line([(p, 0), (p, S)], fill=mul(c, 0.6))
    for y in range(8, S, 16):
        d.line([(0, y), (S, y)], fill=mul(c, 0.8))
    for x in (10, 31, 52):
        for y in (10, 31, 52):
            rivet(d, x, y, (140, 145, 150))
    return im


def tex(name, im):
    p = os.path.join(OUT, 'assets', NS, 'textures', 'block', name + '.png')
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)


def js(path, obj):
    p = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    json.dump(obj, open(p, 'w'), indent=1)
    open(p, 'a').write('\n')


steel = plate((96, 104, 110), 12, 1)
tex('marker_front', front_marker()); tex('marker_side', steel); tex('marker_top', plate((84, 90, 96), 13, 1))
tex('floater', floater()); tex('seat', seat()); tex('buffer', hazard(61))
tex('engine_front', engine_front()); tex('engine_side', plate((74, 78, 84), 42, 1)); tex('engine_top', plate((60, 64, 70), 43, 1))
tex('crate', crate())
for c in COLORS: tex('balloon_' + c, balloon(c))

blocks = ['marker', 'floater', 'seat', 'buffer', 'engine', 'crate'] + ['balloon_' + c for c in COLORS]
cube = {'floater': 'floater', 'seat': 'seat', 'buffer': 'buffer', 'crate': 'crate'}
cube.update({'balloon_' + c: 'balloon_' + c for c in COLORS})

for b in blocks:
    if b in cube:
        js(f'assets/{NS}/models/block/{b}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{cube[b]}'}})
        js(f'assets/{NS}/blockstates/{b}.json', {'variants': {'': {'model': f'{NS}:block/{b}'}}})
    else:
        js(f'assets/{NS}/models/block/{b}.json', {'parent': 'minecraft:block/orientable', 'textures': {
            'front': f'{NS}:block/{b}_front', 'side': f'{NS}:block/{b}_side', 'top': f'{NS}:block/{b}_top'}})
        if b == 'marker':
            js(f'assets/{NS}/blockstates/{b}.json', {'variants': {
                'facing=north': {'model': f'{NS}:block/{b}'}, 'facing=east': {'model': f'{NS}:block/{b}', 'y': 90},
                'facing=south': {'model': f'{NS}:block/{b}', 'y': 180}, 'facing=west': {'model': f'{NS}:block/{b}', 'y': 270}}})
        else:
            js(f'assets/{NS}/blockstates/{b}.json', {'variants': {'': {'model': f'{NS}:block/{b}'}}})
    js(f'assets/{NS}/items/{b}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/{b}'}})
    js(f'data/{NS}/loot_table/blocks/{b}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'bonus_rolls': 0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
        'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{b}'}]}]})

# engine model needs the front texture for the engine; crate keeps cube_all
names = {'marker': 'Ship Helm', 'floater': 'Floater', 'seat': 'Ship Seat', 'buffer': 'Assembly Buffer', 'engine': 'Ship Engine', 'crate': 'Ship Crate'}
lang = {f'block.{NS}.{b}': names[b] for b in names}
for c in COLORS: lang[f'block.{NS}.balloon_{c}'] = c.replace('_', ' ').title() + ' Balloon'
lang.update({f'itemGroup.{NS}': "Archimedes' Ships",
    f'{NS}.assemble.ok': 'Ship assembled. Move with WASD, jump to rise, sprint to brake.',
    f'{NS}.assemble.block_overflow': 'Too many blocks for one ship.',
    f'{NS}.assemble.missing_marker': 'No helm found.', f'{NS}.assemble.error': 'The ship could not be assembled.',
    f'{NS}.engine.fueled': 'Engine fuelled: %s s left', f'{NS}.engine.status': 'Engine: %s s of fuel (hold coal or wood to add)'})
js(f'assets/{NS}/lang/en_us.json', lang)

def shaped(name, pattern, key, result, count=1):
    js(f'data/{NS}/recipe/{name}.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern, 'key': key, 'result': {'id': f'{NS}:{name}', 'count': count}})
def shapeless(name, ings, count=1):
    js(f'data/{NS}/recipe/{name}.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ings, 'result': {'id': f'{NS}:{name}', 'count': count}})

shaped('marker', ['XIX', 'IOI', 'XIX'], {'X': '#minecraft:planks', 'I': 'minecraft:iron_nugget', 'O': 'minecraft:iron_ingot'}, None)
shapeless('floater', ['#minecraft:logs', '#minecraft:wool'])
shaped('seat', ['W', 'P'], {'W': '#minecraft:wool', 'P': '#minecraft:planks'}, None)
shapeless('buffer', ['minecraft:string', '#minecraft:planks'])
shaped('engine', ['III', 'IFI', 'III'], {'I': 'minecraft:iron_ingot', 'F': 'minecraft:furnace'}, None)
shaped('crate', ['PPP', 'PCP', 'PPP'], {'P': '#minecraft:planks', 'C': 'minecraft:chest'}, None)
for c in COLORS:
    shaped('balloon_' + c, ['W', 'S'], {'W': f'minecraft:{c}_wool', 'S': 'minecraft:string'}, None)

js(f'data/{NS}/tags/block/not_on_ships.json', {'values': ['minecraft:bedrock', 'minecraft:barrier', 'minecraft:end_portal_frame']})
def merge_tag(path, add):
    p = os.path.join(OUT, path)
    cur = json.load(open(p)) if os.path.exists(p) else {'values': []}
    cur['values'] = cur['values'] + [v for v in add if v not in cur['values']]
    js(path, cur)
merge_tag('data/minecraft/tags/block/mineable/pickaxe.json', [f'{NS}:engine', f'{NS}:marker'])
merge_tag('data/minecraft/tags/block/mineable/axe.json', [f'{NS}:crate', f'{NS}:floater', f'{NS}:buffer'])
print('ok')
