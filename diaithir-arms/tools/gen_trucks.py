#!/usr/bin/env python3
"""Generates the Diaithir road-haulage and construction pack for Immersive Vehicles (pack id diaithirtrucks): semi tractors, semi-trailers and
construction machines from tools/truck_roster.py, plus engine tiers, wheels, cargo parts, yard JSON for the assembly API, workshop part blueprints,
names, Age gates, library and Codex data. All original geometry (boxes and cylinders) and textures. Ships inside the diaithir-arms jar.
Usage: python3 -I tools/gen_trucks.py <module dir>"""
import json, math, os, random, sys
from PIL import Image, ImageDraw
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from ivgeom import Obj, box, cyl_z, cyl_x
import truck_roster as ros

M = sys.argv[1] if len(sys.argv) > 1 else '.'
PK = 'diaithirtrucks'
A = f'{M}/src/main/resources/assets/{PK}'
D = f'{M}/src/main/resources/data/{PK}'
random.seed(27)
R = {'paint': (0, 0, .5, .5), 'steel': (.5, 0, .75, .5), 'chrome': (.75, 0, 1, .5), 'dark': (0, .5, .25, 1), 'glass': (.25, .5, .5, 1)}
PAINTS = {'': ('Red', (150, 36, 32)), '_white': ('White', (226, 228, 230)), '_blue': ('Blue', (36, 70, 140))}
CPAINTS = {'': ('Yellow', (230, 170, 20)), '_orange': ('Orange', (214, 98, 24)), '_white': ('White', (226, 228, 230))}

def w(path, d):
    os.makedirs(os.path.dirname(path), exist_ok=True); open(path, 'w').write(json.dumps(d, indent=1, ensure_ascii=False))

def atlas(base, path):
    im = Image.new('RGB', (128, 128), (20, 20, 20)); px = im.load(); d = ImageDraw.Draw(im)
    for x in range(64):
        for y in range(64):
            v = random.randint(-7, 7); px[x, y] = tuple(max(0, min(255, c + v)) for c in base)
    for _ in range(14):
        x, y = random.randint(0, 60), random.randint(0, 60); d.line([x, y, x + random.randint(2, 14), y], fill=tuple(max(0, c - 14) for c in base))
    for x in range(64, 96):
        for y in range(64):
            v = random.randint(-8, 8); px[x, y] = tuple(max(0, c + v) for c in (84, 88, 92))
    for x in range(96, 128):
        for y in range(64):
            v = random.randint(-10, 10); px[x, y] = tuple(max(0, min(255, c + v)) for c in (170, 174, 180))
    d.rectangle([0, 64, 31, 127], fill=(24, 24, 26)); d.rectangle([32, 64, 63, 127], fill=(70, 100, 116))
    os.makedirs(os.path.dirname(path), exist_ok=True); im.save(path)

AGE = lambda y: 'V' if y < 1861 else 'VI' if y <= 1945 else 'VII'
age_of = lambda vid, y: ros.AGE_OVERRIDE.get(vid) or AGE(y)

# ---------------- models ----------------
def wheel_model(r, wd):
    o = Obj(); o.add('tyre', cyl_x(0, 0, -wd / 2, wd / 2, r, 12), R['dark']); o.add('hub', cyl_x(0, 0, -wd / 2 - 0.02, wd / 2 + 0.02, r * 0.45, 8), R['chrome']); return o

def axle_zs(front, n, gap=1.35, rear=None):
    return [front] + [(rear if rear is not None else front - 4.0) - i * gap for i in range(n - 1)]

def tractor(layout, axles, kw, year):
    """Returns (Obj, info). z+ is forward, y up, ground at y=0, wheel radius 0.5."""
    o = Obj(); tri = layout == 'tri'
    if layout in ('team', 'steam'): return primitive_tractor(layout, axles, kw)
    wb = 2.2 if tri else (3.8 if layout == 'cab' else 4.4)
    zf = wb * 0.62; zr = zf - wb
    rear = [zr - i * 1.3 for i in range(max(1, axles - 1))]
    big = 1.0 + min(0.3, kw / 1500)
    cw = 0.9 if tri else 1.2
    o.add('frame', box(-0.45, 0.85, rear[-1] - 0.9, 0.45, 1.05, zf + 0.9), R['steel'])
    o.add('fender_f', box(-cw - 0.1, 0.9, zf - 0.7, cw + 0.1, 1.05, zf + 0.7), R['dark'])
    if tri:
        o.add('cab', box(-0.8, 0.9, zf - 0.7, 0.8, 2.1, zf + 0.6), R['paint']); o.add('glass', box(-0.7, 1.4, zf + 0.58, 0.7, 1.95, zf + 0.62), R['glass'])
        fifth = zr + 0.2; seat = zf - 0.1
    elif layout == 'cab':
        h = 3.1 + min(0.5, kw / 900); l = 2.2
        o.add('cab', box(-cw, 1.05, zf - 1.2, cw, h, zf - 1.2 + l), R['paint'])
        o.add('glass', box(-cw + 0.1, h - 1.2, zf - 1.2 + l - 0.02, cw - 0.1, h - 0.15, zf - 1.2 + l + 0.02), R['glass'])
        o.add('grille', box(-0.7, 1.15, zf - 1.2 + l - 0.02, 0.7, 1.8, zf - 1.2 + l + 0.04), R['chrome'])
        if year >= 1960:
            o.add('sleeper', box(-cw, h - 0.9, zf - 2.2, cw, h + 0.1, zf - 1.2), R['paint'])
        fifth = zr + 0.3; seat = zf - 0.4
    else:
        hl = 2.0
        o.add('hood', box(-0.65, 1.05, zf - 0.2, 0.65, 2.1, zf + hl), R['paint'])
        o.add('grille', box(-0.6, 1.1, zf + hl, 0.6, 2.0, zf + hl + 0.06), R['chrome'])
        o.add('cab', box(-cw, 1.05, zf - 1.9, cw, 3.0, zf - 0.2), R['paint'])
        o.add('glass', box(-cw + 0.1, 2.0, zf - 0.24, cw - 0.1, 2.85, zf - 0.18), R['glass'])
        if year >= 1960: o.add('sleeper', box(-cw, 1.9, zf - 3.0, cw, 3.2, zf - 1.9), R['paint'])
        fifth = zr + 0.3; seat = zf - 1.0
    o.add('fifth_wheel', box(-0.6, 1.05, fifth - 0.55, 0.6, 1.2, fifth + 0.55), R['dark'])
    if not tri:
        for sx in (-1, 1):
            o.add('stack%d' % sx, box(sx * (cw + 0.05) - 0.07, 1.2, seat - 1.4, sx * (cw + 0.05) + 0.07, 3.6 if layout == 'conv' else 3.2, seat - 1.26), R['chrome'])
            o.add('tank%d' % sx, cyl_z(sx * (cw + 0.05), 1.35, zr + 0.6, zr + 2.2, 0.3, 10), R['chrome'])
    wheels = [(-0.95 * big, zf), (0.95 * big, zf)] if not tri else [(0.0, zf)]
    for z in rear:
        wheels += [(-0.95, z), (0.95, z)]
    o.add('lamp', box(-0.5, 1.0, zf + (2.1 if layout == 'conv' else 0.9), 0.5, 1.2, zf + (2.2 if layout == 'conv' else 1.0)), R['glass'])
    return o, dict(wheels=wheels, fifth=fifth, seat=seat, front=zf + (2.2 if layout == 'conv' else 1.0), rear=rear[-1] - 0.9, h=3.6, w=2.5)

def primitive_tractor(layout, axles, kw):
    o = Obj()
    if layout == 'team':
        o.add('limber', box(-0.5, 0.7, -1.2, 0.5, 0.95, 0.6), R['paint']); o.add('seat', box(-0.3, 0.95, -0.9, 0.3, 1.5, -0.5), R['dark']); o.add('pole', box(-0.08, 0.8, 0.6, 0.08, 0.9, 3.6), R['dark'])
        for i, sx in enumerate((-0.45, 0.45)): o.add('animal%d' % i, box(sx - 0.3, 0.7, 1.6, sx + 0.3, 1.9, 3.4), R['dark']); o.add('head%d' % i, box(sx - 0.15, 1.5, 3.4, sx + 0.15, 2.0, 3.9), R['dark'])
        wheels = [(-0.7, 0.2), (0.7, 0.2), (-0.7, -0.9), (0.7, -0.9)]; return o, dict(wheels=wheels, fifth=-1.0, seat=-0.7, front=3.9, rear=-1.3, h=2.2, w=1.8, r=0.45, drawbar=-1.3)
    o.add('boiler', cyl_z(0.0, 1.9, -1.2, 2.2, 0.8, 12), R['steel']); o.add('chimney', box(-0.18, 2.6, 1.7, 0.18, 3.8, 2.1), R['dark']); o.add('firebox', box(-0.8, 1.0, -2.0, 0.8, 2.4, -1.2), R['paint'])
    o.add('cylinder', box(-0.3, 2.7, 0.6, 0.3, 3.0, 1.2), R['chrome']); o.add('canopy', box(-1.0, 3.5, -2.2, 1.0, 3.6, -0.6), R['paint'])
    wheels = [(-1.0, 1.5), (1.0, 1.5), (-1.15, -1.2), (1.15, -1.2)]
    return o, dict(wheels=wheels, fifth=-1.6, seat=-1.4, front=2.2, rear=-2.2, h=3.8, w=2.8, r=0.5, drawbar=-2.4, big=True)

def trailer(kind, L, axles):
    o = Obj(); z0, z1 = -L / 2, L / 2; hh = 4.0
    o.add('rails', box(-0.5, 1.0, z0, 0.5, 1.15, z1), R['steel'])
    o.add('kingpin_plate', box(-0.6, 1.15, z1 - 1.5, 0.6, 1.2, z1 - 0.5), R['dark'])
    for sx in (-1, 1): o.add('leg%d' % sx, box(sx * 1.0 - 0.06, 0.2, z1 - 3.4, sx * 1.0 + 0.06, 1.0, z1 - 3.28), R['steel'])
    if kind in ('box', 'chassis', 'curtain', 'livestock'):
        top = {'box': 4.0, 'chassis': 3.6, 'curtain': 3.9, 'livestock': 3.9}[kind]
        reg = 'paint' if kind != 'chassis' else 'steel'
        o.add('body', box(-1.25, 1.15, z0, 1.25, top, z1 - 0.2), R[reg])
        o.add('door', box(-1.2, 1.3, z0 - 0.04, 1.2, top - 0.1, z0), R['steel'])
        if kind == 'livestock':
            for i in range(6): o.add('slat%d' % i, box(-1.27, 1.4 + i * 0.4, z0 + 0.3, 1.27, 1.52 + i * 0.4, z1 - 0.5), R['dark'])
        hh = top
    elif kind == 'flat' or kind == 'step':
        o.add('deck', box(-1.25, 1.15, z0, 1.25, 1.3, z1 - 0.2 if kind == 'flat' else z1 - 0.2 - L * 0.28), R['paint'])
        if kind == 'step': o.add('upper', box(-1.25, 1.3, z1 - 0.2 - L * 0.28, 1.25, 1.55, z1 - 0.2), R['paint'])
        o.add('headboard', box(-1.25, 1.3, z1 - 0.3, 1.25, 2.3, z1 - 0.2), R['steel']); hh = 2.3
    elif kind == 'lowboy':
        o.add('neck', box(-1.0, 1.15, z1 - 2.0, 1.0, 1.75, z1 - 0.2), R['paint'])
        o.add('deck', box(-1.3, 0.6, z0, 1.3, 0.75, z1 - 3.0), R['paint'])
        o.add('ramp', box(-1.0, 0.45, z0 - 1.6, 1.0, 0.6, z0), R['steel']); hh = 1.8
    elif kind == 'tank':
        o.add('shell', cyl_z(0.0, 2.2, z0, z1 - 0.8, 1.1, 14), R['chrome'])
        o.add('manway', box(-0.3, 3.3, -0.4, 0.3, 3.5, 0.4), R['dark']); hh = 3.5
    elif kind == 'carrier':
        o.add('lower', box(-1.2, 1.15, z0, 1.2, 1.3, z1 - 0.2), R['paint']); o.add('upper', box(-1.2, 3.0, z0, 1.2, 3.12, z1 - 3.0), R['paint'])
        for sx in (-1, 1):
            for z in (z0 + 0.2, 0.0, z1 - 3.0): o.add('post%d_%d' % (sx, int(z * 10)), box(sx * 1.2 - 0.05, 1.3, z, sx * 1.2 + 0.05, 3.0, z + 0.1), R['steel'])
        hh = 3.2
    elif kind == 'log':
        o.add('deck', box(-1.25, 1.15, z0, 1.25, 1.3, z1 - 1.0), R['paint'])
        for sx in (-1, 1):
            for z in (z0 + 1.0, z0 + L * 0.5): o.add('bunk%d_%d' % (sx, int(z)), box(sx * 1.1 - 0.05, 1.3, z, sx * 1.1 + 0.05, 3.6, z + 0.1), R['steel'])
        for i, (cx, cy) in enumerate(((-0.5, 1.7), (0.5, 1.7), (0.0, 2.5))): o.add('log%d' % i, cyl_z(cx, cy, z0, z1 - 1.2, 0.4, 8), R['dark'])
        hh = 3.0
    elif kind == 'dump':
        o.add('floor', box(-1.25, 1.15, z0, 1.25, 1.3, z1 - 0.2), R['paint'])
        for sx in (-1, 1): o.add('side%d' % sx, box(sx * 1.25 - 0.06, 1.3, z0, sx * 1.25 + 0.06, 3.0, z1 - 0.2), R['paint'])
        o.add('front', box(-1.25, 1.3, z1 - 0.3, 1.25, 3.2, z1 - 0.2), R['paint']); o.add('tail', box(-1.25, 1.3, z0, 1.25, 3.0, z0 + 0.08), R['steel']); hh = 3.2
    elif kind in ('cart', 'wagon', 'dray'):
        o = Obj(); h = 1.8 if kind == 'wagon' else 1.2
        o.add('bed', box(-0.8, 0.7, z0, 0.8, 0.85, z1), R['paint'])
        for sx in (-1, 1): o.add('side%d' % sx, box(sx * 0.8 - 0.04, 0.85, z0, sx * 0.8 + 0.04, h, z1), R['paint'])
        o.add('tailboard', box(-0.8, 0.85, z0, 0.8, h, z0 + 0.08), R['paint']); o.add('drawbar', box(-0.06, 0.7, z1, 0.06, 0.8, z1 + 1.6), R['dark'])
        if kind == 'wagon': o.add('canvas', box(-0.78, h, z0, 0.78, h + 0.9, z1), R['chrome'])
        if kind == 'dray': o.add('barrels', cyl_z(0.0, 1.2, z0 + 0.3, z1 - 0.3, 0.38, 8), R['dark'])
        zs = [z0 + 0.8 + i * (L - 1.6) / max(1, axles - 1) for i in range(axles)] if axles > 1 else [0.0]
        return o, dict(zs=zs, kingpin=z1, w=1.8, h=h + (0.9 if kind == 'wagon' else 0), L=L, deck=0.85, primitive=True, wide=0.75)
    elif kind == 'modular':
        o.add('deck', box(-1.6, 0.9, z0, 1.6, 1.05, z1), R['paint']); hh = 1.1
    zs = [z0 + 1.2 + i * 1.35 for i in range(axles)]
    return o, dict(zs=zs, kingpin=z1 - 1.0, w=2.6 if kind != 'modular' else 3.2, h=hh, L=L, deck=1.3 if kind not in ('lowboy', 'modular') else 0.8)

CONS = {  # kind: (wheel layout, drive)
 'roller': dict(wheels=[(-0.0, 1.6), (0.0, -1.6)], r=0.7, drum=True), 'dozer': dict(tread=True), 'excavator': dict(tread=True), 'paver': dict(tread=True),
 'grader': dict(wheels=[(-0.9, 3.0), (0.9, 3.0), (-0.9, -1.0), (0.9, -1.0), (-0.9, -2.3), (0.9, -2.3)], r=0.7),
 'loader': dict(wheels=[(-1.1, 1.5), (1.1, 1.5), (-1.1, -1.5), (1.1, -1.5)], r=0.9), 'backhoe': dict(wheels=[(-0.9, 1.3), (0.9, 1.3), (-0.9, -1.3), (1.0, -1.3)], r=0.65),
 'skid': dict(wheels=[(-0.7, 0.8), (0.7, 0.8), (-0.7, -0.8), (0.7, -0.8)], r=0.35), 'miller': dict(wheels=[(-1.0, 2.0), (1.0, 2.0), (-1.0, -2.0), (1.0, -2.0)], r=0.7),
 'haul': dict(wheels=[(-2.0, 4.0), (2.0, 4.0), (-2.0, -2.0), (2.0, -2.0), (-2.0, -3.9), (2.0, -3.9)], r=1.8), 'mixer': dict(truck=True), 'pump': dict(truck=True),
}

def primitive_construction(kind):
    o = Obj()
    if kind == 'hroller':
        o.add('drum', cyl_x(0.5, 0.0, -0.8, 0.8, 0.5, 12), R['steel']); o.add('frame', box(-0.9, 0.9, -0.5, 0.9, 1.0, 0.5), R['paint']); o.add('shaft', box(-0.06, 0.8, 0.5, 0.06, 0.9, 2.6), R['dark'])
        return o, dict(wheels=[], drum=True, w=1.8, h=1.2, L=3.0)
    if kind in ('dumpcart', 'scraper'):
        if kind == 'dumpcart':
            o.add('bed', box(-0.7, 0.7, -0.9, 0.7, 0.8, 0.9), R['paint'])
            for sx in (-1, 1): o.add('side%d' % sx, box(sx * 0.7 - 0.04, 0.8, -0.9, sx * 0.7 + 0.04, 1.3, 0.9), R['paint'])
        else:
            o.add('bowl', box(-0.6, 0.3, -0.6, 0.6, 0.9, 0.6), R['steel']); o.add('handle', box(-0.05, 0.9, -1.4, 0.05, 1.3, -0.5), R['dark'])
        o.add('shaft', box(-0.06, 0.6, 0.6, 0.06, 0.7, 2.6), R['dark'])
        return o, dict(wheels=[(-0.8, 0.0), (0.8, 0.0)], r=0.5, w=1.8, h=1.4, L=3.4)
    o.add('boiler', cyl_z(0.0, 1.9, -1.4, 1.2, 0.8, 12), R['steel']); o.add('chimney', box(-0.18, 2.6, 0.8, 0.18, 3.8, 1.2), R['dark']); o.add('boom', box(-0.15, 2.0, 1.2, 0.15, 4.6, 1.5), R['dark']); o.add('bucket', box(-0.5, 0.3, 1.5, 0.5, 1.2, 2.3), R['steel'])
    o.add('carriage', box(-1.0, 0.8, -2.0, 1.0, 1.1, 1.6), R['paint'])
    return o, dict(wheels=[(-1.0, 1.0), (1.0, 1.0), (-1.0, -1.2), (1.0, -1.2)], r=0.5, w=2.2, h=4.6, L=4.4)

def construction(kind, kw):
    if kind in ('hroller', 'dumpcart', 'scraper', 'steamshovel'): return primitive_construction(kind)
    o = Obj(); c = CONS[kind]; info = {}
    if c.get('truck'):
        t, ti = tractor('cab', 4, kw, 2000); o = t; info = ti
        if kind == 'mixer':
            o.add('drum', cyl_z(0, 2.3, ti['rear'] + 0.2, ti['fifth'] + 2.0, 1.15, 14), R['paint']); o.add('chute', box(-0.2, 1.5, ti['rear'] - 0.8, 0.2, 2.0, ti['rear'] + 0.4), R['steel'])
        else:
            o.add('boom', box(-0.3, 2.4, ti['rear'], 0.3, 2.9, ti['fifth'] + 2.4), R['steel']); o.add('hopper', box(-0.5, 1.6, ti['rear'] - 0.8, 0.5, 2.3, ti['rear']), R['paint'])
        info['wheels'] = ti['wheels']; info['r'] = 0.5; info['tread'] = False
        info['w'] = 2.5; info['h'] = 3.9
        return o, info
    if c.get('tread'):
        L, W, H = {'dozer': (4.2, 3.2, 3.2), 'excavator': (5.0, 3.0, 3.1), 'paver': (5.6, 2.6, 3.0)}[kind]
        o.add('hull', box(-W / 2 + 0.5, 0.8, -L / 2, W / 2 - 0.5, 1.8, L / 2), R['paint'])
        o.add('cab', box(-0.7, 1.8, -L / 2 + 0.3, 0.7, H, 0.8), R['paint']); o.add('glass', box(-0.6, 2.2, 0.78, 0.6, H - 0.1, 0.84), R['glass'])
        if kind == 'dozer': o.add('blade', box(-W / 2, 0.4, L / 2, W / 2, 1.5, L / 2 + 0.2), R['steel']); o.add('ripper', box(-0.3, 0.4, -L / 2 - 0.8, 0.3, 1.0, -L / 2), R['dark'])
        if kind == 'excavator':
            o.add('boom', box(-0.2, 1.8, 0.8, 0.2, 2.6, 4.5), R['paint']); o.add('stick', box(-0.15, 0.5, 4.3, 0.15, 2.4, 4.6), R['paint']); o.add('bucket', box(-0.6, 0.3, 4.3, 0.6, 1.0, 5.2), R['steel'])
        if kind == 'paver': o.add('hopper', box(-W / 2, 1.0, L / 2, W / 2, 2.0, L / 2 + 1.2), R['steel']); o.add('screed', box(-W / 2 - 0.3, 0.3, -L / 2 - 1.4, W / 2 + 0.3, 0.6, -L / 2), R['dark'])
        return o, dict(tread=True, tx=W / 2 - 0.3, tl=L, w=W, h=H, L=L)
    ws = c['wheels']; r = c['r']
    zmax = max(z for _, z in ws); zmin = min(z for _, z in ws); xm = max(abs(x) for x, _ in ws)
    if c.get('drum'):
        o.add('drum_f', cyl_x(0.7, ws[0][1], -1.0, 1.0, 0.7, 12), R['steel']); o.add('drum_r', cyl_x(0.7, ws[1][1], -1.0, 1.0, 0.7, 12), R['steel']); o.add('frame', box(-1.05, 0.9, ws[1][1], 1.05, 1.6, ws[0][1]), R['paint']); o.add('seat', box(-0.5, 1.6, -0.6, 0.5, 2.9, 0.5), R['paint'])
        return o, dict(wheels=[], drum=True, r=r, w=2.2, h=3.0, L=4.2)
    o.add('frame', box(-xm + 0.1, r + 0.2, zmin - 0.6, xm - 0.1, r + 1.1, zmax + 0.6), R['paint'])
    o.add('cab', box(-xm + 0.2, r + 1.1, zmin + 0.2, xm - 0.2, r + 2.4, zmin + 2.2), R['paint']); o.add('glass', box(-xm + 0.3, r + 1.5, zmin + 2.18, xm - 0.3, r + 2.3, zmin + 2.24), R['glass'])
    if kind == 'grader': o.add('blade', box(-1.8, 0.3, 0.0, 1.8, 1.0, 0.25), R['steel'])
    if kind in ('loader', 'backhoe', 'skid'): o.add('bucket', box(-xm, 0.2, zmax + 0.6, xm, 1.1, zmax + 1.6), R['steel']); o.add('arm', box(-0.15, 0.8, zmin + 1.0, 0.15, 1.5, zmax + 0.8), R['steel'])
    if kind == 'backhoe': o.add('hoe', box(-0.2, 0.4, zmin - 3.0, 0.2, 1.8, zmin - 0.6), R['steel'])
    if kind == 'miller': o.add('drum', cyl_x(0.5, 0.0, -1.1, 1.1, 0.5, 12), R['dark'])
    if kind == 'haul': o.add('bed', box(-2.2, r * 2 - 0.3, zmin - 0.4, 2.2, r * 2 + 2.6, zmax - 1.2), R['paint']); o.add('canopy', box(-2.2, r * 2 + 1.5, zmax - 1.2, 2.2, r * 2 + 2.9, zmax + 0.2), R['paint'])
    return o, dict(wheels=ws, r=r, w=xm * 2 + 0.8, h=r + 3.0 if kind != 'haul' else 7.0, L=zmax - zmin + 2.0)

# ---------------- parts ----------------
ENGINE_CLASSES = [(150, 'e150'), (260, 'e250'), (340, 'e350'), (450, 'e450'), (700, 'e700'), (4000, 'e3000')]
CLS_KW = {'draft': 3, 's30': 40, 's80': 90, 'e150': 130, 'e250': 250, 'e350': 335, 'e450': 400, 'e700': 600, 'e3000': 2500}
TIERS = {'stock': ('Stock', 1.0, 1.0), 'eff': ('Efficiency', 1.0, 0.85), 'speed': ('Speed', 1.15, 1.30)}  # power x, fuel x (speed raises W and burn; efficiency = more J per litre)
ENAME = {'draft': 'Draft Team', 's30': 'Steam Engine', 's80': 'Steam Engine'}
FUEL = {'draft': 'hay', 's30': 'coal', 's80': 'coal'}  # primitive power; IV needs a fuel mapping for these (untested)
def eng_class(kw, prim=None):
    if prim == 'draft': return 'draft'
    if prim == 'steam': return 's30' if kw <= 40 else 's80'
    return next(c for lim, c in ENGINE_CLASSES if kw <= lim)
LPH = 0.25  # litres of diesel per kWh at full load (about 40 MJ/L at 36 % efficiency)

def write_parts():
    w(f'{A}/packdefinition.json', {'packID': PK, 'packName': 'Diaithir Haulage and Construction', 'packItem': 'truck_kenworth_w900', 'fileStructure': 0})
    parts = []
    atlas((60, 62, 66), f'{A}/textures/parts/engine.png')
    for cls, kw in CLS_KW.items():
        s = 0.5 + min(1.5, kw / 600); o = Obj(); o.add('block', box(-0.4 * s, 0.0, -0.6 * s, 0.4 * s, 0.7 * s, 0.6 * s), R['steel']); o.add('head', box(-0.35 * s, 0.7 * s, -0.55 * s, 0.35 * s, 0.9 * s, 0.55 * s), R['dark']); o.write(f'{A}/objmodels/parts/engine_{cls}.obj')
        for tid, (tn, px, fx) in TIERS.items():
            pid = f'engine_{cls}_{tid}'; p = kw * px; rpm = 2400 if kw < 800 else 1800
            if cls in FUEL: rpm = 600
            w(f'{A}/jsondefs/parts/{pid}.json', {
             'general': {'type': 'engine_car', 'name': f'{ENAME.get(cls, "Diesel Engine")} {kw} kW ({tn})', 'materialLists': [['minecraft:iron_block:0:%d' % max(2, kw // 60)], ['minecraft:piston:0:%d' % (4 if kw < 500 else 8)]]},
             'engine': {'type': 'NORMAL', 'isAutomatic': False, 'fuelType': FUEL.get(cls, 'diesel'), 'fuelConsumption': round(kw / 300 * fx * (px if tid == 'speed' else 1) * 0.9, 3), 'maxRPM': int(rpm * (1.05 if tid == 'speed' else 1)), 'maxSafeRPM': int(rpm * 0.92), 'idleRPM': 600 if cls not in FUEL else 150, 'startRPM': 450 if cls not in FUEL else 100,
                        'stallRPM': 350 if cls not in FUEL else 50, 'revlimitRPM': int(rpm * 1.1), 'starterPower': 6, 'gearRatios': [-6.0, 0.0, 8.0, 5.0, 3.0] if cls in FUEL else [-9.0, 0.0, 12.0, 8.0, 5.5, 4.0, 3.0, 2.2, 1.6, 1.2] if kw < 800 else [-4.0, 0.0, 6.0, 4.0, 2.8, 2.0, 1.4],
                        'upShiftRPM': [int(rpm * 0.85)] * 7, 'downShiftRPM': [1000] * 7, 'shiftSpeed': 14, 'clutchTime': 14, 'revResistance': 8, 'heatingCoefficient': 0.4, 'coolingCoefficient': 0.6, 'engineWearFactor': 0.3},
             'definitions': [{'subName': '', 'name': f'{ENAME.get(cls, "Diesel Engine")} {kw} kW ({tn})', 'modelName': f'engine_{cls}', 'textureName': 'engine', 'extraMaterialLists': [[]]}]})
            parts.append(dict(id=pid, name=f'{ENAME.get(cls, "Diesel Engine")} {kw} kW ({tn})', kw=round(p), lph=round(p * LPH * fx), mass=int(kw * 4.5), tier=tid))
    for pid, nm, r, wd in (('truck_wheel', 'Truck Wheel', 0.5, 0.35), ('heavy_wheel', 'Heavy Wheel', 0.7, 0.45), ('giant_wheel', 'Giant Wheel', 1.8, 1.0), ('small_wheel', 'Small Wheel', 0.35, 0.25)):
        w(f'{A}/jsondefs/parts/{pid}.json', {
         'general': {'type': 'ground_wheel', 'name': nm, 'materialLists': [['minecraft:iron_ingot:0:%d' % int(8 * r), 'minecraft:black_dye:0:%d' % int(6 * r + 1)]]},
         'ground': {'isWheel': True, 'width': wd, 'height': r * 2, 'flatHeight': r * 2 * 0.85, 'climbHeight': r * 0.9, 'motiveFriction': 1.0, 'lateralFriction': 1.0, 'extraCollisionBoxOffset': 0.0, 'canGoFlat': True},
         'wheel': {'diameter': r * 2, 'motiveFriction': 1.0, 'lateralFriction': 1.0},
         'definitions': [{'subName': '', 'name': nm, 'extraMaterialLists': [[]]}]})
        wheel_model(r, wd).write(f'{A}/objmodels/parts/{pid}.obj'); atlas((40, 40, 42), f'{A}/textures/parts/{pid}.png')
    w(f'{A}/jsondefs/parts/cv_track.json', {
     'general': {'type': 'ground_tread', 'name': 'Machine Track', 'materialLists': [['minecraft:iron_ingot:0:30']]},
     'ground': {'isTread': True, 'width': 0.7, 'height': 0.9, 'flatHeight': 0.9, 'climbHeight': 0.9, 'motiveFriction': 1.0, 'lateralFriction': 1.4, 'extraCollisionBoxOffset': 0.0, 'spacing': 6.0, 'canGoFlat': False},
     'definitions': [{'subName': '', 'name': 'Machine Track', 'extraMaterialLists': [[]]}]})
    o = Obj(); o.add('track', box(-0.35, 0.05, -2.8, 0.35, 0.85, 2.8), R['steel'])
    for i in range(5): o.add('wheel%d' % i, cyl_x(0.45, -2.2 + i * 1.1, -0.38, 0.38, 0.33, 8), R['chrome'])
    o.write(f'{A}/objmodels/parts/cv_track.obj'); atlas((46, 44, 42), f'{A}/textures/parts/cv_track.png')
    w(f'{A}/jsondefs/parts/seat.json', {'general': {'type': 'seat', 'name': 'Cab Seat', 'materialLists': [['minecraft:iron_ingot:0:4', 'minecraft:leather:0:4']]}, 'definitions': [{'subName': '', 'name': 'Cab Seat', 'extraMaterialLists': [[]]}]})
    o = Obj(); o.add('seat', box(-0.25, 0.0, -0.25, 0.25, 0.45, 0.25) + box(-0.25, 0.45, -0.28, 0.25, 1.0, -0.2), R['dark']); o.write(f'{A}/objmodels/parts/seat.obj'); atlas((50, 50, 52), f'{A}/textures/parts/seat.png')
    cargo = []
    for pid, nm, kind, units in (('crate_s', 'Cargo Hold 18 slots', 'CRATE', 2), ('crate_m', 'Cargo Hold 54 slots', 'CRATE', 6), ('crate_l', 'Cargo Hold 108 slots', 'CRATE', 12),
                                 ('tank_10', 'Tank 10000 L', 'BARREL', 1), ('tank_20', 'Tank 20000 L', 'BARREL', 2), ('tank_30', 'Tank 30000 L', 'BARREL', 3)):
        w(f'{A}/jsondefs/parts/{pid}.json', {
         'general': {'type': 'interactable_crate' if kind == 'CRATE' else 'interactable_barrel', 'name': nm, 'materialLists': [['minecraft:iron_ingot:0:%d' % (units * 6)]]},
         'interactable': {'interactionType': kind.lower(), 'inventoryUnits': units, 'inventoryStackSize': 64, 'canBeOpenedInHand': False},
         'definitions': [{'subName': '', 'name': nm, 'extraMaterialLists': [[]]}]})
        o = Obj(); o.add('hold', box(-0.4, 0.0, -0.4, 0.4, 0.6, 0.4), R['steel']); o.write(f'{A}/objmodels/parts/{pid}.obj'); atlas((90, 90, 94), f'{A}/textures/parts/{pid}.png')
        cargo.append(dict(id=pid, name=nm, kind=kind.lower(), units=units, slots=units * 9 if kind == 'CRATE' else 0, litres=units * 10000 if kind == 'BARREL' else 0))
    return parts, cargo

def engine_for(kw, tier='stock', prim=None): return f'{PK}:engine_{eng_class(kw, prim)}_{tier}'
def mats(mass, kind='truck'):
    return [['minecraft:iron_ingot:0:%d' % max(8, mass // 400)], ['minecraft:iron_block:0:%d' % max(1, mass // 4000)], ['minecraft:redstone:0:8'], ['minecraft:black_dye:0:4']]
def paints(pal, name, mdl): return [{'subName': s, 'name': name + f' ({lab})', 'modelName': mdl, 'textureName': ('paintc' if pal is CPAINTS else 'paint') + s, 'extraMaterialLists': [[]]} for s, (lab, _) in pal.items()]

def drive_defs(cls_, ws, r, kw):
    p = []
    wid = {0.35: 'small_wheel', 0.5: 'truck_wheel', 0.7: 'heavy_wheel', 1.8: 'giant_wheel'}[r]
    for x, z in ws: p.append({'pos': [x, r, z], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:{wid}'})
    return p

def motorized(mass, fuel, kw, trailer=False, big=True):
    m = {'emptyMass': mass, 'fuelCapacity': fuel, 'defaultFuelQty': fuel // 2, 'hasRunningLights': True, 'hasHeadlights': True, 'axleRatio': 3.6, 'brakingFactor': 1.4, 'dragCoefficient': 0.8, 'crossSectionalArea': 9.0,
         'steeringForceFactor': 1.0, 'crashSpeedMin': 0.5, 'crashSpeedMax': 1.0, 'crashSpeedDestroyed': 2.0}
    if trailer: m['isTrailer'] = True
    return m

def anim(objs, typ, var, center, axis, dur=100):
    """IV animatedObjects entries for OBJ object names; custom switch variables show on the vehicle panel (parse-checked only, sign and feel untested)."""
    return [{'objectName': ob, 'animations': [{'animationType': typ, 'variable': var, 'centerPoint': center, 'axis': axis, 'clampMin': 0.0, 'clampMax': 3000.0 if var.startswith('engine_') else 1.0, 'duration': dur}]} for ob in objs]

def trailer_anims(kind, L):
    z0 = -L / 2
    if kind == 'dump': return anim(['floor', 'side-1', 'side1', 'front', 'tail'], 'rotation', 'dump_tilt', [0, 1.15, z0], [-35, 0, 0])
    if kind == 'lowboy': return anim(['ramp'], 'rotation', 'ramp_down', [0, 0.6, z0], [-20, 0, 0])
    if kind in ('box', 'livestock'): return anim(['door'], 'rotation', 'rear_doors', [1.2, 2.5, z0], [0, -110, 0])
    return []

def cons_anims(kind, i):
    if kind == 'dozer': return anim(['blade'], 'translation', 'blade_lift', [0, 0, 0], [0, 0.8, 0])
    if kind == 'excavator': return anim(['boom', 'stick', 'bucket'], 'rotation', 'boom_lift', [0, 1.8, 0.8], [-25, 0, 0])
    if kind == 'paver': return anim(['screed'], 'translation', 'screed_lift', [0, 0, 0], [0, 0.5, 0])
    if kind == 'haul': return anim(['bed'], 'rotation', 'dump_tilt', [0, 3.3, -4.3], [-40, 0, 0])
    if kind == 'mixer': return anim(['drum'], 'rotation', 'engine_rpm_1', [0, 2.3, (i['rear'] + i['fifth']) / 2 + 1.1], [0, 0, 0.02], 0)
    if kind in ('loader', 'backhoe', 'skid'): return anim(['bucket', 'arm'], 'rotation', 'bucket_lift', [0, 0.8, min(z for _, z in i['wheels']) + 1.0], [-30, 0, 0])
    if kind == 'dumpcart': return anim(['bed', 'side-1', 'side1'], 'rotation', 'dump_tilt', [0, 0.7, -0.9], [-35, 0, 0])
    if kind == 'steamshovel': return anim(['boom', 'bucket'], 'rotation', 'boom_lift', [0, 2.0, 1.2], [-25, 0, 0])
    return []

def write_vehicle(vid, nm, desc, body_info, mass, hp, extra_parts, extra, pal, mdl, motor, trailer=False, anims=()):
    d = {'general': {'type': 'vehicle', 'name': nm, 'description': desc, 'materialLists': mats(mass), 'health': hp}, 'motorized': motor, 'parts': extra_parts,
         'collision': [{'pos': [0, body_info['h'] / 2 + 0.3, 0], 'width': body_info['w'], 'height': body_info['h']}], 'definitions': paints(pal, nm, mdl), 'rendering': ({'animatedObjects': list(anims), 'customVariables': sorted({a['animations'][0]['variable'] for a in anims if not a['animations'][0]['variable'].startswith('engine_')})} if anims else {})}
    if not trailer: d['car'] = {'isFrontWheelDrive': False, 'isRearWheelDrive': True, 'axleRatio': 3.6, 'dragCoefficient': 0.8, 'isBigTruck': True}
    d.update(extra); w(f'{A}/jsondefs/vehicles/{vid}.json', d)

def main():
    eng_parts, cargo_parts = write_parts()
    for s in PAINTS: atlas(PAINTS[s][1], f'{A}/textures/vehicles/paint{s}.png')
    for s in CPAINTS: atlas(CPAINTS[s][1], f'{A}/textures/vehicles/paintc{s}.png')
    man = {'tractors': [], 'trailers': [], 'construction': [], 'engines': eng_parts, 'cargo_parts': cargo_parts}
    for (vid, nm, maker, yr, ctry, lay, ax, kw, page, gvw) in ros.TRACTORS + ros.PRIM_TRACTORS:
        prim = {'team': 'draft', 'steam': 'steam'}.get(lay)
        o, i = tractor(lay, ax, kw, yr); mid = 'truck_' + vid; o.write(f'{A}/objmodels/vehicles/{mid}.obj')
        wid = 'small_wheel' if lay == 'tri' else 'truck_wheel'
        parts = [{'pos': [0, 1.0, i['rear'] + 1.4], 'types': ['engine_car'], 'defaultPart': engine_for(kw, prim=prim)}]
        parts += [{'pos': [x, 0.5 if lay != 'tri' else 0.35, z], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:{wid}'} for x, z in i['wheels']]
        parts += [{'pos': [-0.5, 1.5, i['seat']], 'types': ['seat'], 'isController': True, 'defaultPart': f'{PK}:seat', 'dismountPos': [-1.8, 0.0, i['seat']]}]
        if lay != 'tri': parts.append({'pos': [0.5, 1.5, i['seat']], 'types': ['seat'], 'defaultPart': f'{PK}:seat'})
        mass = int(2000 + kw * 14) if lay != 'tri' else 1500
        fuel = int(min(1500, max(60, kw * 2.4)))
        if prim == 'draft': fuel = 60
        grp = [{'groupName': 'drawbar', 'canInitiateConnections': True, 'isHitch': True, 'connections': [{'type': 'drawbar', 'pos': [0, 0.8, i['rear'] - 0.2], 'distance': 2.0}]}]
        if not prim: grp.insert(0, {'groupName': 'fifthwheel', 'canInitiateConnections': True, 'isHitch': True, 'connections': [{'type': 'fifthwheel', 'pos': [0, 1.3, i['fifth']], 'distance': 2.0}]})
        write_vehicle(mid, nm, f'{nm} ({yr}, {ctry}): {kw} kW {"tractor unit" if not prim else "motive unit"}, {ax} axles, gross {gvw // 1000} t.', i, mass, int(700 + kw), parts, {'connectionGroups': grp}, PAINTS, mid, motorized(mass, fuel, kw))
        man['tractors'].append(dict(id=mid, name=nm, maker=maker, year=yr, country=ctry, layout=lay, axles=ax, power_w=int(kw * 1000), mass_kg=mass, gvw_kg=gvw, fuel_l=fuel, age=age_of(mid, yr), wiki=page))
    for (vid, nm, yr, kind, cg, payload, ax, L, page) in ros.TRAILERS + ros.PRIM_TRAILERS:
        o, i = trailer(kind, L, ax); mid = 'trailer_' + vid; o.write(f'{A}/objmodels/vehicles/{mid}.obj')
        wr = 0.35 if kind == 'modular' else 0.5
        pw = i.get('wide', 0.95)
        parts = [{'pos': [sx * pw, 0.5, z], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:truck_wheel'} for z in i['zs'] for sx in (-1, 1)]
        crate = tank = None
        if cg == 'crate':
            cid = 'crate_l' if payload >= 40000 or kind in ('box', 'curtain', 'chassis') else 'crate_s' if payload < 4000 else 'crate_m'; parts.append({'pos': [0, i['deck'] + 0.3, 0], 'types': ['interactable_crate'], 'defaultPart': f'{PK}:{cid}'}); crate = cid
        elif cg == 'tank':
            parts.append({'pos': [0, 2.2, 0], 'types': ['interactable_barrel'], 'defaultPart': f'{PK}:tank_30'}); tank = 'tank_30'
        grp = [{'groupName': 'kingpin', 'canInitiateConnections': False, 'isHookup': True, 'connections': [{'type': 'fifthwheel', 'pos': [0, 1.3, i['kingpin']], 'distance': 2.0}]}] if not i.get('primitive') else \
              [{'groupName': 'drawbar', 'canInitiateConnections': False, 'isHookup': True, 'connections': [{'type': 'drawbar', 'pos': [0, 0.8, i['kingpin'] + 1.6], 'distance': 2.0}]}]
        if cg == 'car':
            grp.append({'groupName': 'deck', 'canInitiateConnections': True, 'isHitch': True, 'connections': [{'type': 'tow', 'pos': [0, i['deck'] + 0.2, z], 'mounted': True, 'distance': 3.0} for z in (-L * 0.25, L * 0.1)]})
        mass = int(5000 + payload * 0.25) if not i.get('primitive') else int(150 + payload * 0.3)
        write_vehicle(mid, nm, f'{nm} ({yr}): {L:.1f} m, {ax} axles, payload {payload} kg. Hooks onto a {"drawbar" if i.get("primitive") else "fifth wheel"}.', i, mass, 800, parts, {'connectionGroups': grp}, PAINTS, mid, motorized(mass, 0, 0, trailer=True), trailer=True, anims=trailer_anims(kind, L))
        man['trailers'].append(dict(id=mid, name=nm, year=yr, kind=kind, cargo=cg, crate=crate, tank=tank, payload_kg=payload, axles=ax, length_m=L, mass_kg=mass, age=age_of(mid, yr), wiki=page))
    for (vid, nm, maker, yr, ctry, kind, kw, mass, page, role) in ros.CONSTRUCTION + ros.PRIM_CONSTRUCTION:
        o, i = construction(kind, kw); mid = 'cv_' + vid; o.write(f'{A}/objmodels/vehicles/{mid}.obj')
        if kind in ('hroller', 'dumpcart', 'scraper'):  # drawn by a team or a steam engine
            parts = [{'pos': [x, i.get('r', 0.35), z], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:{"small_wheel" if kind == "hroller" else "truck_wheel"}'} for x, z in (i['wheels'] or [(-0.9, 0.0), (0.9, 0.0)])]
            if kind == 'dumpcart': parts.append({'pos': [0, 0.9, 0], 'types': ['interactable_crate'], 'defaultPart': f'{PK}:crate_s'})
            grp = [{'groupName': 'drawbar', 'canInitiateConnections': False, 'isHookup': True, 'connections': [{'type': 'drawbar', 'pos': [0, 0.8, 2.6], 'distance': 2.0}]}]
            write_vehicle(mid, nm, f'{nm} ({yr}, {ctry}): drawn work vehicle, {mass} kg. Road role: {role}.', i, mass, 300, parts, {'connectionGroups': grp}, CPAINTS, mid, motorized(mass, 0, 0, trailer=True), trailer=True, anims=cons_anims(kind, i))
            man['construction'].append(dict(id=mid, name=nm, maker=maker, year=yr, country=ctry, kind=kind, power_w=0, mass_kg=mass, fuel_l=0, road_role=role, age=age_of(mid, yr), wiki=page)); continue
        if i.get('tread'):
            parts = [{'pos': [sx * i['tx'], 0.0, 0.0], 'types': ['ground_tread'], 'defaultPart': f'{PK}:cv_track'} for sx in (-1, 1)]
            seat = (0.0, 2.1, 0.2)
        elif i.get('drum'):
            parts = []; seat = (0.0, 2.2, 0.0)
        else:
            r = i['r']; wid = {0.35: 'small_wheel', 0.5: 'truck_wheel', 0.7: 'heavy_wheel', 0.9: 'heavy_wheel', 0.65: 'heavy_wheel', 1.8: 'giant_wheel'}[r]
            parts = [{'pos': [x, r, z], 'types': ['ground_wheel'], 'defaultPart': f'{PK}:{wid}'} for x, z in i['wheels']]
            seat = (0.0, r + 1.5, (i['wheels'][0][1] + i['wheels'][-1][1]) / 2 if kind not in ('mixer', 'pump') else i['seat'])
        parts.append({'pos': [0, 1.0, 0.0], 'types': ['engine_car'], 'defaultPart': engine_for(kw, prim='steam' if kind == 'steamshovel' else None)})
        parts.append({'pos': list(seat), 'types': ['seat'], 'isController': True, 'defaultPart': f'{PK}:seat', 'dismountPos': [-2.0, 0.0, seat[2]]})
        extra = {}
        if kind == 'mixer': parts.append({'pos': [0, 2.3, -2.0], 'types': ['interactable_crate'], 'defaultPart': f'{PK}:crate_m'})
        if kind == 'haul': parts.append({'pos': [0, 5.0, -1.0], 'types': ['interactable_crate'], 'defaultPart': f'{PK}:crate_l'})
        if kind == 'pump': parts.append({'pos': [0, 1.8, -1.0], 'types': ['interactable_barrel'], 'defaultPart': f'{PK}:tank_10'})
        fuel = int(min(3000, max(60, kw * 2.4)))
        mot = motorized(mass, fuel, kw)
        if i.get('tread'): mot.update({'hasSkidSteer': True, 'hasPermanentSkidSteer': True})
        write_vehicle(mid, nm, f'{nm} ({yr}, {ctry}): {kw} kW, {mass // 1000} t. Road role: {role}.', i, mass, int(600 + kw // 2), parts, extra, CPAINTS, mid, mot, anims=cons_anims(kind, i))
        man['construction'].append(dict(id=mid, name=nm, maker=maker, year=yr, country=ctry, kind=kind, power_w=int(kw * 1000), mass_kg=mass, fuel_l=fuel, road_role=role, age=age_of(mid, yr), wiki=page))
    w(f'{M}/truck_manifest.json', man)
    return man

if __name__ == '__main__':
    m = main(); print('trucks pack', len(m['tractors']), 'tractors', len(m['trailers']), 'trailers', len(m['construction']), 'construction')
