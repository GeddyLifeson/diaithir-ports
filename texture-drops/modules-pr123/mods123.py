import sys, os, json, math, hashlib, colorsys
sys.path.insert(0, '/home/user/geddylifeson/global-repo/cloud/texture-pack/gaps-src')
from pixel_icons import Cv, ramp, poly, sphere_shade, N
from PIL import Image
OUT = sys.argv[1]; miss = json.load(open('/home/user/work/foundry/missing123.json'))
def rect(cv, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1): cv.put(x, y, c)
def col(name):
    h = int(hashlib.md5(name.encode()).hexdigest()[:6], 16); r = colorsys.hsv_to_rgb((h % 360) / 360, .55, .85); return tuple(int(c * 255) for c in r)
TIER = {'wood': (150, 108, 64), 'iron': (190, 192, 200), 'gold': (230, 190, 60), 'diamond': (80, 200, 220), 'netherite': (80, 68, 74)}
def plate(c, edge=True):
    R = ramp(c, True); cv = Cv(); rect(cv, 0, 0, N, N, R[2])
    if edge:
        for i in range(N): cv.put(i, 0, R[4]); cv.put(0, i, R[3]); cv.put(i, N - 1, R[0]); cv.put(N - 1, i, R[1])
    return cv, R
def disc(cv, cx, cy, r, c):
    for y in range(N):
        for x in range(N):
            if (x - cx) ** 2 + (y - cy) ** 2 <= r * r: cv.put(x, y, c)
def save(ns, kind, name, cv):
    f = '%s/%s/textures/%s/%s.png' % (OUT, ns, kind, name); os.makedirs(os.path.dirname(f), exist_ok=True); cv.img().resize((64, 64), Image.NEAREST).save(f)
def card(name, accent, glyph):
    cv, R = Cv(), ramp((226, 222, 208))
    poly(cv, [(6, 3), (26, 3), (26, 29), (6, 29)], R[2]); rect(cv, 7, 4, 25, 5, R[4]); rect(cv, 25, 4, 26, 29, R[1])
    A = ramp(accent)
    if glyph == 'arrow': poly(cv, [(10, 14), (18, 14), (18, 10), (24, 16), (18, 22), (18, 18), (10, 18)], A[2])
    elif glyph == 'funnel': poly(cv, [(9, 9), (23, 9), (18, 17), (18, 23), (14, 23), (14, 17)], A[2])
    elif glyph == 'ring': disc(cv, 16, 16, 7, A[2]); disc(cv, 16, 16, 3, R[2])
    elif glyph == 'drop': poly(cv, [(16, 8), (22, 17), (22, 20), (16, 24), (10, 20), (10, 17)], A[2])
    elif glyph == 'bolt': poly(cv, [(18, 7), (11, 17), (16, 17), (14, 25), (22, 14), (17, 14)], A[2])
    elif glyph == 'cross': rect(cv, 14, 8, 18, 24, A[2]); rect(cv, 8, 14, 24, 18, A[2])
    elif glyph == 'x': 
        for i in range(-6, 7): cv.put(16 + i, 16 + i, A[2]); cv.put(17 + i, 16 + i, A[2]); cv.put(16 + i, 16 - i, A[2]); cv.put(17 + i, 16 - i, A[2])
    elif glyph == 'note': disc(cv, 13, 21, 3, A[2]); rect(cv, 15, 8, 17, 21, A[2]); rect(cv, 17, 8, 22, 11, A[2])
    elif glyph == 'flame': poly(cv, [(16, 7), (22, 16), (20, 24), (12, 24), (10, 16), (14, 12)], A[2]); poly(cv, [(16, 15), (19, 21), (13, 21)], A[4])
    elif glyph == 'stack': rect(cv, 9, 21, 23, 24, A[1]); rect(cv, 9, 16, 23, 19, A[2]); rect(cv, 9, 11, 23, 14, A[3])
    elif glyph == 'grid': rect(cv, 9, 9, 23, 23, A[1]); rect(cv, 10, 10, 15, 15, A[3]); rect(cv, 17, 10, 22, 15, A[3]); rect(cv, 10, 17, 15, 22, A[3]); rect(cv, 17, 17, 22, 22, A[3])
    elif glyph == 'magnet': rect(cv, 9, 10, 13, 22, A[2]); rect(cv, 19, 10, 23, 22, A[2]); rect(cv, 9, 8, 23, 12, A[2]); rect(cv, 9, 20, 13, 22, (220, 220, 230, 255)); rect(cv, 19, 20, 23, 22, (220, 220, 230, 255))
    elif glyph == 'food': disc(cv, 16, 17, 6, A[2]); rect(cv, 15, 8, 17, 12, (90, 140, 60, 255))
    cv.outline(R[0]); return cv
CARD = {'filter': 'funnel', 'filter_card': 'funnel', 'crafting_upgrade': 'grid', 'feeding_upgrade': 'food', 'jukebox_upgrade': 'note', 'magnet_upgrade': 'magnet', 'pickup_upgrade': 'arrow', 'smelting_upgrade': 'flame', 'stack_upgrade': 'stack', 'void_upgrade': 'x',
        'energy_module': 'bolt', 'extractor_module': 'arrow', 'filter_module': 'funnel', 'fluid_module': 'drop', 'puller_module': 'arrow', 'range_card': 'ring', 'round_robin_module': 'ring', 'router_link': 'ring', 'sender_module': 'arrow', 'sorter_module': 'cross', 'vacuum_module': 'ring', 'void_module': 'x'}
ACC = {'energy': (230, 60, 50), 'fluid': (60, 120, 230), 'filter': (80, 190, 110), 'void': (150, 70, 200), 'feeding': (230, 150, 50), 'smelting': (240, 110, 40), 'magnet': (210, 60, 70), 'jukebox': (60, 190, 190), 'crafting': (200, 160, 80), 'stack': (90, 140, 230)}
def accent(name):
    for k, v in ACC.items():
        if k in name: return v
    return col(name)
def backpack(tier):
    c = TIER[tier]; cv, R = Cv(), ramp(c, tier != 'wood')
    poly(cv, [(7, 10), (25, 10), (26, 27), (6, 27)], R[2]); rect(cv, 6, 8, 26, 12, R[3]); rect(cv, 12, 4, 20, 8, R[1]); rect(cv, 13, 5, 19, 7, R[0])
    rect(cv, 9, 17, 23, 24, R[1]); rect(cv, 10, 18, 22, 23, R[3]); rect(cv, 15, 19, 17, 22, (60, 50, 40, 255)); rect(cv, 9, 12, 10, 27, R[1]); rect(cv, 22, 12, 23, 27, R[1])
    cv.outline(R[0]); return cv
def house(name, tag):
    cv, R = plate((112, 116, 126))
    A = ramp(accent(name))
    if tag == 'funnel':
        poly(cv, [(5, 6), (27, 6), (20, 17), (20, 26), (12, 26), (12, 17)], R[0]); poly(cv, [(7, 8), (25, 8), (19, 16), (19, 24), (13, 24), (13, 16)], (40, 38, 40, 255)); rect(cv, 4, 4, 28, 6, R[4])
    elif tag == 'hole': rect(cv, 8, 8, 24, 24, R[0]); rect(cv, 9, 9, 23, 23, (30, 30, 34, 255)); disc(cv, 16, 16, 4, A[2]); disc(cv, 15, 15, 1, A[4])
    elif tag == 'node': disc(cv, 16, 16, 9, R[0]); disc(cv, 16, 16, 7, A[1]); disc(cv, 16, 16, 4, A[3]); disc(cv, 15, 15, 1, A[4])
    return cv
for ns, path in miss:
    kind, name = path.split('/'); short = ns
    if kind == 'item':
        if name.endswith('_backpack'): cv = backpack(name.split('_')[0])
        elif name == 'deep_null':
            cv, R = Cv(), ramp((40, 36, 52)); poly(cv, [(6, 10), (16, 5), (26, 10), (26, 22), (16, 28), (6, 22)], R[2]); poly(cv, [(6, 10), (16, 15), (16, 28), (6, 22)], R[1]); poly(cv, [(16, 15), (26, 10), (26, 22), (16, 28)], R[0]); G = ramp((90, 230, 140)); disc(cv, 16, 17, 3, G[3]); cv.outline((10, 8, 14, 255))
        elif name == 'wrench':
            cv, R = Cv(), ramp((150, 156, 168), True); poly(cv, [(8, 24), (20, 12), (23, 15), (11, 27)], R[2]); disc(cv, 22, 10, 5, R[2]); rect(cv, 22, 4, 26, 10, (0, 0, 0, 0)); poly(cv, [(22, 4), (27, 4), (27, 9), (24, 9)], (0, 0, 0, 0)); cv.outline(R[0])
        elif name == 'connector':
            cv, R = Cv(), ramp((150, 156, 168), True); rect(cv, 8, 12, 24, 20, R[2]); rect(cv, 4, 14, 8, 18, R[1]); rect(cv, 24, 14, 28, 18, R[1]); rect(cv, 8, 12, 24, 14, R[4]); disc(cv, 16, 16, 2, ramp(accent(name))[3]); cv.outline(R[0])
        else: cv = card(name, accent(name), CARD.get(name, 'ring'))
        save(ns, 'item', name, cv)
    else:
        if name.endswith('_storage'):
            t = name.split('_')[0]; cv, R = plate(TIER[t], True); rect(cv, 3, 12, 29, 15, R[1]); rect(cv, 13, 10, 19, 20, R[0]); rect(cv, 14, 12, 18, 18, ramp((220, 190, 80), True)[3]); rect(cv, 3, 12, 29, 13, R[4])
        elif name.startswith('linked_'):
            cv, R = plate((70, 56, 100)); A = ramp((160, 100, 255)); rect(cv, 5, 5, 27, 27, R[0]); rect(cv, 7, 7, 25, 25, R[1])
            disc(cv, 16, 16, 6, A[2]); disc(cv, 16, 16, 3, A[4]) if name != 'linked_tank' else rect(cv, 9, 14, 23, 25, ramp((70, 130, 235))[2])
        elif name.endswith('_pipe'):
            cv, R = plate((124, 128, 138)); A = ramp(accent(name)); disc(cv, 16, 16, 9, R[0]); disc(cv, 16, 16, 7, R[3]); disc(cv, 16, 16, 5, A[2]); disc(cv, 15, 15, 2, A[4])
        elif name.startswith('cable_'):
            cv, R = plate((58, 62, 72), False); A = ramp(accent(name)); rect(cv, 11, 11, 21, 21, R[0]); rect(cv, 12, 12, 20, 20, A[2]); rect(cv, 13, 13, 17, 17, A[4]) if name == 'cable_core' else None
            if name == 'cable_arm': rect(cv, 20, 14, 30, 18, A[1])
            if name == 'cable_inventory': rect(cv, 6, 6, 26, 26, A[1]); rect(cv, 8, 8, 24, 24, R[1]); rect(cv, 12, 12, 20, 20, A[2])
        elif name in ('filter_hopper', 'copper_filter_hopper', 'fast_hopper', 'item_collector'):
            cv = house(name if name != 'copper_filter_hopper' else 'copper', 'funnel' if 'hopper' in name else 'hole')
            if name == 'copper_filter_hopper':
                cv, R = plate((196, 118, 76)); poly(cv, [(5, 6), (27, 6), (20, 17), (20, 26), (12, 26), (12, 17)], R[0]); poly(cv, [(7, 8), (25, 8), (19, 16), (19, 24), (13, 24), (13, 16)], (40, 30, 30, 255))
        elif name in ('router', 'laser_node'): cv = house(name, 'node' if name == 'laser_node' else 'hole')
        else: cv, _ = plate(col(name))
        save(ns, 'block', name, cv)
print(len(miss), 'done')
