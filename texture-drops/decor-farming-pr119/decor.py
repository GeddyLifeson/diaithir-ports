import sys, os, re, json, math, random, hashlib, subprocess
import numpy as np
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
from pixel_icons import Cv, ramp, poly, sphere_shade, N
from PIL import Image, ImageDraw
OUT = sys.argv[1]; D = json.load(open('/home/user/work/bc/p119.json'))
def rr(n): return random.Random(int(hashlib.md5(n.encode()).hexdigest()[:8], 16))
C = {'oak': (170, 130, 76), 'acacia': (176, 98, 60), 'birch': (200, 184, 130), 'dark_oak': (70, 48, 26), 'jungle': (160, 110, 76), 'spruce': (110, 80, 48), 'copper': (200, 118, 76), 'iron': (190, 192, 198), 'gold': (230, 190, 64),
     'stone': (130, 132, 136), 'andesite': (136, 138, 140), 'granite': (160, 106, 90), 'diorite': (200, 200, 200), 'deepslate': (70, 72, 80), 'glass': (180, 220, 230), 'brick': (160, 70, 52), 'white': (236, 236, 238), 'metal': (172, 176, 186), 'freezer': (214, 232, 244)}
def mat(name):
    for k in ('dark_oak', 'copper', 'iron', 'gold', 'stone', 'andesite', 'granite', 'diorite', 'deepslate', 'glass', 'brick', 'white', 'metal', 'freezer', 'oak', 'acacia', 'birch', 'jungle', 'spruce'):
        if k in name: return k
    return 'oak'
def grain(a, r, amt=6): a += (np.array([[r.uniform(-amt, amt)] * 3 for _ in range(64 * 64)]).reshape(64, 64, 3)); return a
def plank(c, r):
    a = np.zeros((64, 64, 3), np.float32); a[:] = c
    for y in range(0, 64, 16): a[y:y + 1] *= .62; a[y + 1:y + 2] *= 1.12; a[y + 2:y + 15] *= r.uniform(.94, 1.06)
    return grain(a, r)
def metalp(c, r):
    a = np.zeros((64, 64, 3), np.float32); a[:] = c; a *= np.linspace(1.12, .88, 64)[None, :, None]; return grain(a, r, 3)
def stonep(c, r):
    a = np.zeros((64, 64, 3), np.float32); a[:] = c; return grain(a, r, 9)
def base(m, r): return plank(C[m], r) if m in ('oak', 'acacia', 'birch', 'dark_oak', 'jungle', 'spruce') else metalp(C[m], r) if m in ('copper', 'iron', 'gold', 'metal', 'white', 'freezer') else stonep(C[m], r)
def edge(a):
    a[0:2] *= 1.18; a[:, 0:2] *= 1.1; a[62:] *= .62; a[:, 62:] *= .7; return a
def box(a, x0, y0, x1, y1, c, line=None):
    c = np.asarray(c, np.float32); line = None if line is None else np.asarray(line, np.float32)
    a[y0:y1, x0:x1] = c
    if line is not None: a[y0:y0 + 1, x0:x1] = line; a[y1 - 1:y1, x0:x1] = line * .6; a[y0:y1, x0:x0 + 1] = line; a[y0:y1, x1 - 1:x1] = line * .6
def block(name):
    r = rr(name); m = mat(name); a = base(m, r); dark = a.mean() < 110; knob = np.array([228, 200, 90], np.float32)
    if name.startswith('counter_') or name == 'kitchen_counter':
        box(a, 3, 22, 61, 62, a[30, 30] * .9, a[30, 30] * 1.15); a[18:22, :] *= 1.15; a[22:23, :] *= .55; box(a, 20, 30, 44, 36, a[30, 30] * .75, a[30, 30] * 1.2); a[31:34, 30:34] = knob
    elif name.startswith('fridge'):
        a[30:32, 4:60] *= .5; box(a, 6, 6, 58, 30, a[10, 10] * 1.0, a[10, 10] * 1.2); box(a, 6, 33, 58, 60, a[40, 40] * 1.0, a[40, 40] * 1.2); a[14:26, 50:53] = (90, 92, 98); a[38:54, 50:53] = (90, 92, 98)
    elif name.startswith('oven'):
        box(a, 8, 20, 56, 56, np.array([40, 36, 36], np.float32), a[10, 10] * 1.15); box(a, 14, 26, 50, 48, np.array([240, 130, 40], np.float32), np.array([255, 190, 80], np.float32)); a[16:19, 10:54] = a[10, 10] * .5; a[50:52, 18:46] = (210, 210, 215)
        for x in (14, 24, 34, 44): a[8:14, x:x + 6] = (30, 30, 34)
    elif name.startswith('range_hob'):
        a[:] = np.array([36, 36, 40], np.float32); edge(a)
        for cx, cy in ((18, 18), (46, 18), (18, 46), (46, 46)):
            for rad, col in ((11, (70, 70, 76)), (8, (30, 30, 34)), (4, (230, 100, 40))):
                yy, xx = np.ogrid[:64, :64]; a[(xx - cx) ** 2 + (yy - cy) ** 2 <= rad * rad] = col
    elif name.startswith('sink'):
        box(a, 8, 8, 56, 56, a[20, 20] * 1.0, a[20, 20] * 1.2); box(a, 12, 12, 52, 52, np.array([70, 100, 120], np.float32), np.array([40, 60, 74], np.float32)); a[28:36, 30:34] = (180, 190, 200); a[26:30, 26:38] = (150, 160, 170)
    elif name in ('pantry_shelf', 'shelf_oak', 'spice_rack', 'pantry_cabinet_iron'):
        for y in (14, 30, 46): a[y:y + 3, 3:61] = a[5, 5] * .6; a[y - 11:y, 4:60] = a[5, 5] * .4
        rr2 = rr(name + 's')
        for y in (13, 29, 45):
            x = 6
            while x < 56: w = rr2.randint(4, 8); a[y - 8:y, x:x + w] = np.array(rr2.choice([(200, 70, 60), (90, 170, 80), (230, 190, 70), (130, 90, 160), (200, 200, 190)]), np.float32); x += w + 2
        if name == 'pantry_cabinet_iron': a[:] = metalp(C['iron'], r); edge(a); a[8:56, 30:34] = a[8, 8] * .5; a[30:36, 26:29] = knob
    elif name.startswith('shipping_bin'):
        for x in range(0, 64, 8): a[6:58, x:x + 1] *= .55
        a[0:10] = a[0:10] * .8; a[8:10] *= .5; box(a, 24, 16, 40, 22, np.array([30, 30, 34], np.float32), a[30, 30] * 1.2)
    elif name.startswith('sprinkler'):
        a[:] = (60, 70, 74); edge(a); yy, xx = np.ogrid[:64, :64]; a[(xx - 32) ** 2 + (yy - 32) ** 2 <= 15 * 15] = C[m] if m != 'oak' else C['iron']; a[(xx - 32) ** 2 + (yy - 32) ** 2 <= 7 * 7] = (50, 140, 220); a[(xx - 32) ** 2 + (yy - 32) ** 2 <= 3 * 3] = (180, 230, 255)
        if 'wide' in name:
            for ang in range(0, 360, 45): a[int(32 + 22 * math.sin(math.radians(ang)))][int(32 + 22 * math.cos(math.radians(ang)))] = (50, 140, 220)
    elif name.startswith('market_stall'):
        if 'awning' in name:
            for x in range(0, 64, 16): a[:, x:x + 8] = (220, 60, 56); a[:, x + 8:x + 16] = (240, 240, 236)
            a[56:64] *= .75; edge(a); a[:] = grain(a, r, 3)
        else: a = plank(C['oak'] if 'sign' not in name else C['spruce'], r); a[4:12, 4:60] *= .7
    elif name.startswith('farmland') or name == 'coarse_fertile_soil':
        soil = (96, 64, 40) if 'rich' not in name else (60, 42, 28); a[:] = soil if name != 'coarse_fertile_soil' else (120, 90, 66); grain(a, r, 8)
        for x in range(4, 64, 12): a[:, x:x + 3] *= .72
        if name == 'coarse_fertile_soil':
            for _ in range(30): y, x = r.randrange(64), r.randrange(64); a[y:y + 2, x:x + 2] = (90, 150, 70)
        if 'deep' in name: a *= .82
    elif name == 'cooking_table':
        a = plank(C['oak'], r); edge(a); box(a, 14, 14, 50, 50, (60, 60, 66), (110, 110, 118)); yy, xx = np.ogrid[:64, :64]; a[(xx - 32) ** 2 + (yy - 32) ** 2 <= 11 * 11] = (30, 30, 34); a[(xx - 32) ** 2 + (yy - 32) ** 2 <= 8 * 8] = (200, 110, 50)
    elif name.startswith('crate') or name.endswith('_crate'):
        a[:] = plank(C[m], r); edge(a); a[4:8, :] = a[4, 4] * .6; a[56:60, :] = a[4, 4] * .6; a[:, 4:8] = a[4, 4] * .6; a[:, 56:60] = a[4, 4] * .6
        for i in range(56): a[4 + i, 4 + i] = a[4, 4] * .55
    elif 'lamp' in name or name.endswith('lantern'):
        col = (255, 220, 140) if 'glow' in name else (150, 240, 230) if name.endswith('kitchen_lamp') or 'sun' in name else (240, 100, 70) if 'redstone' in name else (255, 170, 90)
        a[:] = col; yy, xx = np.mgrid[:64, :64]; a *= (1 - .18 * (np.abs(xx - 32) + np.abs(yy - 32)) / 64)[..., None]
        for x in (0, 31, 62): a[:, x:x + 2] = (60, 60, 66); a[x:x + 2, :] = (60, 60, 66)
    elif 'pane' in name or 'glass' in name or name.startswith('greenhouse'):
        a[:] = (190, 228, 238) if 'tinted' not in name else (80, 80, 96); a[:, 0:4] = C['iron']; a[:, 60:] = C['iron']; a[0:4] = C['iron']; a[60:] = C['iron']
        if name.startswith('greenhouse'): a[30:34] = C['iron']; a[:, 30:34] = C['iron']; a[8:28, 8:28] = (170, 235, 200)
        a[8:20, 8:12] = (250, 252, 255)
    elif name == 'auto_harvester':
        a = metalp(C['iron'], r); edge(a); box(a, 8, 8, 56, 40, (50, 54, 60), (110, 114, 120))
        for i in range(6): a[12:36, 12 + i * 7:15 + i * 7] = (210, 214, 220)
        a[44:60, 8:56] = (90, 150, 70); a[48:52, 12:52] = (230, 200, 70)
    elif name == 'cotton_block': a[:] = (240, 240, 244); grain(a, r, 6); a[::8] *= .93
    elif name == 'flax_fiber_block': a[:] = (150, 160, 90); grain(a, r, 7); a[::4] *= .9
    elif 'thatch' in name or 'woven' in name or 'hay' in name or 'wheat_crate' in name:
        a[:] = (206, 170, 64); grain(a, r, 7)
        for y in range(0, 64, 6): a[y:y + 1] *= .65
        if 'woven' in name:
            for x in range(0, 64, 6): a[:, x:x + 1] *= .7
    elif name == 'terracotta_tile': a[:] = (178, 102, 66); grain(a, r, 6); a[::32] *= .6; a[:, ::32] *= .6
    elif name == 'stone_trim' or name.startswith('decor_stone') or 'pillar' in name: a = stonep(C['stone'], r); a[::16] *= .6; a[:, ::32] *= .6
    elif name.startswith('glass_crate') or 'copper_plate' in name or 'iron_plate' in name: a = metalp(C['copper'] if 'copper' in name else C['iron'], r); edge(a); [a.__setitem__((slice(6, 58), slice(x, x + 3)), a[6, 6] * .55) for x in (6, 58)]
    edge(a)
    return np.dstack([np.clip(a, 0, 255), np.full((64, 64), 255, np.float32)]).astype(np.uint8)
# ---- items (32 px canvas, nearest 2x)
def disc(cv, cx, cy, rad, c):
    for y in range(N):
        for x in range(N):
            if (x - cx) ** 2 + (y - cy) ** 2 <= rad * rad: cv.put(x, y, c)
def rect(cv, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1): cv.put(x, y, c)
PAL = {'rice': (220, 170, 70), 'barley': (210, 180, 90), 'cinder': (210, 70, 40), 'flax': (110, 130, 200), 'sunroot': (230, 150, 50), 'wheat': (224, 190, 80), 'beetroot': (150, 30, 70), 'carrot': (240, 140, 40), 'melon': (120, 190, 80), 'potato': (200, 170, 100), 'pumpkin': (230, 130, 40),
       'apple': (200, 50, 40), 'berry': (190, 40, 80), 'cocoa': (130, 70, 40), 'cod': (200, 180, 150), 'salmon': (240, 120, 90), 'bread': (200, 140, 70), 'pickle': (110, 160, 70), 'living': (120, 230, 140), 'stew': (160, 90, 50), 'pie': (210, 150, 80), 'feast': (230, 170, 70), 'ration': (170, 130, 90), 'herb': (90, 170, 80), 'juice': (200, 90, 150)}
def colour(name):
    for k, v in PAL.items():
        if k in name: return v
    return (180, 180, 180)
def item(name):
    c = colour(name); R = ramp(c); cv = Cv(); P = ramp((226, 222, 208))
    if name.endswith('_seeds') or name.endswith('_seed') or name == 'living_seed':
        r = rr(name)
        for _ in range(9):
            x, y = 9 + r.randrange(14), 10 + r.randrange(14); poly(cv, [(x, y + 2), (x + 2, y), (x + 4, y + 2), (x + 2, y + 4)], R[2 + r.randrange(2)])
        if name == 'living_seed': disc(cv, 16, 16, 5, ramp((90, 230, 130))[3]); disc(cv, 15, 15, 2, (240, 255, 240, 255))
    elif name.endswith('_crop') or name.startswith('wheat') and 'bundle' in name or name == 'wheat_bundle_item':
        for i in range(5):
            x = 8 + i * 4; poly(cv, [(x, 26), (x + 1, 26), (x + 3, 8), (x + 2, 6)], R[2]); poly(cv, [(x + 1, 8), (x + 4, 11), (x + 2, 12)], R[3])
        rect(cv, 8, 19, 25, 22, ramp((120, 80, 40))[2])
    elif name.endswith('_fruit') or name in ('apple_bait_item', 'sweet_berry_fruit'):
        sphere_shade(cv, 16, 17, 8, 8, R, 3); rect(cv, 15, 6, 17, 10, ramp((90, 60, 30))[1]); poly(cv, [(17, 8), (22, 6), (20, 10)], ramp((90, 170, 70))[2])
    elif 'fish' in name or 'cod' in name or 'salmon' in name or name == 'fish_trophy_item':
        poly(cv, [(5, 16), (14, 8), (24, 11), (28, 8), (28, 24), (24, 21), (14, 24)], R[2]); poly(cv, [(5, 16), (14, 8), (24, 11), (14, 15)], R[3]); disc(cv, 10, 15, 1, (20, 20, 20, 255))
        if 'dish' in name or 'food' in name or 'plate' in name: rect(cv, 3, 25, 29, 28, P[2])
    elif any(k in name for k in ('pie', 'stew', 'feast', 'ration', 'bread', 'salad', 'dish', 'plate', 'hearty')):
        rect(cv, 3, 22, 29, 26, P[2]); poly(cv, [(3, 22), (29, 22), (26, 18), (6, 18)], P[3])
        if 'stew' in name: disc(cv, 16, 17, 8, R[1]); rect(cv, 8, 17, 24, 20, R[2])
        elif 'pie' in name: poly(cv, [(6, 20), (26, 20), (24, 10), (8, 10)], R[2]); rect(cv, 8, 10, 24, 12, R[3]); [cv.put(x, 15, R[1]) for x in range(10, 23, 3)]
        else: sphere_shade(cv, 16, 14, 10, 6, R, 2)
    elif name.startswith('fishing_rod'):
        t = ramp({'gold': (230, 190, 64), 'iron': (190, 192, 198), 'wood': (150, 108, 64)}[name.split('_')[2]], name != 'fishing_rod_wood_item')
        for i in range(22): cv.put(5 + i, 26 - i, t[2]); cv.put(6 + i, 26 - i, t[3])
        for i in range(8): cv.put(27, 4 + i, (230, 230, 230, 255))
        disc(cv, 27, 13, 1, R[2])
    elif name in ('preserved_jar', 'crop_juice', 'herb_pot'):
        G = ramp((190, 220, 230)); rect(cv, 9, 10, 23, 27, G[2]); rect(cv, 11, 6, 21, 10, ramp((150, 108, 64))[2]); rect(cv, 10, 16, 22, 26, R[2]); rect(cv, 10, 16, 22, 18, R[3])
        if name == 'herb_pot': rect(cv, 9, 18, 23, 27, ramp((176, 98, 60))[2]); [poly(cv, [(16, 18), (12 + i * 4, 8), (18, 17)], ramp((90, 170, 80))[2]) for i in range(3)]
    elif name == 'seed_bag': poly(cv, [(8, 10), (24, 10), (26, 27), (6, 27)], ramp((176, 136, 90))[2]); rect(cv, 8, 6, 24, 11, ramp((176, 136, 90))[3]); rect(cv, 8, 10, 24, 12, ramp((90, 60, 30))[1]); disc(cv, 16, 20, 4, R[2])
    elif name == 'cutting_board': poly(cv, [(5, 9), (27, 9), (27, 23), (5, 23)], ramp((196, 150, 90))[2]); rect(cv, 5, 9, 27, 11, ramp((196, 150, 90))[3]); rect(cv, 8, 14, 18, 17, R[2]); rect(cv, 22, 4, 24, 14, ramp((190, 192, 198), True)[3])
    elif name == 'sea_pickle_item': sphere_shade(cv, 16, 16, 5, 9, R, 2)
    else: sphere_shade(cv, 16, 16, 8, 8, R, 4)
    cv.outline(R[0]); return cv.img().resize((64, 64), Image.NEAREST)
BLOCK_MODS = ('diaithirfarmkitchen', 'diaithircropgenes'); n = 0
for mod, ms in D.items():
    for k, tx in ms.items():
        kind, name = k.split('/')
        if kind == 'block':
            if mod not in BLOCK_MODS: continue
            if re.search(r'(_stairs|_slab(_full|_top)?|_wall|_fence)$', name) and mod == 'diaithirfarmkitchen' and not name.startswith('counter_'): continue
            if re.match(r'(acacia|birch|cherry|dark_oak|jungle|spruce|bamboo|oak)_(planter|crate|fence|pane|planks_block|table_top|bookshelf)', name) or name in ('bamboo_wall', 'dark_oak_bookshelf', 'cherry_fence'): continue
            img = Image.fromarray(block(name), 'RGBA')
        else:
            if not name.endswith('_item') and mod in ('diaithirbuildcraft2', 'diaithircreatureutils'): continue
            img = item(name[:-5] if name.endswith('_item') else name) if False else item(name)
        f = '%s/%s/textures/%s/%s.png' % (OUT, mod, kind, name); os.makedirs(os.path.dirname(f), exist_ok=True); img.save(f); n += 1
print(n)
