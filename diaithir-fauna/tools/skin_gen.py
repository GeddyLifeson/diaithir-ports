#!/usr/bin/env python3
"""Skins for gen: species: palette -> flat 16x source skin -> creature-art paint.py (AO from ao_bake.py per plan).

    python3 skin_gen.py ART_DIR OUT_DIR [--only id,id] [--scale 4] [--jobs 4]
ART_DIR holds paint.py, eyes.py (copied from /mnt/project-files/animals-art), models.json (plans) and ao/<plan>.png (4x AO per plan).
Output: OUT_DIR/<species id>.png. Materials per plan below; paint.py's height fields do the rest (fur, feather, hide, chitin ...).
"""
import json, os, re, sys, zlib
from concurrent.futures import ProcessPoolExecutor
import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import catalogue as C

KIND = {  # default material per plan
 **{p: 'fur' for p in 'mustelid viverrid rodent_large rodent_small lagomorph primate_ape primate_monkey primate_small giraffid tapir macropod marsupial_stout monotreme insectivore sloth_tree pinniped bat'.split()},
 'armadillo': 'fur', 'dolphin': 'skin', 'archaic_whale': 'skin',
 **{p: 'feather' for p in 'ratite raptor waterfowl wader galliform songbird corvid owl parrot penguin seabird pigeon terror_bird dromaeosaur'.split()},
 'crocodilian': 'hide', 'lizard': 'hide', 'frog': 'skin', 'sea_turtle': 'hide',
 'fish_fusiform': 'hide', 'fish_deep': 'hide', 'fish_flat': 'hide', 'ray': 'skin', 'eel': 'skin', 'syngnathid': 'chitin', 'fish_primitive': 'hide',
 'insect_flying': 'chitin', 'beetle': 'chitin', 'scorpion': 'chitin', 'crab': 'chitin', 'cephalopod': 'skin', 'jellyfish': 'gel', 'starfish': 'chitin',
 **{p: 'hide' for p in 'theropod sauropod ceratopsian ankylosaur stegosaur hadrosaur ornithopod_small pterosaur therapsid synapsid_sail'.split()},
 'ichthyosaur': 'skin', 'plesiosaur': 'skin', 'mosasaur': 'skin', 'early_tetrapod': 'skin', 'trilobite': 'chitin', 'eurypterid': 'chitin', 'ammonite': 'shell', 'giant_arthropod': 'chitin', 'placoderm': 'bone',
}
BIRDS = set('ratite raptor waterfowl wader galliform songbird corvid owl parrot penguin seabird pigeon terror_bird'.split())

def kind(plan, name):
    if re.match(r'eye_', name): return 'eye'
    if re.search(r'horn|claw|spike|beak|snout' if plan in BIRDS else r'horn|claw|spike|^head_beak', name) and not re.search(r'snout', name) or (plan in BIRDS and re.search(r'beak', name)): return 'horn'
    if re.search(r'tusk', name): return 'bone'
    if plan in BIRDS and re.match(r'(leg|arm)_', name): return 'hide'
    if re.search(r'mane|tuft|hump|crest', name) and KIND[plan] == 'fur': return 'shaggy'
    if re.match(r'wing', name): return 'wing' if plan in ('bat', 'pterosaur') else ('feather' if plan in BIRDS or plan == 'insect_flying' and False else KIND[plan])
    if re.search(r'body_shell|^plate', name): return 'shell' if 'plate' not in name else 'bone'
    if re.search(r'frill', name): return 'horn'
    if re.search(r'(leg|arm)_.*foot', name): return 'pad' if KIND[plan] == 'fur' else 'hide'
    return KIND[plan]

def hexrgb(h): return np.array([int(h[i:i + 2], 16) for i in (1, 3, 5)], np.float32)

def noise(rng, h, w, cell):
    g = rng.random((h // cell + 3, w // cell + 3)).astype(np.float32)
    im = Image.fromarray((g * 255).astype(np.uint8)).resize((w + 3 * cell, h + 3 * cell), Image.BICUBIC)
    return np.asarray(im, np.float32)[:h, :w] / 255

def faces(u, v, w, h, d):
    return [(u, v + d, d, h), (u + d, v + d, w, h), (u + d + w, v + d, d, h), (u + 2 * d + w, v + d, w, h), (u + d, v, w, d), (u + d + w, v, w, d)]

def make_src(model, base, accent, belly, pattern, seed, plan):
    tw, th = model['tw'], model['th']
    img = np.zeros((th, tw, 4), np.float32)
    rng = np.random.default_rng(seed)
    base, accent, belly = hexrgb(base), hexrgb(accent), hexrgb(belly)
    for name, p in model['parts'].items():
        if name.startswith('eye_'): continue
        acc = bool(re.search(r'mane|hump|crest|plate|frill|ear_|tail_tuft|horn|fin_dorsal', name)) or (plan in BIRDS and name.startswith('wing'))
        for b in p['boxes']:
            w, h, d = (int(round(t)) for t in b[3:6])
            for qi, (x, y, fw, fh) in enumerate(faces(*p['uv'], w, h, d)):
                if fw <= 0 or fh <= 0: continue
                yy = np.linspace(0, 1, fh)[:, None] * np.ones((1, fw))
                if qi == 4: col = base[None, None] * np.ones((fh, fw, 1)) * 0.92
                elif qi == 5: col = belly[None, None] * np.ones((fh, fw, 1))
                else:
                    g = np.clip((yy - 0.45) / 0.45, 0, 1)
                    if pattern == 'countershade': g = np.clip((yy - 0.35) / 0.2, 0, 1)
                    col = base * (1 - g[..., None]) + belly * g[..., None]
                if acc: col = accent[None, None] * np.ones((fh, fw, 1))
                m = None
                if not acc and pattern in ('spots', 'rosettes', 'blotch', 'mottled', 'stripes', 'bands', 'diamond', 'eyespot', 'saddle', 'mask'):
                    xx = np.arange(fw)[None, :] * np.ones((fh, 1))
                    if pattern == 'spots': m = noise(rng, fh, fw, 2) > 0.66
                    elif pattern == 'rosettes': n = noise(rng, fh, fw, 3); m = (n > 0.6) & (n < 0.72)
                    elif pattern == 'blotch': m = noise(rng, fh, fw, 4) > 0.58
                    elif pattern == 'mottled': m = noise(rng, fh, fw, 2) > 0.5
                    elif pattern == 'stripes': m = np.sin((xx + noise(rng, fh, fw, 3) * 3) * 2 * np.pi / max(3, fw / 4)) > 0.35
                    elif pattern == 'bands': m = np.sin((yy * fh + noise(rng, fh, fw, 3) * 2) * 2 * np.pi / max(3, fh / 3)) > 0.3
                    elif pattern == 'diamond': m = np.sin((xx + yy * fh) * 2 * np.pi / max(4, fw / 3)) * np.sin((xx - yy * fh) * 2 * np.pi / max(4, fw / 3)) > 0.4
                    elif pattern == 'eyespot': m = np.hypot(xx - fw / 2, yy * fh - fh / 2) < max(1.2, min(fw, fh) / 4)
                    elif pattern == 'saddle': m = np.full((fh, fw), qi == 4)
                    elif pattern == 'mask': m = (yy < 0.55) & (name == 'head' or name.startswith('head_'))
                    if m is not None and qi != 5:
                        col = np.where(m[..., None], col * 0.25 + accent * 0.75, col)
                img[y:y + fh, x:x + fw, :3] = col
                img[y:y + fh, x:x + fw, 3] = 255
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), 'RGBA')

def one(args):
    art, out, sid, plan, coat, scale = args
    sys.path.insert(0, art)
    import paint
    paint.material = lambda pl, sp, name: kind(pl, name)
    models = json.load(open(os.path.join(art, 'models.json')))
    model = models[plan]
    src = make_src(model, *coat, zlib.crc32(sid.encode()), plan)
    ao = Image.open(os.path.join(art, 'ao', plan + '.png'))
    paint.paint(model, plan, sid, 0, src, ao, scale).save(os.path.join(out, sid + '.png'))
    return sid

def jobs(art, out, only=None, scale=4):
    coats = {}
    for fn in os.listdir(os.path.join(C.CAT, 'catalogue')):
        if fn.startswith('coat_'):
            for l in open(os.path.join(C.CAT, 'catalogue', fn), encoding='utf8'):
                f = l.rstrip('\n').split('|')
                if len(f) == 7 and not l.startswith('#'): coats[f[0]] = (f[2], f[3], f[4], f[5])
    for kindname in ('mob', 'fossil'):
        for r in C.rows(kindname):
            if not r['plan'].startswith('gen:') or (only and r['id'] not in only) or r['id'] not in coats: continue
            yield (art, out, r['id'], r['plan'][4:], coats[r['id']], scale)

if __name__ == '__main__':
    art, out = sys.argv[1:3]
    only = set(sys.argv[sys.argv.index('--only') + 1].split(',')) if '--only' in sys.argv else None
    scale = int(sys.argv[sys.argv.index('--scale') + 1]) if '--scale' in sys.argv else 4
    nj = int(sys.argv[sys.argv.index('--jobs') + 1]) if '--jobs' in sys.argv else 4
    os.makedirs(out, exist_ok=True)
    js = list(jobs(art, out, only, scale))
    with ProcessPoolExecutor(nj) as ex:
        for i, sid in enumerate(ex.map(one, js, chunksize=4)):
            if i % 50 == 0: print(i, len(js), sid, flush=True)
