#!/usr/bin/env python3
"""Inventory icons for the vehicle and part items of diaithirvehicles (carts) and diaithirtrucks (arms): 32x32 PNG in
textures/item/<name>.png plus models/item/<vehicles|parts>/<name>.json, same layout gen_tank.py uses for tanks.
Body colour = mean opaque colour of the skin's paint texture. Run from the repo root: python3 diaithir-carts/tools/gen_icons.py ."""
import json, os, sys, glob
from PIL import Image, ImageDraw
root = sys.argv[1] if len(sys.argv) > 1 else '.'
PACKS = [('diaithir-carts', 'diaithirvehicles'), ('diaithir-arms', 'diaithirtrucks')]

def mean(path, fallback=(120, 120, 124)):
    try:
        im = Image.open(path).convert('RGBA').resize((16, 16)); px = [p for p in im.getdata() if p[3] > 128]
        return tuple(sum(p[i] for p in px) // len(px) for i in range(3)) if px else fallback
    except OSError:
        return fallback

def vehicle(body, kind):
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    dark = tuple(max(0, c - 45) for c in body)
    if kind == 'trailer':
        d.rectangle([2, 14, 29, 19], fill=body, outline=dark)
    elif kind == 'truck':
        d.rectangle([2, 13, 20, 21], fill=body, outline=dark); d.rectangle([21, 9, 29, 21], fill=body, outline=dark); d.rectangle([23, 11, 28, 15], fill=(170, 200, 220))
    else:
        d.rounded_rectangle([3, 13, 28, 21], 3, fill=body, outline=dark); d.rectangle([9, 8, 21, 14], fill=body, outline=dark); d.rectangle([11, 9, 19, 13], fill=(170, 200, 220))
    for x in (7, 23): d.ellipse([x - 3, 19, x + 3, 25], fill=(38, 38, 40), outline=(90, 90, 94))
    return im

def part(name):
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    if 'wheel' in name or 'tyre' in name or 'tire' in name:
        d.ellipse([5, 5, 26, 26], fill=(38, 38, 40)); d.ellipse([11, 11, 20, 20], fill=(150, 150, 156))
    elif 'seat' in name:
        d.rectangle([9, 5, 22, 17], fill=(88, 64, 48), outline=(50, 36, 28)); d.rectangle([7, 17, 24, 24], fill=(70, 52, 40), outline=(40, 30, 24))
    else:
        d.rectangle([6, 10, 25, 24], fill=(104, 108, 114), outline=(50, 52, 56)); d.rectangle([9, 6, 22, 10], fill=(80, 84, 90))
        for x in (10, 15, 20): d.rectangle([x, 12, x + 2, 21], fill=(60, 62, 66))
    return im

def save(a, pk, folder, base, im):
    os.makedirs(f'{a}/textures/item', exist_ok=True); os.makedirs(f'{a}/models/item/{folder}', exist_ok=True)
    im.save(f'{a}/textures/item/{base}.png')
    json.dump({'parent': 'mts:item/basic', 'textures': {'layer0': f'{pk}:item/{base}'}}, open(f'{a}/models/item/{folder}/{base}.json', 'w'), indent=1)

n = 0
for repo, pk in PACKS:
    a = f'{root}/{repo}/src/main/resources/assets/{pk}'
    for f in sorted(glob.glob(f'{a}/jsondefs/vehicles/*.json')):
        d = json.load(open(f)); sysn = os.path.basename(f)[:-5]
        kind = 'trailer' if 'trailer' in sysn else 'truck' if pk == 'diaithirtrucks' else 'car'
        for df in d['definitions']:
            tex = df.get('textureName', sysn); sub = df.get('subName', '')
            save(a, pk, 'vehicles', sysn + sub, vehicle(mean(f'{a}/textures/vehicles/{tex}{sub}.png'), kind)); n += 1
    for f in sorted(glob.glob(f'{a}/jsondefs/parts/*.json')):
        d = json.load(open(f)); sysn = os.path.basename(f)[:-5]
        for df in d.get('definitions', [{'subName': ''}]):
            save(a, pk, 'parts', sysn + df.get('subName', ''), part(sysn)); n += 1
print(n, 'icons')
