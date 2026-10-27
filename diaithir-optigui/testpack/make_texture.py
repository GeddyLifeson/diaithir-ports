"""Builds tuath_chest.png, a green tint of the vanilla generic_54.png, from a Minecraft client jar.
Usage: python3 make_texture.py path/to/26.1.2.jar   (the result is not committed, it derives from Mojang art)"""
import io, sys, zipfile
from PIL import Image
jar = zipfile.ZipFile(sys.argv[1])
im = Image.open(io.BytesIO(jar.read('assets/minecraft/textures/gui/container/generic_54.png'))).convert('RGBA')
px = im.load()
for y in range(im.height):
    for x in range(im.width):
        r, g, b, a = px[x, y]
        px[x, y] = (int(r * 0.45), min(255, int(g * 1.05)), int(b * 0.5), a)
im.save(__file__.rsplit('/', 1)[0] + '/assets/minecraft/textures/gui/container/tuath_chest.png')
