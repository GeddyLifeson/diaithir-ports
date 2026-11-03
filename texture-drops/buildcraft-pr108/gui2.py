"""Redraws BuildCraft GUI sheets from scratch: reads only the LAYOUT of each original (where panels, slots, bars, arrows and pages sit), then draws every element with our own code. No original pixel colours or shapes are copied."""
import sys, os, glob, math, random, hashlib, colorsys
import numpy as np
from PIL import Image
from scipy import ndimage as ndi
OUT = sys.argv[1]
def rr(n): return random.Random(int(hashlib.md5(n.encode()).hexdigest()[:8], 16))
PANEL = np.array([171, 171, 167]); PAN_HI = np.array([234, 232, 226]); PAN_LO = np.array([30, 30, 32]); SLOT = np.array([55, 55, 57]); SLOT_LO = np.array([23, 23, 24]); SLOT_HI = np.array([87, 87, 89])
def tone(rgb):
    v = rgb.mean(-1); mx = rgb.max(-1); mn = rgb.min(-1); sat = (mx - mn) / np.maximum(mx, 1)
    t = np.full(v.shape, 'p', dtype='<U1'); t[v < 30] = 'k'; t[(v >= 30) & (v < 100)] = 'd'; t[(v >= 100) & (v < 170)] = 'i'; t[(v >= 170) & (v < 230)] = 'p'; t[v >= 230] = 'w'; t[sat > .22] = 'c'; return t
def draw_panel(o, x0, y0, x1, y1, rnd):
    w, h = x1 - x0, y1 - y0
    yy, xx = np.mgrid[:h, :w]; base = np.zeros((h, w, 3), np.float32); base[:] = PANEL; base += rnd.uniform(-1, 1) + np.array([[rnd.uniform(-2.5, 2.5)] * 3 for _ in range(w * h)]).reshape(h, w, 3)
    base[0:2] = PAN_HI; base[:, 0:2] = PAN_HI; base[h - 2:] = PAN_LO + 25; base[:, w - 2:] = PAN_LO + 25; base[h - 1:] = PAN_LO; base[:, w - 1:] = PAN_LO; base[0:1] = PAN_HI + 0; 
    for (cy, cx) in ((0, 0), (0, w - 1), (h - 1, 0), (h - 1, w - 1)): base[cy, cx] = 0
    o[y0:y1, x0:x1, :3] = np.clip(base, 0, 255); o[y0:y1, x0:x1, 3] = 255
    for (cy, cx) in ((0, 0), (0, w - 1), (h - 1, 0), (h - 1, w - 1)): o[y0 + cy, x0 + cx, 3] = 0
def draw_slot(o, x0, y0, x1, y1):
    o[y0:y1, x0:x1, :3] = SLOT; o[y0:y1, x0:x1, 3] = 255
    o[y0:y0 + 1, x0:x1, :3] = SLOT_LO; o[y0:y1, x0:x0 + 1, :3] = SLOT_LO; o[y1 - 1:y1, x0:x1, :3] = SLOT_HI; o[y0:y1, x1 - 1:x1, :3] = SLOT_HI
def draw_line(o, x0, y0, x1, y1, col): o[y0:y1, x0:x1, :3] = col; o[y0:y1, x0:x1, 3] = 255
def draw_arrow(o, x0, y0, x1, y1):
    w, h = x1 - x0, y1 - y0; cy = (y0 + y1) // 2; col = SLOT; t = max(2, h // 3)
    o[cy - t // 2:cy - t // 2 + t, x0:x1 - h // 2 - 1, :3] = col; o[cy - t // 2:cy - t // 2 + t, x0:x1 - h // 2 - 1, 3] = 255
    for i in range(h // 2 + 1):
        yy0 = cy - (h // 2 - i); yy1 = cy + (h // 2 - i) + 1; xx = x1 - h // 2 - 1 + i
        if 0 <= xx < o.shape[1]: o[max(0, yy0):yy1, xx, :3] = col; o[max(0, yy0):yy1, xx, 3] = 255
def draw_plus(o, x0, y0, x1, y1):
    cx, cy = (x0 + x1) // 2, (y0 + y1) // 2; t = max(2, (x1 - x0) // 4)
    o[cy - t // 2:cy - t // 2 + t, x0:x1, :3] = SLOT; o[cy - t // 2:cy - t // 2 + t, x0:x1, 3] = 255; o[y0:y1, cx - t // 2:cx - t // 2 + t, :3] = SLOT; o[y0:y1, cx - t // 2:cx - t // 2 + t, 3] = 255
def shift(rgb):
    r, g, b = [c / 255 for c in rgb]; h, s, v = colorsys.rgb_to_hsv(r, g, b); r, g, b = colorsys.hsv_to_rgb((h + .08) % 1, min(1, s * .9), min(1, v * .95 + .03)); return np.array([r, g, b]) * 255
def draw_col(o, x0, y0, x1, y1, rgb):
    w, h = x1 - x0, y1 - y0; c = shift(rgb)
    if min(w, h) <= 2: o[y0:y1, x0:x1, :3] = c; o[y0:y1, x0:x1, 3] = 255; return
    g = np.linspace(1.18, .82, h)[:, None, None] * np.ones((1, w, 1)); o[y0:y1, x0:x1, :3] = np.clip(c[None, None, :] * g, 0, 255); o[y0:y1, x0:x1, 3] = 255
    o[y0:y1, x0:x0 + 1, :3] = c * .6; o[y0:y1, x1 - 1:x1, :3] = c * .6; o[y0:y0 + 1, x0:x1, :3] = c * .6; o[y1 - 1:y1, x0:x1, :3] = c * .6
def sheet(f, name):
    a = np.asarray(Image.open(f).convert('RGBA')); H, W = a.shape[:2]; rnd = rr(name); o = np.zeros((H, W, 4), np.uint8); o[..., :3] = 0
    op = a[..., 3] > 128; tn = tone(a[..., :3].astype(np.int32))
    lab, nl = ndi.label(op, structure=np.ones((3, 3)))
    boxes = ndi.find_objects(lab)
    for i, sl in enumerate(boxes):
        h, w = sl[0].stop - sl[0].start, sl[1].stop - sl[1].start
        if h * w < 36: continue
        if h >= 14 and w >= 14 and (op[sl] & (tn[sl] == 'p')).sum() > .12 * h * w: draw_panel(o, sl[1].start, sl[0].start, sl[1].stop, sl[0].stop, rnd)
    cov = o[..., 3] > 0
    ins = (tn == 'i') & op; li, nli = ndi.label(ins)
    for sl in ndi.find_objects(li):
        h, w = sl[0].stop - sl[0].start, sl[1].stop - sl[1].start
        if h * w < 8: continue
        fill = (li[sl] > 0).mean(); y0, y1, x0, x1 = sl[0].start, sl[0].stop, sl[1].start, sl[1].stop
        if min(w, h) <= 3: draw_line(o, x0, y0, x1, y1, SLOT_HI)
        elif fill < .72 and w >= 8 and h >= 6 and w > h * 1.15: draw_arrow(o, x0, y0, x1, y1)
        elif fill < .72 and abs(w - h) <= 3 and w <= 16: draw_plus(o, x0, y0, x1, y1)
        else: draw_slot(o, x0, y0, x1, y1)
    cm = (tn == 'c') & op; lc, nc = ndi.label(cm, structure=np.ones((3, 3)))
    for k, sl in enumerate(ndi.find_objects(lc), 1):
        y0, y1, x0, x1 = sl[0].start, sl[0].stop, sl[1].start, sl[1].stop; reg = (lc[sl] == k)
        col = a[sl][reg][:, :3].mean(0)
        draw_col(o, x0, y0, x1, y1, col)
    # dark/black line art outside panels (e.g. bar ticks): thin dark lines drawn as our shadow colour
    dk = ((tn == 'd') | (tn == 'k')) & op & (~cov); ld, nd = ndi.label(dk)
    for sl in ndi.find_objects(ld):
        h, w = sl[0].stop - sl[0].start, sl[1].stop - sl[1].start
        if min(w, h) <= 2 and max(w, h) >= 3: draw_line(o, sl[1].start, sl[0].start, sl[1].stop, sl[0].stop, PAN_LO)
    return o
def parchment(o, x0, y0, x1, y1, rnd, corner=6):
    w, h = x1 - x0, y1 - y0; low = np.array([[rnd.uniform(-1, 1) for _ in range(max(2, w // 16))] for _ in range(max(2, h // 16))], np.float32)
    big = np.asarray(Image.fromarray(((low + 1) * 127).astype(np.uint8)).resize((w, h), Image.BICUBIC)).astype(np.float32) / 127 - 1
    fib = np.array([[rnd.uniform(-1, 1) for _ in range(w)] for _ in range(h)], np.float32)
    c = np.array([246, 236, 208], np.float32)[None, None, :] + (big[..., None] * 9) + fib[..., None] * 3
    yy, xx = np.mgrid[:h, :w]; d = np.minimum.reduce([xx, yy, w - 1 - xx, h - 1 - yy]).astype(np.float32); c *= (1 - .14 * np.clip(1 - d / 10, 0, 1))[..., None] * np.ones((1, 1, 3))
    c[:, :, 2] *= (1 - .06 * np.clip(1 - d / 6, 0, 1))
    o[y0:y1, x0:x1, :3] = np.clip(c, 0, 255); o[y0:y1, x0:x1, 3] = 255
    for (cy, cx) in [(i, j) for i in range(corner) for j in range(corner) if (corner - i) + (corner - j) > corner + 1]:
        for (py, px) in ((y0 + cy, x0 + cx), (y0 + cy, x1 - 1 - cx), (y1 - 1 - cy, x0 + cx), (y1 - 1 - cy, x1 - 1 - cx)): o[py, px, 3] = 0
def leather(o, x0, y0, x1, y1, rnd):
    w, h = x1 - x0, y1 - y0; yy = np.linspace(1.12, .86, h)[:, None, None] * np.ones((1, w, 1)); c = np.array([120, 66, 44], np.float32)[None, None, :] * yy
    c += np.array([[rnd.uniform(-5, 5)] * 3 for _ in range(w * h)]).reshape(h, w, 3)
    o[y0:y1, x0:x1, :3] = np.clip(c, 0, 255); o[y0:y1, x0:x1, 3] = 255; o[y0:y0 + 2, x0:x1, :3] = (150, 92, 66); o[y1 - 2:y1, x0:x1, :3] = (54, 30, 22); o[y0:y1, x0:x0 + 2, :3] = (140, 84, 58); o[y0:y1, x1 - 2:x1, :3] = (54, 30, 22)
    for (py, px) in ((y0, x0), (y0, x1 - 1), (y1 - 1, x0), (y1 - 1, x1 - 1)): o[py, px, 3] = 0
def page(f, name):
    a = np.asarray(Image.open(f).convert('RGBA')); H, W = a.shape[:2]; rnd = rr(name); o = np.zeros((H, W, 4), np.uint8); op = a[..., 3] > 128
    ys, xs = np.where(op); x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
    lum = a[..., :3].astype(np.float32).mean(-1); par = op & (lum > 200); 
    if name == 'note':
        parchment(o, x0, y0, x1, y1, rnd, 8); return o
    leather(o, x0, y0, x1, y1, rnd)
    if par.sum() > 500:
        ys, xs = np.where(par); px0, px1, py0, py1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
        parchment(o, px0, py0, px1, py1, rnd, 5)
        for i in range(6): yy = py0 + 20 + i * (py1 - py0 - 40) // 6; o[yy:yy + 1, px0 + 2:px0 + 4, :3] = (170, 140, 100)   # binding stitches
    return o
def icons(f, name):
    o = sheet(f, name); return o
n = 0
for f in sorted(glob.glob('/tmp/bcgui/*.png')):
    base = os.path.basename(f)[:-4]
    if '__jei__' in base: continue
    if base.startswith('guide__'):
        stem = base[7:]; o = page(f, stem) if stem != 'icons' else icons(f, stem); d = '%s/buildcraft/guide/gui/%s.png' % (OUT, stem)
    else:
        ns, rel = base.split('__', 1); o = sheet(f, base); d = '%s/%s/textures/%s.png' % (OUT, ns, rel.replace('__', '/'))
    os.makedirs(os.path.dirname(d), exist_ok=True); Image.fromarray(o, 'RGBA').save(d); n += 1
print(n)
