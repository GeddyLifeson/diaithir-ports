"""Bumblesnout: a chubby honey-and-brown striped cub with a big pink snout, round ears, little wings and a curly tail."""
import numpy as np

import mats
import motifs as M
import toon as T
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "bumblesnout"


def make():
    honey = mats.fur((240, 168, 72), clump=2.4, contrast=0.08, steps=3, tone=0.62)
    stripe = mats.fur((84, 52, 38), clump=2.4, contrast=0.08, steps=3, tone=0.5)
    cream = mats.fur((252, 226, 170), clump=2.4, contrast=0.06, steps=3, tone=0.66)
    pink = mats.cel((246, 160, 164), tone=0.64, grad=0.12)
    brown = mats.fur((110, 70, 46), clump=2.4, contrast=0.08, steps=3, tone=0.52)
    wing = mats.fin((190, 230, 255), ribs=3.0, lo=0.5, hi=1.2)

    def striped(f):
        # two dark bands across the back and sides
        for y0 in (int(f.h * 0.22), int(f.h * 0.62)):
            if f.name in ("up", "down"):
                f.paint(f.rect(0, y0 * 0 + f.h * 0.22, f.w, f.h * 0.42), stripe.rp, 0.5, 0.3) if f.name == "up" else None
            else:
                f.paint(f.rect(0, y0, f.w, y0 + int(f.h * 0.18)), stripe.rp, 0.5, 0.4)

    def belly(f):
        if f.name == "north":
            f.paint(f.ellipse(f.w / 2, f.h * 0.58, f.w * 0.34, f.h * 0.38), cream.rp, 0.66, 0.4)

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            T.eye(f, w / 2 + sx * w * 0.27, h * 0.36, 3.6, 4.2, look=(-sx * 0.2, 0.1))
            T.blush(f, w / 2 + sx * w * 0.40, h * 0.66)
        f.paint(f.rect(w / 2 - 6, h * 0.34, w / 2 - 2, h * 0.34 + 1), stripe.rp, 0.3, 0) if False else None

    def snout_front(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            f.paint(f.ellipse(w / 2 + sx * w * 0.22, h * 0.4, 1.8, 2.4), T.INK, 0.2, -0.6)
        T.smile(f, w / 2, h * 0.78, w * 0.5, curve=1.5)

    m = Model(NAME)
    body = m.bone("body", [0, 6, 0])
    T.rbox(body, [-6.5, 4.0, -6.5], [13, 10.5, 14], honey, r=2.4, deco={"sides": striped, "up": lambda f: f.paint(f.rect(0, f.h * 0.25, f.w, f.h * 0.45), stripe.rp, 0.5, 0.3), "north": belly})
    body.cube([-4.5, 4.2, -6.8], [9, 6, 0.8], cream, deco={"north": lambda f: None}, hide=("south", "up", "down"))
    # wings (tiny bee wings)
    for s, sg in (("l", 1), ("r", -1)):
        w = m.bone("wing_" + s, [sg * 3, 14.4, 3], "body", rot=[0, 0, sg * -16])
        w.cube([sg * 3 + (0 if sg > 0 else -6.4), 14.2, 1.0], [6.4, 0.6, 5.4], wing, rot=[0, sg * -18, sg * -8], pivot=[sg * 3, 14.4, 3])
    # tail: a curly tuft
    tail = m.bone("tail", [0, 9, 7.2], "body")
    T.rbox(tail, [-1.6, 8.4, 7.0], [3.2, 3.2, 3.4], cream, r=1.0)
    tail.cube([-0.8, 10.8, 9.2], [1.6, 1.6, 1.6], cream)
    # head
    head = m.bone("head", [0, 11, -5], "body")
    T.rbox(head, [-5.2, 9.6, -11.0], [10.4, 9.0, 9.6], honey, r=2.4, deco={"north": face})
    head.cube([-4.0, 14.9, -11.2], [8, 1.4, 0.5], stripe, hide=("up", "down")) if False else None
    sn = m.bone("snout", [0, 12.5, -11], "head")
    T.rbox(sn, [-3.4, 10.0, -14.6], [6.8, 5.0, 4.4], pink, r=1.2, deco={"north": snout_front})
    for s, sg in (("l", 1), ("r", -1)):
        ear = m.bone("ear_" + s, [sg * 3.6, 17.5, -6.5], "head")
        T.rbox(ear, [sg * 3.6 + (-1.8 if sg > 0 else -1.8), 17.2, -7.9], [3.6, 3.6, 2.6], brown, r=1.0)
        ear.cube([sg * 3.6 - 1.0, 17.8, -8.2], [2.0, 2.2, 0.5], pink)
    # legs
    for nm, z in (("f", -3.4), ("b", 4.0)):
        for s, sg in (("l", 1), ("r", -1)):
            lg = m.bone(f"leg_{nm}_{s}", [sg * 3.6, 5.0, z], "body" if False else None)
            T.rbox(lg, [sg * 3.6 - 1.7, 0.0, z - 1.8], [3.4, 5.6, 3.8], brown, r=0.9)
            lg.cube([sg * 3.6 - 1.4, 0.0, z - 3.0], [2.8, 1.0, 1.6], cream)
    anims = {}
    idle = Anim(2.0)
    idle.bob("body", 0.3, 1).sway("ear_l", 2, 7, 1, 0.1).sway("ear_r", 2, -7, 1, 0.1).sway("tail", 1, 14, 2).sway("wing_l", 2, 8, 4).sway("wing_r", 2, -8, 4)
    idle.sway("snout", 0, 3, 2, 0.2)
    anims["idle"] = idle
    walk = Anim(0.6)
    walk.bob("body", 0.7, 2, 0.25).sway("leg_f_l", 0, 34).sway("leg_f_r", 0, -34).sway("leg_b_l", 0, -34).sway("leg_b_r", 0, 34)
    walk.sway("tail", 1, 24, 2).sway("ear_l", 2, 10, 2, 0.3).sway("ear_r", 2, -10, 2, 0.3).sway("body", 2, 3, 1).sway("wing_l", 2, 14, 6).sway("wing_r", 2, -14, 6)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("head", 0, [(0, 0), (0.12, 16), (0.24, -14), (0.5, 0)])
    atk.keys("snout", 0, [(0, 0), (0.24, -10), (0.5, 0)])
    atk.keys("body", 0, [(0, 0), (0.12, -6), (0.24, 6), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
