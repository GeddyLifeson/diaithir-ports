"""Twirlfin: a teal flying fish in a propeller beanie, with cream wing-fins, a swirly tail and a permanent grin."""
import mats
import motifs as M
import toon as T
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "twirlfin"


def make():
    teal = mats.scales((86, 196, 186), size=4.0, tone=0.62, lo=0.45, hi=1.35)
    teal_d = mats.scales((52, 148, 156), size=4.0, tone=0.56, lo=0.45, hi=1.35)
    cream = mats.cel((252, 238, 204), tone=0.68, grad=0.14)
    wingm = mats.cel((250, 232, 190), tone=0.66, grad=0.2)
    redm = mats.cel((228, 78, 70), tone=0.6, grad=0.14)
    redm_d = mats.cel((180, 52, 52), tone=0.55, grad=0.12)
    yellow = mats.cel((252, 214, 80), tone=0.66, grad=0.12)

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            T.eye(f, w / 2 + sx * w * 0.25, h * 0.36, 3.8, 4.4, look=(-sx * 0.2, 0.0))
            T.blush(f, w / 2 + sx * w * 0.40, h * 0.66, 2.4, 1.5)
        T.smile(f, w / 2, h * 0.70, w * 0.5, curve=2.2, wd=1.2)

    def belly(f):
        if f.name == "north":
            f.paint(f.ellipse(f.w / 2, f.h * 0.6, f.w * 0.3, f.h * 0.36), cream.rp, 0.66, 0.4)

    m = Model(NAME)
    body = m.bone("body", [0, 12, 0])
    T.rbox(body, [-4.6, 6.4, -3.0], [9.2, 9.6, 10.0], teal, r=2.8, deco={"north": belly})
    body.cube([-3.6, 6.5, -2.0], [7.2, 0.6, 8.0], cream, hide=("up", "north", "south"))
    head = m.bone("head", [0, 12, -3], "body")
    T.rbox(head, [-5.6, 6.0, -11.0], [11.2, 10.8, 8.4], teal, r=2.6, deco={"north": face})
    fin = m.bone("fin", [0, 16, 2], "body")
    fin.cube([-0.6, 15.8, -1.0], [1.2, 3.6, 6.0], teal_d)
    fin.cube([-0.6, 15.8, 4.2], [1.2, 2.2, 2.2], teal_d)
    tail = m.bone("tail", [0, 11.5, 7], "body")
    T.rbox(tail, [-3.0, 8.2, 6.8], [6.0, 6.8, 4.6], teal_d, r=1.6)
    tf = m.bone("tailfin", [0, 11.5, 11.4], "tail")
    tf.cube([-0.6, 6.8, 11.0], [1.2, 9.4, 4.4], wingm)
    tf.cube([-0.6, 9.4, 14.8], [1.2, 3.6, 2.4], redm_d)
    for s_, sg in (("l", 1), ("r", -1)):
        w = m.bone("wing_" + s_, [sg * 4.6, 13.5, 1.0], "body")
        # a rounded paddle fin built from four tapering slices, thick enough to read from the front
        for x0, x1, d, zc in ((0.0, 3.2, 8.4, 0.2), (3.2, 6.6, 8.0, 0.8), (6.6, 9.6, 6.4, 1.6), (9.6, 11.4, 3.6, 2.4)):
            lo_, hi_ = sorted((sg * (4.6 + x0), sg * (4.6 + x1)))
            w.cube([lo_, 12.9, zc - d / 2], [hi_ - lo_, 1.4, d], wingm, rot=[0, 0, sg * -20], pivot=[sg * 4.6, 13.5, 1.0])
        w.cube([sg * 4.6 + (0 if sg > 0 else -1.4), 12.6, -3.6], [1.4, 1.9, 8.0], cream)
    cap = m.bone("cap", [0, 16.8, -7], "head")
    T.rbox(cap, [-3.6, 16.4, -9.8], [7.2, 2.6, 5.6], redm, r=1.2)
    cap.cube([-3.7, 16.9, -9.9], [7.4, 0.9, 5.8], yellow, hide=("up", "down"))
    prop = m.bone("prop", [0, 19.6, -7.0], "cap")
    prop.cube([-0.8, 19.0, -7.8], [1.6, 1.6, 1.6], yellow)
    prop.cube([-5.4, 20.2, -7.5], [10.8, 0.7, 1.8], redm, rot=[0, 12, 0], pivot=[0, 20.2, -7.0])
    anims = {}
    idle = Anim(2.0)
    idle.bob("body", 0.5, 1).rot("prop", 1, lambda t: 360 * t / 1.0 * 2 / 2 * 1).sway("tailfin", 1, 14, 2).sway("wing_l", 2, 8, 2).sway("wing_r", 2, -8, 2).sway("fin", 0, 4, 2, 0.2)
    anims["idle"] = idle
    walk = Anim(0.8)
    walk.bob("body", 0.8, 2).rot("prop", 1, lambda t: 360 * t / 0.4).sway("tailfin", 1, 22, 2).sway("wing_l", 2, 18, 2).sway("wing_r", 2, -18, 2).sway("body", 2, 3, 1)
    anims["walk"] = walk
    fly = Anim(0.7)
    fly.bob("body", 0.8, 1).rot("prop", 1, lambda t: 360 * t / 0.23).sway("wing_l", 2, 34, 1).sway("wing_r", 2, -34, 1).sway("tailfin", 1, 20, 2).sway("fin", 0, 6, 2, 0.3)
    anims["fly"] = fly
    atk = Anim(0.5, loop=False)
    atk.keys("head", 0, [(0, 0), (0.12, 18), (0.24, -20), (0.5, 0)])
    atk.keys("body", 0, [(0, 0), (0.12, -8), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
