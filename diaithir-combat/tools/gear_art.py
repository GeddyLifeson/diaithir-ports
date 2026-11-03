#!/usr/bin/env python3
"""S6831 Library gear textures: 64 px, Industrial-Factory look (dark outline, shaded metal, bright edge). Two layers per item:
layer0 untinted (outline, grip, accents by Age metal), layer1 near-white body that the game tints per entry (custom_model_data colour).
Writes textures, models and item definitions for every weapon class and armor slot at Ages 1-9. Run from the module dir."""
import json, math, os
from PIL import Image, ImageDraw
A = 'src/main/resources/assets/diaithircombat/'
S = 64
METAL = [(124, 96, 60), (130, 130, 134), (184, 105, 62), (170, 128, 60), (150, 154, 160), (190, 196, 204), (120, 160, 190), (80, 200, 170), (240, 210, 90)]
CLASSES = ['dagger', 'sword', 'rapier', 'greatsword', 'axe', 'battleaxe', 'mace', 'warhammer', 'spear', 'polearm', 'staff', 'bow', 'longbow']
SLOTS = ['head', 'chest', 'legs', 'feet', 'shield', 'trinket']
OUT = (22, 24, 28, 255)

def shade(c, k): return tuple(max(0, min(255, int(v * k))) for v in c) + (255,)

def poly(d, pts, fill, outline=OUT):
    d.polygon(pts, fill=fill, outline=outline)

def rot(x, y, ang, cx=32, cy=32):  # rotate (x,y) around centre
    ca, sa = math.cos(ang), math.sin(ang)
    return (cx + (x - cx) * ca - (y - cy) * sa, cy + (x - cx) * sa + (y - cy) * ca)

def axis_shape(d0, d1, parts, tier, ang=-math.pi / 4):
    """parts: (layer, kind, x0,y0,x1,y1) in an upright 64 box (hilt at bottom) rotated by ang."""
    m = METAL[tier - 1]
    for layer, color, pts in parts:
        P = [rot(x, y, ang) for x, y in pts]
        d = d1 if layer else d0
        col = (235, 238, 242, 255) if layer else (shade(m, 0.8) if color == 'acc' else (96, 66, 40, 255))
        d.polygon(P, fill=col, outline=OUT if not layer else None)
    if d1 is not None: pass

def weapon(cls, tier, pull=0):
    l0 = Image.new('RGBA', (S, S), (0, 0, 0, 0)); l1 = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d0, d1 = ImageDraw.Draw(l0), ImageDraw.Draw(l1)
    def rect(x0, y0, x1, y1): return [(x0, y0), (x1, y0), (x1, y1), (x0, y1)]
    grip = lambda y0=40, y1=60: (0, 'grip', rect(29, y0, 35, y1))
    cg = lambda y: (0, 'acc', rect(22, y, 42, y + 4))
    P = []
    if cls == 'dagger': P = [grip(44, 60), cg(41), (1, 'b', [(29, 14), (35, 14), (36, 41), (28, 41)]), (1, 'b', [(32, 4), (36, 14), (28, 14)])]
    elif cls == 'sword': P = [grip(46, 62), cg(42), (1, 'b', [(28, 12), (36, 12), (36, 42), (28, 42)]), (1, 'b', [(32, 2), (36, 12), (28, 12)])]
    elif cls == 'rapier': P = [grip(48, 62), (0, 'acc', rect(18, 43, 46, 47)), (1, 'b', [(30, 8), (34, 8), (35, 43), (29, 43)]), (1, 'b', [(32, 1), (34, 8), (30, 8)])]
    elif cls == 'greatsword': P = [grip(46, 62), cg(41), (1, 'b', [(24, 10), (40, 10), (40, 42), (24, 42)]), (1, 'b', [(32, 0), (40, 10), (24, 10)])]
    elif cls in ('axe', 'battleaxe'):
        w = 16 if cls == 'axe' else 22
        P = [(0, 'grip', rect(30, 8, 34, 62)), (1, 'b', [(34, 8), (34 + w, 4), (34 + w, 26), (34, 24)]), (1, 'b', [(30, 12), (30 - (w // 2 if cls == 'battleaxe' else 0), 10), (30 - (w // 2 if cls == 'battleaxe' else 0), 22), (30, 22)]) if cls == 'battleaxe' else (0, 'acc', rect(29, 6, 35, 10))]
    elif cls in ('mace', 'warhammer'):
        hw = 14 if cls == 'mace' else 22
        P = [(0, 'grip', rect(30, 18, 34, 62)), (1, 'b', [(32 - hw // 2, 4), (32 + hw // 2, 4), (32 + hw // 2, 20), (32 - hw // 2, 20)])]
    elif cls == 'spear': P = [(0, 'grip', rect(30, 14, 34, 62)), (1, 'b', [(32, 0), (38, 14), (26, 14)])]
    elif cls == 'polearm': P = [(0, 'grip', rect(30, 8, 34, 62)), (1, 'b', [(32, 0), (37, 10), (27, 10)]), (1, 'b', [(34, 10), (46, 14), (34, 26)])]
    elif cls == 'staff': P = [(0, 'grip', rect(30, 14, 34, 62)), (1, 'b', [(32, 0), (40, 8), (32, 16), (24, 8)])]
    if cls in ('bow', 'longbow'):
        n = 2 if cls == 'longbow' else 0
        pts = [(18 + n, 58), (14 + n, 40), (14 + n, 24), (22 + n, 6)]
        for i in range(len(pts) - 1): d1.line([pts[i], pts[i + 1]], fill=(235, 238, 242, 255), width=5)
        top, bot, mid = (22 + n, 6), (18 + n, 58), (16 + n, 32)
        sx = [16 + n, 26, 34, 40][pull]
        d0.line([top, (sx, 32), bot] if pull else [top, bot], fill=(200, 200, 190, 255), width=1)
        if pull:  # nocked arrow, longer the further the draw
            d0.line([(sx, 32), (sx + 24, 32 - 0)], fill=(96, 66, 40, 255), width=2)
            d0.polygon([(sx + 24, 29), (sx + 30, 32), (sx + 24, 35)], fill=shade(METAL[tier - 1], .9), outline=OUT)
        return l0, l1
    ang = -math.pi / 4 if cls not in ('spear', 'polearm', 'staff', 'axe', 'battleaxe', 'mace', 'warhammer') else -math.pi / 5
    axis_shape(d0, d1, P, tier, ang)
    # outline layer1 shapes into layer0 so the blade has a dark edge, and add metal accent on tier
    return l0, l1

def armor(slot, tier):
    l0 = Image.new('RGBA', (S, S), (0, 0, 0, 0)); l1 = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d0, d1 = ImageDraw.Draw(l0), ImageDraw.Draw(l1)
    m = METAL[tier - 1]
    W = (235, 238, 242, 255)
    if slot == 'head': body = [(14, 50), (14, 28), (22, 12), (42, 12), (50, 28), (50, 50), (40, 50), (40, 36), (24, 36), (24, 50)]
    elif slot == 'chest': body = [(10, 14), (24, 8), (40, 8), (54, 14), (54, 30), (46, 30), (46, 56), (18, 56), (18, 30), (10, 30)]
    elif slot == 'legs': body = [(16, 8), (48, 8), (50, 58), (35, 58), (32, 30), (29, 58), (14, 58)]
    elif slot == 'feet': body = [(12, 20), (28, 20), (28, 38), (52, 44), (52, 56), (12, 56)]
    elif slot == 'shield': body = [(10, 8), (54, 8), (54, 34), (32, 58), (10, 34)]
    else: body = [(32, 6), (46, 20), (46, 34), (32, 46), (18, 34), (18, 20)]
    d0.polygon(body, fill=OUT)
    inner = [(32 + (x - 32) * .86, 32 + (y - 32) * .86) for x, y in body]
    d1.polygon(inner, fill=W)
    # metal rim bands + rivets in Age metal
    d0.line([body[0], body[-1]], fill=shade(m, .9), width=3)
    for (x, y) in body[::2]: d0.ellipse([x - 2, y - 2, x + 2, y + 2], fill=shade(m, 1.15))
    if tier >= 8: d0.ellipse([28, 28, 36, 36], fill=(120, 255, 230, 255))
    return l0, l1

def eq_texture(tier, legs):
    """Worn layer (64x32, vanilla humanoid UV): grey plate panels, tinted per entry through the dyed_color component."""
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    pw = max(3, 9 - tier)  # panel size shrinks with Age: finer plating
    for y in range(32):
        for x in range(64):
            seam = (x % pw == 0) or (y % pw == 0)
            v = 150 if seam else 205 + 4 * ((x // pw + y // pw) % 3)
            if (x % pw == 1 and y % pw == 1): v = 240
            im.putpixel((x, y), (v, v, v, 255))
    if tier >= 8:
        for x in range(0, 64, 8): d.line([(x, 0), (x, 31)], fill=(235, 255, 250, 255))
    return im

def bow_models(A, name, t, l0, frames):
    pass

def main():
    for t in range(1, 10):
        for legs, sub in ((False, 'humanoid'), (True, 'humanoid_leggings')):
            os.makedirs(A + 'textures/entity/equipment/' + sub, exist_ok=True)
            eq_texture(t, legs).save(A + f'textures/entity/equipment/{sub}/lib_t{t}.png')
        os.makedirs(A + 'equipment', exist_ok=True)
        json.dump({'layers': {'humanoid': [{'texture': f'diaithircombat:lib_t{t}', 'dyeable': {'color_when_undyed': 16777215}}],
                              'humanoid_leggings': [{'texture': f'diaithircombat:lib_t{t}', 'dyeable': {'color_when_undyed': 16777215}}]}},
                  open(A + f'equipment/lib_t{t}.json', 'w'))
    os.makedirs(A + 'textures/item/library', exist_ok=True); os.makedirs(A + 'models/item/library', exist_ok=True); os.makedirs(A + 'items', exist_ok=True)
    for name, fn in [(c, lambda t, c=c: weapon(c, t)) for c in CLASSES] + [(s, lambda t, s=s: armor(s, t)) for s in SLOTS]:
        cases = []
        bow = name in ('bow', 'longbow')
        tints = [{'type': 'minecraft:constant', 'value': 16777215}, {'type': 'minecraft:custom_model_data', 'index': 0, 'default': 16777215}]
        def mk(key, l0, l1):
            l0.save(A + f'textures/item/library/{key}_a.png'); l1.save(A + f'textures/item/library/{key}_b.png')
            json.dump({'parent': 'minecraft:item/generated', 'textures': {'layer0': f'diaithircombat:item/library/{key}_a', 'layer1': f'diaithircombat:item/library/{key}_b'}},
                      open(A + f'models/item/library/{key}.json', 'w'))
            return {'type': 'minecraft:model', 'model': f'diaithircombat:item/library/{key}', 'tints': tints}
        for t in range(1, 10):
            l0, l1 = fn(t)
            model = mk(f'lib_{name}_t{t}', l0, l1)
            if bow:  # vanilla bow pull: using_item -> range_dispatch on use_duration (0.65 and 0.9 of a 1 s draw)
                pm = [mk(f'lib_{name}_t{t}_pull{i}', *weapon(name, t, i)) for i in (1, 2, 3)]
                model = {'type': 'minecraft:condition', 'property': 'minecraft:using_item', 'on_false': model,
                         'on_true': {'type': 'minecraft:range_dispatch', 'property': 'minecraft:use_duration', 'scale': 0.05,
                                     'entries': [{'threshold': 0.65, 'model': pm[1]}, {'threshold': 0.9, 'model': pm[2]}], 'fallback': pm[0]}}
            cases.append({'when': f't{t}', 'model': model})
        json.dump({'model': {'type': 'minecraft:select', 'property': 'minecraft:custom_model_data', 'index': 0, 'cases': cases, 'fallback': cases[0]['model']}},
                  open(A + f'items/lib_{name}.json', 'w'))
    print('ok')
main()
