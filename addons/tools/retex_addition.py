#!/usr/bin/env python3
"""Replaces every texture of diaithir_createaddition with original procedural art (Create port art engine, tools/assetgen).
Run after gen_addition.py. Upstream models keep their geometry; each texture is repainted face by face: for every face that
uses a texture the script works out its pixel rectangle (explicit uv, or the auto uv MC derives from the box) and paints a
tile there, so packed Blockbench sheets stay valid. Textures are written at 4x the upstream size.
Also rewrites create:block/* references (textures the Create port does not ship) to our own."""
import os, sys, re, json, glob, shutil, hashlib
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(HERE, '..', '..', 'tools', 'assetgen')))
import numpy as np
from PIL import Image
import art, icons64
from art import Cv, sh, mixc, STEEL, STEEL_D, STEEL_L, COPPER, BRASS, ZINC, ANDESITE, HAZ_Y, HAZ_K, GLOW, RUBBER, S

NS = 'createaddition'
ROOT = os.path.abspath(os.path.join(HERE, '..', 'createaddition', 'src', 'main', 'resources', 'assets', NS))
TEX = os.path.join(ROOT, 'textures')
K = 4  # output scale versus upstream pixel size

ELECTRUM = (214, 196, 128)
GOLD = (222, 182, 60)
IRON = (150, 154, 160)
COCOA = (92, 54, 36)
HONEYC = (222, 164, 52)
SPONGE = (232, 208, 150)
CREAM = (240, 232, 212)

# ------------------------------------------------------------------------------------------------ tile painters (64x64 RGBA)
def T(img): return img if img.size == (64, 64) else img.resize((64, 64), Image.LANCZOS)

def p_plate(name, base=STEEL, rust=0.0): return art.plate_tex(name, base, True, 0.5, rust)
def p_flat(name, base): return art.metal_flat(name, base, 0.4, 0.0)
def p_face(name, base, variant, accent=HAZ_Y): return art.machine_face(name, base, accent, variant)
def p_top(name, base=STEEL): return art.machine_top(name, base)
def p_casing(name, base, trim=STEEL_D): return art.casing_tex(name, base, trim)

def p_coil(name, wire=COPPER, vertical=True):
    """copper windings on a steel former"""
    c = Cv(sh(STEEL, 0.7))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 0.8), 2.0, True)
    for i in range(11):
        y = 6 + i * 5
        c.cyl_h(5, y, 59, y + 4.2, wire)
        c.rect(5, y + 4.2, 59, y + 5, sh(wire, 0.45))
    c.rect(5, 3, 59, 6, sh(STEEL, 0.5)); c.rect(5, 58, 59, 61, sh(STEEL, 0.5))
    im = c.finish(name, 0.3, 0.0)
    return im if vertical else im.rotate(90)

def p_cell(name, base=STEEL):
    """accumulator side: steel casing with a lit cell window"""
    c = Cv(sh(base, 0.78))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 0.84), 2.2, True)
    c.rivets4(0, 0, 64, 64, 3.2, 1.3, base)
    c.bevel(10, 9, 54, 55, STEEL_D, 1.4, False)
    for i in range(5):
        y = 12 + i * 8.4
        on = i < 3
        c.rect(14, y, 50, y + 5.2, (92, 190, 120) if on else (54, 70, 60))
        c.rect(14, y, 50, y + 1.2, (160, 236, 176) if on else (70, 88, 76), 160)
    c.hazard(10, 57, 54, 60.5, 3.5)
    return c.finish(name, 0.5, 0.05)

def p_gaugetile(name):
    c = Cv(sh(STEEL, 0.8))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 0.85), 2.2, True)
    c.gauge(32, 32, 24)
    return c.finish(name, 0.2, 0.0)

def p_conn(name, plate, mode):
    """connector face: coloured plate with a socket; mode none / push / pull / passive"""
    c = Cv(plate)
    c.bevel(0.5, 0.5, 63.5, 63.5, plate, 3.0, True)
    c.rivets4(0, 0, 64, 64, 4.2, 1.5, plate)
    c.ellipse(16, 16, 48, 48, STEEL_D)
    c.ellipse(19, 19, 45, 45, sh(plate, 0.55))
    c.ellipse(23, 23, 41, 41, (24, 24, 28))
    if mode in ('push', 'pull', 'passive'):
        col = {'push': (230, 90, 60), 'pull': (80, 170, 230), 'passive': (200, 200, 90)}[mode]
        pts = [(32, 25), (39, 36), (25, 36)] if mode == 'push' else [(25, 28), (39, 28), (32, 39)]
        if mode == 'passive':
            c.rect(26, 29, 38, 35, col)
        else:
            c.poly(pts, col)
    return c.finish(name, 0.25, 0.0)

def p_bulb(name, on):
    c = Cv(sh(STEEL, 0.6))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 0.7), 2.0, True)
    if on:
        c.ellipse(10, 10, 54, 54, GLOW, 90)
        c.lamp(32, 32, 16, GLOW)
    else:
        c.ellipse(15, 15, 49, 49, STEEL_D)
        c.ellipse(18, 18, 46, 46, (70, 66, 54))
        c.ellipse(22, 20, 34, 30, (110, 104, 86))
    return c.finish(name, 0.2, 0.0, 0.02, 0.0)

def p_relay_stone(name, on):
    c = Cv(None)
    base = (134, 132, 128)
    c = Cv(base)
    c.bevel(0.5, 0.5, 63.5, 63.5, base, 3.0, True)
    c.rect(26, 26, 38, 38, (214, 38, 28) if on else (92, 24, 22))
    if on: c.rect(22, 22, 42, 42, (255, 70, 50), 70)
    return c.finish(name, 0.5, 0.0)

def p_barbed(name):
    r = art.rng(name)
    c = Cv(None, True)
    for yy in (14, 32, 50):
        c.line(0, yy, 64, yy, (92, 94, 100), 2.0)
        c.line(0, yy - 0.6, 64, yy - 0.6, (170, 172, 180), 0.8)
    for xx in range(6, 64, 14):
        for yy in (14, 32, 50):
            c.line(xx - 4, yy - 4, xx + 4, yy + 4, (120, 122, 130), 1.4)
            c.line(xx - 4, yy + 4, xx + 4, yy - 4, (120, 122, 130), 1.4)
    return c.finish(name, 0.0, 0.1, 0.0, 0.0, keep_alpha=True)

def p_cake(name, part, frosting, sponge=SPONGE):
    r = art.rng(name)
    c = Cv(sponge if part in ('side', 'inner', 'bottom') else frosting)
    if part == 'top':
        for i in range(30):
            x, y = r.uniform(4, 60), r.uniform(4, 60)
            c.ellipse(x - 2, y - 1.5, x + 2, y + 1.5, sh(frosting, 1.0 + r.uniform(-0.12, 0.18)))
        c.outline_rect(0, 0, 64, 64, sh(frosting, 0.7), 2.0, 255)
    elif part == 'side':
        c.rect(0, 0, 64, 12, frosting)
        for x in range(0, 64, 10): c.ellipse(x, 6, x + 9, 20, frosting)
        c.rect(0, 28, 64, 32, sh(frosting, 0.9)); c.rect(0, 28, 64, 29.2, sh(frosting, 1.2))
        for i in range(40): c.rect(r.uniform(0, 62), r.uniform(34, 62), 0, 0, sponge) if False else None
    elif part == 'inner':
        c.rect(0, 0, 64, 10, frosting)
        c.rect(0, 28, 64, 32, sh(frosting, 0.9))
        for i in range(60):
            x, y = r.uniform(0, 62), r.uniform(34, 62)
            c.rect(x, y, x + 2, y + 2, sh(sponge, 0.88 + r.uniform(0, 0.2)))
    else:
        c.rect(0, 0, 64, 64, sh(sponge, 0.78))
        c.outline_rect(0, 0, 64, 64, sh(sponge, 0.55), 3.0, 255)
    return c.finish(name, 0.0, 0.0, 0.05, 0.0)

def p_pellet(name, part):
    r = art.rng(name)
    base = (104, 126, 66) if part != 'top' else (116, 138, 74)
    c = Cv(sh(base, 0.55))
    for row in range(8):
        for col in range(8):
            x, y = col * 8 + (4 if row % 2 else 0), row * 8
            c.bevel(x + 0.5, y + 0.5, x + 7.5, y + 7.5, sh(base, 0.9 + r.uniform(-0.1, 0.12)), 1.0, True, False)
    return c.finish(name, 0.2, 0.0, 0.05, 0.0)

def p_electrum(name):
    c = Cv(ELECTRUM)
    c.bevel(0.5, 0.5, 63.5, 63.5, ELECTRUM, 3.0, True)
    for (x0, y0, x1, y1) in ((6, 6, 31, 31), (33, 6, 58, 31), (6, 33, 31, 58), (33, 33, 58, 58)):
        c.bevel(x0, y0, x1, y1, sh(ELECTRUM, 1.05), 1.4, True)
    return c.finish(name, 0.1, 0.0, 0.05, 0.15)

def p_shaft(name):
    return art.shaft_end(name, STEEL)

def p_creative(name, part):
    base = (176, 168, 190)
    c = Cv(sh(base, 0.8))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(base, 0.85), 2.4, True)
    c.rivets4(0, 0, 64, 64, 3.2, 1.3, base)
    c.ellipse(16, 16, 48, 48, (40, 30, 70))
    c.ellipse(19, 19, 45, 45, (120, 80, 220))
    pts = [(36, 18), (26, 34), (33, 34), (28, 46), (40, 29), (33, 29)]
    c.poly(pts, (255, 232, 110))
    return c.finish(name, 0.2, 0.0)

def p_lightning(name, frames=8):
    sheet = Image.new('RGBA', (64, 64 * frames), (0, 0, 0, 0))
    for f in range(frames):
        r = art.rng(name + str(f))
        c = Cv(None, True)
        x = 32 + r.uniform(-4, 4)
        for y in range(0, 64, 8):
            nx = min(54, max(10, x + r.uniform(-10, 10)))
            c.line(x, y, nx, y + 8, (150, 200, 255), 3.0, 90)
            c.line(x, y, nx, y + 8, (235, 245, 255), 1.4)
            x = nx
        sheet.paste(c.finish(name + str(f), 0.0, 0.0, 0.0, 0.0, keep_alpha=True), (0, 64 * f))
    return sheet

def p_tesla(name):
    return p_coil(name, (176, 98, 62), True)

# texture name -> painter returning an image per role {up, down, side}
def style_for(tex):
    n = tex.split('/', 1)[1] if '/' in tex else tex
    # n like block/connector/copper_push
    leaf = n.split('/')[-1]
    sheet_like = {'lightning_new': 'lightning'}
    if leaf in sheet_like: return ('lightning', None)
    if n.startswith('block/connector/'):
        if leaf == 'bulb': return ('bulb', True)
        if leaf == 'bulb_off': return ('bulb', False)
        plate = COPPER if leaf.startswith('copper') or leaf == 'connector' else (GOLD if leaf.startswith('gold') else IRON)
        if leaf.startswith('large'): plate = (84, 88, 100)
        mode = 'push' if leaf.endswith('push') else 'pull' if leaf.endswith('pull') else 'passive' if leaf.endswith('passive') else 'none'
        return ('conn', (plate, mode))
    if n.startswith('block/redstone_relay'): return ('relay', leaf.endswith('_on'))
    if n.startswith('block/barbed_wire'): return ('barbed', None)
    if n.startswith('block/electrum_block'): return ('electrum', None)
    if n.startswith('block/biomass_pellet_block'): return ('pellet', leaf)
    if n.startswith('block/chocolate_cake'): return ('cake', (leaf, COCOA, (160, 104, 72)))
    if n.startswith('block/honey_cake'): return ('cake', (leaf, HONEYC, SPONGE))
    if n.startswith('block/rolling_mill'): return ('casing', ANDESITE)
    if n.startswith('block/modular_accumulator'): return ('gauge' if leaf.startswith('gauge') else 'cell', None)
    if n.startswith('block/accumulator'): return ('cell', None)
    if n.startswith('block/alternator'): return ('coilmach', None)
    if n.startswith('block/electric_motor'): return ('motor', leaf)
    if n.startswith('block/tesla_coil'): return ('tesla', None)
    if leaf in ('axis', 'axis_top'): return ('shaft', None)
    if leaf in ('fan_side',): return ('casing', ANDESITE)
    if leaf in ('girder',): return ('plate', (STEEL, 0.0))
    if leaf in ('crate_creative', 'creative_fluid_tank'): return ('creative', None)
    return ('plate', (STEEL, 0.0))

def paint(style, arg, role, name):
    if style == 'conn': return p_conn(name, *arg)
    if style == 'bulb': return p_bulb(name, arg)
    if style == 'relay': return p_relay_stone(name, arg)
    if style == 'barbed': return p_barbed(name)
    if style == 'electrum': return p_electrum(name)
    if style == 'pellet': return p_pellet(name, arg)
    if style == 'cake': return p_cake(name, arg[0], arg[1], arg[2])
    if style == 'casing': return p_casing(name, arg)
    if style == 'cell': return p_cell(name) if role == 'side' else p_top(name, STEEL)
    if style == 'gauge': return p_gaugetile(name)
    if style == 'coilmach': return p_coil(name, COPPER, True) if role == 'side' else p_top(name, STEEL)
    if style == 'motor':
        if arg == 'coil': return p_coil(name, (120, 134, 150), True)
        if arg.endswith('back'): return p_face(name, (84, 90, 100), 1)
        if arg.endswith('sides'): return p_face(name, (84, 90, 100), 4)
        return p_face(name, (84, 90, 100), 0) if role == 'side' else p_top(name, (84, 90, 100))
    if style == 'tesla': return p_tesla(name) if role == 'side' else p_top(name, COPPER)
    if style == 'shaft': return p_shaft(name)
    if style == 'creative': return p_creative(name, role)
    if style == 'plate': return p_plate(name, arg[0], arg[1])
    return p_plate(name)

# ------------------------------------------------------------------------------------------------ model scan
def load_model(path):
    with open(path) as f: return json.load(f)

def resolve_model(ref):
    """createaddition:block/x -> json path"""
    if ':' in ref: ns, p = ref.split(':', 1)
    else: ns, p = 'minecraft', ref
    if ns != NS: return None
    fp = os.path.join(ROOT, 'models', p + '.json')
    return fp if os.path.exists(fp) else None

def flatten(path, depth=0):
    j = load_model(path)
    texs, elems, size = {}, j.get('elements'), j.get('texture_size')
    par = j.get('parent')
    if par and depth < 8:
        pp = resolve_model(par)
        if pp:
            pt, pe, ps = flatten(pp, depth + 1)
            texs.update(pt)
            if elems is None: elems = pe
            if size is None: size = ps
    texs.update(j.get('textures', {}))
    return texs, elems, size

def tex_of(texs, key):
    seen = 0
    while isinstance(key, str) and key.startswith('#') and seen < 10:
        key = texs.get(key[1:]); seen += 1
    return key

def auto_uv(face, f, t):
    x0, y0, z0 = f; x1, y1, z1 = t
    return {'down': (x0, 16 - z1, x1, 16 - z0), 'up': (x0, z0, x1, z1), 'north': (16 - x1, 16 - y1, 16 - x0, 16 - y0),
            'south': (x0, 16 - y1, x1, 16 - y0), 'west': (z0, 16 - y1, z1, 16 - y0), 'east': (16 - z1, 16 - y1, 16 - z0, 16 - y0)}[face]

def scan():
    """texture ref -> {'size': (w, h), 'faces': [(uv, role)]}"""
    out = {}
    for p in glob.glob(os.path.join(ROOT, 'models', '**', '*.json'), recursive=True):
        texs, elems, size = flatten(p)
        if not elems: continue
        for e in elems:
            for fname, fd in e.get('faces', {}).items():
                tr = tex_of(texs, fd.get('texture'))
                if not tr: continue
                tr = tr.split(':', 1)[-1] if tr.startswith(NS + ':') else tr
                ts = size or [16, 16]
                uv = fd.get('uv') or [v * ts[0] / 16 if i % 2 == 0 else v * ts[1] / 16 for i, v in enumerate(auto_uv(fname, e['from'], e['to']))]
                role = 'up' if fname == 'up' else 'down' if fname == 'down' else 'side'
                d = out.setdefault(tr, {'size': tuple(ts), 'faces': []})
                d['faces'].append((tuple(uv), role))
    return out

def run():
    info = scan()
    # rewrite create:block/* references to ours
    ext = {}
    for p in glob.glob(os.path.join(ROOT, 'models', '**', '*.json'), recursive=True):
        s = open(p).read()
        def sub(m):
            leaf = m.group(1)
            ext[leaf] = True
            return f'"{NS}:block/ext/{leaf}"'
        s2 = re.sub(r'"create:block/([A-Za-z0-9_/]+)"', sub, s)
        if s2 != s: open(p, 'w').write(s2)
    info = scan()
    made = 0
    for tr, d in sorted(info.items()):
        if not tr.startswith('block/') and not tr.startswith('entity/'): continue
        if tr.startswith('entity/'): continue
        w, h = d['size']
        style, arg = style_for(tr)
        outp = os.path.join(TEX, tr + '.png')
        os.makedirs(os.path.dirname(outp), exist_ok=True)
        if style == 'lightning':
            p_lightning(tr).save(outp); made += 1; continue
        canvas = Image.new('RGBA', (w * K, h * K), (0, 0, 0, 0))
        cache = {}
        for uv, role in d['faces']:
            u0, v0, u1, v1 = uv
            x0, x1 = sorted((u0, u1)); y0, y1 = sorted((v0, v1))
            bx0, by0 = int(round(x0 * K)), int(round(y0 * K))
            bx1, by1 = int(round(x1 * K)), int(round(y1 * K))
            if bx1 <= bx0 or by1 <= by0: continue
            if role not in cache: cache[role] = paint(style, arg, role, tr + role)
            tile = cache[role].resize((bx1 - bx0, by1 - by0), Image.LANCZOS)
            canvas.alpha_composite(tile, (bx0, by0)) if style == 'barbed' else canvas.paste(tile, (bx0, by0))
        if style != 'barbed' and canvas.getbbox() is None: canvas = paint(style, arg, 'side', tr).resize((w * K, h * K))
        canvas.save(outp); made += 1
    # unreferenced block textures from upstream are dropped; keep only what models use
    used = {tr + '.png' for tr in info}
    for p in glob.glob(os.path.join(TEX, 'block', '**', '*.png'), recursive=True):
        rel = os.path.relpath(p, TEX).replace(os.sep, '/')
        if rel not in used: os.remove(p)
    for p in glob.glob(os.path.join(TEX, 'block', '**', '*.mcmeta'), recursive=True):
        if not os.path.exists(p[:-7]): os.remove(p)
    shutil.rmtree(os.path.join(TEX, 'entity'), ignore_errors=True)
    shutil.rmtree(os.path.join(TEX, 'fluid'), ignore_errors=True)
    shutil.rmtree(os.path.join(TEX, 'mob_effect'), ignore_errors=True)
    print('block textures', made, 'ext refs', sorted(ext))
    items()

# ------------------------------------------------------------------------------------------------ items
def i_wire(color, name):
    c = Cv(None, True)
    pts = [(10, 46), (18, 30), (26, 44), (34, 28), (42, 42), (50, 26)]
    for (a, b) in zip(pts, pts[1:]):
        c.line(a[0], a[1] + 1.5, b[0], b[1] + 1.5, sh(color, 0.45), 4.0)
    for (a, b) in zip(pts, pts[1:]):
        c.line(a[0], a[1], b[0], b[1], color, 3.4)
        c.line(a[0] - 0.5, a[1] - 0.8, b[0] - 0.5, b[1] - 0.8, sh(color, 1.45), 1.2)
    return icons64._finish(c)

def i_rod(color, name):
    c = Cv(None, True)
    c.poly([(12, 50), (48, 14), (54, 20), (18, 56)], sh(color, 0.62))
    c.poly([(12, 50), (48, 14), (51, 17), (15, 53)], color)
    c.line(14, 49, 47, 16, sh(color, 1.55), 1.4)
    c.ellipse(46, 12, 55, 21, sh(color, 1.1)); c.ellipse(47, 13, 52, 18, sh(color, 1.5))
    return icons64._finish(c)

def i_spool(wire, name, festive=False, empty=False):
    c = Cv(None, True)
    wood = (150, 110, 70)
    c.ellipse(14, 40, 50, 56, sh(wood, 0.6)); c.rect(14, 22, 50, 48, sh(wood, 0.85)); c.ellipse(14, 12, 50, 28, sh(wood, 1.12))
    if not empty:
        for i in range(6):
            y = 25 + i * 3.4
            c.rect(19, y, 45, y + 3.0, sh(wire, 0.8 + 0.05 * (i % 2))); c.rect(19, y, 45, y + 0.9, sh(wire, 1.4))
    else:
        c.rect(26, 24, 38, 46, sh(wood, 0.6))
    c.ellipse(28, 17, 36, 23, sh(wood, 0.5))
    if festive:
        for i, col in enumerate(((220, 50, 50), (60, 200, 80), (60, 100, 230))):
            c.ellipse(18 + i * 12, 30 + (i % 2) * 6, 24 + i * 12, 36 + (i % 2) * 6, col)
    return icons64._finish(c)

def i_capacitor(color, name):
    c = Cv(None, True)
    c.cyl_v(24, 14, 42, 50, (70, 86, 124))
    c.ellipse(24, 10, 42, 18, (150, 160, 180)); c.ellipse(24, 46, 42, 54, (60, 72, 104))
    c.rect(24, 30, 42, 36, (230, 200, 70))
    c.rect(28, 50, 30, 60, (170, 172, 180)); c.rect(36, 50, 38, 60, (170, 172, 180))
    return icons64._finish(c)

def i_amulet(color, name):
    c = Cv(None, True)
    c.line(32, 6, 16, 28, (170, 170, 176), 1.6); c.line(32, 6, 48, 28, (170, 170, 176), 1.6)
    c.ellipse(16, 26, 48, 58, sh(ELECTRUM, 0.6)); c.ellipse(17, 26, 47, 55, ELECTRUM)
    c.ellipse(24, 33, 40, 49, (90, 160, 230)); c.ellipse(26, 34, 33, 41, (200, 230, 255))
    c.poly([(34, 36), (29, 44), (33, 44), (30, 52), (38, 42), (34, 42)], (255, 236, 120))
    return icons64._finish(c)

def i_biomass(color, name, pellet=False):
    r = art.rng(name)
    c = Cv(None, True)
    if pellet:
        for (cx, cy) in ((24, 40), (38, 38), (31, 28)):
            c.cyl_h(cx - 8, cy - 5, cx + 8, cy + 5, (112, 134, 70))
            c.ellipse(cx - 9, cy - 5, cx - 5, cy + 5, (84, 102, 52)); c.ellipse(cx + 5, cy - 5, cx + 9, cy + 5, (126, 148, 80))
    else:
        for i in range(26):
            x, y = r.uniform(12, 50), r.uniform(18, 50)
            col = (96 + r.randint(-16, 24), 124 + r.randint(-16, 20), 56 + r.randint(-10, 14))
            c.ellipse(x - 5, y - 3, x + 5, y + 3, col)
    return icons64._finish(c)

def i_grit(color, name):
    r = art.rng(name)
    c = Cv(None, True)
    for i in range(34):
        x, y = r.uniform(14, 50), r.uniform(26, 52)
        c.poly([(x, y - 3), (x + 3, y), (x, y + 3), (x - 3, y)], (150 + r.randint(0, 50), 224, 230))
    return icons64._finish(c)

def i_straw(color, name):
    c = Cv(None, True)
    c.poly([(14, 52), (46, 12), (52, 18), (20, 58)], (230, 214, 150))
    c.poly([(14, 52), (46, 12), (49, 15), (17, 55)], (246, 232, 176))
    for i in range(5):
        c.line(16 + i * 7, 50 - i * 8.5, 20 + i * 7, 54 - i * 8.5, (186, 160, 90), 1.2)
    return icons64._finish(c)

def i_cakeitem(frosting, sponge, name):
    c = Cv(None, True)
    c.ellipse(10, 38, 54, 54, sh(sponge, 0.7)); c.rect(10, 28, 54, 46, sponge); c.rect(10, 28, 54, 36, frosting)
    for x in range(10, 54, 9): c.ellipse(x, 30, x + 8, 42, frosting)
    c.ellipse(10, 20, 54, 38, sh(frosting, 1.12))
    c.ellipse(28, 22, 36, 30, (200, 40, 50))
    return icons64._finish(c)

def i_dough(color, name): return icons64.dough((226, 204, 152), name)

ITEMS = {
    'electrum_ingot': lambda n: icons64.ingot(ELECTRUM, n), 'electrum_nugget': lambda n: icons64.nugget(ELECTRUM, n),
    'electrum_sheet': lambda n: icons64.sheet(ELECTRUM, n), 'zinc_sheet': lambda n: icons64.sheet(ZINC, n),
    'copper_wire': lambda n: i_wire(COPPER, n), 'iron_wire': lambda n: i_wire(IRON, n), 'gold_wire': lambda n: i_wire(GOLD, n), 'electrum_wire': lambda n: i_wire(ELECTRUM, n),
    'copper_rod': lambda n: i_rod(COPPER, n), 'iron_rod': lambda n: i_rod(IRON, n), 'gold_rod': lambda n: i_rod(GOLD, n),
    'electrum_rod': lambda n: i_rod(ELECTRUM, n), 'brass_rod': lambda n: i_rod(BRASS, n),
    'spool': lambda n: i_spool(COPPER, n, empty=True), 'copper_spool': lambda n: i_spool(COPPER, n), 'gold_spool': lambda n: i_spool(GOLD, n),
    'electrum_spool': lambda n: i_spool(ELECTRUM, n), 'festive_spool': lambda n: i_spool((70, 150, 70), n, festive=True),
    'capacitor': lambda n: i_capacitor(None, n), 'electrum_amulet': lambda n: i_amulet(None, n),
    'biomass': lambda n: i_biomass(None, n), 'biomass_pellet': lambda n: i_biomass(None, n, True),
    'diamond_grit': lambda n: i_grit(None, n), 'diamond_grit_sandpaper': lambda n: icons64.sandpaper((120, 214, 224), n),
    'straw': lambda n: i_straw(None, n), 'cake_base': lambda n: icons64.dough((226, 204, 152), n),
    'cake_base_baked': lambda n: i_cakeitem(SPONGE, sh(SPONGE, 0.9), n), 'chocolate_cake': lambda n: i_cakeitem(COCOA, SPONGE, n),
    'honey_cake': lambda n: i_cakeitem(HONEYC, SPONGE, n),
    # block items drawn flat in the inventory (the 3D models are used where a block model exists)
    'large_connector': lambda n: i_wire((84, 88, 100), n), 'small_light_connector': lambda n: i_wire(GOLD, n),
    'connector': lambda n: i_wire(COPPER, n),
}

def items():
    d = os.path.join(TEX, 'item')
    shutil.rmtree(d, ignore_errors=True)
    os.makedirs(d, exist_ok=True)
    n = 0
    for name, fn in ITEMS.items():
        fn(name).save(os.path.join(d, name + '.png')); n += 1
    print('item textures', n)


def fill_missing():
    miss = set()
    for p in glob.glob(os.path.join(ROOT, 'models', '**', '*.json'), recursive=True):
        for v in load_model(p).get('textures', {}).values():
            if v.startswith('#'): continue
            ns, _, t = v.rpartition(':')
            if ns in ('', NS) and t.startswith('block/') and not os.path.exists(os.path.join(TEX, t + '.png')): miss.add(t)
    for t in miss:
        s, a = style_for(t)
        os.makedirs(os.path.dirname(os.path.join(TEX, t + '.png')), exist_ok=True)
        paint(s, a, 'side', t).save(os.path.join(TEX, t + '.png'))
    print('filled', sorted(miss))

if __name__ == '__main__':
    run(); fill_missing()
