"""Eachrèidhlein: the Mexican horse (Equus conversidens), a small stocky Pleistocene horse about 1.4 m at the withers,
dun coat with a dark dorsal stripe, dark mane, tail and lower legs, and a heavy head."""
import mats
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "eachreidhlein"


def make():
    dun = mats.fur((188, 150, 100), clump=2.4, contrast=0.08, steps=4, tone=0.58)
    dun_l = mats.fur((222, 196, 150), clump=2.4, contrast=0.06, steps=4, tone=0.62)
    dark = mats.fur((52, 40, 34), clump=2.4, contrast=0.08, steps=4, tone=0.5)
    mane = mats.hair((44, 34, 30), tone=0.52, strand=2.2, contrast=0.2)
    hoof = mats.horn((46, 38, 34), ring=4.0)
    nose = mats.skin((92, 76, 70), tone=0.5)
    ink = ramp_of(["#0a0706", "#16100e", "#281c18", "#40302a"])
    shine = ramp_of(["#f4f0e8", "#ffffff"])

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            cx, cy = w / 2 + sx * w * 0.36, h * 0.26
            f.paint(f.ellipse(cx, cy, 1.9, 1.6), ink, 0.14, 0.0, nolight=True)
            f.paint(f.ellipse(cx - 0.5, cy - 0.5, 0.6, 0.5), shine, 0.9, 0, nolight=True)

    def snout(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            f.paint(f.ellipse(w / 2 + sx * w * 0.28, h * 0.55, 1.3, 1.0), ink, 0.2, 0.0, nolight=True)

    m = Model(NAME)
    body = m.bone("body", [0, 14, 0])
    body.cube([-4.6, 10.2, -8.0], [9.2, 9.0, 18.0], dun, bevel=1.8, form=0.22)
    body.cube([-3.6, 10.0, -7.0], [7.2, 1.2, 15.0], dun_l, hide=("up",))
    body.cube([-0.7, 19.0, -7.0], [1.4, 0.4, 16.0], dark, hide=("down",), bevel=0.2)
    body.cube([-4.4, 11.0, 9.0], [8.8, 7.6, 3.0], dun, bevel=1.6, form=0.2)

    neck = m.bone("neck", [0, 17.0, -7.6], "body", rot=[26, 0, 0])
    neck.cube([-2.4, 17.0, -9.6], [4.8, 10.0, 5.6], dun, bevel=1.4, form=0.2)
    neck.cube([-0.9, 17.0, -4.4], [1.8, 10.4, 2.2], mane, bevel=0.5)
    head = m.bone("head", [0, 26.0, -10.5], "neck", rot=[-6, 0, 0])
    head.cube([-2.9, 21.8, -13.5], [5.8, 6.0, 6.4], dun, deco={"north": face}, bevel=1.4, form=0.2)
    head.cube([-2.3, 20.0, -17.4], [4.6, 5.0, 5.6], dun, bevel=1.2, form=0.18)
    head.cube([-2.4, 19.6, -18.0], [4.8, 2.6, 1.0], nose, deco={"north": snout}, bevel=0.5)
    head.cube([-1.4, 27.6, -11.6], [2.8, 1.6, 2.0], mane, bevel=0.4)
    ear_l = m.bone("ear_l", [1.8, 28.0, -10.8], "head")
    ear_l.cube([1.2, 27.6, -11.4], [1.4, 3.2, 1.2], dun, bevel=0.5)
    ear_r = m.bone("ear_r", [-1.8, 28.0, -10.8], "head")
    ear_r.cube([-2.6, 27.6, -11.4], [1.4, 3.2, 1.2], dun, bevel=0.5, mirror_of=ear_l.cubes[0])

    tail = m.bone("tail", [0, 18.0, 11.6], "body")
    tail.cube([-1.0, 10.0, 11.4], [2.0, 9.0, 2.4], mane, bevel=0.5)

    for s, sg in (("l", 1), ("r", -1)):
        for tag, z, up in (("f", -6.0, -1), ("b", 7.0, 1)):
            b = m.bone("leg_%s_%s" % (tag, s), [sg * 3.0, 11.0, z], "body")
            first = m.by_name["leg_%s_l" % tag].cubes if sg < 0 else None
            x = sg * 3.0
            b.cube([x - 1.8, 6.2, z - 1.8], [3.6, 5.0, 3.6], dun, bevel=1.0, mirror_of=first[0] if first else None)
            b.cube([x - 1.3, 2.0, z - 1.3], [2.6, 4.4, 2.6], dark, bevel=0.6, mirror_of=first[1] if first else None)
            b.cube([x - 1.7, 0.0, z - 1.8], [3.4, 2.2, 3.6], hoof, bevel=0.5, mirror_of=first[2] if first else None)

    anims = {}
    idle = Anim(3.2)
    idle.bob("body", 0.15, 1).sway("tail", 0, 4, 1).sway("tail", 2, 6, 2, 0.1)
    idle.keys("head", 0, [(0, 0), (1.4, -8), (2.2, 6), (3.2, 0)]).sway("ear_l", 2, 10, 3, 0.2).sway("ear_r", 2, -10, 3, 0.4)
    anims["idle"] = idle
    walk = Anim(0.9)
    walk.sway("leg_f_l", 0, 28).sway("leg_f_r", 0, -28).sway("leg_b_l", 0, -28).sway("leg_b_r", 0, 28)
    walk.bob("body", 0.35, 2, 0.25).sway("head", 0, 4, 2, 0.1).sway("tail", 2, 8, 1, 0.3)
    anims["walk"] = walk
    atk = Anim(0.6, loop=False)
    atk.keys("neck", 0, [(0, 0), (0.15, 14), (0.28, -18), (0.6, 0)])
    atk.keys("leg_f_l", 0, [(0, 0), (0.15, -50), (0.35, 20), (0.6, 0)])
    atk.keys("leg_f_r", 0, [(0, 0), (0.2, -50), (0.4, 20), (0.6, 0)])
    atk.keys("body", 0, [(0, 0), (0.15, -6), (0.35, 3), (0.6, 0)])
    anims["attack"] = atk
    return m, anims
