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

class Obj:
    def __init__(s): s.v, s.vt, s.vn, s.o = [], [], [], []
    def add(s, name, faces, rect):
        """faces: list of quads/tris of (x,y,z); rect = (u0,v0,u1,v1) texture patch."""
        s.o.append((name, faces, rect))
    def write(s, path):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        L, vi, ti, ni = [], 0, 0, 0
        for name, faces, rect in s.o:
            L.append(f'o {name}')
            u0, v0, u1, v1 = rect
            for f in faces:
                a, b, c = f[0], f[1], f[2]
                n = (( (b[1]-a[1])*(c[2]-a[2]) - (b[2]-a[2])*(c[1]-a[1]) ), ( (b[2]-a[2])*(c[0]-a[0]) - (b[0]-a[0])*(c[2]-a[2]) ), ( (b[0]-a[0])*(c[1]-a[1]) - (b[1]-a[1])*(c[0]-a[0]) ))
                ln = math.sqrt(sum(k*k for k in n)) or 1; n = tuple(k/ln for k in n)
                for p in f: L.append('v %.4f %.4f %.4f' % p)
                uv = [(u0, v0), (u1, v0), (u1, v1), (u0, v1)][:len(f)]
                for u, v in uv: L.append('vt %.4f %.4f' % (u, 1 - v))
                L.append('vn %.4f %.4f %.4f' % n)
                ni += 1
                L.append('f ' + ' '.join(f'{vi + i + 1}/{ti + i + 1}/{ni}' for i in range(len(f))))
                vi += len(f); ti += len(f)
        open(path, 'w').write('\n'.join(L) + '\n')

def box(x0, y0, z0, x1, y1, z1):
    p = lambda x, y, z: (x, y, z)
    return [[p(x0,y0,z1),p(x1,y0,z1),p(x1,y1,z1),p(x0,y1,z1)], [p(x1,y0,z0),p(x0,y0,z0),p(x0,y1,z0),p(x1,y1,z0)],
            [p(x0,y1,z1),p(x1,y1,z1),p(x1,y1,z0),p(x0,y1,z0)], [p(x0,y0,z0),p(x1,y0,z0),p(x1,y0,z1),p(x0,y0,z1)],
            [p(x1,y0,z1),p(x1,y0,z0),p(x1,y1,z0),p(x1,y1,z1)], [p(x0,y0,z0),p(x0,y0,z1),p(x0,y1,z1),p(x0,y1,z0)]]

def cyl_z(cx, cy, z0, z1, r, n=10):
    out = []
    for i in range(n):
        a, b = 2*math.pi*i/n, 2*math.pi*(i+1)/n
        p = lambda ang, z: (cx + r*math.cos(ang), cy + r*math.sin(ang), z)
        out.append([p(a, z0), p(b, z0), p(b, z1), p(a, z1)])
    out.append([(cx + r*math.cos(2*math.pi*i/n), cy + r*math.sin(2*math.pi*i/n), z1) for i in range(n)][:4])
    return out

def cyl_x(cy, cz, x0, x1, r, n=10):
    out = []
    for i in range(n):
        a, b = 2*math.pi*i/n, 2*math.pi*(i+1)/n
        p = lambda ang, x: (x, cy + r*math.sin(ang), cz + r*math.cos(ang))
        out.append([p(a, x0), p(b, x0), p(b, x1), p(a, x1)])
    return out

def wedge(x0, x1, y0, y1, z0, z1, ytop_front):
    """Box whose top slopes down toward +z (glacis): top at y1 for z0 and ytop_front at z1."""
    p = lambda x, y, z: (x, y, z)
    return [[p(x0,y0,z1),p(x1,y0,z1),p(x1,ytop_front,z1),p(x0,ytop_front,z1)], [p(x1,y0,z0),p(x0,y0,z0),p(x0,y1,z0),p(x1,y1,z0)],
            [p(x0,ytop_front,z1),p(x1,ytop_front,z1),p(x1,y1,z0),p(x0,y1,z0)], [p(x0,y0,z0),p(x1,y0,z0),p(x1,y0,z1),p(x0,y0,z1)],
            [p(x1,y0,z1),p(x1,y0,z0),p(x1,y1,z0),p(x1,ytop_front,z1)], [p(x0,y0,z0),p(x0,y0,z1),p(x0,ytop_front,z1),p(x0,y1,z0)]]

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
for n in ('tank_track', 'tank_gun_main', 'tank_engine', 'tank_seat'):
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
w(f'{A}/jsondefs/parts/tank_gun_main.json', {
 'general': {'type': 'gun_tank', 'name': 'Tank Gun 120 mm', 'materialLists': [['minecraft:iron_block:0:8'], ['minecraft:gunpowder:0:16']]},
 'gun': {'capacity': 1, 'reloadTime': 80, 'muzzleVelocity': 160, 'fireDelay': 2.0, 'bulletSpreadFactor': 0.0, 'diameter': 120, 'minCaseLength': 100, 'maxCaseLength': 900,
         'minYaw': -180, 'maxYaw': 180, 'minPitch': -8, 'maxPitch': 20, 'yawSpeed': 0.6, 'pitchSpeed': 0.5, 'isSemiAuto': True, 'autoReload': True},
 'definitions': [{'subName': '', 'name': 'Tank Gun 120 mm', 'extraMaterialLists': [[]]}],
 'rendering': {'rotatableModelObjects': []}})
for b, desc, dmg, types, extra in (('ap', 'Armour-Piercing Tank Shell', 55.0, ['armor_piercing'], {'armorPenetration': 80.0}),
                                    ('he', 'High-Explosive Tank Shell', 25.0, ['explosive'], {'blastStrength': 4.0, 'blastDamage': 40.0, 'blastRadiusXZ': 6.0, 'blastRadiusY': 4.0, 'maxDamageRadius': 2.5})):
    bl = {'types': types, 'diameter': 120.0, 'damage': dmg, 'caseLength': 600.0, 'quantity': 4, 'maxVelocity': 160, 'despawnTime': 400, 'impactDespawnTime': 1, 'gravitationalVelocity': 0.0}
    bl.update(extra)
    w(f'{A}/jsondefs/bullets/tank_shell_{b}.json', {'general': {'type': 'bullet', 'name': desc, 'materialLists': [['minecraft:gunpowder:0:2', 'minecraft:iron_ingot:0:3']]}, 'bullet': bl,
        'definitions': [{'subName': '', 'name': desc, 'extraMaterialLists': [[]]}]})

PARTS = [
 {'pos': [0, 0.9, -2.2], 'types': ['engine_tank'], 'isPermanent': False, 'defaultPart': f'{PK}:tank_engine'},
 {'pos': [-1.35, 0.0, 0.0], 'types': ['ground_tread'], 'defaultPart': f'{PK}:tank_track'},
 {'pos': [1.35, 0.0, 0.0], 'types': ['ground_tread'], 'defaultPart': f'{PK}:tank_track'},
 {'pos': [0.0, 0.95, 1.9], 'types': ['seat'], 'isController': True, 'defaultPart': f'{PK}:tank_seat', 'dismountPos': [0.0, 1.2, 3.5]},
 {'pos': [0.7, 1.2, -0.4], 'types': ['seat'], 'defaultPart': f'{PK}:tank_seat'},
 {'pos': [0.0, 1.5, -0.3], 'types': ['gun_tank'], 'defaultPart': f'{PK}:tank_gun_main', 'minYaw': -180, 'maxYaw': 180, 'minPitch': -8, 'maxPitch': 20,
  'isExterior': False},
]
w(f'{A}/jsondefs/vehicles/tank_mk1.json', {
 'general': {'type': 'vehicle', 'name': 'Main Battle Tank Mk I', 'description': 'Original heavy tracked armour for Age VII. Driver in the hull, commander and gunner under the turret.',
             'materialLists': MAT_VEH, 'health': 1200},
 'motorized': {'emptyMass': 42000, 'fuelCapacity': 1500, 'defaultFuelQty': 600, 'hasSkidSteer': True, 'hasPermanentSkidSteer': True, 'hasRunningLights': True, 'hasHeadlights': True,
               'axleRatio': 3.6, 'brakingFactor': 1.2, 'dragCoefficient': 0.9, 'crossSectionalArea': 8.0, 'steeringForceFactor': 1.0, 'overSteer': 0.0, 'underSteer': 0.0,
               'crashSpeedMin': 0.5, 'crashSpeedMax': 1.0, 'crashSpeedDestroyed': 2.0, 'isBigTruck': False},
 'car': {'isFrontWheelDrive': True, 'isRearWheelDrive': False, 'axleRatio': 3.6, 'dragCoefficient': 0.9},
 'parts': PARTS,
 'collision': [{'pos': [0, 1.1, 0.0], 'width': 2.6, 'height': 1.2}, {'pos': [0, 1.0, 2.4], 'width': 2.6, 'height': 1.0}, {'pos': [0, 1.0, -2.4], 'width': 2.6, 'height': 1.0},
               {'pos': [-1.6, 0.7, 0.0], 'width': 1.0, 'height': 1.2}, {'pos': [1.6, 0.7, 0.0], 'width': 1.0, 'height': 1.2}],
 'definitions': [{'subName': s, 'name': 'Main Battle Tank Mk I' + n, 'extraMaterialLists': [[]]} for s, n in (('', ' (Forest)'), ('_desert', ' (Desert)'), ('_snow', ' (Winter)'))],
 'rendering': {}})
print('tank pack written', A)
