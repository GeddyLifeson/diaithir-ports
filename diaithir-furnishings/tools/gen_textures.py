"""Paints the 16x16 textures for diaithir-furnishings. Run: python3 -I tools/gen_textures.py (needs Pillow). Deterministic."""
import random
from pathlib import Path
from PIL import Image

OUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/diaithirfurnishings/textures"


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c)


def rope(rng):
    img = Image.new("RGBA", (16, 16))
    base = (158, 118, 66)
    for y in range(16):
        for x in range(16):
            strand = ((x + y) // 2) % 3  # three twisted strands running diagonally
            k = (0.78, 1.0, 0.9)[strand] + rng.uniform(-0.05, 0.05)
            if (x + y) % 2 == 0 and strand == 0:
                k -= 0.08  # the groove between strands
            img.putpixel((x, y), shade(base, k) + (255,))
    return img


def rope_item(rng):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    base = (158, 118, 66)
    for i in range(3, 13):  # a coil: diagonal run with a twist and an outline
        for d, k in ((-1, 0.55), (0, 0.85), (1, 1.05), (2, 0.8)):
            x, y = i + d, 15 - i
            if 0 <= x < 16 and 0 <= y < 16:
                img.putpixel((x, y), shade(base, k + rng.uniform(-0.04, 0.04)) + (255,))
    for y in range(5, 11):  # loose loop end
        for x in range(2, 6):
            if (x - 3.5) ** 2 + (y - 8) ** 2 < 6 and (x - 3.5) ** 2 + (y - 8) ** 2 > 1.5:
                img.putpixel((x, y), shade(base, 0.9) + (255,))
    return img


def sack(rng):
    img = Image.new("RGBA", (16, 16))
    base = (178, 150, 100)
    for y in range(16):
        for x in range(16):
            k = 1.0
            if y % 2 == 0:
                k -= 0.07  # weft
            if x % 2 == 0:
                k -= 0.05  # warp
            k += rng.uniform(-0.04, 0.04)
            img.putpixel((x, y), shade(base, k) + (255,))
    for x in range(16):  # the tie, a darker cord band
        for y in (9, 10):
            img.putpixel((x, y), shade((120, 88, 52), 1.0 + rng.uniform(-0.05, 0.05)) + (255,))
    for y in range(11, 16):  # seam stitches
        img.putpixel((8, y), shade(base, 0.6) + (255,))
    return img


def sconce(rng):
    img = Image.new("RGBA", (16, 16))
    base = (66, 68, 74)
    for y in range(16):
        for x in range(16):
            k = 1.0 + rng.uniform(-0.08, 0.08)
            if y in (0, 15) or x in (0, 15):
                k *= 0.7  # forged edge
            elif y in (1, 14) or x in (1, 14):
                k *= 1.25  # edge highlight
            img.putpixel((x, y), shade(base, k) + (255,))
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):  # rivets
        img.putpixel((x, y), (150, 150, 155, 255))
    return img


def main():
    rng = random.Random(1)
    for name, fn in (("block/rope", rope), ("item/rope", rope_item), ("block/sack", sack), ("block/sconce", sconce)):
        (OUT / name).parent.mkdir(parents=True, exist_ok=True)
        fn(rng).save(OUT / f"{name}.png")


if __name__ == "__main__":
    main()
