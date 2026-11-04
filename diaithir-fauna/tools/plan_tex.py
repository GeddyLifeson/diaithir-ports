#!/usr/bin/env python3
# S6893 Paints a texture per body plan and element (assets/diaithirfauna/textures/entity/plan/<plan>_<element>.png) for Library creatures.
"""Usage: plan_tex.py [OUT_DIR]. Reads data/diaithirfauna/plans/*.json (cube-net UVs), paints each face with a plan colour, face shading,
fur-like noise, a lighter belly and dark eyes; the element tints the colour. Deterministic."""
import colorsys, json, os, random, sys, zlib
from PIL import Image
HERE = os.path.dirname(os.path.abspath(__file__))
PL = os.path.join(HERE, '..', 'src/main/resources/data/diaithirfauna/plans')
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, '..', 'src/main/resources/assets/diaithirfauna/textures/entity/plan')
TINT = {'none': None, 'fire': (205, 80, 40), 'ice': (170, 210, 240), 'lightning': (235, 215, 80), 'poison': (110, 180, 70),
        'shadow': (60, 50, 85), 'light': (240, 235, 200), 'earth': (150, 120, 80), 'water': (70, 130, 200), 'wind': (190, 215, 200)}
FAMILY = {'water': (0.58, 0.35, 0.55), 'fly': (0.08, 0.30, 0.50), 'land': (0.08, 0.40, 0.45)}

def base(name, cls):
    h0, s0, v0 = FAMILY[cls]
    r = random.Random(zlib.crc32(name.encode()))
    return colorsys.hsv_to_rgb((h0 + r.uniform(-.07, .07)) % 1, max(.1, s0 + r.uniform(-.15, .15)), min(.9, max(.25, v0 + r.uniform(-.12, .12))))

def faces(p):
    x, y, z, w, h, d = p['boxes'][0][:6]
    u, v = p['uv']; w, h, d = int(w), int(h), int(d)
    return {'top': (u + d, v, w, d, 1.12), 'bottom': (u + d + w, v, w, d, .72), 'right': (u, v + d, d, h, .92), 'front': (u + d, v + d, w, h, 1.0),
            'left': (u + d + w, v + d, d, h, .86), 'back': (u + 2 * d + w, v + d, w, h, .8)}

def paint(plan, el):
    m = json.load(open(f'{PL}/{plan}.json'))
    tw, th = m['tw'], m['th']; cls = m['meta']['cls']
    c = base(plan, cls)
    if TINT[el]:
        t = TINT[el]; c = tuple(.7 * c[i] + .3 * t[i] / 255 for i in range(3))
    im = Image.new('RGBA', (tw, th), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(zlib.crc32((plan + el).encode()))
    for n, p in m['parts'].items():
        eye = n.startswith('eye')
        for f, (fx, fy, fw, fh, sh) in faces(p).items():
            for yy in range(fy, fy + fh):
                for xx in range(fx, fx + fw):
                    if not (0 <= xx < tw and 0 <= yy < th) or fw <= 0 or fh <= 0: continue
                    k = sh * (1 + rnd.uniform(-.07, .07))
                    if f in ('right', 'left', 'front', 'back') and fh > 2 and yy - fy >= fh * .7: k *= 1.12  # lighter belly
                    col = (12, 10, 10) if eye else tuple(max(0, min(255, int(255 * ch * k))) for ch in c)
                    px[xx, yy] = (*col, 255)
    return im

if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    plans = json.load(open(f'{PL}/index.json'))
    for pl in plans:
        for el in TINT: paint(pl, el).save(f'{OUT}/{pl}_{el}.png', optimize=True)
    print(len(plans) * len(TINT), 'textures ->', OUT)
