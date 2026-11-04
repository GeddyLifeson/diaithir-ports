#!/usr/bin/env python3
# S6922 Writes the recipes for the hand-written Stellaris pieces (rocket, suit, oxygen module, distributor, fluid cell). Run after realms_gen.py stellaris.
import json, pathlib
RES = pathlib.Path(__file__).resolve().parent.parent / 'src/main/resources/data/stellaris/recipe'
RES.mkdir(parents=True, exist_ok=True)
S = lambda i: f'stellaris:{i}'
def shaped(name, pattern, key, result, count=1, cat='misc'):
    r = {'id': S(result)} if count == 1 else {'id': S(result), 'count': count}
    (RES / f'{name}.json').write_text(json.dumps({'type': 'minecraft:crafting_shaped', 'category': cat, 'key': key, 'pattern': pattern, 'result': r}, indent=1) + '\n')
Ti, De, Pl = S('titanium_ingot'), S('desh_ingot'), S('titanium_plating_block')
shaped('rocket_nose_cone', [' # ', '###', '###'], {'#': Ti}, 'rocket_nose_cone')
shaped('rocket_fin', ['# ', '##', '##'], {'#': Ti}, 'rocket_fin', 2)
shaped('rocket_engine', ['#D#', '#B#', 'F F'], {'#': Ti, 'D': De, 'B': 'minecraft:blast_furnace', 'F': 'minecraft:flint_and_steel'}, 'rocket_engine')
shaped('rocket', [' N ', 'PEP', 'F F'], {'N': S('rocket_nose_cone'), 'P': Pl, 'E': S('rocket_engine'), 'F': S('rocket_fin')}, 'rocket', cat='equipment')
shaped('fluid_cell', [' # ', '#C#', ' # '], {'#': 'minecraft:iron_nugget', 'C': 'minecraft:coal'}, 'fluid_cell', 4)
shaped('oxygen_distributor', ['#G#', 'GAG', '#P#'], {'#': Ti, 'G': 'minecraft:glass', 'A': 'minecraft:copper_ingot', 'P': 'minecraft:lightning_rod'}, 'oxygen_distributor')
for n, pat in (('helmet', ['###', '#G#']), ('chestplate', ['# #', '###', '###']), ('leggings', ['###', '# #', '# #']), ('boots', ['# #', '# #'])):
    shaped(f'space_suit_{n}', pat, {'#': De, 'G': 'minecraft:glass'} if n == 'helmet' else {'#': De}, f'space_suit_{n}', cat='equipment')
shaped('space_suit_oxygen_module_tier_1', ['#G#', 'GFG', '#G#'], {'#': Ti, 'G': 'minecraft:glass', 'F': S('fluid_cell')}, 'space_suit_oxygen_module_tier_1')
for t in (2, 3):
    shaped(f'space_suit_oxygen_module_tier_{t}', ['#M#', 'MDM', '#M#'], {'#': Ti, 'M': S(f'space_suit_oxygen_module_tier_{t-1}'), 'D': De}, f'space_suit_oxygen_module_tier_{t}')
print('ok')
