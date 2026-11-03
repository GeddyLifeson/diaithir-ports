#!/usr/bin/env python3
"""S6841 Library gear recipes, loot tables and progression nodes. Run from the module dir.
Recipes: one shapeless crafting recipe per class/slot x Age gate (19 x 9) whose result carries forge_age; the first time the stack is held it becomes a real Library entry of that class and Age on the holder's planet (LibraryGear.resolve), so every one of the 99,823 entries is reachable. Materials follow the progression graph's Age chain. Loot: one table per worldgen site class, using the library_gear function (entry of the chest's planet). Nodes: library_gear_nodes.json in the graph.json node schema (owner merges)."""
import json, os
R = 'src/main/resources/data/diaithircombat/'
AGES = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'RODOS']
# Age material tags: unions of real pack items (ids from packmap/item_to_library.tsv and the diaithirarms jar); optional entries are skipped when the mod is absent.
MATTAG = {1: ['minecraft:flint'], 2: ['minecraft:copper_ingot'],
          3: ['#c:ingots/bronze'], 4: ['minecraft:iron_ingot'], 5: ['#c:ingots/steel'], 6: ['#c:plates/steel'],
          7: ['#c:ingots/tempered'], 8: ['minecraft:netherite_ingot'], 9: ['#c:ingots/rodos_alloy']}
CTAGS = {  # c: convention tags we fill (private tags also go into the c: tag, INTEGRATION.md section 1)
 'ingots/bronze': ['modern_industrialization:bronze_ingot', 'mekanism:ingot_bronze', 'railcraft:bronze_ingot', 'silentgear:bronze_ingot'],
 'ingots/steel': ['modern_industrialization:steel_ingot', 'mekanism:ingot_steel', 'energizedpower:steel_ingot', 'railcraft:steel_ingot', 'oritech:steel_ingot', 'tconstruct:steel_ingot'],
 'plates/steel': ['modern_industrialization:steel_plate', 'energizedpower:steel_plate', 'railcraft:steel_plate'],
 'ingots/tempered': ['diaithirarms:tempered_ingot'],
 'ingots/rodos_alloy': ['diaithirarms:rodos_alloy_ingot']}
NODEMAT = {1: 'tool.flint', 2: 'c:ingots/copper', 3: 'c:ingots/bronze', 4: 'c:ingots/iron', 5: 'c:ingots/steel', 6: 'c:plates/steel', 7: 'c:ingots/tempered', 8: 'c:ingots/netherite', 9: 'c:ingots/rodos_alloy'}
MAT = {g: f'#diaithircombat:gear_material/a{g}' for g in range(1, 10)}
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
    os.makedirs('src/main/resources/data/c/tags/item/ingots', exist_ok=True); os.makedirs('src/main/resources/data/c/tags/item/plates', exist_ok=True)
    os.makedirs(R + 'tags/item/gear_material', exist_ok=True)
    for t, ids in CTAGS.items():
        json.dump({'replace': False, 'values': [{'id': i, 'required': False} for i in ids]}, open(f'src/main/resources/data/c/tags/item/{t}.json', 'w'), indent=1)
    for g, vals in MATTAG.items():
        json.dump({'replace': False, 'values': [{'id': v, 'required': False} if v.startswith('#') or not v.startswith('minecraft:') else v for v in vals]},
                  open(R + f'tags/item/gear_material/a{g}.json', 'w'), indent=1)
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
        needs = ([f'gate.{a}'] if g > 1 else []) + [NODEMAT[g]] + ([f'libgear.a{g - 1}'] if g > 1 else [])
        nodes.append({'id': f'combat.gear_a{g}', 'system': 'combat', 'age': a, 'name': f'Forge Age {a} gear', 'needs': needs, 'makes': [f'libgear.a{g}'],
                      'module': 'diaithir-combat-classes', 'quest': f'q.combat.gear_a{g}', 'item': 'diaithircombat:lib_sword',
                      'note': 'Needs the previous Age gear (libgear). Any lib_<class> recipe of this Age; the stack becomes a real Library entry of that class and Age (forge_age component).'})
    json.dump({'_doc': 'Library gear nodes in graph.json schema; the progression owner merges them. Forging needs the Age gate, the Age metal and leather; entries also drop from site chests (loot_table/gear).', 'nodes': nodes},
              open('library_gear_nodes.json', 'w'), indent=1)
    print(len(COUNT) * 9, 'recipes', len(SITE), 'loot tables', len(nodes), 'nodes')
main()
