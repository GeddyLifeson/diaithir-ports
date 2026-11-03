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
