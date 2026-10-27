#!/usr/bin/env python3
"""Converts the upstream (MIT) 1.21.1 datagen output to 26.1 formats and writes our own blockstates/models/items/lang.
usage: gen_data.py UPSTREAM_GENERATED_DATA_DIR OUT_RESOURCES"""
import json, os, sys, glob, shutil
UP, OUT = sys.argv[1], sys.argv[2]
NS = 'createoreexcavation'
def w(path, obj):
    p = os.path.join(OUT, path); os.makedirs(os.path.dirname(p), exist_ok=True)
    open(p, 'w').write(json.dumps(obj, indent=1) if not isinstance(obj, str) else obj)
def ing(i):
    if isinstance(i, list): return [ing(x) for x in i]
    if 'item' in i: return i['item']
    if 'tag' in i: return '#' + i['tag']
    return i
def conv_recipe(r):
    t = r['type']
    if t == 'minecraft:crafting_shaped':
        return {'type': t, 'category': r.get('category', 'misc'), 'key': {k: ing(v) for k, v in r['key'].items()}, 'pattern': r['pattern'], 'result': r['result']}
    if t == 'create:mechanical_crafting':
        return {'type': t, 'key': {k: ing(v) for k, v in r['key'].items()}, 'pattern': r['pattern'], 'result': r['result']}
    if t in ('create:crushing', 'create:cutting', 'create:milling'):
        return {'type': t, 'ingredient': ing(r['ingredients'][0]), 'processing_time': r['processing_time'], 'results': r['results']}
    if t == 'minecraft:smithing_transform':
        return {'type': t, 'template': ing(r['template']), 'base': ing(r['base']), 'addition': ing(r['addition']), 'result': r['result']}
    if t.startswith(NS + ':') and t.endswith(('drilling', 'extracting')):
        o = {'type': t, 'drill': ing(r['drill']), 'priority': r['priority'], 'stress': r['stress'], 'ticks': r['ticks'], 'veinId': r['veinId']}
        if 'fluid' in r:
            f = r['fluid']['ingredient']
            o['fluid'] = {'type': 'fluid_tag' if 'tag' in f else 'fluid_stack', 'amount': r['fluid']['amount'] * 81, ('tag' if 'tag' in f else 'fluid'): f.get('tag', f.get('fluid'))}
        if 'output' in r:
            if isinstance(r['output'], dict):  # extracting: fluid stack, mB -> droplets
                o['output'] = {'id': r['output']['id'], 'amount': r['output']['amount'] * 81}
            else:
                o['output'] = r['output']
        return o
    if t == NS + ':vein':
        r = dict(r)
        n = json.loads(r['name']) if isinstance(r['name'], str) else r['name']
        r['name'] = n
        return r
    raise SystemExit('unhandled ' + t)
for f in glob.glob(UP + '/' + NS + '/recipe/**/*.json', recursive=True):
    rel = os.path.relpath(f, UP + '/' + NS + '/recipe')
    w('data/%s/recipe/%s' % (NS, rel), conv_recipe(json.load(open(f))))
w('data/minecraft/recipe/netherite_drill_smithing.json', conv_recipe(json.load(open(UP + '/minecraft/recipe/netherite_drill_smithing.json'))))
shutil.copy(UP + '/' + NS + '/tags/item/drills.json', os.path.join(OUT, 'data/%s/tags/item/drills.json' % NS)) if os.path.isdir(os.path.join(OUT, 'data/%s/tags/item' % NS)) else (os.makedirs(os.path.join(OUT, 'data/%s/tags/item' % NS)), shutil.copy(UP + '/' + NS + '/tags/item/drills.json', os.path.join(OUT, 'data/%s/tags/item/drills.json' % NS)))
blocks = ['drilling_machine', 'kinetic_input', 'multiblock', 'io_block', 'extractor', 'sample_drill']
for tag in ('mineable/pickaxe', 'needs_iron_tool'):
    w('data/minecraft/tags/block/%s.json' % tag, {'values': ['%s:%s' % (NS, b) for b in blocks]})
for b in ('drilling_machine', 'extractor', 'sample_drill'):
    shutil.copy(UP + '/%s/loot_table/blocks/%s.json' % (NS, b), os.path.join(OUT, 'data/%s/loot_table/blocks/' % NS) + b + '.json') if os.path.isdir(os.path.join(OUT, 'data/%s/loot_table/blocks' % NS)) else (os.makedirs(os.path.join(OUT, 'data/%s/loot_table/blocks' % NS)), shutil.copy(UP + '/%s/loot_table/blocks/%s.json' % (NS, b), os.path.join(OUT, 'data/%s/loot_table/blocks/' % NS) + b + '.json'))
# ---- assets
A = 'assets/' + NS
def item_def(name, model):
    w('%s/items/%s.json' % (A, name), {'model': {'type': 'minecraft:model', 'model': model}})
for n in ('drill', 'diamond_drill', 'netherite_drill', 'raw_diamond', 'raw_emerald', 'raw_redstone', 'vein_finder', 'vein_atlas'):
    tex = 'normal_drill' if n == 'drill' else n
    w('%s/models/item/%s.json' % (A, n), {'parent': 'minecraft:item/generated', 'textures': {'layer0': '%s:item/%s' % (NS, tex)}})
    item_def(n, '%s:item/%s' % (NS, n))
for n, m in (('drilling_machine', 'drill_model'), ('extractor', 'extractor_model'), ('sample_drill', 'sample_drill_item')):
    item_def(n, '%s:block/%s' % (NS, m))
hor = {'facing=' + d: ({'model': '%s:block/%s' % (NS, '%s'), 'y': y} if y else {'model': '%s:block/%s' % (NS, '%s')}) for d, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270))}
def horizontal(model):
    return {'variants': {k: dict(v, model=v['model'] % model) for k, v in hor.items()}}
w(A + '/blockstates/drilling_machine.json', horizontal('drill_model'))
w(A + '/blockstates/extractor.json', horizontal('extractor_model'))
w(A + '/blockstates/sample_drill.json', {'variants': {'': {'model': '%s:block/sample_drill_model' % NS}}})
w(A + '/blockstates/multiblock.json', {'variants': {'': {'model': '%s:block/ghost' % NS}}})
w(A + '/blockstates/io_block.json', {'variants': {'': {'model': '%s:block/ghost' % NS}}})
rot = {'north': {}, 'east': {'y': 90}, 'south': {'y': 180}, 'west': {'y': 270}, 'up': {'x': 270}, 'down': {'x': 90}}
w(A + '/blockstates/kinetic_input.json', {'variants': {'shaft=' + k: dict({'model': '%s:block/kinetic_shaft' % NS}, **v) for k, v in rot.items()}})
# ---- models from boxes
def cube(a, b, tex, faces=None):
    f = {}
    for d in ('north', 'south', 'east', 'west', 'up', 'down'):
        f[d] = {'texture': '#' + tex, 'uv': [0, 0, 16, 16]}
    return {'from': a, 'to': b, 'faces': f}
def model(name, tex, els, particle):
    t = dict(tex); t['particle'] = particle
    w('%s/models/block/%s.json' % (A, name), {'textures': t, 'elements': els})
T = {'plate': NS + ':block/machine_plate', 'hazard': NS + ':block/machine_hazard', 'grate': NS + ':block/machine_grate', 'pipe': NS + ':block/machine_pipe', 'shaft': NS + ':block/machine_shaft'}
model('ghost', {'p': T['plate']}, [], '#p') if False else w('%s/models/block/ghost.json' % A, {'textures': {'particle': NS + ':block/ghost'}, 'elements': []})
model('kinetic_shaft', {'s': T['shaft']}, [cube([6, 6, 0], [10, 10, 8], 's')], '#s')
# Drilling machine: 3x3 footprint, two layers; main block is the upper-middle cell. y in [-16,16).
drill = [cube([-16, -16, -16], [32, -12, 32], 'plate'),                      # base slab
         cube([-16, -12, -16], [-12, 14, -12], 'grate'), cube([28, -12, -16], [32, 14, -12], 'grate'),
         cube([-16, -12, 28], [-12, 14, 32], 'grate'), cube([28, -12, 28], [32, 14, 32], 'grate'),  # corner posts
         cube([-12, 0, -12], [28, 14, 28], 'plate'),                       # gearbox housing
         cube([-12, 14, -12], [28, 16, 28], 'hazard'),                      # hazard cap
         cube([6, -16, 6], [10, 0, 10], 'shaft'),                           # drill string
         cube([4, 1, -20 + 20], [12, 9, 6], 'pipe') if False else cube([5, -10, -16], [11, -4, 6], 'pipe'),  # fluid feed (north)
         cube([-16, -8, 5], [6, -2, 11], 'shaft')]                          # kinetic input (west)
model('drill_model', {'plate': T['plate'], 'hazard': T['hazard'], 'grate': T['grate'], 'pipe': T['pipe'], 'shaft': T['shaft']}, drill, '#plate')
ext = [e for e in drill if True] + [cube([-6, 14, -6], [22, 30, 22], 'pipe')]  # derrick tower on top
model('extractor_model', {'plate': T['plate'], 'hazard': T['hazard'], 'grate': T['grate'], 'pipe': T['pipe'], 'shaft': T['shaft']}, ext, '#plate')
# the elements above may exceed [-16,32] for the extractor tower (y 30 ok)
samp = [cube([0, 0, 0], [16, 3, 16], 'plate'), cube([2, 3, 2], [14, 12, 14], 'plate'), cube([0, 12, 0], [16, 14, 16], 'hazard'),
        cube([7, -16, 7], [9, 0, 9], 'shaft')]
model('sample_drill_model', {'plate': T['plate'], 'hazard': T['hazard'], 'shaft': T['shaft']}, samp, '#plate')
model('sample_drill_item', {'plate': T['plate'], 'hazard': T['hazard'], 'shaft': T['shaft']}, [dict(e, **{}) for e in samp[:3]], '#plate')
print('done')
