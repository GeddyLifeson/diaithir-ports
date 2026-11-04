#!/usr/bin/env python3
"""Generates the Diaithir aircraft pack for Immersive Vehicles (pack id diaithiraircraft): parametric OBJ models (fuselage, wing, tail
boxes), flat textures and JSON defs for each design in SPECS. Original geometry, not copied from any model. Dimensions are rounded from the
Wikipedia page named in aircraft/ROSTER.csv (approximate; check against the source before relying on a number).
Usage: python3 -I tools/gen_aircraft.py <module dir>"""
import csv, json, os, sys
from PIL import Image

M = sys.argv[1] if len(sys.argv) > 1 else '.'
PK = 'diaithiraircraft'
A = f'{M}/src/main/resources/assets/{PK}'
ROSTER = {r['id']: r for r in csv.DictReader(open(f'{M}/aircraft/ROSTER.csv'))}

# id: (span m, length m, empty kg, seats, engines, kind, fuel L, colour)
SPECS = {
 'wright_flyer': (12.3, 6.4, 274, 1, 1, 'piston', 60, (200, 190, 150)),
 'bleriot_xi': (7.7, 7.6, 230, 1, 1, 'piston', 30, (190, 170, 120)),
 'sopwith_camel': (8.5, 5.7, 421, 1, 1, 'piston', 150, (110, 120, 80)),
 'supermarine_spitfire': (11.2, 9.1, 2300, 1, 1, 'piston', 380, (95, 110, 90)),
 'p51_mustang': (11.3, 9.8, 3230, 1, 1, 'piston', 700, (170, 175, 180)),
 'dc3': (29.0, 19.7, 8300, 25, 2, 'piston', 3100, (190, 195, 200)),
 'b17_flying_fortress': (31.6, 22.7, 16400, 10, 4, 'piston', 10000, (120, 125, 100)),
 'me262': (12.6, 10.6, 4000, 1, 2, 'jet', 2000, (110, 110, 100)),
 'cessna_172': (11.0, 8.3, 767, 4, 1, 'piston', 200, (230, 230, 235)),
 'boeing_747': (59.6, 70.7, 178000, 400, 4, 'jet', 200000, (225, 228, 232)),
 'bell_uh1_huey': (14.6, 12.8, 2370, 12, 1, 'jet', 850, (85, 95, 80)),
}

def box(x0, y0, z0, x1, y1, z1):
    p = lambda x, y, z: (x, y, z)
    return [[p(x0,y0,z1),p(x1,y0,z1),p(x1,y1,z1),p(x0,y1,z1)], [p(x1,y0,z0),p(x0,y0,z0),p(x0,y1,z0),p(x1,y1,z0)],
            [p(x0,y1,z1),p(x1,y1,z1),p(x1,y1,z0),p(x0,y1,z0)], [p(x0,y0,z0),p(x1,y0,z0),p(x1,y0,z1),p(x0,y0,z1)],
            [p(x1,y0,z1),p(x1,y0,z0),p(x1,y1,z0),p(x1,y1,z1)], [p(x0,y0,z0),p(x0,y0,z1),p(x0,y1,z1),p(x0,y1,z0)]]

def write_obj(path, objs):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    L, vi = [], 0
    for name, faces in objs:
        L.append(f'o {name}')
        for f in faces:
            for p in f: L.append('v %.3f %.3f %.3f' % p)
            for u, v in [(0, 0), (1, 0), (1, 1), (0, 1)]: L.append(f'vt {u} {v}')
            L.append('vn 0 1 0')
            L.append('f ' + ' '.join(f'{vi+i+1}/{vi+i+1}/1' for i in range(4)))
            vi += 4
    open(path, 'w').write('\n'.join(L) + '\n')

def airframe(span, length, kind, engines):
    h = max(span, length) * 0.09
    objs = [('fuselage', box(-h/2, 0, -length/2, h/2, h, length/2)),
            ('wing', box(-span/2, h*0.3, -length*0.1, span/2, h*0.4, length*0.12)),
            ('tailplane', box(-span*0.18, h*0.6, -length/2, span*0.18, h*0.65, -length/2 + length*0.1)),
            ('fin', box(-0.05, h, -length/2, 0.05, h*2, -length/2 + length*0.12))]
    for i in range(engines if kind == 'jet' else 0):
        x = (i - (engines - 1) / 2) * span * 0.22
        objs.append((f'nacelle{i}', box(x - h*0.2, h*0.1, -length*0.05, x + h*0.2, h*0.5, length*0.2)))
    return objs

def w(path, d):
    os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=1))

w(f'{A}/packdefinition.json', {'packID': PK, 'packName': 'Diaithir Aircraft', 'packItem': 'wright_flyer', 'fileStructure': 0})
manifest = []
for pid, (span, length, mass, seats, n, kind, fuel, col) in SPECS.items():
    r = ROSTER[pid]
    tex = f'{A}/textures/vehicles/{pid}.png'
    os.makedirs(os.path.dirname(tex), exist_ok=True); Image.new('RGB', (16, 16), col).save(tex)
    write_obj(f'{A}/objmodels/vehicles/{pid}.obj', airframe(span, length, kind, n))
    eng = {'general': {'type': 'engine_aircraft', 'name': f'{r["name"]} engine', 'materialLists': [['minecraft:iron_block:0:%d' % (2 * n + 2)]]},
           'engine': {'type': 'NORMAL', 'isAutomatic': True, 'fuelType': 'avgas', 'fuelConsumption': 1.0 if kind == 'jet' else 0.6,
                      'maxRPM': 3000, 'maxSafeRPM': 2800, 'idleRPM': 600, 'startRPM': 500, 'stallRPM': 350, 'revlimitRPM': 3200,
                      'starterPower': 6, 'gearRatios': [1.0], 'upShiftRPM': [2800], 'downShiftRPM': [1000], 'shiftSpeed': 10,
                      'clutchTime': 12, 'revResistance': 8, 'heatingCoefficient': 0.3, 'coolingCoefficient': 0.5, 'engineWearFactor': 0.3,
                      **({'jetPowerFactor': 1.0, 'bypassRatio': 0.0} if kind == 'jet' else {'propellerRatio': 1.0})},
           'definitions': [{'subName': '', 'name': f'{r["name"]} engine', 'extraMaterialLists': [[]]}]}
    w(f'{A}/jsondefs/parts/{pid}_engine.json', eng)
    parts = [{'pos': [(i - (n - 1) / 2) * span * 0.22, 0.8, length * 0.2], 'types': ['engine_aircraft'], 'defaultPart': f'{PK}:{pid}_engine'} for i in range(n)]
    parts += [{'pos': [0, 0.9 + 0.8 * (i // 2), -length * 0.05 - (i % 2) * 0.8], 'types': ['seat'], 'defaultPart': 'mts:unified.seat'} for i in range(min(seats, 4))]
    v = {'general': {'type': 'vehicle', 'name': r['name'], 'description': f'{r["category"]}, {r["country"]}, {r["first_flight_year"]}. Source: {r["source"]}',
                     'materialLists': [['minecraft:iron_ingot:0:%d' % max(8, int(mass / 120))]], 'health': max(100, int(mass / 10))},
         'motorized': {'isAircraft': True, 'emptyMass': mass, 'fuelCapacity': fuel, 'defaultFuelQty': fuel // 4,
                       'wingSpan': span, 'wingArea': round(span * length * 0.12, 1), 'tailDistance': round(length * 0.45, 1),
                       'aileronArea': round(span * 0.15, 1), 'elevatorArea': round(span * 0.12, 1), 'rudderArea': round(span * 0.06, 1),
                       'dragCoefficient': 0.05, 'crossSectionalArea': round(span * 0.09 * length * 0.09, 1),
                       'crashSpeedMin': 0.5, 'crashSpeedMax': 1.0, 'crashSpeedDestroyed': 2.0},
         'parts': parts}
    w(f'{A}/jsondefs/vehicles/{pid}.json', v)
    manifest.append({'id': pid, 'name': r['name'], 'age': r['age'], 'year': int(r['first_flight_year']), 'source': r['source']})
w(f'{M}/aircraft/aircraft_manifest.json', {'vehicles': manifest})
w(f'{M}/age_gates_aircraft.json', {'_doc': 'IV item ids by Age', 'items': {f'mts:{PK}.{m["id"]}': m['age'] for m in manifest}})
os.system(f'python3 {os.path.dirname(os.path.abspath(__file__))}/ivfix.py {A}')
print(len(manifest), 'aircraft written')
