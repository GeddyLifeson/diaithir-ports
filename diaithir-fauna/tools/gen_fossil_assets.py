#!/usr/bin/env python3
"""Fossil blocks and items: textures, models, blockstates, loot tables, tags, worldgen, lang. Run after build_species.py (adds to its lang file)."""
import json, os, random
import numpy as np
from PIL import Image

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources')
A = os.path.join(RES, 'assets/diaithirfauna'); D = os.path.join(RES, 'data/diaithirfauna')
ROCKS = json.load(open(os.path.join(D, 'species.json')))['rocks']
BASE = dict(sandstone='c9a96b', mudstone='7d6a58', limestone='b8b4a6', shale='55575c', marine_sediment='8a8f86', chalk='e4e1d6', amber='c98a1b',
            tar='25201c', volcanic_ash='8d8d8d', permafrost='8aa0a8', conglomerate='8b7967', coal='28282c')
NAME = dict(sandstone='Sandstone', mudstone='Mudstone', limestone='Limestone', shale='Shale', marine_sediment='Marine sediment', chalk='Chalk', amber='Amber',
            tar='Tar pit', volcanic_ash='Volcanic ash', permafrost='Permafrost', conglomerate='Conglomerate', coal='Coal seam')
BONE = np.array([226, 214, 186])

def col(h): return np.array([int(h[i:i + 2], 16) for i in (0, 2, 4)], float)

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, 'w'), indent=1)

def rock_tex(rock, seed):
    r = random.Random(seed); n = np.random.default_rng(seed)
    base = col(BASE[rock]); im = np.ones((16, 16, 3)) * base
    im += n.normal(0, 7, (16, 16, 1))                       # grain
    for y in range(16): im[y] *= 1 + 0.05 * np.sin(y * 1.7 + seed)   # strata bands
    for _ in range(r.randint(3, 4)):                         # bone: a short bar with a knob at each end
        x, y = r.randint(2, 12), r.randint(2, 13); L = r.randint(3, 5); vert = r.random() < .4
        for i in range(L):
            px, py = (x, y + i) if vert else (x + i, y)
            if px < 16 and py < 16: im[py, px] = BONE * (0.9 + 0.1 * r.random())
        for k in (0, L - 1):
            px, py = (x, y + k) if vert else (x + k, y)
            for dx, dy in ((1, 0), (0, 1)):
                if px + dx < 16 and py + dy < 16: im[py + dy, px + dx] = BONE * 0.85
    return Image.fromarray(np.clip(im, 0, 255).astype('uint8'), 'RGB').convert('RGBA')

def item_tex(cleaned):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    if cleaned:                                              # a long bone with two knobs
        for i in range(3, 13):
            px[i, 12 - i + 1] = (226, 214, 186, 255); px[i, 12 - i + 2] = (196, 184, 156, 255)
        for (x, y) in ((2, 12), (3, 13), (2, 13), (12, 2), (13, 3), (12, 3)): px[x, y] = (236, 226, 200, 255)
    else:                                                    # a rock chunk with bone showing
        for y in range(5, 13):
            for x in range(3, 13):
                if (x - 7.5) ** 2 / 20 + (y - 8.5) ** 2 / 14 < 1: px[x, y] = (150 + (x * 7) % 18, 130 + (y * 5) % 14, 100, 255)
        for (x, y) in ((5, 7), (6, 7), (7, 8), (8, 8), (9, 9)): px[x, y] = (226, 214, 186, 255)
    return im

def bench_tex(top):
    n = np.random.default_rng(7 if top else 8); im = np.ones((16, 16, 3)) * np.array([112, 84, 56]); im += n.normal(0, 5, (16, 16, 1))
    if top:
        im[3:13, 3:13] = np.array([150, 120, 84]); im[5:7, 5:11] = BONE; im[9:11, 6:9] = np.array([90, 90, 94])   # a bone and a tool on the bench
    else:
        im[0:2] *= .8; im[14:16] *= .8
    return Image.fromarray(np.clip(im, 0, 255).astype('uint8'), 'RGB').convert('RGBA')

lang = {}
for i, rock in enumerate(ROCKS):
    bid = f'fossil_{rock}'
    rock_tex(rock, i + 1).save(os.path.join(A, f'textures/block/{bid}.png'), optimize=True) if os.makedirs(os.path.join(A, 'textures/block'), exist_ok=True) is None else 0
    w(os.path.join(A, f'blockstates/{bid}.json'), {'variants': {'': {'model': f'diaithirfauna:block/{bid}'}}})
    w(os.path.join(A, f'models/block/{bid}.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'diaithirfauna:block/{bid}'}})
    w(os.path.join(A, f'items/{bid}.json'), {'model': {'type': 'minecraft:model', 'model': f'diaithirfauna:block/{bid}'}})
    w(os.path.join(D, f'loot_table/blocks/{bid}.json'), {'type': 'minecraft:block', 'pools': []})
    lang[f'block.diaithirfauna.{bid}'] = f'{NAME[rock]} with fossils'
    st = lambda n: {'target': {'predicate_type': 'minecraft:tag_match', 'tag': f'minecraft:{n}'}, 'state': {'Name': f'diaithirfauna:{bid}'}}
    w(os.path.join(D, f'worldgen/configured_feature/{bid}.json'), {'type': 'minecraft:ore', 'config': {'size': 4, 'discard_chance_on_air_exposure': 0.5,
        'targets': [st('stone_ore_replaceables'), st('deepslate_ore_replaceables')]}})
    w(os.path.join(D, f'worldgen/placed_feature/{bid}.json'), {'feature': f'diaithirfauna:{bid}', 'placement': [
        {'type': 'minecraft:count', 'count': 3}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': -2000}, 'max_inclusive': {'absolute': 64}}},
        {'type': 'minecraft:biome'}]})
    w(os.path.join(D, f'neoforge/biome_modifier/{bid}.json'), {'type': 'neoforge:add_features', 'biomes': '#minecraft:is_overworld', 'features': f'diaithirfauna:{bid}', 'step': 'underground_ores'})
os.makedirs(os.path.join(A, 'textures/item'), exist_ok=True)
for nm, cl in (('fossil_fragment', False), ('cleaned_fossil', True)):
    item_tex(cl).save(os.path.join(A, f'textures/item/{nm}.png'))
    w(os.path.join(A, f'models/item/{nm}.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'diaithirfauna:item/{nm}'}})
    w(os.path.join(A, f'items/{nm}.json'), {'model': {'type': 'minecraft:model', 'model': f'diaithirfauna:item/{nm}'}})
bench_tex(True).save(os.path.join(A, 'textures/block/preparation_bench_top.png')); bench_tex(False).save(os.path.join(A, 'textures/block/preparation_bench_side.png'))
w(os.path.join(A, 'blockstates/preparation_bench.json'), {'variants': {'': {'model': 'diaithirfauna:block/preparation_bench'}}})
w(os.path.join(A, 'models/block/preparation_bench.json'), {'parent': 'minecraft:block/cube_column', 'textures': {'end': 'diaithirfauna:block/preparation_bench_top', 'side': 'diaithirfauna:block/preparation_bench_side'}})
w(os.path.join(A, 'items/preparation_bench.json'), {'model': {'type': 'minecraft:model', 'model': 'diaithirfauna:block/preparation_bench'}})
w(os.path.join(D, 'loot_table/blocks/preparation_bench.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'diaithirfauna:preparation_bench'}]}]})
w(os.path.join(RES, 'data/minecraft/tags/block/mineable/pickaxe.json'), {'values': [f'diaithirfauna:fossil_{r}' for r in ROCKS]})
w(os.path.join(RES, 'data/minecraft/tags/block/mineable/axe.json'), {'values': ['diaithirfauna:preparation_bench']})
w(os.path.join(RES, 'data/minecraft/tags/block/needs_stone_tool.json'), {'values': [f'diaithirfauna:fossil_{r}' for r in ROCKS]})
w(os.path.join(D, 'recipe/preparation_bench.json'), {'type': 'minecraft:crafting_shaped', 'pattern': ['BIB', 'PPP', 'P P'], 'key': {
    'B': 'minecraft:brush', 'I': 'minecraft:iron_ingot', 'P': 'minecraft:oak_planks'}, 'result': {'id': 'diaithirfauna:preparation_bench'}})
lang.update({'block.diaithirfauna.preparation_bench': 'Preparation bench', 'item.diaithirfauna.fossil_fragment': 'Fossil fragment', 'item.diaithirfauna.cleaned_fossil': 'Cleaned fossil',
             'item.diaithirfauna.fossil_fragment.named': 'Fossil fragment (%s)', 'item.diaithirfauna.cleaned_fossil.named': 'Cleaned fossil (%s)',
             'item.diaithirfauna.fossil_fragment.hint': 'Brush it clean on a preparation bench.',
             'message.diaithirfauna.studied.new': 'Studied %s. New to your notes (%s species studied).', 'message.diaithirfauna.studied.known': 'You already knew %s; a little more detail added.'})
lp = os.path.join(A, 'lang/en_us.json'); cur = json.load(open(lp)); cur.update(lang); json.dump(cur, open(lp, 'w'), ensure_ascii=False, indent=0)
print(len(ROCKS), 'rock blocks')
