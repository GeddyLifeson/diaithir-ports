import sys, os, random
import numpy as np
from PIL import Image
B = '/home/user/diaithirrpg/tools/art/blender/baked/entity/assets/minecraft/textures/entity/'; OUT = sys.argv[1] + '/diaithirbosses/textures/entity/'; os.makedirs(OUT, exist_ok=True)
RAMP = {  # boss: (base skin, dark, mid, light, rune colour, rune density)
    'antlered_hollow': ('hoglin/hoglin', (0.06, 0.05, 0.04), (0.38, 0.30, 0.20), (0.86, 0.80, 0.62), (210, 235, 140), 0.0016),
    'cauldron_heart': ('iron_golem/iron_golem', (0.04, 0.07, 0.06), (0.22, 0.42, 0.36), (0.70, 0.92, 0.74), (120, 255, 190), 0.0016),
    'tide_reaver': ('guardian/guardian', (0.02, 0.07, 0.12), (0.12, 0.38, 0.52), (0.62, 0.92, 0.98), (160, 245, 255), 0.0016),
    'foundry_sovereign': ('iron_golem/iron_golem', (0.08, 0.05, 0.03), (0.48, 0.30, 0.16), (0.98, 0.72, 0.28), (255, 190, 70), 0.0022),
    'mist_weaver': ('spider/spider', (0.07, 0.07, 0.10), (0.46, 0.48, 0.58), (0.92, 0.94, 0.98), (200, 220, 255), 0.0022),
    'ash_seraph': ('blaze/blaze', (0.06, 0.05, 0.05), (0.40, 0.36, 0.34), (0.92, 0.88, 0.82), (255, 140, 70), 0.0022),
    'dusk_wyrm': ('phantom/phantom', (0.03, 0.02, 0.07), (0.22, 0.14, 0.40), (0.72, 0.56, 0.96), (200, 150, 255), 0.0028),
    'rootbreaker': ('illager/ravager', (0.04, 0.06, 0.03), (0.24, 0.34, 0.14), (0.74, 0.86, 0.44), (190, 240, 100), 0.0028),
    'last_wheel': ('illager/vindicator', (0.07, 0.06, 0.02), (0.60, 0.50, 0.14), (1.00, 0.94, 0.56), (255, 240, 150), 0.0028),
}
def lerp(a, b, t): return a + (b - a) * t
for name, (src, d, m, l, rune, dens) in RAMP.items():
    im = Image.open(B + src + '.png').convert('RGBA'); a = np.asarray(im).astype(np.float32) / 255; rgb, al = a[..., :3], a[..., 3]
    lum = (rgb * np.array([.3, .59, .11])).sum(-1)
    lo, hi = np.percentile(lum[al > .5], 4), np.percentile(lum[al > .5], 96); t = np.clip((lum - lo) / max(1e-3, hi - lo), 0, 1)[..., None]
    d, m, l = map(np.array, (d, m, l)); out = np.where(t < .5, lerp(d, m, t * 2), lerp(m, l, (t - .5) * 2))
    rnd = random.Random(name); H, W = al.shape; sc = max(1, W // 64); px = out.copy()
    cand = np.argwhere((al > .9) & (t[..., 0] > .35) & (t[..., 0] < .8))
    for _ in range(int(len(cand) * dens)):
        y, x = cand[rnd.randrange(len(cand))]; ln = rnd.choice((3, 4, 5)) * sc; vert = rnd.random() < .5
        for i in range(ln):
            yy, xx = (y + i, x) if vert else (y, x + i)
            if yy < H and xx < W and al[yy, xx] > .9: px[yy, xx] = np.array(rune) / 255
            if i in (1, 3):
                for k in range(1, 3 * sc // 2 + 1):
                    y2, x2 = (yy, xx + k) if vert else (yy + k, xx)
                    if y2 < H and x2 < W and al[y2, x2] > .9: px[y2, x2] = np.array(rune) / 255
    Image.fromarray((np.dstack([px, al])*255).clip(0, 255).astype(np.uint8), 'RGBA').save(OUT + name + '.png')
    print(name, im.size)
