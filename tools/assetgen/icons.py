"""Flat 16x16 item icons drawn from primitives."""
from tex import *

OUT = (22, 22, 26)

def outline(im):
    s = im.width
    o = im.copy()
    for y in range(s):
        for x in range(s):
            if im.getpixel((x, y))[3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < s and 0 <= ny < s and im.getpixel((nx, ny))[3] > 0:
                        px(o, x, y, OUT); break
    return o

def poly(im, pts, c):
    for (x, y) in pts: px(im, x, y, c)

def rect(im, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1): px(im, x, y, c)

def lit(c): return shade(c, 1.3)
def drk(c): return shade(c, 0.7)

def ingot(c, n):
    im = img()
    for i in range(7):
        rect(im, 3 + i // 2, 9 - i // 2 + (0), 12 - (6 - i) // 2, 9 - i // 2, c) if False else None
    rect(im, 2, 8, 12, 11, c); rect(im, 4, 6, 14, 9, shade(c, 1.12)); rect(im, 4, 6, 14, 6, lit(c)); rect(im, 2, 11, 12, 11, drk(c))
    rect(im, 12, 9, 14, 9, c)
    return outline(im)

def nugget(c, n):
    im = img(); rect(im, 6, 7, 9, 10, c); rect(im, 5, 8, 10, 9, c); rect(im, 6, 7, 8, 7, lit(c)); rect(im, 7, 10, 9, 10, drk(c))
    return outline(im)

def sheet(c, n):
    im = img(); rect(im, 3, 3, 12, 12, c); rect(im, 3, 3, 12, 3, lit(c)); rect(im, 3, 3, 3, 12, lit(c)); rect(im, 12, 3, 12, 12, drk(c)); rect(im, 3, 12, 12, 12, drk(c))
    rivets(im, lit(c), 5) if False else None
    px(im, 5, 5, drk(c)); px(im, 10, 10, drk(c))
    return outline(im)

def dust(c, n):
    im = img(); r = rng(n)
    for y in range(7, 13):
        w = (y - 6)
        for x in range(8 - w, 8 + w):
            if 0 <= x < 16: px(im, x, y, shade(c, 1 + (r.random() - 0.5) * 0.3))
    return outline(im)

def crushed(c, n):
    im = img(); r = rng(n)
    for (cx, cy) in ((5, 9), (9, 8), (7, 11), (10, 11), (6, 6)):
        rect(im, cx - 1, cy - 1, cx + 1, cy + 1, shade(c, 1 + (r.random() - 0.5) * 0.4)); px(im, cx - 1, cy - 1, lit(c))
    return outline(im)

def gear(c, n, big=False):
    im = img(); cx = cy = 7.5; R = 6.5 if big else 5.5
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            ang = math.atan2(y - cy, x - cx)
            tooth = 1.3 if int((ang + math.pi) / (2 * math.pi) * 12) % 2 == 0 else 0
            if d < R + tooth and d > 1.8: px(im, x, y, shade(c, 1.15 if (x + y) % 5 else 0.9))
    return outline(im)

def paper(c, n, stripe=None):
    im = img(); rect(im, 3, 2, 12, 13, (226, 218, 190)); rect(im, 3, 2, 12, 2, (246, 240, 220)); rect(im, 3, 13, 12, 13, (176, 168, 140))
    for y in (5, 7, 9, 11): rect(im, 5, y, 10, y, shade(c, 0.9))
    if stripe: rect(im, 3, 4, 12, 5, stripe)
    return outline(im)

def bucket(c, n):
    im = img(); rect(im, 3, 4, 12, 13, PAL["iron"]); rect(im, 4, 6, 11, 12, c); rect(im, 3, 4, 12, 4, lit(PAL["iron"])); rect(im, 3, 13, 12, 13, drk(PAL["iron"]))
    rect(im, 5, 2, 10, 3, PAL["iron"]); px(im, 7, 2, OUT); px(im, 8, 2, OUT)
    return outline(im)

def food(c, n):
    im = img()
    for y in range(16):
        for x in range(16):
            if math.hypot(x - 7.5, y - 8) < 5: px(im, x, y, shade(c, 1.2 if x + y < 14 else 0.85))
    rect(im, 7, 2, 8, 4, (90, 140, 50))
    return outline(im)

def wrench(c, n):
    im = img()
    for i in range(10): rect(im, 3 + i, 12 - i, 4 + i, 13 - i, PAL["iron"])
    rect(im, 10, 2, 13, 5, c); rect(im, 11, 2, 12, 3, (0, 0, 0, 0)) if False else None
    for (x, y) in ((11, 2), (12, 2)): px(im, x, y, (0, 0, 0), 0)
    return outline(im)

def goggles(c, n):
    im = img(); rect(im, 1, 6, 14, 8, PAL["rubber"]); 
    for cx in (4, 11):
        for y in range(16):
            for x in range(16):
                if math.hypot(x - cx, y - 7) < 3.2: px(im, x, y, PAL["brass"] if math.hypot(x - cx, y - 7) > 2.2 else (110, 200, 220))
    return outline(im)

def helmet(c, n):
    im = img(); rect(im, 3, 3, 12, 6, c); rect(im, 2, 6, 13, 12, c); rect(im, 4, 8, 11, 11, (0, 0, 0, 0)) if False else None
    rect(im, 4, 8, 11, 10, (110, 200, 220)); rect(im, 3, 3, 12, 3, lit(c)); rect(im, 2, 12, 13, 12, drk(c))
    return outline(im)

def boots(c, n):
    im = img(); rect(im, 3, 3, 7, 11, c); rect(im, 9, 3, 12, 11, c); rect(im, 2, 11, 7, 13, drk(c)); rect(im, 9, 11, 14, 13, drk(c)); rect(im, 3, 3, 7, 3, lit(c)); rect(im, 9, 3, 12, 3, lit(c))
    return outline(im)

def chest(c, n):
    im = img(); rect(im, 2, 3, 13, 13, c); rect(im, 5, 2, 10, 4, (0, 0, 0, 0)); rect(im, 0, 3, 2, 8, c); rect(im, 13, 3, 15, 8, c); rect(im, 2, 3, 13, 3, lit(c)); rect(im, 5, 6, 10, 13, shade(c, 0.9))
    return outline(im)

def legs(c, n):
    im = img(); rect(im, 3, 2, 12, 6, c); rect(im, 3, 7, 7, 13, c); rect(im, 8, 7, 12, 13, c); rect(im, 3, 2, 12, 2, lit(c)); rect(im, 7, 7, 8, 13, (0, 0, 0, 0))
    return outline(im)

def sword(c, n):
    im = img()
    for i in range(9): rect(im, 3 + i, 12 - i, 4 + i, 13 - i, c)
    rect(im, 2, 11, 5, 14, PAL["wood"]); rect(im, 5, 8, 8, 10, drk(c)) if False else None
    return outline(im)

def box(c, n):
    im = img(); rect(im, 2, 4, 13, 13, c); rect(im, 2, 4, 13, 5, lit(c)); rect(im, 2, 13, 13, 13, drk(c)); rect(im, 7, 4, 8, 13, shade(c, 0.8)); rect(im, 2, 8, 13, 8, shade(c, 0.85))
    return outline(im)

def tube(c, n):
    im = img(); rect(im, 6, 2, 9, 12, (190, 210, 220)); rect(im, 5, 12, 10, 14, PAL["brass"]); rect(im, 7, 5, 8, 10, c)
    return outline(im)

def cannon(c, n):
    im = img(); rect(im, 3, 6, 13, 9, c); rect(im, 1, 5, 4, 10, drk(c)); rect(im, 5, 10, 7, 13, PAL["wood"])
    return outline(im)

def hand(c, n):
    im = img(); rect(im, 4, 7, 11, 13, c); 
    for x in (4, 6, 8, 10): rect(im, x, 3, x + 1, 7, c)
    return outline(im)

def propeller(c, n):
    im = img(); rect(im, 7, 7, 8, 8, PAL["steel"])
    for (dx, dy) in ((0, -1), (0, 1), (-1, 0), (1, 0)):
        for k in range(2, 7): rect(im, 7 + dx * k, 7 + dy * k, 8 + dx * k, 8 + dy * k, c)
    return outline(im)

def coupling(c, n):
    im = img()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 3.5 < d < 6: px(im, x, y, c)
    return outline(im)

def cake(c, n):
    im = img(); rect(im, 2, 8, 13, 13, c); rect(im, 2, 6, 13, 8, (240, 220, 200)); rect(im, 7, 3, 8, 6, (250, 200, 60))
    return outline(im)

def cardboard(c, n): return box((176, 140, 96), n)

def pack_icon(c, n):
    im = box((186, 148, 100), n)
    rect(im, 6, 4, 9, 13, (210, 190, 150))
    return im

def disk(c, n):
    im = img()
    for y in range(16):
        for x in range(16):
            if math.hypot(x - 7.5, y - 7.5) < 5.5: px(im, x, y, c)
    return outline(im)

def crystal(c, n):
    im = img()
    for y in range(2, 14):
        w = 4 - abs(y - 8) // 2
        rect(im, 8 - w, y, 7 + w, y, shade(c, 1.1 if y < 8 else 0.85))
    rect(im, 6, 4, 6, 7, lit(c))
    return outline(im)

def blueprint(c, n):
    im = img(); rect(im, 2, 3, 13, 12, (40, 80, 140)); 
    for x in range(3, 13, 3): rect(im, x, 4, x, 11, (110, 150, 210))
    for y in range(5, 12, 3): rect(im, 3, y, 12, y, (110, 150, 210))
    return outline(im)

def extend(c, n):
    im = img(); rect(im, 2, 6, 13, 9, c); rect(im, 0, 4, 3, 11, drk(c)); rect(im, 12, 5, 15, 10, PAL["brass"])
    return outline(im)

def wand(c, n):
    im = img()
    for i in range(10): rect(im, 3 + i, 12 - i, 4 + i, 13 - i, PAL["brass"])
    rect(im, 10, 2, 13, 5, (110, 200, 220))
    return outline(im)

def controller(c, n):
    im = img(); rect(im, 3, 4, 12, 12, PAL["steel"]); rect(im, 4, 5, 11, 7, drk(PAL["steel"]))
    for (x, y) in ((5, 9), (8, 9), (11, 9), (5, 11)): px(im, x, y, (200, 60, 60))
    return outline(im)

def sandpaper(c, n):
    im = img(); rect(im, 3, 3, 12, 12, c); rr = rng(n)
    for _ in range(26): px(im, rr.randrange(3, 13), rr.randrange(3, 13), shade(c, 0.7 + rr.random() * 0.6))
    return outline(im)

def whisk(c, n):
    im = img()
    for i in range(6): rect(im, 7, 9 + i // 2 if False else 8, 8, 13, PAL["iron"]) if False else None
    rect(im, 7, 10, 8, 14, PAL["wood"])
    for x in (4, 6, 9, 11): rect(im, x, 3, x, 9, PAL["iron"])
    rect(im, 4, 9, 11, 9, PAL["iron"])
    return outline(im)

ICON_RULES = [
    ("bucket", bucket), ("ingot", ingot), ("nugget", nugget), ("sheet", sheet), ("flour", dust), ("powder", dust), ("crushed", crushed),
    ("goggles", goggles), ("helmet", helmet), ("boots", boots), ("chestplate", chest), ("leggings", legs), ("sword", sword),
    ("wrench", wrench), ("package", pack_icon), ("cardboard", cardboard), ("filter", paper), ("schedule", paper), ("shopping_list", paper),
    ("schematic", blueprint), ("blueprint", blueprint), ("electron_tube", tube), ("transmitter", tube), ("potato_cannon", cannon),
    ("brass_hand", hand), ("propeller", propeller), ("coupling", coupling), ("cake", cake), ("sweet_roll", food), ("apple", food),
    ("berries", food), ("chocolate", food), ("dough", food), ("tea", food), ("extendo", extend), ("wand", wand), ("controller", controller),
    ("sand_paper", sandpaper), ("whisk", whisk), ("precision_mechanism", gear), ("mechanism", gear), ("quartz", crystal),
    ("experience", crystal), ("pulp", dust), ("fertilizer", dust), ("glue", disk), ("worldshaper", wand), ("crafter_slot", disk),
    ("minecart", box), ("super_glue", disk), ("andesite_alloy", ingot), ("obsidian", dust), ("raw_zinc", crushed), ("sturdy", sheet),
]

def material_color(name):
    n = name
    for k, v in (("zinc", PAL["zinc"]), ("brass", PAL["brass"]), ("copper", PAL["copper"]), ("golden", PAL["gold"]), ("gold", PAL["gold"]),
                 ("iron", PAL["iron"]), ("netherite", (70, 60, 70)), ("andesite", PAL["andesite"]), ("quartz", (232, 150, 160)),
                 ("obsidian", (50, 30, 70)), ("sturdy", (70, 66, 72)), ("cardboard", (176, 140, 96)), ("chocolate", (100, 60, 40)),
                 ("honey", (240, 190, 40)), ("tea", (150, 90, 40)), ("flour", (236, 228, 200)), ("cinder", (90, 80, 76)),
                 ("osmium", (120, 150, 190)), ("platinum", (200, 220, 230)), ("silver", (210, 214, 220)), ("tin", (190, 200, 205)),
                 ("lead", (80, 84, 110)), ("quicksilver", (190, 190, 210)), ("aluminum", (200, 205, 210)), ("uranium", (110, 160, 60)),
                 ("nickel", (170, 180, 150)), ("pulp", (200, 190, 150)), ("experience", (140, 220, 60)), ("tree", (200, 220, 200)),
                 ("potion", (90, 130, 220))):
        if k in n: return v
    return PAL["steel"]

def make_icon(name):
    c = material_color(name)
    for k, fn in ICON_RULES:
        if k in name:
            return fn(c, name)
    return gear(c, name)
