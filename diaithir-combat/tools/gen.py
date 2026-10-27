#!/usr/bin/env python3
"""Writes categories, item tags and lang for diaithir-combat. Run from the module dir."""
import json, os, sys
sys.path.insert(0, '/mnt/project-files/diaithiris-archaic/tools')
try:
    import archaic
    arch = lambda w: archaic.word(w)
except Exception:
    arch = lambda w: w
R = 'src/main/resources/'
MATS = ['wooden', 'stone', 'copper', 'bronze', 'iron', 'steel']
ARMS = ['steel', 'tempered', 'composite', 'rodos', 'future']

def sw(*types):  # spartan weaponry ids
    return [f'spartanweaponry:{m}_{t}' for t in types for m in MATS]
def ar(*types):
    return [f'diaithirarms:{m}_{t}' for t in types for m in ARMS]

step = lambda d, c, s, **k: dict(damage=d, cooldown=c, stamina=s, **k)
# id, modern Gaelic word (display), pattern, range, arc, width, max, kb, combo_window, parry, fallback, combo, members
C = [
 ('dagger', 'biodag', 'stab', 2.5, 70, 0.4, 2, 0.15, 10, 8, 'minecraft:iron_sword',
  [step(.7,5,1),step(.7,5,1),step(1.3,8,1.5,arc_mult=.7)], sw('dagger','parrying_dagger')),
 ('sword', 'claidheamh', 'slash', 3.0, 110, 0.4, 3, 0.4, 14, 0, 'minecraft:iron_sword',
  [step(.9,9,2),step(.9,9,2),step(1.3,14,3,arc_mult=1.2,pattern='overhead')],
  ['#minecraft:swords']+sw('longsword','katana','saber')+ar('sword')),
 ('rapier', 'rapier', 'thrust', 3.6, 25, 0.35, 1, 0.1, 10, 8, 'minecraft:iron_sword',
  [step(1.0,8,1.5),step(1.0,8,1.5),step(1.3,11,2,range_mult=1.1)], sw('rapier')),
 ('greatsword', 'claidheamhmòr', 'heavy', 3.8, 150, 0.4, 5, 0.7, 18, 0, 'minecraft:iron_sword',
  [step(1.2,16,4),step(1.2,16,4),step(1.9,24,6,arc_mult=1.2,pattern='overhead')], sw('greatsword')),
 ('axe', 'tuagh', 'overhead', 3.0, 80, 0.4, 2, 0.5, 14, 0, 'minecraft:iron_axe',
  [step(1.1,13,3),step(1.4,16,3.5)], ['#minecraft:axes']+sw('tomahawk')+ar('axe')),
 ('battleaxe', 'tuaghchatha', 'heavy', 3.5, 130, 0.4, 4, 0.7, 16, 0, 'minecraft:iron_axe',
  [step(1.3,18,5),step(1.7,24,6)], sw('battleaxe','scythe')),
 ('mace', 'smachd', 'overhead', 2.9, 70, 0.4, 2, 0.9, 14, 0, 'minecraft:mace',
  [step(1.2,15,3.5),step(1.5,18,4)], ['minecraft:mace']+sw('flanged_mace')+['spartanweaponry:wooden_club','spartanweaponry:studded_club']),
 ('warhammer', 'òrd', 'heavy', 3.4, 90, 0.4, 3, 1.1, 16, 0, 'minecraft:mace',
  [step(1.5,22,6),step(2.0,28,7)], sw('warhammer','battle_hammer')),
 ('spear', 'sleagh', 'thrust', 4.0, 30, 0.45, 2, 0.3, 14, 0, 'minecraft:iron_sword',
  [step(1.0,11,2),step(1.0,11,2),step(1.2,14,2.5,range_mult=1.1)],
  [{'id':'#minecraft:spears','required':False}]+sw('spear','javelin')+ar('spear')),
 ('polearm', 'cuaille', 'sweep', 4.6, 120, 0.4, 5, 0.5, 16, 0, 'minecraft:iron_sword',
  [step(1.0,16,4),step(1.0,16,4),step(1.4,22,5,pattern='overhead')], sw('halberd','glaive','pike','lance')),
 ('staff', 'bataidh', 'sweep', 3.8, 100, 0.4, 3, 0.5, 12, 10, 'minecraft:wooden_sword',
  [step(.7,8,1.5),step(.7,8,1.5),step(.9,12,2)], sw('quarterstaff')),
]
RANGED = [
 ('bow', 'bogha', 20, 0.0, ['minecraft:bow','diaithirarms:bone_bow','diaithirarms:copper_bow','diaithirarms:bronze_bow']),
 ('longbow', 'bogha-fada', 26, 0.5, ['diaithirarms:mail_bow','diaithirarms:steel_bow','diaithirarms:tempered_bow','diaithirarms:composite_bow','diaithirarms:rodos_bow','diaithirarms:future_bow']),
]
def tag(path, vals):
    out = []
    for v in vals:
        out.append(v if isinstance(v, dict) else {'id': v, 'required': False} if not v.startswith('#minecraft:') and not v.startswith('minecraft:') else v)
    p = R + 'data/diaithircombat/tags/item/weapons/' + path + '.json'
    os.makedirs(os.path.dirname(p), exist_ok=True)
    json.dump({'values': out}, open(p, 'w'), indent=1)
os.makedirs(R + 'data/diaithircombat/categories', exist_ok=True)
index = []
names = {}
for (i, g, pat, rng, arc, wid, mx, kb, cw, parry, fb, combo, mem) in C:
    d = dict(id=i, pattern=pat, range=rng, arc=arc, width=wid, max_targets=mx, knockback=kb,
             combo_window=cw, parry_window=parry, fallback=fb, combo=combo)
    json.dump(d, open(R + f'data/diaithircombat/categories/{i}.json', 'w'), indent=1)
    tag(i, mem); index.append(i); names[i] = arch(g)
for (i, g, draw, drop, mem) in RANGED:
    d = dict(id=i, kind='ranged', draw_ticks=draw, drop_reduction=drop, fallback='minecraft:bow')
    json.dump(d, open(R + f'data/diaithircombat/categories/{i}.json', 'w'), indent=1)
    tag(i, mem); index.append(i); names[i] = arch(g)
json.dump(index, open(R + 'data/diaithircombat/categories/index.json', 'w'))
L = {
 'itemGroup.diaithircombat': arch('comhrag'),
 'key.diaithircombat.roll': arch('leum'),
 'key.categories.diaithircombat': arch('comhrag'),
 'diaithircombat.stamina': arch('brìgh'),
 'diaithircombat.parry': arch('dìonadh'),
 'diaithircombat.reach': arch('ruigsinn'),
 'diaithircombat.arc': arch('bogha'),
 'diaithircombat.combo': arch('sreath'),
 'diaithircombat.draw': arch('tarraing'),
}
for k, v in names.items():
    L['diaithircombat.category.' + k] = v
json.dump(L, open(R + 'assets/diaithircombat/lang/en_us.json', 'w') if os.makedirs(R + 'assets/diaithircombat/lang', exist_ok=True) is None else None, ensure_ascii=False, indent=1)
json.dump(names, open('names_map.json', 'w'), ensure_ascii=False, indent=1)
print(len(index), 'categories')
