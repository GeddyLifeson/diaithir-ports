"""TEMPORARY bisect aid for the cockpit HUD (not a feature): adds nine text-only instruments (letters A-I) at a grid of hudX/hudY to tank_mk1,
so a screenshot shows which hudY sign/range lands inside the HUD. Remove with `hud_debug.py <module> remove`."""
import json, os, sys
root = os.path.join(sys.argv[1], 'src/main/resources/assets/diaithirtanks')
remove = len(sys.argv) > 2 and sys.argv[2] == 'remove'
grid = {'A': (60, 40), 'B': (200, 40), 'C': (340, 40), 'D': (60, -40), 'E': (200, -40), 'F': (340, -40), 'G': (60, 100), 'H': (200, 100), 'I': (340, 100)}
vp = os.path.join(root, 'jsondefs/vehicles/tank_mk1.json'); v = json.load(open(vp))
v['instruments'] = [i for i in v['instruments'] if not i['defaultInstrument'].startswith('diaithirtanks:dbg')]
for k, (x, y) in grid.items():
    ip = os.path.join(root, f'jsondefs/instruments/dbg{k}.json')
    if remove:
        if os.path.exists(ip): os.remove(ip)
        continue
    json.dump({'general': {'name': f'Debug {k}', 'materialLists': [['minecraft:iron_ingot:0:1']]}, 'textureName': 'instruments.png',
               'components': [{'xCenter': 0, 'yCenter': 0, 'scale': 1.0, 'textObject': {'pos': [0, 0, 0], 'rot': [0, 0, 0], 'scale': 3.0, 'defaultText': f'{k} {x},{y}', 'maxLength': 12, 'color': '#FFFF00', 'renderPosition': 1}}]}, open(ip, 'w'), indent=1)
    v['instruments'].append({'pos': [0, 1.0, 0.8], 'rot': [0, 0, 0], 'scale': 0.1, 'hudX': x, 'hudY': y, 'hudScale': 1.0, 'defaultInstrument': f'diaithirtanks:dbg{k}'})
json.dump(v, open(vp, 'w'), indent=1)
