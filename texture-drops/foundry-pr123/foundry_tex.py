import sys, os, math
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
import pixel_icons as P
from pixel_icons import Cv, ramp, poly, sphere_shade, seeded, N
from PIL import Image
OUT = sys.argv[1]; NS = 'diaithirfoundry'
def save(kind, name, cv):
    f = '%s/%s/textures/%s/%s.png' % (OUT, NS, kind, name); os.makedirs(os.path.dirname(f), exist_ok=True); cv.img().resize((64, 64), Image.NEAREST).save(f) if hasattr(cv, 'img') else cv.resize((64, 64), Image.NEAREST).save(f)
STEEL = (150, 156, 168); CLAY = (150, 112, 86); BRASS = (196, 150, 70)
def cast(base):  # clay mould with a stamped recess shape
    cv, R = Cv(), ramp(CLAY)
    poly(cv, [(4, 12), (28, 12), (28, 24), (4, 24)], R[2]); poly(cv, [(4, 12), (28, 12), (26, 10), (6, 10)], R[3]); poly(cv, [(28, 12), (28, 24), (26, 22), (26, 10)], R[1])
    base(cv, ramp((60, 40, 32)))
    cv.outline(R[0]); return cv
def r_ingot(cv, D): poly(cv, [(9, 15), (23, 15), (23, 20), (9, 20)], D[1]); poly(cv, [(9, 15), (23, 15), (22, 16), (10, 16)], D[0])
def r_nugget(cv, D): sphere_shade(cv, 16, 17, 4, 3, D, 3)
def r_plate(cv, D): poly(cv, [(8, 14), (24, 14), (24, 21), (8, 21)], D[1])
def r_rod(cv, D): poly(cv, [(15, 12), (17, 12), (17, 23), (15, 23)], D[1])
def r_gear(cv, D):
    sphere_shade(cv, 16, 17, 6, 5, D, 2)
    for a in range(8): x = 16 + round(7 * math.cos(a * math.pi / 4)); y = 17 + round(6 * math.sin(a * math.pi / 4)); cv.put(x, y, D[1]); cv.put(x + 1, y, D[1])
    cv.put(16, 17, ramp(CLAY)[1])
def r_block(cv, D): poly(cv, [(8, 13), (24, 13), (24, 22), (8, 22)], D[1])
for n, f in (('cast_ingot', r_ingot), ('cast_nugget', r_nugget), ('cast_plate', r_plate), ('cast_rod', r_rod), ('cast_gear', r_gear), ('cast_block', r_block)): save('item', n, cast(f))
def metal_part(kind):
    cv, R = Cv(), ramp(STEEL, True)
    if kind == 'plate':
        poly(cv, [(4, 12), (28, 12), (24, 22), (4, 22)], R[2]); poly(cv, [(4, 12), (28, 12), (26, 14), (4, 14)], R[4]); poly(cv, [(24, 22), (28, 12), (28, 16), (25, 24)], R[1]); poly(cv, [(4, 22), (24, 22), (25, 24), (5, 24)], R[1])
        for p in ((8, 17), (20, 17)): cv.put(*p, R[1]); cv.put(p[0] + 1, p[1], R[3])
    elif kind == 'rod':
        poly(cv, [(6, 22), (24, 8), (27, 10), (9, 25)], R[2]); poly(cv, [(6, 22), (24, 8), (25, 9), (7, 23)], R[4]); poly(cv, [(9, 25), (27, 10), (27, 11), (10, 26)], R[1])
    elif kind == 'gear':
        for a in range(10):
            ang = a * math.pi / 5; cx, cy = 16 + 10 * math.cos(ang), 16 + 10 * math.sin(ang); poly(cv, [(cx - 2, cy - 2), (cx + 2, cy - 2), (cx + 2, cy + 2), (cx - 2, cy + 2)], R[2])
        sphere_shade(cv, 16, 16, 8, 8, R, 4)
        for y in range(N):
            for x in range(N):
                if (x - 16) ** 2 + (y - 16) ** 2 <= 9: cv.px[y][x] = None
    cv.outline(R[0]); return cv
for k in ('plate', 'rod', 'gear'): save('item', k, metal_part(k))
for name, kind, rgb in (('raw_gem', 'raw', (70, 150, 210)), ('cut_gem', 'gem', (70, 170, 220))):
    im = P.render(kind, NS + ':' + name, rgb); f = '%s/%s/textures/item/%s.png' % (OUT, NS, name); os.makedirs(os.path.dirname(f), exist_ok=True); im.save(f)
print('items ok')
