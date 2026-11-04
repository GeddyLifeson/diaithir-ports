#!/usr/bin/env python3
"""IV only spawns a slot's defaultPart when the slot has minValue/maxValue (found with the tank gun and engine: slots without them spawn empty).
Adds 0..10 to every defaultPart slot of the car and truck vehicles that lacks them. Run after gen_vehicles.py / gen_trucks.py, from the repo root:
python3 diaithir-carts/tools/fix_slot_ranges.py ."""
import json, glob, sys
root = sys.argv[1] if len(sys.argv) > 1 else '.'
n = 0
for f in glob.glob(f'{root}/diaithir-carts/src/main/resources/assets/diaithirvehicles/jsondefs/vehicles/*.json') + glob.glob(f'{root}/diaithir-arms/src/main/resources/assets/diaithirtrucks/jsondefs/vehicles/*.json'):
    raw = open(f).read(); d = json.loads(raw); ch = False
    for p in d.get('parts', []):
        if 'defaultPart' in p and 'minValue' not in p:
            p['minValue'] = 0.0; p['maxValue'] = 10.0; n += 1; ch = True
    if ch: json.dump(d, open(f, 'w'), indent=1 if raw.startswith('{\n ') else None)
print(n, 'slots given a range')
