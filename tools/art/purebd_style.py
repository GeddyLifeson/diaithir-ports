"""Restyle a Minecraft texture toward the PureBDcraft look: flat cel shading, a
saturated palette and bold dark outlines, upscaled to the target resolution.

Shared by every thread that makes Diaithir art (pack patch generator, mob models).

    python3 purebd_style.py IN.png OUT.png [--scale 2] [--faces faces.json]

--faces: JSON list of [x, y, w, h] rectangles in source pixels (entity UV faces).
Each face gets its own outline. Without it, the whole image is one face (blocks, items).
"""
import argparse, json
import numpy as np
from PIL import Image, ImageFilter

SAT = 1.25        # saturation boost
CONTRAST = 1.12   # contrast around mid grey
COLOURS = 3       # flat colours per face
OUTLINE = 0.38    # outline = colour * this
HIGHLIGHT = 1.18  # top-edge rim light


def _hsv(rgb):
    return np.asarray(Image.fromarray(rgb, 'RGB').convert('HSV'), dtype=np.float32)


def _rgb(hsv):
    return np.asarray(Image.fromarray(np.clip(hsv, 0, 255).astype(np.uint8), 'HSV').convert('RGB'))


def cel(rgb):
    """Punch up colour and contrast."""
    rgb = np.clip((rgb.astype(np.float32) - 128) * CONTRAST + 128, 0, 255).astype(np.uint8)
    hsv = _hsv(rgb)
    hsv[..., 1] *= SAT
    return _rgb(hsv).copy()


def flat(face, alpha):
    """Collapse a face to a few flat colours, drop speckles, add a top-lit gradient."""
    h, w = alpha.shape
    if alpha.max() == 0:
        return face
    k = min(COLOURS, max(1, len(np.unique(face[alpha > 0].reshape(-1, 3), axis=0))))
    q = Image.fromarray(face).quantize(k, method=Image.Quantize.MEDIANCUT).convert('RGB')
    if min(h, w) >= 6:
        q = q.filter(ImageFilter.ModeFilter(5))
    out = np.asarray(q, dtype=np.float32)
    shade = np.linspace(1.08, 0.86, h)[:, None, None] if h > 3 else 1.0
    return np.clip(out * shade, 0, 255).astype(np.uint8)


def edges(rgb, alpha):
    """Pixels whose colour jumps sharply from a neighbour, or that border transparency."""
    lum = rgb.astype(np.float32) @ [0.3, 0.59, 0.11]
    e = np.zeros(lum.shape, bool)
    for axis in (0, 1):
        d = np.abs(np.diff(lum, axis=axis)) > 46
        a = np.diff(alpha.astype(int), axis=axis) != 0
        hit = d | a
        if axis == 0:
            e[1:] |= hit & (lum[1:] <= lum[:-1]); e[:-1] |= hit & (lum[:-1] < lum[1:])
        else:
            e[:, 1:] |= hit & (lum[:, 1:] <= lum[:, :-1]); e[:, :-1] |= hit & (lum[:, :-1] < lum[:, 1:])
    return e & (alpha > 0)


def style(img, scale=2, faces=None):
    img = img.convert('RGBA')
    big = img.resize((img.width * scale, img.height * scale), Image.NEAREST)
    a = np.asarray(big)
    rgb, alpha = cel(np.ascontiguousarray(a[..., :3])), a[..., 3]
    rects = [[int(round(v * scale)) for v in f] for f in faces or [[0, 0, img.width, img.height]]]
    rects = [(x, y, w, h) for x, y, w, h in rects if w >= 2 and h >= 2 and x + w <= rgb.shape[1] and y + h <= rgb.shape[0]]
    for x, y, w, h in rects:
        rgb[y:y + h, x:x + w] = flat(np.ascontiguousarray(rgb[y:y + h, x:x + w]), alpha[y:y + h, x:x + w])
    line = edges(rgb, alpha)
    rim = np.zeros(alpha.shape, bool)
    for x, y, w, h in rects:
        line[y, x:x + w] = line[y + h - 1, x:x + w] = True
        line[y:y + h, x] = line[y:y + h, x + w - 1] = True
        if h > 3 and w > 3:
            rim[y + 1, x + 1:x + w - 1] = True
    out = rgb.astype(np.float32)
    out[rim & ~line] *= HIGHLIGHT
    out[line] *= OUTLINE
    out = np.dstack([np.clip(out, 0, 255).astype(np.uint8), alpha])
    return Image.fromarray(out, 'RGBA')


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('src'); p.add_argument('dst')
    p.add_argument('--scale', type=int, default=2)
    p.add_argument('--faces')
    a = p.parse_args()
    faces = json.load(open(a.faces)) if a.faces else None
    style(Image.open(a.src), a.scale, faces).save(a.dst)
