#!/usr/bin/env python3
"""(S6910) Textures, models, blockstates, item definitions, lang, loot, tags, age gate, library entries and station recipes of the freight layer (FREIGHT.md)."""
import csv, json, os
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'); R = os.path.join(ROOT, 'src/main/resources'); NS = 'diaithircarts'
A = f'{R}/assets/{NS}'; D = f'{R}/data/{NS}'
for d in ('textures/block', 'textures/item', 'models/block', 'models/item', 'blockstates', 'items'):
    os.makedirs(f'{A}/{d}', exist_ok=True)

def wj(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True); json.dump(data, open(path, 'w'), indent=1)
def rj(path, default):
    return json.load(open(path)) if os.path.exists(path) else default

PAINT = {'red': (170, 52, 40), 'blue': (44, 84, 150), 'white': (226, 230, 232)}
def shade(c, f): return tuple(max(0, min(255, int(v * f))) for v in c) + (255,)

def tex(name, painter, kind='block'):
    im = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(im); painter(d, im); im.save(f'{A}/textures/{kind}/{name}.png')

# ---- container walls and doors: corrugated sheet, door has bars and a handle
for p, c in PAINT.items():
    def wall(d, im, c=c):
        for x in range(16): d.line([(x, 0), (x, 15)], fill=shade(c, 1.1 if x % 4 < 2 else 0.8))
        d.rectangle([0, 0, 15, 15], outline=shade(c, 0.6))
    def door(d, im, c=c):
        for x in range(16): d.line([(x, 0), (x, 15)], fill=shade(c, 1.1 if x % 4 < 2 else 0.85))
        for x in (5, 10): d.line([(x, 1), (x, 14)], fill=(70, 70, 74, 255), width=1)
        for y in (3, 12): d.line([(3, y), (12, y)], fill=(50, 50, 54, 255))
        d.rectangle([7, 7, 8, 9], fill=(210, 190, 90, 255)); d.rectangle([0, 0, 15, 15], outline=shade(c, 0.55))
    tex(f'container_wall_{p}', wall); tex(f'container_door_{p}', door)

# ---- loads
WOOD = (150, 108, 64)
def pallet_side(d, im):
    d.rectangle([0, 0, 15, 15], fill=(196, 160, 110, 255))
    for x in (0, 5, 10): d.rectangle([x, 1, x + 4, 11], outline=(120, 90, 52, 255), fill=(184, 146, 96, 255))
    d.rectangle([0, 12, 15, 15], fill=shade(WOOD, 0.9)); d.line([(0, 13), (15, 13)], fill=shade(WOOD, 0.6))
def pallet_top(d, im):
    d.rectangle([0, 0, 15, 15], fill=(196, 160, 110, 255))
    for x in (0, 8):
        for y in (0, 8): d.rectangle([x, y, x + 7, y + 7], outline=(120, 90, 52, 255), fill=(190, 152, 100, 255))
def barrel_side(d, im):
    for x in range(16): d.line([(x, 0), (x, 15)], fill=shade(WOOD, 1.05 if x % 3 else 0.8))
    for y in (2, 13): d.line([(0, y), (15, y)], fill=(70, 70, 76, 255), width=2)
def barrel_top(d, im):
    d.rectangle([0, 0, 15, 15], fill=shade(WOOD, 0.75)); d.ellipse([1, 1, 14, 14], fill=shade(WOOD, 1.0), outline=(70, 70, 76, 255))
def chest(d, im):
    d.rectangle([0, 0, 15, 15], fill=shade(WOOD, 1.0), outline=shade(WOOD, 0.5))
    d.rectangle([0, 6, 15, 8], fill=(70, 70, 76, 255)); d.rectangle([7, 5, 8, 10], fill=(210, 190, 90, 255))
def crate(d, im):
    d.rectangle([0, 0, 15, 15], fill=shade(WOOD, 1.1), outline=shade(WOOD, 0.5))
    d.line([(1, 1), (14, 14)], fill=shade(WOOD, 0.6), width=2); d.line([(14, 1), (1, 14)], fill=shade(WOOD, 0.6), width=2)
def unit(d, im):
    d.rectangle([0, 0, 15, 15], fill=(176, 182, 188, 255), outline=(90, 94, 100, 255))
    for y in range(3, 14, 2): d.line([(3, y), (12, y)], fill=(70, 74, 80, 255))
    d.rectangle([11, 1, 14, 2], fill=(90, 200, 120, 255))
def vessel(d, im):
    d.rectangle([0, 0, 15, 15], fill=(228, 232, 234, 255))
    for y in (0, 7, 15): d.line([(0, y), (15, y)], fill=(150, 156, 162, 255))
    d.rectangle([0, 0, 15, 15], outline=(160, 166, 172, 255))
def dock_front(rift):
    def f(d, im):
        c = (110, 70, 160) if rift else (205, 170, 40)
        d.rectangle([0, 0, 15, 15], fill=(80, 84, 90, 255))
        for i in range(-16, 16, 6):
            d.polygon([(i, 15), (i + 3, 15), (i + 19, 0), (i + 16, 0)], fill=c + (255,))
        d.rectangle([3, 3, 12, 12], fill=(30, 32, 36, 255))
    return f
def dock_side(rift):
    def f(d, im):
        d.rectangle([0, 0, 15, 15], fill=(96, 100, 108, 255), outline=(60, 62, 68, 255))
        d.rectangle([6, 6, 9, 9], fill=((140, 100, 200) if rift else (205, 170, 40)) + (255,))
    return f
tex('freight_pallet_side', pallet_side); tex('freight_pallet_top', pallet_top)
tex('freight_barrel_side', barrel_side); tex('freight_barrel_top', barrel_top)
tex('freight_chest', chest); tex('freight_crate', crate); tex('reefer_unit', unit); tex('tank_vessel', vessel)
tex('loading_dock_front', dock_front(False)); tex('loading_dock_side', dock_side(False))
tex('rift_dock_front', dock_front(True)); tex('rift_dock_side', dock_side(True))

# ---- item icons
def box_icon(p, tank=False, cold=False):
    def f(d, im):
        c = PAINT[p]
        d.rectangle([1, 4, 14, 12], fill=shade(c, 1.0), outline=shade(c, 0.55))
        for x in range(2, 14, 3): d.line([(x, 5), (x, 11)], fill=shade(c, 0.8))
        if tank: d.ellipse([3, 5, 12, 11], fill=(235, 238, 240, 255), outline=(150, 156, 162, 255))
        if cold: d.rectangle([2, 5, 4, 11], fill=(176, 182, 188, 255))
    return f
for n, p, t, c in (('container_20', 'red', 0, 0), ('container_40', 'blue', 0, 0), ('reefer_20', 'white', 0, 1), ('reefer_40', 'white', 0, 1), ('tank_20', 'white', 1, 0)):
    tex(n, box_icon(p, t, c), 'item')
def hook(d, im):
    d.line([(8, 1), (8, 8)], fill=(150, 154, 160, 255), width=2); d.arc([4, 6, 12, 15], 20, 200, fill=(150, 154, 160, 255), width=2)
    d.rectangle([6, 0, 10, 2], fill=(210, 190, 90, 255))
tex('cargo_hook', hook, 'item')

# ---- models and states
def model(name, parent, textures, kind='block'):
    wj(f'{A}/models/{kind}/{name}.json', {'parent': parent, 'textures': textures})
def itemdef(name, m):
    wj(f'{A}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': m}})
def simple(name, parent, textures):
    model(name, parent, textures); wj(f'{A}/blockstates/{name}.json', {'variants': {'': {'model': f'{NS}:block/{name}'}}})
    if name.startswith('freight_'): itemdef(name, f'{NS}:block/{name}')
T = lambda n: f'{NS}:block/{n}'
def column(name, side, end):
    simple(name, 'minecraft:block/cube_column', {'side': T(side), 'end': T(end)})
column('freight_pallet', 'freight_pallet_side', 'freight_pallet_top'); column('freight_barrel', 'freight_barrel_side', 'freight_barrel_top')
simple('freight_chest', 'minecraft:block/cube_all', {'all': T('freight_chest')}); simple('freight_crate', 'minecraft:block/cube_all', {'all': T('freight_crate')})
simple('reefer_unit', 'minecraft:block/cube_all', {'all': T('reefer_unit')}); simple('tank_vessel', 'minecraft:block/cube_all', {'all': T('tank_vessel')})
for p in PAINT:
    model(f'container_wall_{p}', 'minecraft:block/cube_all', {'all': T(f'container_wall_{p}')})
    model(f'container_door_{p}', 'minecraft:block/cube_all', {'all': T(f'container_door_{p}')})
for n in ('container_wall', 'container_core'):
    wj(f'{A}/blockstates/{n}.json', {'variants': {f'paint={p}': {'model': T(f'container_wall_{p}')} for p in PAINT}})
wj(f'{A}/blockstates/container_door.json', {'variants': {f'open={o},paint={p}': {'model': T(f'container_door_{p}')} for p in PAINT for o in ('true', 'false')}})
YROT = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
for n in ('loading_dock', 'rift_dock'):
    model(n, 'minecraft:block/orientable', {'top': T(f'{n}_side'), 'side': T(f'{n}_side'), 'front': T(f'{n}_front')})
    wj(f'{A}/blockstates/{n}.json', {'variants': {f'facing={f}': ({'model': T(n)} | ({'y': y} if y else {})) for f, y in YROT.items()}}); itemdef(n, T(n))
for n in ('container_20', 'container_40', 'reefer_20', 'reefer_40', 'tank_20', 'cargo_hook'):
    model(n, 'minecraft:item/generated', {'layer0': f'{NS}:item/{n}'}, 'item'); itemdef(n, f'{NS}:item/{n}')

# ---- lang
LANG = {'freight_pallet': 'Pallet', 'freight_barrel': 'Barrel', 'freight_chest': 'Chest (freight)', 'freight_crate': 'Crate', 'container_wall': 'Container wall',
        'container_core': 'Container corner', 'container_door': 'Container door', 'reefer_unit': 'Refrigeration unit', 'tank_vessel': 'Tank vessel',
        'loading_dock': 'Loading dock', 'rift_dock': 'Rift dock'}
ITEMLANG = {'container_20': 'Shipping container, 20 ft', 'container_40': 'Shipping container, 40 ft', 'reefer_20': 'Reefer container, 20 ft',
            'reefer_40': 'Reefer container, 40 ft', 'tank_20': 'Tank container, 20 ft', 'cargo_hook': 'Cargo hook'}
lp = f'{A}/lang/en_us.json'; lang = rj(lp, {})
for k, v in LANG.items(): lang[f'block.{NS}.{k}'] = v
for k, v in ITEMLANG.items(): lang[f'item.{NS}.{k}'] = v
wj(lp, lang)

# ---- loot: loads keep their items, docks drop themselves
def loot(name, keep):
    ent = {'type': 'minecraft:item', 'name': f'{NS}:{name}'}
    if keep: ent['functions'] = [{'function': 'minecraft:copy_components', 'source': 'block_entity', 'include': ['minecraft:container']}]
    wj(f'{D}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'random_sequence': f'{NS}:blocks/{name}',
        'pools': [{'rolls': 1, 'bonus_rolls': 0, 'conditions': [{'condition': 'minecraft:survives_explosion'}], 'entries': [ent]}]})
for n in ('freight_pallet', 'freight_barrel', 'freight_chest', 'freight_crate'): loot(n, True)
for n in ('loading_dock', 'rift_dock'): loot(n, False)

# ---- tags: loads by axe, docks and container parts by pickaxe
def tag(path, values):
    p = f'{R}/data/minecraft/tags/block/{path}.json'; t = rj(p, {'replace': False, 'values': []})
    t['values'] = sorted(set(t['values']) | {f'{NS}:{v}' for v in values}); wj(p, t)
tag('mineable/axe', ['freight_pallet', 'freight_barrel', 'freight_chest', 'freight_crate'])
tag('mineable/pickaxe', ['loading_dock', 'rift_dock', 'container_wall', 'container_core', 'container_door', 'reefer_unit', 'tank_vessel'])

# ---- age gate (items a station may make / a player may hold)
ag = f'{D}/age_gate.json'; gate = rj(ag, {})
for age, items in ((1, ['freight_chest']), (2, ['freight_pallet', 'freight_barrel', 'freight_crate']), (5, ['loading_dock']),
                   (6, ['container_20', 'container_40', 'reefer_20', 'reefer_40', 'tank_20', 'cargo_hook']), (8, ['rift_dock'])):
    gate[f'freight_age{age}'] = {'age': age, 'items': [f'{NS}:{i}' for i in items]}
wj(ag, gate)

# ---- library entries
lib = f'{ROOT}/../library-sync/{NS}.json'; entries = rj(lib, [])
TEXT = {
 'freight_chest': 'Chest for freight. Holds 27 stacks. Break it and the items stay inside the item you get. Age I.',
 'freight_pallet': 'Pallet load. Holds 18 stacks and carries up to 1.5 t. Stacks two or three high inside a container. Age II.',
 'freight_barrel': 'Barrel. Holds 18 stacks and carries up to 300 kg. Age II.',
 'freight_crate': 'Crate. Holds 54 stacks and carries up to 1.2 t. Age II.',
 'container_20': 'Shipping container, 20 ft. A walkable box with a 2 by 2 by 5 block interior, rated 28 t. Set it down with the item; swing the door at the far end by using it; lift it back into one item, cargo and all, with a cargo hook or a crane. Age VI.',
 'container_40': 'Shipping container, 40 ft. Interior 2 by 2 by 11 blocks, rated 26.7 t. Age VI.',
 'reefer_20': 'Reefer container, 20 ft. A container with a refrigeration unit at the closed end, drawing 4 kW. Interior 2 by 2 by 4. Age VI.',
 'reefer_40': 'Reefer container, 40 ft. Interior 2 by 2 by 10, refrigeration unit at the closed end, 4 kW. Age VI.',
 'tank_20': 'Tank container, 20 ft. Holds 26,000 litres of fluid in an ISO frame. Pump it empty before lifting. Age VI.',
 'cargo_hook': 'Cargo hook. Use it on a container wall or door to lift the whole box into one item, up to 35 t. Age VI.',
 'loading_dock': 'Loading dock. Faces a container up to three blocks ahead and offers its cargo to storage network cables, hoppers, pipes and depot workers. The comparator reads how full the container is. Age V.',
 'rift_dock': 'Rift dock. Serves the container at its partner, the other rift dock on the same channel in any dimension. Bare hand cycles the channel. Age VIII.'}
have = {e['id'] for e in entries}
for k, v in TEXT.items():
    if f'{NS}:{k}' not in have:
        n = {**LANG, **ITEMLANG}[k]; entries.append({'id': f'{NS}:{k}', 'name': n, 'text': v})
wj(lib, entries)

# ---- station recipes: nothing here is crafted in the grid (rows for the station lanes: benches #178/#180/#182/#185, Foundry #163, container works)
rows = [
 ('freight_chest', 1, 'workshop_bench', 'planks:8;iron_nugget:2', 20), ('freight_pallet', 2, 'workshop_bench', 'planks:6;stick:6', 15),
 ('freight_barrel', 2, 'cooperage', 'planks:7;iron_ingot:2', 20), ('freight_crate', 2, 'workshop_bench', 'planks:8;iron_nugget:4;stick:4', 25),
 ('loading_dock', 5, 'machine_works', 'iron_ingot:12;redstone:4;hopper:1', 40),
 ('container_20', 6, 'container_works', 'steel_plate:24;iron_ingot:8;planks:6', 240), ('container_40', 6, 'container_works', 'steel_plate:44;iron_ingot:12;planks:10', 420),
 ('reefer_20', 6, 'container_works', 'container_20:1;compressor:1;copper_ingot:8', 300), ('reefer_40', 6, 'container_works', 'container_40:1;compressor:1;copper_ingot:12', 480),
 ('tank_20', 6, 'container_works', 'steel_plate:30;iron_ingot:12;glass:0', 300), ('cargo_hook', 6, 'machine_works', 'iron_ingot:6;chain:2', 30),
 ('rift_dock', 8, 'machine_works', 'loading_dock:1;ender_pearl:4;netherite_scrap:2', 120)]
rows[9] = ('tank_20', 6, 'container_works', 'steel_plate:30;iron_ingot:12;bucket:2', 300)
os.makedirs(f'{D}/freight', exist_ok=True)
with open(f'{D}/freight/station_recipes.csv', 'w', newline='') as f:
    w = csv.writer(f); w.writerow(['output', 'age', 'station', 'inputs', 'seconds']); w.writerows(rows)
print('freight assets written')
