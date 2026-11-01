"""Siegewarden Walker: a four-legged olive-drab artillery walker with a rotating turret, long cannon, armour plates and ammo racks."""
import mats
import motifs as M
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "siegewarden"


def make():
    olive = mats.plate((92, 106, 74), panel=(6, 5), rust=0.1, grime=0.9, scratch=9, rustc=(110, 70, 36))
    olive_d = mats.plate((66, 78, 56), panel=(5, 5), rust=0.12, grime=0.9, scratch=6, rustc=(110, 70, 36))
    steel = mats.plate((96, 102, 110), panel=(4, 4), rust=0.08, scratch=6)
    dark = mats.plate((50, 54, 60), panel=(4, 4), rust=0.12, scratch=4)
    chrome = mats.chrome()
    tread = mats.tread()
    brass = mats.brass((170, 128, 56), panel=(4, 4))
    GA = M.GLOW_AMBER
    stripe_a, stripe_b = ramp((214, 170, 40)), ramp((34, 34, 38))

    def hull_deco(f):
        if f.name == "north":
            M.hazard(f, 3, 9, stripe_a, stripe_b, step=5)
            M.vent(f, 8, 20, 36, 34, steel.rp, dark.rp, slats=5, glow=GA)
            # unit number plate
            f.paint(f.rect(f.w - 22, f.h - 18, f.w - 6, f.h - 6), steel.rp, 0.55, 0.8)
            f.paint(f.rect(f.w - 20, f.h - 16, f.w - 8, f.h - 8), dark.rp, 0.2, -0.4)
        elif f.name in ("east", "west"):
            M.hazard(f, f.h - 9, f.h - 3, stripe_a, stripe_b, step=5)
            M.vent(f, 12, 6, 30, 20, steel.rp, dark.rp, slats=4, glow=GA)

    m = Model(NAME)
    hull = m.bone("hull", [0, 28, 0])
    hull.cube([-20, 24, -17], [40, 13, 34], olive, deco={"all": hull_deco})
    hull.cube([-18, 37, -15], [36, 2.4, 30], olive_d)
    hull.cube([-22, 22, -18], [44, 3, 36], dark)
    for sx in (-1, 1):                                                  # side armour skirts
        hull.cube([sx * 20.2 - (0.0 if sx > 0 else 3.0), 22, -14], [3.0, 12, 28], olive_d)
    hull.cube([-14, 26, 17], [28, 9, 4], dark, deco={"north": lambda f: None})
    for sx in (-1, 1):                                                  # engine exhausts at the rear
        hull.cube([sx * 8 - 2, 36, 14], [4, 9, 4], chrome)
        hull.cube([sx * 8 - 2.6, 45, 13.4], [5.2, 1.4, 5.2], dark)
        hull.cube([sx * 8 - 1.6, 46.2, 14.4], [3.2, 0.6, 3.2], mats.glow(GA, core=False))
    # headlights + sensor
    for sx in (-1, 1):
        hull.cube([sx * 14 - 2, 30, -18.8], [4, 4, 1.6], mats.glow(GA))
    tur = m.bone("turret", [0, 39, 2], "hull")
    tur.cube([-11, 39, -8], [22, 9, 20], olive, deco={"all": lambda f: M.vent(f, 6, f.h - 12, f.w - 6, f.h - 4, steel.rp, dark.rp, slats=3) if f.name in ("east", "west") else None})
    tur.cube([-9, 48, -6], [18, 3, 16], olive_d)
    tur.cube([-4, 51, -3], [8, 3.4, 8], steel)                         # hatch
    tur.cube([-1.4, 54.4, -0.6], [2.8, 2.4, 1.2], dark)
    tur.cube([-1.0, 57, 4], [2.0, 9, 2.0], chrome)
    for sx in (-1, 1):                                                  # ammo racks
        tur.cube([sx * 11 - (0 if sx > 0 else 3.2), 40, -5], [3.2, 7, 14], dark)
        for k in range(3):
            tur.cube([sx * 14.5 - (0 if sx > 0 else 0) - 1.0, 41 + k * 2.2, -3.5 + k * 0.0], [2.0, 1.6, 10], brass)
    bar = m.bone("barrel", [0, 44, -8], "turret")
    bar.cube([-4, 40.5, -13], [8, 8, 5], steel)
    bar.cube([-2.4, 42.1, -36], [4.8, 4.8, 24], chrome)
    bar.cube([-3.2, 41.3, -40], [6.4, 6.4, 5.6], dark)
    bar.cube([-3.8, 40.9, -26], [7.6, 7.6, 3.2], steel)
    bar.cube([-1.2, 43.4, -40.6], [2.4, 2.4, 0.8], mats.glow(M.GLOW_ORANGE if hasattr(M, "GLOW_ORANGE") else GA))
    # four legs: hip, shin, round foot pad
    for nm, px, pz in (("leg_f_l", 1, -1), ("leg_f_r", -1, -1), ("leg_b_l", 1, 1), ("leg_b_r", -1, 1)):
        cx, cz = px * 17, pz * 12
        top = m.bone(nm, [cx, 26, cz])
        top.cube([cx - 4.5, 16, cz - 4.5], [9, 10, 9], dark)
        top.cube([cx - 3.6, 12, cz - 3.6], [7.2, 6, 7.2], olive_d)
        sh = m.bone(nm.replace("leg", "shin"), [cx, 14, cz], nm)
        sh.cube([cx - 3.0, 4, cz - 3.0], [6, 11, 6], steel)
        sh.cube([cx + px * 1.6 - 0.9, 5, cz - 4.0], [1.8, 8, 1.8], chrome)
        ft = m.bone(nm.replace("leg", "foot"), [cx, 4, cz], nm.replace("leg", "shin"))
        ft.cube([cx - 6, 0, cz - 6], [12, 4, 12], tread)
        ft.cube([cx - 4.5, 3.6, cz - 4.5], [9, 1.6, 9], dark)
    anims = {}
    idle = Anim(3.0)
    idle.bob("hull", 0.3, 1).sway("turret", 1, 6, 1).sway("barrel", 0, 1.4, 1, 0.3)
    anims["idle"] = idle
    walk = Anim(1.6)
    walk.bob("hull", 0.7, 2, 0.1).sway("hull", 2, 1.2).sway("hull", 0, 0.8, 2)
    for nm, sign, ph in (("leg_f_l", 1, 0.0), ("leg_b_r", 1, 0.0), ("leg_f_r", -1, 0.0), ("leg_b_l", -1, 0.0)):
        walk.sway(nm, 0, 16 * sign)
        walk.sway(nm.replace("leg", "shin"), 0, 8, 1, 0.25 if sign > 0 else 0.75)
    walk.sway("turret", 1, 3, 1, 0.5)
    anims["walk"] = walk
    atk = Anim(0.9, loop=False)
    atk.keys("barrel", 0, [(0, 0), (0.12, 7), (0.3, -2), (0.9, 0)])
    atk.keys("barrel", 2, [(0, 0), (0.12, 0), (0.9, 0)], kind="position") if False else None
    atk.keys("hull", 0, [(0, 0), (0.12, -3), (0.4, 1), (0.9, 0)])
    atk.keys("turret", 0, [(0, 0), (0.12, 2), (0.9, 0)])
    anims["attack"] = atk
    return m, anims
