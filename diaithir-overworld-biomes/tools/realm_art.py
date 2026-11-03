"""Procedural 64 px block and item art for the realms (own design; no third-party pixels).
Palette comes from material plus keywords in the id, detail from the id hash, so every block looks distinct and
a stair or slab matches its parent."""
import hashlib, colorsys, io, re
import numpy as np
from PIL import Image

N = 64
KEY = [  # (regex, hue shift or fixed hue, sat mult, val mult)
    (r'(frozen|frost|ice|shiver|icy|snow|cold)', ('h', 0.55), 0.45, 1.35), (r'(ash|ashen|sinking|grey|gray|fossil|calcified|faded|nullstone|cracked)', ('h', 0.08), 0.12, 1.0),
    (r'(blood|red|rogdor|ember|fire)', ('h', 0.99), 1.0, 1.0), (r'(gold|honey|wax|amber|royal|jelly|sugar|pollen)', ('h', 0.11), 1.1, 1.3),
    (r'(moss|mog|leaf|grass|verdant|green|bush|fern)', ('h', 0.30), 1.0, 1.0), (r'(ink|indigo|veil|deep|dread|blue|aercloud|cloud|quick|sky|holy)', ('h', 0.62), 1.0, 1.0),
    (r'(rot|rotten|decay|infected|sick|toxic|virulent)', ('h', 0.20), 0.7, 0.8), (r'(purple|violet|magnetic|zanite|gravit)', ('h', 0.78), 1.0, 1.0),
]
BASE = {'stone': (0.60, 0.10, 0.45), 'wood': (0.08, 0.50, 0.50), 'log': (0.07, 0.45, 0.35), 'leaves': (0.30, 0.60, 0.40), 'soil': (0.07, 0.45, 0.33),
        'turf': (0.28, 0.55, 0.45), 'metal': (0.58, 0.10, 0.62), 'ore': (0.60, 0.10, 0.45), 'glass': (0.55, 0.15, 0.85), 'mushroom': (0.02, 0.55, 0.55),
        'plant': (0.30, 0.65, 0.50), 'portal': (0.75, 0.70, 0.75)}

def rng(seed): return np.random.default_rng(int(hashlib.sha256(seed.encode()).hexdigest()[:12], 16))
def color(seed, mat, name):
    h, s, v = BASE[mat]
    for rx, hv, sm, vm in KEY:
        if re.search(rx, name): h = hv[1]; s = min(1, s * sm); v = min(1.0, v * vm); break
    r = rng('c' + seed); h = (h + r.uniform(-0.03, 0.03)) % 1.0; s = float(np.clip(s * r.uniform(0.9, 1.1), 0, 1)); v = float(np.clip(v * r.uniform(0.9, 1.1), 0.08, 1))
    return h, s, v
def noise(r, scale):
    g = r.random((N // scale + 2, N // scale + 2)); im = Image.fromarray((g * 255).astype('uint8')).resize((N, N), Image.BICUBIC)
    return np.asarray(im, dtype=float) / 255.0
def to_img(hsv_v, h, s, alpha=None, tintmap=None):
    v = np.clip(hsv_v, 0, 1); out = np.zeros((N, N, 4), 'uint8')
    for y in range(N):
        for x in range(N):
            hh = h if tintmap is None else (h + tintmap[y, x]) % 1.0
            rr, gg, bb = colorsys.hsv_to_rgb(hh, s, v[y, x]); out[y, x] = (int(rr * 255), int(gg * 255), int(bb * 255), 255 if alpha is None else int(alpha[y, x]))
    buf = io.BytesIO(); Image.fromarray(out, 'RGBA').save(buf, 'PNG'); return buf.getvalue()

def block(seed, mat, name, top=False):
    r = rng('b' + seed + ('t' if top else '')); h, s, v = color(seed, mat, name)
    base = v * (0.8 + 0.4 * noise(r, 8)) + 0.08 * (noise(r, 2) - 0.5)
    yy, xx = np.mgrid[0:N, 0:N]
    alpha = None; tint = None
    if mat in ('stone', 'metal', 'ore'):
        if re.search(r'(brick|tile|flagstone|plating|capstone|keystone)', name):
            sh = 16 if 'tile' in name else 32
            seam = ((yy % sh) < 2) | (((xx + (yy // sh) * (sh // 2)) % (sh * 2 if sh == 32 else sh)) < 2)
            base = np.where(seam, base * 0.55, base * 1.05)
        elif mat == 'metal' or 'plating' in name:
            base = base * (1 - 0.35 * (((yy % 32) < 2) | ((xx % 32) < 2)))
            for cy in (6, 57):
                for cx in (6, 57): base[(yy - cy) ** 2 + (xx - cx) ** 2 < 6] *= 1.5
        if mat == 'ore':
            oh = rng('o' + name.split('_')[-2 if name.endswith('_ore') else -1]).random(); blobs = noise(r, 6) > 0.72
            tint = np.where(blobs, 0.0, 0.0); base = np.where(blobs, 0.9, base)
            oc = colorsys.hsv_to_rgb(oh, 0.75, 0.95); out = np.zeros((N, N, 4), 'uint8'); b0 = to_img(base, h, s)
            im = np.asarray(Image.open(io.BytesIO(b0)).convert('RGBA')).copy(); im[blobs] = (int(oc[0] * 255), int(oc[1] * 255), int(oc[2] * 255), 255)
            buf = io.BytesIO(); Image.fromarray(im, 'RGBA').save(buf, 'PNG'); return buf.getvalue()
    elif mat == 'wood':
        planks = ((yy % 16) < 1) ; grain = noise(r, 2)[:, :] * 0.15
        base = np.where(planks, base * 0.6, base) + grain * np.sin(xx / 5.0) * 0.4
    elif mat == 'log':
        if top:
            ring = np.sin(np.sqrt((xx - 32) ** 2 + (yy - 32) ** 2) * 0.9) * 0.12
            base = base * 1.25 + ring; base[(abs(xx - 32) > 29) | (abs(yy - 32) > 29)] *= 0.55
        else: base = base * (0.75 + 0.5 * np.abs(np.sin(xx * 0.7 + noise(r, 4) * 4)))
    elif mat == 'leaves':
        base = base * (0.6 + 0.8 * noise(r, 3)); holes = noise(r, 4) > 0.8; alpha = np.where(holes, 0, 255)
        tint = (noise(r, 8) - 0.5) * 0.06
    elif mat in ('soil', 'turf'):
        base = base * (0.7 + 0.6 * noise(r, 3)) + 0.2 * (noise(r, 1) > 0.85)
        if mat == 'turf' and not top: base = np.where(yy < 14, base, base * 0.8)
    elif mat == 'glass':
        frame = (xx < 3) | (xx > 60) | (yy < 3) | (yy > 60); base = np.where(frame, 0.9, 0.95 + 0.05 * noise(r, 8)); alpha = np.where(frame, 255, 70)
    elif mat == 'mushroom':
        dots = noise(r, 6) > 0.78; base = np.where(dots, 0.95, base * (0.8 + 0.4 * noise(r, 5)))
    elif mat == 'portal':
        ang = np.arctan2(yy - 32, xx - 32); rad = np.hypot(yy - 32, xx - 32)
        base = 0.55 + 0.4 * np.sin(rad * 0.45 + ang * 3 + r.random() * 6); alpha = np.full((N, N), 150)
    return to_img(base, h, s, alpha, tint)

def plant(seed, name):
    r = rng('p' + seed); h, s, v = color(seed, 'plant', name); out = np.zeros((N, N, 4), 'uint8')
    mush = bool(re.search(r'(mushroom|shroom|windcap)', name)); blades = r.integers(5, 9)
    for _ in range(blades):
        x0 = int(r.integers(10, 54)); ht = int(r.integers(22, 58)); lean = r.uniform(-0.25, 0.25); w = int(r.integers(2, 5))
        for t in range(ht):
            x = int(x0 + lean * t); y = N - 1 - t
            for dx in range(w):
                if 0 <= x + dx < N:
                    rr, gg, bb = colorsys.hsv_to_rgb((h + r.uniform(-0.02, 0.02)) % 1, s, float(np.clip(v * (0.7 + 0.5 * t / ht), 0, 1)))
                    out[y, x + dx] = (int(rr * 255), int(gg * 255), int(bb * 255), 255)
        if mush:
            for dy in range(-5, 3):
                for dx in range(-9, 10):
                    if dx * dx / 81 + dy * dy / 25 <= 1 and 0 <= N - ht + dy < N and 0 <= x0 + dx < N:
                        rr, gg, bb = colorsys.hsv_to_rgb((h + 0.5) % 1, 0.6, 0.85); out[N - ht + dy, x0 + dx] = (int(rr * 255), int(gg * 255), int(bb * 255), 255)
    buf = io.BytesIO(); Image.fromarray(out, 'RGBA').save(buf, 'PNG'); return buf.getvalue()

def item(seed, name):
    r = rng('i' + seed); h, s, v = color(seed, 'metal' if re.search(r'(ingot|nugget|crystal|raw|rogdorium)', name) else 'plant', name)
    out = np.zeros((N, N, 4), 'uint8'); yy, xx = np.mgrid[0:N, 0:N]
    if name.endswith('ingot'): m = (abs(xx - 32) + abs(yy - 34) * 2.2) < 26
    elif name.endswith('nugget'): m = (xx - 32) ** 2 + (yy - 38) ** 2 < 120
    elif name.endswith('crystal'): m = (abs(xx - 32) * 1.6 + abs(yy - 32)) < 24
    elif name.startswith('raw_'): m = ((xx - 32) ** 2 + (yy - 34) ** 2 < 360) & (noise(r, 8) > 0.3)
    else: m = ((xx - 32) ** 2 / 500 + (yy - 32) ** 2 / 360 < 1) & (noise(r, 10) > 0.2)
    shade = 0.75 + 0.4 * noise(r, 6) - 0.25 * (yy / N)
    for y in range(N):
        for x in range(N):
            if m[y, x]:
                rr, gg, bb = colorsys.hsv_to_rgb(h, s, float(np.clip(v * 1.2 * shade[y, x], 0, 1))); out[y, x] = (int(rr * 255), int(gg * 255), int(bb * 255), 255)
    buf = io.BytesIO(); Image.fromarray(out, 'RGBA').save(buf, 'PNG'); return buf.getvalue()
