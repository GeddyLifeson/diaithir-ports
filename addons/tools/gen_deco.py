#!/usr/bin/env python3
"""Generates every asset and data file of Create Deco (diaithir_createdeco): textures (original, procedural, from the
Create port's art engine), blockstates, models, item definitions, lang, loot tables, recipes and tags.
Ids here must match Content.java. Run: python3 gen_deco.py (from anywhere)."""
import json, os, sys, shutil, math
HERE = os.path.dirname(os.path.abspath(__file__))
ASSETGEN = os.path.abspath(os.path.join(HERE, '..', '..', 'tools', 'assetgen'))
sys.path.insert(0, ASSETGEN)
import art, icons64
from art import Cv, sh, mixc, STEEL, S
from PIL import Image, ImageDraw
import numpy as np

NS = 'createdeco'
ROOT = os.path.abspath(os.path.join(HERE, '..', 'createdeco', 'src', 'main', 'resources'))
A = os.path.join(ROOT, 'assets', NS)
D = os.path.join(ROOT, 'data', NS)
VAN = os.environ.get('MCSRC', '/home/user/mcsrc') + '/assets/minecraft'

METALS = ['andesite', 'zinc', 'copper', 'brass', 'iron', 'industrial_iron']
COIN_METALS = ['zinc', 'copper', 'brass', 'iron', 'industrial_iron', 'gold', 'netherite']
COLORS = {'blue': (64, 96, 164), 'dean': (200, 160, 62), 'dusk': (62, 58, 72), 'pearl': (214, 210, 196),
          'scarlet': (172, 52, 52), 'verdant': (70, 132, 82), 'umber': (122, 86, 60), 'red': (156, 72, 58)}
DYE = {'blue': 'blue', 'dean': 'yellow', 'dusk': 'black', 'pearl': 'white', 'scarlet': 'red', 'verdant': 'green', 'umber': 'brown'}
BTYPES = ['', 'short', 'tiled', 'long', 'corner', 'cracked', 'mossy']
DECALS = ['warning', 'creeper', 'skull', 'flow', 'ice', 'radioactive', 'top_left', 'up', 'top_right', 'left', 'cross', 'right',
          'down_left', 'down', 'down_right', 'fluid', 'fire', 'electrical', 'fire_diamond', 'no_entry']
LAMPS = {'yellow': (250, 214, 90), 'red': (230, 60, 50), 'green': (90, 210, 110), 'blue': (80, 140, 240)}
LAMP_ITEM = {'yellow': 'minecraft:torch', 'red': 'minecraft:redstone_torch', 'green': 'minecraft:glow_berries', 'blue': 'minecraft:soul_torch'}
MCOL = {'andesite': art.ANDESITE, 'zinc': art.ZINC, 'copper': art.COPPER, 'brass': art.BRASS, 'iron': (150, 154, 160),
        'industrial_iron': (80, 84, 94), 'gold': (222, 182, 60), 'netherite': (68, 60, 66)}
INGOT = {'andesite': 'create:andesite_alloy', 'zinc': 'create:zinc_ingot', 'copper': 'minecraft:copper_ingot',
         'brass': 'create:brass_ingot', 'iron': 'minecraft:iron_ingot', 'industrial_iron': NS + ':industrial_iron_ingot',
         'gold': 'minecraft:gold_ingot', 'netherite': 'minecraft:netherite_ingot'}
SHEET = {'andesite': NS + ':andesite_sheet', 'zinc': NS + ':zinc_sheet', 'copper': 'create:copper_sheet', 'brass': 'create:brass_sheet',
         'iron': 'create:iron_sheet', 'industrial_iron': NS + ':industrial_iron_sheet'}


def cap(s): return ' '.join(w[:1].upper() + w[1:] for w in s.split('_'))
def W(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        if isinstance(obj, str): f.write(obj)
        else: json.dump(obj, f, indent=1)

LANG = {}
BLOCKS = []   # (id, kind, extra)
ITEMS = []    # plain items
TEXN = set()

def tex_save(name, im):
    p = os.path.join(A, 'textures', 'block', name + '.png') if not name.startswith('item/') else os.path.join(A, 'textures', name + '.png')
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)

# ---------------------------------------------------------------------------------------------- textures
def tex_plate(m): return art.plate_tex(NS + 'plate' + m, MCOL[m], True, 0.5, 0.0)
def tex_flat(m): return art.metal_flat(NS + 'flat' + m, MCOL[m], 0.4, 0.0)

def tex_bars(m, overlay=False):
    c = Cv(None, True)
    base = MCOL[m]
    c.cyl_v(28, 0, 36, 64, base)
    if overlay:
        c.bevel(0, 0, 64, 64, base, 2.0, True, True, False)
    return c.finish(NS + 'bars' + m + str(overlay), 0.3, 0.0, 0.04, 0.0, keep_alpha=True)

def tex_mesh(m):
    c = Cv(None, True)
    base = MCOL[m]
    for k in range(-8, 9):
        c.line(k * 8, 0, k * 8 + 64, 64, base, 1.6)
        c.line(k * 8 + 64, 0, k * 8, 64, sh(base, 0.8), 1.6)
    return c.finish(NS + 'mesh' + m, 0.2, 0.0, 0.04, 0.0, keep_alpha=True)

def tex_grate(m):
    c = Cv(MCOL[m])
    c.bevel(0.5, 0.5, 63.5, 63.5, MCOL[m], 2.4, True, True)
    for y in range(8, 60, 12):
        for x in range(8, 60, 14):
            c.rect(x, y, x + 8, y + 6, (0, 0, 0), 0)
    im = c.finish(NS + 'grate' + m, 0.5, 0.0, 0.05, 0.1)
    a = np.array(im)
    for y in range(8, 60, 12):
        for x in range(8, 60, 14):
            a[y:y + 6, x:x + 8, 3] = 0
    return Image.fromarray(a, 'RGBA')

def tex_window(m): return art.glass_tex(NS + 'win' + m, MCOL[m], 'tiled')
def tex_door(m, top):
    c = Cv(MCOL[m])
    c.bevel(0.5, 0.5, 63.5, 63.5, MCOL[m], 3.0, True)
    c.bevel(8, 8, 56, 56, sh(MCOL[m], 1.04), 2.0, False)
    if top:
        c.glass(14, 14, 50, 44)
    else:
        c.hazard(8, 56, 56, 62, 3.0)
    c.rivets4(0, 0, 64, 64, 3.0, 1.2, MCOL[m])
    return c.finish(NS + 'door' + m + str(top), 0.45, 0.0)

def tex_trapdoor(m):
    c = Cv(MCOL[m])
    c.bevel(0.5, 0.5, 63.5, 63.5, MCOL[m], 2.4, True)
    for y in (12, 28, 44):
        c.bevel(6, y, 58, y + 8, sh(MCOL[m], 0.96), 1.2, True)
    return c.finish(NS + 'trap' + m, 0.4, 0.0)

def tex_ladder(m):
    c = Cv(None, True)
    b = MCOL[m]
    c.cyl_v(10, 0, 18, 64, b); c.cyl_v(46, 0, 54, 64, b)
    for y in (8, 24, 40, 56):
        c.cyl_h(10, y - 3, 54, y + 3, sh(b, 0.9))
    return c.finish(NS + 'ladder' + m, 0.3, 0.0, 0.04, 0.0, keep_alpha=True)

def tex_lamp(m, color, lit):
    c = Cv(MCOL[m])
    c.bevel(0.5, 0.5, 63.5, 63.5, MCOL[m], 2.4, True)
    glow = color if lit else sh(color, 0.35)
    c.lamp(32, 32, 16, glow)
    for x in (14, 24, 40, 50):
        c.line(x, 8, x, 56, sh(MCOL[m], 0.7), 1.4)
    return c.finish(NS + 'lamp' + m + str(lit) + str(color), 0.25, 0.0)

def tex_brick(color, kind, name):
    kk = {'': 'bricks', 'short': 'small', 'tiled': 'cut', 'long': 'layered', 'corner': 'pillar_side', 'cracked': 'bricks', 'mossy': 'bricks'}[kind]
    base = color
    im = art.stone_tex(NS + name, base, kk)
    if kind in ('cracked', 'mossy'):
        r = art.rng(NS + name + 'x')
        d = ImageDraw.Draw(im)
        if kind == 'cracked':
            for _ in range(5):
                x, y = r.randint(4, 58), r.randint(4, 58)
                for _ in range(7):
                    nx, ny = x + r.randint(-5, 5), y + r.randint(0, 6)
                    d.line([x, y, nx, ny], fill=sh(base, 0.45) + (255,), width=1); x, y = nx, ny
        else:
            for _ in range(70):
                x, y = r.randint(0, 63), r.choice([r.randint(0, 18), r.randint(46, 63)])
                d.rectangle([x, y, x + r.randint(0, 3), y + r.randint(0, 2)], fill=(60 + r.randint(0, 30), 110 + r.randint(0, 40), 50, 255))
    return im

def tex_coin_item(m):
    c = Cv(None, True)
    b = MCOL[m]
    c.ellipse(14, 18, 50, 50, sh(b, 0.6))
    c.ellipse(14, 14, 50, 46, b)
    c.ellipse(20, 20, 44, 40, sh(b, 1.12))
    c.ellipse(24, 24, 40, 36, sh(b, 0.9))
    c.ellipse(18, 17, 30, 24, sh(b, 1.5))
    return icons64._finish(c)

def tex_coinstack_item(m):
    c = Cv(None, True)
    b = MCOL[m]
    for i, (x, y) in enumerate(((12, 40), (30, 40), (21, 30), (12, 20), (30, 20))):
        c.ellipse(x, y + 5, x + 22, y + 17, sh(b, 0.6))
        c.ellipse(x, y, x + 22, y + 12, b)
        c.ellipse(x + 4, y + 2, x + 18, y + 9, sh(b, 1.18))
    return icons64._finish(c)

def tex_coin_side(m, part):
    c = Cv(MCOL[m])
    if part == 'side':
        for y in range(0, 64, 8):
            c.rect(0, y, 64, y + 6, sh(MCOL[m], 1.05)); c.rect(0, y + 6, 64, y + 8, sh(MCOL[m], 0.6))
    else:
        c.bevel(1, 1, 63, 63, MCOL[m], 3.0, True)
        c.ellipse(14, 14, 50, 50, sh(MCOL[m], 1.12)); c.ellipse(22, 22, 42, 42, sh(MCOL[m], 0.9))
    return c.finish(NS + 'coin' + m + part, 0.2, 0.0)

def tex_decal(kind):
    c = Cv(None, True)
    Y, K = art.HAZ_Y, (34, 32, 34)
    W_ = (232, 232, 226); R_ = (200, 48, 44); B_ = (44, 110, 200)
    def tri(col):
        c.poly([(32, 6), (60, 56), (4, 56)], col)
        c.poly([(32, 16), (51, 50), (13, 50)], Y if col == K else K)
    if kind == 'warning':
        c.poly([(32, 5), (61, 57), (3, 57)], K); c.poly([(32, 13), (54, 52), (10, 52)], Y)
        c.rect(30, 24, 34, 40, K); c.ellipse(29.5, 43, 34.5, 48, K)
    elif kind == 'creeper':
        c.rect(6, 6, 58, 58, (60, 150, 60))
        c.rect(14, 16, 26, 28, K); c.rect(38, 16, 50, 28, K); c.rect(26, 28, 38, 42, K)
        c.rect(20, 42, 26, 52, K); c.rect(38, 42, 44, 52, K); c.rect(26, 42, 38, 46, K)
    elif kind == 'skull':
        c.ellipse(10, 6, 54, 46, W_); c.rect(20, 40, 44, 56, W_)
        c.ellipse(16, 20, 28, 34, K); c.ellipse(36, 20, 48, 34, K); c.poly([(32, 32), (28, 42), (36, 42)], K)
        for x in (24, 30, 36): c.rect(x, 48, x + 2, 56, K)
    elif kind == 'flow':
        c.poly([(32, 4), (52, 38), (12, 38)], B_); c.ellipse(12, 26, 52, 60, B_); c.ellipse(20, 34, 28, 44, (180, 220, 250))
    elif kind == 'ice':
        for a in range(0, 180, 60):
            r = math.radians(a)
            c.line(32 - 26 * math.cos(r), 32 - 26 * math.sin(r), 32 + 26 * math.cos(r), 32 + 26 * math.sin(r), (150, 210, 240), 4)
    elif kind == 'radioactive':
        c.ellipse(4, 4, 60, 60, Y); c.ellipse(26, 26, 38, 38, K)
        for a in (90, 210, 330):
            r1, r2 = math.radians(a - 30), math.radians(a + 30)
            pts = [(32, 32)] + [(32 + 26 * math.cos(math.radians(t)), 32 - 26 * math.sin(math.radians(t))) for t in range(a - 30, a + 31, 5)]
            c.poly(pts, K)
        c.ellipse(28, 28, 36, 36, Y)
    elif kind in ('up', 'down', 'left', 'right'):
        rot = {'up': 0, 'right': 90, 'down': 180, 'left': 270}[kind]
        pts = [(32, 6), (54, 32), (40, 32), (40, 58), (24, 58), (24, 32), (10, 32)]
        c.poly(art._rot(pts, 32, 32, rot) if hasattr(art, '_rot') else pts, Y)
    elif kind in ('top_left', 'top_right', 'down_left', 'down_right'):
        pts = [(8, 8), (34, 8), (34, 18), (28, 18), (50, 40), (44, 46), (22, 24), (22, 30), (8, 30)]
        ang = {'top_left': 0, 'top_right': 90, 'down_right': 180, 'down_left': 270}[kind]
        c.poly(art._rot(pts, 32, 32, ang), Y)
    elif kind == 'cross':
        c.poly([(8, 16), (16, 8), (32, 24), (48, 8), (56, 16), (40, 32), (56, 48), (48, 56), (32, 40), (16, 56), (8, 48), (24, 32)], R_)
    elif kind == 'fluid':
        c.poly([(32, 4), (50, 34), (14, 34)], (60, 140, 200)); c.ellipse(14, 22, 50, 58, (60, 140, 200)); c.rect(8, 44, 56, 48, K)
    elif kind == 'fire':
        c.poly([(32, 4), (48, 30), (54, 46), (44, 60), (20, 60), (10, 46), (18, 28), (26, 36)], (230, 110, 30))
        c.poly([(32, 28), (42, 46), (38, 58), (26, 58), (22, 46)], (250, 210, 70))
    elif kind == 'electrical':
        c.poly([(36, 2), (14, 36), (28, 36), (22, 62), (50, 24), (34, 24)], Y)
    elif kind == 'fire_diamond':
        c.poly([(32, 2), (62, 32), (32, 62), (2, 32)], W_)
        c.poly([(32, 4), (46, 18), (32, 32), (18, 18)], R_); c.poly([(18, 18), (32, 32), (18, 46), (4, 32)], B_)
        c.poly([(46, 18), (60, 32), (46, 46), (32, 32)], Y)
    elif kind == 'no_entry':
        c.ellipse(4, 4, 60, 60, R_); c.rect(14, 28, 50, 36, W_)
    return c.finish(NS + 'decal' + kind, 0.1, 0.0, 0.0, 0.0, keep_alpha=True)

# ---------------------------------------------------------------------------------------------- model helpers
def model(name, obj): W(os.path.join(A, 'models', 'block', name + '.json'), obj)
def item_def(i, mdl): W(os.path.join(A, 'items', i + '.json'), {'model': {'type': 'minecraft:model', 'model': mdl}})
def blockstate(i, obj): W(os.path.join(A, 'blockstates', i + '.json'), obj)
def T(n): return NS + ':block/' + n

def reskin(vname, myid, texmap, default):
    """Copy vanilla blockstate `vname` with every model re-created under our namespace using our textures."""
    bs = json.load(open(VAN + '/blockstates/' + vname + '.json'))
    def tr(m):
        base = m.split('/')[-1]
        vm = json.load(open(VAN + '/models/block/' + base + '.json'))
        nm = myid + '__' + base
        out = {'parent': vm.get('parent', 'minecraft:block/cube_all')}
        if 'textures' in vm:
            out['textures'] = {k: texmap.get(k, default) for k in vm['textures']}
        for k in ('elements', 'display', 'ambientocclusion', 'texture_size'):
            if k in vm: out[k] = vm[k]
        model(nm, out)
        return NS + ':block/' + nm
    def walk(x):
        if isinstance(x, dict):
            if 'model' in x and isinstance(x['model'], str): x['model'] = tr(x['model'])
            for v in x.values(): walk(v)
        elif isinstance(x, list):
            for v in x: walk(v)
    walk(bs)
    blockstate(myid, bs)

def box(frm, to, tex, faces=None, rot=None):
    el = {'from': frm, 'to': to, 'faces': {}}
    for f in (faces or ['north', 'south', 'east', 'west', 'up', 'down']):
        el['faces'][f] = {'texture': '#' + tex}
    return el

def cube_all(i, tex): model(i, {'parent': 'minecraft:block/cube_all', 'textures': {'all': tex}})

def boxes_model(i, boxes, textures, particle):
    els = []
    for (a, b, t) in boxes:
        els.append(box(a, b, t))
    d = {'textures': dict(textures, particle=particle), 'elements': els}
    model(i, d)

def add_block(i, kind, name, **kw):
    BLOCKS.append((i, kind, kw)); LANG['block.' + NS + '.' + i] = name

# ---------------------------------------------------------------------------------------------- build
def main():
    if os.path.isdir(A): shutil.rmtree(A)
    if os.path.isdir(D): shutil.rmtree(D)
    recipes = {}
    def recipe(name, obj): recipes[name] = obj
    def stonecut(src, out, n=1, name=None):
        recipe(name or (out.split(':')[1] + '_from_' + src.split(':')[1] + '_stonecutting'),
               {'type': 'minecraft:stonecutting', 'ingredient': src, 'result': {'id': out, 'count': n}})

    # plain items
    for i, nm, kind, col in [('andesite_sheet', 'Andesite Alloy Sheet', 'sheet', art.ANDESITE), ('zinc_sheet', 'Zinc Sheet', 'sheet', art.ZINC),
                             ('industrial_iron_sheet', 'Industrial Iron Sheet', 'sheet', MCOL['industrial_iron']),
                             ('industrial_iron_nugget', 'Industrial Iron Nugget', 'nugget', MCOL['industrial_iron']),
                             ('industrial_iron_ingot', 'Industrial Iron Ingot', 'ingot', MCOL['industrial_iron'])]:
        tex_save('item/' + i, getattr(icons64, kind)(col, i))
        W(os.path.join(A, 'models', 'item', i + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + ':item/' + i}})
        item_def(i, NS + ':item/' + i); LANG['item.' + NS + '.' + i] = nm; ITEMS.append(i)
    for m in COIN_METALS:
        for suffix, fn, nm in (('coin', tex_coin_item, 'Coin'), ('coinstack', tex_coinstack_item, 'Coin Stack')):
            i = f'{m}_{suffix}'
            tex_save('item/' + i, fn(m))
            W(os.path.join(A, 'models', 'item', i + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + ':item/' + i}})
            item_def(i, NS + ':item/' + i); LANG['item.' + NS + '.' + i] = f'{cap(m)} {nm}'; ITEMS.append(i)
        recipe(f'{m}_coin_from_ingot', {'type': 'minecraft:crafting_shapeless', 'ingredients': [INGOT[m]], 'result': {'id': f'{NS}:{m}_coin', 'count': 9}})
        recipe(f'{m}_coinstack_from_coins', {'type': 'minecraft:crafting_shapeless', 'ingredients': [f'{NS}:{m}_coin'] * 8, 'result': {'id': f'{NS}:{m}_coinstack', 'count': 1}})
        recipe(f'{m}_coin_from_stack', {'type': 'minecraft:crafting_shapeless', 'ingredients': [f'{NS}:{m}_coinstack'], 'result': {'id': f'{NS}:{m}_coin', 'count': 8}})
    # sheets / nuggets / ingot recipes
    for m, sh_id in (('andesite', 'andesite_sheet'), ('zinc', 'zinc_sheet'), ('industrial_iron', 'industrial_iron_sheet')):
        recipe(sh_id + '_pressing', {'type': 'create:pressing', 'ingredient': INGOT[m], 'results': [{'id': f'{NS}:{sh_id}'}]})
    recipe('industrial_iron_nugget_from_ingot', {'type': 'minecraft:crafting_shapeless', 'ingredients': [INGOT['industrial_iron']], 'result': {'id': NS + ':industrial_iron_nugget', 'count': 9}})
    recipe('industrial_iron_ingot_from_nuggets', {'type': 'minecraft:crafting_shapeless', 'ingredients': [NS + ':industrial_iron_nugget'] * 9, 'result': {'id': NS + ':industrial_iron_ingot', 'count': 1}})
    recipe('industrial_iron_ingot_from_block', {'type': 'minecraft:crafting_shapeless', 'ingredients': ['create:industrial_iron_block'], 'result': {'id': NS + ':industrial_iron_ingot', 'count': 9}})
    recipe('industrial_iron_ingot_smelting', {'type': 'minecraft:blasting', 'ingredient': 'minecraft:iron_ingot', 'result': {'id': NS + ':industrial_iron_ingot'}, 'experience': 0.1, 'cookingtime': 100})

    for m in COIN_METALS:
        for part in ('side', 'top', 'bottom'):
            tex_save(f'{m}_coinstack_{part}', tex_coin_side(m, 'side' if part == 'side' else 'top'))

    for m in METALS:
        M = cap(m); ing = INGOT[m]; shs = SHEET[m]
        tex_save(m + '_plate', tex_plate(m))
        tex_save(m + '_hull', tex_plate(m))
        tex_save(m + '_flat', tex_flat(m))
        tex_save(m + '_bars', tex_bars(m)); tex_save(m + '_bars_overlay', tex_bars(m, True))
        tex_save(m + '_mesh', tex_mesh(m)); tex_save(m + '_catwalk', tex_grate(m))
        tex_save(m + '_window', tex_window(m))
        tex_save(m + '_door_top', tex_door(m, True)); tex_save(m + '_door_bottom', tex_door(m, False))
        tex_save(m + '_trapdoor', tex_trapdoor(m)); tex_save(m + '_ladder', tex_ladder(m))
        for lc, rgb in LAMPS.items():
            tex_save(f'{lc}_{m}_lamp', tex_lamp(m, rgb, False)); tex_save(f'{lc}_{m}_lamp_on', tex_lamp(m, rgb, True))

        # bars
        if m != 'iron':
            add_block(m + '_bars', 'bars', f'{M} Bars')
            reskin('iron_bars', m + '_bars', {}, T(m + '_bars'))
            W(os.path.join(A, 'models', 'item', m + '_bars.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(m + '_bars')}})
            item_def(m + '_bars', NS + ':item/' + m + '_bars')
            stonecut(ing, f'{NS}:{m}_bars', 4)
            recipe(f'{m}_bars_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['bbb', 'bbb'], 'key': {'b': ing}, 'result': {'id': f'{NS}:{m}_bars', 'count': 16}} if m != 'copper' else
                   {'type': 'minecraft:crafting_shaped', 'pattern': ['bbb', 'bbb', ' b '], 'key': {'b': ing}, 'result': {'id': f'{NS}:{m}_bars', 'count': 16}})
        add_block(m + '_bars_overlay', 'bars', f'{M} Bar Panel')
        reskin('iron_bars', m + '_bars_overlay', {}, T(m + '_bars_overlay'))
        W(os.path.join(A, 'models', 'item', m + '_bars_overlay.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(m + '_bars_overlay')}})
        item_def(m + '_bars_overlay', NS + ':item/' + m + '_bars_overlay')
        stonecut(ing, f'{NS}:{m}_bars_overlay', 4)
        recipe(f'{m}_bars_overlay_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['sss', 'sss'], 'key': {'s': shs}, 'result': {'id': f'{NS}:{m}_bars_overlay', 'count': 16}})
        # mesh fence
        add_block(m + '_mesh_fence', 'fence', f'{M} Mesh Fence')
        reskin('iron_bars', m + '_mesh_fence', {}, T(m + '_mesh'))
        W(os.path.join(A, 'models', 'item', m + '_mesh_fence.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(m + '_mesh')}})
        item_def(m + '_mesh_fence', NS + ':item/' + m + '_mesh_fence')
        stonecut(ing, f'{NS}:{m}_mesh_fence', 4)
        recipe(f'{m}_mesh_fence_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['sbs', 'sbs'], 'key': {'s': shs, 'b': f'{NS}:{m}_bars' if m != 'iron' else 'minecraft:iron_bars'}, 'result': {'id': f'{NS}:{m}_mesh_fence', 'count': 8}})
        # catwalk
        g = T(m + '_catwalk')
        add_block(m + '_catwalk', 'catwalk', f'{M} Catwalk')
        model(m + '_catwalk', {'textures': {'p': g, 'q': T(m + '_plate'), 'particle': g}, 'elements': [
            box([0, 14, 0], [16, 16, 16], 'p'), box([0, 0, 0], [2, 14, 2], 'q'), box([14, 0, 0], [16, 14, 2], 'q'),
            box([0, 0, 14], [2, 14, 16], 'q'), box([14, 0, 14], [16, 14, 16], 'q')]})
        model(m + '_catwalk_top', {'textures': {'p': g, 'particle': g}, 'elements': [box([0, 14, 0], [16, 16, 16], 'p')]})
        blockstate(m + '_catwalk', {'variants': {'bottom=false': {'model': T(m + '_catwalk_top')}, 'bottom=true': {'model': T(m + '_catwalk')}}})
        item_def(m + '_catwalk', T(m + '_catwalk_top'))
        stonecut(ing, f'{NS}:{m}_catwalk', 4)
        recipe(f'{m}_catwalk_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['sss', 'bbb'], 'key': {'s': shs, 'b': f'{NS}:{m}_bars' if m != 'iron' else 'minecraft:iron_bars'}, 'result': {'id': f'{NS}:{m}_catwalk', 'count': 4}})
        # stairs
        add_block(m + '_catwalk_stairs', 'catwalk_stairs', f'{M} Catwalk Stairs')
        st = []
        for k in range(4):
            st.append((  [0, 2 + 3 * k + 0, 12 - 4 * k], [16, 4 + 3 * k, 16 - 4 * k], 'p'))
        model(m + '_catwalk_stairs', {'textures': {'p': g, 'q': T(m + '_plate'), 'particle': g},
              'elements': [box(a, b, t) for a, b, t in st]})
        model(m + '_catwalk_stairs_rail_l', {'textures': {'q': T(m + '_plate'), 'particle': T(m + '_plate')}, 'elements': [
            box([14, 2 + 4 * k + 2, 12 - 4 * k], [16, 14 + 4 * k, 16 - 4 * k], 'q') for k in range(4)]})
        model(m + '_catwalk_stairs_rail_r', {'textures': {'q': T(m + '_plate'), 'particle': T(m + '_plate')}, 'elements': [
            box([0, 2 + 4 * k + 2, 12 - 4 * k], [2, 14 + 4 * k, 16 - 4 * k], 'q') for k in range(4)]})
        ys = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
        mp = []
        for f, y in ys.items():
            ap = lambda mm: ({'model': mm, 'y': y} if y else {'model': mm})
            mp.append({'when': {'facing': f}, 'apply': ap(T(m + '_catwalk_stairs'))})
            mp.append({'when': {'facing': f, 'railing_left': 'true'}, 'apply': ap(T(m + '_catwalk_stairs_rail_l'))})
            mp.append({'when': {'facing': f, 'railing_right': 'true'}, 'apply': ap(T(m + '_catwalk_stairs_rail_r'))})
        blockstate(m + '_catwalk_stairs', {'multipart': mp})
        item_def(m + '_catwalk_stairs', T(m + '_catwalk_stairs'))
        stonecut(ing, f'{NS}:{m}_catwalk_stairs', 2)
        recipe(f'{m}_catwalk_stairs_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['s  ', 'ss ', 'bbb'], 'key': {'s': shs, 'b': f'{NS}:{m}_bars' if m != 'iron' else 'minecraft:iron_bars'}, 'result': {'id': f'{NS}:{m}_catwalk_stairs', 'count': 4}})
        # railing
        add_block(m + '_catwalk_railing', 'catwalk_railing', f'{M} Catwalk Railing')
        model(m + '_catwalk_railing_side', {'textures': {'b': T(m + '_bars'), 'q': T(m + '_plate'), 'particle': T(m + '_plate')}, 'elements': [
            box([0, 12, 0], [16, 14, 2], 'q'), box([0, 0, 0], [2, 12, 2], 'q'), box([14, 0, 0], [16, 12, 2], 'q'), box([7, 2, 0.5], [9, 12, 1.5], 'b')]})
        mp = [{'when': {d: 'true'}, 'apply': ({'model': T(m + '_catwalk_railing_side'), 'y': y} if y else {'model': T(m + '_catwalk_railing_side')})}
              for d, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270))]
        blockstate(m + '_catwalk_railing', {'multipart': mp})
        item_def(m + '_catwalk_railing', T(m + '_catwalk_railing_side'))
        stonecut(ing, f'{NS}:{m}_catwalk_railing', 8)
        recipe(f'{m}_catwalk_railing_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['sss', 'bbb'], 'key': {'s': ing, 'b': f'{NS}:{m}_bars' if m != 'iron' else 'minecraft:iron_bars'}, 'result': {'id': f'{NS}:{m}_catwalk_railing', 'count': 8}})
        # support
        add_block(m + '_support', 'support', f'{M} Support')
        pl = T(m + '_plate')
        model(m + '_support', {'textures': {'q': pl, 'particle': pl}, 'elements': [
            box([5, 0, 5], [11, 16, 11], 'q'), box([3, 0, 3], [13, 2, 13], 'q'), box([3, 14, 3], [13, 16, 13], 'q')]})
        mp = [{'when': {'facing': f}, 'apply': a} for f, a in (('up', {'model': T(m + '_support')}), ('down', {'model': T(m + '_support'), 'x': 180}),
              ('north', {'model': T(m + '_support'), 'x': 90}), ('south', {'model': T(m + '_support'), 'x': 90, 'y': 180}),
              ('east', {'model': T(m + '_support'), 'x': 90, 'y': 90}), ('west', {'model': T(m + '_support'), 'x': 90, 'y': 270}))]
        blockstate(m + '_support', {'multipart': mp})
        item_def(m + '_support', T(m + '_support'))
        stonecut(ing, f'{NS}:{m}_support', 2)
        recipe(f'{m}_support_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['s', 's', 's'], 'key': {'s': shs}, 'result': {'id': f'{NS}:{m}_support', 'count': 4}})
        # wedge
        add_block(m + '_support_wedge', 'wedge', f'{M} Support Wedge')
        N1 = [([5, 8, 14], [11, 14, 16]), ([5, 14, 8], [11, 16, 16])]
        N2 = [([8, 5, 14], [14, 11, 16]), ([14, 5, 8], [16, 11, 16])]
        N3 = [([5, 2, 14], [11, 8, 16]), ([5, 0, 8], [11, 2, 16])]
        N4 = [([2, 5, 14], [8, 11, 16]), ([0, 5, 8], [2, 11, 16])]
        for o, bx in enumerate((N1, N2, N3, N4), 1):
            model(f'{m}_support_wedge_{o}', {'textures': {'q': pl, 'particle': pl}, 'elements': [box(a, b, 'q') for a, b in bx]})
        mp = []
        for f, y in ys.items():
            for o in range(1, 5):
                mp.append({'when': {'facing': f, 'orientation': str(o)}, 'apply': ({'model': T(f'{m}_support_wedge_{o}'), 'y': y} if y else {'model': T(f'{m}_support_wedge_{o}')})})
        blockstate(m + '_support_wedge', {'multipart': mp})
        item_def(m + '_support_wedge', T(f'{m}_support_wedge_1'))
        stonecut(ing, f'{NS}:{m}_support_wedge', 4)
        recipe(f'{m}_support_wedge_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['ss', 's '], 'key': {'s': shs}, 'result': {'id': f'{NS}:{m}_support_wedge', 'count': 4}})
        # hull
        add_block(m + '_hull', 'hull', f'{M} Train Hull')
        model(m + '_hull', {'parent': 'minecraft:block/cube_column', 'textures': {'end': T(m + '_flat'), 'side': T(m + '_hull'), 'particle': T(m + '_hull')}})
        blockstate(m + '_hull', {'variants': {'facing=up': {'model': T(m + '_hull')}, 'facing=down': {'model': T(m + '_hull'), 'x': 180},
                   'facing=north': {'model': T(m + '_hull'), 'x': 90}, 'facing=south': {'model': T(m + '_hull'), 'x': 90, 'y': 180},
                   'facing=east': {'model': T(m + '_hull'), 'x': 90, 'y': 90}, 'facing=west': {'model': T(m + '_hull'), 'x': 90, 'y': 270}}})
        item_def(m + '_hull', T(m + '_hull'))
        stonecut(ing, f'{NS}:{m}_hull', 1)
        recipe(f'{m}_hull_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['ss', 'ss'], 'key': {'s': shs}, 'result': {'id': f'{NS}:{m}_hull', 'count': 4}})
        # sheet metal pillar
        add_block(m + '_sheet_metal', 'pillar', f'{M} Sheet Metal')
        model(m + '_sheet_metal', {'parent': 'minecraft:block/cube_column', 'textures': {'end': T(m + '_flat'), 'side': T(m + '_plate'), 'particle': T(m + '_plate')}})
        model(m + '_sheet_metal_horizontal', {'parent': 'minecraft:block/cube_column_horizontal', 'textures': {'end': T(m + '_flat'), 'side': T(m + '_plate'), 'particle': T(m + '_plate')}})
        blockstate(m + '_sheet_metal', {'variants': {'axis=y': {'model': T(m + '_sheet_metal')}, 'axis=z': {'model': T(m + '_sheet_metal_horizontal'), 'x': 90},
                   'axis=x': {'model': T(m + '_sheet_metal_horizontal'), 'x': 90, 'y': 90}}})
        item_def(m + '_sheet_metal', T(m + '_sheet_metal'))
        stonecut(ing, f'{NS}:{m}_sheet_metal', 4)
        recipe(f'{m}_sheet_metal_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['ss', 'ss'], 'key': {'s': shs}, 'result': {'id': f'{NS}:{m}_sheet_metal', 'count': 4}})
        # ladder
        if m in ('iron', 'zinc', 'industrial_iron'):
            add_block(m + '_ladder', 'ladder', f'{M} Ladder')
            reskin('ladder', m + '_ladder', {}, T(m + '_ladder'))
            W(os.path.join(A, 'models', 'item', m + '_ladder.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(m + '_ladder')}})
            item_def(m + '_ladder', NS + ':item/' + m + '_ladder')
            stonecut(ing, f'{NS}:{m}_ladder', 4)
        # doors
        if m != 'iron':
            names = [(m + '_door', f'{M} Door', False), ('locked_' + m + '_door', f'Locked {M} Door', True)] if m != 'copper' else [('locked_copper_door', 'Locked Copper Door', True)]
            for did, dn, locked in names:
                add_block(did, 'door', dn)
                reskin('iron_door', did, {'top': T(m + '_door_top'), 'bottom': T(m + '_door_bottom')}, T(m + '_door_top'))
                tex_save('item/' + did, art.plate_tex(NS + 'dooritem' + did, MCOL[m]).resize((64, 64)))
                W(os.path.join(A, 'models', 'item', did + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(m + '_door_top')}})
                item_def(did, NS + ':item/' + did)
            if m != 'copper':
                recipe(f'{m}_door_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['ss', 'ss', 'ss'], 'key': {'s': ing}, 'result': {'id': f'{NS}:{m}_door', 'count': 3}})
                recipe(f'locked_{m}_door_crafting', {'type': 'minecraft:crafting_shapeless', 'ingredients': [f'{NS}:{m}_door', 'minecraft:tripwire_hook'], 'result': {'id': f'{NS}:locked_{m}_door'}})
                add_block(m + '_trapdoor', 'trapdoor', f'{M} Trapdoor')
                reskin('iron_trapdoor', m + '_trapdoor', {}, T(m + '_trapdoor'))
                item_def(m + '_trapdoor', T(m + '_trapdoor__iron_trapdoor_bottom'))
                recipe(f'{m}_trapdoor_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['ss', 'ss'], 'key': {'s': ing}, 'result': {'id': f'{NS}:{m}_trapdoor'}})
            else:
                recipe('locked_copper_door_crafting', {'type': 'minecraft:crafting_shapeless', 'ingredients': ['minecraft:copper_door', 'minecraft:tripwire_hook'], 'result': {'id': f'{NS}:locked_copper_door'}})
        # windows
        add_block(m + '_window', 'window', f'{M} Window')
        cube_all(m + '_window', T(m + '_window')); blockstate(m + '_window', {'variants': {'': {'model': T(m + '_window')}}}); item_def(m + '_window', T(m + '_window'))
        add_block(m + '_window_pane', 'pane', f'{M} Window Pane')
        reskin('glass_pane', m + '_window_pane', {}, T(m + '_window'))
        W(os.path.join(A, 'models', 'item', m + '_window_pane.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(m + '_window')}})
        item_def(m + '_window_pane', NS + ':item/' + m + '_window_pane')
        recipe(f'{m}_window_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': [' i ', 'igi', ' i '], 'key': {'i': ing, 'g': 'minecraft:glass'}, 'result': {'id': f'{NS}:{m}_window', 'count': 4}})
        recipe(f'{m}_window_pane_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['www', 'www'], 'key': {'w': f'{NS}:{m}_window'}, 'result': {'id': f'{NS}:{m}_window_pane', 'count': 16}})
        # lamps
        for lc in LAMPS:
            i = f'{lc}_{m}_lamp'
            add_block(i, 'lamp', f'{cap(lc)} {M} Cage Lamp')
            for on in ('', '_on'):
                tx = T(i + on)
                model(i + on, {'textures': {'t': tx, 'particle': tx}, 'elements': [
                    box([4, 0, 4], [12, 8, 12], 't'), box([5, 8, 5], [11, 10, 11], 't')]})
            blockstate(i, {'multipart': [
                {'when': {'facing': f, 'lit': lit}, 'apply': dict({'model': T(i + ('_on' if lit == 'true' else ''))}, **rot)}
                for f, rot in (('up', {}), ('down', {'x': 180}), ('north', {'x': 90}), ('south', {'x': 90, 'y': 180}), ('east', {'x': 90, 'y': 90}), ('west', {'x': 90, 'y': 270}))
                for lit in ('true', 'false')]})
            item_def(i, T(i))
            recipe(i + '_crafting', {'type': 'minecraft:crafting_shapeless', 'ingredients': [ing, LAMP_ITEM[lc], f'{NS}:{m}_bars' if m != 'iron' else 'minecraft:iron_bars'], 'result': {'id': f'{NS}:{i}'}})

    # coin stack blocks
    for m in COIN_METALS:
        M = cap(m); i = m + '_coinstack_block'
        add_block(i, 'coinstack', f'{M} Coin Stack')
        for L in range(1, 9):
            model(f'{i}_{L}', {'parent': 'minecraft:block/cube_column' if False else None, 'textures': {'s': T(f'{m}_coinstack_side'), 't': T(f'{m}_coinstack_top'), 'particle': T(f'{m}_coinstack_side')},
                  'elements': [{'from': [3, 0, 3], 'to': [13, L * 2, 13], 'faces': {
                      'north': {'texture': '#s'}, 'south': {'texture': '#s'}, 'east': {'texture': '#s'}, 'west': {'texture': '#s'},
                      'up': {'texture': '#t'}, 'down': {'texture': '#t'}}}]})
            p = os.path.join(A, 'models', 'block', f'{i}_{L}.json'); o = json.load(open(p)); o.pop('parent', None); W(p, o)
        blockstate(i, {'variants': {f'layers={L}': {'model': T(f'{i}_{L}')} for L in range(1, 9)}})
        LANG['block.' + NS + '.' + i] = f'{M} Coin Stack'

    # decals
    for d in DECALS:
        i = 'decal_' + d
        tex_save(i, tex_decal(d))
        add_block(i, 'decal', cap(d) + ' Decal')
        model(i, {'textures': {'t': T(i), 'particle': T(i)}, 'elements': [
            {'from': [0, 0, 0], 'to': [16, 0.4, 16], 'faces': {'up': {'texture': '#t'}, 'down': {'texture': '#t'}}}]})
        rots = {'floor': {}, 'wall': {'x': 90}, 'ceiling': {'x': 180}}
        mp = []
        for face, rr in rots.items():
            for f, y in ((('north', 0), ('east', 90), ('south', 180), ('west', 270))):
                ap = {'model': T(i)}
                ap.update(rr)
                yy = (y + (180 if face == 'ceiling' else 0)) % 360
                if face == 'floor' or face == 'ceiling' or True:
                    if yy: ap['y'] = yy
                mp.append({'when': {'face': face, 'facing': f}, 'apply': ap})
        blockstate(i, {'multipart': mp})
        item_def(i, T(i)) if False else None
        W(os.path.join(A, 'models', 'item', i + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': T(i)}})
        item_def(i, NS + ':item/' + i)
        stonecut('create:iron_sheet', f'{NS}:{i}', 4)

    # bricks
    for cname, col in COLORS.items():
        for bt in BTYPES:
            if cname == 'red' and bt == '': continue
            n = (bt + '_' if bt else '') + cname + '_bricks'
            nm = ((cap(bt) + ' ') if bt else '') + cap(cname) + ' Bricks'
            tex_save(n, tex_brick(col, bt, n))
            tx = T(n)
            add_block(n, 'brick', nm)
            cube_all(n, tx); blockstate(n, {'variants': {'': {'model': tx}}}); item_def(n, tx)
            reskin('stone_brick_stairs', n + '_stairs', {}, tx)
            reskin('stone_brick_slab', n + '_slab', {}, tx)
            reskin('stone_brick_wall', n + '_wall', {}, tx)
            add_block(n + '_stairs', 'stairs', nm[:-1] + ' Stairs'); add_block(n + '_slab', 'slab', nm[:-1] + ' Slab'); add_block(n + '_wall', 'wall', nm[:-1] + ' Wall')
            item_def(n + '_stairs', T(n + '_stairs__stone_brick_stairs'))
            item_def(n + '_slab', T(n + '_slab__stone_brick_slab'))
            item_def(n + '_wall', T(n + '_wall__stone_brick_wall_inventory'))
            # the wall inventory model is not in the blockstate, so create it
            vm = json.load(open(VAN + '/models/block/stone_brick_wall_inventory.json'))
            model(n + '_wall__stone_brick_wall_inventory', {'parent': vm['parent'], 'textures': {k: tx for k in vm['textures']}})
            # recipes
            if bt == '':
                dye = DYE[cname]
                recipe(n + '_dyeing', {'type': 'minecraft:crafting_shaped', 'pattern': ['sss', 'sds', 'sss'], 'key': {'s': 'minecraft:stone_bricks', 'd': f'minecraft:{dye}_dye'}, 'result': {'id': f'{NS}:{n}', 'count': 8}})
                plain = f'{NS}:{n}'
            else:
                plain = f'{NS}:{cname}_bricks' if cname != 'red' else 'minecraft:bricks'
                stonecut(plain, f'{NS}:{n}', 1)
            stonecut(f'{NS}:{n}', f'{NS}:{n}_stairs', 1); stonecut(f'{NS}:{n}', f'{NS}:{n}_slab', 2); stonecut(f'{NS}:{n}', f'{NS}:{n}_wall', 1)
            recipe(n + '_stairs_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['s  ', 'ss ', 'sss'], 'key': {'s': f'{NS}:{n}'}, 'result': {'id': f'{NS}:{n}_stairs', 'count': 4}})
            recipe(n + '_slab_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['sss'], 'key': {'s': f'{NS}:{n}'}, 'result': {'id': f'{NS}:{n}_slab', 'count': 6}})
            recipe(n + '_wall_crafting', {'type': 'minecraft:crafting_shaped', 'pattern': ['sss', 'sss'], 'key': {'s': f'{NS}:{n}'}, 'result': {'id': f'{NS}:{n}_wall', 'count': 6}})

    # item defs for blocks with blockstate-only visuals already handled; fix doors for flat item (done). Items for bars etc done.

    # ---- loot tables, tags, lang, recipes
    ids = [b[0] for b in BLOCKS]
    for (i, kind, kw) in BLOCKS:
        drop = i if kind != 'coinstack' else None
        if kind == 'coinstack':
            m = i[:-len('_coinstack_block')]
            drop = m + '_coinstack'
        pool = {'rolls': 1.0, 'bonus_rolls': 0.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}], 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{drop}'}]}
        if kind == 'slab':
            pool['entries'][0]['functions'] = [{'function': 'minecraft:set_count', 'count': 2.0, 'add': False, 'conditions': [{'condition': 'minecraft:block_state_property', 'block': f'{NS}:{i}', 'properties': {'type': 'double'}}]}, {'function': 'minecraft:explosion_decay'}]
        if kind == 'door':
            pool['conditions'].append({'condition': 'minecraft:block_state_property', 'block': f'{NS}:{i}', 'properties': {'half': 'lower'}})
        if kind in ('catwalk_stairs', 'catwalk_railing'):
            pass
        W(os.path.join(D, 'loot_table', 'blocks', i + '.json'), {'type': 'minecraft:block', 'pools': [pool], 'random_sequence': f'{NS}:blocks/{i}'})
    def tag(path, vals): W(os.path.join(ROOT, 'data', path + '.json'), {'replace': False, 'values': vals})
    tag('minecraft/tags/block/mineable/pickaxe', [f'{NS}:{b}' for b in ids])
    for k, tg in (('door', 'doors'), ('trapdoor', 'trapdoors'), ('stairs', 'stairs'), ('slab', 'slabs'), ('wall', 'walls'), ('ladder', 'climbable'), ('fence', 'fences')):
        tag('minecraft/tags/block/' + tg, [f'{NS}:{b[0]}' for b in BLOCKS if b[1] == k])
    tag('minecraft/tags/item/doors', [f'{NS}:{b[0]}' for b in BLOCKS if b[1] == 'door'])
    tag('minecraft/tags/item/trapdoors', [f'{NS}:{b[0]}' for b in BLOCKS if b[1] == 'trapdoor'])
    for r, obj in recipes.items():
        W(os.path.join(D, 'recipe', r + '.json'), obj)
    LANG['itemGroup.createdeco.props'] = 'Create Deco Props'
    LANG['itemGroup.createdeco.bricks'] = 'Create Deco Bricks'
    W(os.path.join(A, 'lang', 'en_us.json'), dict(sorted(LANG.items())))
    # manifest of block ids for tests
    W(os.path.join(HERE, 'deco_ids.txt'), '\n'.join(sorted(ids + ITEMS)) + '\n')
    print(len(BLOCKS), 'blocks', len(ITEMS), 'items', len(recipes), 'recipes')

main()
