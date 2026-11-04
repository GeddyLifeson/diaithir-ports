#!/usr/bin/env python3
"""Writes the Diaithir road-vehicle pack for Immersive Vehicles (pack id diaithirvehicles) from vehicles/roster.csv and the shared fuel table.
Ships inside the diaithir-carts jar (any jar holding assets/<id>/packdefinition.json is a pack), like the tank pack in diaithir-arms.
Original box models (helpers reused from diaithir-arms/tools/gen_tank.py), flat paint. Rows with pack=data get no vehicle (roster only).
Usage: python3 -I tools/gen_vehicles.py <diaithir-carts dir>"""
import csv, json, os, random, sys
M = os.path.abspath(sys.argv[1] if len(sys.argv) > 1 else '.')
GT = os.path.join(M, '..', 'diaithir-arms', 'tools', 'gen_tank.py')
sys.argv = [sys.argv[0], os.path.join(M, '..', 'diaithir-arms')]
exec(open(GT).read().split("\nR = {")[0])  # Obj, box, cyl_x, wedge helpers and the PIL imports
M = os.path.abspath(os.path.join(os.path.dirname(GT), '..', '..', 'diaithir-carts'))  # gen_tank reassigns M
PK = 'diaithirvehicles'
A = f'{M}/src/main/resources/assets/{PK}'
random.seed(26)
R = {'body': (0, 0, .5, .5), 'metal': (.5, 0, 1, .5), 'dark': (0, .5, .5, 1), 'glass': (.5, .5, 1, 1)}

def w(path, d): os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=1))

fuels = {r['id']: r for r in csv.DictReader(open(f'{M}/../diaithir-units/src/main/resources/data/diaithirunits/fuels.csv'))}
EFF = {'petrol_engine': 0.25, 'diesel_engine': 0.35}   # keep in step with Fuels.java
IVTYPE = {'petrol': 'gasoline', 'diesel': 'diesel'}

def iv_consumption(fuel, kw, eff):                      # mB per tick, same formula as Fuels.ivConsumption
    return round(kw * 1000 / eff / (float(fuels[fuel]['mj_per_unit']) * 1e6) * 1000 / 20, 3)

# body: length, width, height, wheel radius, paint, defaults (mass kg, kW, tank L) used only where the roster row is blank (design defaults, not sourced)
BODY = {
 'sedan': (4.2, 1.7, 1.45, 0.33, (150, 40, 40), (1100, 60, 50)), 'jeep': (3.4, 1.6, 1.5, 0.38, (84, 94, 58), (1200, 50, 60)),
 'pickup': (5.0, 1.9, 1.7, 0.38, (60, 90, 140), (1600, 75, 70)), 'van': (4.6, 1.8, 2.1, 0.36, (230, 230, 225), (1700, 70, 70)),
 'bus': (8.4, 2.5, 3.0, 0.5, (190, 30, 30), (7500, 90, 140)), 'armcar': (4.9, 1.9, 2.3, 0.42, (96, 100, 84), (4000, 60, 100)),
 'wrecker': (5.2, 2.0, 2.4, 0.4, (200, 170, 40), (2500, 60, 80)), 'firetruck': (6.5, 2.4, 2.9, 0.45, (190, 25, 25), (6000, 80, 150)),
 'apc': (4.9, 2.7, 2.3, 0.0, (84, 94, 62), (11000, 156, 360)),
 'tractor': (2.9, 1.6, 1.6, 0.55, (40, 80, 150), (1200, 15, 76))}
LOW = {'sedan': .55, 'jeep': .55, 'pickup': .6, 'van': .5, 'bus': .5, 'armcar': .6, 'wrecker': .6, 'firetruck': .6, 'apc': .45, 'tractor': .5}

def body_model(kind):
    L, W, H = BODY[kind][:3]; hl, hw = L / 2, W / 2; low = LOW[kind]; o = Obj()
    o.add('chassis', box(-hw, low, -hl, hw, low + .35, hl), R['body'])
    cab = {'sedan': (-.9, 1.0), 'jeep': (-.7, .7), 'pickup': (.1, 1.5), 'van': (-hl + .1, hl - .1), 'bus': (-hl + .1, hl - .1), 'armcar': (-1.6, 1.5),
           'wrecker': (.2, 1.9), 'firetruck': (.9, hl - .1), 'apc': (-hl + .2, hl - .3), 'tractor': (.2, hl - .1)}[kind]
    if kind == 'tractor':   # open farm tractor: engine hood, steering post and a seat pan, no closed cabin
        o.add('hood', box(-.3, low + .35, cab[0], .3, low + 1.0, cab[1]), R['body'])
        o.add('seatpan', box(-.3, low + .35, -hl + .3, .3, low + .55, -hl + .9), R['dark'])
        o.add('post', box(-.05, low + .35, cab[0] - .3, .05, low + 1.0, cab[0] - .25), R['metal'])
    else:
        o.add('cabin', box(-hw + .1, low + .35, cab[0], hw - .1, H, cab[1]), R['body'])
    if kind != 'tractor': o.add('windscreen', box(-hw + .2, low + .75, cab[1], hw - .2, H - .1, cab[1] + .05), R['glass'])
    if kind == 'wrecker': o.add('boom', box(-.15, low + .35, -hl, .15, H + .4, -hl + 1.6), R['metal'])
    if kind == 'firetruck': o.add('ladder', box(-.3, H, -hl + .2, .3, H + .25, hl - 1.6), R['metal'])
    if kind == 'pickup': o.add('bed', box(-hw, low + .35, -hl, hw, low + .8, -.1), R['dark'])
    o.add('lamp_l', box(-hw + .1, low + .4, hl, -hw + .4, low + .6, hl + .05), R['glass'])
    o.add('lamp_r', box(hw - .4, low + .4, hl, hw - .1, low + .6, hl + .05), R['glass'])
    return o

def atlas(base, path):
    im = Image.new('RGB', (64, 64), (20, 20, 20)); px = im.load(); d = ImageDraw.Draw(im)
    for x in range(32):
        for y in range(32): v = random.randint(-8, 8); px[x, y] = tuple(max(0, min(255, c + v)) for c in base)
    for x in range(32, 64):
        for y in range(32): v = random.randint(-8, 8); px[x, y] = tuple(max(0, c + v) for c in (104, 108, 114))
    d.rectangle([0, 32, 31, 63], fill=(22, 22, 24)); d.rectangle([32, 32, 63, 63], fill=(70, 96, 110))
    os.makedirs(os.path.dirname(path), exist_ok=True); im.save(path)

def simple(path, name, boxes):
    o = Obj(); o.add(name, boxes, R['dark']); o.write(path)

rows = [r for r in csv.DictReader(open(f'{M}/vehicles/roster.csv')) if r['pack'] == 'pack']
w(f'{A}/packdefinition.json', {'packID': PK, 'packName': 'Diaithir Road Vehicles', 'packItem': rows[0]['id'], 'fileStructure': 0})
simple(f'{A}/objmodels/parts/vehicle_seat.obj', 'seat', box(-.25, 0, -.25, .25, .45, .25) + box(-.25, .45, -.28, .25, 1.0, -.2))
simple(f'{A}/objmodels/parts/vehicle_engine.obj', 'engine', box(-.4, 0, -.4, .4, .5, .4))
atlas((90, 90, 90), f'{A}/textures/parts/vehicle_seat.png'); atlas((90, 90, 90), f'{A}/textures/parts/vehicle_engine.png')
# IV 25 reads collisionGroups only (the legacy 'collision' key is ignored): a seat without a click box can never be sat in.
CLICK = lambda w_, h_: [{'collisionTypes': ['click', 'attack', 'bullet'], 'isInterior': False, 'collisions': [{'pos': [0, 0, 0], 'width': w_, 'height': h_}]}]
w(f'{A}/jsondefs/parts/vehicle_seat.json', {'seat': {'standing': False, 'widthScale': 1.0, 'heightScale': 1.0},
   'general': {'type': 'seat', 'name': 'Vehicle Seat', 'width': 0.9, 'height': 0.9, 'mass': 60, 'materialLists': [['minecraft:white_wool:0:2']]}, 'collisionGroups': CLICK(0.9, 0.9),
   'definitions': [{'subName': '', 'name': 'Vehicle Seat', 'extraMaterialLists': [[]]}]})
w(f'{A}/jsondefs/parts/track_apc.json', {'general': {'type': 'ground_wheel', 'name': 'APC Track', 'materialLists': [['minecraft:iron_ingot:0:16']]},
   'ground': {'isWheel': True, 'width': 0.6, 'height': 0.7, 'flatHeight': 0.7, 'climbHeight': 0.7, 'motiveFriction': 1.0, 'lateralFriction': 1.3, 'extraCollisionBoxOffset': 0.0},
   'definitions': [{'subName': '', 'name': 'APC Track', 'extraMaterialLists': [[]]}]})
simple(f'{A}/objmodels/parts/track_apc.obj', 'track', box(-.3, .05, -2.4, .3, .7, 2.4))
atlas((46, 44, 42), f'{A}/textures/parts/track_apc.png')
ICON = {'sedan': (150, 40, 40), 'jeep': (84, 94, 58), 'pickup': (60, 90, 140), 'van': (230, 230, 225), 'bus': (190, 30, 30), 'armcar': (96, 100, 84), 'wrecker': (200, 170, 40), 'firetruck': (190, 25, 25), 'apc': (84, 94, 62)}
def icon(name, kind_, rgb):       # 16x16 flat item icon + the item model IV resolves as mts:item/<pack>.<id>
    os.makedirs(f'{A}/textures/item', exist_ok=True); os.makedirs(f'{A}/models/item/{kind_}', exist_ok=True)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d_ = ImageDraw.Draw(im)
    if kind_ == 'vehicles': d_.rectangle([1, 6, 14, 11], fill=rgb + (255,)); d_.rectangle([4, 3, 11, 6], fill=rgb + (255,)); d_.ellipse([2, 10, 5, 13], fill=(25, 25, 25, 255)); d_.ellipse([10, 10, 13, 13], fill=(25, 25, 25, 255))
    else: d_.ellipse([3, 3, 12, 12], fill=rgb + (255,))
    im.save(f'{A}/textures/item/{name}.png'); w(f'{A}/models/item/{kind_}/{name}.json', {'parent': 'mts:item/basic', 'textures': {'layer0': f'{PK}:item/{name}'}})
icon('vehicle_seat', 'parts', (200, 200, 200)); icon('track_apc', 'parts', (46, 44, 42))
manifest = []
for v in rows:
    kind = v['body']; L, W, H, r, paint, (dm, dkw, dl) = BODY[kind]; vid = v['id']; fuel = v['fuel']; name = v['iconic_design']
    mass = float(v['empty_mass_kg'] or dm); kw = float(v['power_kw'] or dkw); tank = float(v['tank_l_or_kg'] or dl); eff = EFF[v['engine']]
    icon(vid, 'vehicles', ICON[kind]); icon(f'wheel_{vid}', 'parts', (30, 30, 30)); icon(f'engine_{vid}', 'parts', (90, 90, 90))
    body_model(kind).write(f'{A}/objmodels/vehicles/{vid}.obj'); atlas(paint, f'{A}/textures/vehicles/{vid}.png')
    if kind != 'apc':
        simple(f'{A}/objmodels/parts/wheel_{vid}.obj', 'tyre', cyl_x(0, 0, -.15, .15, r, 12)); atlas((30, 30, 30), f'{A}/textures/parts/wheel_{vid}.png')
        w(f'{A}/jsondefs/parts/wheel_{vid}.json', {'general': {'type': 'ground_wheel', 'name': f'{name} Wheel', 'materialLists': [['minecraft:iron_ingot:0:2']]},
           'ground': {'isWheel': True, 'width': 0.3, 'height': round(r * 2, 3), 'motiveFriction': 1.0, 'lateralFriction': 1.0},
           'definitions': [{'subName': '', 'name': f'{name} Wheel', 'modelName': f'wheel_{vid}', 'textureName': f'wheel_{vid}', 'extraMaterialLists': [[]]}]})
    rpm = 2800 if v['engine'] == 'diesel_engine' else 6000 if kw < 60 else 4500
    w(f'{A}/jsondefs/parts/engine_{vid}.json', {'general': {'type': 'engine_car', 'name': f'{name} Engine', 'materialLists': [['minecraft:iron_block:0:2'], ['minecraft:piston:0:2']]},
       'engine': {'type': 'NORMAL', 'isAutomatic': True, 'fuelType': IVTYPE[fuel], 'fuelConsumption': iv_consumption(fuel, kw, eff), 'maxRPM': rpm, 'maxSafeRPM': int(rpm * .93), 'idleRPM': 700,
                  'startRPM': 500, 'stallRPM': 350, 'revlimitRPM': int(rpm * 1.05), 'starterPower': 6, 'gearRatios': [-3.5, 0.0, 3.6, 2.1, 1.4, 1.0], 'upShiftRPM': [int(rpm * .7)] * 4,
                  'downShiftRPM': [int(rpm * .3)] * 4, 'shiftSpeed': 10, 'clutchTime': 12, 'revResistance': 8, 'heatingCoefficient': 0.4, 'coolingCoefficient': 0.6, 'engineWearFactor': 0.3},
       'definitions': [{'subName': '', 'name': f'{name} Engine', 'modelName': 'vehicle_engine', 'textureName': 'vehicle_engine', 'extraMaterialLists': [[]]}]})
    hl, hw = L / 2, W / 2
    parts = [{'pos': [0, 0.7, hl - 1.0], 'types': ['engine_car'], 'defaultPart': f'{PK}:engine_{vid}', 'minValue': 0.0, 'maxValue': 100.0}]
    if kind == 'apc':
        parts += [{'pos': [sx * (hw - .3), 0, 0], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:track_apc', 'minValue': 0.0, 'maxValue': 5.0} for sx in (-1, 1)]
    else:
        for z in ((hl - .9, -hl + .9) if L < 7 else (hl - 1.0, -hl + 1.2, -hl + 2.6)):
            parts += [{'pos': [sx * (hw - .12), r, z], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:wheel_{vid}', 'minValue': 0.0, 'maxValue': 5.0} for sx in (-1, 1)]
    parts.append({'pos': [-.4, 0.9, 0.2], 'types': ['seat'], 'isController': True, 'defaultPart': f'{PK}:vehicle_seat', 'dismountPos': [-hw - .8, 0.5, 0.2]})
    parts += [{'pos': [.4 if i % 2 == 0 else -.4, 0.9, -.8 - (i // 2) * .8], 'types': ['seat'], 'defaultPart': f'{PK}:vehicle_seat'} for i in range(min(int(v['seats']) - 1, 8))]
    w(f'{A}/jsondefs/vehicles/{vid}.json', {
     'general': {'type': 'vehicle', 'name': name, 'description': f'{v["type"].capitalize()} ({v["year"]}). Fuel: {fuels[fuel]["name"]}.',
                 'materialLists': [[f'minecraft:iron_ingot:0:{max(8, int(mass / 60))}'], ['minecraft:redstone:0:4']], 'health': int(100 + mass / 10)},
     'motorized': {'emptyMass': mass, 'fuelCapacity': int(tank * 1000), 'defaultFuelQty': int(tank * 1000 * 0.25), 'hasRunningLights': True, 'hasHeadlights': True, 'axleRatio': 3.6, 'brakingFactor': 1.0,
                   'dragCoefficient': 0.5, 'crossSectionalArea': round(W * H * .8, 2), 'steeringForceFactor': 1.0, 'crashSpeedMin': 0.5, 'crashSpeedMax': 1.0, 'crashSpeedDestroyed': 2.0,
                   **({'hasSkidSteer': True, 'hasPermanentSkidSteer': True} if kind == 'apc' else {}), 'isBigTruck': kind in ('bus', 'firetruck')},
     'car': {'isFrontWheelDrive': False, 'isRearWheelDrive': kind != 'apc', 'axleRatio': 3.6, 'dragCoefficient': 0.5},
     'parts': parts, 'collisionGroups': [{'collisionTypes': ['block', 'vehicle', 'entity', 'attack', 'bullet'], 'isInterior': False, 'health': int(100 + mass / 10), 'armorThickness': 3.0,
                                         'collisions': [{'pos': [0, H / 2, z_], 'width': W, 'height': H} for z_ in (hl - 1.0, 0, -hl + 1.0)]}],
     'definitions': [{'subName': '', 'name': name, 'modelName': vid, 'textureName': vid, 'extraMaterialLists': [[]]}], 'rendering': {}})
    manifest.append({'id': vid, 'name': name, 'age': v['age'], 'role': v['role'], 'type': v['type'], 'fuel': fuel, 'mass_kg': mass, 'power_kw': kw, 'tank_l': tank})
w(f'{M}/vehicles/age_gates_vehicles.json', {'_doc': 'Same shape as DiaithirRPG content/tech_ages.json; ids are Immersive Vehicles item ids.', 'items': {f'mts:{PK}.{m["id"]}': m['age'] for m in manifest}})
w(f'{M}/vehicles/vehicle_manifest.json', {'vehicles': manifest})
print('vehicle pack written:', len(manifest), 'vehicles')
