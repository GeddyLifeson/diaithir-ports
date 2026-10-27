"""Dobhranfamhair: the giant beaver (Castoroides ohioensis), a bear-sized Pleistocene rodent with a long skull, orange-brown
chisel incisors, dense dark fur, small round ears, webbed hind feet and a narrow scaly tail (not the broad paddle of the living
beaver). Real proportions: about 2.2 m nose to tail, 1 m at the hip."""
import mats
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "dobhranfamhair"


def make():
    fur = mats.fur((116, 82, 52), clump=2.2, contrast=0.12, steps=4, tone=0.55)
    fur_d = mats.fur((84, 58, 38), clump=2.2, contrast=0.12, steps=4, tone=0.5)
    fur_l = mats.fur((138, 104, 70), clump=2.2, contrast=0.1, steps=4, tone=0.58)
    skin = mats.skin((70, 54, 48), tone=0.5)
    scale = mats.scales((58, 46, 40), size=2.6, tone=0.5, shine=0.3)
    tooth = mats.horn((206, 118, 40), ring=5.0)
    claw = mats.horn((60, 48, 40), ring=4.0)
    ink = ramp_of(["#0c0806", "#1a1210", "#2c201a", "#44342a"])
    shine = ramp_of(["#f4f0e8", "#ffffff"])

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            cx, cy = w / 2 + sx * w * 0.34, h * 0.30
            f.paint(f.ellipse(cx, cy, 2.4, 2.1), ink, 0.14, 0.0, nolight=True)
            f.paint(f.ellipse(cx - 0.6, cy - 0.7, 0.8, 0.7), shine, 0.9, 0, nolight=True)
        for sx in (-1, 1):
            f.paint(f.ellipse(w / 2 + sx * w * 0.14, h * 0.86, 1.2, 0.9), ink, 0.2, 0.0, nolight=True)

    m = Model(NAME)
    body = m.bone("body", [0, 8, 0])
    body.cube([-6.4, 4.2, -9.0], [12.8, 9.6, 19.0], fur, outline=0.0, bevel=1.8, form=0.22)
    body.cube([-5.0, 3.8, -8.0], [10.0, 1.6, 15.0], fur_l, hide=("up",))
    body.cube([-5.8, 12.6, -5.0], [11.6, 2.4, 12.0], fur_d, hide=("down",))
    body.cube([-5.6, 4.6, 8.0], [11.2, 8.6, 3.4], fur, bevel=1.6, form=0.2)

    head = m.bone("head", [0, 10.5, -9.0], "body")
    head.cube([-4.2, 8.0, -17.0], [8.4, 7.4, 8.6], fur_d, deco={"north": face}, bevel=1.8, form=0.22)
    head.cube([-3.0, 7.4, -20.4], [6.0, 5.2, 4.2], fur_d, hide=("south",), bevel=1.4, form=0.2)
    head.cube([-1.1, 9.6, -20.7], [2.2, 1.1, 0.5], skin)
    lip = m.bone("lip", [0, 8.0, -19.0], "head")
    lip.cube([-2.6, 6.4, -19.4], [5.2, 1.8, 3.6], fur_l, bevel=1.2)
    for sg, n in ((1, "l"), (-1, "r")):
        lip.cube([sg * 1.5 - 0.95, 5.6, -20.2], [1.9, 3.0, 1.2], tooth, bevel=0.5)
    ear_l = m.bone("ear_l", [3.2, 14.6, -11.4], "head")
    ear_l.cube([2.4, 14.0, -12.2], [1.8, 2.2, 1.4], fur_d, bevel=0.8)
    ear_r = m.bone("ear_r", [-3.2, 14.6, -11.4], "head")
    ear_r.cube([-4.2, 14.0, -12.2], [1.8, 2.2, 1.4], fur_d, bevel=0.8, mirror_of=ear_l.cubes[0])

    tail = m.bone("tail", [0, 7.5, 10.4], "body")
    tail.cube([-1.8, 6.0, 10.2], [3.6, 2.4, 6.0], scale, bevel=0.6, form=0.1)
    tail2 = m.bone("tail2", [0, 7.2, 16.0], "tail")
    tail2.cube([-2.4, 5.8, 15.8], [4.8, 1.8, 7.0], scale, bevel=0.6, form=0.1)

    for s, sg in (("l", 1), ("r", -1)):
        fl = m.bone("leg_f_" + s, [sg * 4.4, 6.0, -5.4], "body")
        fl.cube([sg * 4.4 - 2.1, 1.0, -7.4], [4.2, 6.0, 4.0], fur_d, bevel=1.0, mirror_of=None) if sg > 0 else \
            fl.cube([sg * 4.4 - 2.1, 1.0, -7.4], [4.2, 6.0, 4.0], fur_d, bevel=1.0, mirror_of=m.by_name["leg_f_l"].cubes[0])
        fl.cube([sg * 4.4 - 1.9, 0.0, -9.6], [3.8, 1.2, 4.8], skin, bevel=0.6)
        for k in (-1, 0, 1):
            fl.cube([sg * 4.4 + k * 1.0 - 0.45, 0.0, -10.8], [0.9, 0.9, 1.4], claw, bevel=0.3)
        hl = m.bone("leg_b_" + s, [sg * 4.6, 6.4, 6.0], "body")
        src = m.by_name["leg_b_l"].cubes[0] if sg < 0 else None
        hl.cube([sg * 4.6 - 2.4, 0.8, 4.0], [4.8, 6.4, 5.4], fur_d, bevel=1.0, mirror_of=src)
        hl.cube([sg * 4.6 - 2.5, 0.0, 0.8], [5.0, 1.0, 8.0], skin, bevel=0.5, mirror_of=(m.by_name["leg_b_l"].cubes[1] if sg < 0 else None))
        hl.cube([sg * 4.6 - 2.5, 1.0, 0.6], [5.0, 0.2, 2.0], skin, hide=("up",)) if False else None

    anims = {}
    idle = Anim(3.0)
    idle.bob("body", 0.2, 1).keys("head", 0, [(0, 0), (1.2, 6), (1.8, -3), (3.0, 0)])
    idle.sway("tail", 1, 5, 1).sway("ear_l", 2, 8, 2, 0.2).sway("ear_r", 2, -8, 2, 0.2)
    anims["idle"] = idle
    walk = Anim(0.9)
    walk.sway("leg_f_l", 0, 26).sway("leg_f_r", 0, -26).sway("leg_b_l", 0, -26).sway("leg_b_r", 0, 26)
    walk.sway("body", 2, 4, 1).bob("body", 0.4, 2, 0.25).sway("tail", 1, 14, 1, 0.25).sway("head", 1, 5, 1, 0.5)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("head", 0, [(0, 0), (0.12, -14), (0.22, 22), (0.5, 0)])
    atk.keys("lip", 0, [(0, 0), (0.12, -16), (0.22, 8), (0.5, 0)])
    atk.keys("body", 0, [(0, 0), (0.12, -4), (0.22, 5), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
