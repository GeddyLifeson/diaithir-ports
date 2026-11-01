#!/usr/bin/env python3
"""Side-view contact sheet of plans: python3 preview.py OUT.png [plan ...]"""
import json, math, sys, glob, os
import numpy as np
from PIL import Image, ImageDraw
D = os.path.join(os.path.dirname(__file__), '..', 'src/main/resources/data/diaithirfauna/plans')
def rot(r):
    rx, ry, rz = r
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    X = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]]); Y = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]); Z = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return Z @ Y @ X
def polys(m):
    out = []
    def walk(n, R, T):
        p = m['parts'][n]
        T2 = T + R @ np.array(p['pivot']); R2 = R @ rot(p['rot'])
        for x, y, z, w, h, d, g in p['boxes']:
            pts = np.array([[a, b, c] for a in (x, x + w) for b in (y, y + h) for c in (z, z + d)])
            pts = (R2 @ pts.T).T + T2
            out.append((pts, n))
        for c in p['children']: walk(c, R2, T2)
    for r in m['roots']: walk(r, np.eye(3), np.zeros(3))
    return out
def draw(m, s, ox, oy, dr):
    for pts, n in polys(m):
        # side view: screen x = -z (front left), y down; hull of projected corners
        P = [(ox + p[2] * s, oy + p[1] * s) for p in pts]
        from itertools import combinations
        xs = sorted(set(P)); 
        # convex hull
        def cross(o, a, b): return (a[0]-o[0])*(b[1]-o[1])-(a[1]-o[1])*(b[0]-o[0])
        lo = []
        for p in xs:
            while len(lo) >= 2 and cross(lo[-2], lo[-1], p) <= 0: lo.pop()
            lo.append(p)
        up = []
        for p in reversed(xs):
            while len(up) >= 2 and cross(up[-2], up[-1], p) <= 0: up.pop()
            up.append(p)
        h = lo[:-1] + up[:-1]
        col = (200, 110, 60) if n.startswith(('leg', 'arm')) else (90, 140, 200) if n.startswith(('head', 'jaw', 'neck')) else (140, 190, 120)
        dr.polygon(h, fill=col, outline=(20, 20, 20))
out, *names = sys.argv[1:]
names = names or sorted(os.path.basename(f)[:-5] for f in glob.glob(D + '/*.json'))
cell = 200; cols = 6; rows = (len(names) + cols - 1) // cols
im = Image.new('RGB', (cols * cell, rows * cell), (235, 235, 235)); dr = ImageDraw.Draw(im)
for i, n in enumerate(names):
    m = json.load(open(f'{D}/{n}.json'))
    allp = np.concatenate([p for p, _ in polys(m)])
    w = allp[:, 2].max() - allp[:, 2].min(); h = allp[:, 1].max() - allp[:, 1].min()
    s = min((cell - 30) / max(w, 1), (cell - 40) / max(h, 1), 6)
    ox = (i % cols) * cell + cell / 2 - (allp[:, 2].max() + allp[:, 2].min()) / 2 * s
    oy = (i // cols) * cell + cell / 2 + 8 - (allp[:, 1].max() + allp[:, 1].min()) / 2 * s
    draw(m, s, ox, oy, dr)
    dr.text(((i % cols) * cell + 4, (i // cols) * cell + 3), f"{n} {m['tw']}x{m['th']}", fill=(0, 0, 0))
im.save(out)
