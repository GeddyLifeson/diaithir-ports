"""Hand designed models and textures for the shipped Create slice (shafts, cogs, wheels, press, basin, belts, mixer, motor,
depot, chute, funnels, tunnels, pipes, casings). Everything is drawn by art.py; nothing is copied."""
import math
from art import *


def box(frm, to, faces, rot=None, uv=None):
    """faces: texture string for all faces, or dict face -> texture (or (texture, uv, rotation))"""
    e = {"from": [round(v, 4) for v in frm], "to": [round(v, 4) for v in to], "faces": {}}
    for f in ("north", "south", "east", "west", "up", "down"):
        t = faces if isinstance(faces, str) else faces.get(f)
        if t is None:
            continue
        d = {}
        if isinstance(t, tuple):
            d["texture"] = t[0]
            if t[1] is not None:
                d["uv"] = t[1]
            if len(t) > 2 and t[2]:
                d["rotation"] = t[2]
        else:
            d["texture"] = t
            if uv:
                d["uv"] = uv
        e["faces"][f] = d
    if rot:
        e["rotation"] = rot
    return e


def R(angle, origin=(8, 8, 8), axis="y"):
    return {"origin": list(origin), "axis": axis, "angle": angle}


FULL = [0, 0, 16, 16]


def gear_elements(sq_angles, s, y0, y1, face_tex, edge_tex=None, jitter=True):
    """union of squares (half side s) rotated about the axle; their corners are the teeth"""
    els = []
    for i, a in enumerate(sq_angles):
        j = i * 0.004 if jitter else 0
        t = {"up": (face_tex, FULL), "down": (face_tex, FULL)}
        for f in ("north", "south", "east", "west"):
            t[f] = (edge_tex or face_tex, [0, 6, 16, 10])
        els.append(box([8 - s, y0 + j, 8 - s], [8 + s, y1 - j, 8 + s], t, R(a) if a else None))
    return els


def run(g):
    T = g.T
    wtex = g.wtex
    model = g.model
    bstate = g.bstate
    emodel = g.emodel
    rotv = g.rot_variants

    # ------------------------------------------------------------------ textures
    wtex("shaft", shaft_side("shaft"))
    wtex("shaft_end", shaft_end("shaftend"))
    c = Cv(None); c.cyl_v(0, 0, 64, 64, BRASS); c.rect(0, 0, 64, 1, sh(BRASS, 1.4)); c.rect(0, 63, 64, 64, sh(BRASS, 0.5))
    wtex("shaft_collar", c.finish("collar", 0.2, 0, 0.02, 0))
    c = Cv(None); c.cyl_v(0, 0, 64, 64, HAZ_Y)
    for y in range(0, 64, 16): c.rect(0, y + 4, 64, y + 12, HAZ_K)
    wtex("powered_band", c.finish("pband", 0.2, 0, 0.02, 0))

    def cog_face(name, body, rim):
        c = Cv(body)
        c.rect(0, 0, 64, 64, body)
        for r_ in (14, 24):
            c.ellipse(32 - r_, 32 - r_, 32 + r_, 32 + r_, sh(body, 0.62)); c.ellipse(32 - r_ + 1.4, 32 - r_ + 1.4, 32 + r_ - 1.4, 32 + r_ - 1.4, body)
        for k in range(8):
            c.rivet(32 + 19 * math.cos(math.radians(k * 45)), 32 + 19 * math.sin(math.radians(k * 45)), 1.6, body)
        return c.finish(name, 0.4, 0.06, 0.02, 0.0)

    wtex("cog_steel", cog_face("cogs", sh(STEEL, 1.02), STEEL_D))
    wtex("cog_bronze", cog_face("cogb", (112, 92, 58), BRASS))
    c = Cv(None); c.cyl_v(0, 0, 64, 64, sh(BRASS, 1.0)); c.rect(0, 0, 64, 2, sh(BRASS, 1.4)); c.rect(0, 62, 64, 64, sh(BRASS, 0.5))
    wtex("cog_hub", c.finish("hub", 0.2, 0, 0.02, 0))
    c = Cv(sh(BRASS, 0.8)); c.ellipse(6, 6, 58, 58, sh(BRASS, 1.1)); c.ellipse(14, 14, 50, 50, sh(STEEL_D, 0.6)); c.ellipse(22, 22, 42, 42, STEEL)
    c.bevel(26, 26, 38, 38, sh(STEEL, 0.9), 1.2, False)
    wtex("cog_hub_end", c.finish("hubend", 0.2, 0, 0.03, 0))

    # ------------------------------------------------------------------ shaft
    def shaft_elems(y0=0, y1=16, tex="shaft", end="shaft_end"):
        els = [box([6, y0, 6], [10, y1, 10], {"north": (T(tex), FULL), "south": (T(tex), FULL), "east": (T(tex), FULL), "west": (T(tex), FULL),
                                               "up": (T(end), FULL), "down": (T(end), FULL)})]
        for (a, b) in ((0, 1.75), (7.1, 8.9), (14.25, 16)):
            if a >= y0 - 0.01 and b <= y1 + 0.01:
                els.append(box([5.5, a, 5.5], [10.5, b, 10.5], {f: (T("shaft_collar"), FULL) for f in ("north", "south", "east", "west")} | {"up": (T("shaft_end"), FULL), "down": (T("shaft_end"), FULL)}))
        return els

    tx = {"particle": T("shaft")}
    model("block/shaft", emodel(tx, shaft_elems()))
    model("block/shaft_half", emodel(tx, shaft_elems(0, 8)))
    pb = box([4.6, 3.5, 4.6], [11.4, 12.5, 11.4], {f: (T("powered_band"), FULL) for f in ("north", "south", "east", "west")} | {"up": (T("powered_band"), FULL), "down": (T("powered_band"), FULL)})
    model("block/powered_shaft", emodel(tx, shaft_elems() + [pb]))

    # ------------------------------------------------------------------ cogwheels
    def cog(small=True):
        body = "cog_steel" if small else "cog_bronze"
        face = T(body)
        if small:
            s_, y0, y1, L, wt = 4.8, 6.5, 9.5, 7.4, 2.1
            angs_body = (0, 45)
            bars = [("x", 0), ("z", 0), ("x", 45), ("x", -45)]
        else:
            s_, y0, y1, L, wt = 11.0, 6.7, 9.3, 16.0, 2.8
            angs_body = (0, 22.5, 45, -22.5)
            bars = [(ax, a) for ax in ("x", "z") for a in (0, 22.5, 45, -22.5)]
        els = gear_elements(angs_body, s_, y0, y1, face)
        for k, (ax, a) in enumerate(bars):
            j = 0.003 * (k + 1)
            if ax == "x":
                fr, to = [8 - L, y0 + j, 8 - wt / 2], [8 + L, y1 - j, 8 + wt / 2]
            else:
                fr, to = [8 - wt / 2, y0 + j, 8 - L], [8 + wt / 2, y1 - j, 8 + L]
            fcs = {"up": (face, [0, 0, 16, 16]), "down": (face, [0, 0, 16, 16])}
            for f in ("north", "south", "east", "west"):
                fcs[f] = (face, [0, 6, 16, 10])
            els.append(box(fr, to, fcs, R(a) if a else None))
        hub = box([5.2, 5.6, 5.2], [10.8, 10.4, 10.8], {f: (T("cog_hub"), FULL) for f in ("north", "south", "east", "west")} | {"up": (T("cog_hub_end"), FULL), "down": (T("cog_hub_end"), FULL)})
        return els, hub

    es, hub = cog(True)
    tx = {"particle": T("cog_steel")}
    model("block/cogwheel_shaftless", emodel(tx, es + [hub]))
    model("block/cogwheel_shaft", emodel(tx, shaft_elems()))
    model("block/cogwheel", emodel(tx, es + [hub] + shaft_elems()))
    el, hubl = cog(False)
    hubl["from"] = [4.8, 5.4, 4.8]; hubl["to"] = [11.2, 10.6, 11.2]
    model("block/large_cogwheel_shaftless", emodel({"particle": T("cog_bronze")}, el + [hubl]))
    model("block/large_cogwheel", emodel({"particle": T("cog_bronze")}, el + [hubl] + shaft_elems()))

    # ------------------------------------------------------------------ water wheels
    wtex("wheel_planks", planks("wheelp", WOOD, False, True))
    wtex("wheel_planks_h", planks("wheelh", WOOD, False, False))
    c = Cv(STEEL); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 3.0, True)
    for x in (10, 32, 54): c.rivet(x, 32, 2.2, STEEL)
    c.hazard(4, 8, 60, 14, 3.5)
    wtex("wheel_iron", c.finish("wiron", 0.4, 0.1, 0.04, 0.1))
    wood = {"particle": T("wheel_planks")}
    PL, PH, IR = T("wheel_planks"), T("wheel_planks_h"), T("wheel_iron")

    def spoke(angle, wd=2.6, th=2.4):
        """one plank spoke through the axle, built from tiles of at most 16 so the grain is never stretched"""
        els = []
        y0, y1 = 8 - th / 2, 8 + th / 2
        for (a0, a1) in ((-13, 3), (3, 19), (19, 29)):
            ln = a1 - a0
            f = {"north": (PH, [0, 3, ln, 3 + th]), "south": (PH, [0, 3, ln, 3 + th]), "up": (PH, [0, 0, ln, wd]), "down": (PH, [0, 0, ln, wd]),
                 "east": (PH, [0, 0, wd, th]), "west": (PH, [0, 0, wd, th])}
            els.append(box([a0, y0, 8 - wd / 2], [a1, y1, 8 + wd / 2], f, R(angle) if angle else None))
        for ax in (-8.0, 14.5):   # bolted iron clamps
            cf = {k: (IR, [0, 0, 16, 16]) for k in FACES}
            els.append(box([8 + ax - 1.0, y0 - 0.35, 8 - wd / 2 - 0.35], [8 + ax + 1.0, y1 + 0.35, 8 + wd / 2 + 0.35], cf, R(angle) if angle else None))
        return els

    def wheel_elems():
        els = []
        for a in (0, 45, 90, 135):
            els += spoke(a if a != 135 else -45)
        # eight paddles on the rim: bolted planks with two iron straps
        for k in range(8):
            ang = k * 45
            cx, cz = 8 + 21.5 * math.cos(math.radians(ang)), 8 + 21.5 * math.sin(math.radians(ang))
            rot = None
            if ang % 180 == 45:
                rot = R(-45, (cx, 8, cz))
            elif ang % 180 == 135:
                rot = R(45, (cx, 8, cz))
            if ang % 180 == 90:
                hx, hz = 5.2, 1.3
            else:
                hx, hz = 1.3, 5.2
            pf = {"north": (PL, [2.8, 5, 13.2, 11]), "south": (PL, [2.8, 5, 13.2, 11]), "east": (PL, [0, 5, 2.6, 11]), "west": (PL, [0, 5, 2.6, 11]),
                  "up": (PL, [0, 0, 2.6, 10.4]), "down": (PL, [0, 0, 2.6, 10.4])}
            if hx > hz:
                pf = {"north": (PL, [0, 5, 2.6, 11]), "south": (PL, [0, 5, 2.6, 11]), "east": (PL, [2.8, 5, 13.2, 11]), "west": (PL, [2.8, 5, 13.2, 11]),
                      "up": (PL, [0, 0, 10.4, 2.6]), "down": (PL, [0, 0, 10.4, 2.6])}
            if all(-16 <= v <= 32 for v in (cx - hx, cx + hx, cz - hz, cz + hz)):
                els.append(box([cx - hx, 5, cz - hz], [cx + hx, 11, cz + hz], pf, rot))
                for (ya, yb) in ((5.4, 6.6), (9.4, 10.6)):
                    sf = {kk: (IR, [0, 0, 16, 16]) for kk in FACES}
                    els.append(box([cx - hx - 0.35, ya, cz - hz - 0.35], [cx + hx + 0.35, yb, cz + hz + 0.35], sf, rot))
        els.append(box([5, 4.5, 5], [11, 11.5, 11], {"north": IR, "south": IR, "east": IR, "west": IR, "up": (T("cog_hub_end"), FULL), "down": (T("cog_hub_end"), FULL)}))
        return els

    FACES = ("north", "south", "east", "west", "up", "down")
    wheel = wheel_elems() + shaft_elems()
    blk = shaft_elems()
    model("block/water_wheel/wheel", emodel(wood, wheel))
    model("block/water_wheel/block", emodel(wood, blk))
    model("block/water_wheel/item", emodel(wood, wheel))
    model("block/large_water_wheel/block", emodel(wood, blk))
    model("block/large_water_wheel/block_extension", emodel(wood, blk))
    model("block/large_water_wheel/item", emodel(wood, wheel))

    # ------------------------------------------------------------------ press
    def press_side(name):
        c = Cv(sh(STEEL, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True)
        c.bevel(6, 6, 58, 40, sh(STEEL, 1.0), 1.4, False); c.vents(12, 12, 52, 30, 3)
        c.rivets4(0, 0, 64, 42, 3.2, 1.3, STEEL)
        c.hazard(5, 46, 59, 58, 5.0)
        return c.finish(name, 0.5, 0.12)

    wtex("press_side", press_side("pside"))
    c = Cv(sh(STEEL, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True)
    c.ellipse(10, 10, 54, 54, sh(STEEL_D, 0.8)); c.ellipse(13, 13, 51, 51, STEEL); c.ellipse(19, 19, 45, 45, sh(STEEL_D, 0.9)); c.ellipse(23, 23, 41, 41, BRASS)
    for a in range(0, 360, 60): c.rivet(32 + 20 * math.cos(math.radians(a)), 32 + 20 * math.sin(math.radians(a)), 1.5, STEEL)
    c.rivets4(0, 0, 64, 64, 3.2, 1.3, STEEL)
    wtex("press_top", c.finish("ptop", 0.5, 0.1))
    c = Cv(sh(STEEL_D, 0.9)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL_D, 2.4, True)
    for y in (14, 26, 38, 50): c.rect(6, y, 58, y + 4, sh(STEEL_D, 0.5)); c.rect(6, y + 4, 58, y + 5.2, sh(STEEL, 1.1))
    c.rivets4(0, 0, 64, 64, 3.4, 1.4, STEEL)
    wtex("press_head_side", c.finish("phs", 0.5, 0.15))
    c = Cv(sh(STEEL_D, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL_D, 2.6, True)
    for i in range(4):
        for j in range(4): c.bevel(8 + i * 12, 8 + j * 12, 18 + i * 12, 18 + j * 12, sh(STEEL, 0.85), 1.2, True)
    wtex("press_head_face", c.finish("phf", 0.6, 0.1))
    c = Cv(None); c.cyl_v(0, 0, 64, 64, STEEL_L, 1.8)
    wtex("press_rod", c.finish("prod", 0.2, 0, 0.02, 0))
    wtex("press_post", metal_post("ppost"))
    PS, PT, PH, PF, PRD, PP = (T(x) for x in ("press_side", "press_top", "press_head_side", "press_head_face", "press_rod", "press_post"))
    housing = box([0, 9, 0], [16, 16, 16], {"north": PS, "south": PS, "east": PS, "west": PS, "up": PT, "down": PH})
    posts = [box(p, [p[0] + 3, 9, p[2] + 3], PP) for p in ([0, 0, 0], [13, 0, 0], [0, 0, 13], [13, 0, 13])]
    head = [box([3, 3, 3], [13, 9, 13], {"north": PH, "south": PH, "east": PH, "west": PH, "up": PH, "down": PF}), box([6.5, 9, 6.5], [9.5, 16, 9.5], {"north": PRD, "south": PRD, "east": PRD, "west": PRD, "up": PRD, "down": PRD})]
    pm = {"particle": PS}
    model("block/mechanical_press/block", emodel(pm, [housing] + posts))
    model("block/mechanical_press/head", emodel(pm, [box([3, 4, 3], [13, 10, 13], {"north": PH, "south": PH, "east": PH, "west": PH, "up": PH, "down": PF}), box([6.5, 10, 6.5], [9.5, 16, 9.5], PRD)]))
    model("block/mechanical_press/item", emodel(pm, [housing] + posts + [head[0]]))
    bstate("mechanical_press", rotv("mechanical_press", "create:block/mechanical_press/block"))

    # ------------------------------------------------------------------ basin
    c = Cv(sh(STEEL, 0.85)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True)
    c.rect(2, 2, 62, 10, sh(STEEL, 1.1)); c.line(2, 10.5, 62, 10.5, STEEL_D, 1.2)
    c.rivets4(0, 0, 64, 64, 3.2, 1.4, STEEL); c.rivet(32, 6, 1.4, STEEL)
    c.vents(14, 24, 50, 46, 3)
    wtex("basin_side", c.finish("bside", 0.55, 0.16))
    c = Cv(sh(STEEL, 1.0)); c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 1.12), 3.0, True)
    wtex("basin_rim", c.finish("brim", 0.35, 0.05))
    c = Cv(STEEL_D)
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL_D, 0.7), 4.0, False)
    c.ellipse(26, 26, 38, 38, sh(STEEL_D, 0.35)); c.ellipse(28, 28, 36, 36, (12, 12, 14))
    wtex("basin_inner", c.finish("binner", 0.7, 0.2, 0.04, 0.45))
    BS, BR, BI = (T(x) for x in ("basin_side", "basin_rim", "basin_inner"))
    walls = [
        box([0, 2, 0], [16, 12, 2], {"north": BS, "south": BI, "east": BS, "west": BS, "up": BR, "down": BS}),
        box([0, 2, 14], [16, 12, 16], {"north": BI, "south": BS, "east": BS, "west": BS, "up": BR, "down": BS}),
        box([0, 2, 2], [2, 12, 14], {"north": BS, "south": BS, "east": BI, "west": BS, "up": BR, "down": BS}),
        box([14, 2, 2], [16, 12, 14], {"north": BS, "south": BS, "east": BS, "west": BI, "up": BR, "down": BS}),
        box([2, 0, 2], [14, 3, 14], {"north": BS, "south": BS, "east": BS, "west": BS, "up": BI, "down": BS}),
        box([0, 0, 0], [3, 2, 3], BS), box([13, 0, 0], [16, 2, 3], BS), box([0, 0, 13], [3, 2, 16], BS), box([13, 0, 13], [16, 2, 16], BS),
    ]
    model("block/basin/block", emodel({"particle": BS}, walls))
    bstate("basin", {"variants": {"": {"model": "create:block/basin/block"}}})

    # ------------------------------------------------------------------ belt
    wtex("belt", belt_top("belt")); wtex("belt_scroll", belt_top("belts", 8))
    c = Cv(sh(STEEL, 0.85)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True); c.rivets4(0, 0, 64, 64, 3.0, 1.2, STEEL)
    wtex("belt_edge", c.finish("bedge", 0.5, 0.12))
    wtex("belt_end", drum_tex("bend"))
    BT, BE, BD = T("belt"), T("belt_edge"), T("belt_end")
    bt = {"particle": T("belt_edge")}

    def bed(z0=0, z1=16, y1=3):
        return box([2, 0, z0], [14, y1, z1], {"up": (BT, [2, z0, 14, z1]), "down": BE, "north": BE, "south": BE, "east": BE, "west": BE})

    model("block/belt/particle", emodel(bt, [box([5, 0, 5], [11, 1, 11], BE)]))
    drum_s = box([2.5, 0.5, 0], [13.5, 4, 3], {"north": BD, "south": BD, "east": BD, "west": BD, "up": BD, "down": BD})
    drum_e = box([2.5, 0.5, 13], [13.5, 4, 16], {"north": BD, "south": BD, "east": BD, "west": BD, "up": BD, "down": BD})
    for nm, els in (("middle", [bed()]), ("start", [bed(), drum_s]), ("end", [bed(), drum_e]),
                    ("middle_bottom", [bed()]), ("start_bottom", [bed(), drum_s]), ("end_bottom", [bed(), drum_e])):
        model(f"block/belt/{nm}", emodel(bt, els))
    for nm in ("diagonal_middle", "diagonal_start", "diagonal_end"):
        rail = lambda x0, x1: box([x0, -2, -6], [x1, 2.2, 22], {"up": (BE, [0, 0, 2, 16]), "down": (BE, [0, 0, 2, 16]), "north": (BE, [0, 0, 2, 4]), "south": (BE, [0, 0, 2, 4]), "east": (BE, [0, 0, 16, 4]), "west": (BE, [0, 0, 16, 4])}, R(45, (8, 0, 8), "x"))
        model(f"block/belt/{nm}", emodel(bt, [box([2, -2, -6], [14, 1, 22], {"up": (BT, [2, 0, 14, 16]), "down": BE, "north": BE, "south": BE, "east": BE, "west": BE}, R(45, (8, 0, 8), "x")), rail(1, 2.8), rail(13.2, 15)]))
    model("block/belt_pulley", emodel(bt, [box([2, 0, 0], [14, 4, 4], BD), box([4, 4, 1], [12, 10, 3], BE)]))
    for cc, col in (("andesite", ANDESITE), ("brass", BRASS)):
        wtex(f"{cc}_belt_cover", casing_tex(f"{cc}cover", col))
        for ax in ("x", "z"):
            f_, t_ = ([2, 3, 0], [14, 5, 16]) if ax == "z" else ([0, 3, 2], [16, 5, 14])
            model(f"block/belt_cover/{cc}_belt_cover_{ax}", emodel({"particle": T(f"{cc}_belt_cover")}, [box(f_, t_, T(f"{cc}_belt_cover"))]))
    bstate("belt", {"variants": {"": {"model": "create:block/belt/particle"}}})

    # ------------------------------------------------------------------ creative motor (points up)
    def motor_side(name):
        c = Cv(sh(STEEL, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True)
        c.bevel(8, 8, 56, 56, sh(STEEL, 0.95), 1.3, False)
        c.rect(26, 11, 38, 53, (60, 28, 12)); c.rect(28, 13, 36, 51, GLOW); c.rect(30, 13, 34, 51, (255, 232, 150))
        c.outline_rect(26, 11, 38, 53, OUTL, 0.8, 255)
        c.rect(24, 9, 40, 11, sh(STEEL, 1.3)); c.rect(24, 53, 40, 55, sh(STEEL, 0.6))
        c.rivets4(0, 0, 64, 64, 3.2, 1.3, STEEL)
        c.hazard(8, 56, 56, 61, 3.5)
        for y in (20, 44):
            c.rect(10, y, 22, y + 1.4, sh(STEEL_D, 0.7)); c.rect(42, y, 54, y + 1.4, sh(STEEL_D, 0.7))
        return c.finish(name, 0.5, 0.0)

    wtex("motor_side", motor_side("mside"))
    c = Cv(sh(STEEL, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True)
    c.ellipse(14, 14, 50, 50, sh(STEEL_D, 0.8)); c.ellipse(17, 17, 47, 47, BRASS); c.ellipse(22, 22, 42, 42, sh(STEEL_D, 0.7)); c.ellipse(26, 26, 38, 38, STEEL)
    c.rivets4(0, 0, 64, 64, 3.2, 1.3, STEEL)
    wtex("motor_top", c.finish("mtop", 0.4, 0.0))
    wtex("motor_bottom", machine_top("mbot"))
    MS, MT, MB = T("motor_side"), T("motor_top"), T("motor_bottom")
    model("block/creative_motor", emodel({"particle": MS}, [box([1, 0, 1], [15, 13, 15], {"north": MS, "south": MS, "east": MS, "west": MS, "up": MT, "down": MB}),
                                                           box([5.5, 13, 5.5], [10.5, 14, 10.5], T("cog_hub")), box([6, 14, 6], [10, 16, 10], {"north": (T("shaft"), FULL), "south": (T("shaft"), FULL), "east": (T("shaft"), FULL), "west": (T("shaft"), FULL), "up": (T("shaft_end"), FULL)})]))
    bstate("creative_motor", rotv("creative_motor", "create:block/creative_motor"))

    # ------------------------------------------------------------------ mixer
    c = Cv(sh(STEEL, 0.78)); c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 0.82), 2.2, True)
    c.bevel(5, 5, 59, 38, sh(STEEL, 0.95), 1.4, True)
    for x in range(10, 56, 8):
        c.rect(x, 9, x + 3.5, 34, sh(STEEL_D, 0.55)); c.rect(x + 3.5, 9, x + 4.6, 34, sh(STEEL, 1.15))
    c.rivets4(5, 5, 59, 38, 3.0, 1.2, STEEL)
    c.hazard(5, 42, 59, 50, 4.0)
    c.gauge(14, 57, 3.6); c.lamp(32, 57, 1.8, HAZ_Y); c.lamp(42, 57, 1.8, (110, 220, 130)); c.rivet(54, 57, 1.4, STEEL)
    wtex("mixer_side", c.finish("mxs", 0.6, 0.18))
    wtex("mixer_top", casing_tex("mxt", BRASS))
    wtex("mixer_pole", shaft_side("mxp"))
    MX, MXT = T("mixer_side"), T("mixer_top")
    model("block/mechanical_mixer/block", emodel({"particle": MX}, [box([0, 4, 0], [16, 16, 16], {"north": MX, "south": MX, "east": MX, "west": MX, "up": MXT, "down": T("basin_rim")}),
                                                                      box([5, 0, 5], [11, 4, 11], T("cog_hub"))]))
    model("block/mechanical_mixer", emodel({"particle": MX}, [box([0, 4, 0], [16, 16, 16], {"north": MX, "south": MX, "east": MX, "west": MX, "up": MXT, "down": T("basin_rim")}), box([5, 0, 5], [11, 4, 11], T("cog_hub"))]))
    bstate("mechanical_mixer", {"variants": {"": {"model": "create:block/mechanical_mixer/block"}}})
    model("block/mechanical_mixer/pole", emodel({"particle": T("shaft")}, [box([6, 0, 6], [10, 16, 10], {f: (T("shaft"), FULL) for f in FACES})]))
    blade = {f: (T("cog_steel"), FULL) for f in FACES}
    model("block/mechanical_mixer/head", emodel({"particle": T("shaft")}, [box([6, 3, 6], [10, 9, 10], T("cog_hub")), box([2, 0, 7], [14, 5, 9], blade), box([7, 0, 2], [9, 5, 14], blade),
                                                                           box([6.5, 8, 6.5], [9.5, 16, 9.5], {f: (T("shaft"), FULL) for f in FACES})]))

    # ------------------------------------------------------------------ depot
    c = Cv(sh(STEEL, 0.85)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True); c.hazard(4, 44, 60, 56, 5); c.rivets4(0, 0, 64, 42, 3.2, 1.3, STEEL)
    wtex("depot_side", c.finish("dside", 0.55, 0.14))
    c = Cv(sh(STEEL, 1.05)); c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 1.15), 3.4, True)
    c.bevel(8, 8, 56, 56, sh(STEEL_D, 0.85), 2.0, False)
    for i in range(0, 48, 6): c.line(10 + i, 10, 10 + i, 54, sh(STEEL_D, 0.6), 0.8)
    wtex("depot_top", c.finish("dtop", 0.45, 0.1))
    DS, DT = T("depot_side"), T("depot_top")
    model("block/depot", emodel({"particle": DS}, [box([0, 0, 0], [16, 11, 16], {"north": DS, "south": DS, "east": DS, "west": DS, "up": DT, "down": DS})]))
    bstate("depot", {"variants": {"": {"model": "create:block/depot"}}})

    # ------------------------------------------------------------------ chute (down facing tube) and funnels, tunnels
    c = Cv(sh(STEEL, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.2, True)
    c.rect(30, 0, 34, 64, sh(STEEL_D, 0.7))                 # welded seam
    for y in (14, 28, 42, 56): c.rivet(8, y, 1.5, STEEL); c.rivet(56, y, 1.5, STEEL)
    c.hazard(0, 20, 64, 28, 4.0); c.hazard(0, 44, 64, 52, 4.0)
    wtex("chute_side", c.finish("chs", 0.7, 0.2))
    c = Cv(sh(STEEL, 0.9)); c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 1.1), 2.6, True); c.bevel(10, 10, 54, 54, (14, 14, 16), 3.0, False)
    for k in range(8):
        c.rivet(32 + 27 * math.cos(math.radians(k * 45 + 22.5)), 32 + 27 * math.sin(math.radians(k * 45 + 22.5)), 1.6, STEEL)
    wtex("chute_top", c.finish("cht", 0.4, 0.1))
    CS, CT = T("chute_side"), T("chute_top")
    model("block/chute", emodel({"particle": CS}, [box([3, 0, 3], [13, 16, 13], {"north": CS, "south": CS, "east": CS, "west": CS, "up": CT, "down": CT}),
                                                       box([2, 14, 2], [14, 16, 14], {"north": T("press_post"), "south": T("press_post"), "east": T("press_post"), "west": T("press_post"), "up": CT, "down": T("press_post")}),
                                                       box([2, 0, 2], [14, 2, 14], {"north": T("press_post"), "south": T("press_post"), "east": T("press_post"), "west": T("press_post"), "down": CT, "up": T("press_post")}),
                                                       box([2.4, 6.5, 2.4], [13.6, 8.5, 13.6], {"north": T("press_post"), "south": T("press_post"), "east": T("press_post"), "west": T("press_post"), "up": T("press_post"), "down": T("press_post")})]))
    for sh_ in ("intersection", "window", "normal", "encased"):
        pass
    bstate("chute", {"variants": {"": {"model": "create:block/chute"}}})

    for kind, col in (("andesite", ANDESITE), ("brass", BRASS)):
        wtex(f"{kind}_funnel_side", casing_tex(f"{kind}fs", col))
        wtex(f"{kind}_funnel_mouth", machine_top(f"{kind}fm", col))
        FS, FM = T(f"{kind}_funnel_side"), T(f"{kind}_funnel_mouth")
        fm = emodel({"particle": FS}, [box([3, 0, 3], [13, 12, 13], {"north": FS, "south": FS, "east": FS, "west": FS, "up": FS, "down": FM}),
                                       box([1, 11, 1], [15, 16, 15], {"north": FS, "south": FS, "east": FS, "west": FS, "up": CT, "down": FS})])
        model(f"block/{kind}_funnel", fm)
        model(f"block/{kind}_belt_funnel", fm)
        bstate(f"{kind}_funnel", rotv(f"{kind}_funnel", f"create:block/{kind}_funnel"))
        bstate(f"{kind}_belt_funnel", {"variants": {f"facing={f},powered={p},shape={s_},waterlogged={w}": {"model": f"create:block/{kind}_belt_funnel", **({"y": YR[f]} if YR[f] else {})}
                                                    for f in ("north", "south", "east", "west") for p in ("true", "false") for s_ in ("retracted", "extended", "pushing", "pulling") for w in ("true", "false")}})
        wtex(f"{kind}_tunnel_side", casing_tex(f"{kind}ts", col))
        TS = T(f"{kind}_tunnel_side")
        tm = emodel({"particle": TS}, [box([1, 9, 0], [15, 16, 16], {f: TS for f in FACES}), box([1, 0, 0], [3, 9, 16], TS), box([13, 0, 0], [15, 9, 16], TS)])
        model(f"block/{kind}_tunnel", tm)
        bstate(f"{kind}_tunnel", {"variants": {f"axis={a},shape={s_}": {"model": f"create:block/{kind}_tunnel", **({"y": 90} if a == "x" else {})} for a in ("x", "z") for s_ in ("straight", "window", "closed", "t_left", "t_right", "cross")}})

    # ------------------------------------------------------------------ casings and encased shafts
    for kind, col in (("andesite", ANDESITE), ("brass", BRASS), ("copper", COPPER)):
        wtex(f"{kind}_casing", casing_tex(f"{kind}c", col))
        model(f"block/{kind}_casing", {"parent": "minecraft:block/cube_all", "textures": {"all": T(f"{kind}_casing")}})
        c = Cv(STEEL_D); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL_D, 2.0, True)
        c.bevel(5, 5, 59, 59, col, 1.6, True)
        c.ellipse(16, 16, 48, 48, sh(STEEL_D, 0.6)); c.ellipse(19, 19, 45, 45, BRASS); c.ellipse(23, 23, 41, 41, STEEL); c.bevel(27, 27, 37, 37, sh(STEEL, 0.9), 1.0, False)
        c.rivets4(5, 5, 59, 59, 3.4, 1.0, col)
        wtex(f"{kind}_casing_end", c.finish(f"{kind}ce", 0.4, 0))
        for what in ("encased_shaft", "encased_cogwheel", "encased_large_cogwheel"):
            bid = f"{kind}_{what}"
            if bid in g.BLOCKS:
                model(f"block/{bid}", {"parent": "minecraft:block/cube_column", "textures": {"side": T(f"{kind}_casing"), "end": T(f"{kind}_casing_end")}})
                bstate(bid, rotv(bid, f"create:block/{bid}"))

    run_shapes(g)

    # gearboxes and friends share the machine panels with a brass hub on the axle ends
    c = Cv(sh(STEEL, 0.8)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True); c.bevel(6, 6, 58, 58, sh(STEEL, 0.95), 1.3, False)
    c.ellipse(14, 14, 50, 50, sh(STEEL_D, 0.8)); c.ellipse(17, 17, 47, 47, STEEL)
    for a in range(0, 360, 45):
        c.rect(32 + 17 * math.cos(math.radians(a)) - 2.2, 32 + 17 * math.sin(math.radians(a)) - 2.2, 32 + 17 * math.cos(math.radians(a)) + 2.2, 32 + 17 * math.sin(math.radians(a)) + 2.2, STEEL_D)
    c.ellipse(24, 24, 40, 40, BRASS); c.ellipse(28, 28, 36, 36, STEEL_D)
    c.rivets4(0, 0, 64, 64, 3.2, 1.3, STEEL)
    wtex("gearbox_end", c.finish("gbe", 0.5, 0.1))
    for bid in ("gearbox", "clutch", "gearshift", "sequenced_gearshift", "rotation_speed_controller", "chain_drive", "chain_gearshift"):
        if bid in g.BLOCKS and "axis" in g.BLOCKS[bid]["props"]:
            wtex(bid, machine_face(bid, STEEL, HAZ_Y, 0 if bid != "gearbox" else 4))
            model(f"block/{bid}", {"parent": "minecraft:block/cube_column", "textures": {"side": T(bid), "end": T("gearbox_end")}})
            bstate(bid, rotv(bid, f"create:block/{bid}"))


YR = {"north": 0, "east": 90, "south": 180, "west": 270}
FACES = ("north", "south", "east", "west", "up", "down")


def plane_disc(plane, face_tex, thick, s_, L, wt, bars, body_angles, center_off=0.0):
    """toothed disc lying in plane 'xz' (axle along y), 'xy' (axle along z) or 'zy' (axle along x)"""
    els = []
    h = thick / 2

    def mk(fr_ab, to_ab, rot_ang, k):
        # fr_ab/to_ab are (a0, b0) / (a1, b1) in the disc plane, thickness along the axle
        j = 0.003 * k
        if plane == "xz":
            fr, to, ax = [fr_ab[0], 8 - h + j, fr_ab[1]], [to_ab[0], 8 + h - j, to_ab[1]], "y"
        elif plane == "xy":
            fr, to, ax = [fr_ab[0], fr_ab[1], 8 - h + j], [to_ab[0], to_ab[1], 8 + h - j], "z"
        else:
            fr, to, ax = [8 - h + j, fr_ab[1], fr_ab[0]], [8 + h - j, to_ab[1], to_ab[0]], "x"
        faces = {f: (face_tex, FULL) for f in FACES}
        return box(fr, to, faces, R(rot_ang, (8, 8, 8), ax) if rot_ang else None)

    k = 0
    for a in body_angles:
        k += 1
        els.append(mk((8 - s_, 8 - s_), (8 + s_, 8 + s_), a, k))
    for (axis, a) in bars:
        k += 1
        if axis == "x":
            els.append(mk((8 - L, 8 - wt / 2), (8 + L, 8 + wt / 2), a, k))
        else:
            els.append(mk((8 - wt / 2, 8 - L), (8 + wt / 2, 8 + L), a, k))
    return els


def metal_post(name):
    c = Cv(sh(STEEL, 0.8))
    c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL_D, 1.1), 3.0, True)
    c.rect(16, 0, 18, 64, sh(STEEL, 1.2), 160); c.rect(46, 0, 48, 64, sh(STEEL, 0.6), 160)
    c.rivets4(0, 0, 64, 64, 5, 1.6, STEEL)
    return c.finish(name, 0.55, 0.15)


# ====================================================================== what ships
KEEP_MACHINES = {
    "shaft", "powered_shaft", "cogwheel", "large_cogwheel", "gearbox", "clutch", "gearshift", "creative_motor", "hand_crank", "water_wheel", "large_water_wheel",
    "water_wheel_structure", "encased_fan", "millstone", "crushing_wheel", "crushing_wheel_controller", "mechanical_press", "mechanical_mixer", "basin", "belt", "depot", "chute",
    "andesite_funnel", "brass_funnel", "andesite_belt_funnel", "brass_belt_funnel", "andesite_tunnel", "brass_tunnel", "andesite_casing", "brass_casing", "copper_casing",
    "andesite_encased_shaft", "brass_encased_shaft", "andesite_encased_cogwheel", "brass_encased_cogwheel", "andesite_encased_large_cogwheel", "brass_encased_large_cogwheel",
    "speedometer", "stressometer", "mechanical_drill", "mechanical_saw", "fluid_pipe", "mechanical_pump", "fluid_tank", "spout", "item_drain",
    "zinc_block", "raw_zinc_block", "zinc_ore", "deepslate_zinc_ore", "brass_block", "andesite_alloy_block", "industrial_iron_block", "weathered_iron_block",
    "industrial_iron_window", "industrial_iron_window_pane", "weathered_iron_window", "weathered_iron_window_pane", "ornate_iron_window", "ornate_iron_window_pane",
    "tiled_glass", "tiled_glass_pane", "framed_glass", "framed_glass_pane", "horizontal_framed_glass", "horizontal_framed_glass_pane", "vertical_framed_glass", "vertical_framed_glass_pane",
}
KEEP_ITEMS = {
    "goggles", "andesite_alloy", "wrench", "belt_connector", "copper_sheet", "brass_sheet", "iron_sheet", "golden_sheet", "brass_ingot", "wheat_flour", "dough", "zinc_nugget",
    "brass_nugget", "zinc_ingot", "raw_zinc", "sand_paper", "red_sand_paper", "whisk", "honey_bucket", "chocolate_bucket", "vertical_gearbox", "electron_tube", "propeller", "rose_quartz", "polished_rose_quartz",
} | {f"crushed_raw_{m}" for m in ("iron", "gold", "copper", "zinc", "osmium", "platinum", "silver", "tin", "lead", "quicksilver", "aluminum", "uranium", "nickel")}


def shipped_block(g, id):
    import re
    if id in KEEP_MACHINES:
        return True
    if g.stone_info(g.base_of(id)):
        return True
    if re.fullmatch(r"(waxed_)?(exposed_|weathered_|oxidized_)?copper_(shingle|tile)(s|_slab|_stairs)", id):
        return True
    if re.fullmatch(r"(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|crimson|warped|cherry|bamboo)_window(_pane)?", id):
        return True
    return False


def shipped_item(g, id):
    if id in g.BLOCKS:
        return shipped_block(g, id)
    if id in KEEP_ITEMS:
        return True
    return g.BLOCKS.get(id.replace("_item", "")) is not None and shipped_block(g, id.replace("_item", ""))


def run_shapes(g):
    T, wtex, model, bstate, emodel, rotv = g.T, g.wtex, g.model, g.bstate, g.emodel, g.rot_variants
    B = g.BLOCKS

    # ---------- hand crank (points up): base plate and stem, the arm is a partial
    wtex("crank_metal", plate_tex("crankm", STEEL, True))
    CM = T("crank_metal")
    model("block/hand_crank/block", emodel({"particle": CM}, [box([3.5, 0, 3.5], [12.5, 3, 12.5], CM), box([6, 3, 6], [10, 7, 10], {f: (T("shaft"), FULL) for f in FACES})]))
    model("block/hand_crank/handle", emodel({"particle": CM}, [box([7, 5, 7], [9, 7, 16], T("cog_hub")), box([7, 3, 14], [9, 9, 16], CM), box([6.4, 4.4, 5.4], [9.6, 7.6, 8.6], T("cog_hub"))]))
    if "hand_crank" in B:
        model("block/hand_crank", emodel({"particle": CM}, [box([3.5, 0, 3.5], [12.5, 3, 12.5], CM), box([6, 3, 6], [10, 7, 10], {f: (T("shaft"), FULL) for f in FACES}), box([7, 5, 7], [9, 7, 16], T("cog_hub"))]))
        bstate("hand_crank", rotv("hand_crank", "create:block/hand_crank"))

    # ---------- millstone: stone bowl on a steel base
    c = Cv((150, 148, 140))
    c.bevel(0.5, 0.5, 63.5, 63.5, (150, 148, 140), 3.0, True)
    for k in range(10):
        c.line(32, 32, 32 + 34 * math.cos(math.radians(k * 36 + 8)), 32 + 34 * math.sin(math.radians(k * 36 + 8)), (96, 94, 88), 1.2)
    c.ellipse(26, 26, 38, 38, sh(STEEL_D, 0.7)); c.ellipse(29, 29, 35, 35, (14, 14, 16))
    wtex("millstone_top", c.finish("mill", 0.5, 0.0, 0.05, 0.2))
    c = Cv((140, 138, 130)); c.bevel(0.5, 0.5, 63.5, 63.5, (140, 138, 130), 2.4, True)
    for y in (16, 32, 48): c.line(0, y, 64, y, (96, 94, 88), 1.0)
    wtex("millstone_side", c.finish("mills", 0.6, 0.0, 0.05, 0.2))
    MT, MS, MBS = T("millstone_top"), T("millstone_side"), T("press_post")
    base = box([0, 0, 0], [16, 5, 16], {"north": MBS, "south": MBS, "east": MBS, "west": MBS, "up": MBS, "down": MBS})
    ring = [box([0, 5, 0], [16, 13, 2], {"north": MS, "south": MS, "east": MS, "west": MS, "up": MS, "down": MS}), box([0, 5, 14], [16, 13, 16], MS),
            box([0, 5, 2], [2, 13, 14], MS), box([14, 5, 2], [16, 13, 14], MS)]
    model("block/millstone", emodel({"particle": MS}, [base] + ring + [box([2, 5, 2], [14, 8, 14], {"up": MT, "north": MS, "south": MS, "east": MS, "west": MS})]))
    model("block/millstone/inner", emodel({"particle": MS}, [box([2, 5, 2], [14, 10, 14], {"up": MT, "down": MT, "north": MS, "south": MS, "east": MS, "west": MS})]))
    if "millstone" in B:
        bstate("millstone", {"variants": {"": {"model": "create:block/millstone"}}})

    # ---------- crushing wheel (axle along y, rotated by the blockstate): ribbed roller with staggered studs, hazard side plates, axle collars
    wtex("crusher", cog_face_tex("crush", (86, 88, 94)))
    c = Cv(sh(STEEL, 0.9)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL, 2.4, True)
    c.hazard(6, 6, 58, 58, 6.0); c.rivets4(0, 0, 64, 64, 3.2, 1.3, STEEL)
    wtex("crusher_plate", c.finish("crushplate", 0.5, 0.1, 0.04, 0.1))
    c = Cv(sh(STEEL_D, 1.0)); c.bevel(0.5, 0.5, 63.5, 63.5, STEEL_D, 2.0, True)
    for k in range(6): c.line(0, 6 + k * 10.4, 64, 6 + k * 10.4, sh(STEEL_D, 0.6), 1.2)
    wtex("crusher_rib", c.finish("crushrib", 0.55, 0.12, 0.05, 0.0))
    CR, CP, CRB = T("crusher"), T("crusher_plate"), T("crusher_rib")
    hubt = T("cog_hub")

    def octa(y0, y1, s_, tex_side, tex_cap, k0):
        out = []
        for i, a_ in enumerate((0, 45)):
            t = {f: (tex_side, FULL) for f in ("north", "south", "east", "west")} | {"up": (tex_cap, FULL), "down": (tex_cap, FULL)}
            out.append(box([8 - s_, y0 + 0.003 * (k0 + i), 8 - s_], [8 + s_, y1 - 0.003 * (k0 + i), 8 + s_], t, R(a_) if a_ else None))
        return out

    def studs(y0, y1, angs, L, wt, k0):
        out = []
        for i, a_ in enumerate(angs):
            j = 0.003 * (k0 + i)
            tf = {f: (CR, FULL) for f in FACES}
            out.append(box([8 - L, y0 + j, 8 - wt / 2], [8 + L, y1 - j, 8 + wt / 2], tf, R(a_) if a_ else None))
            out.append(box([8 - wt / 2, y0 + j + 0.002, 8 - L], [8 + wt / 2, y1 - j - 0.002, 8 + L], tf, R(a_) if a_ else None))
        return out

    els = octa(3.4, 12.6, 5.4, CRB, CR, 1)                      # drum body
    els += octa(7.7, 8.3, 6.0, CP, CP, 3)                       # centre rib ring
    els += studs(3.6, 7.5, (0, 45), 7.95, 2.4, 5)               # lower studs, 8 around
    els += studs(8.5, 12.4, (22.5, -22.5), 7.95, 2.4, 9)        # upper studs offset by 22.5 degrees
    els += octa(1.6, 3.4, 6.6, CR, CP, 13)                      # hazard side plates
    els += octa(12.6, 14.4, 6.6, CR, CP, 15)
    capt = {f: (T("cog_hub_end"), FULL) for f in FACES}
    hubc = box([5.2, 0, 5.2], [10.8, 16, 10.8], {f: (T("shaft"), FULL) for f in ("north", "south", "east", "west")} | {"up": (T("cog_hub_end"), FULL), "down": (T("cog_hub_end"), FULL)})
    cols = [box([4.2, 0, 4.2], [11.8, 1.6, 11.8], {f: (hubt, FULL) for f in FACES} | {"up": (T("cog_hub_end"), FULL), "down": (T("cog_hub_end"), FULL)}),
            box([4.2, 14.4, 4.2], [11.8, 16, 11.8], {f: (hubt, FULL) for f in FACES} | {"up": (T("cog_hub_end"), FULL), "down": (T("cog_hub_end"), FULL)})]
    els += cols
    model("block/crushing_wheel/block", emodel({"particle": CR}, els + [hubc]))
    if "crushing_wheel" in B:
        model("block/crushing_wheel", emodel({"particle": CR}, els[:] + [hubc]))
        bstate("crushing_wheel", rotv("crushing_wheel", "create:block/crushing_wheel"))
    if "crushing_wheel_controller" in B:
        bstate("crushing_wheel_controller", {"variants": {"": {"model": "minecraft:block/air"}}})

    # ---------- saw blades (thin toothed discs) and drill head, fan propeller
    wtex("blade_steel", cog_face_tex("blade", sh(STEEL_L, 0.92)))
    BL = T("blade_steel")
    bars = [("x", 0), ("z", 0), ("x", 45), ("z", 45), ("x", 22.5), ("z", 22.5), ("x", -22.5), ("z", -22.5)]
    for plane, nm in (("xz", "horizontal"), ("xy", "vertical")):
        disc = plane_disc(plane, BL, 1.6, 5.0, 7.8, 1.6, bars, (0, 45))
        hub = box([6.5, 6.5, 6.5], [9.5, 9.5, 9.5], T("cog_hub"))
        for st in ("active", "inactive", "reversed"):
            model(f"block/mechanical_saw/blade_{nm}_{st}", emodel({"particle": BL}, disc + [hub]))
    wtex("drill_steel", cog_face_tex("drill", sh(STEEL, 1.05)))
    DR = T("drill_steel")
    model("block/mechanical_drill/head", emodel({"particle": DR}, [box([3, 0, 3], [13, 4, 13], DR), box([4.5, 4, 4.5], [11.5, 8, 11.5], DR), box([6, 8, 6], [10, 12, 10], DR), box([7.2, 12, 7.2], [8.8, 16, 8.8], DR),
                                                                    box([2.6, 0, 2.6], [13.4, 1.4, 13.4], T("cog_hub"))]))
    prop_bars = [("x", 0), ("z", 0), ("x", 45), ("x", -45)]
    prop = plane_disc("xy", BL, 1.4, 1.2, 7.6, 2.6, prop_bars, (0,))
    model("block/encased_fan/propeller", emodel({"particle": BL}, prop[1:] + [box([6, 6, 5.5], [10, 10, 10.5], T("cog_hub"))]))

    # ---------- fluid pipe: hub with flanged arms
    wtex("pipe", pipe_tex("pipe", COPPER))
    c = Cv(sh(STEEL_D, 1.1)); c.bevel(0.5, 0.5, 63.5, 63.5, sh(STEEL, 0.95), 3.0, True)
    for k in range(8): c.rivet(32 + 24 * math.cos(math.radians(k * 45 + 22.5)), 32 + 24 * math.sin(math.radians(k * 45 + 22.5)), 3.0, STEEL)
    wtex("pipe_flange", c.finish("flange", 0.3, 0.0, 0.03, 0.1))
    PI, PF = T("pipe"), T("pipe_flange")
    model("block/fluid_pipe/hub", emodel({"particle": PI}, [box([4.5, 4.5, 4.5], [11.5, 11.5, 11.5], PI)]))
    arms = {"north": ([5, 5, 0], [11, 11, 4.5], [4, 4, 0], [12, 12, 1.8]), "south": ([5, 5, 11.5], [11, 11, 16], [4, 4, 14.2], [12, 12, 16]),
            "west": ([0, 5, 5], [4.5, 11, 11], [0, 4, 4], [1.8, 12, 12]), "east": ([11.5, 5, 5], [16, 11, 11], [14.2, 4, 4], [16, 12, 12]),
            "down": ([5, 0, 5], [11, 4.5, 11], [4, 0, 4], [12, 1.8, 12]), "up": ([5, 11.5, 5], [11, 16, 11], [4, 14.2, 4], [12, 16, 12])}
    for d, (f, t, ff, ft) in arms.items():
        rotf = 90 if d in ("up", "down") else 0
        side = {k: (PI, None, rotf) for k in ("north", "south", "east", "west", "up", "down")}
        model(f"block/fluid_pipe/arm_{d}", emodel({"particle": PI}, [box(f, t, side), box(ff, ft, PF)]))
    if "fluid_pipe" in B:
        parts = [{"apply": {"model": "create:block/fluid_pipe/hub"}}]
        for d in arms:
            parts.append({"when": {d: "true"}, "apply": {"model": f"create:block/fluid_pipe/arm_{d}"}})
        bstate("fluid_pipe", {"multipart": parts})
        model("block/fluid_pipe", emodel({"particle": PI}, [box([4.5, 4.5, 4.5], [11.5, 11.5, 11.5], PI), box([5, 5, 0], [11, 11, 16], PI), box([4, 4, 0], [12, 12, 1.8], PF), box([4, 4, 14.2], [12, 12, 16], PF)]))
        g.idef("fluid_pipe", {"model": {"type": "minecraft:model", "model": "create:block/fluid_pipe"}})


def cog_face_tex(name, base):
    c = Cv(base)
    c.bevel(0.5, 0.5, 63.5, 63.5, base, 2.0, True, False, False)
    return c.finish(name, 0.4, 0.05, 0.05, 0.0)
