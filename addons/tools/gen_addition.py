#!/usr/bin/env python3
"""Builds assets + data of Create Crafts & Additions (diaithir_createaddition) from the upstream MIT resources
(models/blockstates/lang/sounds/recipes/tags), converting 1.21.1 formats to 26.1 and dropping everything that
references content that was not ported. Textures are copied from upstream by default; --retex replaces them with
procedural originals (see retex_addition.py). Run: python3 gen_addition.py"""
import json, os, re, shutil, glob, sys

UP = os.environ.get('UP', '/home/user/up3/createaddition/src')
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', 'createaddition', 'src', 'main', 'resources'))
JAVA = os.path.abspath(os.path.join(HERE, '..', 'createaddition', 'src', 'main', 'java', 'com', 'mrh0', 'createaddition', 'index'))
CREATE_ITEMS = '/home/user/work-1a/src/main/resources/assets/create/items'
NS = 'createaddition'
A = os.path.join(ROOT, 'assets', NS)
D = os.path.join(ROOT, 'data')

DROPPED = {'servo_motor', 'electric_pump', 'liquid_blaze_burner', 'portable_energy_interface', 'digital_adapter',
           'energy_meter', 'sprinkler', 'stator', 'bioethanol', 'seed_oil', 'connector_old', 'copper_wire_casing',
           'redstone_connector'}


def reg_ids():
    blocks, items = set(), set()
    for f, tgt in (('CABlocks.java', blocks), ('CAItems.java', items)):
        for m in re.finditer(r'R\.(?:block|item)\("([a-z_]+)"', open(os.path.join(JAVA, f)).read()):
            tgt.add(m.group(1))
    return blocks, items

BLOCKS, ITEMS = reg_ids()
ALLITEMS = BLOCKS | ITEMS
CREATE_HAVE = {os.path.basename(p)[:-5] for p in glob.glob(CREATE_ITEMS + '/*.json')}


def W(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        if isinstance(obj, str): f.write(obj)
        else: json.dump(obj, f, indent=1)


def have_item(i):
    ns, _, n = i.partition(':')
    if ns == 'createaddition': return n in ALLITEMS
    if ns == 'create': return n in CREATE_HAVE
    return True


class Skip(Exception): pass


def ing(x):
    """1.21.1 ingredient object -> 26.1 ingredient string/list."""
    if isinstance(x, list): return [ing(i) for i in x]
    if isinstance(x, str): return x
    if 'item' in x:
        if not have_item(x['item']): raise Skip(x['item'])
        return x['item']
    if 'tag' in x and x.get('type', 'neoforge:tag') in ('neoforge:tag',) and 'amount' not in x:
        return '#' + x['tag']
    raise Skip(str(x))


def is_fluid_ing(x):
    return isinstance(x, dict) and ('amount' in x or 'fluid' in x)


def fluid_ing(x):
    amt = int(x.get('amount', 1000)) * 81
    if 'tag' in x: return {'type': 'fluid_tag', 'amount': amt, 'fluid_tag': '#' + x['tag']}
    if x.get('fluid', '') in ('createaddition:bioethanol', 'createaddition:seed_oil'): raise Skip('fluid')
    return {'type': 'fluid_stack', 'amount': amt, 'fluid': x['fluid']}


def result(r):
    if isinstance(r, str): r = {'id': r}
    if not have_item(r['id']): raise Skip(r['id'])
    o = {'id': r['id']}
    if r.get('count', 1) != 1: o['count'] = r['count']
    if 'chance' in r: o['chance'] = r['chance']
    return o


def conv_recipe(j):
    t = j['type']
    out = {}
    if 'neoforge:conditions' in j: out['neoforge:conditions'] = j['neoforge:conditions']
    if t in ('minecraft:crafting_shaped', 'create:mechanical_crafting'):
        out['type'] = t
        out['category'] = j.get('category', 'misc')
        out['key'] = {k: ing(v) for k, v in j['key'].items()}
        out['pattern'] = j['pattern']
        if 'accept_mirrored' in j: out['accept_mirrored'] = j['accept_mirrored']
        out['result'] = result(j['result'])
    elif t == 'minecraft:crafting_shapeless':
        out['type'] = t
        out['category'] = j.get('category', 'misc')
        out['ingredients'] = [ing(i) for i in j['ingredients']]
        out['result'] = result(j['result'])
    elif t in ('minecraft:smoking', 'minecraft:smelting', 'minecraft:blasting', 'minecraft:campfire_cooking'):
        out['type'] = t
        out['category'] = j.get('category', 'food')
        out['cookingtime'] = j.get('cookingtime', 100)
        out['experience'] = j.get('experience', 0.0)
        out['ingredient'] = ing(j['ingredient'])
        out['result'] = result(j['result'])
    elif t == 'createaddition:rolling':
        out['type'] = t
        out['ingredient'] = ing(j['ingredients'][0])
        out['results'] = [result(r) for r in j['results']]
    elif t == 'createaddition:charging':
        out['type'] = t
        out['ingredient'] = ing(j['ingredients'][0])
        out['result'] = result(j['results'][0])
        out['energy'] = j['energy']
        out['max_charge_rate'] = j.get('max_charge_rate', 0)
    elif t in ('create:mixing', 'create:compacting', 'create:filling', 'create:pressing', 'create:crushing'):
        out['type'] = t
        ings = j.get('ingredients', [])
        solid = [ing(i) for i in ings if not is_fluid_ing(i)]
        fl = [fluid_ing(i) for i in ings if is_fluid_ing(i)]
        for k in ('heat_requirement', 'processing_time'):
            if k in j: out[k] = j[k]
        res = j['results']
        items = [result(r) for r in res if 'amount' not in r]
        fres = [{'id': r['id'], 'amount': int(r['amount']) * 81} for r in res if 'amount' in r]
        for r in fres:
            if r['id'] in ('createaddition:bioethanol', 'createaddition:seed_oil'): raise Skip('fluid')
        if t == 'create:filling':
            if len(solid) != 1 or len(fl) != 1: raise Skip('filling shape')
            out['ingredient'] = solid[0]
            out['fluid_ingredient'] = fl[0]
            out['result'] = items[0]
        elif t in ('create:pressing',):
            out['ingredient'] = solid[0]
            out['results'] = items
        elif t == 'create:crushing':
            out['ingredient'] = solid[0]
            out['results'] = items
        else:
            if solid: out['ingredients'] = solid
            if fl: out['fluid_ingredients'] = fl
            if items: out['results'] = items
            if fres: out['fluid_results'] = fres
    else:
        raise Skip('type ' + t)
    return out


def main():
    shutil.rmtree(A, ignore_errors=True)
    for sub in ('data',):
        shutil.rmtree(os.path.join(ROOT, sub), ignore_errors=True)
    # ---- assets
    for src in (UP + '/main/resources/assets/createaddition', UP + '/generated/resources/assets/createaddition'):
        for dirpath, _, files in os.walk(src):
            rel = os.path.relpath(dirpath, src)
            if rel.split(os.sep)[0] in ('ponder', 'lang') : continue
            for f in files:
                base = f.rsplit('.', 1)[0]
                parts = rel.split(os.sep)
                if any(p in DROPPED for p in parts) or base in DROPPED: continue
                if rel.startswith('blockstates') and f.endswith('.json') and base not in BLOCKS: continue
                d = os.path.join(A, rel)
                os.makedirs(d, exist_ok=True)
                shutil.copy(os.path.join(dirpath, f), os.path.join(d, f))
    shutil.rmtree(os.path.join(A, 'models', 'item'), ignore_errors=True)
    # block models that blockstates never reference are harmless; item models for items:
    # ---- lang
    lang = json.load(open(UP + '/main/resources/assets/createaddition/lang/en_us.json'))
    gl = UP + '/generated/resources/assets/createaddition/lang/en_us.json'
    if os.path.exists(gl): lang.update(json.load(open(gl)))
    keep = {}
    for k, v in lang.items():
        m = re.match(r'(block|item)\.createaddition\.([a-z_]+)', k)
        if m and (m.group(2) in DROPPED or m.group(2) not in ALLITEMS): continue
        if k.startswith('createaddition.ponder') or '.ponder.' in k: continue
        keep[k] = v
    for n in sorted(ALLITEMS):
        kind = 'block' if n in BLOCKS else 'item'
        keep.setdefault(f'{kind}.createaddition.{n}', ' '.join(w.capitalize() for w in n.split('_')))
    keep['itemGroup.createaddition.main'] = 'Create Crafts & Additions'
    W(os.path.join(A, 'lang', 'en_us.json'), keep)
    # ---- item definitions
    for n in sorted(ALLITEMS):
        model = None
        for cand in (f'item/{n}', f'block/{n}/item', f'block/{n}/block', f'block/{n}'):
            if os.path.exists(os.path.join(A, 'models', cand + '.json')): model = cand; break
        if model is None:
            # generate a flat item model from the texture
            if os.path.exists(os.path.join(A, 'textures', 'item', n + '.png')):
                W(os.path.join(A, 'models', 'item', n + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{n}'}})
                model = f'item/{n}'
            else:
                print('NO MODEL for', n); continue
        W(os.path.join(A, 'items', n + '.json'), {'model': {'type': 'minecraft:model', 'model': f'{NS}:{model}'}})
    # ---- data: recipes
    n_ok = n_skip = 0
    for src, pre in ((UP + '/generated/resources/data/createaddition/recipe', ''), (UP + '/main/resources/data/createaddition/recipe', ''),
                     (UP + '/generated/resources/data/create/recipe', 'create_')):
        for p in glob.glob(src + '/**/*.json', recursive=True):
            rel = os.path.relpath(p, src)
            try:
                out = conv_recipe(json.load(open(p)))
            except Skip as e:
                n_skip += 1; continue
            except Exception as e:
                print('ERR', rel, e); n_skip += 1; continue
            W(os.path.join(D, NS, 'recipe', os.path.dirname(rel), pre + os.path.basename(rel)), out)
            n_ok += 1
    print('recipes ok', n_ok, 'skipped', n_skip)
    # ---- data: tags (filter unknown createaddition ids), damage types
    for ns_dir in ('c', 'minecraft', 'create', 'createaddition', 'farmersdelight'):
        base = UP + '/generated/resources/data/' + ns_dir + '/tags'
        for p in glob.glob(base + '/**/*.json', recursive=True):
            rel = os.path.relpath(p, base)
            if rel.startswith('fluid') and False: continue
            j = json.load(open(p))
            vals = []
            for v in j.get('values', []):
                vid = v if isinstance(v, str) else v.get('id', '')
                if vid.startswith('#'): vals.append(v); continue
                if vid.startswith('createaddition:'):
                    n = vid.split(':')[1]
                    if n not in ALLITEMS: continue
                vals.append(v)
            if not vals and not j.get('replace'):
                continue
            W(os.path.join(D, ns_dir, 'tags', rel), {'values': vals})
    for p in glob.glob(UP + '/generated/resources/data/createaddition/damage_type/*.json'):
        shutil.copy(p, os.path.join(D, NS, 'damage_type', os.path.basename(p))) if os.makedirs(os.path.join(D, NS, 'damage_type'), exist_ok=True) is None else None
    # ---- data: loot (self drops; cakes drop nothing)
    for n in sorted(BLOCKS):
        if 'cake' in n: continue
        W(os.path.join(D, NS, 'loot_table', 'blocks', n + '.json'), {
            'type': 'minecraft:block',
            'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{n}'}],
                       'conditions': [{'condition': 'minecraft:survives_explosion'}]}],
            'random_sequence': f'{NS}:blocks/{n}'})
    # ---- sounds / atlases not copied (client port handles); pack.mcmeta not needed in mod jar
    # mod metadata
    W(os.path.join(ROOT, 'META-INF', 'neoforge.mods.toml'), '''modLoader="javafml"
loaderVersion="[1,)"
license="MIT (code, from Create Crafts & Additions by mrh0); art original to the Diaithir pack"

[[mods]]
modId="diaithir_createaddition"
version="${version}"
displayName="Create Crafts & Additions (Diaithir port)"
authors="mrh0 (original), Diaithir port"
description=\'\'\'Electricity for Create: wires, connectors, motors, alternators, rolling mill, tesla coil. Port to NeoForge 26.1 for the Diaithir Create port.\'\'\'

[[dependencies.diaithir_createaddition]]
modId="neoforge"
type="required"
versionRange="[26.1.2.30-beta,)"
ordering="NONE"
side="BOTH"

[[dependencies.diaithir_createaddition]]
modId="minecraft"
type="required"
versionRange="[26.1.2]"
ordering="NONE"
side="BOTH"

[[dependencies.diaithir_createaddition]]
modId="diaithir_create"
type="required"
versionRange="[6.0.9,)"
ordering="AFTER"
side="BOTH"
''')
    print('blocks', len(BLOCKS), 'items', len(ITEMS))


if __name__ == '__main__':
    main()
