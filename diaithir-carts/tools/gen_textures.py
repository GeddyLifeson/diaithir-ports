#!/usr/bin/env python3
"""Draws 16x16 item and block textures for the rail yard content from data/rolling_stock.csv and yards.json (flat pixel art, one glyph per class).
Run from diaithir-carts before gen_rolling_stock.py: python3 tools/gen_textures.py"""
import json, os, hashlib
from PIL import Image
R = "src/main/resources/assets/diaithircarts/textures"
os.makedirs(f"{R}/item", exist_ok=True); os.makedirs(f"{R}/block", exist_ok=True)
Y = json.load(open("src/main/resources/data/diaithircarts/yards.json"))
DARK = (30, 30, 34, 255); WHEEL = (60, 60, 66, 255); GLASS = (150, 200, 230, 255); STEEL = (140, 145, 155, 255)
BODY = {"loco_steam": (40, 40, 44, 255), "loco_primitive": (110, 80, 50, 255), "loco_diesel": (200, 150, 30, 255), "loco_electric": (50, 110, 190, 255),
        "freight": None, "passenger": (150, 40, 40, 255), "primitive_wagon": (130, 95, 60, 255)}
FREIGHT = {"wag_boxcar": (150, 60, 40), "wag_covered_hopper": (170, 170, 175), "wag_open_hopper": (90, 70, 60), "wag_tank": (60, 60, 65), "wag_flatcar": (120, 100, 70),
           "wag_gondola": (110, 80, 60), "wag_reefer": (225, 225, 215), "wag_well": (80, 100, 120), "wag_autorack": (100, 110, 120), "wag_ore": (120, 70, 50),
           "wag_stock": (130, 90, 55), "wag_caboose": (170, 40, 35), "wag_log": (100, 75, 45), "wag_crane": (210, 170, 40), "wag_schnabel": (70, 75, 85)}

def put(im, x0, y0, x1, y1, c):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            im.putpixel((x, y), c)

def wheels(im, xs):
    for x in xs:
        im.putpixel((x, 12), WHEEL); im.putpixel((x + 1, 12), WHEEL); im.putpixel((x, 13), DARK); im.putpixel((x + 1, 13), DARK)
    put(im, 1, 11, 14, 11, DARK)

def vary(c, key):
    h = hashlib.md5(key.encode()).digest()
    return tuple(max(0, min(255, c[i] + h[i] % 41 - 20)) for i in range(3)) + (255,)

def vehicle(v):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); cat = v["category"]
    if cat == "mu": cat = "loco_electric"
    c = BODY.get(cat) or tuple(list(FREIGHT.get(v["id"], (120, 120, 120))) + [255])
    c = vary(c, v["id"])
    if cat.startswith("loco"):
        put(im, 1, 6, 10, 10, c); put(im, 10, 4, 14, 10, c); put(im, 11, 5, 13, 6, GLASS)
        if cat in ("loco_steam", "loco_primitive"): put(im, 3, 3, 4, 6, DARK); put(im, 2, 3, 5, 3, DARK)
        elif cat == "loco_diesel": put(im, 1, 8, 9, 8, DARK); put(im, 2, 5, 4, 5, STEEL)
        else: put(im, 4, 3, 7, 3, STEEL); im.putpixel((5, 4), STEEL); im.putpixel((6, 4), STEEL)
        wheels(im, [2, 5, 8, 12])
    elif cat == "passenger":
        put(im, 1, 5, 14, 10, c)
        for x in range(2, 14, 3): put(im, x, 6, x + 1, 7, GLASS)
        wheels(im, [3, 11])
    else:
        put(im, 1, 6, 14, 10, c); put(im, 1, 6, 14, 6, tuple(min(255, k + 35) for k in c[:3]) + (255,))
        if "tank" in v["id"]: put(im, 2, 5, 13, 5, c)
        wheels(im, [3, 11])
    return im

def glyph(kind):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    kind = {"engine": "diesel_engine", "axle": "wheelset", "motor": "traction_motor", "collector": "pantograph", "cyl": "cylinder", "trans": "gearbox",
            "tender": "tender", "tank": "tank"}.get(kind.split("_")[0], kind.split("_")[0] if kind.split("_")[0] in ("boiler", "bogie", "cab", "coupler", "wheelset", "pantograph", "body") else kind)
    if kind == "wheelset": put(im, 2, 6, 5, 9, WHEEL); put(im, 10, 6, 13, 9, WHEEL); put(im, 5, 7, 10, 8, STEEL)
    elif kind == "bogie": put(im, 1, 5, 14, 7, STEEL); put(im, 2, 8, 5, 11, WHEEL); put(im, 10, 8, 13, 11, WHEEL)
    elif kind == "boiler": put(im, 1, 5, 14, 11, (90, 60, 40, 255)); put(im, 1, 7, 14, 7, (200, 150, 60, 255)); put(im, 3, 2, 5, 4, DARK)
    elif kind == "diesel_engine": put(im, 2, 4, 13, 12, (60, 90, 70, 255)); put(im, 4, 2, 11, 3, STEEL)
    elif kind == "traction_motor": put(im, 3, 3, 12, 12, (70, 90, 150, 255)); put(im, 6, 6, 9, 9, STEEL)
    elif kind == "pantograph": put(im, 2, 3, 13, 3, STEEL); im.putpixel((5, 4), STEEL); im.putpixel((6, 5), STEEL); im.putpixel((7, 6), STEEL); put(im, 4, 12, 11, 12, DARK)
    elif kind == "cab": put(im, 3, 3, 12, 12, (120, 90, 60, 255)); put(im, 5, 5, 10, 8, GLASS)
    elif kind == "coupler": put(im, 2, 7, 13, 9, STEEL); put(im, 12, 5, 14, 11, DARK)
    elif kind == "cylinder": put(im, 2, 6, 11, 9, STEEL); put(im, 11, 7, 14, 8, DARK); put(im, 2, 5, 3, 10, DARK)
    elif kind == "gearbox": put(im, 3, 4, 12, 11, (90, 90, 95, 255)); put(im, 6, 6, 9, 9, DARK); put(im, 1, 7, 2, 8, STEEL); put(im, 13, 7, 14, 8, STEEL)
    elif kind == "tender": put(im, 1, 5, 14, 11, (45, 45, 50, 255)); put(im, 2, 3, 13, 4, (25, 25, 28, 255)); wheels(im, [3, 11])
    elif kind == "tank": put(im, 2, 5, 13, 10, (120, 120, 128, 255)); put(im, 2, 5, 13, 5, (170, 170, 178, 255))
    else: put(im, 1, 3, 14, 12, (130, 95, 60, 255)); put(im, 1, 3, 14, 3, DARK)  # body
    return im

def tint(im, size):
    f = {"xs": 1.15, "s": 1.0, "m": 0.85, "l": 0.7}.get(size, 1.0)
    px = im.load()
    for x in range(16):
        for y in range(16):
            r, g, b, a = px[x, y]
            if a: px[x, y] = (int(r * f), int(g * f), int(b * f), a)
    return im

def tile(base, rivet):
    im = Image.new("RGBA", (16, 16), base + (255,))
    put(im, 0, 0, 15, 0, rivet + (255,)); put(im, 0, 15, 15, 15, rivet + (255,)); put(im, 0, 0, 0, 15, rivet + (255,)); put(im, 15, 0, 15, 15, rivet + (255,))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)): im.putpixel((x, y), rivet + (255,))
    return im

for v in Y["vehicles"]: vehicle(v).save(f"{R}/item/{v['id']}.png")
for p in Y["parts"]:
    pid = p["item"].split(":")[1]; tint(glyph(p["kind"]), pid.rsplit("_", 1)[1]).save(f"{R}/item/{pid}.png")
tc = {"WORKSHOP": ((110, 85, 55), (70, 50, 30)), "ERECTING_SHOP": ((120, 90, 80), (70, 55, 50)), "ASSEMBLY_LINE": ((95, 105, 120), (55, 65, 80)), "AUTOMATED": ((200, 205, 215), (90, 160, 210))}
for c in Y["controllers"]: tile(*tc[c["tier"]]).save(f"{R}/block/{c['id']}.png")
for c in Y["cranes"]:
    h = hashlib.md5(c["id"].encode()).digest()
    base = (110, 85, 55) if c["age"] <= 4 and c["lift_kg"] < 10000 else (210, 170, 40) if c["lift_kg"] < 100000 else (180, 70, 40)
    t = tile(base, (40, 40, 44))
    for i in range(1, 15, 2): t.putpixel((i, i), (40, 40, 44, 255)); t.putpixel((15 - i, i), (40, 40, 44, 255))
    t.save(f"{R}/block/{c['id']}.png")
print("textures done")
