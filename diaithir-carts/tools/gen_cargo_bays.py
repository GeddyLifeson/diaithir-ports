#!/usr/bin/env python3
"""Writes diaithir_cargo_bays.json for the carts jar (cars, pickups, vans, APC) and the arms jar (trailers, haul trucks, tanks).

A bay is the cargo space a vehicle offers through the freight CargoCarrier capability: w/h/l in whole cells (1 cell = 1 m), the payload rating in kg, and
(ox, oy, oz) = the vehicle-local metre position of the bay's corner at cell (0,0,0). The key is "<pack id>:<vehicle id>". Sizes come from the generated
manifests (trailer length and payload) and fixed layouts for cars; the tractor units pull trailers and carry nothing themselves.
Usage: python3 tools/gen_cargo_bays.py <repo root>
"""
import json
import sys
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else '.')


def bay(w, h, l, kg, deck, zc=0.0):
    return dict(w=w, h=h, l=l, payload_kg=kg, ox=-w / 2, oy=deck, oz=round(zc - l / 2, 3))


# cars and light trucks (carts pack): trunk, bed or cargo hold; the deck height is the floor of that space
carts = {
    'benz_motorwagen': bay(1, 1, 1, 100, 0.6, -0.3),
    'model_t': bay(1, 1, 1, 150, 0.7, -1.0),
    'ford_f1': bay(1, 1, 2, 500, 0.8, -1.0),
    'willys_mb': bay(1, 1, 2, 300, 0.7, -0.8),
    'land_rover_series_i': bay(1, 1, 2, 450, 0.8, -0.9),
    'toyota_corolla': bay(1, 1, 1, 300, 0.7, -1.4),
    'vw_type_2': bay(1, 1, 2, 800, 0.8, -0.4),
    'ford_econoline_ambulance': bay(1, 1, 2, 500, 0.8, -0.9),
    'routemaster': bay(1, 1, 2, 800, 0.4, 0.0),
    'rolls_royce_armoured_car': bay(1, 1, 1, 400, 0.9, -0.9),
    'mack_type_75': bay(2, 2, 4, 5000, 1.0, -1.2),
    'm113': bay(2, 1, 2, 1500, 0.9, -0.6),
    'holmes_wrecker': bay(1, 1, 2, 2000, 1.0, -1.5),
}

trucks = {}
man = json.load(open(root / 'diaithir-arms/truck_manifest.json'))
for t in man['trailers']:
    k = t['kind']
    if k == 'tank':
        continue  # liquids ride in the tank part, not the bay
    narrow = k in ('cart', 'wagon', 'dray')
    length = max(1, min(14, int(t['length_m'] - 1.5)))
    deckish = k in ('chassis', 'flat', 'lowboy', 'step', 'modular', 'carrier')  # containers are 4 cells wide, so these decks take them
    h = 3 if k in ('box', 'curtain', 'livestock') else 4 if deckish else 2
    deck = 0.85 if narrow else 0.8 if k in ('lowboy', 'modular') else 1.3  # top of the deck in the generated model
    trucks[t['id']] = bay(1 if narrow else 4 if deckish else 2, h, length, t['payload_kg'], deck)
for c in man['construction']:
    if c['kind'] == 'haul':
        trucks[c['id']] = bay(3, 2, 4, 90000, 1.9, -0.5)
    elif c['kind'] == 'dumpcart':
        trucks[c['id']] = bay(1, 1, 1, 600, 0.9)

tanks = {t: bay(1, 1, 2, 500, 2.4, -1.8) for t in (v['id'] if isinstance(v, dict) else v for v in json.load(open(root / 'diaithir-arms/tank_manifest.json'))['vehicles'])}

(root / 'diaithir-carts/src/main/resources/diaithir_cargo_bays.json').write_text(json.dumps({f'diaithirvehicles:{k}': v for k, v in carts.items()}, indent=1) + '\n')
arms = {f'diaithirtrucks:{k}': v for k, v in trucks.items()}
arms.update({f'diaithirtanks:{k}': v for k, v in tanks.items()})
(root / 'diaithir-arms/src/main/resources/diaithir_cargo_bays.json').write_text(json.dumps(arms, indent=1) + '\n')
print(len(carts), 'cars,', len(trucks), 'trailers and haulers,', len(tanks), 'tanks')
