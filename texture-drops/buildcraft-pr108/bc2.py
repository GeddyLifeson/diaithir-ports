import sys, os, re, csv, math, random, hashlib, colorsys
import numpy as np
from PIL import Image, ImageDraw
OUT = sys.argv[1]; MAN = '/tmp/bc_manifest.csv'
def rr(n): return random.Random(int(hashlib.md5(n.encode()).hexdigest()[:8], 16))
def hue(name, s=.55, v=.8):
    h = int(hashlib.md5(name.encode()).hexdigest()[:6], 16) % 360; return tuple(int(c * 255) for c in colorsys.hsv_to_rgb(h / 360, s, v))
DYE = {'white': (240, 240, 240), 'orange': (240, 140, 40), 'magenta': (200, 80, 200), 'light_blue': (110, 170, 240), 'yellow': (240, 220, 60), 'lime': (130, 210, 50), 'pink': (240, 150, 180), 'gray': (90, 90, 90), 'light_gray': (170, 170, 170), 'cyan': (40, 150, 170), 'purple': (130, 60, 190), 'blue': (50, 70, 190), 'brown': (110, 70, 40), 'green': (80, 120, 40), 'red': (190, 50, 50), 'black': (25, 25, 30)}
def dye_in(s):
    for k in sorted(DYE, key=len, reverse=True):
        if re.search(r'(^|_)' + k + r'(_|$)', s): return DYE[k]
    return None
MACH = {'quarry': (206, 168, 56), 'pump': (64, 110, 190), 'mining_well': (150, 150, 156), 'builder': (190, 112, 60), 'architect': (60, 150, 160), 'library': (120, 90, 150), 'filler': (170, 170, 60), 'marker': (200, 60, 50), 'replacer': (60, 160, 110),
        'requester': (70, 150, 200), 'zone_planner': (60, 160, 90), 'chute': (150, 154, 160), 'distiller': (170, 150, 120), 'heat_exchange': (200, 100, 60), 'refinery': (150, 120, 70), 'tank': (180, 200, 210), 'flood_gate': (100, 110, 120), 'hopper_block': (110, 114, 120),
        'mj_dynamo': (230, 90, 60), 'engine': (170, 150, 120), 'laser': (220, 60, 60), 'table': (120, 128, 140), 'programming': (80, 120, 180), 'integration': (150, 90, 170), 'advanced_crafting': (160, 120, 80), 'charging': (230, 190, 60), 'assembly': (70, 150, 170),
        'auto_workbench_item': (170, 130, 76), 'plain_pipe': (190, 192, 198), 'tube': (190, 192, 198), 'frame': (210, 170, 60), 'gel': (90, 210, 140), 'filtered_buffer': (130, 100, 70), 'leather': (150, 90, 50), 'paper': (236, 232, 214), 'creative': (170, 90, 200), 'wood': (150, 108, 64), 'stone': (130, 132, 136), 'iron': (190, 192, 198), 'fe': (230, 90, 60)}
def mach_col(path):
    for p in reversed(path.split('/')):
        if p in MACH: return MACH[p]
    for k, v in MACH.items():
        if k in path: return v
    return (150, 154, 162)
def plate(c, r):
    a = np.zeros((64, 64, 3), np.float32); a[:] = c; a *= np.linspace(1.1, .9, 64)[None, :, None]; a += np.array([[r.uniform(-4, 4)] * 3 for _ in range(4096)]).reshape(64, 64, 3); return a
def edge(a): a[0:2] *= 1.2; a[:, 0:2] *= 1.1; a[62:] *= .6; a[:, 62:] *= .72; return a
def box(a, x0, y0, x1, y1, c, line=None):
    c = np.asarray(c, np.float32); a[y0:y1, x0:x1] = c
    if line is not None: line = np.asarray(line, np.float32); a[y0:y0 + 1, x0:x1] = line; a[y1 - 1:y1, x0:x1] = line * .55; a[y0:y1, x0:x0 + 1] = line; a[y0:y1, x1 - 1:x1] = line * .55
def disc(a, cx, cy, rad, col):
    yy, xx = np.ogrid[:64, :64]; a[(xx - cx) ** 2 + (yy - cy) ** 2 <= rad * rad] = col
def rgba(a, alpha=None): return np.dstack([np.clip(a, 0, 255), np.full((64, 64), 255, np.float32) if alpha is None else alpha]).astype(np.uint8)
def face(path, name):
    r = rr(path); col = mach_col(path); a = plate((90, 94, 102) if name not in ('front', 'default') else (110, 114, 122), r); acc = np.array(col, np.float32); dark = np.array([40, 42, 48], np.float32)
    led = re.match(r'led_(green|red)(_(on|off|\d_\d))?', name)
    if name.startswith('led') or '_led' in name:
        c = (60, 220, 90) if 'green' in name else (230, 60, 50) if 'red' in name else (230, 190, 60); a[:] = (40, 42, 48); box(a, 12, 12, 52, 52, np.array(c) * (.35 if 'off' in name or name.endswith('_0_3') else 1), (255, 255, 255)); return rgba(edge(a))
    if name in ('front', 'front_on', 'front_off', 'default', 'display', 'center', 'middle'):
        box(a, 8, 8, 56, 56, dark, plate((120, 124, 132), r)[8, 8]); on = 'off' not in name
        box(a, 14, 14, 50, 50, acc * (1 if on else .4), np.minimum(acc * 1.3, 255)); a[30:34, 14:50] = np.minimum(acc * 1.4, 255) if on else dark; a[14:50, 30:34] = dark * 1.3
    elif name in ('back', 'left', 'right'):
        for y in range(10, 54, 6): a[y:y + 2, 8:56] = dark
        box(a, 6, 6, 58, 12, acc * .8, np.minimum(acc, 255))
    elif name in ('bottom',): a *= .8; a[::16] *= .6; a[:, ::16] *= .6
    elif name in ('top', 'tank_sprite_a', 'end'):
        box(a, 6, 6, 58, 58, plate(col, r)[10, 10] * .8, acc); disc(a, 32, 32, 12, dark); disc(a, 32, 32, 8, acc * .8)
    elif name in ('side', 'side2', 'side_alt', 'side_joined_below'):
        a[:, 0:6] = acc * .9; a[:, 58:] = acc * .9
        for y in (12, 52):
            for x in (10, 52): disc(a, x, y, 2, (60, 62, 70))
    elif name in ('drill', 'laser'): a[:] = dark; box(a, 22, 6, 42, 58, acc, np.minimum(acc * 1.3, 255)); a[6:20, 28:36] = (240, 240, 240)
    elif name in ('item_hatch', 'hatch'): box(a, 10, 10, 54, 54, dark, (120, 124, 130)); a[30:34, 14:50] = acc
    elif name in ('open', 'closed'): a[:] = dark; a[20:44, 8:56] = (70, 140, 220) if name == 'open' else (150, 154, 160); edge(a)
    elif name in ('gel', 'water_gel'): a[:] = (90, 210, 140); disc(a, 32, 32, 20, (140, 240, 180))
    else: a[:] = plate(acc, r)
    return rgba(edge(a))
def engine(path, name):
    r = rr(path); col = mach_col(path); a = plate(col, r); dark = np.array([46, 44, 40], np.float32)
    if 'trunk' in name:
        c = dye_in(name) or (200, 170, 60); a = plate((70, 70, 78), r); box(a, 14, 6, 50, 58, c, np.minimum(np.array(c) * 1.3, 255)); a[20:44, 24:40] = dark
    elif 'chamber' in name: a = plate((130, 100, 70), r); box(a, 10, 10, 54, 54, dark, (200, 170, 120)); disc(a, 32, 32, 14, (230, 120, 50))
    elif name == 'back': box(a, 8, 8, 56, 56, plate(col, r)[10, 10] * .8, col); disc(a, 32, 32, 10, dark)
    else: a[::8] *= .8; box(a, 4, 4, 60, 60, plate(col, r)[10, 10], np.minimum(np.array(col) * 1.2, 255)); box(a, 26, 4, 38, 60, dark, (80, 80, 86))
    return rgba(edge(a))
# ---- 64 px icon glyphs
def glyph(d, kind, cx, cy, s, col, col2):
    if kind == 'arrow': d.polygon([(cx - s, cy - s // 3), (cx, cy - s // 3), (cx, cy - s), (cx + s, cy), (cx, cy + s), (cx, cy + s // 3), (cx - s, cy + s // 3)], fill=col)
    elif kind == 'bolt': d.polygon([(cx + s // 3, cy - s), (cx - s // 2, cy + s // 6), (cx, cy + s // 6), (cx - s // 3, cy + s), (cx + s // 2, cy - s // 6), (cx, cy - s // 6)], fill=col)
    elif kind == 'ring': d.ellipse((cx - s, cy - s, cx + s, cy + s), outline=col, width=max(2, s // 3))
    elif kind == 'disc': d.ellipse((cx - s, cy - s, cx + s, cy + s), fill=col); d.ellipse((cx - s // 2, cy - s // 2, cx + s // 2, cy + s // 2), fill=col2)
    elif kind == 'square': d.rectangle((cx - s, cy - s, cx + s, cy + s), fill=col); d.rectangle((cx - s // 2, cy - s // 2, cx + s // 2, cy + s // 2), fill=col2)
    elif kind == 'tri': d.polygon([(cx, cy - s), (cx + s, cy + s), (cx - s, cy + s)], fill=col)
    elif kind == 'cross': d.rectangle((cx - s // 3, cy - s, cx + s // 3, cy + s), fill=col); d.rectangle((cx - s, cy - s // 3, cx + s, cy + s // 3), fill=col)
    elif kind == 'x': d.line((cx - s, cy - s, cx + s, cy + s), fill=col, width=max(3, s // 2)); d.line((cx - s, cy + s, cx + s, cy - s), fill=col, width=max(3, s // 2))
    elif kind == 'drop': d.polygon([(cx, cy - s), (cx + s * .8, cy + s * .3), (cx, cy + s), (cx - s * .8, cy + s * .3)], fill=col)
    elif kind == 'gear':
        d.ellipse((cx - s, cy - s, cx + s, cy + s), fill=col)
        for a in range(8): t = a * math.pi / 4; d.rectangle((cx + (s + 2) * math.cos(t) - 3, cy + (s + 2) * math.sin(t) - 3, cx + (s + 2) * math.cos(t) + 3, cy + (s + 2) * math.sin(t) + 3), fill=col)
        d.ellipse((cx - s // 3, cy - s // 3, cx + s // 3, cy + s // 3), fill=col2)
    elif kind == 'bars':
        for i in range(3): d.rectangle((cx - s + i * s * .7, cy + s - (i + 1) * s * .6, cx - s + i * s * .7 + s * .5, cy + s), fill=col)
    elif kind == 'robot': d.rectangle((cx - s, cy - s // 2, cx + s, cy + s), fill=col); d.rectangle((cx - s // 2, cy - s, cx + s // 2, cy - s // 2), fill=col); d.rectangle((cx - s // 2, cy - s // 4, cx - s // 6, cy), fill=col2); d.rectangle((cx + s // 6, cy - s // 4, cx + s // 2, cy), fill=col2)
KINDS = ['arrow', 'bolt', 'ring', 'disc', 'square', 'tri', 'cross', 'x', 'drop', 'gear', 'bars', 'robot']
def icon(path, name):
    r = rr(path); c = dye_in(name) or hue(path, .6, .85); c2 = tuple(int(v * .45) for v in c)
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); d = ImageDraw.Draw(im); lit = 'lit' in name or name.endswith('_on')
    bg = (52, 56, 64) if 'dark' not in name else (30, 32, 36)
    d.rounded_rectangle((2, 2, 61, 61), 8, fill=bg, outline=(150, 154, 162), width=2); d.line((6, 5, 56, 5), fill=(210, 214, 222), width=2)
    kind = r.choice(KINDS)
    for k, v in (('robot', 'robot'), ('pulsar', 'bolt'), ('light', 'disc'), ('redstone', 'bars'), ('energy', 'bolt'), ('fluid', 'drop'), ('extraction', 'arrow'), ('arrow', 'arrow'), ('machinecontrol', 'gear'), ('gate', 'square'), ('loop', 'ring'), ('off', 'x'), ('on', 'disc'), ('station', 'square'), ('goto', 'arrow')):
        if k in name or k in path.split('/')[-1]: kind = v; break
    glyph(d, kind, 32, 32, 16, c, (255, 255, 255) if lit else c2)
    if re.search(r'(^|_)\d($|_)', name):
        m = re.findall(r'\d+', name); n = int(m[-1]) % 5 + 1
        for i in range(n): d.rectangle((8 + i * 9, 50, 14 + i * 9, 57), fill=c)
    return np.asarray(im)
def paintbrush(name):
    c = DYE.get(name, (190, 190, 190)); im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.polygon([(10, 54), (46, 18), (52, 24), (16, 60)], fill=(160, 112, 64)); d.polygon([(40, 12), (56, 8), (52, 26), (36, 24)], fill=c if name != 'clean' else (230, 230, 230)); d.line((40, 12, 56, 8), fill=(255, 255, 255), width=2); return np.asarray(im)
def wire(name, size=64):
    c = DYE.get(name, (200, 40, 40)); a = np.zeros((64, 64, 4), np.uint8); a[26:38, :] = (*c, 255); a[26:28, :] = (*[min(255, v + 60) for v in c], 255); a[36:38, :] = (*[int(v * .6) for v in c], 255); return a
def liquid(name, still):
    cols = {'oil': (30, 30, 34), 'fuel': (230, 190, 40), 'dense': (60, 50, 40), 'heavy': (60, 46, 34), 'residue': (46, 40, 36), 'distilled': (170, 150, 90), 'gaseous': (200, 200, 120), 'light': (240, 210, 90), 'mixed': (200, 170, 60), 'heat': (200, 90, 50)}
    c = next((v for k, v in cols.items() if k in name), (100, 100, 140)); heat = 1 + .25 * (('hot' in name) + 2 * ('searing' in name)); frames = 32 if True else 1; r = rr(name)
    xs = np.arange(64)[None, :]; ys = np.arange(64)[:, None]; out = []
    for f in range(32):
        ph = f / 32 * 2 * math.pi; v = .78 + .16 * np.sin(xs * .35 + ph) * np.sin(ys * .3 + (ph if still else -ph * 2)) + .06 * np.sin(xs * .8 + ys * .5 + ph * 2)
        col = np.clip(np.array(c, np.float32)[None, None, :] * v[..., None] * heat, 0, 255); out.append(np.dstack([col, np.full((64, 64), 255, np.float32)]))
    return np.concatenate(out, 0).astype(np.uint8)
def save(path, arr): os.makedirs(os.path.dirname(path), exist_ok=True); Image.fromarray(arr, 'RGBA').save(path)
n = 0; skipped = []
for r_ in csv.DictReader(open(MAN)):
    p = r_['path']; ns = r_['namespace']
    if 'buildcrafttransport/textures/pipes/' in p and re.match(r'.*/pipes/(clay|cobblestone|diamond|gold|iron|lapis|obsidian|quartz|sandstone|stone|void|wood|emzuli|daizuli)_', p): continue
    if '/textures/' not in p: continue
    rel = p.split('/textures/')[-1][:-4]; parts = rel.split('/'); name = parts[-1]; w, h = int(r_['w']), int(r_['h'])
    if '/gui/' in p or 'guide/gui' in p or rel.startswith('gui/') or 'jei' in rel or (w >= 256 and h >= 128 and 'fluid' not in rel): skipped.append(rel); continue
    dest = re.sub(r'(.*)/assets/(\w+)/textures/(.*)', r'\2/textures/\3.png', p[:-4] + '.png')
    if not re.search(r'/assets/', p): dest = ns + '/textures/' + rel + '.png'
    try:
        if 'fluids' in rel and (h > w): arr = liquid(rel, 'still' in name)
        elif rel.startswith('items/paintbrush') or '/paintbrush/' in rel: arr = paintbrush(name)
        elif rel.startswith('wires') or '/wire/' in rel: arr = wire(name)
        elif '/engine' in rel: arr = engine(rel, name)
        elif rel.startswith('blocks/') or rel.startswith('block/') or rel.startswith('entities/') and w == 16:
            arr = face(rel, name) if not (rel.startswith('blocks/laser_box') or rel.startswith('blocks/gel')) else face(rel, name)
        elif rel.startswith('entities/'): arr = icon(rel, name)
        else: arr = icon(rel, name)
    except Exception as e: skipped.append(rel + ' ERR ' + str(e)); continue
    save(OUT + '/' + dest, arr); n += 1
print(n, 'written', len(skipped), 'skipped'); open(OUT + '/_skipped.txt', 'w').write('\n'.join(skipped))
