#!/usr/bin/env python3
"""Clean-room realm generator (S-id claimed in Global-Repo SCRIPTS_INDEX).
usage: python3 -I realms_gen.py <realm> --client-jar <minecraft_client.jar>
Reads tools/realms/<realm>.{json,blocks.txt,items.txt,biomes.txt} (ids only, no third-party code or art)
and writes manifest, assets, loot, recipes, tags, dimension, biomes, migrations, quest, codex into src/main/resources.
Vanilla blockstates/models are copied from the client jar and renamed, so shapes are Mojang's, art is stand-in."""
import json, re, sys, zipfile, pathlib, argparse, collections

ap = argparse.ArgumentParser(); ap.add_argument('realm'); ap.add_argument('--client-jar', required=True)
A = ap.parse_args()
ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / 'src/main/resources'
T = ROOT / 'tools/realms'
cfg = json.loads((T / f'{A.realm}.json').read_text())
NS = cfg['modid']; OWN = 'diaithir_overworld_biomes'
rd = lambda n: [l.strip() for l in (T / f'{A.realm}.{n}.txt').read_text().split('\n') if l.strip()]
blocks, items, biomes = rd('blocks'), rd('items'), rd('biomes')
bset, iset = set(blocks), set(items)
Z = zipfile.ZipFile(A.client_jar); names = set(Z.namelist())
def put(p, o):
    p = RES / p; p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(o if isinstance(o, str) else json.dumps(o, indent=1) + '\n')
def vj(p): return json.loads(Z.read(p))

def put_tag(path, vals):
    """tags under minecraft/ and c/ are shared by every realm: keep other realms' entries, replace ours"""
    p = RES / path
    old = json.loads(p.read_text()).get('values', []) if p.exists() else []
    keep = [v for v in old if not str(v).lstrip('#').startswith(NS + ':')]
    put(path, {'replace': False, 'values': sorted(set(keep) | set(vals))})


# ---------- classify blocks ----------
VANILLA_SKIP = [  # (regex, vanilla block/item it migrates to)
 (r'_wall_hanging_sign$', 'minecraft:oak_wall_hanging_sign'), (r'_hanging_sign$', 'minecraft:oak_hanging_sign'),
 (r'_wall_sign$', 'minecraft:oak_wall_sign'), (r'_sign$', 'minecraft:oak_sign'), (r'_bed$', 'minecraft:red_bed'),
 (r'^potted_', 'minecraft:flower_pot'), (r'_pot$', 'minecraft:flower_pot'), (r'farmland$', 'minecraft:farmland'),
 (r'(^|_)wall_torch$', 'minecraft:wall_torch'), (r'_torch$', 'minecraft:torch'), (r'_ladder$', 'minecraft:ladder'),
 (r'_bars$', 'minecraft:iron_bars'), (r'_lantern$', 'minecraft:lantern'), (r'_cauldron$', 'minecraft:cauldron'),
 (r'portal$', None), (r'_stem_attached$', 'minecraft:attached_pumpkin_stem'), (r'^gloomgourd_stem$', 'minecraft:pumpkin_stem'),
 (r'_plant$', None), (r'^infuser$', 'minecraft:enchanting_table'), (r'^seeping_ink$', None),
]
PORTALS = {p['block']: p for p in cfg.get('portals', [])}
def skip_target(n):
    if n in PORTALS: return (False, None)
    for rx, t in VANILLA_SKIP:
        if re.search(rx, n): return (True, t)
    return (False, None)
PLANT = re.compile(cfg.get('plant_rx', r'(bush|grass|vine|bristle|butterbunch|kelp|sprout|sapling|shimmerweed|miserabell|thornreed|twistybush|growth|ditchbulb|bloom|fern|flower|^(blood|indigo|ink|puff|veil)_mushroom$|mushroom_veil|gronglet|boomgourd)'))
def classify(n):
    if n in PORTALS: return 'portal'
    for suf, k in [('_stairs','stairs'),('_stair','stairs'),('_slab','slab'),('_wall','wall'),('_fence_gate','gate'),('_gate','gate'),('_fence','fence'),
                   ('_button','button'),('_pressure_plate','plate'),('_door','door'),('_trapdoor','trapdoor'),
                   ('_glass_pane','pane'),('_pane','pane')]:
        if n.endswith(suf): return k
    if re.search(r'(_log|_wood|_stem|_pillar)$', n) and not n.startswith('potted'): return 'pillar'
    if n.endswith('_leaves'): return 'leaves'
    if PLANT.search(n) and not re.search(r'(cap|block|_stem|_log|_planks|_leaves)$', n): return 'cross'
    return 'block'
WOODS = sorted({n[:-7] for n in blocks if n.endswith('_planks')}, key=len, reverse=True)
METALS = sorted({n[:-6] for n in items if n.endswith('_ingot')}, key=len, reverse=True)
def material(n):
    if n in PORTALS: return 'portal'
    for w in WOODS:
        if re.fullmatch(r'(stripped_)?' + re.escape(w) + r'_(log|wood)', n): return 'log'
        if n.startswith(w + '_') or n == w + '_planks':
            return 'leaves' if n.endswith('_leaves') else 'wood'
    if n.endswith('_leaves'): return 'leaves'
    if n.endswith('_ore'): return 'ore'
    for mt in METALS:
        if re.fullmatch(r'(raw_)?' + re.escape(mt) + r'(_block)?|' + re.escape(mt) + r'_(tiles|pillar|grate|plating)', n): return 'metal'
    if 'glass' in n: return 'glass'
    if re.search(r'(turf)', n): return 'turf'
    if re.search(r'(soil|goo|mogmoss|sediment$|pebble)', n): return 'soil'
    if re.search(r'mushroom_(cap|stem)|_cap$', n): return 'mushroom'
    if classify(n) == 'cross': return 'plant'
    return 'stone'
def base_of(n, kind):
    b = re.sub(r'_(stairs|stair|slab|wall)$', '', n)
    for c in (b, b + '_planks', b + '_block', b + 's', b + '_stone'):
        if c in bset: return c
    return None
man = []; migr = []; made = set(); STAND = collections.OrderedDict()
for n in blocks:
    sk, tgt = skip_target(n)
    if sk:
        if tgt: migr.append({'registry': 'minecraft:block', 'old': f'{NS}:{n}', 'new': tgt})
        continue
    k = classify(n); m = material(n); e = {'id': n, 'kind': k, 'mat': m}
    if k in ('stairs', 'slab', 'wall'):
        b = base_of(n, k)
        if not b: e['kind'] = 'block'
        else: e['base'] = b
    if k in ('door','fence','gate','button','plate','trapdoor') and m in ('stone','metal','glass'): pass
    if k == 'portal': p = PORTALS[n]; e.update({'target': p['target'], 'back': p.get('back', 'minecraft:overworld'), 'pad': p['pad']})
    man.append(e); made.add(n)
    # slab/stairs need base registered first
man.sort(key=lambda e: 0 if 'base' not in e else 1)
SK_ITEM = [(r'_(sword|battleaxe|spear)$','minecraft:iron_sword'),(r'_axe$','minecraft:iron_axe'),(r'_pickaxe$','minecraft:iron_pickaxe'),
 (r'_shovel$','minecraft:iron_shovel'),(r'_hoe$','minecraft:iron_hoe'),(r'_helmet$','minecraft:iron_helmet'),(r'_chestplate$','minecraft:iron_chestplate'),
 (r'_leggings$','minecraft:iron_leggings'),(r'_boots$','minecraft:iron_boots'),(r'_chest_boat$','minecraft:oak_chest_boat'),(r'_boat$','minecraft:oak_boat'),
 (r'_shield$','minecraft:shield'),(r'_bucket$','minecraft:bucket'),(r'_spawn_egg$',None),(r'^music_disc_',None),(r'smithing_template$',None),
 (r'^(slingshot|javelin|crumbling_catalyst|denizen_mask)$',None),(r'(sign)$','minecraft:oak_sign'),(r'_bed$','minecraft:red_bed'),(r'_stew$','minecraft:mushroom_stew'),(r'_bowl$','minecraft:bowl')]
TIER = [('skyroot','wood'),('rotten','wood'),('faded','wood'),('lignum','wood'),('heartwood','wood'),('petrified','stone'),('holystone','stone'),('recovered','stone'),
        ('forgotten','diamond'),('froststeel','diamond'),('utherium','diamond'),('arkenium','diamond'),('gravitite','diamond'),('zanite','diamond')]
GEAR = {}
for n in items:
    mt_ = re.fullmatch(r'(.+)_(sword|battleaxe|spear|axe|pickaxe|shovel|hoe|helmet|chestplate|leggings|boots)', n)
    if mt_ and n not in made and not re.search(r'(shield|bucket)', n):
        pre, kd = mt_.groups(); GEAR[n] = {'id': n, 'prefix': pre, 'kind': kd, 'tier': next((t for k, t in TIER if k in pre), 'iron')}
sitems = []
for n in items:
    if n in GEAR: continue
    if n in made: continue
    if n in bset: continue  # block item of a skipped block
    for rx, t in SK_ITEM:
        if re.search(rx, n):
            if t: migr.append({'registry': 'minecraft:item', 'old': f'{NS}:{n}', 'new': t})
            break
    else: sitems.append(n)
put(f'data/{OWN}/realms/{NS}.json', {'modid': NS, 'blocks': man, 'items': sitems, 'gear': list(GEAR.values()), 'portals': [{k: p[k] for k in ('block', 'activator', 'frame')} for p in cfg.get('portals', [])]})
put(f'data/{NS}/id_migrations.json', migr)

# ---------- assets: copy vanilla shapes, rename, stand-in textures ----------
TPL = {'stairs':'oak_stairs','slab':'oak_slab','wall':'cobblestone_wall','fence':'oak_fence','gate':'oak_fence_gate','button':'oak_button',
       'plate':'oak_pressure_plate','door':'oak_door','trapdoor':'oak_trapdoor','pillar':'oak_log','pane':'glass_pane'}
STAND = {'portal':'purple_stained_glass','wood':'oak_planks','log':'oak_log','leaves':'oak_leaves','soil':'dirt','turf':'grass_block_side','plant':'short_grass','glass':'glass',
         'metal':'iron_block','ore':'iron_ore','mushroom':'brown_mushroom_block','stone':'stone'}
def tex(mat, key=None, bid=None):
    return f'{NS}:block/{bid}' + ('_top' if mat in ('log',) and key in ('end', 'top', 'bottom') else '')
def copy_models(tpl, bid, mat, base):
    """copy every vanilla model for template tpl, renamed to bid"""
    pre = f'assets/minecraft/models/block/{tpl}'
    for p in sorted(names):
        if not p.startswith(pre) or not p.endswith('.json'): continue
        stem = p.split('/')[-1][:-5]
        if stem != tpl and not stem.startswith(tpl + '_'): continue
        if tpl == 'oak_fence' and stem.startswith('oak_fence_gate'): continue
        o = vj(p)
        if 'parent' in o:
            ps = o['parent'].replace('minecraft:block/', '').replace('block/', '')
            if ps == tpl or ps.startswith(tpl + '_'): o['parent'] = f'{NS}:block/{bid}{ps[len(tpl):]}'
        if 'textures' in o:
            o['textures'] = {k: tex(mat, k, bid) for k in o['textures']}
        put(f'assets/{NS}/models/block/{bid}{stem[len(tpl):]}.json', o)
def copy_state(tpl, bid, mat, base):
    s = Z.read(f'assets/minecraft/blockstates/{tpl}.json').decode()
    if tpl == 'oak_slab' and base: s = s.replace('minecraft:block/oak_planks', f'{NS}:block/{base}')
    s = re.sub(r'minecraft:block/' + tpl + r'(?=[_"])', f'{NS}:block/{bid}', s)
    put(f'assets/{NS}/blockstates/{bid}.json', s)
def item(bid, model):
    put(f'assets/{NS}/items/{bid}.json', {'model': {'type': 'minecraft:model', 'model': model}})
def flat(bid, texture):
    put(f'assets/{NS}/models/item/{bid}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}})
lang = {}
title = lambda n: ' '.join(w.capitalize() for w in n.split('_'))
for e in man:
    bid, k, m, b = e['id'], e['kind'], e['mat'], e.get('base')
    if e['kind'] in TPL and (k not in ('stairs','slab','wall') or b):
        tpl = TPL[k]; copy_models(tpl, bid, m, b); copy_state(tpl, bid, m, b)
        if k in ('wall','fence','button'): item(bid, f'{NS}:block/{bid}_inventory')
        elif k == 'trapdoor': item(bid, f'{NS}:block/{bid}_bottom')
        elif k == 'door': item(bid, 'minecraft:item/oak_door')
        elif k == 'pane': flat(bid, 'minecraft:block/glass'); item(bid, f'{NS}:item/{bid}')
        else: item(bid, f'{NS}:block/{bid}')
    elif k == 'cross':
        put(f'assets/{NS}/blockstates/{bid}.json', {'variants': {'': {'model': f'{NS}:block/{bid}'}}})
        put(f'assets/{NS}/models/block/{bid}.json', {'parent': 'minecraft:block/cross', 'textures': {'cross': tex(m, None, bid)}})
        flat(bid, tex(m, None, bid)); item(bid, f'{NS}:item/{bid}')
    else:
        put(f'assets/{NS}/blockstates/{bid}.json', {'variants': {'': {'model': f'{NS}:block/{bid}'}}})
        put(f'assets/{NS}/models/block/{bid}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': tex(m, None, bid)}})
        item(bid, f'{NS}:block/{bid}')
    lang[f'block.{NS}.{bid}'] = title(bid)
for n in sitems:
    flat(n, f'{NS}:item/{n}')
    item(n, f'{NS}:item/{n}'); lang[f'item.{NS}.{n}'] = title(n)
for b in biomes: lang[f'biome.{NS}.{b}'] = title(b)
lang[f'dimension.{NS}'] = cfg['title']
sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent)); import realm_art
SEEDRX = r'_(stairs|stair|slab|wall|fence_gate|gate|fence|door|trapdoor|button|pressure_plate)$'
for e in man:
    bid, m_ = e['id'], e['mat']; seed = re.sub(SEEDRX, '', bid)
    if m_ == 'plant': wb_ = realm_art.plant(seed, bid)
    else: wb_ = realm_art.block(seed, m_, seed)
    p_ = RES / f'assets/{NS}/textures/block/{bid}.png'; p_.parent.mkdir(parents=True, exist_ok=True); p_.write_bytes(wb_)
    if m_ == 'log': (RES / f'assets/{NS}/textures/block/{bid}_top.png').write_bytes(realm_art.block(seed, m_, seed, top=True))
for n_ in sitems:
    p_ = RES / f'assets/{NS}/textures/item/{n_}.png'; p_.parent.mkdir(parents=True, exist_ok=True); p_.write_bytes(realm_art.item(re.sub(r'^raw_', '', n_), n_))
(RES / f'assets/{NS}/textures/block/_art_needed.txt').unlink(missing_ok=True)

# ---------- loot, recipes ----------
for e in man:
    bid = e['id']
    if e['kind'] == 'portal': continue
    put(f'data/{NS}/loot_table/blocks/{bid}.json', {'type': 'minecraft:block', 'random_sequence': f'{NS}:blocks/{bid}',
        'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
                   'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{bid}'}]}]})
SRC = lambda b: re.sub(r'_(fence_gate|gate|fence|door|trapdoor|button|pressure_plate|stairs|stair|slab|wall)$', '', b)
def R(name, o): put(f'data/{NS}/recipe/{name}.json', o)
def res(i, c=1): return {'id': f'{NS}:{i}', 'count': c} if c > 1 else {'id': f'{NS}:{i}'}
for e in man:
    bid, k, b = e['id'], e['kind'], e.get('base')
    if k == 'stairs' and b: R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{b}'},'pattern':['#  ','## ','###'],'result':res(bid,4)})
    elif k == 'slab' and b: R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{b}'},'pattern':['###'],'result':res(bid,6)})
    elif k == 'wall' and b: R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{b}'},'pattern':['###','###'],'result':res(bid,6)})
    elif k in ('button',): 
        src = SRC(bid) + '_planks' if SRC(bid) + '_planks' in bset else SRC(bid)
        if src in bset: R(bid, {'type':'minecraft:crafting_shapeless','category':'redstone','ingredients':[f'{NS}:{src}'],'result':res(bid)})
    elif k == 'plate':
        src = SRC(bid) + '_planks' if SRC(bid) + '_planks' in bset else SRC(bid)
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}'},'pattern':['##'],'result':res(bid)})
    elif k == 'fence':
        src = SRC(bid) + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'misc','key':{'#':f'{NS}:{src}','S':'minecraft:stick'},'pattern':['#S#','#S#'],'result':res(bid,3)})
    elif k == 'gate':
        src = SRC(bid) + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}','S':'minecraft:stick'},'pattern':['S#S','S#S'],'result':res(bid)})
    elif k == 'door':
        src = SRC(bid) + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}'},'pattern':['##','##','##'],'result':res(bid,3)})
    elif k == 'trapdoor':
        src = SRC(bid) + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}'},'pattern':['###','###'],'result':res(bid,2)})
    elif bid.endswith('_log') and bid[:-4] + '_planks' in bset:
        R(bid[:-4] + '_planks', {'type':'minecraft:crafting_shapeless','category':'building','ingredients':[f'#{NS}:{bid[:-4]}_logs'],'result':res(bid[:-4]+'_planks',4)})
        put(f'data/{NS}/tags/item/{bid[:-4]}_logs.json', {'replace': False, 'values': [f'{NS}:{x}' for x in (bid[:-4]+'_log', bid[:-4]+'_wood', 'stripped_'+bid[:-4]+'_log', 'stripped_'+bid[:-4]+'_wood') if x in made]})
    elif bid.endswith('_block') and bid[:-6] + '_ingot' in iset:
        mt = bid[:-6]
        R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{mt}_ingot'},'pattern':['###']*3,'result':res(bid)})
        R(mt + '_ingot_from_block', {'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[f'{NS}:{bid}'],'result':res(mt+'_ingot',9)})
    elif k == 'block' and bid.endswith('_bricks') and bid[:-7] in bset:
        R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{bid[:-7]}'},'pattern':['##','##'],'result':res(bid,4)})
VANORE = {'iron':('minecraft:iron_ingot',0.7),'gold':('minecraft:gold_ingot',1.0),'coal':('minecraft:coal',0.1),'diamond':('minecraft:diamond',1.0)}
for e in man:
    bid = e['id']
    if e['kind'] != 'block' or not bid.endswith('_ore'): continue
    mt = bid[:-4].split('_')[-1]
    out, xp = VANORE.get(mt, (f'{NS}:{mt}_ingot' if mt + '_ingot' in iset else (f'{NS}:{mt}_crystal' if mt + '_crystal' in iset else None), 1.0))
    if out: R(f'{bid}_smelting', {'type':'minecraft:smelting','category':'misc','cookingtime':200,'experience':xp,'ingredient':f'{NS}:{bid}','result':{'id':out}})
for src, dst in [('raw_cloggrum','cloggrum_ingot'),('raw_froststeel','froststeel_ingot')]:
    if src in iset and dst in iset: R(dst + '_smelting', {'type':'minecraft:smelting','category':'misc','cookingtime':200,'experience':0.7,'ingredient':f'{NS}:{src}','result':{'id':f'{NS}:{dst}'}})

# ---------- tags ----------
tg = collections.defaultdict(list)
def addtag(path, v): tg[path].append(v)
for e in man:
    bid, k, m = e['id'], e['kind'], e['mat']; v = f'{NS}:{bid}'
    ptag = 'pickaxe' if m in ('stone','ore','metal') or k in ('wall',) else ('axe' if m in ('wood','log') else ('shovel' if m in ('soil','turf') else None))
    if m == 'wood' and k in ('button','plate','gate','fence','door','trapdoor','stairs','slab') : ptag = 'axe'
    if ptag: addtag(f'minecraft/tags/block/mineable/{ptag}', v)
    for kk, t in [('stairs','stairs'),('slab','slabs'),('wall','walls'),('fence','fences'),('gate','fence_gates'),('plate','pressure_plates'),('door','doors'),('trapdoor','trapdoors'),('leaves','leaves')]:
        if k == kk: addtag(f'minecraft/tags/block/{t}', v); addtag(f'minecraft/tags/item/{t}', v)
    if k == 'button': addtag('minecraft/tags/block/buttons', v); addtag('minecraft/tags/item/buttons', v)
    if k == 'pillar' and re.search('(_log|_wood)$', bid): addtag('minecraft/tags/block/logs', v); addtag('minecraft/tags/item/logs', v)
    if bid.endswith('_planks'): addtag('minecraft/tags/block/planks', v); addtag('minecraft/tags/item/planks', v)
    if k == 'cross' and re.search('sapling', bid): addtag('minecraft/tags/block/saplings', v)
    if bid.endswith('_ore'):
        mt = bid[:-4].split('_')[-1]; addtag('c/tags/block/ores', v); addtag('c/tags/item/ores', v)
        addtag(f'c/tags/block/ores/{mt}', v); addtag(f'c/tags/item/ores/{mt}', v)
    if bid.endswith('_block') and bid[:-6] + '_ingot' in iset:
        mt = bid[:-6]; addtag('c/tags/block/storage_blocks', v); addtag('c/tags/item/storage_blocks', v)
        addtag(f'c/tags/block/storage_blocks/{mt}', v); addtag(f'c/tags/item/storage_blocks/{mt}', v)
    if m == 'stone' and k == 'block' and not bid.endswith(('_bricks','_tiles','_pillar')): addtag('c/tags/block/stones', v); addtag('c/tags/item/stones', v)
for n in sitems:
    if n.endswith('_ingot'): addtag(f'c/tags/item/ingots/{n[:-6]}', f'{NS}:{n}'); addtag('c/tags/item/ingots', f'{NS}:{n}')
    if n.endswith('_nugget'): addtag(f'c/tags/item/nuggets/{n[:-7]}', f'{NS}:{n}'); addtag('c/tags/item/nuggets', f'{NS}:{n}')
    if n.startswith('raw_') and n[4:] + '_ingot' in iset: addtag(f'c/tags/item/raw_materials/{n[4:]}', f'{NS}:{n}'); addtag('c/tags/item/raw_materials', f'{NS}:{n}')
# parent tags just list children; c:ores/* etc as nested tags
for p in list(tg):
    if p.count('/') >= 4 and p.startswith('c/tags/') and p.split('/')[3] in ('ores','ingots','nuggets','raw_materials','storage_blocks'):
        parent = '/'.join(p.split('/')[:4]); tg[parent] = [x for x in tg[parent] if not x.startswith(NS + ':')]
        child = '#c:' + '/'.join(p.split('/')[3:]); 
        if child not in tg[parent]: tg[parent].append(child)
for p, vals in tg.items():
    if p.startswith(('minecraft/', 'c/')): put_tag(f'data/{p}.json', vals)
    else: put(f'data/{p}.json', {'replace': False, 'values': sorted(set(vals))})


# ---------- slice 3: trees and plant patches ----------
import copy
oak = vj('data/minecraft/worldgen/configured_feature/oak.json')
SURF = {'type': 'minecraft:all_of', 'predicates': [{'type': 'minecraft:matching_blocks', 'blocks': 'minecraft:air'}, {'type': 'minecraft:solid', 'offset': [0, -1, 0]}]}
def placed(feature, count, spread=7, rarity=None):
    pl = ([{'type': 'minecraft:rarity_filter', 'chance': rarity}] if rarity else []) + [
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'max_inclusive': {'below_top': 8}, 'min_inclusive': {'above_bottom': 8}}},
        {'type': 'minecraft:biome'}, {'type': 'minecraft:count', 'count': count},
        {'type': 'minecraft:random_offset', 'xz_spread': {'type': 'minecraft:trapezoid', 'max': spread, 'min': -spread, 'plateau': 0}, 'y_spread': {'type': 'minecraft:trapezoid', 'max': 4, 'min': -4, 'plateau': 0}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': SURF}]
    return {'feature': feature, 'placement': pl}
FAM = {}
for w in WOODS:
    lg = next((w + x for x in ('_log', '_wood') if w + x in made), None)
    lv = w + '_leaves' if w + '_leaves' in made else None
    if lg and lv:
        o = copy.deepcopy(oak)
        o['config']['trunk_provider']['state'] = {'Name': f'{NS}:{lg}', 'Properties': {'axis': 'y'}}
        o['config']['foliage_provider']['state'] = {'Name': f'{NS}:{lv}'}
        o['config']['trunk_placer']['base_height'] = 4 + len(w) % 3
        o['config'].pop('below_trunk_provider', None)
        put(f'data/{NS}/worldgen/configured_feature/tree_{w}.json', o)
        put(f'data/{NS}/worldgen/placed_feature/tree_{w}.json', placed(f'{NS}:tree_{w}', 60, 7))
        FAM[w] = f'{NS}:tree_{w}'
PLANTS = [e['id'] for e in man if e['kind'] == 'cross' and 'sapling' not in e['id'] and not e['id'].endswith('_plant') and 'mushroom' not in e['id']]
for pl in PLANTS:
    put(f'data/{NS}/worldgen/configured_feature/patch_{pl}.json', {'type': 'minecraft:simple_block', 'config': {'to_place': {'type': 'minecraft:simple_state_provider', 'state': {'Name': f'{NS}:{pl}'}}}})
    put(f'data/{NS}/worldgen/placed_feature/patch_{pl}.json', placed(f'{NS}:patch_{pl}', 96, 7))
def veg_for(b, i):
    fs = [f for f in FAM if f in b or f.replace('_oak', '') in b]
    if not fs and re.search(r'(forest|wood|thicket|grove|jungle|taiga|swamp)', b) and FAM: fs = [sorted(FAM)[i % len(FAM)]]
    feats = [FAM[f] for f in fs]
    if PLANTS and not re.search(r'(sea|ocean|lake|abyss|barren|grain|still|loss)', b):
        k = max(2, len(PLANTS) // 4)
        feats += [f'{NS}:patch_{PLANTS[(i * 3 + j) % len(PLANTS)]}' for j in range(min(k, 4))]
    return sorted(set(feats))

# ---------- dimensions ----------
ref = lambda x: x if ':' in x else f'{NS}:{x}'
def blk(n): return {'type': 'minecraft:block', 'result_state': {'Name': ref(n)}}
def cond(c, t): return {'type': 'minecraft:condition', 'if_true': c, 'then_run': t}
FLOOR = {'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':False,'secondary_depth_range':0}
FILL = {'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':True,'secondary_depth_range':3}
dims = cfg.get('dimensions') or [{'name': cfg['dimension'], **{k: cfg[k] for k in ('default_block','top_block','filler_block','sea_level','ambient','biome_style') if k in cfg}}]
allbiomes = set()
for D in dims:
    dn = D['name']; style = D.get('biome_style', {}); amb = D.get('ambient', cfg.get('ambient'))
    dbio = [b for b in biomes if b in D['biomes']] if 'biomes' in D else list(biomes)
    allbiomes.update(dbio)
    base = D.get('base', 'nether')
    nt = vj(f'data/minecraft/dimension_type/{"the_nether" if base == "nether" else "overworld"}.json')
    nt['coordinate_scale'] = 1.0
    if base == 'nether':
        nt.update({'coordinate_scale': 1.0, 'has_ceiling': True, 'has_skylight': False, 'has_fixed_time': True, 'ambient_light': 0.08, 'logical_height': 256, 'height': 256, 'min_y': 0,
                   'infiniburn': '#minecraft:infiniburn_overworld'})
        a = nt['attributes']
        for k in ('minecraft:gameplay/fast_lava','minecraft:gameplay/piglins_zombify','minecraft:gameplay/respawn_anchor_works','minecraft:gameplay/water_evaporates','minecraft:visual/default_dripstone_particle'): a.pop(k, None)
        a['minecraft:gameplay/bed_rule'] = {'can_set_spawn': 'always', 'can_sleep': 'always', 'explodes': False}
        a['minecraft:gameplay/sky_light_level'] = 3.0
        a['minecraft:visual/fog_start_distance'] = 6.0; a['minecraft:visual/fog_end_distance'] = 72.0
        a['minecraft:visual/ambient_light_color'] = D.get('light', '#1c2433')
    elif base == 'overworld':
        pass
    else:  # open sky (islands in the air)
        nt.update({'coordinate_scale': 1.0, 'logical_height': 384, 'height': 384, 'min_y': 0, 'has_ender_dragon_fight': False})
        nt.pop('has_ender_dragon_fight', None) if False else None
        nt['has_fixed_time'] = D.get('fixed_time', False)
    put(f'data/{NS}/dimension_type/{dn}.json', nt)
    ns_ = vj('data/minecraft/worldgen/noise_settings/' + {'nether': 'nether', 'end': 'end', 'overworld': 'overworld'}[base] + '.json')
    if base == 'overworld': ns_['ore_veins_enabled'] = False; ns_['aquifers_enabled'] = True
    ns_['default_block'] = {'Name': ref(D['default_block'])}
    ns_['default_fluid'] = {'Name': 'minecraft:water', 'Properties': {'level': '0'}} if base == 'nether' else {'Name': 'minecraft:air'}
    ns_['sea_level'] = D.get('sea_level', 40)
    if base != 'overworld': ns_['aquifers_enabled'] = False
    sr = ns_['surface_rule']['sequence'][:2] if base == 'nether' else ([ns_['surface_rule']['sequence'][0]] if base == 'overworld' else [])
    if base == 'overworld': ns_['default_fluid'] = {'Name': 'minecraft:water', 'Properties': {'level': '0'}}
    rules = []
    for b, st in style.items():
        if 'top' in st and b in dbio:
            rules.append(cond({'type':'minecraft:biome','biome_is':[f'{NS}:{b}']}, {'type':'minecraft:sequence','sequence':[cond(FLOOR, blk(st['top'])), cond(FILL, blk(D['filler_block']))]}))
    if D.get('top_block'): rules.append(cond(FLOOR, blk(D['top_block'])))
    if D.get('filler_block'): rules.append(cond(FILL, blk(D['filler_block'])))
    ns_['surface_rule'] = {'type': 'minecraft:sequence', 'sequence': sr + rules} if (sr or rules) else ns_['surface_rule']
    put(f'data/{NS}/worldgen/noise_settings/{dn}.json', ns_)
    nw = vj('data/minecraft/worldgen/biome/nether_wastes.json')
    for b in dbio:
        o = json.loads(json.dumps(nw)); st = style.get(b, {})
        o['attributes'] = {'minecraft:visual/fog_color': st.get('fog', amb['fog']), 'minecraft:visual/sky_color': amb['sky']}
        o['carvers'] = []; o['has_precipitation'] = False; o['temperature'] = st.get('temp', 0.5); o['downfall'] = 0.4
        o['effects'] = {'water_color': st.get('water', amb['water'])}
        o['features'] = [[] for _ in range(11)]
        o['features'][9] = veg_for(b, biomes.index(b))
        put(f'data/{NS}/worldgen/biome/{b}.json', o)
    bl = sorted(dbio, key=lambda b: (style.get(b, {}).get('temp', 0.5), b)); nbio = len(bl); cells = []
    cols_n = 5; rows_n = max(1, (nbio + cols_n - 1) // cols_n)
    for i, b in enumerate(bl):
        c, r = i % cols_n, i // cols_n
        lo_t, hi_t = -1 + 2 * c / cols_n, -1 + 2 * (c + 1) / cols_n
        lo_h, hi_h = -1 + 2 * r / rows_n, -1 + 2 * (r + 1) / rows_n
        cells.append({'biome': f'{NS}:{b}', 'parameters': {'temperature': [round(lo_t, 3), round(hi_t, 3)], 'humidity': [round(lo_h, 3), round(hi_h, 3)],
                      'continentalness': [-2.0, 2.0], 'erosion': [-2.0, 2.0], 'weirdness': [-2.0, 2.0], 'depth': [-2.0, 2.0], 'offset': 0.0}})
    put(f'data/{NS}/dimension/{dn}.json', {'type': f'{NS}:{dn}', 'generator': {'type': 'minecraft:noise', 'settings': f'{NS}:{dn}',
        'biome_source': {'type': 'minecraft:multi_noise', 'biomes': cells}}})
    put(f'data/{NS}/tags/worldgen/biome/is_{dn}.json', {'replace': False, 'values': [f'{NS}:{b}' for b in dbio]})
    lang[f'dimension.{NS}.{dn}'] = D.get('title', title(dn))
put(f'data/{NS}/tags/worldgen/biome/is_{NS}.json', {'replace': False, 'values': [f'{NS}:{b}' for b in biomes]})
put(f'assets/{NS}/lang/en_us.json', lang)
DIMS = [f'{NS}:{D["name"]}' for D in dims]

# ---------- age gate, quest, codex ----------
put(f'data/{OWN}/diaithir/realms/{NS}.json', {'realm': NS, 'dimension': DIMS[0], 'dimensions': DIMS, 'age': cfg['age'], 'age_gate': f'diaithir:age/{cfg["age"]}',
    'units': 'SI', 'replaces': NS, 'licence_note': 'clean-room; no third-party code, art or data copied'})
put(f'data/{OWN}/quests/{NS}_descent.json', {'id': f'{OWN}:{NS}_descent', 'codex_link': f'{OWN}:{NS}', 'age': cfg['age'], 'age_gate': f'diaithir:age/{cfg["age"]}',
    'title': f'Descent: {cfg["title"]}', 'description': f'Enter {cfg["title"]} and mine its first ore.', 'tasks': [{'type': 'dimension', 'dimension': DIMS[0]}],
    'rewards': [{'type': 'energy', 'amount_j': 100}], 'next': []})
put(f'data/{OWN}/codex/entries/{NS}.json', {'id': f'{OWN}:{NS}', 'category': 'dimension', 'title': f'codex.{OWN}.{NS}.title', 'body': f'codex.{OWN}.{NS}.body',
    'age': cfg['age'], 'icon': cfg['icon'], 'unlock': {'type': 'enter_dimension', 'dimension': DIMS[0]},
    'covers': {'biomes': [f'#{NS}:is_{NS}'], 'blocks': len(man), 'items': len(sitems)}, 'quest': f'{OWN}:{NS}_descent'})
lp = RES / f'assets/{OWN}/lang/en_us.json'; L = json.loads(lp.read_text()) if lp.exists() else {}
L[f'codex.{OWN}.{NS}.title'] = cfg['title']
L[f'codex.{OWN}.{NS}.body'] = cfg['title'] + ': replacement realm with the same ids as the original mod; ' + str(len(man)) + ' blocks, ' + str(len(biomes)) + ' biomes.'
lp.write_text(json.dumps(L, indent=1) + '\n')

# ---------- slice 4: mobs ----------
import hashlib, colorsys, io
from PIL import Image
KIND_TEX = {'zombie':'zombie/zombie','illager':'illager/vindicator','golem':'iron_golem/iron_golem','spider':'spider/spider','ravager':'illager/ravager',
            'blaze':'blaze/blaze','silverfish':'silverfish/silverfish','phantom':'phantom/phantom','guardian':'guardian/guardian','hoglin':'hoglin/hoglin'}
BASE = {'zombie':(20,3),'illager':(24,5),'golem':(100,9),'spider':(16,3),'ravager':(60,7),'blaze':(20,4),'silverfish':(8,1.5),'phantom':(18,3),'guardian':(30,6),'hoglin':(40,5)}
BOSS_RX = re.compile(r'(queen|_prime$|gigas|verdant_engine|forgotten_guardian|greater_dweller|^slider$|sentry_golem|^tempest$|^fractus_prime$)')
PASSIVE_RX = re.compile(r'(bunny|whale|cow|phyg|sheepuff|^moa$|glitterwing|prismallard|lizard|^gwib|^mog$|smog_mog|rootmin|variant_bee|honey_slime|kirrid|burrukai|aechor|sprout|^gloomper$|stoneborn)')
KIND_RX = [('spider', r'(spider|weaver|vermis|trilobita|echinus|spina|palus|scuttler|skephid)'), ('phantom', r'(bee|wing|zephyr|whale|flying|prismallard|tempest)'),
           ('guardian', r'(gwib|watcher|aechor)'), ('golem', r'(golem|slider|taluton|stoneborn|forgotten|nargoyle|sentry)'),
           ('ravager', r'(beast|burrukai|taegore|gigas|muncher|rotbelcher|engine|moa)'), ('hoglin', r'(dweller|brute|^mog$|smog_mog|kirrid|phyg|cow|sheepuff)'),
           ('illager', r'(minion|denizen|edward|carrion)'), ('blaze', r'(blade|electric|^fractus)'),
           ('silverfish', r'(swet|rootmin|lizard|scintling|rotling|slime|gloomper|bunny|chick|glitter|sploogie|puff|fractus)')]
mobs = []
mp = T / f'{A.realm}.mobs.txt'
for n in ([l.strip() for l in mp.read_text().split('\n') if l.strip()] if mp.exists() else []):
    kind = next((k for k, rx in KIND_RX if re.search(rx, n)), 'zombie')
    boss = bool(BOSS_RX.search(n)); passive = (not boss) and bool(PASSIVE_RX.search(n))
    hp, atk = BASE[kind]
    if boss: hp, atk, sc = hp * 5 + 200, atk * 2, 1.6
    elif passive: hp, atk, sc = max(8, hp // 2), 2, 1.0
    else: sc = 1.0
    mobs.append({'id': n, 'kind': kind, 'role': 'boss' if boss else ('passive' if passive else 'monster'), 'hp': hp, 'atk': atk, 'scale': sc})
def tint(png, key, bright=1.0):
    h = int(hashlib.sha256(f'{NS}:{key}'.encode()).hexdigest()[:8], 16)
    hue, sat, val = (h % 360) / 360.0, 0.55 + ((h >> 9) % 40) / 100.0, 0.75 + ((h >> 17) % 30) / 100.0
    im = Image.open(io.BytesIO(png)).convert('RGBA'); px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a == 0: continue
            l = (r + g + b) / 765.0
            rr, gg, bb = colorsys.hsv_to_rgb(hue, min(1.0, sat), min(1.0, (0.15 + 0.85 * l) * val * bright))
            px[x, y] = (int(rr * 255), int(gg * 255), int(bb * 255), a)
    out = io.BytesIO(); im.save(out, 'PNG'); return out.getvalue()
def wb(p, data):
    p = RES / p; p.parent.mkdir(parents=True, exist_ok=True); p.write_bytes(data)
egg = Z.read('assets/minecraft/textures/item/zombie_spawn_egg.png')
dimtags = [D['name'] for D in (cfg.get('dimensions') or [{'name': cfg['dimension']}])]
for i, m in enumerate(mobs):
    n = m['id']
    wb(f'assets/{NS}/textures/entity/{n}.png', tint(Z.read(f'assets/minecraft/textures/entity/{KIND_TEX[m["kind"]]}.png'), n, 1.15 if m['role'] == 'boss' else 1.0))
    wb(f'assets/{NS}/textures/item/{n}_spawn_egg.png', tint(egg, n))
    put(f'assets/{NS}/models/item/{n}_spawn_egg.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{n}_spawn_egg'}})
    item(f'{n}_spawn_egg', f'{NS}:item/{n}_spawn_egg')
    lang[f'entity.{NS}.{n}'] = title(n); lang[f'item.{NS}.{n}_spawn_egg'] = title(n) + ' Spawn Egg'
    drop = {'zombie': 'minecraft:rotten_flesh', 'spider': 'minecraft:string'}.get(m['kind'], 'minecraft:bone')
    if m['role'] == 'passive': drop = 'minecraft:leather'
    cnt = (2, 4) if m['role'] == 'boss' else (0, 2)
    if m['role'] == 'boss': drop = 'minecraft:diamond'
    put(f'data/{NS}/loot_table/entities/{n}.json', {'type': 'minecraft:entity', 'random_sequence': f'{NS}:entities/{n}', 'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0,
        'entries': [{'type': 'minecraft:item', 'name': drop, 'functions': [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': cnt[0], 'max': cnt[1]}}]}]}]})
    if m['role'] != 'boss':
        put(f'data/{NS}/neoforge/biome_modifier/spawn_{n}.json', {'type': 'neoforge:add_spawns', 'biomes': f'#{NS}:is_{dimtags[i % len(dimtags)]}',
            'spawners': {'type': f'{NS}:{n}', 'weight': 6 if m['role'] == 'passive' else 10, 'minCount': 1, 'maxCount': 3}})
if mobs:
    put(f'data/{NS}/tags/entity_type/bosses.json', {'replace': False, 'values': [f'{NS}:{m["id"]}' for m in mobs if m['role'] == 'boss']})
    put(f'data/{NS}/tags/entity_type/{NS}_mobs.json', {'replace': False, 'values': [f'{NS}:{m["id"]}' for m in mobs]})
    mf = json.loads((RES / f'data/{OWN}/realms/{NS}.json').read_text()); mf['mobs'] = mobs
    put(f'data/{OWN}/realms/{NS}.json', mf)
    put(f'data/{OWN}/codex/entries/{NS}_bestiary.json', {'id': f'{OWN}:{NS}_bestiary', 'category': 'bestiary', 'title': f'codex.{OWN}.{NS}_bestiary.title', 'body': f'codex.{OWN}.{NS}_bestiary.body',
        'age': cfg['age'], 'icon': f'{NS}:{mobs[0]["id"]}_spawn_egg', 'unlock': {'type': 'kill_entity_tag', 'tag': f'{NS}:{NS}_mobs'}, 'covers': {'entities': f'#{NS}:{NS}_mobs'}})
    put(f'data/{OWN}/quests/{NS}_hunt.json', {'id': f'{OWN}:{NS}_hunt', 'codex_link': f'{OWN}:{NS}_bestiary', 'age': cfg['age'], 'age_gate': f'diaithir:age/{cfg["age"]}',
        'title': f'Hunt: {cfg["title"]}', 'description': f'Defeat one creature of {cfg["title"]}.', 'tasks': [{'type': 'kill', 'entity_tag': f'{NS}:{NS}_mobs', 'count': 1}], 'rewards': [{'type': 'energy', 'amount_j': 100}]})
    L2 = json.loads(lp.read_text()); L2[f'codex.{OWN}.{NS}_bestiary.title'] = cfg['title'] + ' bestiary'
    L2[f'codex.{OWN}.{NS}_bestiary.body'] = f'{len(mobs)} creatures: ' + ', '.join(title(m['id']) for m in mobs) + '.'
    lp.write_text(json.dumps(L2, indent=1) + '\n')
    put(f'assets/{NS}/lang/en_us.json', lang)


# ---------- slice 6: gear ----------
def tint_png(png, key, bright=1.0): return tint(png, key, bright)
HELD = ('sword','battleaxe','spear','axe','pickaxe','shovel','hoe')
VAN = {'battleaxe': 'axe', 'spear': 'sword'}
mats = collections.OrderedDict()
for g in GEAR.values():
    n, kd, pre = g['id'], g['kind'], g['prefix']; vk = VAN.get(kd, kd)
    wb(f'assets/{NS}/textures/item/{n}.png', tint_png(Z.read(f'assets/minecraft/textures/item/iron_{vk}.png'), pre))
    put(f'assets/{NS}/models/item/{n}.json', {'parent': 'minecraft:item/handheld' if kd in HELD else 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{n}'}})
    item(n, f'{NS}:item/{n}'); lang[f'item.{NS}.{n}'] = title(n)
    mats.setdefault(pre, g)
    ing = next((c for c in (pre + '_ingot', pre + '_crystal', pre) if c in iset), None)
    plank = pre + '_planks' if pre + '_planks' in bset else None
    src = f'{NS}:{ing}' if g['tier'] != 'wood' and ing else (f'{NS}:{plank}' if plank else ('minecraft:oak_planks' if g['tier'] == 'wood' else (f'{NS}:{ing}' if ing else None)))
    pat = {'sword': ['#', '#', 'S'], 'spear': ['#', 'S', 'S'], 'pickaxe': ['###', ' S ', ' S '], 'axe': ['##', '#S', ' S'], 'battleaxe': ['###', '#S#', ' S '], 'shovel': ['#', 'S', 'S'],
           'hoe': ['##', ' S', ' S'], 'helmet': ['###', '# #'], 'chestplate': ['# #', '###', '###'], 'leggings': ['###', '# #', '# #'], 'boots': ['# #', '# #']}[kd]
    if src: R(n, {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'key': {'#': src, **({'S': 'minecraft:stick'} if any('S' in r for r in pat) else {})}, 'pattern': pat, 'result': res(n)})
for pre, g in mats.items():
    ing = next((c for c in (pre + '_ingot', pre + '_crystal', pre) if c in iset), None); plank = pre + '_planks' if pre + '_planks' in bset else None
    vals = [f'{NS}:{ing}'] if ing and g['tier'] != 'wood' else ([f'{NS}:{plank}'] if plank else ['minecraft:oak_planks'])
    put(f'data/{NS}/tags/item/repair/{pre}.json', {'replace': False, 'values': vals})
    if any(x['prefix'] == pre and x['kind'] in ('helmet', 'chestplate', 'leggings', 'boots') for x in GEAR.values()):
        put(f'assets/{NS}/equipment/{pre}.json', {'layers': {'humanoid': [{'texture': f'{NS}:{pre}'}], 'humanoid_leggings': [{'texture': f'{NS}:{pre}'}]}})
        wb(f'assets/{NS}/textures/entity/equipment/humanoid/{pre}.png', tint_png(Z.read('assets/minecraft/textures/entity/equipment/humanoid/iron.png'), pre))
        wb(f'assets/{NS}/textures/entity/equipment/humanoid_leggings/{pre}.png', tint_png(Z.read('assets/minecraft/textures/entity/equipment/humanoid_leggings/iron.png'), pre))
if GEAR:
    put(f'assets/{NS}/lang/en_us.json', lang)
    for kd_, tg_ in (('sword', 'swords'), ('spear', 'swords'), ('axe', 'axes'), ('battleaxe', 'axes'), ('pickaxe', 'pickaxes'), ('shovel', 'shovels'), ('hoe', 'hoes'),
                     ('helmet', 'head_armor'), ('chestplate', 'chest_armor'), ('leggings', 'leg_armor'), ('boots', 'foot_armor')):
        v = [f'{NS}:{g["id"]}' for g in GEAR.values() if g['kind'] == kd_]
        if v: put_tag(f'data/minecraft/tags/item/{tg_}.json', v)

# ---------- slice 5: structures (procedural single templates, own design) ----------
import struct, gzip
def _s(x): b = x.encode(); return struct.pack('>H', len(b)) + b
def nb(tag, name, v):
    return bytes([tag]) + (_s(name) if name is not None else b'') + payload(tag, v)
def payload(tag, v):
    if tag == 3: return struct.pack('>i', v)
    if tag == 8: return _s(v)
    if tag == 9: t, items = v; return bytes([t]) + struct.pack('>i', len(items)) + b''.join(payload(t, i) for i in items)
    if tag == 10: return b''.join(nb(t, k, x) for k, (t, x) in v.items()) + b'\x00'
    raise ValueError(tag)
def write_template(path, size, palette, blocks):
    pal = [{'Name': (8, n)} for n in palette]
    root = {'DataVersion': (3, 4790), 'size': (9, (3, list(size))), 'palette': (9, (10, pal)),
            'blocks': (9, (10, [{'pos': (9, (3, list(p))), 'state': (3, i)} for p, i in blocks])), 'entities': (9, (10, []))}
    data = bytes([10]) + _s('') + payload(10, root)
    p = RES / path; p.parent.mkdir(parents=True, exist_ok=True); p.write_bytes(gzip.compress(data))
sp = T / f'{A.realm}.structures.txt'
structs = [l.strip() for l in sp.read_text().split('\n') if l.strip()] if sp.exists() else []
solid = [b for b in made if (b.endswith(('_bricks', '_planks', '_tiles', '_flagstones', 'stone', 'rock')) and not b.endswith(('_ore',))) and next((e['kind'] for e in man if e['id'] == b), '') == 'block']
if not solid: solid = [D0 for D0 in [cfg.get('default_block') or dims[0]['default_block']] if ':' not in D0] or ['stone']
def pick(n, salt, lst): return lst[int(hashlib.sha256(f'{NS}:{n}:{salt}'.encode()).hexdigest()[:6], 16) % len(lst)]
def full(b): return b if ':' in b else f'{NS}:{b}'
sset = []
for si, n in enumerate(structs):
    base = n.replace('/', '_'); wall = full(pick(base, 'w', solid)); floor = full(pick(base, 'f', solid)); acc = full(pick(base, 'a', solid))
    arche = 'tower' if re.search(r'(tower|pillar|monolith|spire|throne|aviary|fortress|sanctum)', base) else ('shrine' if re.search(r'(shrine|temple|fountain|garden|ranch|house|gazebo|beepartments|honitel|floor|ship|cave|stream)', base) else 'ruin')
    blocks = {}; pal = ['minecraft:air', wall, floor, acc]
    def put_(x, y, z, i): blocks[(x, y, z)] = i
    W = 11 if arche != 'tower' else 7; H = 14 if arche == 'tower' else 6
    for x in range(W):
        for z in range(W):
            for y in range(-5, H): put_(x, y + 5, z, 0)          # clear volume
            for y in range(-5, 0): put_(x, y + 5, z, 2)          # foundation so it never floats
            put_(x, 5, z, 2)
    c = W // 2
    for x in range(W):
        for z in range(W):
            edge = x in (0, W - 1) or z in (0, W - 1)
            if arche == 'tower':
                if edge:
                    for y in range(1, H): 
                        if not (z == 0 and x == c and y <= 2) and not (y % 5 == 3 and (x == c or z == c)): put_(x, 5 + y, z, 1)
            elif arche == 'ruin':
                if edge:
                    h = 1 + int(hashlib.sha256(f'{base}{x}{z}'.encode()).hexdigest()[:2], 16) % 4
                    if not (z == 0 and abs(x - c) <= 1):
                        for y in range(1, h + 1): put_(x, 5 + y, z, 1)
            else:  # shrine
                if edge or (x % 4 == 2 and z % 4 == 2):
                    if (x % 4 == 2 and z % 4 == 2) or (x, z) in ((0, 0), (0, W - 1), (W - 1, 0), (W - 1, W - 1)):
                        for y in range(1, 5): put_(x, 5 + y, z, 1)
                    else: put_(x, 6, z, 3)
                if x in range(2, W - 2) and z in range(2, W - 2): put_(x, 5 + 5, z, 3)
    write_template(f'data/{NS}/structure/{base}.json'.replace('.json', '.nbt'), (W, H + 5, W), pal, [((x, y, z), i) for (x, y, z), i in sorted(blocks.items())])
    put(f'data/{NS}/worldgen/template_pool/{base}.json', {'fallback': 'minecraft:empty', 'elements': [{'weight': 1, 'element': {'element_type': 'minecraft:single_pool_element',
        'location': f'{NS}:{base}', 'processors': 'minecraft:empty', 'projection': 'rigid'}}]})
    dn = dimtags[si % len(dimtags)]; Dd = next(d for d in dims if d['name'] == dn); cave = Dd.get('base', 'nether') == 'nether'
    sj = {'type': 'minecraft:jigsaw', 'biomes': f'#{NS}:is_{dn}', 'step': 'surface_structures', 'spawn_overrides': {}, 'terrain_adaptation': 'beard_thin',
          'start_pool': f'{NS}:{base}', 'size': 1, 'max_distance_from_center': 30, 'use_expansion_hack': False}
    if cave: sj['start_height'] = {'absolute': Dd.get('sea_level', 40) + 2}
    else: sj['start_height'] = {'absolute': -4}; sj['project_start_to_heightmap'] = 'WORLD_SURFACE_WG'
    put(f'data/{NS}/worldgen/structure/{base}.json', sj)
    put(f'data/{NS}/worldgen/structure_set/{base}.json', {'structures': [{'structure': f'{NS}:{base}', 'weight': 1}],
        'placement': {'type': 'minecraft:random_spread', 'spacing': 22 + si % 9, 'separation': 8, 'salt': 1000 + int(hashlib.sha256(f'{NS}{base}'.encode()).hexdigest()[:4], 16)}})
    sset.append(f'{NS}:{base}')
if sset:
    put(f'data/{NS}/tags/worldgen/structure/{NS}_structures.json', {'replace': False, 'values': sset})
    L3 = json.loads(lp.read_text()); L3[f'codex.{OWN}.{NS}_structures.title'] = cfg['title'] + ' structures'
    L3[f'codex.{OWN}.{NS}_structures.body'] = f'{len(sset)} ruins, towers and shrines: ' + ', '.join(title(x.split(':')[1]) for x in sset) + '.'
    lp.write_text(json.dumps(L3, indent=1) + '\n')
    put(f'data/{OWN}/codex/entries/{NS}_structures.json', {'id': f'{OWN}:{NS}_structures', 'category': 'structure', 'title': f'codex.{OWN}.{NS}_structures.title',
        'body': f'codex.{OWN}.{NS}_structures.body', 'age': cfg['age'], 'icon': cfg['icon'], 'unlock': {'type': 'locate_structure_tag', 'tag': f'{NS}:{NS}_structures'},
        'covers': {'structures': f'#{NS}:{NS}_structures'}})
print(NS, 'blocks', len(man), 'items', len(sitems), 'biomes', len(biomes), 'migrations', len(migr))
