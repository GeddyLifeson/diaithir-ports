#!/usr/bin/env python3
"""Software preview of the tank pack models (vehicle hull + tracks + turret) -> PNG. Usage: preview_tank.py <module dir> <out.png>"""
import math, sys
from PIL import Image, ImageDraw
M, OUT = sys.argv[1], sys.argv[2]
D = sys.argv[3] if len(sys.argv) > 3 else '1'
SUF = '' if D == '1' else '2'
A = f'{M}/src/main/resources/assets/diaithirtanks'
def load(path):
    V, VT, objs, cur = [], [], {}, None
    for l in open(path):
        p = l.split()
        if not p: continue
        if p[0] == 'o': cur = objs.setdefault(p[1], [])
        elif p[0] == 'v': V.append(tuple(map(float, p[1:4])))
        elif p[0] == 'vt': VT.append(tuple(map(float, p[1:3])))
        elif p[0] == 'f': cur.append([(V[int(x.split('/')[0]) - 1], VT[int(x.split('/')[1]) - 1]) for x in p[1:]])
    return objs
tex = Image.open(f'{A}/textures/vehicles/tank_mk{D}.png').convert('RGB')
def color(uvs):
    u = sum(a[1][0] for a in uvs) / len(uvs); v = sum(a[1][1] for a in uvs) / len(uvs)
    return tex.getpixel((min(127, int(u * 128)), min(127, int((1 - v) * 128))))
def draw(models, yaw, pitch, size, scale):
    im = Image.new('RGB', (size, size), (186, 206, 226)); d = ImageDraw.Draw(im); polys = []
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)
    for off, objs in models:
        for faces in objs.values():
            for f in faces:
                pts = []
                for (x, y, z), _ in f:
                    x += off[0]; y += off[1]; z += off[2]
                    x, z = x * cy - z * sy, x * sy + z * cy
                    y, z = y * cp + z * sp, -y * sp + z * cp
                    pts.append((size / 2 + x * scale, size * 0.62 - y * scale, z))
                if len(f) >= 3:
                    a, b, c = pts[0], pts[1], pts[2]
                    n = (b[0]-a[0])*(c[1]-a[1]) - (b[1]-a[1])*(c[0]-a[0])
                    shade = 0.62 + 0.38 * min(1, abs(n) / 900)
                    base = color(f)
                    polys.append((sum(p[2] for p in pts) / len(pts), pts, tuple(int(k * shade) for k in base)))
    for _, pts, col in sorted(polys, key=lambda t: -t[0]):
        d.polygon([(p[0], p[1]) for p in pts], fill=col, outline=(20, 20, 20))
    return im
hull, track, turret = load(f'{A}/objmodels/vehicles/tank_mk{D}.obj'), load(f'{A}/objmodels/parts/tank_track{SUF}.obj'), load(f'{A}/objmodels/parts/tank_gun_main{SUF}.obj')
mods = [((0, 0, 0), hull), ((-1.35 + 0.1 * (D == '2'), 0, 0), track), ((1.35 - 0.1 * (D == '2'), 0, 0), track), ((0, 1.5 if D == '1' else 1.25, -0.3), turret)]
views = [(math.radians(35), math.radians(20)), (math.radians(90), math.radians(8)), (math.radians(180), math.radians(25))]
out = Image.new('RGB', (1500, 500))
for i, (y, p) in enumerate(views): out.paste(draw(mods, y, p, 500, 52), (i * 500, 0))
out.save(OUT)
