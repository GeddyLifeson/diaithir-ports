#!/usr/bin/env python3
"""Generates the Diaithir armour pack for Immersive Vehicles (pack id diaithirtanks): OBJ models, textures and JSON defs, all original
(boxes, prisms and cylinders built here; flat camouflage painted with PIL). Ships inside the diaithir-arms jar (any jar that holds
assets/<id>/packdefinition.json is a pack). Usage: python3 -I tools/gen_tank.py <module dir>"""
import json, math, os, random, sys
from PIL import Image, ImageDraw

M = sys.argv[1] if len(sys.argv) > 1 else '.'
PK = 'diaithirtanks'
A = f'{M}/src/main/resources/assets/{PK}'
random.seed(26)

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from ivgeom import Obj, box, cyl_z, cyl_x, wedge

# texture atlas 128x128: camo (0..0.5,0..0.5), track (0.5..0.75,0..0.5), metal (0.75..1,0..0.5), dark (0..0.25,0.5..1), glass (0.25..0.5,0.5..1)
R = {'camo': (0, 0, .5, .5), 'track': (.5, 0, .75, .5), 'metal': (.75, 0, 1, .5), 'dark': (0, .5, .25, 1), 'glass': (.25, .5, .5, 1)}

def atlas(base, spots, path):
    im = Image.new('RGB', (128, 128), (20, 20, 20)); px = im.load(); d = ImageDraw.Draw(im)
    for x in range(64):
        for y in range(64):
            v = random.randint(-9, 9); px[x, y] = tuple(max(0, min(255, c + v)) for c in base)
    for _ in range(22):
        c = random.choice(spots); x, y = random.randint(0, 60), random.randint(0, 60); w, h = random.randint(8, 26), random.randint(6, 18)
        d.ellipse([x, y, x + w, y + h], fill=c)
    for x in range(64, 96):
        for y in range(64):
            v = random.randint(-8, 8); px[x, y] = tuple(max(0, c + v) for c in (46, 44, 42))
        if x % 4 == 0: d.line([x, 0, x, 63], fill=(30, 28, 26))
    for x in range(96, 128):
        for y in range(64):
            v = random.randint(-10, 10); px[x, y] = tuple(max(0, min(255, c + v)) for c in (104, 108, 114))
    d.rectangle([0, 64, 31, 127], fill=(22, 22, 24)); d.rectangle([32, 64, 63, 127], fill=(70, 96, 110))
    os.makedirs(os.path.dirname(path), exist_ok=True); im.save(path)

# ---------------- hull ----------------
def hull_model():
    o = Obj()
    o.add('hull', box(-1.25, 0.55, -3.45, 1.25, 1.35, 2.6) + wedge(-1.25, 1.25, 0.55, 1.35, 2.6, 3.7, 0.85), R['camo'])
    o.add('deck', box(-1.0, 1.35, -3.3, 1.0, 1.5, 1.6), R['camo'])
    o.add('skirt_l', box(-1.95, 0.5, -3.2, -1.6, 1.25, 3.0), R['metal'])
    o.add('skirt_r', box(1.6, 0.5, -3.2, 1.95, 1.25, 3.0), R['metal'])
    o.add('exhaust', box(-0.7, 1.5, -3.4, 0.7, 1.7, -2.4), R['dark'])
    o.add('hatch_driver', box(-0.35, 1.0, 2.0, 0.35, 1.2, 2.7), R['dark'])
    o.add('lamp_l', box(-1.0, 0.95, 3.55, -0.7, 1.15, 3.7), R['glass'])
    o.add('lamp_r', box(0.7, 0.95, 3.55, 1.0, 1.15, 3.7), R['glass'])
    return o

def track_model():
    o = Obj()
    o.add('track', box(-0.4, 0.05, -3.55, 0.4, 0.75, 3.55), R['track'])
    for i in range(7):
        z = -3.0 + i * 1.0
        o.add(f'wheel{i}', cyl_x(0.45, z, -0.45, 0.45, 0.35, 8), R['metal'])
    o.add('sprocket', cyl_x(0.5, 3.15, -0.45, 0.45, 0.4, 8), R['dark'])
    o.add('idler', cyl_x(0.5, -3.3, -0.45, 0.45, 0.38, 8), R['dark'])
    o.add('fender', box(-0.45, 0.78, -3.6, 0.45, 0.86, 3.6), R['camo'])
    return o

def turret_model():
    o = Obj()
    # origin = turret ring centre
    o.add('turret', box(-1.15, 0.0, -1.55, 1.15, 0.8, 1.2) + wedge(-1.15, 1.15, 0.0, 0.8, 1.2, 2.0, 0.45), R['camo'])
    o.add('bustle', box(-0.95, 0.05, -2.35, 0.95, 0.75, -1.55), R['camo'])
    o.add('mantlet', box(-0.4, 0.15, 1.95, 0.4, 0.7, 2.25), R['metal'])
    o.add('barrel', cyl_z(0.0, 0.42, 2.2, 7.2, 0.115, 10), R['dark'])
    o.add('fume_extractor', cyl_z(0.0, 0.42, 4.6, 5.3, 0.17, 10), R['metal'])
    o.add('hatch_cdr', box(0.35, 0.8, -0.5, 0.95, 0.95, 0.1), R['dark'])
    o.add('hatch_gnr', box(-0.95, 0.8, -0.2, -0.45, 0.92, 0.3), R['dark'])
    o.add('mg_mount', box(0.45, 0.95, -0.4, 0.55, 1.15, 0.6), R['dark'])
    o.add('sight', box(-0.55, 0.8, 1.0, -0.2, 1.0, 1.4), R['glass'])
    return o

def hull2_model():
    o = Obj()
    o.add('hull', box(-1.15, 0.5, -3.1, 1.15, 1.2, 2.3) + wedge(-1.15, 1.15, 0.5, 1.2, 2.3, 3.3, 0.7), R['camo'])
    o.add('skirt_l', box(-1.75, 0.45, -2.9, -1.5, 1.0, 2.7), R['metal'])
    o.add('skirt_r', box(1.5, 0.45, -2.9, 1.75, 1.0, 2.7), R['metal'])
    o.add('fuel_l', cyl_z(-1.0, 1.35, -3.0, -1.4, 0.18, 8), R['dark'])
    o.add('fuel_r', cyl_z(1.0, 1.35, -3.0, -1.4, 0.18, 8), R['dark'])
    o.add('log', cyl_z(0.0, 1.3, -3.2, -1.0, 0.14, 8), R['dark'])
    o.add('hatch_driver', box(-0.3, 0.9, 1.9, 0.3, 1.1, 2.5), R['dark'])
    o.add('lamp_l', box(-0.9, 0.85, 3.2, -0.65, 1.0, 3.3), R['glass'])
    o.add('lamp_r', box(0.65, 0.85, 3.2, 0.9, 1.0, 3.3), R['glass'])
    return o

def track2_model():
    o = Obj()
    o.add('track', box(-0.35, 0.05, -3.2, 0.35, 0.7, 3.2), R['track'])
    for i in range(6):
        o.add(f'wheel{i}', cyl_x(0.42, -2.5 + i * 0.95, -0.4, 0.4, 0.34, 8), R['metal'])
    o.add('sprocket', cyl_x(0.5, 2.9, -0.4, 0.4, 0.36, 8), R['dark'])
    o.add('idler', cyl_x(0.5, -3.0, -0.4, 0.4, 0.34, 8), R['dark'])
    return o

def turret2_model():
    o = Obj()
    # low cast dome: stacked octagon-ish prisms
    o.add('turret', box(-0.95, 0.0, -0.9, 0.95, 0.5, 1.0) + box(-0.75, 0.5, -0.7, 0.75, 0.75, 0.8) + wedge(-0.95, 0.95, 0.0, 0.5, 1.0, 1.5, 0.3), R['camo'])
    o.add('mantlet', box(-0.3, 0.1, 1.45, 0.3, 0.55, 1.7), R['metal'])
    o.add('barrel', cyl_z(0.0, 0.32, 1.7, 6.4, 0.125, 10), R['dark'])
    o.add('fume_extractor', cyl_z(0.0, 0.32, 3.9, 4.6, 0.16, 10), R['metal'])
    o.add('hatch_cdr', box(0.2, 0.75, -0.3, 0.7, 0.88, 0.2), R['dark'])
    o.add('mg_mount', box(0.3, 0.88, -0.1, 0.4, 1.05, 0.5), R['dark'])
    o.add('sight', box(-0.5, 0.75, 0.5, -0.2, 0.92, 0.9), R['glass'])
    o.add('smoke_l', cyl_z(-0.85, 0.35, 0.4, 0.8, 0.08, 6), R['dark'])
    o.add('smoke_r', cyl_z(0.85, 0.35, 0.4, 0.8, 0.08, 6), R['dark'])
    return o

def engine_model():
    o = Obj(); o.add('engine', box(-0.5, 0.0, -0.5, 0.5, 0.6, 0.5), R['metal']); return o
def seat_model():
    o = Obj(); o.add('seat', box(-0.25, 0.0, -0.25, 0.25, 0.45, 0.25) + box(-0.25, 0.45, -0.28, 0.25, 1.0, -0.2), R['dark']); return o
def shell_model():
    o = Obj(); o.add('shell', cyl_z(0.0, 0.0, 0.0, 0.5, 0.06, 8), R['metal']); return o

for sub, kind in (('vehicles', 'tank_mk1'),):
    hull_model().write(f'{A}/objmodels/vehicles/tank_mk1.obj')
track_model().write(f'{A}/objmodels/parts/tank_track.obj')
turret_model().write(f'{A}/objmodels/parts/tank_gun_main.obj')
engine_model().write(f'{A}/objmodels/parts/tank_engine.obj')
seat_model().write(f'{A}/objmodels/parts/tank_seat.obj')
shell_model().write(f'{A}/objmodels/bullets/tank_shell_ap.obj')
shell_model().write(f'{A}/objmodels/bullets/tank_shell_he.obj')
camos = {'': ((74, 84, 52), [(56, 66, 40), (96, 88, 56), (46, 52, 38)]), '_desert': ((158, 134, 92), [(130, 108, 72), (176, 154, 112), (110, 92, 64)]),
         '_snow': ((222, 226, 230), [(188, 196, 204), (240, 242, 245), (150, 160, 150)])}
for suf, (base, spots) in camos.items():
    atlas(base, spots, f'{A}/textures/vehicles/tank_mk1{suf}.png')
    atlas(base, spots, f'{A}/textures/parts/tank_track{suf}.png') if False else None
hull2_model().write(f'{A}/objmodels/vehicles/tank_mk2.obj')
track2_model().write(f'{A}/objmodels/parts/tank_track2.obj')
turret2_model().write(f'{A}/objmodels/parts/tank_gun_main2.obj')
for suf, (base, spots) in camos.items():
    atlas(base, spots, f'{A}/textures/vehicles/tank_mk2{suf}.png')
for n in ('tank_track', 'tank_gun_main', 'tank_engine', 'tank_seat', 'tank_track2', 'tank_gun_main2'):
    atlas(camos[''][0], camos[''][1], f'{A}/textures/parts/{n}.png')
for n in ('tank_shell_ap', 'tank_shell_he'):
    atlas((120, 110, 70), [(150, 140, 90)], f'{A}/textures/bullets/{n}.png')

def w(path, d): os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=1))
w(f'{A}/packdefinition.json', {'packID': PK, 'packName': 'Diaithir Armour', 'packItem': 'tank_mk1', 'fileStructure': 0})

MAT_VEH = [['minecraft:iron_ingot:0:96'], ['minecraft:iron_block:0:12'], ['minecraft:redstone:0:16'], ['minecraft:gunpowder:0:8']]
w(f'{A}/jsondefs/parts/tank_track.json', {
 'general': {'type': 'ground_tread', 'name': 'Tank Track', 'materialLists': [['minecraft:iron_ingot:0:24']]},
 'ground': {'isTread': True, 'width': 0.8, 'height': 0.9, 'flatHeight': 0.9, 'climbHeight': 0.9, 'motiveFriction': 1.0, 'lateralFriction': 1.4, 'extraCollisionBoxOffset': 0.0, 'spacing': 7.1, 'canGoFlat': False},
 'definitions': [{'subName': '', 'name': 'Tank Track', 'extraMaterialLists': [[]]}]})
w(f'{A}/jsondefs/parts/tank_engine.json', {
 'general': {'type': 'engine_tank', 'name': 'Tank Diesel Engine', 'materialLists': [['minecraft:iron_block:0:6'], ['minecraft:piston:0:4']]},
 'engine': {'type': 'NORMAL', 'isAutomatic': True, 'fuelType': 'diesel', 'fuelConsumption': 1.4, 'maxRPM': 2800, 'maxSafeRPM': 2600, 'idleRPM': 600, 'startRPM': 500, 'stallRPM': 350, 'revlimitRPM': 3000,
            'starterPower': 6, 'gearRatios': [-4.0, 0.0, 4.5, 2.8, 1.8], 'upShiftRPM': [2200, 2200, 2200], 'downShiftRPM': [1000, 1000, 1000], 'shiftSpeed': 10, 'clutchTime': 12, 'revResistance': 8,
            'heatingCoefficient': 0.4, 'coolingCoefficient': 0.6, 'engineWearFactor': 0.3},
 'definitions': [{'subName': '', 'name': 'Tank Diesel Engine', 'extraMaterialLists': [[]]}]})
w(f'{A}/jsondefs/parts/tank_seat.json', {
 'general': {'type': 'seat', 'name': 'Tank Crew Seat', 'materialLists': [['minecraft:iron_ingot:0:4']]},
 'definitions': [{'subName': '', 'name': 'Tank Crew Seat', 'extraMaterialLists': [[]]}]})
GUNS = {  # id: (name, bore mm, reload ticks, velocity, model, yaw speed, pitch speed)
 'tank_gun_105': ('Tank Gun 105 mm', 105, 90, 150, 'tank_gun_main', 0.55, 0.45),
 'tank_gun_120': ('Tank Gun 120 mm', 120, 80, 160, 'tank_gun_main', 0.6, 0.5),
 'tank_gun_125': ('Tank Gun 125 mm (autoloader)', 125, 60, 165, 'tank_gun_main2', 0.5, 0.45),
}
for gid, (nm, bore, rl, vel, model, ys, ps) in GUNS.items():
    w(f'{A}/jsondefs/parts/{gid}.json', {
     'general': {'type': 'gun_tank', 'name': nm, 'materialLists': [['minecraft:iron_block:0:8'], ['minecraft:gunpowder:0:16']]},
     'gun': {'capacity': 1, 'reloadTime': rl, 'muzzleVelocity': vel, 'fireDelay': 2.0, 'bulletSpreadFactor': 0.0, 'diameter': bore, 'minCaseLength': 100, 'maxCaseLength': 900,
             'minYaw': -180, 'maxYaw': 180, 'minPitch': -8, 'maxPitch': 20, 'yawSpeed': ys, 'pitchSpeed': ps, 'isSemiAuto': True, 'autoReload': True},
     'definitions': [{'subName': '', 'name': nm, 'modelName': model, 'textureName': model, 'extraMaterialLists': [[]]}]})
for b, desc, dmg, types, extra in (('ap', 'Armour-Piercing Tank Shell', 55.0, ['armor_piercing'], {'armorPenetration': 80.0}),
                                    ('he', 'High-Explosive Tank Shell', 25.0, ['explosive'], {'blastStrength': 4.0, 'blastDamage': 40.0, 'blastRadiusXZ': 6.0, 'blastRadiusY': 4.0, 'maxDamageRadius': 2.5})):
    bl = {'types': types, 'diameter': 120.0, 'damage': dmg, 'caseLength': 600.0, 'quantity': 4, 'maxVelocity': 160, 'despawnTime': 400, 'impactDespawnTime': 1, 'gravitationalVelocity': 0.0}
    bl.update(extra)
    w(f'{A}/jsondefs/bullets/tank_shell_{b}.json', {'general': {'type': 'bullet', 'name': desc, 'materialLists': [['minecraft:gunpowder:0:2', 'minecraft:iron_ingot:0:3']]}, 'bullet': bl,
        'definitions': [{'subName': '', 'name': desc, 'extraMaterialLists': [[]]}]})

def parts(design, gun, track):
    seat_z = 1.9 if design == 1 else 1.7
    return [
     {'pos': [0, 0.9, -2.2], 'types': ['engine_tank'], 'minValue': 0.0, 'maxValue': 10.0, 'defaultPart': f'{PK}:tank_engine'},
     {'pos': [-1.35 if design == 1 else -1.25, 0.0, 0.0], 'types': ['ground_tread'], 'defaultPart': f'{PK}:{track}'},
     {'pos': [1.35 if design == 1 else 1.25, 0.0, 0.0], 'types': ['ground_tread'], 'defaultPart': f'{PK}:{track}'},
     {'pos': [0.0, 0.95, seat_z], 'types': ['seat'], 'isController': True, 'defaultPart': f'{PK}:tank_seat', 'dismountPos': [0.0, 1.2, 3.5]},
     {'pos': [0.6, 1.2, -0.4], 'types': ['seat'], 'defaultPart': f'{PK}:tank_seat'},
     {'pos': [0.0, 1.5 if design == 1 else 1.25, -0.3], 'types': ['gun_tank'], 'defaultPart': f'{PK}:{gun}', 'minYaw': -180, 'maxYaw': 180, 'minPitch': -8, 'maxPitch': 20},
    ]
# id, design, display, gun, health, mass, Age, description
VEHICLES = [
 ('tank_mk1', 1, 'Main Battle Tank Mk I', 'tank_gun_105', 1000, 38000, 'VII', 'Early heavy tracked armour with a 105 mm gun.'),
 ('tank_mk1a', 1, 'Main Battle Tank Mk IA', 'tank_gun_120', 1200, 42000, 'VII', 'Mk I with the 120 mm gun and a thicker front.'),
 ('tank_mk1b', 1, 'Main Battle Tank Mk IB', 'tank_gun_120', 1500, 46000, 'RODOS', 'Mk IA with composite side skirts and a stronger engine.'),
 ('tank_mk2', 2, 'Main Battle Tank Mk II', 'tank_gun_125', 800, 36000, 'VII', 'Low, domed three-crew tank with an autoloaded 125 mm gun.'),
 ('tank_mk2a', 2, 'Main Battle Tank Mk IIA', 'tank_gun_125', 1000, 40000, 'VII', 'Mk II with added turret armour and smoke launchers.'),
 ('tank_mk2b', 2, 'Main Battle Tank Mk IIB', 'tank_gun_125', 1300, 44000, 'RODOS', 'Mk IIA with composite armour and a stronger engine.'),
]
CAMO_N = (('', ' (Forest)'), ('_desert', ' (Desert)'), ('_snow', ' (Winter)'))
for vid, design, nm, gun, hp, mass, age, desc in VEHICLES:
    mdl = 'tank_mk1' if design == 1 else 'tank_mk2'
    mat = [['minecraft:iron_ingot:0:%d' % (hp // 12)], ['minecraft:iron_block:0:%d' % (hp // 100)], ['minecraft:redstone:0:16'], ['minecraft:gunpowder:0:8']]
    w(f'{A}/jsondefs/vehicles/{vid}.json', {
     'general': {'type': 'vehicle', 'name': nm, 'description': desc, 'materialLists': mat, 'health': hp},
     'motorized': {'emptyMass': mass, 'fuelCapacity': 1500, 'defaultFuelQty': 600, 'hasSkidSteer': True, 'hasPermanentSkidSteer': True, 'hasRunningLights': True, 'hasHeadlights': True,
                   'axleRatio': 3.6, 'brakingFactor': 1.2, 'dragCoefficient': 0.9, 'crossSectionalArea': 8.0, 'steeringForceFactor': 1.0, 'overSteer': 0.0, 'underSteer': 0.0,
                   'crashSpeedMin': 0.5, 'crashSpeedMax': 1.0, 'crashSpeedDestroyed': 2.0, 'isBigTruck': False},
     'car': {'isFrontWheelDrive': True, 'isRearWheelDrive': False, 'axleRatio': 3.6, 'dragCoefficient': 0.9},
     'parts': parts(design, gun, 'tank_track' if design == 1 else 'tank_track2'),
     'collision': [{'pos': [0, 1.1, 0.0], 'width': 2.6, 'height': 1.2}, {'pos': [0, 1.0, 2.4], 'width': 2.6, 'height': 1.0}, {'pos': [0, 1.0, -2.4], 'width': 2.6, 'height': 1.0},
                   {'pos': [-1.6, 0.7, 0.0], 'width': 1.0, 'height': 1.2}, {'pos': [1.6, 0.7, 0.0], 'width': 1.0, 'height': 1.2}],
     'definitions': [{'subName': sfx, 'name': nm + lab, 'modelName': mdl, 'textureName': mdl + sfx, 'extraMaterialLists': [[]]} for sfx, lab in CAMO_N],
     'rendering': {}})
w(f'{A}/jsondefs/parts/tank_track2.json', {
 'general': {'type': 'ground_tread', 'name': 'Tank Track (narrow)', 'materialLists': [['minecraft:iron_ingot:0:20']]},
 'ground': {'isTread': True, 'width': 0.7, 'height': 0.85, 'flatHeight': 0.85, 'climbHeight': 0.85, 'motiveFriction': 1.0, 'lateralFriction': 1.4, 'extraCollisionBoxOffset': 0.0, 'spacing': 6.4, 'canGoFlat': False},
 'definitions': [{'subName': '', 'name': 'Tank Track (narrow)', 'extraMaterialLists': [[]]}]})
import pathlib
pathlib.Path(f'{A}/jsondefs/parts/tank_gun_main.json').unlink(missing_ok=True)
w(f'{M}/tank_manifest.json', {'vehicles': [{'id': v[0], 'name': v[2], 'age': v[6], 'desc': v[7], 'camo': [c[0] for c in CAMO_N]} for v in VEHICLES],
                              'parts': ['tank_track', 'tank_track2', 'tank_engine', 'tank_seat'] + list(GUNS), 'bullets': ['tank_shell_ap', 'tank_shell_he']})

# ---- inventory icons: textures/items/<systemName><subName>.png (IV looks them up by item resource name) ----
CAMO_RGB = {'': (74, 92, 58), '_desert': (176, 150, 100), '_snow': (214, 220, 226)}
KIND_DIR = {'tank1': 'vehicles', 'tank2': 'vehicles', 'track': 'parts', 'engine': 'parts', 'seat': 'parts', 'gun': 'parts', 'shell': 'bullets'}
def icon(path, body, kind):
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    dark = tuple(max(0, c - 40) for c in body); track = (38, 38, 40)
    if kind == 'tank1' or kind == 'tank2':
        d.rounded_rectangle([3, 19, 28, 26], 3, fill=track); d.rectangle([5, 14, 26, 21], fill=body, outline=dark)
        if kind == 'tank1': d.rectangle([10, 8, 21, 14], fill=body, outline=dark)
        else: d.ellipse([10, 6, 21, 15], fill=body, outline=dark)
        d.rectangle([20, 10, 30, 12], fill=dark)
        for x in range(6, 27, 5): d.ellipse([x, 22, x + 3, 25], fill=(90, 90, 94))
    elif kind == 'track':
        d.rounded_rectangle([3, 12, 28, 22], 4, fill=track, outline=(20, 20, 22))
        for x in range(6, 27, 5): d.ellipse([x, 15, x + 3, 19], fill=(110, 110, 116))
    elif kind == 'engine':
        d.rectangle([6, 10, 25, 24], fill=(104, 108, 114), outline=(50, 52, 56)); d.rectangle([9, 6, 22, 10], fill=(80, 84, 90))
        for x in (10, 15, 20): d.rectangle([x, 12, x + 2, 21], fill=(60, 62, 66))
    elif kind == 'seat':
        d.rectangle([9, 5, 22, 17], fill=(88, 64, 48), outline=(50, 36, 28)); d.rectangle([7, 17, 24, 24], fill=(70, 52, 40), outline=(40, 30, 24))
    elif kind == 'gun':
        d.rectangle([2, 13, 24, 17], fill=(70, 74, 80), outline=(36, 38, 42)); d.rectangle([22, 10, 29, 21], fill=(90, 94, 100), outline=(36, 38, 42))
    elif kind == 'shell':
        d.rectangle([12, 10, 19, 25], fill=(190, 150, 60), outline=(100, 76, 30)); d.polygon([(12, 10), (19, 10), (15, 3)], fill=body, outline=dark)
    os.makedirs(os.path.dirname(path), exist_ok=True); im.save(path)
    base = os.path.basename(path)[:-4]
    folder = KIND_DIR[kind]
    w(f'{A}/models/item/{folder}/{base}.json', {'parent': 'mts:item/basic', 'textures': {'layer0': f'{PK}:item/{base}'}})
for vid, design, *_ in VEHICLES:
    for sfx, _l in CAMO_N: icon(f'{A}/textures/item/{vid}{sfx}.png', CAMO_RGB[sfx], 'tank%d' % design)
for n in ('tank_track', 'tank_track2'): icon(f'{A}/textures/item/{n}.png', (0, 0, 0), 'track')
icon(f'{A}/textures/item/tank_engine.png', (0, 0, 0), 'engine'); icon(f'{A}/textures/item/tank_seat.png', (0, 0, 0), 'seat')
for g in GUNS: icon(f'{A}/textures/item/{g}.png', (0, 0, 0), 'gun')
icon(f'{A}/textures/item/tank_shell_ap.png', (110, 110, 118), 'shell'); icon(f'{A}/textures/item/tank_shell_he.png', (170, 60, 40), 'shell')
print('tank pack written', A)
