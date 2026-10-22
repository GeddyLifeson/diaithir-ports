"""Paint PureBD-style cartoon eyes into each eye plane's UV faces (32x texture)."""
import numpy as np
from PIL import Image

def paint_eyes(img, model, scale=2):
    a = np.array(img)
    for name in ('eye_left', 'eye_right'):
        p = model['parts'].get(name)
        if not p:
            continue
        u, v = p['uv']; _, _, _, w, h, d, _ = p['boxes'][0]
        h, d = int(h), int(d)
        for x in (u, u + d):
            x0, y0, W, H = x * scale, (v + d) * scale, d * scale, h * scale
            eye = np.zeros((H, W, 4), np.uint8); eye[...] = (24, 18, 14, 255)       # outline
            eye[1:-1, 1:-1] = (246, 242, 230, 255)                                  # white
            pw, ph = max(2, W // 3), max(2, (H - 2) * 2 // 3)
            px, py = (W - pw) // 2, H - 1 - ph
            eye[py:py + ph, px:px + pw] = (20, 14, 10, 255)                          # pupil
            eye[py, px + pw - 1] = (255, 255, 255, 255)                              # glint
            a[y0:y0 + H, x0:x0 + W] = eye
    return Image.fromarray(a)
