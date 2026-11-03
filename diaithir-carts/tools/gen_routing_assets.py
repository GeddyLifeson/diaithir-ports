#!/usr/bin/env python3
"""(S6886) Assets and data of the one-way rail, destination rail and cart loader (adds to the lang, age gate, tags and library files gen_assets.py writes)."""
import json, os
from PIL import Image
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'); R = os.path.join(ROOT, 'src/main/resources'); ID = 'diaithircarts'
def w(path, data):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(data, open(p, 'w'), indent=1)
def rj(path):
    return json.load(open(os.path.join(R, path)))
TEX = os.path.join(R, f'assets/{ID}/textures/block')
base = Image.open(f'{TEX}/steel_rail.png').convert('RGBA')

def marked(name, painter):
    im = base.copy(); painter(im.load()); im.save(f'{TEX}/{name}.png')
def arrow(px):   # yellow arrow pointing up the rail, between the steel rails
    for y in range(3, 13):
        px[7, y] = px[8, y] = (240, 190, 30, 255)
    for i in range(4):
        for x in range(7 - i, 9 + i): px[x, 3 + i] = (240, 190, 30, 255)
def dot(colour):
    def f(px):
        for x in range(6, 10):
            for y in range(6, 10): px[x, y] = colour
    return f
marked('one_way_rail', arrow)
marked('destination_rail', dot((90, 120, 220, 255)))
marked('destination_rail_on', dot((120, 255, 140, 255)))

FLAT, NE, SW = 'minecraft:block/rail_flat', 'minecraft:block/template_rail_raised_ne', 'minecraft:block/template_rail_raised_sw'
def model(name, parent, tex): w(f'assets/{ID}/models/block/{name}.json', {'parent': parent, 'textures': {'rail': f'{ID}:block/{tex}'}})
SLOPES = {'ascending_east': ('ne', 90), 'ascending_north': ('ne', 0), 'ascending_south': ('sw', 0), 'ascending_west': ('sw', 90)}
CORNERS = {'south_east': 0, 'south_west': 90, 'north_west': 180, 'north_east': 270}
def v(m, y=0):
    d = {'model': f'{ID}:block/{m}'}
    if y: d['y'] = y
    return d
# one-way: the arrow points north or east; reversed turns the model half a circle
model('one_way_rail', FLAT, 'one_way_rail'); model('one_way_rail_raised_ne', NE, 'one_way_rail'); model('one_way_rail_raised_sw', SW, 'one_way_rail')
model('one_way_rail_corner', 'minecraft:block/rail_curved', 'steel_rail_corner')
var = {}
for rev in (0, 1):
    p = f'reversed={"true" if rev else "false"}'; flip = 180 * rev
    var[f'{p},shape=north_south'] = v('one_way_rail', flip % 360)
    var[f'{p},shape=east_west'] = v('one_way_rail', (90 + flip) % 360)
    for s, y in CORNERS.items(): var[f'{p},shape={s}'] = v('one_way_rail_corner', y)
    for s, (k, y) in SLOPES.items(): var[f'{p},shape={s}'] = v(f'one_way_rail_raised_{k}', (y + flip) % 360)
w(f'assets/{ID}/blockstates/one_way_rail.json', {'variants': var})
# destination: the marker is lit while powered
var = {}
for on in (False, True):
    t = 'destination_rail_on' if on else 'destination_rail'; sfx = '_on' if on else ''
    model(t, FLAT, t); model(t + '_raised_ne', NE, t); model(t + '_raised_sw', SW, t)
    p = f'powered={"true" if on else "false"}'
    var[f'{p},shape=north_south'] = v(t); var[f'{p},shape=east_west'] = v(t, 90)
    for s, y in CORNERS.items(): var[f'{p},shape={s}'] = v('one_way_rail_corner', y)
    for s, (k, y) in SLOPES.items(): var[f'{p},shape={s}'] = v(f'{t}_raised_{k}', y)
w(f'assets/{ID}/blockstates/destination_rail.json', {'variants': var})
for n in ('one_way_rail', 'destination_rail'):
    w(f'assets/{ID}/items/{n}.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:item/{n}'}})
    w(f'assets/{ID}/models/item/{n}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{ID}:block/{n}'}})

# cart loader: a hopper-like frame with an arrow showing the direction of travel
def loader(name, down):
    im = Image.new('RGBA', (16, 16), (66, 72, 82, 255)); px = im.load()
    for i in range(16):
        for xy in ((i, 0), (i, 15), (0, i), (15, i)): px[xy] = (36, 40, 48, 255)
    c = (240, 190, 30, 255)
    for y in range(4, 12):
        px[7, y] = px[8, y] = c
    for i in range(4):
        yy = 11 - i if down else 4 + i
        for x in range(7 - i, 9 + i): px[x, yy] = c
    im.save(f'{TEX}/{name}.png')
loader('cart_loader_load', True); loader('cart_loader_unload', False)
w(f'assets/{ID}/models/block/cart_loader_load.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{ID}:block/cart_loader_load'}})
w(f'assets/{ID}/models/block/cart_loader_unload.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{ID}:block/cart_loader_unload'}})
w(f'assets/{ID}/blockstates/cart_loader.json', {'variants': {'unload=false': {'model': f'{ID}:block/cart_loader_load'}, 'unload=true': {'model': f'{ID}:block/cart_loader_unload'}}})
w(f'assets/{ID}/items/cart_loader.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:block/cart_loader_load'}})

for n in ('one_way_rail', 'destination_rail', 'cart_loader'):
    w(f'data/{ID}/loot_table/blocks/{n}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:{n}'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w(f'data/{ID}/recipe/one_way_rail.json', {'type': 'minecraft:crafting_shapeless', 'category': 'redstone', 'ingredients': [f'{ID}:steel_rail', 'minecraft:arrow'], 'result': {'id': f'{ID}:one_way_rail', 'count': 1}})
w(f'data/{ID}/recipe/destination_rail.json', {'type': 'minecraft:crafting_shapeless', 'category': 'redstone', 'ingredients': [f'{ID}:steel_rail', 'minecraft:name_tag', 'minecraft:redstone'], 'result': {'id': f'{ID}:destination_rail', 'count': 1}})
w(f'data/{ID}/recipe/cart_loader.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': ['IHI', 'IPI'],
  'key': {'I': 'minecraft:iron_ingot', 'H': 'minecraft:hopper', 'P': 'minecraft:piston'}, 'result': {'id': f'{ID}:cart_loader', 'count': 1}})
for rel in ('data/minecraft/tags/block/rails.json', 'data/minecraft/tags/item/rails.json', 'data/minecraft/tags/block/mineable/pickaxe.json'):
    t = rj(rel)
    for n in ('one_way_rail', 'destination_rail') + (('cart_loader',) if 'pickaxe' in rel else ()):
        if f'{ID}:{n}' not in t['values']: t['values'].append(f'{ID}:{n}')
    w(rel, t)
w('data/minecraft/tags/block/needs_iron_tool.json', {'values': [f'{ID}:cart_loader']})
lp = f'assets/{ID}/lang/en_us.json'; l = rj(lp); l.update({f'block.{ID}.one_way_rail': 'One-Way Rail', f'block.{ID}.destination_rail': 'Destination Rail', f'block.{ID}.cart_loader': 'Cart Loader'}); w(lp, l)
ap = f'data/{ID}/age_gate.json'; a = rj(ap); a['routing'] = {'age': 5, 'items': [f'{ID}:one_way_rail', f'{ID}:destination_rail', f'{ID}:cart_loader']}; w(ap, a)
cp = os.path.join(ROOT, '..', 'library-sync/diaithircarts.json'); c = json.load(open(cp))
new = [('one_way_rail', 'One-Way Rail', 'A rail that lets carts through one way only. The arrow on it points north or east; use it bare-handed to turn it round. A cart that rolls on against the arrow is turned back. Place it on a straight stretch. Age V.'),
 ('destination_rail', 'Destination Rail', 'Routes carts by name. It gives a redstone signal of 15 while a cart whose name matches its channel stands on it. Use it bare-handed to cycle the channel: any cart, or the letters A to P, matched against the cart name (name a cart "C" with a name tag). Power a three-way junction rail with it and the named carts take the branch. Age V.'),
 ('cart_loader', 'Cart Loader', 'Stands over a rail with a chest, barrel or tank on top. Every 8 ticks it moves up to 8 items, or 250 mB of fluid, between the container and the cart standing under it: loading from the container, or unloading into it (use it bare-handed to flip). Works on any cart that offers an item or fluid handler, including chest carts, hopper carts and the tank wagons from the yards. Redstone switches it off. Age V.')]
for n, name, text in new:
    if not any(e['id'] == f'{ID}:{n}' for e in c): c.append({'id': f'{ID}:{n}', 'name': name, 'text': text})
json.dump(c, open(cp, 'w'), indent=1)

# ---- locking rail: a padlock-style marker, amber for latch, green for interlock
def lock(colour):
    def f(px):
        for x in range(5, 11):
            for y in range(7, 12): px[x, y] = colour
        for x, y in ((6, 5), (7, 4), (8, 4), (9, 5), (6, 6), (9, 6)): px[x, y] = colour
    return f
marked('locking_rail', lock((240, 160, 40, 255))); marked('locking_rail_interlock', lock((70, 210, 120, 255)))
var = {}
for mode, t in ((0, 'locking_rail'), (1, 'locking_rail_interlock')):
    model(t, FLAT, t); model(t + '_raised_ne', NE, t); model(t + '_raised_sw', SW, t)
    p = f'mode={mode}'
    var[f'{p},shape=north_south'] = v(t); var[f'{p},shape=east_west'] = v(t, 90)
    for s, (k, y) in SLOPES.items(): var[f'{p},shape={s}'] = v(f'{t}_raised_{k}', y)
w(f'assets/{ID}/blockstates/locking_rail.json', {'variants': var})
w(f'assets/{ID}/items/locking_rail.json', {'model': {'type': 'minecraft:model', 'model': f'{ID}:item/locking_rail'}})
w(f'assets/{ID}/models/item/locking_rail.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{ID}:block/locking_rail'}})
w(f'data/{ID}/loot_table/blocks/locking_rail.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{ID}:locking_rail'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
w(f'data/{ID}/recipe/locking_rail.json', {'type': 'minecraft:crafting_shapeless', 'category': 'redstone', 'ingredients': [f'{ID}:station_rail', 'minecraft:iron_bars'], 'result': {'id': f'{ID}:locking_rail', 'count': 1}})
for rel in ('data/minecraft/tags/block/rails.json', 'data/minecraft/tags/item/rails.json', 'data/minecraft/tags/block/mineable/pickaxe.json'):
    t = rj(rel)
    if f'{ID}:locking_rail' not in t['values']: t['values'].append(f'{ID}:locking_rail')
    w(rel, t)
l = rj(lp); l[f'block.{ID}.locking_rail'] = 'Locking Rail'; w(lp, l)
a = rj(ap); a['routing']['items'].append(f'{ID}:locking_rail') if f'{ID}:locking_rail' not in a['routing']['items'] else None; w(ap, a)
c = json.load(open(cp))
if not any(e['id'] == f'{ID}:locking_rail' for e in c):
    c.append({'id': f'{ID}:locking_rail', 'name': 'Locking Rail', 'text': 'Stops a cart that rolls onto it and keeps it there. Use it bare-handed to change mode. Latch (amber) holds the cart until the rail goes from unpowered to powered, a redstone pulse; a rail already powered when the cart arrived does not release it. Interlock (green) holds the cart until the 32 rails ahead are clear of other carts, then lets it go. Age V.'})
    json.dump(c, open(cp, 'w'), indent=1)
