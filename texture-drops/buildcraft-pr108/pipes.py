import sys, os, csv, re, random, hashlib
import numpy as np
from PIL import Image, ImageDraw
OUT = sys.argv[1]; MAN = '/tmp/bc_manifest.csv'
MAT = {'clay': ((196, 140, 110), 'smooth'), 'cobblestone': ((120, 120, 120), 'cobble'), 'diamond': ((90, 200, 215), 'gem'), 'gold': ((226, 184, 64), 'metal'), 'iron': ((190, 192, 198), 'metal'),
       'lapis': ((50, 80, 175), 'gem'), 'obsidian': ((44, 30, 64), 'gem'), 'quartz': ((232, 228, 220), 'gem'), 'sandstone': ((216, 200, 140), 'smooth'), 'stone': ((130, 132, 136), 'smooth'),
       'void': ((26, 20, 34), 'gem'), 'wood': ((150, 108, 64), 'wood'), 'emzuli': ((70, 170, 110), 'metal'), 'daizuli': ((160, 90, 190), 'metal')}
TYPE = {'item': (80, 200, 90), 'fluid': (70, 130, 235), 'power': (235, 70, 55), 'fe': (240, 150, 50)}
DYE = {'white': (240, 240, 240), 'orange': (240, 140, 40), 'magenta': (200, 80, 200), 'light_blue': (110, 170, 240), 'yellow': (240, 220, 60), 'lime': (130, 210, 50), 'pink': (240, 150, 180), 'gray': (90, 90, 90),
       'light_gray': (170, 170, 170), 'cyan': (40, 150, 170), 'purple': (130, 60, 190), 'blue': (50, 70, 190), 'brown': (110, 70, 40), 'green': (80, 120, 40), 'red': (190, 50, 50), 'black': (25, 25, 30)}
def rr(n): return random.Random(int(hashlib.md5(n.encode()).hexdigest()[:8], 16))
def base(name, mat):
    c, style = MAT.get(mat, ((140, 140, 150), 'metal')); r = rr(name + 'b'); a = np.zeros((64, 64, 3), np.float32); a[:] = c
    if style == 'smooth': a += np.array([[r.uniform(-9, 9)] * 3 for _ in range(64 * 64)]).reshape(64, 64, 3)
    elif style == 'metal':
        a *= (np.linspace(1.12, .86, 64)[None, :, None] * np.ones((64, 1, 1)))
        for _ in range(60): x, y = r.randrange(64), r.randrange(64); a[y, x:x + 3] *= 1.15
    elif style == 'gem': a *= (np.linspace(1.1, .8, 64)[:, None, None] * np.ones((1, 64, 1)));  a += np.array([[r.uniform(-5, 5)] * 3 for _ in range(64 * 64)]).reshape(64, 64, 3)
    elif style == 'wood':
        for y in range(64): a[y] *= 1 + .08 * np.sin(y * .9 + r.random() * .2) 
        a += np.array([[r.uniform(-6, 6)] * 3 for _ in range(64 * 64)]).reshape(64, 64, 3)
    elif style == 'cobble':
        a[:] = c
        for cy in range(0, 64, 16):
            for cx in range(-8 if (cy // 16) % 2 else 0, 64, 16):
                v = r.uniform(.8, 1.15); a[max(0, cy + 1):cy + 15, max(0, cx + 1):max(0, cx + 15)] *= v
        a[::16] *= .6; a[:, ::16] *= .6
    return np.clip(a, 0, 255)
def frame(a):
    a[0:3] *= 1.18; a[:, 0:3] *= 1.1; a[61:64] *= .62; a[:, 61:64] *= .7; return np.clip(a, 0, 255)
def make(stem, w):
    parts = stem.split('_'); mat = parts[0]; typ = next((p for p in parts if p in TYPE), None); rest = '_'.join(p for p in parts[1:] if p != typ)
    a = frame(base(stem, mat)); col = np.array(TYPE.get(typ, (200, 200, 200)), np.float32)
    clear = 'clear' in stem; filled = 'filled' in stem; mm = re.search(r'_m(\d+)$', stem); lvl = int(mm.group(1)) if mm else None; direction = next((d for d in ('north', 'south', 'east', 'west', 'up', 'down') if stem.endswith('_' + d)), None)
    dye = next((d for d in sorted(DYE, key=len, reverse=True) if stem.endswith('_' + d)), None)
    if dye: col = np.array(DYE[dye], np.float32)
    if not clear: 
        a[24:40, 3:61] = a[24:40, 3:61] * .35 + col * .65; a[24:26, 3:61] *= 1.15; a[38:40, 3:61] *= .7
    if filled: a[24:40, 3:61] = col; a[26:30, 3:61] = np.clip(col * 1.2, 0, 255)
    if lvl is not None:
        n = 0 if lvl == 0 else int(np.log2(lvl)) + 1
        for i in range(8): a[42:50, 4 + i * 7:9 + i * 7] = (col if i < n else a[42:50, 4 + i * 7:9 + i * 7] * .4)
    if direction:
        im = Image.fromarray(a.astype(np.uint8)); d = ImageDraw.Draw(im); pts = {'north': [(32, 8), (22, 22), (42, 22)], 'south': [(32, 56), (22, 42), (42, 42)], 'east': [(56, 32), (42, 22), (42, 42)], 'west': [(8, 32), (22, 22), (22, 42)], 'up': [(32, 8), (22, 22), (42, 22)], 'down': [(32, 56), (22, 42), (42, 42)]}[direction]
        d.polygon(pts, fill=(255, 255, 255)); a = np.asarray(im).astype(np.float32)
    if any(k in stem for k in ('itemstack', 'cb')): a[8:14, 8:14] = (255, 255, 255)
    return np.dstack([a, np.full((64, 64), 255, np.float32)]).clip(0, 255).astype(np.uint8)
n = 0
for r in csv.DictReader(open(MAN)):
    p = r['path']
    if 'buildcrafttransport/textures/pipes/' not in p: continue
    stem = p.split('/pipes/')[-1][:-4]; w = int(r['w'])
    if not re.match(r'^(clay|cobblestone|diamond|gold|iron|lapis|obsidian|quartz|sandstone|stone|void|wood|emzuli|daizuli)_', stem): continue
    f = OUT + '/buildcrafttransport/textures/pipes/' + stem + '.png'; os.makedirs(os.path.dirname(f), exist_ok=True)
    Image.fromarray(make(stem, w), 'RGBA').save(f); n += 1
print(n, 'pipe textures')
