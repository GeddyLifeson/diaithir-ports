#!/usr/bin/env python3
"""S6841 Library gear recipes, loot tables and progression nodes. Run from the module dir.
Recipes: one shapeless crafting recipe per class/slot x Age gate (19 x 9) whose result carries forge_age; the first time the stack is held it becomes a real Library entry of that class and Age on the holder's planet (LibraryGear.resolve), so every one of the 99,823 entries is reachable. Materials follow the progression graph's Age chain. Loot: one table per worldgen site class, using the library_gear function (entry of the chest's planet). Nodes: library_gear_nodes.json in the graph.json node schema (owner merges)."""
import json, os
R = 'src/main/resources/data/diaithircombat/'
AGES = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'RODOS']
MAT = {1: 'minecraft:flint', 2: '#c:ingots/copper', 3: '#c:ingots/bronze', 4: '#c:ingots/iron', 5: '#c:ingots/steel', 6: '#c:plates/steel',
       7: '#c:ingots/hollowsteel', 8: '#c:plates/nanoalloy', 9: '#c:ingots/netherite'}
COUNT = {'dagger': 1, 'sword': 2, 'rapier': 2, 'greatsword': 4, 'axe': 3, 'battleaxe': 5, 'mace': 3, 'warhammer': 5, 'spear': 2, 'polearm': 3, 'staff': 1,
         'bow': 1, 'longbow': 2, 'head': 5, 'chest': 8, 'legs': 7, 'feet': 4, 'shield': 6, 'trinket': 2}
EXTRA = {'bow': ['minecraft:string', 'minecraft:string', 'minecraft:stick'], 'longbow': ['minecraft:string', 'minecraft:string', 'minecraft:stick'],
         'staff': ['minecraft:stick', 'minecraft:stick']}
HANDLE = ['minecraft:stick']
SITE = {  # site class -> (kind, classes)
 'fortress': ('any', []), 'tomb': ('any', []), 'forge': ('any', []), 'ruin': ('any', []), 'delve': ('weapon', []),
 'tower': ('weapon', ['bow', 'longbow', 'staff', 'spear']), 'harbor': ('weapon', ['spear', 'bow', 'dagger']),
 'temple': ('any', ['staff', 'mace', 'trinket', 'head']), 'hall': ('any', ['sword', 'shield', 'head', 'chest', 'trinket']),
 'garden': ('armor', ['trinket', 'feet']), 'landmark': ('any', ['trinket', 'staff']), 'bridge': ('weapon', ['spear', 'polearm'])}

def main():
    os.makedirs(R + 'recipe', exist_ok=True); os.makedirs(R + 'loot_table/gear', exist_ok=True)
    for cls, n in COUNT.items():
        for g in range(1, 10):
            ing = [MAT[g]] * n + EXTRA.get(cls, [] if cls in ('head', 'chest', 'legs', 'feet', 'shield', 'trinket') else HANDLE)
            ing = ing[:9]
            cond = [{'type': 'neoforge:not', 'value': {'type': 'neoforge:tag_empty', 'tag': i[1:]}} for i in sorted(set(ing)) if i.startswith('#')]
            json.dump({'neoforge:conditions': cond, '_age': AGES[g - 1], 'type': 'minecraft:crafting_shapeless', 'category': 'equipment',
                       'ingredients': ing, 'result': {'id': f'diaithircombat:lib_{cls}', 'count': 1, 'components': {'diaithircombat:forge_age': g}}},
                      open(R + f'recipe/lib_{cls}_a{g}.json', 'w'), indent=1)
    for site, (kind, classes) in SITE.items():
        fn = {'function': 'diaithircombat:library_gear', 'kind': kind, 'classes': classes, 'extra': 2}
        json.dump({'type': 'minecraft:chest', 'pools': [{'rolls': {'type': 'minecraft:uniform', 'min': 0, 'max': 1}, 'entries': [
            {'type': 'minecraft:item', 'name': 'diaithircombat:lib_sword', 'weight': 1, 'functions': [fn]}]}]},
            open(R + f'loot_table/gear/{site}.json', 'w'), indent=1)
    nodes = []
    for g in range(1, 10):
        a = AGES[g - 1]
        needs = ([f'gate.{a}'] if g > 1 else []) + [MAT[g].lstrip('#')] + ['leather']
        nodes.append({'id': f'combat.gear_a{g}', 'system': 'combat', 'age': a, 'name': f'Forge Age {a} gear', 'needs': needs, 'makes': [f'libgear.a{g}'],
                      'module': 'diaithir-combat-classes', 'quest': f'q.combat.gear_a{g}', 'item': 'diaithircombat:lib_sword',
                      'note': 'Any lib_<class> recipe of this Age; the stack becomes a real Library entry of that class and Age (forge_age component).'})
    json.dump({'_doc': 'Library gear nodes in graph.json schema; the progression owner merges them. Forging needs the Age gate, the Age metal and leather; entries also drop from site chests (loot_table/gear).', 'nodes': nodes},
              open('library_gear_nodes.json', 'w'), indent=1)
    print(len(COUNT) * 9, 'recipes', len(SITE), 'loot tables', len(nodes), 'nodes')
main()
