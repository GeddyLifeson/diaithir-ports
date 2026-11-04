#!/usr/bin/env python3
"""Seats could not be clicked on cars and trucks: the vehicle's single hull box ('collision') takes clicks and covers them. Same fix the tank got
(81a7310731, CLICKBOX in gen_tank.py): the hull becomes a collisionGroups entry without 'click', and seat and engine parts get their own click box.
Run from the repo root: python3 diaithir-carts/tools/fix_click_boxes.py ."""
import json, glob, sys
root = sys.argv[1] if len(sys.argv) > 1 else '.'
PACKS = [('diaithir-carts', 'diaithirvehicles'), ('diaithir-arms', 'diaithirtrucks')]
HULL = ['block', 'entity', 'vehicle', 'attack', 'bullet']
CLICK = {'seat': (0.9, 0.9), 'engine_car': (1.0, 0.8)}
nv = np = 0
for repo, pk in PACKS:
    a = f'{root}/{repo}/src/main/resources/assets/{pk}/jsondefs'
    for f in glob.glob(f'{a}/vehicles/*.json'):
        raw = open(f).read(); d = json.loads(raw)
        if 'collision' not in d: continue
        d['collisionGroups'] = [{'collisionTypes': HULL, 'isInterior': False, 'collisions': d.pop('collision')}]
        json.dump(d, open(f, 'w'), indent=1 if raw.startswith('{\n ') else None); nv += 1
    for f in glob.glob(f'{a}/parts/*.json'):
        raw = open(f).read(); d = json.loads(raw)
        t = d.get('general', {}).get('type')
        if t in CLICK and 'collisionGroups' not in d:
            wd, ht = CLICK[t]
            d['collisionGroups'] = [{'collisionTypes': ['click', 'attack', 'bullet'], 'isInterior': False, 'collisions': [{'pos': [0, 0.0, 0], 'width': wd, 'height': ht}]}]
            json.dump(d, open(f, 'w'), indent=1 if raw.startswith('{\n ') else None); np += 1
print(nv, 'vehicles,', np, 'parts')
