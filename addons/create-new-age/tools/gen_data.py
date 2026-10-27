#!/usr/bin/env python3
"""Converts the upstream Create: New Age (1.21.1) generated data and assets to the 26.1 / Create-port formats.

usage: gen_data.py <upstream-checkout> <resources-dir>
"""
import json, os, re, shutil, sys

UP = sys.argv[1]
OUT = sys.argv[2]
GEN = os.path.join(UP, 'neoforge/src/generated/resources')
COMMON = os.path.join(UP, 'common/src/main/resources')
NS = 'create_new_age'
FLUID_UNIT = 81  # the port stores fluid in droplets: 81 per mB


def load(p):
    with open(p) as f:
        return json.load(f)


def save(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w') as f:
        json.dump(o, f, indent=2)
        f.write('\n')


def ing(i):
    if isinstance(i, list):
        return [ing(x) for x in i]
    if 'item' in i:
        return i['item']
    if 'tag' in i:
        return '#' + i['tag']
    raise ValueError(i)


def is_fluid(i):
    return isinstance(i, dict) and ('fluid' in i or 'fluid_tag' in i or i.get('type', '').startswith('neoforge:'))


def fluid(i):
    f = {'type': 'fluid_stack', 'amount': i['amount'] * FLUID_UNIT}
    if 'fluid' in i:
        f['fluid'] = i['fluid']
    else:
        f['fluid'] = i['fluid_tag']
    return f


def results(rs):
    out = []
    for r in rs:
        o = {'id': r['id']}
        if r.get('count', 1) != 1:
            o['count'] = r['count']
        if 'chance' in r:
            o['chance'] = r['chance']
        out.append(o)
    return out


def recipe(r, step=False):
    t = r['type']
    if t == 'create_new_age:energising':
        o = {'type': t, 'energy_needed': r.get('energy_needed', 0),
             'ingredient': '$ingredient' if step else ing(r['ingredients'][0]),
             'results': ['$result'] if step else results(r['results'])}
        return o
    if t in ('minecraft:crafting_shaped',):
        return {'type': t, 'category': r.get('category', 'misc'),
                'key': {k: ing(v) for k, v in r['key'].items()}, 'pattern': r['pattern'],
                'result': {'id': r['result']['id'], 'count': r['result'].get('count', 1)}}
    if t in ('minecraft:crafting_shapeless',):
        return {'type': t, 'category': r.get('category', 'misc'), 'ingredients': [ing(x) for x in r['ingredients']],
                'result': {'id': r['result']['id'], 'count': r['result'].get('count', 1)}}
    if t == 'create:mechanical_crafting':
        o = {'type': t, 'key': {k: ing(v) for k, v in r['key'].items()}, 'pattern': r['pattern'],
             'result': {'id': r['result']['id'], 'count': r['result'].get('count', 1)}}
        if 'accept_mirrored' in r:
            o['accept_mirrored'] = r['accept_mirrored']
        return o
    ins = r.get('ingredients', [])
    items = [i for i in ins if not is_fluid(i)]
    fl = [fluid(i) for i in ins if is_fluid(i)]
    if t in ('create:compacting', 'create:mixing'):
        o = {'type': t, 'ingredients': [ing(i) for i in items]}
        if fl:
            o['fluid_ingredients'] = fl
        if 'heat_requirement' in r:
            o['heat_requirement'] = r['heat_requirement']
        if 'processing_time' in r:
            o['processing_time'] = r['processing_time']
        o['results'] = results(r['results'])
        return o
    if t in ('create:crushing', 'create:cutting', 'create:pressing'):
        o = {'type': t, 'ingredient': '$ingredient' if step else ing(items[0])}
        o['processing_time'] = r.get('processing_time', {'create:cutting': 50, 'create:crushing': 150, 'create:pressing': 0}[t])
        if t == 'create:pressing':
            del o['processing_time']
        o['results'] = ['$result'] if step else results(r['results'])
        return o
    if t in ('create:deploying', 'create:item_application'):
        o = {'type': t, 'target': '$ingredient' if step else ing(items[0]), 'ingredient': ing(items[1]),
             'results': ['$result'] if step else results(r['results'])}
        if r.get('keep_held_item'):
            o['keep_held_item'] = True
        return o
    if t == 'create:filling':
        return {'type': t, 'ingredient': '$ingredient' if step else ing(items[0]),
                'fluid_ingredient': fl[0] if fl else None, 'results': ['$result'] if step else results(r['results'])}
    if t == 'create:sequenced_assembly':
        o = {'type': t, 'ingredient': ing(r['ingredient']),
             'transitional_item': {'id': r['transitional_item']['id']},
             'result': results(r['results'])[0],
             'loops': r.get('loops', 1),
             'sequence': [recipe(s, True) for s in r['sequence']]}
        if 'junks' in r:
            o['junks'] = results(r['junks'])
        return o
    raise ValueError('unhandled recipe type ' + t)


def main():
    # recipes
    rdir = os.path.join(GEN, 'data', NS, 'recipe')
    n = 0
    for root, _, files in os.walk(rdir):
        for fn in files:
            p = os.path.join(root, fn)
            rel = os.path.relpath(p, rdir)
            try:
                save(os.path.join(OUT, 'data', NS, 'recipe', rel), recipe(load(p)))
                n += 1
            except Exception as e:
                print('SKIP recipe', rel, e)
    print('recipes', n)
    # straight copies: loot tables, tags, worldgen, biome modifiers
    for sub in ('loot_table', 'tags', 'worldgen', 'neoforge'):
        src = os.path.join(GEN, 'data', NS, sub)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(OUT, 'data', NS, sub), dirs_exist_ok=True)
    for sub in ('tags',):
        src = os.path.join(GEN, 'data', 'minecraft', sub)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(OUT, 'data', 'minecraft', sub), dirs_exist_ok=True)
    # assets
    adir = os.path.join(GEN, 'assets', NS)
    for sub in ('blockstates', 'models', 'lang'):
        src = os.path.join(adir, sub)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(OUT, 'assets', NS, sub), dirs_exist_ok=True)
    cdir = os.path.join(COMMON, 'assets', NS)
    for sub in ('models', 'textures', 'lang', 'sounds'):
        src = os.path.join(cdir, sub)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(OUT, 'assets', NS, sub), dirs_exist_ok=True)
    # item definitions (26.1 needs assets/<ns>/items/<id>.json)
    idir = os.path.join(OUT, 'assets', NS, 'models', 'item')
    bdir = os.path.join(OUT, 'assets', NS, 'models', 'block')
    ids = sorted(f[:-5] for f in os.listdir(idir) if f.endswith('.json'))
    for i in ids:
        save(os.path.join(OUT, 'assets', NS, 'items', i + '.json'),
             {'model': {'type': 'minecraft:model', 'model': f'{NS}:item/{i}'}})
    print('item definitions', len(ids))


main()
