#!/usr/bin/env python3
"""id_migrations_bees.json: Productive Bees ids (read from its public en_us.json on GitHub, so every old id is real) to this module's. Usage: gen_bee_migrations.py"""
import json, re, urllib.request, os
URL = 'https://raw.githubusercontent.com/JDKDigital/productive-bees/master/src/main/resources/assets/productivebees/lang/en_us.json'
lang = json.load(urllib.request.urlopen(URL))
ours_combs = {'sandy', 'muddy', 'coal', 'iron', 'copper', 'redstone', 'gold', 'lapis', 'quartz', 'glowstone', 'diamond'}
blocks, items, entities = {}, {}, {}
for k in lang:
    m = re.match(r'(block|item|entity)\.productivebees\.(.+)$', k)
    if not m: continue
    kind, name = m.groups()
    old = 'productivebees:' + name
    if kind == 'block':
        if re.fullmatch(r'(oak|spruce|birch|jungle|acacia|dark_oak|cherry|mangrove)_wood_nest', name): blocks[old] = 'diaithirfauna:nest_meadow'
        elif name in ('sand_nest', 'gravel_nest'): blocks[old] = 'diaithirfauna:nest_sandy'
        elif name in ('coarse_dirt_nest', 'slimy_nest'): blocks[old] = 'diaithirfauna:nest_muddy'
        elif re.fullmatch(r'advanced_.*beehive|(bamboo|dragon_egg)_hive|.*_hive', name) and not name.startswith('expansion'): blocks[old] = 'diaithirfauna:apiary'
    elif kind == 'item':
        if name in ('bee_cage', 'sturdy_bee_cage'): items[old] = 'diaithirfauna:bee_jar'
        m2 = re.fullmatch(r'honeycomb_(.+)', name)
        if m2 and m2.group(1) in ours_combs: items[old] = f'diaithirfauna:comb_{m2.group(1)}'
    elif kind == 'entity' and name.endswith('_bee') and name[:-4] in ours_combs | {'bee'}:
        entities[old] = 'minecraft:bee'
out = {'note': 'Productive Bees (all rights reserved) ids read from its public language file. Bees migrate to a plain bee (the species cannot be carried across); hives become apiaries; nests become the nearest wild nest; combs map where this module has the same resource; everything else is dropped.',
       'source': URL, 'blocks': blocks, 'items': items, 'entities': entities}
json.dump(out, open(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'id_migrations_bees.json'), 'w'), indent=1)
print(len(blocks), len(items), len(entities))
