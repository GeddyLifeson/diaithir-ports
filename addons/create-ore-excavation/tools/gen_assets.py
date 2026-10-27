#!/usr/bin/env python3
"""Original art for the Diaithir port of Create Ore Excavation (nothing here is derived from upstream textures).
Industrial / Factorio-style: dull riveted steel, oxidised copper, hazard stripes, raw gem lumps."""
import random, os, math, json, sys
from PIL import Image, ImageDraw
OUT = sys.argv[1] if len(sys.argv) > 1 else 'src/main/resources/assets/createoreexcavation'
T = os.path.join(OUT, 'textures')
for d in ('block', 'item', 'entity'):
    os.makedirs(os.path.join(T, d), exist_ok=True)
R = random.Random(1042639205)

def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)

def noise_fill(im, base, amp=0.10, grain=1):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            k = 1 + R.uniform(-amp, amp)
            px[x, y] = shade(base, k)

def rivet(d, x, y, base):
    d.point((x, y), shade(base, 1.35)); d.point((x + 1, y + 1), shade(base, 0.6))

STEEL = (96, 102, 108); DARK = (52, 56, 60); COPPER = (150, 96, 58); VERD = (82, 128, 112)
YEL = (214, 168, 38); BLK = (30, 30, 30)

def plate():
    im = Image.new('RGBA', (16, 16)); noise_fill(im, STEEL, 0.07)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=shade(DARK, 1.0))
    d.line([(1, 1), (14, 1)], fill=shade(STEEL, 1.25)); d.line([(1, 1), (1, 14)], fill=shade(STEEL, 1.2))
    d.line([(1, 14), (14, 14)], fill=shade(STEEL, .7)); d.line([(14, 1), (14, 14)], fill=shade(STEEL, .72))
    for x, y in ((2, 2), (12, 2), (2, 12), (12, 12)): rivet(d, x, y, STEEL)
    d.line([(3, 8), (12, 8)], fill=shade(STEEL, .8))
    return im

def hazard():
    im = Image.new('RGBA', (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            band = ((x + y) // 4) % 2
            px[x, y] = shade(YEL if band == 0 else BLK, 1 + R.uniform(-.06, .06))
    d = ImageDraw.Draw(im); d.rectangle([0, 0, 15, 15], outline=shade(DARK, .8))
    return im

def grate():
    im = Image.new('RGBA', (16, 16)); noise_fill(im, DARK, 0.06)
    d = ImageDraw.Draw(im)
    for i in range(2, 15, 3):
        d.line([(1, i), (14, i)], fill=shade(STEEL, .45)); d.line([(1, i + 1), (14, i + 1)], fill=shade(STEEL, 1.1))
    d.rectangle([0, 0, 15, 15], outline=shade(STEEL, .9))
    return im

def pipe():
    im = Image.new('RGBA', (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            k = 0.7 + 0.5 * math.sin(math.pi * (y + .5) / 16)
            px[x, y] = shade(COPPER, k * (1 + R.uniform(-.05, .05)))
    d = ImageDraw.Draw(im)
    for x in (3, 12):
        d.line([(x, 0), (x, 15)], fill=shade(VERD, .9))
    return im

def shaft():
    im = Image.new('RGBA', (16, 16)); px = im.load()
    for y in range(16):
        for x in range(16):
            k = 0.6 + 0.6 * math.sin(math.pi * (x + .5) / 16)
            px[x, y] = shade(STEEL, k * (1 + R.uniform(-.04, .04)))
    d = ImageDraw.Draw(im)
    for y in range(1, 16, 5): d.line([(0, y), (15, y)], fill=shade(DARK, .9))
    return im

def ghost():
    im = Image.new('RGBA', (16, 16)); noise_fill(im, STEEL, 0.05); return im

def save(im, kind, name):
    im.save(os.path.join(T, kind, name + '.png'))

# ---- items
def lump(base, hi, seed):
    r = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    pts = [(8, 3), (13, 6), (13, 11), (8, 14), (3, 11), (3, 6)]
    d.polygon(pts, fill=shade(base, 1.0), outline=shade(base, .45))
    d.polygon([(8, 3), (13, 6), (8, 8), (3, 6)], fill=shade(base, 1.25))
    d.polygon([(8, 8), (13, 6), (13, 11), (8, 14)], fill=shade(base, .78))
    for _ in range(5):
        x, y = r.randint(4, 11), r.randint(5, 12)
        d.point((x, y), hi)
    d.point((7, 5), hi); d.point((8, 5), hi)
    return im

def drill_item(tip, body):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    # handle/motor block
    d.rectangle([2, 2, 7, 6], fill=shade(body, 1.0), outline=shade(body, .5))
    d.line([(3, 3), (6, 3)], fill=shade(body, 1.3))
    d.rectangle([4, 7, 6, 8], fill=shade(DARK, 1.0))
    # auger
    cx = 5
    for i, y in enumerate(range(9, 15)):
        w = 3 - i // 2
        for x in range(cx - w + 1, cx + w):
            d.point((x, y), shade(tip, 1.25 if (i % 2 == 0) else .7))
    d.point((5, 14), shade(tip, 1.4))
    d.line([(9, 12), (14, 6)], fill=(0, 0, 0, 0))
    return im

def finder():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.ellipse([2, 2, 11, 11], fill=shade(DARK, 1.0), outline=shade(STEEL, 1.1))
    d.ellipse([4, 4, 9, 9], fill=(40, 90, 70, 255))
    d.line([(6, 6), (8, 5)], fill=(150, 230, 170, 255)); d.point((6, 7), (150, 230, 170, 255))
    d.line([(10, 10), (14, 14)], fill=shade(COPPER, 1.0), width=2)
    return im

def atlas():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.rectangle([2, 1, 13, 14], fill=(110, 84, 52, 255), outline=(52, 38, 22, 255))
    d.rectangle([4, 3, 11, 12], fill=(206, 190, 150, 255))
    for (x, y, c) in ((5, 5, (60, 60, 60)), (8, 6, (30, 120, 150)), (6, 9, (150, 70, 50)), (9, 10, (210, 170, 40))):
        d.rectangle([x, y, x + 1, y + 1], fill=c + (255,))
    d.line([(2, 2), (2, 13)], fill=(150, 120, 80, 255))
    return im

save(plate(), 'block', 'machine_plate'); save(hazard(), 'block', 'machine_hazard'); save(grate(), 'block', 'machine_grate')
save(pipe(), 'block', 'machine_pipe'); save(shaft(), 'block', 'machine_shaft'); save(ghost(), 'block', 'ghost')
save(lump((60, 200, 215), (225, 255, 255, 255), 1), 'item', 'raw_diamond')
save(lump((40, 170, 80), (210, 255, 220, 255), 2), 'item', 'raw_emerald')
save(lump((170, 30, 28), (255, 150, 140, 255), 3), 'item', 'raw_redstone')
save(drill_item((176, 180, 186), (96, 102, 108)), 'item', 'normal_drill')
save(drill_item((70, 215, 230), (96, 102, 108)), 'item', 'diamond_drill')
save(drill_item((60, 48, 66), (70, 74, 80)), 'item', 'netherite_drill')
save(finder(), 'item', 'vein_finder'); save(atlas(), 'item', 'vein_atlas')
print('ok')
