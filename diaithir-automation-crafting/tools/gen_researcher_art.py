#!/usr/bin/env python3
"""Draws the Researcher GUI art in the Diaithir Industrial Factory look (own art, no upstream or Factorio assets).
Usage: gen_researcher_art.py <assets/researcher dir>"""
import math, sys
from PIL import Image, ImageDraw

STEEL_D = (34, 38, 42, 255)
STEEL = (58, 65, 72, 255)
STEEL_L = (96, 106, 116, 255)
STEEL_H = (150, 160, 170, 255)
RUST = (200, 118, 46, 255)
RUST_D = (140, 74, 24, 255)
HAZARD = (224, 176, 32, 255)
BLACK = (12, 14, 16, 255)
RED = (190, 52, 40, 255)


def gear(size):
    s = size * 4  # supersample then nearest-reduce for crisp pixels
    im = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    c = s / 2
    teeth, r_out, r_in, r_hole, r_hub = 8, s * 0.46, s * 0.36, s * 0.14, s * 0.22
    pts = []
    for i in range(teeth * 4):
        a = math.tau * i / (teeth * 4)
        r = r_out if (i % 4) in (1, 2) else r_in
        pts.append((c + r * math.cos(a), c + r * math.sin(a)))
    d.polygon(pts, fill=STEEL_L, outline=BLACK)
    d.ellipse([c - s * 0.32, c - s * 0.32, c + s * 0.32, c + s * 0.32], fill=STEEL, outline=STEEL_D, width=max(1, s // 32))
    d.ellipse([c - r_hub, c - r_hub, c + r_hub, c + r_hub], fill=RUST, outline=RUST_D, width=max(1, s // 24))
    d.ellipse([c - r_hole, c - r_hole, c + r_hole, c + r_hole], fill=STEEL_D)
    d.arc([c - s * 0.30, c - s * 0.30, c + s * 0.30, c + s * 0.30], 200, 290, fill=STEEL_H, width=max(1, s // 40))
    return im.resize((size, size), Image.NEAREST)


def bevel(d, box, light, dark, fill=None):
    x0, y0, x1, y1 = box
    if fill:
        d.rectangle(box, fill=fill)
    d.line([(x0, y0), (x1, y0)], fill=light)
    d.line([(x0, y0), (x0, y1)], fill=light)
    d.line([(x0, y1), (x1, y1)], fill=dark)
    d.line([(x1, y0), (x1, y1)], fill=dark)


def slot(d, x, y, size=18):
    bevel(d, (x, y, x + size - 1, y + size - 1), STEEL_D, STEEL_H, fill=BLACK)
    d.rectangle((x + 1, y + 1, x + size - 2, y + size - 2), fill=(24, 27, 30, 255))


def crafting():
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    bevel(d, (0, 0, 175, 85), STEEL_H, BLACK, fill=STEEL)
    d.rectangle((2, 2, 173, 83), outline=STEEL_D)
    for x, y in ((5, 5), (170, 5), (5, 80), (170, 80)):  # rivets
        d.rectangle((x, y, x + 1, y + 1), fill=STEEL_H)
        d.point((x + 1, y + 1), fill=STEEL_D)
    for r in range(3):
        for c in range(3):
            slot(d, 29 + 18 * c, 16 + 18 * r)
    slot(d, 123, 34)
    # arrow between grid and result
    d.polygon([(92, 40), (108, 40), (108, 36), (118, 43), (108, 50), (108, 46), (92, 46)], fill=RUST, outline=RUST_D)
    # hazard stripe along the bottom
    for x in range(8, 168, 8):
        d.polygon([(x, 78), (x + 4, 78), (x + 8, 82), (x + 4, 82)], fill=HAZARD)
    return im


def error_arrow():
    im = Image.new('RGBA', (22, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.polygon([(1, 5), (12, 5), (12, 1), (21, 8), (12, 14), (12, 10), (1, 10)], fill=RED, outline=BLACK)
    return im


def error_overlay():
    im = Image.new('RGBA', (22, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.line([(4, 2), (17, 14)], fill=RED, width=3)
    d.line([(17, 2), (4, 14)], fill=RED, width=3)
    return im


if __name__ == '__main__':
    root = sys.argv[1]
    gear(64).save(f'{root}/textures/research/default.png')
    gear(128).save(f'{root}/icon.png')
    crafting().save(f'{root}/textures/gui/crafting.png')
    error_arrow().save(f'{root}/textures/gui/error_arrow.png')
    error_overlay().save(f'{root}/textures/gui/error_overlay.png')
