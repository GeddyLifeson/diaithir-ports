import sys, os, json, random, subprocess
import numpy as np
from PIL import Image
B = '/home/user/diaithirrpg/tools/art/blender/baked/entity/assets/minecraft/textures/entity/'; OLD = '/tmp/diaithir-bosses/src/main/resources/assets/diaithirbosses/textures/entity/'; OUT = sys.argv[1] + '/diaithirbosses/textures/entity/'
KINDS = {'zombie': 'zombie/zombie', 'illager': 'illager/vindicator', 'golem': 'iron_golem/iron_golem', 'spider': 'spider/spider', 'ravager': 'illager/ravager', 'blaze': 'blaze/blaze', 'silverfish': 'silverfish/silverfish', 'phantom': 'phantom/phantom', 'guardian': 'guardian/guardian', 'hoglin': 'hoglin/hoglin'}
ASC = {'antlered_hollow', 'cauldron_heart', 'tide_reaver', 'foundry_sovereign', 'mist_weaver', 'ash_seraph', 'dusk_wyrm', 'rootbreaker', 'last_wheel'}
ls = subprocess.run(['git', '-C', '/home/user/diaithir-ports', 'ls-tree', '-r', '--name-only', 'FETCH_HEAD', 'diaithir-bosses/src/main/resources/data/diaithirbosses/boss/'], capture_output=True, text=True).stdout.split()
def lumof(a): return (a[..., :3] * np.array([.3, .59, .11])).sum(-1)
n = 0
for f in ls:
    bid = f.split('/')[-1][:-5]
    if bid in ASC or not f.endswith('.json'): continue
    kind = json.loads(subprocess.run(['git', '-C', '/home/user/diaithir-ports', 'show', 'FETCH_HEAD:' + f], capture_output=True, text=True).stdout)['own']['kind']
    old = np.asarray(Image.open(OLD + bid + '.png').convert('RGBA')).astype(np.float32) / 255; m = old[..., 3] > .5; ol = lumof(old)[m]; oc = old[..., :3][m]
    q = np.percentile(ol, [8, 50, 92]); ramp = []
    for t in q: sel = np.abs(ol - t) < 0.04; ramp.append(oc[sel].mean(0) if sel.sum() > 5 else oc.mean(0))
    d, mi, l = ramp; rune = tuple(int(x) for x in np.clip(l * 255 * 1.15 + 20, 0, 255))
    p = B + KINDS[kind] + '.png'
    if not os.path.exists(p): print('missing base', kind, bid); continue
    im = Image.open(p).convert('RGBA'); a = np.asarray(im).astype(np.float32) / 255; rgb, al = a[..., :3], a[..., 3]; lum = lumof(a)
    lo, hi = np.percentile(lum[al > .5], 4), np.percentile(lum[al > .5], 96); t = np.clip((lum - lo) / max(1e-3, hi - lo), 0, 1)[..., None]
    out = np.where(t < .5, d + (mi - d) * t * 2, mi + (l - mi) * (t - .5) * 2)
    rnd = random.Random(bid); H, W = al.shape; sc = max(1, W // 64); px = out.copy(); cand = np.argwhere((al > .9) & (t[..., 0] > .3) & (t[..., 0] < .85))
    for _ in range(int(len(cand) * 0.0026)):
        y, x = cand[rnd.randrange(len(cand))]; ln = rnd.choice((3, 4, 5)) * sc; vert = rnd.random() < .5
        for i in range(ln):
            yy, xx = (y + i, x) if vert else (y, x + i)
            if yy < H and xx < W and al[yy, xx] > .9: px[yy, xx] = np.array(rune) / 255
            if i in (1, 3):
                for k in range(1, 3 * sc // 2 + 1):
                    y2, x2 = (yy, xx + k) if vert else (yy + k, xx)
                    if y2 < H and x2 < W and al[y2, x2] > .9: px[y2, x2] = np.array(rune) / 255
    if (W, H) != Image.open(OLD + bid + '.png').size: print('size differs', bid, (W, H), Image.open(OLD + bid + '.png').size)
    Image.fromarray((np.dstack([px, al]) * 255).clip(0, 255).astype(np.uint8), 'RGBA').save(OUT + bid + '.png'); n += 1
print(n, 'base skins')
