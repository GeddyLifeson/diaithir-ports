"""Writes the assets of the tide wheel tiers (brass, gold, silver): recoloured textures, blockstates, models, items, loot, tags, lang.
Factorio-style: the panel metal tells the tier. Usage: python3 tools/gen_tide_tiers.py (from the repo root)."""
import colorsys, json, re
from PIL import Image

R = "src/main/resources"
A, D = f"{R}/assets/create", f"{R}/data/create"
TIERS = [("brass", "Cuibhleseòlmaraumha", 38, .85, 1.0), ("gold", "Cuibhleseòlmaraòir", 47, 1.0, 1.12), ("silver", "Cuibhleseòlmaraairgid", 0, .12, 1.22)]


def recolour(src, dst, hue, sat, val):
    im = Image.open(src).convert("RGBA")
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if a and .38 < h < .58 and s > .12:  # teal panel only; hazard stripes, rivets and the lit spiral stay
                h2 = hue / 360 if sat > .2 else h
                r, g, b = (int(c * 255) for c in colorsys.hsv_to_rgb(h2, min(1, s * sat), min(1, v * val)))
                px[x, y] = (r, g, b, a)
    im.save(dst)


def sub(text, name):
    return text.replace("tide_wheel", name)


lang_p = f"{A}/lang/en_us.json"
lang = open(lang_p).read()
for metal, display, hue, sat, val in TIERS:
    name = f"tide_wheel_{metal}"
    for part in ("side", "top", "top_lit", "bottom"):
        recolour(f"{A}/textures/block/tide_wheel_{part}.png", f"{A}/textures/block/{name}_{part}.png", hue, sat, val)
    for rel in ("blockstates/tide_wheel.json", "items/tide_wheel.json", "models/block/tide_wheel.json", "models/block/tide_wheel_lit.json"):
        out = f"{A}/{sub(rel, name)}"
        open(out, "w").write(sub(open(f"{A}/{rel}").read(), name))
    open(f"{D}/loot_table/blocks/{name}.json", "w").write(sub(open(f"{D}/loot_table/blocks/tide_wheel.json").read(), name))
    for kind in ("block", "item"):
        key = f' "{kind}.create.{name}"'
        if key not in lang:
            lang = lang.replace(f' "{kind}.create.tide_wheel": "Cuibhleseòlmara",\n', f' "{kind}.create.tide_wheel": "Cuibhleseòlmara",\n{key}: "{display}",\n')
open(lang_p, "w").write(lang)
for tag in ("data/minecraft/tags/block/mineable/pickaxe.json", "data/create/tags/block/safe_nbt.json"):
    p = f"{R}/{tag}"
    t = open(p).read()
    for metal, *_ in TIERS:
        if f"create:tide_wheel_{metal}" not in t:
            t = t.replace('    "create:tide_wheel",\n', f'    "create:tide_wheel",\n    "create:tide_wheel_{metal}",\n')
    open(p, "w").write(t)
json.load(open(lang_p))
print("ok")
