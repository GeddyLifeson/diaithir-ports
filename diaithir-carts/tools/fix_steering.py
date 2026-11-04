#!/usr/bin/env python3
"""Cars and trucks did not steer: IV turns only the wheel slots flagged turnsWithSteer, and none were. Flags the front axle (highest z; the engine sits forward at +z)
of every vehicle that has an engine slot and a controller seat, skipping tracked and skid-steer vehicles. Run from the repo root: python3 diaithir-carts/tools/fix_steering.py ."""
import json, glob, sys
root = sys.argv[1] if len(sys.argv) > 1 else '.'
n = v = 0
for f in glob.glob(f'{root}/diaithir-carts/src/main/resources/assets/diaithirvehicles/jsondefs/vehicles/*.json') + glob.glob(f'{root}/diaithir-arms/src/main/resources/assets/diaithirtrucks/jsondefs/vehicles/*.json'):
    raw = open(f).read(); d = json.loads(raw)
    parts = d.get('parts', [])
    if not any('engine_car' in p['types'] for p in parts) or any('ground_tread' in p['types'] for p in parts) or d.get('motorized', {}).get('hasSkidSteer'): continue
    wheels = [p for p in parts if 'ground_wheel' in p['types']]
    if not wheels: continue
    front = max(p['pos'][2] for p in wheels)
    ch = False
    for p in wheels:
        if abs(p['pos'][2] - front) < 0.01 and not p.get('turnsWithSteer'):
            p['turnsWithSteer'] = True; n += 1; ch = True
    if ch:
        v += 1; json.dump(d, open(f, 'w'), indent=1 if raw.startswith('{\n ') else None)
print(n, 'front wheels on', v, 'vehicles')
