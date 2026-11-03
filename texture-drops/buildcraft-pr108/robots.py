import sys, os, glob, random, hashlib, colorsys
import numpy as np
from PIL import Image
OUT = sys.argv[1] + '/buildcraftrobotics/textures/entities/'; os.makedirs(OUT, exist_ok=True)
def rr(n): return random.Random(int(hashlib.md5(n.encode()).hexdigest()[:8], 16))
def accent(n):
    if n == 'base': return (170, 176, 186)
    h = int(hashlib.md5(n.encode()).hexdigest()[:6], 16) % 360; return tuple(int(c * 255) for c in colorsys.hsv_to_rgb(h / 360, .62, .86))
for f in sorted(glob.glob('/tmp/bcrobots/*.png')) if os.path.isdir('/tmp/bcrobots') else []:
    pass
import subprocess, csv
rows = [r['path'] for r in csv.DictReader(open('/tmp/bc_manifest.csv')) if 'robotics/textures/entities' in r['path']]
n = 0
for p in rows:
    name = p.split('/')[-1][:-4]
    src = subprocess.run(['git', '-C', '/home/user/diaithir-ports', 'show', 'FETCH_HEAD:diaithir-routing-networks/buildcraft/' + p], capture_output=True).stdout
    import io; orig = np.asarray(Image.open(io.BytesIO(src)).convert('RGBA'))
    if name.startswith('overlay'): Image.fromarray(orig, 'RGBA').save(OUT + name + '.png'); n += 1; continue   # layout-only markers, no art in them
    active = name.endswith('_active'); kind = name[6:].replace('_active', ''); r = rr(name); acc = np.array(accent(kind), np.float32)
    o = np.zeros((32, 32, 4), np.uint8); alpha = orig[..., 3] > 0; steel = np.array([128, 134, 144], np.float32)
    for y in range(32):
        for x in range(32):
            if not alpha[y, x]: continue
            v = steel * (1.1 - .018 * (y % 8)) + r.uniform(-5, 5); c = v
            if y >= 8:
                if y in (8, 9): c = acc * (.9 if active else .6) * (1.1 - .1 * (y - 8))
                elif y in (14, 15): c = acc * (.75 if active else .45)
                if x % 8 in (0,) : c = c * .75
                if y == 10: c = c * 1.12
            o[y, x] = (*np.clip(c, 0, 255).astype(np.uint8), 255)
    # head: eye frame (x8-15,y0-7) and plate (x16-23,y0-7)
    for y in range(8):
        for x in range(8, 16):
            e = (1 <= x - 8 <= 6 and 1 <= y <= 6); o[y, x] = (36, 38, 44, 255) if not e else (70, 74, 82, 255)
            if 3 <= x - 8 <= 4 and 3 <= y <= 4: o[y, x] = (110, 255, 255, 255) if active else (40, 120, 132, 255)
        for x in range(16, 24):
            o[y, x] = (*np.clip(steel * (1.12 - .02 * y), 0, 255).astype(np.uint8), 255)
            if (x - 16) in (1, 6) and y in (1, 6): o[y, x] = (60, 62, 70, 255)
    for (mx, my) in [(a, b) for a in (3, 11, 19, 27) for b in (11, 12)] + [(a + 1, b) for a in (3, 11, 19, 27) for b in (11, 12)]: o[my, mx] = (255, 0, 0, 255)
    Image.fromarray(o, 'RGBA').save(OUT + name + '.png'); n += 1
print(n, 'robot skins')
