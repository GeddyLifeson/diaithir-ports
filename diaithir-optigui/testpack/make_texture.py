"""Builds tuath_chest.png (green tint of generic_54.png) and tuath_furnace.png (red tint of furnace.png) from a Minecraft client jar.
Usage: python3 make_texture.py path/to/26.1.2.jar   (the result is not committed, it derives from Mojang art)"""
import io, sys, zipfile
from PIL import Image
jar = zipfile.ZipFile(sys.argv[1])
def tint(src, dst, mul):
    im = Image.open(io.BytesIO(jar.read('assets/minecraft/textures/gui/container/' + src))).convert('RGBA')
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            px[x, y] = tuple(min(255, int(c * m)) for c, m in zip((r, g, b), mul)) + (a,)
    im.save(__file__.rsplit('/', 1)[0] + '/assets/minecraft/textures/gui/container/' + dst)
tint('generic_54.png', 'tuath_chest.png', (0.45, 1.05, 0.5))
tint('furnace.png', 'tuath_furnace.png', (1.1, 0.5, 0.5))
