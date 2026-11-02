import sys, os, glob, math, colorsys
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
from pixel_icons import Cv, ramp, poly, sphere_shade, N
from PIL import Image
SRC = '/tmp/diaithir-bosses/src/main/resources/assets/diaithirbosses/textures/item/'; OUT = sys.argv[1] + '/diaithirbosses/textures/item/'; os.makedirs(OUT, exist_ok=True)
def rect(cv, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1): cv.put(x, y, c)
def disc(cv, cx, cy, r, c):
    for y in range(N):
        for x in range(N):
            if (x - cx) ** 2 + (y - cy) ** 2 <= r * r: cv.put(x, y, c)
def dom(f):  # dominant saturated colour of the old 16 px icon
    im = Image.open(f).convert('RGBA'); best = None; bs = -1
    for p in im.getdata():
        if p[3] < 200: continue
        h, s, v = colorsys.rgb_to_hsv(*(c / 255 for c in p[:3])); sc = s * v
        if sc > bs and v > .25: bs, best = sc, p[:3]
    return best or (150, 150, 150)
def save(name, cv): cv.img().resize((64, 64), Image.NEAREST).save(OUT + name + '.png')
def knot(cv, cx, cy, c):  # small triquetra-ish ring knot
    for a in range(24):
        t = a * math.pi / 12; r = 3 + 1.2 * math.cos(3 * t)
        cv.put(round(cx + r * math.cos(t)), round(cy + r * math.sin(t)), c)
def trophy(rgb):
    R = ramp(rgb, True); G = ramp((214, 172, 70), True); cv = Cv()
    poly(cv, [(8, 5), (24, 5), (22, 16), (18, 20), (14, 20), (10, 16)], G[2]); poly(cv, [(8, 5), (24, 5), (24, 7), (8, 7)], G[4])
    poly(cv, [(10, 7), (22, 7), (21, 14), (17, 18), (15, 18), (11, 14)], R[2]); poly(cv, [(10, 7), (14, 7), (14, 16), (11, 14)], R[3])
    rect(cv, 6, 8, 8, 14, G[1]); rect(cv, 24, 8, 26, 14, G[1]); rect(cv, 14, 20, 18, 24, G[1]); poly(cv, [(9, 24), (23, 24), (24, 27), (8, 27)], G[2]); rect(cv, 9, 24, 23, 25, G[3])
    knot(cv, 16, 11, R[4]); cv.outline(G[0]); return cv
def egg(rgb, rgb2):
    A, B = ramp(rgb), ramp(rgb2); cv = Cv()
    poly(cv, [(16, 3), (22, 8), (25, 17), (22, 25), (16, 28), (10, 25), (7, 17), (10, 8)], A[2])
    poly(cv, [(16, 3), (11, 7), (8, 15), (10, 25), (12, 22), (13, 12)], A[3])
    poly(cv, [(22, 8), (25, 17), (22, 25), (19, 25), (22, 16)], A[1])
    for (x, y, r) in ((14, 12, 2), (20, 17, 2), (13, 22, 1), (19, 9, 1)): disc(cv, x, y, r, B[2]); cv.put(x - 1, y - 1, B[4])
    cv.outline(A[0]); return cv
for f in sorted(glob.glob(SRC + '*.png')):
    n = os.path.basename(f)[:-4]; c = dom(f)
    if n.endswith('_trophy'): save(n, trophy(c))
    elif n.endswith('_spawn_egg'):
        h, s, v = colorsys.rgb_to_hsv(*(x / 255 for x in c)); c2 = tuple(int(x * 255) for x in colorsys.hsv_to_rgb((h + .08) % 1, s, min(1, v + .15))); save(n, egg(tuple(int(x * .8) for x in c), c2))
cv, R = Cv(), ramp((120, 60, 190)); G = ramp((210, 170, 70), True)   # boss_catalyst: faceted violet crystal on a gold clasp
poly(cv, [(16, 3), (24, 11), (21, 25), (11, 25), (8, 11)], R[2]); poly(cv, [(16, 3), (8, 11), (16, 14)], R[3]); poly(cv, [(16, 3), (24, 11), (16, 14)], R[4]); poly(cv, [(8, 11), (16, 14), (11, 25)], R[2]); poly(cv, [(24, 11), (16, 14), (21, 25)], R[1]); poly(cv, [(11, 25), (21, 25), (16, 14)], R[1])
rect(cv, 10, 25, 22, 28, G[2]); cv.outline(R[0]); save('boss_catalyst', cv)
cv, R = Cv(), ramp((92, 100, 112), True)               # hollow_core: iron shell, hollow centre, ogham ticks
disc(cv, 16, 16, 10, R[2]); disc(cv, 12, 12, 4, R[3]); disc(cv, 16, 16, 5, (14, 12, 16, 255)); disc(cv, 16, 16, 3, (70, 230, 190, 255))
for a in range(8): t = a * math.pi / 4; cv.put(round(16 + 8 * math.cos(t)), round(16 + 8 * math.sin(t)), R[0])
cv.outline(R[0]); save('hollow_core', cv)
cv, R = Cv(), ramp((250, 210, 90), True)               # ageshard: gold hourglass shard
poly(cv, [(16, 2), (23, 12), (19, 30), (13, 30), (9, 12)], R[2]); poly(cv, [(16, 2), (9, 12), (16, 15)], R[3]); poly(cv, [(16, 2), (23, 12), (16, 15)], R[4]); poly(cv, [(19, 30), (13, 30), (16, 15)], R[1]); cv.outline(R[0]); save('ageshard', cv)
cv, R = Cv(), ramp((190, 170, 120)); G = ramp((214, 172, 70), True)   # champion_token: round bronze coin with knot
disc(cv, 16, 16, 11, G[2]); disc(cv, 16, 16, 9, G[1]); disc(cv, 16, 16, 8, G[3]); knot(cv, 16, 16, G[0]); disc(cv, 12, 11, 2, G[4]); cv.outline(G[0]); save('champion_token', cv)
cv, R = Cv(), ramp((90, 170, 70)); G = ramp((214, 172, 70), True)      # rush_laurel
for side in (-1, 1):
    for i in range(7):
        x = 16 + side * (3 + i); y = 22 - i * 2; poly(cv, [(x, y), (x + side * 3, y - 2), (x + side * 2, y + 1)], R[2 + (i % 2)])
rect(cv, 14, 24, 18, 27, G[2]); cv.outline(R[0]); save('rush_laurel', cv)
print(len(glob.glob(OUT + '*.png')), 'items')
