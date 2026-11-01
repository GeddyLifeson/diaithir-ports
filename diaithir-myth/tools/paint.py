"""Simplified procedural painter for the Diaithir Myth Beasts skins (all art is new, drawn here).

Each cube face is drawn as its own small picture from a material (fur strands, skin, scales, feathers, stone, bronze,
horn, cloth, hoof, bark, leaf), shaded by face direction, with darkened edges (cheap ambient occlusion) and small dark eyes.
"""
import numpy as np
from PIL import Image

# material -> (base rgb, kind). Colours are muted on purpose.
MATS = {
    'fur_brown': ((108, 80, 58), 'fur'), 'fur_dark': ((56, 44, 38), 'fur'), 'fur_black': ((38, 33, 32), 'fur'),
    'fur_tawny': ((160, 124, 78), 'fur'), 'fur_sand': ((150, 124, 88), 'fur'), 'fur_cream': ((190, 172, 138), 'fur'),
    'fur_grey': ((118, 112, 104), 'fur'), 'fur_white': ((200, 196, 186), 'fur'), 'mane_dark': ((72, 52, 40), 'fur'),
    'hair_black': ((42, 36, 36), 'fur'), 'hair_brown': ((92, 64, 44), 'fur'), 'hair_teal': ((70, 108, 108), 'fur'),
    'skin': ((176, 138, 106), 'skin'), 'skin_pale': ((196, 168, 142), 'skin'), 'skin_grey': ((128, 120, 108), 'skin'),
    'skin_blue': ((142, 160, 164), 'skin'), 'skin_bark': ((96, 78, 56), 'bark'), 'skin_moss': ((84, 98, 62), 'bark'),
    'scale_sand': ((150, 128, 86), 'scale'), 'scale_green': ((74, 92, 62), 'scale'), 'scale_dark': ((70, 62, 50), 'scale'),
    'feather_brown': ((98, 76, 58), 'feather'), 'feather_white': ((206, 202, 192), 'feather'),
    'feather_bronze': ((132, 104, 62), 'feather'), 'feather_dark': ((58, 50, 46), 'feather'),
    'bronze': ((140, 106, 66), 'metal'), 'bronze_old': ((100, 124, 106), 'metal'),
    'stone': ((122, 118, 110), 'stone'), 'horn': ((200, 190, 164), 'horn'), 'horn_dark': ((70, 62, 54), 'horn'),
    'hoof': ((44, 38, 34), 'hoof'), 'claw': ((60, 54, 48), 'hoof'),
    'cloth_brown': ((112, 90, 64), 'cloth'), 'cloth_linen': ((190, 184, 168), 'cloth'), 'cloth_teal': ((70, 110, 112), 'cloth'),
    'cloth_red': ((112, 56, 46), 'cloth'), 'wood': ((98, 74, 50), 'bark'), 'leaf': ((72, 104, 56), 'leaf'),
    'leaf_dark': ((52, 76, 44), 'leaf'), 'web': ((196, 194, 188), 'cloth'),
}
SHADE = {'top': 1.12, 'front': 1.0, 'back': 0.9, 'right': 0.94, 'left': 0.86, 'bottom': 0.68}


def blur(a, r, axis):
    if r < 1:
        return a
    out = np.zeros_like(a)
    for k in range(-r, r + 1):
        out += np.roll(a, k, axis=axis)
    return out / (2 * r + 1)


def field(kind, h, w, rng, face):
    """Brightness multiplier field around 1.0 for one face."""
    n = rng.random((h, w))
    if kind == 'fur':
        axis = 0 if face not in ('top', 'bottom') else 0
        s = blur(n, 2, axis)
        s = (s - 0.5) * 2.6
        clump = blur(blur(rng.random((h, w)), 2, 0), 2, 1)
        f = 1 + 0.20 * s + 0.35 * (clump - 0.5)
        f[rng.random((h, w)) < 0.06] *= 0.8
        return f
    if kind == 'skin':
        return 1 + 0.10 * (blur(blur(n, 1, 0), 1, 1) - 0.5) * 2 + 0.03 * (n - 0.5)
    if kind == 'bark':
        s = blur(n, 3, 0)
        return 1 + 0.45 * (s - 0.5) + 0.12 * (n - 0.5)
    if kind == 'scale':
        yy, xx = np.mgrid[0:h, 0:w]
        cx = (xx + (yy // 3 % 2) * 1.5) % 3
        cy = yy % 3
        edge = ((cx < 1) | (cy < 1)).astype(np.float32)
        return 1 - 0.20 * edge + 0.08 * (n - 0.5)
    if kind == 'feather':
        yy, xx = np.mgrid[0:h, 0:w]
        row = yy % 3
        shift = (xx + (yy // 3) * 2) % 4
        return 1 - 0.16 * (row == 2) - 0.08 * (shift == 0) + 0.10 * (n - 0.5)
    if kind == 'stone':
        c = np.kron(rng.random((h // 2 + 1, w // 2 + 1)), np.ones((2, 2)))[:h, :w]
        return 1 + 0.22 * (c - 0.5) + 0.10 * (n - 0.5)
    if kind == 'metal':
        s = blur(n, 3, 1)
        low = blur(blur(rng.random((h, w)), 3, 0), 3, 1)
        f = 1 + 0.25 * (s - 0.5)
        return f - 0.22 * np.clip(low - 0.55, 0, 1) * 3
    if kind == 'horn':
        yy = np.mgrid[0:h, 0:w][0]
        return 1 + 0.10 * np.sin(yy / 1.5 * np.pi) + 0.10 * (blur(n, 2, 0) - 0.5)
    if kind == 'cloth':
        yy, xx = np.mgrid[0:h, 0:w]
        return 1 + 0.05 * (((xx + yy) % 2) * 2 - 1) + 0.12 * (n - 0.5)
    if kind == 'leaf':
        yy, xx = np.mgrid[0:h, 0:w]
        v = ((xx * 2 + yy) % 5 == 0).astype(np.float32)
        return 1 - 0.18 * v + 0.22 * (n - 0.5)
    return 1 + 0.12 * (n - 0.5)  # hoof and default


def face_rects(u, v, w, h, d):
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h), 'back': (u + 2 * d + w, v + d, w, h),
    }


EYE = np.array([18, 14, 12], np.float32)


def paint_box(arr, u, v, w, h, d, mat, rng, eyes=()):
    base, kind = MATS[mat]
    base = np.array(base, np.float32)
    for face, (x0, y0, fw, fh) in face_rects(u, v, w, h, d).items():
        if fw <= 0 or fh <= 0:
            continue
        f = field(kind, fh, fw, rng, face)
        col = base[None, None, :] * (f * SHADE[face])[:, :, None]
        if kind == 'fur' and face in ('front', 'back', 'left', 'right') and fh > 4:
            # dust and dirt gathers low on the body
            grad = np.linspace(1.0, 0.82, fh)[:, None, None]
            col = col * grad
        # edge occlusion
        occ = np.ones((fh, fw), np.float32)
        occ[0, :] *= 0.92; occ[-1, :] *= 0.86; occ[:, 0] *= 0.94; occ[:, -1] *= 0.94
        col = col * occ[:, :, None]
        arr[y0:y0 + fh, x0:x0 + fw, :] = np.clip(col, 0, 255)
    rects = face_rects(u, v, w, h, d)
    for face, ex, ey in eyes:
        x0, y0, fw, fh = rects[face]
        if 0 <= ex < fw and 0 <= ey < fh:
            arr[y0 + ey, x0 + ex, :] = EYE
            # a faint wet highlight pixel above on tall faces keeps the eye alive without cartoon whites
            if ey > 0 and fh > 5:
                arr[y0 + ey - 1, x0 + ex, :] = arr[y0 + ey - 1, x0 + ex, :] * 0.8


def egg_icon(c1, c2, seed):
    rng = np.random.default_rng(seed)
    img = np.zeros((16, 16, 4), np.uint8)
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 5.6, (y - 8.2) / (7.2 if y > 8 else 6.4)
            r = dx * dx + dy * dy
            if r <= 1.0:
                spot = rng.random() < 0.16 and r < 0.8
                c = np.array(c2 if spot else c1, np.float32)
                shade = 1.12 - 0.35 * ((x - 5) / 16) - 0.18 * ((y - 3) / 16)
                if r > 0.72:
                    shade *= 0.8
                img[y, x, :3] = np.clip(c * shade, 0, 255)
                img[y, x, 3] = 255
    return Image.fromarray(img, 'RGBA')


def pack(sizes):
    """Shelf-pack (w,h) rects, return positions and atlas size (power-of-two friendly)."""
    for W in (64, 128, 256):
        order = sorted(range(len(sizes)), key=lambda i: -sizes[i][1])
        pos = [None] * len(sizes)
        x = y = shelf = 0
        ok = True
        for i in order:
            w, h = sizes[i]
            if w > W:
                ok = False; break
            if x + w > W:
                x = 0; y += shelf; shelf = 0
            pos[i] = (x, y)
            x += w; shelf = max(shelf, h)
        H = y + shelf
        if ok and H <= max(64, W):
            H = (H + 15) // 16 * 16
            return pos, W, max(H, 16)
    raise ValueError('does not fit')
