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
def skip_target(n):
    for rx, t in VANILLA_SKIP:
        if re.search(rx, n): return (True, t)
    return (False, None)
PLANT = re.compile(r'(bush|grass|vine|bristle|butterbunch|kelp|sprout|sapling|shimmerweed|miserabell|thornreed|twistybush|growth|ditchbulb|bloom|fern|flower|^(blood|indigo|ink|puff|veil)_mushroom$|mushroom_veil|gronglet|boomgourd)')
def classify(n):
    for suf, k in [('_stairs','stairs'),('_slab','slab'),('_wall','wall'),('_fence_gate','gate'),('_fence','fence'),
                   ('_button','button'),('_pressure_plate','plate'),('_door','door'),('_trapdoor','trapdoor'),
                   ('_glass_pane','pane'),('_pane','pane')]:
        if n.endswith(suf): return k
    if re.search(r'(_log|_wood|_stem|_pillar)$', n) and not n.startswith('potted'): return 'pillar'
    if n.endswith('_leaves'): return 'leaves'
    if PLANT.search(n) and not re.search(r'(cap|block|_stem|_log)$', n): return 'cross'
    return 'block'
def material(n):
    if re.search(r'(grongle|smogstem|wigglewood|ancient_root)', n) and not n.startswith('rogdoric'):
        if n.endswith('_leaves'): return 'leaves'
        return 'log' if re.search(r'(_log|_wood)$', n) else 'wood'
    if n.endswith('_leaves'): return 'leaves'
    if n.endswith('_ore'): return 'ore'
    if re.search(r'(cloggrum|froststeel|regalium|rogdorium|utherium|forgotten)_(block|tiles|pillar|grate)|^(raw_)', n): return 'metal'
    if 'glass' in n: return 'glass'
    if re.search(r'(turf)', n): return 'turf'
    if re.search(r'(soil|goo|mogmoss|sediment$|pebble)', n): return 'soil'
    if re.search(r'mushroom_(cap|stem)|_cap$', n): return 'mushroom'
    if classify(n) == 'cross': return 'plant'
    return 'stone'
def base_of(n, kind):
    suf = {'stairs':'_stairs','slab':'_slab','wall':'_wall'}[kind]; b = n[:-len(suf)]
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
    man.append(e); made.add(n)
    # slab/stairs need base registered first
man.sort(key=lambda e: 0 if 'base' not in e else 1)
SK_ITEM = [(r'_(sword|battleaxe|spear)$','minecraft:iron_sword'),(r'_axe$','minecraft:iron_axe'),(r'_pickaxe$','minecraft:iron_pickaxe'),
 (r'_shovel$','minecraft:iron_shovel'),(r'_hoe$','minecraft:iron_hoe'),(r'_helmet$','minecraft:iron_helmet'),(r'_chestplate$','minecraft:iron_chestplate'),
 (r'_leggings$','minecraft:iron_leggings'),(r'_boots$','minecraft:iron_boots'),(r'_chest_boat$','minecraft:oak_chest_boat'),(r'_boat$','minecraft:oak_boat'),
 (r'_shield$','minecraft:shield'),(r'_bucket$','minecraft:bucket'),(r'_spawn_egg$',None),(r'^music_disc_',None),(r'smithing_template$',None),
 (r'^(slingshot|javelin|catalyst|crumbling_catalyst|denizen_mask)$',None),(r'(sign)$','minecraft:oak_sign'),(r'_bed$','minecraft:red_bed'),(r'_stew$','minecraft:mushroom_stew'),(r'_bowl$','minecraft:bowl')]
sitems = []
for n in items:
    if n in made: continue
    if n in bset: continue  # block item of a skipped block
    for rx, t in SK_ITEM:
        if re.search(rx, n):
            if t: migr.append({'registry': 'minecraft:item', 'old': f'{NS}:{n}', 'new': t})
            break
    else: sitems.append(n)
put(f'data/{OWN}/realms/{NS}.json', {'modid': NS, 'blocks': man, 'items': sitems})
put(f'data/{NS}/id_migrations.json', migr)

# ---------- assets: copy vanilla shapes, rename, stand-in textures ----------
TPL = {'stairs':'oak_stairs','slab':'oak_slab','wall':'cobblestone_wall','fence':'oak_fence','gate':'oak_fence_gate','button':'oak_button',
       'plate':'oak_pressure_plate','door':'oak_door','trapdoor':'oak_trapdoor','pillar':'oak_log','pane':'glass_pane'}
STAND = {'wood':'oak_planks','log':'oak_log','leaves':'oak_leaves','soil':'dirt','turf':'grass_block_side','plant':'short_grass','glass':'glass',
         'metal':'iron_block','ore':'iron_ore','mushroom':'brown_mushroom_block','stone':'stone'}
def tex(mat, key=None):
    if mat == 'log': return 'minecraft:block/oak_log_top' if key in ('end','top','bottom') else 'minecraft:block/oak_log'
    if mat == 'turf': return 'minecraft:block/grass_block_top' if key in ('top','end') else 'minecraft:block/dirt'
    return 'minecraft:block/' + STAND[mat]
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
            o['textures'] = {k: tex(mat, k) for k in o['textures']}
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
        put(f'assets/{NS}/models/block/{bid}.json', {'parent': 'minecraft:block/cross', 'textures': {'cross': tex(m)}})
        flat(bid, tex(m)); item(bid, f'{NS}:item/{bid}')
    else:
        put(f'assets/{NS}/blockstates/{bid}.json', {'variants': {'': {'model': f'{NS}:block/{bid}'}}})
        put(f'assets/{NS}/models/block/{bid}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': tex(m)}})
        item(bid, f'{NS}:block/{bid}')
    lang[f'block.{NS}.{bid}'] = title(bid)
for n in sitems:
    flat(n, 'minecraft:item/' + ('iron_ingot' if n.endswith(('ingot','nugget','crystal','rogdorium')) else 'paper'))
    item(n, f'{NS}:item/{n}'); lang[f'item.{NS}.{n}'] = title(n)
for b in biomes: lang[f'biome.{NS}.{b}'] = title(b)
lang[f'dimension.{NS}'] = cfg['title']
put(f'assets/{NS}/lang/en_us.json', lang)
put(f'assets/{NS}/textures/block/_art_needed.txt',
    'Stand-in vanilla textures are used until the art pipeline supplies 64px Industrial Factory style art for each id listed in data/%s/realms/%s.json.\n' % (OWN, NS))

# ---------- loot, recipes ----------
for e in man:
    bid = e['id']
    put(f'data/{NS}/loot_table/blocks/{bid}.json', {'type': 'minecraft:block', 'random_sequence': f'{NS}:blocks/{bid}',
        'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
                   'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{bid}'}]}]})
def R(name, o): put(f'data/{NS}/recipe/{name}.json', o)
def res(i, c=1): return {'id': f'{NS}:{i}', 'count': c} if c > 1 else {'id': f'{NS}:{i}'}
for e in man:
    bid, k, b = e['id'], e['kind'], e.get('base')
    if k == 'stairs' and b: R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{b}'},'pattern':['#  ','## ','###'],'result':res(bid,4)})
    elif k == 'slab' and b: R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{b}'},'pattern':['###'],'result':res(bid,6)})
    elif k == 'wall' and b: R(bid, {'type':'minecraft:crafting_shaped','category':'building','key':{'#':f'{NS}:{b}'},'pattern':['###','###'],'result':res(bid,6)})
    elif k in ('button',): 
        src = bid[:-7] + '_planks' if bid[:-7] + '_planks' in bset else bid[:-7]
        if src in bset: R(bid, {'type':'minecraft:crafting_shapeless','category':'redstone','ingredients':[f'{NS}:{src}'],'result':res(bid)})
    elif k == 'plate':
        src = bid[:-15] + '_planks' if bid[:-15] + '_planks' in bset else bid[:-15]
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}'},'pattern':['##'],'result':res(bid)})
    elif k == 'fence':
        src = bid[:-6] + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'misc','key':{'#':f'{NS}:{src}','S':'minecraft:stick'},'pattern':['#S#','#S#'],'result':res(bid,3)})
    elif k == 'gate':
        src = bid[:-11] + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}','S':'minecraft:stick'},'pattern':['S#S','S#S'],'result':res(bid)})
    elif k == 'door':
        src = bid[:-5] + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}'},'pattern':['##','##','##'],'result':res(bid,3)})
    elif k == 'trapdoor':
        src = bid[:-9] + '_planks'
        if src in bset: R(bid, {'type':'minecraft:crafting_shaped','category':'redstone','key':{'#':f'{NS}:{src}'},'pattern':['###','###'],'result':res(bid,2)})
    elif bid.endswith('_log') and bid[:-4] + '_planks' in bset:
        R(bid[:-4] + '_planks', {'type':'minecraft:crafting_shapeless','category':'building','ingredients':[f'#{NS}:{bid[:-4]}_logs'],'result':res(bid[:-4]+'_planks',4)})
        put(f'data/{NS}/tags/item/{bid[:-4]}_logs.json', {'replace': False, 'values': [f'{NS}:{bid[:-4]}_log', f'{NS}:{bid[:-4]}_wood', f'{NS}:stripped_{bid[:-4]}_log', f'{NS}:stripped_{bid[:-4]}_wood']})
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
    mt = bid[:-4].split('_', 1)[1]
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
        mt = bid[:-4].split('_', 1)[1]; addtag('c/tags/block/ores', v); addtag('c/tags/item/ores', v)
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
for p, vals in tg.items(): put(f'data/{p}.json', {'replace': False, 'values': sorted(set(vals))})

# ---------- dimension ----------
nt = vj('data/minecraft/dimension_type/the_nether.json')
nt.update({'coordinate_scale': 1.0, 'has_ceiling': True, 'has_skylight': False, 'has_fixed_time': True, 'ambient_light': 0.08, 'logical_height': 256, 'height': 256, 'min_y': 0,
           'infiniburn': '#minecraft:infiniburn_overworld'})
a = nt['attributes']
for k in ('minecraft:gameplay/fast_lava','minecraft:gameplay/piglins_zombify','minecraft:gameplay/respawn_anchor_works','minecraft:gameplay/water_evaporates','minecraft:visual/default_dripstone_particle'): a.pop(k, None)
a['minecraft:gameplay/bed_rule'] = {'can_set_spawn': 'always', 'can_sleep': 'always', 'explodes': False}
a['minecraft:gameplay/sky_light_level'] = 3.0
a['minecraft:visual/fog_start_distance'] = 6.0; a['minecraft:visual/fog_end_distance'] = 72.0
a['minecraft:visual/ambient_light_color'] = '#1c2433'
put(f'data/{NS}/dimension_type/{cfg["dimension"]}.json', nt)
ns_ = vj('data/minecraft/worldgen/noise_settings/nether.json')
ns_['default_block'] = {'Name': f'{NS}:{cfg["default_block"]}'}
ns_['default_fluid'] = {'Name': 'minecraft:water', 'Properties': {'level': '0'}}
ns_['sea_level'] = cfg['sea_level']; ns_['aquifers_enabled'] = False
def blk(n): return {'type': 'minecraft:block', 'result_state': {'Name': n}}
def cond(c, t): return {'type': 'minecraft:condition', 'if_true': c, 'then_run': t}
sr = ns_['surface_rule']['sequence'][:2]
top = blk(f'{NS}:{cfg["top_block"]}'); fill = blk(f'{NS}:{cfg["filler_block"]}')
rules = []
for b, st in cfg['biome_style'].items():
    if 'top' in st:
        rules.append(cond({'type':'minecraft:biome','biome_is':[f'{NS}:{b}']}, {'type':'minecraft:sequence','sequence':[
            cond({'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':False,'secondary_depth_range':0}, blk(f'{NS}:{st["top"]}')),
            cond({'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':True,'secondary_depth_range':3}, fill)]}))
rules.append(cond({'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':False,'secondary_depth_range':0}, top))
rules.append(cond({'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':True,'secondary_depth_range':3}, fill))
ns_['surface_rule'] = {'type': 'minecraft:sequence', 'sequence': sr + rules}
put(f'data/{NS}/worldgen/noise_settings/{cfg["dimension"]}.json', ns_)
nw = vj('data/minecraft/worldgen/biome/nether_wastes.json')
cols = {b: cfg['biome_style'].get(b, {}) for b in biomes}
for b in biomes:
    o = json.loads(json.dumps(nw)); st = cols[b]
    o['attributes'] = {'minecraft:visual/fog_color': st.get('fog', cfg['ambient']['fog']), 'minecraft:visual/sky_color': cfg['ambient']['sky']}
    o['carvers'] = []; o['has_precipitation'] = False; o['temperature'] = st.get('temp', 0.5); o['downfall'] = 0.4
    o['effects'] = {'water_color': st.get('water', cfg['ambient']['water'])}
    o['features'] = [[] for _ in range(11)]
    o['features'][5] = ['minecraft:ore_dirt']  # ore_dirt only; bespoke placed features follow in worldgen slice 2
    o['features'][5] = []
    put(f'data/{NS}/worldgen/biome/{b}.json', o)
# multi_noise grid over temperature x humidity (own layout), ordered by biome style temperature
bl = sorted(biomes, key=lambda b: (cols[b].get('temp', 0.5), b)); nbio = len(bl); cells = []
cols_n = 5; rows_n = (nbio + cols_n - 1) // cols_n
for i, b in enumerate(bl):
    c, r = i % cols_n, i // cols_n
    lo_t, hi_t = -1 + 2 * c / cols_n, -1 + 2 * (c + 1) / cols_n
    lo_h, hi_h = -1 + 2 * r / rows_n, -1 + 2 * (r + 1) / rows_n
    cells.append({'biome': f'{NS}:{b}', 'parameters': {'temperature': [round(lo_t, 3), round(hi_t, 3)], 'humidity': [round(lo_h, 3), round(hi_h, 3)],
                  'continentalness': [-1.0, 1.0], 'erosion': [-1.0, 1.0], 'weirdness': [-1.0, 1.0], 'depth': [0.0, 1.0], 'offset': 0.0}})
put(f'data/{NS}/dimension/{cfg["dimension"]}.json', {'type': f'{NS}:{cfg["dimension"]}', 'generator': {'type': 'minecraft:noise', 'settings': f'{NS}:{cfg["dimension"]}',
    'biome_source': {'type': 'minecraft:multi_noise', 'biomes': cells}}})
put(f'data/{NS}/tags/worldgen/biome/is_{NS}.json', {'replace': False, 'values': [f'{NS}:{b}' for b in biomes]})

# ---------- age gate, quest, codex ----------
put(f'data/{OWN}/diaithir/realms/{NS}.json', {'realm': NS, 'dimension': f'{NS}:{cfg["dimension"]}', 'age': cfg['age'], 'age_gate': f'diaithir:age/{cfg["age"]}',
    'units': 'SI', 'replaces': NS, 'licence_note': 'clean-room; no third-party code, art or data copied'})
put(f'data/{OWN}/quests/{NS}_descent.json', {'id': f'{OWN}:{NS}_descent', 'codex_link': f'{OWN}:{NS}', 'age': cfg['age'], 'age_gate': f'diaithir:age/{cfg["age"]}',
    'title': f'Descent: {cfg["title"]}', 'description': f'Enter {cfg["title"]} and mine its first ore.', 'tasks': [{'type': 'dimension', 'dimension': f'{NS}:{cfg["dimension"]}'}],
    'rewards': [{'type': 'energy', 'amount_j': 100}], 'next': []})
put(f'data/{OWN}/codex/entries/{NS}.json', {'id': f'{OWN}:{NS}', 'category': 'dimension', 'title': f'codex.{OWN}.{NS}.title', 'body': f'codex.{OWN}.{NS}.body',
    'age': cfg['age'], 'icon': cfg['icon'], 'unlock': {'type': 'enter_dimension', 'dimension': f'{NS}:{cfg["dimension"]}'},
    'covers': {'biomes': [f'#{NS}:is_{NS}'], 'blocks': len(man), 'items': len(sitems)}, 'quest': f'{OWN}:{NS}_descent'})
lp = RES / f'assets/{OWN}/lang/en_us.json'; L = json.loads(lp.read_text()) if lp.exists() else {}
L[f'codex.{OWN}.{NS}.title'] = cfg['title']
L[f'codex.{OWN}.{NS}.body'] = cfg['title'] + ': replacement realm with the same ids as the original mod; ' + str(len(man)) + ' blocks, ' + str(len(biomes)) + ' biomes.'
lp.write_text(json.dumps(L, indent=1) + '\n')
print(NS, 'blocks', len(man), 'items', len(sitems), 'biomes', len(biomes), 'migrations', len(migr))
