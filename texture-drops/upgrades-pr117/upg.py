import sys, os, math
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
from pixel_icons import Cv, ramp, poly, sphere_shade, N
from PIL import Image
import numpy as np
OUT = sys.argv[1] + '/diaithirupgrades/textures/'; os.makedirs(OUT + 'item', exist_ok=True); os.makedirs(OUT + 'block', exist_ok=True)
def rect(cv, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1): cv.put(x, y, c)
def disc(cv, cx, cy, r, c):
    for y in range(N):
        for x in range(N):
            if (x - cx) ** 2 + (y - cy) ** 2 <= r * r: cv.put(x, y, c)
def glyph(cv, kind, cx, cy, c, c2):
    if kind == 'speed':
        poly(cv, [(cx - 6, cy - 3), (cx, cy - 3), (cx, cy - 6), (cx + 6, cy), (cx, cy + 6), (cx, cy + 3), (cx - 6, cy + 3)], c)
    elif kind == 'productivity':
        rect(cv, cx - 1, cy - 6, cx + 2, cy + 7, c); rect(cv, cx - 6, cy - 1, cx + 7, cy + 2, c); disc(cv, cx, cy, 2, c2)
    elif kind == 'efficiency':
        poly(cv, [(cx, cy - 7), (cx + 5, cy), (cx + 3, cy + 6), (cx - 3, cy + 6), (cx - 5, cy)], c); rect(cv, cx, cy - 2, cx + 1, cy + 7, c2)
    elif kind == 'quality':
        pts = []
        for i in range(10): a = -math.pi / 2 + i * math.pi / 5; r = 7 if i % 2 == 0 else 3; pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
        poly(cv, pts, c)
def pips(cv, tier, x0, y, c, off):
    for i in range(3): rect(cv, x0 + i * 4, y, x0 + i * 4 + 3, y + 2, c if i < tier else off)
def circuit(kind, tier):
    S = ramp((120, 126, 138), True); G = ramp((70, 190, 110)); cv = Cv()
    rect(cv, 3, 4, 29, 28, S[2]); rect(cv, 3, 4, 29, 5, S[4]); rect(cv, 3, 27, 29, 28, S[1])
    for x in range(3, 29, 4): rect(cv, x, 24, x + 2, 27, (235, 195, 30, 255)); rect(cv, x + 2, 24, x + 4, 27, (30, 30, 34, 255)) if x + 4 <= 29 else None
    for (a, b, c_, d) in ((6, 8, 12, 9), (6, 8, 7, 14), (22, 8, 27, 9), (26, 8, 27, 13), (6, 20, 14, 21), (20, 20, 26, 21)): rect(cv, a, b, c_, d, G[1])
    for (x, y) in ((6, 8), (26, 8), (6, 20), (20, 20)): disc(cv, x, y, 1, G[3])
    rect(cv, 11, 10, 21, 20, (28, 30, 36, 255)); rect(cv, 11, 10, 21, 11, (70, 74, 84, 255))
    glyph(cv, kind, 16, 15, (110, 235, 150, 255), (40, 120, 70, 255)); pips(cv, tier, 10, 22, (255, 210, 50, 255), (60, 60, 66, 255)); cv.outline(S[0]); return cv
def ogham(kind, tier):
    R = ramp((86, 92, 100)); G = ramp((110, 235, 170)); cv = Cv()
    poly(cv, [(7, 3), (25, 3), (27, 6), (27, 27), (25, 29), (7, 29), (5, 27), (5, 6)], R[2]); poly(cv, [(7, 3), (25, 3), (27, 6), (5, 6)], R[3]); rect(cv, 5, 6, 7, 27, R[1] if False else R[3]); rect(cv, 25, 6, 27, 27, R[1])
    for t in range(8): a = t * math.pi / 4; cv.put(round(16 + 10 * math.cos(a)), round(16 + 11 * math.sin(a)), R[1])
    rect(cv, 15, 7, 17, 24, G[2]); rect(cv, 15, 7, 16, 24, G[3])
    for i, (y, s) in enumerate(((10, -1), (14, 1), (18, -1), (22, 1))):
        if i < tier + 1:
            for k in range(1, 5): cv.put(16 + s * k, y, G[2]); cv.put(16 + s * k, y + 1, G[1])
    glyph(cv, kind, 10, 25, G[3], G[1]) if False else None
    mk = {'speed': (9, 13), 'productivity': (23, 13), 'efficiency': (9, 21), 'quality': (23, 21)}[kind]; disc(cv, mk[0], mk[1], 2, G[3]); disc(cv, mk[0], mk[1], 1, G[4])
    pips(cv, tier, 10, 26, G[3], R[1]); cv.outline(R[0]); return cv
def spice(kind, tier):
    col = {'speed': (230, 120, 40), 'productivity': (210, 170, 60), 'efficiency': (110, 170, 80), 'quality': (170, 70, 120)}[kind]; C = ramp(col); B = ramp((176, 130, 84)); cv = Cv()
    poly(cv, [(8, 11), (24, 11), (27, 27), (5, 27)], B[2]); poly(cv, [(8, 11), (24, 11), (23, 14), (9, 14)], B[3]); poly(cv, [(24, 11), (27, 27), (22, 27), (21, 13)], B[1])
    rect(cv, 12, 6, 20, 11, B[2]); rect(cv, 11, 9, 21, 11, ramp((150, 100, 60))[1])   # neck + tie
    disc(cv, 16, 19, 4, C[2]); disc(cv, 15, 18, 2, C[3]); [cv.put(x, 24, C[2]) for x in (11, 14, 18, 21)]
    pips(cv, tier, 10, 29 if False else 25, (255, 230, 120, 255), B[1]); cv.outline(B[0]); return cv
for th, fn in (('circuit', circuit), ('ogham', ogham), ('spice', spice)):
    for kind in ('speed', 'productivity', 'efficiency', 'quality'):
        for tier in (1, 2, 3): fn(kind, tier).img().resize((64, 64), Image.NEAREST).save('%sitem/%s_%s_%d.png' % (OUT, th, kind, tier))
# beacon block face (64 px): dark steel, amber core in a ring, antenna ticks
a = np.zeros((64, 64, 3), np.float32); a[:] = (70, 76, 88); a *= np.linspace(1.12, .88, 64)[None, :, None]
rng = np.random.default_rng(3); a += rng.uniform(-4, 4, (64, 64, 1))
a[0:2] *= 1.25; a[:, 0:2] *= 1.12; a[62:] *= .6; a[:, 62:] *= .72
for y in (6, 57):
    for x in (6, 57): a[y - 1:y + 2, x - 1:x + 2] = (40, 42, 48)
yy, xx = np.ogrid[:64, :64]
for r, c in ((22, (30, 32, 38)), (19, (110, 114, 124)), (16, (24, 26, 30)), (12, (230, 140, 30)), (8, (255, 200, 70)), (4, (255, 245, 200))): a[(xx - 32) ** 2 + (yy - 32) ** 2 <= r * r] = c
for x in range(4, 60, 6): a[30:34, x:x + 3] = a[30:34, x:x + 3] * .85
for (x0, y0) in ((14, 14), (50, 14)): pass
for i in range(0, 64, 8): a[2:5, i + 1:i + 5] = (235, 195, 30) if (i // 8) % 2 == 0 else (30, 30, 34)
Image.fromarray(np.clip(a, 0, 255).astype(np.uint8)).convert('RGBA').save(OUT + 'block/beacon.png')
print(len(os.listdir(OUT + 'item')), 'items + beacon')
