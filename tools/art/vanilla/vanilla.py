"""Paint vanilla mob skins fresh at 64x (4x), the locked Diaithir creature method, from ExportLayers.java polygons.

    python3 vanilla.py BLENDER_PYTHON LAYERS_JSON VANILLA_TEXTURES_DIR OUT_DIR [mob ...]
Each mob: bake AO on its own UVs (poly_ao.py), paint every face as its own picture (paint.py materials), keep the
original face features (eyes, nostrils, mouth) as crisp painted marks, then grade. The old skin sets soft colours only.
"""
import json, re, subprocess, sys, zlib
from pathlib import Path
import numpy as np
from PIL import Image
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from paint import paint_face, grade, soft

HERE = Path(__file__).resolve().parent
HOOVES = r'leg'
# mob: layer, textures, hidden parts, [(regex on 'part#cube', material)], hooves on the legs
MOBS = {
    'cow': ('CowModel#createBodyLayer', ['cow/cow_temperate', 'cow/cow_cold', 'cow/cow_warm', 'cow/mooshroom_red', 'cow/mooshroom_brown'], '-',
            [(r'/head#[23]', 'horn'), (r'/body#1', 'skin')], True),
    'pig': ('PigModel#createBodyLayer', ['pig/pig_temperate', 'pig/pig_cold', 'pig/pig_warm'], '-', [(r'.', 'skin')], True),
    'sheep': ('SheepModel#createBodyLayer', ['sheep/sheep'], '-', [(r'/head#0', 'skin')], True),
    'sheep_wool': ('SheepFurModel#createFurLayer', ['sheep/sheep_wool'], '-', [(r'.', 'wool')], False),
    'chicken': ('AdultChickenModel#createBodyLayer', ['chicken/chicken_temperate', 'chicken/chicken_cold', 'chicken/chicken_warm'], '-',
                [(r'beak|_leg', 'horn'), (r'red_thing', 'skin'), (r'.', 'feather')], False),
    'horse': ('AbstractEquineModel#createBodyMesh', ['horse/horse_brown', 'horse/horse_white', 'horse/horse_black', 'horse/horse_chestnut',
              'horse/horse_creamy', 'horse/horse_darkbrown', 'horse/horse_gray', 'horse/donkey', 'horse/mule'], '-',
              [(r'mane|tail', 'shaggy')], True),
    'wolf': ('AdultWolfModel#createBodyLayer', ['wolf/wolf', 'wolf/wolf_ashen', 'wolf/wolf_black', 'wolf/wolf_chestnut', 'wolf/wolf_rusty',
             'wolf/wolf_snowy', 'wolf/wolf_spotted', 'wolf/wolf_striped', 'wolf/wolf_woods'], '-', [(r'upper_body|tail', 'shaggy')], False),
    'goat': ('GoatModel#createBodyLayer', ['goat/goat'], '-', [(r'horn', 'horn'), (r'/head#2|/body#1', 'shaggy')], True),
    'llama': ('LlamaModel#createBodyLayer', ['llama/llama_brown', 'llama/llama_creamy', 'llama/llama_gray', 'llama/llama_white'], r'chest', [(r'.', 'shaggy')], True),
    'polar_bear': ('PolarBearModel#createBodyLayer', ['bear/polarbear'], '-', [(r'.', 'shaggy')], False),
    'fox': ('AdultFoxModel#createBodyLayer', ['fox/fox', 'fox/fox_snow'], '-', [(r'tail', 'shaggy')], False),
    'rabbit': ('AdultRabbitModel#createBodyLayer', ['rabbit/rabbit_brown', 'rabbit/rabbit_white', 'rabbit/rabbit_black', 'rabbit/rabbit_gold',
               'rabbit/rabbit_salt', 'rabbit/rabbit_white_splotched'], '-', [], False),
    'cat': ('AdultFelineModel#createBodyMesh', ['cat/cat_tabby', 'cat/ocelot', 'cat/cat_black', 'cat/cat_calico', 'cat/cat_siamese', 'cat/cat_red',
            'cat/cat_white', 'cat/cat_british_shorthair', 'cat/cat_persian', 'cat/cat_ragdoll', 'cat/cat_jellie', 'cat/cat_all_black'], '-', [], False),
    'panda': ('PandaModel#createBodyLayer', ['panda/panda', 'panda/panda_brown'], '-', [(r'.', 'shaggy')], False),
    'camel': ('AdultCamelModel#createBodyLayer', ['camel/camel'], r'saddle|bridle|reins', [(r'hump', 'shaggy')], False),
    'armadillo': ('AdultArmadilloModel#createBodyLayer', ['armadillo/armadillo'], r'^/cube$', [(r'/body#|tail', 'shell'), (r'ear', 'hide'), (r'.', 'hide')], False),
    'turtle': ('AdultTurtleModel#createBodyLayer', ['turtle/turtle'], r'egg_belly', [(r'/body#0', 'shell'), (r'.', 'skin')], False),
    'frog': ('FrogModel#createBodyLayer', ['frog/frog_temperate', 'frog/frog_cold', 'frog/frog_warm'], r'tongue|croaking', [(r'.', 'skin')], False),
    'sniffer': ('SnifferModel#createBodyLayer', ['sniffer/sniffer'], '-', [(r'nose|mouth', 'hide'), (r'.', 'shaggy')], False),
    'bee': ('AdultBeeModel#createBodyLayer', ['bee/bee'], r'stinger', [(r'wing', 'wing'), (r'antenna|leg', 'horn'), (r'.', 'fur')], False),
    'parrot': ('ParrotModel#createBodyLayer', ['parrot/parrot_red_blue', 'parrot/parrot_blue', 'parrot/parrot_green', 'parrot/parrot_yellow_blue', 'parrot/parrot_grey'],
               '-', [(r'beak|feather$', 'horn'), (r'leg', 'horn'), (r'.', 'feather')], False),
    'bat': ('BatModel#createBodyLayer', ['bat/bat'], '-', [(r'wing', 'hide')], False),
    'axolotl': ('AdultAxolotlModel#createBodyLayer', ['axolotl/axolotl_wild', 'axolotl/axolotl_lucy', 'axolotl/axolotl_gold', 'axolotl/axolotl_cyan', 'axolotl/axolotl_blue'],
                '-', [(r'gills', 'wing'), (r'.', 'skin')], False),
}


def material(rules, part, cube):
    key = f'{part}#{cube}'
    for rx, mat in rules:
        if re.search(rx, key):
            return mat
    return 'fur'


def components(mask):
    """4-connected components of a small boolean mask, as lists of (y, x)."""
    seen = np.zeros_like(mask); out = []
    for y, x in zip(*np.nonzero(mask)):
        if seen[y, x]:
            continue
        stack, comp = [(y, x)], []
        seen[y, x] = True
        while stack:
            cy, cx = stack.pop(); comp.append((cy, cx))
            for ny, nx in ((cy + 1, cx), (cy - 1, cx), (cy, cx + 1), (cy, cx - 1)):
                if 0 <= ny < mask.shape[0] and 0 <= nx < mask.shape[1] and mask[ny, nx] and not seen[ny, nx]:
                    seen[ny, nx] = True; stack.append((ny, nx))
        out.append(comp)
    return out


def features(orig, face, s):
    """Keep the face's small, high-contrast marks (eyes, nostrils, mouth) from the original, drawn crisp on the new paint."""
    rgb = orig[..., :3].astype(np.float32); a = orig[..., 3] > 0
    if a.sum() < 4:
        return face
    med = np.median(rgb[a], 0)
    dist = np.linalg.norm(rgb - med, axis=2)
    mask = (dist > 60) & a
    for comp in components(mask):
        if len(comp) > max(6, a.sum() // 8):  # big patches are markings, already carried by the soft colour
            continue
        ys = [p[0] for p in comp]
        for y, x in comp:
            c = rgb[y, x]
            sl = (slice(y * s, (y + 1) * s), slice(x * s, (x + 1) * s))
            k = max(1, s // 4)
            tile = np.tile(c, (s, s, 1)) * 0.92
            tile[-k:, :] *= 0.8; tile[:, -k:] *= 0.85         # bevel: features sit in the surface
            if c.mean() < 70 and y == min(ys):              # wet highlight on dark eyes
                tile[:k, :k] = np.clip(c * 0.6 + 120, 0, 255)
            face[sl] = tile
    return face


def paint_mob(layer, rules, hide, hooves, src, ao, s=4):
    tw, th = layer['tw'], layer['th']
    src = np.asarray(src.convert('RGBA').resize((tw, th), Image.NEAREST) if src.size != (tw, th) else src.convert('RGBA'), np.float32)
    big = src.repeat(s, 0).repeat(s, 1)
    ao = np.asarray(ao.convert('L'), np.float32) / 255
    out = big.copy(); out[..., :3] = grade(out[..., :3])   # texels no face uses keep the original, graded
    rng = np.random.default_rng(zlib.crc32(json.dumps(rules).encode()))
    done = set()
    for f in layer['faces']:
        if hide != '-' and re.search(hide, f['part']):
            continue
        us = [v[3] for v in f['v']]; vs = [v[4] for v in f['v']]
        x0, x1, y0, y1 = (int(round(t)) for t in (min(us), max(us), min(vs), max(vs)))
        if x1 <= x0 or y1 <= y0 or (x0, y0, x1, y1) in done:
            continue
        done.add((x0, y0, x1, y1))
        sl = (slice(y0 * s, y1 * s), slice(x0 * s, x1 * s))
        a = big[sl][..., 3]
        if not (a > 0).any():
            continue
        kind = material(rules, f['part'], f['cube'])
        P = np.array([v[:3] for v in f['v']]); n = np.cross(P[1] - P[0], P[2] - P[0]); n /= np.linalg.norm(n) + 1e-9
        base = soft(big[sl][..., :3], s)
        if kind in ('horn', 'hoof'):
            base = np.broadcast_to(big[sl][..., :3][a > 0].mean(0), base.shape)
        flat = min(f['box']) < 0.01
        occl = np.maximum(ao[sl], 0.75) if flat else ao[sl]
        legs = bool(re.search(r'leg', f['part']))
        face = paint_face(base, a, kind, occl, rng, s, legs)
        if hooves and legs and abs(n[1]) < 0.5:   # hoof band: the bottom of each leg's side faces
            hb = max(s, (y1 - y0) * s // 6)
            hoof = paint_face(np.broadcast_to(np.array([58, 48, 40], np.float32), (hb, (x1 - x0) * s, 3)), a[-hb:], 'hoof', occl[-hb:], rng, s, False)
            face[-hb:] = hoof
        if re.search(r'head|nose|snout|mouth|beak', f['part']) and n[2] < -0.7:   # forward-facing face of the head
            face = features(src[y0:y1, x0:x1], face, s)
        out[sl] = np.dstack([grade(face), a])
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8))


if __name__ == '__main__':
    blender, layers_json, tex, outdir = sys.argv[1:5]
    names = sys.argv[5:] or list(MOBS)
    layers = json.load(open(layers_json)); outdir = Path(outdir); outdir.mkdir(parents=True, exist_ok=True)
    for mob in names:
        key, textures, hide, rules, hooves = MOBS[mob]
        ao = outdir / f'_ao_{mob}.png'
        subprocess.run([blender, str(HERE / 'poly_ao.py'), layers_json, key, '4', hide, str(ao)], check=True, capture_output=True)
        for t in textures:
            dst = outdir / f'{t}.png'; dst.parent.mkdir(parents=True, exist_ok=True)
            paint_mob(layers[key], rules, hide, hooves, Image.open(Path(tex) / f'{t}.png'), Image.open(ao)).save(dst)
        print(mob, len(textures), flush=True)
