import sys, os, glob, colorsys, re
import numpy as np
from PIL import Image
OUT = sys.argv[1]
ANCH = {0: (23, 23, 24), 55: (30, 30, 32), 85: (87, 87, 89), 139: (55, 55, 57), 198: (171, 171, 167), 255: (234, 232, 226)}
keys = np.array(sorted(ANCH))
def conv(a):
    rgb = a[..., :3].astype(np.int32); out = rgb.copy(); grey = (rgb.max(-1) - rgb.min(-1)) <= 6; v = rgb.mean(-1)
    idx = np.abs(v[..., None] - keys[None, None, :]).argmin(-1); tbl = np.array([ANCH[k] for k in keys]); out[grey] = tbl[idx[grey]]
    col = ~grey
    if col.any():
        for y, x in np.argwhere(col):
            r, g, b = rgb[y, x] / 255; h, s, vv = colorsys.rgb_to_hsv(r, g, b); r2, g2, b2 = colorsys.hsv_to_rgb((h + .06) % 1, min(1, s * .95), min(1, vv * 1.04)); out[y, x] = (int(r2 * 255), int(g2 * 255), int(b2 * 255))
    return np.dstack([out, a[..., 3]]).astype(np.uint8)
n = 0
for f in sorted(glob.glob('/tmp/bcgui/*.png')):
    base = os.path.basename(f)[:-4]; ns, rel = base.split('__', 1)
    if '__jei__' in base: continue
    a = np.asarray(Image.open(f).convert('RGBA')); o = conv(a); d = '%s/%s/textures/%s.png' % (OUT, ns, rel.replace('__', '/'))
    os.makedirs(os.path.dirname(d), exist_ok=True); Image.fromarray(o, 'RGBA').save(d); n += 1
print(n)
