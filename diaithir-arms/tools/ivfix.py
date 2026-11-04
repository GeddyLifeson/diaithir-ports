#!/usr/bin/env python3
"""Idempotent Immersive Vehicles 25 compatibility pass over a generated pack: python3 tools/ivfix.py <assets/PACK dir>
Found with the cloud headless client bot. IV 25 (a) ignores the legacy vehicle 'collision' key, so nothing could be clicked or entered;
(b) only spawns a default part whose height (wheel) / fuelConsumption (engine) / diameter (gun) lies inside the slot's minValue..maxValue,
which defaulted to 0..0, so engines, wheels, tracks and guns never spawned; (c) crashes the client when a ground_tread part has no tread path
in the body model, so treads become wheel-type parts; (d) a body box with 'click' hides the seat boxes inside it."""
import glob, json, os, sys
A = sys.argv[1]
CLICK = lambda w_, h_: [{'collisionTypes': ['click', 'attack', 'bullet'], 'isInterior': False, 'collisions': [{'pos': [0, 0, 0], 'width': w_, 'height': h_}]}]
def load(p): return json.load(open(p))
def save(p, j): open(p, 'w').write(json.dumps(j, indent=1))
n = 0
for p in glob.glob(f'{A}/jsondefs/parts/*.json'):
    j = load(p); g = j.get('general', {}); ch = False
    if g.get('type') == 'ground_tread':
        g['type'] = 'ground_wheel'; gr = j['ground']
        for k in ('isTread', 'spacing', 'canGoFlat'): gr.pop(k, None)
        gr['isWheel'] = True; ch = True
    if g.get('type') == 'seat' and not j.get('collisionGroups'):
        j.setdefault('seat', {'standing': False, 'widthScale': 1.0, 'heightScale': 1.0}); g.setdefault('width', 0.9); g.setdefault('height', 0.9); g.setdefault('mass', 60)
        j['collisionGroups'] = CLICK(0.9, 0.9); ch = True
    if ch: save(p, j); n += 1
for p in glob.glob(f'{A}/jsondefs/vehicles/*.json'):
    j = load(p); ch = False
    for s in j.get('parts', []):
        ts = s.get('types', [])
        if any(t.startswith(('engine', 'ground_', 'gun', 'propeller')) for t in ts):
            if 'ground_tread' in ts: s['types'] = ['ground_wheel' if t == 'ground_tread' else t for t in ts]; ch = True
            if not s.get('maxValue', 0) > 0: s['minValue'] = 0.0; s['maxValue'] = 1000.0; ch = True
    if 'collision' in j:
        boxes = j.pop('collision'); hp = j.get('general', {}).get('health', 100)
        j['collisionGroups'] = [{'collisionTypes': ['block', 'vehicle', 'entity', 'attack', 'bullet'], 'isInterior': False, 'health': hp, 'armorThickness': 3.0, 'collisions': boxes}]; ch = True
    for grp in j.get('collisionGroups', []):
        if 'click' in grp.get('collisionTypes', []): grp['collisionTypes'] = ['entity' if t == 'click' else t for t in grp['collisionTypes']]; ch = True
    if ch: save(p, j); n += 1
print('ivfix', A, n, 'files changed')
# item icons: every vehicle/part/bullet (and subName variant) needs models/item/<kind>/<id><subName>.json or IV logs "Missing textures in model" and draws magenta
try:
    from PIL import Image, ImageDraw
except ImportError:
    Image = None
if Image:
    PK = os.path.basename(os.path.normpath(A)); made = 0
    for kind in ('vehicles', 'parts', 'bullets'):
        for p in glob.glob(f'{A}/jsondefs/{kind}/*.json'):
            j = load(p); base = os.path.basename(p)[:-5]
            for dfn in (j.get('definitions') or [{'subName': ''}]):
                nm = base + dfn.get('subName', ''); mp = f'{A}/models/item/{kind}/{nm}.json'
                if os.path.exists(mp): continue
                os.makedirs(os.path.dirname(mp), exist_ok=True); os.makedirs(f'{A}/textures/item', exist_ok=True)
                h = sum(map(ord, nm)); rgb = (60 + h * 7 % 150, 60 + h * 13 % 150, 60 + h * 29 % 150)
                im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d_ = ImageDraw.Draw(im)
                if kind == 'vehicles': d_.rectangle([1, 6, 14, 11], fill=rgb + (255,)); d_.rectangle([4, 3, 11, 6], fill=rgb + (255,)); d_.ellipse([2, 10, 5, 13], fill=(25, 25, 25, 255)); d_.ellipse([10, 10, 13, 13], fill=(25, 25, 25, 255))
                else: d_.ellipse([3, 3, 12, 12], fill=rgb + (255,))
                im.save(f'{A}/textures/item/{nm}.png'); open(mp, 'w').write(json.dumps({'parent': 'mts:item/basic', 'textures': {'layer0': f'{PK}:item/{nm}'}}, indent=1)); made += 1
    print('ivfix icons', made)
