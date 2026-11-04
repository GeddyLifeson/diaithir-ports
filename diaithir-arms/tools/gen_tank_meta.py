#!/usr/bin/env python3
"""Names (Diaithiris kennings through the archaic layer), Age gates, library and Codex entries for the armour pack.
Reads tank_manifest.json (written by gen_tank.py). Usage: python3 -I tools/gen_tank_meta.py <module dir>"""
import json, sys
sys.path.insert(0, '/mnt/project-files/diaithiris-archaic/tools')
import archaic
M = sys.argv[1] if len(sys.argv) > 1 else '.'
man = json.load(open(f'{M}/tank_manifest.json'))
ID = lambda x: f'mts:diaithirtanks.{x}'
CLS = {'tank_mk1': 'carbadchatha tràth', 'tank_mk1a': 'carbadchatha trom', 'tank_mk1b': 'carbadchatha trom làidir',
       'tank_mk2': 'carbadchatha cruinn', 'tank_mk2a': 'carbadchatha cruinn trom', 'tank_mk2b': 'carbadchatha cruinn làidir',
       'tank_wheel': 'lorg iarainn', 'tank_engine': 'cridheiarainn', 'tank_seat': 'suidheachan',
       'tank_gun_105': 'gunnamòr beag', 'tank_gun_120': 'gunnamòr', 'tank_gun_125': 'gunnamòr làidir',
       'tank_shell_ap': 'slige sàthaidh', 'tank_shell_he': 'slige spreadhaidh'}
CAMO = {'': ('Forest', 'na coille'), '_desert': ('Desert', 'an fhàsaich'), '_snow': ('Winter', 'an t-sneachda')}
EN = {'tank_wheel': 'Tank Road Wheel', 'tank_engine': 'Tank Diesel Engine', 'tank_seat': 'Tank Crew Seat',
      'tank_gun_105': 'Tank Gun 105 mm', 'tank_gun_120': 'Tank Gun 120 mm', 'tank_gun_125': 'Tank Gun 125 mm (autoloader)',
      'tank_shell_ap': 'Armour-Piercing Tank Shell', 'tank_shell_he': 'High-Explosive Tank Shell'}
names, ages, lib = {}, {}, [['Carbadchatha', 'concept', 7, 'armour', 'Tracked armour of the later Ages: heavy battle-chariots of iron, two designs in three marks and three paints. A driver sits in the hull, the commander and gunner under the turret; the gun turns all the way round.']]
for v in man['vehicles']:
    for sfx in v['camo']:
        key = v['id'] + sfx
        nat = archaic.text(CLS[v['id']] + ('' if not sfx else ' ' + CAMO[sfx][1]))
        names[ID(key)] = {'diaithiris': nat, 'english': v['name'] + f' ({CAMO[sfx][0]})'}
        ages[ID(key)] = v['age']
    nat = archaic.text(CLS[v['id']])
    lib.append([nat, 'item', 7 if v['age'] == 'VII' else 8, 'armour', f"{v['name']}: {v['desc']} Crew of two or more; built at the vehicle bench from iron, redstone and gunpowder. Forest, desert and winter paint."])
for pid in man['parts'] + man['bullets']:
    nat = archaic.text(CLS[pid]); names[ID(pid)] = {'diaithiris': nat, 'english': EN[pid]}
    age = 'RODOS' if pid == 'tank_gun_125' else 'VII'
    ages[ID(pid)] = age
    lib.append([nat, 'item', 7 if age == 'VII' else 8, 'armour', f'{EN[pid]}: part for the armoured vehicles of Age {age}; fitted on the vehicle or crafted at the part bench.'])
json.dump({'_doc': 'Same shape as DiaithirRPG content/tech_ages.json; ids are Immersive Vehicles item ids (mts:<packid>.<system name>).', 'items': ages}, open(f'{M}/age_gates_tanks.json', 'w'), indent=1, ensure_ascii=False)
json.dump(names, open(f'{M}/names_map_tanks.json', 'w'), indent=1, ensure_ascii=False)
import os; os.makedirs(f'{M}/library', exist_ok=True); os.makedirs(f'{M}/codex', exist_ok=True)
json.dump(lib, open(f'{M}/library/tanks_library.json', 'w'), indent=1, ensure_ascii=False)
chap = [{'tier': 'tanks', 'name': archaic.text('Carbadchatha'), 'age': 'VII', 'text': lib[0][4],
         'items': [{'id': ID(v['id']), 'name': archaic.text(CLS[v['id']]), 'age': v['age'], 'text': v['desc']} for v in man['vehicles']]},
        {'tier': 'parts', 'name': 'Ballaí', 'age': 'VII', 'text': 'Tracks, engine, seat, guns and shells.',
         'items': [{'id': ID(p), 'name': archaic.text(CLS[p]), 'age': ages[ID(p)], 'text': EN[p]} for p in man['parts'] + man['bullets']]}]
json.dump({'id': 'diaithirtanks:handbook', 'title': archaic.text('Carbadchatha'), 'chapters': chap}, open(f'{M}/codex/tanks_handbook.json', 'w'), indent=1, ensure_ascii=False)
print(len(names), 'names', len(ages), 'age gates', len(lib), 'library entries')
