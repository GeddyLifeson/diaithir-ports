#!/usr/bin/env python3
"""Writes the original textures, models, blockstates, item definitions, lang, recipes, loot, tags, sound, age gate and library entries for
diaithir-steamrails. Everything is drawn here from scratch: riveted iron and copper plate, a firebox with a glowing grate, a coal tender,
spoked iron wheels. Run from anywhere; output goes under ../src/main/resources and ../../library-sync."""
import json, math, os, random, struct, subprocess, wave
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirsteamrails"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "textures/item", "models/block/bogey", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
for d in ("recipe", "loot_table/blocks"):
    os.makedirs(os.path.join(D, d), exist_ok=True)
os.makedirs(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable"), exist_ok=True)
os.makedirs(os.path.join(A, "sounds"), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)
rnd = random.Random(26)
S = 32  # texture size


def clamp(v):
    return max(0, min(255, int(v)))


def shade(c, f):
    return (clamp(c[0] * f), clamp(c[1] * f), clamp(c[2] * f))


def noise_fill(im, base, amp=10, speck=None, speck_p=0.0):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            n = rnd.uniform(-amp, amp)
            c = (clamp(base[0] + n), clamp(base[1] + n), clamp(base[2] + n * 0.9))
            if speck and rnd.random() < speck_p:
                c = speck
            px[x, y] = c + (255,)


def rivet(px, x, y, base, hi=1.35, lo=0.55):
    """A domed rivet: one bright pixel up-left, one dark down-right."""
    if 0 <= x < S and 0 <= y < S:
        px[x, y] = shade(base, hi) + (255,)
    if 0 <= x + 1 < S and 0 <= y + 1 < S:
        px[x + 1, y + 1] = shade(base, lo) + (255,)


def plates(base, seam=0.62, rivet_col=None, w=16, h=16, ox=0, oy=0, rust=None):
    """A sheet of riveted plates: w by h pixel panels with dark seams and rivets near each corner."""
    im = Image.new("RGBA", (S, S))
    noise_fill(im, base, 8, rust, 0.03 if rust else 0)
    px = im.load()
    rc = rivet_col or shade(base, 1.0)
    for y in range(S):
        for x in range(S):
            if (x - ox) % w == 0 or (y - oy) % h == 0:
                px[x, y] = shade(base, seam) + (255,)
            elif (x - ox) % w == 1 or (y - oy) % h == 1:
                px[x, y] = shade(base, 1.15) + (255,)
    for py in range(oy, S, h):
        for pxx in range(ox, S, w):
            for dx, dy in ((3, 3), (w - 5, 3), (3, h - 5), (w - 5, h - 5)):
                rivet(px, pxx + dx, py + dy, rc)
    return im


IRON = (88, 90, 96)
DARK = (46, 48, 54)
COPPER = (176, 98, 56)
VERD = (70, 150, 120)
STEEL = (112, 124, 140)
BRASS = (196, 160, 70)
RED = (122, 38, 30)
COAL = (28, 28, 32)


def save(im, rel):
    im.save(os.path.join(A, rel))


def band(im, y0, y1, col):
    px = im.load()
    for y in range(y0, y1):
        for x in range(S):
            n = rnd.uniform(-5, 5)
            px[x, y] = (clamp(col[0] + n), clamp(col[1] + n), clamp(col[2] + n), 255)
    for x in range(2, S, 6):
        rivet(px, x, y0 + (y1 - y0) // 2 - 1, col)


# ---- blocks ------------------------------------------------------------------------------------------------------
side = plates(IRON, rust=(110, 70, 46))
band(side, 3, 7, shade(IRON, 0.75))
band(side, 25, 29, shade(IRON, 0.75))
save(side, "textures/block/boiler_firebox_side.png")
top = plates(shade(IRON, 0.9), w=32, h=32)
d = ImageDraw.Draw(top)
d.ellipse([9, 9, 22, 22], outline=shade(IRON, 0.5), fill=shade(IRON, 0.7))
d.ellipse([12, 12, 19, 19], outline=shade(IRON, 1.3), fill=shade(IRON, 0.62))
save(top, "textures/block/boiler_firebox_top.png")
for lit in (False, True):
    f = plates(IRON, rust=(110, 70, 46))
    px = f.load()
    d = ImageDraw.Draw(f)
    d.rectangle([6, 5, 25, 27], fill=DARK + (255,), outline=shade(IRON, 0.45) + (255,))
    d.rectangle([8, 7, 23, 25], fill=(20, 18, 20, 255))
    for y in range(9, 24, 3):  # grate bars with fire behind
        for x in range(9, 23):
            px[x, y] = ((255, 150 - (y - 9) * 3, 40) if lit else (50, 44, 44)) + (255,)
            px[x, y + 1] = ((200, 80, 24) if lit else (34, 30, 30)) + (255,)
    if lit:
        for y in range(8, 25):
            for x in range(9, 23):
                if px[x, y][0] < 80 and rnd.random() < 0.4:
                    px[x, y] = (110, 38, 14, 255)
    d.rectangle([22, 15, 24, 18], fill=BRASS + (255,))  # handle
    d.rectangle([6, 3, 25, 4], fill=BRASS + (255,))
    save(f, f"textures/block/boiler_firebox_front{'_lit' if lit else ''}.png")

cop = plates(COPPER, rivet_col=shade(COPPER, 1.25), w=16, h=32, rust=VERD)
px = cop.load()
for y in range(0, S):  # verdigris creeping along seams
    for x in (0, 1, 16, 17):
        if rnd.random() < 0.5:
            px[x, y] = shade(VERD, rnd.uniform(0.8, 1.1)) + (255,)
band(cop, 12, 15, shade(COPPER, 0.8))
band(cop, 22, 25, shade(COPPER, 0.8))
save(cop, "textures/block/copper_boiler_shell.png")
ste = plates(STEEL, rivet_col=(190, 200, 215), w=32, h=16)
band(ste, 6, 10, (62, 70, 84))
band(ste, 22, 26, (62, 70, 84))
save(ste, "textures/block/steel_boiler_shell.png")

stack = Image.new("RGBA", (S, S))
noise_fill(stack, DARK, 6)
px = stack.load()
for x in range(S):  # rounded shading across the column
    f = 0.65 + 0.7 * math.sin(math.pi * (x + 0.5) / S)
    for y in range(S):
        r, g, b, _ = px[x, y]
        px[x, y] = (clamp(r * f), clamp(g * f), clamp(b * f), 255)
band(stack, 13, 16, shade(IRON, 0.8))
band(stack, 27, 30, shade(BRASS, 0.8))
save(stack, "textures/block/smokestack.png")

cyl = plates(shade(IRON, 0.85), w=16, h=32)
px = cyl.load()
for x in range(S):
    f = 0.7 + 0.5 * math.sin(math.pi * (x + 0.5) / S)
    for y in range(S):
        r, g, b, a = px[x, y]
        px[x, y] = (clamp(r * f), clamp(g * f), clamp(b * f), 255)
band(cyl, 2, 6, BRASS)
band(cyl, 26, 30, BRASS)
save(cyl, "textures/block/steam_cylinder.png")

wh = Image.new("RGBA", (S, S))
noise_fill(wh, BRASS, 10)
px = wh.load()
for y in range(S):
    for x in range(S):
        f = 0.8 + 0.4 * math.sin(math.pi * (y + 0.5) / S)
        r, g, b, a = px[x, y]
        px[x, y] = (clamp(r * f), clamp(g * f), clamp(b * f), 255)
for x in range(2, S, 8):
    px[x, 15] = shade(BRASS, 0.5) + (255,)
save(wh, "textures/block/steam_whistle.png")

ts = plates(RED, rust=(150, 80, 40), w=16, h=16)
band(ts, 12, 15, shade(RED, 0.7))
save(ts, "textures/block/fuel_tender_side.png")
tt = Image.new("RGBA", (S, S))
noise_fill(tt, COAL, 8)
px = tt.load()
for _ in range(120):  # lumps of coal
    x, y = rnd.randrange(S), rnd.randrange(S)
    c = rnd.choice([(14, 14, 16), (44, 44, 50), (60, 60, 68)])
    for dx in range(rnd.randint(1, 3)):
        for dy in range(rnd.randint(1, 3)):
            if x + dx < S and y + dy < S:
                px[x + dx, y + dy] = c + (255,)
d = ImageDraw.Draw(tt)
d.rectangle([0, 0, S - 1, S - 1], outline=shade(RED, 0.8) + (255,))
d.rectangle([1, 1, S - 2, S - 2], outline=shade(RED, 1.1) + (255,))
d.rectangle([21, 21, 28, 28], fill=shade(STEEL, 0.9) + (255,), outline=shade(STEEL, 0.5) + (255,))  # water hatch
d.ellipse([23, 23, 26, 26], fill=BRASS + (255,))
save(tt, "textures/block/fuel_tender_top.png")

# bogey metal: dark iron, pitted, with a red painted stripe row used by the wheel rims
bg = Image.new("RGBA", (S, S))
noise_fill(bg, (52, 54, 60), 7)
px = bg.load()
for y in range(S):
    for x in range(S):
        if y in (12, 13, 14) and (x % 8) < 6:
            px[x, y] = (150 + rnd.randint(-8, 8), 40, 34, 255)
for x in range(1, S, 6):
    for y in (4, 20, 28):
        rivet(px, x, y, (96, 98, 104))
save(bg, "textures/block/bogey_iron.png")

# ---- items -------------------------------------------------------------------------------------------------------


def item_img():
    return Image.new("RGBA", (S, S), (0, 0, 0, 0))


im = item_img()
d = ImageDraw.Draw(im)
d.polygon([(4, 20), (20, 6), (28, 10), (28, 24), (12, 28), (4, 26)], fill=shade(IRON, 1.0) + (255,), outline=shade(IRON, 0.45) + (255,))
d.polygon([(6, 20), (20, 8), (26, 11), (12, 20)], fill=shade(IRON, 1.3) + (255,))
px = im.load()
for p in ((8, 22), (22, 12), (24, 22), (12, 25)):
    rivet(px, p[0], p[1], BRASS)
save(im, "textures/item/riveted_plate.png")
im = item_img()
d = ImageDraw.Draw(im)
d.polygon([(6, 24), (8, 26), (26, 8), (24, 6)], fill=COPPER + (255,), outline=shade(COPPER, 0.5) + (255,))
d.line([(8, 22), (23, 7)], fill=shade(COPPER, 1.4) + (255,), width=1)
d.ellipse([22, 3, 29, 10], fill=shade(COPPER, 0.6) + (255,), outline=shade(COPPER, 1.2) + (255,))
d.ellipse([4, 22, 11, 29], fill=shade(COPPER, 0.6) + (255,), outline=shade(COPPER, 1.2) + (255,))
save(im, "textures/item/flue_tube.png")
im = item_img()
d = ImageDraw.Draw(im)
d.line([(6, 26), (26, 6)], fill=shade(STEEL, 1.2) + (255,), width=3)
d.line([(7, 25), (25, 7)], fill=(210, 220, 235, 255), width=1)
d.rectangle([13, 16, 19, 22], fill=BRASS + (255,), outline=shade(BRASS, 0.5) + (255,))
d.ellipse([3, 24, 9, 30], fill=shade(STEEL, 0.7) + (255,))
save(im, "textures/item/piston_rod.png")

# ---- models ------------------------------------------------------------------------------------------------------
def wj(rel, obj, base=A):
    p = os.path.join(base, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(obj, f, indent=1)
        f.write("\n")


def cube_faces(tex="#0", faces=("north", "east", "south", "west", "up", "down")):
    return {f: {"texture": tex} for f in faces}


def elem(frm, to, tex="#0", rot=None):
    e = {"from": frm, "to": to, "faces": cube_faces(tex)}
    if rot:
        e["rotation"] = rot
    return e


def block_model(name, elements, tex, parent="minecraft:block/block", extra=None):
    t = dict(tex)
    t.setdefault("particle", next(iter(tex.values())))
    obj = {"parent": parent, "textures": t, "elements": elements}
    if extra:
        obj.update(extra)
    wj(f"models/block/{name}.json", obj)


def cube_model(name, **faces):
    wj(f"models/block/{name}.json", {"parent": "minecraft:block/cube", "textures": faces})


def item_def(name, model=None):
    wj(f"items/{name}.json", {"model": {"type": "minecraft:model", "model": model or f"{NS}:item/{name}"}})


def block_item(name):
    wj(f"models/item/{name}.json", {"parent": f"{NS}:block/{name}"})
    item_def(name)


def flat_item(name):
    wj(f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    item_def(name)


T = lambda n: f"{NS}:block/{n}"
cube_model("boiler_firebox", particle=T("boiler_firebox_side"), down=T("boiler_firebox_side"), up=T("boiler_firebox_top"), north=T("boiler_firebox_front"),
           east=T("boiler_firebox_side"), south=T("boiler_firebox_side"), west=T("boiler_firebox_side"))
cube_model("boiler_firebox_lit", particle=T("boiler_firebox_side"), down=T("boiler_firebox_side"), up=T("boiler_firebox_top"), north=T("boiler_firebox_front_lit"),
           east=T("boiler_firebox_side"), south=T("boiler_firebox_side"), west=T("boiler_firebox_side"))
wj("models/block/copper_boiler_shell.json", {"parent": "minecraft:block/cube_all", "textures": {"all": T("copper_boiler_shell")}})
wj("models/block/steel_boiler_shell.json", {"parent": "minecraft:block/cube_all", "textures": {"all": T("steel_boiler_shell")}})
cube_model("fuel_tender", particle=T("fuel_tender_side"), down=T("fuel_tender_side"), up=T("fuel_tender_top"), north=T("fuel_tender_side"),
           east=T("fuel_tender_side"), south=T("fuel_tender_side"), west=T("fuel_tender_side"))
block_model("smokestack", [elem([4, 0, 4], [12, 2, 12]), elem([5, 2, 5], [11, 11, 11]), elem([4, 11, 4], [12, 13, 12]), elem([3, 13, 3], [13, 15, 13]),
                           elem([4, 15, 4], [12, 16, 12])], {"0": T("smokestack")})
# cylinder facing north: the rod comes out of the north face
block_model("steam_cylinder", [elem([3, 2, 5], [13, 11, 16]), elem([4, 11, 7], [12, 14, 13]), elem([2, 3, 3], [14, 10, 5]), elem([6, 5, 0], [10, 8, 3]),
                               elem([7, 6, 3], [9, 7, 5]), elem([5, 1, 6], [11, 2, 15])], {"0": T("steam_cylinder")})
block_model("steam_whistle", [elem([5, 0, 5], [11, 3, 11]), elem([7, 3, 7], [9, 7, 9]), elem([5, 7, 5], [11, 12, 11]), elem([6, 12, 6], [10, 13, 10])],
            {"0": T("steam_whistle")})

for n in ("boiler_firebox", "copper_boiler_shell", "steel_boiler_shell", "smokestack", "steam_cylinder", "steam_whistle", "fuel_tender"):
    block_item(n)
for n in ("riveted_plate", "flue_tube", "piston_rod"):
    flat_item(n)

ROT = {"north": 0, "east": 90, "south": 180, "west": 270}
wj("blockstates/boiler_firebox.json", {"variants": {f"facing={f},lit={l}": {"model": f"{NS}:block/boiler_firebox{'_lit' if l == 'true' else ''}", "y": r}
                                                    for f, r in ROT.items() for l in ("false", "true")}})
wj("blockstates/copper_boiler_shell.json", {"variants": {"": {"model": T("copper_boiler_shell")}}})
wj("blockstates/steel_boiler_shell.json", {"variants": {"": {"model": T("steel_boiler_shell")}}})
wj("blockstates/smokestack.json", {"variants": {"": {"model": T("smokestack")}}})
wj("blockstates/steam_cylinder.json", {"variants": {f"facing={f}": {"model": T("steam_cylinder"), "y": r} for f, r in ROT.items()}})
wj("blockstates/steam_whistle.json", {"variants": {f"powered={p}": {"model": T("steam_whistle")} for p in ("false", "true")}})
wj("blockstates/fuel_tender.json", {"variants": {f"facing={f}": {"model": T("fuel_tender"), "y": r} for f, r in ROT.items()}})
for b in ("driver_bogey", "pony_bogey"):
    wj(f"blockstates/{b}.json", {"variants": {"": {"model": "minecraft:block/air"}}})

# ---- bogey parts (client models, drawn centred on 8,8,8; see SteamBogeyClient) ---------------------------------------
BT = {"0": f"{NS}:block/bogey_iron"}


def wheel_elements(radius_px, thick_px):
    """A wheel with its axle along X: a 16-gon from rotated slabs, a hub and eight spoke stubs. Centre (8,8,8)."""
    x0, x1 = 8 - thick_px / 2, 8 + thick_px / 2
    r = radius_px
    w = r * 1.62  # slab width so neighbouring slabs overlap into a round rim
    els = []
    for ang in (-45, -22.5, 0, 22.5, 45):
        els.append(elem([x0, 8 - r, 8 - w / 2], [x1, 8 + r, 8 + w / 2], rot={"origin": [8, 8, 8], "axis": "x", "angle": ang}))
    for ang in (-22.5, 0, 22.5):
        els.append(elem([x0, 8 - w / 2, 8 - r], [x1, 8 + w / 2, 8 + r], rot={"origin": [8, 8, 8], "axis": "x", "angle": ang}))
    els.append(elem([x0 - 0.5, 8 - 2, 8 - 2], [x1 + 0.5, 8 + 2, 8 + 2]))
    return els


wj("models/block/bogey/driver_wheel.json", {"parent": "minecraft:block/block", "textures": {**BT, "particle": BT["0"]}, "elements": wheel_elements(12.5, 2)})
wj("models/block/bogey/pony_wheel.json", {"parent": "minecraft:block/block", "textures": {**BT, "particle": BT["0"]}, "elements": wheel_elements(6.5, 2)})
# frames: side beams along z between the axles, cross beams, axle boxes; spans z -8..24 (two blocks) around the centre
frame_driver = [elem([3, 6, -8], [5, 10, 24]), elem([11, 6, -8], [13, 10, 24]), elem([3, 7, 6], [13, 9, 10]), elem([3, 7, -8], [13, 9, -6]),
                elem([3, 7, 22], [13, 9, 24]), elem([6, 10, -2], [10, 12, 18]), elem([4, 5, -9], [5, 11, -7]), elem([11, 5, -9], [12, 11, -7]),
                elem([4, 5, 23], [5, 11, 25]), elem([11, 5, 23], [12, 11, 25])]
frame_pony = [elem([4, 7, -8], [6, 10, 24]), elem([10, 7, -8], [12, 10, 24]), elem([4, 8, 6], [12, 9, 10]), elem([6, 10, 0], [10, 12, 16])]
wj("models/block/bogey/driver_frame.json", {"parent": "minecraft:block/block", "textures": {**BT, "particle": BT["0"]}, "elements": frame_driver})
wj("models/block/bogey/pony_frame.json", {"parent": "minecraft:block/block", "textures": {**BT, "particle": BT["0"]}, "elements": frame_pony})
wj("models/block/bogey/driver_rod.json", {"parent": "minecraft:block/block", "textures": {**BT, "particle": BT["0"]},
                                          "elements": [elem([13.5, 7, -8], [15, 9, 24]), elem([13.5, 6, -9], [15.5, 10, -7]), elem([13.5, 6, 23], [15.5, 10, 25]),
                                                       elem([1, 7, -8], [2.5, 9, 24]), elem([0.5, 6, -9], [2.5, 10, -7]), elem([0.5, 6, 23], [2.5, 10, 25])]})

# ---- lang, library, age gate ------------------------------------------------------------------------------------------
NAMES = {
    "boiler_firebox": ("Kaldravelk", "Firebox. Burns coal, charcoal or wood, takes water, and makes the steam. Shell blocks touching it add water volume; the weakest block sets the pressure rating (firebox 12 bar). Big boilers heat slower but hold pressure under load. A smokestack on it improves the draught. Age V."),
    "copper_boiler_shell": ("Cuprenhold", "Copper boiler shell. Adds water volume to a boiler and rates it for 7 bar. Cheap, and the weak link of a high pressure boiler. Age V."),
    "steel_boiler_shell": ("Sorvannhold", "Steel boiler shell. Adds water volume and holds 16 bar, so the firebox (12 bar) becomes the limit. Age VI."),
    "smokestack": ("Tharvusk", "Smokestack. Next to a boiler it gives a stronger draught, 25 percent more steam from the same fire, and it smokes while the fire burns. Age V."),
    "steam_cylinder": ("Pistrovane", "Steam cylinder. Put against a boiler on a carriage that rides a steam bogey: each cylinder turns pressure above 1 bar into pulling power, full speed at 6 bar. Several cylinders pull longer trains. The boiler feeds about one cylinder per two size units. Age V."),
    "steam_whistle": ("Wheshindrel", "Steam whistle. Touching a boiler, a redstone signal blows it and repeats while it holds. A blast costs a little steam and the pitch rises with pressure. On a train it blows as the train pulls out. Age V."),
    "fuel_tender": ("Vaskurmeth", "Fuel and water tender. Holds 16000 mB of water and nine stacks of fuel. Boilers beside it, or on any carriage of the same train, draw from it. Pour buckets in, load fuel by hand or with a hopper, and read it bare-handed. Age V."),
    "driver_bogey": ("Gravenroq", "Driver bogey, steam style. Make a bogey as usual (railway casing on the assembly track) and use a wrench on it to cycle styles. A carriage needs a driver or pony bogey for its boiler to pull the train. Age V."),
    "pony_bogey": ("Lepturin", "Pony bogey, steam style: the small leading truck of a locomotive. Wrench a plain small bogey to get it. Age V."),
    "riveted_plate": ("Nitharok", "Riveted plate. Boiler and tender skin. Age V."),
    "flue_tube": ("Cavrelune", "Flue tube. Copper pipe for boilers, chimneys and whistles. Age V."),
    "piston_rod": ("Strumvekt", "Piston rod. Steel rod for steam cylinders. Age V."),
}
ITEMS_ONLY = ("riveted_plate", "flue_tube", "piston_rod")
lang = {}
for k, (n, _) in NAMES.items():
    lang[("item." if k in ITEMS_ONLY else "block.") + f"{NS}.{k}"] = n
lang["bogey.diaithirsteamrails.driver"] = "Gravenroq driver"
lang["bogey.diaithirsteamrails.pony"] = "Lepturin pony"
lang["message.diaithirsteamrails.boiler"] = "Pressure %1$s of %2$s bar, water %3$s of %4$s mB, %5$s fuel, %6$s cylinders, fire %7$s"
lang["message.diaithirsteamrails.tender"] = "Water %1$s of %2$s mB, %3$s fuel"
lang["message.diaithirsteamrails.fire_lit"] = "burning"
lang["message.diaithirsteamrails.fire_out"] = "out"
lang["subtitles.diaithirsteamrails.steam_whistle"] = "Steam whistle blows"
wj("lang/en_us.json", lang)

with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v[0], "text": v[1]} for k, v in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")

TIER1 = ["boiler_firebox", "copper_boiler_shell", "smokestack", "steam_cylinder", "steam_whistle", "fuel_tender", "riveted_plate", "flue_tube", "piston_rod"]
with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 5, "items": [f"{NS}:{k}" for k in TIER1]},
               "tier2": {"age": 6, "items": [f"{NS}:steel_boiler_shell"]}}, f, indent=2)
    f.write("\n")

BLOCKS = ["boiler_firebox", "copper_boiler_shell", "steel_boiler_shell", "smokestack", "steam_cylinder", "steam_whistle", "fuel_tender"]
wj("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:{k}" for k in BLOCKS + ["driver_bogey", "pony_bogey"]]}, ROOT)
wj("data/minecraft/tags/block/needs_stone_tool.json", {"values": [f"{NS}:{k}" for k in BLOCKS]}, ROOT)
for k in BLOCKS:
    wj(f"loot_table/blocks/{k}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{k}"}],
                                                                            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}, D)
for k in ("driver_bogey", "pony_bogey"):  # a bogey gives back its casing, like Create's own
    wj(f"loot_table/blocks/{k}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "create:railway_casing"}],
                                                                            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}, D)


def recipe(name, obj, count=1):
    obj["result"] = {"id": f"{NS}:{name}", "count": count}
    obj["category"] = "misc"
    wj(f"recipe/{name}.json", obj, D)


def shaped(name, pattern, key, count=1):
    recipe(name, {"type": "minecraft:crafting_shaped", "key": key, "pattern": pattern}, count)


P, TUBE, ROD = f"{NS}:riveted_plate", f"{NS}:flue_tube", f"{NS}:piston_rod"
shaped("riveted_plate", ["NI", "IN"], {"N": "minecraft:iron_nugget", "I": "minecraft:iron_ingot"}, 2)
shaped("flue_tube", ["C", "C", "C"], {"C": "minecraft:copper_ingot"}, 2)
shaped("piston_rod", ["I", "I", "N"], {"I": "minecraft:iron_ingot", "N": "minecraft:iron_nugget"}, 2)
shaped("boiler_firebox", ["PPP", "PFP", "PTP"], {"P": P, "F": "minecraft:furnace", "T": TUBE})
shaped("copper_boiler_shell", ["CPC", "PTP", "CPC"], {"C": "minecraft:copper_ingot", "P": P, "T": TUBE}, 2)
shaped("steel_boiler_shell", ["PIP", "PTP", "PIP"], {"I": "minecraft:iron_ingot", "P": P, "T": TUBE}, 2)
shaped("smokestack", ["T", "T", "P"], {"T": TUBE, "P": P})
shaped("steam_cylinder", ["PPP", "TRT"], {"P": P, "T": TUBE, "R": ROD})
shaped("steam_whistle", ["N", "C", "T"], {"N": "minecraft:iron_nugget", "C": "minecraft:copper_ingot", "T": TUBE})
shaped("fuel_tender", ["PPP", "BCB", "PPP"], {"P": P, "B": "minecraft:bucket", "C": "minecraft:chest"})

# ---- sound: a three-reed whistle, a chord with breath and a slow wobble, 2.4 seconds ------------------------------------
sounds_dir = os.path.join(A, "sounds")
wav = os.path.join(sounds_dir, "steam_whistle.wav")
RATE, DUR = 44100, 2.4
frames = bytearray()
r2 = random.Random(5)
for i in range(int(RATE * DUR)):
    t = i / RATE
    env = min(1.0, t / 0.09) * (1.0 if t < DUR - 0.7 else max(0.0, (DUR - t) / 0.7)) ** 1.4
    wob = 1 + 0.004 * math.sin(2 * math.pi * 5.2 * t)
    s = 0.0
    for f, a in ((392.0, 0.5), (494.0, 0.38), (587.0, 0.3)):
        ph = 2 * math.pi * f * wob * t
        s += a * (math.sin(ph) + 0.35 * math.sin(2 * ph) + 0.12 * math.sin(3 * ph))
    s += 0.12 * r2.uniform(-1, 1) * (1 - math.exp(-t * 20))  # breath
    frames += struct.pack("<h", int(max(-1, min(1, s * env * 0.42)) * 32000))
with wave.open(wav, "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(RATE)
    w.writeframes(bytes(frames))
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "4", os.path.join(sounds_dir, "steam_whistle.ogg")], check=True)
os.remove(wav)
wj("sounds.json", {"steam_whistle": {"subtitle": "subtitles.diaithirsteamrails.steam_whistle", "sounds": [{"name": f"{NS}:steam_whistle", "attenuation_distance": 96}]}})
print("ok")
