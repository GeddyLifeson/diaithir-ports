"""Marrowcrawl: a ribcage crawling on four jointed bone limbs, a long-jawed skull, raw marrow-red flesh and a hooked vertebra tail."""
import math

import mats
import motifs as M
import parts
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "marrowcrawl"


def make():
    bonem = mats.bone((208, 198, 176))
    bone_d = mats.bone((168, 156, 134))
    flesh = mats.flesh((128, 40, 44))
    flesh_d = mats.flesh((84, 26, 34))
    dark = ramp((20, 12, 14))
    RED = ramp_of(["#401010", "#902020", "#e04030", "#ffa080"])

    def skull_front(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            f.paint(f.ellipse(w / 2 + sx * w * 0.27, h * 0.42, 2.6, 2.6), dark, 0.05, -1.2)
            f.paint(f.rect(w / 2 + sx * w * 0.27 - 0.5, h * 0.42 - 0.5, w / 2 + sx * w * 0.27 + 0.6, h * 0.42 + 0.6), RED, 0.8, -0.5, emit=True)
        f.paint(f.rect(w / 2 - 1.0, h * 0.62, w / 2 + 1.0, h * 0.74), dark, 0.05, -1.0)

    def jaw_front(f):
        M.teeth(f, 1.5, 1.5, f.w - 1.5, f.h - 1.5, ramp((232, 224, 200)), count=5, h=2, dark=dark)

    m = Model(NAME)
    body = m.bone("body", [0, 9, 0])
    # spine and ribs
    body.cube([-1.6, 10, -9], [3.2, 2.4, 19], bone_d)
    for k in range(6):
        z = -7.4 + k * 3.0
        body.cube([-6.2, 5.0, z - 0.8], [12.4, 5.6, 1.6], bonem, hide=("down",))
        body.cube([-5.6, 4.4, z - 0.8], [1.2, 2.4, 1.6], bonem)
        body.cube([4.4, 4.4, z - 0.8], [1.2, 2.4, 1.6], bonem)
    body.cube([-4.6, 5.2, -8.0], [9.2, 5.2, 17.0], flesh_d, hide=("up", "down"))    # raw belly between the ribs
    body.cube([-3.0, 3.8, -6.4], [6.0, 2.0, 12.8], flesh, hide=("up",))
    # tail of vertebrae
    tail = m.bone("tail", [0, 11, 9.5], "body")
    pts = [(0, 11.0, 10.5), (0, 12.5, 13.5), (0, 15.0, 16.0), (0, 18.5, 17.0), (0, 21.0, 15.5)]
    for k in range(len(pts) - 1):
        (x0, y0, z0), (x1, y1, z1) = pts[k], pts[k + 1]
        L = math.dist((y0, z0), (y1, z1)) + 0.8
        s_ = 2.8 - k * 0.4
        ang = math.degrees(math.atan2(z1 - z0, y1 - y0))
        tail.cube([-s_ / 2, (y0 + y1) / 2 - L / 2, (z0 + z1) / 2 - s_ / 2], [s_, L, s_], bone_d if k % 2 else bonem, rot=[ang, 0, 0], pivot=[0, (y0 + y1) / 2, (z0 + z1) / 2])
    tail.cube([-0.5, 21.4, 12.6], [1.0, 1.0, 3.6], bonem, rot=[-30, 0, 0], pivot=[0, 21, 15])
    # skull on a short neck
    nk = m.bone("neck", [0, 11, -8.5], "body")
    nk.cube([-1.6, 8.6, -12.5], [3.2, 3.2, 4.4], bone_d)
    head = m.bone("head", [0, 10, -12], "neck")
    head.cube([-3.6, 7.2, -17.0], [7.2, 6.4, 6.4], bonem, deco={"north": skull_front})
    head.cube([-2.6, 7.6, -22.4], [5.2, 3.4, 5.6], bonem, hide=("down",))
    jaw = m.bone("jaw", [0, 8, -17], "head")
    jaw.cube([-2.4, 4.8, -25.0], [4.8, 2.6, 9.0], bone_d, deco={"north": jaw_front, "up": lambda f: M.teeth(f, 1, 1, f.w - 1, f.h - 1, ramp((232, 224, 200)), count=4, h=2, dark=dark)})
    head.cube([-0.8, 13.4, -15.6], [1.6, 1.6, 2.2], bonem)
    # limbs: shoulder joint up and out, forearm down to a claw foot
    legs = [("a", -1, -6.4), ("b", 1, 5.6)]
    for s, sg in (("l", 1), ("r", -1)):
        for nm, zz in (("f", -6.0), ("b", 6.0)):
            sh = (sg * 5.0, 9.5)
            kn = (sg * 12.5, 15.0)
            ft = (sg * 16.0, 0.6)
            up = m.bone(f"up{nm}_{s}", [sh[0], sh[1], zz], "body")
            parts.seg(up, sh, kn, 2.2, bonem, z=zz)
            up.cube([kn[0] - 1.6, kn[1] - 1.6, zz - 1.6], [3.2, 3.2, 3.2], bone_d)
            lo = m.bone(f"low{nm}_{s}", [kn[0], kn[1], zz], f"up{nm}_{s}")
            parts.seg(lo, kn, ft, 1.8, bone_d, z=zz)
            lo.cube([ft[0] - 2.2 + (-sg * 1.0), 0, zz - 2.4], [4.4, 1.4, 4.8], bonem)
            lo.cube([ft[0] - 0.5 + sg * -1.0, 0, zz - 4.4], [1.0, 1.0, 2.2], bonem)
    anims = {}
    idle = Anim(3.0)
    idle.bob("body", 0.3, 2).sway("jaw", 0, 6, 3).sway("head", 1, 10, 1, 0.2).sway("tail", 1, 8, 1).sway("tail", 0, 5, 2)
    anims["idle"] = idle
    walk = Anim(0.7)
    walk.bob("body", 0.6, 2).sway("body", 2, 4).sway("head", 1, 9, 1).sway("jaw", 0, 8, 2).sway("tail", 1, 14, 1)
    for nm, sg_, ph in (("f_l", 1, 0.0), ("b_r", 1, 0.0), ("f_r", 1, 0.5), ("b_l", 1, 0.5)):
        walk.sway("up" + nm, 1, 28, 1, ph)
        walk.rot("low" + nm, 2, lambda t, ph=ph, sg=(1 if nm.endswith("l") else -1): sg * -16 * max(0.0, math.sin(2 * math.pi * (t / 0.7 + ph + 0.25))))
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("head", 0, [(0, 0), (0.12, -24), (0.28, 12), (0.5, 0)])
    atk.keys("jaw", 0, [(0, 0), (0.1, 40), (0.26, -8), (0.5, 0)])
    atk.keys("body", 0, [(0, 0), (0.12, 8), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
