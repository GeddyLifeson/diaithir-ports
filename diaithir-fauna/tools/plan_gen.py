#!/usr/bin/env python3
"""Writes src/main/resources/data/diaithirfauna/plans/<plan>.json: parametric body-plan models.

Model format = DiaithirRPG tools/art models.json (parts tree: uv, pivot, rot, boxes [x,y,z,w,h,d,grow], mirror, children),
so the creature-art pipeline (ao_bake.py, paint.py) reads them unchanged. Ground is y=24, front is -z, 16 px = 1 block.
Part names end in a role the runtime animates: leg_*, arm_*, head, jaw, tail_N, wing_*, fin_*, neck, body, ear_*, trunk_N.
Species size = scale on top of the plan's default size (entity box = model extent * scale).
"""
import json, math, os, sys

def pk(parts):
    """Shelf-pack box nets into a texture; sets uv on each part, returns (tw, th)."""
    items = []
    for n, p in parts.items():
        if p['mirror_of']: continue
        w, h, d = p['boxes'][0][3:6]
        items.append((n, int(2 * (d + w)), int(d + h)))
    for tw in (64, 128, 256, 512):
        if max(i[1] for i in items) > tw: continue
        x = y = rowh = 0; ok = True; pos = {}
        for n, iw, ih in sorted(items, key=lambda t: -t[2]):
            if x + iw > tw: x, y, rowh = 0, y + rowh, 0
            pos[n] = (x, y); x += iw; rowh = max(rowh, ih)
        th = y + rowh
        if th <= tw * 2 and (tw >= 128 or th <= 64): break
    th = 1 << max(5, (th - 1).bit_length())
    for n, p in parts.items():
        p['uv'] = list(pos[p['mirror_of'] or n])
    return tw, th

class M:
    def __init__(s): s.parts = {}; s.roots = []
    def add(s, name, parent, pivot, rot, size, off, mirror_of=None, grow=0.0):
        w, h, d = (0 if v == 0 else max(1, int(round(v + 1e-6))) for v in size)
        off = (off[0], off[1], off[2])
        s.parts[name] = dict(uv=[0, 0], pivot=[round(v, 2) for v in pivot], rot=[round(v, 3) for v in rot],
            boxes=[[round(off[0], 2), round(off[1], 2), round(off[2], 2), round(w, 2), round(h, 2), round(d, 2), grow]],
            mirror=bool(mirror_of), children=[], sets=None, mirror_of=mirror_of)
        if parent: s.parts[parent]['children'].append(name)
        else: s.roots.append(name)
    def pair(s, name, parent, pivot, rot, size, off, grow=0.0):
        """name_l / name_r mirrored about x. Right is the original, left shares its UV."""
        s.add(name + '_r', parent, (-pivot[0], pivot[1], pivot[2]), (rot[0], -rot[1], -rot[2]), size, off, None, grow)
        s.add(name + '_l', parent, pivot, rot, size, (-off[0] - size[0], off[1], off[2]), name + '_r', grow)
    def eyes(s):
        h = s.parts.get('head')
        if not h: return
        x, y, z, w, hh, d, _ = h['boxes'][0]
        e = round(min(3.0, max(1.5, w * .3)), 1)
        px = x + w + .02
        s.add('eye_right', 'head', (-px, y + hh * .3, z + d * .3), (0, 0, 0), (0, e, e), (0, -e / 2, -e / 2))
        s.add('eye_left', 'head', (px, y + hh * .3, z + d * .3), (0, 0, 0), (0, e, e), (0, -e / 2, -e / 2), 'eye_right')
    def out(s):
        s.eyes()
        tw, th = pk(s.parts)
        for p in s.parts.values(): p.pop('mirror_of')
        return dict(tw=tw, th=th, parts=s.parts, roots=s.roots)

def limb(m, name, parent, pivot, t, l, segs=2, rot=0.0, foot=0.0, side=True):
    """Leg/arm of `segs` boxes; returns names. Pair about x."""
    seg = l / segs
    first = name
    def mk(nm, par, pv, last, mirror_of=None):
        pass
    names = []
    for side_ in ('r', 'l'):
        sx = -1 if side_ == 'r' else 1
        par = parent
        for i in range(segs):
            nm = f'{name}_{i + 1}_{side_}'
            pv = (sx * pivot[0], pivot[1], pivot[2]) if i == 0 else (0, seg, 0)
            mo = f'{name}_{i + 1}_r' if side_ == 'l' else None
            tt = t * (1 - 0.18 * i)
            m.add(nm, par, pv, (rot if i == 0 else -rot * 0.8, 0, 0), (tt, seg + (1 if i == 0 else 0), tt),
                  (-tt / 2, -1 if i == 0 else 0, -tt / 2), mo)
            par = nm
        if foot:
            nm = f'{name}_foot_{side_}'
            mo = f'{name}_foot_r' if side_ == 'l' else None
            m.add(nm, par, (0, seg, 0), (0, 0, 0), (t * 0.95, 1.5, foot), (-t * 0.475, 0, -foot * 0.7), mo)
    return names

def quad(P):
    """Four-legged mammal / reptile / amphibian."""
    g = lambda k, d=0: P.get(k, d)
    bl, bw, bh, ll, lt = g('bl', 20), g('bw', 9), g('bh', 9), g('ll', 9), g('lt', 3)
    m = M(); by = 24 - ll - bh / 2 + g('sink', 0)
    m.add('body', None, (0, by, 0), (0, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    if g('hump'): m.add('body_hump', 'body', (0, -bh / 2, -bl * .2), (0, 0, 0), (bw * .8, g('hump'), bl * .3), (-bw * .4, -g('hump'), 0))
    if g('mane'): m.add('body_mane', 'body', (0, -bh / 2, -bl * .35), (0, 0, 0), (bw * .5, g('mane'), bl * .3), (-bw * .25, -g('mane'), 0))
    if g('armour'): m.add('body_shell', 'body', (0, -bh / 2, 0), (0, 0, 0), (bw + 2, g('armour'), bl - 2), (-(bw + 2) / 2, -g('armour') + 1, -(bl - 2) / 2))
    fz = bl / 2 - lt / 2 - .5; hx = bw / 2 - lt / 2 + g('splay', 0)
    rot = g('legrot', 0)
    limb(m, 'leg_f', 'body', (hx, bh / 2 - 1, -fz), lt, ll + 1, g('segs', 2), rot, g('foot', 0))
    limb(m, 'leg_b', 'body', (hx, bh / 2 - 1, fz), lt * g('bt', 1), ll + 1, g('segs', 2), -rot, g('foot', 0))
    nl, hs, hl = g('nl', 3), g('hs', 6), g('hl', 7)
    nup = g('nup', .5)
    nz = -bl / 2 + 1
    m.add('neck', 'body', (0, -bh / 2 + g('neckdrop', 3), nz), (-nup, 0, 0), (hs * .8, hs * .8, nl + 1), (-hs * .4, -hs * .4, -nl))
    hy = -hs * .4
    m.add('head', 'neck', (0, 0, -nl), (nup * .9, 0, 0), (hs, hs, hl - g('sl', 3)), (-hs / 2, -hs / 2, -(hl - g('sl', 3))))
    if g('sl', 3) > 0:
        sw = hs * g('snw', .55); sh = hs * g('snh', .5)
        m.add('head_snout', 'head', (0, hs * (.5 - g('snh', .5)) / 2 + g('snlow', 0), -(hl - g('sl', 3))), (0, 0, 0), (sw, sh, g('sl', 3)), (-sw / 2, -sh / 2, -g('sl', 3)))
        m.add('jaw', 'head', (0, hs / 2 - 1, -(hl - g('sl', 3)) + 1), (0, 0, 0), (sw * .9, 1.5, g('sl', 3) + hl * .35), (-sw * .45, 0, -g('sl', 3)))
    else:
        m.add('jaw', 'head', (0, hs / 2 - 1, -hl + 1), (0, 0, 0), (hs * .6, 1.5, hl * .5), (-hs * .3, 0, 0))
    if g('ear', 0):
        e = g('ear'); m.pair('ear', 'head', (hs * .35, -hs / 2, hs * -.1), (0, 0, g('earrot', .3)), (e * .6, e, 1), (-e * .3, -e, 0))
    if g('horn', 0):
        h = g('horn'); m.pair('horn', 'head', (hs * .3, -hs / 2, -hs * .2), (g('hornrot', -.3), 0, g('hornsplay', .3)), (g('hornt', 1.5), h, g('hornt', 1.5)), (-.75, -h, -.75))
    if g('tusk', 0):
        h = g('tusk'); m.pair('tusk', 'head', (hs * .3, hs / 2 - 1, -hl + 2), (.5, 0, 0), (1.5, 1.5, h), (-.75, -.75, -h))
    if g('trunk', 0):
        tl = g('trunk'); tw_ = hs * .35
        for i in range(3):
            m.add(f'trunk_{i + 1}', 'head' if i == 0 else f'trunk_{i}', (0, hs * .2 if i == 0 else tl / 3 - 1, -hl + 1 if i == 0 else 0), (.15, 0, 0), (tw_, tl / 3 + 1, tw_), (-tw_ / 2, -.5, -tw_ / 2))
    tl, tt, ts = g('tl', 6), g('tt', 2), g('tseg', 2)
    par = 'body'
    for i in range(ts):
        w_ = max(1.0, tt * (1 - .25 * i)); l_ = tl / ts
        m.add(f'tail_{i + 1}', par, (0, -bh / 2 + 2 + (0 if i == 0 else 0), bl / 2 - 1 if i == 0 else l_ - .5), (g('tailrot', .5) if i == 0 else .15, 0, 0), (w_, w_, l_ + 1), (-w_ / 2, -w_ / 2, 0))
        par = f'tail_{i + 1}'
    if g('tuft'): m.add('tail_tuft', par, (0, 0, tl / ts), (.2, 0, 0), (tt * 1.6, tt * 1.6, g('tuft')), (-tt * .8, -tt * .8, 0))
    return m

def biped_bird(P):
    g = lambda k, d=0: P.get(k, d)
    bl, bw, bh, ll, lt = g('bl', 9), g('bw', 6), g('bh', 6), g('ll', 6), g('lt', 1.5)
    m = M(); pitch = g('pitch', .35)
    by = 24 - ll - bh * .6
    m.add('body', None, (0, by, 0), (pitch, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    limb(m, 'leg', 'body', (bw * .3, bh / 2 - 1, bl * .1), lt, ll + 1, 2, -pitch, g('foot', 3))
    nl, hs = g('nl', 5), g('hs', 3.5)
    m.add('neck', 'body', (0, -bh * .1, -bl / 2 + 1), (-pitch - .6, 0, 0), (hs * .6, hs * .6, nl + 1), (-hs * .3, -hs * .3, -nl))
    m.add('head', 'neck', (0, 0, -nl), (pitch + .6, 0, 0), (hs, hs, hs), (-hs / 2, -hs / 2, -hs))
    bk, bt = g('beak', 3), g('beakt', 1.5)
    m.add('head_beak', 'head', (0, 0, -hs), (g('beakdown', 0), 0, 0), (bt, bt, bk), (-bt / 2, -bt / 2, -bk))
    m.add('jaw', 'head', (0, bt / 2, -hs), (0, 0, 0), (bt * .8, .8, bk * .8), (-bt * .4, 0, -bk * .8))
    if g('crest'): m.add('head_crest', 'head', (0, -hs / 2, -hs * .3), (-.2, 0, 0), (1, g('crest'), hs * .7), (-.5, -g('crest'), 0))
    wl, ww = g('wl', 8), g('ww', 5)
    m.pair('wing', 'body', (bw / 2, -bh / 2 + 1, -bl * .1), (0, 0, g('wingrot', .25)), (wl, .8, ww), (0, 0, -ww / 2))
    tl = g('tl', 4)
    m.add('tail_1', 'body', (0, -bh * .2, bl / 2 - 1), (-.1 - pitch * .5, 0, 0), (g('tw', 4), .8, tl), (-g('tw', 4) / 2, 0, 0))
    return m

def biped_dino(P):
    g = lambda k, d=0: P.get(k, d)
    bl, bw, bh, ll, lt = g('bl', 20), g('bw', 9), g('bh', 11), g('ll', 16), g('lt', 5)
    m = M(); by = 24 - ll - bh * .4
    m.add('body', None, (0, by, 0), (g('pitch', .12), 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    limb(m, 'leg', 'body', (bw / 2 - lt / 2 + .5, bh / 2 - 2, bl * .12), lt, ll + 2, 3, -.45, g('foot', 7))
    if g('arm', 6):
        limb(m, 'arm', 'body', (bw / 2 - 1, bh * .15, -bl / 2 + 3), g('armt', 2), g('arm', 6), 2, .6, 0)
    nl, hs, hl = g('nl', 6), g('hs', 8), g('hl', 12)
    m.add('neck', 'body', (0, -bh * .25, -bl / 2 + 2), (-g('neckrot', .6) - g('pitch', .12), 0, 0), (hs * .7, hs * .75, nl + 2), (-hs * .35, -hs * .35, -nl))
    m.add('head', 'neck', (0, 0, -nl), (g('neckrot', .6) * .9 + g('pitch', .12), 0, 0), (hs, hs, hl * .6), (-hs / 2, -hs / 2, -hl * .6))
    sw = hs * g('snw', .7); sh = hs * g('snh', .55); sl = hl * .4
    m.add('head_snout', 'head', (0, hs * .1, -hl * .6), (0, 0, 0), (sw, sh, sl), (-sw / 2, -sh / 2, -sl))
    m.add('jaw', 'head', (0, hs / 2 - 1, -hl * .6 + 1), (0, 0, 0), (sw * .9, 2, sl + hl * .3), (-sw * .45, 0, -sl))
    if g('horns'):
        for i, (zz, hh) in enumerate(g('horns')):
            m.pair(f'horn_{i + 1}', 'head', (hs * .3 if zz > -3 else hs * .25, -hs / 2 + 1, -hl * .4 - zz), (-.2, 0, .1), (1.6, hh, 1.6), (-.8, -hh, -.8))
    if g('frill'):
        f = g('frill'); m.add('head_frill', 'head', (0, -hs * .3, -1), (-.6, 0, 0), (hs * 1.6, f, 1.5), (-hs * .8, -f, 0))
    if g('crest'): m.add('head_crest', 'head', (0, -hs / 2, -hl * .4), (-.1, 0, 0), (1.2, g('crest'), hl * .5), (-.6, -g('crest'), 0))
    if g('plates'):
        for i in range(g('plates')): m.add(f'plate_{i + 1}', 'body', (0, -bh / 2, -bl / 2 + (i + .5) * bl / g('plates')), (0, 0, 0), (1.2, g('platesz', 6), 3), (-.6, -g('platesz', 6) - (0 if i%2==0 else -1), -1.5))
    tl, tt, ts = g('tl', 20), g('tt', 5), g('tseg', 3)
    par = 'body'
    for i in range(ts):
        w_ = max(1.2, tt * (1 - .28 * i)); l_ = tl / ts
        m.add(f'tail_{i + 1}', par, (0, -bh * .15 if i == 0 else 0, bl / 2 - 1 if i == 0 else l_ - .5), (.1 if i == 0 else .05, 0, 0), (w_, w_ * .9, l_ + 1), (-w_ / 2, -w_ * .45, 0))
        par = f'tail_{i + 1}'
    if g('club'): m.add('tail_club', par, (0, 0, tl / ts), (0, 0, 0), (g('club'), g('club') * .8, g('club')), (-g('club') / 2, -g('club') * .4, 0))
    if g('spikes'): m.pair('tail_spike', par, (1, 0, tl / ts - 1), (0, .3, 0), (1.2, 1.2, g('spikes')), (-.6, -.6, 0))
    return m

def quad_dino(P):
    """Quadrupedal dinosaur: reuse quad with dino cranium defaults."""
    return quad(P)

def serpent(P):
    g = lambda k, d=0: P.get(k, d)
    n, w, l = g('n', 8), g('w', 3), g('l', 28)
    m = M(); par = None; seg = l / n
    for i in range(n):
        ww = w * (0.5 + 0.5 * math.sin(math.pi * (i + 1) / (n + 1))) if g('taper', 1) else w
        ww = max(1.2, ww)
        m.add('body' if i == 0 else f'tail_{i}', par, (0, 24 - ww / 2 if i == 0 else 0, 0 if i == 0 else seg - .5), (0, 0, 0), (ww, ww * .9, seg + 1), (-ww / 2, -ww * .45, 0))
        par = 'body' if i == 0 else f'tail_{i}'
    hs = g('hs', w * 1.1)
    m.add('head', 'body', (0, 0, 0), (0, 0, 0), (hs, hs * .7, hs * 1.4), (-hs / 2, -hs * .35, -hs * 1.4))
    m.add('jaw', 'head', (0, hs * .3, -hs * 1.4 + 1), (0, 0, 0), (hs * .8, 1, hs * 1.1), (-hs * .4, 0, -hs * .1))
    return m

def fish(P):
    g = lambda k, d=0: P.get(k, d)
    bl, bh, bw = g('bl', 16), g('bh', 7), g('bw', 4)
    m = M(); y = 24 - g('lift', 8) - bh / 2
    m.add('body', None, (0, y, 0), (0, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    hl = g('hl', 5)
    m.add('head', 'body', (0, 0, -bl / 2), (0, 0, 0), (bw * .9, bh * .85, hl), (-bw * .45, -bh * .42, -hl))
    if g('snout'): m.add('head_snout', 'head', (0, 0, -hl), (0, 0, 0), (bw * .5, bh * .3, g('snout')), (-bw * .25, -bh * .15, -g('snout')))
    m.add('jaw', 'head', (0, bh * .2, -hl + 1), (0, 0, 0), (bw * .6, 1, hl * .6), (-bw * .3, 0, -hl * .4))
    tl = g('tl', 5); th = g('th', bh * .9)
    m.add('tail_1', 'body', (0, 0, bl / 2 - 1), (0, 0, 0), (bw * .5, bh * .5, tl * .6), (-bw * .25, -bh * .25, 0))
    m.add('tail_2', 'tail_1', (0, 0, tl * .6), (0, 0, 0), (.8, th, tl * .6), (-.4, -th / 2, 0))
    if g('dorsal', 1): m.add('fin_dorsal', 'body', (0, -bh / 2, 0), (0, 0, 0), (.6, g('dorsal', 3), bl * .35), (-.3, -g('dorsal', 3), -bl * .15))
    m.pair('fin_pec', 'body', (bw / 2, bh * .15, -bl * .2), (0, 0, .7), (g('fin', 3), .6, 2.5), (0, 0, 0))
    return m

def flatfish(P):
    g = lambda k, d=0: P.get(k, d)
    bl, bw, bh = g('bl', 18), g('bw', 14), g('bh', 2.5)
    m = M(); m.add('body', None, (0, 24 - bh / 2 - 1, 0), (0, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    m.add('head', 'body', (0, 0, -bl / 2), (0, 0, 0), (bw * .5, bh * 1.4, 4), (-bw * .25, -bh * .7, -4))
    m.add('jaw', 'head', (0, bh * .4, -3), (0, 0, 0), (bw * .3, 1, 3), (-bw * .15, 0, -2))
    m.add('tail_1', 'body', (0, 0, bl / 2 - 1), (0, 0, 0), (1.5, 1.2, g('tl', 6)), (-.75, -.6, 0))
    return m

def ray(P):
    g = lambda k, d=0: P.get(k, d)
    bw, bl = g('bw', 22), g('bl', 14)
    m = M(); m.add('body', None, (0, 24 - 4 - g('lift', 6), 0), (0, 0, 0), (bw * .5, 4, bl), (-bw * .25, -2, -bl / 2))
    m.pair('wing', 'body', (bw * .25, 0, 0), (0, 0, 0), (bw * .38, 1.5, bl * .9), (0, -.75, -bl * .45))
    m.add('head', 'body', (0, 0, -bl / 2), (0, 0, 0), (bw * .3, 3, 3), (-bw * .15, -1.5, -3))
    m.add('jaw', 'head', (0, 1, -3), (0, 0, 0), (bw * .2, .8, 2), (-bw * .1, 0, -1.5))
    par = 'body'
    for i in range(3):
        m.add(f'tail_{i + 1}', par, (0, 0, bl / 2 - 1 if i == 0 else g('tl', 18) / 3), (0, 0, 0), (1.5 - i * .4, 1.2, g('tl', 18) / 3 + 1), (-(1.5 - i * .4) / 2, -.6, 0)); par = f'tail_{i + 1}'
    return m

def eel(P):
    P = dict(P); P.setdefault('n', 10); P.setdefault('w', 3); P.setdefault('l', 34); P.setdefault('lift', 6)
    return serpent(P)

def cetacean(P):
    g = lambda k, d=0: P.get(k, d)
    bl, bw, bh = g('bl', 30), g('bw', 10), g('bh', 10)
    m = M(); m.add('body', None, (0, 24 - bh / 2 - g('lift', 4), 0), (0, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    hl = g('hl', 8)
    m.add('head', 'body', (0, 0, -bl / 2), (0, 0, 0), (bw * .85, bh * .85, hl), (-bw * .425, -bh * .45, -hl))
    if g('beak', 4): m.add('head_snout', 'head', (0, bh * .1, -hl), (0, 0, 0), (bw * .3, bh * .25, g('beak', 4)), (-bw * .15, -bh * .12, -g('beak', 4)))
    m.add('jaw', 'head', (0, bh * .3, -hl + 1), (0, 0, 0), (bw * .5, 1.5, hl * .8 + g('beak', 4) * .6), (-bw * .25, 0, -hl * .6 - g('beak', 4) * .6))
    m.pair('fin_pec', 'body', (bw / 2, bh * .3, -bl * .25), (0, 0, .8), (g('fin', 7), 1, 4), (0, 0, -2))
    m.add('fin_dorsal', 'body', (0, -bh / 2, bl * .1), (.3, 0, 0), (1, g('dorsal', 5), 4), (-.5, -g('dorsal', 5), 0))
    m.add('tail_1', 'body', (0, 0, bl / 2 - 1), (0, 0, 0), (bw * .6, bh * .6, bl * .3), (-bw * .3, -bh * .3, 0))
    m.add('tail_2', 'tail_1', (0, 0, bl * .3), (0, 0, 0), (bw * 1.4, 1.5, bl * .15), (-bw * .7, -.75, 0))
    return m

def pinniped(P):
    g = lambda k, d=0: P.get(k, d)
    bl, bw, bh = g('bl', 22), g('bw', 9), g('bh', 8)
    m = M(); m.add('body', None, (0, 24 - bh / 2, 0), (g('pitch', -.1), 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    m.add('neck', 'body', (0, -bh * .2, -bl / 2 + 1), (-.3, 0, 0), (bw * .7, bh * .7, 4), (-bw * .35, -bh * .35, -4))
    hs = g('hs', 6)
    m.add('head', 'neck', (0, 0, -4), (.3, 0, 0), (hs, hs * .9, hs * .9), (-hs / 2, -hs * .45, -hs * .9))
    m.add('head_snout', 'head', (0, hs * .1, -hs * .9), (0, 0, 0), (hs * .5, hs * .4, 2), (-hs * .25, -hs * .2, -2))
    m.add('jaw', 'head', (0, hs * .4, -hs * .8), (0, 0, 0), (hs * .4, 1, 2), (-hs * .2, 0, -1.5))
    if g('tusk'): m.pair('tusk', 'head', (hs * .2, hs * .4, -hs * .8), (.1, 0, 0), (1, g('tusk'), 1), (-.5, 0, -.5))
    m.pair('arm', 'body', (bw / 2, bh * .3, -bl * .25), (.3, 0, .9), (g('flip', 8), 1.2, 4), (0, -.6, -2))
    m.pair('leg', 'body', (bw * .2, bh * .2, bl / 2 - 2), (0, 0, 0), (4, 1.2, g('flip', 8) * .8), (-2, -.6, 0))
    m.add('tail_1', 'body', (0, 0, bl / 2 - 1), (0, 0, 0), (2, 2, 3), (-1, -1, 0))
    return m

def flyer(P):
    """Bat / pterosaur: quad-ish body with big wing membranes."""
    g = lambda k, d=0: P.get(k, d)
    m = M(); bl, bw, bh = g('bl', 8), g('bw', 5), g('bh', 5)
    m.add('body', None, (0, 24 - g('lift', 3) - bh / 2, 0), (0, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    hs = g('hs', 4); nl = g('nl', 2)
    m.add('neck', 'body', (0, -bh * .2, -bl / 2 + 1), (-.2, 0, 0), (hs * .6, hs * .6, nl + 1), (-hs * .3, -hs * .3, -nl))
    m.add('head', 'neck', (0, 0, -nl), (.2, 0, 0), (hs, hs, g('hl', 5)), (-hs / 2, -hs / 2, -g('hl', 5)))
    if g('crest'): m.add('head_crest', 'head', (0, -hs / 2, -g('hl', 5) * .3), (0, 0, 0), (1, g('crest'), g('hl', 5) * .6), (-.5, -g('crest'), 0))
    m.add('jaw', 'head', (0, hs * .4, -g('hl', 5) + 1), (0, 0, 0), (hs * .6, 1, g('hl', 5) * .7), (-hs * .3, 0, -g('hl', 5) * .5))
    if g('ear'): m.pair('ear', 'head', (hs * .3, -hs / 2, 0), (0, 0, .2), (2, g('ear'), .5), (-1, -g('ear'), 0))
    wl, ww = g('wl', 14), g('ww', 8)
    for i in range(2):
        m.pair(f'wing_{i + 1}', 'body' if i == 0 else None, (bw / 2, -bh * .2, -bl * .15), (0, 0, 0), (wl / 2, .6, ww), (0, 0, -ww / 2)) if i == 0 else None
    m.pair('wing_o', 'wing_1_r', (0, 0, 0), (0, 0, 0), (0.01, 0.01, 0.01), (0, 0, 0)) if False else None
    m.add('wing_2_r', 'wing_1_r', (-wl / 2, 0, 0), (0, 0, 0), (wl / 2, .6, ww * .8), (-wl / 2, 0, -ww * .4))
    m.add('wing_2_l', 'wing_1_l', (wl / 2, 0, 0), (0, 0, 0), (wl / 2, .6, ww * .8), (0, 0, -ww * .4), 'wing_2_r')
    m.pair('leg', 'body', (bw * .3, bh / 2 - 1, bl * .3), (0, 0, 0), (1.5, g('ll', 3), 1.5), (-.75, 0, -.75))
    if g('tl', 0): m.add('tail_1', 'body', (0, 0, bl / 2 - 1), (0, 0, 0), (1.2, 1.2, g('tl')), (-.6, -.6, 0))
    return m

def croc(P):
    P = dict(P)
    P.setdefault('ll', 4); P.setdefault('lt', 3.5); P.setdefault('sink', 1); P.setdefault('splay', 2.5); P.setdefault('nl', 1)
    P.setdefault('nup', .1); P.setdefault('neckdrop', 2); P.setdefault('sl', 8); P.setdefault('hl', 14); P.setdefault('hs', 6)
    P.setdefault('tseg', 4); P.setdefault('tl', 24); P.setdefault('tt', 5); P.setdefault('tailrot', 0); P.setdefault('legrot', .5)
    return quad(P)

def insect(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); ll = g('ll', 6)
    m.add('body', None, (0, 24 - ll - 2, 0), (0, 0, 0), (g('bw', 4), 3.5, g('bl', 6)), (-g('bw', 4) / 2, -1.75, -g('bl', 6) / 2))
    m.add('head', 'body', (0, 0, -g('bl', 6) / 2), (0, 0, 0), (3, 3, 3), (-1.5, -1.5, -3))
    m.add('jaw', 'head', (0, 1.5, -3), (0, 0, 0), (2, .8, 1.5), (-1, 0, -1.5))
    if g('antenna', 1): m.pair('antenna', 'head', (1, -1.5, -2.5), (-.6, 0, .3), (.4, 4, .4), (-.2, -4, -.2))
    m.add('abdomen', 'body', (0, 0, g('bl', 6) / 2 - .5), (.1, 0, 0), (g('bw', 4) * .9, 3.2, g('ab', 8)), (-g('bw', 4) * .45, -1.6, 0))
    for i in range(3):
        m.pair(f'leg_{i + 1}', 'body', (g('bw', 4) / 2, 1, -g('bl', 6) * .25 + i * g('bl', 6) * .25), (0, 0, .7), (.8, ll, .8), (0, 0, -.4))
    if g('wing', 1): m.pair('wing', 'body', (1, -1.75, -.5), (0, 0, .2), (g('wl', 9), .3, g('ww', 4)), (0, 0, -g('ww', 4) / 2))
    return m

def arachnid(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); ll = g('ll', 6)
    m.add('body', None, (0, 24 - ll - 2, 0), (0, 0, 0), (g('bw', 6), 3.5, g('bl', 6)), (-g('bw', 6) / 2, -1.75, -g('bl', 6) / 2))
    m.add('head', 'body', (0, 0, -g('bl', 6) / 2), (0, 0, 0), (4, 3, 3), (-2, -1.5, -3))
    m.add('jaw', 'head', (0, 1.5, -3), (0, 0, 0), (2.5, 1, 1.5), (-1.25, 0, -1.5))
    m.add('abdomen', 'body', (0, 0, g('bl', 6) / 2 - .5), (.1, 0, 0), (g('bw', 6) * 1.1, 4, g('ab', 9)), (-g('bw', 6) * .55, -2, 0))
    if g('tail'):
        par = 'abdomen'
        for i in range(4):
            m.add(f'tail_{i + 1}', par, (0, -1 if i == 0 else 0, g('ab', 9) if i == 0 else 3), (-.6, 0, 0), (1.4 - i * .2, 1.4 - i * .2, 3.5), (-.7, -.7, 0)); par = f'tail_{i + 1}'
    if g('claws', 1): m.pair('arm', 'body', (g('bw', 6) / 2, 0, -g('bl', 6) / 2), (0, -.3, .3), (g('cl', 1.5), 1.5, g('cl', 1.5) * 3), (0, -.75, -g('cl', 1.5) * 3))
    for i in range(4):
        m.pair(f'leg_{i + 1}', 'body', (g('bw', 6) / 2, 1, -g('bl', 6) * .3 + i * g('bl', 6) * .22), (0, 0, .8), (.9, ll, .9), (0, 0, -.45))
    return m

def crab(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); m.add('body', None, (0, 24 - 5, 0), (0, 0, 0), (g('bw', 12), 4, g('bl', 9)), (-g('bw', 12) / 2, -2, -g('bl', 9) / 2))
    m.pair('arm', 'body', (g('bw', 12) / 2, 0, -g('bl', 9) / 2), (0, -.4, 0), (3, 3, 6), (0, -1.5, -6))
    for i in range(4): m.pair(f'leg_{i + 1}', 'body', (g('bw', 12) / 2, 1, -2 + i * 2), (0, 0, .6), (6, 1, 1), (0, -.5, -.5))
    m.add('head', 'body', (0, 0, -g('bl', 9) / 2), (0, 0, 0), (4, 2, 2), (-2, -1, -2))
    m.add('jaw', 'head', (0, 1, -2), (0, 0, 0), (3, .6, 1), (-1.5, 0, -1))
    return m

def cephalopod(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); r = g('r', 8)
    m.add('body', None, (0, 24 - g('lift', 6) - r / 2, 0), (g('tilt', 0), 0, 0), (r, r, r * 1.2), (-r / 2, -r / 2, -r * .6))
    m.add('head', 'body', (0, 0, -r * .6), (0, 0, 0), (r * .8, r * .7, r * .5), (-r * .4, -r * .35, -r * .5))
    m.add('jaw', 'head', (0, r * .3, -r * .5), (0, 0, 0), (2, 1, 2), (-1, 0, -1))
    n = g('arms', 8)
    for i in range(n):
        a = (i - (n - 1) / 2) / n * 2.4
        par = 'head'
        for k in range(3):
            nm = f'arm_{i + 1}_{k + 1}'
            m.add(nm, par, (math.sin(a) * r * .3 if k == 0 else 0, r * .2 if k == 0 else g('al', 8) / 3, -r * .4 if k == 0 else 0), (-.3 if k == 0 else .2, 0, a * .3), (1.6 - k * .4, g('al', 8) / 3 + 1, 1.6 - k * .4), (-.8 + k * .2, 0, -.8 + k * .2)); par = nm
    return m

def jelly(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); r = g('r', 10)
    m.add('body', None, (0, 24 - g('lift', 10) - r / 2, 0), (0, 0, 0), (r, r * .6, r), (-r / 2, -r * .3, -r / 2))
    m.add('head', 'body', (0, 0, 0), (0, 0, 0), (r * .5, 1, r * .5), (-r * .25, r * .3, -r * .25))
    for i in range(6):
        a = i / 6 * 2 * math.pi
        m.add(f'tail_{i + 1}', 'body', (math.cos(a) * r * .35, r * .3, math.sin(a) * r * .35), (0, 0, 0), (.8, g('tl', 14), .8), (-.4, 0, -.4))
    return m

def starfish(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); r = g('r', 5)
    m.add('body', None, (0, 23, 0), (0, 0, 0), (r, 2, r), (-r / 2, -1, -r / 2))
    m.add('head', 'body', (0, 0, 0), (0, 0, 0), (1, 1, 1), (-.5, -1.5, -.5))
    for i in range(5):
        a = i / 5 * 2 * math.pi
        m.add(f'arm_{i + 1}', 'body', (math.sin(a) * r * .4, 0, -math.cos(a) * r * .4), (0, a, 0), (1.8, 1.4, r * 1.1), (-.9, -.7, -r * 1.1))
    return m

def lizard(P):
    P = dict(P); P.setdefault('ll', 3); P.setdefault('lt', 2); P.setdefault('sink', .5); P.setdefault('splay', 1.8); P.setdefault('nl', 1.5)
    P.setdefault('nup', .15); P.setdefault('sl', 3); P.setdefault('tseg', 3); P.setdefault('tailrot', 0); P.setdefault('legrot', .45)
    return quad(P)

def frog(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); bl, bw, bh = g('bl', 8), g('bw', 7), g('bh', 5)
    m.add('body', None, (0, 24 - 3 - bh / 2, 0), (.15, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    hs = g('hs', 6)
    m.add('head', 'body', (0, -bh * .1, -bl / 2), (-.15, 0, 0), (hs, 3.5, 4.5), (-hs / 2, -2, -4.5))
    m.add('jaw', 'head', (0, 1.5, -4), (0, 0, 0), (hs * .8, 1, 3.5), (-hs * .4, 0, -3))
    m.pair('eye', 'head', (hs * .3, -2, -1.5), (0, 0, 0), (2, 1.5, 2), (-1, -1.5, -1))
    limb(m, 'arm', 'body', (bw / 2 - .5, bh / 2 - 1, -bl * .3), 1.6, 3.5, 2, .2, 1.5)
    limb(m, 'leg', 'body', (bw / 2, bh / 2 - 1, bl * .3), 2.2, g('ll', 6), 2, -.7, 3)
    return m

def sea_turtle(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); bl, bw = g('bl', 16), g('bw', 12)
    m.add('body', None, (0, 24 - 4 - g('lift', 3), 0), (0, 0, 0), (bw, 3, bl), (-bw / 2, -1.5, -bl / 2))
    m.add('body_shell', 'body', (0, -1.5, 0), (0, 0, 0), (bw * .85, 4, bl * .85), (-bw * .425, -3.5, -bl * .425))
    m.add('head', 'body', (0, -.5, -bl / 2), (0, 0, 0), (4, 3.5, 5), (-2, -1.75, -5))
    m.add('jaw', 'head', (0, 1.5, -5), (0, 0, 0), (3, .8, 2.5), (-1.5, 0, -2.5))
    m.pair('arm', 'body', (bw / 2, 0, -bl * .25), (0, 0, .3), (g('fl', 10), 1, 4), (0, -.5, -2))
    m.pair('leg', 'body', (bw / 2 - 1, 0, bl * .3), (0, 0, .3), (4, 1, 3), (0, -.5, -1.5))
    m.add('tail_1', 'body', (0, 0, bl / 2), (0, 0, 0), (1.5, 1, 3), (-.75, -.5, 0))
    return m

def marine_reptile(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); bl, bw, bh = g('bl', 30), g('bw', 10), g('bh', 9)
    m.add('body', None, (0, 24 - bh / 2 - g('lift', 4), 0), (0, 0, 0), (bw, bh, bl), (-bw / 2, -bh / 2, -bl / 2))
    nl = g('nl', 4); hs = g('hs', 7); hl = g('hl', 14)
    m.add('neck', 'body', (0, -bh * .1, -bl / 2 + 1), (-.1, 0, 0), (hs * .7, hs * .7, nl + 1), (-hs * .35, -hs * .35, -nl))
    m.add('head', 'neck', (0, 0, -nl), (.1, 0, 0), (hs, hs * .8, hl * .5), (-hs / 2, -hs * .4, -hl * .5))
    m.add('head_snout', 'head', (0, 0, -hl * .5), (0, 0, 0), (hs * .5, hs * .4, hl * .5), (-hs * .25, -hs * .2, -hl * .5))
    m.add('jaw', 'head', (0, hs * .3, -hl * .4), (0, 0, 0), (hs * .5, 1.5, hl * .7), (-hs * .25, 0, -hl * .5))
    m.pair('fin_f', 'body', (bw / 2, bh * .3, -bl * .25), (0, 0, .6), (g('fl', 12), 1.2, 6), (0, -.6, -3))
    m.pair('fin_b', 'body', (bw / 2, bh * .3, bl * .25), (0, 0, .6), (g('fl', 12) * .8, 1.2, 5), (0, -.6, -2.5))
    par = 'body'; tl = g('tl', 14)
    for i in range(3): m.add(f'tail_{i + 1}', par, (0, 0, bl / 2 - 1 if i == 0 else tl / 3), (0, 0, 0), (bw * (.6 - i * .15), bh * (.6 - i * .12), tl / 3 + 1), (-bw * (.6 - i * .15) / 2, -bh * (.6 - i * .12) / 2, 0)); par = f'tail_{i + 1}'
    if g('dorsal'): m.add('fin_dorsal', 'body', (0, -bh / 2, 0), (0, 0, 0), (.8, g('dorsal'), 6), (-.4, -g('dorsal'), 0))
    return m

def trilobite(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); bw, bl = g('bw', 8), g('bl', 12)
    m.add('body', None, (0, 23, 0), (0, 0, 0), (bw, 2, bl), (-bw / 2, -1, -bl / 2))
    m.add('head', 'body', (0, 0, -bl / 2), (0, 0, 0), (bw * 1.05, 2.5, 4), (-bw * .525, -1.25, -4))
    m.add('jaw', 'head', (0, 1, -3.5), (0, 0, 0), (2, .6, 1), (-1, 0, -.5))
    m.add('tail_1', 'body', (0, 0, bl / 2), (0, 0, 0), (bw * .7, 1.6, 3), (-bw * .35, -.8, 0))
    for i in range(3): m.pair(f'leg_{i + 1}', 'body', (bw / 2, 1, -bl * .3 + i * bl * .3), (0, 0, .8), (3, .6, .6), (0, -.3, -.3))
    return m

def ammonite(P):
    g = lambda k, d=0: P.get(k, d)
    m = M(); r = g('r', 10)
    m.add('body', None, (0, 24 - r / 2 - 1, 0), (0, 0, 0), (r * .5, r, r), (-r * .25, -r / 2, -r / 2))
    m.add('head', 'body', (0, r * .2, -r / 2), (0, 0, 0), (r * .4, r * .4, r * .35), (-r * .2, -r * .2, -r * .35))
    m.add('jaw', 'head', (0, r * .2, -r * .3), (0, 0, 0), (2, 1, 2), (-1, 0, -1))
    for i in range(8):
        a = (i - 3.5) / 8 * 2
        m.add(f'tail_{i + 1}', 'head', (math.sin(a) * r * .15, r * .2, -r * .3), (-.4, 0, a * .3), (.9, g('al', 6), .9), (-.45, 0, -.45))
    return m

def archaic_whale(P):
    P = dict(P); P.setdefault('bl', 60); P.setdefault('bw', 8); P.setdefault('bh', 8); P.setdefault('hl', 10); P.setdefault('beak', 6)
    return cetacean(P)

def ratite_like(P):
    P = dict(P); P.setdefault('pitch', .1); P.setdefault('nl', 12); P.setdefault('ll', 14); P.setdefault('lt', 3); P.setdefault('bw', 10); P.setdefault('bh', 10); P.setdefault('bl', 14)
    P.setdefault('wl', 3); P.setdefault('foot', 5)
    return biped_bird(P)

PLANS = {}
def plan(name, fn, **P): PLANS[name] = (fn, P)

# --- mammals (quadruped) ---
plan('mustelid', quad, bl=16, bw=5, bh=5, ll=3.5, lt=2.2, nl=2, hs=4, hl=5, sl=1.5, tl=11, tt=2.5, tseg=2, ear=1.4, nup=.2, neckdrop=1.5)
plan('viverrid', quad, bl=15, bw=5.5, bh=5.5, ll=5, lt=2.2, nl=2, hs=4.5, hl=6.5, sl=3, snw=.45, tl=14, tt=3, tseg=3, ear=2, nup=.3, neckdrop=1.5)
plan('rodent_large', quad, bl=17, bw=9, bh=9, ll=4.5, lt=3, nl=1.5, hs=6, hl=8, sl=2, snw=.7, snh=.7, tl=3, tt=2, tseg=1, ear=1.5, nup=.2)
plan('rodent_small', quad, bl=8, bw=3.5, bh=3.5, ll=2.5, lt=1.4, nl=1, hs=3, hl=4, sl=1.5, snw=.5, tl=7, tt=1.4, tseg=2, ear=1.2, nup=.3, neckdrop=1)
plan('lagomorph', quad, bl=9, bw=4, bh=5, ll=5, lt=1.8, nl=1.2, hs=3.8, hl=5, sl=1.5, tl=1.8, tt=2.4, tseg=1, ear=5, earrot=.1, nup=.5, neckdrop=1.2, segs=2)
plan('primate_ape', quad, bl=11, bw=9, bh=12, ll=10, lt=3.8, nl=1.5, hs=7, hl=7.5, sl=2, snw=.7, snh=.5, tl=0, tseg=1, tt=1, ear=1.8, nup=1.0, neckdrop=1, legrot=.1)
plan('primate_monkey', quad, bl=10, bw=4.5, bh=5, ll=6, lt=1.8, nl=1.5, hs=4.5, hl=5, sl=1.5, tl=14, tt=1.6, tseg=3, ear=1.5, nup=.7, tailrot=.3, neckdrop=1)
plan('primate_small', quad, bl=6, bw=3, bh=3.5, ll=3.5, lt=1.2, nl=1, hs=3.2, hl=3.8, sl=1.2, tl=9, tt=1.3, tseg=3, ear=1.3, nup=.8, tailrot=.2, neckdrop=1)
plan('giraffid', quad, bl=20, bw=9, bh=12, ll=22, lt=3, nl=22, hs=6, hl=10, sl=4, snw=.6, snh=.6, tl=10, tt=1.5, tseg=2, ear=3, horn=3, hornt=1.5, nup=.2, hump=3, neckdrop=0, legrot=0)
plan('tapir', quad, bl=22, bw=11, bh=12, ll=10, lt=4, nl=3, hs=7, hl=11, sl=5, snw=.55, snh=.55, tl=3, tt=2, tseg=1, ear=2.5, nup=.2, trunk=4)
plan('macropod', quad, bl=14, bw=7, bh=10, ll=13, lt=3, nl=4, hs=5, hl=8, sl=4, snw=.5, tl=20, tt=3.5, tseg=3, ear=4, nup=.8, tailrot=.3, legrot=.2, neckdrop=2)
plan('marsupial_stout', quad, bl=16, bw=9, bh=9, ll=5, lt=3.5, nl=1.5, hs=6, hl=7, sl=2.5, snw=.6, tl=2, tt=1.5, tseg=1, ear=2.5, nup=.2)
plan('monotreme', quad, bl=13, bw=8, bh=6, ll=3, lt=2.5, nl=1, hs=4, hl=9, sl=6, snw=.35, snh=.35, tl=3, tt=2, tseg=1, ear=0, nup=.1, mane=3, splay=1)
plan('armadillo', quad, bl=14, bw=9, bh=7, ll=3.5, lt=2.5, nl=1.5, hs=4.5, hl=7, sl=3, snw=.6, snh=.5, tl=12, tt=2.5, tseg=3, ear=2, nup=.2, armour=3)
plan('insectivore', quad, bl=8, bw=4, bh=3.5, ll=2, lt=1.4, nl=1, hs=2.8, hl=6, sl=4, snw=.4, snh=.35, tl=2, tt=1, tseg=1, ear=.8, nup=.1, neckdrop=1)
plan('sloth_tree', quad, bl=13, bw=7, bh=7, ll=7, lt=2.2, nl=1.5, hs=5, hl=5, sl=1, snw=.8, snh=.6, tl=1.5, tt=1.5, tseg=1, ear=0, nup=.4, legrot=.2, splay=1.5, foot=0)
plan('pinniped', pinniped)
plan('dolphin', cetacean, bl=26, bw=8, bh=9, hl=7, beak=4, fin=6, dorsal=5, lift=6)
plan('bat', flyer, bl=4, bw=3, bh=3, hs=3, hl=3.5, wl=14, ww=6, ear=3, ll=2, lift=6)
# --- birds ---
plan('ratite', ratite_like, bl=14, bw=10, bh=10, nl=12, ll=14, hs=4, beak=3, wl=2, tl=3)
plan('raptor', biped_bird, bl=10, bw=6, bh=6, ll=5, lt=1.4, nl=2, hs=4, beak=3, beakdown=.3, wl=10, ww=6, tl=6, pitch=.5, foot=3.5)
plan('waterfowl', biped_bird, bl=11, bw=6, bh=5, ll=4, lt=1.2, nl=7, hs=3.5, beak=3.5, beakt=1.6, wl=8, ww=5, tl=3, pitch=.15, foot=3.5)
plan('wader', biped_bird, bl=10, bw=4.5, bh=5, ll=14, lt=1.1, nl=11, hs=3, beak=7, beakt=1, wl=9, ww=5, tl=2, pitch=.3, foot=3)
plan('galliform', biped_bird, bl=11, bw=7, bh=7, ll=6, lt=1.5, nl=3.5, hs=3.5, beak=2, wl=6, ww=5, tl=6, tw=7, pitch=.3, foot=3, crest=2)
plan('songbird', biped_bird, bl=6, bw=3.5, bh=3.5, ll=3, lt=.8, nl=1.5, hs=2.6, beak=1.8, beakt=1, wl=6, ww=3, tl=4, pitch=.45, foot=1.5)
plan('corvid', biped_bird, bl=9, bw=5, bh=5, ll=4.5, lt=1.2, nl=2, hs=3.6, beak=4, beakt=2, wl=9, ww=5, tl=6, pitch=.45, foot=2.5)
plan('owl', biped_bird, bl=7, bw=6, bh=7, ll=4, lt=1.4, nl=.8, hs=5, beak=1.3, beakt=1.4, beakdown=.4, wl=9, ww=6, tl=5, pitch=.8, foot=2.5)
plan('parrot', biped_bird, bl=8, bw=4.5, bh=5, ll=3, lt=1, nl=1.5, hs=3.5, beak=2.5, beakt=2.2, beakdown=.4, wl=7, ww=4, tl=10, tw=2.5, pitch=.6, foot=2, crest=2)
plan('penguin', biped_bird, bl=9, bw=7, bh=10, ll=3, lt=2, nl=1, hs=3.5, beak=3, beakt=1.3, wl=7, ww=2, wingrot=.5, tl=2, pitch=1.2, foot=3)
plan('seabird', biped_bird, bl=10, bw=5, bh=5, ll=4, lt=1, nl=2.5, hs=3.2, beak=4, beakt=1.2, wl=14, ww=4, tl=4, pitch=.3, foot=3)
plan('pigeon', biped_bird, bl=8, bw=5, bh=5, ll=3.5, lt=1, nl=2, hs=2.8, beak=1.8, beakt=1, wl=7, ww=4, tl=5, pitch=.4, foot=2)
plan('terror_bird', biped_bird, bl=16, bw=10, bh=12, ll=22, lt=3, nl=10, hs=8, beak=9, beakt=4, beakdown=.5, wl=3, ww=2, tl=5, pitch=.25, foot=6)
# --- reptiles etc ---
plan('crocodilian', croc, bl=28, bw=9, bh=6)
plan('lizard', lizard, bl=9, bw=4.5, bh=3.5, hs=3.5, hl=5, tl=12, tt=2.5)
plan('sea_turtle', sea_turtle)
plan('frog', frog)
# --- fish & inverts ---
plan('fish_fusiform', fish, bl=18, bh=7, bw=4, hl=5, tl=6, lift=8)
plan('fish_deep', fish, bl=12, bh=11, bw=4, hl=4, tl=4, dorsal=5, lift=8)
plan('fish_flat', flatfish)
plan('ray', ray)
plan('eel', eel)
plan('syngnathid', serpent, n=6, w=2, l=14, taper=0, hs=3)
plan('fish_primitive', fish, bl=26, bh=6, bw=5, hl=7, snout=5, tl=8, lift=8)
plan('insect_flying', insect)
plan('beetle', insect, bw=5, bl=7, ab=5, wing=0, antenna=1)
plan('scorpion', arachnid, tail=True, bw=5, bl=7, ab=8, cl=2)
plan('crab', crab)
plan('cephalopod', cephalopod)
plan('jellyfish', jelly)
plan('starfish', starfish)
# --- prehistoric ---
plan('theropod', biped_dino, bl=34, bw=14, bh=17, ll=26, lt=8, hs=11, hl=20, arm=5, armt=2, tl=34, tt=9, pitch=.1)
plan('dromaeosaur', biped_dino, bl=14, bw=5, bh=7, ll=12, lt=3, hs=5, hl=10, arm=7, armt=1.5, tl=22, tt=2.5, crest=0, pitch=.15)
plan('sauropod', quad, bl=40, bw=16, bh=18, ll=26, lt=9, nl=40, hs=7, hl=11, sl=3, tl=40, tt=7, tseg=4, ear=0, nup=.5, neckdrop=0, legrot=0)
plan('ceratopsian', quad, bl=34, bw=16, bh=16, ll=12, lt=7, nl=6, hs=14, hl=20, sl=6, snw=.5, snh=.5, tl=14, tt=5, tseg=2, horn=14, hornt=2.5, hornrot=-.5, hornsplay=.2, nup=.1, splay=2)
plan('ankylosaur', quad, bl=32, bw=18, bh=11, ll=9, lt=6, nl=2, hs=10, hl=12, sl=3, tl=26, tt=6, tseg=3, ear=0, nup=.1, armour=5, splay=2)
plan('stegosaur', quad, bl=34, bw=14, bh=14, ll=14, lt=6, nl=4, hs=5, hl=11, sl=6, snw=.5, snh=.45, tl=24, tt=6, tseg=3, ear=0, nup=.1, mane=11)
plan('hadrosaur', biped_dino, bl=34, bw=12, bh=14, ll=22, lt=6, hs=8, hl=18, arm=8, armt=3, tl=30, tt=7, crest=6, snw=.9, snh=.5, pitch=.15)
plan('ornithopod_small', biped_dino, bl=14, bw=5, bh=7, ll=12, lt=3, hs=5, hl=8, arm=5, armt=1.5, tl=20, tt=3, pitch=.2)
plan('pterosaur', flyer, bl=14, bw=6, bh=6, hs=4, hl=16, nl=5, wl=40, ww=12, crest=8, ll=4, lift=4, tl=2)
plan('ichthyosaur', marine_reptile, bl=40, bw=10, bh=11, nl=1, hs=7, hl=22, fl=10, tl=18, dorsal=8)
plan('plesiosaur', marine_reptile, bl=34, bw=14, bh=9, nl=24, hs=6, hl=10, fl=18, tl=10)
plan('mosasaur', marine_reptile, bl=50, bw=9, bh=9, nl=2, hs=8, hl=22, fl=8, tl=34)
plan('synapsid_sail', quad, bl=26, bw=7, bh=8, ll=7, lt=3, nl=2, hs=6, hl=11, sl=4, snw=.5, tl=24, tt=3, tseg=3, ear=0, nup=.1, splay=3, mane=14, legrot=.3, sink=1)
plan('therapsid', quad, bl=18, bw=8, bh=8, ll=7, lt=3, nl=2, hs=6, hl=9, sl=3, tl=8, tt=2.5, tseg=2, ear=0, nup=.2, splay=2, tusk=3)
plan('early_tetrapod', croc, bl=22, bw=10, bh=5, sl=4, hl=9, tl=14)
plan('trilobite', trilobite)
plan('eurypterid', arachnid, tail=True, bw=8, bl=12, ab=14, ll=5, cl=2.5)
plan('ammonite', ammonite)
plan('giant_arthropod', insect, bw=7, bl=24, ab=0, wing=0, ll=5)
plan('archaic_whale', archaic_whale)
plan('placoderm', fish, bl=40, bh=10, bw=9, hl=14, snout=0, tl=10, lift=8)


def extents(m):
    """Bounding box (x, y, z extent in px) of the posed model: hitbox size for the entity type."""
    import numpy as np
    def rot(r):
        rx, ry, rz = r
        cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
        return np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]) @ np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]) @ np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    pts = []
    def walk(n, R, T):
        p = m['parts'][n]; T2 = T + R @ np.array(p['pivot']); R2 = R @ rot(p['rot'])
        for x, y, z, w, h, d, g in p['boxes']:
            c = np.array([[a, b, c_] for a in (x, x + w) for b in (y, y + h) for c_ in (z, z + d)])
            pts.append((R2 @ c.T).T + T2)
        for ch in p['children']: walk(ch, R2, T2)
    for r in m['roots']: walk(r, np.eye(3), np.zeros(3))
    a = np.concatenate(pts)
    return [float(a[:, i].max() - a[:, i].min()) for i in range(3)]

WATER = set('fish_fusiform fish_deep fish_flat ray eel syngnathid fish_primitive dolphin archaic_whale ichthyosaur plesiosaur mosasaur placoderm cephalopod jellyfish ammonite trilobite eurypterid starfish'.split())
FLY = set('bat pterosaur insect_flying raptor waterfowl wader songbird corvid owl parrot seabird pigeon'.split())
SOUND = {  # vanilla sound family per plan; the runtime falls back to generic hurt/death when an event is missing
 'cat': 'mustelid owl', 'fox': 'viverrid', 'pig': 'rodent_large tapir marsupial_stout monotreme', 'rabbit': 'rodent_small lagomorph insectivore',
 'polar_bear': 'primate_ape', 'panda': 'primate_monkey primate_small sloth_tree', 'llama': 'giraffid', 'sheep': 'macropod', 'armadillo': 'armadillo',
 'dolphin': 'pinniped dolphin archaic_whale', 'bat': 'bat pterosaur', 'chicken': 'ratite galliform terror_bird penguin waterfowl wader seabird pigeon songbird corvid parrot raptor',
 'turtle': 'crocodilian lizard sea_turtle', 'frog': 'frog', 'cod': 'fish_fusiform fish_deep fish_flat ray eel syngnathid fish_primitive placoderm jellyfish ammonite starfish trilobite',
 'squid': 'cephalopod', 'spider': 'insect_flying beetle scorpion crab eurypterid giant_arthropod', 'ravager': 'theropod dromaeosaur', 'horse': 'hadrosaur ornithopod_small stegosaur ceratopsian',
 'cow': 'sauropod ankylosaur therapsid synapsid_sail early_tetrapod', 'goat': 'ichthyosaur plesiosaur mosasaur',
}
def meta(name):
    snd = next((k for k, v in SOUND.items() if name in v.split()), 'cow')
    return dict(cls='water' if name in WATER else 'fly' if name in FLY else 'land', sound=snd)

def build(outdir):
    os.makedirs(outdir, exist_ok=True)
    for name, (fn, P) in PLANS.items():
        m = fn(P).out(); m['meta'] = meta(name); ex = extents(m); m['meta']['w'] = round(max(.3, min(max(ex[0], ex[2]) * .8, 24 * 16) / 16), 2); m['meta']['h'] = round(max(.2, ex[1] / 16), 2)
        json.dump(m, open(os.path.join(outdir, name + '.json'), 'w'), separators=(',', ':'))
    return len(PLANS)

if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else 'src/main/resources/data/diaithirfauna/plans'
    print(build(out), 'plans ->', out)
